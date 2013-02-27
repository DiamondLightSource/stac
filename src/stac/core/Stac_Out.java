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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;

public class Stac_Out {
	static ArrayList<Stac_Out_logger> loggers=new ArrayList<Stac_Out_logger>();
	//static Stac_Session session=null;
	static boolean outEnabled=true;
	
    public Stac_Out (Stac_Session session) {
    	//this.session=session;
    	addLogger(session);
    }    
    
    public static void setStac_Session(Stac_Session mySession) {
    	//session=mySession;    	
    	addLogger(mySession);
    }
    
    public static void addLogger(Stac_Out_logger logger) {
    	loggers.add(logger);
    }
    
    public static void setOutput(boolean out) {
    	outEnabled=out;
    }
    
    public static void println(String log) {
    	print(log+"\n");
    }
    
    public static String getTimeStr() {
  	  GregorianCalendar gc = new GregorianCalendar();	  
  	  String actDateStr = new PrintfFormat("[%02d:%02d.%02d] ").sprintf(new Object[] {
  				//new Integer(gc.get(GregorianCalendar.YEAR)),
  				//new Integer(gc.get(GregorianCalendar.MONTH)+1),
  				//new Integer(gc.get(GregorianCalendar.DAY_OF_MONTH)),
  				new Integer(gc.get(GregorianCalendar.HOUR_OF_DAY)),
  				new Integer(gc.get(GregorianCalendar.MINUTE)),
  				new Integer(gc.get(GregorianCalendar.SECOND)),
  				//new Integer(gc.get(GregorianCalendar.MILLISECOND)),
  				});
  	  return actDateStr;
    }
    
    public static void printTimeln(String log) {
    	print(getTimeStr()+log+"\n");
    }

    public static synchronized void print(String log) {
    	if (outEnabled) {
    		System.out.print(log);
    	}
//    	if (session!=null) {
//    		session.print(log);
//    	}
    	for (int i=0;i<loggers.size();i++) {
    		loggers.get(i).print(log);
    	}
    }

    
}



