package models

import scala.collection.JavaConversions._
import common._
import RelTypes._
import NodeTypes.NODE_TYPE
import play.api.libs.json.Json._
import org.neo4j.graphdb.Node
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.play.Neo4j
import org.neo4j.graphdb.index.Index

object DataSource {
  val DATASET_ID = "ds_id"
  val COUNTRY = "ds_country"
  val DATE = "ds_date"
  val NAME = "ds_name"
  val URL = "ds_url"
  val COMMENT = "ds_comment"

  def all: Iterator[DataSource] = {
    val index: Index[Node] = Neo4j.getIndexManager.forNodes(NutrientsStore.INDEX_NODES)
    index.get(NODE_TYPE, NodeTypes.DATASOURCE_NODE_TYPE).iterator.map(new DataSource(_))
  }

  def count: Int = IteratorUtil.count(all)

  def getById(id: String): DataSource = {
    val index: Index[Node] = Neo4j.getIndexManager.forNodes(NutrientsStore.INDEX_NODES)
    new DataSource(index.get(DataSource.DATASET_ID, id).getSingle)
  }

  // TODO
  //  def create(id: String, country: String, date: String, name: String, url: String, comment: String) {
  //    NutrientsStore.createDataSource(id, country, date, name, url, comment)
  //  }

}

class DataSource(personNode: Node) {
  private val underlyingNode: Node = personNode

  def getDatasetId: String = underlyingNode.getProperty(DataSource.DATASET_ID).asInstanceOf[String]
  def getCountry: String = underlyingNode.getProperty(DataSource.COUNTRY).asInstanceOf[String]
  def getDate: String = underlyingNode.getProperty(DataSource.DATE).asInstanceOf[String]
  def getName: String = underlyingNode.getProperty(DataSource.NAME).asInstanceOf[String]
  def getUrl: String = underlyingNode.getProperty(DataSource.URL).asInstanceOf[String]
  def getComment: String = underlyingNode.getProperty(DataSource.COMMENT).asInstanceOf[String]

  // TODO
  //  def delete {
  //    Neo4j.withConnection {
  //      underlyingNode.getRelationships.foreach(_.delete)
  //      underlyingNode.delete
  //    }
  //  }

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