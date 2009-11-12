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

// TODO: externalize simultationLogger messages (in ResourcesBundles...)
package simulip.ip;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulip.gui.model.NodeGraph;
import simulip.net.Application;
import simulip.net.NetworkAddress;
import simulip.net.Transport;

//import simulip.protocols.icmp.*;
/**
 * The router is an IP router with one or more network interface, a routing
 * table. Applications are binded to this router by the transport entity.
 * Graphical events are coming from the graphical node
 * 
 * @author Emmanuel Nataf
 */
public class IpRouter {
	/**
	 * Messages bundle for simulip.ip package
	 */
	private static ResourceBundle messages;
	static String country = Locale.getDefault().getCountry();
	static {

		try {
			messages = ResourceBundle.getBundle("simulip.ip.MessagesBundle");
		} catch (MissingResourceException mre) {
			Logger
					.getLogger("")
					.logp(
							Level.SEVERE,
							"simulip.ip.RoutingTable",
							"static initialization",
							"Can not read  MessagesBundles (messages properties files)",
							mre);
		}
		/*
		try {
			messages = ResourceBundle.getBundle("simulip.ip.MessagesBundle_"
					+ country);
		} catch (MissingResourceException mre) {
			// Local country not found, try english
			try {

				messages = ResourceBundle
						.getBundle("simulip.gui.MessagesBundle_EN");
			} catch (MissingResourceException mre2) {
				// Not message found... good luck
				// TODO should we exit the simulator or
				// put some English default language in a class ?
				Logger
						.getLogger("")
						.logp(
								Level.SEVERE,
								"simulip.gui.Build",
								"main",
								"Can not read MessagesBundle (messages properties files)",
								mre2);
			}
			Logger
					.getLogger("")
					.logp(
							Level.SEVERE,
							"simulip.ip.RoutingTable",
							"static initialization",
							"Can not read  MessagesBundles (messages properties files)",
							mre);
		}
		*/
	}
	/**
	 * The hash table of network interfaces indexed by their network address
	 */
	protected Hashtable<String, NetworkInterface> net_ifs = new Hashtable<String, NetworkInterface>();
	/**
	 * The routing table
	 */
	private RoutingTable routing_table;
	/**
	 * Local address for router with only one network interface
	 */
	protected NetworkAddress localhost = null;
	/**
	 * The graphical node of this router
	 */
	private NodeGraph node_graph;
	/**
	 * The transport entity on this router
	 */
	private Transport transport;

	/**
	 * Create a IP router with an empty routing table and a new transport entity
	 * 
	 */
	public IpRouter() {
		routing_table = new RoutingTable(this);
		transport = new Transport(this);
	}

	/**
	 * Give the position of the node containing this IP router
	 * 
	 * @return a Point for this ipRouter
	 */
	public java.awt.Point getLocationOnScreen() {
		return node_graph.getLocationOnScreen();
	}

	/**
	 * Set the graphical node for this router
	 * 
	 * @param n
	 *            the Graphical node
	 */
	public void setPhysicalNode(NodeGraph n) {
		node_graph = n;
	}

	/**
	 * Add a new network interface to this router
	 * 
	 * @param ni
	 *            the new network interface TODO: would we introduce an kind of
	 *            "IP FORWARDING" variable on node to get a separate between
	 *            route/host. And when be able to have "multihoming" on one
	 *            host.
	 */
	public void addNetIf(NetworkInterface ni) {
		net_ifs.put(ni.getAddress().getStrAddress(), ni);
		if (net_ifs.size() == 1)
			localhost = ni.getAddress();
		else
			localhost = null;
	}

	/**
	 * Return a string of the network interface(s) of this router
	 * 
	 */
	public String toString() {
		Enumeration<NetworkInterface> en = net_ifs.elements();
		String s = new String();
		while (en.hasMoreElements()) {
			s = s + ", " + en.nextElement().getAddress().getStrAddress();
		}
		return s;
	}

