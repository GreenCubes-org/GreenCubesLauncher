package org.greencubes.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.greencubes.swing.JPanelBG;


public class LauncherFirst {
	
	public static final boolean IS_64_BIT_JAVA;
	
	public JFrame mainFrame;
	//private static final String version = "4";
	
	private Launcher launcher;
	private Downloader downloader;
	private JTextField loginField;
	private JPasswordField passwordField;
	private String authResult;
	private boolean processing = false;
	private JPanel emptyPadding;
	
	public LauncherFirst(Launcher launcher) {
		this.launcher = launcher;
		this.downloader = this.launcher.downloader;
		mainFrame = new JFrame("GreenCubes Launcher");
		mainFrame.setUndecorated(true);
		mainFrame.setAlwaysOnTop(false);
		try {
			ArrayList<BufferedImage> icons = new ArrayList<BufferedImage>(5);
			icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico32x32.png")));
			icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico48x48.png")));
			icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico64x64.png")));
			icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico128x128.png")));
			icons.add(ImageIO.read(LauncherFirst.class.getResource("/gcico256x256.png")));
			mainFrame.setIconImages(icons);
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		mainFrame.setBackground(new Color(1.0F, 1.0F, 1.0F, 0.0F));
		
		JPanel background = new JPanelBG("/bg.png");
		JPanel button = new JPanelBG("/login.png");

		button.setBackground(new Color(0, 0, 0, 1));
		button.setPreferredSize(new Dimension(253, 31));
		JPanel cross = new JPanelBG("/cross.png");
		cross.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		cross.setPreferredSize(new Dimension(26, 26));
		cross.addMouseListener(this.launcher.closeMouseListener);
		
		/* Main Layer */
		mainFrame.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		mainFrame.setPreferredSize(new Dimension(560, 384));
		//mainFrame.setLayout(new GridLayout());
		mainFrame.add(background, BorderLayout.CENTER);
		
		background.setPreferredSize(new Dimension(560, 384));
		background.setOpaque(false);
		
		/* Main grid */
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		background.setLayout(layout);
		
		/* Top left corner to layout elements */
		JPanel panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(128, 48));
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;
		background.add(panel, c);
		c.gridheight = 1;
		/* Bottom right corner to layout elements */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(128, 48));
		c.gridx = 2;
		c.gridy = 3;
		background.add(panel, c);
		
		/* Cross button */
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		background.add(cross, c);
		c.anchor = GridBagConstraints.CENTER;
		
		/* Inner grid */
		JPanel inner = new JPanel();
		inner.setBackground(new Color(0F, .5F, .5F, 0F));
		inner.setPreferredSize(new Dimension(304, 288));
		inner.setOpaque(false);
		layout = new GridBagLayout();
		inner.setLayout(layout);
		c.gridx = 1;
		c.gridy = 2;
		background.add(inner, c);
		
