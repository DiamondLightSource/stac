

package stac.vbcm.tine;


import stac.core.*;
import stac.vbcm.*;

import java.io.File;
import java.util.*;

import de.desy.tine.server.devices.*;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoDs.DeviceClass;

import de.desy.tine.dataUtils.*;
import de.desy.tine.definitions.*;
import de.desy.tine.server.devices.*;
import de.desy.tine.server.equipment.*;
import de.desy.tine.server.properties.*;
import de.desy.tine.types.*;



import java.util.LinkedList;
import de.desy.tine.server.equipment.*;


class vbcm_device extends TDevice
{
	
	static vBCM vbcm = null;
	
	double CenterNeedleRadius = -100; //unsuccessfull centering
	
	String STATUSOK="STANDBY";
	String STATUSMOVE="MOVING";
	
	int requestCt=0;

	public vBCM getVBCM() {
		return vbcm;
	}
	
	public void setVBCM(vBCM vbcm) {
		this.vbcm=vbcm;
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

	vbcm_device(int newNumber)
	{
		super(newNumber);
		init_device();
	}
	vbcm_device(String name,int newNumber)
	{
		super(name,newNumber);
		init_device();
	}
	
	
	
	//--------- Start of attributes data members ----------

//	protected double[]	attr_Omega_read = new double[1];
//	protected double	attr_Omega_write;
//	protected double[]	attr_Kappa_read = new double[1];
//	protected double	attr_Kappa_write;
//	protected double[]	attr_Phi_read = new double[1];
//	protected double	attr_Phi_write;
//	protected double[]	attr_X_read = new double[1];
//	protected double	attr_X_write;
//	protected double[]	attr_Y_read = new double[1];
//	protected double	attr_Y_write;
//	protected double[]	attr_Z_read = new double[1];
//	protected double	attr_Z_write;

//--------- End of attributes data members ----------


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


class vbcm_EquipmentModule extends TEquipmentModule {
	boolean wrapMultiChannelArrays = true;
	TDeviceList myDeviceSet;
	// Wrapper which casts to sineDevice
	private vbcm_device findDevice(String deviceName)
	{
		if (myDeviceSet == null) return null;
		return (vbcm_device) myDeviceSet.getDevice(deviceName);
	}
	private vbcm_device findMyDevice()
	{
		if (myDeviceSet == null) return null;
		return (vbcm_device) myDeviceSet.getDeviceList()[0];
	}
	public vbcm_EquipmentModule(String localName, vbcm_device[] devices)
	{
		super(localName);
		registerDevices(devices);
		myDeviceSet = super.getDeviceList();
		registerProperties();
	}
	public vbcm_EquipmentModule(vbcm_device[] devices)
	{
		super();
		registerDevices(devices);
		myDeviceSet = super.getDeviceList();
		registerProperties();
	}
	
	vBCM vbcm;
	
	abstract class VBS_TPropertyHandler extends TPropertyHandler {
		String propName="";
		
		VBS_TPropertyHandler(String propName) {
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
		vbcm_device device = findMyDevice();
		vbcm=device.getVBCM();
		//if (device == null) return TErrorList.device_not_connected;
		String [] mlist=vbcm.getMotorList();
		TPropertyHandler handler;
		for (int i=0;i<mlist.length;i++) {
			attachPropertyHandler(mlist[i],handler=new VBS_TPropertyHandler(mlist[i])
			{
				protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
				{
					if (devAccess.isWrite()) {
						try {
					    	ParamTable newPos=new ParamTable();
							newPos.setSingleDoubleValue(propName,((double [])din.getDataObject())[0]);
							vbcm.MoveMotors(newPos);
						} catch (Exception e) {
							return 100;
						}
					} else {
						try {
							double pos=vbcm.getMotorPos(propName);
							return dout.putData(pos);
						} catch (Exception e) {
							return 100;
						}
					}
					return 0;
				}
			});
			registerProperty(new TExportProperty(mlist[i],1,TFormat.CF_DOUBLE,1,TFormat.CF_DOUBLE), handler);
		}

		attachPropertyHandler("GetMotorLimits",handler=new TPropertyHandler()
		{
			protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
			{
				try {
					String mname=new String((char [])din.getDataObject());
					Vector lim=vbcm.getMotorLimits(mname);
					String resp="";
					double off=0;
					boolean rot=vbcm.isMotorRotational(mname);
					if (	(!rot &&
								(
									((Double)lim.elementAt(0)).doubleValue()!=((Double)lim.elementAt(1))
								)
							) || 
							(rot && 
								(	
									((Double)lim.elementAt(0)).doubleValue()!=0 ||
									((Double)lim.elementAt(1)).doubleValue()!=360
								)	
							)
						)
						off=1e-4;
					resp=""+new Double(((Double)lim.elementAt(0))-off).toString()+","+new Double(((Double)lim.elementAt(1))+off).toString();
					//dout=new TDataType (resp);
					//dout.putData(resp.toCharArray());
					dout.putData(resp);
				} catch (Exception e) {
					return 100;
				}
				return 0;
			}
		});
		registerProperty(new TExportProperty("GetMotorLimits",200,TFormat.CF_TEXT,1,TFormat.CF_TEXT), handler);
		
		attachPropertyHandler("SyncMoveMotors",handler=new TPropertyHandler()
		{
			protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
			{
				try {
			    	ParamTable newPos=new ParamTable();
					//String [] val1=(String [])din.getDataObject();
					String val= new String((char [])din.getDataObject());
					//for (int j=0;j<val1.length;j++) {
					//	String val=val1[j];
						String[] vals=val.split(",");
						for(int i=0;i<vals.length;i++) {
							String[] val2=vals[i].split("=");
							newPos.setSingleDoubleValue(val2[0],new Double(val2[1]).doubleValue());
						}
					//}
					vbcm.MoveMotors(newPos);
				} catch (Exception e) {
					return 100;
				}
				return 0;
			}
		});
		registerProperty(new TExportProperty("SyncMoveMotors",1,TFormat.CF_TEXT,1,TFormat.CF_TEXT), handler);
		
		attachPropertyHandler("SimpleLoopType",handler=new TPropertyHandler()
		{
			protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
			{
				return 0;//Not Used Now
			}
		});
		registerProperty(new TExportProperty("SimpleLoopType",1,TFormat.CF_STRING,1,TFormat.CF_STRING), handler);
		
		attachPropertyHandler("CenterNeedle",handler=new TPropertyHandler()
		{
			protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
			{
				try {
					vbcm.CenterNeedle();
				}catch (Exception e) {
					return 100;
				}
				return 0;
			}
		});
		registerProperty(new TExportProperty("CenterNeedle",1,TFormat.CF_STRING,1,TFormat.CF_STRING), handler);

		attachPropertyHandler("VBS_State",handler=new TPropertyHandler()
		{
			protected int call(String devName, TDataType dout, TDataType din, TAccess devAccess)
			{
				/**
				 * 1 -OK
				 * 2 -MOVE
				 */
				int STATUS;
				try {
					if (vbcm.numOfActiveMotions()>0) {
						STATUS=2;
					} else {
						STATUS=1;
					}
		        	NAME48I[] refdat = {new NAME48I()};
		        	refdat[0].setValues("State",STATUS);
					dout.putData(refdat);
				} catch (Exception e) {
					return 100;
				}
				return 0;
			}
		});
		registerProperty(new TExportProperty("VBS_State",200,TFormat.CF_NAME48I,1,TFormat.CF_BYTE), handler);
		//registerProperty(new TExportProperty("VBS_State",1,TFormat.CF_BYTE,1,TFormat.CF_BYTE), handler);
		
		attachPropertyHandler("CheckPositionSafety",handler=new TPropertyHandler()
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
		    		vbcm.checkCollision(true);
					vbcm.MoveMotors(newPos);
					Thread sleep= new Thread();
					sleep.start();
					while (vbcm.numOfActiveMotions()!=0) {
						sleep.sleep(200);				
					}
					sleep.stop();
					coll=vbcm.getCollisionInfo();
					vbcm.checkCollision(false);
					if (coll)
						dout.putData(new String("collision"));
					else
						dout.putData(new String("safe"));
				}catch (Exception e) {
					return 100;
				}
				return 0;
			}
		});
		registerProperty(new TExportProperty("CheckPositionSafety",1,TFormat.CF_TEXT,1,TFormat.CF_TEXT), handler);
		
		
	}
}






