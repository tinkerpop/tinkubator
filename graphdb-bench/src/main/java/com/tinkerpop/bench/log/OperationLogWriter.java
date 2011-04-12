package com.tinkerpop.bench.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.tinkerpop.bench.LogUtils;
import com.tinkerpop.bench.operation.Operation;

public class OperationLogWriter {
	private final String logDelim = LogUtils.LOG_DELIMITER;
	private BufferedWriter bufferedLogWriter = null;

	public OperationLogWriter(File logFile) throws IOException {
		super();
		(new File(logFile.getParent())).mkdirs();
		bufferedLogWriter = new BufferedWriter(new FileWriter(logFile));
		writeHeaders();
	}

	// Write .csv log column headers
	private void writeHeaders() throws IOException {
		bufferedLogWriter.write("id");
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write("name");
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write("type");
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write("args");
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write("time");
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write("result");
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.newLine();
	}

	// Write a .csv log data row
	public void logOperation(Operation op) throws IOException {
		bufferedLogWriter.write(Integer.toString(op.getId()));
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write(op.getName());
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write(op.getType());
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write(Arrays.toString(op.getArgs()));
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write(Long.toString(op.getTime()));
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.write(op.getResult().toString());
		bufferedLogWriter.write(logDelim);

		bufferedLogWriter.newLine();
	}

	public void close() throws IOException {
		bufferedLogWriter.flush();
		bufferedLogWriter.close();
	}
}
