package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.gui.*;
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

/**
 * 
 * esrf_dial: BCM plugin for spec at ESRF
 * It is using the spec dial values ONLY, 
 * This plugin does NOT SET the dial/user values. Instead, it is managing
 * offsets between the spec and STAC values   
 * It is used as base class for all later spec plugins managing offsets   
 *
 * @author sudol
 *
 */



public class Stac_BCMplugin_esrf_dial extends Stac_BCMplugin_esrf implements Stac_BCMplugin {
	/*
	 * motorDescriptor:
	 * eg:
     *# motorName   motorName  multiplication factor    offset
     *# (in STAC)   (in tango) (tangoValue=StacValue*f+offset)
	 * 
	 *     X         sampx           -1             0
	 *     Y         sampy            1             0
	 *     Z         phiy             1             0   
	 *    Omega      phi              1             0
	 *    Kappa      kap1             1             0
	 *    Phi        kap2             1             0
	 * 
	 * remark:
	 * mulfac assumes that calibration has been done, and the
	 * motors are aligned to the lab axes, otherwise
	 * [XYZ] <-> [spec translation] would require a 3d transformation
	 * 
	 */
    
	final int specOffset=2;

	String spec_mv() {
		return new String("mvd");
	}
	String spec_set() {
		return new String("set_dial");
	}
	String spec_getPosition() {
		return new String("getDialPosition");
	}
	String ProDC_MotorNameSuffix() {
		return new String("_dial");		
	}
		
    /**
     * converts the standard plugin input (here, it is the dial value) to user value
     */
	public String spec_A_value (String specMotorName, double value) {
		String A_value= new String("user("+specMotorName+","+value+")");
		return A_value;
	}	

	/**
	 * converts the dial input to the standard plugin value (here, it is dial)
	 */
	public String spec_to_plugin_value (String specMotorName, String value) {
		String plugin_value= new String(""+value);
		return plugin_value;
	}
	
//	public void read_specdef_general (Vector params,String lineTokenized[],int tokenOffset){
//		String localid =lineTokenized[1+tokenOffset];            params.addElement(localid);
//		Double mulfac = new Double(lineTokenized[2+tokenOffset]);params.addElement(mulfac);
//		Double offsetValue = new Double(lineTokenized[3+tokenOffset]);params.addElement(offsetValue);		
//	}
//	public void read_specdef(String specfile) {
//		String corr;
//		Util utils=new Util();
//		//read spec.dat
//		corr = utils.opReadCl(specfile);
//		String tmp1[] = corr.split("\n");
//		for (int l=0;l<tmp1.length;l++) {
//			if (tmp1[l].startsWith("#",0))
//				continue;
//			String tmp2[] = tmp1[l].split("\\s+");
//			if(tmp2.length==0)
//				continue;
//			int offset=0;
//			if (tmp2[0].length()==0)
//				offset++;
//			String name = tmp2[0+offset];
//			if (name.equals("SPECVERSION")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else if (name.equals("BCM_Plugin")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else if (name.equals("INITSTRING")) {
//				String value=new String("");
//				for(int i=1+offset;i<tmp2.length;i++){
//					value=value.concat(tmp2[i]+" ");
//				}
//				motorDescriptor.setSingleValue(name,value);
//			} else {
//				Vector params = new Vector();
//				read_specdef_general (params,tmp2,offset);
//				motorDescriptor.setValueList(name,params);				
//			}
//		}
//		
//	}
//    
//    void update_specdef(String motorName,Vector motorParam) { //int parameter,double value) {
//    	try {
//    		if (motorParam!=null) { //parameter==specOffset) {
//    			String corr;
//    			Util utils=new Util();
//    			//read spec.dat
//    			corr = utils.opReadCl(System.getProperty("BCMDEF"));
//    			FileWriter newFile= new FileWriter(System.getProperty("BCMDEF"));
//    			String tmp1[] = corr.split("\n");
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
//    				if (name.equals(motorName)) {
////    					newFile.write("# "+tmp1[l]+"\n");
///*    					for (int pos=0;pos<tmp2.length;pos++) {
//						if (pos==parameter+1) {
//							newFile.write(value+" ");
//						} else {
//							newFile.write(tmp2[pos]+" ");
//						}
//					}
//*/    					newFile.write(""+name);
//    					for (int pos=0;pos<motorParam.size();pos++) {
//   							newFile.write(" "+motorParam.elementAt(pos));
//    					}    					
//    					newFile.write("\n");
//    				} else {
//    					newFile.write(tmp1[l]+"\n");
//    				}
//    			}
//    			newFile.close();
//       			//motorDescriptor.setDoubleValueAt(motorName,parameter,value);
//       			motorDescriptor.setValueList(motorName,motorParam);//parameter,value);
//    		}
//    	} catch (Exception e) {
//    	}
//    }
//
    public void setMotorPosition(String motorName,double newValue) throws Exception {
    	double currentValue=getMotorPosition(motorName);
    	double actualOffset=config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	double newOffset=-newValue*config.Descriptor.getDoubleValueAt(motorName,specMulf)+
			currentValue*config.Descriptor.getDoubleValueAt(motorName,specMulf)+
			config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	Vector motorParam=config.Descriptor.getValueList(motorName);
    	motorParam.removeElementAt(specOffset);
    	motorParam.insertElementAt(new Double(newOffset),specOffset);
    	
    	//config.Descriptor.setDoubleValueAt(motorName,specOffset,newOffset);
    	config.updateConfiguration(config.Descriptor,config.Descriptor);
    	//updateConfiguration(motorName,motorParam);
    	return;
    }
	
    public double convertMotorPosition_Plugin2Stac(String motorName,double pluginPos) {
    	double stacPos=(pluginPos-config.Descriptor.getDoubleValueAt(motorName,specOffset))/config.Descriptor.getDoubleValueAt(motorName,specMulf);
    	return stacPos;
    }
    
    public double convertMotorPosition_Stac2Plugin(String motorName,double stacPos) {
    	double pluginPos=stacPos*config.Descriptor.getDoubleValueAt(motorName,specMulf)+config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	return pluginPos;
    }    
    
	@Override
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-esrfDIAL from S. Brockhauser";
	}
    
}