		/* Top padding */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(304, 50));
		c.gridx = 0;
		c.gridy = 0;
		inner.add(panel, c);
		/* Login row */
		JPanel loginRow = new JPanel();
		loginRow.setOpaque(false);
		loginRow.setLayout(new GridBagLayout());
		loginRow.setBackground(new Color(1.0F, .5F, 0.5F, 0F));
		loginRow.setPreferredSize(new Dimension(304, 23));
		c.gridx = 0;
		c.gridy = 1;
		inner.add(loginRow, c);
		/* Login password fields padding */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(304, 12));
		c.gridx = 0;
		c.gridy = 2;
		inner.add(panel, c);
		/* Password row */
		JPanel passwordRow = new JPanel();
		passwordRow.setOpaque(false);
		passwordRow.setLayout(new GridBagLayout());
		passwordRow.setBackground(new Color(1.0F, .5F, 0.5F, 0F));
		passwordRow.setPreferredSize(new Dimension(304, 23));
		c.gridx = 0;
		c.gridy = 3;
		inner.add(passwordRow, c);
		/* Button padding */
		emptyPadding = new JPanel();
		emptyPadding.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		emptyPadding.setPreferredSize(new Dimension(304, 132));
		emptyPadding.setOpaque(false);
		c.gridx = 0;
		c.gridy = 4;
		inner.add(emptyPadding, c);
		
		/* Login button */
		c.gridx = 0;
		c.gridy = 5;
		inner.add(button, c);
		/* After button padding */
		panel = new JPanel();
		panel.setBackground(new Color(1.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(304, 17));
		c.gridx = 0;
		c.gridy = 6;
		inner.add(panel, c);
		
		/* Login row */
		panel = new JPanel();
		panel.setBackground(new Color(0.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(116, 23));
		c.gridx = 0;
		c.gridy = 0;
		loginRow.add(panel, c);
		loginField = new JTextField(0);
		loginField.setPreferredSize(new Dimension(158, 23));
		c.gridx = 1;
		c.gridy = 0;
		loginRow.add(loginField, c);
		loginField.setBorder(new EmptyBorder(0, 5, 0, 0));
		loginField.setOpaque(false);
		loginField.setForeground(new Color(170, 255, 102));
		loginField.setCaretColor(new Color(170, 255, 102));
		loginField.setFont(loginField.getFont().deriveFont(Font.BOLD));
		panel = new JPanel();
		panel.setBackground(new Color(0.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(30, 23));
		c.gridx = 2;
		c.gridy = 0;
		loginRow.add(panel, c);
		
		/* Password row */
		panel = new JPanel();
		panel.setBackground(new Color(0.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(116, 23));
		c.gridx = 0;
		c.gridy = 0;
		passwordRow.add(panel, c);
		passwordField = new JPasswordField(0);
		passwordField.setPreferredSize(new Dimension(158, 23));
		c.gridx = 1;
		c.gridy = 0;
		passwordRow.add(passwordField, c);
		passwordField.setBorder(new EmptyBorder(0, 5, 0, 0));
		passwordField.setOpaque(false);
		passwordField.setForeground(new Color(170, 255, 102));
		passwordField.setCaretColor(new Color(170, 255, 102));
		passwordField.setFont(loginField.getFont());
		panel = new JPanel();
		panel.setBackground(new Color(0.0F, 1.0F, 1.0F, 0F));
		panel.setPreferredSize(new Dimension(30, 23));
		c.gridx = 2;
		c.gridy = 0;
		passwordRow.add(panel, c);
		
		passwordField.setActionCommand("OK");
		passwordField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(ae.getActionCommand().equals("OK"))
					doLogin();
			}
		});
		loginField.setActionCommand("OK");
		loginField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(ae.getActionCommand().equals("OK"))
					doLogin();
			}
		});
		button.addMouseListener(new MouseListener() {
			// Вот бы можно было сделать это лучше...
			@Override
            public void mouseClicked(MouseEvent e) {
				doLogin();
            }
			@Override
            public void mousePressed(MouseEvent e) {
				
            }

			@Override
            public void mouseReleased(MouseEvent e) {
            }

			@Override
            public void mouseEntered(MouseEvent e) {
            }

			@Override
            public void mouseExited(MouseEvent e) {
            }
		});
		load();
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}
	
	private void save() {
		DataOutputStream dos = null;
		try {
			File f = new File("launcher.dat");
			Cipher cipher = getCipher(1, "c8d3563578b9264ee7fc86d44bbb9a79");
			if(cipher == null)
				return;
			dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(f), cipher));
			dos.writeUTF("d000m" + loginField.getText());
			dos.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			Util.close(dos);
		}
	}
	
	private void load() {
		File f = new File("launcher.dat");
		if(f.exists()) {
			DataInputStream dis = null;
			try {
				Cipher cipher = getCipher(2, "c8d3563578b9264ee7fc86d44bbb9a79");
				if(cipher == null)
					return;
				dis = new DataInputStream(new CipherInputStream(new FileInputStream(f), cipher));
				loginField.setText(dis.readUTF().substring(5));
				loginField.setCaretPosition(loginField.getText().length());
				dis.close();
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				Util.close(dis);
			}
		}
	}
	
	private Cipher getCipher(int mode, String password) throws Exception {
		Random random = new Random(43287234L);
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
		SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(mode, pbeKey, pbeParamSpec);
		return cipher;
	}
	
	public void doLogin() {
		if(loginField.getText() == null || loginField.getText().length() == 0 || passwordField.getPassword() == null || passwordField.getPassword().length == 0)
			return;
		if(processing)
			return;
		try {
	        String result = getLoginResult(loginField.getText(), String.valueOf(passwordField.getPassword()));
	        if(result.equals("NO")) {
	        	JOptionPane.showMessageDialog(mainFrame, "Не верный логин или пароль!", "Результат авторизации", JOptionPane.OK_OPTION);
	        	return;
			} else if(result.equals("BLOCKED")) {
				JOptionPane.showMessageDialog(mainFrame, "IP заблокирован. Ждите один час.", "Результат авторизации", JOptionPane.OK_OPTION);
				return;
			}
	        this.authResult = result;
        } catch(IOException e) {
        	JOptionPane.showMessageDialog(mainFrame, e, "Ошибка авторизации", JOptionPane.ERROR_MESSAGE);
        	return;
        }
		save();
		processing = true;
		new Thread("Client launch thread") {
			@Override
			public void run() {
				try {
	                downloadAndStart();
                } catch(Exception e) {
                	JOptionPane.showMessageDialog(mainFrame, e, "Ошибка запуска или обновления", JOptionPane.ERROR_MESSAGE);
                }
				processing = false;
			}
		}.start();		
	}
	
	private File clientDir = new File(System.getProperty("user.dir"));
	private String clientName = "greencubes";
	
	private void downloadAndStart() throws Exception {
		JTextPane statusArea = new JTextPane();
		StyledDocument doc = statusArea.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		statusArea.setEditable(false);
		statusArea.setBackground(new Color(0f, 0f, 0f, 0f));
		statusArea.setOpaque(false);
		emptyPadding.add(statusArea);
		statusArea.setText("Проверка обновлений...");
		
		List<GameFile> files = prepareVersionControl();
		statusArea.setText("Загрузка файлов...");
		downloadFiles(files, statusArea);
		launch(files);
		mainFrame.dispose();
	}
	private void launch(List<GameFile> files) {
		List<String> classPath = new ArrayList<String>();
		try {
	        Scanner fr = new Scanner(new File(clientDir, "libraries/liborder.txt"));
	        while(fr.hasNext())
	        	classPath.add(fr.nextLine());
	        fr.close();
        } catch(FileNotFoundException e1) {
	        throw new RuntimeException(e1);
        }
		List<String> command = new ArrayList<String>();
		command.add(OperatingSystem.getCurrentPlatform().getJavaDir());
		command.add("-Xincgc");
		command.add("-Djava.net.preferIPv4Stack=true");
		command.add("-Xms1024M");
		command.add("-Xmx1024M");
		command.add("-cp");
		StringBuilder cp = new StringBuilder();
		
		for(int i = 0; i < classPath.size(); ++i) {
			cp.append(new File(clientDir, "libraries/" + classPath.get(i)).getAbsolutePath());
			cp.append(System.getProperty("path.separator"));
		}
		cp.append("client.jar");
		command.add(cp.toString());
		command.add("org.greencubes.client.Main");
		command.add("--fullscreen");
		command.add("--session");
		String[] split = authResult.split(":");
		command.add(split[0]);
		command.add(split[1]);
		command.add("--connect");
		command.add("srv1.greencubes.org");
		ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
		pb.directory(clientDir);
		try {
			
	        Process p = pb.start();
	        new ProcessMonitorThread(p).start();
        } catch(IOException e) {
        	throw new AssertionError(e);
        }
		/*ClassLoader loader = new URLClassLoader(newClassPath.toArray(new URL[0]));
		try {
	        Class<?> mainClass = loader.loadClass("org.greencubes.client.Main");
	        Method m = mainClass.getMethod("main", String[].class);
	        
	        String[] split = authResult.split(":");
	        m.invoke(null, (Object) new String[] {"--fullscreen", "--session", split[0], split[1], "--connect", "srv1.greencubes.org"});
        } catch(Exception e) {
	        throw new AssertionError(e);
        }*/
		
    }

	private void downloadFiles(List<GameFile> files, JTextPane statusArea) throws IOException {
		int toDownload = 0;
		for(int i = 0; i < files.size(); i++) {
			GameFile file = files.get(i);
			if(file.userFile.exists() && !file.needUpdate)
				continue;
			toDownload++;
		}
		if(toDownload > 0) {
			int downloaded = 0;
			for(int i = 0; i < files.size(); i++) {
				GameFile file = files.get(i);
				if(file.userFile.exists() && !file.needUpdate)
					continue;
				int repeat = 0;
				while(true) {
					if(++repeat == 3)
						throw new IOException("Downloaded file does not match hash 3 times!");
					statusArea.setText("Загрузка файлов... " + downloaded + "/" + toDownload + "\n" + file.name);
					try {
						downloader.downloadFile(file.userFile, file.fileUrl);
					} catch(IOException e) {
						throw e;
					}
					if(file.md5 != null)
						try {
							String downloadedMd5 = Util.getMD5Checksum(file.userFile.getAbsolutePath());
							if(!file.md5.equals(downloadedMd5))
								throw new IOException(downloadedMd5 + " != " + file.md5);
						} catch(Exception e) {
							throw new IOException(e);
						}
					break;
				}
				downloaded++;
			}
		}
		statusArea.setText("Загрузка файлов...");
		downloader.downloadFile(new File(clientDir, "version.md5"), "vc/" + clientName + "/version.md5");
    }

	private List<GameFile> prepareVersionControl() throws IOException {
		List<GameFile> gameFiles = new ArrayList<GameFile>();
		Map<String, String> currentHashes = new HashMap<String, String>();
		File versionFile = new File(clientDir, "version.md5");
		if(versionFile.exists()) {
			try {
				Scanner s = new Scanner(versionFile);
				while(s.hasNextLine()) {
					String line = s.nextLine();
					if(line.startsWith("#"))
						continue;
					String[] split = line.split(";");
					if(split.length < 2)
						continue;
					if(split.length == 2)
						currentHashes.put(split[0], split[1]);
					else
						currentHashes.put(Util.join(split, ";", 0, split.length - 2), split[split.length - 1]);
				}
				s.close();
			} catch(FileNotFoundException e) {
			}
		}
		String serverHash = downloader.readURL("vc/" + clientName + "/version.md5");
		List<String> newFiles = new ArrayList<String>();
		for(String line : serverHash.split("\n")) {
			if(line.startsWith("#"))
				continue;
			String[] split = line.split(";");
			if(split.length < 2)
				continue;
			String name;
			String hash;
			if(split.length == 2) {
				name = split[0];
				hash = split[1];
			} else {
				name = Util.join(split, ";", 0, split.length - 2);
				hash = split[split.length - 1];
			}
			GameFile file = new GameFile(name, clientDir);
			file.fileUrl = URLEncoder.encode("vc/" + clientName + "/" + file.name,"UTF-8").replace("+","%20");
			file.md5 = hash;
			String currentHash = currentHashes.get(name);
			if(!file.userFile.exists())
				file.needUpdate = true;
			else if(currentHash == null || !hash.equals(currentHash))
				file.needUpdate = true;
			gameFiles.add(file);
			newFiles.add(name);
		}
		Iterator<Entry<String, String>> iterator = currentHashes.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, String> e = iterator.next();
			if(!newFiles.contains(e.getKey())) {
				File oldLib = new File(clientDir, e.getKey());
				if(!oldLib.delete())
					oldLib.deleteOnExit();
			}
		}
		return gameFiles;
	}
	
	private String getLoginResult(String userName, String password) throws IOException {
		return downloader.readURL("auth.php?name=" + URLEncoder.encode(userName, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"));
	}
	
	static {
		String[] opts = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};
		boolean is64bit = false;
		for(String opt : opts) {
			String val = System.getProperty(opt);
			if(val != null && val.contains("64")) {
				is64bit = true;
				break;
			}
		}
		IS_64_BIT_JAVA = is64bit;
	}
	
	public class ProcessMonitorThread extends Thread {
		private final Process process;

		public ProcessMonitorThread(Process process) {
			this.process = process;
		}

		public boolean isRunning() {
			try {
				this.process.exitValue();
			} catch(IllegalThreadStateException ex) {
				return true;
			}

			return false;
		}

		@Override
		public void run() {
			InputStreamReader reader = new InputStreamReader(this.process.getInputStream());
			BufferedReader buf = new BufferedReader(reader);
			String line = null;

			while(isRunning()) {
				try {
					while((line = buf.readLine()) != null) {
						System.out.println("Client> " + line);
						//this.process.getSysOutLines().add(line);
					}
				} catch(IOException ex) {
					ex.printStackTrace();
				} finally {
					try {
						buf.close();
					} catch(IOException ex) {
						//Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
			System.out.println("Client finished, disabling launcher " + this.process.exitValue());
			System.exit(0);
		}
	}
	
}
