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

/**
 * Request Handler which takes the Requests and processes them using plugins
 */
public abstract class Stac_PluginSlot extends Stac_ReqHandler {
	//Class activePlugin=null;
	public String SlotType;
	
	protected Stac_Plugin activePlugin = null;
	
    public Stac_Configuration config=null;
	
	//process the jobs using the only one activePlugin
	final int USE_SINGLE_PLUGIN=0;
	//process the jobs unsinchronized using all the available plugins
	final int USE_PARALLEL_PLUGINS=1;
	//process the jobs sinchronously by the plugins with the
	//condition implemented in startNextPlugin(Stac_RespMsg lastResp)
	final int USE_SEQUENTIAL_PLUGINS=2;
	//process the jobs by plugins organized in a tree, where
	//the dequnetial groups
	final int USE_PLUGIN_EXECUTION_TREE=3;

	int currentUseMode = USE_SINGLE_PLUGIN;

    public final String getName() {
    	return ""+SlotType+"_Plugins";
    }
	
	
	class pluginTree extends Vector {
		void addPlugin(Object newplugin) {
			this.addElement(newplugin);
		}
		/**
		 * searches for previously added refplugin, 
		 * if it is found, it adds newlist below,
		 * otherwise it appends newplugin
		 * @param newplugin
		 * @param newList
		 * @param referencePlugin
		 */
		void addPlugin(Object newplugin,Object newList,Object referencePlugin) {
			Vector fnd=findPlugin(referencePlugin,this);
			if (fnd!=null) {
				((pluginTree)fnd.elementAt(0)).add(((Integer)fnd.elementAt(1)).intValue()+1, newList);				
			} else {
				this.addElement(newplugin);
			}
		}		
		void addPlugin(Object newplugin,Object referencePlugin) {
			addPlugin(newplugin,newplugin,referencePlugin);
		}		
		void addSubPlugin(Object newplugin,Object referencePlugin) {
			pluginTree list=new pluginTree();
			list.addElement(newplugin);
			addPlugin(newplugin,list,referencePlugin);
		}
		Vector findPlugin(Object ref,pluginTree node) {
			Vector pos=null;
			pluginTree treeNode=node;
			for (int i=0;i<treeNode.size();i++) {
				if (treeNode.elementAt(i) instanceof Vector) {
					Vector res=findPlugin(ref,(pluginTree)treeNode.elementAt(i));
					if (res!=null)
						return res;
				} else if (treeNode.elementAt(i).equals(ref)) {
					pos=new Vector();
					pos.addElement(treeNode);
					pos.addElement(new Integer(i));
				}
			}
			return pos;
		}
		Vector selectParallelPlugins(Object ref){
			Vector fnd=findPlugin(ref,this);
			pluginTree resfnd=null;
			Vector res=new Vector(0);
			if (fnd!=null) {
				resfnd= ((pluginTree)fnd.elementAt(0));				
			} else {
				resfnd= this;
			}
			for (int i=0;i<resfnd.size();i++){
				if (!(resfnd.elementAt(i) instanceof Vector)) {
					res.addElement(resfnd.elementAt(i));
				}
			}
			return res;
		}
		
	}
	pluginTree activePluginTree = new pluginTree();
	
	
    public Stac_PluginSlot (Stac_Session session,String SlotType) {
    	super(session);
    	this.SlotType=SlotType;
    	config= new Stac_Configuration(getName());
    	config.getConfiguration();
    }    
    
    public void closePlugin(Stac_Plugin pl) {
    	if (pl!=null)
    		pl.closePlugin();
    }
    
    public void closePlugin() {
    	closePlugin(activePlugin);
    	activePlugin=null;
    }
    
    public void closeJobManager() {
    	closePlugin();
    }

    
//    /**
//     * Reads the PluginSlot configuration file.
//     * @param specfile  - name of the file
//     * @param Descr - config parameters
//     */
//    public void read_specdef(String specfile,ParamTable Descr) {
//      	String corr;
//      	Util utils=new Util();
//        //read spec.dat
//      	corr = utils.opReadCl(specfile);
//        String tmp1[] = corr.split("SECTION");
// 
//    	read_section(tmp1,""+SlotType+"plugin",Descr);
//      	
//      }
//
    
    
    
    protected final Stac_Plugin loadPlugin (String pluginName,String instanceName) {
    	boolean notinstalled=false;
    	Class activePlugin1 = null;
    	Stac_Plugin newPlugin=null;
    	try {
    		//String modulename=pluginName.substring(0, pluginName.toLowerCase().lastIndexOf("plugin"));
    		String classBaseName=this.getClass().getName();
    		classBaseName=classBaseName.substring(0, classBaseName.lastIndexOf("."));
    		activePlugin1=Class.forName(classBaseName+".plugins.Stac_"+pluginName);
    		newPlugin = (Stac_Plugin)activePlugin1.newInstance();
    		((Stac_Plugin)newPlugin).startPlugin(session,instanceName);
    		Stac_Out.println(SlotType+" plugin " +pluginName+" is loaded!");
    	} catch (ClassNotFoundException e) {
    		notinstalled=true;
    		if (pluginName=="") {
    			Stac_Out.println("No "+SlotType+" plugin selected! Check the configuration file!");
    		} else {
    			Stac_Out.println(SlotType+" plugin " +pluginName+" has not been installed!");
    		}
    	} catch (Exception e) {
			Stac_Out.println(SlotType+" plugin " +pluginName+" is NOT loaded!");    			
    	}
    	return newPlugin;
    }
    
    protected final Stac_Plugin changePlugin (String pluginName,String instName) {
    	Stac_Plugin newPlugin=loadPlugin(pluginName,instName);
    	activePlugin=newPlugin;
    	return activePlugin;
    }
    
    protected final Stac_Plugin changePlugin (Stac_Plugin loadedPlugin) {
    	activePlugin=loadedPlugin;
    	return activePlugin;
    }
    
    
    
    protected final void setUseMode (int useMode) {
    	this.currentUseMode=useMode;
    }
    
    protected final Object addPlugin (String pluginName,String instName) {
    	Object newPlugin=loadPlugin(pluginName,instName);
    	activePluginTree.addPlugin(newPlugin);
    	return newPlugin;
    }

    
    
    
    @Override
    public void handle(Stac_ReqMsg req) {
    	if (currentUseMode==USE_SINGLE_PLUGIN){
    		super.handle(req);
    	} else if (currentUseMode==USE_PARALLEL_PLUGINS) {
    		Vector plugins=activePluginTree.selectParallelPlugins((Object)(new Integer(0)));
    		for (int i=0;i<plugins.size();i++) {
    			Stac_JobAction job;
    			Stac_ReqMsg newreq=req;
				try {
					if (i>0) {
						newreq=(Stac_ReqMsg)(req.duplicate());
					}
					super.handle(newreq);
				} catch (CloneNotSupportedException e) {
					session.addErrorMsg(req.msgId, "Could not delegate the job request to the plugin "+((Stac_Plugin)(plugins.elementAt(i))).getName());
				}    			
    		}

    	}
    	
    }
    
    
}


