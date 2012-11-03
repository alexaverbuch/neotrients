package org.neo4j.play

import common._

object Cypher {

}

class Cypher {
  def setStart(cypher: Cypher): Cypher = ???
  //START food=node:nodes({node_type}={food_node_type})  

  def setReturn: Cypher = ???

  def addMatch: Cypher = ???
  def addWhere: Cypher = ???
  def addCreate: Cypher = ???
  def addDelete: Cypher = ???
  def addSet: Cypher = ???
  def addForEach: Cypher = ???
  def addWith: Cypher = ???
}
 
//WITH: Divides a query into multiple, distinct parts.
