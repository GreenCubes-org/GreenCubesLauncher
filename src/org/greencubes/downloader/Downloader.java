package org.greencubes.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.greencubes.util.Util;

public class Downloader {

	public static boolean printUrls = false;

	public List<Exception> errors = new ArrayList<Exception>();

	private List<String> serversList = new ArrayList<String>();
	private int currentServer = 0;
	private int serverListRepeated = 0;

	public volatile int bytesToDownload = 0;
	public volatile int bytesDownloaded = 0;
	public volatile IOException lastError = null;
	public volatile boolean waitingForRepeat = false;
	private final HttpClient httpClient = new DefaultHttpClient();

	private List<String> log = new ArrayList<String>();

	public Downloader(String server) {
		httpClient.getParams().setParameter("http.socket.timeout", new Integer(5000));
		addServer(server);
	}

	public Downloader() {
	}

	public void clearServers() {
		serversList.clear();
	}

	public String[] getServers() {
		return serversList.toArray(new String[0]);
	}

	public String[] getLog() {
		return log.toArray(new String[0]);
	}

	public void addServer(String serverUrl) {
		if(serversList.contains(serverUrl))
			return;
		serversList.add(serverUrl);
		serverListRepeated = 0;
		currentServer = 0;
	}

	private void nextServer() {
		if(currentServer == serversList.size() - 1) {
			serverListRepeated++;
			currentServer = 0;
		} else
			currentServer++;
	}

	public boolean isCrashed() {
		return serverListRepeated > 1;
	}

	public int getFileSize(String fileName) throws IOException {
		if(serversList.size() == 0)
			throw new IOException("No download servers specified!");
		if(printUrls)
			System.out.println("File size " + fileName);
		serverListRepeated = 0;
		currentServer = 0;
		bytesToDownload = 0;
		bytesDownloaded = 0;
		lastError = null;
		while(!isCrashed()) {
			URL u = new URL(serversList.get(currentServer) + fileName);
			waitingForRepeat = false;
			HttpHead headRequest;
			try {
				headRequest = new HttpHead(u.toURI());
			} catch(URISyntaxException e2) {
				throw new IOException(e2);
			}
			try {
				HttpResponse response = httpClient.execute(headRequest);
				int code = response.getStatusLine().getStatusCode();
				if(code >= 400) {
					if(printUrls)
						System.out.println("Http no 200 answer: " + code + " " + response.getStatusLine().getReasonPhrase());
					EntityUtils.consumeQuietly(response.getEntity());
					throw new HTTPResponseError(code + " " + response.getStatusLine().getReasonPhrase() + " for " + u.getPath());
				}
				EntityUtils.consumeQuietly(response.getEntity());
				return Integer.parseInt(response.getFirstHeader("Content-Length").getValue());
			} catch(IOException e) {
				if(lastError == null)
					lastError = e;
				errors.add(e);
				nextServer();
				waitingForRepeat = true;
				try {
					Thread.sleep(2000L);
				} catch(InterruptedException e1) {
				}
				continue;
			}
		}
		if(lastError != null)
			throw lastError;
		return -1;
	}

	public void downloadFile(File output, String fileName) throws IOException {
		if(serversList.size() == 0)
			throw new IOException("No download servers specified!");
		if(!output.exists()) {
			if(!output.getParentFile().exists() && !output.getParentFile().mkdirs())
				throw new IOException("Unable to create dir for " + output.getAbsolutePath());
			if(!output.createNewFile())
				throw new IOException("Unable to create new file for " + output.getAbsolutePath());
		}
		if(printUrls)
			System.out.println("Downloading " + fileName);
		serverListRepeated = 0;
		currentServer = 0;
		bytesToDownload = 0;
		lastError = null;
		while(!isCrashed()) {
			URL u = new URL(serversList.get(currentServer) + fileName);
			bytesDownloaded = 0;
			waitingForRepeat = false;
			HttpGet getRequest;
			try {
				getRequest = new HttpGet(u.toURI());
			} catch(URISyntaxException e2) {
				throw new IOException(e2);
			}
			InputStream is = null;
			FileOutputStream os = null;
			try {
				HttpResponse response = httpClient.execute(getRequest);
				HttpEntity entity = response.getEntity();
				int code = response.getStatusLine().getStatusCode();
				if(code >= 400) {
					if(printUrls)
						System.out.println("Http no 200 answer: " + code + " " + response.getStatusLine().getReasonPhrase());
					EntityUtils.consumeQuietly(response.getEntity());
					throw new HTTPResponseError(code + " " + response.getStatusLine().getReasonPhrase() + " for " + u.getPath());
				}
				is = entity.getContent();
				BufferedInputStream bis = new BufferedInputStream(is);
				bytesToDownload = (int) entity.getContentLength();
				os = new FileOutputStream(output);
				byte[] buffer = new byte[1024];
				int bufferSize;
				while((bufferSize = bis.read(buffer, 0, buffer.length)) != -1) {
					os.write(buffer, 0, bufferSize);
					bytesDownloaded += bufferSize;
				}
				EntityUtils.consumeQuietly(entity);
				return;
			} catch(IOException e) {
				if(printUrls) {
					System.out.println("Exception when using server " + serversList.get(currentServer) + ":");
					e.printStackTrace();
				}
				if(lastError == null)
					lastError = e;
				errors.add(e);
				nextServer();
				waitingForRepeat = true;
				try {
					Thread.sleep(2000L);
				} catch(InterruptedException e1) {
				}
				continue;
			} finally {
				Util.close(is);
				Util.close(os);
			}
		}
		if(lastError != null)
			throw lastError;
	}

