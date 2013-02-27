//+======================================================================
// $Source:  $
//
// Project:   	Tango Device Server
//
// Description:	java source code for the MicroDiff class .
//              This class is a singleton class and implements everything
//              which exists only once for all the  MicroDiff object
//              It inherits from the DeviceClass class.
//
// $Author:  $
//
// $Revision:  $
//
// $Log:  $
//
// copyleft :    European Synchrotron Radiation Facility
//               BP 220, Grenoble 38043
//               FRANCE
//
//-======================================================================
//
//  		This file is generated by POGO
//	(Program Obviously used to Generate tango Object)
//
//         (c) - Software Engineering Group - ESRF
//=============================================================================

package stac.vbcm.tango;

import java.util.*;
import org.omg.CORBA.*;
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;

public class vbcm_serverClass extends DeviceClass implements TangoConst
{
	/**
	 *	MicroDiffClass class instance (it is a singleton).
	 */
	private static vbcm_serverClass	_instance = null;

	/**
	 *	Class properties array.
	 */
	private DbDatum[]	cl_prop = null;

	//--------- Start of properties data members ----------


//--------- End of properties data members ----------


//===================================================================			
//
// method : 		instance()
// 
// description : 	static method to retrieve the MicroDiffClass object 
//					once it has been initialised
//
//===================================================================			
	public static vbcm_serverClass instance()
	{
		if (_instance == null)
		{
			System.err.println("MicroDiffClass is not initialised !!!");
			System.err.println("Exiting");
			System.exit(-1);
		}
		return _instance;
	}

//===================================================================			
//
// method : 		Init()
// 
// description : 	static method to create/retrieve the MicroDiffClass
//					object. This method is the only one which enables a 
//					user to create the object
//
// in :			- class_name : The class name
//
//===================================================================			
	public static vbcm_serverClass init(String class_name) throws DevFailed
	{
		if (_instance == null)
		{
			_instance = new vbcm_serverClass(class_name);
		}
		return _instance;
	}
	
//===================================================================			
//
// method : 		MicroDiffClass()
// 
// description : 	constructor for the MicroDiffClass class
//
// argument : in : 	- name : The class name
//
//===================================================================			
	protected vbcm_serverClass(String name) throws DevFailed
	{
		super(name);

		Util.out2.println("Entering MicroDiffClass constructor");
		write_class_property();
		get_class_property();
	
		Util.out2.println("Leaving MicroDiffClass constructor");
	}
	
//===================================================================			
//
// method : 		command_factory()
// 
// description : 	Create the command object(s) and store them in the
//					command list
//===================================================================			
	public void command_factory()
	{
		command_list.addElement(new ResetCmd("Reset",
			Tango_DEV_VOID, Tango_DEV_VOID,
			"",
			"",
			DispLevel.OPERATOR));
		command_list.addElement(new SyncMoveMotorsCmd("SyncMoveMotors",
				Tango_DEVVAR_DOUBLESTRINGARRAY, Tango_DEV_VOID,
				"NewPositions",
				"",
				DispLevel.OPERATOR));
		command_list.addElement(new CenterNeedleCmd("CenterNeedle",
				Tango_DEV_VOID, Tango_DEV_BOOLEAN,
				"",
				"",
				DispLevel.OPERATOR));
		command_list.addElement(new GetCollisionInfoCmd("GetCollisionInfo",
				Tango_DEVVAR_DOUBLESTRINGARRAY, Tango_DEV_BOOLEAN,
				"NewPositions",
				"",
				DispLevel.OPERATOR));
//		command_list.addElement(new StateCmd("State",
//				Tango_DEV_VOID, Tango_DEV_STRING,
//				"",
//				"",
//				DispLevel.OPERATOR));

		//	add polling if any
		for (int i=0 ; i<command_list.size(); i++)
		{
			Command	cmd = (Command)command_list.elementAt(i);
		}
	}


//===================================================================			
//
// method : 		device_factory()
// 
// description : 	Create the device object(s) and store them in the 
//					device list
//
// argument : in : 	String[] devlist : The device name list
//
//===================================================================			
	public void device_factory(String[] devlist) throws DevFailed
	{
	
		for (int i=0 ; i<devlist.length ; i++)
		{
			Util.out4.println("Device name : " + devlist[i]);
						
			// Create device and add it into the device list
			//----------------------------------------------
			device_list.addElement(new vbcm_server(this, devlist[i]));

			// Export device to the outside world
			//----------------------------------------------
			if (Util._UseDb == true)
				export_device(((DeviceImpl)(device_list.elementAt(i))));
			else
				export_device(((DeviceImpl)(device_list.elementAt(i))), devlist[i]);
		}
	}

//=============================================================================
//
//	Method:	attribute_factory(Vector att_list)
//
//=============================================================================
	public void attribute_factory(Vector att_list) throws DevFailed
	{
		//read and set up from vbcm
		String [] list = vbcm_server.vbcm.getMotorList();
		
		for (int i=0;i<list.length;i++) {
			Attr	att = 
			new Attr(list[i], Tango_DEV_DOUBLE, AttrWriteType.READ_WRITE);
			att_list.addElement(att);			
		}

		//	Attribute : CenterNeedleRadius
		Attr	center_needle_radius = 
			new Attr("CenterNeedleRadius", Tango_DEV_DOUBLE, AttrWriteType.READ_WRITE);
		att_list.addElement(center_needle_radius);
		
//		//	Attribute : PhiTableYAxisPosition
//		Attr	phi_table_yaxis_position = 
//			new Attr("Z", Tango_DEV_DOUBLE, AttrWriteType.READ_WRITE);
//		att_list.addElement(phi_table_yaxis_position);
//
//		//	Attribute : KappaPosition
//		Attr	kappa_position = 
//			new Attr("Kappa", Tango_DEV_DOUBLE, AttrWriteType.READ_WRITE);
//		att_list.addElement(kappa_position);
//
//		//	Attribute : OmegaPosition
//		Attr	omega_position = 
//			new Attr("Omega", Tango_DEV_DOUBLE, AttrWriteType.READ_WRITE);
//		att_list.addElement(omega_position);
//
//		//	Attribute : CentringTableXAxisPosition
//		Attr	centring_table_xaxis_position = 
//			new Attr("X", Tango_DEV_DOUBLE, AttrWriteType.READ_WRITE);
//		att_list.addElement(centring_table_xaxis_position);
//
//		//	Attribute : CentringTableYAxisPosition
//		Attr	centring_table_yaxis_position = 
//			new Attr("Y", Tango_DEV_DOUBLE, AttrWriteType.READ_WRITE);
//		att_list.addElement(centring_table_yaxis_position);
//
//		//	Attribute : KappaPhiPosition
//		Attr	kappa_phi_position = 
//			new Attr("Phi", Tango_DEV_DOUBLE, AttrWriteType.READ_WRITE);
//		att_list.addElement(kappa_phi_position);

	}
//===================================================================
/**
 *	Get the class property for specified name.
 *
 *	@param name The property name.
 */
//===================================================================
	public DbDatum get_class_property(String name)
	{
		for (int i=0 ; i<cl_prop.length ; i++)
			if (cl_prop[i].name.equals(name))
				return cl_prop[i];
		//	if not found, return  an empty DbDatum
		return new DbDatum(name);
	}

//===================================================================
/**
 *	Read the class properties from database.
 */
//===================================================================			
	public void get_class_property() throws DevFailed
	{
		//	Initialize your default values here.
		//------------------------------------------


		//	Read class properties from database.(Automatic code generation)
		//-------------------------------------------------------------
		if (Util._UseDb==false)
			return;
		String[]	propnames = {
			};

		//	Call database and extract values
		//--------------------------------------------
		cl_prop = get_db_class().get_property(propnames);
		int	i = -1;

		//	End of Automatic code generation
		//-------------------------------------------------------------

	}

//===================================================================
/**
 *	Set class description as property in database
 */
//===================================================================			
	private void write_class_property() throws DevFailed
	{	
		//	First time, check if database used
		//--------------------------------------------
		if (Util._UseDb == false)
			return;

		//	Prepeare DbDatum
		//--------------------------------------------
		DbDatum[]	data = new DbDatum[2];
		data[0] = new DbDatum("ProjectTitle");
		data[0].insert("MicroDiff Device Server");

		data[1] = new DbDatum("Description");
		data[1].insert("High level control");

		//	Call database and and values
		//--------------------------------------------
		get_db_class().put_property(data);
	}

}
