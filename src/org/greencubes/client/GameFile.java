package org.greencubes.client;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.bouncycastle.util.Arrays;
import org.greencubes.download.Downloader;
import org.greencubes.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

public class GameFile {
	
	public File localFile;
	public String remoteFileUrl;
	public byte[] localmd5;
	public byte[] remotemd5;
	public boolean needUpdate;
	public int remoteFileSize;
	
	public GameFile(File localFile, String remoteFileUrl, byte[] localmd5, byte[] remotemd5) {
		this.localFile = localFile;
		this.remoteFileUrl = remoteFileUrl;
		this.localmd5 = localmd5;
		this.remotemd5 = remotemd5;
		if(!this.localFile.exists()) {
			needUpdate = true;
		} else {
			if(this.localmd5 == null && this.remotemd5 != null) {
				try {
					this.localmd5 = Util.createChecksum(this.localFile);
					needUpdate = !Arrays.areEqual(this.localmd5, this.remotemd5);
				} catch(IOException e) {
					needUpdate = true;
				}				
			} else if(this.localmd5 != null && (this.remotemd5 == null || this.remoteFileUrl == null)) {
				needUpdate = true;
			} else {
				needUpdate = !Arrays.areEqual(this.localmd5, this.remotemd5);
			}
		}
	}
	
	@Override
	public String toString() {
		return remoteFileUrl;
	}
	
	public JSONObject getJSONObject(File baseDirectory) throws JSONException {
		JSONObject fileObject = new JSONObject();
		String name = Util.getRelativePath(baseDirectory, localFile);
		fileObject.put("name", name);
		fileObject.put("hash", Util.byteArrayToHex(localmd5));
		return fileObject;
	}
	
	public void downloadFile(Downloader downloader, String prefix) throws IOException {
		downloader.downloadFile(localFile, Util.urlEncode(prefix + remoteFileUrl));
		localmd5 = Util.createChecksum(localFile);
		needUpdate = false;
	}
	
	public static GameFile getFile(JSONObject fileObject, File baseDirectory, Map<String, byte[]> localHashes) {
		byte[] hash = Util.hexStringToByteArray(fileObject.optString("hash"));
		String name = fileObject.optString("name");
		GameFile file = new GameFile(new File(baseDirectory, name), name, localHashes.get(name), hash);
		file.remoteFileSize = fileObject.optInt("length", -1);
		return file;
	}
}
