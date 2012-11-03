package models

import scala.collection.JavaConversions._
import common._
import NodeTypes.NODE_TYPE
import play.api.libs.json._
import play.api.libs.json.Json._
import org.neo4j.graphdb.Node
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.cypher.ExecutionEngine
import org.neo4j.cypher.ExecutionResult
import org.neo4j.play.Neo4j
import org.neo4j.graphdb.index.Index

import play.api._

object Food {
  // Properties
  val DATASET_ID = "fd_ds_id"
  val NAME = "fd_name"
  val DESC = "fd_desc"
  val SCIENTIFIC = "fd_sci"

  // Relationships
  val NUTRIENTS = "fd_nts"
  val NUTRIENT_AMOUNT = "fd_nt_amnt"

  // Relationships
  val DATASET = "fd_ds"

  def count: Int = IteratorUtil.count(all)

  def all: Iterator[Food] = {
    val result =
      Neo4j.doCypher("""
    		  START food=node:%1$s(%2$s='%3$s')
    		  RETURN food"""
        format
        ("nodes", NodeTypes.NODE_TYPE, NodeTypes.FOOD_NODE_TYPE))

    result.columnAs("food").map((n: Node) => new Food(n))
  }

  def allForDataset(datasetId: String): Iterator[Food] = {
    val result =
      Neo4j.doCypher("""
    		  START food=node:%1$s(%2$s='%3$s')
    		  WHERE food.%4$s='%5$s'
              RETURN food"""
        format
        ("nodes", NodeTypes.NODE_TYPE, NodeTypes.FOOD_NODE_TYPE, Food.DATASET_ID, datasetId))

    result.columnAs("food").map((n: Node) => new Food(n))
  }

  def allWithNamePrefix(namePrefix: String): Iterator[Food] = {
    val result =
      Neo4j.doCypher("""
    		  START food=node:%1$s(%2$s='%3$s')
    		  WHERE food.%4$s =~ '%5$s.*'    		  			
    		  RETURN food"""
        format
        ("nodes", NodeTypes.NODE_TYPE, NodeTypes.FOOD_NODE_TYPE, Food.NAME, namePrefix))

    result.columnAs("food").map((n: Node) => new Food(n))
  }

  def allForDatasetWithNamePrefix(datasetId: String, namePrefix: String): Iterator[Food] = {
    val result =
      Neo4j.doCypher("""
    		  START food=node:%1$s(%2$s='%3$s')
    		  WHERE food.%4$s='%5$s' AND food.%6$s =~ '%7$s.*'
    		  RETURN food"""
        format
        ("nodes", NodeTypes.NODE_TYPE, NodeTypes.FOOD_NODE_TYPE, Food.DATASET_ID, datasetId, Food.NAME, namePrefix))

    result.columnAs("food").map((n: Node) => new Food(n))
  }

  def getByName(name: String): Option[Food] = {
    val result =
      Neo4j.doCypher("""
    		  START food=node:%1$s(%2$s='%3$s')
    		  RETURN food"""
        format
        ("nodes", Food.NAME, name))

    val resultNodes: List[Node] = result.columnAs("food").toList
    resultNodes match {
      case List(foodNode) => Some(new Food(foodNode))
      case _ => None
    }
  }

  def create(datasetId: String, name: String, desc: String, scientific: String) {
    //    NutrientsStore.createFood(datasetId, name, desc, scientific)
    val result =
      Neo4j.doCypher("""
    		  CREATE food = {
    		  	%1$s:'%2$s',
    		  	%3$s:'%4$s',
    		  	%5$s:'%6$s',
    		  	%7$s:'%8$s'}
    		  RETURN food"""
        format (
          Food.DATASET_ID, datasetId,
          Food.NAME, name,
          Food.DESC, desc,
          Food.SCIENTIFIC, scientific))

    val resultNodes: List[Node] = result.columnAs("food").toList
    resultNodes match {
      case List(foodNode) => Neo4j.withConnection {
        val index: Index[Node] = Neo4j.getIndexManager.forNodes(NutrientsStore.INDEX_NODES)
        index.add(foodNode, Food.NAME, name)
        index.add(foodNode, Food.DESC, desc)
        index.add(foodNode, NODE_TYPE, NodeTypes.FOOD_NODE_TYPE)

        val dataSourceNode = index.get(DataSource.DATASET_ID, datasetId).getSingle
        if (dataSourceNode != null)
          foodNode.createRelationshipTo(dataSourceNode, RelTypes.ORIGIN_DATASOURCE)
      }
      case _ => None
    }

    //	START me=node(3)
    //	MATCH p1 = me-[*2]-friendOfFriend
    //	CREATE p2 = me-[:MARRIED_TO]-(wife {name:"Gunhild"})
    //	CREATE UNIQUE p3 = wife-[:KNOWS]-friendOfFriend
    //	RETURN p1,p2,p3

    //	CREATE n = {name : 'Andres', title : 'Developer'}

    //	START a=node(1), b=node(2)
    //	CREATE a-[r:RELTYPE]->b
    //	RETURN r

    //creates what needs to be created only
    //	CREATE p = (andres {name:'Andres'})-[:WORKS_AT]->neo<-[:WORKS_AT]-(michael {name:'Michael'})
    //	RETURN p

    //props is an Iterable<Map<String,Object>>
    //creates one node for each Map
    //	CREATE (n {props}) RETURN n

    //left matched agains the two right nodes. only relationships that dont exist are created
    //	START left=node(1), right=node(3,4)
    //	CREATE UNIQUE left-[r:KNOWS]->right
    //	RETURN r    

    //if no node connected with root has the name D, a new node is created to match the pattern
    //	START root=node(2)
    //	CREATE UNIQUE root-[:X]-(leaf {name:'D'} )
    //	RETURN leaf

    //	START root=node(2)
    //	CREATE UNIQUE root-[r:X {since:'forever'}]-()
    //	RETURN r

    //	START root=node(2)
    //	CREATE UNIQUE root-[:FOO]->x, root-[:BAR]->x
    //	RETURN x
  }

}

