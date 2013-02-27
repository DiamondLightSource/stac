package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.gui.*;
import stac.modules.BCM.*;

import gov.aps.jca.Channel;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;

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

import jive.ExecDev;

//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;



public class Stac_BCMplugin_epics extends Stac_BCMplugin_base implements Stac_BCMplugin {
	/*
	 * motorDescriptor:
	 * eg:
	 * motorName   motorName  multiplication factor 
	 * (in STAC)   (in spec)  (specValue=StacValue*f)
	 * 
	 *     X         sampx           -1
	 *     Y         sampy            1
	 *     Z         phiy             1   
	 *    Omega      phi              1   
	 *    Kappa      kap1             1   
	 *    Phi        kap2             1   
	 * 
	 * remark:
	 * mulfac assumes that calibration has been done, and the
	 * motors are aligned to the lab axes, otherwise
	 * [XYZ] <-> [spec translation] would require a 3d transformation
	 * 
	 */
	//ParamTable motorDescriptor=config.Descriptor;
	String     tangoURL;
	final int specName =0;
	final int specMulf =1;
	final int specOffset=2;
	
	// TANGO device stuff
	jive.DevHelper device;
	String device_name = null;
	String session_id = null;
	int nb_commands;
	String[] attrs;
	final String SEP_COMMAND_NAME = "_____________";
	int getto_index;
	int setto_index;
	int setlim_index;
	public static int answerLimit = 2048;
	
	//final ExecDev p = new ExecDev();	     
	
	
	
	
    public Stac_BCMplugin_epics () {
    	
	}
    
	@Override
	public void initPlugin() {
		// TODO Auto-generated method stub
		
    	//read the actual motorDescriptor
//    	{
//    		//X motor
//    		{
//    			String name ="X";
//    			Vector params = new Vector();
//    			String localid ="sampx";                 params.addElement(localid);
//    			Double mulfac = new Double(-1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Y motor
//    		{
//    			String name ="Y";
//    			Vector params = new Vector();
//    			String localid ="sampy";                 params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Z motor
//    		{
//    			String name ="Z";
//    			Vector params = new Vector();
//    			String localid ="phiy";                 params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Omega motor
//    		{
//    			String name ="Omega";
//    			Vector params = new Vector();
//    			String localid ="phi";                   params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Kappa motor
//    		{
//    			String name ="Kappa";
//    			Vector params = new Vector();
//    			String localid ="kap1";                  params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Phi motor
//    		{
//    			String name ="Phi";
//    			Vector params = new Vector();
//    			String localid ="kap2";                  params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    	}
    	//read the specversion
    	//read_specdef(System.getProperty("BCMDEF"));
    	config.getConfiguration();
    	try {
    		tangoURL=config.Descriptor.getFirstStringValue("TANGOURL");
    		//specversion="artemis2:sandor";
    		//specversion="artemis2:eh4";
    	} catch (Exception e) {
    	}
    	 try {
    	      // Get the JCALibrary instance.
    	      JCALibrary jca= JCALibrary.getInstance();
    	      
    	      // Create a single threaded context with default configuration values.
    	      Context ctxt= jca.createContext(JCALibrary.JNI_SINGLE_THREADED);
    	      
    	      // Display basic information about the context.
    	      ctxt.printInfo();
    	      
    	      // Create the Channel to connect to the PV.
    	      Channel ch= ctxt.createChannel(tangoURL);


    	      // send the request and wait 5.0 seconds for the channel to connect to the PV.
    	      ctxt.pendIO(5.0);
    	 
    	      // If we're here, then everything went fine.
    	      // Display basic information about the channel.
    	      ch.printInfo();

    	 
    	      // Disconnect the channel.
    	      ch.destroy();
    	 
    	      // Destroy the context.
    	      ctxt.destroy();


    	    } catch(Exception ex) {
    	    	Stac_Out.println("Could not connect to Epics channel!");
    	    }
    	
    	
    }

//    public void read_specdef(String specfile) {
//      	String corr;
//      	Util utils=new Util();
//        //read spec.dat
//      	corr = utils.opReadCl(specfile);
//        String tmp1[] = corr.split("\n");
//        for (int l=0;l<tmp1.length;l++) {
//        	if (tmp1[l].startsWith("#",0))
//        		continue;
//        	String tmp2[] = tmp1[l].split("\\s+");
//        	if(tmp2.length==0)
//        		continue;        	
//        	int offset=0;
//        	if (tmp2[0].length()==0)
//        		offset++;
//			String name = tmp2[0+offset];
//			if (name.equals("TANGOURL")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else if (name.equals("SOURCE")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else if (name.equals("BCM_Plugin")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else {
//				Vector params = new Vector();
//				String localid =tmp2[1+offset];                 params.addElement(localid);
//				Double mulfac = new Double(tmp2[2+offset]);     params.addElement(mulfac);
//				Double offsetValue = new Double(tmp2[3+offset]);params.addElement(offsetValue);
//				motorDescriptor.setValueList(name,params);				
//			}
//        }
//    }
//
//    void update_specdef(String motorName,int parameter,double value) {
//    	try {
//    		if (parameter==specOffset) {
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
//    					for (int pos=0;pos<tmp2.length;pos++) {
//    						if (pos==parameter+1) {
//    							newFile.write(value+"   ");
//    						} else {
//    							newFile.write(tmp2[pos]+"   ");
//    						}
//    					}
//    					newFile.write("\n");
//    				} else {
//    					newFile.write(tmp1[l]+"\n");
//    				}
//    			}
//    			newFile.close();
//       			motorDescriptor.setDoubleValueAt(motorName,parameter,value);
//    		}
//    	} catch (Exception e) {
//    	}
//    }
//	
    
