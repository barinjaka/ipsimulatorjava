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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

import javax.swing.*;

import simulip.gui.Build;
import simulip.gui.controller.NetworkController;
import simulip.gui.model.NodeGraph;
import simulip.ip.*;

/**
 * @author nataf
 */
public class NodeName extends JFrame implements ActionListener , KeyListener{

	static final long serialVersionUID = 1232343454;
	private NetworkController controler;
	private NodeGraph node;
	private JTextField tname;
	private String oldname;

	public NodeName(NetworkController nc, NodeGraph n) {
		super();
		controler = nc;
		node = n;
		oldname = n.getNodeName();
		setTitle(oldname);
		addWindowListener(new NameWa());
		addKeyListener(this);

		tname = new JTextField(oldname);
		tname.addKeyListener(this);

		add(BorderLayout.NORTH, tname);

		String ifs = new String();
		Enumeration<NetworkInterface> eni = node.getRouter().getNetIfs();
		while (eni.hasMoreElements()) {
			NetworkInterface ni = eni.nextElement();
			ifs = ifs + ni.getAddress().getStrAddress() + "; "
					+ ni.getMask().toString() + "\n";
		}
		JTextArea tifs = new JTextArea(ifs);
		tifs.setEditable(false);
		tifs.addKeyListener(this);
		add(BorderLayout.CENTER, tifs);

		JButton ok = new JButton(Build.messages.getString("ok"));
		ok.setActionCommand("ok");
		ok.setMnemonic(Integer.parseInt(Build.messages.getString("ok_mnemo")));
		ok.addActionListener(this);
		ok.addKeyListener(this);
		add(BorderLayout.SOUTH, ok);

		doLayout();
		pack();
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent arg0) {
		String c = arg0.getActionCommand();
		if (c.equals("ok"))
			if (oldname.compareTo(tname.getText().trim()) != 0) {
				controler.updateNodeName(node, tname.getText().trim());
				Build.changed();
			}
		dispose();
	}

	private class NameWa extends WindowAdapter {
		/**
		 * Close the frame when close button
		 */
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}
	}

	public void keyPressed(KeyEvent e) {
		// nothing to do
		
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			ActionEvent ae = new ActionEvent(this,0,"ok");
			actionPerformed(ae);
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			dispose();
		
	}

	public void keyTyped(KeyEvent e) {
		// nothing to do
		
	}

}
