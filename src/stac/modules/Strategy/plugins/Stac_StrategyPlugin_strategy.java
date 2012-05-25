package stac.modules.Strategy.plugins;

import org.sudol.sun.util.*;

import stac.core.*;
//import stac.gui.ReorientTableModel;
import stac.modules.Alignment.StacAlignment;
import stac.modules.Alignment.Stac_AlignmentReqMsg;
import stac.modules.Alignment.Stac_AlignmentRespMsg;
import stac.modules.BCM.*;
import stac.modules.Strategy.*;


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

import edna.XSData.*;

public class Stac_StrategyPlugin_strategy extends Stac_Plugin implements Stac_StrategyPlugin {
	/*
	 * 
	 */
	
	XSBeamSetting bs= new XSBeamSetting();
	
    public Stac_StrategyPlugin_strategy () {
    	
    }
    
    public String CalculateStrategy(Stac_Session session,int msgId,String indir,String outdir,Vector3d currDatum,Vector DesiredOrientations,String options,StacBCM bcm,Util utils) {
    	//remove the old strategy result file
    	try {
			File fp = new File(outdir+"STAC_strategy.res");
			fp.delete();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			utils.printErrorMessage(session, msgId, "Could not clean the execution directory!");
		}
		
    	//calculate the new strategies for each request
		int StrID=0;
    	for (int i=0;i<DesiredOrientations.size();i+=2) {
    		double omega = ((Vector3d)DesiredOrientations.elementAt(i)).x;
    		double kappa = ((Vector3d)DesiredOrientations.elementAt(i)).y;
    		double phi   = ((Vector3d)DesiredOrientations.elementAt(i)).z;
    		String comment=(String)DesiredOrientations.elementAt(i+1);
    		try {
				//update name1.x
				//DMANIPCALLS
				//the omega offset of 90 degree is coming from test results, 
				//it could have also built into the correction matrix instead
				//Since omega movements are handles as oscillation only in change_denzo_matrices_byFullReading,
				//therefore it is the only acceptable solution that will give back valid strategy (without offsets)
				//change_denzo_matrices(System.getProperty("GNSDEF"),"name0.x",omega-90,kappa,phi,"name1.x");
				utils.change_denzo_matrices_by_gonset(bcm,indir+"name0.x",omega,currDatum.x,kappa,currDatum.y,phi,currDatum.z,outdir+"name1.x");
				//change_denzo_matrices_byFullReading(System.getProperty("GNSDEF"),"name0.x",omega,omegaCurrent,kappa,kappaCurrent,phi,phiCurrent,"name1.x");
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
	    		utils.printErrorMessage(session, msgId, "Problem with generating the name1.x file for the orientation "+omega+";"+kappa+";"+phi);
			}
    		//call strategy
    		//=============
    		//erase interface file
    		//prepare strategy calls
    		PrepareExternalStrategy(session,msgId,indir,outdir,options,utils);//"$STACDIR"+File.separator+"strategy"+File.separator+"strategy_name1.todo");
    		//calls and checks all the result files
    		//FilterStrategies (outdir,i+1,kappa,phi,currDatum.y,currDatum.z);
        	//run0: get the last row with the requested kappa/phi
    		StrID++;
    		ExecutingStrategy(session,msgId,utils,outdir,"strategy_run0.todo",true,1);
			//read the results, and if not 100% the first run, go for optimal orienation, too
    		if (!selectStrategy(outdir,"STAC_strategy1.res",StrID,comment,kappa,phi,1)) {
    			//run1: get the highest value with the original kappa/phi
    			ExecutingStrategy(session,msgId,utils,outdir,"strategy_run1.todo",true,2);
    			selectStrategy(outdir,"STAC_strategy2.res",StrID,comment,currDatum.y,currDatum.z,0);

    			//check with optimal orientation too  
    			//restore orig results from the 1st strategy run
    			try {
    				utils.copyFile(new File(outdir+"STAC_strategy1.res_1"), new File(outdir+"STAC_strategy1.res"));
    				utils.copyFile(new File(outdir+"STAC_strategy2.res_1"), new File(outdir+"STAC_strategy2.res"));
    			} catch (Exception e) {
    				utils.printErrorMessage(session, msgId, "Could not retrieve result files: STAC_strategy[12].res");
    			}
    			//run2: get the highest value with an optimal kappa/phi
    			try {
					ExecutingStrategy(session,msgId,utils,outdir,"strategy_run2.todo",true,3);
				} catch (Exception e2) {
				}
    			//check possible gonio settings
    			Vector newSetting=new Vector();
    			Vector3d v1best=new Vector3d();
    			double kappabest=0;
    			//in case of bliund zone it is simple, as the best is any orientation 
    			//90 degree away from the previously aligned axis
    			//Now, we rather calls STARTEGY to get the axis, because
    			//this method is more generic and will work for
    			//finishing partial data collections
    			Vector list = utils.getOptAlign(session, msgId, outdir, "strategyRun_3.out");
        		for (int ct=0;ct<list.size()/3;ct++) {
        			String hkl=(String)list.elementAt(ct*3+0);
        			String [] hklVec=hkl.replaceAll("\\(", "").replaceAll("\\)", "").split("\\s+");
        			Vector3d hklOpt=new Vector3d(new Double(hklVec[0]).doubleValue(),new Double(hklVec[1]).doubleValue(),new Double(hklVec[2]).doubleValue());
        			//call Align Calc
        			Vector orientations= new Vector();
        			Vector orient= new Vector();
    	    		try {
						orient.addElement(hkl);
						orient.addElement("");
						orient.addElement(true);
						orient.addElement("best 2nd Data Collectoin");
						orientations.addElement(orient);
						AlignResult ar= new AlignResult(session,utils);
						//NO info on actual Transl for currDatum?!
						Stac_AlignmentReqMsg req = new Stac_AlignmentReqMsg(session,ar,currDatum,new Point3d(),orientations,bcm,utils); 
						session.handle(req);
						ar.waitForResult();
						//get gonio settings
						Vector res=ar.getResult();
						Stac_Out.printTimeln("Gonio setting possibilities for "+hkl+": "+res.size());
						//get the closest
						for (int j=0;j<res.size();j++){
							Vector sol=(Vector)res.elementAt(j);
							String [] v1Vec=((String)sol.elementAt(0)).replaceAll("\\(","").replaceAll("\\)","").split(";");
							Vector3d v1=new Vector3d(new Double(v1Vec[0]).doubleValue(),new Double(v1Vec[1]).doubleValue(),new Double(v1Vec[2]).doubleValue());
							double kappa1=((Double)sol.elementAt(3)).doubleValue();
							//angle comp is not good, as the resciprocal space may not be orthogonal
							double anglediff=v1best.angle(hklOpt)-v1.angle(hklOpt);
							if (newSetting.size()==0 || anglediff>0 || (Math.abs(anglediff)<0.5*Math.PI/180.0 && kappabest>kappa1)) {
								newSetting=sol;
								v1best=v1;
								kappabest=kappa1;
							}
						}
					} catch (Exception e) {
						Stac_Out.printTimeln("Could not calculate Orinetation for optimal filling of "+hkl);
					}
        		}
    			double newKappa=0;
    			double newPhi=0; 
    			try {
					newKappa=((Double)newSetting.elementAt(3)).doubleValue();
					newPhi=((Double)newSetting.elementAt(4)).doubleValue();
				} catch (Exception e1) {
					Stac_Out.printTimeln("No valid gonio settings for the Orinetation for optimal filling!");
				}
    			//call Align Calc
    			//get gonio settings
    			//check if there was a result other than the original
    			if (newKappa!=currDatum.y || newPhi!=currDatum.z) {
    				//create new .x file
    				try {
    					//update name2.x
    					utils.change_denzo_matrices_by_gonset(bcm,indir+"name0.x",omega,currDatum.x,newKappa,currDatum.y,newPhi,currDatum.z,outdir+"name2.x");
    				} catch (RuntimeException e) {
    					// TODO Auto-generated catch block
    					utils.printErrorMessage(session, msgId, "Problem with generating the name2.x file for the orientation "+omega+";"+newKappa+";"+newPhi);
    				}
    				//restore orig results from the 1st strategy run
    				try {
    					utils.copyFile(new File(outdir+"STAC_strategy1.res_1"), new File(outdir+"STAC_strategy1.res"));
    					utils.copyFile(new File(outdir+"STAC_strategy2.res_1"), new File(outdir+"STAC_strategy2.res"));
    				} catch (Exception e) {
    					utils.printErrorMessage(session, msgId, "Could not retrieve result files: STAC_strategy[12].res");
    				}
    				//run3: get the highest value with an optimal kappa/phi
    				try {
						ExecutingStrategy(session,msgId,utils,outdir,"strategy_run3.todo",true,4);
						StrID++;
						selectStrategy(outdir,"STAC_strategy1.res",StrID,comment,kappa,phi,1);
						selectStrategy(outdir,"STAC_strategy2.res",StrID,comment,newKappa,newPhi,0);
					} catch (Exception e) {
					}
    			}
        	}
    		
    		
    	}
    	//return
		return "STAC_strategy.res";
            
	}

