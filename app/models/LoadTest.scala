package models;

import play.Logger
import java.net.URI
import play.api.libs.concurrent.Akka
import scala.concurrent.duration.DurationInt
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.ning.http.client._
import com.ning.http.client.websocket._
import java.io.IOException

object LoadTest {
    val httpClient = new AsyncHttpClient()
}
class LoadTest(uri: URI, userId: Int) {
  
  def run(threads: Int, count: Int) {
    for (i <- 1 to threads) {
      val client = new LoadTestWebSocket(LoadTest.httpClient, uri, i)
      Akka.system.scheduler.scheduleOnce(5 seconds) {
        execute(client, 1, count)
      }
      Thread.sleep(1000)
    }
  }
  
  def execute(client: LoadTestWebSocket, count: Int, limit: Int): Unit = {
    val msg = s"""{
      "id" : $count,
      "command" : "tweet",
      "data" : {
        "userId" : $userId,
        "msg" : "test ${client.id} - $count",
        "twitter" : false
      }
    }"""
    client.send(msg)
    if (count == limit) {
      if (client.id > 2) {
        client.close
      }
    } else {
      Akka.system.scheduler.scheduleOnce(5 seconds) {
        execute(client, count + 1, limit)
      }
    }
  }
}

class LoadTestWebSocket(httpClient: AsyncHttpClient, uri: URI, val id: Int) {

  val websocket = {
    val l = new MyListener()
    def createWebSocket(retryCount: Int): WebSocket = {
      try {
        httpClient.prepareGet(uri.toString).execute(
          new WebSocketUpgradeHandler.Builder().addWebSocketListener(l).build
        ).get
      } catch {
        case e: Exception =>
          if (retryCount > 0) {
              Logger.info(s"Retry connect: $uri, $id")
              createWebSocket(retryCount - 1)
            } else {
              throw e
            }
      }
    }
    createWebSocket(5)
  }
  
  var chatCount = 0

  def send(msg: String):Unit = {
    try {
      websocket.sendTextMessage(msg)
    } catch {
      case e: Exception =>
        Logger.error(s"Send error: $uri, $id")
        e.printStackTrace
    }
  }

  def close = websocket.close

  class MyListener extends WebSocketTextListener {

    def onMessage(message: String): Unit = {
      if (message.indexOf("\"chat\"") != -1) {
        chatCount += 1
      }
    }

    def onOpen(ws: WebSocket): Unit = {
      Logger.info(s"onOpen: $id, $uri")
    }

    def onFragment(fragment: String, last: Boolean): Unit = {}

    def onClose(ws: WebSocket): Unit = {
      Logger.info(s"onClose: $uri, $id")
      if (id <= 2) {
        Logger.info(s"Chat count: $uri, $id = $chatCount")
      }
    }
    
    def onError(ex: Throwable): Unit = {
      Logger.error(s"onError: $id, $ex", ex)
    }
  }
}
