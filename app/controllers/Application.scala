package controllers

import play.api._
import play.api.mvc._
import models.LoadTest
import java.net.URI

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def test = Action { implicit request =>
    val test = new LoadTest(new URI("ws://127.0.0.1:9000/test/ws/2"), 1)
    test.run(10, 10)
    Ok("OK")
  }

}