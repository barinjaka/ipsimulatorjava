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
package simulip.protocols.rip;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulip.ip.*;
import simulip.net.*;

/**
 * @author  nataf
 */
public class Rip extends Application {
	
	/**
	 * @uml.property  name="router"
	 * @uml.associationEnd  
	 */
	private IpRouter router;
	private Hashtable<String, RouteEntry> received_routes = new Hashtable<String, RouteEntry>();
	private Hashtable<String, RouteEntry> actual_routes = new Hashtable<String, RouteEntry>();
	private Hashtable<String, Long> routes_ages = new Hashtable<String, Long>();
	private boolean  triggeredUpdate = true;
	private boolean updated = false;
	private boolean readed = false;
	private boolean checked = false;
	/**
	 * @uml.property  name="socket"
	 * @uml.associationEnd  
	 */
	private DatagramSocket socket;
	
	public static int PORT = 520;
	private static long OUT_INTERVAL = 4000;
	private static long ANNOUNCE_INTERVAL = 2000;
	
	public Rip(){}
	
	public void run(){
		
		router = getRouter();
		try{
			socket = new DatagramSocket(this,520);
		}
		catch(java.net.BindException b){
			b.printStackTrace();
		}
		rip(0);
	}
	private void rip(int shedule){
		
		 final class ripAnnounceTask extends TimerTask {
			  public  synchronized void run(){
				  while(updated || checked)
					  try{
						  readed = true;
						  wait();
					  }
				  catch(Exception e){}
				  Enumeration<NetworkInterface> eni = router.getNetIfs();
				  while(eni.hasMoreElements()){
					  NetworkInterface ni = eni.nextElement();
					  NetworkAddress na = ni.getNetwork();
					  NetworkAddress nb = ni.getBroadcast();
					  
					  Enumeration<RouteEntry> er = null, ers = null;
					  int rasize = 0;
					  int nroute = 0;
	
					  ers = router.getRoutes().elements();
					  er = router.getRoutes().elements();
					  

					
					  while(ers.hasMoreElements()){
						  RouteEntry re = ers.nextElement();						
						  NetworkAddress nexthop = re.getNextHop();
						  if(!na.sameNetwork(nexthop,ni.getMask()))
							  rasize++;
					  }
					  
					  
					  byte[] ra = new byte[rasize * 9];

					  while(er.hasMoreElements()){
						  RouteEntry re = er.nextElement();						
						  NetworkAddress nexthop = re.getNextHop();
						  if(!na.sameNetwork(nexthop,ni.getMask())){
								  NetworkAddress dest = re.getDestination();
								  NetworkMask msk = re.getMask();
								  int m = re.getMetric();
								  byte[] netadd = NetworkAddress.toBytes(dest.getBits());
								  byte[] maskadd = null;
								  try{
								  maskadd = NetworkAddress.toBytes(NetworkAddress.toBitSet(msk.toString()));
								  }
								  catch(NetworkAddressFormatException e){}
								  byte metric = (byte)m;
								  for(int i = nroute; i < nroute + 4; i++){
									  ra[i] = netadd[i - nroute];
									  ra[i+4] = maskadd[i - nroute];
								  }
								  ra[nroute + 8] = metric;
								  nroute = nroute + 9;	
						  }		
					  }		
					  try{
						  if(rasize > 0){
							  DatagramPacket p = new DatagramPacket(ra, ra.length,nb.getStrAddress(), Rip.PORT);
							  socket.send(p);
							  java.lang.System.out.println("rip");
						  }
					  }
					  catch(Exception e){
						  e.printStackTrace();
					  }
				  }
				  readed = false;
				  notifyAll();
			  }
		 }
	
		final class ripCheckRouteTask extends TimerTask {
			public  synchronized void run(){
				while(readed || updated)
					try{
						wait();
					}
				catch(Exception e){}
				checked = true;
				boolean update = false;
				Enumeration<String> ea = routes_ages.keys();
				while(ea.hasMoreElements()){
					String na = ea.nextElement();
					Long l = routes_ages.get(na);
					if(java.lang.System.currentTimeMillis() - l > 2 * Rip.ANNOUNCE_INTERVAL){
						RouteEntry re = actual_routes.get(na);
						if(re.getMetric() != 16){
							update = true;
							re.setMetric(16);
							router.delRoute(na);
							router.addRoute(re);
							NetworkAddress nhop = re.getNextHop();
							Enumeration<RouteEntry>ere = actual_routes.elements();
							while(ere.hasMoreElements()){
								RouteEntry othere = ere.nextElement();
								if(!othere.getDestination().getStrAddress().equals(na))
									if(othere.getNextHop().getStrAddress().equals(nhop.getStrAddress())){
										if(othere.getMetric() != 16){
											othere.setMetric(16);
											router.delRoute(othere.getDestination().getStrAddress());
											router.addRoute(othere);
											update = true;
										}
									}	
							}
							ripAnnounceTask ratt = new ripAnnounceTask();
							Timer t = new Timer();
							t.schedule(ratt, new Date(1));
						}
						
					}
				
					setChanged(update);
				}
				checked = false;
				notifyAll();
			}
		}
		if(shedule == 0){
			// first call of rip, the router must construct its 
			// routing table for connected interfaces 
			
			if(router.getRoutes().size() == 0){
				Enumeration<NetworkInterface> eni = router.getNetIfs();
				while(eni.hasMoreElements()){
					NetworkInterface ni = eni.nextElement();
					NetworkAddress na = ni.getAddress();
					NetworkMask nm = ni.getMask();
					NetworkAddress nn = ni.getNetwork();
					RouteEntry re = new RouteEntry(nn,nm,na,na,0);
					router.addRoute(re);
				}
			}
			
			
			ripAnnounceTask ratt = new ripAnnounceTask();
			ripCheckRouteTask rout = new ripCheckRouteTask();
			Timer t = new Timer();
			t.scheduleAtFixedRate(ratt, Rip.ANNOUNCE_INTERVAL, Rip.ANNOUNCE_INTERVAL);
			t.scheduleAtFixedRate(rout, Rip.OUT_INTERVAL, Rip.OUT_INTERVAL);
			receive();
		}
		else {
			ripAnnounceTask ratt = new ripAnnounceTask();
			Timer t = new Timer();
			t.schedule(ratt, new Date(1));
		}
		
			
	}
	
