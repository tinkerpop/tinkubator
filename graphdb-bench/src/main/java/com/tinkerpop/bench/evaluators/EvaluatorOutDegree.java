package com.tinkerpop.bench.evaluators;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public class EvaluatorOutDegree extends Evaluator {

	@Override
	@SuppressWarnings("unused")
	public double evaluate(Vertex vertex) {
		double outDegree = 0;
		for (Edge edge : vertex.getOutEdges()) {
			outDegree++;
		}
		return outDegree;
	}
}