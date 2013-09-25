package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.gui.*;
import stac.modules.BCM.*;

import java.awt.*;
import java.awt.event.*;

import javax.imageio.ImageIO;
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
public class Stac_BCMplugin_esrf_complex extends Stac_BCMplugin_esrf_safe implements Stac_BCMplugin {
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
    
	ESRF_Complex_tango tangoclient = null;
	
	@Override
	public void initPlugin() {
		super.initPlugin();
		tangoclient=new ESRF_Complex_tango(config.Descriptor);
		
	}
    
	@Override
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-esrfCOMPLEX from S. Brockhauser";
	}
	
	
    /**
     * centers a needle (used for automatic Translation Calibration)
     * @throws Exception 
     *
     */
    public void centerNeedle() throws Exception {
    	//spec macro to prepare
    	String cmd = config.Descriptor.getFirstStringValue("CENTERPREPARE");
    	spec_cmd(cmd);
    	//tango command to do centering
    	tangoclient.centerNeedle();
    }
	
	
    
}



class ESRF_Complex_tango implements fr.esrf.TangoDs.TangoConst {
	
	/***************************************************************************** 
	*
	*
	*/
	class CmdInfo
	{
	public CmdInfo() { }

	public CmdInfo(String cmd_name,
	              int cmd_tag,
	              int in_type,
	              int out_type,
	              String in_type_desc,
	              String out_type_desc)
	{
	 this.cmd_name      = cmd_name;
	 this.cmd_tag       = cmd_tag;
	 this.in_type       = in_type;
	 this.out_type      = out_type;
	 this.in_type_desc  = in_type_desc;
	 this.out_type_desc = out_type_desc;
	}

	public String cmd_name;
	public int cmd_tag;
	public int in_type;
	public int out_type;
	public String in_type_desc;
	public String out_type_desc;

	} // end of class CmdInfo

	/***************************************************************************** 
	*
	* The aim of this class which is a redefinition of Tango AttributeInfo
	* class is to avoid having any Tango dependency when running as an Applet
	*
	*/
	class AttrInfo
	{
	public AttrInfo() { }

	public AttrInfo(
		String name,
		String writable,
		String data_format,
		String data_type,
		int    data_type_num,
		String max_dim_x,
		String max_dim_y,
		String description,
		String label,
		String unit,
		String standard_unit,
		String display_unit,
		String format,
		String min_value,
		String max_value,
		String min_alarm,
		String max_alarm)
	{
	 this.name          = name;
	 this.writable      = writable;
	 this.data_format   = data_format;
	 this.data_type     = data_type;
	 this.data_type_num = data_type_num;
	 this.max_dim_x     = max_dim_x;
	 this.max_dim_y     = max_dim_y;
	 this.description   = description;
	 this.label         = label;
	 this.unit          = unit;
	 this.standard_unit = standard_unit;
	 this.display_unit  = display_unit;
	 this.format        = format;
	 this.min_value     = min_value;
	 this.max_value     = max_value;
	 this.min_alarm     = min_alarm;
	 this.max_alarm     = max_alarm;
	}

	public String name;
	public String writable;
	public String data_format;
	public String data_type;
	public int    data_type_num;
	public String max_dim_x;
	public String max_dim_y;
	public String description;
	public String label;
	public String unit;
	public String standard_unit;
	public String display_unit;
	public String format;

	public String min_value;
	public String max_value;
	public String min_alarm;
	public String max_alarm;

	} // end of class AttrInfo



	
	
	
	ParamTable motorDescriptor=new ParamTable();
	String     tangoURL;
	final int specName =0;
	final int specMulf =1;
	final int specOffset=2;
	
	// TANGO device stuff
	jive.DevHelper device;
	String device_name = null;
	String session_id = null;
	int nb_commands;
	CmdInfo[] commands;
	String[] attrs;
	AttrInfo[] attr_configs;
	final String SEP_COMMAND_NAME = "_____________";
	int getto_index;
	int setto_index;
	int setlim_index;
	public static int answerLimit = 2048;
	
	//final ExecDev p = new ExecDev();	     
	
	
	
  	/**********************************
  	 * Return: false if device could be imported
  	 */
  	public boolean set_device_name(String devname)
  	{
  		device_name = devname;
  		
  		//
  		// TANGO init (only if running in standalone application)
  		//
  		{
  			try 
			{   
  				device = new jive.DevHelper(device_name); 
			}
  			catch(jive.DevHelperDevFailed e)
			{
  				StringBuffer errmsg = new StringBuffer();
  				
  				errmsg.append("failed to import device: " + device_name + "\n");
  				errmsg.append(e);
  				
  				Stac_Out.println(errmsg.toString());
  				
  				return true;
			}
  			catch(Exception e)
			{
  				StringBuffer errmsg = new StringBuffer();
  				
  				errmsg.append("ORB failed connecting to database");
  				
  				
  				Stac_Out.println(errmsg.toString());
  				
  				return true;
			}
  		}
  		return false;
  	}
  		
  		
	
