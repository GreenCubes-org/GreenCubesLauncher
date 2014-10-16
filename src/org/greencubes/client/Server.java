package org.greencubes.client;

public class Server {
	
	public String address;
	public int port;
	
	public Server(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public String getFullAddress() {
		return address + ":" + port;
	}
}
