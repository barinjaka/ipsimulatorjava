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

package simulip.gui.model;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;

import simulip.gui.Build;
import simulip.ip.IpRouter;

public class NetworkModel {

	private Vector<LinkGraph> links = new Vector<LinkGraph>();
	private Hashtable<Integer, LanGraph> lans = new Hashtable<Integer, LanGraph>();
	private Hashtable<Integer, NodeGraph> nodes = new Hashtable<Integer, NodeGraph>();

	private int ielement = 0;
	private int nbhosts = 0;
	private int nbrouters = 0;
	private int nblans = 0;

	private ResourceBundle messages;

	public NetworkModel(Hashtable<Integer, NodeGraph> n, Vector<LinkGraph> li,
			Hashtable<Integer, LanGraph> la) {
		nodes = n;
		links = li;
		lans = la;
		ielement = nodes.size() + lans.size();
		messages = Build.messages;
	}

	public NetworkModel() {
		messages = Build.messages;
	}

	public Vector<LinkGraph> getLinks() {
		return links;
	}

	public Hashtable<Integer, LanGraph> getLans() {
		return lans;
	}

	public Hashtable<Integer, NodeGraph> getNodes() {
		return nodes;
	}

	public NodeGraph getNode(int id) {
		if (nodes.containsKey(id))
			return nodes.get(id);
		if (lans.containsKey(id))
			return lans.get(id).getFakeNode();
		return null;
	}

	public void createNode(int x, int y, boolean isrouter) {
		String ldefaultNodeName = null;
		if (isrouter)
			ldefaultNodeName = MessageFormat.format(messages
					.getString("newRouterName"), nbrouters);
		else
			ldefaultNodeName = MessageFormat.format(messages
					.getString("newHostName"), nbhosts);

		_createNode(ldefaultNodeName, ielement, x, y, isrouter);

	}

	public NodeGraph createNode(String name, int id, int x, int y,
			boolean isrouter) {
		return _createNode(name, id, x, y, isrouter);
	}

	private NodeGraph _createNode(String name, int id, int x, int y,
			boolean isrouter) {
		if (isrouter)
			nbrouters++;
		else
			nbhosts++;
		if (id > ielement)
			ielement = id + 1;
		else
			ielement++;
		NodeGraph n = new NodeGraph(name, x, y);
		n.setId(id);
		IpRouter r = new IpRouter();
		n.setRouter(r);
		r.setPhysicalNode(n);
		n.setRouter(isrouter);
		nodes.put(n.getId(), n);
		return n;
	}

	public void createLan(int x, int y, int pwidth, int phigh) {
		String ldefaultLanName = MessageFormat.format(messages
				.getString("newLanName"), nblans);
		_createLan(x, y, pwidth, phigh, ldefaultLanName, ielement++);
	}

	public void createLan(int x, int y, int pwidth, int phigh, String name,
			int id) {
		_createLan(x, y, pwidth, phigh, name, id);
	}

	private void _createLan(int x, int y, int pwidth, int pheight, String name,
			int id) {
		LanGraph llanGraph = new LanGraph(x, y, pwidth, pheight, name);
		nblans++;
		if (id > ielement)
			ielement = id + 1;
		else
			ielement++;
		llanGraph.setId(id);
		lans.put(id, llanGraph);
	}

	public LinkGraph createLinkGraph(NodeGraph pe0, NodeGraph pe1) {

		LinkGraph lg = new LinkGraph();
		NodeGraph[] ngs = new NodeGraph[2];
		ngs[0] = pe0;
		ngs[1] = pe1;
		lg.addNodes(ngs);
		ngs[0].addLinkGraph(lg);
		ngs[1].addLinkGraph(lg);
		links.add(lg);
		return lg;
	}

	public Link2LanGraph createLink2LanGraph(NodeGraph pn, LanGraph plg) {

		Link2LanGraph ll2lg = new Link2LanGraph();
		NodeGraph[] lngs = new NodeGraph[2];
		lngs[0] = pn;
		lngs[1] = plg.getFakeNode();
		ll2lg.addNodes(lngs);
		lngs[0].addLinkGraph(ll2lg);
		lngs[1].addLinkGraph(ll2lg);
		links.add(ll2lg);
		plg.addLink2LanGraph(ll2lg);
		return ll2lg;
	}

}
