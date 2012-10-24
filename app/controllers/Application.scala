package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object Application extends Controller {

  def init = Action {
    NutrientsStore().initialize
    Ok("Database successfully initialized")
  }

  def shutdown = Action {
    NutrientsStore().shutDown
    Ok("Database successfully shutdown")
  }

  def nutrients = Action {
    Ok("%s Nutrient records found".format(NutrientsStore().getNutrientCount))
  }

  def datasources = Action {
    Ok("%s DataSource records found".format(NutrientsStore().getDataSourceCount))
  }

  def foods = Action {
    Ok("%s Food records found".format(NutrientsStore().getFoodCount))
  }

  def index = Action {
    Ok("index")
    //    Redirect(routes.Application.tasks)
    // ---
    //      Ok(views.html.index(Task.all(), taskForm))
    // ---
    //      implicit request =>
    //      taskForm.bindFromRequest.fold(
    //        errors => BadRequest(views.html.index(Task.all(), errors)),
    //        label => {
    //          Task.create(label)
    //          Redirect(routes.Application.tasks)
    //        })
    // ---
    //      Task.delete(id)
    //      Redirect(routes.Application.tasks)
  }

}