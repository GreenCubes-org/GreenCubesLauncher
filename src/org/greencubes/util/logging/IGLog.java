package org.greencubes.util.logging;

import java.util.logging.Level;

public interface IGLog {

	public void info(String message);

	public void severe(String message);

	public void warning(String message);

	public void debug(String message);

	public void log(Level level, String message);

	public void log(Level level, String message, Throwable t);

	static class GLevel extends Level {

		private static final long serialVersionUID = -539856764608026895L;

		private GLevel(String s, int i) {
			super(s, i);
		}
	}
}
