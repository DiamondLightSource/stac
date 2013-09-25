package stac.modules.BCM;

import stac.modules.Alignment.Stac_AlignmentReqMsg;
import stac.modules.Alignment.Stac_AlignmentRespMsg;
import stac.modules.BCM.plugins.*;
import stac.core.*;
import stac.gui.*;

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

import org.sudol.sun.util.Util;

import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


public class StacBCM extends Stac_PluginSlot {
	//Stac_BCMplugin activePlugin;
	public Stac_BCMplugin activeVBSPlugin=null;
	//ParamTable BCM_Descriptor;
	org.sudol.sun.util.Util utils;
	Stac_Configuration calib = new Stac_Configuration("");

	@Override
	public boolean doAcceptJob(Object job) {
		return false;
	}

	public void generateCCPNCollisionMap(String fp,Double [] dtox, double res) {
		if (activeVBSPlugin!=null)
			((Stac_BCMplugin_vBCM)activeVBSPlugin).generateCCPNCollisionMap(fp,dtox,res);
	}
	
	/**
	 * Initialize the BCM by 
	 * + reading the configuration file (BCMDEF) and
	 * + starting the appropriate BCM_Plugin
	 */
    public StacBCM (Stac_Session session) {
    	super(session,"BCM");
    	calib.getConfiguration();
    	utils= new Util();
    	String BCMplugin=null;
    	activePlugin=changePlugin("BCMplugin_base","");
    	//get the active plugin from the descriptor
    	//BCM_Descriptor= new ParamTable();
    	//utils.read_section(System.getProperty("BCMDEF"),""+SlotType+"plugin",BCM_Descriptor);
    	//config.getConfiguration();
    	String pluginInstName="";
    	try{
    		BCMplugin=config.Descriptor.getFirstStringValue("BCM_Plugin");
    		pluginInstName=(String)config.Descriptor.getValueAt("BCM_Plugin",1);
    	} catch (Exception e) {
   		}
    	if (!BCMplugin.equals("BCMplugin_base"))
    		activePlugin=changePlugin(BCMplugin,pluginInstName);
    	
    	//VBS
    	String VBSname=null;
    	try{
    		VBSname=config.Descriptor.getFirstStringValue("vBCM_configuration");
    	} catch (Exception e) {
   		}
    	if (VBSname!=null) {
    		activeVBSPlugin=(Stac_BCMplugin)addPlugin("BCMplugin_vBCM",VBSname);
    		if (activeVBSPlugin!=null)
    			((Stac_BCMplugin_vBCM)activeVBSPlugin).adjustCalibration(calib.Descriptor);
    	}

    	//debug test
//    	{
//    		if(false)
//    		{
//    			for(int i=0;i<100;i++) {
//    				try {
//    	            String outfile="spec_cmd.out";
//    	            // Create channel on the destination
//    	            FileWriter dstStream = new FileWriter(outfile);
//    	            // Copy file contents from source to destination
//    	            dstStream.write(new String("#! /usr/bin/env python\n"+
//    	            		"import sys\n"+
//    						"from SpecClient import *\n"+
//    						"m=SpecMotor.SpecMotor('%s','%s',500)\n"+
//    						"p=m."+"()\n"+
//    						"position_of_the_specified_motor_is: "+i+"\n"+
//    						"cmd=SpecCommand.SpecCommand('','%s',500)\n"+
//    						"print 'speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"velocity\")')\n"+
//    						"print 'lowest_speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"base_rate\")')\n"+
//    						"print 'stepsize_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"step_size\")')\n"+
//    						"print 'low_limit_of_the_specified_motor_is: ',cmd.executeCommand('"+"')\n"+
//    						"print 'high_limit_of_the_specified_motor_is: ',cmd.executeCommand('"+"')\n"+
//    						"print 'name of the specified motor is: ','%s'"));
//    	            // Close the file
//    	            dstStream.flush();
//    	            dstStream.close();
//    	            {
//    	            	String value=new String("0");
//    	            	boolean set=false;
//    	                //read the results from spec_cmd.out
//    	              	String corr;
//    	              	StacUtil utils=new StacUtil();
//    	                //read spec.dat
//    	              	corr = utils.opReadCl("spec_cmd.out");
//    	                String tmp1[] = corr.split("\n");
//    	                for (int l=0;l<tmp1.length;l++) {
//    	                	if (tmp1[l].startsWith("#",0))
//    	                		continue;
//    	                	String tmp2[] = tmp1[l].split("\\s+");
//    	                	int offset=0;
//    	                	//if (tmp2[0].length()==0)
//    	                	//	offset++;
//    	        			String name = tmp2[0+offset];
//    	        			if (name.equals("position_of_the_specified_motor_is:")) {
//    	        				value=tmp2[1+offset];
//    	        				set=true;
//    	        			}
//    	                }
//    	                if (set) 
//    	                	Stac_Out.println(""+value);
//    	                else
//    	                	Stac_Out.println("NO value set!");
//    	            }
//    	            
//    				} catch (Exception e) {};
//    				
//    			}
//    		}
//    		{
//    			Point3d dat=new Point3d();
//    			for (int i=0;i<10;i++) {
//    				this.getCurrentDatum(dat);
//    				Stac_Out.println("Test datum: "+dat.x+";"+dat.y+";"+dat.z);
//    			}
//    		}
//    	}
    }

