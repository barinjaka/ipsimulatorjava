package simulip.gui.views.build;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import simulip.gui.Build;
import simulip.gui.controller.NetworkController;
import simulip.gui.model.LanGraph;

public class LanName extends JFrame implements ActionListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9003612589785885525L;

	private LanGraph lan;
	private NetworkController controller;
	private String oldname;
	private JTextField jtfname;

	public LanName(NetworkController nc, LanGraph l) {
		lan = l;
		controller = nc;
		oldname = l.getName();
		jtfname = new JTextField(oldname);
		jtfname.addKeyListener(this);
		jtfname.setFocusable(true);
		
		addWindowListener(new NameWa());
		addKeyListener(this);
		add(BorderLayout.CENTER, jtfname);
		JButton ok = new JButton(Build.messages.getString("ok"));
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		ok.addKeyListener(this);
		add(BorderLayout.SOUTH, ok);

		doLayout();
		pack();
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent arg0) {
		String c = arg0.getActionCommand();
		if (c.equals("ok"))
			if (oldname.compareTo(jtfname.getText().trim()) != 0) {
				if (!jtfname.getText().trim().equals("")) {
					controller.updateLanName(lan, jtfname.getText().trim());
					Build.changed();
					dispose();
				}
			}
			else
				dispose(); // just a consultation of the name
	}

	private class NameWa extends WindowAdapter {
		/**
		 * Close the frame when close button
		 */
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}
	}

	public void keyPressed(KeyEvent arg0) {
		// nothing to do
		
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			ActionEvent ae = new ActionEvent(this,0,"ok");
			actionPerformed(ae);
		}
	}

	public void keyTyped(KeyEvent arg0) {
		// nothing to do
		
	}

}
