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

package simulip.gui.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;

import javax.tools.*;

import simulip.gui.model.LanGraph;
import simulip.gui.model.LanNodeGraph;
import simulip.gui.model.Link2LanGraph;
import simulip.gui.model.LinkGraph;
import simulip.gui.model.NetworkModel;
import simulip.gui.model.NodeGraph;
import simulip.gui.views.share.ApplicationCompilationError;
import simulip.ip.NetworkInterface;
import simulip.ip.NetworkMask;
import simulip.ip.NetworkMaskFormatException;
import simulip.ip.RouteEntry;
import simulip.net.NetworkAddress;
import simulip.net.NetworkAddressFormatException;

public class NetworkController {

	private NetworkModel model;

	private FileNotFoundException fne = null;
	private IOException ioe = null;
	private NoClassDefFoundError ncdfe = null;

	public NetworkController(NetworkModel pm) {
		model = pm;
	}

	public void createHostNode(int x, int y) {
		model.createNode(x, y, false);
	}

	public NodeGraph createHostNode(int x, int y, String n, int id) {
		return model.createNode(n, id, x, y, false);
	}

	public void createRouterNode(int x, int y) {
		model.createNode(x, y, true);
	}

	public NodeGraph createRouterNode(int x, int y, String n, int id) {
		return model.createNode(n, id, x, y, true);
	}

	public void createLan(int x, int y, int xw, int yw) {
		model.createLan(x, y, xw, yw);
	}

	public void createLan(int x, int y, int xw, int yw, String ln, int i) {
		model.createLan(x, y, xw, yw, ln, i);
	}

	public LinkGraph createLinkGraph(NodeGraph e0, NodeGraph e1) {
		if (e0 != e1) {
			Enumeration<LinkGraph> elg0 = e0.getLinksGraphs().keys();
			boolean found = false;
			while (elg0.hasMoreElements() && !found) {
				LinkGraph lg0 = elg0.nextElement();
				NodeGraph[] ngs = lg0.getNodes();
				found = ngs[0] == e1 || ngs[1] == e1;
			}
			if (!found) {
				return model.createLinkGraph(e0, e1);

			}
		}
		return null;
	}

	public Link2LanGraph createLink2LanGraph(NodeGraph pn, LanGraph plg) {
		boolean isnew = true;
		for (Enumeration<Link2LanGraph> el2l = plg.getLinks().elements(); el2l
				.hasMoreElements()
				&& isnew;) {
			Link2LanGraph l2l = el2l.nextElement();
			NodeGraph[] ngs = l2l.getNodes();
			if (pn == ngs[0] || pn == ngs[1])
				isnew = false;
		}
		if (isnew)
			return model.createLink2LanGraph(pn, plg);
		return null;
	}

	public void updateNodeName(NodeGraph node, String name) {
		if (!name.equals(""))
			node.setName(name);
	}

	public void updateNodeAddress(NodeGraph node, LinkGraph lg, String padd,
			String pmask) throws NetworkAddressFormatException,
			NetworkMaskFormatException {

		NetworkAddress na = new NetworkAddress(padd);
		NetworkMask mask = new NetworkMask(pmask);
		NetworkInterface ni = new NetworkInterface(na, mask, 100, node
				.getRouter());

		Enumeration<NetworkInterface> eni1 = node.getRouter().getNetIfs();
		boolean foundexisting1 = false;
		while (eni1.hasMoreElements() && !foundexisting1) {
			NetworkInterface ini1 = eni1.nextElement();
			LinkGraph lg1 = ini1.getLinkGraph();
			foundexisting1 = lg1 == lg;
			if (foundexisting1) {
				node.getRouter().removeNetIf(ini1.getAddress());
				node.removeLink(lg1);
			}
		}
		node.addLink(ni, lg);

		ni.setLinkGraph(lg);
		lg.setNetworkMask(mask);

		node.getRouter().addNetIf(ni);
		if (lg instanceof Link2LanGraph) {
			NodeGraph fnode = lg.getEndPoint(node);
			fnode.addLink(ni, lg);
			fnode.getRouter().addNetIf(ni);
		}
	}

	public void updateRoutingTableEntry(NodeGraph node, String pdest,
			String pmask, String pnh, String pif, String pmet)
			throws NetworkAddressFormatException, NetworkMaskFormatException {
		// TODO No validation at this
		// stage... If wanted
		// done into checkRoutes
		NetworkAddress dest = new NetworkAddress(pdest);
		NetworkMask mask = new NetworkMask(pmask);
		NetworkAddress nxh = new NetworkAddress(pnh);
		NetworkAddress ifs = new NetworkAddress(pif);
		int met = Integer.parseInt(pmet);
		RouteEntry re = new RouteEntry(dest, mask, nxh, ifs, met);
		node.getRouter().addRoute(re);
		// TODO
		// make a comparison between old and new routing table
		// Build.changed();

	}

