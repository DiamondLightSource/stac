package stac.gui;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.modules.BCM.*;
import stac.modules.Alignment.*;
import stac.modules.Strategy.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
//import javax.swing.border.*;
import java.io.*;

import Jama.*;
//import java2d.demos.Colors.Rotator3D.Objects3D.Matrix3D;
//import javax.swing.table.TableColumn;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.vecmath.*;

import sun.awt.VerticalBagLayout;

import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.lang.management.ThreadInfo;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;




abstract class STAC_GUI_AbstractTableModel extends AbstractTableModel implements STAC_GUI_Widget {
	
	public String [] ColumnNames=null;
	  String [] actColumnNames=null;
	  String [] confID=null;
	  public Vector[] Data =null;
		JTable table=null;
		
		  public STAC_GUI_AbstractTableModel(Stac_Configuration config) {
			  this();
			  this.setConfig(config);
		  }

		
		  public void setTable(JTable t){
			    table=t;
			    //this.addTableModelListener(this);
			  }
		  abstract void initColumns();
		  abstract void initfill();
		  public STAC_GUI_AbstractTableModel() {
			  initColumns();
			  Data = new Vector[ColumnNames.length];
		    for (int i = 0; i < ColumnNames.length; i++)
		      Data[i] = new Vector(0);
		    initfill();
		  }
	  
	  public String getActColumnName(int col) {
		  if (actColumnNames==null)
		    return ColumnNames[col];
		  else
			return actColumnNames[col];
		  }

	  public int getColumnCount() {
		    return ColumnNames.length;
		  }

		  public int getRowCount() {
		    //return alignData.length;
		    return Data[0].size();
		  }

		  public String getColumnName(int col) {
			    return ColumnNames[col];
			  }

		  public int getColId(String name) {
		    for (int i = 0; i < ColumnNames.length; i++)
		        if (ColumnNames[i].equalsIgnoreCase(name))
		        	return i;
		  	return 0;
		  }

		  public Object getValueAt(int row, int col) {
		    return Data[col].elementAt(row);
		  }

		  public Class getColumnClass(int c) {
		    return getValueAt(0, c).getClass();
		  }
	  
	  
	  public void setConfig(Stac_Configuration config) {
			if (config==null)
				return;
	 		  if (confID==null) {
			  //get instance specific IDs
			  confID=new String[this.ColumnNames.length+1];
			  for (int i=0;i<this.ColumnNames.length;i++){
				  confID[i]=new String(this.ColumnNames[i]);
				  confID[i]=confID[i].replaceAll(" ", "_");
			  }
			  confID[this.ColumnNames.length]=confID[0].concat("_TOOLTIP");
		  }

		  //read actual config foreach ID

		  String newtext=null;
		  actColumnNames=new String[this.ColumnNames.length];
		  //column text
		  for (int i=0;i<this.ColumnNames.length;i++){
			  newtext=config.Descriptor.getConcatenatedStringValue(confID[i]);
			  if (newtext!=null && newtext.length()>0) {
				  this.actColumnNames[i]=new String(newtext);
			  } else {
				  this.actColumnNames[i]=new String(this.ColumnNames[i]);
			  }
				  
		  }
		  //tooltip text
		  newtext=config.Descriptor.getConcatenatedStringValue(confID[this.ColumnNames.length]);
		  if (newtext!=null && newtext.length()>0 && this.table!=null) {
			  this.table.setToolTipText(new String(newtext));
		  }
		  //this.table.setAutoCreateColumnsFromModel(true);
		  this.createTableColumnModel();
	  }
	  
	   abstract void createTableColumnModel();

	   
	}
