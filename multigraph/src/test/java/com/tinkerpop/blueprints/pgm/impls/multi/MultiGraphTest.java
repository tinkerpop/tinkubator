package com.tinkerpop.blueprints.pgm.impls.multi;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class MultiGraphTest {
    private Graph base1, base2;
    private Graph graph;

    @Before
    public void setUp() throws Exception {
        base1 = new TinkerGraph();
        base2 = new TinkerGraph();

        graph = new MultiGraph(base1, base2);

        Vertex arthur1 = base1.addVertex("Arthur");
        Vertex ford1 = base1.addVertex("Ford");
        Vertex zaphod1 = base1.addVertex("Zaphod");
        Vertex earth1 = base1.addVertex("Earth");

        arthur1.setProperty("comment", "he's a jerk");
        ford1.setProperty("comment", "a little odd");

        base1.addEdge("Arthur knows Ford", arthur1, ford1, "knows").setProperty("comment", "but not very well");
        base1.addEdge("Arthur knows Zaphod", arthur1, zaphod1, "knows");
        base1.addEdge("Arthur's home planet", arthur1, earth1, "home planet");
        base1.addEdge("Ford's home planet", ford1, earth1, "home planet");

        Vertex ford2 = base2.addVertex("Ford");
        Vertex zaphod2 = base2.addVertex("Zaphod");
        Vertex betelgeuse2 = base2.addVertex("Betelgeuse");

        ford2.setProperty("comment", "he really knows where his towel is");

        base2.addEdge("Ford knows Zaphod", ford2, zaphod2, "knows");
        base2.addEdge("Ford's home planet", ford2, betelgeuse2, "home planet");

        // Note: currently, Arthur cannot be from Betelgeuse, as they don't exist in the same graph
    }

    @After
    public void tearDown() throws Exception {
        graph.shutdown();
    }

    @Test
    public void testVertices() throws Exception {
        int count = 0;
        Set<Object> ids = new HashSet<Object>();
        for (Vertex v : graph.getVertices()) {
            assertTrue(v instanceof MultiVertex);

            ids.add(v.getId());
            count++;
        }

        assertEquals(5, count);
        assertEquals(5, ids.size());
        assertTrue(ids.contains("Ford"));
        assertTrue(ids.contains("Arthur"));
        assertTrue(ids.contains("Zaphod"));
        assertTrue(ids.contains("Earth"));
        assertTrue(ids.contains("Betelgeuse"));
    }

    @Test
    public void testEdges() throws Exception {
        Edge e;

        e = graph.getEdge("Arthur's home planet");
        assertEquals("Arthur", e.getOutVertex().getId());
        assertEquals("Earth", e.getInVertex().getId());

        e = graph.getEdge("Ford's home planet");
        assertEquals("Ford", e.getOutVertex().getId());
        // The answer "Betelgeuse" is ignored, because base graph #1 takes precedence
        assertEquals("Earth", e.getInVertex().getId());

        Vertex arthur = graph.getVertex("Arthur");
        Vertex ford = graph.getVertex("Ford");
        Vertex zaphod = graph.getVertex("Zaphod");

        Collection<Edge> edges;

        edges = asCollection(arthur.getOutEdges("knows"));
        assertEquals(2, edges.size());
        for (Edge ed : edges) {
            assertTrue(ed instanceof MultiEdge);
        }

        edges = asCollection(zaphod.getInEdges("knows"));
        assertEquals(2, edges.size());

        // Only one of the conflicting edges is chosen.
        edges = asCollection(ford.getOutEdges("home planet"));
        assertEquals(1, edges.size());
    }

    @Test
    public void testVertexProperties() throws Exception {
        Vertex arthur = graph.getVertex("Arthur");
        Vertex ford = graph.getVertex("Ford");

        assertEquals("he's a jerk", arthur.getProperty("comment"));
        // Graph #1 takes precedence
        assertEquals("a little odd", ford.getProperty("comment"));
    }

    @Test
    public void testEdgeProperties() throws Exception {
        Edge e = graph.getEdge("Arthur knows Ford");

        assertEquals("but not very well", e.getProperty("comment"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClearUnsupported() throws Exception {
        graph.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddVertexUnsupported() throws Exception {
        graph.addVertex(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddEdgeUnsupported() throws Exception {
        graph.addEdge(null, null, null, "test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveEdgeUnsupported() throws Exception {
        graph.removeEdge(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveVertexUnsupported() throws Exception {
        graph.removeVertex(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetVertexPropertyUnsupported() throws Exception {
        Vertex v = graph.getVertex("Arthur");

        v.setProperty("comment", "a complete kneebiter");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetEdgePropertyUnsupported() throws Exception {
        Edge e = graph.getEdge("Arthur knows Ford");

        e.setProperty("comment", "for several years");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveVertexPropertyUnsupported() throws Exception {
        Vertex v = graph.getVertex("Arthur");

        v.removeProperty("comment");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveEdgePropertyUnsupported() throws Exception {
        Edge e = graph.getEdge("Arthur knows Ford");

        e.removeProperty("comment");
    }

    private <T> Collection<T> asCollection(Iterable<T> iter) {
        Collection<T> c = new LinkedList<T>();
        for (T t : iter) {
            c.add(t);
        }

        return c;
    }
}
