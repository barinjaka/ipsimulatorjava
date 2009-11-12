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
    	
package simulip.examples;

import java.net.*;
import simulip.net.*;

public class UdpExampleServer extends Application{
	
	private byte[] data = new byte[5];
	
	public void run(){
		try{
		simulip.net.DatagramSocket d = new simulip.net.DatagramSocket(this,53);
		simulip.net.DatagramPacket p = new simulip.net.DatagramPacket(data,5);
		while(true){
			d.receive(p);
			p.setAddress(p.getAddress());
			p.setPort(p.getPort());
			system.out.println("recu " + new String(p.getData()));
			d.send(p);
		}
		}catch(BindException b){
			system.out.println(b.getMessage());
		}		
	}

}
