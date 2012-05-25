package stac.core;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.border.*;
import java.io.*;

import Jama.*;
//import java2d.demos.Colors.Rotator3D.Objects3D.Matrix3D;
//import javax.swing.table.TableColumn;
import javax.swing.table.AbstractTableModel;
import javax.vecmath.*;

import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;

/**
 * JobManager being registered in STAC_session
 * It receives Requests and handles them by calling the final class' process method
 */
public abstract class Stac_ReqHandler extends Stac_JobManager {
	protected Stac_Session session;
	boolean allowGUI;
	
    public Stac_ReqHandler (Stac_Session session) {
    	super(session);
    	this.session=session;    	
    	session.addReqHandler(this);
    	allowGUI=session.getAllowGUI();
    }    

    /**
     * implementation of handling the request
     * this method is called by the separate JobAction thread
     * therefore all variables used must be local
     * @param req
     * @return
     */
    protected abstract Stac_RespMsg process(Stac_ReqMsg req,Stac_JobAction job);
    
    public void handle(Stac_ReqMsg req) {
    	Stac_JobAction job= new Stac_JobAction(session,this,req);
		if (!addJob(job)){
			//if job was not accepted:
			session.addErrorMsg(req.msgId, "Request is rejected by ReqHandler ("+this.getClass().getSimpleName()+")");
			session.printErrorMsg(req.msgId);
	    	return;
		}		
    	job.start();
    	
    }
    
    //run() {
    	
    //}
    
    public void addErrorMsg(int msgId, String msg) {
    	session.addErrorMsg(msgId,msg);
    }
    public void addErrorMsg(int msgId) {
    	session.printErrorMsg(msgId);
    }
    public void errorMsg(String msg) {
    	session.errorMsg(msg);
    }
    public Stac_Session getSession() {
    	return session;
    }

}



