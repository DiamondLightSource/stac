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
 * <pre>
 * generic tango server
 * things to modify:
 * + backgroundDevice - StacSERVER
 * + getID
 * + my_tango_serverClass: 
 *            command_factory
 *            command classes calling the command wrappers
 * + my_tango_server: 
 *            command wrapper calling the "background" device
 *
 * 
 * How to TEST the server:
 * sudol@sudol-laptop:~/work/kappa/dev/STAC/thirdparty/TangoClient$ java -cp jive-5.0.1.jar:TangORB-5.1.0.jar jive.ExecDev tango://localhost:5955/stac/stac_server/general#dbase=no  
 * Command TranslationCorrectionIntern:
 * Input:
 * [0,0,0,0,0,0,180,0,0,0,0,-1,0,0,1,0,0,0,1]["Kappa","Phi","X","Y","Z","Kappa2","Phi2","KPtx","KPty","KPtz","PPtx","PPty","PPtz","KDx","KDy","KDz","PDx","PDy","PDz"]
 * Output:
 * 07/12/2009 21:49:30
 * From host   : sudol-laptop
 * tango://localhost:5955/stac/stac_server/general#dbase=no: TranslationCorrection
 * Duration    : 7 (ms)
 * Out Argument(s):
 * 1.2246467991473532E-16 2.0 -0.0
 * 
 * </pre>
 */
public class Stac_SERVERplugin_tango extends Stac_Plugin implements Stac_SERVERplugin {
//public class Stac_SERVERplugin_tango extends DeviceImpl implements Stac_SERVERplugin {
	
	StacSERVER backgroundDevice=null;
	/**
	 * 3 char identifier for Tine server
	 * in case it is VBS
	 * the client can access:
	 * /VBS_cont/VBS_eqm_exp/VBS_dev_0
	 * @return
	 */
	public String getID() {
		//return "STAC";
		return "stac";
	}
  public Stac_SERVERplugin_tango () {
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
		init_server();		
	}

	public void activate() {
		activate_server();		
	}

	public void setInterface(StacSERVER MainServer) {
		this.backgroundDevice=MainServer;		
	}
    
	
//	=========================================================
	/**
	 *	main part for the device server class
	 */
//	=========================================================
	Util tg;
	public void init_server()
	{
		try
		{
			
    		//config the tango server
    		//System.setProperty("TANGO_HOST","localhost:5925");
    		System.setProperty("OAPort","5955");
    		
			//start tango server
    		String[] serverargs = {getID()+"_server","-nodb","-dlist",getID()+"/"+getID()+"_server/general","-v5"};
    		tg = Util.init(serverargs,getID()+"_server");

    		//if nodb we have to manually add the calss!
    		String className=this.getClass().getName();
    		className=className.substring(0,className.lastIndexOf("."));
    		tg.add_class(className+".my_tango_server");//"stac.core.SERVER.plugins.Stac_SERVERplugin_tango.my_tango_server");
			tg.server_init();
			
		}

		catch (OutOfMemoryError ex)
		{
			System.err.println("Can't allocate memory !!!!");
			System.err.println("Exiting");
		}
		catch (UserException ex)
		{
			Except.print_exception(ex);
			
			System.err.println("Received a CORBA user exception");
			System.err.println("Exiting");
		}
		catch (SystemException ex)
		{
			Except.print_exception(ex);
			
			System.err.println("Received a CORBA system exception");
			System.err.println("Exiting");
		}
		
	}
	
	public void activate_server()
	{
		try
		{
			
			my_tango_server srv = (my_tango_server)(tg.get_device_by_name(getID()+"/"+getID()+"_server/general"));
			srv.setInterface(backgroundDevice);
			
			System.out.println("Ready to accept request\n");

			tg.server_run();			
		}

		catch (OutOfMemoryError ex)
		{
			System.err.println("Can't allocate memory !!!!");
			System.err.println("Exiting");
		}
		catch (UserException ex)
		{
			Except.print_exception(ex);
			
			System.err.println("Received a CORBA user exception");
			System.err.println("Exiting");
		}
		catch (SystemException ex)
		{
			Except.print_exception(ex);
			
			System.err.println("Received a CORBA system exception");
			System.err.println("Exiting");
		}
		
		System.exit(-1);		
	}
	

}





