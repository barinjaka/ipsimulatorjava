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

package simulip.util;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import java.text.MessageFormat;

/**
 * A simple window to display logger records. The records are formatted using  {@link java.text.MessageFormat.format} . {0} handles class name, {1} handles method name, {2} handle raw message, {3} handle possible throwable. A defaut value the le formatting string is given by a constructor with minimum parameters. Notes:  - one can play with  {@link java.util.logging.Logger.setUseParentHandlers}  or  {@link java.util.logging.Logger.removeHandlers}   to avoid multiple display (by example suppress default log on standard error). - This class adds its own  {@link java.util.logging.Handler}  and removes it when window is closed.
 * @author  andrey
 */
public class LoggerFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private Logger lg=null;	
	/**
	 * @uml.property  name="lt"
	 * @uml.associationEnd  
	 */
	private LoggerText lt=null;
	private Handler h=null;
	// See MesssageFormat
	private String messageFormat;

	public LoggerFrame(String windowTitle, Logger pLg, String mf, int r, int c){
		super(windowTitle);
		this.messageFormat=mf;
		this.lg=pLg;
	        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

//		lg.setUseParentHandlers(false); // User should change logger before building LoggerFrame 
		this.addWindowListener(
				       new WindowListener(){
					   // see also : WindowAdapter
					   public void windowActivated(WindowEvent e) {}
					   public void windowClosed(WindowEvent e) {
					       if (lg!=null){
					    	   lg.removeHandler(h);
					    	   //	Logger.getLogger("").info("Handler removed");
					       }
					   }
					   public void windowClosing(WindowEvent e){}
					   public void windowDeactivated(WindowEvent e){}
					   public void windowDeiconified(WindowEvent e){}
					   public void windowIconified(WindowEvent e){}
					   public void windowOpened(WindowEvent e){}
				       });
	        //Add contents to the window.
		 	lt=new LoggerText(r,c);
		 	add(lt);
	        //Display the window.
	        pack();
	        setVisible(true);
	    }

	    public LoggerFrame(String windowTitle, Logger pLg){
	    	this(windowTitle, pLg, "{0}.{1}: {2} -- {3}", 20, 80);
	    }
	   
	    private class LoggerText extends JPanel  {
			private static final long serialVersionUID = 1L;
			protected JTextArea textArea;
			public LoggerText(int r, int c) {
				super(new GridBagLayout());
				textArea = new JTextArea(r,c);
				textArea.setEditable(false);
				textArea.setFont(new Font("Courier New", Font.PLAIN, 12)); // Externalize in resources file ?
				JScrollPane scrollPane = new JScrollPane(textArea);

				//Add Components to this panel.
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridwidth = GridBagConstraints.REMAINDER;

				//c.fill = GridBagConstraints.HORIZONTAL;
				//add(textField, c);

				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				add(scrollPane, gbc);

				h=new Handler(){
					public void close(){}
					public void flush(){}
					synchronized public void publish(LogRecord pr){
						final LogRecord r=pr; // final member for inner anonymous Runnable class
						try {
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								String cn = r.getSourceClassName();
								String mn = r.getSourceMethodName();
								String m = r.getMessage();
								Throwable t=r.getThrown();
								textArea.append(
								MessageFormat.format(
										messageFormat,
										(cn!=null?cn:""), // {0}
										(mn!=null?mn:""), // {1}
										(m!=null?m:""),   // {2}
										(t!=null?t.toString():""))+"\n"); // {3}
										
								//textArea.append(text);
								//Make sure the new text is visible, even if there
								//was a selection in the text area.
								textArea.setCaretPosition(textArea.getDocument().getLength());
							}
						});
						}catch (Throwable t){
							System.err.println("Trouble in LoggerFrame !!!" +t); //no log ! to avoid storm is LoggerFrame display default logger...
						}
					}
				};
		    lg.addHandler(h);
			}
	    }
}
