package org.greencubes.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.greencubes.launcher.LauncherOptions;
import org.greencubes.util.I18n;
import org.greencubes.util.Util;

public class CrashReport extends Panel {

	private static final long serialVersionUID = 3420347234451905795L;
	
	public static void processCrashReport(String error, Throwable t) {
		Frame crashFrame = new Frame();
		crashFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}
		});
		crashFrame.setLayout(new BorderLayout());
		crashFrame.add(new CrashReport(error, t), "Center");
		crashFrame.pack();
		crashFrame.setLocationRelativeTo(null);
		crashFrame.validate();
		crashFrame.setVisible(true);
	}

	public CrashReport(String error, Throwable t) {
		setBackground(new Color(0x2e3444));
		setPreferredSize(new Dimension(1024, 640));
		setLayout(new BorderLayout());
		StringWriter stringwriter = new StringWriter();
		if(t != null)
			t.printStackTrace(new PrintWriter(stringwriter));
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.get("----- При работе лаунчера GreenCubes произошла ошибка -----") + "\n");
		sb.append(I18n.get("Чтобы помочь улучшить GreenCubes и исправить ошибки, пожалуйста, пошлите нижеследующее сообщение в систему поддержки help.greencubes.org в раздел Баг-репортов. Спасибо.") + "\n");
		sb.append("\n--- CRASH REPORT ---\n");
		sb.append("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
		sb.append("Version: " + Main.BUILD_INFO + "\n");
		sb.append(error).append('\n');
		sb.append(stringwriter);
		sb.append("\n\nSys info:");
		sb.append("\nOS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
		sb.append("\nJava: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
		Runtime runtime = Runtime.getRuntime();
		try {
			com.sun.management.OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
			sb.append("\nTotal memory: ").append(Util.getBytesAsString(runtime.totalMemory())).append(", free memory: ").append(Util.getBytesAsString(runtime.freeMemory())).append(", max memory: ").append(Util.getBytesAsString(runtime.maxMemory())).append(", physical: ").append(Util.getBytesAsString(bean.getTotalPhysicalMemorySize())).append(", processors: ").append(runtime.availableProcessors());
		} catch(Throwable t1) { // If by reason unkown we can not fetch from internal restricted API
			sb.append("\nTotal memory: ").append(Util.getBytesAsString(runtime.totalMemory())).append(", free memory: ").append(Util.getBytesAsString(runtime.freeMemory())).append(", max memory: ").append(Util.getBytesAsString(runtime.maxMemory())).append(", processors: ").append(runtime.availableProcessors());
		}
		sb.append("\n--- CRASH REPORT END ---\n");
		sb.append("Спасибо, что помогаете GreenCubes стать лучше!");
		String s = sb.toString().replace("\r\n","\n").replace("\n", System.getProperty("line.separator"));
		TextArea textarea = new TextArea(s, 0, 0, 1);
		textarea.setFont(new Font("Monospaced", 0, 12));
		add(textarea, "Center");
		if(LauncherOptions.debug)
			System.out.println(s);
	}
}