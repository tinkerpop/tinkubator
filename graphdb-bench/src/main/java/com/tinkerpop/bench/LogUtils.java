package com.tinkerpop.bench;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.tinkerpop.bench.log.OperationLogReader;
import com.tinkerpop.bench.log.OperationLogWriter;
import com.tinkerpop.bench.log.SummaryLogWriter;

public class LogUtils {

	public static final String LOG_DELIMITER = Bench.benchProperties
			.getProperty(Bench.LOGS_DELIMITER);

	public static void makeResultsSummary(String summaryFilePath,
			Map<String, String> resultFilePaths) throws IOException {
		SummaryLogWriter summaryLogWriter = new SummaryLogWriter();
		summaryLogWriter.writeSummary(summaryFilePath, resultFilePaths);
	}

	public static OperationLogReader getOperationLogReader(File logFile) {
		return new OperationLogReader(logFile);
	}

	public static OperationLogWriter getOperationLogWriter(File logFile)
			throws IOException {
		return new OperationLogWriter(logFile);
	}

	// FIXME (new File(pathStr)).mkdirs(); seems to cause problems with Neo4j
	// for some reason...
	// public static void cleanDir(String pathStr) {
	// deleteDir(pathStr);
	// (new File(pathStr)).mkdirs();
	// }

	public static void deleteDir(String dirStr) {
		File dir = new File(dirStr);

		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				if (file.isDirectory())
					deleteDir(file.getAbsolutePath());
				else
					file.delete();
			}
			dir.delete();
		}
	}

	public static String pathToName(String filename) {
		int startName = (filename.lastIndexOf(File.separator) == -1) ? -1
				: filename.lastIndexOf(File.separator);
		int endName = (filename.lastIndexOf(".") == -1) ? filename.length()
				: filename.lastIndexOf(".");
		return filename.substring(startName + 1, endName);
	}

	public static String msToTimeStr(long msTotal) {
		long ms = msTotal % 1000;
		long s = (msTotal / 1000) % 60;
		long m = ((msTotal / 1000) / 60) % 60;
		long h = ((msTotal / 1000) / 60) / 60;

		return String.format("%d(h):%d(m):%d(s):%d(ms)", h, m, s, ms);
	}
}
