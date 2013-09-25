package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;

import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceData;
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



public class Stac_BCMplugin_tango_md2_2012 extends Stac_BCMplugin_tango implements Stac_BCMplugin, fr.esrf.TangoDs.TangoConst {

    String tango_AttrSuffix () {
    	return "Position";
    }	

    AttrInfo tango_getAttrProperties (String attr_name){
    	String ret_client_string = null;

    	DeviceData result = tango_CMD_DA(config.Descriptor.getFirstStringValue("MOTOR_LIMITS"), attr_name);
    	double[] limits = result.extractDoubleArray();
    	
    	AttrInfo	attr_config = new AttrInfo(
    				attr_name+tango_AttrSuffix(),
					"",//jive.ExecDev.AttrWriteType_to_string(tango_attr_configs[i].writable),
					"",//jive.ExecDev.AttrDataFormat_to_string(tango_attr_configs[i].data_format),
					"",//Tango_CmdArgTypeName[tango_attr_configs[i].data_type],
					0,//tango_attr_configs[i].data_type,
					"0",//Integer.toString(tango_attr_configs[i].max_dim_x),
					"0",//Integer.toString(tango_attr_configs[i].max_dim_y),
					"",//tango_attr_configs[i].description,
					"",//tango_attr_configs[i].label,
					"",//tango_attr_configs[i].unit,
					"",//tango_attr_configs[i].standard_unit,
					"",//tango_attr_configs[i].display_unit,
					"",//tango_attr_configs[i].format,
					""+limits[0],//tango_attr_configs[i].min_value,
					""+limits[1],//tango_attr_configs[i].max_value,
					"",//tango_attr_configs[i].min_alarm,
					"");//tango_attr_configs[i].max_alarm);
    	    
    	return attr_config;
    }
    
    public void moveMotorsSyncronized(ParamTable newPositions) throws Exception {
    	//send tango command if available
    	String string_array = new String("");
    	for(int i=0;i<newPositions.pnames.size();i++){
    		//motor , value
    		String motorName = (String)newPositions.pnames.elementAt(i);
    		if (i>0)
    			string_array=string_array.concat(",");
    		string_array=string_array.concat(config.Descriptor.getFirstStringValue(motorName)+"");
    		//value
        	double newpos=newPositions.getFirstDoubleValue(motorName);
   			string_array=string_array.concat("=");
        	string_array=string_array.concat(""+convertMotorPosition_Stac2Plugin(motorName,newpos)+"");
    	}
		string_array=string_array.concat("");
		Stac_Out.print("Synchronised DatumTrans Movement "+string_array+"...");
		String ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("SYNC_MOVE"),string_array);
		
		
		boolean moving=true;
		do {
			String ret="";
			try {
				ret=""+this.tango_getAttrDA(config.Descriptor.getFirstStringValue("STATUSREQUEST"),"").extractString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	if (ret.equalsIgnoreCase(config.Descriptor.getFirstStringValue("STATUSOK"))) {
	    		ret_client_string="DONE";
	    		moving=false;
	    	} else if (!ret.startsWith(config.Descriptor.getFirstStringValue("STATUSMOVE"))) {
	    		ret_client_string="FAILED: "+ret;
	    		moving=false;
	    	}
		} while (moving);
		Stac_Out.println(ret_client_string);
    	
    	//otherwise simply move the motors
    	//moveMotors(newPositions);
    	return;
    	
    }
    
    
	public String getCreditString() {
		return "using BCM-tango-MD2-2012 from S. Brockhauser";
	}
    
}


