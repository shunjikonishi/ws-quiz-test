package models;

import play.Logger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import play.api.libs.concurrent.Akka
import scala.concurrent.duration.DurationInt
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

class LoadTest(uri: URI, userId: Int) {
  
  def run(threads: Int, count: Int) {
    for (i <- 1 to threads) {
      val client = new LoadTestWebSocket(uri, i)
      client.connectBlocking
      Akka.system.scheduler.scheduleOnce(5 seconds) {
        execute(client, 1, count)
      }
      Thread.sleep(3000)
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

class LoadTestWebSocket(uri: URI, val id: Int) extends WebSocketClient(uri) {
  
  var chatCount = 0

  def onOpen(sh: ServerHandshake): Unit = {
    Logger.info(s"onOpen: $id, $uri")
  }
  
  def onMessage(message: String): Unit = {
    if (id <= 2 && message.indexOf("\"chat\"") != -1) {
      chatCount += 1
    }
  }
  
  def onClose(code: Int, reason: String, remote: Boolean): Unit = {
    Logger.info(s"onClose: $id, $code, $reason, $remote")
    if (id <= 2) {
      Logger.info(s"Chat count: $uri, $id = $chatCount")
    }
  }
  
  def onError(ex: Exception): Unit = {
    Logger.error(s"onError: $id, $ex", ex)
  }

}
