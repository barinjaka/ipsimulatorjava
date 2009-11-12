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

/*
 * TODO: prefixe - validation. 
 */



package simulip.ip;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An IPv4 network mask
 * @author  Emmanuel Nataf   
 * TODO: better messages in NetworkMaskFormatException  
 * TODO: Prefix size than not validated...
 */
public class NetworkMask {
	/**
	 * A dotted decimal String representation of the mask
	 */
	private String str_mask;
	
	/**
	 * @uml.property  name="prefixSize"
	 */
	private int prefixSize=0;
	
	/** Create a new mask
	 * @param m the dotted decimal string for the mask
	 * @throws NetworkMaskFormatException 
	 */
	public NetworkMask(String m)throws NetworkMaskFormatException  {
		this(m, true);
	}
	
	/**
	 * Create a new mask
	 * @param m the dotted decimal string for the mask
	 * @param a boolean to force validation and then possibly get a NetworkMaskFormatException 
	 * @throws NetworkMaskFormatException (only if validateIt is set)
	 */
	public NetworkMask(String m, boolean validateIt) throws NetworkMaskFormatException  {
		NetworkMaskFormatException ce=null;
		this.str_mask = m;
		this.prefixSize=0;
		try {
			Pattern maskp=Pattern.compile("\\A(\\d{1,3}).(\\d{1,3}).(\\d{1,3}).(\\d{1,3})\\z");
			Matcher maskm=maskp.matcher(m);
			if (maskm.matches()){
				boolean acceptsOnly0=false;
				for (int i=0; i<4; i++){
					int octet=Integer.parseInt(maskm.group(i+1));
					// regex ensures >=0...
					if (octet>255){
						ce=new NetworkMaskFormatException("Illegal mask value : " + octet);
					}else{
						if (acceptsOnly0) {
			    			if (octet!=0){
			    				ce=new NetworkMaskFormatException("Only 0 value : " + octet);
			    			}
			    			// else ok go on... and prefixe size does not change (we already have it indeed)
			    		}else{ // 
			    			switch (octet) {
			    			case 0: acceptsOnly0=true; break;
			    			case 128: acceptsOnly0=true; this.prefixSize+=1; break;
			    			case 192: acceptsOnly0=true; this.prefixSize+=2; break;
			    			case 224: acceptsOnly0=true; this.prefixSize+=3; break;
			    			case 240: acceptsOnly0=true; this.prefixSize+=4; break;
			    			case 248: acceptsOnly0=true; this.prefixSize+=5; break;
			    			case 252: acceptsOnly0=true; this.prefixSize+=6; break;
			    			case 254: acceptsOnly0=true; this.prefixSize+=7; break;
			    			case 255: this.prefixSize+=8; break;
			    			default: ce=new NetworkMaskFormatException("Illegal mask value : " + octet);
			    			}
			    		}
					}
				}
			}else{
				ce=new NetworkMaskFormatException("Illegal mask format : " + m);
			}
		}catch(Exception e){
			ce=new NetworkMaskFormatException(e);
		}
		if (validateIt && ce!=null){
			throw ce;
		}
	} 
	
	/**
	 * Create a new mask
	 * @param a  four byte array for the 32 bits mask
	 * @param a boolean to force validation and then possibly get a NetworkMaskFormatException
	 * @throws NetworkMaskFormatException (only if validateIt is set)
	 */
	public NetworkMask(byte[] a, boolean validateIt) throws NetworkMaskFormatException {
		boolean good=true;
		boolean acceptsOnly0=false;
    	String add = new String();
    	this.prefixSize=0;
    	
    	for(int i = 0; i < 4; i++){
    		int iba=a[i];
    		if(iba < 0) {
    			good=false;
    			iba = 256 + iba;
    		}
    		if (iba>255){
    			good=false;
    			iba=iba%256;
    		}
    		add = add.concat(iba + ".");
    		
    		if (acceptsOnly0) {
    			if (iba!=0){
    				good=false;
    			}
    			// else ok go on... and prefixe size does not change (we already have it indeed)
    		}else{ // 
    			switch (iba) {
    			case 0: acceptsOnly0=true; break;
    			case 128: acceptsOnly0=true; this.prefixSize+=1; break;
    			case 192: acceptsOnly0=true; this.prefixSize+=2; break;
    			case 224: acceptsOnly0=true; this.prefixSize+=3; break;
    			case 240: acceptsOnly0=true; this.prefixSize+=4; break;
    			case 248: acceptsOnly0=true; this.prefixSize+=5; break;
    			case 252: acceptsOnly0=true; this.prefixSize+=6; break;
    			case 254: acceptsOnly0=true; this.prefixSize+=7; break;
    			case 255: this.prefixSize+=8; break;
    			default: good=false;
    			}
    		}
    	}
    	str_mask = add.substring(0, add.length() - 1);
    	if (!good && validateIt){
    		throw new NetworkMaskFormatException();
    	}
    }
	
	public NetworkMask(byte[] a) throws NetworkMaskFormatException {
		this(a,false);
	}
	public String toString(){
		return str_mask;
	}
	public boolean equals(NetworkMask m){
		// could also  test prefixSize
		return str_mask.equals(m.toString());
	}
	/**
	 * @return
	 * @uml.property  name="prefixSize"
	 */
	public int getPrefixSize(){
		return this.prefixSize;
	}
}
