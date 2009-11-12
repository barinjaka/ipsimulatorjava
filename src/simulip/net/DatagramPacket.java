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

/**
 * @author  nataf
 */
public class DatagramPacket extends IpData {
	/**
	 * The local port number that will be chosen by IP
	 */
	private int source_port = -1;
	/**
	 *  The remote port number
	 */
	private int dest_port;
	/**
	 * The bytes carried by this UDP datagram
	 * @uml.property  name="data"
	 */
	private byte[] data;
	/**
	 * The destination IP address
	 * @uml.property  name="dest_addr"
	 * @uml.associationEnd  
	 */
	private NetworkAddress dest_addr;
	/**
	 * The source IP address chosen by IP
	 * @uml.property  name="source_addr"
	 * @uml.associationEnd  
	 */
	private NetworkAddress source_addr;
	/**
	 * Create a new DatagramPacket, generally in order to receive a DatagramPacket from the DatagramSocket
	 * @param data the data in the packet
	 * @param length the number of bytes of the data
	 */
	public DatagramPacket(byte[] data, int length){
		this.data = data;
	}
	/**
	 * Create a new DatagramPacket, generally to send the datagramPacket to a DatagramSocket
	 * @param data the data in the packet
	 * @param length the number of bytes of the data
	 * @param dest the destination IP address for this DatagramPacket
	 * @param port the destination port number for this DatagramPacket
	 * @throws NetworkAddressFormatException if the given address is not well formed (i.e. decimal dotted notation)
	 */
	public DatagramPacket(byte[] data , int length, String dest, int port) throws NetworkAddressFormatException{
		this(data, length);
		dest_addr = new NetworkAddress(dest);
		dest_port = port;
	}
	/**
	 * Clone this datagram packet into a new one
	 * @return a new DatagramPacket with the same data, addresses and ports as this DatagramPacket.
	 */
	public DatagramPacket clone(){
		byte[] cldata = new byte[data.length];
		for(int i = 0; i < data.length;i++)
			cldata[i] = data[i];
		DatagramPacket p = new DatagramPacket(cldata, data.length);
		p.setSourcePort(this.getPort());
		p.setPort(this.getDestPort());
		try{
			if(this.getDestAddress() != null){
				NetworkAddress dadd = new NetworkAddress(getDestAddress().getStrAddress());
				p.setAddress(dadd);
			}
			if(this.getAddress() != null){
				NetworkAddress sadd = new NetworkAddress(getAddress().getStrAddress());
				p.setSourceAddress(sadd);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return p;
	}
	/**
	 * Get the source address of this Datagram
	 * @return the source address
	 */
	public NetworkAddress getAddress(){
		return source_addr;
	}	
	/**
	 * Get the data of this DatagramPacket
	 * @return  the contained data
	 * @uml.property  name="data"
	 */
	public byte[] getData(){;
		return data;
	}
	/**
	 * Get the IP destination address of this datagram packet
	 * @return a @see simulip.net.NetworkAddress for this packet
	 */
	public NetworkAddress getDestAddress(){
		return dest_addr;
	}
	/**
	 * Get the remote port number of this datagram packet
	 * @return the port number
	 */
	public  int getDestPort(){
		return dest_port;
	}
	/**
	 * Get the source port of this Datagram
	 * @return the source port.
	 */
	public int getPort(){
		return source_port;
	}
	/**
	 * Allows to set this datagram packet by an existing one
	 * @param p a datagram packet
	 */
	public void set(DatagramPacket p){
		this.setData(p.getData());
		this.setSourcePort(p.getPort());
		this.setPort(p.getDestPort());
		this.setAddress(p.getDestAddress());
		this.setSourceAddress(p.getAddress());
	}
	/**
	 * Set the destination address of this Datagram
	 * @param dest the destination
	 */
	public void setAddress(NetworkAddress dest){
		dest_addr = dest;
	}
	/**
	 * Set the data in this DatagramPacket
	 * @param  data
	 * @uml.property  name="data"
	 */
	public void setData(byte[] data){
		this.data = data;
	}
	/**
	 * Set the destination port of this Datagram
	 * @param port the destination port
	 */
	public void setPort(int port){
		dest_port = port;
	}
	/**
	 * Set the IP source address for this datagram packet
	 * @param s a @see simulip.net.NetworkAddress
	 */
	public void setSourceAddress(NetworkAddress s){
		source_addr = s;
	}
	/**
	 * Set the local source port number
	 * @param port the port number
	 */
	public void setSourcePort(int port){
		source_port = port;
	}
	
}
