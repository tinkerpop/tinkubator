package com.tinkerpop.bench.operationFactory.factories;

import java.util.ArrayList;
import java.util.Arrays;

import com.tinkerpop.bench.StatisticsHelper;
import com.tinkerpop.bench.evaluators.EvaluatorOutDegree;
import com.tinkerpop.bench.operation.operations.OperationGremlin;
import com.tinkerpop.bench.operationFactory.OperationArgs;
import com.tinkerpop.bench.operationFactory.OperationFactoryBase;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationFactoryGremlinOutNeighbors extends OperationFactoryBase {

	private int opCount = 0;
	private String propertyKey = null;
	private String tag = null;
	private ArrayList<Object> vertexSamples = null;

	public OperationFactoryGremlinOutNeighbors(int opCount, String propertyKey) {
		this(opCount, propertyKey, "");
	}

	public OperationFactoryGremlinOutNeighbors(int opCount, String propertyKey,
			String tag) {
		this.opCount = opCount;
		this.propertyKey = propertyKey;
		this.tag = tag;
	}

	@Override
	public void onInitialize() {
		vertexSamples = new ArrayList<Object>(Arrays.asList(StatisticsHelper
				.getSampleVertexIds(getGraph(), new EvaluatorOutDegree(),
						opCount)));
	}

	@Override
	public boolean hasNext() {
		return vertexSamples.isEmpty() == false;
	}

	@Override
	protected OperationArgs onCreateOperation() throws Exception {
		Object startVertexId = vertexSamples.remove(0);
		Vertex startVertex = getGraph().getVertex(startVertexId);

		if (startVertex == null)
			throw new Exception(String.format("Vertex ID[%d] not found!",
					startVertexId));

		String propertyValue = startVertex.getProperty(propertyKey).toString();
		String gremlinScript = String.format("g:key-v('%s','%s')/outE/inV",
				propertyKey, propertyValue);

		String tagLine = ("".equals(tag)) ? gremlinScript : tag + "-"
				+ gremlinScript;

		// args
		// -> 0 gremlinScript
		String[] args = new String[] { gremlinScript };

		return new OperationArgs(args, OperationGremlin.class, tagLine);
	}

}