    public void closePlugin() {
    	closePlugin((Stac_Plugin)activeVBSPlugin);
    	activeVBSPlugin=null;
    	super.closePlugin();
    }
    
    
    

    //BCM plugin-independent routines    
    
    /**
     * Interprets a line of the specdef file, and actualize the Descriptor 
     * @param specline
     * @param Descr
     */
//    public void interpret_specdef(String specline,ParamTable Descr) {
//        activePlugin.interpret_specdef(specline,Descr);
//    }
    
    /**
     * Reads the BCM configuration file.
     * @param specfile  - name of the file
     * @param Descr - config parameters
     */
//    public void read_specdef(String specfile,ParamTable Descr) {
//    		activePlugin.read_specdef(specfile,Descr);
//      }

    /**
     * Updates the BCM specdef file. 
     * @param newParams: KEYWORD and te new parameters to be written
     */
//    void update_specdef(ParamTable newParams) {
//    	activePlugin.update_specdef(BCM_Descriptor,newParams);
//    }

    /**
     * serves the current Calibration stored in BCM config
     * @param okp
     */
    public void getCalibration(Vector3d okp[]) {
    	((Stac_BCMplugin)activePlugin).getCalibration(calib.Descriptor,okp);
    }
    
    /**
     * updates the BCM config with the new Calibration values
     * @param par
     */
    public void setCalibration(Vector3d[] par) {
    	((Stac_BCMplugin)activePlugin).setCalibration(calib,calib.Descriptor,par);
    	if (activeVBSPlugin!=null)
    		((Stac_BCMplugin_vBCM)activeVBSPlugin).adjustCalibration(calib.Descriptor);
    }
    
    /**
     * imports the Calibration values from gnsfile to BCM config
     * @param gnsfile
     * @param utils contains the tool to read gnsfile
     */
//    public void setCalibration(String gnsfile,StacUtil utils) {
//    	activePlugin.setCalibration(BCM_Descriptor,gnsfile,utils);
//    }
    
    
    //simple routines
    
    /**
     * loads the motor positions from a stream
     * @param motorName - STAC name of the motor
     * @param data - stream containing the necessary information
     *               generally coming from OM descriptor written 
     *               by the data acquasition software 
     * @return the motorposition for STAC
     */
    public double loadMotorPosition(String motorName,String data){
    	return ((Stac_BCMplugin)activePlugin).loadMotorPosition(motorName,data);
    }    
    
    /**
     * sets the curent position to a new STAC value 
     * @param motorName - STAC name of the motor
     * @param newValue - new STAC value of the motor
     */
    public void setMotorPosition(String motorName,double newValue){
    	try {
    		((Stac_BCMplugin)activePlugin).setMotorPosition(motorName,newValue);
    		if (activeVBSPlugin!=null) activeVBSPlugin.setMotorPosition(motorName, newValue);
	} catch (Exception e) {
	}
    	return;
    }
    
    /**
     * moves the motor to a new STAC value 
     * @param motorName - STAC name of the motor
     * @param newValue - new STAC value of the motor
     */
    public void moveMotor(String motorName,double newValue){
    	try {
    		if (activeVBSPlugin!=null) activeVBSPlugin.moveMotor(motorName, newValue);
    		((Stac_BCMplugin)activePlugin).moveMotor(motorName,newValue);
    	} catch (Exception e) {
    	}
    	
    	return;
    }    
    
