package com.tinkerpop.tinkubator.idindex;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdIndexGraphTest {
    private IndexableGraph base;
    IdIndexGraph graph;

    @Before
    public void setUp() throws Exception {
        base = new TinkerGraph();
        graph = new IdIndexGraph(base);
    }

    @After
    public void tearDown() throws Exception {
        base.shutdown();
    }

    @Test
    public void testElementClasses() throws Exception {
        Vertex v1 = graph.addVertex(null);
        Vertex v2 = graph.addVertex(null);
        Edge e = graph.addEdge(null, v1, v2, "knows");

        assertTrue(v1 instanceof IdIndexVertex);
        assertTrue(e instanceof IdIndexEdge);

        Iterator<Edge> outE = v1.getOutEdges().iterator();
        assertTrue(outE.hasNext());
        e = outE.next();
        assertTrue(e instanceof IdIndexEdge);
        assertTrue(e.getInVertex() instanceof IdIndexVertex);
        assertTrue(e.getOutVertex() instanceof IdIndexVertex);

        Iterator<Vertex> vertices = graph.getVertices().iterator();
        assertTrue(vertices.hasNext());
        while (vertices.hasNext()) {
            assertTrue(vertices.next() instanceof IdIndexVertex);
        }

        Iterator<Edge> edges = graph.getEdges().iterator();
        assertTrue(edges.hasNext());
        while (edges.hasNext()) {
            assertTrue(edges.next() instanceof IdIndexEdge);
        }
    }

    @Test
    public void testIdIndicesExist() throws Exception {
        Index<Vertex> vertexIds = base.getIndex(IdIndexGraph.VERTEX_IDS, Vertex.class);
        Index<Edge> edgeIds = base.getIndex(IdIndexGraph.EDGE_IDS, Edge.class);

        assertNotNull(vertexIds);
        assertNotNull(edgeIds);

        assertNull(graph.getIndex(IdIndexGraph.VERTEX_IDS, Vertex.class));
        assertNull(graph.getIndex(IdIndexGraph.EDGE_IDS, Edge.class));
    }

    @Test
    public void testDefaultIdFactory() throws Exception {
        Vertex v = graph.addVertex(null);
        String id = (String) v.getId();

        assertEquals(36, id.length());
        assertEquals(5, id.split("-").length);

        Vertex v2 = graph.addVertex(null);
        Edge e = graph.addEdge(null, v, v2, "knows");

        id = (String) e.getId();
        assertEquals(36, id.length());
        assertEquals(5, id.split("-").length);
    }

    @Test
    public void testAddVertexWithSpecifiedId() throws Exception {
        Vertex v = graph.addVertex("forty-two");

        assertEquals("forty-two", v.getId());
    }

    @Test
    public void testIndices() throws Exception {
        Set<String> nameKeys = new HashSet<String>();
        nameKeys.add("name");

        graph.createAutomaticIndex("names", Vertex.class, nameKeys);
        graph.createManualIndex("weights", Edge.class);

        Iterable<Index<? extends Element>> indices = graph.getIndices();
        int count = 0;
        for (Index<? extends Element> i : indices) {
            String name = i.getIndexName();
            Class c = i.getIndexClass();
            Index.Type t = i.getIndexType();

            if (name.equals("names")) {
                assertEquals(Index.Type.AUTOMATIC, t);
                assertEquals(Vertex.class, c);
                Set<String> keys = ((AutomaticIndex) i).getAutoIndexKeys();
                assertEquals(1, keys.size());
                assertTrue(keys.contains("name"));
            } else if (name.equals("weights")) {
                assertEquals(Index.Type.MANUAL, t);
                assertEquals(Edge.class, c);
            } else if (!name.equals("edges") && !name.equals("vertices")) {
                fail("unexpected index: " + name);
            }

            count++;
        }
        assertEquals(4, count);

        AutomaticIndex<Vertex> names = (AutomaticIndex<Vertex>) graph.getIndex("names", Vertex.class);
        Index<Edge> weights = graph.getIndex("weights", Edge.class);

        Vertex v1 = graph.addVertex(null);
        v1.setProperty("name", "Arthur");

        Vertex v2 = graph.addVertex(null);
        v2.setProperty("name", "Ford");

        Edge e = graph.addEdge(null, v1, v2, "knows");
        e.setProperty("weight", 0.8);

        Collection<Vertex> vertices;
        vertices = toCollection(names.get("name", "Arthur"));
        assertEquals(1, vertices.size());
        assertEquals(v1.getId(), vertices.iterator().next().getId());
                vertices = toCollection(names.get("name", "Ford"));
        assertEquals(1, vertices.size());
        assertEquals(v2.getId(), vertices.iterator().next().getId());

        weights.put("weight", 0.4, e);
        Collection<Edge> edges;
        edges = toCollection(weights.get("weight", 0.8));
        assertEquals(0, edges.size());
        edges = toCollection(weights.get("weight", 0.4));
        assertEquals(1, edges.size());
        assertEquals(e.getId(), edges.iterator().next().getId());
    }

    @Test
    public void testProperties() throws Exception {
        Vertex v = graph.addVertex(null);
        v.setProperty("name", "Zaphod");
        v.setProperty("profession", "ex-president of the Galaxy");

        Set<String> keys = v.getPropertyKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("name"));
        assertTrue(keys.contains("profession"));
        assertEquals("Zaphod", v.getProperty("name"));
    }

    private <T> Collection<T> toCollection(final CloseableSequence<T> s) {
        Collection<T> c = new LinkedList<T>();
        try {
            while (s.hasNext()) {
                c.add(s.next());
            }
        } finally {
            s.close();
        }

        return c;
    }
}
