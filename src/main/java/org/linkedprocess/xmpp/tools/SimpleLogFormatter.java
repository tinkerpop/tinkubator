package gov.lanl.cnls.linkedprocess.xmpp.tools;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleLogFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
        return record.getLevel() + ": "
            + record.getSourceClassName() + "."
            + record.getSourceMethodName() + ": "
            + record.getMessage() + "\n";
      }

}
