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



public class STAC_MovePanel extends JPanel 
implements STAC_GUI_Button_Panel {

	int datumId=0;
	
	Util utils;
	StacBCM bcm;
	
	Dimension rbSize = new Dimension(100,30);
	Dimension rbdSize = new Dimension(200,30);
	Dimension fsbSize = new Dimension(30,30);
	Dimension tfSize = new Dimension(1000,30);
	Dimension tfnSize = new Dimension(10,30);
	
    //JLabel jLabelAlignVector = new JLabel();
	JButton buttonClearTable = new JButton("Clear Table");
    //AlignTableModel alignTableModel = new AlignTableModel();
    STAC_GUI_JDatumTable jTable1;// = new JTable(alignTableModel);
    //JScrollPane jpanelTable = new JScrollPane(jTable1);
    //StrategyTableCellRenderer generalTableCellRenderer = new StrategyTableCellRenderer(true);
    
    
    //vector evaluation
    JTextField jTFEvalO=new JTextField();
    JTextField jTFEvalK=new JTextField();
    JTextField jTFEvalP=new JTextField();
    JTextField jTFEvalX=new JTextField();
    JTextField jTFEvalY=new JTextField();
    JTextField jTFEvalZ=new JTextField();  
    JButton jButtonEval_getP = new JButton();
    JTextField jTFEvalO2=new JTextField();
    JTextField jTFEvalK2=new JTextField();
    JTextField jTFEvalP2=new JTextField();
    JTextField jTFEvalX2=new JTextField();
    JTextField jTFEvalY2=new JTextField();
    JTextField jTFEvalZ2=new JTextField();  
    JButton jButtonClear      = new JButton();
    JButton jButtonEval_getP2 = new JButton();
    
    StacMainWin mainWin;
    
    
    
    public STAC_MovePanel(STAC_GUI_JDatumTable table,Util utils,StacBCM bcm, StacMainWin mainWin){
		this.utils=utils;
		this.bcm=bcm;
		this.mainWin=mainWin;
		this.jTable1=table;
		
	    this.setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.PAGE_AXIS));

	    //trans settings labels
	    JPanel jPanelEvalTrans = new JPanel();
	    this.add(jPanelEvalTrans);
	    jPanelEvalTrans.setLayout(new BoxLayoutFixed(jPanelEvalTrans,BoxLayoutFixed.LINE_AXIS));
	    jPanelEvalTrans.add(Box.createRigidArea(new Dimension(10,10)));
	    jPanelEvalTrans.add(Box.createRigidArea(new Dimension(rbSize)));
	    JLabel jLabelEvalTransO = new JLabel();
	    jPanelEvalTrans.add(jLabelEvalTransO, null);
	    jLabelEvalTransO.setText("Omega");
	    jLabelEvalTransO.setHorizontalAlignment(SwingConstants.CENTER);
	    jLabelEvalTransO.setMinimumSize(tfnSize);
	    jLabelEvalTransO.setMaximumSize(tfSize);
	    jLabelEvalTransO.setPreferredSize(tfSize);
	    JLabel jLabelEvalTransK = new JLabel();
	    jPanelEvalTrans.add(jLabelEvalTransK, null);
	    jLabelEvalTransK.setText("Kappa");
	    jLabelEvalTransK.setHorizontalAlignment(SwingConstants.CENTER);
	    jLabelEvalTransK.setMinimumSize(tfnSize);
	    jLabelEvalTransK.setMaximumSize(tfSize);
	    jLabelEvalTransK.setPreferredSize(tfSize);
	    JLabel jLabelEvalTransP = new JLabel();
	    jPanelEvalTrans.add(jLabelEvalTransP, null);
	    jLabelEvalTransP.setText("Phi");
	    jLabelEvalTransP.setHorizontalAlignment(SwingConstants.CENTER);
	    jLabelEvalTransP.setMinimumSize(tfnSize);
	    jLabelEvalTransP.setMaximumSize(tfSize);
	    jLabelEvalTransP.setPreferredSize(tfSize);
	    JLabel jLabelEvalTransX = new JLabel();
	    jPanelEvalTrans.add(jLabelEvalTransX, null);
	    jLabelEvalTransX.setText("X");
	    jLabelEvalTransX.setHorizontalAlignment(SwingConstants.CENTER);
	    jLabelEvalTransX.setMinimumSize(tfnSize);
	    jLabelEvalTransX.setMaximumSize(tfSize);
	    jLabelEvalTransX.setPreferredSize(tfSize);
	    JLabel jLabelEvalTransY = new JLabel();
	    jPanelEvalTrans.add(jLabelEvalTransY, null);
	    jLabelEvalTransY.setText("Y");
	    jLabelEvalTransY.setHorizontalAlignment(SwingConstants.CENTER);
	    jLabelEvalTransY.setMinimumSize(tfnSize);
	    jLabelEvalTransY.setMaximumSize(tfSize);
	    jLabelEvalTransY.setPreferredSize(tfSize);
	    JLabel jLabelEvalTransZ = new JLabel();
	    jPanelEvalTrans.add(jLabelEvalTransZ, null);
	    jLabelEvalTransZ.setText("Z");
	    jLabelEvalTransZ.setHorizontalAlignment(SwingConstants.CENTER);
	    jLabelEvalTransZ.setMinimumSize(tfnSize);
	    jLabelEvalTransZ.setMaximumSize(tfSize);
	    jLabelEvalTransZ.setPreferredSize(tfSize);
	    jPanelEvalTrans.add(Box.createRigidArea(rbSize));
	    jPanelEvalTrans.add(Box.createRigidArea(new Dimension(10,10)));
	    //trans settings fields
	    JPanel jPanelEvalTransf = new JPanel();
	    this.add(jPanelEvalTransf);
	    jPanelEvalTransf.setLayout(new BoxLayoutFixed(jPanelEvalTransf,BoxLayoutFixed.LINE_AXIS));
	    jPanelEvalTransf.add(Box.createRigidArea(new Dimension(10,10)));
	    jPanelEvalTransf.add(Box.createRigidArea(new Dimension(rbSize)));
	    jPanelEvalTransf.add(jTFEvalO, null);
	    jTFEvalO.setToolTipText("Current Omega Angle");
	    jTFEvalO.setMinimumSize(tfnSize);
	    jTFEvalO.setMaximumSize(tfSize);
	    jTFEvalO.setPreferredSize(tfSize);
	    jTFEvalO.setEditable(false);
	    jPanelEvalTransf.add(jTFEvalK, null);
	    jTFEvalK.setToolTipText("Current Kappa Angle");
	    jTFEvalK.setMinimumSize(tfnSize);
	    jTFEvalK.setMaximumSize(tfSize);
	    jTFEvalK.setPreferredSize(tfSize);
	    jTFEvalK.setEditable(false);
	    jPanelEvalTransf.add(jTFEvalP, null);
	    jTFEvalP.setToolTipText("Current Phi Angle");
	    jTFEvalP.setMinimumSize(tfnSize);
	    jTFEvalP.setMaximumSize(tfSize);
	    jTFEvalP.setPreferredSize(tfSize);
	    jTFEvalP.setEditable(false);
	    jPanelEvalTransf.add(jTFEvalX, null);
	    jTFEvalX.setToolTipText("Current X Translation");
	    jTFEvalX.setMinimumSize(tfnSize);
	    jTFEvalX.setMaximumSize(tfSize);
	    jTFEvalX.setPreferredSize(tfSize);
	    jTFEvalX.setEditable(false);
	    jPanelEvalTransf.add(jTFEvalY, null);
	    jTFEvalY.setToolTipText("Current Y Translation");
	    jTFEvalY.setMinimumSize(tfnSize);
	    jTFEvalY.setMaximumSize(tfSize);
	    jTFEvalY.setPreferredSize(tfSize);
	    jTFEvalY.setEditable(false);
	    jPanelEvalTransf.add(jTFEvalZ, null);
	    jTFEvalZ.setToolTipText("Current Z Translation");
	    jTFEvalZ.setMinimumSize(tfnSize);
	    jTFEvalZ.setMaximumSize(tfSize);
	    jTFEvalZ.setPreferredSize(tfSize);
	    jTFEvalZ.setEditable(false);
	    jButtonEval_getP.setText("Mv Sel.");
	    jButtonEval_getP.setMinimumSize(rbSize);
	    jButtonEval_getP.setMaximumSize(rbSize);
	    jButtonEval_getP.setPreferredSize(rbSize);
	    jButtonEval_getP.setToolTipText("Move the gonio to the uppermost selection");
	    jButtonEval_getP.addActionListener(new Stac_Button_actionAdapter(this));
	    jButtonEval_getP.setEnabled(true);
	    jPanelEvalTransf.add(jButtonEval_getP,null);
	    jPanelEvalTransf.add(Box.createRigidArea(new Dimension(10,10)));
	    //separator
	    JPanel thisSepLine = new JPanel();
	    this.add(thisSepLine);
	    thisSepLine.setLayout(new BoxLayoutFixed(thisSepLine,BoxLayoutFixed.LINE_AXIS));
	    thisSepLine.add(Box.createRigidArea(new Dimension(10,10)));
	    JTextField jTF3SepL = new JTextField();
	    thisSepLine.add(jTF3SepL, null);
	    jTF3SepL.setEditable(false);
	    jTF3SepL.setMinimumSize(new Dimension(1,3));
	    jTF3SepL.setMaximumSize(new Dimension(2000,3));
	    jTF3SepL.setPreferredSize(new Dimension(1,3));
	    thisSepLine.add(Box.createRigidArea(new Dimension(10,10)));
	    //trans settings fields for arbitrary motion
	    JPanel jPanelEvalTransf2 = new JPanel();
	    this.add(jPanelEvalTransf2);
	    jPanelEvalTransf2.setLayout(new BoxLayoutFixed(jPanelEvalTransf2,BoxLayoutFixed.LINE_AXIS));
	    jPanelEvalTransf2.add(Box.createRigidArea(new Dimension(10,10)));
	    jButtonClear.setText("Clear:");
	    jButtonClear.setMinimumSize(rbSize);
	    jButtonClear.setMaximumSize(rbSize);
	    jButtonClear.setPreferredSize(rbSize);
	    jButtonClear.setToolTipText("Clear the edit fileds");
	    jButtonClear.addActionListener(new Stac_Button_actionAdapter(this));
	    jButtonClear.setEnabled(true);
	    jPanelEvalTransf2.add(jButtonClear,null);
	    jPanelEvalTransf2.add(jTFEvalO2, null);
	    jTFEvalO2.setToolTipText("Current Omega Angle");
	    jTFEvalO2.setMinimumSize(tfnSize);
	    jTFEvalO2.setMaximumSize(tfSize);
	    jTFEvalO2.setPreferredSize(tfSize);
	    jPanelEvalTransf2.add(jTFEvalK2, null);
	    jTFEvalK2.setToolTipText("Current Kappa Angle");
	    jTFEvalK2.setMinimumSize(tfnSize);
	    jTFEvalK2.setMaximumSize(tfSize);
	    jTFEvalK2.setPreferredSize(tfSize);
	    jPanelEvalTransf2.add(jTFEvalP2, null);
	    jTFEvalP2.setToolTipText("Current Phi Angle");
	    jTFEvalP2.setMinimumSize(tfnSize);
	    jTFEvalP2.setMaximumSize(tfSize);
	    jTFEvalP2.setPreferredSize(tfSize);
	    jPanelEvalTransf2.add(jTFEvalX2, null);
	    jTFEvalX2.setToolTipText("Current X Translation");
	    jTFEvalX2.setMinimumSize(tfnSize);
	    jTFEvalX2.setMaximumSize(tfSize);
	    jTFEvalX2.setPreferredSize(tfSize);
	    jTFEvalX2.setEditable(false);
	    jPanelEvalTransf2.add(jTFEvalY2, null);
	    jTFEvalY2.setToolTipText("Current Y Translation");
	    jTFEvalY2.setMinimumSize(tfnSize);
	    jTFEvalY2.setMaximumSize(tfSize);
	    jTFEvalY2.setPreferredSize(tfSize);
	    jTFEvalY2.setEditable(false);
	    jPanelEvalTransf2.add(jTFEvalZ2, null);
	    jTFEvalZ2.setToolTipText("Current Z Translation");
	    jTFEvalZ2.setMinimumSize(tfnSize);
	    jTFEvalZ2.setMaximumSize(tfSize);
	    jTFEvalZ2.setPreferredSize(tfSize);
	    jTFEvalZ2.setEditable(false);
	    jButtonEval_getP2.setText("Mv Edit");
	    jButtonEval_getP2.setMinimumSize(rbSize);
	    jButtonEval_getP2.setMaximumSize(rbSize);
	    jButtonEval_getP2.setPreferredSize(rbSize);
	    jButtonEval_getP2.setToolTipText("Move the gonio to the edited position");
	    jButtonEval_getP2.addActionListener(new Stac_Button_actionAdapter(this));
	    jButtonEval_getP2.setEnabled(true);
	    jPanelEvalTransf2.add(jButtonEval_getP2,null);
	    jPanelEvalTransf2.add(Box.createRigidArea(new Dimension(10,10)));
	    //this.add(Box.createRigidArea(new Dimension(10,10)));
	    
		
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
    
    
    void EvalGetCurrentDatumTrans(){
    	Point3d trn= new Point3d();
    	Point3d dat= new Point3d();
    	double o,k,p,x,y,z;
    	//if something is selected
    	int[] vecIDs = jTable1.getSelectedRows();
    	//get the current positions
    	bcm.getCurrentDatumTrans(dat,trn);
    	o=dat.x;
    	k=dat.y;
    	p=dat.z;
    	x=trn.x;
    	y=trn.y;
    	z=trn.z;
    	boolean recalcTrans=false;
    	if(vecIDs.length>0)
    	{
    		DatumTableModel amodel = jTable1.getDatumTableModel();
    		//for now, as getSelectedDatumTrans does not work after strategy sorting!!!
    		if (amodel != jTable1.getModel())
    			return;
    		double [] datumTrans=amodel.getSelectedDatumTrans(vecIDs[0]);
    		
    		o=datumTrans[0];
    		k=datumTrans[1];
    		p=datumTrans[2];
    		
//    		o=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("omega"))).doubleValue();
//    		k=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("kappa"))).doubleValue();
//    		p=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("phi"))).doubleValue();
//    		String tr = (String)jTable1.getValueAt(vecIDs[0],amodel.getColId("Trans"));
//    		tr=tr.substring(1,tr.length()-1);
//    		String tmp[]=tr.split(";");
    		try {
        		x=datumTrans[3];
        		y=datumTrans[4];
        		z=datumTrans[5];
        		
//    			x = new Double(tmp[0]).doubleValue();
//    			y = new Double(tmp[1]).doubleValue();
//    			z = new Double(tmp[2]).doubleValue();
    		} catch (Exception e) {
    			recalcTrans=true;
    		}
    	}
    	if (recalcTrans){
    		double angles[]={o,k,p};
    		Point3d pts= new Point3d();
    		pts=CalculateCalibratedTranslation(angles,dat,trn);
    		x=pts.x;y=pts.y;z=pts.z;
    	}
    	//move to the desired datum/trans
    	bcm.moveToDatumTransSync(o,k,p,x,y,z);
    	//update the field
    	bcm.getCurrentDatumTrans(dat,trn);
    	jTFEvalO.setText(""+new PrintfFormat("%.4f").sprintf(dat.x));
    	jTFEvalK.setText(""+new PrintfFormat("%.4f").sprintf(dat.y));
    	jTFEvalP.setText(""+new PrintfFormat("%.4f").sprintf(dat.z));
    	jTFEvalX.setText(""+new PrintfFormat("%.4f").sprintf(trn.x));
    	jTFEvalY.setText(""+new PrintfFormat("%.4f").sprintf(trn.y));
    	jTFEvalZ.setText(""+new PrintfFormat("%.4f").sprintf(trn.z));
    	
    	Stac_Out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    	Stac_Out.println("!!!  Spec user oscillation start: !!!");//TODO
    	Stac_Out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    	
    }
    /**
     * moves to new datum by keeping the point in the middle
     * if centerpoint registration textfield is empty (eg: DNA) => use the CURRENTly centered point
     * otherwise => 
     *
     */
    void EvalGetCurrentDatumTrans2(){
    	Point3d trn= new Point3d();
    	Point3d dat= new Point3d();
    	double o,k,p,x,y,z;
    	//get the current positions
    	bcm.getCurrentDatumTrans(dat,trn);
    	o=dat.x;
    	k=dat.y;
    	p=dat.z;
    	x=trn.x;
    	y=trn.y;
    	z=trn.z;
    	boolean recalcTrans=false;
    	{
    		//otherwise set only specified values, so
    		//modify them if specified
    		try {
    			o = new Double(jTFEvalO2.getText()).doubleValue();
    		} catch (Exception e) {
    		}
    		try {
    			k = new Double(jTFEvalK2.getText()).doubleValue();
    			recalcTrans=true;
    		} catch (Exception e) {
    		}
    		try {
    			p = new Double(jTFEvalP2.getText()).doubleValue();
    			recalcTrans=true;
    		} catch (Exception e) {
    		}
//    		try {
//    			x = new Double(jTFEvalX2.getText()).doubleValue();
//    		} catch (Exception e) {
//    		}
//    		try {
//    			y = new Double(jTFEvalY2.getText()).doubleValue();
//    		} catch (Exception e) {
//    		}
//    		try {
//    			z = new Double(jTFEvalZ2.getText()).doubleValue();
//    		} catch (Exception e) {
//    		}
    	}
    	if (recalcTrans){
    		double angles[]={o,k,p};
    		Point3d pts= new Point3d();
    		pts=CalculateCalibratedTranslation(angles,dat,trn);
    		x=pts.x;y=pts.y;z=pts.z;
    	}
    	//move to the desired datum/trans
    	bcm.moveToDatumTransSync(o,k,p,x,y,z);
    	//update the field
    	bcm.getCurrentDatumTrans(dat,trn);
    	jTFEvalO2.setText(""+new PrintfFormat("%.4f").sprintf(dat.x));
    	jTFEvalK2.setText(""+new PrintfFormat("%.4f").sprintf(dat.y));
    	jTFEvalP2.setText(""+new PrintfFormat("%.4f").sprintf(dat.z));
    	jTFEvalX2.setText(""+new PrintfFormat("%.4f").sprintf(trn.x));
    	jTFEvalY2.setText(""+new PrintfFormat("%.4f").sprintf(trn.y));
    	jTFEvalZ2.setText(""+new PrintfFormat("%.4f").sprintf(trn.z));
    	
    }
    
    
    public void Button_actionAdapter(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
	        Stac_Out.println("USER ACTION: Button pressed (STAC Datum Panel: "+((JButton)e.getSource()).getText()+")");			
		}
    	if (e.getSource()==this.jButtonEval_getP) {
    		//panel 3
    		this.EvalGetCurrentDatumTrans();
    	} else if (e.getSource()==this.jButtonEval_getP2) {
    		//panel 3
    		this.EvalGetCurrentDatumTrans2();
    	} else if (e.getSource()==this.jButtonClear) {
    		//panel 3
    		this.jTFEvalO2.setText("");
    		this.jTFEvalK2.setText("");
    		this.jTFEvalP2.setText("");
    	}
    	
    }
    
}


