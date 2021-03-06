package org.greencubes.client;

import static org.greencubes.util.Util.getDocumentsDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

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
	
	private final ClinetStatus status;
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
		status = new ClinetStatus();
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
			Reader fr = null;
			try {
				fr = new InputStreamReader(new FileInputStream(serversFile), "UTF-8");
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
	
	public File getUserDirectory() {
		JSONObject config = Main.getConfig();
		try {
			if(config.has("userdir"))
				return new File(getWorkingDirectory(), config.getString("userdir"));
		} catch(JSONException e) {}
		return new File(getDocumentsDir(), "GreenCubes/").getAbsoluteFile();
	}
	
	protected List<String> getLaunchParameters(String username, String session, Server server) {
		List<String> params = new ArrayList<String>();
		params.add("--userdir");
		params.add(getUserDirectory().getAbsolutePath());
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
	public IClientStatus getStatus() {
		return status;
	}
	
	@Override
	public void load(LauncherMain launcherWindow) {
		super.load(launcherWindow);
		synchronized(worker) {
			if(status.getStatus() != Status.UPDATING)
				status(Status.CHECK, "", -1f);
			if(!worker.isAlive())
				worker.start();
		}
	}
	
	protected String getUrlName() {
		return "main";
	}
	
	@Override
	public void doJob() {
		switch(status.getStatus()) {
		case OFFLINE:
			break;
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
	
	private void prepareClientUpdate() {
		worker.lastUpdateCheck = System.currentTimeMillis();
		File workingDirectory = getWorkingDirectory();
		if(!workingDirectory.exists()) {
			if(!workingDirectory.mkdirs()) {
				status(Status.ERROR, I18n.get("client.update.error.folder"), -1f);
				return;
			}
		}
		boolean needUpdate = false;
		Map<String, byte[]> localHashes = new HashMap<String, byte[]>();
		List<String> remoteFiles = new ArrayList<String>();
		List<GameFile> newGameFiles = new ArrayList<GameFile>();
		Reader fr = null;
		try {
			fr = new InputStreamReader(new FileInputStream(new File(workingDirectory, "version.json")), "UTF-8");
		} catch(IOException e) {}
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
			serverHash = LauncherOptions.getClientDownloader(this).readURL(Util.urlEncode("files/" + getUrlName() + "/version.json"));
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
			status(Status.ERROR, I18n.get("client.update.error.generic", 1), -1f);
			return;
		}
		
		for(int i = 0; i < hashesArray.length(); ++i) {
			JSONObject fileObject = hashesArray.optJSONObject(i);
			if(fileObject == null) {
				status(Status.ERROR, I18n.get("client.update.error.generic2", 2, i), -1f);
				return;
			}
			GameFile file = GameFile.getFile(fileObject, workingDirectory, localHashes);
			if(file.needUpdate) {
				//System.out.println("File to update: " + file.remoteFileUrl + " (local: " + Util.toString(file.localmd5) + ", remote: " + Util.toString(file.remotemd5) + ")");
				needUpdate = true;
			}
			newGameFiles.add(file);
			remoteFiles.add(fileObject.optString("name"));
		}
		Iterator<Entry<String, byte[]>> oldFilesIterator = localHashes.entrySet().iterator();
		while(oldFilesIterator.hasNext()) {
			Entry<String, byte[]> e = oldFilesIterator.next();
			if(!remoteFiles.contains(e.getKey())) {
				//System.out.println("File to remove: " + e.getKey());
				// File not found remote, create game file so we can delete it later
				newGameFiles.add(new GameFile(new File(workingDirectory, e.getKey()), null, e.getValue(), null));
			}
		}
		gameFiles.clear();
		gameFiles.addAll(newGameFiles);
		assert checkGameFilesCorrectness();
		if(needUpdate)
			status(Status.CHECK, I18n.get("client.update.counting"), 0f);
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
			//status(Status.CHECK, I18n.get("client.update.counting"), (float) i / gameFiles.size());
		}
		if(filesToDownload != 0) {
			status(Status.NEED_UPDATE, I18n.get("client.update.required", filesToDownload, (isEstimate ? "~" : "") + Util.getBytesAsString(bytesToDownload)), -1f);
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
				status(Status.ERROR, I18n.get("client.update.error.folder"), -1f);
			else
				status(Status.NEED_UPDATE, I18n.get("client.update.ready"), -1f);
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
			fw = new FileWriter(new File(getWorkingDirectory(), "version.json"));
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
	
	private void moveUserFolder() {
		if(new File("user/").exists() && !getUserDirectory().exists()) {
			try {
				Files.move(new File("user/").toPath(), getUserDirectory().toPath());
			} catch(IOException e) {}
		}
	}
	
	private class ClinetStatus implements IClientStatus {
		
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
				case OFFLINE:
					break;
				case CHECK:
					if(LauncherOptions.isOffline())
						status(Status.OFFLINE, I18n.get(Status.OFFLINE.statusName), -1f);
					else
						prepareClientUpdate();
					break;
				case LOADING:
					if(processMonitor == null) {
						moveUserFolder();
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
					} else if(processMonitor.isStarted()) {
						status(Status.RUNNING, "", -1f);
						Main.performWindowAction(LauncherOptions.onClientStart);
					} else if(!processMonitor.isProcessRunning()) { // If process crashed before fully started
						status(Status.RUNNING, "", -1f);
					}
					break;
				case NEED_UPDATE:
					if(LauncherOptions.autoUpdate && !updateAborted)
						status(Status.UPDATING, "", 0f);
					break;
				case READY:
					// Check updates every 10 minutes
					if(!LauncherOptions.isOffline() && lastUpdateCheck + 600000 < System.currentTimeMillis())
						prepareClientUpdate();
					break;
				case RUNNING:
					if(processMonitor == null || !processMonitor.isProcessRunning()) {
						processMonitor = null;
						Main.undoWindowAction(LauncherOptions.onClientStart);
						status(Status.CHECK, "", -1f);
					}
					break;
				case UPDATING:
					updateAborted = false;
					if(downloader == null) {
						downloader = new FileDownloader();
						downloader.start();
					} else {
						//@formatter:off
						status(Status.UPDATING, I18n.get("client.update.downloading", filesDownloaded,
								filesToDownload, (isEstimate ? "~" : "") + Util.getBytesAsString(bytesDownloaded), Util.getBytesAsString(bytesToDownload))
								+ (downloader.error.isEmpty() ? "" : "\n" + downloader.error), ((float) filesDownloaded / filesToDownload + (float) bytesDownloaded / bytesToDownload) / 2);
						//@formatter:on
					}
					if(downloader.finished) {
						downloader = null;
						status(Status.CHECK, I18n.get(Status.CHECK.statusName), -1f);
					}
					break;
				default:
					break;
				}
				try {
					Thread.sleep(20L);
				} catch(InterruptedException e) {}
			}
		}
	}
	
	private class FileDownloader extends Thread {
		
		private static final int MAX_REPEATS = 5;
		
		private boolean abort = false;
		public boolean finished = false;
		//public GameFile downloading;
		public String error = "";
		public int repeats = 0;
		
		public void abort() {
			abort = true;
		}
		
		@Override
		public void run() {
			boolean setFinished = false;
			try {
				Downloader d = LauncherOptions.getClientDownloader(ClientMain.this);
				filesDownloaded = 0;
				bytesDownloaded = 0;
				Queue<GameFile> queue = new ArrayBlockingQueue<>(gameFiles.size());
				queue.addAll(gameFiles);
				while(!abort) {
					GameFile gf = queue.poll();
					if(gf == null) {
						setFinished = true;
						return;
					}
					if(gf.needUpdate) {
						//System.out.println("Need update: " + gf.remoteFileUrl);
						if(gf.remotemd5 != null) {
							//System.out.println("Downloading: " + gf.remoteFileUrl);
							//downloading = gf;
							repeats = 0;
							while(true) {
								try {
									gf.downloadFile(d, "files/" + getUrlName() + "/");
									//System.out.println("Downloaded: " + gf.remoteFileUrl + " (new local: " + Util.toString(gf.localmd5) + ")");
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
				finished = setFinished;
			}
		}
	}
	
}
