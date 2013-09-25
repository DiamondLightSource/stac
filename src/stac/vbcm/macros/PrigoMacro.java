package stac.vbcm.macros;

import java.util.Vector;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import stac.core.ParamTable;
import stac.vbcm.macros.MacroInterface;

import PrigoGM.PrigoGM;

/**
 * TODO: min/max pos (implemented in PrigoGM, but has not been exported)
 *       is needed for proper operation of Tango front end
 *       now, it does not accept motor motion requests (exceeding limits)
 * @author sudol
 *
 */

public class PrigoMacro extends BasicMacro implements MacroInterface {

	PrigoGM myPrigo = new PrigoGM();

	Transform3D InitTrafo = null;
	Transform3D InitTrafo_inv = null;
	
	public PrigoMacro() {
		String[] PreferredMotorNames={"X","Y","Z","Chi","Phi"};
		String[] PreferredDeviceNames={"PRIGO_SLIDER1","PRIGO_SLIDER2","PRIGO_SLIDER3","PRIGO_SLIDER4","PRIGO_SCHUB","PRIGO_ORION","PRIGO_SWING","PRIGO_PHI"};
	}

	//CONFIG
    ///////////////////////////
    public void setConfig(String param) throws Exception {
    	String [] strs=param.split(" ");
    	double [] nums= new double[strs.length];
    	for (int i=0; i<strs.length;i++)
    		nums[i]=new Double(strs[i]).doubleValue();   	
        InitTrafo=new Transform3D(nums);  	
    	InitTrafo_inv=new Transform3D(InitTrafo);
    	InitTrafo_inv.invert();
    }
	
	
    //VIRTUAL MOTORS
    ///////////////////////////
	
	public void moveSingleMotor(String motorName, double Value, boolean absolute) throws Exception {
		//get motorId
		int i;
		for(i=0;i<registeredMotorNames.size();i++) {
			if (motorName.equals((String)registeredMotorNames.elementAt(i)))
				break;
		}
		if (i<registeredMotorNames.size()) {
			//perform motion
			switch (i) {
			case 0: 
				// X
				//if we want to control the Prigo in normal VBS untis,
				//we have to scale the translation motors:
				//convert from VBS unit [m] to Prigo [mm]
				//Value*=1000;
				if (absolute)
					myPrigo.setSHX(Value);
				else {
					double ref=myPrigo.getSHX();
					myPrigo.setSHX(ref+Value);
				}
				break;
			case 1:
				// Y
				if (absolute)
					myPrigo.setSHY(Value);
				else {
					double ref=myPrigo.getSHY();
					myPrigo.setSHY(ref+Value);
				}
				break;
			case 2:
				// Z
				if (absolute)
					myPrigo.setSHZ(Value);
				else {
					double ref=myPrigo.getSHZ();
					myPrigo.setSHZ(ref+Value);
				}
				break;
			case 3:
				// Chi
				if (absolute)
					myPrigo.setCHI(Value);
				else {
					double ref=myPrigo.getCHI();
					myPrigo.setCHI(ref+Value);
				}
				break;
			case 4:
				// Phi
				if (absolute)
					myPrigo.setPHI(Value);
				else {
					double ref=myPrigo.getPHI();
					myPrigo.setPHI(ref+Value);
				}
				break;
			}
			Transform3D trf=new Transform3D();

			//apply new matrices
			for (int j=0;j<registeredDevices.size();j++) {
				switch (j) {
				case 0:
					// "PRIGO_SLIDER1"
					trf=myPrigo.getTM_slider1();
					break;
				case 1:
					// "PRIGO_SLIDER2"
					trf=myPrigo.getTM_slider2();
					break;
				case 2:
					// "PRIGO_SLIDER3"
					trf=myPrigo.getTM_slider3();
					break;
				case 3:
					// "PRIGO_SLIDER4"
					trf=myPrigo.getTM_slider4();
					break;
				case 4:
					// "PRIGO_SCHUB"
					trf=myPrigo.getTM_schubstange();
					break;
				case 5:
					// "PRIGO_ORION"
					trf=myPrigo.getTM_orion();
					break;
				case 6:
					// "PRIGO_SWING"
					trf=myPrigo.getTM_swing();
					break;
				case 7:
					// "PRIGO_PHI"
					trf=myPrigo.getTM_spinepin();
					break;
				}
				//as Prigo uses the unit [mm],				
				//we have to scale the translation
				//from [mm] to unit [m] as in the VRML model
	  	    	Vector3d currentTrans= new Vector3d();
	  	    	trf.get(currentTrans);
				currentTrans.scale(0.001);
				trf.setTranslation(currentTrans);
				
				//TODO: 
				//Prigo calculations are done in a certain lab frame
				//if we transform the object somewhere else,
				//the calculated prigo transformations must also be transformed!
				//apply the inittrafo:
				//inittrafo_again*freshly_calculated*inverse_back
				if (InitTrafo!=null) {
					trf.mul(trf,InitTrafo_inv);
					trf.mul(InitTrafo,trf);
				}
				
				//apply the new trafo
				((TransformGroup)(registeredDevices.elementAt(j))).setTransform(trf);
			}
		} else {
			//System.out.println("Could not find motor "+motorName);
		}
		
	}
	
	public double getMotorPos(String motorName) throws Exception {
		//get motorId
		int i;
		for(i=0;i<registeredMotorNames.size();i++) {
			if (motorName.equals((String)registeredMotorNames.elementAt(i)))
				break;
		}
		if (i<registeredMotorNames.size()) {
			//PrigoGM myPrigo = new PrigoGM();
			//perform motion
			switch (i) {
			case 0: 
				// X
				return myPrigo.getSHX();
			case 1:
				// Y
				return myPrigo.getSHY();
			case 2:
				// Z
				return myPrigo.getSHZ();
			case 3:
				// Chi
				return myPrigo.getCHI();
			case 4:
				// Phi
				return myPrigo.getPHI();
			}
		}
		return 0;
	}


}
