package stac.modules.Alignment.plugins;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.modules.BCM.*;
import stac.modules.Alignment.*;

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

import java.util.StringTokenizer;
import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


public abstract class Stac_AlignmentPlugin_exact extends Stac_AlignmentPlugin_1by1 implements Stac_AlignmentPlugin {
	/*
	 * 
	 */
	
    public Stac_AlignmentPlugin_exact () {
    	
    }

    /**
     * 
     * @param datum (phi,kappa,omega) - omega will be set to 0
     * @param orientations - list of desired V1-V2 pairs, 
     * where V1 is a vector should be paralllel to the spindle,
     * and V2 is in the plane of the beam and spindle  
     * @param bcm - STAC BCM
     * @param utils - STAC utilities
     */
    public String CalculateSingleAlignment(Stac_Session session,int msgId,String indir,String outdir,Vector3d datum,Point3d trans,Vector orientation,StacBCM bcm,Util utils) {
	  		Vector neworientation=new Vector(0);
	  		neworientation.addElement(orientation.elementAt(0));
	  		neworientation.addElement(orientation.elementAt(1));
	  		String pos=CalculateExactAlignment(session,msgId,indir,outdir,datum,trans,neworientation,bcm,utils);
	  		double testAngle=0;
//	  		double startAngle=180;
//	  		double eps=1;
//	  		while (((Boolean)(orientation.elementAt(2))) && Math.abs(testAngle-startAngle)>eps && CheckSingleAlignment(pos)){
//	  			//get new hint if possible
//	  			testAngle=(startAngle+testAngle)/2.0;
//	  			//if there is no more hint => go out
//	  			//test it
//		  		pos=CalculateExactAlignment(session,msgId,indir,outdir,datum,trans,neworientation,bcm,utils);
//	  		}
			String v1=(String)neworientation.elementAt(0);
//			String v2=(String)neworientation.elementAt(1);
	  		//Vector3d target= new Vector3d();
			try {
				StringTokenizer substr=new StringTokenizer(v1);
				double h=0;
				double k=0;
				double l=0;
				boolean realVec=false;
				if (v1.indexOf("[")>=0) {
					realVec=true;
				}
				String hklstr=substr.nextToken(" ()[]");
				if (hklstr.indexOf("a*")>=0) {
					//v1 contains a special a*
					h=1;
				} else if (hklstr.indexOf("b*")>=0) {
					//v1 contains a special b*
					k=1;
				} else if (hklstr.indexOf("c*")>=0) {
					//v1 contains a special c*
					l=1;
				} else if (hklstr.indexOf("a")>=0) {
					//v1 contains a special a
					realVec=true;
					h=1;
				} else if (hklstr.indexOf("b")>=0) {
					//v1 contains a special b
					realVec=true;
					k=1;
				} else if (hklstr.indexOf("c")>=0) {
					//v1 contains a special c
					realVec=true;
					l=1;
				} else {
					//it must be a normal abc, or hkl value
					h=new Double(hklstr);
					hklstr=substr.nextToken(" ()[]");
					k=new Double(hklstr);
					hklstr=substr.nextToken(" ()[]");
					l=new Double(hklstr);
				}
				String sepChar="()";
				if (realVec)
					sepChar="[]";
				//target
				Vector3d hkl2c=new Vector3d(h,k,l);
				//vector independent to target
				Vector3d hkl2d=new Vector3d(utils.getIndependentVec(h,k,l));
				//vector1 orthogonal to target (we use the weighted combination on this and the target)
				Vector3d hkl2a=new Vector3d();
				hkl2a.cross(hkl2c,hkl2d);
				//vector2 orthogonal to target and vector1 (we use the weighted combination on this and the target)
				Vector3d hkl2b=new Vector3d();
				hkl2b.cross(hkl2c,hkl2a);
				
//				double [] hkl2a=utils.getIndependentVec(h,k,l);
//				double [] hkl2b=utils.getIndependentVec(hkl2a[0],hkl2a[1],hkl2a[2]); //rotated one
//				double [] hkl2;
				//(we use the weighted combination on this and the target)
				Vector3d hkl2=new Vector3d();
				//this will rotate away from the target v1 by testangle
				hkl2a=new Vector3d(hkl2c);
				double rotStep=30;
				double rot=-rotStep;
				boolean close=((Boolean)orientation.elementAt(2)).booleanValue();
	  		while (close && !CheckSingleAlignment(outdir+pos)){
	  			rot+=rotStep;
	  			
	  			
	  			//get new hint if possible
	  			if (rot<360 && testAngle!=0.0) {
	  				AxisAngle4d rot1=new AxisAngle4d(hkl2c,rot/180.0*Math.PI);
	  				Matrix3d rot1m=new Matrix3d(); rot1m.set(rot1);
	  				hkl2=new Vector3d(hkl2a);
	  				rot1m.transform(hkl2);
	  			} else {
	  				rot=0;
	  				testAngle+=5;
					//h2-k2-l2: new v1 target
	  				AxisAngle4d rot1=new AxisAngle4d(hkl2b,testAngle/180.0*Math.PI);
	  				Matrix3d rot1m=new Matrix3d(); rot1m.set(rot1);
	  				hkl2a=new Vector3d(hkl2c);
	  				rot1m.transform(hkl2a);
	  				hkl2=new Vector3d(hkl2a);
	  			}
//				double h2=(90-testAngle)*h+testAngle*hkl2[0];
//				double k2=(90-testAngle)*k+testAngle*hkl2[1];
//				double l2=(90-testAngle)*l+testAngle*hkl2[2];
//				double h2=(90-testAngle)*h+testAngle*hkl2.x;
//				double k2=(90-testAngle)*k+testAngle*hkl2.y;
//				double l2=(90-testAngle)*l+testAngle*hkl2.z;
				
	  			//do not check if v1(=hkl2) || v2
	  			//if ()
	  			//	continue;
				
	  			neworientation.setElementAt(sepChar.substring(0,1)+hkl2.x+" "+hkl2.y+" "+hkl2.z+sepChar.substring(1,2),0);
	  			//now we keep v2!
//				double [] hkl=utils.getIndependentVec(h2,k2,l2);
//	  			neworientation.setElementAt(sepChar.substring(0,1)+hkl[0]+" "+hkl[1]+" "+hkl[2]+sepChar.substring(1,2),1);
	  			//if there is no more hint => go out
	  			if (testAngle>=90)
	  				break;
	  			//test it
		  		pos=CalculateExactAlignment(session,msgId,indir,outdir,datum,trans,neworientation,bcm,utils);
	  		}
			}catch (Exception e) {
				session.addErrorMsg(msgId,"Alignment request could not be processed");
			}
	  	
		return pos;    	
    }
    