    public double epics_getChInfo(String motorName) {
    	return 0;
    }
    public Channel epics_getCh(String motorName) {
    	return null;
    }
    public void epics_setCh(String motorName,double value) {
    	return ;
    }
    
    /*
     *  STACMotorPosition=(MeasuredMotorPosition-DefinedOffset)/DefinedScale
     * @see stacgui.Stac_BCMplugin#getMotorPosition(java.lang.String)
     */
    public double getMotorPosition(String motorName) throws Exception {
    	return convertMotorPosition_Plugin2Stac(motorName,epics_getChInfo(config.Descriptor.getFirstStringValue(motorName)));
    }
    
    public void setMotorPosition(String motorName,double newValue) throws Exception {
    	double currentValue=getMotorPosition(motorName);
    	double actualOffset=config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	double newOffset=-newValue*config.Descriptor.getDoubleValueAt(motorName,specMulf)+
			currentValue*config.Descriptor.getDoubleValueAt(motorName,specMulf)+
			config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	config.Descriptor.setDoubleValueAt(motorName,specOffset,newOffset);
    	config.updateConfiguration(config.Descriptor,config.Descriptor);
    	return;
    }
    
    public void moveMotor(String motorName,double newValue) throws Exception {
    	epics_setCh(config.Descriptor.getFirstStringValue(motorName),convertMotorPosition_Stac2Plugin(motorName,newValue));
    	return;
    }    
    
    public double getMotorSpeed(String motorName) throws Exception {
		return 1;
    }
    
    //standard values served:
    // + pos
    // + minpos
    // + maxpos
    // + speed
    // + minspeed
    public ParamTable getMotorParams(String motorName) throws Exception {
    	ParamTable params=new ParamTable();
        params.setSingleDoubleValue("pos",getMotorPosition(motorName));
    	Channel ch =epics_getCh(config.Descriptor.getFirstStringValue(motorName));
    	double min_value=0,max_value=0;
    	double minpos=convertMotorPosition_Plugin2Stac(motorName,new Double(min_value).doubleValue());
    	double maxpos=convertMotorPosition_Plugin2Stac(motorName,new Double(max_value).doubleValue());
		params.setSingleDoubleValue("minpos",Math.min(minpos,maxpos));
		params.setSingleDoubleValue("maxpos",Math.max(minpos,maxpos));
		params.setSingleDoubleValue("speed",getMotorSpeed(motorName));
		params.setSingleDoubleValue("minspeed",getMotorSpeedLowLimit(motorName));
    	return params;
    }
    
    public double getMotorSpeedLowLimit(String motorName) throws Exception {
		return 1;
    }
    
