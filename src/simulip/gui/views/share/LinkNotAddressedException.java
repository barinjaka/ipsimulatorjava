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
    	
package simulip.gui.views.share;

/**
 * @author  nataf
 */
public class LinkNotAddressedException extends Exception {

	static final long serialVersionUID = 1232343454;
	
	/**
	 * @uml.property  name="end0"
	 */
	private String end0;

	/**
	 * @uml.property  name="end1"
	 */
	private String end1;
	
	public LinkNotAddressedException(String e0, String e1){
		super();
		end0 = e0;
		end1 = e1;
	}
	
	/**
	 * @return
	 * @uml.property  name="end0"
	 */
	public String getEnd0(){
		return new String(end0);
	}
	/**
	 * @return
	 * @uml.property  name="end1"
	 */
	public String getEnd1(){
		return new String(end1);
	}

}