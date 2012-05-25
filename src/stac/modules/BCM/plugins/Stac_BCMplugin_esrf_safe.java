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
 * esrf_safe: BCM plugin for spec at ESRF
 * It is using the spec user values, but always checks the dial values,
 * as well. If the offset between the user and dial values has been
 * changed, a warning window comes up.
 * This plugin does NOT SET the dial/user values. Instead, it is managing
 * offsets between the spec and STAC values   
 *
 * @author sudol
 *
 */
public class Stac_BCMplugin_esrf_safe extends Stac_BCMplugin_esrf_user implements Stac_BCMplugin {
	/*
	 * motorDescriptor:
	 * eg:
     *# motorName   motorName  multiplication factor    offset    dialOffset
     *# (in STAC)   (in tango) (tangoValue=StacValue*f+offset)   (user=dial+dialOffset)
	 * 
	 *     X         sampx           -1             0               0
	 *     Y         sampy            1             0               0
	 *     Z         phiy             1             0               0
	 *    Omega      phi              1             0               0
	 *    Kappa      kap1             1             0               0
	 *    Phi        kap2             1             0               0
	 * 
	 * remark:
	 * mulfac assumes that calibration has been done, and the
	 * motors are aligned to the lab axes, otherwise
	 * [XYZ] <-> [spec translation] would require a 3d transformation
	 * 
	 */
    
	final int specOffset=2;
	final int dialOffset=3;

	String spec_getPosition_dial() {
		return new String("getDialPosition");
	}
/*	
	String spec_mv_dial() {
		return new String("mvd");
	}
	String spec_set_dial() {
		return new String("set_dial");
	}
	String ProDC_MotorNameSuffix_dial() {
		return new String("_dial");		
	}
*/	
//	public void read_specdef_general (Vector params,String lineTokenized[],int tokenOffset){
//		if (lineTokenized.length<=tokenOffset+4)
//			return;
//		String localid =lineTokenized[1+tokenOffset];            params.addElement(localid);
//		Double mulfac = new Double(lineTokenized[2+tokenOffset]);params.addElement(mulfac);
//		Double offsetValue = new Double(lineTokenized[3+tokenOffset]);params.addElement(offsetValue);		
//		Double dialOffsetValue = new Double(lineTokenized[4+tokenOffset]);params.addElement(dialOffsetValue);
//	}
	
	
	
	void spec_getMotor(String motorName){
		boolean finalized=false;
		while(!finalized) {
			String outfile;
			String PyCommand=new String();
			try {
				outfile="spec_cmd.py";
				// Create channel on the destination
				FileWriter dstStream = new FileWriter(outfile);
				// Copy file contents from source to destination
				PyCommand=new PrintfFormat("#! /usr/bin/env python\n"+
						"import sys\n"+
						"from SpecClient import *\n"+
						"m=SpecMotor.SpecMotor('%s','%s',500)\n"+
						"p=m."+spec_getPosition()+"()\n"+
						"print 'position_of_the_specified_motor_is: ',p\n"+
						"p=m."+spec_getPosition_dial()+"()\n"+
						"print 'dial_position_of_the_specified_motor_is: ',p\n"+
						"cmd=SpecCommand.SpecCommand('','%s',500)\n"+
						"print 'speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"velocity\")')\n"+
						"print 'lowest_speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"base_rate\")')\n"+
						"print 'stepsize_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"step_size\")')\n"+
						"print 'low_limit_of_the_specified_motor_is: ',cmd.executeCommand('"+spec_to_plugin_value(motorName,"get_lim(%s,-1)")+"')\n"+
						"print 'high_limit_of_the_specified_motor_is: ',cmd.executeCommand('"+spec_to_plugin_value(motorName,"get_lim(%s,1)")+"')\n").sprintf(new Object[] {motorName,specversion,specversion,motorName,motorName,motorName,motorName,motorName});
				dstStream.write(PyCommand);
				// Close the file
				dstStream.flush();
				dstStream.close();
			} catch (IOException e) {
				Stac_Out.println("Problem with generating the spec_cmd.py file");
			}
			Stac_Out.println("Get MotorProperties ("+motorName+")");
			int exitval=1;
			for(int i=0;i<3 && exitval!=0;i++)
				exitval=spec_comm.spec_exec(PyCommand);
			
			finalized=true;
			if (exitval==0) {
				try {
					//check for user/dial offset change:
					String STAC_motorName=(String)config.Descriptor.getNameForParam(motorName);
					double userValue=new Double(getParam("position_of_the_specified_motor_is:")).doubleValue();
					double dialValue=new Double(getParam("dial_position_of_the_specified_motor_is:")).doubleValue();
					double desiredUser=dialValue+config.Descriptor.getDoubleValueAt(config.Descriptor.getNameForParam(motorName),dialOffset);
					double diff;
					if (
							STAC_motorName.equalsIgnoreCase("Omega") ||
							STAC_motorName.equalsIgnoreCase("Kappa") ||
							STAC_motorName.equalsIgnoreCase("Phi")) {
						Util utils = new Util();
						diff=utils.angleDegreeDiff(userValue,desiredUser);
					} else {
						diff=Math.abs(userValue-desiredUser);
					}
					if (diff>1e-2) {
						//TODO: dialbox to 
						//        + accept the new values, or
						//        + chg the settings manually in spec and reread the values
						//          (finalized=false)
						Stac_Out.println("WARNING!!!\nSpec user/dial offset has been changed:");
						Stac_Out.println("Spec Motor Name:      "+motorName);
						Stac_Out.println("Current dial value:   "+dialValue);
						Stac_Out.println("Current user value:   "+userValue);
						Stac_Out.println("user value should be: "+desiredUser);
						finalized=true;
					}
				}catch(Exception e){}
			}
		}
		
	}

    
    public void setMotorPosition(String motorName,double newValue) throws Exception {
    	double currentValue=getMotorPosition(motorName);
    	double actualOffset=config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	double newOffset=-newValue*config.Descriptor.getDoubleValueAt(motorName,specMulf)+
			currentValue*config.Descriptor.getDoubleValueAt(motorName,specMulf)+
			config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	Vector motorParam=config.Descriptor.getValueList(motorName);
    	//specOffset
    	motorParam.removeElementAt(specOffset);
    	motorParam.insertElementAt(new Double(newOffset),specOffset);
    	//dialOffset
      	double userValue=new Double(getParam("position_of_the_specified_motor_is:")).doubleValue();
      	double dialValue=new Double(getParam("dial_position_of_the_specified_motor_is:")).doubleValue();
      	double dialOffsetValue=userValue-dialValue;
    	motorParam.removeElementAt(dialOffset);
    	motorParam.insertElementAt(new Double(dialOffsetValue),dialOffset);
    	
    	//config.Descriptor.setDoubleValueAt(motorName,specOffset,newOffset);
    	config.updateConfiguration(config.Descriptor,config.Descriptor);
    	//update_specdef(motorName,motorParam);
    	return;
    }
    
    public void initMotors() throws Exception {
    	super.initMotors();
    	setMotorPosition("Omega",getMotorPosition("Omega"));
    	setMotorPosition("Kappa",getMotorPosition("Kappa"));
    	setMotorPosition("Phi",getMotorPosition("Phi"));
    	return;
    }
    
	@Override
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-esrfSAFE from S. Brockhauser";
	}
    
}


