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

import java.net.*;

/**
 * @author  nataf
 */
public class DatagramSocket  {
	/**
	 * The local port used to send and receive datagram Packets
	 */
	private int port = -1;
	/**
	 * The Application using this datagram socket
	 * @uml.property  name="application"
	 * @uml.associationEnd  
	 */
	private Application application;
	/**
	 * Construct a new datagram socket binded with the given application
	 * @param appli
	 */
	public DatagramSocket(Application appli){
		application = appli;
		port = application.newPort();
	}
	/**
	 * Construct a new socket binded with the given application and that will receive data from the given port number
	 * @param appli
	 * @param port
	 * @throws BindException if the port is already used by the node
	 */
	public DatagramSocket(Application appli, int port) throws BindException{
		application = appli;
		this.port = port;
		application.askPort(port);
	}
	public void close(){		
	}
	/**
	 * Get the port from which data are received
	 * @return the local port
	 */
	public int getLocalPort(){
		return port;
	}	
	/**
	 * Wait an incoming  datagram packet from the network. This instruction suspends the calling application
	 * @param d the received DatagramPacket
	 */
	public void receive(DatagramPacket d){
		IpDatagram ip = application.waitForDatagram();
		d.set((DatagramPacket) ip.getData());
	}	
	/**
	 * Send the DatagramPacket on the network
	 * @param d the DatagramPacket to send
	 */
	public void send(DatagramPacket d){
		d.setSourcePort(port);
		d.setSourceAddress(null);
		simulip.net.DatagramPacket dp = d.clone();
		IpDatagram ipd = new IpDatagram(dp);
		String addr = dp.getDestAddress().getStrAddress();
		try{
			ipd.setDestination(new NetworkAddress(addr));
			ipd.setType(IpDatagram.UDP);
			ipd.setTtl(32);
			application.send(ipd);
		}
		catch(NetworkAddressFormatException e){
			// could not happend
		}
	}
}
