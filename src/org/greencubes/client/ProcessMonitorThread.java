package org.greencubes.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.greencubes.util.Util;

public class ProcessMonitorThread extends Thread {
	
	private final Process process;
	private int exitValue = -1;
	private boolean started = false;
	
	public ProcessMonitorThread(Process process) {
		this.process = process;
	}
	
	public int getExitValue() {
		return exitValue;
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public boolean isProcessRunning() {
		try {
			exitValue = this.process.exitValue();
		} catch(IllegalThreadStateException ex) {
			return true;
		}
		return false;
	}
	
	protected void processSignal(String sig) {
		System.err.println("Signal: " + sig);
		if(sig.equals("STARTED")) {
			synchronized(process) {
				started = true;
				process.notifyAll();
			}
		}
	}
	
	@Override
	public void run() {
		InputStreamReader reader = new InputStreamReader(this.process.getInputStream());
		BufferedReader buf = new BufferedReader(reader);
		String line = null;
		while(isProcessRunning()) {
			try {
				while((line = buf.readLine()) != null) {
					if(line.startsWith("[INFO] SIG: ", 9))
						processSignal(line.substring(21));
					else
						System.out.println(line);
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			} finally {
				Util.close(buf);
			}
		}
	}
}