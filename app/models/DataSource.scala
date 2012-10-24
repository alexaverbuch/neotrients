package models

import common._
import RelTypes._

import play.api.db._
import play.api.Play.current

import org.neo4j.graphdb.Node

class DataSource(personNode: Node) {
  private val underlyingNode: Node = personNode

  def getDatasetId = underlyingNode.getProperty(DataSource.DATASET_ID)
  def getCountry = underlyingNode.getProperty(DataSource.COUNTRY)
  def getDate = underlyingNode.getProperty(DataSource.DATE)
  def getName = underlyingNode.getProperty(DataSource.NAME)
  def getUrl = underlyingNode.getProperty(DataSource.URL)
  def getComment = underlyingNode.getProperty(DataSource.COMMENT)

  override def hashCode = underlyingNode.hashCode
  override def equals(obj: Any) =
    if (obj.isInstanceOf[DataSource]) {
      val that = obj.asInstanceOf[DataSource]
      this.underlyingNode.equals(that.underlyingNode)
    } else false

  override def toString = "DataSource(%s; %s; %s; %s)".
    format(getDatasetId, getCountry, getDate, getName)
}

object DataSource {
  val DATASET_ID = "ds_id"
  val COUNTRY = "ds_country"
  val DATE = "ds_date"
  val NAME = "ds_name"
  val URL = "ds_url"
  val COMMENT = "ds_comment"

  def all(): List[DataSource] = ???

  def create(id: String) { ??? }

  def delete(id: String) { ??? }
}