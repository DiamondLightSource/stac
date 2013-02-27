package stac.modules.BCM.plugins;

import stac.core.*;
import stac.gui.*;
import stac.modules.BCM.*;

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


/**
 * 
 * esrf_user: BCM plugin for spec at ESRF
 * It is using the spec user values ONLY, 
 * This plugin does NOT SET the dial/user values. Instead, it is managing
 * offsets between the spec and STAC values   
 *
 * @author sudol
 *
 */

public class Stac_BCMplugin_esrf_user extends Stac_BCMplugin_esrf_dial implements Stac_BCMplugin {
	/*
	 * motorDescriptor:
	 * eg:
     *# motorName   motorName  multiplication factor    offset
     *# (in STAC)   (in tango) (tangoValue=StacValue*f+offset)
	 * 
	 *     X         sampx           -1             0
	 *     Y         sampy            1             0
	 *     Z         phiy             1             0   
	 *    Omega      phi              1             0
	 *    Kappa      kap1             1             0
	 *    Phi        kap2             1             0
	 * 
	 * remark:
	 * mulfac assumes that calibration has been done, and the
	 * motors are aligned to the lab axes, otherwise
	 * [XYZ] <-> [spec translation] would require a 3d transformation
	 * 
	 */
    
	final int specOffset=2;

	String spec_mv() {
		return new String("mv");
	}
	String spec_set() {
		return new String("set");
	}
	String spec_getPosition() {
		return new String("getPosition");
	}
	String ProDC_MotorNameSuffix() {
		return new String("");		
	}
		
    /**
     * converts the standard plugin input (here, it is the user value) to user value
     */
	public String spec_A_value (String specMotorName, double value) {
		String A_value= new String(""+value);
		return A_value;
	}

	/**
	 * converts the dial input to the standard plugin value (here, it is user)
	 */
	public String spec_to_plugin_value (String specMotorName, String value) {
		String plugin_value= new String("user("+specMotorName+","+value+")");
		return plugin_value;
	}
	
	@Override
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-esrfUSER from S. Brockhauser";
	}
	
   
}


