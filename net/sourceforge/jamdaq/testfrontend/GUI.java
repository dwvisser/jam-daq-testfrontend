package net.sourceforge.jamdaq.testfrontend;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 15, 2004
 */
public class GUI extends JFrame {
	final Status status=new Status(Status.Value.BOOTED);
	final Counter eventsMade=new Counter("Events Generated",0);
	final Counter eventsSent=new Counter("Events Sent",0);
	final Counter buffersSent=new Counter("Buffers Sent",0);
	final Container contents;
	final Console console=new Console();
	
	public GUI(){
		super("Test Front End for Jam");
		contents=getContentPane();
		contents.setLayout(new BorderLayout());
		JPanel center=new JPanel(new GridLayout(2,2));
		contents.add(center, BorderLayout.CENTER);
		center.add(status);
		center.add(eventsMade);
		center.add(buffersSent);
		center.add(eventsSent);
		contents.add(console,BorderLayout.SOUTH);
		final Runnable showWindow=new Runnable(){
			public void run(){ 
				pack();
				setSize(300,getHeight());
				show();
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}
	
	public static void main(String [] args){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			final String title="Test Front End for Jam--error setting GUI appearance";
			JOptionPane.showMessageDialog(null,e.getMessage(),title,
			JOptionPane.WARNING_MESSAGE);
		}
		new GUI();
	}
}
