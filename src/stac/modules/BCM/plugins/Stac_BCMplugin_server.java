package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.gui.*;
import stac.modules.BCM.*;

import java.net.*;
import java.util.*;


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

import com.sun.j3d.utils.behaviors.vp.WandViewBehavior.ResetViewListener;

import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;



/**
 * 
 * esrf: BCM plugin for spec at ESRF
 * It is using the spec user values ONLY, but gives a base class for
 * all further spec plugins
 * NOTE: that this plugin is SETting the user values instead of managing
 * offsets between the spec and STAC values   
 *
 * @author sudol
 *
 */

public class Stac_BCMplugin_server extends Stac_BCMplugin_base implements Stac_BCMplugin {
	
    
	private boolean fileupdate () {
		return false;
	}

	@Override
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-server from S. Brockhauser";
	}

    
}


