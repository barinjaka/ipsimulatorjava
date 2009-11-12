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

package simulip.gui.views.simulation;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import simulip.gui.Build;
import simulip.gui.model.NodeGraph;
import simulip.net.Application;

/**
 * A class for displaying hosted application(s) by a host
 * 
 * @author Emmanuel Nataf
 */
public class Applications extends JFrame implements KeyListener, ActionListener, WindowListener {

	private static final long serialVersionUID = 132413234;

	/**
	 * The graphical node
	 */
	private NodeGraph node;
	private Frame me;
	/**
	 * Hosted applications index by their java class name
	 */
	private Hashtable<String, Class<?>> appli_classes;

	private Hashtable<String, String> classes = new Hashtable<String, String>();

	private String appliToLaunch = "";

	/**
	 * Create a new application list frame for the given graphical node
	 * 
	 * @param n
	 *            the graphical node
	 */
	public Applications(NodeGraph n) {
		super();
		me = this;
		node = n;
		appli_classes = node.getApplicationClasses();
		ButtonGroup bg = new ButtonGroup();

		this.setLayout(new BorderLayout());
		this.setTitle(MessageFormat.format(Build.messages
				.getString("appli_title"), node.getNodeName()));
		setAlwaysOnTop(true);
		setVisible(true);
		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		for (Enumeration<String> eka = appli_classes.keys(); eka
				.hasMoreElements();) {
			String fullappname = eka.nextElement();
			String appname = fullappname.substring(0, fullappname
					.lastIndexOf(".class"));
			appname = appname.substring(
					appname.lastIndexOf(File.separator) + 1, appname.length());
			JRadioButton jrb = new JRadioButton(appname);
			jrb.setActionCommand(appname);
			jrb.addActionListener(this);
			jrb.addKeyListener(this);
			radioPanel.add(jrb);
			bg.add(jrb);
			classes.put(appname, fullappname);
		}
		add(radioPanel, BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new GridLayout(0,1));
		JButton launch = new JButton(MessageFormat.format(Build.messages
				.getString("appli_launch"), node.getNodeName()));
		launch.addActionListener(this);
		launch.setActionCommand("launch");
		launch.addKeyListener(this);
		buttonPanel.add(launch);
		JButton cancel = new JButton(MessageFormat.format(Build.messages
				.getString("appli_cancel"), node.getNodeName()));
		cancel.addActionListener(this);
		cancel.addKeyListener(this);
		cancel.setActionCommand("cancel");
		buttonPanel.add(cancel);
		add(buttonPanel, BorderLayout.SOUTH);
		this.setSize(200, 80 + (40 * classes.size()));
		
		this.addKeyListener(this);
		this.addWindowListener(this);
	}

	/**
	 * Close the frame when close button
	 */
	public void windowClosing(WindowEvent e) {System.out.println("out");
		e.getWindow().dispose();
	}

	public void keyPressed(KeyEvent e) {
		// nothing to do

	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			actionPerformed(new ActionEvent(this,0,"launch"));
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			actionPerformed(new ActionEvent(this,0,"cancel"));
	}

	public void keyTyped(KeyEvent e) {
		// nothing to do

	}

	public void actionPerformed(ActionEvent e) {
		String c = e.getActionCommand();

		if (c.equals("launch") && !appliToLaunch.equals("")) {
			try {
				Class<?> ca = appli_classes.get(classes.get(appliToLaunch));

				Application a = (Application) ca.newInstance();
				a.setAppliName(appliToLaunch);
				node.launchApplication(appliToLaunch, a);
				me.dispose();
			} catch (InstantiationException ei) {
				JOptionPane.showMessageDialog(me, MessageFormat.format(
						Build.messages.getString("insexcep_message"), ei
								.getMessage()), Build.messages
						.getString("alertDialogTitle"),
						JOptionPane.INFORMATION_MESSAGE);

			} catch (IllegalAccessException eac) {
				JOptionPane.showMessageDialog(me, MessageFormat.format(
						Build.messages.getString("illacc_message"), eac
								.getMessage()), Build.messages
						.getString("alertDialogTitle"),
						JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (!c.equals("launch") && !c.equals("cancel")) {
			appliToLaunch = c;
		}
		else if (c.equals("cancel"))
			dispose();
	}

	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
