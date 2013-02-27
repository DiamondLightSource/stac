package stac.modules.Strategy;

import org.sudol.sun.util.*;
import stac.core.*;
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


public interface Stac_StrategyPlugin {
    public String CalculateStrategy(Stac_Session session,int msgId,String indir,String outdir,Vector3d currDatum,Vector DesiredOrientations,String options,StacBCM bcm,Util utils);
    //public String CalculatePreferedOrientation(Stac_Session session,int msgId,String indir,String outdir,Vector3d currDatum,Vector DesiredOrientations,StacBCM bcm,Util utils);
	public String getCreditString();
}


