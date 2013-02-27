package stac.vbcm;

import memops.api.Implementation.MemopsRoot;
import memops.api.Implementation.Url;
import memops.api.Implementation.Repository;
import memops.general.ApiException;

import bioxdm.api.instrumentation.Synchrotron.BeamLine;
import bioxdm.api.instrumentation.Goniometry.*;
import bioxdm.api.Types.GenVector;
import bioxdm.api.Types.UnitVector;
import bioxdm.api.Types.OrthogonalMatrix;
import bioxdm.api.Types.OrientationMatrix;
import bioxdm.api.Types.Angle;

import org.sudol.sun.util.*;
import org.sudol.sun.simulator.*;

import stac.core.*;
import stac.gui.*;

// Standard library imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;


// Standard imports
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.media.j3d.*;
import javax.media.j3d.View;
import javax.vecmath.*;
import javax.sound.midi.Sequencer.SyncMode;
import javax.swing.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

// Application specific imports
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigator;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.picking.*;
import com.sun.j3d.utils.geometry.*;

//import com.sun.j3d.exp.swing.JCanvas3D;
import com.sun.j3d.loaders.Scene;

//import org.ietf.uri.ContentHandlerFactory;
//import org.ietf.uri.FileNameMap;
//import org.ietf.uri.URI;

import org.web3d.j3d.loaders.*;
import org.web3d.net.content.VRMLContentHandlerFactory;
import org.web3d.net.content.VRMLFileNameMap;
import org.web3d.vrml.renderer.j3d.input.J3DPickingManager;


//new ones
import org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.renderer.j3d.browser.VRMLUniverse;
import org.web3d.vrml.nodes.runtime.RouteManager;
import org.web3d.vrml.sav.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.nodes.runtime.*;
import org.web3d.vrml.nodes.loader.*;
import org.web3d.vrml.renderer.j3d.input.*;

import org.web3d.browser.BrowserCore;
import org.web3d.vrml.nodes.loader.ScriptLoader;
import org.web3d.vrml.nodes.runtime.RouteManager;
import org.web3d.vrml.parser.VRMLParserFactory;
import org.web3d.vrml.parser.FactoryConfigurationError;
import org.web3d.vrml.renderer.j3d.browser.VRMLBrowserCanvas;
import org.web3d.vrml.renderer.j3d.browser.VRMLUniverse;
import org.web3d.vrml.scripting.ScriptEngine;
import org.web3d.vrml.scripting.jsai.VRML97ScriptEngine;
import org.web3d.vrml.scripting.ecmascript.ECMAScriptEngine;
import org.web3d.vrml.scripting.ecmascript.JavascriptScriptEngine;
import org.web3d.vrml.scripting.sai.JavaSAIScriptEngine;

import sun.util.calendar.CalendarDate;


import javax.swing.*;
import javax.swing.plaf.*;

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicToolTipUI;
import javax.swing.text.*;


/**
 * An Xj3D demo example based implementation of 
 * a VirtualGoniometer Class
 *
 * @author Sandor Brockhauser
 * @version $Revision: 1.5 $
 */
public class vBCM extends SimulatorFrame{ //JFrame {

	  String selectedMotorName="";

//	 JMultiLineToolTip.java

    private PickRotateBehavior pickRotate=null;
    private PickTranslateBehavior pickTranslate=null;
    private PickZoomBehavior pickZoom=null;
    private PickDragBehavior pickDrag=null;
	
	
    public class MyPickCallbackClass extends Object implements PickingCallback{
        public void transformChanged(int type, TransformGroup tg) {
        		
                if (isDevice(tg)) {
                	
                	if (jLMode.getText().charAt(0)=='E') {
                		jLMode.setText("Edit: "+getDeviceName(tg));
                	} else {
                    	jLMode.setText("Operate: "+getDeviceName(tg));
                	}
                	
                } else {
                	//Stac_Out.println("picking");                	
                }
        }        
    }
    
    public class PickDragBehavior extends PickMouseBehavior {
    	
    	private TransformGroup currentTG;
    	
    	//WakeupCriterion[] defaultConditions = null;
    	
    	PickDragBehavior (BranchGroup root,Canvas3D canvas,Bounds bounds){
    		super(canvas,root,bounds);
    		this.setSchedulingBounds(bounds);
    	}
    	
