package models

import common._

import org.neo4j.graphdb.Node

class Food(node: Node) {
  private val underlyingNode: Node = node

  def getDatasetId = underlyingNode.getProperty(Food.DATASET_ID)
  def getName = underlyingNode.getProperty(Food.NAME)
  def getDescription = underlyingNode.getProperty(Food.DESC)
  def getScientific = underlyingNode.getProperty(Food.SCIENTIFIC)

  override def hashCode = underlyingNode.hashCode
  override def equals(obj: Any) =
    if (obj.isInstanceOf[Food]) {
      val that = obj.asInstanceOf[Food]
      this.underlyingNode.equals(that.underlyingNode)
    } else false

  //  override def toString = "Food(%s; %s; [%s,...])".format(getDatasetId, getName, nutrients(0))
  override def toString = ???
}

object Food {
  val DATASET_ID = "fd_ds_id"
  val NAME = "fd_name"
  val DESC = "fd_desc"
  val SCIENTIFIC = "fd_sci"

  def all(): List[Food] = ???

  def create(id: String) { ??? }

  def delete(id: String) { ??? }
}