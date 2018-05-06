package controllers

import javax.inject._

import play.api.mvc._

@Singleton
class HomeController @Inject()(cc: ControllerComponents)(implicit assetsFinder: AssetsFinder)
    extends AbstractController(cc) {

  /**
    * Display the home page of the application
    */
  def index = Action {
    Ok(views.html.index())
  }

}
