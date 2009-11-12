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


import simulip.net.*;

public class UdpExampleClient extends  Application {

	public void run(){
		try{
			int count = 0;
			byte[] data = String.valueOf(count).getBytes();
		
			String ip = system.in.read();
			
			simulip.net.DatagramSocket d = new simulip.net.DatagramSocket(this);
			simulip.net.DatagramPacket p = new simulip.net.DatagramPacket(data,data.length,ip,53);
			while(true){
				try{
					Thread.sleep(400);
				}
				catch(Exception e){}				
				d.send(p);
				count++;
				byte[] nd = String.valueOf(count).getBytes();
				p.setData(nd);
			}
		}
		catch(Exception e){
			system.out.println(e.getMessage());
		}
	}
}
