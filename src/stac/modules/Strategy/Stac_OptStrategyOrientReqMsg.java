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


public class Stac_OptStrategyOrientReqMsg extends Stac_ReqMsg {
    String workDir;
    Vector3d datum;
    Vector orientations;
    StacBCM bcm;
    Util utils;	
	
    public Stac_OptStrategyOrientReqMsg (Stac_Session session,Stac_RespHandler respHandler) {
    	super(session,respHandler);
    }
    
    
    
    
	public void load(DataInputStream in) throws IOException {
		//loadMsgHeader(in);
	}

	public void save(DataOutputStream out) throws IOException {
		//saveMsgHeader(out);
	}

    public void loadMsgHeader(DataInputStream in) throws IOException {
    	super.loadMsgHeader(in);
    	String msgType=in.readUTF();
    	if (!msgType.equals("Stac_OptStrategyOrientReqMsg"))
    		throw new IOException("STAC Message Type Missmatch!");
    }
    
    public void saveMsgHeader(DataOutputStream out) throws IOException {
    	super.saveMsgHeader(out);
    	out.writeUTF("Stac_OptStrategyOrientReqMsg");
    }
	
    
}