    class AlignResult extends Stac_RespHandler {
    	Stac_Session session=null;
    	Util utils=null;
    
    	AlignResult(Stac_Session session,Util utils) {
    		this.session=session;
    		this.utils=utils;
    		if (this.session.findJobManager(new Stac_AlignmentReqMsg(session, this))==null) {
    			StacAlignment alignment = new StacAlignment(session);
    		}
    	}

    	Vector result=new Vector();
    	
    	boolean finished=false;
    	
    	synchronized public void waitForResult() {
    		if (!finished) {
    			try {
    				wait();
    			} catch (InterruptedException e) {
    			}
    		}
    	}
    	
   		public Vector getResult() {
    		return (Vector)result.elementAt(0);
    	}
    	
    	synchronized public void setResult(Object res) {
    		result.addElement(res);
        	finished=true;
    		notifyAll();
    	}
    	
		@Override
		protected void handleResponse(Stac_RespMsg response) {
			Vector res=null;
			if (response instanceof Stac_AlignmentRespMsg)
			{
				Stac_AlignmentRespMsg alignResp=(Stac_AlignmentRespMsg) response;
				String alignFile="";
				String workDir=alignResp.req.outdir;
				if (alignResp.status==alignResp.OK) {
					alignFile=workDir+alignResp.possibleDatumsFile;				
					res=utils.ReadCalculatedAlignmentFile(session, alignFile, response.requestId);
				} else {
					session.addErrorMsg(response.requestId,"Response status = "+response.statusToString());
				}
				setResult(res);
			} else {
				setResult(null);
			}
		}
    	
    }
    
    
    public boolean selectStrategy(String workDir,String myfile,int strID,String comment,double kappa,double phi,int selectOption) {
    	Stac_Out.println("File to be read: " + workDir+myfile);
    	boolean complete=false;
    	try {
    		FileInputStream StrategyResultFile = new FileInputStream(workDir+myfile);
    		TextReader grf = new TextReader(StrategyResultFile);
    		grf.IOCheck(true);
    		
    		//select the appropriate answer
			double omegaStart=0.0;
			double omegaEnd  =0.0;
			double completion=0.0;
			double completionRef=-1.0;
			double res =2.35;
			double rangeRef=500.0;
			boolean validValue=false;
    		try {
    			do {
    				double omegaStartTemp = grf.readDouble();
    				double omegaEndTemp   = grf.readDouble();
    				double completionTemp = grf.readDouble();
    				double resTemp = grf.readDouble();
    				double rangeTemp      = Math.abs(omegaEndTemp-omegaStartTemp);
    				
    				validValue=true;
    				if (selectOption==1 || completionTemp>completionRef ||
    	    	       (completionTemp==completionRef &&  rangeRef>rangeTemp)) {
    					omegaStart = omegaStartTemp;
    					omegaEnd   = omegaEndTemp;
    					completion = completionTemp;
    					completionRef = completionTemp;
    					res = resTemp;
    					rangeRef      = rangeTemp;
    				}
    				
    			}
    			while (true);
    		} catch (Exception grfex) {
    			grf.close();
    		}
    		StrategyResultFile.close();
    		
    		//write the selected out
    		if (validValue) {
    			
//	             //calc the new orientation
//	             utils.change_denzo_matrices_by_gonset(bcm,workDir+"name0.x",
//	            		 (omegaStart+omegaEnd)/2.0,currDatum.x,
//	            		 kappa,currDatum.y,
//	            		 phi,currDatum.z,
//	            		 workDir+"alignmentOM.x");
//	             ParamTable newOrient= new ParamTable();
//	             utils.read_denzo_x(workDir+"alignmentOM.x",newOrient);
//	             Matrix3d CA = new Matrix3d(newOrient.getDoubleVector("Amat"));	             
//	             //calc ranking
//	             double rank  = utils.AlignmentRanking(trans,omega,kappa,phi,bcm,CA);
    			
    			FileOutputStream aFile= new FileOutputStream(workDir+"STAC_strategy.res",true);
    			DataOutputStream outFile = new DataOutputStream(aFile);
    			
    			outFile.writeInt(strID);
    			outFile.writeDouble(omegaStart);
    			outFile.writeDouble(omegaEnd);             
    			outFile.writeDouble(kappa);
    			outFile.writeDouble(phi);
    			outFile.writeDouble(completion);
    			outFile.writeDouble(res);
    			outFile.writeDouble(1.0); //rank!
    			outFile.writeUTF(comment);

    			if (completion>=99.9){//==100.0) {
    				complete=true;
    			}
    			
    			outFile.close();
    			aFile.close();
    		}
    	}
    	catch (IOException ex2) {
    	}
    	return complete;
    }
    public void FilterStrategies_NOTCALLED (String workDir,int strID,String comment,double kappa,double phi,double actkappa,double actphi) {
    	//run1: get the last row with the requested kappa/phi
    	if (!selectStrategy(workDir,"STAC_strategy1.res",strID,comment,kappa,phi,1)) {
    		//run2: get the highest value with the original kappa/phi
    		selectStrategy(workDir,"STAC_strategy2.res",strID,comment,actkappa,actphi,0);
    	}
    }
    
    
    
