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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.*;

import simulip.ip.NetworkMask;

/**
 * A network address with mask in formats BitSet and String
 * 
 * @author Emmanuel Nataf
 * 
 */
public class NetworkAddress {
	/**
	 * To convert a IP address from the dotted decimal String format to a bitSet
	 * 
	 * @param s the IP address
	 * @return the same in a BitSet
	 */
	public static BitSet toBitSet(String s)
			throws NetworkAddressFormatException {
		int idot = 0;
		int nboctets = 0;
		int rangoct = 0;
		boolean lastoct = false;
		boolean finish = false;
		BitSet bs = new BitSet(32);
		while (!finish) {
			int digit = 128;
			String octet;
			try {
				if (!lastoct){
					octet = s.substring(idot, s.indexOf('.', idot + 1));	
				}
				else {
					octet = s.substring(idot, s.length());	
					finish = true;
				}
				nboctets++;
				if(nboctets == 5)
					throw new NetworkAddressFormatException("too many . in " + s);
					
			} catch (IndexOutOfBoundsException iob) {
				throw new NetworkAddressFormatException(iob.getMessage());
			}
			byte b = 0;
			try {
				b = (new Integer(Integer.parseInt(octet))).byteValue();
			} catch (NumberFormatException e) {
				throw new NetworkAddressFormatException(e.getMessage());
			}
			if (Integer.parseInt(octet) > 255)
				throw new NetworkAddressFormatException("incorrect value " + octet);
			for (int i = 0; i < 8; i++) {
				if ((b & digit) == digit)
					bs.set(i + rangoct, true);
				else
					bs.set(i + rangoct, false);
				digit = digit / 2;
			}
			rangoct = rangoct + 8;
			idot = s.indexOf('.', idot + 1) + 1;
			if (s.indexOf('.', idot) == -1)
				lastoct = true;
		}
		if (nboctets < 4)
			throw new NetworkAddressFormatException("not enougth . in " + s);
			
		return bs;
	}
	/**
	 * convert the given BitSet in a array of 4 byte
	 * 
	 * @param s
	 *            the IP address in a BitSet
	 * @return a byte[4] of the given BitSet
	 */
	public static byte[] toBytes(BitSet b) {
		byte[] res = new byte[4];
		BitSet[] octets = new BitSet[4];
		octets[0] = b.get(0, 8);
		octets[1] = b.get(8, 16);
		octets[2] = b.get(16, 24);
		octets[3] = b.get(24, 32);
		for (int i = 0; i < 4; i++) {
			int octet = 0;
			for (int j = 0; j < 8; j++) {
				if (octets[i].get(j))
					octet += Math.pow(2, 7 - j);
			}
			res[i] = (byte) octet;
		}
		return res;

	}

	/**
	 * Convert the given BitSet in the dotted string format
	 * 
	 * @param b
	 *            the bitSet for a IP address
	 * @return the dotted string
	 */
	public static String toDottedString(BitSet b) {

		BitSet[] octets = new BitSet[4];
		octets[0] = b.get(0, 8);
		octets[1] = b.get(8, 16);
		octets[2] = b.get(16, 24);
		octets[3] = b.get(24, 32);
		String straddr = new String();
		for (int i = 0; i < 4; i++) {
			int octet = 0;
			for (int j = 0; j < 8; j++) {
				if (octets[i].get(j))
					octet += Math.pow(2, 7 - j);
			}
			straddr += octet + ".";
		}
		return straddr.substring(0, straddr.length() - 1);
	}

	/**
	 * The address in a BitSet
	 */
	private BitSet address = new BitSet(32);

	/**
	 * String version of address and mask
	 */
	private String str_addr;

	/**
	 * Create a new NetworkAddress from a 4 bytes arrays
	 * 
	 * @param a
	 *            the NetworkAddress
	 */
	public NetworkAddress(byte[] a) throws NetworkAddressFormatException {
		String add = new String();
		for (int i = 0; i < 4; i++) {
			Byte ba = new Byte(a[i]);
			int iba = ba.intValue();
			if (iba < 0)
				iba = 256 + iba;
			add = add.concat(iba + ".");
		}
		str_addr = add.substring(0, add.length() - 1);
		address = toBitSet(str_addr);

	}

	/**
	 * Create a IP address with a string
	 * 
	 * @param s
	 *            the string
	 */
	public NetworkAddress(String s) throws NetworkAddressFormatException {
		str_addr = s; 
		address = toBitSet(str_addr);
	}