public class vbcm_server
{
  private static vbcm_EquipmentModule vbsEqpModule;
  private static TEquipmentBackgroundTask sineBkgFcn=null;
  private TEquipmentModuleFactory thisEqmFactory;
  private LinkedList vbcm_DeviceSet = new LinkedList();
  
  public vbcm_server(vBCM vbcm)
  {
    initializeDevices(vbcm); // should create a device list (eqm not yet known !)
    initializeDeviceServer(); // will create an eqm, passing a device list
   // optional: spit these out just to confirm they have been registered ...
//    vbsEqpModule.dumpProperties(); 
//    vbsEqpModule.dumpDevices();
  }
  private void initializeDevices(vBCM vbcm)
  {
	  vbcm_device vdev;
	  vbcm_DeviceSet.add(vdev=new vbcm_device("VBS_dev_"+0,0));
	  //vbcm_DeviceSet.add(vdev=new vbcm_device(0));
	  vdev.setVBCM(vbcm);
  }
  private void initializeDeviceServer()
  {
    // Create Equipment Module(s) (this example has only one)
    vbsEqpModule = new vbcm_EquipmentModule("VBS_eqm",(vbcm_device[])vbcm_DeviceSet.toArray(new vbcm_device[0]));
    vbsEqpModule.setExportName("VBS_eqm_exp");
    vbsEqpModule.setContext("VBS_cont");
    vbsEqpModule.setLocalName("VeqLoc");
  }
  public void activate()
  {
//      if (mTineHome!=null)
//      {
//          if (IOTools.existPath(mTineHome 
//+java.io.File.separator+"cshosts.csv"))
              System.setProperty("tine.home","./");
//      }
//      if (mLogHome!=null)
//          System.setProperty("log.home",mLogHome);
//      if (mHistoryHome!=null)
//          System.setProperty("history.home",mHistoryHome);


	  
	  
    // record a reference to the equipment module factory (all modules will use this one)
    thisEqmFactory = vbsEqpModule.getTEqmFactory();
    // add the background task(s) to the factory
    //if (sineBkgFcn != null) 
    //  thisEqmFactory.addEquipmentBackgroundTask(sineBkgFcn);
    // initialize the FEC (starts all services, reads configuration databases, etc.)
    //thisEqmFactory.systemInit();   // initialize the factory
    // alternative: thisEqmFactory.systemInit(myFecName, myTinePort) hardcodes
    // the FECNAME instead of using fecid.csv
    //thisEqmFactory.systemInit("/TEST/VBS/VBS_0", 5926);
    TEquipmentModuleFactory.getInstance().setRespondToServiceRequests(true);
    thisEqmFactory.systemInit("VBS_fec", 10);
    //thisEqmFactory.systemInit();
    
	System.out.println("Ready to accept request\n");

    thisEqmFactory.systemWait(-1); // wait here forever ... (or loop here and check command line?)
  }
	static vBCM vbcm = null;

