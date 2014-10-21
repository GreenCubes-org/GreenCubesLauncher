package org.greencubes.client;

import java.io.File;
import java.io.IOException;

import org.bouncycastle.util.Arrays;
import org.greencubes.util.Util;

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
}
