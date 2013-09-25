package stac.modules.Alignment.plugins;

import org.sudol.sun.util.*;

import stac.core.*;
import stac.gui.STAC_GUI_Button_Panel;
import stac.modules.BCM.*;
import stac.modules.Alignment.*;



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


public class Stac_AlignmentPlugin_cones extends Stac_AlignmentPlugin_gonset implements Stac_AlignmentPlugin {
	/*
	 * 
	 */
	
    public Stac_AlignmentPlugin_cones () {
    	
    }
    
	@Override
	public String CalculateExactAlignment(Stac_Session session, int msgId, String indir, String outdir, Vector3d datum, Point3d trans, Vector orientation, StacBCM bcm, Util utils) {
		//Vector orientations???
		return CalculateMultiAlignment(session,msgId,indir,outdir,datum,trans,orientation,bcm,utils);
	}
	  

    /**
     * 
     * @param datum (phi,kappa,omega) - omega will be set to 0
     * @param orientations - list of desired V1-V2 pairs, 
     * where V1 is a vector should be paralllel to the spindle,
     * and V2 is in the plane of the beam and spindle  
     * @param bcm - STAC BCM
     * @param utils - STAC utilities
     */
    @Override
    public String CalculateMultiAlignment(Stac_Session session,int msgId,String indir,String outdir,Vector3d datum,Point3d trans,Vector orientations,StacBCM bcm,Util utils) {
		//call external xo

    	//calibration
        Vector3d okp[]=new Vector3d[7];
        bcm.getCalibration(okp);
	  	
	  	Vector3d actDatum=new Vector3d();
	  	boolean resetOmega=true;
	  	if (resetOmega) {
	  		//phi,kappa,0
	  		actDatum.set(datum.z,datum.y,0.0);
	  	} else {
	  		//phia,kappa,omega
	  		actDatum.set(datum.z,datum.y,datum.x);  		
	  	}
	  	
        File fp=new File(outdir+"STAC_align.vec");
        fp.delete();
	  	for (int i =0;i<orientations.size();i+=2) {
	  		String command="-D \""+actDatum.x+","+actDatum.y+","+actDatum.z+"\""+
	  		" -O \""+okp[0].x+","+okp[0].y+","+okp[0].z+"\""+
	  		" -K \""+okp[1].x+","+okp[1].y+","+okp[1].z+"\""+
	  		" -P \""+okp[2].x+","+okp[2].y+","+okp[2].z+"\""+
	  		" -V \""+orientations.elementAt(i)+"\" -W \""+orientations.elementAt(i+1)+"\""+
	  		" name0.x";	  		
	  		CallExternalXO(session,msgId,indir,outdir,command,utils);
	  		AddSolutions(outdir,"XO.out","STAC_align.vec",utils);
	  	}

	  	
	  	//new way (CallExternalXO copies the name0.x):
	  	
        String [] o1=((String)orientations.elementAt(0)).substring(1, ((String)orientations.elementAt(0)).length()-1).split(" ");
        String [] o2=((String)orientations.elementAt(1)).substring(1, ((String)orientations.elementAt(1)).length()-1).split(" ");
        
        Vector3d v1= new Vector3d(new Double(o1[0]).doubleValue(),new Double(o1[1]).doubleValue(),new Double(o1[2]).doubleValue());
        Vector3d v2= new Vector3d(new Double(o2[0]).doubleValue(),new Double(o2[1]).doubleValue(),new Double(o2[2]).doubleValue());
        //Vector newresults=utils.getAlignmentSettings(outdir+"name0.x",okp[0], okp[1], okp[2], actDatum, v1, v2, false);
        Vector newresults=utils.getAlignmentSettings(outdir+"name0.x",okp[0], okp[1], okp[2], new Vector3d(actDatum.z,actDatum.y,actDatum.x), v1, v2, false);
	  	session.addErrorMsg(msgId, "==\nnew results:\n"+newresults+"\n==================");
	  	
	  	
	  	
	  	
	  	//filter gonset alignment
		String out="STAC_filtered_align.vec";
        fp=new File(outdir+out);
        fp.delete();
		FilterCalculatedAlignments(session,msgId,indir,outdir,datum,trans,out,utils,bcm);
		return out;
	}

    void AddSolutions(String workDir,String in,String out,Util utils) {
    	String inFile=utils.opReadCl(workDir+in);
    	Vector solutions= new Vector();
    	if (inFile!=null) {
    		String sol[]=inFile.split("Independent Solutions for possible Datum positions:");
    		if (sol!=null && sol.length==2) {
    			//desired settings
    			String dsetting[]=sol[0].split("\n");
    			String desired="";
    			if (dsetting.length>4) {
    				desired=desired.concat(dsetting[dsetting.length-4].substring(2,26));
    				desired=desired.concat(dsetting[dsetting.length-3].substring(2,26));
    			}
    			//solutions
    			String solLines[]=sol[1].split("\n");
    			for (int i=1;i<solLines.length;i++){
    				Vector par=utils.map("double",solLines[i].split("\\s+"));
    				if (par.size()==4) {
    					solutions.addElement(par);
    				}
    			}
    			
    			System.out.println("==================\nXO results:\n"+solutions);
    			
    			if (solutions.size()>0) {
    				try {
    					FileWriter alignFile= new FileWriter(workDir+out,true);
    					alignFile.write(desired+" "+solutions.size()+"\n");
    					for (int i=0;i<solutions.size();i++){
    						alignFile.write(
    								" "+((Vector)solutions.elementAt(i)).elementAt(1)+
    								" "+((Vector)solutions.elementAt(i)).elementAt(2)+
    								" "+((Vector)solutions.elementAt(i)).elementAt(3)+"\n");
    					}
    					alignFile.close();
    				} catch (IOException ex) {
    				}
    				solutions.clear();
    			}
    		}
    	}    	
    	return;
    }
	
	  
	  void CallExternalXO(Stac_Session session,int msgId,String indir,String outdir,String command,Util utils) {
		  
			///////////////////////////////////
	    	//clean
			
			///////////////////////////////////
			//input prepare
			//copy the denzo file to outdir
			if (indir.compareTo(outdir)!=0)
				try {
					utils.copyFile(new File(indir+"name0.x"), new File(outdir+"name0.x"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
		    		utils.printErrorMessage(session, msgId, "Problem with copying the original OM file: name0.x");
				}
			
			///////////////////////////////////
			// command line
	    	String mycmd = new String("p:\\work\\dev\\STAC\\gonset\\argtest.exe sutike < argtest.log");
	    	try {
	    		String osName = System.getProperty("os.name" );
	    		String[] cmd = new String[3];

	    		if( !osName.contains( "Windows " ) )
	    		{
	  	          mycmd="cd "+outdir+"; export PYTHONPATH="+System.getProperty("STACDIR")+"/thirdparty/XO/src/ ; python "+System.getProperty("STACDIR")+"/thirdparty/XO/src/XOalign.py "+command;
	    		}
	    	}
	    	catch (Exception ex) {
	    		//ex.printStackTrace();
	    		utils.printErrorMessage(session, msgId, "Problem while preparing command line: "+mycmd);
	    	}
	    	
			///////////////////////////////////
			// execution
	    	utils.executeExternalJob(session, msgId, outdir, "XO", mycmd);

		  
	  }

	  
		public String getCreditString() {
			return "using internal calculation from Sandor Brockhauser";
		}
	
	
	
}


