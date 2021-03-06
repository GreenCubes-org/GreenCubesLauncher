package org.greencubes.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

public class ClientOld extends Client {
	
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
	
	public ClientOld(String name, String localizedName) {
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
		if(isSinglePlayerModeAllowed())
			servers.add(new Server(I18n.get("servers.singleplayer"), null, 0));
		if(isSinglePlayerModeAllowed() && LauncherOptions.isOffline())
			selectServer(servers.get(servers.size() - 1));
		else
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
		if(OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX)
			return new File(Util.getDocumentsDir(), "GreenCubes/oldclient/").getAbsoluteFile();
		return new File("oldclient/").getAbsoluteFile();
	}
	
	protected List<String> getLaunchParameters(String username, String session, Server server) {
		List<String> params = new ArrayList<String>();
		params.add("--directory");
		params.add(getWorkingDirectory().getPath());
		if(username != null) {
			params.add("--player");
			params.add(username);
			params.add("--session");
			if(session == null)
				params.add("-");
			else
				params.add("1-" + session);
		}
		if(server != null && server.address != null && server.port != 0) {
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
		return true;
	}
	
	@Override
	protected String getUrlName() {
		return "old";
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
	
	@Override
	public void doJob() {
		switch(status.getStatus()) {
		case OFFLINE:
			selectServer(null);
			status(Status.LOADING, "", -1f);
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
			fr = new InputStreamReader(new FileInputStream(new File(getWorkingDirectory(), "version.json")), "UTF-8");
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
			serverHash = LauncherOptions.getClientDownloader(ClientOld.this).readURL(Util.urlEncode("files/" + getUrlName() + "/version.json"));
		} catch(IOException e) {
			status(Status.ERROR, e.getLocalizedMessage(), -1f);
			if(Main.TEST)
				e.printStackTrace();
			return;
		}
		JSONObject remoteVersion;
		try {
			remoteVersion = new JSONObject(serverHash);
		} catch(JSONException e) {
			status(Status.ERROR, e.getLocalizedMessage(), -1f);
			if(Main.TEST)
				e.printStackTrace();
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
			GameFile file = GameFile.getFileOsSpecific(fileObject, workingDirectory, localHashes);
			if(file == null)
				continue;
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
			fw = new FileWriter(new File(workingDirectory, "version.json"));
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
	
	private void copyOldConfig() {
		File targetFile = new File(getWorkingDirectory(), "options.txt");
		File sourceFile = new File(getOldClientDir(), "options.txt");
		if(!targetFile.exists() && sourceFile.exists()) {
			try {
				Files.copy(sourceFile.toPath(), targetFile.toPath());
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		targetFile = new File(getWorkingDirectory(), "chat/config.yml");
		sourceFile = new File(getOldClientDir(), "chat/config.yml");
		if(!targetFile.exists() && sourceFile.exists()) {
			try {
				Files.copy(sourceFile.toPath(), targetFile.toPath());
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		targetFile = new File(getWorkingDirectory(), "servers.dat");
		sourceFile = new File(getOldClientDir(), "servers.dat");
		if(!targetFile.exists() && sourceFile.exists()) {
			try {
				Files.copy(sourceFile.toPath(), targetFile.toPath());
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String extractNatives() throws IOException {
		File nativeFolder = new File(getWorkingDirectory(), "natives");
		if(!nativeFolder.exists())
			nativeFolder.mkdir();
		for(File f : getWorkingDirectory().listFiles())
			if(f.isDirectory() && f.getName().startsWith("natives-"))
				f.deleteOnExit();
		if(nativeFolder.exists()) {
			for(File n : nativeFolder.listFiles()) {
				if(!n.delete()) {
					System.err.println("Error deleting native file " + n.getAbsolutePath() + ", using workaround");
					nativeFolder.deleteOnExit();
					nativeFolder = new File(getWorkingDirectory(), "natives-" + System.currentTimeMillis());
					break;
				}
			}
		}
		String nativesName = "natives_";
		switch(OperatingSystem.getCurrentPlatform()) {
		case OSX:
			nativesName += "mac";
			break;
		case WINDOWS:
			nativesName += "win";
			break;
		case LINUX:
			nativesName += "linux";
			break;
		default:
			throw new IllegalArgumentException("Unsupported OS");
		}
		nativesName += ".zip";

		ZipFile zipped = new ZipFile(new File(getWorkingDirectory(), nativesName));
		Enumeration<? extends ZipEntry> entities = zipped.entries();
		while(entities.hasMoreElements()) {
			ZipEntry entry = entities.nextElement();
			if(entry.isDirectory() || entry.getName().indexOf('/') != -1)
				continue;
		}
		entities = zipped.entries();
		while(entities.hasMoreElements()) {
			ZipEntry entry = entities.nextElement();

			if(entry.isDirectory() || entry.getName().indexOf('/') != -1)
				continue;
			File f = new File(nativeFolder, entry.getName());
			if(f.exists() && !f.delete())
				continue;
			InputStream in = zipped.getInputStream(zipped.getEntry(entry.getName()));
			OutputStream out;
			try {
				out = new FileOutputStream(f);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
			byte[] buffer = new byte[65536];
			int bufferSize;
			while((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, bufferSize);
			}
			in.close();
			out.close();
		}
		zipped.close();
		return nativeFolder.getAbsolutePath();
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
						String nativesPath;
						try {
							nativesPath = extractNatives();
						} catch(IOException e1) {
							status(Status.ERROR, e1.getLocalizedMessage(), -1f);
							break;
						}
						copyOldConfig();
						List<String> classPath = new ArrayList<String>();
						for(File f : new File(getWorkingDirectory(), "libraries/").listFiles())
							classPath.add(f.getAbsolutePath());
						List<String> command = new ArrayList<String>();
						command.add(OperatingSystem.getJavaExecutable(false));
						command.add("-Djava.net.preferIPv4Stack=true");
						command.add("-Djava.library.path=" + nativesPath);
						Reader fr = null;
						try {
							fr = new InputStreamReader(new FileInputStream(new File(getWorkingDirectory(), "params.json")), "UTF-8");
						} catch(IOException e) {
						}
						JSONObject paramsObj = null;
						if(fr == null) {
							paramsObj = new JSONObject();
						} else {
							try {
								paramsObj = new JSONObject(new JSONTokener(fr));
							} catch(JSONException e) {
								paramsObj = new JSONObject();
							} finally {
								Util.close(fr);
							}
						}
						JSONArray params = paramsObj.optJSONArray("params");
						if(params == null || params.length() == 0) {
							params = new JSONArray();
							params.put("-Xincgc");
							params.put("-Xms512M");
							params.put("-Xmx512M");
							try {
								paramsObj.put("params", params);
							} catch(JSONException e) {
								e.printStackTrace();
							}
						}
						FileWriter fw = null;
						try {
							fw = new FileWriter(new File(getWorkingDirectory(), "params.json"));
							paramsObj.write(fw);
						} catch(Exception e) {
							if(Main.TEST)
								e.printStackTrace();
						} finally {
							Util.close(fw);
						}
						for(int i = 0; i < params.length(); ++i)
							command.add(params.optString(i));
						command.add("-cp");
						StringBuilder cp = new StringBuilder();
						for(int i = 0; i < classPath.size(); ++i) {
							cp.append(classPath.get(i));
							cp.append(System.getProperty("path.separator"));
						}
						command.add(cp.toString());
						command.add("org.greencubes.util.Start");
						String session;
						JSONObject jo;
						try {
							jo = LauncherUtil.sessionRequest("action=session");
							session = jo.optString("ssid");
						} catch(Exception e) {
							if(LauncherOptions.isOffline()) {
								session = null;
							} else {
								status(Status.ERROR, e.getLocalizedMessage(), -1f);
								break;
							}
						}
						command.addAll(getLaunchParameters(LauncherOptions.sessionUser, session, getSelectedServer()));
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
				} catch(InterruptedException e) {
				}
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
				Downloader d = LauncherOptions.getClientDownloader(ClientOld.this);
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
	
	public static File getOldClientDir() {
		String appName = "greencubes";
		File baseDir = Util.getAppDir();
		File f;
		switch(OperatingSystem.getCurrentPlatform()) {
		case LINUX:
		case WINDOWS:
			f = new File(baseDir, "." + appName + "/");
			break;
		case OSX:
			f = new File(baseDir, appName + "/");
			break;
		default:
			f = new File(baseDir, "." + appName + "/");
			break;
		}
		if(!f.exists() && !f.mkdirs())
			throw new RuntimeException("The working directory could not be created: " + f.getPath());
		return f;
	}	
}
