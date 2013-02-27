package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;

import stac.core.*;
import stac.gui.*;
import stac.modules.BCM.*;

import java.net.*;
import java.util.*;


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

import com.sun.j3d.utils.behaviors.vp.WandViewBehavior.ResetViewListener;

import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;



/**
 * 
 * esrf: BCM plugin for spec at ESRF
 * It is using the spec user values ONLY, but gives a base class for
 * all further spec plugins
 * NOTE: that this plugin is SETting the user values instead of managing
 * offsets between the spec and STAC values   
 *
 * @author sudol
 *
 */

public class Stac_BCMplugin_base extends Stac_Plugin implements Stac_BCMplugin {
	
	int ct=0;
	ParamTable motorListeners = new ParamTable();
	Util utils=new Util();
	
	private boolean fileupdate () {
		return true;
	}

	public double getMotorPosition(String motorName) throws Exception {
		// TODO Auto-generated method stub
		return 11+ct;
	}

	public void setMotorPosition(String motorName, double newValue) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void moveMotor(String motorName, double newValue) throws Exception {
		// TODO Auto-generated method stub
		ct++;
		
	}

	public double getMotorSpeed(String motorName) throws Exception {
		// TODO Auto-generated method stub
		return 10;
	}

	public double getMotorSpeedLowLimit(String motorName) throws Exception {
		// TODO Auto-generated method stub
		return 1;
	}

	public void setMotorSpeed(String motorName, double newValue) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public ParamTable getMotorParams(String motorName) throws Exception {
		// TODO Auto-generated method stub
    	ParamTable params=new ParamTable();
    	double minpos=0;
    	double maxpos="Kappa".equalsIgnoreCase(motorName)?240:0;
		params.setSingleDoubleValue("pos",getMotorPosition(motorName));
		params.setSingleDoubleValue("minpos",Math.min(minpos,maxpos));
		params.setSingleDoubleValue("maxpos",Math.max(minpos,maxpos));
		params.setSingleDoubleValue("speed",getMotorSpeed(motorName));
		params.setSingleDoubleValue("minspeed",getMotorSpeedLowLimit(motorName));
    	return params;
	}

	public void moveMotors(ParamTable newPositions) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void moveMotorsSyncronized(ParamTable newPositions) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void initMotors() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public double loadMotorPosition(String motorName, String data) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String saveMotorPositions(ParamTable positions, String data) {
		// TODO Auto-generated method stub
		return data;
	}