    	public void updateScene(int xpos, int ypos){
    		TransformGroup tg = null;
    		
    		SceneGraphPath[] tgList = pickScene.pickAllSorted(xpos,ypos);
    		if (tgList==null)
    			return;
    		int i;
    		for (i=0;i<tgList.length;i++) {
        		tg =(TransformGroup)pickScene.pickNode(tgList[i],
    					PickObject.TRANSFORM_GROUP);
                if (isDevice(tg)) {
    				break;
    			}
    		}
    		if (i==tgList.length)
    			return;
    		/*
    		tg =(TransformGroup)pickScene.pickNode(pickScene.pickClosest(xpos,
    				ypos),
					PickObject.TRANSFORM_GROUP);
    		*/
    		// Check for valid selection
    		if ((tg != null) &&
    				(tg.getCapability(TransformGroup.ALLOW_TRANSFORM_READ)) &&
					(tg.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE))){
                if (isDevice(tg)) {
        			currentTG = tg;
    				jLMode.setText("Operate: ");
    				jCBDevice.setSelectedItem(getDeviceFileName(tg));
    			} else {
    				//Stac_Out.println("picking");                	
    			}
    			
    		}
    		
    	}
    }
    
	
    class Button_actionAdapter implements java.awt.event.ActionListener {
    	  vBCM adaptee;

    	  Button_actionAdapter(vBCM adaptee) {
    	    this.adaptee = adaptee;
    	  }

    	  
    	  public void actionPerformed(ActionEvent e) {
      	    if (e.getSource()==adaptee.jChBSlow) {
    	        //Slow MOvements
      	    	setslowMovements(jChBSlow.isSelected());
    	    } else if (e.getSource()==adaptee.jChBStereo) {
    	        //Stereo view
   	    		((Canvas3D)get3dcanvas()).setStereoEnable(adaptee.jChBStereo.isSelected());
    	    } else if (e.getSource()==adaptee.jBCenter) {
    	        //Slow MOvements
      	    	CenterNeedle();
    	    } else if (e.getSource()==adaptee.jBCol) {
    	        //Collsion map
				GregorianCalendar gc = new GregorianCalendar();
				Date actDate = gc.getTime();
				String actDateStr = new PrintfFormat("%04d_%02d_%02d_%02d_%02d_%02d.%03d").sprintf(new Object[] {
						new Integer(gc.get(GregorianCalendar.YEAR)),
						new Integer(gc.get(GregorianCalendar.MONTH)+1),
						new Integer(gc.get(GregorianCalendar.DAY_OF_MONTH)),
						new Integer(gc.get(GregorianCalendar.HOUR_OF_DAY)),
						new Integer(gc.get(GregorianCalendar.MINUTE)),
						new Integer(gc.get(GregorianCalendar.SECOND)),
						new Integer(gc.get(GregorianCalendar.MILLISECOND)),
						});		
				//TODO: read dtox array and resolution from textfield

    	    	generateCCPNCollisionMap("CM_STORAGE_"+actDateStr,new Double [] {new Double(0.05),new Double(0.08),new Double(0.15),new Double(0.30)},10.0);
    	    } else if (e.getSource()==adaptee.jBNav) {
    	        //Navigation
    	    	pickRotate.setEnable(false);
    	    	pickTranslate.setEnable(false);
    	    	pickZoom.setEnable(false);
    	    	pickDrag.setEnable(false);
    	    	setmousenavigation(true);
    	    	jLMode.setText("Navigation");
				jCBDevice.setSelectedItem("???");
				jCBDevice.setEnabled(false);
    	    } else if (e.getSource()==adaptee.jBEdit) {
    	        //Edit
    	    	setmousenavigation(false);
    	    	pickDrag.setEnable(false);
    	    	pickRotate.setEnable(true);
    	    	pickTranslate.setEnable(true);
    	    	pickZoom.setEnable(true);
    	    	jLMode.setText("Edit: ");
				jCBDevice.setSelectedItem("???");
				jCBDevice.setEnabled(false);
    	    } else if (e.getSource()==adaptee.jBOperate) {
    	        //Operate
    	    	pickRotate.setEnable(false);
    	    	pickTranslate.setEnable(false);
    	    	pickZoom.setEnable(false);
    	    	setmousenavigation(true);
    	    	pickDrag.setEnable(true);
    	    	jLMode.setText("Operate: ");
				//jCBDevice.setSelectedItem("???");
				jCBDevice.setEnabled(true);
    	    } else if (e.getSource()==adaptee.jBDecrease) {
    	        //Decrease
    	    	if (jLMode.getText().charAt(0)=='O'){
    	    		adaptee.DoOperation(selectedMotorName,-1*new Double(jTFStep.getText()).doubleValue(),false);
    	    	}
    	    } else if (e.getSource()==adaptee.jBIncrease) {
    	        //Increase
    	    	if (jLMode.getText().charAt(0)=='O'){
    	    		adaptee.DoOperation(selectedMotorName,new Double(jTFStep.getText()).doubleValue(),false);
	    	    }
    	    } else if (e.getSource()==adaptee.jBGo) {
    	    	//adaptee.setMotorGUI(adaptee);
    	        //Set
    	    	if (jLMode.getText().charAt(0)=='O'){
    	    		adaptee.DoOperation(selectedMotorName,new Double(jTFStep.getText()).doubleValue(),true);
	    	    }
    	    } else if (e.getSource()==adaptee.jCBDevice) {
    	        //Set
    	    	if (jLMode.getText().charAt(0)=='O'){
    	    		int device = jCBDevice.getSelectedIndex();
    	    		device--;
    	    		if (device<0) {
    	    			device=0;
    	    			jCBDevice.setSelectedIndex(device);
    	    		}
    	    		//pick the object
    	    	    pickDrag.currentTG=(TransformGroup)getdeviceelementAt(device);
    	    	    selectedMotorName=getMotorList()[device];
    	    	    //System.out.println("Selected Device: "+device);
    	    	    //System.out.println("Selected Motor : "+selectedMotorName);
	    	    }
    	    }
    	}
    }
    
    
    
    JButton jBNav      = new JButton()
    {
        public JToolTip createToolTip()
        {
                return new JMultiLineToolTip();
        }
    };
    JButton jBEdit     = new JButton();
    JButton jBOperate  = new JButton();
    JCheckBox jChBSlow = new JCheckBox();
    JCheckBox jChBStereo = new JCheckBox();
    JButton jBCol      = new JButton();
    JButton jBCenter   = new JButton();
    
    JLabel  jLMode     = new JLabel();
    JComboBox jCBDevice= new JComboBox();
    JButton jBDecrease = new JButton();
    JButton jBIncrease = new JButton();
    JButton jBGo       = new JButton();
    JTextField jTFStep = new JTextField(); 

    public void addDevice(String devName) {
    	super.addDevice(devName);
		jCBDevice.addItem(devName);    	
    }
    
    public String [] getMotorList() {
    	return super.getMotorList();
    }
    
    String vbsName;
    
    /**
     * Create a new loader
     *
     * @param initLocation The world to load
     */
    public vBCM(String vGonioFileName,boolean fr) {
        super(vGonioFileName,!fr);

        vbsName=vGonioFileName;
        
        Container content_pane = this;//getContentPane();
        content_pane.setLayout(new BorderLayout());

        
        content_pane.add("Center",get3dcanvas());

        content_pane.add("North",geturlLabel());
        
        
        JPanel south= new JPanel();
        content_pane.add("South",south);        
        south.setLayout(new BoxLayoutFixed(south,BoxLayoutFixed.LINE_AXIS));
        JPanel southW= new JPanel();
        south.add(southW);        
        southW.setLayout(new BoxLayoutFixed(southW,BoxLayoutFixed.PAGE_AXIS));
        JPanel southWN= new JPanel();
        southW.add(southWN);        
        southWN.setLayout(new BoxLayoutFixed(southWN,BoxLayoutFixed.LINE_AXIS));
        
        southWN.add(jBNav);
        jBNav.setText("Navigate");
        jBNav.setPreferredSize(new Dimension(100,34));
        jBNav.setToolTipText(
        		"Mouse Navigation\nSpecial Keys:\n"+
        		" [=]         HOME\n"+
        		" [<-/->]     Rotate about Y\n"+
        		" [PgUp/Down] Rotate about X\n"+
        		" [Up/Down]   Translate along Z\n"+
        		" [Gray +/-]  Zoomify");
        jBNav.addActionListener(new Button_actionAdapter(this));
        southWN.add(jBEdit);
        jBEdit.setText("Edit");
        jBEdit.setPreferredSize(new Dimension(100,34));
        jBEdit.setToolTipText("Mouse Editing of the Objects");
        jBEdit.addActionListener(new Button_actionAdapter(this));
        southWN.add(jBOperate);
        jBOperate.setText("Operate");
        jBOperate.setPreferredSize(new Dimension(100,34));
        jBOperate.setToolTipText("Mouse Editing of the Objects");
        jBOperate.addActionListener(new Button_actionAdapter(this));
        
        JPanel southWS= new JPanel();
        southW.add(southWS);        
        southWS.setLayout(new BoxLayoutFixed(southWS,BoxLayoutFixed.LINE_AXIS));
        
        southWS.add(jBCenter);
        jBCenter.setText("Center");
        jBCenter.setToolTipText("Automatic Centering");
        jBCenter.addActionListener(new Button_actionAdapter(this));
        jBCenter.setPreferredSize(new Dimension(100,34));
        
        southWS.add(jBCol);
        jBCol.setText("Collision");
        jBCol.setToolTipText("Generate new collision map");
        jBCol.addActionListener(new Button_actionAdapter(this));
        jBCol.setPreferredSize(new Dimension(100,34));
        
        southWS.add(jChBSlow);
        jChBSlow.setText("Slow Movement");
        jChBSlow.setToolTipText("Slow Motor Movements");
        jChBSlow.addActionListener(new Button_actionAdapter(this));
        jChBSlow.setSelected(getslowMovements());
        
        southWS.add(jChBStereo);
        jChBStereo.setText("Stereo view");
        jChBStereo.setToolTipText("Set Stereo View");
        jChBStereo.addActionListener(new Button_actionAdapter(this));
        jChBStereo.setEnabled(((Canvas3D)get3dcanvas()).getStereoAvailable());
        jChBStereo.setSelected(((Canvas3D)get3dcanvas()).getStereoEnable());
        
        JPanel southRight= new JPanel();
        south.add(southRight);        
        southRight.setLayout(new BoxLayoutFixed(southRight,BoxLayoutFixed.PAGE_AXIS));
        JPanel southRUp= new JPanel();
        southRight.add(southRUp);        
        southRUp.setLayout(new BoxLayoutFixed(southRUp,BoxLayoutFixed.LINE_AXIS));
        southRUp.add(jLMode);
        jLMode.setText("Navigation");
        //jLMode.setMinimumSize(new Dimension(200,34));
        //jLMode.setMaximumSize(new Dimension(200,34));
        jLMode.setPreferredSize(new Dimension(100,34));
        southRUp.add(jCBDevice);
        jCBDevice.setPreferredSize(new Dimension(200,34));
        jCBDevice.addItem("???");
        jCBDevice.enableInputMethods(false);
        jCBDevice.setEnabled(false);
        jCBDevice.addActionListener(new Button_actionAdapter(this));
        JPanel southRDown= new JPanel();
        southRight.add(southRDown);        
        southRDown.setLayout(new BoxLayoutFixed(southRDown,BoxLayoutFixed.LINE_AXIS));
        southRDown.add(javax.swing.Box.createHorizontalGlue());
        southRDown.add(jBDecrease);
        jBDecrease.setText("-");
        jBDecrease.setPreferredSize(new Dimension(50,34));
        jBDecrease.setMinimumSize(new Dimension(50,34));
        jBDecrease.setMaximumSize(new Dimension(50,34));
        jBDecrease.setToolTipText("Decreasing the Position of the Objects");
        jBDecrease.addActionListener(new Button_actionAdapter(this));
        southRDown.add(jTFStep);
        jTFStep.setText("1.0");
        jTFStep.setToolTipText("Stepsize for Increasing/Decreasing the Position");
        jTFStep.setMinimumSize(new Dimension(100,34));
        jTFStep.setMaximumSize(new Dimension(100,34));
        jTFStep.setPreferredSize(new Dimension(100,34));
        southRDown.add(jBIncrease);
        jBIncrease.setText("+");
        jBIncrease.setPreferredSize(new Dimension(50,34));
        jBIncrease.setMinimumSize(new Dimension(50,34));
        jBIncrease.setMaximumSize(new Dimension(50,34));
        jBIncrease.setToolTipText("Increasing the Position of the Objects");
        jBIncrease.addActionListener(new Button_actionAdapter(this));
        southRDown.add(jBGo);
        jBGo.setText("GO");
        jBGo.setPreferredSize(new Dimension(50,34));
        jBGo.setMinimumSize(new Dimension(50,34));
        jBGo.setMaximumSize(new Dimension(50,34));
        jBGo.setToolTipText("Set the Position of the Objects");
        jBGo.addActionListener(new Button_actionAdapter(this));
        southRDown.add(javax.swing.Box.createHorizontalGlue());
        
        
        
        
        
        
        

        
        //examineGroup.addChild(new ColorCube(0.4));
        
        PickingCallback myPickCallback = new MyPickCallbackClass();
        pickRotate = new PickRotateBehavior(getsceneRoot(),(Canvas3D)get3dcanvas(),new BoundingSphere());
        pickRotate.setEnable(false);
        getsceneRoot().addChild(pickRotate);
        pickRotate.setupCallback(myPickCallback);
        pickTranslate = new PickTranslateBehavior(getsceneRoot(),(Canvas3D)get3dcanvas(),new BoundingSphere());
        pickTranslate.setEnable(false);
        getsceneRoot().addChild(pickTranslate);
        pickTranslate.setupCallback(myPickCallback);
        pickZoom = new PickZoomBehavior(getsceneRoot(),(Canvas3D)get3dcanvas(),new BoundingSphere());
        pickZoom.setEnable(false);
        getsceneRoot().addChild(pickZoom);
        pickZoom.setupCallback(myPickCallback);
        pickDrag = new PickDragBehavior(getsceneRoot(),(Canvas3D)get3dcanvas(),new BoundingSphere());
        pickDrag.setEnable(false);
        getsceneRoot().addChild(pickDrag);

        
         
        if (vGonioFileName!=null) {
        	LoadVirtualGonio(vGonioFileName);
        }

        // Create a new Behavior object that will perform the collision
        // detection on the specified Bounding Box with is
        // the same as shape box
        Vector actcolldev = new Vector(0);
        
        setupCollisionDetection();
       
        //java.util.Enumeration lights=canvas.getGraphicsContext3D().getAllLights();
        //while(canvas.getGraphicsContext3D().numLights()>0) {
        //	canvas.getGraphicsContext3D().removeLight(0);
        //}
        //sceneRoot.
        //DisableLighting(sceneRoot);
        
        finalizeScene();
        
        if (fr) {
        	boolean packFrame = false;

        	frame = new MainFrame();

        	frame.setTitle("VBS: Virtual Beamline Simulator");
        	frame.setSize(new Dimension(1000, 700));

        	frame.add(this);

        	//Validate frames that have preset sizes
        	//Pack frames that have useful preferred size info, e.g. from their layout
        	if (packFrame) {
        		frame.pack();
        	}
        	else {
        		frame.validate();
        	}
        	//Center the window
        	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        	Dimension frameSize = frame.getSize();
        	if (frameSize.height > screenSize.height) {
        		frameSize.height = screenSize.height;
        	}
        	if (frameSize.width > screenSize.width) {
        		frameSize.width = screenSize.width;
        	}
        	frame.setLocation( (screenSize.width - frameSize.width) / 2,
        			(screenSize.height - frameSize.height) / 2);
        	frame.setVisible(true);

        }

    }
    
    /**
     * maybe buggy as the MyTransformGroup.maintrafoGroup should be passed over after 
     * @param modification
     * @param absolute
     */
	  public void DoOperation(String motorName,double modification,boolean absolute) {
		  DoOperation(pickDrag.currentTG,motorName,modification,absolute);
  	  }
    

    public void LoadVirtualGonio(String fname){

      	String corr;
      	Util utils=new Util();
        //read spec.dat
      	corr = utils.opReadCl(fname);
        String tmp1[] = corr.split("SECTION");
    	ParamTable vGonioDescriptor= new ParamTable();

    	read_section(tmp1,"virtual_goniometer",vGonioDescriptor);
    	
    	Vector rootDevices=vGonioDescriptor.getValueList("holds");
    	for(int i=0;rootDevices!=null && i<rootDevices.size();i++) {
    		LoadDevice(tmp1,(String)rootDevices.elementAt(i));
    	}
    	setMotorGUI(this);
    }
    
    
    //----------------------------------------------------------
    // Methods local to loader
    //----------------------------------------------------------


    //----------------------------------------------------------
    // Implmentation of base class abstract methods
    //----------------------------------------------------------

    /**
     * Go to the named URL location. No checking is done other than to make
     * sure it is a valid URL.
     *
     * @param url The URL to open
     */
    public void gotoLocation(URL url) {
        getmainCanvas().loadWorld(url.toString());
    }

    /**
     * Load the named file. The file is checked to make sure that it exists
     * before calling this method.
     *
     * @param file The file to load
     */
    public void gotoLocation(File file) {
        try {
            gotoLocation(file.toURL());
        } catch(MalformedURLException mue) {
            setstatusLabelText(mue.getMessage());
            //console.errorReport(mue.getMessage(), mue);
        }
    }

    public void setWarning(String msg) {
    	setstatusLabelText(msg);
        //console.warningReport(msg, null);
    }

    public void setError(String msg) {
    	setstatusLabelText(msg);
        //console.errorReport(msg, null);
    }





    class MainFrame extends JFrame {
    	boolean disposed = false;
    	/**
    	 * Overridden so we can exit when window is closed
    	 *
    	 * @param e WindowEvent
    	 */
    	protected void processWindowEvent(WindowEvent e) {
    		super.processWindowEvent(e);
    		if (e.getID() == WindowEvent.WINDOW_CLOSING && !disposed) {
    			System.exit(0);
    		}
    	}
    	
    }

    public MainFrame frame;


    public void close () {
    	if (frame!=null) {
    		frame.disposed=true;
    		frame.dispose();
    	}
    }
    
    
    /**
     * A main body for running as an application
     *
     * @param args The arugment array
     */
    public static void main(String[] args) {

        String locString = null;
        Util util= new Util();

        if (args.length == 0) {
            Stac_Out.println("No file to display");
            Stac_Out.println("Usage: java vBCM [-server] pathname | URL");
            System.exit(0);
        }

        boolean serverLoad = args[0].equals("-server");

        if(((args.length == 1) && serverLoad) ||
           ((args.length == 2) && !serverLoad)) {

            Stac_Out.println("No file to display");
            Stac_Out.println("Usage: java vBCM [-server] pathname | URL");
            System.exit(0);
        }

        String filename = (args.length == 1) ? args[0] : args[1];

        vBCM vbcm = new vBCM(util.getConfigDir()+File.separator+filename,!serverLoad);
        
        
        //demo.checkCube();
        
        /*
        for (int i=0;i<args.length;i++) {
        //for (int i=0;i<2;i++) {
            demo.loadFile(args[i],demo.examineGroup);
        }
        */
        
        //activateUniverse(vbcm);
        vbcm.activateVBCM(vbcm);
        
        
    }
    
    public final void activateVBCM(vBCM vbcm) {
        
        super.activateUniverse(vbcm);
        
    }
    
    class GonioAxisComparator implements java.util.Comparator<GonioAxis> {
        public int compare( GonioAxis a1, GonioAxis a2 ) {
    	try {
    	    return a1.getAxisSerial().compareTo(a2.getAxisSerial());
    	}
    	catch (memops.general.ApiException e) {
    	    throw new java.lang.RuntimeException(e);
    	}
        }
    }

    class GonioCalibratedAxisComparator implements java.util.Comparator<GonioCalibratedAxis> {
        public int compare( GonioCalibratedAxis a1, GonioCalibratedAxis a2 ) {
    	try {
    	    return new GonioAxisComparator().compare(a1.getGonioAxis(), a2.getGonioAxis());
    	}
    	catch (memops.general.ApiException e) {
    	    throw new java.lang.RuntimeException(e);
    	}
        }
    }

    public void CenterNeedle(){
    	centerCalibPeakXYZPos();
    }
    
    
	public void generateCCPNCollisionMap(String fp,Double [] dtox, double res) {
		//int ostep=10,kstep=10,pstep=10;
		Angle[] olimits=null;
		Angle[] klimits=null;
		Angle[] plimits=null;
		try {
			Vector lim = getMotorLimits("Omega");			
			olimits = new Angle [] {new Angle(((Double)lim.elementAt(0)).doubleValue()), new Angle(((Double)lim.elementAt(1)).doubleValue()) };
		} catch (Exception e1) {
			try {
				olimits = new Angle [] {new Angle(0.0), new Angle(0.0) };
			} catch (ApiException e) {
			}
		}
		try {
			Vector lim = getMotorLimits("Kappa");			
			klimits = new Angle [] {new Angle(((Double)lim.elementAt(0)).doubleValue()), new Angle(((Double)lim.elementAt(1)).doubleValue()) };
		} catch (Exception e1) {
			try {
				klimits = new Angle [] {new Angle(0.0), new Angle(0.0) };
			} catch (ApiException e) {
			}
		}
		try {
			Vector lim = getMotorLimits("Phi");			
			plimits = new Angle [] {new Angle(((Double)lim.elementAt(0)).doubleValue()), new Angle(((Double)lim.elementAt(1)).doubleValue()) };
		} catch (Exception e1) {
			try {
				plimits = new Angle [] {new Angle(0.0), new Angle(0.0) };
			} catch (ApiException e) {
			}
		}
		//double res=10.0;
		double [] ostep=null;
		try {
			ostep= new double [(int)((olimits[1].getValueDegrees()-olimits[0].getValueDegrees())/res)];
			for(int o=0;o<ostep.length;o++)
				ostep[o]=olimits[0].getValueDegrees()+o*res;
		} catch (ApiException e1) {
		}
		double [] kstep=null;
		try {
			kstep= new double [(int)((klimits[1].getValueDegrees()-klimits[0].getValueDegrees())/res)];
			for(int o=0;o<kstep.length;o++)
				kstep[o]=klimits[0].getValueDegrees()+o*res;
		} catch (ApiException e1) {
		}
		double [] pstep=null;
		try {
			pstep= new double [(int)((plimits[1].getValueDegrees()-plimits[0].getValueDegrees())/res)];
			for(int o=0;o<pstep.length;o++)
				pstep[o]=plimits[0].getValueDegrees()+o*res;
		} catch (ApiException e1) {
		}
		//as for kappa gonios
		Vector collmaps=generateCollisionMap(
				"Detector",dtox,
				//"Detector",new Double [] {new Double(0.05)},
				"Omega",ostep,
				"Kappa",kstep,
				"Phi",pstep);
		
//		//new, generic way:
//		ParamTable pt= new ParamTable();
//		pt.pnames.addElement("Detector");pt.pvalues.addElement(new double[]{0.05});            
//		pt.pnames.addElement("Omega");pt.pvalues.addElement(ostep);            
//		pt.pnames.addElement("Kappa");pt.pvalues.addElement(kstep);            
//		//pt.pnames.addElement("Phi");pt.pvalues.addElement(pstep);            		
//		byte[] newmap= generateCollisionMap(pt);
//
//		System.out.println("Collision map for distance ");
//		int mct=0;
//		for (int o=0;o<ostep.length;o++){
//			for (int k=0;k<kstep.length;k++){
//				    if (((byte [])collmaps.elementAt(0+1))[mct]!=newmap[mct]) {
//				    	System.out.print("!"+((byte [])collmaps.elementAt(0+1))[mct]);
//				    } else {
//				    	System.out.print("  ");
//				    }				    	
//				    System.out.print(newmap[mct++]);
//			}
//			System.out.println();
//		}
//		System.out.println("End of Collision map for distance ");
		
		
//		Vector collmaps=new Vector(0);
//		Random rnd = new Random();
//		Double [] res= new Double [] {new Double(0.05),new Double(0.08)};
//		byte [] map1 ;
//		for (int i=0,mapct=0;i<res.length;i++,mapct=0) {
//			map1 = new byte[360/ostep*270/kstep*360/pstep];
//			for (int o=0;o<360;o+=ostep){
//				for (int k=0;k<270;k+=kstep){
//				    //map[mapct++]=(rnd.nextFloat()<0.5)?(byte)1:(byte)0;
//				    map1[mapct++]=(o<180 && k<180)?(byte)1:(byte)0;
//				}
//			}
//			System.out.println("Collision map for distance "+res[i]);
//			mapct=0;
//			for (int o=0;o<360;o+=ostep){
//				for (int k=0;k<270;k+=kstep){
//					    System.out.print(" "+map1[mapct++]);
//				}
//				System.out.println();
//			}
//			System.out.println("End of Collision map for distance "+res[i]);
//			collmaps.addElement(res[i]);
//			collmaps.addElement(map1);
//		}
		

	    try {
			// MemopsRoot has no parent, so always have to use the
			// Java-style constructor. This is method (1b)
			MemopsRoot root = new MemopsRoot("collisiontest");

			// CCPN-style constructor. This is method (1a)
			BeamLine beamLine = root.newBeamLine("STAC-Station");
			System.out.println(beamLine);

			// Method (1a) again.
			Goniostat gonio = root.newGoniostat(2, "STAC-VBS", vbsName);
			System.out.println(gonio);

			// This is a more complicated example of method (1b). Here
			// the instances of complex type attributes are
			// constructed in-place. This makes the code hard to read
			// at first sight, but the typing of attributes with
			// multiplicities > 1 is made very explicit here.
			FixedFrame frameCam  = new FixedFrame(gonio,                                                                   // goniostat (parent)
					new OrthogonalMatrix( Arrays.asList(new UnitVector[] { new UnitVector(Arrays.asList(new java.lang.Double[] {1.0, 0.0, 0.0}) ),
							new UnitVector(Arrays.asList(new java.lang.Double[] {0.0, 1.0, 0.0}) ),
							new UnitVector(Arrays.asList(new java.lang.Double[] {0.0, 0.0, 1.0}) ) }
					)
					),                                                 // convertFromMeta
					new UnitVector( Arrays.asList(new java.lang.Double[] {1.0, 0.0, 0.0}) ), // incidentBeamDirection
					new UnitVector( Arrays.asList(new java.lang.Double[] {0.0, 0.0, 1.0}) ), // principalAxisDirection
					"cam"                                                                    // shortName
			);

			// Now set optional attributes.
			frameCam.setName("Cambridge Laboratory Frame");
			frameCam.setDescription("X-axis is along the source beam direction; it points from the crystal to the detector\n" +
					"Z-axis is parallel to the rotation axis\n" + 
			"Y-axis completes a right handed coordinate system\n");

			System.out.println(frameCam);
			
			FixedFrame frame  = new FixedFrame(gonio,                                                                   // goniostat (parent)
					new OrthogonalMatrix( Arrays.asList(new UnitVector[] { 
							new UnitVector(Arrays.asList(new java.lang.Double[] {0.0, 0.0,-1.0}) ),
							new UnitVector(Arrays.asList(new java.lang.Double[] {0.0, 1.0, 0.0}) ),
							new UnitVector(Arrays.asList(new java.lang.Double[] {1.0, 0.0, 0.0}) ) }
					)
					),                                                 // convertFromMeta
					new UnitVector( Arrays.asList(new java.lang.Double[] {0.0, 0.0, 1.0}) ), // incidentBeamDirection
					new UnitVector( Arrays.asList(new java.lang.Double[] {-1.0, 0.0, 0.0}) ), // principalAxisDirection
					"vbs"                                                                    // shortName
			);

			//Now set optional attributes.
			frame.setName("Kappa Workgroup - Virtual Beamline Simulator Frame");
			frame.setDescription("Z-axis is along the source beam direction; it points from the crystal to the detector\n" +
					"X-axis is in the horizontal plane pointing to outer wall\n" + 
			"Y-axis completes a right handed coordinate system\n");

			System.out.println(frame);

			// Now an example of method (2a)
			//
			// First, create and populate the Map....
			java.util.Map<java.lang.String, java.lang.Object> configAttributes = 
			new java.util.Hashtable<java.lang.String, java.lang.Object>();
			configAttributes.put(GonioConfig.INSTALLATIONDATE, (new java.util.Date()).toString());
			configAttributes.put(GonioConfig.BEAMLINE, beamLine);
			configAttributes.put(GonioConfig.DEFAULTFRAME, frame);

			// .... now construct the new object.
			// CCPN-style constructor
			GonioConfig config = gonio.newGonioConfig( configAttributes );
			System.out.println(config);

			// Construct configuration, Axes and directions for this configuration.
			GonioCalibration calibration = config.newGonioCalibration( (new java.util.Date()).toString(), "me");
			config.setCurrentGonioCalibration(calibration);
			System.out.println(calibration);

			String [] motors=getMotorList();
			for (int i=0,rot=0;i<motors.length;i++) {
				if (isMotorRotational(motors[i])) {
			    	if (motors[i].equalsIgnoreCase("omega")) {
			    		rot++;
			    	}
			    	if (rot>0) {
			    	    ParamTable params=getMotorData(motors[i]);
			    	    
			    	    GonioAxis tmpaxis = config.newGonioAxis(rot, true, motors[i]);
			    	    // GonioCalibratedAxis is the bottom of the tree, so don't
			    	    // need to hold it in a variable for use in a constructor
			    	    // for a child object later on.
			    	    double [] axis = params.getDoubleVector("CalibratedAxisDirection");
			    	    Vector3d ax=new Vector3d(axis);
			    	    ax.normalize();
			    	    tmpaxis.newGonioCalibratedAxis( new UnitVector( Arrays.asList(new Double [] {new Double(ax.x),
	  	  	    				new Double(ax.y),
	  	  	    				new Double(ax.z)}) ),
			    					  calibration);
			    	    
			    	}
					
				}
			}
			
			// Work out rotation matrix corresponding to a setting of
			// ( omega = 50, kappa = 10, phi = 30 ) Here we use
			// knowledge about what the axes are called to decide how
			// to multiply the matrices corresponding to the axes.
//	    OrientationMatrix mOmega = config.findFirstGonioAxis(GonioAxis.NAME, "omega").getRotMat ( new Angle(50.0) );
//	    OrientationMatrix mKappa = config.findFirstGonioAxis(GonioAxis.NAME, "kappa").getRotMat ( new Angle(10.0) );
//	    OrientationMatrix mPhi   = config.findFirstGonioAxis(GonioAxis.NAME, "phi"  ).getRotMat ( new Angle(30.0) );

			// Is this in the right order?
//	    OrientationMatrix m = mOmega.postMultM( mKappa.postMultM(mPhi) );

//	    System.out.println("\nRotation matrix is: \n" +m);

			// Do this again, but without any a priori knowledge about
			// how the axes are named, or even how many there are. If
			// we made axisSerial the key of the GonioAxis class
			// (rather than name), the CCPN-generated method
			// GonioConfg.sortedGonioAxes would return the axes in the
			// order we want automatically. However, here we sort
			// explicitly by axisSerial, for a completely general
			// approach.
//	    java.util.List<GonioAxis> sortedAxes = new java.util.ArrayList(config.findAllGonioAxes());
//	    java.util.Collections.sort(sortedAxes, new GonioAxisComparator() );

//	    Angle settings[] = { new Angle(50.0), new Angle(10.0), new Angle(30.0) };
//	    m = sortedAxes.get(0).getRotMat(settings[0]);
//	    for ( int i = 1; i < settings.length; i++ ) {
//		GonioAxis a = sortedAxes.get(i);
//		m = m.postMultM( a.getRotMat(settings[i]) );
//	    }

			// Should be the same matrix as above.
//	    System.out.println("\nIs this matrix the same as the previous one?\n" + m);

			
			//
			CollisionMapSet mapset = calibration.newCollisionMapSet();
		    java.util.List<GonioCalibratedAxis> sortedAxes = new java.util.ArrayList(calibration.getGonioCalibratedAxes());
		    java.util.Collections.sort(sortedAxes, new GonioCalibratedAxisComparator() );
		    calibration.getGonioCalibratedAxes();
		    for ( int i = 0; i < sortedAxes.size(); i++ ) {
		    	double step=0;
		    	Angle [] limits=null;
		    	if (i==0) {
		    		step=res;
		    		limits=olimits;
		    	} else if (i==1) {
		    		step=res;
		    		limits=klimits;
		    	} else {
		    		step=res;
		    		limits=plimits;
		    	}  
		    	CollisionMapAxis taxis = mapset.newCollisionMapAxis(new Integer(i+1), Arrays.asList(limits),new Angle(step),sortedAxes.get(i));
		    }
			
		    for (int i=0;i<collmaps.size();i+=2) {
				java.util.Map<java.lang.String, java.lang.Object> cmap = new java.util.Hashtable<java.lang.String, java.lang.Object>();
				cmap.put(CollisionMap.DETECTORDISTANCE , new Float ((Double)collmaps.elementAt(i)));
		    	CollisionMap map= calibration.newCollisionMap(cmap);
		    	Set<CollisionMapSet> actmapsets=map.getCollisionMapSets();
		    	actmapsets.add(mapset);
		    	map.setCollisionMapSets( actmapsets);
		    	
		    	//map.setDetectorDistance(((Double)collmaps.elementAt(i)).floatValue());
		    	
		    	byte[] actmap=(byte[])collmaps.elementAt(i+1);
				for (int o=0,mapct=0;o<ostep.length;o++){
					for (int k=0;k<kstep.length;k++){
						java.util.Map<java.lang.String, java.lang.Object> mpoint = new java.util.Hashtable<java.lang.String, java.lang.Object>();
						mpoint.put(CollisionMapPoint.POSITION , new GenVector (Arrays.asList(new Double[] {new Double(ostep[o]),new Double(kstep[k])})));
						//mpoint.put(CollisionMapPoint.MINORAXISLIMITS , Arrays.asList(new Angle [] {new Angle(0.0),new Angle(0.0)}));
						mpoint.put(CollisionMapPoint.AVAILABILITY , actmap[mapct++]==1?"F":"U");
							
						CollisionMapPoint mp=map.newCollisionMapPoint(mpoint);
					}
				}
		    	
		    }
			
			
			
			// Set up repository to save data to specified location
			Repository repository = root.findFirstRepository(Repository.NAME, "userData");
			repository.setUrl( new memops.api.Implementation.Url(fp));
			
			// Save project root
			//root.saveTo(repository);

			// Save all other data 
			//java.util.Set<memops.api.Implementation.TopObject> topObjects = root.findAllTopObjects();
			//for ( memops.api.Implementation.TopObject to: topObjects ) {
			//to.saveTo(repository);
			//}

			//gonio.saveTo(repository);
			//beamLine.saveTo(repository);

			root.saveAll();
			
			System.out.println("Collision Map Generation Finished");
			
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		
		
	}
    
	/**
	 * TC space:
	 *  - X : 1 0 0
	 *  - Y : 0 1 0
	 *  - Z : 0 0 1
	 *  - kappa/phi Dir/Loc : measured
	 * RC space:
	 *  - beam  : 1 0 0
	 *  - omega ~?
	 *  - detX  :
	 *  - detY  :
	 *  - omega/kappa/phi Dir : measured
	 *  
	 * RC - TC assumptions:
	 *  - Z is parallel to Omega (not used!!!)
	 * RC - Realworld assumptions:
	 *  - detX is horizontal
	 *  - detY is vertical
	 * TC - RealWorld assumptions:
	 *  - X/Y/Z motors define a normalised and orthogonalised coordinate frame
	 *  
	 * @param Descriptor
	 */
	public void adjustCalibration(ParamTable Descriptor) {
		Transform3D TCorth = new Transform3D();
		Transform3D TCnorm = new Transform3D();
		Transform3D Mtc_rc = new Transform3D();
		Transform3D Mrc_vbs = new Transform3D();
		// note that motor order is used later
		String [] STACmotors = new String [] {"Kappa","Phi","Omega","X","Y","Z"};
		String [] VBSmotors = new String [] {"","","","","",""};
		int [] VBSmotorOrder = new int [] { -1, -1, -1, -1 , -1, -1 };
		Vector3d [] TCDir = new Vector3d [] {
				new Vector3d(Descriptor.getDoubleVector("KappaTransD")),
				new Vector3d(Descriptor.getDoubleVector("PhiTransD")),
				null,
				new Vector3d(1,0,0),
				new Vector3d(0,1,0),
				new Vector3d(0,0,1)
		};
		Vector3d [] RCDir = new Vector3d [] {
				new Vector3d(Descriptor.getDoubleVector("KappaRot")),
				new Vector3d(Descriptor.getDoubleVector("PhiRot")),
				new Vector3d(Descriptor.getDoubleVector("OmegaRot")),
				null,
				null,
				null
		};
		
		//define TC orthogonalisation matrix and apply
		{
			TCorth.setIdentity();
			for(int i=0;i<TCDir.length;i++){
				try {
					TCorth.transform(TCDir[i]);
				} catch (RuntimeException e) {
				}
			}
		}
		//define TC normalisation matrix and apply
		{
			TCnorm.setIdentity();
			for(int i=0;i<TCDir.length;i++){
				try {
					TCnorm.transform(TCDir[i]);
				} catch (RuntimeException e) {
				}
			}
		}
		//calc M(tcNorm->rc)
		{
			//RC matrix
			Vector3d rcCross = new Vector3d();
			rcCross.cross(RCDir[0], RCDir[1]);
			Transform3D rc = new Transform3D( new double [] {
					RCDir[0].x,RCDir[1].x,rcCross.x,0 ,
					RCDir[0].y,RCDir[1].y,rcCross.y,0 ,
					RCDir[0].z,RCDir[1].z,rcCross.z,0 ,
					0,0,0,1
			});
			//normalised TC matrix
			Vector3d tcCross = new Vector3d();
			tcCross.cross(TCDir[0], TCDir[1]);
			Transform3D tc = new Transform3D( new double [] {
					TCDir[0].x,TCDir[1].x,tcCross.x,0 ,
					TCDir[0].y,TCDir[1].y,tcCross.y,0 ,
					TCDir[0].z,TCDir[1].z,tcCross.z,0 ,
					0,0,0,1
			});
			//M(tcNorm->rc)
			Transform3D tc_inv = new Transform3D(tc);
			tc_inv.invert();
			Mtc_rc.mul(rc, tc_inv);
			//apply 
			for(int i=0;i<TCDir.length;i++){
				try {
					Mtc_rc.transform(TCDir[i]);
				} catch (RuntimeException e) {
				}
			}
		}
		//define M(rc->vbs)
		//TODO if the model is not in IDEAL VBS space, that should be taken into account!!!
		{
			Mrc_vbs.set(new double [] {0,0,-1,0, 0,1,0,0, 1,0,0,0, 0,0,0,1});
		}
		//transform TC assumed vectors (TCxDir/TCyDir/TCzDir) to VBS space
		//transform TC measured vectors (TCkappaDir/TCphiDir/TCkappaLoc?/TCphiLoc?) to VBS space
		{
			for(int i=0;i<TCDir.length;i++){
				try {
					Mrc_vbs.transform(TCDir[i]);
				} catch (RuntimeException e) {
				}
			}
		}
		//transform RC assumed vectors (beam/?detX) to VBS space
		//transform RC measured vectors (RComegaDir/RCkappaDir/RCphiDir) to VBS space
		{
			for(int i=0;i<RCDir.length;i++){
				try {
					Mrc_vbs.transform(RCDir[i]);
				} catch (RuntimeException e) {
				}
			}
		}
		//comapre kappa/phi vectors from TC-RC to indicate overall precision (with all the assumptions)
		{
			
		}
		//compare TCx,y,z to beam etc. to indicate beamline alignment
		{
			Stac_Out.println("TransCalib transformed to VBS space");
			for(int i=0;i<TCDir.length;i++){
				try {
					Stac_Out.println("   "+STACmotors[i]+": "+TCDir[i]);
				} catch (RuntimeException e) {
				}
			}
			Stac_Out.println("RotCalib transformed to VBS space");
			for(int i=0;i<RCDir.length;i++){
				try {
					Stac_Out.println("   "+STACmotors[i]+": "+RCDir[i]);
				} catch (RuntimeException e) {
				}
			}
		}
		//adjust the instrument
		{
			//get the list of motors to be adjusted
			for (int i=0;i<STACmotors.length;i++)
				VBSmotors[i]=Descriptor.getFirstStringValue(STACmotors[i]);
			//sort the motors (parent child hierarchy - the same as loading order!)			
			String [] motorlist = getMotorList();
			for (int i=0,act=0;i<motorlist.length;i++)
				for (int j=0;j<VBSmotors.length;j++)
					if (motorlist[i].equals(VBSmotors[j])) {
						VBSmotorOrder[act]=j;
						act++;
						if (act==VBSmotors.length) {
							j=VBSmotors.length;
							i=motorlist.length;
						}
						break;
					}			
			//foreach motor in list take ZEROPOS into account for CalibrateVia device
			for (int i=0;i<VBSmotorOrder.length;i++)
				if (VBSmotorOrder[i]==-1)
				{
					//do not do anything
				} else {
					//get available new calibrated zerodirection
					Vector3d newDir=((TCDir[VBSmotorOrder[i]]!=null)?(TCDir[VBSmotorOrder[i]]):(RCDir[VBSmotorOrder[i]]));
					if (newDir!=null)
						performDeviceCalibration(VBSmotors[VBSmotorOrder[i]],newDir);					
				}
		}
	}

    
    
}
