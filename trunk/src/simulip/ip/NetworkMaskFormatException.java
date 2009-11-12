/*
  <simulip : an IP and UDP simulator>
    Copyright (C) 2008  Emmanuel Nataf

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
   
   Created on: Nov 6, 2008  	
 */
package simulip.ip;

/**
 * @author andrey
 * @see simulip.ip.NetworkMask
 */
public class NetworkMaskFormatException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public NetworkMaskFormatException(){
		super();
	}
	
	public NetworkMaskFormatException(Throwable cause){
		super(cause);
	}
	
	public NetworkMaskFormatException(String msg){
		super(msg);
	}
	
}
