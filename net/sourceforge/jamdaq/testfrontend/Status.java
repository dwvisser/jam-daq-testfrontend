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
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Feb 15, 2004
 */
public class Status extends JPanel {
	private transient final JLabel label = new JLabel();

	static class Value {

		private static final String[] values = { "Booted", "Initialized",
				"Started", "Stopped" };

		private transient final String stringValue;

		private Value(int value) {
			this.stringValue = values[value];
		}

		public static final Value BOOTED = new Value(0);
		public static final Value INIT = new Value(1);
		public static final Value START = new Value(2);
		public static final Value STOP = new Value(3);

		public String toString() {
			return stringValue;
		}
	}

	public Status(Value init) {
		super();
		final Border border = BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED);
		setBorder(BorderFactory.createTitledBorder(border, "Status"));
		setValue(init);
		add(label);
	}

	public final void setValue(final Value value) {
		synchronized (label) {
			label.setText(value.toString());
		}
	}
}
