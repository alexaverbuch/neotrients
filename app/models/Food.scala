package models

import scala.collection.JavaConversions._
import common._
import NodeTypes.NODE_TYPE
import play.api.libs.json.Json._
import org.neo4j.graphdb.Node
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.cypher.ExecutionResult
import org.neo4j.play.Neo4j

import play.api._

object Food {
  val DATASET_ID = "fd_ds_id"
  val NAME = "fd_name"
  val DESC = "fd_desc"
  val SCIENTIFIC = "fd_sci"

  val NUTRIENT_AMOUNT = "fd_nt_amnt"

  val NUTRIENTS = "fd_nts"

  def all: Iterator[Food] =
    Neo4j().getNodesByProperty(NODE_TYPE, NodeTypes.FOOD_NODE_TYPE).map(new Food(_))

  def count: Int =
    IteratorUtil.count(Neo4j().getNodesByProperty(NODE_TYPE, NodeTypes.FOOD_NODE_TYPE))

  def getByName(name: String): Food =
    new Food(Neo4j().getNodeByProperty(Nutrient.NAME, name))

  def create(datasetId: String, name: String, desc: String, scientific: String) {
    NutrientsStore.createFood(datasetId, name, desc, scientific)
  }

}

class Food(node: Node) {
  private val PREVIEW = 2
  private val underlyingNode: Node = node

  def getDatasetId: String = underlyingNode.getProperty(Food.DATASET_ID).asInstanceOf[String]
  def getName: String = underlyingNode.getProperty(Food.NAME).asInstanceOf[String]
  def getDescription: String = underlyingNode.getProperty(Food.DESC).asInstanceOf[String]
  def getScientific: String = underlyingNode.getProperty(Food.SCIENTIFIC).asInstanceOf[String]

  //  def getDataSource: DataSource = ???
  //  def getNutrients: Iterator[Nutrient] = ???
  //  def addNutrients(nutrients: List[Nutrient]) {
  //    NutrientsStore.createFood(name, unit)
  //  }

  def delete(id: String) {
    Neo4j().withConnection {
      underlyingNode.getRelationships.foreach(_.delete)
      underlyingNode.delete
    }
  }

  def toJsonSmall = toJson(Map(
    Food.NAME -> getName,
    Food.DESC -> getDescription,
    Food.NUTRIENTS -> toJson(getFirstNutrientValues(PREVIEW).map(m => toJson(m)).toList).toString))
  def toJsonBig = toJson(Map(
    Food.DATASET_ID -> getDatasetId,
    Food.NAME -> getName,
    Food.DESC -> getDescription,
    Food.SCIENTIFIC -> getScientific,
    Food.NUTRIENTS -> toJson(getNutrientValues.map(m => toJson(m)).toList).toString))

  private def getFirstNutrientValues(n: Int): Iterator[Map[String, String]] =
    getNutrientValues.take(n)
  private def getNutrientValues: Iterator[Map[String, String]] = {
    val queryResult =
      //      Neo4j().doCypher("""
      //              START food=node({id}) 
      //              MATCH food-[contains:{relType}]->nutrient 
      //              RETURN nutrient.{nutrientName} AS name, contains.{foodAmount} AS amount""",
      //        Map(
      //          "id" -> underlyingNode.getId(),
      //          "relType" -> RelTypes.FOOD_CONTAINS,
      //          "nutrientName" -> Nutrient.NAME,
      //          "foodAmount" -> Food.NUTRIENT_AMOUNT))
      Neo4j().doCypher("""
            START food=node(%s) 
            MATCH food-[contains:%s]->nutrient 
            RETURN nutrient.%s AS name, contains.%s AS amount""".
        format(underlyingNode.getId(), RelTypes.FOOD_CONTAINS, Nutrient.NAME, Food.NUTRIENT_AMOUNT))

    queryResult.map(n => Map(
      "name" -> n("name").toString,
      "amount" -> n("amount").toString))
  }

//  private def getNutrients: Iterator[Nutrient] = {
//    val queryResult =
//      Neo4j().doCypher("""
//            START food=node(%s) 
//            MATCH food-[:%s]->nutrient 
//            RETURN nutrient AS n""".
//        format(underlyingNode.getId(), RelTypes.FOOD_CONTAINS))
//
//    queryResult.map(n => new Nutrient(n))
//  }

  override def hashCode = underlyingNode.hashCode
  override def equals(obj: Any) =
    if (obj.isInstanceOf[Food]) {
      val that = obj.asInstanceOf[Food]
      this.underlyingNode.equals(that.underlyingNode)
    } else false

  override def toString = "Food(%s; %s; %s)".
    format(getDatasetId, getName, toJson(getFirstNutrientValues(PREVIEW).map(m => toJson(m)).toList).toString)

}