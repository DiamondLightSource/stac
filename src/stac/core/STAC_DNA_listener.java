package stac.core;

import org.sudol.sun.util.*;

//import stac.gui.ReorientTableModel;
import stac.modules.BCM.*;
import stac.modules.Alignment.*;
import stac.modules.Strategy.*;

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


/**
 * restriction: it can handle one request at a time!
 * @author sudol
 *
 */
public class STAC_DNA_listener extends Stac_RespHandler {
	//Session
	Stac_Session session;
	//utility module
	Util      utils;
	//plugin modules
	StacBCM       bcm;
	StacAlignment alignment;
	StacStrategy  strategy;
	
	static String workDir="";
	static Kappa_alignment_response _kappa_alignment_response= new Kappa_alignment_response();
	static Status stat=new Status();
	static String credit="";
	static String calcType="";

	
	
	public STAC_DNA_listener(String[] args) {
		String req="";
		//String workDir="";
		session=new Stac_Session();
		session.setAllowGUI(false);
		try {
			req=args[0];
			workDir=args[1];
			if (workDir.length()>1 && workDir.substring(0,1).equals("-")) {
				workDir= new String(workDir.substring(1));
			}
		} catch (Exception e) {
			session.errorMsg("Insufficient number of arguments passed");
		}
		
		bcm   = new StacBCM(session);
		utils = new Util();//"DNA");
		
		if (req.equalsIgnoreCase("kappa_alignment")) {
			alignment = new StacAlignment(session);
			AlignmentCalculation_requested(workDir);
		} else if (req.equalsIgnoreCase("kappa_strategy")) {
//			alignment = new StacAlignment(session);
			strategy  = new StacStrategy(session);
			StrategyCalculation_requested(workDir);			
		} else if (req.equalsIgnoreCase("kappa_collect_settings")) {
			BCMaction_requested(workDir);			
		} else if (req.equalsIgnoreCase("kappa_settings_request")) {
			BCMinfo_requested(workDir);
		} else if (req.equalsIgnoreCase("translation_correction")) {
			//all values must be plugin values
			TranslationCorrection_requested(workDir,bcm,utils);
		} else if (req.equalsIgnoreCase("kappa_orientation_request")) {
			//valid types as next parameter:
			//SmartSpotSeparation
			//Anomalous
			//Cell
			//SmallestOverallOscillation
			//MultiCrystalReference
			Orientation_requested(workDir,args[2],bcm,utils);
		} else {
			Stac_Out.println("Unrecognised Request: "+req);
		}
		
		//TODO if necessary: waiting loop for job finished
//		Thread sleep = null;
//		sleep= new Thread();
//		sleep.start();
//		try {
//			sleep.sleep(2000);
//		} catch (InterruptedException e) {
//		}
//		sleep.stop();
		
		session.closeReqHandlers();
		
	}
	
	private void TranslationCorrection_requested(String workDir,StacBCM bcm,Util utils) {
		Stac_Out.println("Translation Correction Calculation Request Received");
		ParamTable center = null;
		ParamTable newPos = null;
		double k=0,p=0,x=0,y=0,z=0,k2=0,p2=0;
		try {
			center = utils.LoadOMDescriptorTable(workDir+"trans_center.sav",bcm);
			k=new Double(center.getFirstStringValue("kappa"));
			p=new Double(center.getFirstStringValue("phi"));
			x=new Double(center.getFirstStringValue("X"));
			y=new Double(center.getFirstStringValue("Y"));
			z=new Double(center.getFirstStringValue("Z"));
			newPos = utils.LoadOMDescriptorTable(workDir+"trans_newPos.sav",bcm);
			k2=new Double(newPos.getFirstStringValue("kappa"));
			p2=new Double(newPos.getFirstStringValue("phi"));
		} catch (Exception e) {
			//error
			Stac_Out.println("Error reading the motorPositions");
		}
		Point3d newTrans = utils.CalculateTranslation(new Point3d(x,y,z),k,k2,p,p2,bcm);
		String result = bcm.saveDatumTrans(0,k2,p2,newTrans.x,newTrans.y,newTrans.z);
		FileWriter newFile;
		try {
			newFile = new FileWriter(workDir+"trans_result.sav");
			newFile.write(result);
			newFile.close();
		} catch (Exception e) {
			//error
			Stac_Out.println("Error writing the results of the Translation Correction Calculation");
		}
		
		Stac_Out.println("Translation Correction Calculation Performed");
	}

