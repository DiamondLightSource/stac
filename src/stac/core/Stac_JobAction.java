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


public class Stac_JobAction extends Thread {
	Stac_Session session;
	Stac_ReqHandler handler;
	Stac_ReqMsg req;
	boolean killed=false;
	
	public Stac_JobAction(Stac_Session session,Stac_ReqHandler handler,Stac_ReqMsg req) {
    	this.session=session;
    	this.handler=handler;
    	this.req=req;
	}
	
	public void run() {
		
    	//log Request
    	if (session.logging)
			try {
				req.saveMsg(session.logOut);
			} catch (IOException e) {				
				session.errorInLogging();
			}
    	//process
    	Stac_RespMsg response;
    	response=handler.process(req,this);
    	//log Response
    	if (session.logging) {
			try {
				response.saveMsg(session.logOut);
			} catch (IOException e) {
				session.errorInLogging();
			}
    	}
    	if (!killed)
    		handler.generateJobResponse(this,response);
    	if (!killed)
    		session.printErrorMsg(req.msgId);
		handler.removeJob(this);
    	
	}
	
	public void setKilled() {
		killed=true;
	}
	
	
}

