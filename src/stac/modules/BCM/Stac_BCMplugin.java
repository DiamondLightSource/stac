package stac.modules.BCM;

import stac.core.*;
import stac.gui.*;

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


public interface Stac_BCMplugin {
    public double getMotorPosition(String motorName) throws Exception ;
    public void setMotorPosition(String motorName,double newValue) throws Exception ;
    public void moveMotor(String motorName,double newValue) throws Exception ;
    public double getMotorSpeed(String motorName) throws Exception ;
    public double getMotorSpeedLowLimit(String motorName) throws Exception ;
    public void setMotorSpeed(String motorName,double newValue) throws Exception ;
    public ParamTable getMotorParams(String motorName) throws Exception ;
    public void moveMotors(ParamTable newPositions) throws Exception ;
    public void moveMotorsSyncronized(ParamTable newPositions) throws Exception ;
    public void initMotors() throws Exception ;
    public double loadMotorPosition(String motorName,String data);
    public String saveMotorPositions(ParamTable positions,String data);
    public boolean checkDatumTrans(Point3d dat, Point3d trans);
    public void centerNeedle() throws Exception ;
    //maybe removeable
    public double convertMotorPosition_Plugin2Stac(String motorName,double pluginPos);
    public double convertMotorPosition_Stac2Plugin(String motorName,double stacPos);

    //base
    //public void interpret_specdef(String specline,ParamTable Descr);
    //public void read_specdef(String specfile,ParamTable Descr);
    //void update_specdef(ParamTable BCM_Descriptor,ParamTable newParams);
    public void getCalibration(ParamTable BCM_Descriptor,Vector3d okp[]);
    public void setCalibration(Stac_Configuration calib,ParamTable BCM_Descriptor,Vector3d[] par);
    //public void setCalibration(ParamTable BCM_Descriptor,String gnsfile,StacUtil utils);
	public void registerMotorListener(String motorName, MotorWidget_actionAdapter listener);
	public Vector getMotorListeners(String motorName);
	public boolean useCentralMotorListener();



}


