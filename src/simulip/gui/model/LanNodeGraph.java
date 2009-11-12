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

import java.util.Enumeration;
import java.util.Hashtable;

import simulip.ip.*;
import simulip.net.*;

public class LanNodeGraph extends NodeGraph {

	private Hashtable<String, NetworkInterface> arp = new Hashtable<String, NetworkInterface>();
	private LanGraph lan;

	public LanNodeGraph(String n, int x, int y, LanGraph plan) {
		super(n, x, y);
		lan = plan;
	}
	
	public LanGraph getLanGraph(){
		return lan;
	}

	public void removeLinkGraph(LinkGraph lg) {
		super.removeLinkGraph(lg);
		lan.removeLink((Link2LanGraph) lg);
	}

	/**
	 * Add a link to this node with a network adress.
	 * 
	 * @param na
	 *            the network address (IP address)
	 * @param lg
	 *            the link
	 */
	public void addLink(NetworkInterface ni, LinkGraph lg) {
		super.addLink(ni, lg);
		arp.put(ni.getAddress().getStrAddress(), ni);
	}

	public void knock(IpDatagram ip) {
		NetworkAddress nh = ip.getNextHop();
		NetworkInterface ni = arp.get(nh.getStrAddress());
		if (ni != null)
			new SendingThread(ni, ip).start();
		else {
			// is it an IP broadcast on a IP network ?
			for (Enumeration<NetworkInterface> eni = arp.elements(); eni.hasMoreElements();){
				NetworkInterface nib = eni.nextElement();
				if (nib.getBroadcast().equals(nh))
					new SendingThread(nib, ip).start();
			}
		}
	}

	public LanNodeGraph deepClone(LanGraph clan) {

		LanNodeGraph clonednode = new LanNodeGraph(getNodeName(), getX(),
				getY(), clan);

		clonednode.setId(getId());
		IpRouter clonedrouter = new IpRouter();
		IpRouter router2clone = getRouter();

		Enumeration<RouteEntry> er = router2clone.getRoutes().elements();
		while (er.hasMoreElements()) {
			RouteEntry re = er.nextElement();
			NetworkAddress dest2clone = re.getDestination();
			NetworkMask mask2clone = re.getMask();
			NetworkAddress nexthop2clone = re.getNextHop();
			NetworkAddress if2clone = re.getNetIf();
			int metric2clone = re.getMetric();
			try {
				NetworkAddress cloneddest = new NetworkAddress(dest2clone
						.getStrAddress());
				NetworkMask clonedmask = new NetworkMask(mask2clone.toString());
				NetworkAddress clonednexthop = new NetworkAddress(nexthop2clone
						.getStrAddress());
				NetworkAddress clonedif = new NetworkAddress(if2clone
						.getStrAddress());
				RouteEntry clonedentry = new RouteEntry(cloneddest, clonedmask,
						clonednexthop, clonedif, metric2clone);
				clonedrouter.addRoute(clonedentry);
			} catch (NetworkAddressFormatException nafe) {
				java.lang.System.out
						.println("Panic : illegal network address format");
			} catch (NetworkMaskFormatException nme) {
				java.lang.System.out
						.println("Panic : illegal network mask format");
			}
		}
		Enumeration<NetworkInterface> eni = router2clone.getNetIfs();
		while (eni.hasMoreElements()) {
			NetworkInterface ni = eni.nextElement();
			NetworkAddress na = ni.getAddress();
			NetworkMask nm = ni.getMask();
			NetworkAddress clonedna = null;
			try {
				clonedna = new NetworkAddress(na.getStrAddress());
			} catch (NetworkAddressFormatException nafe) {
				java.lang.System.out
						.println("Panic : illegal network address format");
			}
			NetworkMask clonednm = null;
			try {
				clonednm = new NetworkMask(nm.toString());
			} catch (NetworkMaskFormatException nme) {
				java.lang.System.out
						.println("Panic : illegal network mask format");
			}
			int bufsize = ni.getBufferSize();
			NetworkInterface clonedni = new NetworkInterface(clonedna,
					clonednm, bufsize, clonedrouter);
			clonedrouter.addNetIf(clonedni);
			clonednode.arp.put(clonedna.getStrAddress(), clonedni);
		}
		clonednode.setRouter(clonedrouter);
		clonedrouter.setPhysicalNode(clonednode);

		Enumeration<String> esa = getApplicationClasses().keys();
		while (esa.hasMoreElements()) {
			String sa = esa.nextElement();
			try {
				Class<?> ca = Class.forName(sa);
				clonednode.putApplicationClass(sa, ca);
			} catch (ClassNotFoundException cnf) {
				java.lang.System.out.println("PANIC : " + sa
						+ " is no more found");
			} 
		}
		return clonednode;
	}

}
