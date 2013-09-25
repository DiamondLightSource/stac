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


public abstract class Stac_ReqMsg extends Stac_Msg {
	Stac_RespHandler respHandler;
	public String indir="./";
	public String outdir="./";
	
    public Stac_ReqMsg (Stac_Session session,Stac_RespHandler respHandler) {
    	this(session,respHandler,"");
    }
    
    public Stac_ReqMsg (Stac_Session session,Stac_RespHandler respHandler,String workDir) {
    	super(session);
    	this.respHandler=respHandler;
    	updateReqMsg(workDir);
    }
    
    public void updateMsg() {
    	updateReqMsg("");
    }
    
    public void updateReqMsg(String workDir) {
    	if (workDir!=null && workDir.length()>0) {
    		indir=workDir;
    		outdir=workDir;
    	} else {
    		indir=session.workDir;
    		outdir=session.workDir+"STAC_OUT_"+session.sessionId+"/"+this.msgId+"_"+this.getClass().getSimpleName()+"/";
    		File oDir= new File(outdir);
    		boolean stat=oDir.isDirectory();
    		if (!stat) {
    			try {
					oDir.delete();
				} catch (RuntimeException e) {
				}
    			stat=oDir.mkdirs();
    		}
    		if (!stat) {
    			session.addErrorMsg(msgId,"Could not create the output dir: "+outdir+"\n Use '.' instead.");
    			outdir="./";
    		}
    		//File iDir = new File(indir);
    		indir=new File(indir).getAbsolutePath()+"/";
    		outdir=new File(outdir).getAbsolutePath()+"/";
    	}
    }    

    /**
     * loads extendable message header
     * @param in
     * @throws IOException
     */
    public void loadMsgHeader(DataInputStream in) throws IOException {
    	super.loadMsgHeader(in);
    	String respHandlerName=in.readUTF();
    	try {
			respHandler=(Stac_RespHandler) Class.forName(respHandlerName).newInstance();
		} catch (InstantiationException e) {
			throw new IOException("InstantiationException");
		} catch (IllegalAccessException e) {
			throw new IOException("IllegalAccessException");
		} catch (ClassNotFoundException e) {
			throw new IOException("ClassNotFoundException");
		}
		indir=in.readUTF();
		outdir=in.readUTF();
    }
    
    /**
     * stores extendable message header
     * @param out
     * @throws IOException
     */
    public void saveMsgHeader(DataOutputStream out) throws IOException {
    	super.saveMsgHeader(out);
    	out.writeUTF(respHandler.getClass().getSimpleName());
    	out.writeUTF(indir);
    	out.writeUTF(outdir);
    }
        
    
}



