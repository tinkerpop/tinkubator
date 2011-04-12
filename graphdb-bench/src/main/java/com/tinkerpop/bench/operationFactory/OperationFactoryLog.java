package com.tinkerpop.bench.operationFactory;

import java.io.File;
import java.util.Iterator;

import com.tinkerpop.bench.GraphDescriptor;
import com.tinkerpop.bench.LogUtils;
import com.tinkerpop.bench.log.OperationLogEntry;
import com.tinkerpop.bench.operation.Operation;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public final class OperationFactoryLog extends OperationFactory {

	private Iterator<OperationLogEntry> operationLogIterator = null;

	public OperationFactoryLog(File file) {
		operationLogIterator = LogUtils.getOperationLogReader(file).iterator();
	}

	@Override
	public void initialize(GraphDescriptor graphDescriptor, int firstOpId) {
	}

	@Override
	protected void onInitialize() {
	}

	@Override
	public boolean hasNext() {
		return operationLogIterator.hasNext();
	}

	@Override
	public Operation next() {
		OperationLogEntry operationLogEntry = operationLogIterator.next();
		try {
			return loadOperation(operationLogEntry.getOpId(), operationLogEntry
					.getType(), operationLogEntry.getArgs(), operationLogEntry
					.getName());
		} catch (Exception e) {
			throw new RuntimeException("Error in loadOperation", e.getCause());
		}
	}

	@Override
	public int getCurrentOpId() {
		return -1;
	}

}