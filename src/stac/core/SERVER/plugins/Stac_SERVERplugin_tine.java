package stac.core.SERVER.plugins;

import org.sudol.sun.util.*;

import de.desy.tine.dataUtils.TDataType;
import de.desy.tine.definitions.TAccess;
import de.desy.tine.definitions.TFormat;
import de.desy.tine.server.devices.TDevice;
import de.desy.tine.server.devices.TDeviceList;
import de.desy.tine.server.equipment.TEquipmentBackgroundTask;
import de.desy.tine.server.equipment.TEquipmentModule;
import de.desy.tine.server.equipment.TEquipmentModuleFactory;
import de.desy.tine.server.properties.TExportProperty;
import de.desy.tine.server.properties.TPropertyHandler;
import de.desy.tine.types.NAME48I;

import stac.core.*;
import stac.core.SERVER.StacSERVER;
import stac.core.SERVER.Stac_SERVERplugin;
import stac.modules.BCM.*;
import stac.modules.Strategy.*;
import stac.vbcm.vBCM;


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
 * generic tine server
 * things to modify:
 * + backgroundDevice - StacSERVER
 * + getID
 * + properties
 */
public class Stac_SERVERplugin_tine extends Stac_Plugin implements Stac_SERVERplugin {
	
	StacSERVER backgroundDevice=null;
	/**
	 * 3 char identifier for Tine server
	 * in case it is VBS
	 * the client can access:
	 * /VBS_cont/VBS_eqm_exp/VBS_dev_0
	 * @return
	 */
	public String getID() {
		return "STC";
	}
    public Stac_SERVERplugin_tine () {
    }

	@Override
	protected void closePlugin() {
	}

	@Override
	public String getCreditString() {
		return null;
	}

	@Override
	public void initPlugin() {
		
	}

	public void activate() {
		init_server(backgroundDevice);		
		activate_server();
	}

	public void setInterface(StacSERVER MainServer) {
		this.backgroundDevice=MainServer;		
	}
    
    

	class mytine_device extends TDevice
	{
		
		Object backgroundDevice = null;
		
		double CenterNeedleRadius = -100; //unsuccessfull centering
		
		String STATUSOK="STANDBY";
		String STATUSMOVE="MOVING";
		
		int requestCt=0;

		public Object getBackgroundDevice() {
			return backgroundDevice;
		}
		
		public void setBackgroundDevice(Object backgroundDevice) {
			this.backgroundDevice=backgroundDevice;
		}
		
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

		mytine_device(int newNumber)
		{
			super(newNumber);
			init_device();
		}
		mytine_device(String name,int newNumber)
		{
			super(name,newNumber);
			init_device();
		}
		
		
		
		//--------- Start of attributes data members ----------

//		protected double[]	attr_Omega_read = new double[1];
//		protected double	attr_Omega_write;
//		protected double[]	attr_Kappa_read = new double[1];
//		protected double	attr_Kappa_write;
//		protected double[]	attr_Phi_read = new double[1];
//		protected double	attr_Phi_write;
//		protected double[]	attr_X_read = new double[1];
//		protected double	attr_X_write;
//		protected double[]	attr_Y_read = new double[1];
//		protected double	attr_Y_write;
//		protected double[]	attr_Z_read = new double[1];
//		protected double	attr_Z_write;

//	--------- End of attributes data members ----------


		//--------- Start of properties data members ----------

		//--------- End of properties data members ----------


		//	Add your own data members here
		//--------------------------------------


		//String [] mlist;//=vbcm.getMotorList();

		public void init_device() 
		{
			//System.out.println("MicroDiff() create " + device_name);

			//	Initialise variables to default values
			//-------------------------------------------
			//set_state(DevState.ON);
			
			//vbcm
			//String [] mlist=vbcm.getMotorList();
			
		}



	}


