// <simulip : an IP and UDP simulator>
//    Copyright (C) 2008  Emmanuel Nataf
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package simulip.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import simulip.gui.views.build.BuildView;
import simulip.gui.views.share.LinkNotAddressedException;
import simulip.gui.views.simulation.SimulationView;
import simulip.ip.NetworkMaskFormatException;
import simulip.net.NetworkAddressFormatException;
import simulip.util.Properties;

/**
 * @author nataf
 */
public class Build extends JFrame implements ActionListener, KeyListener {

	static final long serialVersionUID = 1232343454;

	private BuildView view = null;

	static private boolean saved = true;

	static public Build me;

	private int x;
	private int y;

	private File currentFile = null;
	static public ResourceBundle messages;

	static protected Properties properties = new Properties(
			"simulip.gui.resources");

	@SuppressWarnings("static-access")
	public ResourceBundle getMessages() {
		return this.messages;
	}

	public Properties getProperties() {
		return properties;
	}

	public static void main(String[] args) {

		ResourceBundle messages = null;

		try {
			new Properties("simulip.gui.MessagesBundle");
			messages = ResourceBundle.getBundle("simulip.gui.MessagesBundle");
			me = new Build(messages.getString("windowTitle"), messages, args);
		} catch (MissingResourceException mre) {
			Logger
					.getLogger("")
					.logp(
							Level.SEVERE,
							"simulip.ip.RoutingTable",
							"static initialization",
							"Can not read  MessagesBundles (messages properties files)",
							mre);
		}
		/*
		try {
			messages = ResourceBundle.getBundle("simulip.gui.MessagesBundle_"
					+ country);
		} catch (MissingResourceException mre) {
			// Local country not found, try english
			try {

				messages = ResourceBundle
						.getBundle("simulip.gui.MessagesBundle_EN");
			} catch (MissingResourceException mre2) {
				// Not message found... good luck 
				// TODO should we exit the simulator or 
				// put some English default language in a class ?
				Logger
						.getLogger("")
						.logp(
								Level.SEVERE,
								"simulip.gui.Build",
								"main",
								"Can not read MessagesBundle (messages properties files)",
								mre2);
			}
		}
		*/

	}

