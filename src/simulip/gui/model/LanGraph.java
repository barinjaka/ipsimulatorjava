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

import java.util.Vector;

import simulip.ip.*;
import simulip.net.NetworkAddress;

/**
 * @author  nataf
 */
public class LanGraph {
	private int x;
	private int y;
	private int w;
	private int h;
	private NetworkMask mask;
	private NetworkAddress address;
	private Vector<Link2LanGraph> links = new Vector<Link2LanGraph>();
	private LanNodeGraph fakenode;

	public LanGraph(int x, int y, int w, int h, String n) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		fakenode = new LanNodeGraph(n, x + w/2, y + h/2, this);
		IpRouter ip = new IpRouter();
		fakenode.setRouter(ip);
		ip.setPhysicalNode(fakenode);
	}

	/**
	 * @return
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x
	 */
	public void setX(int x) {
		this.x = x;
		fakenode.setX(x + w / 2);
	}

	/**
	 * @param y
	 */
	public void setY(int y) {
		this.y = y;
		fakenode.setY(y + h / 2);
	}
	
	public void setWidth(int w){
		this.w = w;
	}
	
	public void setHeight(int h){
		this.h = h;
	}

	/**
	 * @return
	 */
	public int getY() {
		return y;
	}

	public int getWidth() {
		return w;
	}

	public int getHigh() {
		return h;
	}

	public String getName() {
		return getFakeNode().getNodeName();
	}

	public void setMask(NetworkMask m) {
		mask = m;
	}

	public NetworkMask getMask() {
		return mask;
	}

	public void addLink2LanGraph(Link2LanGraph l) {
		links.add(l);
	}
	
	public void removeLink(Link2LanGraph l){
		links.remove(l);
	}

	public Vector<Link2LanGraph> getLinks() {
		return links;
	}

	public NetworkAddress getAddress() {
		return address;
	}


	public LanNodeGraph getFakeNode() {
		return fakenode;
	}
	
	public void setId(int id){
		getFakeNode().setId(id);
	}
	
	public int getId(){
		return getFakeNode().getId();
	}

	public void setName(String ln) {
		getFakeNode().setName(ln);
		
	}
	
}
