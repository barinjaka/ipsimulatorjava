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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import simulip.gui.model.LanGraph;
import simulip.gui.model.LinkGraph;
import simulip.gui.model.NetworkModel;
import simulip.gui.model.NodeGraph;
import simulip.gui.views.share.*;
import simulip.ip.IpDatagram;
import simulip.net.DatagramPacket;
import simulip.net.NetworkAddress;
import simulip.protocols.rip.Rip;
import simulip.util.LoggerFrame;
import simulip.util.Properties;

/**
 * @author nataf Emmanuel
 */

public class SimulationView extends JFrame implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9099041103781406163L;

	private SimulationView2 sv = null;

	private int FPS_INIT = 25;

	public SimulationView(NetworkModel m) throws LinkNotAddressedException {

		sv = new SimulationView2(m);

		int FPS_MIN = 0;
		int FPS_MAX = 50;
		JSlider framesPerSecond = new JSlider(JSlider.VERTICAL, FPS_MIN,
				FPS_MAX, FPS_INIT);

		Enumeration<NodeGraph> en = m.getNodes().elements();
		int minX = 100000, minY = 100000, maxX = 0, maxY = 0;
		while (en.hasMoreElements()) {
			NodeGraph node = en.nextElement();
			node.setSimulationFrame(sv);
			int x = node.getX();
			int y = node.getY();
			minX = (x < minX) ? x : minX;
			minY = (y < minY) ? y : minY;
			maxX = (x > maxX) ? x : maxX;
			maxY = (y > maxY) ? y : maxY;
		}
		Enumeration<LanGraph> elans = m.getLans().elements();
		while (elans.hasMoreElements()) {
			LanGraph lan = elans.nextElement();
			int x = lan.getX();
			int y = lan.getY();
			minX = (x < minX) ? x : minX;
			minY = (y < minY) ? y : minY;
			int xw = x + lan.getWidth();
			int yh = y + lan.getHigh();
			maxX = (xw > maxX) ? xw : maxX;
			maxY = (yh > maxY) ? yh : maxY;
			lan.getFakeNode().setSimulationFrame(sv);
		}

		Enumeration<NodeGraph> enr = m.getNodes().elements();
		while (enr.hasMoreElements()) {
			NodeGraph nr = enr.nextElement();
			nr.setX(nr.getX() - minX + 100);
			nr.setY(nr.getY() - minY + 100);
		}

		setSize(maxX - minX + 250, maxY - minY + 200);
		framesPerSecond.addChangeListener(this);

		// Turn on labels at major tick marks.
		framesPerSecond.setMajorTickSpacing(10);
		framesPerSecond.setMinorTickSpacing(1);
		framesPerSecond.setPaintTicks(true);
		framesPerSecond.setPaintLabels(true);

		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		add(BorderLayout.CENTER, sv);
		add(BorderLayout.EAST, framesPerSecond);
		// pack();
		setVisible(true);

	}

	private int old = FPS_INIT;

	public void stateChanged(ChangeEvent e) {
		int value = (((JSlider) e.getSource()).getValue());

		int delta = value - old;
		old = value;
		sv.setFps(sv.getFps() + delta);

	}

	public class SimulationView2 extends Container implements ActionListener,
			MouseListener, Runnable {

		static final long serialVersionUID = 1232343454;

		private ResourceBundle messages;
		private Properties properties = new Properties("simulip.gui.resources");
		private MediaTracker tracker = new MediaTracker(this);

		/* Possible logger display for "simulationLogger" logger. */

		private LoggerFrame loggerFrame = null;

		private Graphics offGraphics;
		private Dimension offDimension; // flashing eliminator: double buffering
		private Image offImage;
		private int Xmouse;

		private int Ymouse;
		private NodeGraph selected_node;
		private boolean isrunning;
		private Vector<JFrame> subJframes = new Vector<JFrame>();
		/**
		 * Threads for the animation
		 */
		// private Thread simulThread;
		/**
		 * Default value for the graphical refresh; the number of refresh in a
		 * second.
		 */
		private int fps = 25;
		/**
		 * Packets send
		 */
		private Vector<Packet> packets = new Vector<Packet>();
		/**
		 * Nodes (AS routers and end border routers
		 */
		private Hashtable<Integer, NodeGraph> nodes = new Hashtable<Integer, NodeGraph>();
		/**
		 * Links between nodes
		 */
		private Vector<LinkGraph> links = new Vector<LinkGraph>();
		/**
		 * lans
		 */
		private Hashtable<Integer, LanGraph> lans = new Hashtable<Integer, LanGraph>();
		/**
		 * Create a simulation frame
		 * 
		 * @param view
		 * @param m
		 * @param p
		 */

		private Image pc;
		private Image router;
		private String routerfilename = "images/routerSimul.gif";
		private String pcfilename = "images/pcSimul.gif";
		private int routerwidth = 78;
		private int routerhigh = 45;
		private int pcwidth = 60;
		private int pchigh = 72;

		public SimulationView2(NetworkModel model)
				throws LinkNotAddressedException {
			super();
			try {
				pcfilename = properties.getOptionalProperty("pcSimul",
						pcfilename);
				routerfilename = properties.getOptionalProperty("routerSimul",
						routerfilename);
				routerwidth = properties.getOptionalIntProperty(
						"routerSimulWidth", 35);
				routerhigh = properties.getOptionalIntProperty(
						"routerSimulHigh", 45);
				pcwidth = properties.getOptionalIntProperty("pcSimulWidth", 60);
				pchigh = properties.getOptionalIntProperty("pcSimulHigh", 72);
				pc = Toolkit.getDefaultToolkit().getImage(
						((java.net.URLClassLoader) ClassLoader
								.getSystemClassLoader())
								.findResource(pcfilename));
				tracker.addImage(pc, 0);
				router = Toolkit.getDefaultToolkit().getImage(
						((java.net.URLClassLoader) ClassLoader
								.getSystemClassLoader())
								.findResource(routerfilename));
				tracker.addImage(router, 1);

				try {
					tracker.waitForID(0);
					tracker.waitForID(1);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			} catch (MissingResourceException mre) {
				Logger
						.getLogger("")
						.logp(
								Level.SEVERE,
								"simulip.gui.Build",
								"main",
								"Can not read MessagesBundle (messages properties files)",
								mre);
			}

			messages = ResourceBundle.getBundle("simulip.gui.MessagesBundle");
			nodes = model.getNodes();
			links = model.getLinks();
			lans = model.getLans();

			Enumeration<LinkGraph> el = links.elements();
			while (el.hasMoreElements()) {
				LinkGraph link = el.nextElement();
				NodeGraph end0 = link.getNodes()[0];
				NodeGraph end1 = link.getNodes()[1];

				Hashtable<String, LinkGraph> links0 = end0.getLinks();
				Enumeration<String> ena0 = links0.keys();
				boolean found0 = false;
				LinkGraph lg0 = null;
				String na0 = null;
				while (ena0.hasMoreElements() && !found0) {
					na0 = ena0.nextElement();
					try {
						lg0 = end0.getLink(new NetworkAddress(na0));
						found0 = lg0.equals(link);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Hashtable<String, LinkGraph> links1 = end1.getLinks();
				Enumeration<String> ena1 = links1.keys();
				boolean found1 = false;
				LinkGraph lg1 = null;
				String na1 = null;
				while (ena1.hasMoreElements() && !found1) {
					na1 = ena1.nextElement();
					try {
						lg1 = end1.getLink(new NetworkAddress(na1));
						found1 = lg1.equals(link);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (!found0 || !found1)
					throw new LinkNotAddressedException(end0.getNodeName(),
							end1.getNodeName());
			}

			addWindowListener(new SimulListener());
			addMouseListener(this);

			isrunning = true;
			new Thread(this).start();

			Enumeration<LinkGraph> elgs = links.elements();
			while (elgs.hasMoreElements()) {
				LinkGraph lg = elgs.nextElement();
				lg.simul();
			}

			// pack();
			// doLayout();
			// setVisible(true);
			// repaint();
			if (this.properties.getOptinalBooleanProperty("showSimulationLog",
					false)) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						loggerFrame = new LoggerFrame(messages
								.getString("simulationLoggerWtitle"), Logger
								.getLogger("simulationLogger"), "{2}", 30, 80);
					}
				});
			}
		}

		/**
		 * Create a new packet
		 * 
		 * @param x
		 *            origin x coordinate
		 * @param y
		 *            origin y coordinate
		 * @param dx
		 *            horizontal direction
		 * @param dy
		 *            vertical direction
		 * @param ip
		 *            the contained datagram
		 * @param sender
		 *            the graphical node sending the packet
		 */
		public void sended(int x, int y, int dx, int dy, IpDatagram ip,
				NodeGraph sender) {

			packets.add(new Packet(x, y, dx, dy, ip, sender));
		}

		/**
		 * Remove the packet that contain the killed datagram.
		 * 
		 * @param n
		 *            the graphical node where the datagram was killed
		 * @param ip
		 *            the killed IP datagram
		 */
		public void kill(NodeGraph n, IpDatagram ip) {
			for (int i = 0; i < packets.size(); i++) {
				Packet p = (Packet) packets.get(i);
				if (p.getDatagram() == ip) {
					packets.remove(i);
					break;
				}
			}
		}

		public void actionPerformed(ActionEvent e) {

			String cmd = e.getActionCommand();

			if (cmd == "fast") // Faster button pressed
			{
				fps += 2;
			} else if (cmd == "slow" && fps > 2) {
				fps -= 2;
			} else if (cmd == "rst")
				reset_app();
			else if (cmd == "routestable") {
				Point p = this.getLocationOnScreen();
				p.translate(Xmouse, Ymouse);
				RoutingTable rf = null;
				if (selected_node.isSettedRoutingTableFrame())
					rf = selected_node.getRoutingTableFrame();
				else
					rf = new RoutingTable(selected_node);
				rf.update();
				rf.setLocation(p);
				rf.toFront();
				subJframes.add(rf);
			} else if (cmd == "menuapplis") {
				Point p = this.getLocationOnScreen();
				p.translate(Xmouse, Ymouse);
				Applications fa = new Applications(selected_node);
				fa.setLocation(p);
				subJframes.add(fa);
			}
		}

		public void mouseClicked(MouseEvent arg0) {

		}

		public void mousePressed(MouseEvent e) {

			int x = e.getX();
			int y = e.getY();

			int xl = x;
			int yl = y;

			boolean isOnLink = false;
			LinkGraph lg = null;
			Enumeration<LinkGraph> elgs = links.elements();
			while (elgs.hasMoreElements() && !isOnLink) {
				lg = (LinkGraph) elgs.nextElement();
				isOnLink = lg.isOnLink(xl, yl);
			}
			if (isOnLink) {
				if (!lg.isShutdown())
					lg.shutdown();
				else
					lg.up();
			}

			boolean found = false;
			Enumeration<NodeGraph> enode = nodes.elements();
			while (enode.hasMoreElements() && !found) {
				NodeGraph node = (NodeGraph) enode.nextElement();
				int width, high;
				if (node.isRouter()) {
					width = routerwidth;
					high = routerhigh;
				} else {
					width = pcwidth;
					high = pchigh;
				}
				if (x >= node.getX() - width / 2
						&& x <= node.getX() + width / 2
						&& y >= node.getY() - high / 2
						&& y <= node.getY() + high / 2) {
					found = true;
					selected_node = node;
					if (e.getButton() == MouseEvent.BUTTON2
							|| e.getButton() == MouseEvent.BUTTON3) {
						Xmouse = x;
						Ymouse = y;
						JPopupMenu pm = new JPopupMenu(messages
								.getString("puoc_title"));
						JMenuItem mroutes = new JMenuItem(messages
								.getString("puoc_routeTable_item"));
						mroutes.setMnemonic(Integer.parseInt(messages
								.getString("puoc_routeTable_item_mnemo")));
						JMenuItem mapplis = new JMenuItem(messages
								.getString("puoc_applications_item"));
						mapplis.setMnemonic(Integer.parseInt(messages
								.getString("puoc_applications_item_mnemo")));
						mroutes.addActionListener(this);
						mroutes.setActionCommand("routestable");
						mapplis.addActionListener(this);
						mapplis.setActionCommand("menuapplis");
						pm.add(mroutes);
						pm.add(mapplis);
						add(pm);
						pm.show(this, x, y);
						repaint();
					}
				}
			}
			repaint();
		}

		public void mouseReleased(MouseEvent arg0) {
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		/**
		 * restore broken lines, destroy packets and set the framing to default
		 * 
		 */
		public void reset_app() {
			packets = new Vector<Packet>();
			Enumeration<NodeGraph> enodes = nodes.elements();
			while (enodes.hasMoreElements()) {
				NodeGraph node = (NodeGraph) enodes.nextElement();
				node.raz();
			}
			Enumeration<LinkGraph> elgs = links.elements();
			while (elgs.hasMoreElements()) {
				LinkGraph lg = (LinkGraph) elgs.nextElement();
				lg.up();
			}
			fps = 25;
			repaint();
		}

		public void setFps(int f) {
			if (f == 0)
				fps = 1;
			else
				fps = f;

		}

		public int getFps() {
			return fps;
		}

		public void run() {
			while (isrunning) {
				Enumeration<Packet> epack = packets.elements();
				while (epack.hasMoreElements()) {
					Packet p = (Packet) epack.nextElement();
					p.move();
					Enumeration<NodeGraph> enodes = nodes.elements();
					boolean found = false;
					while (enodes.hasMoreElements() && !found) {
						NodeGraph node = (NodeGraph) enodes.nextElement();
						int width = (node.isHost()) ? pcwidth : routerwidth;
						int high = (node.isHost()) ? pchigh : routerhigh;
						boolean isin = p.getX() >= node.getX() - width / 2
								&& p.getX() <= node.getX() + width / 2
								&& p.getY() >= node.getY() - high / 2
								&& p.getY() <= node.getY() + high / 2;

						if (isin) {
							if (p.getSender() != node) {
								found = true;
								packets.remove(p);
								node.knock(p.getDatagram());
							}
						}
					}
					Enumeration<LanGraph> elans = lans.elements();
					int width = 20;
					int high = 20;
					while (elans.hasMoreElements() && !found) {
						LanGraph lan = elans.nextElement();
						NodeGraph fnode = lan.getFakeNode();
						boolean isin = p.getX() >= fnode.getX() - width / 2
								&& p.getX() <= fnode.getX() + width / 2
								&& p.getY() >= fnode.getY() - high / 2
								&& p.getY() <= fnode.getY() + high / 2;
						if (isin) {
							if (p.getSender() != fnode) {
								found = true;
								packets.remove(p);
								fnode.knock(p.getDatagram());
							}
						}
					}
				}
				repaint();
				try {
					Thread.sleep(1000 / fps);
				} catch (InterruptedException e) {
					// java.lang.System.out.println("Main thread interrupted, zut");
					Logger.getLogger("simulationLogger").warning(
							"Main thread interrupted, zut");
				}
			}
		}

		public void paint(Graphics g)
		// To eliminate flushing, update is
		// overriden
		{
			update(g);
		}

		/**
		 * Draws network topology, packets and messages
		 */
		public void update(Graphics g) {

			Dimension d = getSize();

			// Create the offscreen graphics context, if no good one exists.

			if ((offGraphics == null) || (d.width != offDimension.width)
					|| (d.height != offDimension.height)) {
				offDimension = d;
				offImage = createImage(d.width, d.height);
				offGraphics = offImage.getGraphics();
			}

			// Erase the previous image.

			offGraphics.setColor(Color.white);
			offGraphics.fillRect(0, 0, d.width, d.height);

			// draws the links

			Enumeration<LinkGraph> elinks = links.elements();
			while (elinks.hasMoreElements()) {
				LinkGraph link = (LinkGraph) elinks.nextElement();
				if (!link.isShutdown()) {
					NodeGraph[] nodeslink = link.getNodes();
					int x1 = nodeslink[0].getX();
					int y1 = nodeslink[0].getY();
					int x2 = nodeslink[1].getX();
					int y2 = nodeslink[1].getY();

					offGraphics.setColor(Color.black);
					offGraphics.drawLine(x1, y1, x2, y2);
				}
			}

			// draws the lans

			for (Enumeration<LanGraph> elgs = lans.elements(); elgs
					.hasMoreElements();) {
				LanGraph lan = elgs.nextElement();
				offGraphics.setColor(Color.BLACK);
				offGraphics.drawOval(lan.getX(), lan.getY(), lan.getWidth(),
						lan.getHigh());
				offGraphics.setColor(Color.WHITE);
				offGraphics.fillOval(lan.getX() + 1, lan.getY() + 1, lan
						.getWidth() - 2, lan.getHigh() - 2);
				offGraphics.setColor(Color.black);
				offGraphics.drawString(lan.getName(), lan.getX()
						+ lan.getWidth() / 2 - 10, lan.getY() + lan.getHigh()
						/ 2);
			}

			// draws the nodes
			Enumeration<NodeGraph> enodesg = nodes.elements();
			while (enodesg.hasMoreElements()) {
				NodeGraph node = (NodeGraph) enodesg.nextElement();
				int x = node.getX();
				int y = node.getY();
				Image image = null;
				int width, high;
				if (node.isRouter()) {
					image = router;
					width = routerwidth;
					high = routerhigh;
				} else {
					image = pc;
					width = pcwidth;
					high = pchigh;
				}

				offGraphics.drawImage(image, x - width / 2, y - high / 2,
						width, high, this);
				offGraphics.setColor(Color.black);
				offGraphics.drawString(node.getNodeName(), x - width / 2, y
						- high / 2 - 3);

				// draw the buffers node

				Vector<Integer> occupation = node.getOccupation();

				for (int i = 0; i < occupation.size(); i++) {
					int blarge = 5;
					int bheight = 25;
					Integer io = (Integer) occupation.get(i);
					int o = io.intValue();
					int xo = x + width / 2 + i * blarge;
					int yo = y + high / 2;

					float fo = (bheight * o) / 100;
					int ro = (int) fo;

					offGraphics.setColor(Color.black);
					offGraphics.drawRect(xo, yo - bheight, blarge, bheight);
					offGraphics.fill3DRect(xo, yo - ro, blarge, ro, true);

					// draw the killed bubble on top of the buffer

					if (node.hasKill() != 0 && o >= 80) {
						offGraphics.setColor(Color.red);
						offGraphics.fillOval(xo, yo - bheight - blarge, blarge,
								blarge);
					}

				}
			}
			Enumeration<Packet> epack = packets.elements();
			while (epack.hasMoreElements()) {
				Packet p = (Packet) epack.nextElement();
				DatagramPacket dp = (DatagramPacket) p.getDatagram().getData();
				if (dp.getPort() == Rip.PORT)
					offGraphics.setColor(Color.RED);
				else
					offGraphics.setColor(Color.BLUE);
				offGraphics.fill3DRect(p.getX(), p.getY(), 20, 10, true);

			}

			g.drawImage(offImage, 0, 0, this);
		}

		private class SimulListener extends WindowAdapter {
			public void windowOpening(WindowEvent e) {
				e.getWindow().repaint();
			}

			/**
			 * Close the frame when close button
			 */
			public void windowClosing(WindowEvent e) {
				if (loggerFrame != null) {
					loggerFrame.dispose();
				}
				isrunning = false;
				Enumeration<JFrame> ef = subJframes.elements();
				while (ef.hasMoreElements())
					ef.nextElement().dispose();
				subJframes = null;
				e.getWindow().dispose();
			}
		}

	}
}
