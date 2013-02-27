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


public abstract class Stac_RespMsg extends Stac_Msg {
	public final int OK=0;
	public final int WARNINGS=1;
	public final int ERROR=2;
	public final int UNKNOWN=3;
	public final String[] statusName={"OK","WARNING","ERROR","UNKNOWN"};
	
	public int status=UNKNOWN;
	public int requestId=0;
	public Stac_ReqMsg req=null;
	
    public Stac_RespMsg (Stac_Session session,Stac_ReqMsg req) {
    	super(session);
    	requestId=req.msgId;
    	this.req=req;
    }    

    public void set_requestId(int id) {
    	requestId=id;
    }
    
    public int get_requestId() {
    	return requestId;
    }
    
    public void loadMsgHeader(DataInputStream in) throws IOException {
    	super.loadMsgHeader(in);
    	requestId=in.readInt();
    	status=in.readInt();
    	//req.loadMsg(in);
    }
    
    public void saveMsgHeader(DataOutputStream out) throws IOException {
    	super.saveMsgHeader(out);
    	out.writeInt(requestId);
    	out.writeInt(status);
    	//req.saveMsg(out);
    }
    
    public String statusToString() {
    	return statusName[status];
    }
    
    public void updateMsg(){}
}