    /**
     * Used by synchronised movement controll exceptionally.
     * @param motorName - STAC name of the motor
     * @return Returns the current speed of the motor.
     */
    private double getMotorSpeed(String motorName){
    	try {
    	return ((Stac_BCMplugin)activePlugin).getMotorSpeed(motorName);
    	} catch (Exception e) {
    		return 0;
    	}
    	
    }
    
    /**
     * Changed the speed of the motor.
     * Used by synchronised movement controll exceptionally.
     * @param motorName - STAC name of the motor
     * @param newValue - new value of the speed
     */
    private void setMotorSpeed(String motorName,double newValue){
    	try {
    		if (newValue !=0.0 && newValue != 1/0.0) {
    			((Stac_BCMplugin)activePlugin).setMotorSpeed(motorName,newValue);
        		if (activeVBSPlugin!=null) activeVBSPlugin.setMotorSpeed(motorName, newValue);
    		}
    	} catch (Exception e) {
    	}

    	return;
    }
        
    /**
     * move motors on a non-specific way
     * @param newPositions - The list of te STAC motornames and their new values
     */
    public void moveMotors(ParamTable newPositions){
    	try{
    		if (activeVBSPlugin!=null) activeVBSPlugin.moveMotors(newPositions);
    		((Stac_BCMplugin)activePlugin).moveMotors(newPositions);
    	} catch (Exception e) {
    	}

    	return;
    	
    }
    
    /**
     * move motors on a synchronised way
     * @param newPositions - The list of te STAC motornames and their new values
     */
    public void moveMotorsSyncronized(ParamTable newPositions){
    	try {
    		if (activeVBSPlugin!=null) activeVBSPlugin.moveMotorsSyncronized(newPositions);
    		((Stac_BCMplugin)activePlugin).moveMotorsSyncronized(newPositions);
    	} catch (Exception e) {
    	}

    	return;
    	
    }

    /**
     * Initialize the motors, like homing, 
     * settig default offsets...
     * 
     * NOT YET:
     * As a result:
     * Omega,Kappa,Phi must be at 0 position! 
     *
     */
    public void initMotors(){
    	try {
    		if (activeVBSPlugin!=null) activeVBSPlugin.initMotors();
    		((Stac_BCMplugin)activePlugin).initMotors();
    	} catch (Exception e) {
    	}
    	return;
    }
    
    public boolean checkDatumTrans(Point3d dat,Point3d trans) {
    	return ((activeVBSPlugin!=null)?activeVBSPlugin.checkDatumTrans(calib.Descriptor,dat,trans):true) && ((Stac_BCMplugin)activePlugin).checkDatumTrans(calib.Descriptor,dat,trans);
    }
    
    public Vector avoidShadow(Point3d dat,double start, double end,boolean omegaosc) {
    	Vector res = null;
    	if (activeVBSPlugin!=null) {
    		res=activeVBSPlugin.avoidShadow(calib.Descriptor,dat,start,end,omegaosc);
    	} else {
    		res=((Stac_BCMplugin)activePlugin).avoidShadow(calib.Descriptor,dat,start,end,omegaosc);
    	}
    	return res;
    }
    
    public void registerMotorListener(String motorName,MotorWidget_actionAdapter listener) {
    	if (activeVBSPlugin!=null) activeVBSPlugin.registerMotorListener(motorName, listener);
    	((Stac_BCMplugin)activePlugin).registerMotorListener(motorName,listener);    	
    }
        
    /**
     * centers a needle (used for automatic Translation Calibration)
     * since the calibration is not valid at this time, 
     * it makes no sense to perform it within the VBS as well
     *
     */
    public void centerNeedle() {
    	try {
    		((Stac_BCMplugin)activePlugin).centerNeedle();
		} catch (Exception e) {
			Stac_Out.println("Could not perform the centering of the needle!");
		}
    }

    
    
        
    //complex routines
    
    /**
     * get the motorpositions for the STAC translation motors:
     * "X" - along the beam vector<br>
     * "Y" - vertically upwards<br>
     * "Z" - towards the inner wall<br>
     * @param trans returns the translation vector (x,y,z)  
     */
    public void getCurrentTrans(Point3d trans){
    	trans.x=getMotorPosition("X");
    	trans.y=getMotorPosition("Y");
    	trans.z=getMotorPosition("Z");
    }
    
