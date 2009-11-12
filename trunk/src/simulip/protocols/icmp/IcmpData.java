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

import simulip.ip.*;

/**
 * @author  nataf
 */
public class IcmpData extends IpData {

    public static int ECHO = 3;
    public static int SEND = 0;
    public static int REPLY = 1;

    /**
	 * @uml.property  name="id"
	 */
    private int id;
    /**
	 * @uml.property  name="type"
	 */
    private int type;
    /**
	 * @uml.property  name="code"
	 */
    private int code;
    /**
	 * @uml.property  name="data"
	 */
    private byte[] data;

    public IcmpData(int i, int t, int c){
	id = i;
	type = t;
	code = c;
    }
    /**
	 * @return
	 * @uml.property  name="type"
	 */
    public int getType(){
	return type;
    }
    /**
	 * @return
	 * @uml.property  name="code"
	 */
    public int getCode(){
	return code;
    }
    /**
	 * @return
	 * @uml.property  name="id"
	 */
    public int getId(){
	return id;
    }
    /**
	 * @param data
	 * @uml.property  name="data"
	 */
    public void setData(byte[] data){
    	this.data = data;
    }
    /**
	 * @return
	 * @uml.property  name="data"
	 */
    public byte[] getData(){
    	return data;
    }
}
