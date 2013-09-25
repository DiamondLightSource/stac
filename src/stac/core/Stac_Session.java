package stac.core;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
//import javax.swing.border.*;
import java.io.*;

import Jama.*;
//import java2d.demos.Colors.Rotator3D.Objects3D.Matrix3D;
//import javax.swing.table.TableColumn;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.vecmath.*;

import stac.gui.BoxLayoutFixed;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


class JobControlWidget extends JPanel implements java.awt.event.ActionListener {
	
	Stac_JobManager jobManager;
	
	JLabel title = new JLabel();
	JButton abort   = new JButton ();
	
    Dimension rbSize = new Dimension(100,30);
    Dimension rbdSize = new Dimension(200,30);
    Dimension fsbSize = new Dimension(30,30);
    Dimension fsdbSize = new Dimension(60,30);
    Dimension tfSize = new Dimension(1000,30);
    Dimension tfnSize = new Dimension(10,30);
    Dimension slnSize = new Dimension(50,50);
    Dimension slxSize = new Dimension(1000,50);
    
    int errorLevel=0;
	
	public JobControlWidget(String titleStr,Stac_JobManager jobManager) {
		this.jobManager=jobManager;
		
		//gui
	    setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.LINE_AXIS));
	    //title
	    add(title,null);
	    title.setText(titleStr);
		title.setMinimumSize(rbSize);
		title.setMaximumSize(rbSize);
		title.setPreferredSize(rbSize);
		title.setToolTipText("Name of the Module");
		title.setEnabled(true);
		//Abort
		add(abort,null);
		abort.setText("READY");
		abort.setMinimumSize(rbdSize);
		abort.setMaximumSize(rbdSize);
		abort.setPreferredSize(rbdSize);
		abort.setToolTipText("Abort all the running jobs");
		abort.addActionListener(this);
		abort.setEnabled(false);
		//update
		int numOfJobs=jobManager.numOfJobs();
		if (numOfJobs!=0) {
			jobStarted(numOfJobs);
		}
	}
	
	void jobStarted(int numOfJobs){
		abort.setEnabled(true);
		abort.setText("ABORT: "+numOfJobs+" Jobs");
	}

	void jobsGone(){
		abort.setEnabled(false);		
		abort.setText("READY");
	}


	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==abort) {
			//aborting
			Stac_Out.println("USER ACTION: Button pressed ("+title.getText()+" - ABORT)");
			jobManager.stopJobs();
		} //rightclick...
	}
	
	
	
}

class JobControlWindow extends JDialog {
	boolean guiEnabled=false;
	Component gui=null;
	
	public JobControlWindow (Component gui) {
	    //this.setUndecorated(true);
		this.gui=gui;
		if (gui!=null)
			guiEnabled=true;
		//gui
	    getContentPane().setLayout(new BoxLayoutFixed(getContentPane(),BoxLayoutFixed.PAGE_AXIS));
	    JLabel lab=new JLabel("");
	    //getContentPane().add(lab);
	    getContentPane().add(lab);
	    lab.setMinimumSize(new Dimension(10,10));
	    lab.setMaximumSize(new Dimension(10,10));
	    lab.setPreferredSize(new Dimension(10,10));
	    //setModal(false);
	    this.pack();
	    this.setVisible(true);
	    setEnabled(guiEnabled);
	    this.setTitle("STAC- JOB CONTROL");
	}
	
	public void setGuiEnabled(boolean enable) {
		guiEnabled=enable;
	    this.setEnabled(guiEnabled);
	}
	
	public synchronized JobControlWidget addJobManager(Stac_JobManager jobManager) {
		JobControlWidget newcontrol= new JobControlWidget(jobManager.getClass().getSimpleName(),jobManager);
		getContentPane().add(newcontrol);
		newcontrol.setEnabled(true);
		this.pack();
		return newcontrol;
	}
	
}


public class Stac_Session extends Stac_JobDirector implements Stac_Out_logger{
	String sessionId;
	int messageCounter=0;
	boolean logging=true;
	DataOutputStream logOut=null;
	String workDir;
	String logDir;
	boolean verbose=false;
	PrintStream exec_log=null;
	String exec_log_fname=null;
	
	Component errorGUI=null;
	ParamTable MsgProcessingErrors;
	
//	Vector jobManagers=new Vector(0);
//	Vector jobControllers=new Vector(0);
	JobControlWindow jobWindow=null;
	
	boolean allowGUI=true;
	
	public void setAllowGUI(boolean flag) {
		allowGUI=flag;
	}
	
	public boolean getAllowGUI() {
		return allowGUI;
	}
	
	
	/**
	 * Creates a default Session that can throw Error Boxes to the screen 
	 * @param errorGUI
	 */
	public Stac_Session (Component errorGUI) {
		this();
		setErrorGUI(errorGUI);
	}
	
