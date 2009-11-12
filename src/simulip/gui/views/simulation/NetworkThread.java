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
    	
package simulip.gui.views.simulation;

import simulip.gui.model.NodeGraph;
import simulip.ip.IpDatagram;
import simulip.ip.NetworkInterface;

/**
 * A NetworkThread continuous get datagram from its source network interface  and call back it for each.
 * @author  Emmanuel Nataf
 */
public class NetworkThread extends Thread{

	/**
	 * The source NetworkInterface
	 * @uml.property  name="from_net_if"
	 * @uml.associationEnd  
	 */
    private NetworkInterface from_net_if;
    /**
	 * The graphical node of this interface
	 * @uml.property  name="node"
	 * @uml.associationEnd  
	 */
    private NodeGraph node;
    /**
     * Is the link stopped (or shutdown)
     */
    private boolean stopped = false;
    /**
     * Create a NetworkLink from a NetworkInterface
     * @param from the NetworkInterface
     */
    public NetworkThread(NetworkInterface from, NodeGraph n){
    	from_net_if = from;
    	node = n;
    	this.start();
    }
    /**
     * The run method try to get a datagram from the NetworkInterface. 
     * It signals to the NetworkInterface that the datagram was send
     */
    public void run(){
    	while(true && ! stopped){
    		IpDatagram ip = from_net_if.get();
    		node.sended(from_net_if,ip);
    		try{
    			Thread.sleep(50);
    		} catch (Exception e){}
    	}
    }
    /**
     * To stop the process of getting datagrams from the NetworkInterface
     * @param s true if this NetworkLink must stop getting datagrams
     */
    public void setStopTime(boolean s){
    	stopped = s;
    	if(!stopped)
    		run();
    }
}