	/**
	 * Add a new route to the routing table of this router
	 * 
	 * @param r
	 *            a string at the routing table format
	 */
	public void addRoute(String r) {
		routing_table.addRoute(r);
	}

	/**
	 * Add a new route to the routing table of this router
	 * 
	 * @param r
	 *            a route entry
	 */
	public void addRoute(RouteEntry r) {
		routing_table.addRoute(r);
	}

	/**
	 * Remove a route from the routing table of this router
	 * 
	 * @param d
	 * @param i
	 * @param string3
	 * @param string2
	 * @param string
	 */
	public void delRoute(String dest, String mask, String nextHop, String ifce,
			int metric) {
		routing_table.removeRoute(dest, mask, nextHop, ifce, metric);
	}

	/**
	 * Remove a route from the routing table of this router
	 * 
	 * @param d
	 */
	public void delRoute(String d) {
		routing_table.removeRoute(d);
	}

	/**
	 * Get routes of the routing table of this router
	 * 
	 * @return a vector of routing entries
	 */
	public Vector<RouteEntry> getRoutes() {
		return routing_table.getRoutes();
	}

	/**
	 * Get routes of the routing table of this router
	 * 
	 * @return an array of string at the routing entry format
	 */
	public String[] getStrRoutes() {
		Enumeration<RouteEntry> er = routing_table.getRoutes().elements();
		int i = 0;
		String[] routes = new String[routing_table.getRoutes().size()];
		while (er.hasMoreElements())
			routes[i++] = er.nextElement().toString();
		return routes;
	}

	/**
	 * Get the local address of this router
	 * 
	 * @return the local address or null if this router has more than one
	 *         network interfaces
	 */
	public NetworkAddress getAddress() {
		return localhost;
	}

	/**
	 * Get the ratio of output network interface buffer filling
	 * 
	 * @return a vector of int for the percentage of buffer filling
	 */
	public Vector<Integer> getOccupation() {
		Vector<Integer> occupation = new Vector<Integer>();
		Enumeration<NetworkInterface> enetifs = net_ifs.elements();
		while (enetifs.hasMoreElements()) {
			NetworkInterface netif = enetifs.nextElement();
			occupation.add(netif.getOccupation());
		}
		return occupation;
	}

	/**
	 * Ask if this router is in a host or in a router
	 * 
	 * @return true if this router has only one network interface TODO: see TODO
	 *         about ipforwarding... A host is a ip euipment which does not
	 *         forward IP packets..
	 */
	public boolean isHost() {
		return net_ifs.size() == 1;
	}

	/**
	 * Get a network interface of this router
	 * 
	 * @param a
	 *            a network address
	 * @return the network interface with the network address or null if not
	 *         present in this router
	 */
	public NetworkInterface getNetIf(NetworkAddress a) {
		return net_ifs.get(a.getStrAddress());
	}

	/**
	 * Remove a NetworkInterface from this Ip router
	 * 
	 * @param a
	 *            the NetworkAddress of the NetworkInterface to remove
	 */
	public void removeNetIf(NetworkAddress a) {
		net_ifs.remove(a.getStrAddress());
	}

	/**
	 * Remove all the routes of this router
	 * 
	 */
	public void removeRoutes() {
		routing_table = new RoutingTable(this);
	}

	/**
	 * Get the network interfaces of this router
	 * 
	 * @return an Enumeration of network interfaces
	 */
	public Enumeration<NetworkInterface> getNetIfs() {
		return net_ifs.elements();
	}

