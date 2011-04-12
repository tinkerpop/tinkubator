package com.tinkerpop.bench.operation.operations;

import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Mode;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationIndexPutAllElements extends Operation {

	private String propertyKey = null;
	private final int TRANSACTION_BUFFER = 1000;

	// args
	// -> 0 property key
	@Override
	protected void onInitialize(String[] args) {
		this.propertyKey = args[0];
	}

	@Override
	protected void onExecute() throws Exception {
		try {
			if ((getGraph() instanceof IndexableGraph) == false)
				throw new Exception("Graph is not IndexableGraph");

			int elementCount = (getGraph() instanceof TransactionalGraph) ? indexElementsTransactional()
					: indexElements();

			setResult(elementCount);
		} catch (Exception e) {
			throw e;
		}
	}

	private int indexElements() {
		IndexableGraph indexableGraph = ((IndexableGraph) getGraph());

		Index<Vertex> vIndex = indexableGraph.getIndex(Index.VERTICES,
				Vertex.class);

		Index<Edge> eIndex = indexableGraph.getIndex(Index.EDGES, Edge.class);

		int elementCount = 0;

		for (Vertex v : getGraph().getVertices()) {
			vIndex.put(propertyKey, v.getProperty(propertyKey), v);
			elementCount++;
		}

		for (Edge e : getGraph().getEdges()) {
			eIndex.put(propertyKey, e.getProperty(propertyKey), e);
			elementCount++;
		}

		return elementCount;
	}

	private int indexElementsTransactional() {
		Index<Vertex> vIndex = ((IndexableGraph) getGraph()).getIndex(
				Index.VERTICES, Vertex.class);

		Index<Edge> eIndex = ((IndexableGraph) getGraph()).getIndex(
				Index.EDGES, Edge.class);

		int elementCount = 0;

		TransactionalGraph transactionalGraph = (TransactionalGraph) getGraph();
		Mode transactionMode = transactionalGraph.getTransactionMode();

		transactionalGraph.setTransactionMode(Mode.MANUAL);
		transactionalGraph.startTransaction();

		for (Vertex v : getGraph().getVertices()) {
			vIndex.put(propertyKey, v.getProperty(propertyKey), v);
			elementCount++;

			if (elementCount % TRANSACTION_BUFFER == 0) {
				transactionalGraph.stopTransaction(Conclusion.SUCCESS);
				transactionalGraph.startTransaction();
			}
		}

		for (Edge e : getGraph().getEdges()) {
			eIndex.put(propertyKey, e.getProperty(propertyKey), e);
			elementCount++;

			if (elementCount % TRANSACTION_BUFFER == 0) {
				transactionalGraph.stopTransaction(Conclusion.SUCCESS);
				transactionalGraph.startTransaction();
			}
		}

		transactionalGraph.stopTransaction(Conclusion.SUCCESS);
		transactionalGraph.setTransactionMode(transactionMode);

		return elementCount;
	}

}
