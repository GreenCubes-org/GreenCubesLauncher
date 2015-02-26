package org.greencubes.util.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class LogManager {

	private static AtomicBoolean initialized = new AtomicBoolean(false);

	public static Logger global = Logger.getLogger("");
	public static final IGLog log = new GLogSimple(global);
	public static ConsoleHandler handler;

	public static void initialize(File logFile) {
		if(initialized.getAndSet(true))
			return;
		if(logFile.exists())
			logFile.delete();
		Formatter loc = new OnlyTextFormatter();
		try {
			Handler mHandler = new StreamHandler(new FileOutputStream(logFile), loc) {
				@Override
				public synchronized void publish(LogRecord record) {
					super.publish(record);
					flush();
				}
			};
			addHandler(mHandler);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void addHandler(Handler handler) {
		global.addHandler(handler);
	}

	public static void removeHandler(Handler handler) {
		global.removeHandler(handler);
	}

	static {
		handler = new ConsoleHandler();
		for(Handler handler : global.getHandlers())
			global.removeHandler(handler);
		handler.setFormatter(new ShortConsoleLogFormatter());
		global.addHandler(handler);
		System.setOut(new PrintStream(new LoggerOutputStream(log, Level.INFO), true));
		System.setErr(new PrintStream(new LoggerOutputStream(log, Level.SEVERE), true));
		Logger.getLogger("").setLevel(Level.INFO);
	}
}