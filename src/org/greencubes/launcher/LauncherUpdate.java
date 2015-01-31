package org.greencubes.launcher;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.greencubes.client.GameFile;
import org.greencubes.download.DownloadThread;
import org.greencubes.download.Downloader;
import org.greencubes.main.Main;
import org.greencubes.swing.AbstractWindowListener;
import org.greencubes.swing.GAWTUtil;
import org.greencubes.util.I18n;
import org.greencubes.util.OperatingSystem;
import org.greencubes.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LauncherUpdate {

	private static final long RENAME_DELAY = 2000;
	
	private JFrame frame;
	private JLabel statusPane = null;
	
	/**
	 * Should not be invoked in AWT thread
	 */
	public LauncherUpdate() {
		if(LauncherOptions.noUpdateLauncher) {
			launcherLoad();
		} else {
			createWindow();
			if(doUpdate()) // true means not updated and can launch further
				launcherLoad();
		}
	}
	
	private boolean doUpdate() {
		Downloader d = LauncherOptions.getDownloader();
		String serverHash;
		try {
			serverHash = d.readURL(Util.urlEncode("files/launcher/version.json"));
		} catch(IOException e) {
			updateError(100, e.getLocalizedMessage());
			return false;
		}
		JSONObject remoteVersion;
		try {
			remoteVersion = new JSONObject(serverHash);
		} catch(JSONException e) {
			updateError(101, e.getLocalizedMessage());
			return false;
		}
		JSONArray hashesArray = remoteVersion.optJSONArray("files");
		if(hashesArray == null) {
			updateError(102, null);
			return false;
		}
		List<GameFile> files = new ArrayList<GameFile>();
		File workingDirectory = new File("").getAbsoluteFile();
		boolean needUpdate = false;
		for(int i = 0; i < hashesArray.length(); ++i) {
			JSONObject fileObject = hashesArray.optJSONObject(i);
			if(fileObject == null) {
				updateError(103, null);
				return false;
			}
			GameFile file = GameFile.getFile(fileObject, workingDirectory, null);
			if(file.needUpdate)
				needUpdate = true;
			files.add(file);
		}
		if(needUpdate) {
			if(!downloadUpdate(files))
				return false;
			processUpdate(files);
			return false;
		}
		return true; // true means continue launching
	}
	
	@SuppressWarnings("unchecked")
	private void processUpdate(List<GameFile> files) {
		setStatus(I18n.get("launcher.update.loading"));
		JSONObject patchJson = new JSONObject();
		File patchDir = new File("patch");
		try {
			patchJson.put("patchdir", patchDir.getAbsolutePath());
			patchJson.put("silent", false);
			patchJson.put("exec", new JSONArray((Collection<Object>) (Collection<?>) getLaunchParameters()));
			patchJson.put("delay", RENAME_DELAY);
			JSONArray filesArray = new JSONArray();
			patchJson.put("files", filesArray);
			for(int i = 0; i < files.size(); ++i) {
				GameFile file = files.get(i);
				if(file.needUpdate) {
					File localFile = new File(patchDir, file.remoteFileUrl);
					if(localFile.exists()) {
						JSONObject fileObj = new JSONObject();
						fileObj.put("src", localFile.getAbsolutePath());
						fileObj.put("target", file.localFile.getAbsolutePath());
						filesArray.put(fileObj);
					}
				}
			}
		} catch(JSONException e) {
			updateError(301, e.getLocalizedMessage());
			return;
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File("patch.json"));
			patchJson.writeWithIdent(fw);
		} catch(Exception e) {
			updateError(302, e.getLocalizedMessage());
			return;
		} finally {
			Util.close(fw);
		}
		runPatcher();
	}
	
	private void runPatcher() {
		List<String> command = new ArrayList<String>();
		command.add(OperatingSystem.getJavaExecutable(false));
		command.add("-jar");
		command.add("patcher.jr");
		command.add(new File("patch.json").getAbsolutePath());
		frame.dispose();
		frame = null;
		ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
		try {
			Process p = pb.start();
			InputStreamReader reader = new InputStreamReader(p.getInputStream());
			BufferedReader buf = new BufferedReader(reader);
			String line = null;
			while(isRunning(p)) {
				try {
					while((line = buf.readLine()) != null) {
						if(line.startsWith("[SIG] ")) {
							if(line.equals("[SIG] LOADED")) {
								restart();
								return;
							}
						} else
							System.out.println(line);
					}
				} catch(IOException ex) {
					ex.printStackTrace();
				} finally {
					Util.close(buf);
				}
			}
			updateError(304, null);
			return;
		} catch(IOException e) {
			updateError(303, e.getLocalizedMessage());
			return;
		}
	}
	
	private void restart() {
		Main.close();
	}
	
	private boolean isRunning(Process p) {
		try {
			p.exitValue();
		} catch(IllegalThreadStateException e) {
			return true;
		}
		return false;
	}
	
	private boolean downloadUpdate(List<GameFile> files) {
		Downloader d = LauncherOptions.getDownloader();
		int updateSize = 0;
		for(int i = 0; i < files.size(); ++i) {
			GameFile file = files.get(i);
			if(file.needUpdate) {
				if(file.remoteFileSize >= 0)
					updateSize += file.remoteFileSize;
			}
		}
		File patchDir = new File("patch");
		if(patchDir.isFile()) {
			updateError(201, null);
			return false;
		} else if(!patchDir.exists() && !patchDir.mkdirs()) {
			updateError(202, null);
			return false;
		}
		setDownloadStatus(0, updateSize);
		long downloaded = 0;
		for(int i = 0; i < files.size(); ++i) {
			GameFile file = files.get(i);
			if(file.needUpdate) {
				File localFile = new File(patchDir, file.remoteFileUrl);
				int trys = 0;
				do {
					DownloadThread dt = new DownloadThread(localFile, Util.urlEncode("files/launcher/" + file.remoteFileUrl), d);
					dt.start();
					while(!dt.downloaded) {
						try {
							Thread.sleep(100L);
						} catch(InterruptedException e) {}
						setDownloadStatus(downloaded + d.bytesDownloaded, updateSize);
					}
					if(dt.lastError == null)
						break;
					if(++trys > 3) {
						updateError(104, null);
						return false;
					}
					setStatus(I18n.get("launcher.update.repeat", trys, 3));
				} while(true);
				downloaded += d.bytesDownloaded;
			}
		}
		return true;
	}
	
	private void setDownloadStatus(final long current, final long max) {
		setStatus(I18n.get("launcher.update.downloading", Util.getBytesAsString(current), Util.getBytesAsString(max)));
	}
	
	private void setStatus(final String s) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusPane.setText(s);
			}
		});
	}
	
	private void createWindow() {
		frame = new JFrame(I18n.get("title"));
		frame.setIconImages(LauncherOptions.getIcons());
		frame.setUndecorated(true);
		frame.setResizable(false);
		
		frame.add(new JPanel() {{
			setPreferredSize(new Dimension(400, 100));
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			setBackground(new Color(11, 24, 24, 255));
			setBorder(BorderFactory.createLineBorder(new Color(35, 61, 58, 255), 1));
			add(new JPanel() {{
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				setOpaque(false);
				add(new JLabel() {{
					setText(I18n.get("launcher.update.title"));
					setOpaque(false);
					setBackground(new Color(0, 0, 0, 0));
					setForeground(new Color(236, 255, 255, 255));
					setFont(new Font("ClearSans Light", Font.BOLD, 20));
					disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
					setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				}});
				add(statusPane = new JLabel() {{
					setText(I18n.get("launcher.update.check"));
					setOpaque(false);
					setBackground(new Color(0, 0, 0, 0));
					setForeground(new Color(236, 255, 255, 255));
					setFont(new Font("ClearSans Light", Font.PLAIN, 14));
					disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
					setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				}});
				
				add(Box.createVerticalGlue());
			}});
		}});
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(new AbstractWindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				Main.close();
			}
		});
	}
	
	private void updateError(int code, String message) {
		saveUpdateErrorMessage(code, message);
		frame.dispose();
		frame = null;
		System.err.println("Update error " + code + ": " + message);
		int answer = -1;
		if(code >= 100 && code < 200) {
			// Ошибка связи с сервером
			answer = GAWTUtil.showDialog(I18n.get("launcher.update.error.title"), I18n.get("launcher.update.error.server"), new String[] {I18n.get("launcher.update.error.continue"),
				I18n.get("launcher.update.error.repeat"), I18n.get("launcher.update.error.exit")}, JOptionPane.ERROR_MESSAGE, 300);
		} else if(code >= 200 && code < 300) {
			// TODO : Уточнить сообщения об ошибке
			answer = GAWTUtil.showDialog(I18n.get("launcher.update.error.title"), I18n.get("launcher.update.error.update", code), new String[] {I18n.get("launcher.update.error.continue"),
				I18n.get("launcher.update.error.repeat"), I18n.get("launcher.update.error.exit")}, JOptionPane.ERROR_MESSAGE, 300);
		} else if(code >= 300 && code < 400) {
			// TODO : Уточнить сообщения об ошибке
			answer = GAWTUtil.showDialog(I18n.get("launcher.update.error.title"), I18n.get("launcher.update.error.update", code), new String[] {I18n.get("launcher.update.error.continue"),
				I18n.get("launcher.update.error.repeat"), I18n.get("launcher.update.error.exit")}, JOptionPane.ERROR_MESSAGE, 300);
		} else {
			// TODO : Уточнить сообщения об ошибке
			answer = GAWTUtil.showDialog(I18n.get("launcher.update.error.title"), I18n.get("launcher.update.error.update", code), new String[] {I18n.get("launcher.update.error.continue"),
				I18n.get("launcher.update.error.repeat"), I18n.get("launcher.update.error.exit")}, JOptionPane.ERROR_MESSAGE, 300);
		}
		if(answer == 0) {
			launcherLoad();
		} else if(answer == 1) {
			new Thread() {
				@Override
				public void run() {
					new LauncherUpdate();
				}
			}.start();
		} else {
			Main.close();
		}
	}
	
	private void saveUpdateErrorMessage(int code, String message) {
		FileWriter fw = null;
		try {
			fw = new FileWriter("launcherUpdateError.txt");
			StringBuilder sb = new StringBuilder();
			sb.append("-- LAUNCHER UPDATE ERROR " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " --\n\n");
			sb.append("Error code: " + code + ", message: " + message + ", version: " + Main.BUILD_INFO + "\n\n");
			StringWriter stringwriter = new StringWriter();
			Throwable t = new Throwable();
			t.printStackTrace(new PrintWriter(stringwriter));
			sb.append("Stacktrace: \n");
			sb.append(stringwriter.toString());
			sb.append("\n\nSys info:");
			sb.append("\nOS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version"));
			sb.append("\nJava: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
			Runtime runtime = Runtime.getRuntime();
			sb.append("\nTotal memory: ").append(runtime.totalMemory()).append(", free memory: ").append(runtime.freeMemory()).append(", max memory: ").append(runtime.maxMemory()).append(", processors: ").append(runtime.availableProcessors());
			sb.append("\n\n-- END --\n");
			fw.write(sb.toString().replace("\r\n","\n").replace("\n", System.getProperty("line.separator")));
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			Util.close(fw);
		}
	}
	
	private List<String> getLaunchParameters() {
		List<String> command = new ArrayList<String>();
		command.add(OperatingSystem.getJavaExecutable(false));
		command.add("-jar");
		command.add(Main.launcherFile.getAbsolutePath());
		command.add("-updated");
		return command;
	}
	
	/**
	 * Should not be invoked in AWT thread
	 */
	public void launcherLoad() {
		new LauncherLogin(frame); // Send current frame so next window can destroy it when ready
	}
}
