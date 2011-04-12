package com.tinkerpop.bench.evaluators;

import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class EvaluatorProperty extends Evaluator {

	private String property = null;

	public EvaluatorProperty(String property) {
		this.property = property;
	}

	@Override
	public double evaluate(Vertex vertex) {
		return (Double) vertex.getProperty(property);
	}
}