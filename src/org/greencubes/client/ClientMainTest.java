package org.greencubes.client;

import java.io.File;

public class ClientMainTest extends ClientMain {

	public ClientMainTest(String name, String localizedName) {
		super(name, localizedName);
	}
	
	@Override
	public File getWorkingDirectory() {
		return new File("test").getAbsoluteFile();
	}
	
	@Override
	protected String getUrlName() {
		return "test";
	}
}
