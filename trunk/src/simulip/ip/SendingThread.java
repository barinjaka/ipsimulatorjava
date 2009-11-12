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

import simulip.net.NetworkAddress;

/**
 * A SendingThread put a IP datagram on the Network Interface in order to send it. The NetworkThread get all IP datagram putted. 
 * @author  Emmanuel Nataf
 */
public class SendingThread extends Thread {
    /**
	 * The network interface onto send
	 */
    private NetworkInterface net_if;
    /**
	 * The IP datagram to send
	 * @uml.property  name="ip_dtg"
	 * @uml.associationEnd  
	 */
    private IpDatagram ip_dtg;
    /**
     * Create a new SendingThread and does not start it but fill source address and source port
     * @param ni the Network Interface to use
     * @param ip the IP datagram to send
     */
    public SendingThread(NetworkInterface ni, IpDatagram ip){
    	net_if = ni;
    	ip_dtg = ip;
    	//ip.setNextHop(ni.getAddress());
    	if (ip.getSource() == null)
    		ip.setSource(ni.getAddress());
    	if (ip.getType() == IpDatagram.UDP){
    		simulip.net.DatagramPacket p = (simulip.net.DatagramPacket)ip.getData();
    		try{
    			if(p.getAddress() == null)
    				p.setSourceAddress(new NetworkAddress(ni.getAddress().getStrAddress()));
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}
    	}   		
    }
    /**
     * Try to send the IP datagram or kill it
     */
    public void run(){
	if(!net_if.put(ip_dtg))
	    net_if.kill(ip_dtg);
    }
}
