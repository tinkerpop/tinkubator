package com.tinkerpop.bench.benchmark;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.tinkerpop.bench.Bench;
import com.tinkerpop.bench.GraphDescriptor;
import com.tinkerpop.bench.LogUtils;
import com.tinkerpop.bench.operation.OperationDeleteGraph;
import com.tinkerpop.bench.operation.operations.OperationIndexPutAllElements;
import com.tinkerpop.bench.operation.operations.OperationLoadGraphML;
import com.tinkerpop.bench.operation.operations.OperationPipesEchoIntensive;
import com.tinkerpop.bench.operation.operations.OperationPipesEchoLazy;
import com.tinkerpop.bench.operationFactory.OperationFactory;
import com.tinkerpop.bench.operationFactory.OperationFactoryGeneric;
import com.tinkerpop.bench.operationFactory.factories.OperationFactoryPipesEcho;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class BenchmarkEchoVersusDepth extends Benchmark {

	/*
	 * Static Code
	 */

	public static void run() throws Exception {
		String dirResults = Bench.benchProperties
				.getProperty(Bench.RESULTS_DIRECTORY)
				+ "EchoVersusDepth/";

		LogUtils.deleteDir(dirResults);

		String dirGraphML = Bench.benchProperties
				.getProperty(Bench.DATASETS_DIRECTORY);

		GraphDescriptor graphDescriptor = null;

		Benchmark benchmark = new BenchmarkEchoVersusDepth(dirResults
				+ "echo.csv", dirGraphML + "barabasi_1000000_5000000.graphml");

		// Load operation logs with Orient
		graphDescriptor = new GraphDescriptor(OrientGraph.class, dirResults
				+ "orient/", "local:" + dirResults + "orient/");
		benchmark.loadOperationLogs(graphDescriptor, dirResults
				+ "echo_orient.csv");

		// Load operation logs with Neo4j
		graphDescriptor = new GraphDescriptor(Neo4jGraph.class, dirResults
				+ "neo4j/", dirResults + "neo4j/");
		benchmark.loadOperationLogs(graphDescriptor, dirResults
				+ "echo_neo4j.csv");

		// Load operation logs with TinkerGraph
		graphDescriptor = new GraphDescriptor(TinkerGraph.class);
		benchmark.loadOperationLogs(graphDescriptor, dirResults
				+ "echo_tinker.csv");

		// Create file with summarized results from all databases and operations
		LinkedHashMap<String, String> resultFiles = new LinkedHashMap<String, String>();
		resultFiles.put("Neo4j", dirResults + "echo_neo4j.csv");
		resultFiles.put("OrientDB", dirResults + "echo_orient.csv");
		resultFiles.put("TinkerGraph", dirResults + "echo_tinker.csv");
		LogUtils.makeResultsSummary(dirResults + "echo_summary.csv",
				resultFiles);
	}

	/*
	 * Instance Code
	 */

	private final String idPropertyKey = Bench.benchProperties
			.getProperty(Bench.GRAPH_PROPERTY_ID);

	private final int ECHO_OP_COUNT = 1000;

	private final int ECHO_MAX_DEPTH = 7;

	private String graphMLFile = null;

	public BenchmarkEchoVersusDepth(String log, String graphMLFile) {
		super(log);
		this.graphMLFile = graphMLFile;
	}

	@Override
	protected ArrayList<OperationFactory> getOperationFactories() {
		ArrayList<OperationFactory> operationFactories = new ArrayList<OperationFactory>();

		operationFactories.add(new OperationFactoryGeneric(
				OperationDeleteGraph.class, 1));

		operationFactories.add(new OperationFactoryGeneric(
				OperationLoadGraphML.class, 1, new String[] { graphMLFile },
				LogUtils.pathToName(graphMLFile)));

		operationFactories.add(new OperationFactoryGeneric(
				OperationIndexPutAllElements.class, 1,
				new String[] { idPropertyKey }, LogUtils
						.pathToName(graphMLFile)));

		for (int depth = 1; depth < ECHO_MAX_DEPTH + 1; depth++)
			operationFactories.add(new OperationFactoryPipesEcho(ECHO_OP_COUNT,
					idPropertyKey, depth, OperationPipesEchoLazy.class));

		for (int depth = 1; depth < ECHO_MAX_DEPTH + 1; depth++)
			operationFactories.add(new OperationFactoryPipesEcho(ECHO_OP_COUNT,
					idPropertyKey, depth, OperationPipesEchoIntensive.class));

		operationFactories.add(new OperationFactoryGeneric(
				OperationDeleteGraph.class, 1));

		return operationFactories;
	}

}
