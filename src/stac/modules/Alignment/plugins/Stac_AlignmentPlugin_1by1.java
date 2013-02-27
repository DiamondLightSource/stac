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


public abstract class Stac_AlignmentPlugin_1by1 extends Stac_Plugin implements Stac_AlignmentPlugin {
	/*
	 * 
	 */
	
    public Stac_AlignmentPlugin_1by1 () {
    	
    }

	  void FilterCalculatedAlignments(Stac_Session session,int msgId,String indir,String workDir,Vector3d currDatum,Point3d actualTrans,String out,Util utils,StacBCM bcm) {
		  {
			File fp=new File(workDir+out);
			fp.delete();
		  }
	    try {
	    	
	      String osName = System.getProperty("os.name");
	      String myfile = new String(workDir+"STAC_align.vec");

//	      if (osName.equals("Windows NT") || osName.equals("Windows XP") ||
//	          osName.equals("Windows 95")) {
//	        myfile = "P:\\work\\dev\\STAC\\gui\\stacgui\\STAC_align.vec";
//	      }
	      Stac_Out.println("File to be read: " + myfile);
	      FileInputStream GonsetResultFile = new FileInputStream(myfile);
	      TextReader grf = new TextReader(GonsetResultFile);
	      grf.IOCheck(true);
	      FileOutputStream alignFile= new FileOutputStream(workDir+out);
	      DataOutputStream outFile = new DataOutputStream(alignFile);
	      //get info for trans calc
	      double actualKappa=0.,actualPhi=0.;
	      	actualKappa = currDatum.y;
	      	actualPhi = currDatum.z;
	      try {
	         do {
	           double v1x = grf.getDouble();
	           double v1y = grf.getDouble();
	           double v1z = grf.getDouble();
	           double v2x = grf.getDouble();
	           double v2y = grf.getDouble();
	           double v2z = grf.getDouble();
	           int numAlignVecs = grf.getInt();
	           for (; numAlignVecs > 0; numAlignVecs--) {
	   			File fp=new File(workDir+"alignmentOM.x");
				fp.delete();
	             double omega = utils.angleInRange(grf.getDouble(),0); //Stac_Out.println(omega);
	             double kappa = utils.angleInRange(grf.getDouble(),-5.0);
	             double phi   = utils.angleInRange(grf.getDouble(),0);
	             Point3d trans;
	             trans=utils.CalculateTranslation(actualTrans,actualKappa,kappa,actualPhi,phi,bcm);
	             //calc the new orientation
	             utils.change_denzo_matrices_by_gonset(bcm,indir+"name0.x",
	            		 omega,currDatum.x,
	            		 kappa,currDatum.y,
	            		 phi,currDatum.z,
	            		 workDir+"alignmentOM.x");
	             ParamTable newOrient= new ParamTable();
	             utils.read_denzo_x(workDir+"alignmentOM.x",newOrient);

			  	    double[] cell = new double[6];
			  	    double[] tcell = new double[6];
			  	    Matrix3d B = new Matrix3d();
			  	    Matrix3d Bm1t = new Matrix3d();
			  	    Matrix3d U = new Matrix3d();
			  	    Vector3d phixyz = new Vector3d();
			  		Matrix3d osc = new Matrix3d();
	 			Matrix3d OMU = new Matrix3d(newOrient.getDoubleVector("Amat"));
				Matrix3d OMA = new Matrix3d(newOrient.getDoubleVector("Umat"));
				Matrix3d OMAi= new Matrix3d(OMA); OMAi.invert();
			    Matrix3d OMUB = new Matrix3d(OMA);
			    utils.denzo2mosflm_matrices_by_gonset(OMU,cell,tcell,B,Bm1t,U,OMUB,phixyz);
	             
	             //calc ranking
	             double rank  = utils.AlignmentRanking(trans,omega,kappa,phi,bcm,OMUB);
	             if (rank<0.0) {
	 		    	session.addErrorMsg(msgId,"Goniometer does not allow the solution datum: ("+omega+","+kappa+","+phi+") found for the alignment "+"V1: ("+v1x+","+v1y+","+v1z+")"+" - V2: ("+v2x+","+v2y+","+v2z+")");		    		            	 
	             	continue;
	             }
	             
	             outFile.writeDouble(v1x);
	             outFile.writeDouble(v1y);
	             outFile.writeDouble(v1z);
	             outFile.writeDouble(v2x);
	             outFile.writeDouble(v2y);
	             outFile.writeDouble(v2z);
	             outFile.writeDouble(omega);
	             outFile.writeDouble(kappa);
	             outFile.writeDouble(phi);
	             outFile.writeUTF(new PrintfFormat("(%.4f;%.4f;%.4f)").
	             		sprintf(new Double[] {new Double(trans.x),new Double(trans.y),new Double(trans.z)}));
						             
	             outFile.writeDouble(rank);
	             
	           }
	         }
	         while (true);
	      } catch (Exception grfex) {
	      	grf.close();
	      }
	  	  GonsetResultFile.close();
          outFile.close();
          alignFile.close();
	    }
	    catch (IOException ex2) {
	    }
	  }
    

