package models

import org.neo4j.graphdb._

object RelTypes extends Enumeration {
  type RelTypes = Value
  val FOOD_CONTAINS, ORIGIN_DATASOURCE = Value

  implicit def conv(rt: RelTypes) = new RelationshipType { def name = rt.toString }
}

object NodeTypes {
  val NODE_TYPE = "_type"
  val NUTRIENT_NODE_TYPE = "_nutrient"
  val DATASOURCE_NODE_TYPE = "_datasource"
  val FOOD_NODE_TYPE = "_food"
}
