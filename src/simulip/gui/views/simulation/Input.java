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
import java.awt.event.*;
import java.text.MessageFormat;

import javax.swing.*;
import javax.swing.event.*;

import simulip.gui.Build;
import simulip.net.Application;

/**
 * @author nataf
 */
public class Input extends JFrame {

	private static final long serialVersionUID = 132413234;
	private Application appli;
	JTextArea in = new JTextArea(1, 30);
	private String cl;
	private Input input;
	IfDocListener ifdocl = new IfDocListener();

	public Input(Application a) {
		super();
		appli = a;
		addWindowListener(new InAdapter());
		setAlwaysOnTop(true);
		in.getDocument().addDocumentListener(ifdocl);
		input = this;
		add(in);
	}

	public synchronized void readed() {
		notify();
	}

	public synchronized String read(String prompt) {
		in.setText("");
		setVisible(true);
		setSize(getPreferredSize());
		doLayout();
		Point p = appli.getLocationOnScreen();
		setLocation(p);
		setTitle(prompt);
		in.setCaretPosition(0);
		try {
			wait();
		} catch (Exception e) {
		}
		setVisible(false);
		return cl.substring(0, cl.lastIndexOf('\n'));

	}

	public String read() {
		return this.read(MessageFormat.format(Build.messages
				.getString("appli_input"), appli.getAppliName()));
	}

	private class InAdapter extends java.awt.event.WindowAdapter {
		/**
		 * Close the frame when close button
		 */
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}
	}

	private class IfDocListener implements DocumentListener {

		public void changedUpdate(DocumentEvent de) {
			// nothing TODO ?
		}

		public void insertUpdate(DocumentEvent de) {
			try {
				cl = de.getDocument().getText(0, de.getDocument().getLength());
				if (cl.charAt(cl.length() - 1) == '\n') {
					input.readed();
				}
			} catch (Exception e) {
				// nothing TODO ?
			}
		}

		public void removeUpdate(DocumentEvent de) {
			// nothing TODO ?
		}
	}
}