	public  synchronized void receive(){
		while(true){
			boolean update = false;
			boolean lost_route = false;
			Enumeration<NetworkInterface> eni = router.getNetIfs();
			boolean found = false;
			NetworkInterface ni = null;
			byte[] t = new byte[1000];
			simulip.net.DatagramPacket dp = new DatagramPacket(t, t.length);
			//try{
				socket.receive(dp);
			//}
			//catch(Exception e){}
				NetworkAddress ips = null;
				NetworkAddress ipd = null;
			try{
			ips = new NetworkAddress(dp.getAddress().getStrAddress());
			ipd = new NetworkAddress(dp.getDestAddress().getStrAddress());
			}
			catch(NetworkAddressFormatException e){}
			while(!found && eni.hasMoreElements()){
				ni = eni.nextElement();
				NetworkAddress na = ni.getAddress();
				found = na.sameNetwork(ipd,ni.getMask()) &&  
				!ips.getStrAddress().equals(na.getStrAddress());
			}
			if(found){
				received_routes.clear();
				byte[] ra = dp.getData();
				int nbra = ra.length / 9;
				for(int i = 0; i < nbra; i++){
					byte[] raadd = new  byte[4];
					byte[] ramsk = new byte[4];
					int metric = (int)ra[8 + (i * 9)];
					for(int j = 0; j < 4; j++){
						raadd[j] = ra[(i*9) + j];
						ramsk[j] = ra[4 + (i * 9) + j];
					}
					NetworkAddress nd = null;
					try{
						nd = new NetworkAddress(raadd);
					}
					catch(NetworkAddressFormatException e){
						Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(), "receive", "", e);
					}
					NetworkMask nm = null;
					try {
						nm = new NetworkMask(ramsk, true);
					}catch(NetworkMaskFormatException nme){
						Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(), "receive", "", nme);
					}
					RouteEntry re = new RouteEntry(nd, nm, ips,ni.getAddress(),metric);
					received_routes.put(nd.getStrAddress(),re);
				}
				while(readed || checked)
					try{
						wait();
					}
				catch(Exception e){}
				updated = true;
				Enumeration<RouteEntry> er = received_routes.elements();
				while(er.hasMoreElements()){
					RouteEntry re = er.nextElement();
					NetworkAddress newdest = re.getDestination();
					String sndest = newdest.getStrAddress();
					RouteEntry actroute = actual_routes.get(sndest);
					if(actroute != null){

						NetworkAddress ngatw = re.getNextHop();
						int newmet = re.getMetric();
						NetworkAddress oldgatw = actroute.getNextHop();
						if(newmet == 16 && actroute.getMetric() != 16)
							lost_route = true;
						if(oldgatw.getStrAddress().equals(ngatw.getStrAddress())){
							if(newmet +1 <= 16 )
								re.setMetric(++newmet);
							if(actroute.getMetric() != newmet)
								update = true;
							actual_routes.remove(sndest);
							actual_routes.put(sndest,re);
							router.delRoute(sndest);
							router.addRoute(re);						
							routes_ages.put(sndest,java.lang.System.currentTimeMillis());
						}
						else{
							int actmet = actroute.getMetric();
							if(actmet > newmet + 1){
								re.setMetric(++newmet);
								actual_routes.remove(sndest);
								actual_routes.put(sndest, re);
								router.delRoute(sndest);
								router.addRoute(re);
								routes_ages.put(sndest,java.lang.System.currentTimeMillis());
								update = true;
							}
						}				
					}
					else {
						java.util.Vector<RouteEntry> routes = router.getRoutes();
						found = false;
						for(int i = 0; i < routes.size();i++)
							if(routes.get(i).getDestination().getStrAddress().equals(sndest)){
								found = true;
								break;
							}
						if(!found){
							re.setMetric(re.getMetric() + 1);
							actual_routes.put(sndest,re);	
							router.addRoute(re);
							routes_ages.put(sndest,java.lang.System.currentTimeMillis());
							update = true;
						}
					}
				}
				setChanged(update);
				if(lost_route && triggeredUpdate)
					rip(1);
				updated = false;
				notifyAll();
			}
		}
	}
	
	public void setTriggered(boolean b){
		triggeredUpdate = b;
	}
	private boolean stopped = false;
	public void setStoppedTime(boolean s){
		if(s == false && stopped == true)
			razTimeout();
		stopped = s;
	}
	private void razTimeout(){
		Enumeration <String> es = routes_ages.keys();
		while(es.hasMoreElements())
			routes_ages.put(es.nextElement(),java.lang.System.currentTimeMillis());
	}

}
