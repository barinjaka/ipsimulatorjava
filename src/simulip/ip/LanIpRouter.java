package simulip.ip;

public class LanIpRouter extends IpRouter {
	
	private int interfaces = 0;
	
	
	/**
	 * Add a new network interface to this router
	 * 
	 * @param ni the new network interface
	 * TODO: would we introduce an kind of "IP FORWARDING" variable on node to get a separate between route/host. 
	 * And when be able to have "multi-homing" on one host.
	 */
	public void addNetIf(NetworkInterface ni) {
		net_ifs.put(ni.getAddress().getStrAddress() + "." + interfaces, ni);
		if (net_ifs.size() == 1)
			localhost = ni.getAddress();
		else
			localhost = null;
		interfaces++;
	}
}
