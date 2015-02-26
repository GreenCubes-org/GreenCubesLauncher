package org.greencubes.util.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GLogSimple implements IGLog {

	private final Logger logger;

	public static class LogEntry {

		private Level level;
		private String message;
		private Throwable t;

		public LogEntry(Level level, String message, Throwable t) {
			this(level, message);
			this.t = t;
		}

		public LogEntry(Level level, String message) {
			this.level = level;
			this.message = message;
			this.t = null;
		}

		public Level getLevel() {
			return level;
		}

		public String getMessage() {
			return message;
		}

		public Throwable getException() {
			return t;
		}
	}

	@Override
	public void info(String message) {
		add(new LogEntry(Level.INFO, message));
	}

	@Override
	public void severe(String message) {
		add(new LogEntry(Level.SEVERE, message));
	}

	@Override
	public void warning(String message) {
		add(new LogEntry(Level.WARNING, message));
	}

	@Override
	public void debug(String message) {
		add(new LogEntry(Level.WARNING, "[DEBUG] " + message));
	}

	@Override
	public void log(Level level, String message) {
		add(new LogEntry(level, message));
	}

	@Override
	public void log(Level level, String message, Throwable t) {
		add(new LogEntry(level, message, t));
	}

	private void add(LogEntry entry) {
		if(entry.t != null)
			logger.log(entry.level, entry.message, entry.t);
		else
			logger.log(entry.level, entry.message);
	}

	public GLogSimple(Logger logger) {
		this.logger = logger;
	}
}