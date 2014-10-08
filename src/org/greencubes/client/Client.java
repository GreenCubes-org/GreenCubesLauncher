package org.greencubes.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Client {
	
	public static final List<Client> clients = new ArrayList<Client>();
	
	public final String name;
	public final String localizedName;
	
	protected Client(String name, String localizedName) {
		this.name = name;
		this.localizedName = localizedName;
		clients.add(this);
	}
	
	public abstract File getWorkingDirectory();
	
	public abstract String getLaunchParameters(String username, String session, Server server);
	
	public abstract List<Server> getServers();
	
	public abstract boolean isSinglePlayerModeAllowed();
	
}
