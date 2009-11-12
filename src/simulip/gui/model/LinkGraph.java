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

import simulip.gui.views.simulation.NetworkThread;
import simulip.ip.*;
import simulip.net.NetworkAddress;
import simulip.net.NetworkAddressFormatException;

import java.lang.Math;
import java.util.*;

/**
 * The class for link between two nodes
 * 
 * @author Emmanuel Nataf
 */
public class LinkGraph {
	/**
	 * An array of two nodes for each link ends
	 * 
	 * @uml.property name="end_points"
	 * @uml.associationEnd multiplicity="(0 -1)"
	 */
	protected NodeGraph[] end_points;

	private NetworkMask mask;
	/**
	 * Maximal and minimal graphical coordinates for end points nodes
	 */
	private int minX;
	/**
	 * Maximal and minimal graphical coordinates for end points nodes
	 */
	private int minY;
	/**
	 * Maximal and minimal graphical coordinates for end points nodes
	 */
	private int maxX;
	/**
	 * Maximal and minimal graphical coordinates for end points nodes
	 */
	private int maxY;
	/**
	 * horizontal and vertical distances between end points nodes
	 */
	private double dx;
	/**
	 * horizontal and vertical distances between end points nodes
	 */
	private double dy;
	/**
	 * The state of this link
	 * 
	 * @uml.property name="shutdown"
	 */
	private boolean shutdown = false;

	public void simul() {
		if (isAddressed()) {
			new NetworkThread(ni0, end_points[0]);
			new NetworkThread(ni1, end_points[1]);
		}
	}

	/**
	 * Set the two nodes of this link
	 * 
	 * @param nodes
	 *            an array of two nodes
	 */
	public void addNodes(NodeGraph[] nodes) {
		end_points = nodes;
		minX = nodes[0].getX() <= nodes[1].getX() ? nodes[0].getX() : nodes[1]
				.getX();
		maxX = nodes[0].getX() > nodes[1].getX() ? nodes[0].getX() : nodes[1]
				.getX();
		minY = nodes[0].getY() <= nodes[1].getY() ? nodes[0].getY() : nodes[1]
				.getY();
		maxY = nodes[0].getY() > nodes[1].getY() ? nodes[0].getY() : nodes[1]
				.getY();

		dx = nodes[0].getX() - nodes[1].getX();
		dy = nodes[0].getY() - nodes[1].getY();

	}

	public void removed() {
		end_points[0].removeLinkGraph(this);
		end_points[1].removeLinkGraph(this);
		if (isAddressed()) {
			String e0 = getEnd0();
			String e1 = getEnd1();

			try {
				end_points[0].getRouter().removeNetIf(new NetworkAddress(e0));
				end_points[1].getRouter().removeNetIf(new NetworkAddress(e1));
			} catch (NetworkAddressFormatException e) {
			}

		}
	}

	/**
	 * Get the end points nodes
	 * 
	 * @return an array of two end points nodes
	 */
	public NodeGraph[] getNodes() {
		return end_points;
	}

	/**
	 * Get the other end point
	 * 
	 * @param n
	 *            the looked end point
	 * @return the other link end point
	 */
	public NodeGraph getEndPoint(NodeGraph n) {
		if (n == end_points[0])
			return end_points[1];
		return end_points[0];
	}

	/**
	 * Compute if given graphical coordinates are near the link
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 * @return true if coordinates are near the link
	 */
	public boolean isOnLink(int x, int y) {
		// if nodes have changed of positions
		addNodes(end_points);
		double r = Math.atan((double) dy / (double) dx)
				- Math.atan((double) (end_points[0].getY() - y)
						/ (end_points[0].getX() - x));
		if (r >= -0.05 && r <= 0.05) {
			if (x >= minX - 5 && x <= maxX + 5 && y >= minY - 5
					&& y <= maxY + 5) {
				if (!(end_points[0].isIn(x, y) || end_points[1].isIn(x, y))) {
					// shutdown();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Shutdown the interface
	 * 
	 */
	public void shutdown() {
		shutdown = true;
	}

	/**
	 * Ask if the link is down
	 * 
	 * @return true if the link is down
	 * @uml.property name="shutdown"
	 */

	public boolean isShutdown() {
		return shutdown;
	}

	/**
	 * Set the link to up
	 * 
	 */
	public void up() {
		shutdown = false;
	}

	public boolean equals(LinkGraph lg) {
		NodeGraph[] ngs = lg.getNodes();
		return (end_points[0].equals(ngs[0]) || end_points[0].equals(ngs[1]))
				&& (end_points[1].equals(ngs[0]) || end_points[1]
						.equals(ngs[1]));
	}

	/**
	 * @uml.property name="ni0"
	 * @uml.associationEnd
	 */
	protected NetworkInterface ni0 = null;
	/**
	 * @uml.property name="ni1"
	 * @uml.associationEnd
	 */
	protected NetworkInterface ni1 = null;
	/**
	 * @uml.property name="mask0"
	 * @uml.associationEnd
	 */
	protected NetworkMask mask0 = null;
	/**
	 * @uml.property name="mask1"
	 * @uml.associationEnd
	 */
	protected NetworkMask mask1 = null;
	/**
	 * @uml.property name="na0"
	 * @uml.associationEnd
	 */
	protected NetworkAddress na0 = null;
	/**
	 * @uml.property name="na1"
	 * @uml.associationEnd
	 */
	protected NetworkAddress na1 = null;

	private String getEnd(int e) {
		boolean found = false;

		Enumeration<NetworkInterface> eni = end_points[e].getRouter()
				.getNetIfs();
		NetworkInterface ni = null;
		while (eni.hasMoreElements() && !found) {
			ni = eni.nextElement();
			if (ni.getLinkGraph() == this)
				found = true;
		}
		if (found)
			return ni.getAddress().getStrAddress();
		else
			return "";
	}

	private boolean isAddressed() {
		ni0 = null;
		ni1 = null;
		mask0 = null;
		mask1 = null;
		na0 = null;
		na1 = null;
		Boolean found = false;

		Enumeration<NetworkInterface> eni0 = end_points[0].getRouter()
				.getNetIfs();
		while (eni0.hasMoreElements() && !found) {
			ni0 = eni0.nextElement();
			mask0 = ni0.getMask();
			na0 = ni0.getAddress();
			Enumeration<NetworkInterface> eni1 = end_points[1].getRouter()
					.getNetIfs();
			while (eni1.hasMoreElements() && !found) {
				ni1 = eni1.nextElement();
				mask1 = ni1.getMask();
				na1 = ni1.getAddress();
				if (mask0.equals(mask1)) {
					if (na0.sameNetwork(na1, mask0)
							&& ni0.getLinkGraph() == ni1.getLinkGraph())
						found = true;
				}
			}
		}
		if (found)
			return true;
		else
			return false;

	}

	public void setNetworkMask(NetworkMask m) {
		mask = m;
	}

	public NetworkMask getNetworkMask() {
		return mask;
	}

	public String getEnd0() {
		return getEnd(0);
	}

	public String getEnd1() {
		return getEnd(1);
	}
}
