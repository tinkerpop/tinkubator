package com.tinkerpop.bench;

import java.lang.reflect.Constructor;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Mode;

public class GraphDescriptor {

	private Class<?> graphType = null;
	private String graphDir = null;
	private String graphPath = null;
	private Graph graph = null;

	public GraphDescriptor(Class<?> graphType) {
		this(graphType, null, null);
	}

	public GraphDescriptor(Class<?> graphType, String graphDir, String graphPath) {
		this.graphType = graphType;
		this.graphDir = graphDir;
		this.graphPath = graphPath;
	}

	//
	// Getter Methods
	// 

	public Class<?> getGraphType() {
		return graphType;
	}

	public Graph getGraph() {
		return graph;
	}

	public boolean getPersistent() {
		return graphPath != null;
	}

	//
	// Functionality
	//

	public Graph openGraph() throws Exception {
		if (null != graph)
			return graph;

		Object[] args = (null == graphPath) ? new Object[] {}
				: new Object[] { graphPath };

		Constructor<?> graphConstructor = (null == graphPath) ? Class.forName(
				graphType.getName()).getConstructor() : Class.forName(
				graphType.getName()).getConstructor(String.class);

		try {
			graph = (Graph) graphConstructor.newInstance(args);
		} catch (Exception e) {
			throw e;
		}

		if (TransactionalGraph.class.isAssignableFrom(graphType))
			((TransactionalGraph) graph).setTransactionMode(Mode.AUTOMATIC);

		return graph;
	}

	public void shutdownGraph() {
		if (null != graph) {
			graph.shutdown();
			graph = null;
		}
	}

	public void deleteGraph() {
		shutdownGraph();
		if (true == getPersistent()) {
			deleteDir(graphDir);
		}
	}

	private void deleteDir(String pathStr) {
		LogUtils.deleteDir(pathStr);
	}

}
