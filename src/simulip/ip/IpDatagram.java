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
 * The IpDatagram class is a IP datagram with Ipv4 header
 * @author  Emmanuel Nataf
 */
public class IpDatagram {
	/**
	 * Static value for the Type of service field
	 */
    private static int TOS = 0;
    /**
     * Static value for the Time to live field
     */
    private static int TTL = 30;
    /**
     * Static value for more fragment bit
     */
    private static boolean MF = false;
    /**
     * Static value for do not fragment bit
     */
    private static boolean DF = false;
    /**
     * Static value for the offset of a fragment
     */
    private static long OFST = 0;
    /**
     * Static value for the ICMP protocol type
     */
    public static int ICMP = 11;
    /**
     * Static value for the TCP protocol type
     */
    public static int TCP = 12;
    /**
     * Static value for the UDP protocol type
     */
    public static int UDP = 13;
    /**
	 * The IP address of the source.
	 * @uml.property  name="source"
	 * @uml.associationEnd  
	 */
    private NetworkAddress source;
    /**
	 * The IP address of the destination.
	 * @uml.property  name="destination"
	 * @uml.associationEnd  
	 */
    private NetworkAddress destination;
    /**
	 * The type of contained data (see constants).
	 * @uml.property  name="type"
	 */
    private int type;
    /**
	 * The Type Of Service field.
	 * @uml.property  name="tos"
	 */
    private int tos;
    /**
	 * The Time To Live for the datagram.
	 * @uml.property  name="ttl"
	 */
    private int ttl;
    /**
	 * The offset of this datagram fragment.
	 * @uml.property  name="offset"
	 */
    private long offset;
    /**
	 * The identifier of the IP datagram from which this datagram fragment is part of.
	 * @uml.property  name="id"
	 */
    private long id;
    /**
     * The more fragment bit boolean value
     **/
    private boolean mf;  
    /**
     * The don't fragment bit boolean value
     **/
    private boolean df;
    /**
	 * The contained data
	 * @uml.property  name="data"
	 * @uml.associationEnd  
	 */
    private IpData data;
    /**
     * The next hop destination, instead of a physical address
     */
    private NetworkAddress nexthop;
    /**
     * Create a basic IP datagram whithout source nor destination address. Default values are setted.
     *
     */
    public IpDatagram(){
    	tos = TOS;
    	id = getId();
    	ttl = TTL;
    	mf = MF;
    	df = DF;
    	offset = OFST;
    }
    /**
     * Create a IP datagram with fragmentation parameters.
     * @param offset the number of the first octet in the data in the original datagram
     * @param mf the more fragment bit
     * @param df the do not fragment bit
     */
    public IpDatagram(int offset, boolean mf, boolean df){
    	this();
    	this.offset = offset;
    	this.mf = mf;
    	this.df = df;
    }
    /**
     * Create a IP datagram with source and destination addresses.
     * @param s the IP source address
     * @param d the IP destination address
     */
    public IpDatagram (NetworkAddress s, NetworkAddress d){
    	source = s;
    	destination = d;
    }
    /**
     * Create a IP datagram with the same source/destination IP addresses and the same type and data.
     * @param ip the IP datagram used to create this datagram
     */
    public IpDatagram(IpDatagram ip){
    	source = ip.getSource();
    	destination = ip.getDestination();
    	type = ip.getType();
    	data = ip.getData();
    	ttl = ip.getTtl();
    }
    public IpDatagram(IpData data){
    	this.data = data;
    }
    /**
	 * Get the Time To Live value of this datagram. Each IP router decrease of 1 this  value before process a datagram, and do not process if getTtl() == 0;
	 * @return  the current value of the TTL field
	 * @uml.property  name="ttl"
	 */
    public int getTtl(){
    	return ttl;
    }
    /**
	 * Set the Time To Live valueof this datagram. Each IP router decrease of 1 this  value before process a datagram, and do not process if getTtl() == 0;
	 * @param ttl  the new value for the TTL field
	 * @uml.property  name="ttl"
	 */
    public void setTtl(int ttl){
    	this.ttl = ttl;
    }
    /**
	 * Get the value of the type of service field
	 * @return  the TOS
	 * @uml.property  name="tos"
	 */
    public int getTos(){
    	return tos;
    }
    /**
	 * Set the type of service field
	 * @param tos  the TOS
	 * @uml.property  name="tos"
	 */
    public void setTos(int tos){
    	this.tos = tos;
    }
    /**
	 * Get the fragment identifier
	 * @return  the identifier
	 * @uml.property  name="id"
	 */
    public long getId(){
    	return id;
    }
    /**
	 * Set the fragment identifier
	 * @param id  the identifier
	 * @uml.property  name="id"
	 */
    public void setId(long id){
    	this.id = id;
    }
    /**
     * Get the more fragment bit value
     * @return true if there is more fragments 
     */
    public boolean getMoreFragment(){
    	return mf;
    }
    /**
     * Set the more fragment bit value
     * @param mf true if there are more fragments
     */
    public void setMoreFragment(boolean mf){
    	this.mf = mf;
    }
    /**
     * Get the do not fragment bit
     * @return true if the datagram must not be fragmented
     */
    public boolean getDontFragment(){
    	return df;
    }
    /**
     * Set the do not fragment value
     * @param df true if the datagram must no be fragmented
     */
    public void setDontFragment(boolean df){
    	this.df = df;
    }
    /**
	 * Get the offset of this datagram fragment
	 * @return  the offset from the original fragment
	 * @uml.property  name="offset"
	 */
    public long getOffset(){
    	return offset;
    }
    /**
	 * Set the offset of this datagram fragment
	 * @param offset  the offset from the original datagram
	 * @uml.property  name="offset"
	 */
    public void setOffset(long offset){
    	this.offset = offset;
    }
    /**
	 * Get the destination address
	 * @return  the destination address
	 * @uml.property  name="destination"
	 */
    public NetworkAddress getDestination(){
    	return destination;
    }
    public NetworkAddress getNextHop(){
    	return nexthop;
    }
    /**
	 * Get the source address
	 * @return  the source address
	 * @uml.property  name="source"
	 */
    public NetworkAddress getSource(){
    	return source;
    }
    /**
	 * Get the type of the data in this datagram
	 * @return  the type (see Ipdatagram static values
	 * @uml.property  name="type"
	 */
    public int getType(){
    	return type;
    }
    /**
	 * Get the data in the datagram
	 * @return  the datat (@see Data)
	 * @uml.property  name="data"
	 */
    public IpData getData(){
    	return data;
    }
    /**
	 * Set the source address
	 * @param s  the source address
	 * @uml.property  name="source"
	 */
    public void setSource(NetworkAddress s){
    	source = s;
    }
    /**
	 * Set the destination address
	 * @param d  the destination
	 * @uml.property  name="destination"
	 */
    public void setDestination(NetworkAddress d){
    	destination = d;
    }
    /**
     * Set the next hop network address for this datagram
     * @param n the NetworkAddress
     */
    public void setNextHop(NetworkAddress n){
    	nexthop = n;
    }
    /**
	 * Set the data in the datagram
	 * @param d  the data (@see Data)
	 * @uml.property  name="data"
	 */
    public void setData(IpData d){
    	data = d;
    }
    /**
	 * Set the data type
	 * @param t  the data type @see IPDatagram static definitions
	 * @uml.property  name="type"
	 */
    public void setType(int t){
    	type = t;
    }
    /**
     * Swap source and destination IP addresses
     *
     */
    public void swap(){
    	NetworkAddress tmp = source;
    	source = destination;
    	destination = tmp;
    }
}
