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
/**
 * The RoutingThread compute datagram next hop and send it
 * @author  Emmanuel Nataf
 */
public class RoutingThread extends Thread {
	/**
	 * The @see RoutingTable used
	 * @uml.property  name="routing_table"
	 * @uml.associationEnd  
	 */
    private RoutingTable routing_table;
    /**
	 * The @see IpDatagram to send
	*/
    private IpDatagram ip_dtg;
    /**
     * Create a new RoutingThread and do not start it
     * @param rt  the routing table
     * @param ip the IP datagram
     */
    public RoutingThread(RoutingTable rt, IpDatagram ip){
    	routing_table = rt;
    	ip_dtg = ip;
    }
    /**
     * Try to send the IpDatagram to the NetworkInterface toward the next hop
     */
    public void run(){
    	NetworkInterface ni = routing_table.nextHop(ip_dtg);
    	if(ni != null)
    		(new SendingThread(ni, ip_dtg)).start();
    }
}
