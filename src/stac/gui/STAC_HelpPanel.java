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
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;



public class STAC_HelpPanel extends JPanel 
implements HyperlinkListener {

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
    JTable jTable1 = new JTable(alignTableModel);
    JScrollPane jpanelTable = new JScrollPane(jTable1);
    StrategyTableCellRenderer generalTableCellRenderer = new StrategyTableCellRenderer(true);
    
    
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
    
    
    public String loadText() throws MalformedURLException {
    	String text = new String();
    	//text.concat("<HTML><BODY>sajt <b>STAC</b> itt</BODY></HTML>");
    	File f = new File(System.getProperty("STACDIR")+File.separator+"doc"+File.separator+"manual.html");
    	return f.toURL().toString();
    }
    
    
    public STAC_HelpPanel() {
		
    	
	    this.setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.PAGE_AXIS));
	    this.setBorder(BorderFactory.createRaisedBevelBorder());
	    this.add(Box.createRigidArea(new Dimension(10,10)));
	    
	    try {
	        JEditorPane textArea = new JEditorPane();
	        textArea.setEditable(false);
	        textArea.addHyperlinkListener(this);
	        JScrollPane scroller = new JScrollPane(textArea);
	        textArea.setPage(loadText());
	        this.add(scroller);
	    } catch (Exception err) {
    	   JLabel nohelpLabel = new JLabel("STAC help file is not installed properly under $STACDIR/doc/");
    	   this.add(nohelpLabel);
	    }
		
		
	    this.add(Box.createRigidArea(new Dimension(10,10)));
	}
	
    
    


	public void hyperlinkUpdate(HyperlinkEvent e) {
		// TODO Auto-generated method stub
		
	}
    
}


