package com.tinkerpop.bench.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import com.tinkerpop.bench.LogUtils;

/**
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class OperationLogReader implements Iterable<OperationLogEntry> {

	private final String logDelim = LogUtils.LOG_DELIMITER;
	private File logFile = null;

	public OperationLogReader(File logFile) {
		super();
		this.logFile = logFile;
	}

	public Iterator<OperationLogEntry> iterator() {
		try {
			return new OperationLogEntryIterator(logFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(
					"Could not create OperationLogEntryIterator", e.getCause());
		}
	}

	private class OperationLogEntryIterator implements
			Iterator<OperationLogEntry> {

		private OperationLogEntry nextLogEntry = null;
		private Scanner logScanner = null;

		public OperationLogEntryIterator(File logFile)
				throws FileNotFoundException {
			this.logScanner = new Scanner(logFile);

			// skip first line: .csv headers
			logScanner.nextLine();
		}

		@Override
		public boolean hasNext() {
			if (nextLogEntry != null)
				return true;

			return ((nextLogEntry = parseLogEntry()) != null);
		}

		@Override
		public OperationLogEntry next() {
			if (nextLogEntry == null)
				throw new NoSuchElementException();

			OperationLogEntry logEntry = nextLogEntry;
			nextLogEntry = null;
			return logEntry;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private OperationLogEntry parseLogEntry() {
			if (logScanner.hasNextLine() == false) {
				logScanner.close();
				logScanner = null;
				return null;
			}

			return extractLogEntry(logScanner.nextLine());
		}

		private OperationLogEntry extractLogEntry(String currentLine) {
			int opId = -1;
			String name = null;
			String type = null;
			String[] args = null;
			long time = -1;
			String result = null;

			int index = -1;
			String token = null;
			StringTokenizer tokenizer = new StringTokenizer(currentLine,
					logDelim + "\t\n\r\f");

			while (tokenizer.hasMoreTokens()) {
				index++;
				token = tokenizer.nextToken();

				switch (index) {
				case 0:
					opId = Integer.parseInt(token);
					break;
				case 1:
					name = token;
					break;
				case 2:
					type = token;
					break;
				case 3:
					args = extractArgs(token);
					break;
				case 4:
					time = Integer.parseInt(token);
					break;
				case 5:
					result = token;
					break;
				}
			}

			return new OperationLogEntry(opId, name, type, args, time, result);
		}

		private String[] extractArgs(String argsStr) {
			Vector<String> argsVector = new Vector<String>();
			for (String arg : argsStr.replaceAll("[\\[\\]]", "").split(", "))
				argsVector.add(arg);

			return argsVector.toArray(new String[argsVector.size()]);
		}

	}

}