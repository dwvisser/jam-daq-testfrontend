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
public class Status extends JPanel {
	private final JLabel label=new JLabel();
	
	static class Value{
		
		private static final String [] values = {"Booted", "Initialized", 
"Started", "Stopped"};

		private final String value;
		
		private Value(int n){
			value=values[n];
		}
		
		public static final Value BOOTED=new Value(0);
		public static final Value INIT=new Value(1);
		public static final Value START=new Value(2);
		public static final Value STOP=new Value(3);
		
		public String toString(){
			return value;
		}
	}
	
	public Status(Value init){
		final Border b=BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		setBorder(BorderFactory.createTitledBorder(b,"Status"));
	 	setValue(init);
	 	add(label);
	}
	
	public final void setValue(Value v){
		synchronized (label){
			label.setText(v.toString());
		}
	}
}
