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

//import jive.ExecDev;

//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;

import de.desy.tine.client.*;
import de.desy.tine.dataUtils.*;
import de.desy.tine.definitions.*;
import de.desy.tine.queryUtils.*;
import de.desy.tine.structUtils.*;
import de.desy.tine.types.*;



public class Stac_BCMplugin_tine extends Stac_BCMplugin_base implements Stac_BCMplugin {
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
	String     tineURL;
	final int specName =0;
	final int specMulf =1;
	final int specOffset=2;
	
	// TANGO device stuff
	jive.DevHelper device;
	String device_name = null;
	String session_id = null;
	int nb_commands;
	//CmdInfo[] commands;
	String[] attrs;
	//TineAttrInfo[] attr_configs;
	final String SEP_COMMAND_NAME = "_____________";
	int getto_index;
	int setto_index;
	int setlim_index;
	public static int answerLimit = 2048;
	
	//final ExecDev p = new ExecDev();	     
	
	
	
  		
  		
	
    public Stac_BCMplugin_tine () {
		
	}
    
    	//@Override
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
    		tineURL=config.Descriptor.getFirstStringValue("TINEURL");
    		//specversion="artemis2:sandor";
    		//specversion="artemis2:eh4";
    	} catch (Exception e) {
    	}
    	
    	
    	if( !tine_client_start( tineURL ) ) {
    		Stac_Out.println("Could not connect to TINE device!");
    	}
    	
    	
    }
    	
    	public boolean tine_client_start (String tineURL) {
    		try {
        		//prepare TINE environement
    			if (config.Descriptor.getConcatenatedStringValue("TINEENS").compareToIgnoreCase("NOENS")==0) {
					//try without ENS
					TLinkFactory.getInstance().setAllowNetworkAddressResolution(true);
    			} else {
    				String cshosts = "FecName,Protocol,FecNetwork,FecNode,IPaddr,Description,Location,Hardware,Responsible\n";
    				cshosts= cshosts+config.Descriptor.getConcatenatedStringValue("TINEENS")+"\n";
    				FileWriter fos = new FileWriter("cshosts.csv");
    				fos.write(cshosts);
    				fos.close();
    			}
        		//start tine
				tine_readState();
			} catch (Exception e) {
				return false;
			}
    		
    		return true;
    	}
    	
/*
    	public void tine_poll(TineListener listener, int interval) 
        {
            stop();
            TineClientCallback callback = new TineClientCallback(listener);
            mOpenLink = new TLink(mDevice.getFullDeviceName(),getName(),getDataBuffer(),null,TAccess.CA_READ);
            if (mOpenLink==null)
                throw new TineCreateLinkException();
            if (mOpenLink.attach(TMode.CM_POLL, callback, interval) < 0)
            {
                stop();
                throw new TineAttachException(mOpenLink);
            }
        }

        public void tine_monitor(TineListener listener, int interval) throws TineCreateLinkException, TineAttachException
        {
            stop();
            TineClientCallback callback = new TineClientCallback(listener);
            mOpenLink = new TLink(mDevice.getFullDeviceName(),getName(),getDataBuffer(),null,TAccess.CA_READ);
            if (mOpenLink==null)
                throw new TineCreateLinkException();
            if (mOpenLink.attach(TMode.CM_DATACHANGE, callback, interval) < 0)
            {
              stop();
              throw new TineAttachException(mOpenLink);
            }
        }
*/
        public double tine_readDouble(String name) throws Exception
        {  
        	double[] refdat = new double[1];

        	TDataType buff=new TDataType (refdat);
        	tine_write_read(name,null,buff,TAccess.CA_READ);
            return refdat[0];
        }
        
        public String tine_WriteReadString(String name,String str) throws Exception
        {  
        	String refdat = new String("                                                                                          ");

        	TDataType buffR=new TDataType (refdat);
        	TDataType buffW=new TDataType (str);
        	refdat=tine_write_read(name,buffW,buffR,(short)(TAccess.CA_WRITE|TAccess.CA_READ));
            return refdat;
        }
        
        /**
         * 
         * @param fullname
         * @param name
         * @return integer:
         *   0 - RUNNING
         *   1 - STANDBY
         *   2 - ALARM
         *   3 - FAULT
         * @throws Exception
         */
        public int tine_readState() throws Exception
        {  
        	NAME48I[] refdat = new NAME48I[1];

        	TDataType buff=new TDataType (refdat);
        	tine_write_read(config.Descriptor.getFirstStringValue("STATUSREQUEST"),null,buff,TAccess.CA_READ);
            return refdat[0].ival;
        }