	/** 
	 * check basic motor limits
	 */
	public boolean checkDatumTrans(ParamTable BCM_Descriptor,Point3d dat, Point3d trans) {
    	boolean valid=true;
    	
		String motorName[] = new String [] {"Omega","Kappa","Phi","X","Y","Z"};
		double motorPos[] = new double[] {dat.x,dat.y,dat.z,trans.x,trans.y,trans.z};
		for(int i=0;valid && i<motorName.length;i++){
    		ParamTable params;
    		double min=0;
    		double max=0;
    		double pos=motorPos[i];
			try {
				params = getMotorParams(motorName[i]);
				min=params.getFirstDoubleValue("minpos");
				max=params.getFirstDoubleValue("maxpos");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			if (min==max)
				//free movement
				continue;
			if (i<3) {
				//rotation
				pos=utils.angleInRange(pos,min);
				if (pos>max)
					valid=false;
			} else {
				//translation
				if (pos<min || pos>max)
					valid=false;
			}
		}

    	return valid;
    	
	}

	public double convertMotorPosition_Plugin2Stac(String motorName, double pluginPos) {
		// TODO Auto-generated method stub
		return pluginPos;
	}

	public double convertMotorPosition_Stac2Plugin(String motorName, double stacPos) {
		// TODO Auto-generated method stub
		return stacPos;
	}
	
//    /**
//     * Interprets a line of the specdef file, and actualize the Descriptor 
//     * @param specline
//     * @param Descr
//     */
//    public void interpret_specdef(String specline,ParamTable Descr) {
//    	if (specline.startsWith("#",0))
//    		return;
//    	String tmp2[] = specline.split("\\s+");
//    	if(tmp2.length==0)
//    		return;
//    	int offset=0;
//    	if (tmp2[0].length()==0)
//    		offset++;
//		String name = tmp2[0+offset];
//		if (name.equals("BCM_Plugin")) {
//			String value =tmp2[1+offset];
//			Descr.setSingleValue(name,value);
//		} else {
//			String value =tmp2[1+offset];
//			for(int i=2+offset;i<tmp2.length;i++)
//				value+=" "+tmp2[i];
//			Descr.setSingleValue(name,value);
//		}
//		
//    }
    
//  /**
//  * Reads the BCM configuration file.
//  * @param specfile  - name of the file
//  * @param Descr - config parameters
//  */
// public void read_specdef(String specfile,ParamTable Descr) {
//   	String corr;
//   	Util utils=new Util();
//     //read spec.dat
//   	corr = utils.opReadCl(specfile);
//     String tmp1[] = corr.split("\n");
//     for (int l=0;l<tmp1.length;l++) {
//     	interpret_specdef(tmp1[l],Descr);
//     }
//   	
//   }
	
//  /**
//  * Reads the BCM configuration file.
//  * @param specfile  - name of the file
//  * @param Descr - config parameters
//  */
// public void read_specdef(String specfile,ParamTable Descr) {
//   	String corr;
//   	Util utils=new Util();
//     //read spec.dat
//   	utils.read_section(specfile, getName(), Descr);
//   	
//   }

	
//    /**
//     * Updates the BCM specdef file. 
//     * @param newParams: KEYWORD and te new parameters to be written
//     */
//    public void update_specdef(ParamTable BCM_Descriptor,ParamTable newParams) {
//    	try {
//    			String corr;
//    			Util utils=new Util();
//    			//read spec.dat
//    			corr = utils.opReadCl(System.getProperty("BCMDEF"));
//    			FileWriter newFile= new FileWriter(System.getProperty("BCMDEF"));
//    			String tmp1[] = corr.split("\n");
//    			Vector paramVal;
//    			for (int l=0;l<tmp1.length;l++) {
//    				if (tmp1[l].startsWith("#",0)) {
//    					newFile.write(tmp1[l]+"\n");
//    					continue;
//    				}
//    				String tmp2[] = tmp1[l].split("\\s+");
//    				if(tmp2.length==0) {
//    					newFile.write(tmp1[l]+"\n");
//    					continue;        	
//    				}
//    				int offset=0;
//    				if (tmp2[0].length()==0)
//    					offset++;
//    				String name = tmp2[0+offset];
//    				paramVal=newParams.getValueList(name);
//    				if (paramVal!=null) {
////    					newFile.write("# "+tmp1[l]+"\n");
//    					String actParam = "";
//    					newFile.write(name);
//    					for(int li=0;li<paramVal.size();li++) actParam+=" "+paramVal.elementAt(li);
//    					newFile.write(actParam);
//    					newFile.write("\n");
//    					interpret_specdef(name+actParam,BCM_Descriptor);
//    					newParams.removeOldValueList(name);
//    				} else {
//    					newFile.write(tmp1[l]+"\n");
//    				}
//    			}
//    			for(int i=0;i<newParams.pnames.size();i++) {
//					String actParam = "";
//					newFile.write(""+newParams.pnames.elementAt(i));
//					for(int li=0;li<((Vector)newParams.pvalues.elementAt(i)).size();li++) actParam+=" "+((Vector)newParams.pvalues.elementAt(i)).elementAt(li);
//					newFile.write(actParam);
//					newFile.write("\n");
//					interpret_specdef(""+newParams.pnames.elementAt(i)+actParam,BCM_Descriptor);
//    			}
//    			newFile.close();
//    	} catch (Exception e) {
//    	}
//    }
    


    /**
     * serves the current Calibration stored in BCM config
     * @param okp
     */
    public void getCalibration(ParamTable BCM_Descriptor,Vector3d okp[]) {
        for (int l=0;l<7;l++) okp[l] = new Vector3d();
//    	String data[]=BCM_Descriptor.getFirstStringValue("OmegaRot").split("\\s+");
//    	okp[0].set(new Double(data[0]).doubleValue(),new Double(data[1]).doubleValue(),new Double(data[2]).doubleValue());
//    	data=BCM_Descriptor.getFirstStringValue("KappaRot").split("\\s+");
//    	okp[1].set(new Double(data[0]).doubleValue(),new Double(data[1]).doubleValue(),new Double(data[2]).doubleValue());
//    	data=BCM_Descriptor.getFirstStringValue("PhiRot").split("\\s+");
//    	okp[2].set(new Double(data[0]).doubleValue(),new Double(data[1]).doubleValue(),new Double(data[2]).doubleValue());
//    	data=BCM_Descriptor.getFirstStringValue("KappaTrans").split("\\s+");
//    	okp[3].set(new Double(data[0]).doubleValue(),new Double(data[1]).doubleValue(),new Double(data[2]).doubleValue());
//    	data=BCM_Descriptor.getFirstStringValue("PhiTrans").split("\\s+");
//    	okp[4].set(new Double(data[0]).doubleValue(),new Double(data[1]).doubleValue(),new Double(data[2]).doubleValue());
//    	data=BCM_Descriptor.getFirstStringValue("KappaTransD").split("\\s+");
//    	okp[5].set(new Double(data[0]).doubleValue(),new Double(data[1]).doubleValue(),new Double(data[2]).doubleValue());
//    	data=BCM_Descriptor.getFirstStringValue("PhiTransD").split("\\s+");
//    	okp[6].set(new Double(data[0]).doubleValue(),new Double(data[1]).doubleValue(),new Double(data[2]).doubleValue());
    	Vector data=BCM_Descriptor.getValueList("OmegaRot");//BCM_Descriptor.getFirstStringValue("OmegaRot").split("\\s+");
    	okp[0].set(new Double((String)data.elementAt(0)).doubleValue(),new Double((String)data.elementAt(1)).doubleValue(),new Double((String)data.elementAt(2)).doubleValue());
    	data=BCM_Descriptor.getValueList("KappaRot");
    	okp[1].set(new Double((String)data.elementAt(0)).doubleValue(),new Double((String)data.elementAt(1)).doubleValue(),new Double((String)data.elementAt(2)).doubleValue());
    	data=BCM_Descriptor.getValueList("PhiRot");
    	okp[2].set(new Double((String)data.elementAt(0)).doubleValue(),new Double((String)data.elementAt(1)).doubleValue(),new Double((String)data.elementAt(2)).doubleValue());
    	data=BCM_Descriptor.getValueList("KappaTrans");
    	okp[3].set(new Double((String)data.elementAt(0)).doubleValue(),new Double((String)data.elementAt(1)).doubleValue(),new Double((String)data.elementAt(2)).doubleValue());
    	data=BCM_Descriptor.getValueList("PhiTrans");
    	okp[4].set(new Double((String)data.elementAt(0)).doubleValue(),new Double((String)data.elementAt(1)).doubleValue(),new Double((String)data.elementAt(2)).doubleValue());
    	data=BCM_Descriptor.getValueList("KappaTransD");
    	okp[5].set(new Double((String)data.elementAt(0)).doubleValue(),new Double((String)data.elementAt(1)).doubleValue(),new Double((String)data.elementAt(2)).doubleValue());
    	data=BCM_Descriptor.getValueList("PhiTransD");
    	okp[6].set(new Double((String)data.elementAt(0)).doubleValue(),new Double((String)data.elementAt(1)).doubleValue(),new Double((String)data.elementAt(2)).doubleValue());
    }
    
    /**
     * updates the BCM config with the new Calibration values
     * @param par
     */
    public void setCalibration(Stac_Configuration calib,ParamTable BCM_Descriptor,Vector3d[] par) {
    	ParamTable newValues= new ParamTable();
//    	newValues.setSingleValue("OmegaRot",""+par[0].x+" "+par[0].y+" "+par[0].z);
//    	newValues.setSingleValue("KappaRot",""+par[1].x+" "+par[1].y+" "+par[1].z);
//    	newValues.setSingleValue("PhiRot",""+par[2].x+" "+par[2].y+" "+par[2].z);
//    	newValues.setSingleValue("KappaTrans",""+par[3].x+" "+par[3].y+" "+par[3].z);
//    	newValues.setSingleValue("PhiTrans",""+par[4].x+" "+par[4].y+" "+par[4].z);
//    	newValues.setSingleValue("KappaTransD",""+par[5].x+" "+par[5].y+" "+par[5].z);
//    	newValues.setSingleValue("PhiTransD",""+par[6].x+" "+par[6].y+" "+par[6].z);
    	Vector values;
    	values=new Vector();
    	values.addElement(""+par[0].x);
    	values.addElement(""+par[0].y);
    	values.addElement(""+par[0].z);
    	newValues.setValueList("OmegaRot",values);
    	values=new Vector();
    	values.addElement(""+par[1].x);
    	values.addElement(""+par[1].y);
    	values.addElement(""+par[1].z);
    	newValues.setValueList("KappaRot",values);
    	values=new Vector();
    	values.addElement(""+par[2].x);
    	values.addElement(""+par[2].y);
    	values.addElement(""+par[2].z);
    	newValues.setValueList("PhiRot",values);
    	values=new Vector();
    	values.addElement(""+par[3].x);
    	values.addElement(""+par[3].y);
    	values.addElement(""+par[3].z);
    	newValues.setValueList("KappaTrans",values);
    	values=new Vector();
    	values.addElement(""+par[4].x);
    	values.addElement(""+par[4].y);
    	values.addElement(""+par[4].z);
    	newValues.setValueList("PhiTrans",values);
    	values=new Vector();
    	values.addElement(""+par[5].x);
    	values.addElement(""+par[5].y);
    	values.addElement(""+par[5].z);
    	newValues.setValueList("KappaTransD",values);
    	values=new Vector();
    	values.addElement(""+par[6].x);
    	values.addElement(""+par[6].y);
    	values.addElement(""+par[6].z);
    	newValues.setValueList("PhiTransD",values);
    	
    	calib.updateConfiguration(BCM_Descriptor,newValues,fileupdate());
    }
    
    /**
     * imports the Calibration values from gnsfile to BCM config
     * @param gnsfile
     * @param utils contains the tool to read gnsfile
     */
//    public void setCalibration(ParamTable BCM_Descriptor,String gnsfile,Util utils) {
//        Vector3d okp[]=new Vector3d[7];
//    	utils.read_gnsdef(gnsfile,okp);
//    	setCalibration(BCM_Descriptor,okp);
//    }

	@Override
	public void initPlugin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-base from S. Brockhauser";
	}

