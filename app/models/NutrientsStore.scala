package models

import play.api._
import common._
import NodeTypes.NODE_TYPE
import RelTypes._
import org.neo4j.graphdb.Node
import org.neo4j.play.Neo4j
import org.neo4j.graphdb.index.Index

object NutrientsStore {

  private val PATH = "neo4jdb"
  val INDEX_NODES = "nodes"

  def initialize {
    Neo4j.shutDown
    Neo4j.clearDb(PATH)
    Neo4j.createDb(PATH)
    importDataSources
    importNutrients
    importFoods
  }

  //  def createDataSource(id: String, country: String, date: String, name: String, url: String, comment: String) {
  //    Neo4j.withConnection { createDataSource0(id, country, date, name, url, comment) }
  //  }
  private def importDataSources {
    Logger.info("Importing DataSource records...")
    Neo4j.withConnection { DatasetReader.readDataSources.foreach(createDataSource0(_)) }
    Logger.info("DataSources imported")
  }
  private def createDataSource0(ds: DataSourceRecord) {
    createDataSource0(ds.id, ds.country, ds.date, ds.name, ds.url, ds.comment)
  }
  private def createDataSource0(id: String, country: String, date: String, name: String, url: String, comment: String) {
    val dataSourceNode = Neo4j._createNode
    dataSourceNode.setProperty(DataSource.DATASET_ID, id)
    dataSourceNode.setProperty(DataSource.COUNTRY, country)
    dataSourceNode.setProperty(DataSource.DATE, date)
    dataSourceNode.setProperty(DataSource.NAME, name)
    dataSourceNode.setProperty(DataSource.URL, url)
    dataSourceNode.setProperty(DataSource.COMMENT, comment)

    val index: Index[Node] = Neo4j.getIndexManager.forNodes(INDEX_NODES)
    index.add(dataSourceNode, DataSource.DATASET_ID, id)
    index.add(dataSourceNode, NODE_TYPE, NodeTypes.DATASOURCE_NODE_TYPE)
  }

  //  def createNutrient(name: String, unit: String) {
  //    Neo4j.withConnection { createNutrient0(name, unit) }
  //  }
  private def importNutrients {
    Logger.info("Importing Nutrient records...")
    Neo4j.withConnection { DatasetReader.readNutrients.foreach(createNutrient0(_)) }
    Logger.info("Nutrients imported")
  }
  private def createNutrient0(nt: NutrientRecord) {
    createNutrient0(nt.name, nt.unit)
  }
  private def createNutrient0(name: String, unit: String) {
    val nutrientNode = Neo4j._createNode
    nutrientNode.setProperty(Nutrient.NAME, name)
    nutrientNode.setProperty(Nutrient.UNIT, unit)

    val index: Index[Node] = Neo4j.getIndexManager.forNodes(INDEX_NODES)
    index.add(nutrientNode, Nutrient.NAME, name)
    index.add(nutrientNode, NODE_TYPE, NodeTypes.NUTRIENT_NODE_TYPE)
  }

  //  def createFood(datasetId: String, name: String, desc: String, scientific: String) {
  //    Neo4j.withConnection { createFood0(datasetId, name, desc, scientific, Nil) }
  //  }
  def addNutrientsToFood(foodNode: Node, nutrients: List[(String, Double)]) {
    Neo4j.withConnection { addNutrientsToFood(foodNode, nutrients) }
  }
  private def importFoods {
    Logger.info("Importing Food records...")
    Neo4j.withConnection { DatasetReader.readFoods.foreach(createFood0(_)) }
    Logger.info("Foods imported")
  }
  private def createFood0(fd: FoodRecord) {
    createFood0(fd.datasetId, fd.name, fd.desc, fd.scientific, fd.nutrients)
  }
  private def createFood0(datasetId: String, name: String, desc: String, scientific: String, nutrients: List[(String, Double)]) {
    val foodNode = Neo4j._createNode
    foodNode.setProperty(Food.DATASET_ID, datasetId)
    foodNode.setProperty(Food.NAME, name)
    foodNode.setProperty(Food.DESC, desc)
    foodNode.setProperty(Food.SCIENTIFIC, scientific)

    val index: Index[Node] = Neo4j.getIndexManager.forNodes(INDEX_NODES)
    index.add(foodNode, Food.NAME, name)
    index.add(foodNode, Food.DESC, desc)
    index.add(foodNode, NODE_TYPE, NodeTypes.FOOD_NODE_TYPE)

    val dataSourceNode = index.get(DataSource.DATASET_ID, datasetId).getSingle
    foodNode.createRelationshipTo(dataSourceNode, RelTypes.ORIGIN_DATASOURCE)

    addNutrientsToFood0(foodNode, nutrients)
  }
  private def addNutrientsToFood0(foodNode: Node, nutrients: List[(String, Double)]) {
    def connectToNutrient(nutrientName: String, nutrientValue: Double) = {
      val index: Index[Node] = Neo4j.getIndexManager.forNodes(INDEX_NODES)
      // Don't create relationships for no reason
      if (nutrientValue > 0.0) {
        val nutrientNode = index.get(Nutrient.NAME, nutrientName).getSingle
        val rel = foodNode.createRelationshipTo(nutrientNode, RelTypes.FOOD_CONTAINS)
        rel.setProperty(Food.NUTRIENT_AMOUNT, nutrientValue)
      }
    }

    nutrients.foreach { case (name, value) => connectToNutrient(name, value) }
  }

}