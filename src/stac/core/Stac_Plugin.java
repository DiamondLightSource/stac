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


public abstract class Stac_Plugin {
    protected Stac_Session session;
    //ParamTable Descriptor= new ParamTable(); 
    Util utils = new Util();
    String instanceName="";
    public boolean allowGUI=true;
    
    public Stac_Configuration config=null;
    
    public final void setSession(Stac_Session session) {
    	this.session=session;
    }
    public final void startPlugin(Stac_Session session,String instanceName) {
    	setSession(session);
    	allowGUI=session.getAllowGUI();
    	this.setInstanceName(instanceName);
    	config= new Stac_Configuration(getName());
    	config.getConfiguration();
    	initPlugin();
    }
    public final void setInstanceName(String name) {
    	if (name!=null)
    		instanceName=name;
    }
    public abstract void initPlugin();
    public abstract String getCreditString();
    public final String getName() {
    	if (instanceName.length()==0)
    		return this.getClass().getSimpleName();
    	else
    		return this.getClass().getSimpleName()+"_"+instanceName;
    }
    
//    public String[] getConfiguration() {
//    	return utils.read_section(System.getProperty("BCMDEF"),getName(),Descriptor);
//    }
    
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

    /**
     * Updates the BCM specdef file. 
     * @param newParams: KEYWORD and te new parameters to be written
     */
//    public void updateConfiguration(ParamTable BCM_Descriptor,ParamTable newParams) {
//    	try {
//    			String corr;
//    			//Util utils=new Util();
//    			//read spec.dat
//    			String [] sections=utils.read_section(System.getProperty("BCMDEF"),"",null);
//    			FileWriter newFile= new FileWriter(System.getProperty("BCMDEF"));
//    		  	for(int is=0; is<sections.length;is++) {
//    		  		String tmp[] = sections[is].split("\\s+");
//    		  		if (tmp.length < 2)
//    		  			continue;
//    		  		if (tmp[1].startsWith(getName())) {
//    	    			String tmp1[] = sections[is].split("\n");
//    	    			Vector paramVal;
//    	    			for (int l=0;l<tmp1.length;l++) {
//    	    				if (tmp1[l].startsWith("#",0)) {
//    	    					newFile.write(tmp1[l]+"\n");
//    	    					continue;
//    	    				}
//    	    				String tmp2[] = tmp1[l].split("\\s+");
//    	    				if(tmp2.length==0) {
//    	    					newFile.write(tmp1[l]+"\n");
//    	    					continue;        	
//    	    				}
//    	    				int offset=0;
//    	    				if (tmp2[0].length()==0)
//    	    					offset++;
//    	    				String name = tmp2[0+offset];
//    	    				paramVal=newParams.getValueList(name);
//    	    				if (paramVal!=null) {
////    	    					newFile.write("# "+tmp1[l]+"\n");
//    	    					String actParam = "";
//    	    					newFile.write(name);
//    	    					for(int li=0;li<paramVal.size();li++) actParam+=" "+paramVal.elementAt(li);
//    	    					newFile.write(actParam);
//    	    					newFile.write("\n");
//    	    					interpret_specdef(name+actParam,BCM_Descriptor);
//    	    					newParams.removeOldValueList(name);
//    	    				} else {
//    	    					newFile.write(tmp1[l]+"\n");
//    	    				}
//    	    			}
//    	    			for(int i=0;i<newParams.pnames.size();i++) {
//    						String actParam = "";
//    						newFile.write(""+newParams.pnames.elementAt(i));
//    						for(int li=0;li<((Vector)newParams.pvalues.elementAt(i)).size();li++) actParam+=" "+((Vector)newParams.pvalues.elementAt(i)).elementAt(li);
//    						newFile.write(actParam);
//    						newFile.write("\n");
//    						interpret_specdef(""+newParams.pnames.elementAt(i)+actParam,BCM_Descriptor);
//    	    			}
//    		  		} else {
//    		  			
//    		  		}
//    		  	}
//    			newFile.close();
//    	} catch (Exception e) {
//    	}
//    }
    
    abstract protected void closePlugin();
    
    
}