    public void PrepareStrategyTodo(Stac_Session session,int msgId,Util utils,String fname,String context){
	  	//preparing the strategy.todo file
	    try {
	        // Create channel on the destination
	        FileWriter dstStream = new FileWriter(fname);
	        // Copy file contents from source to destination
	        dstStream.write(context);
	        // Close the file
	        dstStream.flush();
	        dstStream.close();
	    } catch (IOException e) {
	    	utils.printErrorMessage(session, msgId, "Problem with generating the strategy.todo file: "+fname);
	    }
    	
    }

    public void ExecutingStrategy(Stac_Session session,int msgId,Util utils,String outdir,String todofile,boolean removeOldResults,int runNumber) {
    	
		///////////////////////////////////
		// command line
    	String mycmd = new String("p:\\work\\dev\\STAC\\gonset\\argtest.exe sutike < argtest.log");
    	try {
    		String osName = System.getProperty("os.name" );
    		String[] cmd = new String[3];

    		if( !osName.contains( "Windows " ) )
    		{
		    	  String cmdPre= new String("cd "+outdir+";");
		    	  if (removeOldResults) {
		    		  cmdPre=cmdPre.concat("rm STAC_strategy"+runNumber+".res;");
		    	  }
		          mycmd=cmdPre+System.getProperty("STACDIR")+"/thirdparty/strategy/strategy < "+todofile;//$STACDIR/strategy/strategy_name1.todo";
    		}
    	}
    	catch (Exception ex) {
    		//ex.printStackTrace();
    		utils.printErrorMessage(session, msgId, "Problem while preparing command line: "+mycmd);
    	}
    	
		///////////////////////////////////
		// execution
    	utils.executeExternalJob(session, msgId, outdir, "strategyRun_"+runNumber, mycmd);

    	try {
			utils.copyFile(new File(outdir+"STAC_strategy1.res"), new File(outdir+"STAC_strategy1.res_"+runNumber));
			utils.copyFile(new File(outdir+"STAC_strategy2.res"), new File(outdir+"STAC_strategy2.res_"+runNumber));
		} catch (Exception e) {
    		utils.printErrorMessage(session, msgId, "Could not copy result files: STAC_strategy[12].res");
		}
    	
    	
    }
    
