package models

import scala.collection.JavaConversions._
import common._
import RelTypes._
import NodeTypes.NODE_TYPE
import org.neo4j.graphdb.Node
import org.neo4j.helpers.collection.IteratorUtil
import play.api.libs.json.Json._
import org.neo4j.play.Neo4j

object Nutrient {
  val NAME = "nu_name"
  val UNIT = "nu_unit"

  def all: Iterator[Nutrient] =
    Neo4j().getNodesByProperty(NODE_TYPE, NodeTypes.NUTRIENT_NODE_TYPE).map(new Nutrient(_))

  def count: Int =
    IteratorUtil.count(Neo4j().getNodesByProperty(NODE_TYPE, NodeTypes.NUTRIENT_NODE_TYPE))

  def getByName(name: String): Nutrient =
    new Nutrient(Neo4j().getNodeByProperty(Nutrient.NAME, name))

  def create(name: String, unit: String) {
    NutrientsStore.createNutrient(name, unit)
  }
}

class Nutrient(node: Node) {
  private val underlyingNode: Node = node

  def getName: String = underlyingNode.getProperty(Nutrient.NAME).asInstanceOf[String]
  def getUnit: String = underlyingNode.getProperty(Nutrient.UNIT).asInstanceOf[String]

  def delete {
    Neo4j().withConnection {
      underlyingNode.getRelationships.foreach(_.delete)
      underlyingNode.delete
    }
  }

  def toJsonSmall = toJson(Map(
    Nutrient.NAME -> getName,
    Nutrient.UNIT -> getUnit))
  def toJsonBig = toJson(Map(
    Nutrient.NAME -> getName,
    Nutrient.UNIT -> getUnit))

  override def hashCode = underlyingNode.hashCode
  override def equals(obj: Any) =
    if (obj.isInstanceOf[Nutrient]) {
      val that = obj.asInstanceOf[Nutrient]
      this.underlyingNode.equals(that.underlyingNode)
    } else false

  override def toString = "Nutrient(%s; %s)".format(getName, getUnit)
}