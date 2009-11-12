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
import simulip.gui.model.Link2LanGraph;
import simulip.gui.model.LinkGraph;
import simulip.gui.model.NodeGraph;
import simulip.ip.*;
import simulip.net.NetworkAddress;
import simulip.net.NetworkAddressFormatException;
import simulip.util.Properties;

/**
 * @author nataf
 */
public class LinkAddresses extends JFrame implements ActionListener,
		KeyListener {

	static final long serialVersionUID = 1232343454;
	static private Properties properties = new Properties(
			"simulip.gui.resources");
	private NetworkController controler;
	private LinkGraph link;
	private NodeGraph end0;
	private NodeGraph end1;
	private NodeGraph node;
	private JTextField mask;
	private JTextField node0;
	private JTextField node1;
	private String oldmask;
	private String oldn0;
	private String oldn1;

	private boolean isLanAddress;

	public LinkAddresses(NetworkController nc, Link2LanGraph lg) {
		super();
		controler = nc;
		isLanAddress = true;
		addWindowListener(new NetWa());
		addKeyListener(this);
		link = lg;
		node = lg.getNode();

		setTitle(MessageFormat.format(Build.messages
				.getString("link2lan_af_title"), node.getNodeName(), lg
				.getLanName()));
		setLayout(new BorderLayout());
		JTextField titlemask = new JTextField(Build.messages
				.getString("link_mask"));
		titlemask.setEditable(false);
		titlemask.addKeyListener(this);
		titlemask.setFocusable(false);
		String tmask = "";
		if (lg.getNetworkMask() != null)
			tmask = lg.getNetworkMask().toString();
		oldmask = tmask;
		mask = new JTextField(tmask, 15);
		mask.addKeyListener(this);

		JTextField titleend1 = new JTextField(node.getNodeName());
		titleend1.setEditable(false);
		titleend1.addKeyListener(this);
		titleend1.setFocusable(false);
		String tn0 = null;

		tn0 = lg.getNodeAddress();
		oldn0 = tn0;
		node0 = new JTextField(tn0, 15);
		node0.addKeyListener(this);

		BorderLayout lytmask = new BorderLayout();
		Container cm = new Container();
		cm.setLayout(lytmask);
		cm.add(BorderLayout.WEST, titlemask);
		cm.add(BorderLayout.CENTER, mask);
		add(BorderLayout.NORTH, cm);

		BorderLayout lytend1 = new BorderLayout();
		Container cend1 = new Container();
		cend1.setLayout(lytend1);
		cend1.add(BorderLayout.WEST, titleend1);
		cend1.add(BorderLayout.CENTER, node0);
		add(BorderLayout.CENTER, cend1);

		JButton ok = new JButton(Build.messages.getString("ok"));
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		ok.setMnemonic(Integer.parseInt(Build.messages.getString("ok_mnemo")));
		ok.addKeyListener(this);
		cend1.add(BorderLayout.SOUTH, ok);
		add(BorderLayout.SOUTH, cend1);

		doLayout();
		pack();
		setAlwaysOnTop(true);
		setVisible(true);

	}

	public LinkAddresses(NetworkController nc, LinkGraph lg) {
		super();
		controler = nc;
		isLanAddress = false;
		addWindowListener(new NetWa());
		addKeyListener(this);
		link = lg;
		NodeGraph[] ngs = lg.getNodes();
		end0 = ngs[0];
		end1 = ngs[1];

		setTitle(MessageFormat.format(
				Build.messages.getString("link_af_title"), end0.getNodeName(),
				end1.getNodeName()));

		setLayout(new BorderLayout());

		JTextField titlemask = new JTextField(Build.messages
				.getString("link_mask"));
		titlemask.setEditable(false);
		titlemask.addKeyListener(this);
		titlemask.setFocusable(false);
		String tmask = null;
		if (link.getNetworkMask() != null)
			tmask = link.getNetworkMask().toString();
		oldmask = tmask;
		mask = new JTextField(tmask, 15);
		mask.addKeyListener(this);

		JTextField titleend1 = new JTextField(end0.getNodeName());
		titleend1.setEditable(false);
		titleend1.addKeyListener(this);
		titleend1.setFocusable(false);
		String tn1 = link.getEnd0();
		oldn0 = tn1;
		node0 = new JTextField(tn1, 15);
		node0.addKeyListener(this);

		JTextField titleend2 = new JTextField(end1.getNodeName());
		titleend2.setEditable(false);
		titleend2.addKeyListener(this);
		titleend2.setFocusable(false);
		String tn2 = link.getEnd1();
		oldn1 = tn2;
		node1 = new JTextField(tn2, 15);
		node1.addKeyListener(this);

		BorderLayout lytmask = new BorderLayout();
		Container cm = new Container();
		cm.setLayout(lytmask);
		cm.add(BorderLayout.WEST, titlemask);
		cm.add(BorderLayout.CENTER, mask);
		add(BorderLayout.NORTH, cm);

		BorderLayout lytend1 = new BorderLayout();
		Container cend1 = new Container();
		cend1.setLayout(lytend1);
		cend1.add(BorderLayout.WEST, titleend1);
		cend1.add(BorderLayout.CENTER, node0);
		add(BorderLayout.CENTER, cend1);

		BorderLayout lytend2 = new BorderLayout();
		Container cend2 = new Container();
		cend2.setLayout(lytend2);
		cend2.add(BorderLayout.WEST, titleend2);
		cend2.add(BorderLayout.CENTER, node1);
		JButton ok = new JButton(Build.messages.getString("ok"));
		ok.setActionCommand("ok");
		ok.addKeyListener(this);
		ok.addActionListener(this);
		ok.setMnemonic(Integer.parseInt(Build.messages.getString("ok_mnemo")));
		cend2.add(BorderLayout.SOUTH, ok);
		add(BorderLayout.SOUTH, cend2);

		doLayout();
		pack();
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent a) {
		if (isLanAddress) {
			setLanAddress(a);
		} else {
			String c = a.getActionCommand();
			if (c.equals("ok")) {
				String m = mask.getText();
				String n0 = node0.getText();
				String n1 = node1.getText();

				// do not dispose if uncompleted form

				if ((n0.equals("") && n1.equals("") && m.equals(""))
						|| (!n0.equals("") && !n1.equals("") && !m.equals(""))) {

					// is there something changed

					if (!m.equals(oldmask) || !n0.equals(oldn0)
							|| !n1.equals(oldn1)) {

						try {

							// is all filled ?

							if (!(n0.equals("") || n1.equals("") || m
									.equals(""))) {

								// are addresses on the same network ?

								if (new NetworkAddress(n0).sameNetwork(
										new NetworkAddress(n1),
										new NetworkMask(m))) {

									// are addresses not already used by other
									// node interfaces ?

									if (!end0.isUsed(n0, link)) {
										if (!end1.isUsed(n1, link)) {

											controler.updateLinkAddresses(end0,
													link, end1, n0, n1, m);
											Build.changed();
											dispose();

										} else {
											JOptionPane
													.showMessageDialog(
															this,
															MessageFormat
																	.format(
																			Build.messages
																					.getString("node_already_add"),
																			n1,
																			end1
																					.getNodeName()),
															Build.messages
																	.getString("error_add_title"),
															JOptionPane.ERROR_MESSAGE);
										}
									} else {
										JOptionPane
												.showMessageDialog(
														this,
														MessageFormat
																.format(
																		Build.messages
																				.getString("node_already_add"),
																		n0,
																		end0
																				.getNodeName()),
														Build.messages
																.getString("error_add_title"),
														JOptionPane.ERROR_MESSAGE);
									}

								} else
									JOptionPane
											.showMessageDialog(
													this,
													MessageFormat
															.format(
																	Build.messages
																			.getString("not_same_net"),
																	n0, n1),
													Build.messages
															.getString("error_add_title"),
													JOptionPane.ERROR_MESSAGE);

							} else
								dispose();
						} catch (NetworkAddressFormatException e) {
							JOptionPane.showMessageDialog(this, MessageFormat
									.format(Build.messages
											.getString("node_addr_error"), e
											.getMessage()), Build.messages
									.getString("error_add_title"),
									JOptionPane.ERROR_MESSAGE);
						} catch (NetworkMaskFormatException nme) {
							if (properties.getOptinalBooleanProperty(
									"validateNetworkMask", false)) {
								JOptionPane.showMessageDialog(this,
										MessageFormat.format(Build.messages
												.getString("mask-addr_err"),
												nme.getMessage()),
										Build.messages
												.getString("error_add_title"),
										JOptionPane.ERROR_MESSAGE);
							}
						}
					} else
						dispose();
				}
			}
		}
	}

	private void setLanAddress(ActionEvent a) {
		String c = a.getActionCommand();
		if (c.equals("ok")) {
			String m = mask.getText();
			String n0 = node0.getText();

			// do not dispose if only one field is set

			if ((n0.equals("") && m.equals(""))
					|| (!n0.equals("") && !m.equals(""))) {

				// is there something changed ?

				if (oldn0.compareTo(n0) != 0 || oldmask.compareTo(m) != 0) {

					// is there something ?

					if (!(n0.equals("") || m.equals(""))) {

						// is the address already used by the node (on another
						// interface) ?
						try {
							if (!node.isUsed(n0, link)) {

								controler.updateNodeAddress(node, link, n0, m);
								Build.changed();
								dispose();

							} else {
								JOptionPane.showMessageDialog(this,
										MessageFormat.format(Build.messages
												.getString("node_already_add"),
												n0, end0.getNodeName()),
										Build.messages
												.getString("error_add_title"),
										JOptionPane.ERROR_MESSAGE);
							}
						} catch (NetworkAddressFormatException e) {
							JOptionPane.showMessageDialog(this, MessageFormat
									.format(Build.messages
											.getString("node_addr_error"), e
											.getMessage()), Build.messages
									.getString("error_add_title"),
									JOptionPane.ERROR_MESSAGE);
						} catch (NetworkMaskFormatException nme) {
							if (properties.getOptinalBooleanProperty(
									"validateNetworkMask", false)) {
								JOptionPane.showMessageDialog(this,
										MessageFormat.format(Build.messages
												.getString("mask-addr_err"),
												nme.getMessage()),
										Build.messages
												.getString("error_add_title"),
										JOptionPane.ERROR_MESSAGE);
							}
						}
					} else
						dispose();
				} else
					dispose();
			}
		}
	}

	private class NetWa extends WindowAdapter {
		/**
		 * Close the frame when close button
		 */
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
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
