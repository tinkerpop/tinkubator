package com.tinkerpop.bench;

import java.util.Random;

import com.tinkerpop.bench.evaluators.Evaluator;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public class StatisticsHelper {

	private static Random rand = new Random(42);
	private static long time = -1l;

	public static Object[] getSampleVertexIds(Graph db, Evaluator evaluator,
			int sampleSize) {

		Object[] samples = new Object[sampleSize];
		Double[] sampleVals = new Double[sampleSize];

		double totalVal = evaluator.evaluateTotal(db);

		for (int i = 0; i < sampleVals.length; i++) {
			sampleVals[i] = rand.nextDouble() * totalVal;
			samples[i] = null;
		}

		boolean finished = true;

		for (Vertex currentVertex : db.getVertices()) {

			double currentVal = evaluator.evaluate(currentVertex);

			finished = true;

			for (int i = 0; i < sampleVals.length; i++) {
				if (samples[i] == null) {
					sampleVals[i] -= currentVal;
					if (sampleVals[i] <= 0)
						samples[i] = currentVertex.getId();
					else
						finished = false;
				}
			}

			if (finished == true)
				break;
		}

		return samples;
	}

	public static long stopWatch() {
		if (time == -1l) {
			time = System.currentTimeMillis();
			return time;
		} else {
			long temp = System.currentTimeMillis() - time;
			time = -1l;
			return temp;
		}
	}

}
