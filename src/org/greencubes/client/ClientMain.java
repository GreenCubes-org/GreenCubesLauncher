package org.greencubes.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import org.greencubes.client.IClientStatus.Status;
import org.greencubes.download.Downloader;
import org.greencubes.launcher.LauncherMain;
import org.greencubes.launcher.LauncherOptions;
import org.greencubes.launcher.LauncherUtil;
import org.greencubes.main.Main;
import org.greencubes.util.I18n;
import org.greencubes.util.OperatingSystem;
import org.greencubes.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ClientMain extends Client {
	
	private final MainClinetStatus status;
	private final List<Server> servers = new ArrayList<Server>();
	private final ClientWorker worker = new ClientWorker();
	private final List<GameFile> gameFiles = new ArrayList<GameFile>();
	private FileDownloader downloader;
	private int filesToDownload;
	private long bytesToDownload;
	private boolean isEstimate = false;
	private int filesDownloaded = 0;
	private long bytesDownloaded = 0;
	private ProcessMonitorThread processMonitor;
	private JSONObject currentVersion;
	private JSONObject remoteVersion;
	
	public ClientMain(String name, String localizedName) {
		super(name, localizedName);
		status = new MainClinetStatus();
	}
	
	@Override
	public void updateServerList() {
		servers.clear();
		addServers(new File(getWorkingDirectory(), "serverlist.json"));
		addServers(new File(getWorkingDirectory(), "customservers.json"));
		if(LauncherOptions.showLocalServer || Main.TEST)
			servers.add(new Server(I18n.get("servers.local"), "127.0.0.1", 25565));
		selectServer(servers.get(0));
	}
	
	private void addServers(File serversFile) {
		if(serversFile.exists()) {
			FileReader fr = null;
			try {
				fr = new FileReader(serversFile);
				JSONObject obj = new JSONObject(new JSONTokener(fr));
				JSONArray serversArray = obj.optJSONArray("servers");
				for(int i = 0; i < serversArray.length(); ++i) {
					JSONObject serverObj = serversArray.getJSONObject(i);
					JSONObject names = serverObj.optJSONObject("names");
					String selectedName = "Unknown";
					if(names != null) {
						if(names.has("lang") && I18n.hasLang(names.optString("lang"))) {
							selectedName = I18n.get(names.optString("lang"));
						} else if(names.has(I18n.currentLanguage)) {
							selectedName = names.optString(I18n.currentLanguage);
						} else if(names.has("lang")) {
							selectedName = names.optString("lang");
						}
					}
					servers.add(new Server(selectedName, serverObj.optString("address"), serverObj.optInt("port")));
				}
			} catch(IOException e) {
				// Ignore
			} catch(JSONException e) {
				// Ignore
			} finally {
				Util.close(fr);
			}
		}
	}
	
	@Override
	public File getWorkingDirectory() {
		return new File("").getAbsoluteFile();
	}
	
	protected List<String> getLaunchParameters(String username, String session, Server server) {
		List<String> params = new ArrayList<String>();
		params.add("--fullscreen");
		if(username != null) {
			params.add("--session");
			params.add(username);
			if(session == null)
				params.add("-");
			else
				params.add("1-" + session);
		}
		if(server != null) {
			params.add("--connect");
			params.add(server.getFullAddress());
		}
		return params;
	}
	
	@Override
	public List<Server> getServers() {
		return servers;
	}
	
	@Override
	public boolean isSinglePlayerModeAllowed() {
		return false;
	}
	
	@Override
	public void openBrowserPage(final WebEngine browser) {
		Platform.runLater(new Runnable() {
            @Override 
            public void run() {
                browser.load("https://greencubes.org/?action=clientpage&client=main");
            }
        });
	}
	
	@Override
	public IClientStatus getStatus() {
		return status;
	}
	
	@Override
	public void load(LauncherMain launcherWindow) {
		super.load(launcherWindow);
		synchronized(worker) {
			if(!worker.isAlive())
				worker.start();
		}
	}
	
	@Override
	public void doJob() {
		switch(status.getStatus()) {
		case CHECK:
			break;
		case LOADING:
			break;
		case NEED_UPDATE:
			status(Status.UPDATING, "", 0f);
			break;
		case READY:
			status(Status.LOADING, "", -1f);
			break;
		case RUNNING:
			break;
		case UPDATING:
			worker.updateAborted = true;
			status(Status.CHECK, "", -1f);
			if(downloader != null) {
				downloader.abort();
				downloader = null;
			}
			break;
		default:
			status(Status.CHECK, "", -1f);
			break;
		}
	}
	
	public class MainClinetStatus implements IClientStatus {
		
		private Status status = Status.CHECK;
		private String title = "";
		private float progress = -1f;
		
		@Override
		public Status getStatus() {
			return status;
		}
		
		@Override
		public String getStatusTitle() {
			return title;
		}
		
		@Override
		public float getStatusProgress() {
			return progress;
		}
	}
	
	private void prepareClientUpdate() {
		worker.lastUpdateCheck = System.currentTimeMillis();
		File workingDirectory = getWorkingDirectory();
		if(!workingDirectory.exists()) {
			if(!workingDirectory.mkdirs())
				status(Status.ERROR, I18n.get("Невозможно создать папку клиента"), -1f);
			else
				status(Status.NEED_UPDATE, I18n.get("Готово к установке"), 0f);
			return;
		}
		boolean needUpdate = false;
		Map<String, byte[]> localHashes = new HashMap<String, byte[]>();
		List<String> remoteFiles = new ArrayList<String>();
		List<GameFile> newGameFiles = new ArrayList<GameFile>();
		FileReader fr = null;
		try {
			fr = new FileReader(new File("version.json"));
		} catch(IOException e) {
		}
		JSONObject localVersion = null;
		if(fr == null) {
			localVersion = new JSONObject();
		} else {
			try {
				localVersion = new JSONObject(new JSONTokener(fr));
			} catch(JSONException e) {
				localVersion = new JSONObject();
			} finally {
				Util.close(fr);
			}
		}
		currentVersion = localVersion;
		JSONArray hashesArray = localVersion.optJSONArray("files");
		if(hashesArray != null)
			for(int i = 0; i < hashesArray.length(); ++i) {
				JSONObject fileObject = hashesArray.optJSONObject(i);
				if(fileObject != null)
					localHashes.put(fileObject.optString("name"), Util.hexStringToByteArray(fileObject.optString("hash")));
			}
		// Load hases from server
		String serverHash;
		try {
			serverHash = LauncherOptions.getClientDownloader(ClientMain.this).readURL(Util.urlEncode("files/main/version.json"));
		} catch(IOException e) {
			status(Status.ERROR, e.getLocalizedMessage(), -1f);
			return;
		}
		JSONObject remoteVersion;
		try {
			remoteVersion = new JSONObject(serverHash);
		} catch(JSONException e) {
			status(Status.ERROR, e.getLocalizedMessage(), -1f);
			return;
		}
		this.remoteVersion = remoteVersion;
		hashesArray = remoteVersion.optJSONArray("files");
		if(hashesArray == null) {
			status(Status.ERROR, I18n.get("Ошибка обновления #1"), -1f);
			return;
		}
		
		for(int i = 0; i < hashesArray.length(); ++i) {
			JSONObject fileObject = hashesArray.optJSONObject(i);
			if(fileObject == null) {
				status(Status.ERROR, I18n.get("Ошибка обновления #2 (" + i + ")"), -1f);
				return;
			}
			GameFile file = GameFile.getFile(fileObject, workingDirectory, localHashes);
			if(file.needUpdate)
				needUpdate = true;
			newGameFiles.add(file);
			remoteFiles.add(fileObject.optString("name"));
		}
		Iterator<Entry<String, byte[]>> oldFilesIterator = localHashes.entrySet().iterator();
		while(oldFilesIterator.hasNext()) {
			Entry<String, byte[]> e = oldFilesIterator.next();
			if(!remoteFiles.contains(e.getKey())) {
				// File not found remote, create game file so we can delete it later
				newGameFiles.add(new GameFile(new File(workingDirectory, e.getKey()), null, e.getValue(), null));
			}
		}
		gameFiles.clear();
		gameFiles.addAll(newGameFiles);
		assert checkGameFilesCorrectness();
		if(needUpdate)
			status(Status.CHECK, I18n.get("Подсчёт размера обновления..."), 0f);
		// Fetch file sizes
		bytesToDownload = 0;
		filesToDownload = 0;
		isEstimate = false;
		for(int i = 0; i < gameFiles.size(); ++i) {
			GameFile gf = gameFiles.get(i);
			if(gf.remotemd5 != null && gf.needUpdate) {
				if(gf.remoteFileSize < 0) {
					isEstimate = true;
				} else {
					bytesToDownload += gf.remoteFileSize;
				}
				filesToDownload++;
			}
			status(Status.CHECK, I18n.get("Подсчёт размера обновления..."), (float) i / gameFiles.size());
		}
		if(filesToDownload != 0) {
			status(Status.NEED_UPDATE, I18n.get("Требуется обновление (" + filesToDownload + " файлов, " + (isEstimate ? "~" : "") + Util.getBytesAsString(bytesToDownload) + ")"), 0f);
		} else {
			updateServerList();
			status(Status.READY, I18n.get(Status.READY.statusName), -1f);
		}
	}
	
	/**
	 * Test. Check if some files are present two or more times in game files list
	 * @return
	 */
	private boolean checkGameFilesCorrectness() {
		for(int i = 0; i < gameFiles.size(); ++i) {
			GameFile f1 = gameFiles.get(i);
			for(int i1 = 0; i1 < gameFiles.size(); ++i1) {
				if(i != i1) {
					GameFile f2 = gameFiles.get(i1);
					if(f2.equals(f1))
						return false;
				}
			}
		}
		return true;
	}
	
	private void updateVersionFile() {
		File workingDirectory = getWorkingDirectory();
		if(!workingDirectory.exists()) {
			if(!workingDirectory.mkdirs())
				status(Status.ERROR, I18n.get("Невозможно создать папку клиента"), -1f);
			else
				status(Status.NEED_UPDATE, I18n.get("Готово к установке"), 0f);
			return;
		}
		currentVersion = remoteVersion;
		remoteVersion = null;
		FileWriter fw = null;
		try {
			JSONArray newFilesList = new JSONArray();
			for(int i = 0; i < gameFiles.size(); ++i) {
				GameFile gf = gameFiles.get(i);
				if(gf.remoteFileUrl != null) {
					JSONObject fileObject = gf.getJSONObject(workingDirectory);
					newFilesList.put(fileObject);
				}
			}
			currentVersion.put("files", newFilesList);
			fw = new FileWriter(new File("version.json"));
			currentVersion.write(fw);
		} catch(Exception e) {
			if(Main.TEST)
				e.printStackTrace();
		} finally {
			Util.close(fw);
		}
	}
	
	private void status(Status status, String title, float progress) {
		this.status.status = status;
		this.status.title = title;
		this.status.progress = progress;
		clientStatusUpdate();
	}
	
	private class ClientWorker extends Thread {
		
		private long lastUpdateCheck = 0;
		private boolean updateAborted = false; // To prevent autoupdate if update is aborted
		
		private ClientWorker() {
			super(Main.TEST ? "New Client Worker Thread" : "Thread-" + (int) (Math.random() * 20));
			setDaemon(true);
		}
		
		@Override
		public void run() {
			while(true) {
				switch(status.getStatus()) {
				case CHECK:
					prepareClientUpdate();
					break;
				case LOADING:
					if(processMonitor == null) {
						List<String> classPath = new ArrayList<String>();
						try {
							Scanner fr = new Scanner(new File(getWorkingDirectory(), "libraries/liborder.txt"));
							while(fr.hasNext())
								classPath.add(fr.nextLine());
							fr.close();
						} catch(FileNotFoundException e1) {
							throw new RuntimeException(e1);
						}
						List<String> command = new ArrayList<String>();
						command.add(OperatingSystem.getJavaExecutable(false));
						command.add("-Xincgc");
						command.add("-Djava.net.preferIPv4Stack=true");
						command.add("-Xms1024M");
						command.add("-Xmx1024M");
						command.add("-cp");
						StringBuilder cp = new StringBuilder();
						for(int i = 0; i < classPath.size(); ++i) {
							cp.append(new File(getWorkingDirectory(), "libraries/" + classPath.get(i)).getAbsolutePath());
							cp.append(System.getProperty("path.separator"));
						}
						cp.append("client.jar");
						command.add(cp.toString());
						command.add("org.greencubes.client.Main");
						JSONObject jo;
						try {
							jo = LauncherUtil.sessionRequest("action=session");
						} catch(Exception e) {
							status(Status.ERROR, e.getLocalizedMessage(), -1f);
							break;
						}
						command.addAll(getLaunchParameters(LauncherOptions.sessionUser, jo.optString("ssid"), getSelectedServer()));
						ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
						pb.directory(getWorkingDirectory());
						try {
							if(LauncherOptions.debug)
								System.err.println("Starting client with " + Util.toString(command));
							Process p = pb.start();
							processMonitor = new ProcessMonitorThread(p);
							processMonitor.start();
						} catch(IOException e) {
							status(Status.ERROR, e.getLocalizedMessage(), -1f);
							break;
						}
					} else if(processMonitor.isStarted())
						status(Status.RUNNING, "", -1f);
					break;
				case NEED_UPDATE:
					if(LauncherOptions.autoUpdate && !updateAborted)
						status(Status.UPDATING, "", 0f);
					break;
				case READY:
					// Check updates every 10 minutes
					if(lastUpdateCheck + 600000 < System.currentTimeMillis())
						prepareClientUpdate();
					break;
				case RUNNING:
					if(processMonitor == null || processMonitor.getExitValue() >= 0)
						status(Status.CHECK, "", -1f);
					break;
				case UPDATING:
					updateAborted = false;
					if(downloader == null) {
						downloader = new FileDownloader();
						downloader.start();
					} else {
						//@formatter:off
						status(Status.UPDATING, "Загрузка " + downloader.downloading + " (" + Util.getBytesAsString(LauncherOptions.getClientDownloader(ClientMain.this).bytesDownloaded)
								+ "/" + Util.getBytesAsString(LauncherOptions.getClientDownloader(ClientMain.this).bytesDownloaded) + "), прогресс: " + filesDownloaded
								+ "/" + filesToDownload + ", " + (isEstimate ? "~" : "") + Util.getBytesAsString(bytesDownloaded) + "/" + Util.getBytesAsString(bytesToDownload)
								+ " " + downloader.error, ((float) filesDownloaded / filesToDownload + (float) bytesDownloaded / bytesToDownload) / 2);
						//@formatter:on
					}
					if(downloader.finished) {
						downloader = null;
						status(Status.CHECK, "Проверка...", -1f);
					}
					break;
				default:
					break;
				}
				try {
					Thread.sleep(20L);
				} catch(InterruptedException e) {
				}
			}
		}
	}
	
	private class FileDownloader extends Thread {
		
		private static final int MAX_REPEATS = 5;
		
		private boolean abort = false;
		public boolean finished = false;
		public GameFile downloading;
		public String error = "";
		public int repeats = 0;
		
		public void abort() {
			abort = true;
		}
		
		@Override
		public void run() {
			try {
				Downloader d = LauncherOptions.getClientDownloader(ClientMain.this);
				filesDownloaded = 0;
				bytesDownloaded = 0;
				Queue<GameFile> queue = new ArrayBlockingQueue<>(gameFiles.size());
				queue.addAll(gameFiles);
				while(!abort) {
					GameFile gf = queue.poll();
					if(gf == null) {
						finished = true;
						return;
					}
					if(gf.needUpdate) {
						if(gf.remotemd5 != null) {
							downloading = gf;
							repeats = 0;
							while(true) {
								try {
									gf.downloadFile(d, "files/main/");
									//d.downloadFile(gf.localFile, Util.urlEncode("files/main/" + gf.remoteFileUrl));
									//gf.needUpdate = false;
									//gf.localmd5 = Util.createChecksum(gf.localFile);
									filesDownloaded++;
									bytesDownloaded += d.bytesDownloaded;
									break;
								} catch(IOException e) {
									if(repeats++ > MAX_REPEATS) {
										status(Status.ERROR, e.getLocalizedMessage(), -1f);
										return;
									}
									error = I18n.get("Error. Repeats: " + repeats);
								}
							}
							error = "";
						} else {
							if(!gf.localFile.delete())
								gf.localFile.deleteOnExit();
							gf.needUpdate = false;
						}
					}
				}
			} finally {
				updateVersionFile();
			}
		}
	}
	
}
