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
package simulip.protocols.icmp;

import java.util.*;

import simulip.ip.*;
import simulip.net.NetworkAddress;

/**
 * @author  nataf
 */
public class IcmpEntity {

    static public int id = 0;

    /**
	 * @uml.property  name="ip_entity"
	 * @uml.associationEnd  
	 */
    IpRouter ip_entity;

    Hashtable<Integer, IcmpData> pendings = new Hashtable<Integer, IcmpData>();

    public IcmpEntity(IpRouter r){
    	ip_entity = r;
    }
    public IpDatagram send(NetworkAddress d){
    	IcmpData data = new IcmpData(id, IcmpData.ECHO, IcmpData.SEND);
    	pendings.put(new Integer(id), data); 
    	id++;
    	IpDatagram dtg = new IpDatagram(ip_entity.getAddress(), d);
    	dtg.setType(IpDatagram.ICMP);
    	dtg.setData(data);
    	dtg.setTtl(32);
    	return dtg;
    }
	
    public void knock(IpDatagram ip){
    	IcmpData data = (IcmpData)ip.getData();
    	if(data.getType() == IcmpData.ECHO){
    		if(data.getCode() == IcmpData.SEND){
    			int idin = data.getId();
    			IcmpData datarep = new IcmpData(idin, IcmpData.ECHO, IcmpData.REPLY);
    			ip.setData(datarep);
    			ip.setType(IpDatagram.ICMP);
    			ip.setTtl(32);
    			ip.swap();
    			ip_entity.knock(ip);
    		}
    		else if (data.getCode() == IcmpData.REPLY) {
    			if(pendings.get(new Integer(data.getId())) != null)
    				pendings.remove(new Integer(data.getId()));
    		}
    	}
    }
}
