package stac.gui;

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



public class STAC_DatumPanel extends JPanel 
implements STAC_GUI_Button_Panel, STAC_GUI_Widget {

	int datumId=0;
	
	Util utils;
	StacBCM bcm;
	
	Dimension rbSize = new Dimension(100,30);
	Dimension rbdSize = new Dimension(200,30);
	Dimension fsbSize = new Dimension(30,30);
	Dimension tfSize = new Dimension(1000,30);
	Dimension tfnSize = new Dimension(10,30);
	
    JLabel jLabelAlignVector = new JLabel();
	JButton buttonClearTable = new JButton("Clear Table");
    AlignTableModel alignTableModel = new AlignTableModel();
    STAC_GUI_JDatumTable jTable1 = new STAC_GUI_JDatumTable(alignTableModel,alignTableModel);
    //alignTableModel.setTable(jTable1);
    JScrollPane jpanelTable = new JScrollPane(jTable1);
    StrategyTableCellRenderer generalTableCellRenderer = new StrategyTableCellRenderer(true);
    
    
    //vector evaluation
//    JTextField jTFEvalO=new JTextField();
//    JTextField jTFEvalK=new JTextField();
//    JTextField jTFEvalP=new JTextField();
//    JTextField jTFEvalX=new JTextField();
//    JTextField jTFEvalY=new JTextField();
//    JTextField jTFEvalZ=new JTextField();  
//    JButton jButtonEval_getP = new JButton();
//    JTextField jTFEvalO2=new JTextField();
//    JTextField jTFEvalK2=new JTextField();
//    JTextField jTFEvalP2=new JTextField();
//    JTextField jTFEvalX2=new JTextField();
//    JTextField jTFEvalY2=new JTextField();
//    JTextField jTFEvalZ2=new JTextField();  
//    JButton jButtonClear      = new JButton();
//    JButton jButtonEval_getP2 = new JButton();
    
    StacMainWin mainWin;
    
    
    
    public STAC_DatumPanel(Util utils,StacBCM bcm, StacMainWin mainWin){
		if (utils!= null)
			this.utils=utils;
		else 
			this.utils=new Util();
		this.bcm=bcm;
		this.mainWin=mainWin;
		
	    this.setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.PAGE_AXIS));
	    this.setBorder(BorderFactory.createRaisedBevelBorder());
	    //this.setBounds(new Rectangle(37, 17, 575, 196));
	    //this.setPreferredSize(new Dimension(140, 140));
	    this.add(Box.createRigidArea(new Dimension(10,10)));
	    
	    JPanel titleRow= new JPanel();
	    titleRow.setLayout(new BoxLayoutFixed(titleRow,BoxLayoutFixed.LINE_AXIS));
	    this.add(titleRow);
	    titleRow.add(Box.createRigidArea(new Dimension(10,10)));
	    titleRow.add(jLabelAlignVector, null);
	    jLabelAlignVector.setText("Alignment Vectors:");
	    jLabelAlignVector.setBounds(new Rectangle(10, 0, 123, 34));
	    titleRow.add(Box.createHorizontalGlue());
		titleRow.add(buttonClearTable);
		buttonClearTable.setMinimumSize(rbdSize);
		buttonClearTable.setMaximumSize(rbdSize);
		buttonClearTable.setPreferredSize(rbdSize);
		buttonClearTable.setToolTipText("Remove the table entries");
		buttonClearTable.addActionListener(new Stac_Button_actionAdapter(this));
	    titleRow.add(Box.createRigidArea(new Dimension(10,10)));
	    
	    //this.add(jButton31, null);
	    //jButton31.setText("Process");
	    //jButton31.setBounds(new Rectangle(229, 151, 88, 34));
	    this.add(Box.createRigidArea(new Dimension(10,10)));
	    this.add(jpanelTable, null);
	    jpanelTable.getViewport().add(jTable1);
	    jpanelTable.setBounds(new Rectangle(9,38,558,99));
	    //jTable1.setMaximumSize(new Dimension(1000, 10000));
	    //jTable1.setPreferredSize(new Dimension(600, 10000));
	    jTable1.setToolTipText("Standard Allignment Vectors");
	    //jTable1.setVerifyInputWhenFocusTarget(true);
	    jTable1.setAutoscrolls(true);
	    jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);//TODO
	    jTable1.setDefaultRenderer(Object.class, generalTableCellRenderer);
	    jTable1.setDefaultRenderer(String.class, generalTableCellRenderer);
	    jTable1.setDefaultRenderer(Double.class, generalTableCellRenderer);
	    jTable1.setDefaultRenderer(Integer.class, generalTableCellRenderer);
	    alignTableModel.setTable(jTable1);

	    
	    
	    STAC_MovePanel movePanel = new STAC_MovePanel(jTable1,utils,bcm,mainWin);
	    this.add(movePanel);
	    