	public Build(String title, ResourceBundle pMessages, String[] args) {
		super(title);
		messages = pMessages;
		// Set log level :
		// 1) Using -D (system properties) first
		// 2) Using resources file
		// Set log level for default logger Use -D option for java to set
		// loglevel
		String logLevelStr = System.getProperty("loglevel", null);
		if (logLevelStr == null) {
			// Try resources file
			logLevelStr = properties.getOptionalProperty("loglevel", null);
		}
		if (logLevelStr != null) {
			Logger.getLogger("").setLevel(Level.parse(logLevelStr));
		}

		setLocationRelativeTo(null);
		addKeyListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		BuildWindowAdapter bw = new BuildWindowAdapter();
		addWindowListener(bw);

		JMenuBar menubar = new JMenuBar();
		JMenu filemenu = new JMenu(messages.getString("file"));
		filemenu.setMnemonic(Integer.parseInt(messages.getString("fileMnemo")));
		// KeyEvent.VK_F
		JMenu simulmenu = new JMenu(messages.getString("simulation"));
		simulmenu.setMnemonic(Integer.parseInt(messages
				.getString("simulationMnemo"))); // KeyEvent.VK_S
		JMenu helpmenu = new JMenu(messages.getString("help"));
		helpmenu.addActionListener(this);
		helpmenu.setActionCommand("help");
		helpmenu.setMnemonic(Integer.parseInt(messages.getString("helpMnemo")));
		// KeyEvent.VK_H

		JMenuItem filenewmenuitem = new JMenuItem(messages.getString("new"));
		filenewmenuitem.addActionListener(this);
		filenewmenuitem.setActionCommand("new");
		filenewmenuitem.setMnemonic(Integer.parseInt(messages
				.getString("newMnemo")));// KeyEvent.VK_N
		filenewmenuitem.setAccelerator(KeyStroke.getKeyStroke(Integer
				.parseInt(messages.getString("newMnemo")),
				java.awt.event.InputEvent.CTRL_DOWN_MASK));

		JMenuItem fileloadmenuitem = new JMenuItem(messages.getString("open"));
		fileloadmenuitem.addActionListener(this);
		fileloadmenuitem.setActionCommand("open");
		fileloadmenuitem.setMnemonic(Integer.parseInt(messages
				.getString("openMnemo")));// KeyEvent.VK_0
		fileloadmenuitem.setAccelerator(KeyStroke.getKeyStroke(Integer
				.parseInt(messages.getString("openMnemo")),
				java.awt.event.InputEvent.CTRL_DOWN_MASK));

		JMenuItem filesavemenuitem = new JMenuItem(messages.getString("save"));
		filesavemenuitem.addActionListener(this);
		filesavemenuitem.setActionCommand("save");
		filesavemenuitem.setMnemonic(Integer.parseInt(messages
				.getString("saveMnemo")));// KeyEvent.VK_S
		filesavemenuitem.setAccelerator(KeyStroke.getKeyStroke(Integer
				.parseInt(messages.getString("saveMnemo")),
				java.awt.event.InputEvent.CTRL_DOWN_MASK));

		JMenuItem filesaveasmenuitem = new JMenuItem(messages
				.getString("saveas"));
		filesaveasmenuitem.addActionListener(this);
		filesaveasmenuitem.setActionCommand("saveas");
		filesaveasmenuitem.setMnemonic(Integer.parseInt(messages
				.getString("saveasMnemo")));// KeyEvent.VK_A
		filesaveasmenuitem.setAccelerator(KeyStroke.getKeyStroke(Integer
				.parseInt(messages.getString("saveasMnemo")),
				java.awt.event.InputEvent.CTRL_DOWN_MASK));

		JMenuItem filequitmenuitem = new JMenuItem(messages.getString("quit"));
		filequitmenuitem.addActionListener(this);
		filequitmenuitem.setActionCommand("quit");
		filequitmenuitem.setMnemonic(Integer.parseInt(messages
				.getString("quitMnemo")));// KeyEvent.VK_Q
		filequitmenuitem.setAccelerator(KeyStroke.getKeyStroke(Integer
				.parseInt(messages.getString("quitMnemo")),
				java.awt.event.InputEvent.CTRL_DOWN_MASK));

		filemenu.add(filenewmenuitem);
		filemenu.add(fileloadmenuitem);
		filemenu.add(filesavemenuitem);
		filemenu.add(filesaveasmenuitem);
		filemenu.add(filequitmenuitem);
		JMenuItem simulrunmenuitem = new JMenuItem(messages.getString("run"));
		simulrunmenuitem.addActionListener(this);
		simulrunmenuitem.setActionCommand("run");
		simulrunmenuitem.setMnemonic(Integer.parseInt(messages
				.getString("runMnemo")));// KeyEvent.VK_R;
		simulrunmenuitem.setAccelerator(KeyStroke.getKeyStroke(Integer
				.parseInt(messages.getString("runMnemo")),
				java.awt.event.InputEvent.CTRL_DOWN_MASK));

		simulmenu.add(simulrunmenuitem);
		JMenuItem helpinfomenuitem = new JMenuItem(messages.getString("info"));
		helpinfomenuitem.addActionListener(this);
		helpinfomenuitem.setActionCommand("info");
		helpmenu.add(helpinfomenuitem);
		menubar.add(filemenu);
		menubar.add(simulmenu);
		menubar.add(helpmenu);
		setJMenuBar(menubar);

		view = new BuildView();
		add(view);
		pack();

		if ((args != null) && (args.length > 0)) {
			// Load file given as command line parameter
			open(args[0]);
		}

		setVisible(true);
		/*
		 * Sucks... Notif storm ? if
		 * (this.properties.getOptinalBooleanProperty("showDefaultLog", false)){
		 * javax.swing.SwingUtilities.invokeLater(new Runnable() { public void
		 * run() { new LoggerFrame(messages.getString("defaultLoggerWtitle"),
		 * Logger.getLogger("")); } }); }
		 */
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		view.keyPressed(e);
	}

