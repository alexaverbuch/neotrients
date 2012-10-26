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
    Food.DESC -> getDescription))
  def toJsonBig = toJson(Map(
    Food.DATASET_ID -> getDatasetId,
    Food.NAME -> getName,
    Food.DESC -> getDescription,
    Food.SCIENTIFIC -> getScientific,
    Food.NUTRIENTS -> toJson(getFirstNutrients(3)).toString))

  private def getFirstNutrients(n: Int) =
    getNutrients.take(n).map(m => toJson(m)).toList
  private def getNutrients: Iterator[Map[String, String]] = {
    val queryResult =
      Neo4j().doCypher("""
        START food=node({id}) 
        MATCH food-[contains:{relType}]->nutrient 
        RETURN nutrient.name AS name, contains.{propName} AS amount""",

        Map("id" -> underlyingNode.getId(),
          "relType" -> RelTypes.FOOD_CONTAINS,
          "propName" -> Food.NUTRIENT_AMOUNT))

    queryResult.map(n => Map(
      "name" -> n("name").toString,
      "amount" -> n("amount").toString))
  }

  override def hashCode = underlyingNode.hashCode
  override def equals(obj: Any) =
    if (obj.isInstanceOf[Food]) {
      val that = obj.asInstanceOf[Food]
      this.underlyingNode.equals(that.underlyingNode)
    } else false

  override def toString = "Food(%s; %s; )".
    format(getDatasetId, getName, toJson(getFirstNutrients(3)).toString)
}