package stac.vbcm.macros;

import javax.media.j3d.TransformGroup;

import org.sudol.sun.simulator.SimulatorFrame;

public interface MacroInterface {
	
	//CONFIG
    ///////////////////////////
    public void setConfig(String param) throws Exception ;
    public void setInstanceName(String name);
    public void setFrame(SimulatorFrame frame);
    
    
    //OBJECTS
    ///////////////////////////
    //add VRML elements (one by one) as configured by the user
    public void registerElement(TransformGroup macroDevice) throws Exception ;
    //retrieve the list of macroObject Names those will be manipulated by the macro 
    public String[] getPreferredDeviceNames() throws Exception ;
    
    //VIRTUAL MOTORS
    ///////////////////////////
    //add motor Names (one by one) as configured by the user
    public void registerActualMotors(String[] objectNames) throws Exception ;
    //retrieve the list of motors as described by the macro
    public String[] getPreferredMotorNames() throws Exception ;
    //set/get pos
    public void moveMotor(String motorName,double Value,boolean absolute) throws Exception ;
    public double getMotorPos(String motorName) throws Exception ;

}
