package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import models.LoadTest
import java.net.URI

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def test(roomId: Int, thread: Int, count: Int) = Action { implicit request =>
    val test = new LoadTest(new URI("ws://ws-quiz.herokuapp.com/test/ws/" + roomId), 1)
    new Thread() {
      override def run = test.run(thread, count)
    }.start
    Ok("OK")
  }

  def test2(room: Int, thread: Int, count: Int) = Action { implicit request =>
    for (i <- 1 to room) {
      val url = s"http://ws-quiz-test.herokuapp.com/test?roomId=${i}&thread=${thread}&count=${count}"
       WS.url(url).get()
    }
    Ok("OK")
  }
}