	/**
	 * default Session:
	 * logging (in the directory specified by {@code STAC_LOG_DIR}, or the current directory)
	 *
	 */
    public Stac_Session () {
    	this(true);    	
    }
    
    /**
     * Creates a new Session with controlled Logging feature.
     * @param logging
     */
    public Stac_Session(boolean logging) {
    	this(logging,null,null);
    }	
	
    /**
     * Creates a new Session with Logging in the specified Directory.
     * @param logdir
     */
    public Stac_Session(String logDir,String workDir) {
    	this(true,logDir,workDir);
    }
    
    private static final String STAC_LOG_DIR_PROPERTY_NAME = "STAC_LOG_DIR";        
    private static final String DEFAULT_LOG_DIR = "./";        
       
    private static final String STAC_WORK_DIR_PROPERTY_NAME = "STAC_WORK_DIR";        
    private static final String DEFAULT_WORK_DIR = "./";        
 
    public String getLogDir() {
      	return logDir;
    }
    
    public String getWorkDir() {
      	return workDir;
    }
    
    public Stac_Configuration gui_config;
    
    public String getNonEmptyProperty(String key, String defvalue) {
    	String ret=System.getProperty(key, defvalue);
    	if (ret.length()==0) {
    		ret=defvalue;
    	}
    	return ret;
    }
    
    /**
     * Creates a new Session with controlled Logging feature.
     * Logdir must be specified.
     * @param logging
     * @param logdir
     */
    public Stac_Session (boolean logging,String logDir,String workDir) {
    	this.logDir=(logDir!=null)?logDir:getNonEmptyProperty(STAC_LOG_DIR_PROPERTY_NAME, DEFAULT_LOG_DIR);
    	this.workDir=(workDir!=null)?workDir:getNonEmptyProperty(STAC_WORK_DIR_PROPERTY_NAME, DEFAULT_WORK_DIR);
    	//get hostname
    	try {
			sessionId=InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			sessionId="UnknownHost";
		}
    	//get time
		GregorianCalendar gc = new GregorianCalendar();
		Date actDate = gc.getTime();
		String actDateStr = new PrintfFormat("%04d_%02d_%02d_%02d_%02d_%02d.%03d").sprintf(new Object[] {
				new Integer(gc.get(GregorianCalendar.YEAR)),
				new Integer(gc.get(GregorianCalendar.MONTH)+1),
				new Integer(gc.get(GregorianCalendar.DAY_OF_MONTH)),
				new Integer(gc.get(GregorianCalendar.HOUR_OF_DAY)),
				new Integer(gc.get(GregorianCalendar.MINUTE)),
				new Integer(gc.get(GregorianCalendar.SECOND)),
				new Integer(gc.get(GregorianCalendar.MILLISECOND)),
				});		
		//sessionId=sessionId.concat(" - "+actDate.toString()+" - "+gc.get(GregorianCalendar.MILLISECOND));
		sessionId=sessionId.concat("_"+actDateStr);
		//licence: check the modification time of the config file and see if the current date is bigger than this and smaller than the limit  
		if (/*gc.after(config) &&*/	gc.after(new GregorianCalendar(2013,12,20))) {
			System.out.println("STAC Session could not be established! (too old version)");
			//System.exit(-100);
		}
		//logging
		if (logging) {
			//message logging
			try {
				String log_fname=getLogDir()+"STAC_LOG_"+sessionId;
				FileOutputStream logFile= new FileOutputStream(log_fname);
				logOut = new DataOutputStream(logFile);
			} catch (Exception e) {
				errorInLogging();
			}
		} else {
			this.logging=false;
		}
		//execution_logging
		try {
			exec_log_fname=getLogDir()+"STAC_EXEC_LOG_"+sessionId;
			FileOutputStream logFile= new FileOutputStream(exec_log_fname);
			exec_log = new PrintStream(logFile);
		} catch (Exception e) {
			Stac_Out.println("I/O Error: STAC Session ("+sessionId+") exec_logging disabled!");
			exec_log =null;
		}
		if (exec_log!=null){
			//Stac_Out.setOutput(verbose);
			Stac_Out.setStac_Session(this);
			this.setVerbose(verbose);
		}
		//Error Init
		MsgProcessingErrors=new ParamTable();		
		
		//guiconfig
		Stac_Configuration gui_config_selector=new Stac_Configuration("STAC_GUI");
		gui_config_selector.getConfiguration();
		gui_config=new Stac_Configuration("STAC_GUI_"+gui_config_selector.Descriptor.getConcatenatedStringValue("STAC_GUI_language"));
		gui_config.getConfiguration();
    }
        
    /**
     * If there is probelm with logging, it gives feedback and switches it out.
     *
     */
    public void errorInLogging() {
		Stac_Out.println("I/O Error: STAC Session ("+sessionId+") logging disabled!");
		logging=false;    	
    }
    
