package main

// Useful links:
// http://blog.fakod.eu/2010/10/04/neo4j-example-written-in-scala/

import common._
import org.neo4j.graphdb._
import org.neo4j.kernel.impl.util.FileUtils
import java.io.File
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.index.IndexManager
import org.neo4j.graphdb.index.Index

object RelTypes extends Enumeration {
  type RelTypes = Value
  val FOOD_CONTAINS, ORIGIN_DATASOURCE = Value

  implicit def conv(rt: RelTypes) = new RelationshipType() { def name = rt.toString }
}
import RelTypes._

object NeotrientsDb {
  def main(args: Array[String]) = {
    val neotrientsDb = new NeotrientsDb("neotrientsdb")
    neotrientsDb.addDataSources(NutrientReader.readDataSources)
    neotrientsDb.addNutrients(NutrientReader.readNutrients)
    neotrientsDb.addFoods(NutrientReader.readFoods)
  }
}

class NeotrientsDb(path: String) {

  private val (graphDb, nodeIndex) = createDb(path)

  // Create and return wrapper classes for the Nodes and Relationships?
  def getDataSource(datasourceId: String) = ???

  def getNutrient(nutrientName: String) = ???

  def getFood(foodName: String) = ???

  def addDataSources(dss: Iterator[DataSource]) = addNutritionRecords(dss, addDataSource0)
  def addDataSource(ds: DataSource) = addNutritionRecords(List(ds).iterator, addDataSource0)
  private def addDataSource0(ds: DataSource) = {
    val dataSourceNode = graphDb.createNode()
    dataSourceNode.setProperty(DataSource.DATASET_ID, ds.id)
    dataSourceNode.setProperty(DataSource.COUNTRY, ds.country)
    dataSourceNode.setProperty(DataSource.DATE, ds.date)
    dataSourceNode.setProperty(DataSource.NAME, ds.name)
    dataSourceNode.setProperty(DataSource.URL, ds.url)
    dataSourceNode.setProperty(DataSource.COMMENT, ds.comment)

    nodeIndex.add(dataSourceNode, DataSource.DATASET_ID, ds.id)
  }

  def addNutrients(nts: Iterator[Nutrient]) = addNutritionRecords(nts, addNutrient0)
  def addNutrient(nt: Nutrient) = addNutritionRecords(List(nt).iterator, addNutrient0)
  private def addNutrient0(nt: Nutrient) = {
    val nutrientNode = graphDb.createNode()
    nutrientNode.setProperty(Nutrient.NAME, nt.name)
    nutrientNode.setProperty(Nutrient.UNIT, nt.unit)

    nodeIndex.add(nutrientNode, Nutrient.NAME, nt.name)
  }

  def addFoods(fds: Iterator[Food]) = addNutritionRecords(fds, addFood0)
  def addFood(fd: Food) = addNutritionRecords(List(fd).iterator, addFood0)
  private def addFood0(fd: Food) = {
    val foodNode = graphDb.createNode()
    foodNode.setProperty(Food.DATASET_ID, fd.datasetId)
    foodNode.setProperty(Food.NAME, fd.name)
    foodNode.setProperty(Food.DESC, fd.desc)
    foodNode.setProperty(Food.SCIENTIFIC, fd.scientific)

    nodeIndex.add(foodNode, Food.NAME, fd.name)
    nodeIndex.add(foodNode, Food.DESC, fd.desc)

    val dataSourceNode = nodeIndex.get(DataSource.DATASET_ID, fd.datasetId).getSingle()
    foodNode.createRelationshipTo(dataSourceNode, ORIGIN_DATASOURCE)

    def connectToNutrient(nName: String, nValue: Double) = {
      // Don't create relationships for no reason
      if (nValue > 0.0) {
        val nutrientNode = nodeIndex.get(Nutrient.NAME, nName).getSingle()
        val rel = foodNode.createRelationshipTo(nutrientNode, FOOD_CONTAINS)
        rel.setProperty("amount", nValue)
      }
    }

    fd.nutrients.foreach(nutrient => nutrient match {
      case (nName, nValue) => connectToNutrient(nName, nValue)
    })
  }

  private def addNutritionRecords[T <: NutritionRecord](nrs: Iterator[T], f: T => Unit) = {
    val tx = graphDb.beginTx()
    try {
      nrs.foreach(f(_))
      tx.success()
    } finally {
      tx.finish()
    }
  }

  def createDb(path: String): (GraphDatabaseService, Index[Node]) = {
    clearDb(path)

    // What should I put in here?
    val config = new java.util.HashMap[String, String]
    config.put("neostore.nodestore.db.mapped_memory", "10M")
    config.put("string_block_size", "60")
    config.put("array_block_size", "300")

    val gdb: GraphDatabaseService = new GraphDatabaseFactory().
      newEmbeddedDatabaseBuilder(path).setConfig(config).newGraphDatabase
    val nIndex: Index[Node] = gdb.index.forNodes("nodes")

    registerShutdownHook(gdb)

    (gdb, nIndex)
  }

  def shutDown(gdb: GraphDatabaseService): Unit = {
    println
    println("Shutting down database...")
    gdb.shutdown()
  }

  private def clearDb(path: String): Unit = FileUtils.deleteRecursively(new File(path))

  private def registerShutdownHook(gdb: GraphDatabaseService): Unit =
    Runtime.getRuntime.addShutdownHook(new Thread { override def run = gdb.shutdown })
}