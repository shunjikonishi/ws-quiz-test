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
      client.close
    } else {
      Akka.system.scheduler.scheduleOnce(5 seconds) {
        execute(client, count + 1, limit)
      }
    }
  }
}

class LoadTestThread(uri: URI, userId: Int, threadId: Int, count: Int) extends Thread {
  
  override def run: Unit = {
    val client = new LoadTestWebSocket(uri, threadId)
    try {
      client.connectBlocking
      for (i <- 1 to count) {
        val msg = s"""{
          "id" : $i,
          "command" : "tweet",
          "data" : {
            "userId" : $userId,
            "msg" : "test $threadId - $i",
            "twitter" : false
          }
        }"""
        client.send(msg)
        Thread.sleep(5000)
      }
    } finally {
      client.close
    }
  }
}

class LoadTestWebSocket(uri: URI, val id: Int) extends WebSocketClient(uri) {
  
  def onOpen(sh: ServerHandshake): Unit = {
    Logger.info(s"onOpen: $id, $uri")
  }
  
  def onMessage(message: String): Unit = {
    Logger.info(s"onMessage: $id, $message")
  }
  
  def onClose(code: Int, reason: String, remote: Boolean): Unit = {
    Logger.info(s"onClose: $id, $code, $reason, $remote")
  }
  
  def onError(ex: Exception): Unit = {
    Logger.error(s"onError: $id, $ex", ex)
  }

}
