package simulip.gui.views.build;

import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

import simulip.gui.Build;
import simulip.gui.controller.NetworkController;
import simulip.gui.model.LanGraph;
import simulip.gui.model.LanNodeGraph;
import simulip.gui.model.Link2LanGraph;
import simulip.gui.model.LinkGraph;
import simulip.gui.model.NodeGraph;
import simulip.ip.NetworkMaskFormatException;
import simulip.net.NetworkAddressFormatException;

/**
 * @author nataf
 */
public class LanAddresses extends JFrame implements ActionListener, KeyListener {

	static final long serialVersionUID = 1232343454;
	private NetworkController controler;

	private LanNodeGraph fnode = null;
	private JTextField[] tadd;
	private JTextField[] tmask;
	private NodeGraph[] tnode;
	private LinkGraph[] tlink;
	private String[] oldtadd;
	private String[] oldtmask;
	private int nbint;
	

	public LanAddresses(NetworkController nc, LanGraph lg) {
		super();
		controler = nc;

		addKeyListener(this);
		addWindowListener(new LanWa());

		setAlwaysOnTop(true);

		setTitle(lg.getName());

		fnode = lg.getFakeNode();
		nbint = fnode.getNbLinks();

		setLayout(new GridLayout(nbint + 2, 1));

		JTextField hname = new JTextField(Build.messages
				.getString("lan_name_af"));
		JTextField hip = new JTextField(Build.messages.getString("lan_ip_af"));
		JTextField hmask = new JTextField(Build.messages
				.getString("lan_mask_af"));
		hname.setEditable(false);
		hip.setEditable(false);
		hmask.setEditable(false);
		hname.addKeyListener(this);
		hname.setFocusable(false);
		hip.addKeyListener(this);
		hip.setFocusable(false);
		hmask.addKeyListener(this);
		GridLayout hly = new GridLayout(1, 3);
		Container header = new Container();
		header.setLayout(hly);
		header.add(hname);
		header.add(hip);
		header.add(hmask);
		add(header);

		Container[] tcontadd = new Container[nbint];
		tlink = new Link2LanGraph[nbint];
		tnode = new NodeGraph[nbint];
		tadd = new JTextField[nbint];
		tmask = new JTextField[nbint];
		oldtadd = new String[nbint];
		oldtmask = new String[nbint];

		Hashtable<LinkGraph, int[]> hlg = fnode.getLinksGraphs();

		int i = 0;
		for (Enumeration<LinkGraph> elg = hlg.keys(); elg.hasMoreElements();) {
			LinkGraph lkg = elg.nextElement();
			tlink[i] = lkg;
			NodeGraph node = lkg.getEndPoint(fnode);
			tnode[i] = node;
			String nodename = node.getNodeName();
			JTextField jnodename = new JTextField(nodename);
			jnodename.setEditable(false);
			jnodename.setFocusable(false);

			GridLayout bl = new GridLayout(1, 3);

			tadd[i] = new JTextField();
			tmask[i] = new JTextField();

			tadd[i].addKeyListener(this);
			tmask[i].addKeyListener(this);
			jnodename.addKeyListener(this);

			if (node.isLinkAddressed(lkg)) {
				tadd[i].setText(node.getLinkAddress(lkg));
				oldtadd[i] = tadd[i].getText();
				tmask[i].setText(lkg.getNetworkMask().toString());
				oldtmask[i] = tmask[i].getText();
			} else {
				oldtadd[i] = "";
				oldtmask[i] = "";
			}
			tcontadd[i] = new Container();
			tcontadd[i].setLayout(bl);
			tcontadd[i].add(jnodename);
			tcontadd[i].add(tadd[i]);
			tcontadd[i].add(tmask[i]);

			add(tcontadd[i]);
			i++;
		}
		JButton ok = new JButton(Build.messages.getString("ok"));
		ok.setActionCommand("Myok");
		ok.addActionListener(this);
		ok.setMnemonic(Integer.parseInt(Build.messages.getString("ok_mnemo")));
		ok.addActionListener(this);
		ok.addKeyListener(this);
		add(ok);
		doLayout();
		pack();
		setVisible(true);
	}
	public void actionPerformed(ActionEvent ae) {
		String c = ae.getActionCommand();
		if (c.equals("Myok")) {
			boolean allok = true;

			for (int i = 0; i < nbint; i++) {

				LinkGraph link = tlink[i];
				NodeGraph node = tnode[i];
				String add = tadd[i].getText();
				String mask = tmask[i].getText();

				// not exclusive or fill of address and mask

				if ((add.equals("") && mask.equals(""))
						|| (!add.equals("") && !mask.equals(""))) {

					// is there something changed ?

					if (!add.equals(oldtadd[i]) || !mask.equals(oldtmask[i])) {

						try {

							// is the address already in use by another
							// interface ?

							if (!node.isUsed(add, link)) {
								controler.updateLink2LanAddress(node, link,
										fnode, add, mask);
								Build.changed();
							} else {
								allok = false;
								JOptionPane.showMessageDialog(this,
										MessageFormat.format(Build.messages
												.getString("node_already_add"),
												add, node.getNodeName()), Build.messages
												.getString("error_add_title"),
										JOptionPane.ERROR_MESSAGE);
							}
						} catch (NetworkAddressFormatException e) {
							allok = false;
							JOptionPane.showMessageDialog(this, MessageFormat
									.format(Build.messages
											.getString("node_addr_err"), e
											.getMessage()), Build.messages
									.getString("error_add_title"),
									JOptionPane.ERROR_MESSAGE);
						} catch (NetworkMaskFormatException e) {
							allok = false;
							JOptionPane.showMessageDialog(this, MessageFormat
									.format(Build.messages
											.getString("mask_addr_err"), e
											.getMessage()), Build.messages
									.getString("error_add_title"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
				} else
					allok = false;
			}
			if (allok)
				dispose();
		}
	}

	private class LanWa extends WindowAdapter {
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
			ActionEvent ae = new ActionEvent(this, 0, "Myok");
			actionPerformed(ae);
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			dispose();
	}

	public void keyTyped(KeyEvent arg0) {
		// nothing to do

	}
}
