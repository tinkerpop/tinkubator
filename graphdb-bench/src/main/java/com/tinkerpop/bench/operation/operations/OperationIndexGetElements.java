package com.tinkerpop.bench.operation.operations;

import java.util.ArrayList;

import com.tinkerpop.bench.operation.Operation;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationIndexGetElements extends Operation {

	private String[] propertyKeys = null;
	private String[] propertyValues = null;

	// args
	// -> 0 property keys
	// -> 1 property values
	@Override
	protected void onInitialize(String[] args) {
		this.propertyKeys = args[0].split(",");
		this.propertyValues = args[1].split(",");
	}

	@Override
	protected void onExecute() throws Exception {
		try {
			if ((getGraph() instanceof IndexableGraph) == false)
				throw new Exception("Graph is not IndexableGraph");

			Index<Vertex> vIndex = ((IndexableGraph) getGraph()).getIndex(
					Index.VERTICES, Vertex.class);

			ArrayList<Element> elements = new ArrayList<Element>();

			for (int i = 0; i < propertyKeys.length; i++) {
				for (Element element : vIndex.get(propertyKeys[i],
						propertyValues[i]))
					elements.add(element);
			}

			setResult(elements);
		} catch (Exception e) {
			throw e;
		}
	}
}