	/**
	 * Add or update the application to the node. The application is a string
	 * that refers either to the absolute name of the java source file or java
	 * class file. If it is a java source file then the file is compiled and the
	 * class file is saved in the file system from the same where the source
	 * file was found. The class file is loaded and given to the node
	 * 
	 * @param node
	 *            the node which applications are updated
	 * @param papp
	 *            the string of
	 * @throws ClassNotFoundException
	 *             when the class file is not found
	 * @throws IllegalAccessException
	 *             when the class file can not be accessed
	 * @throws IOException
	 *             when general IO problem occurs
	 * @throws FileNotFoundException
	 *             when the java source (or class) file is not found
	 * @throws ApplicationCompilationError
	 *             when the application java source code has at least one error
	 */
	public Class<?> putApplication(NodeGraph node, String papp)
			throws ClassNotFoundException, IllegalAccessException, IOException,
			FileNotFoundException, ApplicationCompilationError,
			NoClassDefFoundError {

		SimulipAppliClassLoader cl = new SimulipAppliClassLoader();

		if (papp.endsWith(".java")) {
			File file = new File(papp);
			File[] files1 = { file };

			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
			StandardJavaFileManager fileManager = compiler
					.getStandardFileManager(diagnostics, null, null);

			Iterable<? extends JavaFileObject> compilationUnits1 = fileManager
					.getJavaFileObjectsFromFiles(Arrays.asList(files1));
			compiler.getTask(null, fileManager, diagnostics, null, null,
					compilationUnits1).call();
			String err = "";
		       for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics())
		           err = err + diagnostic.getMessage(Locale.getDefault()) + "\n";
		       if (err.compareTo("") != 0)
		    	   throw new ApplicationCompilationError(err);
		       

			papp = papp.substring(0, papp.lastIndexOf(".java")) + ".class";
		}

		Class<?> ca = cl.findClass(papp);
		if (ca == null) {
			if (fne != null) {
				FileNotFoundException lfne = fne;
				fne = null;
				throw lfne;
			} else if (ioe != null) {
				IOException lioe = ioe;
				ioe = null;
				throw lioe;
			} else if (ncdfe != null) {
				NoClassDefFoundError lncdfe = ncdfe;
				throw lncdfe;
			}
		}

		return node.putApplicationClass(papp, ca);
	}

	public void updateLink2LanAddress(NodeGraph node, LinkGraph link,
			LanNodeGraph fnode, String add, String mask)
			throws NetworkAddressFormatException, NetworkMaskFormatException {
		updateLinkAddresses(node, link, fnode, add, add, mask);

	}

	public void updateLinkAddresses(NodeGraph node, LinkGraph link,
			NodeGraph fnode, String add0, String add1, String mask)
			throws NetworkAddressFormatException, NetworkMaskFormatException {
		NetworkInterface netif = new NetworkInterface(new NetworkAddress(add0),
				new NetworkMask(mask), 100, node.getRouter());

		NetworkInterface fakenetif = new NetworkInterface(new NetworkAddress(
				add1), new NetworkMask(mask), 100, fnode.getRouter());

		netif.setLinkGraph(link);
		link.setNetworkMask(new NetworkMask(mask));
		fakenetif.setLinkGraph(link);

		Enumeration<NetworkInterface> eni1 = node.getRouter().getNetIfs();
		boolean foundexisting1 = false;
		while (eni1.hasMoreElements() && !foundexisting1) {
			NetworkInterface ini1 = eni1.nextElement();
			LinkGraph lg1 = ini1.getLinkGraph();
			foundexisting1 = lg1 == link;
			if (foundexisting1) {
				node.getRouter().removeNetIf(ini1.getAddress());
				node.removeLink(lg1);
			}
		}

		node.addLink(netif, link);
		node.getRouter().addNetIf(netif);

		Enumeration<NetworkInterface> eni2 = fnode.getRouter().getNetIfs();
		boolean foundexisting2 = false;
		while (eni2.hasMoreElements() && !foundexisting2) {
			NetworkInterface ini2 = eni2.nextElement();
			LinkGraph lg2 = ini2.getLinkGraph();
			foundexisting1 = lg2 == link;
			if (foundexisting2) {
				fnode.getRouter().removeNetIf(ini2.getAddress());
				fnode.removeLink(lg2);
			}
		}

		fnode.addLink(fakenetif, link);
		fnode.getRouter().addNetIf(fakenetif);

	}

	private class SimulipAppliClassLoader extends ClassLoader {

		@SuppressWarnings("unchecked")
		public Class findClass(String name) {
			FileInputStream fi;
			byte[] b = null;
			try {
				fi = new FileInputStream(name);
				b = new byte[fi.available()];
				fi.read(b);
			} catch (FileNotFoundException e) {
				fne = e;
				return null;
			} catch (IOException e) {
				ioe = e;
				return null;
			}
			String classname = name.substring(0, name.indexOf(".class"));
			classname = classname.substring(classname
					.lastIndexOf(File.separator) + 1, classname.length());
			try {
				Class c = defineClass(classname, b, 0, b.length);
				return c;
			} catch (Throwable ncdf) {
				ncdfe = (NoClassDefFoundError) ncdf;
				return null;
			}

		}
	}

	public void updateLanName(LanGraph lan, String ln) {
		lan.setName(ln);

	}
}