	  void MergeCalculatedAlignments(Stac_Session session,int msgId,Vector orient,String inDir,String workDir,String workFile,String workDir2,String workFile2) {
	      String osName = System.getProperty("os.name");
	      String myfile = new String(workDir2+workFile2);
	      String errormsg;
	      if ((Boolean)orient.elementAt(4)) {
	    	  errormsg="V1: "+orient.elementAt(0)+" - V2: "+orient.elementAt(1);
	      } else {
	    	  errormsg="V1: "+orient.elementAt(0)+" - V2: -";
	      }
	    try {
		    	
		      Stac_Out.println("File to be read: " + myfile);
		      FileInputStream alignInFile= new FileInputStream(myfile);
		      DataInputStream inFile = new DataInputStream(alignInFile);
		      
		      Vector acceptedSolutions=new Vector();
		      
		      FileOutputStream alignFile;
			try {
				alignFile = new FileOutputStream(workDir+workFile,true);
			} catch (FileNotFoundException e) {
				alignFile = new FileOutputStream(workDir+workFile);
			}
		      DataOutputStream outFile = new DataOutputStream(alignFile);
		      try {
		         do {
		             double v1x=inFile.readDouble();
		             double v1y=inFile.readDouble();
		             double v1z=inFile.readDouble();
		             double v2x=inFile.readDouble();
		             double v2y=inFile.readDouble();
		             double v2z=inFile.readDouble();
		             double omega=inFile.readDouble();
		             double kappa=inFile.readDouble();
		             double phi=inFile.readDouble();
		             String tcorr=inFile.readUTF();							             
		             double rank=inFile.readDouble();
		             //String comm=inFile.readUTF();
		             
		            //if v2 was not specified, we do not set omega
		   	      	if (!(Boolean)orient.elementAt(4)) {
		   	      		omega=v2x=v2y=v2z=0.0;
		   	      	}
		             
		             int i=0;
		             double tol=0.5;
		             for(i=0;i<acceptedSolutions.size();i++){
		            	 Vector actSol=(Vector)acceptedSolutions.elementAt(i);
		            	 if (
		            			 Math.abs(omega-((Double)actSol.elementAt(0)).doubleValue())<=tol &&
		            			 Math.abs(kappa-((Double)actSol.elementAt(1)).doubleValue())<=tol &&
		            			 Math.abs(phi-((Double)actSol.elementAt(2)).doubleValue())<=tol
		            			 )
		            		 break;
		             }
		             if (i==acceptedSolutions.size()) {
		            	 Vector sol=new Vector();
		            	 sol.add(new Double(omega));
		            	 sol.add(new Double(kappa));
		            	 sol.add(new Double(phi));
		            	 acceptedSolutions.add(sol);
		            	 
		            	 outFile.writeUTF("("+v1x+";"+v1y+";"+v1z+")");
		            	 if (v2x==0 && v2y==0 && v2z==0) {
			            	 outFile.writeUTF("");		            		 
		            	 } else {
			            	 outFile.writeUTF("("+v2x+";"+v2y+";"+v2z+")");
		            	 }
//		            	 outFile.writeDouble(v1x);
//		            	 outFile.writeDouble(v1y);
//		            	 outFile.writeDouble(v1z);
//		            	 outFile.writeDouble(v2x);
//		            	 outFile.writeDouble(v2y);
//		            	 outFile.writeDouble(v2z);
		            	 outFile.writeDouble(omega);
		            	 outFile.writeDouble(kappa);
		            	 outFile.writeDouble(phi);
		            	 outFile.writeUTF(tcorr);							             
		            	 outFile.writeDouble(rank);		             
		            	 outFile.writeUTF((String)orient.elementAt(3));
		             }
		         }
		         while (true);
		      } catch (Exception grfex) {
		      	inFile.close();
		      	alignInFile.close();
		      }
	          outFile.close();
	          alignFile.close();
		    }
		    catch (IOException ex2) {
		    	session.addErrorMsg(msgId,"Could not find solution for "+errormsg);//+"\nCould not read the solution file: "+myfile);		    	
		    }
		  }
    
    
    /**
     * 
     * @param datum (phi,kappa,omega) - omega will be set to 0
     * @param orientations - list of Vectors, where each vector contains desired V1-V2 pairs, 
     * where V1 is a vector should be paralllel to the spindle,
     * and V2 is in the plane of the beam and spindle 
     * such vector also contains if exact match is required or approaching is enough 
     * @param bcm - STAC BCM
     * @param utils - STAC utilities
     */
    public String CalculateAlignment(Stac_Session session,int msgId,String indir,String outdir,Vector3d datum,Point3d trans,Vector orientations,StacBCM bcm,Util utils) {
		String out="STAC_merged_align.vec";
        File fp=new File(outdir+out);
        fp.delete();
	  	for (int i =0;i<orientations.size();i++) {
	  		//create subdir
	  		String outdirs=outdir+"/orientation"+new Integer(i+1).toString();
    		File oDir= new File(outdirs);
    		boolean stat=oDir.isDirectory();
    		if (!stat) {
    			try {
					oDir.delete();
				} catch (RuntimeException e) {
				}
    			stat=oDir.mkdirs();
    		}
    		if (!stat) {
    			session.addErrorMsg(msgId,"Could not create the output dir: "+outdirs+"\n Use '"+outdir+"' instead.");
    			outdirs=outdir;
    		}
    		outdirs=new File(outdirs).getAbsolutePath()+"/";
	  		//call
	  		Vector orient=(Vector)orientations.elementAt(i);
	  		String solution=CalculateSingleAlignment(session,msgId,indir,outdirs,datum,trans,orient,bcm,utils);
	  		//get result merged
	  		MergeCalculatedAlignments(session,msgId,orient,indir,outdir,out,outdirs,solution);
	  	}    	
		return out;
    }
    
    public abstract String CalculateSingleAlignment(Stac_Session session,int msgId,String indir,String outdir,Vector3d datum,Point3d trans,Vector orientations,StacBCM bcm,Util utils);

	
}