    public void PrepareExternalStrategy(Stac_Session session,int msgId,String indir,String workDir,String options,Util utils) {
    	//preparing the strategy.todo files
    	try {
			//get the info from the denzo files
			ParamTable denzoTable=new ParamTable();
			utils.read_denzo_x(indir+"name0.x",denzoTable);
			
			try {
				String[] opts=options.split("%%%");
				for (int i=1;i<opts.length;i++) {
					String [] actopt=opts[i].split("=");
					if ("res".equalsIgnoreCase(actopt[0])) {
						//set requested resolution
						String origres=(String)denzoTable.getFirstValue("resolution_limits");
						String min=origres.trim().split("\\s+")[0];
						denzoTable.setSingleValue("resolution_limits", min+" "+actopt[1]);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				utils.printErrorMessage(session, msgId, "Problem with interpreting the strategy options");
			}
			
			//copy the denzo file to outdir
			if (indir.compareTo(workDir)!=0)
				try {
					utils.copyFile(new File(indir+"name0.x"), new File(workDir+"name0.x"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
		    		utils.printErrorMessage(session, msgId, "Problem with copying the original OM file: name0.x");
				}
			
//    	PrepareStrategyTodo("strategy.todo",new PrintfFormat(
//    			"X1FILE name0.x\n"+
//				"X2FILE name1.x\n"+
//				"COMPLETENESS 100 99 98 97 96 95\n"+
//				"NBINS 10\n"+
//				">NFRAME 90\n"+
//				"PSFILE auto_strategy.ps\n"+
//				"FORMAT %s\n"+          //MAR225
//				"SPACE GROUP %s\n"+     //P3221
//				"RESOLUTION %s\n"       //50.00  2.00
//    	).sprintf(new Object[] {
//    			utils.detector_denzo2strategy(denzoTable.getFirstStringValue("detector")),
//				denzoTable.getFirstValue("spg"),
//				denzoTable.getFirstValue("resolution_limits")}));
//    	ExecutingStrategy("strategy.todo");
			//TODO:
			//do it in two calls
			PrepareStrategyTodo(session,msgId,utils,workDir+"strategy_run0.todo",new PrintfFormat(
					"XFILE name1.x\n"+
					"SIMUL AUTO name1.sca\n"+
					"COMPLETENESS 100 99 98 97 96 95\n"+
					"NBINS 10\n"+
					"PSFILE strategy1.ps\n"+
					"FORMAT %s\n"+          //MAR225
					"SPACE GROUP %s\n"+     //P3221
					"RESOLUTION %s\n" +      //50.00  2.00
					"TURBO 10\n"
			).sprintf(new Object[] {
					utils.detector_denzo2strategy(denzoTable.getFirstStringValue("detector")),
					denzoTable.getFirstValue("spg"),
					denzoTable.getFirstValue("resolution_limits")}));
			PrepareStrategyTodo(session,msgId,utils,workDir+"strategy_run1.todo",new PrintfFormat(
					"XFILE name0.x\n"+//"XFILE %sname0.x\n"+
					"ALREADY MEASURED name1.sca\n"+
					"COMPLETENESS 99.9 99.8 99.7 99 98 95\n"+
					"NBINS 10\n"+
					"PSFILE strategy0.ps\n"+
					"FORMAT %s\n"+          //MAR225
					"SPACE GROUP %s\n"+     //P3221
					"RESOLUTION %s\n" +      //50.00  2.00
					"TURBO 10\n"
			).sprintf(new Object[] {
					//indir,
					utils.detector_denzo2strategy(denzoTable.getFirstStringValue("detector")),
					denzoTable.getFirstValue("spg"),
					denzoTable.getFirstValue("resolution_limits")}));
			PrepareStrategyTodo(session,msgId,utils,workDir+"strategy_run2.todo",new PrintfFormat(
					"XFILE name0.x\n"+//"XFILE %sname0.x\n"+
					"ALREADY MEASURED name1.sca\n"+
					"COMPLETENESS 99.9 \n"+
					"NBINS 10\n"+
					"PSFILE strategy0_OptAlign.ps\n"+
					"FORMAT %s\n"+          //MAR225
					"SPACE GROUP %s\n"+     //P3221
					"RESOLUTION %s\n" +      //50.00  2.00
					"ALIGN ALL\n"+
					"TURBO 10\n"
			).sprintf(new Object[] {
					//indir,
					utils.detector_denzo2strategy(denzoTable.getFirstStringValue("detector")),
					denzoTable.getFirstValue("spg"),
					denzoTable.getFirstValue("resolution_limits")}));
			PrepareStrategyTodo(session,msgId,utils,workDir+"strategy_run3.todo",new PrintfFormat(
					"XFILE name2.x\n"+//"XFILE %sname0.x\n"+
					"ALREADY MEASURED name1.sca\n"+
					"COMPLETENESS 99.9 99.8 99.7 99 98 95\n"+
					"NBINS 10\n"+
					"PSFILE strategy2.ps\n"+
					"FORMAT %s\n"+          //MAR225
					"SPACE GROUP %s\n"+     //P3221
					"RESOLUTION %s\n" +      //50.00  2.00
					"TURBO 10\n"
			).sprintf(new Object[] {
					//indir,
					utils.detector_denzo2strategy(denzoTable.getFirstStringValue("detector")),
					denzoTable.getFirstValue("spg"),
					denzoTable.getFirstValue("resolution_limits")}));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
    		utils.printErrorMessage(session, msgId, "Problem with generating the strategy todo files");
		}
    	
//    	ExecutingStrategy(session,msgId,utils,workDir,"strategy_run0.todo",true,1);
//    	ExecutingStrategy(session,msgId,utils,workDir,"strategy_run1.todo",true,2);
//    	//if we execute, the system automatically gets the best result
//    	//but there is no kappa-phi calculted to this optimal orientations
//    	//ExecutingStrategy(session,msgId,utils,workDir,"strategy_run2.todo",true,3);
    }

	public String getCreditString() {
		return "using strategy from Raimond Ravelli";
	}

	@Override
	public void initPlugin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void closePlugin() {
		// TODO Auto-generated method stub
		
	}
    
	
	
	
}


