package com.tinkerpop.bench;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Bench {

	public static Logger logger = Logger.getLogger(Bench.class);
	public static Properties benchProperties = new Properties();

	// DATASETS - GraphML & Databases
	public static final String DATASETS_DIRECTORY = "bench.datasets.directory";

	// LOGS - Operation Logs
	public static final String LOGS_DELIMITER = "bench.logs.delimiter";

	// RESULTS - Logs, Summaries, Plots
	public static final String RESULTS_DIRECTORY = "bench.results.directory";

	// GRAPH GENERAL
	public static final String GRAPH_PROPERTY_ID = "bench.graph.property.id";
	public static final String GRAPH_LABEL = "bench.graph.label";
	public static final String GRAPH_LABEL_FAMILY = "bench.graph.label.family";
	public static final String GRAPH_LABEL_FRIEND = "bench.graph.label.friend";

	// GRAPH FILES
	public static final String GRAPHML_BARABASI = "bench.graph.barabasi.file";

	static {
		try {
			benchProperties.load(Bench.class
					.getResourceAsStream("bench.properties"));
			System.out.println(benchProperties);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PropertyConfigurator.configure(Bench.class
				.getResource("log4j.properties"));
	}

}
