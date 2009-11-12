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

package simulip.gui.views.build;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import simulip.gui.Build;
import simulip.gui.controller.NetworkController;
import simulip.gui.model.NodeGraph;
import simulip.gui.views.share.ApplicationCompilationError;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;

/**
 * @author nataf
 */
public class Applications extends JFrame implements ActionListener,
		ItemListener, KeyListener {

	static final long serialVersionUID = 1232343454;
	private NetworkController controller;
	private NodeGraph node;

	private int nbapp;
	private List<String> classes = new Vector<String>();

	private java.awt.List la = new java.awt.List();

	public Applications(NetworkController nc, NodeGraph n) {
		super();
		controller = nc;
		node = n;
		nbapp = 0;
		addWindowListener(new iaf());
		addKeyListener(this);
		la.addKeyListener(this);
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public void update() {
		String s = MessageFormat.format(Build.messages.getString("appli_title"), node
				.getNodeName());

		setTitle(s);
		setLayout(new BorderLayout());
		nbapp = node.getApplicationClasses().size();
		for (Enumeration<String> ea = node.getApplicationClasses().keys(); ea
				.hasMoreElements();) {
			String fullappname = ea.nextElement();
			classes.add(fullappname);

			String appname = fullappname.substring(0, fullappname
					.lastIndexOf(".class"));
			appname = appname.substring(
					appname.lastIndexOf(File.separator) + 1, appname.length());
			la.add(appname);

		}
		la.addItemListener(this);
		

		JButton add = new JButton(Build.messages.getString("add"));
		JButton rem = new JButton(Build.messages.getString("remove"));
		JButton ok = new JButton(Build.messages.getString("ok"));

		add.addActionListener(this);
		rem.addActionListener(this);
		ok.addActionListener(this);

		add.addKeyListener(this);
		rem.addKeyListener(this);
		ok.addKeyListener(this);

		add.setActionCommand("add");
		add.setMnemonic(Integer.parseInt(Build.messages.getString("add_mnemo")));
		rem.setActionCommand("remove");
		rem.setMnemonic(Integer.parseInt(Build.messages.getString("remove_mnemo")));
		ok.setActionCommand("ok");
		ok.setMnemonic(Integer.parseInt(Build.messages.getString("ok_mnemo")));

		Container cb = new Container();
		cb.setLayout(new BorderLayout());
		cb.add(BorderLayout.WEST, add);
		cb.add(BorderLayout.CENTER, ok);
		cb.add(BorderLayout.EAST, rem);

		add(BorderLayout.CENTER, la);
		add(BorderLayout.SOUTH, cb);

		pack();
	}

	public void actionPerformed(ActionEvent arg0) {
		String c = arg0.getActionCommand();
		if (c.equals("add")) {
			JFileChooser jfilechooser = null;
			String napp = null;
			jfilechooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					Build.messages.getString("appli_fc_class"), "class");
			jfilechooser.setFileFilter(filter);
			FileNameExtensionFilter nofilter = new FileNameExtensionFilter(
					Build.messages.getString("appli_fc_java"), "java");
			jfilechooser.setFileFilter(nofilter);
			jfilechooser.setAcceptAllFileFilterUsed(false);
			jfilechooser.showOpenDialog(this);

			try {
				if (jfilechooser.getSelectedFile() != null) {
					napp = jfilechooser.getSelectedFile().getCanonicalFile()
							.getCanonicalPath();
					Class<?> cl = controller.putApplication(node, napp);
					if (napp.endsWith(".java"))
						napp = napp.replaceAll(".java", ".class");
					classes.add(napp);
					napp = napp.substring(0, napp.indexOf(".class"));
					napp = napp.substring(napp.lastIndexOf(File.separator) + 1,
							napp.length());
					la.add(napp);
					if (cl == null)
						Build.changed();
				}
			} catch (ClassNotFoundException ea) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(
						Build.messages.getString("anfd_message"), ea
								.getMessage()), Build.messages
						.getString("alertDialogTitle"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IllegalAccessException eac) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(
						Build.messages.getString("illacc_message"), eac
								.getMessage()), Build.messages
						.getString("alertDialogTitle"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(
						Build.messages.getString("ioclp_message"), e
								.getMessage()), Build.messages
						.getString("alertDialogTitle"),
						JOptionPane.INFORMATION_MESSAGE);
			} catch (ClassCastException e) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(
						Build.messages.getString("cast_message"), e
								.getMessage()), Build.messages
						.getString("alertDialogTitle"),
						JOptionPane.INFORMATION_MESSAGE);

			} catch (ApplicationCompilationError e) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(
						Build.messages.getString("compile_message"), e
								.getMessage()), Build.messages
						.getString("alertDialogTitle"),
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

		if (c.equals("remove")) {
			if (remove) {
				if (nbapp > 0)
					nbapp--;
				remove = false;
				node.removeApplication(classes.get(iremove));
				la.remove(iremove);
				classes.remove(iremove);
				Build.changed();
			}
		}
		if (c.equals("ok")) {
			dispose();
		}
	}

	private boolean remove = false;
	private int iremove;

	public void itemStateChanged(ItemEvent e) {
		remove = true;
		iremove = ((Integer) e.getItem()).intValue();

	}

	public void keyPressed(KeyEvent arg0) {
		// We do nothing

	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			ActionEvent ae = new ActionEvent(this, 0, "ok");
			actionPerformed(ae);
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			dispose();
	}

	public void keyTyped(KeyEvent arg0) {
		// we do nothing

	}

	private class iaf extends WindowAdapter {
		/**
		 * Display the routing table when opened the frame
		 */
		public void windowOpened(WindowEvent e) {
			update();
		}

		/**
		 * Close the frame when close button
		 */
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}
	}

}
