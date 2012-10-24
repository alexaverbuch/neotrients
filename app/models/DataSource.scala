package models

import common._
import RelTypes._

import play.api.db._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.Json._

import org.neo4j.graphdb.Node

class DataSource(personNode: Node) {
  private val underlyingNode: Node = personNode

  def getDatasetId: String = underlyingNode.getProperty(DataSource.DATASET_ID).asInstanceOf[String]
  def getCountry: String = underlyingNode.getProperty(DataSource.COUNTRY).asInstanceOf[String]
  def getDate: String = underlyingNode.getProperty(DataSource.DATE).asInstanceOf[String]
  def getName: String = underlyingNode.getProperty(DataSource.NAME).asInstanceOf[String]
  def getUrl: String = underlyingNode.getProperty(DataSource.URL).asInstanceOf[String]
  def getComment: String = underlyingNode.getProperty(DataSource.COMMENT).asInstanceOf[String]

  def toJsonSmall = toJson(Map(
    DataSource.COUNTRY -> getCountry,
    DataSource.NAME -> getName))
  def toJsonBig = toJson(Map(
    DataSource.DATASET_ID -> getDatasetId,
    DataSource.COUNTRY -> getCountry,
    DataSource.DATE -> getDate,
    DataSource.NAME -> getName,
    DataSource.URL -> getUrl,
    DataSource.COMMENT -> getComment))

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