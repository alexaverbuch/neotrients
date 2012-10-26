package org.neo4j.play

// Useful links:
// http://blog.fakod.eu/2010/10/04/neo4j-example-written-in-scala/

import scala.collection.JavaConversions._
import org.neo4j.graphdb._
import org.neo4j.kernel.impl.util.FileUtils
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.graphdb.index.Index
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.cypher.ExecutionResult
import java.io.File
import common._
import play.api._

object Neo4j {
  private val PATH = "neo4jdb"
  private var neo4j: Neo4j = null
  def apply() = Option(neo4j) match {
    case Some(aNeo4j) =>
      aNeo4j
    case None =>
      neo4j = new Neo4j(PATH)
      neo4j
  }
}

class Neo4j(private val path: String) {
  private var graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path).newGraphDatabase
  private var nodeIndex = graphDb.index.forNodes("nodes")
  private var engine: ExecutionEngine = new ExecutionEngine(graphDb)

  def doCypher(query: String): ExecutionResult =
    engine.execute(query)
  //    doCypher(query, Map())
  def doCypher(query: String, params: Map[String, Any]): ExecutionResult =
    engine.execute(query, params)

  def getNodeByProperty(key: String, value: Any): Node = nodeIndex.get(key, value).getSingle
  def getNodesByProperty(key: String, value: Any): Iterator[Node] = nodeIndex.get(key, value).iterator

  def createNode: Node = graphDb.createNode

  def getNodeIndex: Index[Node] = nodeIndex

  def shutDown {
    try {
      Logger.info("Shutting down database...")
      graphDb.shutdown
    } finally {
      Logger.info("Database shutdown complete")
    }
  }

  def createDb {
    Logger.info("Creating database instance...")
    val config = Map[String, String](
      "neostore.nodestore.db.mapped_memory" -> "10M",
      "string_block_size" -> "60",
      "array_block_size" -> "300")
    graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path).setConfig(config).newGraphDatabase
    nodeIndex = graphDb.index.forNodes("nodes")
    registerShutdownHook
    Logger.info("Database created")
  }

  def clearDb {
    Logger.info("Deleting database directory...")
    FileUtils.deleteRecursively(new File(path))
    Logger.info("Database deleted")
  }

  def withConnection(f: => Unit) {
    val tx = graphDb.beginTx
    try {
      f
      tx.success
    } finally {
      tx.finish
    }
  }

  private def registerShutdownHook {
    Runtime.getRuntime.addShutdownHook(new Thread { override def run = graphDb.shutdown })
  }
}