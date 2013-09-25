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


public class ParamTable implements ParamTableInt {
    public Vector pnames  = new Vector();
    public Vector pvalues = new Vector();
    public ParamTable () {
    }
    public ParamTable (Object[][] initData) {
      for(int i=0; i<initData.length;i++) {
        Vector actValueList = new Vector();
        for(int j=1;j<initData[i].length;j++) {
          actValueList.addElement(initData[i][j]);
        }
        setValueList(initData[i][0],actValueList);
      }
    }
    public ParamTable (String[] initNames, double[] initData) {
      for(int i=0; i<initNames.length;i++) {
        setSingleValue(initNames[i],new Double(initData[i]));
      }
    }
    public ParamTable (DataInputStream in){
    	load(in);
    }
    /**
     * creates a new Paramtable with the same values as "in"
     * the new table references each original pvalues object 
     * @param in the input paramtable 
     */
    public ParamTable (ParamTable in){
    	add(in);
    }
    /**
     * appends with the new Paramtable with the same values as "in"
     * the new table references each original pvalues object 
     * @param in the input paramtable 
     */
    public void add (ParamTable in){
    	try {
    		int size=in.pnames.size();
    		for(int i=0;i<size;i++){
    			String name=(String)in.pnames.elementAt(i);
    			Vector list=new Vector();
    			Vector origlist=(Vector)in.pvalues.elementAt(i);
    			for (int j=0;j<origlist.size();j++) {
    				list.addElement(origlist.elementAt(j));
    			}
    			pnames.addElement(name);
    			pvalues.addElement(list);
    		}
    	} catch (Exception e) {
    		
    	}
    }
    public int findNameOfStarting(Object name) throws Exception {
    	int index=0;
    	if (!(name instanceof String))
    		//error
    		throw new Exception("The passed Object is not a String");
    	for (index=0;index<pnames.size();index++)
    		if ((pnames.elementAt(index) instanceof String) && ((String)name).startsWith((String)(pnames.elementAt(index))))
    			break;
    	if (index>=pnames.size())
    		//error;
    		throw new Exception("Has not found any String in the table that would be contained by the String passed");
    	return index;
    }
    public Object getNameForParam(Object firstParam) throws Exception {
    	int index=0;
    	for (index=0;index<pnames.size();index++)
    			if (((Vector)(pvalues.elementAt(index))).size()>=1)
    				if (((Vector)(pvalues.elementAt(index))).elementAt(0).equals(firstParam))
    					break;
    	if (index>=pnames.size())
    		//error;
    		throw new Exception("Has not found any Name in the table that would contained the parameter as its first value");
    	return pnames.elementAt(index);
    	
    }
    public Object getNameForAnyParam(Object firstParam) throws Exception {
    	int index=0;
    	boolean fnd=false;
    	for (index=0;index<pnames.size();index++) {
    			for (int sInd=0;sInd<((Vector)(pvalues.elementAt(index))).size();sInd++)
    				if (((Vector)(pvalues.elementAt(index))).elementAt(sInd).equals(firstParam)) {
    					fnd=true;
    					break;
    				}
    			if (fnd)
    				break;
    	}
    	if (index>=pnames.size())
    		//error;
    		throw new Exception("Has not found any Name in the table that would contained the parameter as its first value");
    	return pnames.elementAt(index);
    	
    }
    public void removeOldValueList (Object name) {
    	int index=pnames.indexOf(name);
    	if(index==-1)
    		return;
    	pnames.removeElementAt(index);
    	pvalues.removeElementAt(index);
    }
    public void setValueList(Object name, Object valueList) {
    	removeOldValueList(name);
        pnames.addElement(name);
        pvalues.addElement(valueList);
      }
    public void setDoubleValueList(Object name, double[] valueList) {
    	removeOldValueList(name);
        Vector v = new Vector();
        for(int l=0;l<valueList.length;l++) {
        	v.addElement(new Double(valueList[l]));
        }
        pnames.addElement(name);
        pvalues.addElement(v);
      }
    public void setSingleValue(Object name, Object value) {
    	removeOldValueList(name);
        pnames.addElement(name);
        Vector singleParam = new Vector();
        singleParam.addElement(value);
        pvalues.addElement(singleParam);
      }
    public void setSingleDoubleValue(Object name, double value) {
    	removeOldValueList(name);
        pnames.addElement(name);
        Vector singleParam = new Vector();
        singleParam.addElement(new Double(value));
        pvalues.addElement(singleParam);
      }
    public void setDoubleValueAt(Object name, int pos, double value) {
    	Vector valueList = getValueList(name);
    	if (valueList==null) {
    		removeOldValueList(name);
    		pnames.addElement(name);
            valueList = new Vector();
            pvalues.addElement(valueList);            
    	}
    	while (valueList.size()<pos) {
            valueList.addElement(new Double(0.0));
    	}
    	valueList.setElementAt(new Double(value),pos);
      }
    public void addValue(Object name, Object value) {
    	Vector valueList = getValueList(name);
    	if (valueList==null) {
    		removeOldValueList(name);
    		pnames.addElement(name);
            valueList = new Vector();
            pvalues.addElement(valueList);            
    	}
    	valueList.addElement(value);
    }
    public Vector getValueList(Object name) {
    	try {
      return (Vector)(pvalues.elementAt(pnames.indexOf(name)));
    	} catch (Exception e) {
    		return null;
    	}
    }
    public Vector getValueListOfStarting(Object name) {
    	try {
      return (Vector)(pvalues.elementAt(findNameOfStarting(name)));
    	} catch (Exception e) {
    		return null;
    	}
    }
    public Object getValueAt (Object name, int pos) {
    	try {
      return getValueList(name).elementAt(pos);
    	} catch (Exception e) {
    		return null;
    	}
    	
    }
    public Object getFirstValue(Object name){
    	try {
      return getValueList(name).elementAt(0);
    	} catch (Exception e) {
    		return null;
    	}      
    }
    public Object getFirstValueOfStarting(Object name){
    	try {
      return getValueListOfStarting(name).elementAt(0);
    	} catch (Exception e) {
    		return null;
    	}      
    }
    public String getFirstStringValue(Object name) {
    	try {
      return ((String)(getFirstValue(name)));
    	} catch (Exception e) {
    		return null;
    	}
    }
    public String getConcatenatedStringValue(Object name) {
    	try {
    		Vector list = getValueList(name);
    		String res="";
    		String sp="";
    		boolean nl=true;
    		for (int i=0;i<list.size();i++) {
    			if ("\\n".compareTo((String)list.elementAt(i))==0) {
    				res=res.concat("\n");
    				nl=true;
    			}
    			else 
    			{
    				if (!nl)
    					sp=" ";
    				else
    					sp="";
    				nl=false;
    				res=res.concat(sp+(String)list.elementAt(i));
    			}
    		}
    		return (res);
    	} catch (Exception e) {
    		return null;
    	}
    }
    public String getFirstStringValueOfStarting(Object name) {
    	try {
    		return ((String)(getFirstValueOfStarting(name)));
    	} catch (Exception e) {
    		return null;
    	}
    }
    public double getFirstDoubleValue(Object name) {
    	try {
    		Object val=getFirstValue(name);
    		if (val instanceof String) {
    			val=new Double((String)val);
    		} else if (val instanceof Integer) {
    			val=new Double(((Integer)val).intValue());
    		}    			
    		return ((Double)(val)).doubleValue();
    	} catch (Exception e) {
    		return 0;
    	}
    }
    public int getFirstIntegerValue(Object name) {
    	try {
    		Object val=getFirstValue(name);
    		if (val instanceof String) {
    			val=new Integer((String)val);
    		} 
    		return ((Integer)(val)).intValue();
    	} catch (Exception e) {
    		return 0;
    	}
    }
    public double getDoubleValueAt(Object name,int pos) {
    	try {
    		Object val=getValueAt(name,pos);
    		if (val instanceof String) {
    			val=new Double((String)val);
    		} else if (val instanceof Integer) {
    			val=new Double(((Integer)val).intValue());
    		}
    		return ((Double)(val)).doubleValue();
    	} catch (Exception e) {
    		return 0;
    	}
    }
    public double[] getDoubleVector(Object name) {
    	try {
      double[] vl = new double[(getValueList(name)).size()];
      for(int i=0;i<vl.length;i++) {
        vl[i] = getDoubleValueAt(name,i);
      }
      return vl;
    	} catch (Exception e) {
    		return null;
    	}
    }
    public void clear() {
      pnames.clear();
      for(int i=0; i< pvalues.size();i++) {
        ((Vector)(pvalues.elementAt(i))).clear();
      }
      pvalues.clear();
    }
    public void dump() {
      Stac_Out.println("ParamTable: (size = "+pvalues.size()+")" );
      for(int i=0; i< pvalues.size();i++) {
      	int size=((Vector)(pvalues.elementAt(i))).size();
      	Stac_Out.println("  parameter name: "+pnames.elementAt(i)+" (number of values = "+size+")");
        Stac_Out.println("    "+((Vector)(pvalues.elementAt(i))));
      }
    }
    public void load(DataInputStream in){
    	try {
    		int size=in.readInt();
    		for(int i=0;i<size;i++){
    			String name=in.readUTF();
    			Vector list=new Vector();
    			int vsize=in.readInt();
    			for(int j=0;j<vsize;j++){
    				String velement=in.readUTF();
    				list.addElement(velement);
    			}
    			pnames.addElement(name);
    			pvalues.addElement(list);
    		}
    	} catch (Exception e) {
    		
    	}
    	
    }
    public void save(DataOutputStream out){
    	try {
    		out.writeInt(pnames.size());
    		for(int i=0;i<pnames.size();i++){
    			out.writeUTF(""+pnames.elementAt(i));
    			Vector list=(Vector)pvalues.elementAt(i);
    			out.writeInt(list.size());
    			for(int j=0;j<list.size();j++){
    				out.writeUTF(""+list.elementAt(j));
    			}
    		}
    	} catch (Exception e) {
    		
    	}
    }
  }


