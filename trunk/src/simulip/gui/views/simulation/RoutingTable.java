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
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;

import simulip.gui.Build;
import simulip.gui.model.NodeGraph;
import simulip.ip.*;

/**
 * A frame for displaying the routing table of the graphical node
 * 
 * @author Emmanuel Nataf
 */
public class RoutingTable extends JFrame implements KeyListener {

	private static final long serialVersionUID = 132413234;
	/**
	 * The graphical node
	 * 
	 * @uml.property name="node"
	 * @uml.associationEnd
	 */
	private NodeGraph node;
	/**
	 * private listener for frame actions
	 * 
	 * @uml.property name="rtlistener"
	 * @uml.associationEnd
	 */
	private rtWindowListener rtlistener;

	/**
	 * Create a new routing table frame
	 * 
	 * @param n
	 *            the graphical node
	 */
	public RoutingTable(NodeGraph n) {
		super(MessageFormat.format(Build.messages.getString("rt_title"), n
				.getNodeName()));
		node = n;
		node.setRoutingTableFrame(this);
		rtlistener = new rtWindowListener();
		addWindowListener(rtlistener);
	}

	/**
	 * Update of the routing table
	 * 
	 */
	public void update() {
		Vector<RouteEntry> vre = node.getRouter().getRoutes();
		int size = vre.size();
		getContentPane().removeAll();
		getContentPane().setLayout(new GridLayout());
		GridLayout gl = (GridLayout) getContentPane().getLayout();
		gl.setColumns(5);
		gl.setRows(size + 1);
		JTextField headdest = new JTextField(Build.messages
				.getString("rt_dest"));
		JTextField headmask = new JTextField(Build.messages
				.getString("rt_mask"));
		JTextField headnxh = new JTextField(Build.messages.getString("rt_gtw"));
		JTextField headifs = new JTextField(Build.messages.getString("rt_int"));
		JTextField headmet = new JTextField(Build.messages.getString("rt_met"));
		headdest.setEditable(false);
		headmask.setEditable(false);
		headnxh.setEditable(false);
		headifs.setEditable(false);
		headmet.setEditable(false);
		headdest.addKeyListener(this);
		headmask.addKeyListener(this);
		headnxh.addKeyListener(this);
		headifs.addKeyListener(this);
		headmet.addKeyListener(this);
		add(headdest);
		add(headmask);
		add(headnxh);
		add(headifs);
		add(headmet);
		Enumeration<RouteEntry> ere = vre.elements();
		while (ere.hasMoreElements()) {
			RouteEntry re = ere.nextElement();
			JTextField tfdest = new JTextField(re.getDestination()
					.getStrAddress());
			JTextField tfmask = new JTextField(re.getMask().toString());
			JTextField tfnxh = new JTextField(re.getNextHop().getStrAddress());
			JTextField tfifs = new JTextField(re.getNetIf().getStrAddress());
			JTextField tfmet = new JTextField(new Integer(re.getMetric())
					.toString());
			tfdest.setEditable(false);
			tfmask.setEditable(false);
			tfnxh.setEditable(false);
			tfifs.setEditable(false);
			tfmet.setEditable(false);
			tfdest.addKeyListener(this);
			tfmask.addKeyListener(this);
			tfnxh.addKeyListener(this);
			tfifs.addKeyListener(this);
			tfmet.addKeyListener(this);
			add(tfdest);
			add(tfmask);
			add(tfnxh);
			add(tfifs);
			add(tfmet);
		}
		this.pack();
		setAlwaysOnTop(true);
		this.setVisible(true);
	}

	/**
	 * Frame behavior on opening and closing
	 * 
	 * @author Emmanuel
	 * 
	 */
	private class rtWindowListener extends WindowAdapter {
		/**
		 * Display the routing table when opened the frame
		 */
		public void windowOpened(WindowEvent e) {
			// update();
		}

		/**
		 * Close the frame when closing button
		 */
		public void windowClosing(WindowEvent e) {
			node.setRoutingTableFrame(null);
			e.getWindow().dispose();
		}
	}

	public void keyPressed(KeyEvent e) {
		// nothing to do

	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER
				|| e.getKeyCode() == KeyEvent.VK_ESCAPE)
			dispose();
	}

	public void keyTyped(KeyEvent e) {
		// nothing to do

	}
}