	public void registerMotorListener(String motorName, MotorWidget_actionAdapter listener) {
		motorListeners.addValue(motorName,listener);
		return;
	}

	public boolean useCentralMotorListener() {
		return true;
	}

	public Vector getMotorListeners(String motorName) {
		return motorListeners.getValueList(motorName);
	}
    
	
    public void centerNeedle() throws Exception {
    	session.errorMsg("Be sure that the Needle is centered!");
    }

	@Override
	protected void closePlugin() {
		// TODO Auto-generated method stub
		
	}

	/* simple shadow test for MK3
	 * 
	 */
	public boolean simpleShadow(ParamTable BCM_Descriptor, Point3d dat) {
		//analytic shadow management for MK
		//calc phi orientation
        Vector3d okp[]=new Vector3d[7];
        getCalibration(BCM_Descriptor,okp);
        Vector3d phi=new Vector3d(okp[2]);
        AxisAngle4d rot = new AxisAngle4d(okp[1],dat.y*Math.PI/180.);
        Matrix3d trf = new Matrix3d();
        trf.set(rot);
        trf.transform(phi);
        rot.set(okp[0],dat.x*Math.PI/180.);trf.set(rot);
        trf.transform(phi);
		//check for too short loops (pin shadow angle)
		//check for device shadow angle
		//check for resolution
        //  skip for now
        if (phi.x>0.1)
        	return true;

        return false;
	}
	
