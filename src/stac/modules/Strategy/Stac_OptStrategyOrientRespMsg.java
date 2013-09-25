package stac.modules.Strategy;

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


public class Stac_OptStrategyOrientRespMsg extends Stac_RespMsg {
	Vector OA= new Vector(0);
	
    public Stac_OptStrategyOrientRespMsg (Stac_Session session,Stac_ReqMsg req) {
    	super(session,req);    	
    }

    public void set_optAlign(Vector OA) {
    	this.OA=OA;
    }
    public Vector get_optAlign() {
    	return OA;
    }

	protected void load(DataInputStream in) throws IOException {
    	//possibleDatumsFile=in.readUTF();
	}

	protected void save(DataOutputStream out) throws IOException {
    	//out.writeUTF(possibleDatumsFile);
	}
    

    
}



