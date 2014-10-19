package org.greencubes.client;

public class Server {
	
	public final String name;
	public final String address;
	public final int port;
	
	public Server(String name, String address, int port) {
		this.name = name;
		this.address = address;
		this.port = port;
	}
	
	public String getFullAddress() {
		return address + ":" + port;
	}
}
