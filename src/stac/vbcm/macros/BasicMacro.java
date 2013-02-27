package stac.vbcm.macros;

import java.util.Vector;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.sudol.sun.simulator.SimulatorFrame;

import stac.vbcm.macros.MacroInterface;

public abstract class BasicMacro implements MacroInterface {

	String[] PreferredMotorNames;
	String[] PreferredDeviceNames;
	
	Vector registeredMotorNames=new Vector();
	Vector registeredDevices=new Vector();
	
	String name="";
	SimulatorFrame frame;

	//CONFIG
    ///////////////////////////
	public void setInstanceName(String name) {
		this.name=name;
	}
    public void setFrame(SimulatorFrame frame){
    	this.frame=frame;
    }

    //OBJECTS
    ///////////////////////////
	public void registerElement(TransformGroup macroDevice) throws Exception {
		registeredDevices.addElement(macroDevice);		
	}
	
	public String[] getPreferredDeviceNames() throws Exception {
		return PreferredDeviceNames;
	}

	
    //VIRTUAL MOTORS
    ///////////////////////////
	public void registerActualMotors(String[] objectNames) throws Exception {
		for (int i=0; i< objectNames.length;i++)
			registeredMotorNames.addElement(objectNames[i]);		
	}
	
	public String[] getPreferredMotorNames() throws Exception {
		String [] names=new String[PreferredMotorNames.length];
		for (int i=0; i< PreferredMotorNames.length;i++)
			names[i]=name+"_"+PreferredMotorNames[i];	
		return names;
	}

	public abstract void moveSingleMotor(String motorName, double Value, boolean absolute) throws Exception;

	public final synchronized void moveMotor(String motorName, double Value, boolean absolute) throws Exception {
		moveSingleMotor(motorName,Value,absolute);
	}

}