	class mytine_EquipmentModule extends TEquipmentModule {
		boolean wrapMultiChannelArrays = true;
		TDeviceList myDeviceSet;
		// Wrapper which casts to sineDevice
		private mytine_device findDevice(String deviceName)
		{
			if (myDeviceSet == null) return null;
			return (mytine_device) myDeviceSet.getDevice(deviceName);
		}
		private mytine_device findMyDevice()
		{
			if (myDeviceSet == null) return null;
			return (mytine_device) myDeviceSet.getDeviceList()[0];
		}
		public mytine_EquipmentModule(String localName, mytine_device[] devices)
		{
			super(localName);
			registerDevices(devices);
			myDeviceSet = super.getDeviceList();
			registerProperties();
		}
		public mytine_EquipmentModule(mytine_device[] devices)
		{
			super();
			registerDevices(devices);
			myDeviceSet = super.getDeviceList();
			registerProperties();
		}
		
		abstract class my_TPropertyHandler extends TPropertyHandler {
			String propName="";
			
			my_TPropertyHandler(String propName) {
				super();
				this.propName=propName;
			}
		}
		
		private void registerProperties()
		{ // all property information (except the handlers) from exports.csv file
			//getExportInformationFromFile();
			//setExportName("VBS");
			//setLocalName("VBS");
			// attach the property handlers ...
			mytine_device device = findMyDevice();
			//vbcm=device.getVBCM();
			//if (device == null) return TErrorList.device_not_connected;
			//String [] mlist=vbcm.getMotorList();
			TPropertyHandler handler;

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
			attachPropertyHandler("TranslationCorrectionIntern",handler=new TPropertyHandler()
			{
				protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
				{
					boolean coll=false;
					try {
				    	ParamTable newPos=new ParamTable();
						//String [] val1=(String [])din.getDataObject();
			    		String val=new String((char [])din.getDataObject());
						//for (int j=0;j<val1.length;j++) {
				    		//String val=val1[j];
							String[] vals=val.split(",");
							for(int i=0;i<vals.length;i++) {
								String[] val2=vals[i].split("=");
								newPos.setSingleDoubleValue(val2[0],new Double(val2[1]).doubleValue());
							}
						//}
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
									
						dout.putData(new String(""+newTrans[0]+""+newTrans[1]+" "+newTrans[2]));
					}catch (Exception e) {
						return 100;
					}
					return 0;
				}
			});
			registerProperty(new TExportProperty("TranslationCorrectionIntern",1,TFormat.CF_TEXT,1,TFormat.CF_TEXT), handler);
			
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
			attachPropertyHandler("TranslationCorrection",handler=new TPropertyHandler()
			{
				protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
				{
					boolean coll=false;
					try {
				    	ParamTable newPos=new ParamTable();
						//String [] val1=(String [])din.getDataObject();
			    		String val=new String((char [])din.getDataObject());
						//for (int j=0;j<val1.length;j++) {
				    		//String val=val1[j];
							String[] vals=val.split(",");
							for(int i=0;i<vals.length;i++) {
								String[] val2=vals[i].split("=");
								newPos.setSingleDoubleValue(val2[0],new Double(val2[1]).doubleValue());
							}
						//}
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
									
						dout.putData(new String(""+newTrans[0]+""+newTrans[1]+" "+newTrans[2]));
					}catch (Exception e) {
						return 100;
					}
					return 0;
				}
			});
			registerProperty(new TExportProperty("TranslationCorrection",1,TFormat.CF_TEXT,1,TFormat.CF_TEXT), handler);
			
			/**
			 * Input parameters (in a single string):
			 * datum (omega, kappa, phi), osc start, osc end, omegaosc
			 * 
			 * Output is a string containing the new osc_start-osc_end pairs
			 * 
			 * 
			 * eg:
			 * Input:
			 * 110,10,20,0,120,true
			 * Output:
			 * 0 20 200 320
			 */
			attachPropertyHandler("AvoidShadow",handler=new TPropertyHandler()
			{
				protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
				{
					boolean coll=false;
					try {
			    		String val=new String((char [])din.getDataObject());
							String[] vals=val.split(",");
							Vector oscillations=backgroundDevice.AvoidShadow(
									new Point3d(
											new Double(vals[0]).doubleValue(),
											new Double(vals[1]).doubleValue(),
											new Double(vals[2]).doubleValue()),
									new Double(vals[2]).doubleValue(),
									new Double(vals[2]).doubleValue(),
									new Boolean(vals[2]).booleanValue());
						String res="";
						for (int i=0;i<oscillations.size();i++) {
							res=res.concat(" "+((Double)(oscillations.elementAt(i))).doubleValue());
						}
									
						dout.putData(res);
					}catch (Exception e) {
						return 100;
					}
					return 0;
				}
			});
			registerProperty(new TExportProperty("AvoidShadow",1,TFormat.CF_TEXT,1,TFormat.CF_TEXT), handler);
			
			
		}
	}






