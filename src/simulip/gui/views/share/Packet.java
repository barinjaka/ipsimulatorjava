/// <simulip : an IP and UDP simulator>
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
    	
package simulip.gui.views.share;

import simulip.gui.model.NodeGraph;
import simulip.ip.IpDatagram;

/**
 * A class for packet on the simulation graphical interface
 * @author  Emmanuel Nataf
 */
public class Packet {
	/**
	 * The scale of one move in pixel
	 */
    private int anim = 7;
    /**
	 * The actual coordinates and the direction
	 * @uml.property  name="x"
	 */
    private double x;
	/**
	 * The actual coordinates and the direction
	 * @uml.property  name="y"
	 */
	private double y;
	/**
     * The actual coordinates and the direction
     */
	private double Dx = 0;
	/**
     * The actual coordinates and the direction
     */
	private double Dy = 0;
    /**
	 * The IP datagram transported
	 * @uml.property  name="ip_dtg"
	 * @uml.associationEnd  
	 */
    private IpDatagram ip_dtg;
    /**
	 * The sender graphical node
	 * @uml.property  name="sender"
	 * @uml.associationEnd  
	 */
    private NodeGraph sender;
  
    /**
     * Create a new packet at a graphical position, with a direction,
     * an IP datagram and the node source of the packet.
     * @param px the x coordinate of the packet
     * @param py the y coordinate of the packet
     * @param dx the horizontal direction
     * @param dy the vertical direction
     * @param ip the transported datagram
     * @param sdr the sender graphical node
     */
    public Packet(int px, int py, int dx, int dy, IpDatagram ip, NodeGraph sdr){
    	x = (double)px;
    	y = (double)py;
    	ip_dtg = ip;
    	sender = sdr;	
    	if(dx != 0){
    		double alpha = Math.atan((double)dy/(double)dx);
    		if(dx > 0){
    			Dx = anim * Math.cos(alpha);
    			Dy = anim * Math.sin(alpha);
    		}
    		else{		   
    			Dx = - anim * Math.cos(alpha);
    			Dy = - anim * Math.sin(alpha); 
    		}
    	}
    	else {
    		if(dy > 0)
    			Dy = anim;
    		else
    			Dy = - anim;
    	}
    }
    /**
	 * Get the sender graphical node
	 * @return  the node graph that has send this packet
	 * @uml.property  name="sender"
	 */
    public NodeGraph getSender(){
    	return sender;
    }
    /**
     * Move the packet in its direction
     *
     */
    public void move(){
    	x = x + Dx;
    	y = y + Dy;
    }
    /**
	 * Get the actual x coordinate of this packet 
	 * @return  the x coordinate
	 * @uml.property  name="x"
	 */
    public int getX(){
    	return (int) x;
    }
    /**
	 * Get the actual y coordinate of this packet
	 * @return  the y coordinate
	 * @uml.property  name="y"
	 */
    public int getY(){
    	return (int)y;
    }
    /**
     * Get the IP datagram transported by this packet
     * @return the IP datagram
     */
    public IpDatagram getDatagram(){
    	return ip_dtg;
    }
}
