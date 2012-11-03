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
import org.neo4j.graphdb.index.IndexManager

object Neo4j {
  private var graphDb: GraphDatabaseService = null
  private var engine: ExecutionEngine = null

  def createDb(path: String) {
    Logger.info("Creating database instance...")
    val config = Map[String, String](
      "neostore.nodestore.db.mapped_memory" -> "10M",
      "string_block_size" -> "60",
      "array_block_size" -> "300")
    graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path).setConfig(config).newGraphDatabase
    engine = new ExecutionEngine(graphDb)
    registerShutdownHook
    Logger.info("Database created")
  }

  def shutDown {
    Option(graphDb) match {
      case Some(graphDb) =>
        try {
          Logger.info("Shutting down database...")
          graphDb.shutdown
        } finally {
          Logger.info("Database shutdown complete")
        }
      case None => Logger.info("No database to shutdown")
    }
  }

  def clearDb(path: String) {
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

  def doCypher(query: String): ExecutionResult = doCypher(query, Map())
  def doCypher(query: String, params: Map[String, Any]): ExecutionResult =
    engine.execute(query, params)

  def _createNode: Node = graphDb.createNode

  def getIndexManager: IndexManager = graphDb.index

  private def registerShutdownHook {
    Runtime.getRuntime.addShutdownHook(new Thread { override def run = graphDb.shutdown })
  }
}