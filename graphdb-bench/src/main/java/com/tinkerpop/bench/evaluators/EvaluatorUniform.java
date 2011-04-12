package com.tinkerpop.bench.evaluators;

import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public class EvaluatorUniform extends Evaluator {

	@Override
	public double evaluate(Vertex vertex) {
		return 1d;
	}
}