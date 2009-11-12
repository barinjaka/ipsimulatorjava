package simulip.gui.views.build;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.*;

import javax.swing.*;

import simulip.gui.Build;
import simulip.gui.controller.NetworkController;
import simulip.gui.model.LinkGraph;
import simulip.gui.model.NodeGraph;
import simulip.ip.NetworkMaskFormatException;
import simulip.net.NetworkAddressFormatException;

/**
 * @author nataf
 */
public class NodeAddresses extends JFrame implements ActionListener,
		KeyListener {

	private static final long serialVersionUID = -3783779760564964654L;
	private NetworkController controler;
	private NodeGraph node;
	private JTextField tname;
	private Container[] tcontadd = null;
	private int[] numint = null;
	private JTextField[] tadd = null;
	private JTextField[] tmask = null;
	private String[] oldtadd = null;
	private String[] oldtmask = null;
	private int nbint = 0;

	public NodeAddresses(NetworkController nc, NodeGraph n) {
		super();
		controler = nc;
		node = n;
		setTitle(node.getNodeName());
		nbint = node.getNbLinks();

		oldtadd = new String[nbint];
		oldtmask = new String[nbint];
		tadd = new JTextField[nbint];
		tmask = new JTextField[nbint];
		tcontadd = new Container[nbint];
		numint = new int[nbint];

		setLayout(new GridLayout(nbint + 3, 1));

		addWindowListener(new NameWa());

		tname = new JTextField(node.getNodeName());
		tname.setEditable(false);
		tname.addKeyListener(this);
		tname.setFocusable(false);

		add(tname);
		JTextField header = new JTextField(Build.messages
				.getString("node_add_header"));
		header.setEditable(false);
		header.addKeyListener(this);
		header.setFocusable(false);
		add(header);

		int i = 0;
		Hashtable<LinkGraph, int[]> hlg = node.getLinksGraphs();
		for (Enumeration<LinkGraph> elg = hlg.keys(); elg.hasMoreElements();) {
			int[] t = new int[1];
			LinkGraph lg = elg.nextElement();
			t = hlg.get(lg);
			JTextField interfacename = new JTextField(MessageFormat.format(
					Build.messages.getString("interface"), t[0]));
			interfacename.setEditable(false);
			interfacename.addKeyListener(this);
			interfacename.setFocusable(false);

			GridLayout bl = new GridLayout(1, 3);

			tadd[i] = new JTextField();
			tmask[i] = new JTextField();
			tadd[i].addKeyListener(this);
			tmask[i].addKeyListener(this);
			if (node.isLinkAddressed(lg)) {
				String add = node.getLinkAddress(lg);
				String mask = lg.getNetworkMask().toString();
				tadd[i].setText(add);
				tmask[i].setText(mask);
				oldtadd[i] = add;
				oldtmask[i] = mask;
			} else {
				oldtadd[i] = "";
				oldtmask[i] = "";
			}
			numint[i] = t[0];
			tcontadd[i] = new Container();
			tcontadd[i].setLayout(bl);
			tcontadd[i].add(interfacename);
			tcontadd[i].add(tadd[i]);
			tcontadd[i].add(tmask[i]);

			add(tcontadd[i]);
			i++;
		}
		JButton ok = new JButton(Build.messages.getString("ok"));
		ok.setActionCommand("Myok");
		ok.setMnemonic(Integer.parseInt(Build.messages.getString("ok_mnemo")));
		ok.addActionListener(this);
		ok.addKeyListener(this);
		add(ok);
		addKeyListener(this);
		doLayout();
		pack();
		setAlwaysOnTop(true);
		setVisible(true);
	}
	public void actionPerformed(ActionEvent a) {
		String c = a.getActionCommand();
		if (c.equals("Myok")) {
			Hashtable<LinkGraph, int[]> hlg = node.getLinksGraphs();
			LinkGraph lg = null;
			boolean allok = true;
			for (int i = 0; i < tcontadd.length; i++) {

				String add = tadd[i].getText();
				String mask = tmask[i].getText();

				// Do not dispose if one of address and mask is not filled

				if ((add.compareTo("") == 0 && mask.compareTo("") != 0)
						|| (add.compareTo("") != 0 && mask.compareTo("") == 0))
					allok = false;

				// is there something changed for this interface ?

				if (add.compareTo(oldtadd[i]) != 0
						|| mask.compareTo(oldtmask[i]) != 0) {

					// Update Node address if address and mask are sets

					if (add.compareTo("") != 0 && mask.compareTo("") != 0) {
						boolean found = false;
						for (Enumeration<LinkGraph> elg = hlg.keys(); elg
								.hasMoreElements()
								&& !found;) {
							int[] t = new int[1];
							lg = elg.nextElement();
							t = hlg.get(lg);
							if (numint[i] == t[0])
								found = true;
						}
						if (found)
							try {
								if (!node.isUsed(add, lg)) {
									try {
										controler.updateNodeAddress(node, lg,
												add, mask);
										Build.changed();
									} catch (NetworkMaskFormatException e) {
										allok = false;
										JOptionPane
												.showMessageDialog(
														this,
														MessageFormat
																.format(
																		Build.messages
																				.getString("mask-addr_err"),
																		e
																				.getMessage()),
														Build.messages
																.getString("error_add_title"),
														JOptionPane.ERROR_MESSAGE);
									}
								} else {
									allok = false;
									JOptionPane
											.showMessageDialog(
													this,

													MessageFormat
															.format(
																	Build.messages
																			.getString("node_already_add"),
																	tadd[i]
																			.getText(),
																	node
																			.getNodeName()),
													Build.messages
															.getString("error_add_title"),
													JOptionPane.ERROR_MESSAGE);
								}
							} catch (NetworkAddressFormatException e) {
								allok = false;
								JOptionPane.showMessageDialog(this,
										MessageFormat.format(Build.messages
												.getString("node_addr_err"), e
												.getMessage()), Build.messages
												.getString("error_add_title"),
										JOptionPane.ERROR_MESSAGE);
							}
					}
				}
			}
			if (allok)
				dispose();
		}
	}

	private class NameWa extends WindowAdapter {
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
