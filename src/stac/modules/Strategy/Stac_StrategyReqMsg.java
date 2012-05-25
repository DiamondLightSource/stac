package stac.modules.Strategy;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.modules.BCM.*;

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


public class Stac_StrategyReqMsg extends Stac_ReqMsg {
    String workDir;
    Vector3d datum;
    Vector orientations;
    String options;
    StacBCM bcm;
    Util utils;	
	
    public Stac_StrategyReqMsg (Stac_Session session,Stac_RespHandler respHandler) {
    	super(session,respHandler);
    }
    
    /**
     * use the automatic ReqMsg indir/outdir (based on the session settings)
     * @param session
     * @param respHandler
     * @param datum
     * @param orientations
     * @param bcm
     * @param utils
     */
    public Stac_StrategyReqMsg (Stac_Session session,Stac_RespHandler respHandler,
    		Vector3d datum,
    		Vector orientations,
    		String options,
    		StacBCM bcm,
    		Util utils) {
    	this(session,respHandler,"",datum,orientations,options,bcm,utils);
    }
    
    public Stac_StrategyReqMsg (Stac_Session session,Stac_RespHandler respHandler,
    		String workDir,
    		Vector3d datum,
    		Vector orientations,
    		String options,
    		StacBCM bcm,
    		Util utils) {
    	super(session,respHandler,workDir);
    	set_workDir(workDir);
    	set_datum(datum);
    	set_orienattions(orientations);
    	set_options(options);
    	set_bcm(bcm);
    	set_utils(utils);
    }
        

    public String get_workDir() {
    	return workDir;
    }
    public Vector3d get_datum() {
    	return datum;
    }
    public Vector get_orienattions() {
    	return orientations;
    }
    public String get_options() {
    	return options;
    }
    
    //only now:
    //normally the Moduls should automatically come from the session!
    public StacBCM get_bcm() {
    	return bcm;
    }
    public Util get_utils() {
    	return utils;
    }

    
    public void set_workDir(String workDir) {
    	this.workDir=workDir;
    }
    public void set_datum(Vector3d datum) {
    	this.datum=datum;
    }
    public void set_orienattions(Vector orientations) {
    	this.orientations=orientations;
    }
    public void set_options(String options) {
    	this.options=options;
    }
    
    //only now:
    //normally the Moduls should automatically come from the session!
    public void set_bcm(StacBCM bcm) {
    	this.bcm=bcm;
    }
    public void set_utils(Util utils) {
    	this.utils=utils;
    }
    
    
    
	public void load(DataInputStream in) throws IOException {
		//loadMsgHeader(in);
		workDir=in.readUTF();
		datum.x=in.readDouble();
		datum.y=in.readDouble();
		datum.z=in.readDouble();
		int oct=in.readInt();
		for(int i=0;i<oct;i+=2){
			Vector3d v3d= new Vector3d(in.readDouble(),in.readDouble(),in.readDouble());
			orientations.addElement(v3d);
			String comment=in.readUTF();
			orientations.addElement(comment);
		}
		options=in.readUTF();
	}

	public void save(DataOutputStream out) throws IOException {
		//saveMsgHeader(out);
		out.writeUTF(workDir);
		out.writeDouble(datum.x);
		out.writeDouble(datum.y);
		out.writeDouble(datum.z);
		out.writeInt(orientations.size());
		for(int i=0;i<orientations.size();i+=2){
			out.writeDouble(((Vector3d)orientations.elementAt(i)).x);
			out.writeDouble(((Vector3d)orientations.elementAt(i)).y);
			out.writeDouble(((Vector3d)orientations.elementAt(i)).z);
			out.writeUTF((String)orientations.elementAt(i+1));
		}
		out.writeUTF(options);
		out.writeChars(options);
	}

    public void loadMsgHeader(DataInputStream in) throws IOException {
    	super.loadMsgHeader(in);
    	String msgType=in.readUTF();
    	if (!msgType.equals("Stac_StrategyReqMsg"))
    		throw new IOException("STAC Message Type Missmatch!");
    }
    
    public void saveMsgHeader(DataOutputStream out) throws IOException {
    	super.saveMsgHeader(out);
    	out.writeUTF("Stac_StrategyReqMsg");
    }
	
    
}



