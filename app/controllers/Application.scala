package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json._

import org.neo4j.play.Neo4j

import common._
import models._

object Application extends Controller {

  def home = Action {
    //    Food.create("testid", "testname", "this is just a test", "scientific what?")
    Ok("Welcome to Neotrients!")
  }

  def getFood(name: String) = Action {
    Food.getByName(name) match {
      case Some(food) => Ok(food.toJsonBig)
      case None => NotFound("Food not found: %s".format(name))
    }
  }

  def getFoods(namePrefix: Option[String], dataset: Option[String]) = Action { implicit request =>
    val foods: Iterator[Food] =
      (namePrefix, dataset) match {
        case (None, None) => Food.all
        case (Some(aName), None) => Food.allWithNamePrefix(aName)
        case (None, Some(aDataset)) => Food.allForDataset(aDataset)
        case (Some(aName), Some(aDataset)) => Food.allForDatasetWithNamePrefix(aDataset, aName);
      }

    Ok(toJson(foods.take(20).toList.map(_.toJsonSmall)))
  }

  def init = Action {
    NutrientsStore.initialize
    Ok("Neotrients database successfully initialized")
  }

  def shutdown = Action {
    Neo4j.shutDown
    Ok("Neotrients database successfully shutdown")
  }

  def datasources = Action {
    Ok("Data Sources:\n\n" + DataSource.all.foldLeft("")((acc, ds) => "%s\n%s".format(acc, ds.toString)))
  }

  def nutrients = Action {
    Ok("Nutrients:\n\n" + Nutrient.all.foldLeft("")((acc, ds) => "%s\n%s".format(acc, ds.toString)))
  }

}