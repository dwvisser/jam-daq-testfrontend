/*
 * Created on Feb 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sourceforge.jamdaq.testfrontend;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 15, 2004
 */
public class Counter extends JPanel {
	private final JLabel number=new JLabel();
	private int val=0;
	
	public Counter(String sname, int init){
		final Border b=BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		setBorder(BorderFactory.createTitledBorder(b,sname));
	 	setValue(init);
	 	add(number);
	}
	
	public final void setValue(int n){
		synchronized (number){
			val=n;
			number.setText(String.valueOf(n));
		}
	}
	
	public final void increment(){
		synchronized (number){
			val++;
			number.setText(String.valueOf(val));
		}
	}
	
	public final void reset(){
		setValue(0);
	}
}