/*
        public void tine_read(String name,TDataType buff) throws Exception
        {  
            TLink lnk = new TLink(config.Descriptor.getFirstStringValue("TINEURL"),name,buff,null,TAccess.CA_READ);
            if (lnk==null)
            {
        		StringBuffer errmsg = new StringBuffer();
        		//errmsg.append("Exception: " + e.getClass().getName() + "\n");
        		errmsg.append("message:\t" + "Could not create read Link to " +  name+ "\n");
    		
        		Stac_Out.println(errmsg.toString());
    		return;
            }

            int res;
            try {
            if ((res=lnk.execute(1000)) != 0)
            {
        		StringBuffer errmsg = new StringBuffer();
        		errmsg.append("message:\t" + "Could not read " +name+ " ("+res+")"  + "\n");
    		
        		Stac_Out.println(errmsg.toString());
            }
            } catch (Exception ex){
        		StringBuffer errmsg = new StringBuffer();
        		errmsg.append("message:\t" + "Could not read " +name+ " ("+ex.getMessage()+")"  + "\n");
    		
        		Stac_Out.println(errmsg.toString());
            	
            }
            finally {try{lnk.cancel();} catch (Exception ex){}} 
        }
        
        public void tine_write(String name,TDataType buff) throws Exception
        {
            //buff.putData(value);
            TLink lnk = new TLink(config.Descriptor.getFirstStringValue("TINEURL"),name,null,buff,TAccess.CA_WRITE);
            if (lnk==null)
            {
        		StringBuffer errmsg = new StringBuffer();
        		//errmsg.append("Exception: " + e.getClass().getName() + "\n");
        		errmsg.append("message:\t" + "Could not create Link to " +  name+ "\n");
    		
        		Stac_Out.println(errmsg.toString());
    		return;
            }

            try
            {
            	int res;
                if ((res=lnk.execute(1000)) != 0)
                {
            		StringBuffer errmsg = new StringBuffer();
            		//errmsg.append("Exception: " + e.getClass().getName() + "\n");
            		errmsg.append("message:\t" + "Could not set value to " +name + " ("+res+")" + "\n");
        		
            		Stac_Out.println(errmsg.toString());
                }
            }catch (Exception ex){
        		StringBuffer errmsg = new StringBuffer();
        		errmsg.append("message:\t" + "Could not set value to " +name+ " ("+ex.getMessage()+")"  + "\n");
    		
        		Stac_Out.println(errmsg.toString());
            	
            }
            finally
            {
                try{lnk.cancel();} catch (Exception ex){}
            }
        }
*/        
        public String tine_write_read(String name,TDataType buffW,TDataType buffR,short TAmode) throws Exception
        {
        	String resp="";
            //buff.putData(value);
            TLink lnk = new TLink(config.Descriptor.getFirstStringValue("TINEURL"),name,buffR,buffW,TAmode);//TAccess.CA_READ|TAccess.CA_WRITE);
            if (lnk==null)
            {
        		StringBuffer errmsg = new StringBuffer();
        		errmsg.append("message:\t" + "Could not create Link to " +  name+ "\n");
        		Stac_Out.println(errmsg.toString());
        		return "FAILED";
            }

            try
            {
            	int res;
                if ((res=lnk.execute(1000)) != 0)
                {
            		StringBuffer errmsg = new StringBuffer();
            		errmsg.append("message:\t" + "Could not "+(((TAmode&TAccess.CA_WRITE)!=0)?"[Write]":"")+(((TAmode&TAccess.CA_READ)!=0)?"[Read]":"")+" to " +name + " ("+res+")" + "\n");
            		Stac_Out.println(errmsg.toString());
            		resp=resp+res;
                } else {
                	if (buffR!=null && buffR.dFormat==TFormat.CF_TEXT) {
                		resp= new String((char[])buffR.getDataObject());
                		int size =resp.indexOf(0);
                		if (size>0)
                			resp=resp.substring(0,size);
                		else if (size==0)
                			resp="DONE";
                	} else
                		resp="DONE";
                }
            }catch (Exception ex){
        		StringBuffer errmsg = new StringBuffer();
        		errmsg.append("message:\t" + "Could not "+(((TAmode&TAccess.CA_WRITE)!=0)?"[Write]":"")+(((TAmode&TAccess.CA_READ)!=0)?"[Read]":"")+" to " +name + " ("+ex.getMessage()+")" + "\n");
        		Stac_Out.println(errmsg.toString());
        		resp=resp+" EXCEPTION";
            }
            finally
            {
                try{lnk.cancel();} catch (Exception ex){}
            }
            return resp;
        }

        public void tine_writeDouble(String name,double value) throws Exception
        {
        	double[] refdat = new double[1];
        	refdat[0]=value;
        	TDataType buff=new TDataType (refdat);
        	tine_write_read(name,buff,null,TAccess.CA_WRITE);
        }

        public String tine_writeCMD(String name,TDataType buff) throws Exception
        {
        	if (buff == null) {
        		double[] refdat = new double[1];
        		refdat[0]=0;
        		buff=new TDataType (refdat);
        	}
        	return tine_write_read(name,buff,null,TAccess.CA_WRITE);
        }

   	

    
    /*
     *  STACMotorPosition=(MeasuredMotorPosition-DefinedOffset)/DefinedScale
     * @see stacgui.Stac_BCMplugin#getMotorPosition(java.lang.String)
     */
    public double getMotorPosition(String motorName) throws Exception {
    	return convertMotorPosition_Plugin2Stac(motorName,tine_readDouble(config.Descriptor.getFirstStringValue(motorName)));
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
    	tine_writeDouble(config.Descriptor.getFirstStringValue(motorName),convertMotorPosition_Stac2Plugin(motorName,newValue));
		String ret_client_string=poll();
		Stac_Out.println(ret_client_string);
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
    	String minmax=tine_WriteReadString(config.Descriptor.getFirstStringValue("GETMOTORLIMITS"),(String)config.Descriptor.getValueAt(motorName,3));
    	String min_value=minmax.split(",")[0];
    	String max_value=minmax.split(",")[1];
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
    
    public String poll() {
    	String ret_client_string="";
    	boolean moving=true;
    	try {
    		do {
    			int ret;
    			ret = tine_readState();
    			if (ret==config.Descriptor.getFirstIntegerValue("STATUSOK")) {
    				ret_client_string="DONE";
    				moving=false;
    			} else if (ret!=config.Descriptor.getFirstIntegerValue("STATUSMOVE")) {
    				ret_client_string="FAILED ("+ret+")";
    				moving=false;
    			}
    		} while (moving);
    	} catch (Exception e) {
    		ret_client_string="FAILED with Exception";
    	}
    	return ret_client_string;
    }
    
    public void moveMotorsSyncronized(ParamTable newPositions) throws Exception {
    	//send tango command if available
    	String string_array = new String("");
    	for(int i=0;i<newPositions.pnames.size();i++){
    		//motor
    		String motorName = (String)newPositions.pnames.elementAt(i);
    		if (i>0)
    			string_array=string_array.concat(",");
    		//tine sync_motor uses different motor names than the direct property names!!!
    		string_array=string_array.concat((String)config.Descriptor.getValueAt(motorName,3));
			string_array=string_array.concat("=");
    		//value
        	double newpos=newPositions.getFirstDoubleValue(motorName);
        	string_array=string_array.concat(""+convertMotorPosition_Stac2Plugin(motorName,newpos));
    	}
    	//string_array="cx=1258";//,cy=40";
		Stac_Out.print("Synchronised DatumTrans Movement "+string_array+"...");
		tine_writeCMD(config.Descriptor.getFirstStringValue("SYNC_MOVE"),new TDataType(string_array));
		String ret_client_string=poll();
		Stac_Out.println(ret_client_string);
    	
    	//otherwise simply move the motors
    	//moveMotors(newPositions);
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
    public boolean checkDatumTrans(ParamTable BCM_Descriptor,Point3d dat,Point3d trans) {
    	//CHECKPOSITION
    	String string_array = new String("");
    	String motorName[] = new String [] {"Omega","Kappa","Phi","X","Y","Z"};
    	double motorPos[] = new double[] {dat.x,dat.y,dat.z,trans.x,trans.y,trans.z};
    	for(int i=0;i<motorName.length;i++){
    		//motor
    		if (i>0)
    			string_array=string_array.concat(",");
    		//tine sync_motor uses different motor names than the direct property names!!!
    		string_array=string_array.concat((String)config.Descriptor.getValueAt(motorName[i],3));
			string_array=string_array.concat("=");
    		//value
        	string_array=string_array.concat(""+convertMotorPosition_Stac2Plugin(motorName[i],motorPos[i]));
    	}
    	String ret_client_string;
		try {
			ret_client_string = tine_writeCMD(config.Descriptor.getFirstStringValue("CHECKPOSITION"),new TDataType(string_array));
			if (ret_client_string.equalsIgnoreCase("DONE"))
				return true;
		} catch (Exception e) {
			return super.checkDatumTrans(BCM_Descriptor,dat, trans);
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
		return "using BCM-tine from S. Brockhauser";
	}
    
    public void centerNeedle() throws Exception {
    	//send tango command if available
		Stac_Out.print("Centering a Needle "+"...");
		tine_writeCMD(config.Descriptor.getFirstStringValue("SAMPLELOOPTYPEPROP"),new TDataType(config.Descriptor.getFirstStringValue("SAMPLELOOPTYPE")));
		tine_writeCMD(config.Descriptor.getFirstStringValue("CENTER_NEEDLE"),new TDataType(""));
		String ret_client_string=poll();
		Stac_Out.println(ret_client_string);
    	
		Stac_Out.println("Centering precision: "+tine_readDouble(config.Descriptor.getFirstStringValue("CENTERNEEDLERADIUS")));
		
    	return;
    	
    }
    
    
    
}


