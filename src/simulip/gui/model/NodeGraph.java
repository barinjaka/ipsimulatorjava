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
    	
package simulip.gui.model;

import java.util.*;
import java.awt.*;

import javax.swing.*;

import simulip.gui.views.simulation.RoutingTable;
//import simulip.gui.views.simulation.SimulationView;
import simulip.gui.views.simulation.SimulationView.SimulationView2;
import simulip.ip.*;
import simulip.net.Application;
import simulip.net.NetworkAddress;
import simulip.net.NetworkAddressFormatException;

/**
 * The class for graphical nodes, either a router or a end border router both with application capability.
 * @author  Emmanuel Nataf
 */
public class NodeGraph {
	/**
	 * This node identifier
	 * @uml.property  name="id"
	 */
	private int id;
	/**
	 * Node coordinates
	 * @uml.property  name="x"
	 */
	private int x;
	/**
	 * Node coordinates
	 * @uml.property  name="y"
	 */
	private int y;
	/**
	 * Graphical node name, not a DNS one.
	 */
	private String nodename;
	/**
	 * Links linked to this node.
	 * @uml.property  name="links"
	 */
	private Hashtable<String, LinkGraph> links = new Hashtable<String, LinkGraph>();
	/**
	 * The number of datagram killed since the last time
	 */
	private int memory_killed = 0;
	/**
	 * The number of datagram killed since the last time
	 */
	private int /**
	 * the last time in number of isKill method call
	 * 
	 */
	mem_kill_period = 5;
	/**
	 * has this node kill some datagrams since last time
	 */
	private boolean haskill = false;
	/**
	 * The IP entity of this node
	 * @uml.property  name="router"
	 * @uml.associationEnd  
	 */
	private IpRouter router;
	/**
	 * Graphical image for the node
	 */
	private Image baseimage;// , changedImage;
	/**
	 * Graphical panel
	 * @uml.property  name="graphic"
	 * @uml.associationEnd  
	 */
	private SimulationView2 graphic;
	/**
	 * true if the node is a router
	 */
	private boolean isrouter = false;
	private String imagefilename;
	private int imagewidth;
	private int imagehigh;

	public NodeGraph(String n, int px, int py) {
		nodename = n;
		x = px;
		y = py;
		initNumberLinks();
	}

	public NodeGraph(String n, int px, int py, boolean ir) {
		this(n, px, py);
		isrouter = ir;
	}

	public void setSimulationFrame(SimulationView2 g) {
		graphic = g;
	//	MediaTracker tracker = new MediaTracker(graphic);
	//	baseimage = Toolkit.getDefaultToolkit().getImage(
	//			((java.net.URLClassLoader) ClassLoader.getSystemClassLoader())
	//					.findResource(imagefilename));
	//	tracker.addImage(baseimage, 0);
	}

	/**
	 * Ask if the node is a router
	 */
	public boolean isRouter() {
		return isrouter;
	}

	/**
	 * Set the node as a router or not
	 */
	public void setRouter(boolean ir) {
		isrouter = ir;
	}

	/**
	 * Get this node identifier
	 * @return  the int
	 * @uml.property  name="id"
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the identifier of this node. It must be unique
	 * @param i  the node identifier
	 * @uml.property  name="id"
	 */
	public void setId(int i) {
		id = i;
	}

	/**
	 * The isIn method is used to know if a mouse event is in the node, that
	 * inside the box from node center coordinates and width and higth.
	 * 
	 * @param px
	 *            x coordinate
	 * @param py
	 *            y coordinate
	 * @return true if coordinates are in the node
	 */
	public boolean isIn(int px, int py) {
		return px >= x - getImageWidth() / 2 && px <= x + getImageWidth() / 2
				&& py >= y - getImageHigh() / 2 && py <= y + getImageHigh() / 2;
	}

	/**
	 * Get the location on screen of this graphical node
	 * 
	 * @return a Point for this node
	 */
	public java.awt.Point getLocationOnScreen() {
		java.awt.Point p = graphic.getLocationOnScreen();
		p.translate(getX(), getY());
		return p;
	}

	/**
	 * To initialize the IP router
	 * @param ipr  the IP router
	 * @uml.property  name="router"
	 */
	public void setRouter(IpRouter ipr) {
		router = ipr;
	}

	/**
	 * Reset node (actually do nothing)
	 * 
	 */
	public void raz() {
	}

	/**
	 * Get the x coordinate of this node in the graphical.
	 * @return  the x coordinate
	 * @uml.property  name="x"
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get the rate of datagram in the output buffer by network interface.
	 * 
	 * @return a Vector of number between 0 and 100
	 */
	public Vector<Integer> getOccupation() {
		return router.getOccupation();
	}

