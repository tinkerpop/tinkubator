package com.tinkerpop.bench.benchmark;

import java.io.File;
import java.util.ArrayList;

import com.tinkerpop.bench.BenchRunner;
import com.tinkerpop.bench.GraphDescriptor;
import com.tinkerpop.bench.operationFactory.OperationFactoryLog;
import com.tinkerpop.bench.operationFactory.OperationFactory;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public abstract class Benchmark {

	private String log = null;

	public Benchmark(String log) {
		this.log = log;
	}

	private void createOperationLogs() throws Exception {
		GraphDescriptor graphDescriptor = new GraphDescriptor(TinkerGraph.class);

		try {
			BenchRunner benchRunner = new BenchRunner(graphDescriptor,
					new File(log), getOperationFactories());

			benchRunner.startBench();
		} catch (Exception e) {
			throw e;
		}
	}

	protected abstract ArrayList<OperationFactory> getOperationFactories();

	public final void loadOperationLogs(GraphDescriptor graphDescriptor,
			String logOut) throws Exception {
		if (new File(log).exists() == false)
			createOperationLogs();

		OperationFactory operationFactory = new OperationFactoryLog(new File(
				log));

		BenchRunner benchRunner = new BenchRunner(graphDescriptor, new File(
				logOut), operationFactory);

		benchRunner.startBench();
	}

}