    public void setMotorSpeed(String motorName,double newValue) throws Exception {
    	return;
    }
    
    public void moveMotors(ParamTable newPositions) throws Exception {
    	moveMotorsSyncronized(newPositions);
//    	for(int i=0;i<newPositions.pnames.size();i++){
//    		String motorName = (String)newPositions.pnames.elementAt(i);
//        	double newpos=newPositions.getFirstDoubleValue(motorName);
//        	moveMotor(motorName,newpos);
//    	}
    	return;
    	
    }
    
    public void moveMotorsSyncronized(ParamTable newPositions) throws Exception {
    	//send tango command if available
    	String double_array = new String("[ ");
    	String string_array = new String("[ ");
    	for(int i=0;i<newPositions.pnames.size();i++){
    		//motor
    		String motorName = (String)newPositions.pnames.elementAt(i);
    		if (i>0)
    			string_array=string_array.concat(", ");
    		string_array=string_array.concat(config.Descriptor.getFirstStringValue(motorName)+" ");
    		//value
        	double newpos=newPositions.getFirstDoubleValue(motorName);
    		if (i>0)
    			double_array=double_array.concat(", ");
        	double_array=double_array.concat(""+convertMotorPosition_Stac2Plugin(motorName,newpos)+" ");
    	}
		string_array=string_array.concat(" ] ");
    	double_array=double_array.concat(" ] ");
		Stac_Out.print("Synchronised DatumTrans Movement "+double_array+"...");
//		String ret_client_string = epics_setCh(motorDescriptor.getFirstStringValue("SYNC_MOVE"),double_array+string_array);
//		boolean moving=true;
//		do {
//			ret_client_string = epics_setCh(motorDescriptor.getFirstStringValue("STATUSREQUEST"),"");
//	    	String[] tmp= ret_client_string.split("\n");
//	    	if (tmp[tmp.length-1].equals(motorDescriptor.getFirstStringValue("STATUSOK"))) {
//	    		ret_client_string="DONE";
//	    		moving=false;
//	    	} else if (!tmp[tmp.length-1].equalsIgnoreCase("MOVING")) {
//	    		ret_client_string="FAILED";
//	    		moving=false;
//	    	}
//		} while (moving);
//		Stac_Out.println(ret_client_string);
    	
    	//otherwise simply move the motors
    	//moveMotors(newPositions);
    	return;
    	
    }
    
    public void initMotors() throws Exception {
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
 
	/* (non-Javadoc)
	 * @see stacgui.Stac_BCMplugin#loadMotorPosition(java.lang.String, java.lang.String)
	 */
	public double loadMotorPosition(String motorName, String data) {
		String spec_motorName= new String(config.Descriptor.getFirstStringValue(motorName));
        String tmp1[] = data.split("\n");
        for (int l=0;l<tmp1.length;l++) {
        	if (tmp1[l].startsWith("#",0))
        		continue;
        	String tmp2[] = tmp1[l].split("\\s+");
        	if(tmp2.length==0)
        		continue;
        	int offset=0;
        	if (tmp2[0].length()==0)
        		offset++;
        	if(offset>=tmp2.length)
        		continue;
			String name = tmp2[0+offset];
			{
				String value =tmp2[1+offset];
				if (name.equalsIgnoreCase(spec_motorName)){
					return convertMotorPosition_Plugin2Stac(motorName,new Double(value).doubleValue());
				}
			}
        }
		
		return 0;
	}
	/* (non-Javadoc)
	 * @see stacgui.Stac_BCMplugin#saveMotorPositions(stacgui.ParamTable, java.lang.String)
	 */
	public String saveMotorPositions(ParamTable positions, String data) {
    	for(int i=0;i<positions.pnames.size();i++){
    		String motorName = (String)positions.pnames.elementAt(i);
    		double newValue  = positions.getFirstDoubleValue(motorName);
    		data=data.concat(config.Descriptor.getFirstStringValue(motorName)+"  "+convertMotorPosition_Stac2Plugin(motorName,newValue)+"\n");
    	}
    	return data;
	}
    
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-epics from S. Brockhauser";
	}
    
    
    
}


