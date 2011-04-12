package com.tinkerpop.bench.operation.operations;

import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.gremlin.Gremlin;
import com.tinkerpop.pipes.Pipe;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationGremlin extends Operation {

	private String gremlinScript = null;
	private Pipe compiledScript = null;

	// args
	// -> 0 gremlinScript
	@Override
	protected void onInitialize(String[] args) {
		this.gremlinScript = args[0];
		this.compiledScript = Gremlin.compile(this.gremlinScript);
		// compiledScript.setStarts(graph.getVertex(1)); // FIXME necessary?
	}

	@Override
	protected void onExecute() throws Exception {
		try {
			int resultCount = 0;

			for (Object result : compiledScript)
				resultCount++;

			setResult(Integer.toString(resultCount));
		} catch (Exception e) {
			throw e;
		}
	}

}