	void convertDNAformat(String workDir) {
		
		if (new File(workDir+"bestfile.par").canRead()) {
			String mosfile=workDir+"DNA_mosflm.inp";
			//input conversion
			String denzoname=workDir+"name0.x";
			utils.ConvertInputFormat(workDir+"bestfile.par",mosfile,"mosflm",denzoname);
			//utils.ConvertInputFormat(matfile,mosfile,"mosflm",denzoname);
		} else if (new File(workDir+"CORRECT.LP").canRead()) {
			String mosfile=workDir+"";
			//input conversion
			String denzoname=workDir+"name0.x";
			utils.ConvertInputFormat(workDir+"CORRECT.LP",mosfile,"xds",denzoname);
			//utils.ConvertInputFormat(matfile,mosfile,"mosflm",denzoname);
		} else if (new File(workDir+"IDXREF.LP").canRead()) {
			String mosfile=workDir+"";
			//input conversion
			String denzoname=workDir+"name0.x";
			utils.ConvertInputFormat(workDir+"IDXREF.LP",mosfile,"xds",denzoname);
			//utils.ConvertInputFormat(matfile,mosfile,"mosflm",denzoname);
		} else {
			Stac_Out.println("Could not find Orientation matrix file");
		}
		
	}

	/**
	 * types:
	 *   SmartSpotSeparation
	 *   Anomalous
	 *   Cell
	 *   SmallestOverallOscillation
	 *   MultiCrystalReference
	 *   
	 * @param workDir
	 * @param type
	 * @param bcm
	 * @param utils
	 */
	void Orientation_requested(String workDir,String type,StacBCM bcm,Util utils) {
		Stac_Out.println("Fast Orientation Suggestion Request Received");

		Vector res = new Vector(0);//v1-v2-comment
		stat.setCode(Status_code.OK);
		boolean close=false;

		//SSS
		//input: Char (OM,resolution,wavelength), act datum
		if ("SmartSpotSeparation".equalsIgnoreCase(type)) {
			//SSS
			//input: Char (OM,resolution,wavelength), act datum
			convertDNAformat(workDir);
			
			//read DNA_STAC_Kappa_Settings

			
			//datumtrans from DNA
			Vector3d datumGet=new Vector3d();
			try
			{
					String xml_message = utils.opReadCl(workDir+"DNA_STAC_Kappa_Settings");
					StringReader reader = new StringReader(xml_message);
					Kappa_collect_settings _kappa_collect_settings =
						(Kappa_collect_settings) Unmarshaller.unmarshal(
								Kappa_collect_settings.class,
								reader);
					//datumtrans conversion
					ParamTable datumtrans = new ParamTable();
					for (int i=0;i<_kappa_collect_settings.getMotorSettingsCount();i++) {
						datumtrans.setSingleDoubleValue(_kappa_collect_settings.getMotorSettings(i).getMotorName(),_kappa_collect_settings.getMotorSettings(i).getMotorValue());
					}
					datumGet.set(
							datumtrans.getFirstDoubleValue("Omega"),
							datumtrans.getFirstDoubleValue("Kappa"),
							datumtrans.getFirstDoubleValue("Phi")
							);
//					actualTrans.set(
//							datumtrans.getFirstDoubleValue("X"),
//							datumtrans.getFirstDoubleValue("Y"),
//							datumtrans.getFirstDoubleValue("Z")
//							);
			}
			catch (Exception ex) {
				datumGet.set(0,0,0);
//				actualTrans.set(0,0,0);
			}
			
			//bcm.getCurrentDatumTrans(datumGet,actualTrans);
			Vector3d datum=new Vector3d(datumGet);

			//calculation
			Vector OM = utils.getHKLforOrthogonalBeamAlignment(""+datum.y,""+datum.z,workDir+"name0.x",bcm);

			res.add(OM.elementAt(0));
			res.add("");
			res.add("Smart Spot Separation ");
			credit="from Sandor Brockhauser";
			calcType="Smart Spot Separation ";
			close=true;
		} else if ("Anomalous".equalsIgnoreCase(type)) {
			//SAD
			//input: OM (spacegroup)
			String [] v1list = new String[] {"a*","b*","c*"};
			String spgFnd="generic";

			convertDNAformat(workDir);
  			ParamTable omT = new ParamTable();
  			utils.read_denzo_x(workDir+"name0.x",omT);
  			
  			String spg=omT.getFirstStringValue("spg");
			spg=spg.toUpperCase();
			spg=spg.replaceAll(" ", "");
			Integer id=null;
			try {
				id = (Integer) utils.getSPGlib().getNameForAnyParam(spg);
				v1list = (String [])utils.getSPGlib().getValueAt(id, utils.getSPGlibAnomAxes());
				spgFnd=spg;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


			//set it up
			for (int i=0;i<v1list.length;i++) {
				res.add(v1list[i]);
				res.add("");
				res.add("Anomalous Data Collection ("+spgFnd+") ");
			}

			credit="from Sandor Brockhauser";
			calcType="Anomalous Data Collection Orientation ";
			close=false;
		} else if ("Cell".equalsIgnoreCase(type)) {
			//STD
			//input: -
			String [] v1list = new String[] {"a*","a*","b*","b*","c*","c*"};
			String [] v2list = new String[] {"b*","c*","a*","c*","a*","b*"};
			//set it up
			for (int i=0;i<v1list.length;i++) {
				res.add(v1list[i]);
				res.add(v2list[i]);
				res.add("Cell Alignment");
			}
			credit="from Sandor Brockhauser";
			calcType="Standard Cell Alignment ";
			close=false;
		} else if ("SmallestOverallOscillation".equalsIgnoreCase(type)) {
			//OptAlign
			//input: Char
			convertDNAformat(workDir);
			res = utils.getOptAlign(null,0,workDir,"");
			credit="using STRATEGY from Raimond Ravelli";
			calcType="SPECIAL Optimal Alignment ";
			close=true;
		} else if ("MultiCrystalReference".equalsIgnoreCase(type)) {
			//Multi
			//input: Char (OM)
			convertDNAformat(workDir);
			res = utils.getHKLfromOMFile(workDir+"name0.x",bcm);
			res.add("Alignment as in "+workDir+"name0.x");
			credit="from Sandor Brockhauser";
			calcType="Multiple Crystal Reference Orinetation ";
			close=false;

		} else {
			Stac_Out.println("Fast Orientation Suggestion Request Received\n");
			Stac_Out.println("Usage:");
			Stac_Out.println("  kappa_orientation_request [type]");
			Stac_Out.println("type can be:");
			Stac_Out.println("  SmartSpotSeparation        - maximum spot separation without blind zone ");
			Stac_Out.println("  Anomalous                  - bringing Bijvoet pairs to the same image");
			Stac_Out.println("  Cell                       - standard reciprocal cell alignment");
			Stac_Out.println("  SmallestOverallOscillation - smalles DC wedge");
			Stac_Out.println("  MultiCrystalReference      - previous xtal orientation");

			return;
		}
		
		//output conversion
		Kappa_alignment_request _kappa_alignment_request=new Kappa_alignment_request();
		try {
				for(int i=0;i<res.size()/3;i++){
					
					Kappa_alignment desiredOrientation= new Kappa_alignment();
					desiredOrientation.setV1((String)res.elementAt(3*i+0));
					desiredOrientation.setV2((String)res.elementAt(3*i+1));
					desiredOrientation.setClose(new Boolean(close).toString());
					desiredOrientation.setComment((String)res.elementAt(3*i+2));
					_kappa_alignment_request.addDesired_orientation(desiredOrientation);
					
				}
		}
		catch (Exception ex2) {
			stat.setCode(Status_code.ERROR);
		}
		
		//writing output
		_kappa_alignment_request.setComment(calcType+"Calculation performed by STAC ("+credit+")\n"+((stat.getCode()==Status_code.ERROR)?"FAILED":"DONE"));

		write_kappa_alignment_request(workDir,_kappa_alignment_request);
		
	}
	
	
	
	void AlignmentCalculation_requested(String workDir) {
		//reading DNA matrix...input
		convertDNAformat(workDir);
		
		//reading special request
		Kappa_alignment_request _kappa_alignment_request=null;
		try
		{
			String xml_message = utils.opReadCl(workDir+"DNA_kappa_alignment_request");
			StringReader reader = new StringReader(xml_message);
			_kappa_alignment_request =
				(Kappa_alignment_request) Unmarshaller.unmarshal(
						Kappa_alignment_request.class,
						reader);
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		
		//orientation request conversion
		Vector orientations = new Vector(0);
		for (int i=0;i<_kappa_alignment_request.getDesired_orientationCount();i++) {
			Vector orient = new Vector(0);
			orient.addElement(_kappa_alignment_request.getDesired_orientation(i).getV1());
			orient.addElement(_kappa_alignment_request.getDesired_orientation(i).getV2());
			orient.addElement(new Boolean(_kappa_alignment_request.getDesired_orientation(i).getClose()));
			orient.addElement(_kappa_alignment_request.getDesired_orientation(i).getComment());
			orientations.addElement(orient);
		}

		
		//Kappa_alignment_response _kappa_alignment_response= new Kappa_alignment_response();
		//Status stat=new Status();
		stat.setCode(Status_code.OK);
		//String credit="";
		//String calcType="";
		
		if ("SPECIAL".equalsIgnoreCase((String)(((Vector)orientations.elementAt(0)).elementAt(0)))) {
			Vector res = new Vector(0);
			if ("OptAlign".equalsIgnoreCase((String)(((Vector)orientations.elementAt(0)).elementAt(1)))) {
				res = utils.getOptAlign(null,0,workDir,"");
				credit="using STRATEGY from Raimond Ravelli";
				calcType="SPECIAL Optimal Alignment ";
			}

			//output conversion
			try {
					for(int i=0;i<res.size()/3;i++){
						
						Kappa_possible_alignment possibleOrientation=new Kappa_possible_alignment();
						possibleOrientation.setV1((String)res.elementAt(3*i+0));
						possibleOrientation.setV2((String)res.elementAt(3*i+1));
						possibleOrientation.setOmega(0.);
						possibleOrientation.setKappa(0.);
						possibleOrientation.setPhi(0.);
						possibleOrientation.setTrans((String)res.elementAt(3*i+2));
						possibleOrientation.setRank(0.);
						_kappa_alignment_response.addPossible_orientation(possibleOrientation);
					}
			}
			catch (Exception ex2) {
				stat.setCode(Status_code.ERROR);
			}
			
			//writing output
			_kappa_alignment_response.setStatus(stat);
			_kappa_alignment_response.setComment(calcType+"Calculation performed by STAC ("+credit+")");
			write_kappa_alignment_response(workDir);
			
		} else {
			
		
		
		//reading DNA_BCM input
		//datumtrans!!! from DNA???
		Point3d datumGet=new Point3d();
		Point3d actualTrans=new Point3d();

		//read DNA_STAC_Kappa_Settings

		
		//datumtrans from DNA
		try
		{
				String xml_message = utils.opReadCl(workDir+"DNA_STAC_Kappa_Settings");
				StringReader reader = new StringReader(xml_message);
				Kappa_collect_settings _kappa_collect_settings =
					(Kappa_collect_settings) Unmarshaller.unmarshal(
							Kappa_collect_settings.class,
							reader);
				//datumtrans conversion
				ParamTable datumtrans = new ParamTable();
				for (int i=0;i<_kappa_collect_settings.getMotorSettingsCount();i++) {
					datumtrans.setSingleDoubleValue(_kappa_collect_settings.getMotorSettings(i).getMotorName(),_kappa_collect_settings.getMotorSettings(i).getMotorValue());
				}
				datumGet.set(
						datumtrans.getFirstDoubleValue("Omega"),
						datumtrans.getFirstDoubleValue("Kappa"),
						datumtrans.getFirstDoubleValue("Phi")
						);
				actualTrans.set(
						datumtrans.getFirstDoubleValue("X"),
						datumtrans.getFirstDoubleValue("Y"),
						datumtrans.getFirstDoubleValue("Z")
						);
		}
		catch (Exception ex) {
			datumGet.set(0,0,0);
			actualTrans.set(0,0,0);
		}
		
		//bcm.getCurrentDatumTrans(datumGet,actualTrans);
		Vector3d datum=new Vector3d(datumGet);
		
		
		
		//alignment calc
		Stac_AlignmentReqMsg req = new Stac_AlignmentReqMsg(session,this,workDir,datum,actualTrans,orientations,bcm,utils); 
		alignment.handle(req);
		}
		
	}
	
	void StrategyCalculation_requested(String workDir) {
		//reading DNA matrix...input
		convertDNAformat(workDir);
		
		//reading DNA_BCM input
		//datumtrans!!! from DNA???
		Point3d datumGet=new Point3d();
		Point3d actualTrans=new Point3d();
		//bcm.getCurrentDatumTrans(datumGet,actualTrans);
		//Vector3d datum=new Vector3d(datumGet);

		//read DNA_STAC_Kappa_Settings

		
		//datumtrans from DNA
		try
		{
				String xml_message = utils.opReadCl(workDir+"DNA_STAC_Kappa_Settings");
				StringReader reader = new StringReader(xml_message);
				Kappa_collect_settings _kappa_collect_settings =
					(Kappa_collect_settings) Unmarshaller.unmarshal(
							Kappa_collect_settings.class,
							reader);
				//datumtrans conversion
				ParamTable datumtrans = new ParamTable();
				for (int i=0;i<_kappa_collect_settings.getMotorSettingsCount();i++) {
					datumtrans.setSingleDoubleValue(_kappa_collect_settings.getMotorSettings(i).getMotorName(),_kappa_collect_settings.getMotorSettings(i).getMotorValue());
				}
				datumGet.set(
						datumtrans.getFirstDoubleValue("Omega"),
						datumtrans.getFirstDoubleValue("Kappa"),
						datumtrans.getFirstDoubleValue("Phi")
						);
				actualTrans.set(
						datumtrans.getFirstDoubleValue("X"),
						datumtrans.getFirstDoubleValue("Y"),
						datumtrans.getFirstDoubleValue("Z")
						);
		}
		catch (Exception ex) {
			datumGet.set(0,0,0);
			actualTrans.set(0,0,0);
		}
		
		//bcm.getCurrentDatumTrans(datumGet,actualTrans);
		Vector3d datum=new Vector3d(datumGet);
		
		
		
		
		
		
		
		//reading special request
		Kappa_strategy_request _kappa_strategy_request=null;
		Index_response _index_response=null;
		try
		{
			String xml_message = utils.opReadCl(workDir+"DNA_kappa_strategy_request");
			StringReader reader = new StringReader(xml_message);
			_kappa_strategy_request =
				(Kappa_strategy_request) Unmarshaller.unmarshal(
						Kappa_strategy_request.class,
						reader);
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		
		//datum request conversion
		Vector desiredDatums = new Vector(0);
		for (int i=0;i<_kappa_strategy_request.getDesired_datumCount();i++) {
			Vector3d newDatum = new Vector3d(
					_kappa_strategy_request.getDesired_datum(i).getOmega(),
					_kappa_strategy_request.getDesired_datum(i).getKappa(),
					_kappa_strategy_request.getDesired_datum(i).getPhi());
			desiredDatums.addElement(newDatum);
			String comment="";
			desiredDatums.addElement(comment);
		}
		
		String options="";		
		
		//strategy calc
		Stac_StrategyReqMsg req = new Stac_StrategyReqMsg(session,this,workDir,datum,desiredDatums,options,bcm,utils);
		strategy.handle(req);
		
	}
		
	
	void BCMaction_requested(String workDir) {
		
		//reading special request
		Kappa_collect_settings _kappa_collect_settings=null;
		try
		{
			String xml_message = utils.opReadCl(workDir+"DNA_kappa_collect_settings_request");
			StringReader reader = new StringReader(xml_message);
			_kappa_collect_settings =
				(Kappa_collect_settings) Unmarshaller.unmarshal(
						Kappa_collect_settings.class,
						reader);
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		
		//datum request conversion
		ParamTable newPositions = new ParamTable();
		for (int i=0;i<_kappa_collect_settings.getMotorSettingsCount();i++) {
			newPositions.setSingleDoubleValue(_kappa_collect_settings.getMotorSettings(i).getMotorName(),_kappa_collect_settings.getMotorSettings(i).getMotorValue());
		}
		
		//Transcorrectioncalc		
        double omega = utils.angleInRange(newPositions.getFirstDoubleValue("Omega"),0); //Stac_Out.println(omega);
        double kappa = utils.angleInRange(newPositions.getFirstDoubleValue("Kappa"),-5.0);
        double phi   = utils.angleInRange(newPositions.getFirstDoubleValue("Phi"),0);
        
		Point3d actualTrans= new Point3d();
		Point3d actualDatum= new Point3d();
		bcm.getCurrentDatumTrans(actualDatum,actualTrans);
        
        Point3d trans=utils.CalculateTranslation(actualTrans,actualDatum.y,kappa,actualDatum.z,phi,bcm);
		
        newPositions.setSingleDoubleValue("X",trans.x);
        newPositions.setSingleDoubleValue("Y",trans.y);
        newPositions.setSingleDoubleValue("Z",trans.z);
		
		//bcm action
		bcm.moveMotorsSyncronized(newPositions);
		//check the movement: getMotorPosition
		
		//output conversion
		Status stat=new Status();
		stat.setCode(Status_code.OK);
		
		//writing output
		stat.setMessage("BCM action performed by STAC");
		StringWriter stringWriter = new StringWriter();
		String xml_string="";
		try
		{
			stat.marshal(stringWriter);
			xml_string = stringWriter.toString();
			FileWriter responseFile = new FileWriter(workDir+"STAC_DNA_kappa_collect_settings_status");
			responseFile.write(xml_string);
			responseFile.close();
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		catch (IOException ex) {
			Stac_Out.println("IOException: " + ex);        	
		}
		
	}
	
	void BCMgeneralAction_requested(String workDir) {
		
		//reading special request
		Kappa_collect_settings _kappa_collect_settings=null;
		try
		{
			String xml_message = utils.opReadCl(workDir+"DNA_kappa_collect_settings_request");
			StringReader reader = new StringReader(xml_message);
			_kappa_collect_settings =
				(Kappa_collect_settings) Unmarshaller.unmarshal(
						Kappa_collect_settings.class,
						reader);
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		
		//datum request conversion
		ParamTable newPositions = new ParamTable();
		for (int i=0;i<_kappa_collect_settings.getMotorSettingsCount();i++) {
			newPositions.setSingleDoubleValue(_kappa_collect_settings.getMotorSettings(i).getMotorName(),_kappa_collect_settings.getMotorSettings(i).getMotorValue());
		}
		
		//bcm action
		bcm.moveMotorsSyncronized(newPositions);
		//check the movement: getMotorPosition
		
		//output conversion
		Status stat=new Status();
		stat.setCode(Status_code.OK);
		
		//writing output
		stat.setMessage("BCM action performed by STAC");
		StringWriter stringWriter = new StringWriter();
		String xml_string="";
		try
		{
			stat.marshal(stringWriter);
			xml_string = stringWriter.toString();
			FileWriter responseFile = new FileWriter(workDir+"STAC_DNA_kappa_collect_settings_status");
			responseFile.write(xml_string);
			responseFile.close();
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		catch (IOException ex) {
			Stac_Out.println("IOException: " + ex);        	
		}
		
	}
	
	void BCMinfo_requested(String workDir) {
				
		//bcm action
		Point3d trans= new Point3d();
		Point3d datum= new Point3d();
		bcm.getCurrentDatumTrans(datum,trans);
		
		//output conversion
		Kappa_collect_settings kapSet = new Kappa_collect_settings();
		Kappa_motor_setting motSet;
		motSet=new Kappa_motor_setting();motSet.setMotorName("Omega");motSet.setMotorValue(datum.x);kapSet.addMotorSettings(motSet);
		motSet=new Kappa_motor_setting();motSet.setMotorName("Kappa");motSet.setMotorValue(datum.y);kapSet.addMotorSettings(motSet);
		motSet=new Kappa_motor_setting();motSet.setMotorName("Phi");  motSet.setMotorValue(datum.z);kapSet.addMotorSettings(motSet);
		motSet=new Kappa_motor_setting();motSet.setMotorName("X");    motSet.setMotorValue(trans.x);kapSet.addMotorSettings(motSet);
		motSet=new Kappa_motor_setting();motSet.setMotorName("Y");    motSet.setMotorValue(trans.y);kapSet.addMotorSettings(motSet);
		motSet=new Kappa_motor_setting();motSet.setMotorName("Z");    motSet.setMotorValue(trans.z);kapSet.addMotorSettings(motSet);
		
		//writing output
		kapSet.setComment("BCM query performed by STAC");
		StringWriter stringWriter = new StringWriter();
		String xml_string="";
		try
		{
			kapSet.marshal(stringWriter);
			xml_string = stringWriter.toString();
			FileWriter responseFile = new FileWriter(workDir+"STAC_DNA_kappa_settings_response");
			responseFile.write(xml_string);
			responseFile.close();
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		catch (IOException ex) {
			Stac_Out.println("IOException: " + ex);        	
		}
		
	}
	
	
	
	
	/**
	 * Main method
	 *
	 * @param args String[]
	 */
	public static void main(String[] args) {
		

//		Properties pro=new Properties();
//		pro.setProperty("log4j.logger.org.exolab.castor.xml.Unmarshaller", "INFO, console");
//		pro.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
//		pro.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
//		pro.setProperty("log4j.appender.console.layout.ConversionPattern", "%d %-5p - %m%n");
//        PropertyConfigurator.configure(pro);
//        Logger logger = Logger.getLogger("org.exolab.castor.xml.Unmarshaller");
//        logger.info("test info");
        
		new STAC_DNA_listener(args);
		System.exit(0);
	}

	private void write_kappa_alignment_response(String workDir) {
		StringWriter stringWriter = new StringWriter();
		String xml_string="";
		try
		{
			_kappa_alignment_response.marshal(stringWriter);
			xml_string = stringWriter.toString();
			FileWriter responseFile = new FileWriter(workDir+"STAC_DNA_kappa_alignment_response");
			responseFile.write(xml_string);
			responseFile.close();
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		catch (IOException ex) {
			Stac_Out.println("IOException: " + ex);        	
		}
	}
	
	private void write_kappa_alignment_request(String workDir,Kappa_alignment_request req) {
		StringWriter stringWriter = new StringWriter();
		String xml_string="";
		try
		{
			req.marshal(stringWriter);
			xml_string = stringWriter.toString();
			FileWriter responseFile = new FileWriter(workDir+"DNA_kappa_alignment_request");
			responseFile.write(xml_string);
			responseFile.close();
		}
		catch (MarshalException mex)
		{
			Stac_Out.println("MarshallException:" + mex);
		}
		catch (ValidationException vex)
		{
			Stac_Out.println("ValidationException: " + vex);
		}
		catch (IOException ex) {
			Stac_Out.println("IOException: " + ex);        	
		}
	}
	
	@Override
	protected synchronized void handleResponse(Stac_RespMsg response) {
		if (response instanceof Stac_AlignmentRespMsg)
		{
			Stac_AlignmentRespMsg alignResp=(Stac_AlignmentRespMsg) response;
			
			String alignFile="";
			if (alignResp.status==alignResp.OK) {
				alignFile=workDir+alignResp.possibleDatumsFile;
			}
			//TODO credit should be stamped to each response!
			String credit=alignment.getCreditString();
			//alignFile=workDir+alignment.CalculateAlignment(workDir,datum,actualTrans,orientations,bcm,utils);
			
			//output conversion
            Vector res=utils.ReadCalculatedAlignmentFile(session, alignFile, response.requestId);
            if (res==null || res.size()==0) {
				stat.setCode(Status_code.ERROR);
            } else { 
            	for (int i=0;i<res.size();i++){
            		Vector actLine=(Vector)res.elementAt(i);
            		Kappa_possible_alignment possibleOrientation=new Kappa_possible_alignment();
            		possibleOrientation.setV1(((String)actLine.elementAt(0)));
            		possibleOrientation.setV2(((String)actLine.elementAt(1)));
            		possibleOrientation.setOmega(((Double)actLine.elementAt(2)).doubleValue());
            		possibleOrientation.setKappa(((Double)actLine.elementAt(3)).doubleValue());
            		possibleOrientation.setPhi(((Double)actLine.elementAt(4)).doubleValue());
            		possibleOrientation.setTrans(((String)actLine.elementAt(5)));
            		possibleOrientation.setRank(((Double)actLine.elementAt(6)).doubleValue());
            		//possibleOrientation.setRank(((String)actLine.elementAt(7)));
            		_kappa_alignment_response.addPossible_orientation(possibleOrientation);
            	}
			}
			
			
//			try {
//				Stac_Out.println("File to be read: " + alignFile);
//				FileInputStream GonsetResultFile = new FileInputStream(alignFile);
//				DataInputStream grf = new DataInputStream(GonsetResultFile);
//				try {
//					do {
//						double v1x = grf.readDouble();
//						double v1y = grf.readDouble();
//						double v1z = grf.readDouble();
//						double v2x = grf.readDouble();
//						double v2y = grf.readDouble();
//						double v2z = grf.readDouble();
//						double omega = grf.readDouble();
//						double kappa = grf.readDouble();
//						double phi   = grf.readDouble();
//						String trans = grf.readUTF();
//						double rank  = grf.readDouble();
//						
//						Kappa_possible_alignment possibleOrientation=new Kappa_possible_alignment();
//						possibleOrientation.setV1("("+v1x+";"+v1y+";"+v1z+")");
//						possibleOrientation.setV2("("+v2x+";"+v2y+";"+v2z+")");
//						possibleOrientation.setOmega(omega);
//						possibleOrientation.setKappa(kappa);
//						possibleOrientation.setPhi(phi);
//						possibleOrientation.setTrans(trans);
//						possibleOrientation.setRank(rank);
//						_kappa_alignment_response.addPossible_orientation(possibleOrientation);
//					}
//					while (true);
//				} catch (IOException grfex) {
//					grf.close();
//				}
//				GonsetResultFile.close();
//			}
//			catch (Exception ex2) {
//				stat.setCode(Status_code.ERROR);
//			}
			

			//writing output
			_kappa_alignment_response.setStatus(stat);
			_kappa_alignment_response.setComment(calcType+"Calculation performed by STAC ("+credit+")");
			
			write_kappa_alignment_response(workDir);
			
		} else if (response instanceof Stac_StrategyRespMsg) {
			
			Stac_StrategyRespMsg strResp=(Stac_StrategyRespMsg) response;
			
			String alignFile=strResp.req.outdir+strResp.get_strategyFile();
			
			
			String credit=strategy.getCreditString();
			
			
			//output conversion
			Status stat=new Status();
			stat.setCode(Status_code.OK);
			Kappa_strategy_response _kappa_strategy_response= new Kappa_strategy_response();
			try {
				Stac_Out.println("File to be read: " + alignFile);
				FileInputStream ResultFile = new FileInputStream(alignFile);
				DataInputStream grf = new DataInputStream(ResultFile);
				try {
					do {
			             int   strategyID = grf.readInt();//grf.readInt();
			             double omegaStart = grf.readDouble();
			             double omegaEnd   = grf.readDouble();
			             double kappa      = grf.readDouble();
			             double phi        = grf.readDouble();
			             double completion = grf.readDouble();
			             double res        = grf.readDouble();		             
			             double rank       = grf.readDouble();
			             String comment    = grf.readUTF();
						
						Kappa_strategy_sweep possibleSweep=new Kappa_strategy_sweep();
						possibleSweep.setStrategyID(strategyID);
						possibleSweep.setOmegaStart(omegaStart);
						possibleSweep.setOmegaEnd(omegaEnd);
						possibleSweep.setKappa(kappa);
						possibleSweep.setPhi(phi);
						possibleSweep.setCompleteness(completion);
						possibleSweep.setRank(rank);
						_kappa_strategy_response.addGenerated_sweep(possibleSweep);
					}
					while (true);
				} catch (IOException grfex) {
					grf.close();
				}
				ResultFile.close();
			}
			catch (Exception ex2) {
				stat.setCode(Status_code.ERROR);
			}
			
			//writing output
			_kappa_strategy_response.setStatus(stat);
			_kappa_strategy_response.setComment("Calculation performed by STAC ("+credit+")");
			StringWriter stringWriter = new StringWriter();
			String xml_string="";
			try
			{
				_kappa_strategy_response.marshal(stringWriter);
				xml_string = stringWriter.toString();
				FileWriter responseFile = new FileWriter(workDir+"STAC_DNA_kappa_strategy_response");
				responseFile.write(xml_string);
				responseFile.close();
			}
			catch (MarshalException mex)
			{
				Stac_Out.println("MarshallException:" + mex);
			}
			catch (ValidationException vex)
			{
				Stac_Out.println("ValidationException: " + vex);
			}
			catch (IOException ex) {
				Stac_Out.println("IOException: " + ex);        	
			}
						
		} else if (response instanceof Stac_AlignmentRespMsg) {
			
		} else if (response instanceof Stac_AlignmentRespMsg) {
			
		} else if (response instanceof Stac_AlignmentRespMsg) {
			
		} else if (response instanceof Stac_AlignmentRespMsg) {
			
		}
		
		
	}
	
}
