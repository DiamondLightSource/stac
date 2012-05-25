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




public class STAC_StrategyPanel extends JPanel 
implements STAC_GUI_Button_Panel {
	
	int strategyId=0;
	Util utils;
	StacBCM bcm;
    StacMainWin mainWin;
	
	Dimension rbSize = new Dimension(100,30);
	Dimension rbdSize = new Dimension(200,30);
	Dimension fsbSize = new Dimension(30,30);
	Dimension tfSize = new Dimension(1000,30);
	Dimension tfnSize = new Dimension(10,30);
	
	JLabel jLabelStrategy = new JLabel();
	JButton buttonClearTable = new JButton("Clear Table");
	StrategyTableModel strategyTableModel = new StrategyTableModel();
	TableSorter sorter = new TableSorter(strategyTableModel);
	//JTable jTableStrategy = new JTable(strategyTableModel);
	STAC_GUI_JDatumTable jTableStrategy = new STAC_GUI_JDatumTable(sorter,strategyTableModel);
	JScrollPane jpanelTableStrategy = new JScrollPane(jTableStrategy);
	
	Vector strategyData = new Vector();
	Vector origData = new Vector();
	
    StrategyTableCellRenderer strategyTableCellRenderer = new StrategyTableCellRenderer();
	
	
	public STAC_StrategyPanel(Util utils,StacBCM bcm, StacMainWin mainWin){
		if (utils!= null)
			this.utils=utils;
		else 
			this.utils=new Util();
		this.bcm=bcm;
		this.mainWin=mainWin;
		
		strategyTableModel.setTable(jTableStrategy);

		
	    this.setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.PAGE_AXIS));
	    this.setBorder(BorderFactory.createRaisedBevelBorder());
	    //this.setBounds(new Rectangle(37, 17, 575, 196));
	    //this.setPreferredSize(new Dimension(140, 140));
	    this.add(Box.createRigidArea(new Dimension(10,10)));
	    JPanel titleRow= new JPanel();
	    titleRow.setLayout(new BoxLayoutFixed(titleRow,BoxLayoutFixed.LINE_AXIS));
	    this.add(titleRow);
	    titleRow.add(Box.createRigidArea(new Dimension(10,10)));
	    titleRow.add(jLabelStrategy, null);
	    jLabelStrategy.setText("Multiple-Sweep Strategies:");
	    jLabelStrategy.setBounds(new Rectangle(10, 0, 123, 34));
	    titleRow.add(Box.createHorizontalGlue());
		titleRow.add(buttonClearTable);
		buttonClearTable.setMinimumSize(rbdSize);
		buttonClearTable.setMaximumSize(rbdSize);
		buttonClearTable.setPreferredSize(rbdSize);
		buttonClearTable.setToolTipText("Remove the table entries");
		buttonClearTable.addActionListener(new Stac_Button_actionAdapter(this));
	    titleRow.add(Box.createRigidArea(new Dimension(10,10)));

	    //this.add(jButton41, null);
	    //jButton41.setText("Process");
	    //jButton41.setBounds(new Rectangle(229, 151, 88, 34));
	    this.add(Box.createRigidArea(new Dimension(10,10)));
	    this.add(jpanelTableStrategy, null);
	    jpanelTableStrategy.getViewport().add(jTableStrategy);
	    jpanelTableStrategy.setBounds(new Rectangle(9,38,558,99));
	    //jTableStrategy.setMaximumSize(new Dimension(1000, 10000));
	    //jTableStrategy.setPreferredSize(new Dimension(800, 10000));
	    jTableStrategy.setToolTipText("Multiple-Sweep Strategies with Completeness");
	    jTableStrategy.setVerifyInputWhenFocusTarget(true);
	    jTableStrategy.setAutoscrolls(true);
	    jTableStrategy.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	    jTableStrategy.setDefaultRenderer(Object.class, strategyTableCellRenderer);
	    jTableStrategy.setDefaultRenderer(String.class, strategyTableCellRenderer);
	    jTableStrategy.setDefaultRenderer(Double.class, strategyTableCellRenderer);
	    jTableStrategy.setDefaultRenderer(Integer.class, strategyTableCellRenderer);
	    jTableStrategy.setDefaultRenderer(ModifiedObject.class, strategyTableCellRenderer);

        sorter.setTableHeader(jTableStrategy.getTableHeader()); //ADDED THIS
        //Set up tool tips for column headers.
        jTableStrategy.getTableHeader().setToolTipText(
                "Click to specify sorting; Control-Click to specify secondary sorting");
        
	    STAC_MovePanel movePanel = new STAC_MovePanel(jTableStrategy,utils,bcm,mainWin);
	    this.add(movePanel);
	    
	    this.add(Box.createRigidArea(new Dimension(10,10)));

	    
	}


    public void clearTable() {
        if (jTableStrategy.getRowCount()>0) {
        	jTableStrategy.setValueAt("ClearTable",0,0);
        	strategyData.removeAllElements();
        	origData.removeAllElements();
        }
    }
    public void addTableRow(ParamTable actRow) {
        //StrategyTableModel amodel = (StrategyTableModel) jTableStrategy.getModel();        
        StrategyTableModel amodel = this.strategyTableModel;        
        int newrow = jTableStrategy.getRowCount();
        //for (int i=0;i<amodel.getColumnCount();i++) {
        //    jTableStrategy.setValueAt(actRow.getFirstValue(amodel.getColumnName(i)), newrow, i);        	
        //}
        for (int i=0;i<actRow.pnames.size();i++) {
        	//do not accept precalculated rank values/instead use the dynamic refreshing for now
        	if (((String)actRow.pnames.elementAt(i)).compareToIgnoreCase("rank")!=0) {
            	jTableStrategy.setValueAt(actRow.getFirstValue(actRow.pnames.elementAt(i)), newrow, amodel.getColId((String)actRow.pnames.elementAt(i)));
        	}
        }
        strategyData.addElement(actRow.getFirstValue("strategy_data"));
        origData.addElement(actRow);
        jTableStrategy.setValueAt("RestartRanking",0,0);
        
    }
    public int getStrategyId() {
    	return strategyId;
    }
    public void setStrategyId(int newId) {
    	strategyId=newId;
    }
    public void setTable(Vector dataRows) {
    	clearTable();
    	//StrategyTableModel amodel = (StrategyTableModel) jTableStrategy.getModel();        
        for(int i=0;i<dataRows.size();i++) {
        	addTableRow((ParamTable)dataRows.elementAt(i));
        }
    }
    
    public Vector getStrategyRequest() {
    	Vector orientations = new Vector();
    	int[] vecIDs = jTableStrategy.getSelectedRows();
        //StrategyTableModel tmodel = (StrategyTableModel) jTableStrategy.getModel();        
        StrategyTableModel tmodel = this.strategyTableModel;        
    	tmodel.setValueAt("ActivateCells",0,0);
    	for (int i=0;i<((vecIDs.length!=0)?vecIDs.length:jTableStrategy.getRowCount());i++) {
    		ParamTable origCols = (ParamTable)origData.elementAt((vecIDs.length!=0)?vecIDs[i]:i);
    		ParamTable cols= new ParamTable(origCols);
    		cols.setSingleValue("ID",          jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("ID")));
    		cols.setSingleValue("OmegaStart",  jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("OmegaStart")));
    		cols.setSingleValue("Incr",        jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("Incr")));
    		cols.setSingleValue("Time",        jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("Time")));
    		cols.setSingleValue("Images",      jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("Images")));
    		cols.setSingleValue("1st img",     jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("1st img")));
    		cols.setSingleValue("Resolution",  jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("Resolution")));
    		cols.setSingleValue("Kappa",       jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("Kappa")));
    		cols.setSingleValue("Phi",         jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("Phi")));
    		cols.setSingleValue("Completeness",jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("Completeness")));
    		cols.setSingleValue("rank",        jTableStrategy.getValueAt((vecIDs.length!=0)?vecIDs[i]:i,tmodel.getColId("rank")));
    		cols.setSingleValue("strategy_data",strategyData.elementAt((vecIDs.length!=0)?vecIDs[i]:i));
    		orientations.addElement(cols);
    	}
    	
    	return orientations;
    	
    }


	public void Button_actionAdapter(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
	        Stac_Out.println("USER ACTION: Button pressed (STAC Strategy Panel: "+((JButton)e.getSource()).getText()+")");			
		}
		if (e.getSource()==this.buttonClearTable) {
			this.clearTable();
	    }
	}

	public void setConfig(Stac_Configuration config) {
		strategyTableModel.setConfig(config);
		
	}

}

