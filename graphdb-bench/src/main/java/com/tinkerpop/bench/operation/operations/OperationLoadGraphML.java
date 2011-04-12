package com.tinkerpop.bench.operation.operations;

import java.io.FileInputStream;

import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLReader;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationLoadGraphML extends Operation {

	private String graphmlPath = null;
	private final int TRANSACTION_BUFFER = 1000;

	// args
	// -> 0 graphmlDir
	@Override
	protected void onInitialize(String[] args) {
		this.graphmlPath = args[0];
	}

	@Override
	protected void onExecute() throws Exception {
		try {
			GraphMLReader.inputGraph(getGraph(), new FileInputStream(
					graphmlPath), TRANSACTION_BUFFER, null, null, null);
			setResult("DONE");
		} catch (Exception e) {
			throw e;
		}
	}

}
