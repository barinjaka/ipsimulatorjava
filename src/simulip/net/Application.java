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

import simulip.ip.IpDatagram;
import simulip.ip.IpRouter;

import java.net.*;

/**
 * @author nataf
 */
public class Application extends Thread {

	private Transport transport;
	public IpDatagram datagram = null;
	private boolean changed = false;
	private boolean knocked = false;
	/**
	 * The pseudo System class to simulate stdin and stdout
	 */
	public simulip.net.System system = new System(this);
	private String appliName = "";

	public void setAppliName(String n){
		appliName = n;
	}
	
	public String getAppliName(){
		return appliName;
	}
	public void setIpBinding(Transport i) {
		transport = i;
	}

	/**
	 * @return
	 * @uml.property name="changed"
	 */
	final public boolean isChanged() {
		return changed;
	}

	/**
	 * @param c
	 * @uml.property name="changed"
	 */
	final public void setChanged(boolean c) {
		if (c)
			transport.getNodeGraph().update();
		changed = c;
	}

	public String getNodeName() {
		return transport.getNodeName();
	}

	public java.awt.Point getLocationOnScreen() {
		return transport.getLocationOnScreen();
	}

	public int newPort() {
		return transport.newPort(this);
	}

	public boolean askPort(int p) throws BindException {

		return transport.askPort(p, this);
	}

	public synchronized void knock(IpDatagram datagram) {
		this.datagram = datagram;
		knocked = true;
		notify();
	}

	public synchronized IpDatagram waitForDatagram() {
		IpDatagram ret = null;
		while (!knocked) {
			try {
				this.wait();
			} catch (Exception e) {
			}
		}
		ret = datagram;
		datagram = null;
		knocked = false;
		return ret;
	}

	public void send(IpDatagram datagram) {
		transport.send(datagram);
	}

	public IpRouter getRouter() {
		return transport.getRouter();
	}
}
