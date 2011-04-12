package com.tinkerpop.bench;

import com.tinkerpop.bench.benchmark.BenchmarkReadWriteVersusSize;

public class BenchmarkSuite {
	public static void main(String[] args) throws Exception {
		BenchmarkReadWriteVersusSize.run();
		// BenchmarkEchoVersusDepth.run();
	}
}