	private void quit(Window w) {
		if (!saved) {
			String s1 = messages.getString("qd_quit");
			String s2 = messages.getString("qd_cancel");
			Object[] options = { s1, s2 };
			int n = JOptionPane.showOptionDialog(w, messages
					.getString("qd_message"), messages.getString("qd_title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, s1);
			if (n == JOptionPane.YES_OPTION) {
				w.dispose();
				System.exit(0);
			}
		} else {
			w.dispose();
			System.exit(0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Point p = this.getLocationOnScreen();
		p.translate(x, y);
		try {
			if (cmd.equals("save") || cmd.equals("saveas")) {
				File file = null;
				FileOutputStream fo = null;
				if (cmd.equals("saveas") || currentFile == null) {
					JFileChooser jfilechooser = new JFileChooser();
					jfilechooser.setAcceptAllFileFilterUsed(true);
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"SimulIp topologie files", "topo");
					jfilechooser.setFileFilter(filter);
					jfilechooser.showSaveDialog(this);
					file = jfilechooser.getSelectedFile();
					if (file != null) {

						if (file.exists()) {
							String s1 = messages.getString("fod_overwrite");
							String s2 = messages.getString("fod_cancel");
							Object[] options = { s1, s2 };
							int n = JOptionPane.showOptionDialog(this,
									MessageFormat.format(messages
											.getString("fod_message"), file
											.getName()), messages
											.getString("fod_title"),
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									options, s1);
							if (n == JOptionPane.OK_OPTION) {

								fo = new FileOutputStream(file);
								this.setTitle(MessageFormat.format(messages
										.getString("windowTitleWithFile"),
										messages.getString("windowTitle"), file
												.getName()));
								currentFile = file;
								save(fo);
								fo.close();
							}
						} else {
							try {
								String fname = file.getName();
								if (fname.indexOf('.') == -1)
									fname = fname + ".topo";
								file = new File(file.getParent()
										+ File.separator + fname);
								fo = new FileOutputStream(file);
								this.setTitle(MessageFormat.format(messages
										.getString("windowTitleWithFile"),
										messages.getString("windowTitle"), file
												.getName()));
								currentFile = file;
								save(fo);
								fo.close();
							}

							catch (IOException ioe) {
								JOptionPane.showMessageDialog(this, ioe
										.getMessage(), "Error file",
										JOptionPane.ERROR_MESSAGE);
							}

						}
					}
				} else {
					file = currentFile;
					fo = new FileOutputStream(file);
					this.setTitle(MessageFormat.format(messages
							.getString("windowTitleWithFile"), messages
							.getString("windowTitle"), file.getName()));
					save(fo);
					fo.close();
				}

			} else if (cmd.equals("open") || cmd.equals("new")) {
				if (!saved) {
					// save confirmation dialog (scd)
					String s1 = messages.getString("scd_save");
					String s2 = messages.getString("scd_noSave");
					String s3 = messages.getString("scd_cancel");
					Object[] options = { s1, s2, s3 };
					int n = JOptionPane.showOptionDialog(this, messages
							.getString("scd_message"), messages
							.getString("scd_title"),
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, s1);
					if (n == JOptionPane.YES_OPTION) {
						File file = null;
						FileOutputStream fo = null;
						if (currentFile != null) {
							file = currentFile;
							fo = new FileOutputStream(file);
							save(fo);
							fo.close();
						} else {
							JFileChooser jfilechooser = new JFileChooser();
							jfilechooser.showSaveDialog(this);
							jfilechooser.setAcceptAllFileFilterUsed(true);
							FileNameExtensionFilter filter = new FileNameExtensionFilter(
									"SimulIp topologie files", "topo");
							jfilechooser.setFileFilter(filter);
							file = jfilechooser.getSelectedFile();
							if (file != null) {
								if (!file.getName().endsWith(".topo"))
									file = new File(file.getParent()
											+ File.separator + file.getName()
											+ ".topo");
								fo = new FileOutputStream(file);
								this.setTitle(MessageFormat.format(messages
										.getString("windowTitleWithFile"),
										messages.getString("windowTitle"), file
												.getName()));
								currentFile = file;
								save(fo);
								fo.close();
							}
						}
					} else if (n == JOptionPane.NO_OPTION) {
						if (cmd.equals("open"))
							open();
						else {
							newSimul();
						}
					}
				} else {
					if (cmd.equals("open"))
						open();
					else {
						newSimul();
					}
				}
			} else if (cmd.equals("run")) {
				try {
					JFrame f = new SimulationView(view.cloneModel());
					saved = true;

					if (currentFile != null) {
						f
								.setTitle(MessageFormat.format(messages
										.getString("sw_title1"), currentFile
										.getName()));
					} else {
						f.setTitle(messages.getString("sw_title2"));
					}

					f.setLocation(p);
				} catch (LinkNotAddressedException l) {
					// Link not well addressed dialog (lnwad)
					JOptionPane.showMessageDialog(this, MessageFormat.format(
							messages.getString("lnwad_message"), l.getEnd0(), l
									.getEnd1()), messages
							.getString("lnwad_title"),
							JOptionPane.INFORMATION_MESSAGE);

				} catch (IOException ee) {
					JOptionPane.showMessageDialog(this, ee.getMessage(),
							messages.getString("errorFileSaving"),
							JOptionPane.ERROR_MESSAGE);
				} catch (NetworkAddressFormatException en) {
					JOptionPane.showMessageDialog(this,
							"Network address error " + en.getMessage(),
							"Error addressing", JOptionPane.ERROR_MESSAGE);
				} catch (NetworkMaskFormatException em) {
					JOptionPane.showMessageDialog(this,
							"Network address error " + em.getMessage(),
							"Error addressing", JOptionPane.ERROR_MESSAGE);
				} catch (ClassNotFoundException ea) {
					JOptionPane.showMessageDialog(this, MessageFormat
							.format(messages.getString("anfd_message"), ea
									.getMessage()), messages
							.getString("alertDialogTitle"),
							JOptionPane.INFORMATION_MESSAGE);
				} catch (InstantiationException ei) {
					JOptionPane.showMessageDialog(this, MessageFormat.format(
							messages.getString("insexcep_message"), ei
									.getMessage()), messages
							.getString("alertDialogTitle"),
							JOptionPane.INFORMATION_MESSAGE);

				} catch (IllegalAccessException eac) {
					JOptionPane.showMessageDialog(this, MessageFormat.format(
							messages.getString("illacc_message"), eac
									.getMessage()), messages
							.getString("alertDialogTitle"),
							JOptionPane.INFORMATION_MESSAGE);
				} catch (XMLStreamException ese) {
					// TODO Auto-generated catch block
					ese.printStackTrace();
				} catch (FactoryConfigurationError efc) {
					// TODO Auto-generated catch block
					efc.printStackTrace();
				}

			} else if (cmd.equals("quit")) {
				quit(this);
			} else if (cmd.equals("info")) {
				JOptionPane
						.showMessageDialog(null, messages.getString("about"));
			}
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(this, ioe.getMessage(), messages
					.getString("errorFileMsg"), JOptionPane.ERROR_MESSAGE);
		} catch (XMLStreamException xse) {
			// TODO Auto-generated catch block
			xse.printStackTrace();
		} catch (FactoryConfigurationError fce) {
			// TODO Auto-generated catch block
			fce.printStackTrace();
		}

	}

	private void newSimul() {
		remove(view);
		view = new BuildView();
		add(view);
		pack();
		saved = true;
		currentFile = null;
		this.setTitle(MessageFormat.format(messages.getString("noFile"),
				messages.getString("windowTitle")));
		repaint();
	}

	private void save(FileOutputStream fo) throws IOException,
			XMLStreamException, FactoryConfigurationError {
		view.save(fo);
		saved = true;
	}

	private void open() {
		open(null);
	}

	/*
	 * Open a simulation file
	 * 
	 * @param fileName (pathname) to open and load, if null a file chooser
	 * dialog is displayed
	 * 
	 * Note: the use of a non null file name its mostly dedicated to file name
	 * coming from command line...
	 */
	private void open(String fileName) {
		File file = null;
		FileInputStream fi = null;
		if (fileName == null) { // use file chooser
			JFileChooser jfilechooser = new JFileChooser();
			jfilechooser.setAcceptAllFileFilterUsed(true);
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"SimulIp topologie files", "topo");
			jfilechooser.setFileFilter(filter);
			jfilechooser.showOpenDialog(this);
			file = jfilechooser.getSelectedFile();
		} else {
			file = new File(fileName);
		}
		if (file != null) {
			try {
				fi = new FileInputStream(file);
				view.open(fi);
				fi.close();
				setTitle(MessageFormat.format(messages
						.getString("windowTitleWithFile"), messages
						.getString("windowTitle"), file.getName()));
				currentFile = file;
				saved = true;
				pack();
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), messages
						.getString("errorFileMsg"), JOptionPane.ERROR_MESSAGE);

			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NetworkAddressFormatException e) {
				JOptionPane.showMessageDialog(this, e.getMessage()
						+ " badly formed");
			} catch (NetworkMaskFormatException e) {
				JOptionPane.showMessageDialog(this, e.getMessage()
						+ " badly formed");
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), messages
						.getString("alertDialogTitle"),
						JOptionPane.ERROR_MESSAGE);
			} finally {
				try {
					if (fi != null)
						fi.close();
				} catch (IOException e) {
					// Nothing to do as the file
					// can not be closed because
					// it does not exist
				}
			}
		}
		repaint();
	}

	static public void changed() {
		saved = false;
	}

	private class BuildWindowAdapter extends WindowAdapter {

		/**
		 * Close the frame when close button
		 */
		public void windowClosing(WindowEvent e) {
			quit(e.getWindow());
		}
	}

}
