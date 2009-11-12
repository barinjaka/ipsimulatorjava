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
    	
package simulip.gui.views.simulation;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;

import javax.swing.*;

import simulip.gui.Build;
import simulip.net.*;

/**
 * @author  nataf
 */
public class Output extends JFrame {

	private static final  long serialVersionUID = 132413234;
	private Application appli;
	private boolean used = false;
	JTextArea out = new JTextArea();
	JScrollPane jsp = new JScrollPane(out);
	public Output(Application a){
		super();
		appli = a;
		add(jsp);
		out.setSize(new Dimension(300,300));
		setSize(out.getSize());
		addWindowListener(new OutAdapter());
		setAlwaysOnTop(true);
	}
	
	public void println(String s){
		if(!used){
			setVisible(true);
			doLayout();
			Point p = appli.getLocationOnScreen();
			setLocation(p);MessageFormat.format(Build.messages
					.getString("appli_input"), appli.getName());
			used = true;
		}
		out.append(s + "\n");
		out.setSize(out.getWidth(), out.getHeight() + 10);
		
	}
	
	private class OutAdapter extends java.awt.event.WindowAdapter {
		/**
		 * Close the frame when close button
		 */
		public void windowClosing(WindowEvent e) {
			used = false;
			e.getWindow().dispose();
		}
	}

}
