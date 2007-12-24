/*
 * Created on Feb 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sourceforge.jamdaq.testfrontend;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 15, 2004
 */
public class Counter extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient final JLabel number = new JLabel();
	private transient int val = 0;

	public Counter(String sname, int init) {
		super();
		final Border border = BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED);
		setBorder(BorderFactory.createTitledBorder(border, sname));
		setValue(init);
		add(number);
	}

	public final void setValue(final int value) {
		synchronized (number) {
			val = value;
			number.setText(String.valueOf(value));
		}
	}

	public final void increment() {
		synchronized (number) {
			val++;
			number.setText(String.valueOf(val));
		}
	}

	public final void reset() {
		setValue(0);
	}
}
