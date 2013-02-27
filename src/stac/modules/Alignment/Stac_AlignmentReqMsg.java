package stac.modules.Alignment;

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

public class Stac_AlignmentReqMsg extends Stac_ReqMsg {
    Vector3d datum;
    Point3d trans;
    Vector orientations;
    StacBCM bcm;
    Util utils;	
	
    public Stac_AlignmentReqMsg (Stac_Session session,Stac_RespHandler respHandler) {
    	super(session,respHandler);
    }
    
    public Stac_AlignmentReqMsg (Stac_Session session,Stac_RespHandler respHandler,
    		Vector3d datum,
    		Point3d trans,
    		Vector orientations,
    		StacBCM bcm,
    		Util utils) {
    	this(session,respHandler,"",datum,trans,orientations,bcm,utils);
    }    
    
    public Stac_AlignmentReqMsg (Stac_Session session,Stac_RespHandler respHandler,
    		String workdir,
    		Vector3d datum,
    		Point3d trans,
    		Vector orientations,
    		StacBCM bcm,
    		Util utils) {
    	super(session,respHandler,workdir);
    	set_datum(datum);
    	set_trans(trans);
    	set_orienattions(orientations);
    	set_bcm(bcm);
    	set_utils(utils);
    }
        

    public Vector3d get_datum() {
    	return datum;
    }
    public Point3d get_trans() {
    	return trans;
    }
    public Vector get_orienattions() {
    	return orientations;
    }
    
    //only now:
    //normally the Moduls should automatically come from the session!
    public StacBCM get_bcm() {
    	return bcm;
    }
    public Util get_utils() {
    	return utils;
    }

    
    public void set_datum(Vector3d datum) {
    	this.datum=datum;
    }
    public void set_trans(Point3d trans) {
    	this.trans=trans;
    }
    public void set_orienattions(Vector orientations) {
    	this.orientations=orientations;
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
		datum.x=in.readDouble();
		datum.y=in.readDouble();
		datum.z=in.readDouble();
		trans.x=in.readDouble();
		trans.y=in.readDouble();
		trans.z=in.readDouble();
		int oct=in.readInt();
		for(int i=0;i<oct;i++){
			int ect=in.readInt();
			Vector orient= new Vector(0);
			for(int j=0;j<ect;j++){
				try {
					String cl=in.readUTF();
					Object obj=Class.forName(cl).newInstance();
					String val=in.readUTF();
					if (obj instanceof String) {
						obj = val;
					} else if (obj instanceof Boolean) {
						if (Boolean.TRUE.toString().equals(val)) {
							obj=true;
						} else {
							obj=false;
						}
					}
					orient.addElement(obj);
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			orientations.addElement(orient);
		}
	}

	public void save(DataOutputStream out) throws IOException {
		//saveMsgHeader(out);
		out.writeDouble(datum.x);
		out.writeDouble(datum.y);
		out.writeDouble(datum.z);
		out.writeDouble(trans.x);
		out.writeDouble(trans.y);
		out.writeDouble(trans.z);
		out.writeInt(orientations.size());
		for(int i=0;i<orientations.size();i++){
			Vector orient=(Vector)orientations.elementAt(i);
			out.writeInt(orient.size());
			for(int j=0;j<orient.size();j++){
				out.writeUTF(orient.elementAt(j).getClass().getName());
				out.writeUTF(orient.elementAt(j).toString());
			}
		}
	}

    public void loadMsgHeader(DataInputStream in) throws IOException {
    	super.loadMsgHeader(in);
    	String msgType=in.readUTF();
    	if (!msgType.equals("Stac_AlignmentReqMsg"))
    		throw new IOException("STAC Message Type Missmatch!");
    }
    
    public void saveMsgHeader(DataOutputStream out) throws IOException {
    	super.saveMsgHeader(out);
    	out.writeUTF("Stac_AlignmentReqMsg");
    }
	
    
}