    /**
     * returns tha actual SessionID
     * @return
     */
    public String getSessionId() {
    	return sessionId;
    }
    

//    public void addReqHandler(Stac_ReqHandler jobManager) {
//    	jobManagers.addElement(jobManager);
//    	jobControllers.addElement(jobWindow.addJobManager(jobManager));
//    }
    
    
    
//    public void addModule() {
//    	
//    }
//    
//    public void getModule() {
//    	
//    }
    
    /**
     * It allocates a new message in the given Session.
     * It is automatically called, when a new message is created.
     * @return
     */
    public synchronized int getNewMsgId() {
    	//increase the counter
    	messageCounter++;
    	//return the new value
    	return messageCounter;
    }

    /**
     * It controlls if the output messages will appear on the screen.
     * If it is false, then the messages will not be displayed, but 
     * will end up in the EXEC_LOG file. 
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
    	this.verbose=verbose;
    		Stac_Out.setOutput(true);
    		Stac_Out.println("Screen output is "+((verbose)?"enabled.":"disabled."));
    		if (exec_log!=null) {
    			Stac_Out.println("The messages are still logged in "+exec_log_fname);
    		}
    	Stac_Out.setOutput(verbose);
    }
    
    /**
     * Prints a message directly to the EXEC_LOG file.
     * It is always called by the generic Message Printer (STAC_out).
     * @param log
     */
    public void print(String log) {
    	if (exec_log!=null){
    		exec_log.print(log);
    		exec_log.flush();
    	}
    }
    
    /**
     * makes the use of ERROR Dialogs available
     * and opens a JOB control window
     * @param errorGUI
     */
    public void setErrorGUI(Component errorGUI) {
    	this.errorGUI=errorGUI;
    	if (errorGUI!=null) {
    		jobWindow=new JobControlWindow(null);
    		setNewJobWindow(jobWindow);
        	jobWindow.setGuiEnabled(true);
    	}
    }
    
    /**
     * Appends an error message to the list of error mesages of a given Message request.
     * @param msgId
     * @param msg
     */
    public synchronized void addErrorMsg(int msgId,String msg) {
    	Vector msgList=MsgProcessingErrors.getValueList(new Integer(msgId).toString());
    	if (msgList==null)
    		msgList=new Vector(0);
    	msgList.addElement(msg);    	
    	MsgProcessingErrors.setValueList(new Integer(msgId).toString(),msgList);
    	generalErrorMsg(msg,true);
    }
    
    /**
     * Prints the list of errors for a given message.
     * If Dialogs are enabled, it thorws one.
     * It also makes this list empty after printing.
     * @param msgId
     */
    public synchronized void printErrorMsg(int msgId) {
    	Vector msg=MsgProcessingErrors.getValueList(new Integer(msgId).toString());
    	if (msg!=null && msg.size()>0) {
    		ArrayList msgList = new ArrayList(0);
    		msgList.add("Note the following:");
    		for(int i=0;i<msg.size();i++) {
    			msgList.add(" - "+(String)msg.elementAt(i));
    		}
    		errorMsg(msgList,false);
    	}
    	MsgProcessingErrors.removeOldValueList(new Integer(msgId).toString());
    }
    
    /**
     * Prints a complex error message.
     * If Dialogs are enabled, it thorws one for nonintermediate arrors.
     * @param msg
     */
    public void errorMsg(ArrayList msg,boolean intermediate) {
    	if (!msg.isEmpty()) {
    		Stac_Out.println("###"+((intermediate)?" INTERMEDIATE":"")+" SYSTEM MESSAGE ###");
    		for(int i=0;i<msg.size();i++) {
    			Stac_Out.println((String)msg.get(i));
    		}
    		Stac_Out.println("######################");
    		//report the error using utils.logging
    		//report the error to the screen
    		if (errorGUI!=null && !intermediate) {
    			JOptionPane.showMessageDialog(errorGUI, msg.toArray());
    		}    		
    	}
    }
    
    /**
     * Prints a simple error message.
     * If Dialogs are enabled, it thorws one.
     * @param msg
     */
    public void errorMsg(String msg) {
    	generalErrorMsg(msg,false);
    }

    /**
     * Prints a simple error message.
     * If Dialogs are enabled, it thorws one if not an intermediate message
     * @param msg
     * @param intermediate
     */
    public void generalErrorMsg(String msg,boolean intermediate) {
		ArrayList msgList = new ArrayList(0);
		msgList.add(msg);
		errorMsg(msgList,intermediate);
    }

	

//	public void jobQueueModified(Stac_JobManager jobManager,int numofActiveJobs) {
//			for(int i=0;i<jobManagers.size();i++)
//				if (((Stac_JobManager)jobManagers.elementAt(i)).equals((Object)jobManager)) {
//					if (numofActiveJobs==0) {
//						((JobControlWidget)(jobControllers.elementAt(i))).jobsGone();
//					} else {
//						((JobControlWidget)(jobControllers.elementAt(i))).jobStarted(numofActiveJobs);
//					}
//					break;
//				}			
//		
//	}
    
    
}


