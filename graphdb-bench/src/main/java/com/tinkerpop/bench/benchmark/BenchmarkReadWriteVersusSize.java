package com.tinkerpop.bench.benchmark;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.tinkerpop.bench.Bench;
import com.tinkerpop.bench.GraphDescriptor;
import com.tinkerpop.bench.LogUtils;
import com.tinkerpop.bench.operation.OperationDeleteGraph;
import com.tinkerpop.bench.operation.operations.OperationIndexPutAllElements;
import com.tinkerpop.bench.operation.operations.OperationLoadGraphML;
import com.tinkerpop.bench.operationFactory.OperationFactory;
import com.tinkerpop.bench.operationFactory.OperationFactoryGeneric;
import com.tinkerpop.bench.operationFactory.factories.OperationFactoryIndexGetElements;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class BenchmarkReadWriteVersusSize extends Benchmark {

	/*
	 * Static Code
	 */

	public static void run() throws Exception {
		String dirResults = Bench.benchProperties
				.getProperty(Bench.RESULTS_DIRECTORY)
				+ "ReadWriteVersusSize/";

		LogUtils.deleteDir(dirResults);

		String dirGraphML = Bench.benchProperties
				.getProperty(Bench.DATASETS_DIRECTORY);

		GraphDescriptor graphDescriptor = null;

		String[] graphmlFiles = new String[] {
				dirGraphML + "barabasi_1000_5000.graphml",
				dirGraphML + "barabasi_10000_50000.graphml",
				dirGraphML + "barabasi_100000_500000.graphml",
				dirGraphML + "barabasi_1000000_5000000.graphml" };

		Benchmark benchmark = new BenchmarkReadWriteVersusSize(dirResults
				+ "load_graphml.csv", graphmlFiles);

		// // Load operation logs with Orient
		// graphDescriptor = new GraphDescriptor(OrientGraph.class, dirResults
		// + "orient/", "local:" + dirResults + "orient/");
		// benchmark.loadOperationLogs(graphDescriptor, dirResults
		// + "load_graphml_orient.csv");

		// Load operation logs with Neo4j
		graphDescriptor = new GraphDescriptor(Neo4jGraph.class, dirResults
				+ "neo4j/", dirResults + "neo4j/");
		benchmark.loadOperationLogs(graphDescriptor, dirResults
				+ "load_graphml_neo4j.csv");

		// Load operation logs with TinkerGraph
		graphDescriptor = new GraphDescriptor(TinkerGraph.class);
		benchmark.loadOperationLogs(graphDescriptor, dirResults
				+ "load_graphml_tinker.csv");

		// Create file with summarized results from all databases and operations
		LinkedHashMap<String, String> resultFiles = new LinkedHashMap<String, String>();
		resultFiles.put("Neo4j", dirResults + "load_graphml_neo4j.csv");
		// resultFiles.put("OrientDB", dirResults + "load_graphml_orient.csv");
		resultFiles.put("TinkerGraph", dirResults + "load_graphml_tinker.csv");
		LogUtils.makeResultsSummary(dirResults + "load_graphml_summary.csv",
				resultFiles);
	}

	/*
	 * Instance Code
	 */

	private String[] graphmlFilenames = null;

	private final String idPropertyKey = Bench.benchProperties
			.getProperty(Bench.GRAPH_PROPERTY_ID);

	private final int GET_OP_COUNT = 1000;
	private final int GET_LOOKUPS_PER_OP = 100;

	public BenchmarkReadWriteVersusSize(String log, String[] graphmlFilenames) {
		super(log);
		this.graphmlFilenames = graphmlFilenames;
	}

	@Override
	protected ArrayList<OperationFactory> getOperationFactories() {
		ArrayList<OperationFactory> operationFactories = new ArrayList<OperationFactory>();

		for (String graphmlFilename : graphmlFilenames) {
			operationFactories.add(new OperationFactoryGeneric(
					OperationDeleteGraph.class, 1));

			operationFactories.add(new OperationFactoryGeneric(
					OperationLoadGraphML.class, 1,
					new String[] { graphmlFilename }, LogUtils
							.pathToName(graphmlFilename)));

			operationFactories.add(new OperationFactoryGeneric(
					OperationIndexPutAllElements.class, 1,
					new String[] { idPropertyKey }, LogUtils
							.pathToName(graphmlFilename)));

			operationFactories.add(new OperationFactoryIndexGetElements(
					GET_OP_COUNT, idPropertyKey, GET_LOOKUPS_PER_OP, LogUtils
							.pathToName(graphmlFilename)));
		}

		// NOTE Keep largest graph for use in other Benchmarks
		// operationFactories.add(new OperationFactoryGeneric(
		// OperationDeleteGraph.class, 1));

		return operationFactories;
	}

}
