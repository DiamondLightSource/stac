package stac.core.SERVER;

import org.sudol.sun.util.*;

import stac.core.*;
import stac.core.SERVER.plugins.Stac_SERVERplugin_tango;
import stac.modules.BCM.*;
import stac.modules.BCM.plugins.Stac_BCMplugin_vBCM;
import stac.modules.Alignment.*;
import stac.modules.Strategy.*;
import stac.vbcm.vBCM;
import stac.vbcm.tine.vbcm_server;

import java.io.*;

import javax.vecmath.*;

import java.util.Properties;
import java.util.Vector;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import dna.xml.Bcm_parameters_response;
import dna.xml.Beam;
import dna.xml.Cell;
import dna.xml.Collect_data_request;
import dna.xml.Collect_request;
import dna.xml.Collect_response;
import dna.xml.Fileinfo;
import dna.xml.Index_response;
import dna.xml.Kappa_motor_setting;
import dna.xml.Kappa_collect_settings;
import dna.xml.Kappa_possible_alignment;
import dna.xml.Kappa_strategy_sweep;
import dna.xml.Matrix;
import dna.xml.Oscillation_sequence;
import dna.xml.Resolution;
import dna.xml.Status;
import dna.xml.Strategy_interpretation;
import dna.xml.Strategy_request;
import dna.xml.Strategy_response;
import dna.xml.Strategy_settings;

import dna.xml.Kappa_alignment;
import dna.xml.Kappa_alignment_request;
import dna.xml.Kappa_alignment_response;
import dna.xml.Kappa_strategy_request;
import dna.xml.Kappa_strategy_response;
import dna.xml.types.Status_code;


class ServerThread extends Thread {
	Stac_SERVERplugin lastPlugin; 
	
	public ServerThread(Stac_SERVERplugin lastPlugin) {
		this.lastPlugin=lastPlugin;
	}
	
	public void run() {
		lastPlugin.activate();
	}
}



/**
 * restriction: it can handle one request at a time!
 * @author sudol
 *
 */
public class StacSERVER extends Stac_PluginSlot {
	
	StacBCM bcm;

	Util utils = new Util();
	
	@Override
	public boolean doAcceptJob(Object job) {
		return false;
	}
	
    public StacSERVER (Stac_Session session) {
    	super(session,"SERVER");
    	//session.setAllowGUI(false);
    	//startBCM();

    }
    
    void startBCM () {
    	bcm = new StacBCM(session);
    }

    //public void closePlugin() {
    //	closePlugin((Stac_Plugin)activeVBSPlugin);
    //	activeVBSPlugin=null;
    //	super.closePlugin();
    //}

	/**
	 * usage StacSERVER plugins
	 * @param argv
	 */
	public static void main(String[] argv)
	{
		try
		{
			int argct=0;
			
			if (argv.length == 0) {
				Stac_Out.println("Usage: java StacSERVER [-background] plugins");
				System.exit(0);
			}

			boolean serverLoad = argv[0].equals("-background");
			
			if (serverLoad) {
				argct++;
				if (argv.length == 1) {
					Stac_Out.println("Usage: java StacSERVER [-background] plugins");
					System.exit(0);
				}
			}

			Stac_Session session = new Stac_Session();
			session.setAllowGUI(!serverLoad);
			StacSERVER MainServer=new StacSERVER(session);

			//load the front end servers (load/setinterface/activate)
	    	//calib.getConfiguration();
	    	//utils= new Util();
			Stac_SERVERplugin [] lastPlugin=new Stac_SERVERplugin[argv.length];
			for (int i=argct;i<argv.length;i++) {
				lastPlugin[i]=(Stac_SERVERplugin)MainServer.addPlugin("SERVERplugin_"+argv[i],"");
				lastPlugin[i].setInterface(MainServer);
				lastPlugin[i].initPlugin();
			}
			//we had to first create the tango server (above)
			//and only after create the tango client (below)
			//(tango bug reported to Pascal)
			MainServer.startBCM();
			for (int i=argct;i<argv.length;i++) {
				ServerThread srv = new ServerThread(lastPlugin[i]);
				srv.start();
			}


		}

		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		
		//System.exit(-1);		
	}
	
