package models

import common._

import org.neo4j.graphdb.Node

class Nutrient(node: Node) {
  private val underlyingNode: Node = node

  def getName = underlyingNode.getProperty(Nutrient.NAME)
  def getUnit = underlyingNode.getProperty(Nutrient.UNIT)

  def toJsonSmall = ???
  def toJsonBig = ???

  override def hashCode = underlyingNode.hashCode
  override def equals(obj: Any) =
    if (obj.isInstanceOf[Nutrient]) {
      val that = obj.asInstanceOf[Nutrient]
      this.underlyingNode.equals(that.underlyingNode)
    } else false

  override def toString = "Nutrient(%s; %s)".format(getName, getUnit)
}

object Nutrient {
  val NAME = "nu_name"
  val UNIT = "nu_unit"

  def all(): List[Nutrient] = ???

  def create(id: String) { ??? }

  def delete(id: String) { ??? }
}