package com.tinkerpop.bench.operationFactory;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationFactoryGeneric extends OperationFactoryBase {

	private Class<?> operationType = null;
	private int opCount = -1;
	private String[] args = null;
	private String tag = null;

	public OperationFactoryGeneric(Class<?> operationType) {
		this(operationType, -1, new String[] {});
	}

	public OperationFactoryGeneric(Class<?> operationType, int opCount) {
		this(operationType, opCount, new String[] {});
	}

	public OperationFactoryGeneric(Class<?> operationType, int opCount,
			String[] args) {
		this(operationType, opCount, args, "");
	}

	public OperationFactoryGeneric(Class<?> operationType, int opCount,
			String[] args, String tag) {
		this.operationType = operationType;
		this.opCount = opCount;
		this.args = args;
		this.tag = tag;
	}

	@Override
	public void onInitialize() {
	}

	@Override
	public boolean hasNext() {
		return (-1 == opCount) ? true : (opCount > 0);
	}

	@Override
	protected OperationArgs onCreateOperation() throws Exception {
		if (-1 != opCount)
			opCount--;
		return new OperationArgs(args, operationType, tag);
	}
}