    /**
     * get the motorpositions for the STAC rotation motors:
     * "Omega" - main omega rotation axis that holds<br>
     * "Kappa" - kappa rotation that holds<br>
     * "Phi" - phi rotation<br>
     * @param trans returns the datum vector (o,k,p)  
     */
    public void getCurrentDatum(Point3d dat){
    	dat.x=getMotorPosition("Omega");
    	dat.y=getMotorPosition("Kappa");
    	dat.z=getMotorPosition("Phi");
    }
    
    /**
     * get the motorpositions for the STAC translation and rotation motors:
     * "Omega" - main omega rotation axis that holds<br>
     * "Kappa" - kappa rotation that holds<br>
     * "Phi" - phi rotation<br>
     * "X" - Translation motor along the beam vector<br>
     * "Y" - vertically upwards<br>
     * "Z" - towards the inner wall<br>
     * @param trans returns the datum vector (o,k,p), and the translation vector(o,k,p)    
     */
    public void getCurrentDatumTrans(Point3d dat,Point3d trans){
    	trans.x=getMotorPosition("X");
    	trans.y=getMotorPosition("Y");
    	trans.z=getMotorPosition("Z");
    	dat.x=getMotorPosition("Omega");
    	dat.y=getMotorPosition("Kappa");
    	dat.z=getMotorPosition("Phi");
    }
    
    /*
     * staccoord:
     *  2 - dat in motor ccords
     *  4 - trans in motor coords
     */
    public void getCurrentDatumTrans(Point3d dat,Point3d trans,int motorCoord){
    	getCurrentDatumTrans(dat,trans);
    	if ((motorCoord&2) !=0) {
    		dat.x=((Stac_BCMplugin)activePlugin).convertMotorPosition_Stac2Plugin("Omega", dat.x);
    		dat.y=((Stac_BCMplugin)activePlugin).convertMotorPosition_Stac2Plugin("Kappa", dat.y);
    		dat.z=((Stac_BCMplugin)activePlugin).convertMotorPosition_Stac2Plugin("Phi", dat.z);
    	} 
    	if ((motorCoord&4) !=0) {
    		trans.x=((Stac_BCMplugin)activePlugin).convertMotorPosition_Stac2Plugin("X", trans.x);
    		trans.y=((Stac_BCMplugin)activePlugin).convertMotorPosition_Stac2Plugin("Y", trans.y);
    		trans.z=((Stac_BCMplugin)activePlugin).convertMotorPosition_Stac2Plugin("Z", trans.z);
    	}
    }
    
    public double convertMotorPosition_Plugin2Stac(String motor,double pos) {
    	return ((Stac_BCMplugin)activePlugin).convertMotorPosition_Plugin2Stac(motor, pos);
    }
    
    public double convertMotorPosition_Stac2Plugin(String motor,double pos) {
    	return ((Stac_BCMplugin)activePlugin).convertMotorPosition_Stac2Plugin(motor, pos);
    }
    
    public void moveToDatum(double o,double k,double p){
    	ParamTable motorVal = new ParamTable();
    	motorVal.setSingleDoubleValue("Omega",o);
    	motorVal.setSingleDoubleValue("Kappa",k);
    	motorVal.setSingleDoubleValue("Phi",p);
    	moveMotors(motorVal);
    }
    
    public void moveToTrans(double x,double y,double z){
    	ParamTable motorVal = new ParamTable();
    	motorVal.setSingleDoubleValue("X",x);
    	motorVal.setSingleDoubleValue("Y",y);
    	motorVal.setSingleDoubleValue("Z",z);
    	moveMotors(motorVal);
    }
    
    public void moveToDatumTrans(double o,double k,double p,double x,double y,double z){
    	ParamTable motorVal = new ParamTable();
    	motorVal.setSingleDoubleValue("Omega",o);
    	motorVal.setSingleDoubleValue("Kappa",k);
    	motorVal.setSingleDoubleValue("Phi",p);
    	motorVal.setSingleDoubleValue("X",x);
    	motorVal.setSingleDoubleValue("Y",y);
    	motorVal.setSingleDoubleValue("Z",z);
    	moveMotors(motorVal);
    }
    
    public void moveToDatumTransSync(double o,double k,double p,double x,double y,double z){
    	ParamTable motorVal = new ParamTable();
    	motorVal.setSingleDoubleValue("Omega",o);
    	motorVal.setSingleDoubleValue("Kappa",k);
    	motorVal.setSingleDoubleValue("Phi",p);
    	motorVal.setSingleDoubleValue("X",x);
    	motorVal.setSingleDoubleValue("Y",y);
    	motorVal.setSingleDoubleValue("Z",z);
    	moveMotorsSyncronized(motorVal);
    }
    
