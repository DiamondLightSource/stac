package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;

import stac.core.*;
import stac.gui.*;
import stac.vbcm.*;
import stac.modules.BCM.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Vector;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * 
 * vBCM: BCM plugin for driving a virtual Beamline
 *
 * @author sudol
 *
 */

public class Stac_BCMplugin_vBCM extends Stac_BCMplugin_base implements Stac_BCMplugin {
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
	String     vBCMconf;
	final int specName =0;
	final int specMulf =1;
	final int specOffset=2;
	
	public void generateCCPNCollisionMap(String fp,Double [] dtox, double res) {
		if (vbcm!=null)
			vbcm.generateCCPNCollisionMap(fp,dtox,res);
	}

	
	
	String spec_mv() {
		return new String("mv");
	}
	String spec_set() {
		return new String("set");
	}
	String spec_getPosition() {
		return new String("getPosition");
	}
	String ProDC_MotorNameSuffix() {
		return new String("");		
	}
	
	
	//final boolean fastexec=false;
	//final boolean fastexec=true;
	
	//spec_communicator spec_comm = new spec_communicator();
	//spec_communicator_listener spec_comm = new spec_communicator_listener(10000);
	public vBCM vbcm = null;
	
	protected void closePlugin () {
		if (vbcm!=null)
			vbcm.close();
	}
	
    public Stac_BCMplugin_vBCM () {}
    
	@Override
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-vBCM from S. Brockhauser";
	}
 
    
    @Override
    public void initPlugin() {
    	
    	//read the actual motorDescriptor
    	//motorDescriptor= new ParamTable();
    	//read the specversion
    	//read_specdef(System.getProperty("BCMDEF"));
    	config.getConfiguration();
    	try {
    		File cfg= new File(System.getProperty("BCMDEF"));
    		vBCMconf=cfg.getParent()+File.separator+config.Descriptor.getFirstStringValue("vBCM_configuration");
    	} catch (Exception e) {
    		vBCMconf=System.getProperty("STACDIR")+"/config/vBCM.dat";
    	}
    	
    	//start the vBCM
    		try {
    			//if allowGUI, it creates a FRAME with VBS
    			//  otherwise it creates a panel which will not be displayed
    			vbcm = new vBCM(vBCMconf,allowGUI);
    			vbcm.activateVBCM(vbcm);
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
//			if (name.equals("BCM_Plugin") || name.equals("vBCM_configuration")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else {
//			  try {
//				Vector params = new Vector();
//				String localid =tmp2[1+offset];            params.addElement(localid);
//				Double mulfac = new Double(tmp2[2+offset]);params.addElement(mulfac);
//				Double offsetValue = new Double(tmp2[3+offset]);params.addElement(offsetValue);
//				motorDescriptor.setValueList(name,params);
//			  } catch (Exception e) {
//				  //unexpected line
//			  }
//			}
//        }
//      	
//      }
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
    
	public boolean checkDatumTrans(ParamTable BCM_Descriptor,Point3d dat, Point3d trans) {
		try {
			moveMotor("Omega",dat.x);
			moveMotor("Kappa",dat.y);
			moveMotor("Phi",dat.z);
			moveMotor("X",trans.x);
			moveMotor("Y",trans.y);
			moveMotor("Z",trans.z);
			if (vbcm.getCollisionInfo())
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;		
	}
    
    public double getMotorPosition(String motorName) throws Exception {
    	return convertMotorPosition_Plugin2Stac(motorName,vbcm.getMotorPos(config.Descriptor.getFirstStringValue(motorName)));
    }
    
    public void moveMotor(String motorName,double newValue) throws Exception {
    	ParamTable newPos=new ParamTable();
    	newPos.setSingleDoubleValue(motorName,newValue);
    	moveMotors(newPos);
    	return;
    }    
    
    public void moveMotors(ParamTable newPositions) throws Exception {
    	ParamTable newPos=new ParamTable();
    	for(int i=0;i<newPositions.pnames.size();i++){
    		String motorNameStac = (String)newPositions.pnames.elementAt(i);
    		String motorNamePlugin = config.Descriptor.getFirstStringValue(motorNameStac);
    		newPos.setSingleDoubleValue(motorNamePlugin,convertMotorPosition_Stac2Plugin(motorNameStac,newPositions.getFirstDoubleValue(motorNameStac)));
    	}
    	vbcm.MoveMotors(newPos);
    	return;
    	
    }
    
    public void centerNeedle() throws Exception {
    	vbcm.CenterNeedle();		
    }

    public void moveMotorsSyncronized(ParamTable newPositions) throws Exception {
    	moveMotors(newPositions);
    	return;
    }

    
	public ParamTable getMotorParams(String motorName) throws Exception {
		// TODO Auto-generated method stub
    	ParamTable params=new ParamTable();
    	Vector limits=vbcm.getMotorLimits(config.Descriptor.getFirstStringValue(motorName));
    	double minpos=convertMotorPosition_Plugin2Stac(motorName,((Double)limits.elementAt(0)).doubleValue());
    	double maxpos=convertMotorPosition_Plugin2Stac(motorName,((Double)limits.elementAt(1)).doubleValue());
		params.setSingleDoubleValue("pos",getMotorPosition(motorName));
		params.setSingleDoubleValue("minpos",Math.min(minpos,maxpos));
		params.setSingleDoubleValue("maxpos",Math.max(minpos,maxpos));
		params.setSingleDoubleValue("speed",getMotorSpeed(motorName));
		params.setSingleDoubleValue("minspeed",getMotorSpeedLowLimit(motorName));
    	return params;
	}
    
    
    public void initMotors() throws Exception {
    	moveMotor("Omega",0.0);
    	moveMotor("Kappa",0.0);
    	moveMotor("Phi",0.0);
    	moveMotor("X",0.0);
    	moveMotor("Y",0.0);
    	moveMotor("Z",0.0);
    	return;
    }
    
//    public boolean checkDatumTrans(Point3d dat,Point3d trans) {
//    	boolean valid=true;
//    	if (dat.y>270.0)
//    		return false;
//    	return valid;
//    }
    
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
				if (name.equalsIgnoreCase(spec_motorName+ProDC_MotorNameSuffix())){
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
 
	/**
	 * TC space:
	 *  - X : 1 0 0
	 *  - Y : 0 1 0
	 *  - Z : 0 0 1
	 *  - kappa/phi Dir/Loc : measured
	 * RC space:
	 *  - beam  : 1 0 0
	 *  - omega ~?
	 *  - detX  :
	 *  - detY  :
	 *  - omega/kappa/phi Dir : measured
	 *  
	 * RC - TC assumptions:
	 *  - Z is parallel to Omega
	 * RC - Realworld assumptions:
	 *  - detX is horizontal
	 *  - detY is vertical
	 * TC - RealWorld assumptions:
	 *  - X/Y/Z motors define a normalised and orthogonalised coordinate frame
	 *  
	 * @param Descriptor containing the new calibration settings (it is not the vbcm configuration!)
	 */
	public void adjustCalibration(ParamTable Descriptor) {
		ParamTable conf = new ParamTable(Descriptor);
		conf.add(config.Descriptor);
		vbcm.adjustCalibration(conf);
	}

	
    
}


