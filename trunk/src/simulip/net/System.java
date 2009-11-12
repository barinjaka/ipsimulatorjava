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

import simulip.gui.views.simulation.Input;
import simulip.gui.views.simulation.Output;
/**
 * Class for application terminal
 * @author  Emmanuel Nataf
 */
public class System {
	
	/**
	 * @uml.property  name="out"
	 * @uml.associationEnd  
	 */
	public Output out;
	/**
	 * @uml.property  name="in"
	 * @uml.associationEnd  
	 */
	public Input in;
	/**
	 * @uml.property  name="appli"
	 * @uml.associationEnd  
	 */
	private Application appli;
	
	public System(Application a){
		appli = a;
		out = new Output(appli);
		in = new Input(appli);
	}

}