	  private static mytine_EquipmentModule myEqpModule;
	  private static TEquipmentBackgroundTask sineBkgFcn=null;
	  private TEquipmentModuleFactory thisEqmFactory;
	  private LinkedList my_DeviceSet = new LinkedList();
	  
	  public void init_server(Object backgroundDevice)
	  {
	    initializeDevices(backgroundDevice); // should create a device list (eqm not yet known !)
	    initializeDeviceServer(); // will create an eqm, passing a device list
	   // optional: spit these out just to confirm they have been registered ...
//	    vbsEqpModule.dumpProperties(); 
//	    vbsEqpModule.dumpDevices();
	  }
	  private void initializeDevices(Object backgroundDevice)
	  {
		  mytine_device vdev;
		  my_DeviceSet.add(vdev=new mytine_device(getID()+"_dev_"+0,0));
		  //vbcm_DeviceSet.add(vdev=new tine_device(0));
		  vdev.setBackgroundDevice(backgroundDevice);
	  }
	  private void initializeDeviceServer()
	  {
	    // Create Equipment Module(s) (this example has only one)
	    myEqpModule = new mytine_EquipmentModule(getID()+"_eqm",(mytine_device[])my_DeviceSet.toArray(new mytine_device[0]));
	    myEqpModule.setExportName(getID()+"_eqm_exp");
	    myEqpModule.setContext(getID()+"_cont");
	    myEqpModule.setLocalName(getID()+"Loc");
	  }
	  public void activate_server()
	  {
//	      if (mTineHome!=null)
//	      {
//	          if (IOTools.existPath(mTineHome 
//	+java.io.File.separator+"cshosts.csv"))
	              System.setProperty("tine.home","./");
//	      }
//	      if (mLogHome!=null)
//	          System.setProperty("log.home",mLogHome);
//	      if (mHistoryHome!=null)
//	          System.setProperty("history.home",mHistoryHome);


		  
		  
	    // record a reference to the equipment module factory (all modules will use this one)
	    thisEqmFactory = myEqpModule.getTEqmFactory();
	    // add the background task(s) to the factory
	    //if (sineBkgFcn != null) 
	    //  thisEqmFactory.addEquipmentBackgroundTask(sineBkgFcn);
	    // initialize the FEC (starts all services, reads configuration databases, etc.)
	    //thisEqmFactory.systemInit();   // initialize the factory
	    // alternative: thisEqmFactory.systemInit(myFecName, myTinePort) hardcodes
	    // the FECNAME instead of using fecid.csv
	    //thisEqmFactory.systemInit("/TEST/VBS/VBS_0", 5926);
	    TEquipmentModuleFactory.getInstance().setRespondToServiceRequests(true);
	    thisEqmFactory.systemInit(getID()+"_fec", 10);
	    //thisEqmFactory.systemInit();
	    
		System.out.println("Ready to accept request\n");

	    thisEqmFactory.systemWait(-1); // wait here forever ... (or loop here and check command line?)
	  }
	
}


