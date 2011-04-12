package com.tinkerpop.bench.operation;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationShutdownGraph extends Operation {

	@Override
	protected void onInitialize(String[] args) {
	}

	@Override
	protected void onExecute() throws Exception {
		try {
			if (getGraphDescriptor().getPersistent() == true)
				getGraphDescriptor().shutdownGraph();
			setResult("DONE");
		} catch (Exception e) {
			throw e;
		}
	}

}
