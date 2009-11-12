package simulip.gui.views.build;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import javax.xml.stream.*;

import simulip.gui.Build;
import simulip.gui.controller.NetworkController;
import simulip.gui.model.LanGraph;
import simulip.gui.model.LanNodeGraph;
import simulip.gui.model.Link2LanGraph;
import simulip.gui.model.LinkGraph;
import simulip.gui.model.NetworkModel;
import simulip.gui.model.NodeGraph;
import simulip.gui.views.share.ApplicationCompilationError;
import simulip.ip.NetworkMaskFormatException;
import simulip.ip.RouteEntry;
import simulip.net.NetworkAddressFormatException;
import simulip.util.Properties;

public class BuildView extends JPanel implements MouseListener,
		MouseMotionListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2480966390216062311L;
	private Graphics offGraphics;
	private Dimension offDimension;
	private Image offImage;
	private Image pc;
	private Image router;
	private Image cursor;
	private Image etrash;
	private Image ftrash;

	private String routerfilename = "images/router.gif";
	private String pcfilename = "images/pc.gif";
	private String cursorfilename = "images/cursor.gif";
	private String etrashfilename = "images/etrash.png";
	private String ftrashfilename = "images/ftrash.png";
	private int routerwidth = 78;
	private int routerhigh = 45;
	private int pcwidth = 60;
	private int pchigh = 72;

	private ResourceBundle messages;
	static public Properties properties = new Properties(
			"simulip.gui.resources");
	private MediaTracker tracker = new MediaTracker(this);

	private NetworkController controler;
	private NetworkModel model;
	/**
	 * current mouse position
	 */
	private int x, y;

	private int Dx, Dy;

	/**
	 * GUI states
	 */
	private int STATE;
	final private int START = 0;
	final private int DRAW_HOST = 1;
	final private int DRAW_ROUTER = 2;
	final private int DRAW_LINK = 3;
	final private int DRAW_LAN = 4;
	final private int LINK_FROM_NODE = 5;
	final private int LINK_FROM_LAN = 6;
	final private int FIRST_CORNER_LAN = 7;
	final private int RESIZE_LAN = 8;
	final private int SELECTION = 9;

	/**
	 * GUI Actions flags
	 */
	private Point firstcornerlan = null;
	private LinkGraph linkSelected = null;
	private NodeGraph nodeSelected = null;
	private NodeGraph firstNodeSelected = null;
	private LanGraph firstLanSelected = null;
	private LanGraph lanSelected = null;
	private int cfirstXselection, cfirstYselection;
	private Vector<NodeGraph> selectednodes = new Vector<NodeGraph>();
	private Vector<LanGraph> selectedlans = new Vector<LanGraph>();

	public BuildView() {
		super();
		try {
			model = new NetworkModel();
			controler = new NetworkController(model);

			setPreferredSize(new Dimension(500, 400));
			addMouseListener(this);
			addMouseMotionListener(this);
			STATE = START;

			messages = Build.messages;

			pcfilename = properties.getOptionalProperty("pcfilename",
					pcfilename);
			routerfilename = properties.getOptionalProperty("routerfilename",
					routerfilename);
			cursorfilename = properties.getOptionalProperty("cursorfilename",
					cursorfilename);
			etrashfilename = properties.getOptionalProperty("etrashfilename",
					etrashfilename);
			ftrashfilename = properties.getOptionalProperty("ftrashfilename",
					ftrashfilename);
			routerwidth = properties.getOptionalIntProperty("routerwidth", 78);
			routerhigh = properties.getOptionalIntProperty("routerhigh", 45);
			pcwidth = properties.getOptionalIntProperty("pcwidth", 60);
			pchigh = properties.getOptionalIntProperty("pchigh", 72);
			pc = Toolkit.getDefaultToolkit().getImage(
					((java.net.URLClassLoader) ClassLoader
							.getSystemClassLoader()).findResource(pcfilename));
			tracker.addImage(pc, 0);
			router = Toolkit.getDefaultToolkit().getImage(
					((java.net.URLClassLoader) ClassLoader
							.getSystemClassLoader())
							.findResource(routerfilename));
			tracker.addImage(router, 1);
			cursor = Toolkit.getDefaultToolkit().getImage(
					((java.net.URLClassLoader) ClassLoader
							.getSystemClassLoader())
							.findResource(cursorfilename));
			tracker.addImage(cursor, 0);
			etrash = Toolkit.getDefaultToolkit().getImage(
					((java.net.URLClassLoader) ClassLoader
							.getSystemClassLoader())
							.findResource(etrashfilename));
			tracker.addImage(etrash, 2);
			ftrash = Toolkit.getDefaultToolkit().getImage(
					((java.net.URLClassLoader) ClassLoader
							.getSystemClassLoader())
							.findResource(ftrashfilename));
			tracker.addImage(ftrash, 3);

			try {
				tracker.waitForID(0);
				tracker.waitForID(1);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		} catch (MissingResourceException mre) {
			Logger.getLogger("").logp(Level.SEVERE, "simulip.gui.Build",
					"main",
					"Can not read MessagesBundle (messages properties files)",
					mre);
		}
	}

	private void changed() {
		Build.changed();
	}

	public void paint(Graphics g) {
		update(g);
	}

	private NodeGraph getNode(int x, int y) {
		Enumeration<Integer> ek = model.getNodes().keys();
		boolean found = false;
		NodeGraph n = null;
		while (ek.hasMoreElements() && !found) {
			n = model.getNodes().get(ek.nextElement());
			int width, high;
			if (n.isRouter()) {
				width = routerwidth;
				high = routerhigh;
			} else {
				width = routerwidth;
				high = routerhigh;
			}
			if (x > n.getX() - width / 2 && x < n.getX() + width / 2
					&& y > n.getY() - high / 2 && y < n.getY() + high / 2)
				found = true;

		}
		if (found)
			return n;
		else
			return null;
	}

	private void createLanGraph(Point p1, Point p2) {
		int x0 = (int) p1.getX();
		int y0 = (int) p1.getY();
		int x = (int) p2.getX();
		int y = (int) p2.getY();
		if (x >= x0 && y >= y0) {
			controler.createLan(x0, y0, x - x0, y - y0);
		} else if (x >= x0 && y < y0) {
			controler.createLan(x0, y, x - x0, y0 - y);
		} else if (x < x0 && y < y0) {
			controler.createLan(x, y, x0 - x, y0 - y);
		} else if (x < x0 && y >= y0) {
			controler.createLan(x, y0, x0 - x, y - y0);
		}
	}

	private LanGraph getLan(int x, int y) {
		Enumeration<LanGraph> elg = model.getLans().elements();
		boolean found = false;
		LanGraph lan = null;
		while (elg.hasMoreElements() && !found) {
			lan = elg.nextElement();
			if (x > lan.getX() && x < lan.getX() + lan.getWidth()
					&& y > lan.getY() && y < lan.getY() + lan.getHigh())
				found = true;
		}
		if (found)
			return lan;
		else
			return null;
	}

	private Point resizeLan(LanGraph l, int x, int y) {
		if (x < l.getX() + 10 && x > l.getX() && y < l.getY() + 10
				&& y > l.getY()) {
			return new Point(l.getX() + l.getWidth(), l.getY() + l.getHigh());
		} else if (x < l.getX() + l.getWidth()
				&& x > l.getX() + l.getWidth() - 10
				&& y < l.getY() + l.getHigh()
				&& y > l.getY() + l.getHigh() - 10) {
			return new Point(l.getX(), l.getY());
		}
		return null;
	}

	private boolean isInDrawMenu(int x, int y) {
		return x > 0 && x < 80 && y > 0 && y < 300;
	}

	private boolean isInHostMenu(int x, int y) {
		return x > 0 && x < 60 && y > 120 && y < 192;
	}

	private boolean isInRouterMenu(int x, int y) {
		return x > 0 && x < 78 && y > 30 && y < 75;
	}

	private boolean isInLinkMenu(int x, int y) {
		return x > 5 && x < 70 && y > 200 && y < 230;
	}

	private boolean isInLanMenu(int x, int y) {
		return x > 5 && x < 70 && y > 250 && y < 280;
	}

	private LinkGraph isOnLink(int x, int y) {
		Enumeration<LinkGraph> el = model.getLinks().elements();
		while (el.hasMoreElements()) {
			LinkGraph lg = el.nextElement();
			if (lg.isOnLink(x, y))
				return lg;
		}
		return null;
	}

	public void mouseClicked(MouseEvent e) {
		changeState(e);
		repaint();

	}

	public void mouseEntered(MouseEvent e) {
		// funny cursor
		setCursor(java.awt.Toolkit.getDefaultToolkit().createCustomCursor(
				cursor, new Point(1, 1), "simulip cursor"));
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		changeState(e);
	}

	public void mouseReleased(MouseEvent e) {
		changeState(e);
	}

	public void mouseDragged(MouseEvent e) {
		Dx = e.getX() - x;
		Dy = e.getY() - y;
		x = e.getX();
		y = e.getY();
		changeState(e);

	}

	public void mouseMoved(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		repaint();
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 27) { // Escape key so return to START state
			nodeSelected = null;
			lanSelected = null;
			linkSelected = null;
			firstcornerlan = null;
			firstLanSelected = null;
			firstNodeSelected = null;
			STATE = START;
			repaint();
		}
		if ((e.getKeyCode() == 8 || e.getKeyCode() == 127)) {
			if (nodeSelected != null) {
				model.getNodes().remove(nodeSelected.getId());
				if (model.getNodes().size() == 0)
					changed();
				Enumeration<LinkGraph> el = model.getLinks().elements();
				while (el.hasMoreElements()) {
					LinkGraph lg = el.nextElement();
					NodeGraph[] ngs = lg.getNodes();
					if (ngs[0].equals(nodeSelected)
							|| ngs[1].equals(nodeSelected)) {
						changed();
						model.getLinks().remove(lg);
						lg.removed();
						el = model.getLinks().elements();
					}
				}
				nodeSelected = null;
			} else if (linkSelected != null) {
				Enumeration<LinkGraph> el = model.getLinks().elements();
				while (el.hasMoreElements()) {
					LinkGraph lg = el.nextElement();
					if (linkSelected.equals(lg)) {
						changed();
						model.getLinks().remove(lg);
						lg.removed();
						linkSelected = null;
						break;
					}
				}
			} else if (lanSelected != null) {

				Enumeration<Link2LanGraph> el2l = lanSelected.getLinks()
						.elements();
				while (el2l.hasMoreElements()) {
					Link2LanGraph l2l = el2l.nextElement();
					l2l.removed();
					model.getLinks().remove(l2l);
					el2l = lanSelected.getLinks().elements();
				}
				model.getLans().remove(lanSelected.getId());
				lanSelected = null;
			}
		}
		repaint();
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Point p = this.getLocationOnScreen();
		p.translate(x, y);

		if (cmd.equals("name")) {
			Frame f = new NodeName(controler, nodeSelected);
			f.setLocation(p);
		}
		if (cmd.equals("addresses")) {
			Frame f = new NodeAddresses(controler, nodeSelected);
			f.setLocation(p);
		}
		if (cmd.equals("routestable")) {
			JFrame f = new RoutingTable(controler, nodeSelected,
					etrash, ftrash);
			f.setLocation(p);

		} else if (cmd.equals("menuapplis")) {
			Frame f = new Applications(controler, nodeSelected);
			f.setLocation(p);
		} else if (cmd.equals("netaddr")) {
			Frame f = null;
			if (linkSelected instanceof Link2LanGraph) {
				f = new LinkAddresses(controler,
						(Link2LanGraph) linkSelected);
			} else {
				f = new LinkAddresses(controler, linkSelected);
			}
			f.setLocation(p);
		} else if (cmd.equals("lanaddr")) {
			Frame f = new LanAddresses(controler, lanSelected);
			f.setLocation(p);
		} else if (cmd.equals("lanname")) {
			Frame f = new LanName(controler, lanSelected);
			f.setLocation(p);
		}

	}

	private void changeState(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int event = e.getID();
		switch (STATE) {
		case START: {
			switch (event) {
			case MouseEvent.MOUSE_PRESSED: {
				if (getNode(x, y) != null) {
					NodeGraph n = getNode(x, y);
					nodeSelected = n;
					lanSelected = null;
					linkSelected = null;
				} else if (getLan(x, y) != null) {
					LanGraph l = getLan(x, y);
					if (l != lanSelected) {
						lanSelected = l;
						nodeSelected = null;
						linkSelected = null;
					} else if (resizeLan(l, x, y) != null) {
						firstcornerlan = resizeLan(l, x, y);
						STATE = RESIZE_LAN;
						lanSelected = l;
						nodeSelected = null;
						linkSelected = null;
					}
				} else if (isOnLink(x, y) != null) {
					LinkGraph lg = isOnLink(x, y);
					linkSelected = lg;
					lanSelected = null;
					nodeSelected = null;
				} else {
					linkSelected = null;
					lanSelected = null;
					nodeSelected = null;
					STATE = SELECTION;
					cfirstXselection = x;
					cfirstYselection = y;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					if (nodeSelected != null) {
						// Popup "Host Configuration" (puoc). Host o=and
						// router
						// indeed...
						JPopupMenu pm = new JPopupMenu(messages
								.getString("puoc_title"));
						JMenuItem mname = new JMenuItem(messages
								.getString("puoc_name_item"));
						mname.setMnemonic(Integer.parseInt(messages
								.getString("puoc_name_item_mnemo")));
						JMenuItem madd = new JMenuItem(messages
								.getString("puoc_add_item"));
						madd.setMnemonic(Integer.parseInt(messages
								.getString("puoc_add_item_mnemo")));
						// KeyEvent.VK_N
						JMenuItem mroutes = new JMenuItem(messages
								.getString("puoc_routeTable_item"));
						mroutes.setMnemonic(Integer.parseInt(messages
								.getString("puoc_routeTable_item_mnemo")));
						// KeyEvent.VK_R
						JMenuItem mapplis = new JMenuItem(messages
								.getString("puoc_applications_item"));
						mapplis.setMnemonic(Integer.parseInt(messages
								.getString("puoc_applications_item_mnemo")));
						// KeyEvent.VK_A
						mname.addActionListener(this);
						mname.setActionCommand("name");
						madd.addActionListener(this);
						madd.setActionCommand("addresses");
						mroutes.addActionListener(this);
						mroutes.setActionCommand("routestable");
						mapplis.addActionListener(this);
						mapplis.setActionCommand("menuapplis");
						pm.add(mname);
						pm.add(madd);
						pm.add(mroutes);
						pm.add(mapplis);
						add(pm);
						pm.show(this, x, y);
						repaint();

					}
					if (linkSelected != null) {
						// Pop up Link Configuration
						JPopupMenu pm = new JPopupMenu(messages
								.getString("pulc_title"));
						JMenuItem mroutes = new JMenuItem(messages
								.getString("pulc_networkAddresses_item"));
						mroutes.addActionListener(this);
						mroutes.setActionCommand("netaddr");
						mroutes
								.setMnemonic(Integer
										.parseInt(messages
												.getString("pulc_networkAddresses_item_mnemo")));
						// KeyEvent.VK_N
						pm.add(mroutes);
						add(pm);
						pm.show(this, x, y);
						repaint();

					}
					if (lanSelected != null) { // Pop up Link Configuration
						JPopupMenu pm = new JPopupMenu(messages
								.getString("pulan_title"));
						JMenuItem lanname = new JMenuItem(messages
								.getString("pulan_name_item"));
						lanname.addActionListener(this);
						lanname.setActionCommand("lanname");
						lanname.setMnemonic(Integer.parseInt(messages
								.getString("pulan_name_item_mnemo")));
						// KeyEvent.VK_N
						pm.add(lanname);
						JMenuItem lanaddr = new JMenuItem(messages
								.getString("pulan_networkAddresses_item"));
						lanaddr.addActionListener(this);
						lanaddr.setActionCommand("lanaddr");
						lanaddr
								.setMnemonic(Integer
										.parseInt(messages
												.getString("pulan_networkAddresses_item_mnemo")));
						// KeyEvent.VK_L
						pm.add(lanaddr);
						add(pm);
						pm.show(this, x, y);
						repaint();
					}

				}
				break;
			}
			case MouseEvent.MOUSE_DRAGGED: {
				for (Enumeration<NodeGraph> eng = selectednodes.elements(); eng
						.hasMoreElements();) {
					NodeGraph n = eng.nextElement();
					n.setX(n.getX() + Dx);
					n.setY(n.getY() + Dy);
					changed();
				}
				for (Enumeration<LanGraph> elg = selectedlans.elements(); elg
						.hasMoreElements();) {
					LanGraph l = elg.nextElement();
					l.setX(l.getX() + Dx);
					l.setY(l.getY() + Dy);
					changed();
				}
				if (nodeSelected != null) {
					nodeSelected.setX(e.getX());
					nodeSelected.setY(e.getY());
					changed();
				} else if (lanSelected != null) {
					lanSelected.setX(e.getX() - lanSelected.getWidth() / 2);
					lanSelected.setY(e.getY() - lanSelected.getHigh() / 2);
					changed();
				}
				break;
			}
			case MouseEvent.MOUSE_CLICKED: {
				if (isInDrawMenu(x, y)) {
					if (isInHostMenu(x, y)) {
						STATE = DRAW_HOST;
					} else if (isInRouterMenu(x, y)) {
						STATE = DRAW_ROUTER;
					} else if (isInLinkMenu(x, y)) {
						STATE = DRAW_LINK;
					} else if (isInLanMenu(x, y)) {
						STATE = DRAW_LAN;
					}
				}
				break;
			}
			}
			break;
		}
		case DRAW_HOST: {
			switch (event) {
			case MouseEvent.MOUSE_PRESSED: {
				if (!isInDrawMenu(x, y)) {
					controler.createHostNode(x, y);
					STATE = START;
					changed();
				}
				break;
			}
			case MouseEvent.MOUSE_CLICKED: {
				if (isInDrawMenu(x, y)) {
					if (isInRouterMenu(x, y)) {
						STATE = DRAW_ROUTER;
					} else if (isInLinkMenu(x, y)) {
						STATE = DRAW_LINK;
					} else if (isInLanMenu(x, y)) {
						STATE = DRAW_LAN;
					}
				}
				break;
			}
			}
			break;
		}
		case DRAW_ROUTER: {
			switch (event) {
			case MouseEvent.MOUSE_PRESSED: {
				if (!isInDrawMenu(x, y)) {
					controler.createRouterNode(x, y);
					STATE = START;
					changed();
				}
				break;
			}

			case MouseEvent.MOUSE_CLICKED: {
				if (isInDrawMenu(x, y)) {
					if (isInHostMenu(x, y)) {
						STATE = DRAW_HOST;
					} else if (isInLinkMenu(x, y)) {
						STATE = DRAW_LINK;
					} else if (isInLanMenu(x, y)) {
						STATE = DRAW_LAN;
					}
				}
				break;
			}
			}
			break;
		}
		case DRAW_LINK: {
			switch (event) {
			case MouseEvent.MOUSE_PRESSED: {
				if (getNode(x, y) != null) {
					NodeGraph n = getNode(x, y);
					firstNodeSelected = n;
					firstLanSelected = null;
					STATE = LINK_FROM_NODE;
				} else if (getLan(x, y) != null) {
					LanGraph l = getLan(x, y);
					firstLanSelected = l;
					firstNodeSelected = null;
					STATE = LINK_FROM_LAN;
				}
				break;
			}
			case MouseEvent.MOUSE_CLICKED: {
				if (isInDrawMenu(x, y)) {
					if (isInRouterMenu(x, y)) {
						STATE = DRAW_ROUTER;
					} else if (isInHostMenu(x, y)) {
						STATE = DRAW_HOST;
					} else if (isInLanMenu(x, y)) {
						STATE = DRAW_LAN;
					}
				}
				break;
			}
			}
			break;
		}
		case LINK_FROM_NODE: {
			switch (event) {
			case MouseEvent.MOUSE_RELEASED: {
				if (getNode(x, y) != null) {
					NodeGraph n = getNode(x, y);
					if (n != firstNodeSelected) {
						if (controler.createLinkGraph(firstNodeSelected, n) != null) {
							STATE = START;
							changed();
						}
					}
				} else if (getLan(x, y) != null) {
					LanGraph l = getLan(x, y);
					if (controler.createLink2LanGraph(firstNodeSelected, l) != null) {
						STATE = START;
						changed();
					}
				}
				break;
			}
			}
			break;
		}
		case LINK_FROM_LAN: {
			switch (event) {
			case MouseEvent.MOUSE_RELEASED: {
				if (getNode(x, y) != null) {
					NodeGraph n = getNode(x, y);
					if (controler.createLink2LanGraph(n, firstLanSelected) != null) {
						STATE = START;
						changed();
					}
				}
				break;
			}
			}
			break;
		}
		case DRAW_LAN: {
			switch (event) {
			case MouseEvent.MOUSE_PRESSED: {
				if (!isInDrawMenu(x, y)) {
					firstcornerlan = e.getPoint();
					STATE = FIRST_CORNER_LAN;
				}
				break;
			}
			case MouseEvent.MOUSE_CLICKED: {
				if (isInDrawMenu(x, y)) {
					if (isInRouterMenu(x, y)) {
						STATE = DRAW_ROUTER;
					} else if (isInHostMenu(x, y)) {
						STATE = DRAW_HOST;
					} else if (isInLinkMenu(x, y)) {
						STATE = DRAW_LINK;
					}
				}
				break;
			}
			}

			break;
		}
		case FIRST_CORNER_LAN: {
			switch (event) {
			case MouseEvent.MOUSE_RELEASED: {
				if (!isInDrawMenu(x, y) && Math.abs(firstcornerlan.x - x) > 10
						&& Math.abs(firstcornerlan.y - y) > 10) {
					createLanGraph(firstcornerlan, e.getPoint());
					STATE = START;
					changed();
				}
				break;
			}
			}
			break;
		}
		case RESIZE_LAN: {
			int x0 = (int) firstcornerlan.getX();
			int y0 = (int) firstcornerlan.getY();
			lanSelected.setWidth(Math.abs(x - x0));
			lanSelected.setHeight(Math.abs(y - y0));
			if (x <= x0 && y > y0) {
				lanSelected.setX(x);
			} else if (x <= x0 && y <= y0) {
				lanSelected.setY(y);
				lanSelected.setX(x);
			} else if (x > x0 && y <= y0) {
				lanSelected.setY(y);
			} else {
				lanSelected.setX(lanSelected.getX());
				lanSelected.setY(lanSelected.getY());
			}
			switch (event) {
			case MouseEvent.MOUSE_RELEASED: {
				STATE = START;
				changed();
			}
			}

			break;
		}
		case SELECTION: {
			switch (event) {
			case MouseEvent.MOUSE_RELEASED: {
				// first clean old selected nodes and lans

				selectednodes.removeAllElements();
				selectedlans.removeAllElements();

				int x0, y0, x1, y1;
				x0 = cfirstXselection < x ? cfirstXselection : x;
				y0 = cfirstYselection < y ? cfirstYselection : y;
				x1 = cfirstXselection > x ? cfirstXselection : x;
				y1 = cfirstYselection > y ? cfirstYselection : y;

				// looks for nodes inside the selection square

				for (Enumeration<NodeGraph> eng = model.getNodes().elements(); eng
						.hasMoreElements();) {
					NodeGraph n = eng.nextElement();
					int width, high;
					if (n.isRouter()) {
						width = routerwidth;
						high = routerhigh;
					} else {
						width = pcwidth;
						high = pchigh;
					}
					int xnode = n.getX();
					int ynode = n.getY();
					if (xnode - width / 2 > x0 && xnode + width / 2 < x1
							&& ynode - high / 2 > y0 && ynode + width / 2 < y1)
						selectednodes.add(n);
				}

				// looks for lans inside the selection square

				for (Enumeration<LanGraph> elg = model.getLans().elements(); elg
						.hasMoreElements();) {
					LanGraph l = elg.nextElement();
					int xlan = l.getX();
					int ylan = l.getY();
					int width = l.getWidth();
					int high = l.getHigh();
					if (xlan > x0 && xlan + width < x1 && ylan > y0
							&& ylan + high < y1)
						selectedlans.add(l);
				}
				STATE = START;
			}
			}
			break;
		}
		}
		repaint();
	}

	public void open(InputStream fi) throws FileNotFoundException,
			XMLStreamException, FactoryConfigurationError,
			 NetworkAddressFormatException,
			NetworkMaskFormatException {
		NetworkModel nmodel = new NetworkModel();
		NetworkController ncont = new NetworkController(nmodel);
		_open(fi, ncont, nmodel);
		model = nmodel;
		controler = new NetworkController(model);

		Enumeration<NodeGraph> en = model.getNodes().elements();
		int minX = 100000, minY = 100000, maxX = 0, maxY = 0;
		while (en.hasMoreElements()) {
			NodeGraph node = en.nextElement();
			int x = node.getX();
			int y = node.getY();
			minX = (x < minX) ? x : minX;
			minY = (y < minY) ? y : minY;
			maxX = (x > maxX) ? x : maxX;
			maxY = (y > maxY) ? y : maxY;
		}
		Enumeration<LanGraph> elans = model.getLans().elements();
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
		}
		setPreferredSize(new Dimension(maxX - minX + 200, maxY - minY + 200));
		repaint();
	}

	private void _open(InputStream in, NetworkController c, NetworkModel m)
			throws XMLStreamException, FactoryConfigurationError,
			NetworkAddressFormatException, NetworkMaskFormatException {

		XMLStreamReader xsr = XMLInputFactory.newInstance()
				.createXMLStreamReader(in);

		if (xsr.getEventType() == XMLStreamReader.START_DOCUMENT) {
			xsr.nextTag();
			if (xsr.getLocalName().equals("topo")) {
				xsr.nextTag();
				while (xsr.hasNext()) {
					if (xsr.getLocalName().equals("lan")) {
						String name = xsr.getAttributeValue(0);
						int id = Integer.parseInt(xsr.getAttributeValue(1));
						int x = Integer.parseInt(xsr.getAttributeValue(2));
						int y = Integer.parseInt(xsr.getAttributeValue(3));
						int w = Integer.parseInt(xsr.getAttributeValue(4));
						int h = Integer.parseInt(xsr.getAttributeValue(5));
						c.createLan(x, y, w, h, name, id);
						xsr.nextTag(); // </lan>
						xsr.nextTag();
					}
					if (xsr.getLocalName().equals("node")) {
						String name = xsr.getAttributeValue(0);
						int id = Integer.parseInt(xsr.getAttributeValue(4));
						int x = Integer.parseInt(xsr.getAttributeValue(1));
						int y = Integer.parseInt(xsr.getAttributeValue(2));
						String ntype = xsr.getAttributeValue(3);
						NodeGraph node = null;
						if (ntype.equals("router"))
							node = c.createRouterNode(x, y, name, id);
						else
							node = c.createHostNode(x, y, name, id);
						xsr.nextTag();
						if (xsr.getLocalName().equals("route")) {
							while (xsr.getLocalName().equals("route")) {
								String dest = "", nh = "", mask = "", ifs = "", met = "";
								xsr.nextTag();
								dest = xsr.getElementText();
								xsr.nextTag();
								mask = xsr.getElementText();
								xsr.nextTag();
								nh = xsr.getElementText();
								xsr.nextTag();
								ifs = xsr.getElementText();
								xsr.nextTag();
								met = xsr.getElementText();
								c.updateRoutingTableEntry(node, dest, mask, nh,
										ifs, met);
								xsr.nextTag();
								xsr.nextTag();
							}
						}
						while (xsr.getLocalName().equals("application")) {
							String app = xsr.getElementText();
							try {
								c.putApplication(node, app);
							} catch (ApplicationCompilationError e) {
								JOptionPane.showMessageDialog(this, e
										.getMessage(), messages
										.getString("errorFileSaving"),
										JOptionPane.ERROR_MESSAGE);
							} catch (ClassCastException e) {
								JOptionPane.showMessageDialog(this, MessageFormat.format(
										Build.messages.getString("cast_message"), e
												.getMessage()), Build.messages
										.getString("alertDialogTitle"),
										JOptionPane.INFORMATION_MESSAGE);
							} catch (FileNotFoundException e) {
								JOptionPane.showMessageDialog(this, MessageFormat
										.format(messages.getString("anfd_message"), e
												.getMessage()), messages
										.getString("alertDialogTitle"),
										JOptionPane.INFORMATION_MESSAGE);
							} catch (ClassNotFoundException e) {
								JOptionPane.showMessageDialog(this, MessageFormat
										.format(messages.getString("anfd_message"), e
												.getMessage()), messages
										.getString("alertDialogTitle"),
										JOptionPane.INFORMATION_MESSAGE);
							} catch (IllegalAccessException e) {
								JOptionPane.showMessageDialog(this, MessageFormat.format(
										messages.getString("illacc_message"), e
												.getMessage()), messages
										.getString("alertDialogTitle"),
										JOptionPane.INFORMATION_MESSAGE);
							} catch (IOException e) {
								JOptionPane.showMessageDialog(this, e.getMessage(), messages
									.getString("alertDialogTitle"),
									JOptionPane.ERROR_MESSAGE);
							} catch (NoClassDefFoundError e) {
								JOptionPane.showMessageDialog(this, e.getMessage(), messages
										.getString("alertDialogTitle"),
										JOptionPane.ERROR_MESSAGE);
								}
							xsr.nextTag();
						}

						xsr.nextTag();
					}
					if (xsr.getLocalName().equals("link")) {
						String mask = xsr.getAttributeValue(0);
						xsr.nextTag(); // <end1>
						xsr.nextTag(); // <id>
						int id1 = Integer.parseInt(xsr.getElementText());
						xsr.nextTag(); // <add>
						String add1 = xsr.getElementText();
						xsr.nextTag(); // </end1>
						xsr.nextTag(); // <end2>
						xsr.nextTag(); // <id>
						int id2 = Integer.parseInt(xsr.getElementText());
						xsr.nextTag(); // <add>
						String add2 = xsr.getElementText();

						NodeGraph n1 = null;
						NodeGraph n2 = null;
						n1 = m.getNode(id1);
						n2 = m.getNode(id2);

						if (n1 != null && n2 != null) {

							LanGraph lan = null;
							NodeGraph node = null;
							if (n1 instanceof LanNodeGraph) {
								lan = ((LanNodeGraph) n1).getLanGraph();
								node = n2;
							} else if (n2 instanceof LanNodeGraph) {
								lan = ((LanNodeGraph) n2).getLanGraph();
								node = n1;
							}

							if (lan == null && node == null) {
								LinkGraph lg = c.createLinkGraph(n1, n2);
								if (add1 != "" && add2 != "" && mask != "")
									c.updateLinkAddresses(n1, lg, n2, add1,
											add2, mask);
							} else {
								Link2LanGraph l2l = c.createLink2LanGraph(node,
										lan);
								if (add1 != "" && mask != "")
									c.updateLink2LanAddress(node, l2l, lan
											.getFakeNode(), add1, mask);
							}

						}

						xsr.nextTag(); // </end2>
						xsr.nextTag(); // </link>
						xsr.nextTag(); // ?
					}
					if (xsr.getLocalName().equals("topo"))
						xsr.next();
				}

			}
		}

	}

	public void save(OutputStream fo) throws IOException, XMLStreamException,
			FactoryConfigurationError {

		XMLStreamWriter xsw = XMLOutputFactory.newInstance()
				.createXMLStreamWriter(fo);

		xsw.writeStartDocument();
		xsw.writeCharacters("\n");
		xsw.writeStartElement("topo");

		Enumeration<NodeGraph> en = model.getNodes().elements();
		while (en.hasMoreElements()) {
			NodeGraph node = en.nextElement();
			xsw.writeCharacters("\n   ");
			xsw.writeStartElement("node");
			xsw.writeAttribute("name", node.getNodeName());
			xsw.writeAttribute("x", (new Integer(node.getX())).toString());
			xsw.writeAttribute("y", (new Integer(node.getY())).toString());
			String type = null;
			if (node.isRouter())
				type = "router";
			else
				type = "pc";
			xsw.writeAttribute("type", type);
			xsw.writeAttribute("id", (new Integer(node.getId())).toString());

			Enumeration<RouteEntry> ere = node.getRouter().getRoutes()
					.elements();
			while (ere.hasMoreElements()) {
				RouteEntry re = ere.nextElement();
				xsw.writeCharacters("\n      ");
				xsw.writeStartElement("route");
				xsw.writeCharacters("\n         ");
				xsw.writeStartElement("dest");
				xsw.writeCharacters(re.getDestination().getStrAddress());
				xsw.writeEndElement();
				xsw.writeCharacters("\n         ");
				xsw.writeStartElement("mask");
				xsw.writeCharacters(re.getMask().toString());
				xsw.writeEndElement();
				xsw.writeCharacters("\n         ");
				xsw.writeStartElement("next");
				xsw.writeCharacters(re.getNextHop().getStrAddress());
				xsw.writeEndElement();
				xsw.writeCharacters("\n         ");
				xsw.writeStartElement("int");
				xsw.writeCharacters(re.getNetIf().getStrAddress());
				xsw.writeEndElement();
				xsw.writeCharacters("\n         ");
				xsw.writeStartElement("met");
				xsw.writeCharacters((new Integer(re.getMetric())).toString());
				xsw.writeEndElement();
				xsw.writeCharacters("\n      ");
				xsw.writeEndElement();
			}

			Enumeration<String> eapp = node.getApplicationClasses().keys();
			while (eapp.hasMoreElements()) {
				String napp = eapp.nextElement();
				xsw.writeCharacters("\n      ");
				xsw.writeStartElement("application");
				xsw.writeCharacters(napp);
				xsw.writeEndElement();
			}
			xsw.writeCharacters("\n   ");
			xsw.writeEndElement();
		}

		Enumeration<LanGraph> elans = model.getLans().elements();
		while (elans.hasMoreElements()) {
			LanGraph lan = elans.nextElement();
			xsw.writeCharacters("\n   ");
			xsw.writeStartElement("lan");
			xsw.writeAttribute("name", lan.getName());
			xsw.writeAttribute("id", new Integer(lan.getId()).toString());
			xsw.writeAttribute("x", new Integer(lan.getX()).toString());
			xsw.writeAttribute("y", new Integer(lan.getY()).toString());
			xsw.writeAttribute("width", new Integer(lan.getWidth()).toString());
			xsw.writeAttribute("high", new Integer(lan.getHigh()).toString());
			xsw.writeEndElement();

		}
		Enumeration<LinkGraph> elg = model.getLinks().elements();
		while (elg.hasMoreElements()) {
			LinkGraph lg = elg.nextElement();
			xsw.writeCharacters("\n   ");
			xsw.writeStartElement("link");
			if (lg.getNetworkMask() != null)
				xsw.writeAttribute("mask", lg.getNetworkMask().toString());
			else
				xsw.writeAttribute("mask", "");
			xsw.writeCharacters("\n      ");

			NodeGraph[] ngs = lg.getNodes();

			xsw.writeStartElement("end1");
			xsw.writeCharacters("\n         ");
			xsw.writeStartElement("id");
			xsw.writeCharacters(new Integer(ngs[0].getId()).toString());
			xsw.writeEndElement();
			xsw.writeCharacters("\n         ");
			xsw.writeStartElement("add");
			xsw.writeCharacters(lg.getEnd0());
			xsw.writeEndElement();
			xsw.writeCharacters("\n      ");
			xsw.writeEndElement();
			xsw.writeCharacters("\n      ");

			xsw.writeStartElement("end2");
			xsw.writeCharacters("\n         ");
			xsw.writeStartElement("id");
			xsw.writeCharacters(new Integer(ngs[1].getId()).toString());
			xsw.writeEndElement();
			xsw.writeCharacters("\n         ");
			xsw.writeStartElement("add");
			xsw.writeCharacters(lg.getEnd1());
			xsw.writeEndElement();
			xsw.writeCharacters("\n      ");
			xsw.writeEndElement();
			xsw.writeCharacters("\n   ");
			xsw.writeEndElement();

		}

		xsw.writeCharacters("\n");
		xsw.writeEndElement();
		xsw.writeEndDocument();

		xsw.flush();

		fo.close();

	}

	public NetworkModel cloneModel() throws IOException,
			NetworkAddressFormatException, NetworkMaskFormatException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, XMLStreamException,
			FactoryConfigurationError, FileNotFoundException {
		PipedOutputStream out = new PipedOutputStream();
		// TODO need an estimation of the model size
		PipedInputStream in = new PipedInputStream(out, 650000);
		save(out);
		NetworkModel clonedmodel = new NetworkModel();
		NetworkController clonedcontroler = new NetworkController(clonedmodel);
		_open(in, clonedcontroler, clonedmodel);
		return clonedmodel;

	}

	/**
	 * Draws network topology, packets and messages
	 */
	public void update(Graphics g) {

		Dimension d = getSize();

		if ((offGraphics == null) || (d.width != offDimension.width)
				|| (d.height != offDimension.height)) {
			offDimension = d;
			offImage = createImage(d.width, d.height);
			offGraphics = offImage.getGraphics();
		}

		// Erase the previous image.

		offGraphics.setColor(Color.white);
		offGraphics.fillRect(0, 0, d.width, d.height);

		// Draws links

		offGraphics.setColor(Color.black);

		Enumeration<LinkGraph> el = model.getLinks().elements();
		while (el.hasMoreElements()) {
			LinkGraph lg = el.nextElement();
			NodeGraph[] ngs = lg.getNodes();
			int x0 = ngs[0].getX();
			int y0 = ngs[0].getY();
			int x1 = ngs[1].getX();
			int y1 = ngs[1].getY();

			offGraphics.drawLine(x0, y0, x1, y1);

			int dx = Math.abs(x0 - x1);
			int dy = Math.abs(y0 - y1);
			double Dx = 0, Dy = 0;
			if (dx != 0) {
				double alpha = Math.atan((double) dy / (double) dx);
				if (dx > 0) {
					Dx = 50 * Math.cos(alpha);
					Dy = 50 * Math.sin(alpha);
				} else {
					Dx = -50 * Math.cos(alpha);
					Dy = -50 * Math.sin(alpha);
				}
			} else {
				if (dy > 0)
					Dy = 50;
				else
					Dy = -50;
			}
			int xcoef, ycoef;
			if (x0 != x1)
				xcoef = (x1 - x0) / (Math.abs(x0 - x1));
			else
				xcoef = x1 - x0;
			if (y0 != y1)
				ycoef = (y1 - y0) / (Math.abs(y0 - y1));
			else
				ycoef = y1 - y0;

			int inum0 = ngs[0].getLinkNumber(lg);
			int inum1 = ngs[1].getLinkNumber(lg);

			offGraphics.drawOval(x0 + (int) Dx * xcoef - 4, y0 - 7 + (int) Dy
					* ycoef, 15, 15);
			offGraphics.setColor(Color.WHITE);
			offGraphics.fillOval(x0 + (int) Dx * xcoef - 3, y0 - 6 + (int) Dy
					* ycoef, 14, 14);
			offGraphics.setColor(Color.BLACK);
			offGraphics.drawString(Integer.toString(inum0), x0 + (int) Dx
					* xcoef, y0 + 5 + (int) Dy * ycoef);
			if (!(lg instanceof Link2LanGraph)) {
				offGraphics.drawOval(x1 + (int) Dx * (-xcoef) - 4, y1 - 7
						+ (int) Dy * (-ycoef), 15, 15);
				offGraphics.setColor(Color.WHITE);
				offGraphics.fillOval(x1 + (int) Dx * (-xcoef) - 3, y1 - 6
						+ (int) Dy * (-ycoef), 14, 14);
				offGraphics.setColor(Color.BLACK);
				offGraphics.drawString(Integer.toString(inum1), x1 + (int) Dx
						* (-xcoef), y1 + 5 + (int) Dy * (-ycoef));
			}
		}

		// Draws Nodes

		Enumeration<Integer> ek = model.getNodes().keys();
		while (ek.hasMoreElements()) {
			NodeGraph n = model.getNodes().get(ek.nextElement());
			Image nodeimage = null;
			int width, high;
			if (n.isRouter()) {
				width = routerwidth;
				high = routerhigh;
				nodeimage = Toolkit.getDefaultToolkit().getImage(
						((java.net.URLClassLoader) ClassLoader
								.getSystemClassLoader())
								.findResource(routerfilename));
			} else {
				nodeimage = Toolkit.getDefaultToolkit().getImage(
						((java.net.URLClassLoader) ClassLoader
								.getSystemClassLoader())
								.findResource(pcfilename));
				width = pcwidth;
				high = pchigh;
			}
			offGraphics.drawImage(nodeimage, n.getX() - width / 2, n.getY()
					- high / 2, width, high, this);
			offGraphics.setColor(Color.black);
			offGraphics.drawString(n.getNodeName(), n.getX() - width / 2 + 10,
					n.getY() - high / 2 - 3);

		}

		// Draws Lan

		Enumeration<LanGraph> elan = model.getLans().elements();
		while (elan.hasMoreElements()) {
			LanGraph lan = elan.nextElement();
			offGraphics.setColor(Color.BLACK);
			offGraphics.drawOval(lan.getX(), lan.getY(), lan.getWidth(), lan
					.getHigh());
			offGraphics.setColor(Color.WHITE);
			offGraphics.fillOval(lan.getX() + 1, lan.getY() + 1,
					lan.getWidth() - 2, lan.getHigh() - 2);
			offGraphics.setColor(Color.black);
			offGraphics.drawString(lan.getName(), lan.getX() + lan.getWidth()
					/ 2 - 10, lan.getY() + lan.getHigh() / 2);
		}
		offGraphics.setColor(Color.black);

		// Draws link or lan icone when linking or laning

		if (STATE == DRAW_LINK) {
			offGraphics.drawLine(x - 30, y - 30, x + 30, y + 30);
		}
		if (STATE == DRAW_LAN) {
			offGraphics.drawOval(x - 30, y - 10, 70, 20);
		}
		if (STATE == SELECTION) {
			int x0 = cfirstXselection;
			int y0 = cfirstYselection;
			int width = 0, high = 0;
			if (x >= x0 && y >= y0) {
				width = x - x0;
				high = y - y0;
			} else if (x < x0 && y >= y0) {
				width = x0 - x;
				high = y - y0;
				x0 = x;
			} else if (x >= x0 && y < y0) {
				width = x - x0;
				high = y0 - y;
				y0 = y;
			} else {
				width = x0 - x;
				high = y0 - y;
				x0 = x;
				y0 = y;

			}
			offGraphics.drawRect(x0, y0, width, high);
		}

		// Draws selected node

		if (nodeSelected != null) {
			int width, high;
			if (nodeSelected.isRouter()) {
				width = routerwidth;
				high = routerhigh;
			} else {
				width = pcwidth;
				high = pchigh;
			}
			offGraphics.drawRect(nodeSelected.getX() - width / 2, nodeSelected
					.getY()
					- high / 2, width, high);
		}

		for (Enumeration<NodeGraph> eng = selectednodes.elements(); eng
				.hasMoreElements();) {
			NodeGraph nodeSelected = eng.nextElement();
			int width, high;
			if (nodeSelected.isRouter()) {
				width = routerwidth;
				high = routerhigh;
			} else {
				width = pcwidth;
				high = pchigh;
			}
			offGraphics.drawRect(nodeSelected.getX() - width / 2, nodeSelected
					.getY()
					- high / 2, width, high);
		}

		// Draws selected link

		if (linkSelected != null) {
			NodeGraph[] ngs = linkSelected.getNodes();
			offGraphics.fillRect(ngs[0].getX(), ngs[0].getY(), 7, 7);
			offGraphics.fillRect(ngs[1].getX(), ngs[1].getY(), 7, 7);
		}

		// Draws selected Lan

		if (lanSelected != null) {
			offGraphics.fillRect(lanSelected.getX(), lanSelected.getY(), 7, 7);
			offGraphics
					.fillRect(lanSelected.getX() + lanSelected.getWidth() - 10,
							lanSelected.getY() + lanSelected.getHigh() - 10, 7,
							7);
		}
		for (Enumeration<LanGraph> elg = selectedlans.elements(); elg
				.hasMoreElements();) {
			LanGraph l = elg.nextElement();
			offGraphics.fillRect(l.getX(), l.getY(), 7, 7);
			offGraphics.fillRect(l.getX() + l.getWidth() - 10, l.getY()
					+ l.getHigh() - 10, 7, 7);

		}

		// Draws the menu

		offGraphics.setColor(Color.black);
		offGraphics.drawLine(85, 0, 85, 300);
		offGraphics.drawLine(0, 300, 85, 300);
		offGraphics.drawString(messages.getString("componentsStr"), 5, 15);
		offGraphics.drawImage(router, 0, 30, routerwidth, routerhigh, this);
		offGraphics.drawString(messages.getString("routerStr"), 17, 90);
		offGraphics.drawImage(pc, 0, 120, pcwidth, pchigh, this);
		offGraphics.drawString(messages.getString("hostStr"), 50, 170);
		offGraphics.drawLine(5, 200, 70, 230);
		offGraphics.drawString(messages.getString("linkStr"), 15, 230);
		offGraphics.drawOval(5, 260, 70, 20);
		offGraphics.drawString(messages.getString("lanStr"), 28, 275);

		if (STATE == DRAW_HOST) {
			Image nodeimage = Toolkit.getDefaultToolkit().getImage(
					((java.net.URLClassLoader) ClassLoader
							.getSystemClassLoader()).findResource(pcfilename));
			offGraphics.drawImage(nodeimage, x - pcwidth / 2, y - pchigh / 2,
					pcwidth, pchigh, this);
			offGraphics.drawString(" ", x - pcwidth / 2, y - pchigh / 2 - 3);

		}
		if (STATE == DRAW_ROUTER) {
			Image nodeimage = Toolkit.getDefaultToolkit().getImage(
					((java.net.URLClassLoader) ClassLoader
							.getSystemClassLoader())
							.findResource(routerfilename));
			offGraphics.drawImage(nodeimage, x - routerwidth / 2, y
					- routerhigh / 2, routerwidth, routerhigh, this);
			offGraphics.drawString(" ", x - routerwidth / 2, y - routerhigh / 2
					- 3);

		}
		if (STATE == LINK_FROM_NODE || STATE == LINK_FROM_LAN) {
			int x1 = 0, y1 = 0;
			if (firstLanSelected != null) {
				x1 = firstLanSelected.getX() + firstLanSelected.getWidth() / 2;
				y1 = firstLanSelected.getY() + firstLanSelected.getHigh() / 2;
			} else if (firstNodeSelected != null) {
				x1 = firstNodeSelected.getX();
				y1 = firstNodeSelected.getY();
			}
			offGraphics.drawLine(x1, y1, x, y);
		}
		if (STATE == FIRST_CORNER_LAN) {
			int x0 = firstcornerlan.x, y0 = firstcornerlan.y;
			if (x0 < x && y0 < y) {
				offGraphics
						.drawOval(x0, y0, Math.abs(x0 - x), Math.abs(y0 - y));
			} else if (x0 > x && y0 < y) {
				offGraphics.drawOval(x, y0, Math.abs(x0 - x), Math.abs(y0 - y));

			} else if (x0 > x && y0 > y) {
				offGraphics.drawOval(x, y, Math.abs(x0 - x), Math.abs(y0 - y));
			} else if (x0 < x && y0 > y) {
				offGraphics.drawOval(x0, y, Math.abs(x0 - x), Math.abs(y0 - y));
			}

		}
		g.drawImage(offImage, 0, 0, this);
	}
}