	@Override
	protected Stac_RespMsg process(Stac_ReqMsg req, Stac_JobAction job) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * TranslationCorrectionIntern: 
	 * calculates the new Translation settings to keep 
	 * a certain point centered at a given datum (datumtrans)
	 * while changing the datum
	 * Note that Calibration seetings must be given in the same labframe where the motor settings are
	 * Also note that in the calibration space the rotation about a gonio motor 
	 * is approached by a Circle!
	 * @param datumtrans original point centered: Omega, Kappa, Phi, X, Y, Z
	 * @param newDatum new gonio settings: Omega, Kappa , Phi
	 * @param calib List of vectors:<br>
	 * - COmegaDir in Cambridge convention (not Used)<br>
	 * - CKappaDir in Cambridge convention (not Used)<br>
	 * - CPhiDir   in Cambridge convention (not Used)<br>
	 * - KappaPt   point on the Kappa Axis<br>
	 * - PhiPt     point on the Phi Axis<br>
	 * - KappaDir  direction of Kappa Axis<br>
	 * - PhiDir    direction of Phi Axis<br>
	 * Could also take X,Y,Z scaling and offset as normal BCM plugins allow
	 * @return
	 */
	public double[] TranslationCorrectionIntern(double [] datumtrans,double newDatum[], Vector3d[] calib) {
		Stac_Out.println("Translation Correction Calculation Request Received");
		double k=0,p=0,x=0,y=0,z=0,k2=0,p2=0;
		k=datumtrans[1];
		p=datumtrans[2];
		x=datumtrans[3];
		y=datumtrans[4];
		z=datumtrans[5];
		k2=newDatum[1];
		p2=newDatum[2];
		Point3d newTrans = utils.CalculateTranslationWithCalib(new Point3d(x,y,z),k,k2,p,p2,calib);
		Stac_Out.println("Translation Correction Calculation Performed");
		return new double [] {newTrans.x,newTrans.y,newTrans.z};
		
	}
	
	/**
	 * TranslationCorrection: 
	 * calculates the new Translation settings to keep 
	 * a certain point centered at a given datum (datumtrans)
	 * while changing the datum
	 * Note that motor settings are in Pluginposition (used by the active/calibrated BCM plugin)
	 * @param datumtrans original point centered: Omega, Kappa, Phi, X, Y, Z
	 * @param newDatum new gonio settings: Omega, Kappa , Phi
	 * @return
	 */
	public double[] TranslationCorrection(double [] datumtrans,double newDatum[]) {
		Stac_Out.println("Translation Correction Calculation Request Received");
		double k=0,p=0,x=0,y=0,z=0,k2=0,p2=0;
		k=bcm.convertMotorPosition_Plugin2Stac("Kappa", datumtrans[1]);
		p=bcm.convertMotorPosition_Plugin2Stac("Phi", datumtrans[2]);
		x=bcm.convertMotorPosition_Plugin2Stac("X", datumtrans[3]);
		y=bcm.convertMotorPosition_Plugin2Stac("Y", datumtrans[4]);
		z=bcm.convertMotorPosition_Plugin2Stac("Z", datumtrans[5]);
		k2=bcm.convertMotorPosition_Plugin2Stac("Kappa", newDatum[1]);
		p2=bcm.convertMotorPosition_Plugin2Stac("Phi", newDatum[2]);
		Point3d newTrans = utils.CalculateTranslation(new Point3d(x,y,z),k,k2,p,p2,bcm);
		Stac_Out.println("Translation Correction Calculation Performed");
		return new double [] {
				bcm.convertMotorPosition_Stac2Plugin("X",newTrans.x),
				bcm.convertMotorPosition_Stac2Plugin("Y",newTrans.y),
				bcm.convertMotorPosition_Stac2Plugin("Z",newTrans.z)};
		
	}
	
	
}