	public InputStream getInputStream(String request) throws IOException {
		if(serversList.size() == 0)
			throw new IOException("No download servers specified!");
		if(printUrls)
			System.out.println("Reading " + request);
		serverListRepeated = 0;
		currentServer = 0;
		bytesToDownload = 0;
		bytesDownloaded = 0;
		lastError = null;
		while(!isCrashed()) {
			URL u = new URL(serversList.get(currentServer) + request);
			waitingForRepeat = false;
			HttpGet getRequest;
			try {
				getRequest = new HttpGet(u.toURI());
			} catch(URISyntaxException e2) {
				throw new IOException(e2);
			}
			InputStream is = null;
			try {
				HttpResponse response = httpClient.execute(getRequest);
				HttpEntity entity = response.getEntity();
				int code = response.getStatusLine().getStatusCode();
				if(code >= 400) {
					if(printUrls)
						System.out.println("Http no 200 answer: " + code + " " + response.getStatusLine().getReasonPhrase());
					EntityUtils.consumeQuietly(response.getEntity());
					throw new HTTPResponseError(code + " " + response.getStatusLine().getReasonPhrase() + " for " + u.getPath());
				}
				is = entity.getContent();
				return is;
			} catch(IOException e) {
				if(lastError == null)
					lastError = e;
				errors.add(e);
				nextServer();
				waitingForRepeat = true;
				try {
					Thread.sleep(2000L);
				} catch(InterruptedException e1) {
				}
				Util.close(is);
				continue;
			}
		}
		if(lastError != null)
			throw lastError;
		return null;
	}

	public String readURL(String request) throws IOException {
		if(serversList.size() == 0)
			throw new IOException("No download servers specified!");
		if(printUrls)
			System.out.println("Reading " + request);
		serverListRepeated = 0;
		currentServer = 0;
		bytesToDownload = 0;
		bytesDownloaded = 0;
		lastError = null;
		while(!isCrashed()) {
			URL u = new URL(serversList.get(currentServer) + request);
			waitingForRepeat = false;
			HttpGet getRequest;
			try {
				getRequest = new HttpGet(u.toURI());
			} catch(URISyntaxException e2) {
				throw new IOException(e2);
			}
			InputStream is = null;
			FileOutputStream os = null;
			try {
				HttpResponse response = httpClient.execute(getRequest);
				HttpEntity entity = response.getEntity();
				int code = response.getStatusLine().getStatusCode();
				if(code >= 400) {
					if(printUrls)
						System.out.println("Http no 200 answer: " + code + " " + response.getStatusLine().getReasonPhrase());
					EntityUtils.consumeQuietly(response.getEntity());
					throw new HTTPResponseError(code + " " + response.getStatusLine().getReasonPhrase() + " for " + u.getPath());
				}
				is = entity.getContent();
				bytesToDownload = (int) entity.getContentLength();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();
				String s;
				while((s = reader.readLine()) != null) {
					if(sb.length() != 0)
						sb.append('\n');
					sb.append(s);
				}
				return sb.toString();
			} catch(IOException e) {
				if(lastError == null)
					lastError = e;
				errors.add(e);
				nextServer();
				waitingForRepeat = true;
				try {
					Thread.sleep(2000L);
				} catch(InterruptedException e1) {
				}
				continue;
			} finally {
				Util.close(is);
				Util.close(os);
			}
		}
		if(lastError != null)
			throw lastError;
		return null;
	}
}
