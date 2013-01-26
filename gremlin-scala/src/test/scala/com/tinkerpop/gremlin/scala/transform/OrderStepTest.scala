package com.tinkerpop.gremlin.scala.transform

import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.scala._
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.pipes.Pipe
import com.tinkerpop.pipes.util.structures.Pair

class OrderStepTest extends com.tinkerpop.gremlin.test.transform.OrderStepTest {
  val g = TinkerGraphFactory.createTinkerGraph()

  override def testCompliance() {
    ComplianceTest.testCompliance(this.getClass)
  }

  def test_g_V_name_order() {
    super.test_g_V_name_order(g.V.property("name").asInstanceOf[GremlinScalaPipeline[Vertex, String]].order())
  }

  def test_g_V_name_orderXabX() {
    super.test_g_V_name_orderXabX(g.V.property("name").asInstanceOf[GremlinScalaPipeline[Vertex, String]].order({ arg: Pair[String, String] => arg.getB.compareTo(arg.getA) }))
  }

  def test_g_V_orderXa_nameXb_nameX_name() {
    super.test_g_V_orderXa_nameXb_nameX_name(g.V.order({ arg: Pair[Vertex, Vertex] => arg.getB.as[String]("name").get.compareTo(arg.getA.as[String]("name").get) }).property("name").asInstanceOf[GremlinScalaPipeline[Vertex, String]])
  }
}