//
//	    //trans settings labels
//	    JPanel jPanelEvalTrans = new JPanel();
//	    this.add(jPanelEvalTrans);
//	    jPanelEvalTrans.setLayout(new BoxLayoutFixed(jPanelEvalTrans,BoxLayoutFixed.LINE_AXIS));
//	    jPanelEvalTrans.add(Box.createRigidArea(new Dimension(10,10)));
//	    jPanelEvalTrans.add(Box.createRigidArea(new Dimension(rbSize)));
//	    JLabel jLabelEvalTransO = new JLabel();
//	    jPanelEvalTrans.add(jLabelEvalTransO, null);
//	    jLabelEvalTransO.setText("Omega");
//	    jLabelEvalTransO.setHorizontalAlignment(SwingConstants.CENTER);
//	    jLabelEvalTransO.setMinimumSize(tfnSize);
//	    jLabelEvalTransO.setMaximumSize(tfSize);
//	    jLabelEvalTransO.setPreferredSize(tfSize);
//	    JLabel jLabelEvalTransK = new JLabel();
//	    jPanelEvalTrans.add(jLabelEvalTransK, null);
//	    jLabelEvalTransK.setText("Kappa");
//	    jLabelEvalTransK.setHorizontalAlignment(SwingConstants.CENTER);
//	    jLabelEvalTransK.setMinimumSize(tfnSize);
//	    jLabelEvalTransK.setMaximumSize(tfSize);
//	    jLabelEvalTransK.setPreferredSize(tfSize);
//	    JLabel jLabelEvalTransP = new JLabel();
//	    jPanelEvalTrans.add(jLabelEvalTransP, null);
//	    jLabelEvalTransP.setText("Phi");
//	    jLabelEvalTransP.setHorizontalAlignment(SwingConstants.CENTER);
//	    jLabelEvalTransP.setMinimumSize(tfnSize);
//	    jLabelEvalTransP.setMaximumSize(tfSize);
//	    jLabelEvalTransP.setPreferredSize(tfSize);
//	    JLabel jLabelEvalTransX = new JLabel();
//	    jPanelEvalTrans.add(jLabelEvalTransX, null);
//	    jLabelEvalTransX.setText("X");
//	    jLabelEvalTransX.setHorizontalAlignment(SwingConstants.CENTER);
//	    jLabelEvalTransX.setMinimumSize(tfnSize);
//	    jLabelEvalTransX.setMaximumSize(tfSize);
//	    jLabelEvalTransX.setPreferredSize(tfSize);
//	    JLabel jLabelEvalTransY = new JLabel();
//	    jPanelEvalTrans.add(jLabelEvalTransY, null);
//	    jLabelEvalTransY.setText("Y");
//	    jLabelEvalTransY.setHorizontalAlignment(SwingConstants.CENTER);
//	    jLabelEvalTransY.setMinimumSize(tfnSize);
//	    jLabelEvalTransY.setMaximumSize(tfSize);
//	    jLabelEvalTransY.setPreferredSize(tfSize);
//	    JLabel jLabelEvalTransZ = new JLabel();
//	    jPanelEvalTrans.add(jLabelEvalTransZ, null);
//	    jLabelEvalTransZ.setText("Z");
//	    jLabelEvalTransZ.setHorizontalAlignment(SwingConstants.CENTER);
//	    jLabelEvalTransZ.setMinimumSize(tfnSize);
//	    jLabelEvalTransZ.setMaximumSize(tfSize);
//	    jLabelEvalTransZ.setPreferredSize(tfSize);
//	    jPanelEvalTrans.add(Box.createRigidArea(rbSize));
//	    jPanelEvalTrans.add(Box.createRigidArea(new Dimension(10,10)));
//	    //trans settings fields
//	    JPanel jPanelEvalTransf = new JPanel();
//	    this.add(jPanelEvalTransf);
//	    jPanelEvalTransf.setLayout(new BoxLayoutFixed(jPanelEvalTransf,BoxLayoutFixed.LINE_AXIS));
//	    jPanelEvalTransf.add(Box.createRigidArea(new Dimension(10,10)));
//	    jPanelEvalTransf.add(Box.createRigidArea(new Dimension(rbSize)));
//	    jPanelEvalTransf.add(jTFEvalO, null);
//	    jTFEvalO.setToolTipText("Current Omega Angle");
//	    jTFEvalO.setMinimumSize(tfnSize);
//	    jTFEvalO.setMaximumSize(tfSize);
//	    jTFEvalO.setPreferredSize(tfSize);
//	    jTFEvalO.setEditable(false);
//	    jPanelEvalTransf.add(jTFEvalK, null);
//	    jTFEvalK.setToolTipText("Current Kappa Angle");
//	    jTFEvalK.setMinimumSize(tfnSize);
//	    jTFEvalK.setMaximumSize(tfSize);
//	    jTFEvalK.setPreferredSize(tfSize);
//	    jTFEvalK.setEditable(false);
//	    jPanelEvalTransf.add(jTFEvalP, null);
//	    jTFEvalP.setToolTipText("Current Phi Angle");
//	    jTFEvalP.setMinimumSize(tfnSize);
//	    jTFEvalP.setMaximumSize(tfSize);
//	    jTFEvalP.setPreferredSize(tfSize);
//	    jTFEvalP.setEditable(false);
//	    jPanelEvalTransf.add(jTFEvalX, null);
//	    jTFEvalX.setToolTipText("Current X Translation");
//	    jTFEvalX.setMinimumSize(tfnSize);
//	    jTFEvalX.setMaximumSize(tfSize);
//	    jTFEvalX.setPreferredSize(tfSize);
//	    jTFEvalX.setEditable(false);
//	    jPanelEvalTransf.add(jTFEvalY, null);
//	    jTFEvalY.setToolTipText("Current Y Translation");
//	    jTFEvalY.setMinimumSize(tfnSize);
//	    jTFEvalY.setMaximumSize(tfSize);
//	    jTFEvalY.setPreferredSize(tfSize);
//	    jTFEvalY.setEditable(false);
//	    jPanelEvalTransf.add(jTFEvalZ, null);
//	    jTFEvalZ.setToolTipText("Current Z Translation");
//	    jTFEvalZ.setMinimumSize(tfnSize);
//	    jTFEvalZ.setMaximumSize(tfSize);
//	    jTFEvalZ.setPreferredSize(tfSize);
//	    jTFEvalZ.setEditable(false);
//	    jButtonEval_getP.setText("Mv Sel.");
//	    jButtonEval_getP.setMinimumSize(rbSize);
//	    jButtonEval_getP.setMaximumSize(rbSize);
//	    jButtonEval_getP.setPreferredSize(rbSize);
//	    jButtonEval_getP.setToolTipText("Move the gonio to the uppermost selection");
//	    jButtonEval_getP.addActionListener(new Stac_Button_actionAdapter(this));
//	    jButtonEval_getP.setEnabled(true);
//	    jPanelEvalTransf.add(jButtonEval_getP,null);
//	    jPanelEvalTransf.add(Box.createRigidArea(new Dimension(10,10)));
//	    //separator
//	    JPanel thisSepLine = new JPanel();
//	    this.add(thisSepLine);
//	    thisSepLine.setLayout(new BoxLayoutFixed(thisSepLine,BoxLayoutFixed.LINE_AXIS));
//	    thisSepLine.add(Box.createRigidArea(new Dimension(10,10)));
//	    JTextField jTF3SepL = new JTextField();
//	    thisSepLine.add(jTF3SepL, null);
//	    jTF3SepL.setEditable(false);
//	    jTF3SepL.setMinimumSize(new Dimension(1,3));
//	    jTF3SepL.setMaximumSize(new Dimension(2000,3));
//	    jTF3SepL.setPreferredSize(new Dimension(1,3));
//	    thisSepLine.add(Box.createRigidArea(new Dimension(10,10)));
//	    //trans settings fields for arbitrary motion
//	    JPanel jPanelEvalTransf2 = new JPanel();
//	    this.add(jPanelEvalTransf2);
//	    jPanelEvalTransf2.setLayout(new BoxLayoutFixed(jPanelEvalTransf2,BoxLayoutFixed.LINE_AXIS));
//	    jPanelEvalTransf2.add(Box.createRigidArea(new Dimension(10,10)));
//	    jButtonClear.setText("Clear:");
//	    jButtonClear.setMinimumSize(rbSize);
//	    jButtonClear.setMaximumSize(rbSize);
//	    jButtonClear.setPreferredSize(rbSize);
//	    jButtonClear.setToolTipText("Clear the edit fileds");
//	    jButtonClear.addActionListener(new Stac_Button_actionAdapter(this));
//	    jButtonClear.setEnabled(true);
//	    jPanelEvalTransf2.add(jButtonClear,null);
//	    jPanelEvalTransf2.add(jTFEvalO2, null);
//	    jTFEvalO2.setToolTipText("Current Omega Angle");
//	    jTFEvalO2.setMinimumSize(tfnSize);
//	    jTFEvalO2.setMaximumSize(tfSize);
//	    jTFEvalO2.setPreferredSize(tfSize);
//	    jPanelEvalTransf2.add(jTFEvalK2, null);
//	    jTFEvalK2.setToolTipText("Current Kappa Angle");
//	    jTFEvalK2.setMinimumSize(tfnSize);
//	    jTFEvalK2.setMaximumSize(tfSize);
//	    jTFEvalK2.setPreferredSize(tfSize);
//	    jPanelEvalTransf2.add(jTFEvalP2, null);
//	    jTFEvalP2.setToolTipText("Current Phi Angle");
//	    jTFEvalP2.setMinimumSize(tfnSize);
//	    jTFEvalP2.setMaximumSize(tfSize);
//	    jTFEvalP2.setPreferredSize(tfSize);
//	    jPanelEvalTransf2.add(jTFEvalX2, null);
//	    jTFEvalX2.setToolTipText("Current X Translation");
//	    jTFEvalX2.setMinimumSize(tfnSize);
//	    jTFEvalX2.setMaximumSize(tfSize);
//	    jTFEvalX2.setPreferredSize(tfSize);
//	    jTFEvalX2.setEditable(false);
//	    jPanelEvalTransf2.add(jTFEvalY2, null);
//	    jTFEvalY2.setToolTipText("Current Y Translation");
//	    jTFEvalY2.setMinimumSize(tfnSize);
//	    jTFEvalY2.setMaximumSize(tfSize);
//	    jTFEvalY2.setPreferredSize(tfSize);
//	    jTFEvalY2.setEditable(false);
//	    jPanelEvalTransf2.add(jTFEvalZ2, null);
//	    jTFEvalZ2.setToolTipText("Current Z Translation");
//	    jTFEvalZ2.setMinimumSize(tfnSize);
//	    jTFEvalZ2.setMaximumSize(tfSize);
//	    jTFEvalZ2.setPreferredSize(tfSize);
//	    jTFEvalZ2.setEditable(false);
//	    jButtonEval_getP2.setText("Mv Edit");
//	    jButtonEval_getP2.setMinimumSize(rbSize);
//	    jButtonEval_getP2.setMaximumSize(rbSize);
//	    jButtonEval_getP2.setPreferredSize(rbSize);
//	    jButtonEval_getP2.setToolTipText("Move the gonio to the edited position");
//	    jButtonEval_getP2.addActionListener(new Stac_Button_actionAdapter(this));
//	    jButtonEval_getP2.setEnabled(true);
//	    jPanelEvalTransf2.add(jButtonEval_getP2,null);
//	    jPanelEvalTransf2.add(Box.createRigidArea(new Dimension(10,10)));
	    
	    this.add(Box.createRigidArea(new Dimension(10,10)));
	    
		
	}
	
    
    
    public Point3d CalculateCalibratedTranslation(double angles[],Point3d dat,Point3d trn) {
    	//get info for trans calc
    	//double actualKappa=0.,actualPhi=0.;
    	double actualKappa=dat.y,actualPhi=dat.z;
    	try {
    		actualKappa = new Double(mainWin.jTFKappa.getText()).doubleValue();
    	} catch (Exception e) {
    	}
    	try {
    		actualPhi = new Double(mainWin.jTFPhi.getText()).doubleValue();
    	} catch (Exception e) {
    	}
    	//Point3d actualTrans = new Point3d();
    	try {
    		trn.x = new Double(mainWin.jTFTransX.getText()).doubleValue();
    	} catch (Exception e) {
    	}
    	try {
    		trn.y = new Double(mainWin.jTFTransY.getText()).doubleValue();
    	} catch (Exception e) {
    	}
    	try {
    		trn.z = new Double(mainWin.jTFTransZ.getText()).doubleValue();
    	} catch (Exception e) {
    	}
    	double omega = angles[0]; 
    	double kappa = angles[1];
    	double phi   = angles[2];
    	//Stac_Out.println("omega: "+omega+"kappa: "+kappa+"phi  : "+phi);
    	Point3d trans;
    	trans= utils.CalculateTranslation(trn,actualKappa,kappa,actualPhi,phi,bcm);
    	return trans;
    }
    
     
    
    public void Button_actionAdapter(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
	        Stac_Out.println("USER ACTION: Button pressed (STAC Datum Panel: "+((JButton)e.getSource()).getText()+")");			
		}
    	if (e.getSource()==this.buttonClearTable) {
			this.clearTable();
	    }

    	
    }
    
    public void clearTable() {
        if (jTable1.getRowCount()>0)
        	jTable1.setValueAt("ClearTable",0,0);    	
    }
    public void addTableRow(Vector actRow) {
        String v1 = (String)actRow.elementAt(0);
        String v2 = (String)actRow.elementAt(1);
        Double omega = (Double)actRow.elementAt(2);
        Double kappa = (Double)actRow.elementAt(3);
        Double phi   = (Double)actRow.elementAt(4);
        String trans = (String)actRow.elementAt(5);
        Double rank  = (Double)actRow.elementAt(6);
        String comm  = (String)actRow.elementAt(7);
        
        AlignTableModel amodel = (AlignTableModel) jTable1.getModel();        
        int newrow = jTable1.getRowCount();        
        jTable1.setValueAt(new Integer(++datumId), newrow, amodel.getColId("ID"));
        jTable1.setValueAt(v1, newrow, amodel.getColId("v1"));
        jTable1.setValueAt(v2, newrow, amodel.getColId("v2"));
        jTable1.setValueAt(omega, newrow, amodel.getColId("omega"));
        jTable1.setValueAt(kappa, newrow, amodel.getColId("kappa"));
        jTable1.setValueAt(phi, newrow, amodel.getColId("phi"));
        jTable1.setValueAt(trans,	newrow, amodel.getColId("trans"));             
        jTable1.setValueAt(rank, newrow, amodel.getColId("rank"));    	
        jTable1.setValueAt(comm, newrow, amodel.getColId("comment"));    	
    }
    public void setTable(Vector dataRows) {
    	clearTable();
        AlignTableModel amodel = (AlignTableModel) jTable1.getModel();        
        for(int i=0;i<dataRows.size();i++) {
        	addTableRow((Vector)dataRows.elementAt(i));
        }
    }
    
    public Vector getDatumRequest() {
	    Vector orientations = new Vector(0);
	    int[] vecIDs = jTable1.getSelectedRows();
	    AlignTableModel tmodel = (AlignTableModel) jTable1.getModel();
	    tmodel.setValueAt("ActivateCells",0,0);
	    if (vecIDs.length!=0) {
	    	for (int i=0;i<vecIDs.length;i++) {
	    		orientations.addElement(jTable1.getValueAt(vecIDs[i],tmodel.getColId("v1")));
	    		orientations.addElement(jTable1.getValueAt(vecIDs[i],tmodel.getColId("v2")));
	    		orientations.addElement(jTable1.getValueAt(vecIDs[i],tmodel.getColId("omega")));
	    		orientations.addElement(jTable1.getValueAt(vecIDs[i],tmodel.getColId("kappa")));
	    		orientations.addElement(jTable1.getValueAt(vecIDs[i],tmodel.getColId("phi")));
	    		orientations.addElement(jTable1.getValueAt(vecIDs[i],tmodel.getColId("trans")));
	    		orientations.addElement(jTable1.getValueAt(vecIDs[i],tmodel.getColId("rank")));
	    		orientations.addElement(jTable1.getValueAt(vecIDs[i],tmodel.getColId("comment")));
	    	}
	    } else {
	    	for (int i=0;i<jTable1.getRowCount();i++) {
	    		orientations.addElement(jTable1.getValueAt(i,tmodel.getColId("v1")));
	    		orientations.addElement(jTable1.getValueAt(i,tmodel.getColId("v2")));
	    		orientations.addElement(jTable1.getValueAt(i,tmodel.getColId("omega")));
	    		orientations.addElement(jTable1.getValueAt(i,tmodel.getColId("kappa")));
	    		orientations.addElement(jTable1.getValueAt(i,tmodel.getColId("phi")));
	    		orientations.addElement(jTable1.getValueAt(i,tmodel.getColId("trans")));
	    		orientations.addElement(jTable1.getValueAt(i,tmodel.getColId("rank")));
	    		orientations.addElement(jTable1.getValueAt(i,tmodel.getColId("comment")));
	    	}    	
	    }
	    return orientations;
    	
    }



	public void setConfig(Stac_Configuration config) {
		alignTableModel.setConfig(config);
		
	}
    
}


