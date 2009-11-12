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

import simulip.gui.model.LinkGraph;
import simulip.net.NetworkAddress;


/**
 * A NetworkInterface object is a network interface responsible of sending and receiving datagram packet from and to a IpRouter. When a datagram should be send, the network interface try to put it in its output buffer. If the output buffer is full the SendingThread ask the NetworkInterface to kill the datagram. 
 * @author  Emmanuel Nataf
 */
public class NetworkInterface {

	/**
	 * The network address of this network interface
	 * @uml.property  name="if_addr"
	 * @uml.associationEnd  
	 */
    private NetworkAddress if_addr;
    /**
	 * The network mask of this network interface
	 * @uml.property  name="net_mask"
	 * @uml.associationEnd  
	 */
    private NetworkMask net_mask;
    /**
	 * The output buffer of this network interface. It contains IpDatagram.
	 * @uml.property  name="ip_dtgs"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    private IpDatagram[] ip_dtgs;
    /**
     * Indexes to manage the output buffer as a circular one
     */
    private int get_from = 0;
	/**
     * Indexes to manage the output buffer as a circular one
     */
	private int put_to = 0;
	/**
     * Indexes to manage the output buffer as a circular one
     */
	private int occuped = 0;
	/**
     * Indexes to manage the output buffer as a circular one
     */
	private int buf_size;
    /**
     * Booleans to manage the Producer/Consumer model between this NetworkInterface and each
     * @see SendingThread and NetworkThread attempting to put or get datagrams to and from the buffer 
     */
    private boolean is_empty = true;
	/**
     * Booleans to manage the Producer/Consumer model between this NetworkInterface and each
     * @see SendingThread and NetworkThread attempting to put or get datagrams to and from the buffer 
     */
	private boolean is_full = false;
	/**
     * Booleans to manage the Producer/Consumer model between this NetworkInterface and each
     * @see SendingThread and NetworkThread attempting to put or get datagrams to and from the buffer 
     */
	private boolean putting = true;
	/**
     * Booleans to manage the Producer/Consumer model between this NetworkInterface and each
     * @see SendingThread and NetworkThread attempting to put or get datagrams to and from the buffer 
     */
	private boolean getting = false;
    /**
	 * The IpRouter to which this NetworkAddress is connected
	 * @uml.property  name="ip_router"
	 * @uml.associationEnd  
	 */
    private IpRouter ip_router;
    /**
     * Create a network interface with its mask, buffer size and a router
     * @param n the NetworkAddress
     * @param m the NetworkMask
     * @param s the size of the output buffer
     * @param r the IpRouter
     */
    public NetworkInterface(NetworkAddress n, NetworkMask m, int s, IpRouter r){
    	if_addr = n;
    	net_mask = m;
    	ip_dtgs = new IpDatagram[s];
    	buf_size = s;
    	ip_router = r;
    }
    public int getBufferSize(){
    	return buf_size;
    }
    /**
     * Get the network address
     * @return the NetworkAdress
     */
    public NetworkAddress getAddress(){
    	return if_addr;
    }
    /**
     * Get the network mask of this network interface
     * @return the NetworkMask
     */
    public NetworkMask getMask(){
    	return net_mask;
    }
    /**
     * Get the network address of this network interface
     * @return the NetworkAddress
     * */
    public NetworkAddress getNetwork(){
    	return getAddress().getNetwork(getMask());
    }
    /**
     * Get the broadcast address of this network interface
     * @return the NetworkAddress
     */
    public NetworkAddress getBroadcast(){
    	return getAddress().broadcast(getMask());
    }
    /**
     * Kill the IpDatagram
     * @param the IpDatagram
     */
    public void kill(IpDatagram ip){
    	ip_router.kill(ip);
    }
    /**
     * Get the precentage of output buffer occupation
     * @return the occupation between 0 and 100
     */
    public Integer getOccupation(){
    	float fo = occuped;
    	float fs = buf_size;
    	float ro = (fo / fs) * 100;
    	int o = (int)ro;
    	return new Integer(o);
    }
    /**
     * Put a datagram in the output buffer
     * @param ip_dtg the datagram to send
     * @return true if there was enougth places in the output buffer
     */
    synchronized public  boolean put(IpDatagram ip_dtg){
    	try{
    		while(getting) wait();
    	}
    	catch(Exception e){}
    	putting = true;
    	if(!is_full){
    		ip_dtgs[put_to] = ip_dtg;
    		put_to = (put_to + 1) % buf_size;
    		is_full = put_to == get_from;
    		occuped++;
    		putting = false;
    		is_empty = false;
    		notify();
    		return true;
    	}
    	putting = false;
    	notify();
    	return false;	
    }
    /**
     * Get a IpDatagram from the output buffer
     * @return the IpDatagram
     */
    synchronized public  IpDatagram  get(){
    	try{
    		while(putting) wait();
    		while(is_empty) wait();
    	}
    	catch(Exception e){}
    	getting = true;
    	IpDatagram ipget = ip_dtgs[get_from];
    	while(ipget == null){
    		get_from = (get_from + 1) % buf_size;
    		ipget = ip_dtgs[get_from];
    	}
    	get_from = (get_from + 1) % buf_size;
    	is_empty = get_from == put_to;
    	occuped--;
    	getting = false;
    	is_full = false;
    	notify();
    	return ipget;
    }
    private LinkGraph link;
    
    public void setLinkGraph(LinkGraph lg){
    	link = lg;
    }
    public LinkGraph getLinkGraph(){
    	return link;
    }
}
