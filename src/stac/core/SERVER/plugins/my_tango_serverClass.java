package stac.core.SERVER.plugins;

import stac.core.SERVER.plugins.*;
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
public class my_tango_serverClass extends DeviceClass implements TangoConst
{
	/**
	 *	MicroDiffClass class instance (it is a singleton).
	 */
	private static my_tango_serverClass	_instance = null;

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
	public static my_tango_serverClass instance()
	{
		if (_instance == null)
		{
			System.err.println("my_tango_serverClass is not initialised !!!");
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
	public static my_tango_serverClass init(String class_name) throws DevFailed
	{
		if (_instance == null)
		{
			_instance = new my_tango_serverClass(class_name);
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
	protected my_tango_serverClass(String name) throws DevFailed
	{
		super(name);

		Util.out2.println("Entering my_tango_serverClass constructor");
		write_class_property();
		get_class_property();
	
		Util.out2.println("Leaving my_tango_serverClass constructor");
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
		command_list.addElement(new TranslationCorrectionIntern("TranslationCorrectionIntern",
				Tango_DEVVAR_DOUBLESTRINGARRAY, Tango_DEV_STRING,
				"NewPositions",
				"",
				DispLevel.OPERATOR));
		command_list.addElement(new TranslationCorrection("TranslationCorrection",
				Tango_DEVVAR_DOUBLESTRINGARRAY, Tango_DEV_STRING,
				"NewPositions",
				"",
				DispLevel.OPERATOR));
		command_list.addElement(new TranslationCorrectionString("TranslationCorrectionString",
				Tango_DEV_STRING, Tango_DEV_STRING,
				"NewPositions",
				"",
				DispLevel.OPERATOR));

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
			device_list.addElement(new my_tango_server(this, devlist[i]));

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
		data[0].insert("my_tango_serverClass Device Server");

		data[1] = new DbDatum("Description");
		data[1].insert("High level control");

		//	Call database and and values
		//--------------------------------------------
		get_db_class().put_property(data);
	}

//}






class TranslationCorrectionIntern extends Command implements TangoConst
{
	//===============================================================
	/**
	 *	Constructor for Command class TranslationCorrectionIntern
	 *
	 *	@param	name	command name
	 *	@param	in	argin type
	 *	@param	out	argout type
	 */
	//===============================================================
	public TranslationCorrectionIntern(String name,int in,int out)
	{
		super(name, in, out);
	}

	//===============================================================
	/**
	 *	Constructor for Command class GetCollisionInfoCmd
	 *
	 *	@param	name            command name
	 *	@param	in              argin type
	 *	@param	in_comments     argin description
	 *	@param	out             argout type
	 *	@param	out_comments    argout description
	 */
	//===============================================================
	public TranslationCorrectionIntern(String name,int in,int out, String in_comments, String out_comments)
	{
		super(name, in, out, in_comments, out_comments);
	}
	//===============================================================
	/**
	 *	Constructor for Command class GetCollisionInfoCmd
	 *
	 *	@param	name            command name
	 *	@param	in              argin type
	 *	@param	in_comments     argin description
	 *	@param	out             argout type
	 *	@param	out_comments    argout description
	 *	@param	level           The command display type OPERATOR or EXPERT
	 */
	//===============================================================
	public TranslationCorrectionIntern(String name,int in,int out, String in_comments, String out_comments, DispLevel level)
	{
		super(name, in, out, in_comments, out_comments, level);
	}
	//===============================================================
	/**
	 *	return the result of the device's command.
	 */
	//===============================================================
	public Any execute(DeviceImpl device,Any in_any) throws DevFailed
	{
		((my_tango_server)(device)).addReq();
		Util.out2.println("TranslationCorrectionIntern.execute(): arrived");
		DevVarDoubleStringArray argin = extract_DevVarDoubleStringArray(in_any);
		String res=((my_tango_server)(device)).TranslationCorrectionIntern(argin);
		((my_tango_server)(device)).delReq();
		return insert(res);
	}

	//===============================================================
	/**
	 *	Check if it is allowed to execute the command.
	 */
	//===============================================================
	public boolean is_allowed(DeviceImpl device, Any data_in)
	{
			//	End of Generated Code

			//	Re-Start of Generated Code
		return true;
	}
}

	
class TranslationCorrection extends Command implements TangoConst
{
	//===============================================================
	/**
	 *	Constructor for Command class TranslationCorrectionExt
	 *
	 *	@param	name	command name
	 *	@param	in	argin type
	 *	@param	out	argout type
	 */
	//===============================================================
	public TranslationCorrection(String name,int in,int out)
	{
		super(name, in, out);
	}

	//===============================================================
	/**
	 *	Constructor for Command class TranslationCorrectionExt
	 *
	 *	@param	name            command name
	 *	@param	in              argin type
	 *	@param	in_comments     argin description
	 *	@param	out             argout type
	 *	@param	out_comments    argout description
	 */
	//===============================================================
	public TranslationCorrection(String name,int in,int out, String in_comments, String out_comments)
	{
		super(name, in, out, in_comments, out_comments);
	}
	//===============================================================
	/**
	 *	Constructor for Command class GetCollisionInfoCmd
	 *
	 *	@param	name            command name
	 *	@param	in              argin type
	 *	@param	in_comments     argin description
	 *	@param	out             argout type
	 *	@param	out_comments    argout description
	 *	@param	level           The command display type OPERATOR or EXPERT
	 */
	//===============================================================
	public TranslationCorrection(String name,int in,int out, String in_comments, String out_comments, DispLevel level)
	{
		super(name, in, out, in_comments, out_comments, level);
	}
	//===============================================================
	/**
	 *	return the result of the device's command.
	 */
	//===============================================================
	public Any execute(DeviceImpl device,Any in_any) throws DevFailed
	{
		((my_tango_server)(device)).addReq();
		Util.out2.println("TranslationCorrection.execute(): arrived");
		DevVarDoubleStringArray argin = extract_DevVarDoubleStringArray(in_any);
		String res=((my_tango_server)(device)).TranslationCorrection(argin);
		((my_tango_server)(device)).delReq();
		return insert(res);
	}

	//===============================================================
	/**
	 *	Check if it is allowed to execute the command.
	 */
	//===============================================================
	public boolean is_allowed(DeviceImpl device, Any data_in)
	{
			//	End of Generated Code

			//	Re-Start of Generated Code
		return true;
	}
}

/**
 * string,value,string,value
 * @author sudol
 *
 */
class TranslationCorrectionString extends Command implements TangoConst
{
	//===============================================================
	/**
	 *	Constructor for Command class TranslationCorrectionExt
	 *
	 *	@param	name	command name
	 *	@param	in	argin type
	 *	@param	out	argout type
	 */
	//===============================================================
	public TranslationCorrectionString(String name,int in,int out)
	{
		super(name, in, out);
	}

	//===============================================================
	/**
	 *	Constructor for Command class TranslationCorrectionExt
	 *
	 *	@param	name            command name
	 *	@param	in              argin type
	 *	@param	in_comments     argin description
	 *	@param	out             argout type
	 *	@param	out_comments    argout description
	 */
	//===============================================================
	public TranslationCorrectionString(String name,int in,int out, String in_comments, String out_comments)
	{
		super(name, in, out, in_comments, out_comments);
	}
	//===============================================================
	/**
	 *	Constructor for Command class GetCollisionInfoCmd
	 *
	 *	@param	name            command name
	 *	@param	in              argin type
	 *	@param	in_comments     argin description
	 *	@param	out             argout type
	 *	@param	out_comments    argout description
	 *	@param	level           The command display type OPERATOR or EXPERT
	 */
	//===============================================================
	public TranslationCorrectionString(String name,int in,int out, String in_comments, String out_comments, DispLevel level)
	{
		super(name, in, out, in_comments, out_comments, level);
	}
	//===============================================================
	/**
	 *	return the result of the device's command.
	 */
	//===============================================================
	public Any execute(DeviceImpl device,Any in_any) throws DevFailed
	{
		((my_tango_server)(device)).addReq();
		Util.out2.println("TranslationCorrectionString.execute(): arrived");
		String arginorig=extract_DevString(in_any);
		String[] args=arginorig.split(" ");
		double [] dv= new double[(int)(args.length/2)];
		String [] sv= new String[(int)(args.length/2)];
		for(int i=0;i<(int)(args.length/2);i++) {
			sv[i]=new String(args[i*2]);
			dv[i]=new Double(args[i*2+1]).doubleValue();
		}
		DevVarDoubleStringArray argin = new DevVarDoubleStringArray();
		argin.dvalue=dv;
		argin.svalue=sv;		
		String res=((my_tango_server)(device)).TranslationCorrection(argin);
		((my_tango_server)(device)).delReq();
		return insert(res);
	}

	//===============================================================
	/**
	 *	Check if it is allowed to execute the command.
	 */
	//===============================================================
	public boolean is_allowed(DeviceImpl device, Any data_in)
	{
			//	End of Generated Code

			//	Re-Start of Generated Code
		return true;
	}
}

}
