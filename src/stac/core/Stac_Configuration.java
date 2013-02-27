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

import org.sudol.sun.util.Util;

import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


public class Stac_Configuration {
    public ParamTable Descriptor= new ParamTable(); 
    Util utils = new Util();
    String secName="";
    
    public Stac_Configuration (String secName) {
    	this.secName=secName;
    }

    
    public final void setInstanceName(String name) {
    	secName=name;
    }
    
    public String[] getConfiguration() {
    	try {
			return utils.read_section(System.getProperty("BCMDEF"),secName,Descriptor);
		} catch (RuntimeException e) {
			String [] res= new String [] {"config_not_found"};
			return res;
		}
    }
    
    /**
     * Interprets a line of the specdef file, and actualize the Descriptor 
     * @param specline
     * @param Descr
     */
    public void interpret_specdef(String specline,ParamTable Descr) {
    	if (specline.startsWith("#",0))
    		return;
    	String tmp2[] = specline.split("\\s+");
    	if(tmp2.length==0)
    		return;
    	int offset=0;
    	if (tmp2[0].length()==0)
    		offset++;
    	if (tmp2.length<=offset+1)
    		return;
    	String name = tmp2[0+offset];
    	Vector params = new Vector();
    	for(int i=offset+1;i<tmp2.length;i++) {
    		params.addElement(tmp2[i]);    			
    	}
    	Descr.setValueList(name,params);
    }

    public void updateConfiguration(ParamTable BCM_Descriptor,ParamTable newParamsInput) {
    	this.updateConfiguration(BCM_Descriptor, newParamsInput, true);
    }
    /**
     * Updates the BCM specdef file. 
     * @param newParams: KEYWORD and te new parameters to be written
     */
    public void updateConfiguration(ParamTable BCM_Descriptor,ParamTable newParamsInput,boolean fileupdate) {
    	if (fileupdate) {
    	try {
    		ParamTable newParams=new ParamTable(newParamsInput);
    		String corr;
    			//Util utils=new Util();
    			//read spec.dat
    		String [] sections=utils.read_section(System.getProperty("BCMDEF"),"",null);
    		FileWriter newFile= new FileWriter(System.getProperty("BCMDEF"));
    		try {
    		  	for(int is=0; is<sections.length;is++) {
    		  		String tmp[] = sections[is].split("\\s+");
    		  		if ((tmp.length >=2) && (tmp[1].startsWith(secName) || (secName.length()==0 && is==0) )) {
    	    			String tmp1[] = sections[is].split("\n");
    	    			Vector paramVal;
    	    			for (int l=0;l<tmp1.length;l++) {
    	    				if (tmp1[l].startsWith("#",0)) {
    	    					newFile.write(tmp1[l]+"\n");
    	    					continue;
    	    				}
    	    				String tmp2[] = tmp1[l].split("\\s+");
    	    				if(tmp2.length==0) {
    	    					newFile.write(tmp1[l]+"\n");
    	    					continue;        	
    	    				}
    	    				int offset=0;
    	    				for (;offset<tmp2.length && tmp2[offset].length()==0;offset++);
    	    				if (offset>=tmp2.length) {
    	    					newFile.write(tmp1[l]+"\n");
    	    					continue;        	
    	    				}
    	    				String name = tmp2[0+offset];
    	    				paramVal=newParams.getValueList(name);
    	    				if (paramVal!=null) {
//    	    					newFile.write("# "+tmp1[l]+"\n");
    	    					String actParam = "";
    	    					newFile.write(name);
    	    					for(int li=0;li<paramVal.size();li++) actParam+=" "+paramVal.elementAt(li);
    	    					newFile.write(actParam);
    	    					newFile.write("\n");
    	    					interpret_specdef(name+actParam,BCM_Descriptor);
    	    					newParams.removeOldValueList(name);
    	    				} else {
    	    					newFile.write(tmp1[l]+"\n");
    	    				}
    	    			}
    	    			for(int i=0;i<newParams.pnames.size();i++) {
    						String actParam = "";
    						newFile.write(""+newParams.pnames.elementAt(i));
    						for(int li=0;li<((Vector)newParams.pvalues.elementAt(i)).size();li++) actParam+=" "+((Vector)newParams.pvalues.elementAt(i)).elementAt(li);
    						newFile.write(actParam);
    						newFile.write("\n");
    						interpret_specdef(""+newParams.pnames.elementAt(i)+actParam,BCM_Descriptor);
    	    			}
    	    			newFile.write("\n");
    		  		} else {
    		  			newFile.write(sections[is]);
    		  		}
    		  		if (is<sections.length-1)
    		  			newFile.write("SECTION");
    		  	}
    			newFile.close();
    		} catch (Exception e) {
    			Stac_Out.printTimeln("Problem during updating the configuration ("+System.getProperty("BCMDEF")+")");
    			newFile.close();
    		}
    	} catch (Exception e) {
			Stac_Out.printTimeln("Problem in writing the configuration ("+System.getProperty("BCMDEF")+")");
    	}
    	} else {
        	try {
        		ParamTable newParams=new ParamTable(newParamsInput);
        	    			for(int i=0;i<newParams.pnames.size();i++) {
        						String actParam = "";
        						for(int li=0;li<((Vector)newParams.pvalues.elementAt(i)).size();li++) actParam+=" "+((Vector)newParams.pvalues.elementAt(i)).elementAt(li);
        						interpret_specdef(""+newParams.pnames.elementAt(i)+actParam,BCM_Descriptor);
        	    			}
        	} catch (Exception e) {
        	}
    		
    	}
    }
    
    
}


