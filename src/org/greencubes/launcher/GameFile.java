package org.greencubes.launcher;

import java.io.File;

public class GameFile {

	public File userFile;
	public String fileUrl;
	public String name;
	public String md5;
	public boolean needUpdate = false;
	public boolean wereUpdated = false;

	public GameFile(String name, File dir) {
		this.name = name;
		userFile = new File(dir, name);
		fileUrl = name;
	}
}