	public static void main(String[] argv)
	{
		try
		{
			//start the vBCM	vBCM vbcm = null;

			if (argv.length == 0) {
				Stac_Out.println("No file to display");
				Stac_Out.println("Usage: java vBCM [-server] pathname | URL");
				System.exit(0);
			}

			boolean serverLoad = argv[0].equals("-server");

			if(((argv.length == 1) && serverLoad) ||
					((argv.length == 2) && !serverLoad)) {

				Stac_Out.println("No file to display");
				Stac_Out.println("Usage: java vBCM [-server] pathname | URL");
				System.exit(0);
			}

			String filename = (argv.length == 1) ? argv[0] : argv[1];

			File cfg= new File(System.getProperty("BCMDEF"));
			String vBCMconf=cfg.getParent()+File.separator+filename;
			vbcm = new vBCM(""+vBCMconf,!serverLoad);
			vbcm.activateVBCM(vbcm);


			//config the tine server

			//start tine server
			// create the device server; export name read from exports.csv file 
			vbcm_server theServer = new vbcm_server(vbcm);
			// optional: sineEqpModule.setExportName(myExportName) hardcodes 
			// the exported device server name instead of reading from exports.csv

			// Now start serving ...
			theServer.activate();

		}

		catch (Exception ex)
		{
			int a=1;
			ex.printStackTrace();
		}

		System.exit(-1);		





	}
}
