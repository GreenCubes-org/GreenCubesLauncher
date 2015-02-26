package org.greencubes.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class OnlyTextFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder();
		Throwable ex = record.getThrown();
		builder.append(record.getMessage());
		builder.append('\n');

		if(ex != null) {
			StringWriter writer = new StringWriter();
			ex.printStackTrace(new PrintWriter(writer));
			builder.append(writer);
		}

		String message = builder.toString();
		Object parameters[] = record.getParameters();
		if(parameters == null || parameters.length == 0)
			return message;
		if(message.indexOf("{0") >= 0 || message.indexOf("{1") >= 0 || message.indexOf("{2") >= 0 || message.indexOf("{3") >= 0)
			return java.text.MessageFormat.format(message, parameters);
		return record.getMessage() + '\n';
	}

}
