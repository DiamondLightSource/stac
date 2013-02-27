package stac.modules.Strategy;

import org.sudol.sun.util.*;

import stac.core.*;
import stac.modules.Alignment.Stac_AlignmentReqMsg;
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


public class StacStrategy extends Stac_PluginSlot {
	Stac_StrategyPlugin activePlugin;
	org.sudol.sun.util.Util utils;
	
	@Override
	public boolean doAcceptJob(Object job) {
		if (job instanceof Stac_StrategyReqMsg)
			return true;
		return false;
	}
	
    public StacStrategy (Stac_Session session) {
    	super(session,"Strategy");
    	String StrategyPlugin=null;
    	//get the active plugin from the descriptor
    	//ParamTable Descriptor= new ParamTable();
    	//read_specdef(System.getProperty("BCMDEF"),Descriptor);
    	//utils= new Util();
    	//utils.read_section(System.getProperty("BCMDEF"),""+SlotType+"plugin",Descriptor);
    	String pluginInstName="";
    	try{
    		StrategyPlugin=config.Descriptor.getFirstStringValue("Strategy_Plugin");
    		pluginInstName=(String)config.Descriptor.getValueAt("Strategy_Plugin",1);
    	} catch (Exception e) {
   		}
	    if (StrategyPlugin==null) {
	    	config.Descriptor.setSingleValue("Strategy_Plugin","StrategyPlugin_strategy");
	    	StrategyPlugin=config.Descriptor.getFirstStringValue("Strategy_Plugin");
		}
    	
    	
    	activePlugin=(Stac_StrategyPlugin)changePlugin(StrategyPlugin,pluginInstName);
    }

    
    public void read_specdef(String specfile,ParamTable Descr) {
    	Descr.setSingleValue("Strategy_Plugin","StrategyPlugin_strategy");
    }
    

    //simple routines
    private String CalculateStrategy(String indir,String outdir,Vector3d currDatum,Vector DesiredOrientations,String options,StacBCM bcm,Util utils,int msgId) {
    	try {
    		return activePlugin.CalculateStrategy(session,msgId,indir,outdir,currDatum,DesiredOrientations,options,bcm,utils);
    	} catch (Exception e) {
    		//if we handle strategy requests as Stac_Request
    		//session.addErrorMsg(,"Problem with Startegy Calculation ("+activePlugin.getCreditString()+")");
    		//session.printErrorMsg();
    		session.addErrorMsg(msgId,"Problem with Startegy Calculation ("+activePlugin.getCreditString()+")");
    		return "";
    	}
    }
    
	public String getCreditString() {
    	try {
    		return activePlugin.getCreditString();
    	} catch (Exception e) {
    		return "";
    	}
	}

	protected Stac_RespMsg process(Stac_ReqMsg req,Stac_JobAction job) {
		Stac_RespMsg response=new Stac_StrategyRespMsg(this.session,req);
		if (req instanceof Stac_StrategyReqMsg) {
			Stac_StrategyReqMsg alignReq=(Stac_StrategyReqMsg) req;
			//get input data from AlignmentReqMsg
		    try {
				//get input data from AlignmentReqMsg
		    	String OMfile=alignReq.get_workDir();
			    Vector3d datum=alignReq.get_datum();
			    Vector orientations=alignReq.get_orienattions();
			    String options=alignReq.get_options();
			    StacBCM bcm=alignReq.get_bcm();
			    Util utils=alignReq.get_utils();
				//process
				String strFile = CalculateStrategy(alignReq.indir,alignReq.outdir,datum,orientations,options,bcm,utils,alignReq.msgId);
				setPercentageDone(98,job);
				//build reponse
				((Stac_StrategyRespMsg)response).set_strategyFile(strFile);
				if (strFile.equals("")) {
					response.status=response.ERROR;
				} else {
					response.status=response.OK;
				}
		    } catch (Exception e) {
				response.status=response.ERROR;		    	
		    }
		} else if (req instanceof Stac_OptStrategyOrientReqMsg) {
			Stac_OptStrategyOrientReqMsg optReq=(Stac_OptStrategyOrientReqMsg) req;
			response= new Stac_OptStrategyOrientRespMsg(this.session,req);
			
			Util utils= new Util();
			
			//new workdir
			String workDir=optReq.outdir;
			try {
				utils.copyFile(new File(optReq.indir+"name0.x"), new File(workDir+"name0.x"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
	    		utils.printErrorMessage(session, optReq.msgId, "Problem with copying the original OM file: name0.x");
			}
			
			//does not go via the actual plugin!
			Vector OM = utils.getOptAlign(session,optReq.msgId,workDir,"");
			//Vector OM = optReq.utils.getOptAlign(session,optReq.msgId,"./");
			if (OM==null || OM.size()==0) {
				response.status=response.ERROR;
			} else {
				response.status=response.OK;
			}
			((Stac_OptStrategyOrientRespMsg)response).set_optAlign(OM);
		}
		
		return response;
	}
    
    
}