    public void moveToNewDatumCenteredSync(ParamTable pos) {
    	Point3d trn= new Point3d();
    	Point3d dat= new Point3d();
    	//get the current positions
    	getCurrentDatumTrans(dat,trn);
    	Point3d newTrn= new Point3d();    	
    	double newO=dat.x;
    	double newK=dat.y;
    	double newP=dat.z;
    	try {
    		newO=((Double)pos.getFirstValue("Omega")).doubleValue();
    	} catch (Exception e) {    		
    	}
    	try {
    		newK=((Double)pos.getFirstValue("Kappa")).doubleValue();
    	} catch (Exception e) {    		
    	}
    	try {
    		newP=((Double)pos.getFirstValue("Phi")).doubleValue();
    	} catch (Exception e) {    		
    	}
    	newTrn=utils.CalculateTranslation(trn,dat.y,newK,dat.z,newP,this);
    	//move to the desired datum/trans
    	moveToDatumTransSync(newO,newK,newP,newTrn.x,newTrn.y,newTrn.z);
    	//update the field
    	getCurrentDatumTrans(dat,trn);
    	
    	return;
    }
    
    
    public void loadDatumTrans(Point3d dat, Point3d trans,String data){
    	trans.x=loadMotorPosition("X",data);
    	trans.y=loadMotorPosition("Y",data);
    	trans.z=loadMotorPosition("Z",data);
    	dat.x=loadMotorPosition("Omega",data);
    	dat.y=loadMotorPosition("Kappa",data);
    	dat.z=loadMotorPosition("Phi",data);
    }
    
    public String saveDatumTrans(double o,double k,double p,double x,double y,double z){
    	ParamTable motorVal = new ParamTable();
    	motorVal.setSingleDoubleValue("Omega",o);
    	motorVal.setSingleDoubleValue("Kappa",k);
    	motorVal.setSingleDoubleValue("Phi",p);
    	motorVal.setSingleDoubleValue("X",x);
    	motorVal.setSingleDoubleValue("Y",y);
    	motorVal.setSingleDoubleValue("Z",z);
    	String motorSave = ((Stac_BCMplugin)activePlugin).saveMotorPositions(motorVal,"");
    	
    	return motorSave;
    }
    
    public void resetTrans() {
    	setMotorPosition("X",0.);
    	setMotorPosition("Y",0.);
    	setMotorPosition("Z",0.);
    }
    
    public void resetDatum (){
    	setMotorPosition("Omega",0.);
    	setMotorPosition("Kappa",0.);
    	setMotorPosition("Phi",0.);
    }

    protected void notifyMotorListeners(double pos,String motorName) {
		if (((Stac_BCMplugin)activePlugin).useCentralMotorListener()) {
			Vector motorListeners = ((Stac_BCMplugin)activePlugin).getMotorListeners(motorName);
			for (int i=0;i<motorListeners.size();i++) {
				((MotorWidget_actionAdapter)(motorListeners.elementAt(i))).monitorPosition(pos);
			}
			if (activeVBSPlugin!=null) {
				motorListeners = activeVBSPlugin.getMotorListeners(motorName);
				for (int i=0;i<motorListeners.size();i++) {
					((MotorWidget_actionAdapter)(motorListeners.elementAt(i))).monitorPosition(pos);
				}
			}
		}    	
    }
    
    /**
     * @return the motorposition for STAC
     */
    public double getMotorPosition(String motorName){
    	double pos=0.;
    	try {
    		pos= ((Stac_BCMplugin)activePlugin).getMotorPosition(motorName);
    		notifyMotorListeners(pos,motorName);
    	} catch (Exception e) {
    	}
    	return pos;
    }
    
    /**
     * gets the different parametres of a motor
     * @param motorName - STAC name of the motor
     * @return a ParamTable 
     * standard values served:
     * + pos
     * + minpos
     * + maxpos
     * + speed
     * + minspeed
     * additional parametres can also be served by the diffrent plugins,
     * like the spec-specific value:
     * - steps
     */
    public ParamTable getMotorParams(String motorName){
    	ParamTable params=new ParamTable();
    	try {
    		params=((Stac_BCMplugin)activePlugin).getMotorParams(motorName);
    		double pos= params.getFirstDoubleValue("pos");
    		notifyMotorListeners(pos,motorName);
    	} catch (Exception e) {
    		Stac_Out.println("Problem in getting the parameters for "+ motorName);
    	}
    	return params;
    }
    