	/**
	 * This method is called by graphical node when it receive a packet or by
	 * the transport entity when an application send some data
	 * 
	 * @param ip
	 *            the incoming datagram
	 */
	public void knock(IpDatagram ip) {
		String rname = this.getNodeName();
		if (ip.getTtl() > 1) {
			ip.setTtl(ip.getTtl() - 1);
			if (ip.getSource() == null) {// What does exactly mean this case ?
				Logger.getLogger("simulationLogger").info(
						MessageFormat.format(messages
								.getString("IpRouter.knock.start"), rname, ip));
				(new RoutingThread(routing_table, ip)).start();
			} else {
				NetworkAddress destination = ip.getDestination();
				if (localhost == null) {// TODO: use isHost() ?
					Enumeration<NetworkInterface> enifs = net_ifs.elements();
					boolean found = false;
					while (!found && enifs.hasMoreElements()) {
						NetworkInterface ni = enifs.nextElement();
						NetworkAddress oneofmyaddr = ni.getAddress();
						NetworkMask mask = ni.getMask();
						NetworkAddress subnetaddr = oneofmyaddr
								.getNetwork(mask);
						NetworkAddress bcast = ni.getBroadcast();
						if (oneofmyaddr.sameNetwork(destination, ni.getMask())) {
							if (destination.getStrAddress().equals(
									ni.getBroadcast().getStrAddress())) {
								Logger
										.getLogger("simulationLogger")
										.info(
												MessageFormat
														.format(
																messages
																		.getString("IpRouter.knock.matchOutBcast"),
																rname, ip,
																oneofmyaddr,
																mask, bcast));
								found = true; // <<<<== TODO: what is this case
												// ?
							} else { // same net, not bcast, not same unicast...
								Logger
										.getLogger("simulationLogger")
										.info(
												MessageFormat
														.format(
																messages
																		.getString("IpRouter.knock.matchOut"),
																rname, ip,
																oneofmyaddr,
																mask,
																subnetaddr));
							}
						}
					}
					if (found) {
						Logger.getLogger("simulationLogger").info(
								MessageFormat.format(messages
										.getString("IpRouter.knock.deliver"),
										rname, ip));
						toUpperLayer(ip);
					} else {
						Logger.getLogger("simulationLogger").info(
								MessageFormat.format(messages
										.getString("IpRouter.knock.start"),
										rname, ip));
						(new RoutingThread(routing_table, ip)).start();
					}
				} else { // localhost !=null ; A PC == a LAN.. TODO: add LAN and
							// ipforwarding concept ?
					NetworkMask mask = net_ifs.get(localhost.getStrAddress())
							.getMask();
					if (localhost.sameNetwork(destination, mask)) {
						if (!localhost.equals(destination)) {
							Logger
									.getLogger("simulationLogger")
									.info(
											MessageFormat
													.format(
															messages
																	.getString("IpRouter.knock.notForMe"),
															rname,
															ip,
															localhost
																	.getStrAddress(),
															mask));

						}
						Logger.getLogger("simulationLogger").info(
								MessageFormat.format(messages
										.getString("IpRouter.knock.deliver"),
										rname, ip));
						toUpperLayer(ip);
					} else {
						Logger
								.getLogger("simulationLogger")
								.info(
										MessageFormat
												.format(
														messages
																.getString("IpRouter.knock.notForMyNet"),
														rname,
														ip,
														localhost
																.getStrAddress(),
														mask));
					}
				}
			}
		} else {
			// TODO: Send an ICMP Time To Live Exceeded
			Logger.getLogger("simulationLogger").info(
					MessageFormat
							.format(messages
									.getString("IpRouter.knock.ttlExceeded"),
									rname, ip));
		}
	}

	/**
	 * the toUpperLayer method to implement to process incoming datagram
	 */
	public void toUpperLayer(IpDatagram ip) {
		if (ip.getType() == IpDatagram.UDP)
			transport.knock(ip);
	}

	/**
	 * Signal to the graphical node that a datagram has been killed by the
	 * router because of its output buffer overflow
	 * 
	 * @param ip
	 *            the killed datagram
	 */
	public void kill(IpDatagram ip) {
		node_graph.kill(ip);
	}

	public NodeGraph getNodeGraph() {
		return node_graph;
	}

	public String getNodeName() {
		return node_graph.getNodeName();
	}

	/**
	 * Add an application on this router
	 * 
	 * @param ipb
	 *            the application
	 */
	public void addBinded(Application ipb) {
		transport.addBinded(ipb);
		ipb.setIpBinding(transport);
	}

	public void removeBinded(String name) {
		transport.removeBinded(name);
	}
}