	/**
	 * Give the broadcast address of this IP address
	 * 
	 * @param the
	 *            mask for this address
	 * @return the broadcast address
	 */
	public NetworkAddress broadcast(NetworkMask m) {
		try {
			BitSet bs1 = (BitSet) address.clone();
			bs1.and(toBitSet(m.toString())); // flip mask and  Use andNot ?
			BitSet bs2 = toBitSet(m.toString());
			bs2.flip(0, 32); // 31 ?
			bs1.or(bs2);
			String broadcaststr = toDottedString(bs1);
			return new NetworkAddress(broadcaststr);
		} catch (NetworkAddressFormatException e) {
			Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(), "broadcast", "Really unexcept error !", e);
			return null; // Could not append
		}
	}

	/**
	 * 
	 * @param a
	 *            an IP address to compare
	 * @return true if the given address is the same of its IP address
	 */
	public boolean equals(NetworkAddress a) {
		if (a != null)
			return a.getStrAddress().equals(str_addr);
		else
			return false;
	}

	/**
	 * Get the IP address at the BitSet format
	 * 
	 * @return the IP address
	 */
	public BitSet getBits() {
		return address;
	}

	/**
	 * Give the network address of this IP address
	 * 
	 * @param the
	 *            mask for this address
	 * @return the network address
	 */
	public NetworkAddress getNetwork(NetworkMask m) {
		try {
			BitSet bs1 = (BitSet) address.clone();
			bs1.and(NetworkAddress.toBitSet(m.toString()));
			String netaddrstr = toDottedString(bs1);
			return new NetworkAddress(netaddrstr);
		} catch (NetworkAddressFormatException e) {
			Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(), "getNetwork", "Really unexcept error !", e);
			return null; // could not happen
		}

	}

	/**
	 * Get the IP address in the String format
	 * 
	 * @return the IP address
	 */
	public String getStrAddress() {
		return str_addr;
	}

	/**
	 * Ask if this IP address is a default address, equals to 0.0.0.0
	 * 
	 * @return true if this is a default address
	 */
	public boolean isDefault() {
		return str_addr.equals("0.0.0.0");
	}

	/**
	 * Ask if this IP address is in the network address / mask
	 * 
	 * @param dest
	 *            the network address
	 * @param fmask
	 *            the mask for the network address
	 * @return true if this IP address is in the network / mask
	 */
	public boolean isInDest(NetworkAddress dest, NetworkAddress fmask) {
		BitSet mybs = (BitSet) address.clone();
		mybs.and(fmask.getBits());
		if (mybs.equals(dest.getBits()))
			return true;
		else
			return false;
	}

	/**
	 * Ask if this IP address is in the network address / mask
	 * 
	 * @param dest
	 *            the network address
	 * @param fmask
	 *            the mask for the network address
	 * @return true if this IP address and the mask is the given network address
	 */
	public boolean isInDest(NetworkAddress dest, NetworkMask fmask) {
		try {
			BitSet mybs = (BitSet) address.clone();
			mybs.and(toBitSet(fmask.toString()));
			if (mybs.equals(dest.getBits()))
				return true;
			else
				return false;
		} catch (NetworkAddressFormatException e) {
			Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(), "isInDest", "Really unexcept error !", e);
			return false; // could not happen
		}
	}

	/**
	 * Test if this IP address and the given address are in the same network.
	 * The given mask of this IP adress is used.
	 * 
	 * @param b
	 *            a network address
	 * @param m
	 *            the mask
	 * @return true if the specified network address is in the network of this
	 *         network address
	 */
	public boolean sameNetwork(NetworkAddress b, NetworkMask m) {

		BitSet bs = (BitSet) b.getBits().clone();
		BitSet bsa = (BitSet) address.clone();
		try {
			bs.and(toBitSet(m.toString()));
			bsa.and(toBitSet(m.toString()));
			return bs.equals(bsa);
		} catch (NetworkAddressFormatException e) {
			Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(), "sameNetwork", "Really unexcept error !", e);
			return false; // Could not happen
		}
	}

	/**
	 * Give a binary representation of this IP address
	 * 
	 * @return a String with "0" and "1"
	 */
	public String toBinaryString() {
		BigInteger bi = null;
		BitSet[] octets = new BitSet[4];
		octets[0] = address.get(0, 8);
		octets[1] = address.get(8, 16);
		octets[2] = address.get(16, 24);
		octets[3] = address.get(24, 32);
		for (int i = 0; i < 4; i++) {
			bi = new BigInteger("0");
			int octet = 0;
			for (int j = 0; j < 8; j++) {
				if (octets[i].get(j))
					octet += Math.pow(2, 7 - j);
			}
			bi.multiply(new BigInteger((new Integer(octet)).toString()));
		}
		return bi.toString();
	}

	/**
	 * the string IP address and its mask
	 */
	public String toString() {
		return str_addr;
	}
}