	protected Stac_RespMsg process(Stac_ReqMsg req,Stac_JobAction job) {
		Stac_PluginSlotRespMsg response=new Stac_PluginSlotRespMsg(this.session,req);
		if (req instanceof Stac_PluginSlotReqMsg) {
			Stac_PluginSlotReqMsg alignReq=(Stac_PluginSlotReqMsg) req;
			//get input data from AlignmentReqMsg
		    try {
				//get input data from AlignmentReqMsg
		    	String method=alignReq.get_method();
			    Object [] param=alignReq.get_param();
				//process
			    Class[] pclass=new Class[param.length];
			    for (int i=0;i<param.length;i++) {
			    	pclass[i]=param[i].getClass();
			    }
			    Method m=this.getClass().getMethod(method, pclass);
			    Object resp=m.invoke(this, param);
				setPercentageDone(95,job);
				//build reponse
				response.set_result(resp);
				response.status=response.OK;
		    } catch (Exception e) {
				response.status=response.ERROR;		    	
		    }
		}
		return response;
	}
    
    public void getCalibration2(Vector3d okp[]) {
    	createReq("getCalibration",new Object[] {okp});
    }
	
    class Stac_PluginSlotReqMsg extends Stac_ReqMsg {
        String method;
        Object [] param;
    	
        public Stac_PluginSlotReqMsg (Stac_Session session,Stac_RespHandler respHandler) {
        	super(session,respHandler);
        }
        
        public Stac_PluginSlotReqMsg (Stac_Session session,Stac_RespHandler respHandler,
                String method,
        		Object [] param) {
        	super(session,respHandler,method);
        	set_method(method);
        	set_param(param);
        }
            

        public String get_method() {
        	return method;
        }
        public Object [] get_param() {
        	return param;
        }
        
        public void set_method(String method) {
        	this.method=method;
        }
        public void set_param(Object [] param) {
        	this.param=param;
        }
        
    	public void load(DataInputStream in) throws IOException {
    		//loadMsgHeader(in);
    	}

    	public void save(DataOutputStream out) throws IOException {
    		//saveMsgHeader(out);
    	}

        public void loadMsgHeader(DataInputStream in) throws IOException {
        	super.loadMsgHeader(in);
        	String msgType=in.readUTF();
        	if (!msgType.equals("Stac_PluginSlotReqMsg"))
        		throw new IOException("STAC Message Type Missmatch!");
        }
        
        public void saveMsgHeader(DataOutputStream out) throws IOException {
        	super.saveMsgHeader(out);
        	out.writeUTF("Stac_PluginSlotReqMsg");
        }
    	
        
    }
    
    class Stac_PluginSlotRespMsg extends Stac_RespMsg {
        Object result;
    	
        public Stac_PluginSlotRespMsg (Stac_Session session,Stac_ReqMsg req) {
        	super(session,req);    	
        }
       
        public Object get_result() {
        	return result;
        }
        
        public void set_result(Object result) {
        	this.result=result;
        }
        
    	public void load(DataInputStream in) throws IOException {
    		//loadMsgHeader(in);
    	}

    	public void save(DataOutputStream out) throws IOException {
    		//saveMsgHeader(out);
    	}

        public void loadMsgHeader(DataInputStream in) throws IOException {
        	super.loadMsgHeader(in);
        	String msgType=in.readUTF();
        	if (!msgType.equals("Stac_PluginSlotReqMsg"))
        		throw new IOException("STAC Message Type Missmatch!");
        }
        
        public void saveMsgHeader(DataOutputStream out) throws IOException {
        	super.saveMsgHeader(out);
        	out.writeUTF("Stac_PluginSlotReqMsg");
        }
    	
        
    }
    
    class BCM_listener extends Stac_RespHandler {

		@Override
		protected void handleResponse(Stac_RespMsg response) {
			// TODO Auto-generated method stub
			
		}
    	
    }
	
    BCM_listener bcmresp = new BCM_listener();
    
	public void createReq(String name,Object [] param) {
		Stac_PluginSlotReqMsg req = new Stac_PluginSlotReqMsg(session,bcmresp,name,param);
		this.handle(req);
	}
	
  }


