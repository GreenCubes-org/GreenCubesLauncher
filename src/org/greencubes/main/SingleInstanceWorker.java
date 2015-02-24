package org.greencubes.main;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.greencubes.util.Util;

public class SingleInstanceWorker extends Thread implements Closeable {
	
	private static final int PORT = 56001;
	private static final int HEAD = 0xFEFE0001;
	
	private ServerSocket serverSocket;
	private boolean isAlreadyRunning = false;
	
	public SingleInstanceWorker(String[] args) {
		setDaemon(true);
		try {
			serverSocket = new ServerSocket(PORT, 0, InetAddress.getByAddress(new byte[] {127,0,0,1}));
			start();
		} catch(UnknownHostException e) {
			// Wired shit happend, skip
		} catch(IOException e) {
			// Probably socket busy, try send info
			DataInputStream input = null;
			DataOutputStream output = null;
			Socket socket = null;
			try {
				socket = new Socket(InetAddress.getByAddress(new byte[] {127,0,0,1}), PORT);
				socket.setSoTimeout(2000);
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				output.writeInt(HEAD);
				output.flush();
				if(input.readInt() == HEAD) {
					output.writeInt(args.length);
					for(int i = 0; i < args.length; ++i) {
						output.writeUTF(args[i]);
					}
					output.flush();
					input.readInt();
				}
				isAlreadyRunning = true;
			} catch(UnknownHostException e1) {
			} catch(IOException e1) {
			} finally {
				Util.close(input, output, socket);
			}
			
		}
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Socket s = serverSocket.accept();
				process(s);
			} catch(Exception e) {
				Util.close(serverSocket);
				serverSocket = null;
				return;
			}
		}
	}
	
	@Override
	public void close() {
		Util.close(serverSocket);
	}
	
	public boolean isAlreadyRunning() {
		return isAlreadyRunning;
	}
	
	private void process(Socket socket) {
		DataInputStream input = null;
		DataOutputStream output = null;
		try {
			socket.setSoTimeout(3000);
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
			if(input.readInt() == HEAD) {
				output.writeInt(HEAD);
				output.flush();
				int length = input.readInt();
				String[] args = new String[length];
				for(int i = 0; i < args.length; ++i) {
					args[i] = input.readUTF();
				}
				output.writeInt(HEAD);
				output.flush();
				Main.newInstanceCreated(args);
			}
		} catch(Exception e) {
			// Skip exceptions
		} finally {
			Util.close(input, output, socket);
		}
	}
	
}
