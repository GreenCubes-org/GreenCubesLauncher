package org.greencubes.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextArea;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.greencubes.util.I18n;

public class CrashReport extends Panel {

	private static final long serialVersionUID = 3420347234451905795L;

	public CrashReport(String error, Throwable t) {
		setBackground(new Color(0x2e3444));
		setPreferredSize(new Dimension(1024, 640));
		setLayout(new BorderLayout());
		StringWriter stringwriter = new StringWriter();
		if(t != null)
			t.printStackTrace(new PrintWriter(stringwriter));
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.get("----- При работе клиента GreenCubes произошла ошибка -----") + "\n");
		sb.append(I18n.get("Чтобы помочь улучшить GreenCubes и исправить ошибки, пожалуйста, пошлите нижеследующее сообщение в систему поддержки help.greencubes.org в раздел Баг-репортов. Спасибо.") + "\n");
		sb.append("\n--- CRASH REPORT ---\n");
		sb.append("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
		sb.append(error).append('\n');
		sb.append(stringwriter);
		sb.append("\n\nSys info:");
		sb.append("\nOS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
		sb.append("\nJava: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
		Runtime runtime = Runtime.getRuntime();
		sb.append("\nTotal memory: ").append(runtime.totalMemory()).append(", free memory: ").append(runtime.freeMemory()).append(", max memory: ").append(runtime.maxMemory()).append(", processors: ").append(runtime.availableProcessors());
		sb.append("\n\nThreads traces:");
		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		Iterator<Entry<Thread, StackTraceElement[]>> iterator = traces.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<Thread, StackTraceElement[]> e = iterator.next();
			sb.append("\n" + e.getKey().toString() + ":");
			for(StackTraceElement traceElement : e.getValue())
				sb.append("\n    at " + traceElement);
		}
		sb.append("\n--- CRASH REPORT END ---\n");
		sb.append("Спасибо, что помогаете GreenCubes стать лучше!");
		TextArea textarea = new TextArea(sb.toString(), 0, 0, 1);
		textarea.setFont(new Font("Monospaced", 0, 12));
		add(textarea, "Center");
	}
}