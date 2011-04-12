package com.tinkerpop.bench.operationFactory.factories;

import java.util.ArrayList;
import java.util.Arrays;

import com.tinkerpop.bench.StatisticsHelper;
import com.tinkerpop.bench.evaluators.EvaluatorOutDegree;
import com.tinkerpop.bench.operationFactory.OperationArgs;
import com.tinkerpop.bench.operationFactory.OperationFactoryBase;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationFactoryPipesEcho extends OperationFactoryBase {

	private int opCount = 0;
	private String propertyKey = null;
	private int echoLength = 0;
	private Class<?> echoType = null;
	private String tag = null;
	private ArrayList<Object> vertexSamples = null;

	public OperationFactoryPipesEcho(int opCount, String propertyKey,
			int echoLength, Class<?> echoType) {
		this(opCount, propertyKey, echoLength, echoType, "");
	}

	public OperationFactoryPipesEcho(int opCount, String propertyKey,
			int echoLength, Class<?> echoType, String tag) {
		this.opCount = opCount;
		this.propertyKey = propertyKey;
		this.echoLength = echoLength;
		this.echoType = echoType;
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

		String tagLine = ("".equals(tag)) ? Integer.toString(echoLength) : tag
				+ "-" + Integer.toString(echoLength);

		// args
		// -> 0 startVertexIndexKey
		// -> 1 startVertexIndexValue
		// -> 2 steps
		String[] args = new String[] { propertyKey, propertyValue,
				Integer.toString(echoLength) };

		return new OperationArgs(args, echoType, tagLine);
	}
}
