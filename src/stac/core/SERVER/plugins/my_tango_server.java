package stac.core.SERVER.plugins;

import org.sudol.sun.util.*;

import org.omg.CORBA.*;

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoDs.Util;
import fr.esrf.TangoApi.*;

import stac.core.*;
import stac.core.SERVER.StacSERVER;
import stac.core.SERVER.Stac_SERVERplugin;
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

import java.util.LinkedList;
import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;

/**
 * generic tango server
 * things to modify:
 * + backgroundDevice - StacSERVER
 * + getID
 * + my_tango_serverClass: 
 *            command_factory
 *            command classes calling the command wrappers
 * + my_tango_server: 
 *            command wrapper calling the "background" device
 */
	public class my_tango_server extends DeviceImpl implements TangoConst
	{
		StacSERVER backgroundDevice=null;
		
		public void setInterface(StacSERVER MainServer) {
			this.backgroundDevice=MainServer;		
		}
		
		String STATUSOK="STANDBY";
		String STATUSMOVE="MOVING";
		
		int requestCt=0;

		synchronized void addReq () {
			requestCt++;
		}
		synchronized void delReq () {
			if (requestCt>0)
				requestCt--;
		}
		synchronized int numReq () {
			return requestCt;
		}
		
		protected	int	state;

		String myID="";
		
		public void setID(String ID) {
			this.myID=ID;
		}
		
		public String getID() {
			return myID;
		}


//	=========================================================
	/**
	 *	Constructor for simulated Time Device Server.
	 *
	 *	@param	cl	The DeviceClass object
	 *	@param	s	The Device name.
	 */
//	=========================================================
		my_tango_server(DeviceClass cl, String s) throws DevFailed
		{
			super(cl,s);
			init_device();
		}
//	=========================================================
	/**
	 *	Constructor for simulated Time Device Server.
	 *
	 *	@param	cl	The DeviceClass object
	 *	@param	s	The Device name.
	 *	@param	d	Device description.
	 */
//	=========================================================
		my_tango_server(DeviceClass cl, String s, String d) throws DevFailed
		{
			super(cl,s,d);
			init_device();
		}


//	=========================================================
	/**
	 *	Initialize the device.
	 */
//	=========================================================
		public void init_device() throws DevFailed
		{
			System.out.println(getID()+" create " + device_name);

			//	Initialise variables to default values
			//-------------------------------------------
			set_state(DevState.ON);
			
		}

//	=========================================================
	/**
	 *	Method always executed before command execution.
	 */
//	=========================================================
		public void always_executed_hook()
		{	
			get_logger().info("In always_executed_hook method()");
		}

//	===================================================================
	/**
	 *	Method called by the write_attributes CORBA operation to
	 *	write device hardware.
	 *
	 *	@param	attr_list	vector of index in the attribute vector
	 *		of attribute to be written
	 */
//	===================================================================			
		public void write_attr_hardware(Vector attr_list)
		{
			get_logger().info("In write_attr_hardware for "+attr_list.size()+" attribute(s)");
		
		}
//	===================================================================
	/**
	 *	Method called by the read_attributes CORBA operation to
	 *	read device hardware
	 *
	 *	@param	attr_list	Vector of index in the attribute vector
	 *		of attribute to be read
	 */
//	===================================================================			
		public void read_attr_hardware(Vector attr_list) throws DevFailed
		{
			get_logger().info("In read_attr_hardware for "+attr_list.size()+" attribute(s)");

		}
//	===================================================================
	/**
	 *	Method called by the read_attributes CORBA operation to
	 *	set internal attribute value.
	 *
	 *	@param	attr	reference to the Attribute object
	 */
//	===================================================================			
		public void read_attr(Attribute attr) throws DevFailed
		{
			String attr_name = attr.get_name();
			get_logger().info("In read_attr for attribute " + attr_name);

		}



//	=========================================================
	/**
	 *	Execute command "Reset" on device.
	 *	Clear the current error and try to reset motors (stopped , loop closed
	 *
	 */
//	=========================================================
	public void reset() throws DevFailed
		{
			get_logger().info("Entering reset()");

			// ---Add your Own code to control device here ---

			get_logger().info("Exiting reset()");
		}



//	=========================================================
	/**
	 *	Execute command "State" on device.
	 *
	 * @param	argin	NewPositions
	 */
//	=========================================================
	public DevState dev_state()
		{
			get_logger().info("Entering getState()");
			
			String STATUS="";
			DevState state=super.get_state();
			
			try {
				if (numReq()>0 ) {
					STATUS=STATUSMOVE;
					state=DevState.MOVING;
				} else {
					STATUS=STATUSOK;
					state=DevState.STANDBY;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				state=DevState.OFF;
			}

			get_logger().info("Exiting getState()");
			//return STATUS;
			return state;
		}

//	=========================================================
	/**
	 *	Execute command "TranslationCorrectionIntern" on device.
	 *
	 * @param	argin	NewPositions
	 */
//	=========================================================
	/**
	 * Input parameters (in a single string):
	 * Kappa, Phi, X, Y, Z
	 * Kappa2, Phi2
	 * KPtx/y/z, PPtx/y/z
	 * KDx/y/z, PDx/y/z
	 * 
	 * Output is a string containing the new X, Y, Z
	 * 
	 * 
	 * eg:
	 * Input:
	 * Kappa=3.12, Phi=4.32, X=5.54,...
	 * Output:
	 * 6.243 5.1254
	 */
	public String TranslationCorrectionIntern(DevVarDoubleStringArray argin) throws DevFailed
		{
			get_logger().info("Entering TranslationCorrectionIntern()");
			String res="";

			// ---Add your Own code to control device here ---
	    	ParamTable newPos=new ParamTable();
	    	for(int i=0;i<argin.dvalue.length;i++){
	    		String motorName = (String)argin.svalue[i];
	    		newPos.setSingleDoubleValue(motorName,argin.dvalue[i]);
	    	}
	    	boolean coll=false;
	    	try {
				try {
						double[] newTrans=backgroundDevice.TranslationCorrectionIntern(
								new double[] {
										0,
										newPos.getFirstDoubleValue("Kappa"),
										newPos.getFirstDoubleValue("Phi"),
										newPos.getFirstDoubleValue("X"),
										newPos.getFirstDoubleValue("Y"),
										newPos.getFirstDoubleValue("Z")
								},
								new double[] {
										0,
										newPos.getFirstDoubleValue("Kappa2"),
										newPos.getFirstDoubleValue("Phi2")
								},
								new Vector3d[] {
										new Vector3d(0,0,0),
										new Vector3d(0,0,0),
										new Vector3d(0,0,0),
										new Vector3d(newPos.getFirstDoubleValue("KPtx"),newPos.getFirstDoubleValue("KPty"),newPos.getFirstDoubleValue("KPtz")),
										new Vector3d(newPos.getFirstDoubleValue("PPtx"),newPos.getFirstDoubleValue("PPty"),newPos.getFirstDoubleValue("PPtz")),
										new Vector3d(newPos.getFirstDoubleValue("KDx"),newPos.getFirstDoubleValue("KDy"),newPos.getFirstDoubleValue("KDz")),
										new Vector3d(newPos.getFirstDoubleValue("PDx"),newPos.getFirstDoubleValue("PDy"),newPos.getFirstDoubleValue("PDz"))
								}
								);
								
						res = ""+newTrans[0]+" "+newTrans[1]+" "+newTrans[2];
				}catch (Exception e) {
				}
			} catch (Exception e) {
			}
			

			get_logger().info("Exiting TranslationCorrectionIntern()");
			return res;
		}

//	=========================================================
	/**
	 *	Execute command "TranslationCorrection" on device.
	 *
	 * @param	argin	NewPositions
	 */
//	=========================================================
	/**
	 * Input parameters (in a single string):
	 * Kappa, Phi, X, Y, Z
	 * Kappa2, Phi2
	 * 
	 * Output is a string containing the new X, Y, Z
	 * 
	 * 
	 * eg:
	 * Input:
	 * Kappa=3.12, Phi=4.32, X=5.54,...
	 * Output:
	 * 6.243 5.1254
	 */
	public String TranslationCorrection(DevVarDoubleStringArray argin) throws DevFailed
		{
			get_logger().info("Entering TranslationCorrection()");
			String res="";

			// ---Add your Own code to control device here ---
	    	ParamTable newPos=new ParamTable();
	    	for(int i=0;i<argin.dvalue.length;i++){
	    		String motorName = (String)argin.svalue[i];
	    		newPos.setSingleDoubleValue(motorName,argin.dvalue[i]);
	    	}
	    	boolean coll=false;
	    	try {
				try {
						double[] newTrans=backgroundDevice.TranslationCorrection(
								new double[] {
										0,
										newPos.getFirstDoubleValue("Kappa"),
										newPos.getFirstDoubleValue("Phi"),
										newPos.getFirstDoubleValue("X"),
										newPos.getFirstDoubleValue("Y"),
										newPos.getFirstDoubleValue("Z")
								},
								new double[] {
										0,
										newPos.getFirstDoubleValue("Kappa2"),
										newPos.getFirstDoubleValue("Phi2")
								}
								);
								
						res = ""+newTrans[0]+" "+newTrans[1]+" "+newTrans[2];
				}catch (Exception e) {
				}
			} catch (Exception e) {
			}
			

			get_logger().info("Exiting TranslationCorrection()");
			return res;
		}

//	=========================================================
	/**
	 *	Execute command "AvoidShadow" on device.
	 *
	 * @param	argin	NewPositions
	 */
//	=========================================================
	/**
	 * Input parameters (in a single string):
	 * Kappa, Phi, X, Y, Z
	 * Kappa2, Phi2
	 * 
	 * Output is a string containing the new X, Y, Z
	 * 
	 * 
	 * eg:
	 * Input:
	 * Kappa=3.12, Phi=4.32, X=5.54,...
	 * Output:
	 * 6.243 5.1254
	 */
	public String AvoidShadow(String val) throws DevFailed
		{
			get_logger().info("Entering AvoidShadow()");
			String res="";

			// ---Add your Own code to control device here ---
			
				String[] vals=val.split(",");
				Vector oscillations=backgroundDevice.AvoidShadow(
						new Point3d(
								new Double(vals[0]).doubleValue(),
								new Double(vals[1]).doubleValue(),
								new Double(vals[2]).doubleValue()),
						new Double(vals[3]).doubleValue(),
						new Double(vals[4]).doubleValue(),
						new Boolean(vals[5]).booleanValue());
			for (int i=0;i<oscillations.size();i++) {
				res=res.concat(" "+((Double)(oscillations.elementAt(i))).doubleValue());
			}
			

			get_logger().info("Exiting AvoidShadow()");
			return res;
		}

//	public static Util util_init(String port,String [] serverargs,String id,String classname) {
//		//System.setProperty("OAPort","5955");
//		System.setProperty("OAPort",port);
//		//String[] serverargs = {"stac_server","-nodb","-dlist","stac/stac_server/general","-v5"};
//		Util tg = Util.init(serverargs,id+"_server");
//		//tg.add_class("stac.core.SERVER.plugins.my_tango_server");
//		tg.add_class(classname);
//		my_tango_server srv=null;
//		try {
//			tg.server_init();
//			//srv = (my_tango_server)(tg.get_device_by_name("stac/stac_server/general"));
//			srv = (my_tango_server)(tg.get_device_by_name(id+"/"+id+"_server/general"));
//		} catch (DevFailed e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return tg;
//	}
	
	public static void main(String[] argv)
	{
	
		
//		Util tg = util_init("5955",new String []{"stac_server","-nodb","-dlist","stac/stac_server/general","-v5"},"stac","stac.core.SERVER.plugins.my_tango_server");
		
	System.setProperty("OAPort","5955");
	String[] serverargs = {"stac_server","-nodb","-dlist","stac/stac_server/general","-v5"};
	Util tg = Util.init(serverargs,"stac_server");
	tg.add_class("stac.core.SERVER.plugins.my_tango_server");
		
	my_tango_server srv=null;
	try {
		tg.server_init();
		
		srv = (my_tango_server)(tg.get_device_by_name("stac/stac_server/general"));
	} catch (DevFailed e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	Stac_Session session = new Stac_Session();
	session.setAllowGUI(false);
	StacSERVER MainServer=new StacSERVER(session);
	
	srv.setInterface(MainServer);
	
	System.out.println("Ready to accept request\n");

	tg.server_run();			

	}
	
	}

	

