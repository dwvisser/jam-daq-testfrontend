package net.sourceforge.jamdaq.testfrontend;
import jam.global.MessageHandler;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Class Console displays a output of commands and error messages
 * and allows the input of commands using the keyboard.
 *
 * @author  Ken Swartz
 * @author Dale Visser
 * @version 1.2 alpha last edit 15 Feb 2000
 * @version 0.5 last edit 11-98
 * @version 0.5 last edit 1-99
 */
public class Console
	extends JPanel
	implements MessageHandler {

	final static int NUMBER_LINES_DISPLAY = 25;
	final static int NUMBER_LINES_LOG = 1000;

	/**
	 * End of line character(s).
	 */
	private String END_LINE = (String) System.getProperty("line.separator");

	private JTextPane textLog; //output text area
	private Document doc;
	private SimpleAttributeSet attr_normal, attr_warning, attr_error;
	private boolean newMessage;
	//Is the message a new one or a continuation of one
	private boolean msgLock;
	//a lock for message output so message dont overlap
	/**
	 * Private.
	 *
	 * @serial
	 */
	private int maxLines;
	private int numberLines; //number of lines in output

	/**
	 * Private.
	 *
	 * @serial
	 */
	private String logFileName; //name of file to which the log is written
	/**
	 * Private.
	 *
	 * @serial
	 */
	private BufferedWriter logFileWriter; //output stream
	/**
	 * Private.
	 *
	 * @serial
	 */
	private boolean logFileOn; //are we logging to a file
	/**
	 * Private.
	 *
	 * @serial
	 */
	private String messageFile; //message for file

	/**
	 * Create a JamConsole which has an text area for output
	 * a text field for intput.
	 */
	public Console() {
		this(NUMBER_LINES_LOG);
	}

	/**
	 *Constructor:
	 * Create a JamConsole which has an text area for output
	 * a text field for intput
	 */
	public Console(int linesLog) {
		maxLines = linesLog;
		this.setLayout(new BorderLayout(5, 5));
		textLog = new JTextPane();
		doc = textLog.getStyledDocument();
		attr_normal = new SimpleAttributeSet();
		attr_warning = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_warning, Color.blue);
		attr_error = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_error, Color.red);
		textLog.setEditable(false);
		JScrollPane jsp =
			new JScrollPane(
				textLog,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(jsp, BorderLayout.CENTER);
		newMessage = true;
		msgLock = false;
		numberLines = 1;
		logFileOn = false;
		this.setPreferredSize(new Dimension(800, 28+16*NUMBER_LINES_DISPLAY));

	}


	/**
	 * Outputs the string as a message to the console, which has more than one part,
	 * so message can continued by a subsequent call.
	 *
	 * @param _message the message to be output
	 * @param part one of NEW, CONTINUE, or END
	 */
	public synchronized void messageOut(String _message, int part) {
		String message=new String(_message);
		if (part == NEW) {
			msgLock = true;
			messageFile = getDate() + ">" + message;
			message = END_LINE + getTime() + ">" + message;
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				System.err.println(e);
			}
		} else if (part == CONTINUE) {
			messageFile = messageFile + message;
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				System.err.println(e);
			}
		} else if (part == END) {
			messageFile = messageFile + message + END_LINE;
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				System.err.println(e);
			}
			trimLog();
			textLog.setCaretPosition(doc.getLength());
			//if file logging on write to file
			if (logFileOn) {
				try {
					logFileWriter.write(messageFile, 0, messageFile.length());
					logFileWriter.flush();
				} catch (IOException ioe) {
					logFileOn = false;
					errorOutln("Unable to write to log file, logging turned off [JamConsole]");
				}
			}
			//unlock text area and notify others they can use it
			msgLock = false;
			notifyAll();
		} else {
			System.err.println("Error not a valid message part [JamConsole]");
		}
	}

	/**
	 * Output a message so it will be continued on the same line.
	 */
	public synchronized void messageOut(String message) {
		messageOut(message, CONTINUE);
	}

	/**
	 * Output a message with a carriage return.
	 *
	 * @param _message the message to be printed to the console
	 */
	public synchronized void messageOutln(String _message) {
		String message=new String(_message);
		msgLock = true;
		messageFile = getDate() + ">" + message + END_LINE;
		message = END_LINE + getTime() + ">" + message;
		try {
			doc.insertString(doc.getLength(), message, attr_normal);
		} catch (BadLocationException e) {
			System.err.println(e);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
		//if file logging on write to file
		if (logFileOn) {
			try {
				logFileWriter.write(messageFile, 0, messageFile.length());
				logFileWriter.flush();
			} catch (IOException ioe) {
				logFileOn = false;
				errorOutln("Unable to write to log file, logging turned off [JamConsole]");
			}
		}
		//unlock text area and notify others they can use it
		msgLock = false;
		notifyAll();
		newMessage = true;
	}

	/**
	 * Writes an error message to the console immediately.
	 */
	public void errorOutln(String message) {
		promptOutln("Error: " + message, attr_error);
	}

	/**
	 * Outputs a warning message to the console immediately.
	 */
	public void warningOutln(String message) {
		promptOutln("Warning: " + message, attr_warning);
	}

	private synchronized void promptOutln(final String _message, AttributeSet attr) {
		String message=new String(_message);
		/* Dont wait for lock.  
		 * Output message right away. */
		if (msgLock) { //if locked add extra returns
			messageFile = END_LINE + getDate() + ">" + message + END_LINE;
			message = END_LINE + getTime() + ">" + message + END_LINE;
		} else { //normal message
			messageFile = getDate() + ">" + message + END_LINE;
			message = END_LINE + getTime() + ">" + message;
		}
		try {
			doc.insertString(doc.getLength(), message, attr);
		} catch (BadLocationException e) {
			System.err.println(e);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
		/* beep */
		Toolkit.getDefaultToolkit().beep();
		if (logFileOn) { //if file logging on write to file
			try {
				logFileWriter.write(messageFile, 0, messageFile.length());
				logFileWriter.flush();
			} catch (IOException ioe) {
				logFileOn = false;
				errorOutln("Unable to write to log file, logging turned off [JamConsole]");
			}
		}
	}

	public static final String INTS_ONLY="int";
	

	/**
	 * Create a file for the log to be saved to.
	 * The method appends a number (starting at 1) to the file name
	 * if the file already exists.
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	public String setLogFileName(String name) {
		File file;
		String newName;
		int i;

		newName = name + ".log";
		file = new File(newName);
		//create a unique file, append a number if a log already exits.
		i = 1;
		while (file.exists()) {
			newName = name + i + ".log";
			file = new File(newName);
			i++;
		}
		//create a new logFileWriter
		try {
			logFileWriter = new BufferedWriter(new FileWriter(file));

		} catch (IOException ioe) {
			errorOutln("Not able to create log file " + newName);
		}
		logFileName = newName;
		return newName;
	}

	/**
	 * Close the log file
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	public void closeLogFile() {
		try {
			logFileWriter.flush();
			logFileWriter.close();
		} catch (IOException ioe) {
			errorOutln("Could not close log file  [JamConsole]");
		}
	}

	/**
	 * Turn on the logging to a file
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	public void setLogFileOn(boolean state) {
		if (logFileWriter != null) {
			logFileOn = state;
		} else {
			logFileOn = false;
			errorOutln("Cannot turn on logging to file, log file does not exits  [JamConsole]");
		}
	}

	/**
	 * Trim the text on screen Log so it does not get too long
	 */
	private void trimLog() {
		numberLines++;
		if (numberLines > maxLines) { //get rid of top line
			numberLines--;
			try{
				doc.remove(0,textLog.getText().indexOf(END_LINE)+END_LINE.length());
			} catch (BadLocationException ble){
				System.err.println(ble);
			}
		}
	}

	/**
	 * get the current time
	 */
	private String getTime() {
		Date date;
		DateFormat datef;
		String stime;

		date = new java.util.Date(); //get time
		datef = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		//medium time format
		datef.setTimeZone(TimeZone.getDefault()); //set time zone
		stime = datef.format(date); //format time
		return stime;
	}

	/**
	 * Get the current date and time
	 */
	private String getDate() {
		Date date; //date object
		DateFormat datef;
		String stime;

		date = new java.util.Date(); //get time
		datef = DateFormat.getDateTimeInstance(); //medium date time format
		datef.setTimeZone(TimeZone.getDefault()); //set time zone
		stime = datef.format(date); //format time
		return stime;
	}

	/**
	 * On a class destruction close log file
	 */
	protected void finalize() {
		try {
			if (logFileOn) {
				closeLogFile();
			}
		} catch (Exception e) {
			System.err.println(
				"Error closing log file in finalize [JamConsole]");
		}
	}
}
