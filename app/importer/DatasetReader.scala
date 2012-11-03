package models

import scala.io.Source

object DatasetReader {
  val DATA_SOURCES_PATH = "public/data/Data-Sources.csv"
  val NUTRIENTS_PATH = "public/data/Nutrients-Standardized.csv"
  val FOODS_PATH = "public/data/Foods-Standardized.csv"

  def readDataSources: Iterator[DataSourceRecord] =
    Source.fromFile(DATA_SOURCES_PATH).getLines.drop(1).map(parseDataSource(_))
  def readNutrients: Iterator[NutrientRecord] =
    Source.fromFile(NUTRIENTS_PATH).getLines.drop(1).map(parseNutrient(_))
  def readFoods: Iterator[FoodRecord] = {
    val nutrientNamesList = readNutrients.map(_.name).toList
    Source.fromFile(FOODS_PATH).getLines.drop(1).map(parseFood(_, nutrientNamesList))
  }

  private def parseDataSource(line: String): DataSourceRecord = line.split(";", -1) match {
    case Array(id, country, date, name, url, comment) =>
      new DataSourceRecord(id, country, date, name, url, comment)
    case _ => throw new Exception("Invalid DataSource CSV entry: " + line)
  }
  private def parseNutrient(line: String): NutrientRecord = line.split(";", -1) match {
    case Array(name, unit) => new NutrientRecord(name, unit)
    case _ => throw new Error("Invalid Nutrient CSV entry: " + line)
  }
  private def parseFood(line: String, nutrientNames: List[String]): FoodRecord = line.split(";", -1).toList match {
    case datasetId :: name :: desc :: scientific :: nutrientStringVals =>
      val nutrientNumberVals = nutrientStringVals.map(s => if (s.trim.isEmpty) 0 else s.toDouble)
      new FoodRecord(datasetId, name, desc, scientific, nutrientNames zip nutrientNumberVals)
    case _ => throw new Error("Invalid Food CSV entry: " + line)
  }
}

abstract class NutritionRecord

class DataSourceRecord(
  val id: String,
  val country: String,
  val date: String,
  val name: String,
  val url: String,
  val comment: String) extends NutritionRecord

class NutrientRecord(
  val name: String,
  val unit: String) extends NutritionRecord

class FoodRecord(
  val datasetId: String,
  val name: String,
  val desc: String,
  val scientific: String,
  val nutrients: List[(String, Double)]) extends NutritionRecord