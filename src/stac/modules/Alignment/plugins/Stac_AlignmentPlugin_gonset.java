package stac.modules.Alignment.plugins;

import org.sudol.sun.util.*;

import stac.core.*;
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


public class Stac_AlignmentPlugin_gonset extends Stac_AlignmentPlugin_exact implements Stac_AlignmentPlugin {
	/*
	 * 
	 */
	
    public Stac_AlignmentPlugin_gonset () {
    	
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
    public String CalculateMultiAlignment(Stac_Session session,int msgId,String indir,String outdir,Vector3d datum,Point3d trans,Vector orientations,StacBCM bcm,Util utils) {
		//call external gonset
		CallExternalMultiGonset(session,msgId,indir,outdir,datum,true,orientations,bcm,utils);
		//filter gonset alignment
		String out="STAC_filtered_align.vec";
		FilterCalculatedAlignments(session,msgId,indir,outdir,datum,trans,out,utils,bcm);
		return out;
	}

	

    void CallExternalMultiGonset(Stac_Session session,int msgId,String indir,String outdir,Vector3d currDatum,boolean resetOmega,Vector orientations,StacBCM bcm,Util utils) {
		///////////////////////////////////
    	//clean
    	try {
			File fp=new File(outdir+"STAC_align.vec");
			fp.delete();
		} catch (RuntimeException e1) {
			// TODO Auto-generated catch block
			utils.printErrorMessage(session, msgId, "Could not clean the execution directory!");
		}
		
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
    	//preparing the gonset.todo file
    	String outfile="";//jTextFieldDefFile1.getText();
    	try {
        	//calculate datum from name0.x
        	//new way:
        	Vector3d datum=new Vector3d();
        	if (resetOmega) {
        		//phi,kappa,0
        		datum.set(currDatum.z,currDatum.y,0.0);
        	} else {
        		//phia,kappa,omega
        		datum.set(currDatum.z,currDatum.y,currDatum.x);  		
        	}
        	//because the matrices are representing the stage omega=0!
        	//old datum, when the xtal misseting agle was calculated...
//      	if(omega!=0 || kappa!=0 || phi!=0)
//      	get_matrices_by_gonset("name0.x",datum);
    		
    		if (outfile.equals("")) {
    			outfile=outdir+"gonset.todo";
    		}
    		// Create channel on the destination
    		FileWriter dstStream = new FileWriter(outfile);
    		// Copy file contents from source to destination
    		dstStream.write(new PrintfFormat("now\ndenzo name0.x\n" + 
    				"datum %12.6lg %12.6lg %12.6lg\n"
    				//old style:
    				//"v1 a*\nv2 b*\nnow\nsolve\nchoose 1\nnow\n" +
    				//new way:
    				//"v1 a*\nv2 b*\nnow\nsolve\nnow\n" +
    				//"v1 b*\nv2 a*\nnow\nsolve\nnow\n" +
    				//"v1 a*\nv2 c*\nnow\nsolve\nnow\n" +
    				//"v1 c*\nv2 a*\nnow\nsolve\nnow\n" +
    				//"v1 b*\nv2 c*\nnow\nsolve\nnow\n" +
    				//"v1 c*\nv2 b*\nnow\nsolve\nnow\n" +
    				//"end\n").sprintf(new Object[] {new Double(datum.x),new Double(datum.y),new Double(datum.z)}));
    		).sprintf(new Object[] {new Double(datum.x),new Double(datum.y),new Double(datum.z)}));
    		for(int i=0;i<orientations.size();i+=2) {
    			dstStream.write("v1 "+orientations.elementAt(i)+"\nv2 "+orientations.elementAt(i+1)+"\nnow\nsolve\nnow\n");        	
    		}
    		dstStream.write("end\n");
    		// Close the file
    		dstStream.flush();
    		dstStream.close();
    	} catch (Exception e) {
    		//Stac_Out.println("Problem with generating the gonset.todo file: "+outfile);
    		utils.printErrorMessage(session, msgId, "Problem with generating the gonset.todo file: "+outfile);
    	}
    	
		String outfileGNS=outdir+"gns.dat";
    	try {
			//GNS.DEF
			Vector3d okp[]=new Vector3d[7];
			bcm.getCalibration(okp);
			utils.write_gnsdef(outfileGNS,okp);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
    		utils.printErrorMessage(session, msgId, "Problem with generating the gns.dat file: "+outfileGNS);
		}
		
		///////////////////////////////////
		// command line
    	String mycmd = new String("p:\\work\\dev\\STAC\\gonset\\argtest.exe sutike < argtest.log");
    	try {
    		String osName = System.getProperty("os.name" );
    		String[] cmd = new String[3];

    		if( !osName.contains( "Windows " ) )
    		{
    			//mycmd="rm STAC_align.vec ; export GNSDEF=/user/sudol/work/test/gns.dat ; $STACDIR/gonset/gonset < $STACDIR/gonset/gonset.todo";
    			mycmd="cd "+outdir+"; rm STAC_align.vec ; export GNSDEF=gns.dat; "+System.getProperty("STACDIR")+"/thirdparty/gonset/gonset < "+outfile;
    			//mycmd="rm "+outdir+"STAC_align.vec ; export GNSDEF="+outfileGNS+"; $STACDIR/gonset/gonset < "+outfile;
    		}
    	}
    	catch (Exception ex) {
    		//ex.printStackTrace();
    		utils.printErrorMessage(session, msgId, "Problem while preparing command line: "+mycmd);
    	}
    	
		///////////////////////////////////
		// execution
    	utils.executeExternalJob(session, msgId, outdir, "gonset", mycmd);

    }



	public String getCreditString() {
		return "using gonset from Phil Evans";
	}

	@Override
	public void initPlugin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String CalculateExactAlignment(Stac_Session session, int msgId, String indir, String outdir, Vector3d datum, Point3d trans, Vector orientation, StacBCM bcm, Util utils) {
		//Vector orientations???
		return CalculateMultiAlignment(session,msgId,indir,outdir,datum,trans,orientation,bcm,utils);
	}

	@Override
	protected void closePlugin() {
		// TODO Auto-generated method stub
		
	}
	  
	
	
	
}


