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
package simulip.ip;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulip.net.NetworkAddress;

/**
 * A RoutingTable contains one or more RoutingEntry and one at more default
 * route entry. There is only one route for a destination, should be improved
 * 
 * @author Emmanuel Nataf TODO: check synchronization policy (may be an
 *         overkill, simple synchronized methods might be good enough.
 */
public class RoutingTable {
	private static ResourceBundle messages;
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
	}
	private static Logger simulationLogger = Logger
			.getLogger("simulationLogger");
	/**
	 * Do we handle multiple routes to same network...
	 */
	private static boolean checkAll = simulip.gui.views.build.BuildView.properties
			.getOptinalBooleanProperty(
					"simulip.ip.RoutingTable.nextHop.checkAllEntries", false);

	/**
	 * A Hashtable of @see simulip.ip.RouteEntry indexed by their destination
	 * address in a dotted string notation
	 * 
	 * @uml.property name="routes"
	 */
	private Hashtable<String, RouteEntry> routes = new Hashtable<String, RouteEntry>();
	/**
	 * The Containing @see IpRouter
	 * 
	 * @uml.property name="router"
	 * @uml.associationEnd
	 */
	private IpRouter router;
	/**
	 * Is this routing table currently updated
	 */
	private boolean updated = false;
	/**
	 * Is this routing table currently read
	 */
	private boolean readed = false;

	/**
	 * Create a new IP routing table with its {@link simulip.ip.IpRouter}
	 * 
	 * @param r
	 *            the containing @see IpRouter
	 */
	public RoutingTable(IpRouter r) {
		router = r;
	}

	public String genRouteKey(RouteEntry re) {
		return genRouteKey(re.getDestination().getStrAddress(), re.getMask()
				.toString(), re.getNextHop().getStrAddress(), re.getNetIf()
				.getStrAddress(), re.getMetric());
	}

	public String genRouteKey(String dest, String mask, String nextHop,
			String netIf, int metric) {
		return dest + "/" + mask + "-" + nextHop + "-" + netIf + "-" + metric;
	}

	/**
	 * Add a new route to this RoutinTable
	 * 
	 * @param r
	 *            a string formated route
	 */
	synchronized public void addRoute(String r) {
		while (readed)
			try {
				wait();
			} catch (Exception e) {
			}
		updated = true;
		RouteEntry re = new RouteEntry(r);
		// routes.put(re.getDestination().getStrAddress(), re);
		addRouteUnsync(re);
		updated = false;
		notifyAll();
	}

	/**
	 * Add a new route to this RoutingTable
	 * 
	 * @param re
	 *            the RouteEntry
	 */
	synchronized public void addRoute(RouteEntry re) {
		while (readed)
			try {
				wait();
			} catch (Exception e) {
			}
		updated = true;
		// routes.put(re.getDestination().getStrAddress(), re);
		addRouteUnsync(re);
		updated = false;
		notifyAll();
	}

	/**
	 * Add a new route to this RoutingTable NOTE: This method is not thread
	 * save. It MUST only be called from one of the 2 others addRoute.
	 * 
	 * @param re
	 */
	private void addRouteUnsync(RouteEntry re) {
		if (checkAll) {
			routes.put(genRouteKey(re), re);
		} else {
			routes.put(re.getDestination().getStrAddress(), re);
		}
	}

	/**
	 * Remove any route to a destination network from this RoutingTable
	 * 
	 * @param fdest
	 *            the destination of the route to remove
	 * @param metric
	 * @param ifce
	 * @param nextHop
	 * @param mask
	 */
	synchronized public void removeRoute(String fdest, String mask,
			String nextHop, String ifce, int metric) {
		while (readed)
			try {
				wait();
			} catch (Exception e) {
			}
		updated = true;
		/*
		 * TODO: clarify this. Route table are maps. Keys are dest net. javadoc
		 * for HashTable: "Associates the specified value with the specified key
		 * in this map. If the map previously contained a mapping for this key,
		 * the old value is replaced." So... a simple remove(fdest) is enough
		 * !!!!! But, LA wants several routes to same net (let say one unique
		 * for a give out ifc and metric... nextHop is already changed to
		 * support this.
		 */
		if (checkAll) {
			routes.remove(genRouteKey(fdest, mask, nextHop, ifce, metric));
		} else {
			for (int i = 0; i < routes.size(); i++) {
				RouteEntry route = routes.get(fdest);
				if (route != null) {
					NetworkAddress dest = route.getDestination();
					String sd = dest.getStrAddress();
					if (sd.equals(fdest)) {
						routes.remove(sd);
						i = routes.size();
					}
				}
			}
		}
		updated = false;
		notifyAll();
	}

	/**
	 * Remove a route from this RoutinTable
	 * 
	 * @param fdest
	 *            the destination of the route to remove
	 */
	synchronized public void removeRoute(String fdest) {
		while (readed)
			try {
				wait();
			} catch (Exception e) {
			}
		updated = true;
		/*
		 * TODO: clarify this. Route table are maps. Keys are dest net. javadoc
		 * for HashTable: "Associates the specified value with the specified key
		 * in this map. If the map previously contained a mapping for this key,
		 * the old value is replaced." So... a simple remove(fdest) is enough
		 * !!!!! But, LA wants several routes to same net (let say one unique
		 * for a give out ifc and metric... nextHop is already changed to
		 * support this.
		 */
		for (int i = 0; i < routes.size(); i++) {
			RouteEntry route = routes.get(fdest);
			if (route != null) {
				NetworkAddress dest = route.getDestination();
				String sd = dest.getStrAddress();
				if (sd.equals(fdest)) {
					routes.remove(sd);
					i = routes.size();
				}
			}
		}
		updated = false;
		notifyAll();
	}

	/**
	 * Get all the RouteEntry of this RoutingTable
	 * 
	 * @return a Vector of RouteEntry
	 * @uml.property name="routes"
	 */
	synchronized public Vector<RouteEntry> getRoutes() {
		while (updated)
			try {
				readed = true;
				wait();
			} catch (Exception e) {
			}
		Vector<RouteEntry> v = new Vector<RouteEntry>(routes.values());
		readed = false;
		notifyAll();
		return v;
	}

	/**
	 * Compute the NetworkInterface to use to send a datagram toward the
	 * destination
	 * 
	 * @param ip
	 *            the datagram to send
	 * @return the NetworkInterface to use or null if not found
	 */
	public synchronized NetworkInterface nextHop(IpDatagram ip) {
		while (updated)
			try {
				readed = true;
				wait();
			} catch (Exception e) {
			}
		NetworkAddress netif = null;
		NetworkAddress nexth = null;
		NetworkAddress dest = ip.getDestination();
		boolean found = false;


		int lowerMetric = Integer.MAX_VALUE;// TODO: push 16 in resources ; use
											// a robust initialization and test.

		Vector<RouteEntry> routes = getRoutes();
		for (int i = 0; i < routes.size(); i++) {
			RouteEntry route = (RouteEntry) routes.get(i);
			if (!route.isDefault()) {
				if (route.isInDest(dest)) {

					if (route.getMetric() != 16) { // TODO : add a boolean
						// "active" on route.
						if (checkAll) {
							if (this.router.getNetIf(route.getNetIf()) != null) {
								if (route.getMetric() < lowerMetric) {
									found = true;
									// This route is candidate
									netif = route.getNetIf();
									lowerMetric = route.getMetric();
									simulationLogger
											.info(MessageFormat
													.format(
															messages
																	.getString("routeCanditateShorter"),
															this.router
																	.getNodeName(),
															dest,
															route.toString2(),
															netif));
								} else {
									simulationLogger
											.info(MessageFormat
													.format(
															messages
																	.getString("routePossibleButLonger"),
															this.router
																	.getNodeName(),
															dest,
															route.toString2(),
															netif));
								}
							} else {// out ifce not present for this routeur !
									// (down, link broken...)
								simulationLogger
										.info(MessageFormat
												.format(
														messages
																.getString("ifcMissingForRoute"),
														this.router
																.getNodeName(),
														dest,
														route.toString2(),
														netif));
							}
						} else { // not checkAll, original Manu
							found = true;
							netif = route.getNetIf();
							nexth = route.getNextHop();
							// If it the last hop
							if (netif.getStrAddress().compareTo(
									nexth.getStrAddress()) == 0)
								ip.setNextHop(ip.getDestination());
							else
								ip.setNextHop(nexth);
							simulationLogger.info(MessageFormat.format(messages
									.getString("selectRoute"), this.router
									.getNodeName(), dest, route.toString2(),
									netif));
							break;
						}
					} else {// metric==16, ifc down
						// **************
						// TODO
						// * Should send an ICMP message to the sender
						// **************
						if (checkAll) {
							simulationLogger.info(MessageFormat.format(messages
									.getString("routeDown"), this.router
									.getNodeName(), dest, route.toString2()));
							// we continue to loop thru route entries...
						} else { // not checkAll, original Manu
							simulationLogger.info(MessageFormat.format(messages
									.getString("routeDown"), this.router
									.getNodeName(), dest, route.toString2()));
							// System.out.println("No valid route for " +
							// ip.getDestination());
							readed = false;
							notifyAll();
							return null;
						}// endif checkAll
					} // endif metric != 16
				} else {// current packet NOT for net of current entry
					simulationLogger.info(MessageFormat.format(messages
							.getString("routeDown"), this.router.getNodeName(),
							dest, route.toString2()));
				}// if isInDest
			}// if ! isDefault
		}// endfor entries
		if (!found) {
			for (int i = 0; i < routes.size(); i++) {
				RouteEntry route = (RouteEntry) routes.get(i);
				if (route.isDefault()) {
					netif = route.getNetIf();
					ip.setNextHop(route.getNextHop());
					simulationLogger.info(MessageFormat.format(messages
							.getString("useDefault"),
							this.router.getNodeName(), dest, route.toString2(),
							netif));
					break;
				}
			}
			if (netif == null) {
				// **************
				// TODO
				// * Should send an ICMP message to the sender
				// **************
				// System.out.println("No route found for " +
				// ip.getDestination());
				simulationLogger.info(MessageFormat.format(messages
						.getString("noFoundRoute"), this.router.getNodeName(),
						dest));
				readed = false;
				notifyAll();
				return null;
			}
		}
		readed = false;
		notifyAll();
		NetworkInterface result = router.getNetIf(netif);
		if (result == null) {
			simulationLogger.info(MessageFormat.format(messages
					.getString("RoutingTable.nextHop.missingOutIfce"),
					this.router.getNodeName(), netif.getStrAddress()));
		}
		return result;

	}

	/**
	 * Check routing table in the context of its owning router
	 * 
	 * @param lg
	 *            a logger to happen with warning messages
	 * @return true if everything seems ok, false otherwise.
	 * 
	 *         Note: This is a local check, and not a validation of the
	 *         coherence of the whole network configuration.
	 * 
	 *         TODO: need synchro ?
	 */
	public boolean check(Logger lg) {
		boolean result = true;
		Vector<RouteEntry> routes = getRoutes();
		String routerName = this.router.getNodeName();
		lg.info(MessageFormat.format(messages
				.getString("routeTableValidationStart"), routerName));
		for (int i = 0; i < routes.size(); i++) {
			RouteEntry route = (RouteEntry) routes.get(i);
			lg.info(MessageFormat.format(messages.getString("routeValidation"),
					new Integer(i + 1), route.toString2()));
			if (router.getNetIf(route.getNetIf()) == null) {
				// route's outcoming ifce does not exist on this router
				result = false;
				lg.info(MessageFormat.format(messages
						.getString("unexistingIfc"), route.getNetIf()));
			}
		}
		return result;
	}
}
