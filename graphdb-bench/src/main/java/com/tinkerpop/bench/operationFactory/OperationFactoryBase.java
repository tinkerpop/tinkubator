package com.tinkerpop.bench.operationFactory;

import com.tinkerpop.bench.GraphDescriptor;
import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.blueprints.pgm.Graph;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public abstract class OperationFactoryBase extends OperationFactory {

	private GraphDescriptor graphDescriptor = null;
	private int opId = 0;

	//
	// Event methods
	//
	@Override
	public final void initialize(GraphDescriptor graphDescriptor, int firstOpId) {
		this.graphDescriptor = graphDescriptor;
		this.opId = firstOpId;
		onInitialize();
	}

	abstract protected OperationArgs onCreateOperation() throws Exception;

	//
	// Getter methods
	//
	@Override
	public final int getCurrentOpId() {
		return opId;
	}

	protected final GraphDescriptor getGraphDescriptor() {
		return graphDescriptor;
	}

	protected final Graph getGraph() {
		return graphDescriptor.getGraph();
	}

	//
	// Iterator/Iterable methods
	//
	@Override
	public final Operation next() {
		OperationArgs operationArgs = null;
		try {
			operationArgs = onCreateOperation();
		} catch (Exception e) {
			throw new RuntimeException("Error in onCreateOperation", e
					.getCause());
		}

		opId++;

		try {
			return loadOperation(opId, operationArgs.getType().getName(),
					operationArgs.getArgs(), operationArgs.getName());
		} catch (Exception e) {
			throw new RuntimeException("Error in loadOperation", e.getCause());
		}
	}

}
