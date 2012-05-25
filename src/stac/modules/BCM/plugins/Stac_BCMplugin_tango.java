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



public class Stac_BCMplugin_tango extends Stac_BCMplugin_base implements Stac_BCMplugin, fr.esrf.TangoDs.TangoConst {
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
  		
  		
	
    public Stac_BCMplugin_tango () {
		
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
    	//config.getConfiguration();
    	try {
    		tangoURL=config.Descriptor.getFirstStringValue("TANGOURL");
    		//specversion="artemis2:sandor";
    		//specversion="artemis2:eh4";
    	} catch (Exception e) {
    	}
    	
    	if( !set_device_name( tangoURL ) ) {
    		/*
    		final JFrame f = new JFrame();
    		f.setTitle("Device Panel");
    		f.getContentPane().setLayout(null);
    		f.getContentPane().add(p);   	     	     
    		f.addComponentListener( new ComponentListener() {
    			public void componentHidden(ComponentEvent e) {
    				System.exit(0);
    			} 
    			public void componentMoved(ComponentEvent e) {}	  	     
    			public void componentResized(ComponentEvent e) { 
    				p.placeComponents(f.getContentPane().getSize());
    			}
    			public void componentShown(ComponentEvent e) { 
    				p.placeComponents(f.getContentPane().getSize());
    			}
    		});
    		f.setBounds(50,50,450,650);
    		//see the window in debug mode
    		f.setVisible(true);
    		//f.setVisible(false);
    		 */
    	} else {
    		Stac_Out.println("Could not connect to Tango device!");
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
////				String localid =tmp2[1+offset];                 params.addElement(localid);
////				Double mulfac = new Double(tmp2[2+offset]);     params.addElement(mulfac);
////				Double offsetValue = new Double(tmp2[3+offset]);params.addElement(offsetValue);
//				for(int i=1+offset;i<tmp2.length;i++)
//					params.addElement(tmp2[i]);
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
    
    double tango_getAttr (String attr_name){
    	
    	String ret_client_string = null;
    	
    	//
    	// Running in a standalone application, direct access to TANGO
    	//
    	
    	try
		{
    		device.set_source( new Integer(config.Descriptor.getFirstStringValue("SOURCE")).intValue() );
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
    		device.set_source( new Integer(config.Descriptor.getFirstStringValue("SOURCE")).intValue() );
    		Stac_Out.print("Moving "+attr_name+" to "+newValue+" ...");
    		ret_client_string = device.write_attr(attr_name,""+newValue);
    		boolean inMove=true;
    		
    		do {
    			ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("STATUSREQUEST"),"");
    	    	String[] tmp= ret_client_string.split("\n");
    	    	inMove=false;
    	    	if (tmp[tmp.length-1].equals(config.Descriptor.getFirstStringValue("STATUSOK"))) {
    	    		ret_client_string="OK";
    	    	} else if (tmp[tmp.length-1].equals(config.Descriptor.getFirstStringValue("STATUSMOVE"))) {
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
    		device.set_source( new Integer(config.Descriptor.getFirstStringValue("SOURCE")).intValue() );
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


    
    void spec_getMotor(String motorName){
    }
    
    /*
     *  STACMotorPosition=(MeasuredMotorPosition-DefinedOffset)/DefinedScale
     * @see stacgui.Stac_BCMplugin#getMotorPosition(java.lang.String)
     */
    public double getMotorPosition(String motorName) throws Exception {
    	return convertMotorPosition_Plugin2Stac(motorName,tango_getAttr(config.Descriptor.getFirstStringValue(motorName)));
    }
    
    public void setMotorPosition(String motorName,double newValue) throws Exception {
    	double currentValue=getMotorPosition(motorName);
    	double actualOffset=config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	double newOffset=-newValue*config.Descriptor.getDoubleValueAt(motorName,specMulf)+
			currentValue*config.Descriptor.getDoubleValueAt(motorName,specMulf)+
			config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	
    	config.Descriptor.setDoubleValueAt(motorName,specOffset,newOffset);
    	config.updateConfiguration(config.Descriptor,config.Descriptor);
    	//updateConfiguration(motorName,specOffset,newOffset);
    	return;
    }
    
    public void moveMotor(String motorName,double newValue) throws Exception {
    	tango_setAttr(config.Descriptor.getFirstStringValue(motorName),convertMotorPosition_Stac2Plugin(motorName,newValue));
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
    	AttrInfo attr_cfg=tango_getAttrProperties(config.Descriptor.getFirstStringValue(motorName));
    	double minpos=convertMotorPosition_Plugin2Stac(motorName,new Double(attr_cfg.min_value).doubleValue());
    	double maxpos=convertMotorPosition_Plugin2Stac(motorName,new Double(attr_cfg.max_value).doubleValue());
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
		String ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("SYNC_MOVE"),double_array+string_array);
		boolean moving=true;
		do {
			ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("STATUSREQUEST"),"");
	    	String[] tmp= ret_client_string.split("\n");
	    	if (tmp[tmp.length-1].equals(config.Descriptor.getFirstStringValue("STATUSOK"))) {
	    		ret_client_string="DONE";
	    		moving=false;
	    	} else if (!tmp[tmp.length-1].equalsIgnoreCase(config.Descriptor.getFirstStringValue("STATUSMOVE"))) {
	    		ret_client_string="FAILED";
	    		moving=false;
	    	}
		} while (moving);
		Stac_Out.println(ret_client_string);
    	
    	//otherwise simply move the motors
    	//moveMotors(newPositions);
    	return;
    	
    }
    
    public void grabImage() throws Exception {
    	//send tango command if available
		Stac_Out.print("Grabbing Image "+"...");
		String ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("GRAB_IMG"),"");
		{
			//writes the img to a file
		}
		Stac_Out.println(ret_client_string);
    	
    	return;
    	
    }
     
    
    public void initMotors() throws Exception {
    	return;
    }

    /**
     * checks dynamic limits by underlying TINE server if 
     * CHECKPOSITION service is available, 
     * otherwise basic motor limit check
     */    
    public boolean checkDatumTrans(Point3d dat,Point3d trans) {
    	try {
    		//TODO: how to get the answer, hence falback solution via exception
    		if (true)
    			throw new Exception("fallback solution");
    		
			//CHECKPOSITION
			String double_array = new String("[ ");
			String string_array = new String("[ ");
			String motorName[] = new String [] {"Omega","Kappa","Phi","X","Y","Z"};
			double motorPos[] = new double[] {dat.x,dat.y,dat.z,trans.x,trans.y,trans.z};
			for(int i=0;i<motorName.length;i++){
				//motor
				if (i>0)
					string_array=string_array.concat(", ");
				string_array=string_array.concat(config.Descriptor.getFirstStringValue(motorName[i])+" ");
				//value
				if (i>0)
					double_array=double_array.concat(", ");
				double_array=double_array.concat(""+convertMotorPosition_Stac2Plugin(motorName[i],motorPos[i])+" ");
			}
			string_array=string_array.concat(" ] ");
			double_array=double_array.concat(" ] ");
			Stac_Out.print("Checking DatumTrans Position "+double_array+"...");
			String ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("CHECKPOSITION"),double_array+string_array);
			boolean moving=true;
				// how to get the answer???
				//ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("STATUSREQUEST"),"");
				//String[] tmp= ret_client_string.split("\n");
				//if (tmp[tmp.length-1].equals(config.Descriptor.getFirstStringValue("STATUSOK"))) {
				//	ret_client_string="DONE";
				//} else if (!tmp[tmp.length-1].equalsIgnoreCase(config.Descriptor.getFirstStringValue("STATUSMOVE"))) {
				//	ret_client_string="FAILED";
			Stac_Out.println(ret_client_string);
		} catch (Exception e) {
			return super.checkDatumTrans(dat, trans);
		}
		return false;
    }
    
    public double convertMotorPosition_Plugin2Stac(String motorName,double pluginPos) {
    	double stacPos=(pluginPos-config.Descriptor.getDoubleValueAt(motorName,specOffset))/config.Descriptor.getDoubleValueAt(motorName,specMulf);
    	return stacPos;
    }
    
    public double convertMotorPosition_Stac2Plugin(String motorName,double stacPos) {
    	double pluginPos=stacPos*config.Descriptor.getDoubleValueAt(motorName,specMulf)+config.Descriptor.getDoubleValueAt(motorName,specOffset);
    	if (motorName.equalsIgnoreCase("Omega")||
        		motorName.equalsIgnoreCase("Kappa")||
        		motorName.equalsIgnoreCase("Phi")) {
        		//fix because tango server handles the limits on its own
        		ParamTable params;
    			try {
    				params = getMotorParams(motorName);
    				pluginPos-=params.getFirstDoubleValue("minpos");
    				pluginPos=pluginPos-(((pluginPos<0)?-1:0)+(double)((int)(pluginPos/360.0)))*360.0;
    				pluginPos+=params.getFirstDoubleValue("minpos");
    			} catch (Exception e) {
    			}
    	}
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
		return "using BCM-tango from S. Brockhauser";
	}
    
    public void centerNeedle() throws Exception {
    	//send tango command if available
    	String double_array = new String("[ ");
    	String string_array = new String("[ ");
		string_array=string_array.concat(" ] ");
    	double_array=double_array.concat(" ] ");
		Stac_Out.print("Centering a Needle "+"...");
		String ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("CENTER_NEEDLE"),double_array+string_array);
		boolean moving=true;
		do {
			ret_client_string = tango_CMD(config.Descriptor.getFirstStringValue("STATUSREQUEST"),"");
	    	String[] tmp= ret_client_string.split("\n");
	    	if (tmp[tmp.length-1].equals(config.Descriptor.getFirstStringValue("STATUSOK"))) {
	    		ret_client_string="DONE";
	    		moving=false;
	    	} else if (!tmp[tmp.length-1].equalsIgnoreCase(config.Descriptor.getFirstStringValue("STATUSMOVE"))) {
	    		ret_client_string="FAILED";
	    		moving=false;
	    	}
		} while (moving);
		Stac_Out.println(ret_client_string);
    	
		Stac_Out.println("Centering precision: "+tango_getAttr(config.Descriptor.getFirstStringValue("CENTERNEEDLERADIUS")));
		
    	return;
    	
    }
    
    
    
}


