package stac.core;

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


public interface ParamTableInt {
    public int findNameOfStarting(Object name) throws Exception;
    public void setValueList(Object name, Object valueList);
    public void setDoubleValueList(Object name, double[] valueList);
    public void setSingleValue(Object name, Object value);
    public void setSingleDoubleValue(Object name, double value);
    public void setDoubleValueAt(Object name, int pos, double value);
    public Vector getValueList(Object name);
    public Vector getValueListOfStarting(Object name);
    public Object getValueAt (Object name, int pos);
    public Object getFirstValue(Object name);
    public Object getFirstValueOfStarting(Object name);
    public String getFirstStringValue(Object name);
    public String getConcatenatedStringValue(Object name);
    public String getFirstStringValueOfStarting(Object name);
    public double getFirstDoubleValue(Object name);
    public int getFirstIntegerValue(Object name);
    public double getDoubleValueAt(Object name,int pos);
    public double[] getDoubleVector(Object name);
    public void clear();
    public void dump();
    public void load(DataInputStream in);
    public void save(DataOutputStream out);
  }


