package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.gui.*;
import stac.modules.BCM.*;

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


public class Stac_BCMplugin_cbass extends Stac_BCMplugin_base implements Stac_BCMplugin {
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
	final int specName =0;
	final int specMulf =1;
	final int specOffset=2;
	
	
 		
	
    public Stac_BCMplugin_cbass () {
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
	    //tangoURL=motorDescriptor.getFirstStringValue("TANGOURL");
    		//specversion="artemis2:sandor";
    		//specversion="artemis2:eh4";
    	} catch (Exception e) {
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
//			if ( // only 1 parameter
//					name.equals("BCM_Plugin") ||
//					name.equals("CBASS_PIPE") ||
//					name.equals("STAC_PIPE") 
//					) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else if ( // general motor parameters
//					name.equals("Omega") ||
//					name.equals("Kappa") ||
//					name.equals("Phi") ||
//					name.equals("X") ||
//					name.equals("Y") ||
//					name.equals("Z")
//					) {
//				Vector params = new Vector();
//				String localid =tmp2[1+offset];                 params.addElement(localid);
//				Double mulfac = new Double(tmp2[2+offset]);     params.addElement(mulfac);
//				Double offsetValue = new Double(tmp2[3+offset]);params.addElement(offsetValue);
//				motorDescriptor.setValueList(name,params);				
//			} else {
//				String value =tmp1[l].substring(tmp1[l].indexOf(name)+name.length());
//				motorDescriptor.setSingleValue(name,value);				
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
    


    
    /*
     *  STACMotorPosition=(MeasuredMotorPosition-DefinedOffset)/DefinedScale
     * @see stacgui.Stac_BCMplugin#getMotorPosition(java.lang.String)
     */
    public double getMotorPosition(String motorName) throws Exception {
    	String motorparams=getMotorParamString(motorName);
    	return getMotorPosition(motorName,motorparams);
    }
    
    public double getMotorPosition(String motorName,String motorparams) throws Exception {
    	return convertMotorPosition_Plugin2Stac(motorName,new Double(getParam(motorparams,"pos")));
    }

    String getParam(String motorparams,String mask) throws IOException {
    	String value=new String("0");
    	boolean set=false;
        String tmp1[] = motorparams.split("\n");
        for (int l=0;l<tmp1.length;l++) {
        	if (tmp1[l].startsWith("#",0))
        		continue;
        	String tmp2[] = tmp1[l].split("\\s+");
        	int offset=0;
        	//if (tmp2[0].length()==0)
        	//	offset++;
			String name = tmp2[0+offset];
			if (name.equals(mask)) {
				value=tmp2[1+offset];
				set=true;
			}
        }
        if(!set) {
        	throw new IOException("Failed to read cbass parameter");
        }
        
        return value;
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
    

    public synchronized String cbass_exec(String command) throws Exception  {
        String syscom = "rm -f "+config.Descriptor.getFirstStringValue("STAC_PIPE")+"; mkfifo "+config.Descriptor.getFirstStringValue("STAC_PIPE")+"; echo "+command+" "+config.Descriptor.getFirstStringValue("STAC_PIPE")+" > "+config.Descriptor.getFirstStringValue("CBASS_PIPE")+"; cat < "+config.Descriptor.getFirstStringValue("STAC_PIPE")+" > "+session.getWorkDir()+"cbass.out";
        Stac_Out.println(syscom);
        Process p = new ProcessBuilder("sh","-c",syscom).start();
        p.waitFor();
      	Util utils=new Util();
        //read spec.dat
      	String result = utils.opReadCl(session.getWorkDir()+"cbass.out");
        return result;
    }

    public void moveMotor(String motorName,double newValue) throws Exception {
    	System.out.println("cbass call\n");
        cbass_exec("move_axis_absolute "+config.Descriptor.getFirstStringValue(motorName)+" "+convertMotorPosition_Stac2Plugin(motorName,newValue));
    	return;
    }    
    
    public double getMotorSpeed(String motorName) throws Exception {
		return 1;
    }
    
    public String getMotorParamString(String motorName) throws Exception {
    	return cbass_exec("getmotorparams "+config.Descriptor.getFirstStringValue(motorName));
    }
    //standard values served:
    // + pos
    // + minpos
    // + maxpos
    // + speed
    // + minspeed
    public ParamTable getMotorParams(String motorName) throws Exception {
    	ParamTable params=new ParamTable();
    	String paramstr=getMotorParamString(motorName);
        params.setSingleDoubleValue("pos",convertMotorPosition_Plugin2Stac(motorName,new Double(getParam(paramstr,"pos"))));
    	double minpos=convertMotorPosition_Plugin2Stac(motorName,new Double(getParam(paramstr,"minpos")));
    	double maxpos=convertMotorPosition_Plugin2Stac(motorName,new Double(getParam(paramstr,"maxpos")));
		params.setSingleDoubleValue("minpos",Math.min(minpos,maxpos));
		params.setSingleDoubleValue("maxpos",Math.max(minpos,maxpos));
		params.setSingleDoubleValue("speed",1.0);
		params.setSingleDoubleValue("minspeed",1.0);
    	return params;
    }
    
    public double getMotorSpeedLowLimit(String motorName) throws Exception {
		return 1;
    }
    
    public void setMotorSpeed(String motorName,double newValue) throws Exception {
    	return;
    }
    
    public void moveMotors(ParamTable newPositions) throws Exception {
    	for(int i=0;i<newPositions.pnames.size();i++){
    		String motorName = (String)newPositions.pnames.elementAt(i);
        	double newpos=newPositions.getFirstDoubleValue(motorName);
        	moveMotor(motorName,newpos);
    	}
    	return;
    	
    }
        
    public void moveMotorsSyncronized(ParamTable newPositions) throws Exception {
        moveMotors(newPositions);
	return;
    }

    public void initMotors() throws Exception {
    	cbass_exec(config.Descriptor.getFirstStringValue("INITSTRING"));
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
		return "using BCM-cbass from S. Brockhauser and J.F. Skinner";
	}
    
    
}


