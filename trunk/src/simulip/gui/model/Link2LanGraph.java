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

import simulip.gui.views.simulation.NetworkThread;
import simulip.ip.NetworkInterface;

/**
 * @author nataf
 */
public class Link2LanGraph extends LinkGraph {
	
	public void simul() {
		if (isAddressed()) {
			new NetworkThread(ni0, end_points[0]);
			new NetworkThread(ni1, end_points[1]);
		}
	}
	
	public LanNodeGraph getLanNodeGraph(){
		return (LanNodeGraph)end_points[1];
	}
	
	public String getLanName(){
		return getLanNodeGraph().getNodeName();
	}
	
	public NodeGraph getNode(){
		return end_points[0];
	}
	
	public String getNodeAddress(){
		return getEnd0();
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
					if (na0.getStrAddress().compareTo(na1.getStrAddress()) == 0)
						found = true;
				}
			}
		}
		if (found)
			return true;
		else
			return false;

	}

}