	/**
	 * Get the y coordinate of this node.
	 * @return  the y coordinate
	 * @uml.property  name="y"
	 */
	public int getY() {
		return y;
	}

	/**
	 * Get the node name (not the DNS name).
	 * 
	 * @return the graphical node name
	 */
	public String getNodeName() {
		return nodename; 
	}

	/**
	 * Set the node name
	 * 
	 * @param n
	 *            the String for the node name
	 */
	public void setName(String n) {
		nodename = n;
	}
	
	private int nblinks = 0;
	private boolean[] intnumbers = new boolean[100]; // 100 interfaces should be enough ;-)
	
	private void initNumberLinks(){
		for (int i = 0; i < 100; i++)
			intnumbers[i] = false;
	}
	private int getFirstFreeNumberLink(){
		int i = 1;
		for (i = 1; intnumbers[i] && i < 100; i++);
		intnumbers[i] = true;
		return i;
	}
	private Hashtable<LinkGraph, int[]> linksByNumber = new Hashtable<LinkGraph, int[]>();

	/**
	 * Add a link to this node with a network adress.
	 * 
	 * @param na
	 *            the network address (IP address)
	 * @param lg
	 *            the link
	 */
	public void addLink(NetworkInterface ni, LinkGraph lg) {
		links.put(ni.getAddress().getStrAddress(), lg);
	}
	
	public void removeLink(LinkGraph lg){
		
		links.remove(getLinkAddress(lg));
		
	}
	
	public boolean isLinkAddressed(LinkGraph lg){
		boolean found = false;
		for (Enumeration<LinkGraph> elg = links.elements(); elg.hasMoreElements() && !found;){
			LinkGraph ilg = elg.nextElement();
			found = ilg == lg;
		}
		return found;
	}
	
	public String getLinkAddress(LinkGraph lg){
		if (isLinkAddressed(lg)){
			boolean found = false;
			String add = null;
			for (Enumeration<String> eadd = links.keys(); eadd.hasMoreElements()&& !found;){
				add = eadd.nextElement();
				found = links.get(add) == lg;
			}
			if (found)
					return add;
			else 
				return "";
		}
		return "";
	}
	
	public void addLinkGraph(LinkGraph lg){
		nblinks++;
		int t[] = new int[1];
		t[0] = getFirstFreeNumberLink();
		linksByNumber.put(lg, t);
		
	}
	
	public int getLinkNumber(LinkGraph lg){
		int t[] = linksByNumber.get(lg);
		return t[0];
	}

	public void removeLinkGraph(LinkGraph lg) {
		int t[] = linksByNumber.get(lg);
		intnumbers[t[0]] = false;
		linksByNumber.remove(lg);
		nblinks--;
	}
	
	public Hashtable<LinkGraph, int[]> getLinksGraphs(){
		return linksByNumber;
	}
	
	public int getNbLinks(){
		return nblinks;
	}

	/**
	 * Add a route to the router
	 * 
	 * @param r
	 *            the new IP route
	 * @see RouteEntry
	 */
	public void addRoute(String r) {
		router.addRoute(r);
	}

	/**
	 * Get a a graphical link attached to this node by one of the network
	 * interface of this node.
	 * 
	 * @param na
	 *            the network address of the link
	 * @return the link or null if not found
	 */
	public LinkGraph getLink(NetworkAddress na) {
		return (LinkGraph) links.get(na.getStrAddress());
	}

	/**
	 * Get all the graphicals links
	 * @return  a hash table of link indexed by a string for the network address
	 * @uml.property  name="links"
	 */
	public Hashtable<String, LinkGraph> getLinks() {
		return links;
	}

	/**
	 * Get the IP router of this node
	 * @return  the
	 * @see  IpRouter
	 * @uml.property  name="router"
	 */
	public IpRouter getRouter() {
		return router;
	}

	/**
	 * Ask if the node is a host or a router
	 * 
	 * @return true if no more one network link is attached to this node
	 */
	public boolean isHost() {
		return router.isHost();
	}

	/**
	 * Kill the datagram
	 * 
	 * @param ip
	 *            the killed datagram
	 */
	public void kill(IpDatagram ip) {
		graphic.kill(this, ip);
		haskill = true;
	}

	/**
	 * Get the number of killed datagram since this method has been called
	 * 
	 * @return the number of killed datagram
	 */
	public int hasKill() {
		if (haskill) {
			memory_killed = (memory_killed + 1) % mem_kill_period;
			if (memory_killed == (mem_kill_period - 1)) {
				memory_killed = 0;
				haskill = false;
			}
		} else
			return 0;
		return memory_killed;
	}

	/**
	 * Give a incoming datagram to the Ip router
	 * 
	 * @param ip
	 *            the incoming datagram
	 */
	public void knock(IpDatagram ip) {
		router.knock(ip);
	}

