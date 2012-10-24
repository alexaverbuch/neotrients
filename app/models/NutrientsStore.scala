package models

// Useful links:
// http://blog.fakod.eu/2010/10/04/neo4j-example-written-in-scala/

import scala.collection.JavaConversions._

import org.neo4j.graphdb._
import org.neo4j.kernel.impl.util.FileUtils
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.index.IndexManager
import org.neo4j.graphdb.index.Index
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.graphdb.index.IndexHits
import org.neo4j.cypher.ExecutionEngine
//import org.neo4j.cypher.ExecutionResult

import java.io.File

import common._
import NodeTypes.NODE_TYPE

import play.api._

object NutrientsStore {
  private val PATH = "nutrients_db"
  private var nutrientsStore: NutrientsStore = null
  def apply() = Option(nutrientsStore) match {
    case Some(nutrientsStore) =>
      nutrientsStore
    case None =>
      nutrientsStore = new NutrientsStore(PATH)
      nutrientsStore
  }
}

class NutrientsStore(private val path: String) {
  private var graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path).newGraphDatabase
  private var nodeIndex = graphDb.index.forNodes("nodes")
  private var engine: ExecutionEngine = new ExecutionEngine(graphDb);

  def getDataSourceById(datasetId: String) =
    new DataSource(nodeIndex.get(DataSource.DATASET_ID, datasetId).getSingle)
  def getAllDataSources: Iterator[DataSource] =
    nodeIndex.get(NODE_TYPE, NodeTypes.DATASOURCE_NODE_TYPE).iterator.map(new DataSource(_))
  def getDataSourceCount =
    IteratorUtil.count(nodeIndex.get(NODE_TYPE, NodeTypes.DATASOURCE_NODE_TYPE).iterator)

  def getNutrientByName(nutrientName: String) =
    new Nutrient(nodeIndex.get(Nutrient.NAME, nutrientName).getSingle)
  def getAllNutrients: Iterator[Nutrient] =
    nodeIndex.get(NODE_TYPE, NodeTypes.NUTRIENT_NODE_TYPE).iterator.map(new Nutrient(_))
  def getNutrientCount =
    IteratorUtil.count(nodeIndex.get(NODE_TYPE, NodeTypes.NUTRIENT_NODE_TYPE).iterator)

  def getFoodByName(foodName: String) =
    new Food(nodeIndex.get(Food.NAME, foodName).getSingle)
  def getAllFoods: Iterator[Food] =
    nodeIndex.get(NODE_TYPE, NodeTypes.FOOD_NODE_TYPE).iterator.map(new Food(_))
  def getFoodCount =
    IteratorUtil.count(nodeIndex.get(NODE_TYPE, NodeTypes.FOOD_NODE_TYPE).iterator)

  def initialize {
    shutDown
    clearDb
    createDb
    importDataSources
    importNutrients
    importFoods
  }

  def shutDown {
    try {
      Logger.info("Shutting down database...")
      graphDb.shutdown
    } finally {
      Logger.info("Database shutdown complete")
    }
  }

  private def createDb {
    Logger.info("Creating database instance...")
    val config = Map[String, String]()
    config.put("neostore.nodestore.db.mapped_memory", "10M")
    config.put("string_block_size", "60")
    config.put("array_block_size", "300")
    graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path).setConfig(config).newGraphDatabase
    nodeIndex = graphDb.index.forNodes("nodes")
    registerShutdownHook
    Logger.info("Database created")
  }

  private def clearDb {
    Logger.info("Deleting database directory...")
    FileUtils.deleteRecursively(new File(path))
    Logger.info("Database deleted")
  }

  private def registerShutdownHook {
    Runtime.getRuntime.addShutdownHook(new Thread { override def run = graphDb.shutdown })
  }

  private def importDataSources {
    Logger.info("Importing DataSource records...")
    addNutritionRecords(DatasetReader.readDataSources, createDataSource0)
    Logger.info("DataSources imported")
  }
  private def createDataSource0(datasource: DataSourceRecord) {
    val dataSourceNode = graphDb.createNode
    dataSourceNode.setProperty(DataSource.DATASET_ID, datasource.id)
    dataSourceNode.setProperty(DataSource.COUNTRY, datasource.country)
    dataSourceNode.setProperty(DataSource.DATE, datasource.date)
    dataSourceNode.setProperty(DataSource.NAME, datasource.name)
    dataSourceNode.setProperty(DataSource.URL, datasource.url)
    dataSourceNode.setProperty(DataSource.COMMENT, datasource.comment)

    nodeIndex.add(dataSourceNode, DataSource.DATASET_ID, datasource.id)
    nodeIndex.add(dataSourceNode, NODE_TYPE, NodeTypes.DATASOURCE_NODE_TYPE)
  }

  private def importNutrients {
    Logger.info("Importing Nutrient records...")
    addNutritionRecords(DatasetReader.readNutrients, createNutrient0)
    Logger.info("Nutrients imported")
  }
  private def createNutrient0(nutrient: NutrientRecord) {
    val nutrientNode = graphDb.createNode
    nutrientNode.setProperty(Nutrient.NAME, nutrient.name)
    nutrientNode.setProperty(Nutrient.UNIT, nutrient.unit)

    nodeIndex.add(nutrientNode, Nutrient.NAME, nutrient.name)
    nodeIndex.add(nutrientNode, NODE_TYPE, NodeTypes.NUTRIENT_NODE_TYPE)
  }

  private def importFoods {
    Logger.info("Importing Food records...")
    addNutritionRecords(DatasetReader.readFoods, createFood0)
    Logger.info("Foods imported")
  }
  private def createFood0(food: FoodRecord) {
    val foodNode = graphDb.createNode
    foodNode.setProperty(Food.DATASET_ID, food.datasetId)
    foodNode.setProperty(Food.NAME, food.name)
    foodNode.setProperty(Food.DESC, food.desc)
    foodNode.setProperty(Food.SCIENTIFIC, food.scientific)

    nodeIndex.add(foodNode, Food.NAME, food.name)
    nodeIndex.add(foodNode, Food.DESC, food.desc)
    nodeIndex.add(foodNode, NODE_TYPE, NodeTypes.FOOD_NODE_TYPE)

    val dataSourceNode = nodeIndex.get(DataSource.DATASET_ID, food.datasetId).getSingle
    foodNode.createRelationshipTo(dataSourceNode, RelTypes.ORIGIN_DATASOURCE)

    def connectToNutrient(nutrientName: String, nutrientValue: Double) = {
      // Don't create relationships for no reason
      if (nutrientValue > 0.0) {
        val nutrientNode = nodeIndex.get(Nutrient.NAME, nutrientName).getSingle
        val rel = foodNode.createRelationshipTo(nutrientNode, RelTypes.FOOD_CONTAINS)
        rel.setProperty("amount", nutrientValue)
      }
    }

    food.nutrients.foreach { case (name, value) => connectToNutrient(name, value) }
  }

  private def addNutritionRecords[T <: NutritionRecord](nrs: Iterator[T], f: T => Unit) {
    val tx = graphDb.beginTx
    try {
      nrs.foreach(f(_))
      tx.success
    } finally {
      tx.finish
    }
  }
}