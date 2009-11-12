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

import java.util.logging.Level;
import java.util.logging.Logger;

import simulip.net.NetworkAddress;
import simulip.net.NetworkAddressFormatException;

/**
 * The RouteEntry is a entry of a Ip routing table in a @see RoutingTable. It
 * gives the network destination address and the mask, the next hop router the
 * network interface to use and a route metric.
 * 
 * @author Emmanuel Nataf
 */
public class RouteEntry {
	/**
	 * The Network Address for the destination
	 * 
	 * @uml.property name="destination"
	 * @uml.associationEnd
	 */
	private NetworkAddress destination;
	/**
	 * The mask of the destination
	 * 
	 * @uml.property name="mask"
	 * @uml.associationEnd
	 */
	private NetworkMask mask;
	/**
	 * The Network Address for the next hop
	 * 
	 * @uml.property name="next_hop"
	 * @uml.associationEnd
	 */
	private NetworkAddress next_hop;
	/**
	 * The Network Interface to use
	 * 
	 * @uml.property name="net_if"
	 * @uml.associationEnd
	 */
	private NetworkAddress net_if;
	/**
	 * The route metric
	 * 
	 * @uml.property name="metric"
	 */
	private int metric;

	/**
	 * Create a Route entry from a string formated in the applet parameter
	 * 
	 * @param r
	 *            one route : destination mask next_hop interface metric
	 */
	public RouteEntry(String r) {
		r = r.replaceAll("\\s+", " ");
		int isp = r.indexOf(' ');
		String dest = r.substring(0, isp).trim();
		String msk = r.substring(isp + 1, r.indexOf(' ', isp + 1)).trim();
		isp = r.indexOf(' ', isp + 1);
		String nh = r.substring(isp + 1, r.indexOf(' ', isp + 1)).trim();
		isp = r.indexOf(' ', isp + 1);
		String ni = r.substring(isp + 1, r.indexOf(' ', isp + 1)).trim();
		isp = r.indexOf(' ', isp + 1);
		try {
			destination = new NetworkAddress(dest);
			mask = new NetworkMask(msk);
			next_hop = new NetworkAddress(nh);
			net_if = new NetworkAddress(ni);
			metric = Integer.parseInt(r.substring(isp + 1));
		} catch (NetworkAddressFormatException e) {
			Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(),
					"RouteEntry", "", e);
		} catch (NetworkMaskFormatException nme) {
			Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(),
					"RouteEntry", "", nme);
		}
	}

	/**
	 * Create a route entry directly with Network Addresses
	 * 
	 * @param pdest
	 *            the destination
	 * @param pmask
	 *            the mask
	 * @param pnhop
	 *            the next hop
	 * @param pif
	 *            the (out) interface
	 * @param pm
	 *            the metric
	 */
	public RouteEntry(NetworkAddress pdest, NetworkMask pmask,
			NetworkAddress pnhop, NetworkAddress pif, int pm) {
		destination = pdest;
		mask = pmask;
		next_hop = pnhop;
		net_if = pif;
		metric = pm;
	}

	/**
	 * Ask if this route is a default route
	 * 
	 * @return true if destination is "0.0.0.0"
	 */
	public boolean isDefault() {
		return destination.isDefault();
	}

	/**
	 * Compute if the Network Address is in the destination address of this
	 * route
	 * 
	 * @param ip
	 *            the Network Address
	 * @return true if the Network Address is in this destination address
	 */
	public boolean isInDest(NetworkAddress ip) {
		return ip.isInDest(destination, mask);
	}

	/**
	 * Get the destination address
	 * 
	 * @return the Network Address for the destination of this route
	 * @uml.property name="destination"
	 */
	public NetworkAddress getDestination() {
		return destination;
	}

	/**
	 * Get the destination mask
	 * 
	 * @return the Network Address of the mask
	 * @uml.property name="mask"
	 */
	public NetworkMask getMask() {
		return mask;
	}

	/**
	 * Get the next hop address
	 * 
	 * @return the Network Address of the next hop
	 */
	public NetworkAddress getNextHop() {
		return next_hop;
	}

	/**
	 * Get the Network Address of the Network Interface to use
	 * 
	 * @return the Network Address
	 */
	public NetworkAddress getNetIf() {
		return net_if;
	}

	/**
	 * Set the metric of this route
	 * 
	 * @param m
	 *            the new metric
	 * @uml.property name="metric"
	 */
	public void setMetric(int m) {
		metric = m;
	}

	/**
	 * Get the metric of this route
	 * 
	 * @return the metric of this route (max is 16 with RIP)
	 * @uml.property name="metric"
	 */
	public int getMetric() {
		return metric;
	}

	/**
	 * A string of the route, formated with tabs
	 */
	public String toString() {
		return destination.getStrAddress() + " \t| " + mask.toString()
				+ " \t|     " + next_hop.getStrAddress() + " \t|    "
				+ net_if.getStrAddress() + " \t|     " + metric;
	}

	/**
	 * A string of the route with strict size and formated with spaces.
	 * 
	 */
	public String toString2() {
		// TODO: externalize string format in resources file ?
		return String.format("%-19s|%-19s|%-19s|%-19s|%3d", destination
				.getStrAddress(), mask.toString(), next_hop.getStrAddress(),
				net_if.getStrAddress(), metric);
	}
}
