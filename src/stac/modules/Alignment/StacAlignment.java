package stac.modules.Alignment;

import org.sudol.sun.util.*;

import stac.core.*;
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

import java.util.StringTokenizer;
import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


public class StacAlignment  extends Stac_PluginSlot {
	Stac_AlignmentPlugin activePlugin;
	org.sudol.sun.util.Util utils;
	
    public StacAlignment (Stac_Session session) {
    	super(session,"Alignment");
    	String AlignmentPlugin=null;
    	//get the active plugin from the descriptor
    	//ParamTable Descriptor= new ParamTable();
    	//read_specdef(System.getProperty("BCMDEF"),Descriptor);
    	//utils= new Util();
    	//utils.read_section(System.getProperty("BCMDEF"),""+SlotType+"plugin",Descriptor);
    	String pluginInstName="";
    	try{
    		AlignmentPlugin=config.Descriptor.getFirstStringValue("Alignment_Plugin");
       		pluginInstName=(String)config.Descriptor.getValueAt("Alignment_Plugin",1);
       	     	} catch (Exception e) {
    		AlignmentPlugin=null;
   		}
	    if (AlignmentPlugin==null) {
	    	config.Descriptor.setSingleValue("Alignment_Plugin","AlignmentPlugin_gonset");
	    	AlignmentPlugin=config.Descriptor.getFirstStringValue("Alignment_Plugin");
		}
    	activePlugin=(Stac_AlignmentPlugin)changePlugin(AlignmentPlugin,pluginInstName);
    }
    /**
     * Interprets a line of the specdef file, and actualize the Descriptor 
     * @param specline
     * @param Descr
     */
    public void interpret_specdef(String specline,ParamTable Descr) {
    	if (specline.startsWith("#",0))
    		return;
    	String tmp2[] = specline.split("\\s+");
    	if(tmp2.length==0)
    		return;
    	int offset=0;
    	if (tmp2[0].length()==0)
    		offset++;
		String name = tmp2[0+offset];
		if (name.equals("Alignment_Plugin")) {
			String value =tmp2[1+offset];
			Descr.setSingleValue(name,value);
		}
		
    }
    
//    public void read_specdef(String specfile,ParamTable Descr) {
//          	String corr;
//          	Util utils=new Util();
//            //read spec.dat
//          	corr = utils.opReadCl(specfile);
//            String tmp1[] = corr.split("\n");
//            for (int l=0;l<tmp1.length;l++) {
//            	interpret_specdef(tmp1[l],Descr);
//            }
//    }
//    

    //simple routines
    //private String CalculateAlignment(String OMfile,Vector3d datum,Point3d trans,Vector orientations,StacBCM bcm,StacUtil utils){
    //just now for keeping the original functionalities as well
    //convinience feature: if v2 is not given, we will try to figure it out
    //orienation requestes look like:
    //v1,v2,close,comment
    //as we calc v2 anyway, we add one more field to know if that v2 was requested or not
    private String CalculateAlignment(String indir,String outdir,Vector3d datum,Point3d trans,Vector orientations,StacBCM bcm,Util utils,int msgId){
    	try {
    		//check if the user left v2 empty
    		for (int i=0;i<orientations.size();i++) {
    			Vector orient=(Vector)orientations.elementAt(i);
    			orient.setElementAt(((String)orient.elementAt(0)).replaceAll(",", " "),0);
    			orient.setElementAt(((String)orient.elementAt(1)).replaceAll(",", " "),1);
    			orient.add(new Boolean(true)); //v2 is given @ elementAt(4)  			
    			String v1=((String)orient.elementAt(0));
    			String v2=((String)orient.elementAt(1));
    			if (v2.length()==0) {
        			orient.setElementAt(new Boolean(false),4); //v2 is NOT given   			
    				try {
    					StringTokenizer substr=new StringTokenizer(v1);
    					double h=0;
    					double k=0;
    					double l=0;
    					boolean realVec=false;
    					if (v1.indexOf("[")>=0) {
    						realVec=true;
    					}
    					String hklstr=substr.nextToken(" ()[]");
    					if (hklstr.indexOf("a*")>=0) {
    						//v1 contains a special a*
    						h=1;
    					} else if (hklstr.indexOf("b*")>=0) {
    						//v1 contains a special b*
    						k=1;
    					} else if (hklstr.indexOf("c*")>=0) {
    						//v1 contains a special c*
    						l=1;
    					} else if (hklstr.indexOf("a")>=0) {
    						//v1 contains a special a
    						realVec=true;
    						h=1;
    					} else if (hklstr.indexOf("b")>=0) {
    						//v1 contains a special b
    						realVec=true;
    						k=1;
    					} else if (hklstr.indexOf("c")>=0) {
    						//v1 contains a special c
    						realVec=true;
    						l=1;
    					} else {
    						//it must be a normal abc, or hkl value
    						h=new Double(hklstr);
    						hklstr=substr.nextToken(" ()[]");
    						k=new Double(hklstr);
    						hklstr=substr.nextToken(" ()[]");
    						l=new Double(hklstr);
    					}
    					double [] hkl=utils.getIndependentVec(h,k,l);
    					String sepChar="()";
    					if (realVec)
    						sepChar="[]";
    					orient.setElementAt(sepChar.substring(0,1)+hkl[0]+" "+hkl[1]+" "+hkl[2]+sepChar.substring(1,2),1);
    				}catch (Exception e) {
    					session.addErrorMsg(msgId,"Uninterpretable alignment request "+v1+" is passed to the plugin");
    				}
    			}
    		}
    		return activePlugin.CalculateAlignment(session,msgId,indir,outdir,datum,trans,orientations,bcm,utils);
    	} catch (Exception e) {
    		session.addErrorMsg(msgId,"Error in Alignment calculation");
    	}
    	return "";
    }
    
	protected Stac_RespMsg process(Stac_ReqMsg req,Stac_JobAction job) {
		Stac_AlignmentRespMsg response=new Stac_AlignmentRespMsg(this.session,req);
		if (req instanceof Stac_AlignmentReqMsg) {
			Stac_AlignmentReqMsg alignReq=(Stac_AlignmentReqMsg) req;
			//get input data from AlignmentReqMsg
		    try {
				//get input data from AlignmentReqMsg
			    Vector3d datum=alignReq.get_datum();
			    Point3d trans=alignReq.get_trans();
			    Vector orientations=alignReq.get_orienattions();
			    StacBCM bcm=alignReq.get_bcm();
			    Util utils=alignReq.get_utils();
				//process
				String possibleDatumsFile = CalculateAlignment(alignReq.indir,alignReq.outdir,datum,trans,orientations,bcm,utils,req.msgId);
				setPercentageDone(95,job);
				//build reponse
				response.set_possibleDatumsFile(possibleDatumsFile);
				if (possibleDatumsFile.equals("")) {
					response.status=response.ERROR;
				} else {
					response.status=response.OK;
				}
		    } catch (Exception e) {
				response.status=response.ERROR;		    	
		    }
		}
		return response;
	}
    
	public String getCreditString() {
    	try {
    		return activePlugin.getCreditString();
    	} catch (Exception e) {
    		return "";
    	}
	}
	@Override
	public boolean doAcceptJob(Object job) {
		if (job instanceof Stac_AlignmentReqMsg)
			return true;
		return false;
	}
    
}


