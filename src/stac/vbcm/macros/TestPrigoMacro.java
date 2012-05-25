package stac.vbcm.macros;

import java.util.Vector;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import stac.core.ParamTable;
import stac.vbcm.macros.MacroInterface;

public class TestPrigoMacro extends BasicMacro implements MacroInterface {

	public TestPrigoMacro() {
		String[] PreferredMotorNames={"X","Y","Z","Chi"};
		String[] PreferredDeviceNames={"PRIGO_SLIDER1","PRIGO_SLIDER2","PRIGO_SLIDER3","PRIGO_SLIDER4","PRIGO_ORION","PRIGO_SWING"};
	}
	
	//CONFIG
    ///////////////////////////
    public void setConfig(String param) throws Exception {
    }
	
	
    //VIRTUAL MOTORS
    ///////////////////////////
	Transform3D ident = new Transform3D(new double[]{1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1}); 
	Transform3D [] trafos= new Transform3D[]{new Transform3D(ident),new Transform3D(ident),new Transform3D(ident),new Transform3D(ident),new Transform3D(ident),new Transform3D(ident)};
	
	public void moveSingleMotor(String motorName, double Value, boolean absolute) throws Exception {
		//get motorId
		int i;
		for(i=0;i<registeredMotorNames.size();i++) {
			if (motorName.equals((String)registeredMotorNames.elementAt(i)))
				break;
		}
		if (i<registeredMotorNames.size()) {
			//perform motion
			Transform3D trf=new Transform3D();
			trf.setTranslation(new Vector3d(0.,0.,Value));
			if (absolute)
				trafos[i].set(trf);
			else
				trafos[i].mul(trf);
			switch (i) {
			case 0: 
				// X
				if (absolute)
					trafos[4].set(trf);
				else
					trafos[4].mul(trf);
				break;
			case 1:
				// Y
				break;
			case 2:
				// Z
				break;
			case 3:
				// Chi
				if (absolute)
					trafos[5].set(trf);
				else
					trafos[5].mul(trf);
				break;
			}
			//apply new matrices
			for (int j=0;j<registeredDevices.size();j++) {
				Transform3D trfRes=new Transform3D();
				trfRes.set(trafos[j]);
				switch (j) {
				case 0:
					// "PRIGO_SLIDER1"
					break;
				case 1:
					// "PRIGO_SLIDER2"
					break;
				case 2:
					// "PRIGO_SLIDER3"
					break;
				case 3:
					// "PRIGO_SLIDER4"
					break;
				case 4:
					// "PRIGO_ORION"
					break;
				case 5:
					// "PRIGO_SWING"
					break;
				}
				((TransformGroup)(registeredDevices.elementAt(j))).setTransform(trfRes);
			}
		} else {
			//System.out.println("Could not find motor "+motorName);
		}
		
	}
	
	public double getMotorPos(String motorName) throws Exception {
		return 10.0;
	}



}