	/**
	 * Send a datagram to the next node to the destination
	 * 
	 * @param ni
	 *            the network interface to send the datagram
	 * @param ip
	 *            the datagram
	 */
	public void sended(NetworkInterface ni, IpDatagram ip) {
		// ********************
		// * Test if there is no destination not found error
		// * in this case, ni is null
		// * To remove when ICMP message will be emitted
		// ********************
		if (ni != null) {
			LinkGraph lg = getLink(ni.getAddress());
			if (!lg.isShutdown()) {
				NodeGraph ep = lg.getEndPoint(this);
				int destx = ep.getX();
				int desty = ep.getY();
				int dx = destx - x;
				int dy = desty - y;
				int xc, yc;
				xc = x;
				yc = y;
				graphic.sended(xc, yc, dx, dy, ip, this);
			}
		}
	}

	/**
	 * Enable ICMP capability TO DO remove when application
	 * 
	 */
	/*
	 * public void icmpEnable(){ router.setIcmpEntity(); }
	 */
	/**
	 * enable RIP capability TO DO remove when applcation
	 * 
	 */
	/*
	 * public void ripEnable(){ router.setRipEntity(); }
	 */
	/**
	 * Ask if the IP router has changed (new routes metrics)
	 * 
	 * @return true if routes has been modified
	 */
	public boolean isChanged() {

		boolean changed = false;
		Enumeration<String> esapp = applications.keys();
		while (esapp.hasMoreElements()) {
			String sa = esapp.nextElement();
			Application a = applications.get(sa);
			boolean isc = a.isChanged();
			changed = changed || isc;
		}
		return changed;
	}

	/**
	 * Update of the routing table frame
	 * 
	 */
	public void update() {
		if (routing_table_frame != null) {
			routing_table_frame.update();
		}
	}

	/**
	 * A frame for the routing table of this node
	 * @uml.property  name="routing_table_frame"
	 * @uml.associationEnd  
	 */
	private RoutingTable routing_table_frame = null;

	/**
	 * Set the routing table frame
	 * 
	 * @param rtf
	 *            a routing table frame
	 */
	public void setRoutingTableFrame(RoutingTable rtf) {
		routing_table_frame = rtf;
	}

	public RoutingTable getRoutingTableFrame() {
		return routing_table_frame;
	}

	public boolean isSettedRoutingTableFrame() {
		return routing_table_frame != null;
	}

	/**
	 * Stop or resume the time
	 * 
	 * @param s
	 *            true if the time must be stoped, false if not
	 */
	public void setStoppedTime(boolean s) {
		// router.setStoppedTime(s);
	}

	/**
	 * Applications hosted by this node
	 * @uml.property  name="applications"
	 */
	private Hashtable<String, Application> applications = new Hashtable<String, Application>();

	private Hashtable<String, Class<?>> appli_classes = new Hashtable<String, Class<?>>();
	
	/**
	 * Set a new application to this node
	 * 
	 * @param name
	 *            the name of the java class of the application
	 * @param application
	 *            the java class of the application
	 */
	
	public void launchApplication(String name, Application application){
		router.addBinded(application);
		new Thread(application).start();
	}
	
	public Class<?> putApplicationClass(String name, Class<?> application) {
		return appli_classes.put(name, application);
	}

	public void removeApplication(String name) {
		appli_classes.remove(name);
	}


	public Hashtable<String, Class<?>> getApplicationClasses(){
		return appli_classes;
	}

	public Image getImage() {
		if (!isChanged())
			return baseimage;
		else
			return baseimage;
	}

	public void showImage(JFrame g) {
		baseimage = g.getToolkit().createImage(imagefilename);
	}

	public int getImageWidth() {
		return imagewidth;
	}

	public int getImageHigh() {
		return imagehigh;
	}

	public void setX(int x) {
		this.x = x;
	}


	public void setY(int y) {
		this.y = y;
	}

	public boolean equals(NodeGraph n) {
		return n.getNodeName().equals(getNodeName());
	}
	
	public boolean isUsed(String add, LinkGraph lg)
			throws NetworkAddressFormatException{

		java.util.Enumeration<NetworkInterface> eni0 = getRouter().getNetIfs();
		boolean found = false;
		// Is there the IP address on the same host or router with a
		// different link ?
		while (eni0.hasMoreElements() && !found) {
			NetworkInterface ini0 = eni0.nextElement();
			LinkGraph lg0 = ini0.getLinkGraph();
			
				if (ini0.getAddress().sameNetwork(new NetworkAddress(add),
						ini0.getMask())
						&& lg0 != lg)
					found = true;
			
		}
		return found;
	}
}
