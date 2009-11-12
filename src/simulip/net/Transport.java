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
package simulip.net;

import simulip.ip.*;

import java.util.*;
import java.net.*;

/**
 * @author  nataf
 */
public class Transport {
	
	/**
	 * @uml.property  name="router"
	 * @uml.associationEnd  
	 */
	private IpRouter router;
	private Hashtable<Integer, Boolean> assigned = new Hashtable<Integer, Boolean>();
	private Integer last = new Integer(-1);
	private Integer max = new Integer(65535);
	
	private Hashtable<Integer, Application> binded = new Hashtable<Integer, Application>();
	private Vector<Application> tobind = new Vector<Application>();
	
	public Transport(IpRouter router){
		this.router = router;
	}
	public void addBinded(Application ipbd){
		tobind.add(ipbd);
	}
	public void removeBinded(String a){
		for(int i = 0; i < tobind.size(); i++){
			Application ap = tobind.get(i);
			String na = ap.getClass().getCanonicalName();
			if(na.equals(a)){
				tobind.remove(i);
				break;
			}
		}
		tobind.remove(a);
	}
	/**
	 * @return
	 * @uml.property  name="router"
	 */
	public IpRouter getRouter(){
		return router;
	}
	public String getNodeName(){
		return router.getNodeName();
	}
	public simulip.gui.model.NodeGraph getNodeGraph(){
		return router.getNodeGraph();
	}
	public java.awt.Point getLocationOnScreen(){
		return router.getLocationOnScreen();
	}
	public synchronized void knock(IpDatagram datagram){
		if(datagram.getType() == IpDatagram.UDP) {
			simulip.net.DatagramPacket data = (simulip.net.DatagramPacket)datagram.getData();
			int port = data.getDestPort();
			if(assigned.get(port) != null)
				if(assigned.get(port) == true){
					binded.get(port).knock(datagram);
				}
		}
	}
	public synchronized void send(IpDatagram datagram){
		router.knock(datagram);
	}
	public int newPort(Application ipbd){
		last = (last + 1)%max;
		if(assigned.get(last) != null){
			if(!assigned.get(last)){
				binded.put(last,ipbd);
				assigned.put(last,new Boolean(true));
				return last.intValue();
			}
			else {
				int i = last.intValue();
				int j = (i + 1)%max;
				int find = -1;
				boolean found = false;
				while(i != j && !found){
					if(assigned.get(j) != null){
						found = !assigned.get(j);
						if (found){
							find = j;
							assigned.put(j,true);
							binded.put(j,ipbd);
						}
						else
							j = (j + 1)%max;
					}
						else
							j = (j + 1)%max;
				}				
				return find;
			}
		}
		else {
			binded.put(last, ipbd);
			assigned.put(last, true);
			return last.intValue();
		}
			
	}
	public boolean askPort(int i, Application ipbd) throws BindException{
		if(assigned.get(i) != null)
			if(!assigned.get(i)){
				assigned.put(i, true);
				binded.put(i, ipbd);
				return true;
			}
			else
				throw new BindException("simulip : port " + i + " already in use");
		assigned.put(i,true);
		binded.put(i,ipbd);
		return true;
	}

}
