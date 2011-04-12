package com.tinkerpop.bench.evaluators;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public abstract class Evaluator {

	private double total = -1;

	public double evaluateTotal(Graph db) {
		if (total != -1)
			return total;

		total = 0d;
		for (Vertex vertex : db.getVertices())
			total += evaluate(vertex);

		return total;
	}

	public abstract double evaluate(Vertex vertex);
}
