package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import org.neo4j.play.Neo4j

import models._

object Application extends Controller {

  def home = Action {
    Ok("Welcome to Neotrients!")
  }

  def init = Action {
    NutrientsStore.initialize
    Ok("Neotrients database successfully initialized")
  }

  def shutdown = Action {
    Neo4j().shutDown
    Ok("Neotrients database successfully shutdown")
  }

  def datasources = Action {
    Ok("Data Sources:\n\n" + DataSource.all.foldLeft("")((acc, ds) => "%s\n%s".format(acc, ds.toString)))
  }

  def nutrients = Action {
    Ok("Nutrients:\n\n" + Nutrient.all.foldLeft("")((acc, ds) => "%s\n%s".format(acc, ds.toString)))
  }

  def foods = Action {
    Ok("Food:\n\n" + Food.all.take(50).foldLeft("")((acc, ds) => "%s\n%s".format(acc, ds.toString)))
  }

}