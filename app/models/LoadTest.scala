package models;

import play.Logger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class LoadTest(uri: URI, userId: Int) {
  
  def run(threads: Int, count: Int) {
    for (i <- 1 to threads) {
      val t = new LoadTestThread(uri, userId, i, count)
      t.start
      Thread.sleep(3000)
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

class LoadTestWebSocket(uri: URI, id: Int) extends WebSocketClient(uri) {
  
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