    public ESRF_Complex_tango (ParamTable motorDescriptor) {
    	this.motorDescriptor=motorDescriptor;
    	
    	//read the specversion
    	//read_specdef(System.getProperty("BCMDEF"));
    	try {
    		tangoURL=motorDescriptor.getFirstStringValue("TANGOURL_CENTERNEEDLE");
    	} catch (Exception e) {
    	}
    	
    	if( !set_device_name( tangoURL ) ) {
    	} else {
    		Stac_Out.println("Could not connect to Tango device!");
    	}
    	
    	
    }
//
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
//			if (name.startsWith("TANGOURL")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else if (name.equals("CENTERNEEDLE_COMMAND")) {
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
	
    
    double tango_getAttr (String attr_name){
    	
    	String ret_client_string = null;
    	
    	//
    	// Running in a standalone application, direct access to TANGO
    	//
    	
    	try
		{
    		device.set_source( new Integer(motorDescriptor.getFirstStringValue("SOURCE")).intValue() );
    		ret_client_string = device.read_attr(attr_name);
		}
    	catch(jive.DevHelperDevFailed e)
		{
    		StringBuffer errmsg = new StringBuffer();
    		
    		errmsg.append("failed reading attribute: " + attr_name + "\n");
    		errmsg.append(e);
    		
    		Stac_Out.println(errmsg.toString());
    		return 0;
		}
    	catch(Exception e)
		{
    		StringBuffer errmsg = new StringBuffer();
    		errmsg.append("Exception: " + e.getClass().getName() + "\n");
    		errmsg.append("message:\t" + e.getMessage()   + "\n");
    		
    		Stac_Out.println(errmsg.toString());
    		return 0;
		}
    	
    	//Stac_Out.println(ret_client_string);
    	
    	String[] tmp= ret_client_string.split("\n");
    	for(int i=0;i<tmp.length;i++) {
    		if (tmp[i]==null)
    			continue;
    		String[] tmp2=tmp[i].split(":");
    		if (tmp2!=null && tmp2[0]!=null && tmp2[0].equals("read point")){
    			return new Double(tmp2[1]).doubleValue();
    		}
    	}
    	
    	//Stac_Out.println("Error reading Tango Attribute");
    	
    	return 0;
    }
    
    void tango_setAttr (String attr_name,double newValue){
    	
    	String ret_client_string = null;
    	
    	//
    	// Running in a standalone application, direct access to TANGO
    	//
    	
    	try
		{
    		device.set_source( new Integer(motorDescriptor.getFirstStringValue("SOURCE")).intValue() );
    		Stac_Out.print("Moving "+attr_name+" to "+newValue+" ...");
    		ret_client_string = device.write_attr(attr_name,""+newValue);
    		boolean inMove=true;
    		
    		do {
    			ret_client_string = tango_CMD(motorDescriptor.getFirstStringValue("STATUSREQUEST"),"");
    	    	String[] tmp= ret_client_string.split("\n");
    	    	inMove=false;
    	    	if (tmp[tmp.length-1].equals(motorDescriptor.getFirstStringValue("STATUSOK"))) {
    	    		ret_client_string="OK";
    	    	} else if (tmp[tmp.length-1].equals(motorDescriptor.getFirstStringValue("STATUSMOVE"))) {
    	    		inMove=true;
    	    	}
    		} while (inMove);
    		//Stac_Out.println(ret_client_string);
		}
    	catch(jive.DevHelperDevFailed e)
		{
    		StringBuffer errmsg = new StringBuffer();
    		
    		errmsg.append("failed writing attribute: " + attr_name + "\n");
    		errmsg.append(e);
    		
    		Stac_Out.println(errmsg.toString());
    		return;
		}
    	catch(Exception e)
		{
    		StringBuffer errmsg = new StringBuffer();
    		errmsg.append("Exception: " + e.getClass().getName() + "\n");
    		errmsg.append("message:\t" + e.getMessage()   + "\n");
    		
    		Stac_Out.println(errmsg.toString());
    		return;
		}
    	Stac_Out.println(ret_client_string);
    }
    
