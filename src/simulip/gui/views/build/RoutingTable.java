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
import java.text.MessageFormat;

import javax.swing.*;

import simulip.gui.Build;
import simulip.gui.controller.NetworkController;
import simulip.gui.model.NodeGraph;
import simulip.ip.*;
import simulip.net.NetworkAddress;
import simulip.net.NetworkAddressFormatException;
import simulip.util.Properties;

/**
 * @author nataf
 */
public class RoutingTable extends JFrame implements ActionListener,
		ItemListener, KeyListener {

	static final long serialVersionUID = 1232343454;
	static private Properties properties = new Properties(
			"simulip.gui.resources");
	private Image empty, full;
	private NetworkController controler;
	private NodeGraph node;
	private Container ct;
	private int rows = 0;

	public RoutingTable(NetworkController nc, NodeGraph n, Image et, Image ft) {
		super();
		controler = nc;
		node = n;
		empty = et;
		full = ft;
		addWindowListener(new irtf());
		addKeyListener(this);
		ct = new Container();
		setTitle(MessageFormat.format(Build.messages.getString("rt_title"),
				node.getNodeName()));
		setAlwaysOnTop(true);
		setVisible(true);
	}

	private void update() {

		setLayout(new BorderLayout());
		ct.setLayout(new GridLayout());
		GridLayout gl = (GridLayout) ct.getLayout();
		gl.setColumns(5);
		java.util.Vector<RouteEntry> vre = node.getRouter().getRoutes();
		int size = vre.size();
		rows = size;
		gl.setRows(1 + size);
		JTextField headremove = new JTextField(Build.messages
				.getString("remove"));
		JTextField headdest = new JTextField(Build.messages
				.getString("rt_dest"));
		JTextField headmask = new JTextField(Build.messages
				.getString("rt_mask"));
		JTextField headnxh = new JTextField(Build.messages.getString("rt_gtw"));
		JTextField headifs = new JTextField(Build.messages.getString("rt_int"));
		JTextField headmet = new JTextField(Build.messages.getString("rt_met"));
		headremove.setEditable(false);
		headdest.setEditable(false);
		headmask.setEditable(false);
		headnxh.setEditable(false);
		headifs.setEditable(false);
		headmet.setEditable(false);
		headremove.setFocusable(false);
		headdest.setFocusable(false);
		headmask.setFocusable(false);
		headnxh.setFocusable(false);
		headifs.setFocusable(false);
		headmet.setFocusable(false);
		headremove.addKeyListener(this);
		headdest.addKeyListener(this);
		headmask.addKeyListener(this);
		headnxh.addKeyListener(this);
		headifs.addKeyListener(this);
		headmet.addKeyListener(this);
		ct.add(headremove);
		ct.add(headdest);
		ct.add(headmask);
		ct.add(headnxh);
		ct.add(headifs);
		ct.add(headmet);
		java.util.Enumeration<RouteEntry> ere = vre.elements();
		while (ere.hasMoreElements()) {
			RouteEntry re = ere.nextElement();
			MyTrashCheckBox cb = new MyTrashCheckBox(new ImageIcon(empty));
			cb.addItemListener(this);
			cb.addKeyListener(this);
			cb.setFocusable(false);
			JTextField tfdest = new JTextField(re.getDestination()
					.getStrAddress());
			JTextField tfmask = new JTextField(re.getMask().toString());
			JTextField tfnxh = new JTextField(re.getNextHop().getStrAddress());
			JTextField tfifs = new JTextField(re.getNetIf().getStrAddress());
			JTextField tfmet = new JTextField(new Integer(re.getMetric())
					.toString());
			tfdest.addKeyListener(this);
			tfmask.addKeyListener(this);
			tfnxh.addKeyListener(this);
			tfifs.addKeyListener(this);
			tfmet.addKeyListener(this);
			ct.add(cb);
			ct.add(tfdest);
			ct.add(tfmask);
			ct.add(tfnxh);
			ct.add(tfifs);
			ct.add(tfmet);
		}
		add(BorderLayout.NORTH, ct);

		Container c = new Container();
		c.setLayout(new BorderLayout());
		JButton ok = new JButton(Build.messages.getString("ok"));
		ok.addActionListener(this);
		ok.addKeyListener(this);
		ok.setActionCommand("Myok");
		ok.setMnemonic(Integer.parseInt(Build.messages.getString("ok_mnemo")));
		c.add(BorderLayout.CENTER, ok);
		JButton add = new JButton(Build.messages.getString("add"));
		add.addActionListener(this);
		add.addKeyListener(this);
		add.setActionCommand("add");
		add
				.setMnemonic(Integer.parseInt(Build.messages
						.getString("add_mnemo")));
		c.add(BorderLayout.WEST, add);
		JButton del = new JButton(Build.messages.getString("remove"));
		del.addActionListener(this);
		del.addKeyListener(this);
		del.setActionCommand("remove");
		ok.setMnemonic(Integer.parseInt(Build.messages
				.getString("remove_mnemo")));
		c.add(BorderLayout.EAST, del);
		add(BorderLayout.SOUTH, c);

		pack();
	}

	private boolean checkRoutes(Component[] routes) {
		int i = 7; // skip headers and first column (check box).
		try {
			for (i = 7; i < routes.length;) {
				JTextField tdest = (JTextField) routes[i++];
				new NetworkAddress(tdest.getText());
				JTextField tmask = (JTextField) routes[i++];
				new NetworkMask(tmask.getText(), true);
				JTextField tnxh = (JTextField) routes[i++];
				new NetworkAddress(tnxh.getText());
				JTextField tifs = (JTextField) routes[i++];
				new NetworkAddress(tifs.getText());
				JTextField tmet = (JTextField) routes[i++];
				Integer.parseInt(tmet.getText());
				i++;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(
					Build.messages.getString("err_met"),
					((JTextField) routes[i - 1]).getText()));
			return false;
		} catch (NetworkAddressFormatException ex) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(
					Build.messages.getString("node_addr_err"),
					((JTextField) routes[i - 1]).getText()), Build.messages
					.getString("error_add_title"), JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (NetworkMaskFormatException nme) {
			if (properties.getOptinalBooleanProperty("validateNetworkMask",
					false)) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(
						Build.messages.getString("mask_addr_err"), nme
								.getMessage()), Build.messages
						.getString("error_add_title"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		String c = e.getActionCommand();
		if (c.equals("Myok")) {
			Component[] routes = ct.getComponents();
			if (checkRoutes(routes)) {
				node.getRouter().removeRoutes(); // TODO controller job
				int i;// Start at 6 because 6 headers
				for (i = 6; i < routes.length;) {
					MyTrashCheckBox trash = (MyTrashCheckBox) routes[i++];
					if (!trash.isFull()) {
						JTextField tdest = (JTextField) routes[i++];
						JTextField tmask = (JTextField) routes[i++];
						JTextField tnxh = (JTextField) routes[i++];
						JTextField tifs = (JTextField) routes[i++];
						JTextField tmet = (JTextField) routes[i++];
						// TODO check if something is changed to
						// call Build.changed()
						Build.changed();
						try {
							controler.updateRoutingTableEntry(node, tdest
									.getText(), tmask.getText(),
									tnxh.getText(), tifs.getText(), tmet
											.getText());
						} catch (NetworkAddressFormatException e1) {
							JOptionPane.showMessageDialog(this, MessageFormat
									.format(Build.messages
											.getString("node_addr_err"), e1
											.getMessage()), Build.messages
									.getString("error_add_title"),
									JOptionPane.ERROR_MESSAGE);
						} catch (NetworkMaskFormatException e1) {
							if (properties.getOptinalBooleanProperty(
									"validateNetworkMask", false)) {
								JOptionPane.showMessageDialog(this,
										MessageFormat.format(Build.messages
												.getString("mask_addr_err"), e1
												.getMessage()), Build.messages
												.getString("error_add_title"),
										JOptionPane.ERROR_MESSAGE);
							}
						}
					} else
						i += 6;
				}
				dispose();
			}
		}
		if (c.equals("add")) {
			rows++;
			GridLayout gl = (GridLayout) ct.getLayout();
			gl.setRows((gl.getRows() + 1));
			MyTrashCheckBox cb = new MyTrashCheckBox(new ImageIcon(empty));
			cb.addItemListener(this);
			JTextField tfdest = new JTextField();
			JTextField tfmask = new JTextField();
			JTextField tfnxh = new JTextField();
			JTextField tfifs = new JTextField();
			JTextField tfmet = new JTextField();
			ct.add(cb);
			ct.add(tfdest);
			ct.add(tfmask);
			ct.add(tfnxh);
			ct.add(tfifs);
			ct.add(tfmet);
			pack();

		}
		if (c.equals("remove")) {
			if (rows > 0) {
				Component[] routes = ct.getComponents();
				for (int i = 6; i < routes.length;) {
					MyTrashCheckBox trash = (MyTrashCheckBox) routes[i];
					if (trash.isFull()) {
						rows--;
						GridLayout gl = (GridLayout) ct.getLayout();
						gl.setRows(rows + 1);
						// node.getRouter().delRoute(
						// ((JTextField) routes[routes.length - 5])
						// .getText());
						Build.changed();

						for (int j = i + 5; j >= i; j--)
							ct.remove(routes[j]);

					}

					i += 6;
				}
				pack();
			}
		}
	}

	private class irtf extends WindowAdapter {
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

	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() instanceof MyTrashCheckBox) {
			MyTrashCheckBox jcb = (MyTrashCheckBox) ie.getSource();
			if (jcb.isFull()) {
				jcb.setIcon(new ImageIcon(empty));
				jcb.setFull(false);
			} else {
				jcb.setIcon(new ImageIcon(full));
				jcb.setFull(true);
			}
		}
	}

	private class MyTrashCheckBox extends JCheckBox {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4540258718294880912L;
		private boolean full = false;

		public MyTrashCheckBox(Icon i) {
			super(i);
		}

		public void setFull(boolean b) {
			full = b;
		}

		public boolean isFull() {
			return full;
		}
	}

	public void keyPressed(KeyEvent arg0) {
		// nothing to do

	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			ActionEvent ae = new ActionEvent(this, 0, "ok");
			actionPerformed(ae);
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			dispose();
	}

	public void keyTyped(KeyEvent arg0) {
		// nothing to do

	}

}
