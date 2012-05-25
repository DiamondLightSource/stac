package stac.gui;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.modules.BCM.*;
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

import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


class StacOrientRespHandler extends Stac_RespHandler {
	STAC_OrientationPanel mainWin=null;

	StacOrientRespHandler(STAC_OrientationPanel mainWin) {
		this.mainWin=mainWin;
	}
	
	
	@Override
	protected void handleResponse(Stac_RespMsg response) {
		if (response instanceof Stac_OptStrategyOrientRespMsg)
		{
	    	Stac_OptStrategyOrientRespMsg resp=(Stac_OptStrategyOrientRespMsg) response;
	    	
			if (resp.status==resp.OK) {
		    	Vector OM = resp.get_optAlign();
		    	mainWin.addOrientationRequest(OM);
			} else {
				resp.session.addErrorMsg(response.requestId,"Response status = "+response.statusToString());
			}
		}		
		
	}


}





public class STAC_OrientationPanel extends JPanel 
implements STAC_GUI_File_Chooser_Panel, STAC_GUI_Button_Panel, STAC_GUI_Widget {
	
	Util utils;
	StacBCM bcm;
	StacMainWin stacmainwin;
	STAC_GUI_Button_Panel remotelistener;
	
	Dimension rbSize = new Dimension(150,30);
	Dimension rbdSize = new Dimension(300,30);
	Dimension fsbSize = new Dimension(30,30);
	Dimension tfSize = new Dimension(400,30);
	Dimension tfnSize = new Dimension(10,30);
	
	
	JButton jButtonRLeftAdd = new JButton("Add New");
	JButton jButtonRLeftDelete = new JButton("Del Selected");
	public JButton jButtonOptAlign = new JButton("Smallest Overall Oscillation");
	public JButton jButtonSSS = new JButton("Smart Spot Separation");
	public JButton jButtonSAD = new JButton("Anomalous Data Collection");
	public JButton jButtonSTD = new JButton("Standard Cell Alignment");
	JTextField jTextFieldRGF=new JTextField();  
	JButton jButtonRGF = new JButton("",createImageIcon("images/folder.gif"));
	JButton jButtonRLeftGetFile = new JButton("Multi-crystal Reference:");
	ReorientTableModel reorientTableModel = new ReorientTableModel();
	public STAC_GUI_JTable jTableReorient = new STAC_GUI_JTable(reorientTableModel);
	JPanel jpanelRight = new JPanel();
	JScrollPane jpanelReorientTable = new JScrollPane(jTableReorient);
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = STAC_OrientationPanel.class.getResource(path);
		if (imgURL != null) {
			ImageIcon ic = new ImageIcon(imgURL);
			ic.getImage();
			ic.getIconHeight();
			ic.getIconWidth();
//			Map<Thread, StackTraceElement []> threads=Thread.getAllStackTraces();
//			Thread [] thr = threads.keySet().toArray(new Thread [] {null});
//			for (int i=0;i<thr.length;i++){
//				if (thr[i].getName().startsWith("Image Fetcher")) {//thr[i] instanceof ImageFetcher){
//					try {
//						thr[i].wait(5000);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						//e.printStackTrace();
//					}
//				}
//			}
			//System.out.println(ic.getIconHeight()+" - "+ic.getIconWidth());
			return ic;
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
	public STAC_OrientationPanel(Util utils,StacBCM bcm){
		init(utils,bcm,false,this,null);
	}
	
	public STAC_OrientationPanel(Util utils,StacBCM bcm,STAC_GUI_Button_Panel listener){
		init(utils,bcm,false,listener,null);
	}
	
	public STAC_OrientationPanel(StacBCM bcm,STAC_GUI_Button_Panel listener){
		init(null,bcm,false,listener,null);
	}
	
	public STAC_OrientationPanel(Util utils,StacBCM bcm,STAC_GUI_Button_Panel listener,Stac_Configuration config){
		init(utils,bcm,false,listener,config);
	}
	
//	public STAC_OrientationPanel(StacUtil utils,StacBCM bcm,StacMainWin StacMainWin){
//		this.stacmainwin=StacMainWin;
//		init(utils,bcm,true,this);
//	}
	
	
	public void init(Util utils,StacBCM bcm,boolean stand_alone,STAC_GUI_Button_Panel listener,Stac_Configuration config){
		if (utils!= null)
			this.utils=utils;
		else 
			this.utils=new Util();
		this.bcm=bcm;
	
		this.remotelistener=listener;
		
		this.setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.LINE_AXIS));
		this.add(Box.createRigidArea(new Dimension(10,10)));
		//Left
		JPanel jPanelRLeft = new JPanel();
		this.add(jPanelRLeft);
		jPanelRLeft.setLayout(new BoxLayoutFixed(jPanelRLeft,BoxLayoutFixed.PAGE_AXIS));
		JLabel jLabelRLTitle=new JLabel("Reorientations:");
		jPanelRLeft.add(jLabelRLTitle, null);
		jLabelRLTitle.setMinimumSize(rbSize);
		jLabelRLTitle.setMaximumSize(rbSize);
		jLabelRLTitle.setPreferredSize(rbSize);
		JPanel jPanelRLeftAD = new JPanel();
		jPanelRLeft.add(jPanelRLeftAD, null);
		jPanelRLeftAD.setLayout(new BoxLayoutFixed(jPanelRLeftAD,BoxLayoutFixed.LINE_AXIS));
		jPanelRLeftAD.add(Box.createHorizontalGlue());
		jPanelRLeftAD.add(jButtonRLeftAdd);
		jButtonRLeftAdd.setMinimumSize(rbSize);
		jButtonRLeftAdd.setMaximumSize(rbSize);
		jButtonRLeftAdd.setPreferredSize(rbSize);
		jButtonRLeftAdd.setToolTipText("Add a new entry to the Reorientation list");
		jButtonRLeftAdd.addActionListener(new Stac_Button_actionAdapter(this));
		jPanelRLeftAD.add(jButtonRLeftDelete);
		jButtonRLeftDelete.setMinimumSize(rbSize);
		jButtonRLeftDelete.setMaximumSize(rbSize);
		jButtonRLeftDelete.setPreferredSize(rbSize);
		jButtonRLeftDelete.setToolTipText("Remove the selected entries from the Reorientation list");
		jButtonRLeftDelete.addActionListener(new Stac_Button_actionAdapter(this));
		jPanelRLeftAD.add(Box.createHorizontalGlue());
		//cell alignment
		JPanel jPanelRLeftSTD= new JPanel();    
		jPanelRLeft.add(jPanelRLeftSTD);
		jPanelRLeftSTD.setLayout(new BoxLayoutFixed(jPanelRLeftSTD,BoxLayoutFixed.LINE_AXIS));
		jPanelRLeftSTD.add(Box.createHorizontalGlue());
		jPanelRLeftSTD.add(jButtonSTD);
		jButtonSTD.setMinimumSize(rbdSize);
		jButtonSTD.setMaximumSize(rbdSize);
		jButtonSTD.setPreferredSize(rbdSize);
		jButtonSTD.setToolTipText("Cell Alignment to produce specific diffraction pattern");
		jButtonSTD.addActionListener(new Stac_Button_actionAdapter(this));
		jPanelRLeftSTD.add(Box.createHorizontalGlue());
		//anomalous data collection
		JPanel jPanelRLeftSAD= new JPanel();    
		jPanelRLeft.add(jPanelRLeftSAD);
		jPanelRLeftSAD.setLayout(new BoxLayoutFixed(jPanelRLeftSAD,BoxLayoutFixed.LINE_AXIS));
		jPanelRLeftSAD.add(Box.createHorizontalGlue());
		jPanelRLeftSAD.add(jButtonSAD);
		jButtonSAD.setMinimumSize(rbdSize);
		jButtonSAD.setMaximumSize(rbdSize);
		jButtonSAD.setPreferredSize(rbdSize);
		jButtonSAD.setToolTipText("Bringing Bijvoet pairs to the same image to minimise systematic errors of Radiation Damage while measuring the Anomalous signal");
		jButtonSAD.addActionListener(new Stac_Button_actionAdapter(this));
		jPanelRLeftSAD.add(Box.createHorizontalGlue());
		//smart sport separation
		JPanel jPanelRLeftSSS= new JPanel();    
		jPanelRLeft.add(jPanelRLeftSSS);
		jPanelRLeftSSS.setLayout(new BoxLayoutFixed(jPanelRLeftSSS,BoxLayoutFixed.LINE_AXIS));
		jPanelRLeftSSS.add(Box.createHorizontalGlue());
		jPanelRLeftSSS.add(jButtonSSS);
		jButtonSSS.setMinimumSize(rbdSize);
		jButtonSSS.setMaximumSize(rbdSize);
		jButtonSSS.setPreferredSize(rbdSize);
		jButtonSSS.setToolTipText("Maximum spot separarion without blind zone");
		jButtonSSS.addActionListener(new Stac_Button_actionAdapter(remotelistener));
		jPanelRLeftSSS.add(Box.createHorizontalGlue());
		//
		JPanel jPanelRLeftOA= new JPanel();    
		jPanelRLeft.add(jPanelRLeftOA);
		jPanelRLeftOA.setLayout(new BoxLayoutFixed(jPanelRLeftOA,BoxLayoutFixed.LINE_AXIS));
		jPanelRLeftOA.add(Box.createHorizontalGlue());
		jPanelRLeftOA.add(jButtonOptAlign);
		jButtonOptAlign.setMinimumSize(rbdSize);
		jButtonOptAlign.setMaximumSize(rbdSize);
		jButtonOptAlign.setPreferredSize(rbdSize);
		jButtonOptAlign.setToolTipText("Add the optimal orientation for smallest oscillation range");
		jButtonOptAlign.addActionListener(new Stac_Button_actionAdapter(remotelistener));
		//jButtonOptAlign.setEnabled(stand_alone);
		jPanelRLeftOA.add(Box.createHorizontalGlue());
		//
		JPanel jPanelRLeftGetFile= new JPanel();    
		jPanelRLeft.add(jPanelRLeftGetFile);
		jPanelRLeftGetFile.setLayout(new BoxLayoutFixed(jPanelRLeftGetFile,BoxLayoutFixed.LINE_AXIS));
		jPanelRLeftGetFile.add(Box.createHorizontalGlue());
		jPanelRLeftGetFile.add(jButtonRLeftGetFile);
		jButtonRLeftGetFile.setMinimumSize(rbdSize);
		jButtonRLeftGetFile.setMaximumSize(rbdSize);
		jButtonRLeftGetFile.setPreferredSize(rbdSize);
		jButtonRLeftGetFile.setToolTipText("Reference orienation of a previous crystal as in the file specified");
		jButtonRLeftGetFile.addActionListener(new Stac_Button_actionAdapter(this));
		jPanelRLeftGetFile.add(Box.createHorizontalGlue());
		JPanel jPanelRLeftGF=new JPanel();
		jPanelRLeft.add(jPanelRLeftGF, null);
		jPanelRLeftGF.setLayout(new BoxLayoutFixed(jPanelRLeftGF,BoxLayoutFixed.LINE_AXIS));
		jPanelRLeftGF.add(Box.createHorizontalGlue());
		jPanelRLeftGF.add(jTextFieldRGF, null);
		jTextFieldRGF.setToolTipText("Input Descriptor, or Matrix Definiton file for old alignment");
		jTextFieldRGF.setMinimumSize(rbdSize);//tfnSize);
		jTextFieldRGF.setMaximumSize(rbdSize);//tfSize);
		jTextFieldRGF.setPreferredSize(rbdSize);//tfSize);
		jPanelRLeftGF.add(jButtonRGF, null);
		jButtonRGF.addActionListener(new Stac_FileChooser(this));
		jButtonRGF.setMinimumSize(fsbSize);
		jButtonRGF.setMaximumSize(fsbSize);
		jButtonRGF.setPreferredSize(fsbSize);
		jButtonRGF.setToolTipText("Select the file");
		//jPanelRLeftGF.add(Box.createRigidArea(new Dimension(10,10)));
		jPanelRLeftGF.add(Box.createHorizontalGlue());
		this.add(Box.createRigidArea(new Dimension(10,10)));
		//Right
		this.add(jpanelReorientTable);
		//this.add(jpanelRight);
		//jpanelRight.setPreferredSize(new Dimension(400,400));
		//jpanelRight.add(jpanelReorientTable);
		//jpanelReorientTable.setPreferredSize(new Dimension(400,400));
		this.add(Box.createRigidArea(new Dimension(10,10)));
		this.add(Box.createHorizontalGlue());
		reorientTableModel.setTable(jTableReorient); 


      //reorientTableModel.createTableColumnModel();

      setConfig(config);

 	}
	
	public void clearTable() {
		int numOfVecs = this.jTableReorient.getRowCount();
		ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
		for(int i=numOfVecs-1;i>=0;i--){
			try {
				if (((String)(tmodel.getValueAt(i, tmodel.getColId("Comment")))).contains("_specific_")) {
					tmodel.setValueAt("ClearRow",i,i);
				}
			} catch (Exception e) {
			}
		}
	}
	
	
	
	
   File activeFileSelectionDir=null;
	/* (non-Javadoc)
	 * @see stacgui.STAC_GUI_File_Chooser_Panel#FileChooser_actionPerformed(java.awt.event.ActionEvent)
	 */
	public void FileChooser_actionPerformed(ActionEvent e) {
		//pop up a file selection window
		final JFileChooser fc = new JFileChooser();
		File file=null;
		try {
			fc.setCurrentDirectory(activeFileSelectionDir);
		}catch (Exception ex) {
		}
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		}
		if(file==null){
			return;
		}
		if (e.getSource() instanceof JButton) {
	        Stac_Out.println("USER ACTION: Button pressed (Orientation Panel - Fileselect: "+file.getAbsolutePath()+" - "+((JButton)e.getSource()).getText()+")");
	        activeFileSelectionDir=file.getParentFile();
		}
		//change the appropriate field
		if (e.getSource() == jButtonRGF) {
			jTextFieldRGF.setText(file.getAbsolutePath());
		}
		
	}

	String spg="";
	
	public void setSPG(String spg) {
		this.spg=spg;
	}
	


	/* (non-Javadoc)
	 * @see stacgui.STAC_GUI_Button_Panel#Button_actionAdapter(java.awt.event.ActionEvent)
	 */
	public void Button_actionAdapter(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
	        Stac_Out.println("USER ACTION: Button pressed (STAC Orinetation Panel: "+((JButton)e.getSource()).getText()+")");			
		}
		if (e.getSource()==this.jButtonRLeftAdd) {
	        //panel 1
	    	int rowct=this.jTableReorient.getRowCount();
	        ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
	        this.jTableReorient.setValueAt("", rowct, tmodel.getColId("V1"));
	        this.jTableReorient.setValueAt("User Value", rowct, tmodel.getColId("Comment"));
	    } else if (e.getSource()==this.jButtonRLeftDelete) {
	        //panel 1
	        int numOfVecs = this.jTableReorient.getSelectedRowCount();
	        int[] vecIDs = this.jTableReorient.getSelectedRows();
	        ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
	        for(int i=vecIDs.length-1;i>=0;i--){
	            tmodel.setValueAt("ClearRow",vecIDs[i],vecIDs[i]);
	        }
	    } else if (e.getSource()==this.jButtonSAD) {
	        //panel 1
	    	//TODO
	    	//anomalous orientations (as functin of spacegroup)
	    	String [] v1list = new String[] {"a*","b*","c*"};
	    	String spgFnd="generic";
	    	
	    	spg=spg.toUpperCase();
	    	spg=spg.replaceAll(" ", "");
	    	Integer id=null;
	    	try {
				id = (Integer) utils.getSPGlib().getNameForAnyParam(spg);
		    	v1list = (String [])utils.getSPGlib().getValueAt(id, utils.getSPGlibAnomAxes());
		    	spgFnd=spg;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	
	    	
	    	//set it up
	    	for (int i=0;i<v1list.length;i++) {
	    		int rowct=this.jTableReorient.getRowCount();
	    		ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
	    		this.jTableReorient.setValueAt(v1list[i], rowct, tmodel.getColId("V1"));
	    		this.jTableReorient.setValueAt("", rowct, tmodel.getColId("V2"));
	    		this.jTableReorient.setValueAt(false, rowct, tmodel.getColId("Close"));
	    		this.jTableReorient.setValueAt("Anomalous Data Collection ("+spgFnd+") _specific_", rowct, tmodel.getColId("Comment"));
	    	}
	    } else if (e.getSource()==this.jButtonSTD) {
	        //panel 1
	    	//TODO
	    	//anomalous orientations (as functin of spacegroup)
	    	String [] v1list = new String[] {"a*","a*","b*","b*","c*","c*"};
	    	String [] v2list = new String[] {"b*","c*","a*","c*","a*","b*"};
	    	//set it up
	    	for (int i=0;i<v1list.length;i++) {
	    		int rowct=this.jTableReorient.getRowCount();
	    		ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
	    		this.jTableReorient.setValueAt(v1list[i], rowct, tmodel.getColId("V1"));
	    		this.jTableReorient.setValueAt(v2list[i], rowct, tmodel.getColId("V2"));
	    		this.jTableReorient.setValueAt(false, rowct, tmodel.getColId("Close"));
	    		this.jTableReorient.setValueAt("Cell Alignment", rowct, tmodel.getColId("Comment"));
	    	}
	    } else if (e.getSource()==this.jButtonRLeftGetFile) {
	        //panel 1
	    	Vector OM = utils.getHKLfromOMFile(this.jTextFieldRGF.getText(),bcm);
	    	if (OM!=null && OM.size()>=2) {
	    		int rowct=this.jTableReorient.getRowCount();
	    		ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
	    		this.jTableReorient.setValueAt(OM.elementAt(0), rowct, tmodel.getColId("V1"));
	    		this.jTableReorient.setValueAt(OM.elementAt(1), rowct, tmodel.getColId("V2"));
	    		this.jTableReorient.setValueAt("Alignment as in "+this.jTextFieldRGF.getText(), rowct, tmodel.getColId("Comment"));
	    	}
	    } 
	}
	
	public void addGenericOrientationRequest(Vector list) {
    	if (list!=null && list.size()>=3) {
    		for (int ct=0;ct<list.size()/3;ct++) {
    			int rowct=this.jTableReorient.getRowCount();
    			ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
    			this.jTableReorient.setValueAt(list.elementAt(ct*3+0), rowct, tmodel.getColId("V1"));
    			this.jTableReorient.setValueAt(list.elementAt(ct*3+1), rowct, tmodel.getColId("V2"));
    			this.jTableReorient.setValueAt(list.elementAt(ct*3+2), rowct, tmodel.getColId("Comment"));
    		}
    	}
		
	}
	/**
	 * addsq an orientation request specific to the crystal mounted now!
	 * It is siged so then we will be able to remove when new crystal is mounted
	 * @param list
	 */
	public void addOrientationRequest(Vector list) {
    	if (list!=null && list.size()>=3) {
    		for (int ct=0;ct<list.size()/3;ct++) {
    			int rowct=this.jTableReorient.getRowCount();
    			ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
    			this.jTableReorient.setValueAt(list.elementAt(ct*3+0), rowct, tmodel.getColId("V1"));
    			this.jTableReorient.setValueAt(list.elementAt(ct*3+1), rowct, tmodel.getColId("V2"));
    			this.jTableReorient.setValueAt(list.elementAt(ct*3+2)+" _specific_", rowct, tmodel.getColId("Comment"));
    		}
    	}
		
	}
	
	
	public Vector getOrientationRequest() {
		
	    Vector orientations = new Vector(0);
	    int[] vecIDs = jTableReorient.getSelectedRows();
	    ReorientTableModel tmodel = (ReorientTableModel) jTableReorient.getModel();
	    tmodel.setValueAt("ActivateCells",0,0);
	    if (vecIDs.length!=0) {
	    	for (int i=0;i<vecIDs.length;i++) {
	    		Vector orient=new Vector(0);
	    		orient.addElement(jTableReorient.getValueAt(vecIDs[i],tmodel.getColId("v1")));
	    		orient.addElement(jTableReorient.getValueAt(vecIDs[i],tmodel.getColId("v2")));
	    		orient.addElement(jTableReorient.getValueAt(vecIDs[i],tmodel.getColId("Close")));
	    		orient.addElement(jTableReorient.getValueAt(vecIDs[i],tmodel.getColId("Comment")));
	    		orientations.addElement(orient);
	    	}
	    } else {
	    	for (int i=0;i<jTableReorient.getRowCount();i++) {
	    		Vector orient=new Vector(0);
	    		orient.addElement(jTableReorient.getValueAt(i,tmodel.getColId("v1")));
	    		orient.addElement(jTableReorient.getValueAt(i,tmodel.getColId("v2")));
	    		orient.addElement(jTableReorient.getValueAt(i,tmodel.getColId("Close")));
	    		orient.addElement(jTableReorient.getValueAt(i,tmodel.getColId("Comment")));
	    		orientations.addElement(orient);
	    	}    	
	    }
	    return orientations;
	}

	public void setConfig(Stac_Configuration config) {
		reorientTableModel.setConfig(config);
		
	}
	

	/*
	  void AllignmentCalculation_actionPerformed(ActionEvent e,Object responsible) {
	    //input conversion
	    responsible.ConvertInputFormat();
	    Vector orientations = new Vector(0);
	    int[] vecIDs = jPanelReorient.jTableReorient.getSelectedRows();
	    ReorientTableModel tmodel = (ReorientTableModel) jPanelReorient.jTableReorient.getModel();
	    tmodel.setValueAt("ActivateCells",0,0);
	    if (vecIDs.length!=0) {
	    	for (int i=0;i<vecIDs.length;i++) {
	    		orientations.addElement(jPanelReorient.jTableReorient.getValueAt(vecIDs[i],tmodel.getColId("v1")));
	    		orientations.addElement(jPanelReorient.jTableReorient.getValueAt(vecIDs[i],tmodel.getColId("v2")));
	    	}
	    } else {
	    	for (int i=0;i<jPanelReorient.jTableReorient.getRowCount();i++) {
	    		orientations.addElement(jPanelReorient.jTableReorient.getValueAt(i,tmodel.getColId("v1")));
	    		orientations.addElement(jPanelReorient.jTableReorient.getValueAt(i,tmodel.getColId("v2")));
	    	}    	
	    }
	    
	  	Vector3d datum = new Vector3d(0.,0.,0.);
	  	double omega=0,kappa=0,phi=0;
	  	try {
	  	omega=new Double(jTFOmega.getText()).doubleValue();
	  	} catch (NumberFormatException nfe){
	  	}
	  	try {
	  	kappa=new Double(jTFKappa.getText()).doubleValue();
		} catch (NumberFormatException nfe){
	  	}
	  	try {
	  	phi  =new Double(jTFPhi.getText()).doubleValue();
	  	} catch (NumberFormatException nfe){
	  	}
		datum.set(omega,kappa,phi);  		
	    
	    Point3d actualTrans = new Point3d();
	    try {
	    	actualTrans.x = new Double(jTFTransX.getText()).doubleValue();
	    } catch (Exception ex) {
	    }
	    try {
	    	actualTrans.y = new Double(jTFTransY.getText()).doubleValue();
	    } catch (Exception ex) {
	    }
	    try {
	    	actualTrans.z = new Double(jTFTransZ.getText()).doubleValue();
	    } catch (Exception ex) {
	    }

	    
	    String alignFile=alignment.CalculateAlignment(datum,actualTrans,orientations,bcm,utils);
	    //calling gonset
		//only case of not resetting omega,
	    // would be when indexing is performed assuming Oscillation start=0
	    //CallExternalGonset(true,orientations);
	    //fill the table from the file STAC_align.vec
	    ReadCalculatedAlignments(alignFile);
	  }
	*/
	
}