    String tango_CMD (String cmd_name, String cmd_param){
    	
    	String ret_client_string = null;
    	
    	
    	//
    	// Running in a standalone application, direct access to TANGO
    	//
    	
    	try
		{
    		device.set_source( new Integer(motorDescriptor.getFirstStringValue("SOURCE")).intValue() );
    		ret_client_string = device.exec_command(cmd_name,cmd_param);
		}
    	catch(jive.DevHelperDevFailed e)
		{
    		StringBuffer errmsg = new StringBuffer();
    		
    		errmsg.append("failed sending command: " + cmd_name + "\n");
    		errmsg.append(e);
    		
    		Stac_Out.println(errmsg.toString());
    		return null;
		}
    	catch(Exception e)
		{
    		StringBuffer errmsg = new StringBuffer();
    		errmsg.append("Exception: " + e.getClass().getName() + "\n");
    		errmsg.append("message:\t" + e.getMessage()   + "\n");
    		
    		Stac_Out.println(errmsg.toString());
    		return null;
		}
    	//Stac_Out.println(ret_client_string);
    	return ret_client_string;
    }
    
    
    
    
    AttrInfo tango_getAttrProperties (String attr_name){
    	
    	String ret_client_string = null;

    	//
    	// Running in a standalone application, direct access to TANGO
    	//
    	
    	// Get the list of attributes of the device
    	attrs = device.get_attribute_list();
    	// Get for each attribute all its properties
    	fr.esrf.TangoApi.AttributeInfo[] tango_attr_configs;
    	tango_attr_configs = device.get_attribute_config();
    	
    	// Get the number of attributes
    	int nb_attrs = tango_attr_configs.length;
    	
    	
    	// Convert Tango stuff to non Tango (required when running as Applet)
    	attr_configs = new AttrInfo[nb_attrs];
    	int attrId=0;
    	for(int i=0;i<nb_attrs;i++)
    	{
    		if(!attr_name.equals(tango_attr_configs[i].name))
    			continue;
    		attr_configs[i] = new AttrInfo(
    				tango_attr_configs[i].name,
					jive.ExecDev.AttrWriteType_to_string(tango_attr_configs[i].writable),
					jive.ExecDev.AttrDataFormat_to_string(tango_attr_configs[i].data_format),
					Tango_CmdArgTypeName[tango_attr_configs[i].data_type],
					tango_attr_configs[i].data_type,
					Integer.toString(tango_attr_configs[i].max_dim_x),
					Integer.toString(tango_attr_configs[i].max_dim_y),
					tango_attr_configs[i].description,
					tango_attr_configs[i].label,
					tango_attr_configs[i].unit,
					tango_attr_configs[i].standard_unit,
					tango_attr_configs[i].display_unit,
					tango_attr_configs[i].format,
					tango_attr_configs[i].min_value,
					tango_attr_configs[i].max_value,
					tango_attr_configs[i].min_alarm,
					tango_attr_configs[i].max_alarm);
    		attrId=i;
    		break;
    	}
    	
    	if(attrId<nb_attrs)
    		Stac_Out.println("Attribute ("+attr_name+") is read");
    	else
    		Stac_Out.println("Attribute ("+attr_name+") is not available");
    		
    
    	return attr_configs[attrId];
    }

    
    public void centerNeedle() throws Exception {
    	//System.out.println(motorDescriptor.toString());
    	//send tango command if available
    	String double_array = new String("[ ");
    	String string_array = new String("[ ");
		string_array=string_array.concat(" ] ");
    	double_array=double_array.concat(" ] ");
		Stac_Out.print("Centering a Needle "+"...");
		String ret_client_string = tango_CMD(motorDescriptor.getFirstStringValue("CENTERNEEDLE"),double_array+string_array);
		boolean moving=true;
		do {
			ret_client_string = tango_CMD(motorDescriptor.getFirstStringValue("CENTERSTATUSREQUEST"),"");
	    	String[] tmp= ret_client_string.split("\n");
	    	if (tmp[tmp.length-1].equals(motorDescriptor.getFirstStringValue("CENTERSTATUSOK"))) {
	    		ret_client_string="DONE";
	    		moving=false;
	    	} else if (!tmp[tmp.length-1].equalsIgnoreCase(motorDescriptor.getFirstStringValue("CENTERSTATUSMOVE"))) {
	    		ret_client_string="FAILED";
	    		moving=false;
	    	}
		} while (moving);
		Stac_Out.println(ret_client_string);
    	
		Stac_Out.println("Centering precision: "+tango_getAttr(motorDescriptor.getFirstStringValue("CENTERNEEDLERADIUS")));
    	return;
    	
    }
    
    
}



