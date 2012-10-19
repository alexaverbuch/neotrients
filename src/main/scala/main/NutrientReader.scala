package main

import common._
import scala.io.Source

object NutrientReader {
  def readDataSources: Iterator[DataSource] =
    Source.fromFile("data/Data-Sources.csv").getLines.drop(1).map(new DataSource(_))

  def readNutrients: Iterator[Nutrient] =
    Source.fromFile("data/Nutrients-Standardized.csv").getLines.drop(1).map(new Nutrient(_))

  def readFoods: Iterator[Food] = {
    val nutrientNamesList = readNutrients.map(_.name).toList
    Source.fromFile("data/Foods-Standardized.csv").getLines.drop(1).map(new Food(_, nutrientNamesList))
  }
}

abstract class NutritionRecord

object DataSource extends NutritionRecord {
  val DATASET_ID = "ds_id"
  val COUNTRY = "ds_country"
  val DATE = "ds_date"
  val NAME = "ds_name"
  val URL = "ds_url"
  val COMMENT = "ds_comment"
}
class DataSource(line: String) extends NutritionRecord {
  val (id: String, country: String, date: String, name: String, url: String, comment: String) =
    line.split(";", -1) match {
      case Array(id, country, date, name, url, comment) =>
        (id, country, date, name, url, comment)
      case _ => new Error("Invalid CSV entry: " + line)
    }
  override def toString = "DataSource(%s; %s; %s; %s)".format(id, country, date, name)
}

object Nutrient extends NutritionRecord {
  val NAME = "nu_name"
  val UNIT = "nu_unit"
}
class Nutrient(line: String) extends NutritionRecord {
  val (name: String, unit: String) =
    line.split(";", -1) match {
      case Array(name, unit) => (name, unit)
      case _ => new Error("Invalid CSV entry: " + line)
    }
  override def toString = "Nutrient(%s; %s)".format(name, unit)
}

object Food extends NutritionRecord {
  val DATASET_ID = "fd_ds_id"
  val NAME = "fd_name"
  val DESC = "fd_desc"
  val SCIENTIFIC = "fd_sci"
}
class Food(line: String, nutrientNames: List[String]) extends NutritionRecord {
  val (datasetId: String, name: String, desc: String, scientific: String, nutrients: List[(String, Double)]) =
    line.split(";", -1).toList match {
      case datasetId :: name :: desc :: scientific :: nutrientStringVals =>
        val nutrientNumberVals = nutrientStringVals.map(
          (s: String) => if (s.trim.isEmpty) 0 else s.toDouble)
        (datasetId, name, desc, scientific, nutrientNames zip nutrientNumberVals)
      case _ => new Error("Invalid CSV entry: " + line)
    }
  override def toString = "Food(%s; %s; [%s,...])".format(datasetId, name, nutrients(0))
}
