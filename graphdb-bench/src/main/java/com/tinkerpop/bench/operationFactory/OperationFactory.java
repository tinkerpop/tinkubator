package com.tinkerpop.bench.operationFactory;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import com.tinkerpop.bench.GraphDescriptor;
import com.tinkerpop.bench.operation.Operation;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Martin Neumann (m.neumann.1980@gmail.com)
 */
public abstract class OperationFactory implements Iterator<Operation>,
		Iterable<Operation> {

	//
	// Getter methods
	//	
	public abstract int getCurrentOpId();

	//
	// Event methods
	//
	public abstract void initialize(GraphDescriptor graphDescriptor,
			int firstOpId);

	protected abstract void onInitialize();

	//
	// Iterator/Iterable methods
	//
	@Override
	public final Iterator<Operation> iterator() {
		return this;
	}

	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}

	//
	// Helper methods
	//
	protected final Operation loadOperation(int opId, String type,
			String[] args, String name) throws Exception {
		Constructor<?> operationConstructor = null;
		Operation operation = null;

		try {
			operationConstructor = Class.forName(type).getConstructors()[0];
			operation = (Operation) operationConstructor
					.newInstance(new Object[] {});
			operation.setId(opId);
			operation.setArgs(args);
			operation.setName(name);
		} catch (Exception e) {
			throw e;
		}

		return operation;
	}
}