class Food(private val underlyingNode: Node) {
  private val PREVIEW = 2

  def getDatasetId: String = underlyingNode.getProperty(Food.DATASET_ID).asInstanceOf[String]
  def getName: String = underlyingNode.getProperty(Food.NAME).asInstanceOf[String]
  def getDescription: String = underlyingNode.getProperty(Food.DESC).asInstanceOf[String]
  def getScientific: String = underlyingNode.getProperty(Food.SCIENTIFIC).asInstanceOf[String]

  def toJsonSmall: JsValue = toJson(Map[String, JsValue](
    Food.DATASET_ID -> toJson(getDatasetId),
    Food.NAME -> toJson(getName),
    Food.DESC -> toJson(getDescription),
    Food.NUTRIENTS -> toJson(getNutrientValues.take(PREVIEW).map(toJson(_)).toList)))
  def toJsonBig: JsValue = toJson(Map[String, JsValue](
    Food.DATASET_ID -> toJson(getDatasetId),
    Food.DATASET -> getDataSource.toJsonSmall,
    Food.NAME -> toJson(getName),
    Food.DESC -> toJson(getDescription),
    Food.SCIENTIFIC -> toJson(getScientific),
    Food.NUTRIENTS -> toJson(getNutrientValues.map(toJson(_)).toList)))

  private def getNutrientValues: Iterator[Map[String, String]] = {
    val result =
      Neo4j.doCypher("""
    		  START food=node(%1$s)
    		  MATCH food-[contains:%2$s]->nutrient 
    		  RETURN nutrient.%3$s AS name, contains.%4$s AS amount"""
        format
        (underlyingNode.getId(), RelTypes.FOOD_CONTAINS, Nutrient.NAME, Food.NUTRIENT_AMOUNT))

    result.map(n => Map[String, String](
      "name" -> n("name").toString,
      "amount" -> n("amount").toString))
  }

  //  private def getNutrients: Iterator[Nutrient] = {
  //    val queryResult =
  //      Neo4j().doCypher("""
  //            START food=node(%s) 
  //            MATCH food-[:%s]->nutrient 
  //            RETURN nutrient AS n""".
  //        format(underlyingNode.getId(), RelTypes.FOOD_CONTAINS))
  //
  //    queryResult.map(n => new Nutrient(n))
  //  }

  //  def addNutrients(nutrients: List[Nutrient]) {
  //    NutrientsStore.createFood(name, unit)
  //  }

  def getDataSource: DataSource = {
    val result =
      Neo4j.doCypher("""
    		  START food=node(%1$s)
    		  MATCH food-[:%2$s]->datasource
    		  RETURN datasource"""
        format
        (underlyingNode.getId(), RelTypes.ORIGIN_DATASOURCE))

    new DataSource(result.columnAs("datasource").toList(0))
  }

  // TODO
  //  def delete {
  //    Neo4j.withConnection {
  //      underlyingNode.getRelationships.foreach(_.delete)
  //      underlyingNode.delete
  //    }
  //  }

  override def hashCode = underlyingNode.hashCode
  override def equals(obj: Any) =
    if (obj.isInstanceOf[Food]) {
      val that = obj.asInstanceOf[Food]
      this.underlyingNode.equals(that.underlyingNode)
    } else false
  override def toString = "Food(%s; %s; %s)" format
    (getDatasetId, getName, toJson(getNutrientValues.take(PREVIEW).map(toJson(_)).toList).toString)
}