    public boolean CheckSingleAlignment(String file) {
    	boolean res=true;
    	Stac_Out.println("File to be read: " + file);

    	try {
    		FileInputStream alignInFile= new FileInputStream(file);
    		DataInputStream inFile = new DataInputStream(alignInFile);
    		try {
    			{
    				double v1x=inFile.readDouble();
    				double v1y=inFile.readDouble();
    				double v1z=inFile.readDouble();
    				double v2x=inFile.readDouble();
    				double v2y=inFile.readDouble();
    				double v2z=inFile.readDouble();
    				double omega=inFile.readDouble();
    				double kappa=inFile.readDouble();
    				double phi=inFile.readDouble();
    				String tcorr=inFile.readUTF();

    				double rank=inFile.readDouble();

    			}
    		} catch (RuntimeException e) {
    	    	Stac_Out.println("Problem with reading the file: " + file);
    			res=false;
    		}
    		inFile.close();
    		alignInFile.close();
    	} catch (Exception grfex) {
	    	Stac_Out.println("Problem with reading the file: " + file);
    		res=false;
    	}
    	return res;
    }

    /**
     * 
     * @param datum (phi,kappa,omega) - omega will be set to 0
     * @param orientations - list of desired V1-V2 pairs, 
     * where V1 is a vector should be paralllel to the spindle,
     * and V2 is in the plane of the beam and spindle  
     * @param bcm - STAC BCM
     * @param utils - STAC utilities
     */
    public abstract String CalculateExactAlignment(Stac_Session session,int msgId,String indir,String outdir,Vector3d datum,Point3d trans,Vector orientation,StacBCM bcm,Util utils);

	
}