	@Override
	public Vector avoidShadow(ParamTable BCM_Descriptor, Point3d dat,
			double start, double end, boolean omegaosc) {
		Vector ranges=new Vector();
		//check for omega scans
		if (omegaosc) {
			double delta=end-start;
			Point3d pt= new Point3d(dat);
			while (delta >0) {
				//look for start
				// flip if at least the first 10 degree is in shadow
				pt.x=start;
				if (simpleShadow(BCM_Descriptor, pt)) {
					pt.x=start+10;
					if (simpleShadow(BCM_Descriptor, pt))
						start=(start+180)%360;
				}
				ranges.add(new Double(start));
				//look for end
				//register the found range and flip when enter into shadow
				double ad=20;
				double newdelta=delta;
				for (;ad<delta;ad+=10) {
					pt.x=start+ad;
					if (simpleShadow(BCM_Descriptor, pt)) {
						ranges.add(new Double(start+ad-10));
						start=(start+ad-10+180)%360;
						newdelta=delta-(ad-10);
						break;
					}
				}
				if (ad>=delta) {
					ranges.add(new Double(start+delta));
					delta=0;
				} else {
					delta=newdelta;
				}
				
			}
			
		} else {
			// no check:
			ranges.add(new Double(start));
			ranges.add(new Double(end));
		}
		
		return ranges;
	}

    
}


