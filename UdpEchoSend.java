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
    	


import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

import simulip.net.*;

public class UdpEchoSend extends Application{
	static private ResourceBundle messages=null;
	static {
		try { 
			messages= ResourceBundle.getBundle("simulip.examples.UdpEchoMessages");
		}catch(MissingResourceException mre){
			Logger.getLogger("").logp(Level.SEVERE, 
						"simulip.examples.UdpEchoSend", "static initialization", 
						"Can not read  UdpEchoMessages (messages properties files)", mre);
		}
	}
	public void run(){
		String ipdest="";
		try{
			
			ipdest = system.in.read(messages.getString("ipdest"));
			int count = 0;
			byte[] data = String.valueOf(count).getBytes();
		
			simulip.net.DatagramSocket d = new simulip.net.DatagramSocket(this);
			simulip.net.DatagramPacket p = new simulip.net.DatagramPacket(data,5,ipdest,54);
			while(true){
			d.send(p);
			d.receive(p);
			system.out.println(MessageFormat.format(messages.getString("ackOf"), new String(p.getData())));
			p.setAddress(p.getAddress());
			p.setPort(p.getPort());
			count++;
			data = String.valueOf(count).getBytes();
			p.setData(data);
			}
		}catch(NetworkAddressFormatException nafe){
			system.out.println(MessageFormat.format(messages.getString("badlyFormedAddress"), ipdest));
		}catch(Exception e){
			system.out.println(e.getMessage());
			Logger.getLogger("").log(Level.SEVERE, "UdpEchoSend error", e);
		}
	}
	
}
