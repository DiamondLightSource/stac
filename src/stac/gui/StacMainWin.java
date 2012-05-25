package stac.gui;

import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardAction;
import org.pietschy.wizard.models.StaticModel;
import org.pietschy.wizard.pane.WizardPaneStep;
import org.sudol.sun.util.*;
import stac.core.*;
import stac.modules.BCM.*;
import stac.modules.Alignment.*;
import stac.modules.Strategy.*;
import stac.vbcm.vBCM;

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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.lang.management.ThreadInfo;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;

class MotorWidget extends JPanel {
	public StacBCM bcm;
	String motorName="";
	boolean rotMotor=true;
	ParamTable motorParams;
	
	JButton title   = new JButton ();
	JCheckBox sync  = new JCheckBox("Sync");
	JButton goLeft  = new JButton ();
	JSlider mvThere = new JSlider ();
	JButton goRight = new JButton ();
	JTextField stepSize = new JTextField();	
	JButton getPos  = new JButton ();
	JTextField whereToGo = new JTextField();	
	JButton goThere = new JButton ();
	
    Dimension rbSize = new Dimension(100,30);
    Dimension rbdSize = new Dimension(200,30);
    Dimension fsbSize = new Dimension(30,30);
    Dimension fsdbSize = new Dimension(60,30);
    Dimension tfSize = new Dimension(1000,30);
    Dimension tfnSize = new Dimension(10,30);
    Dimension slnSize = new Dimension(50,50);
    Dimension slxSize = new Dimension(1000,50);
	
	public MotorWidget(String motor,boolean rotMotor,StacBCM stacbcm) {
		
		//gui
		
		mvThere.setEnabled(false);
		
	    setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.LINE_AXIS));
	    //title
	    add(title,null);
		title.setMinimumSize(rbSize);
		title.setMaximumSize(rbSize);
		title.setPreferredSize(rbSize);
		title.setToolTipText("Resyncronise with BCM");
		title.addActionListener(new MotorWidget_actionAdapter(this));
		title.setEnabled(true);
	    //synch
		if (rotMotor) {
			add(sync,null);
		}
		sync.setMinimumSize(fsdbSize);
		sync.setMaximumSize(fsdbSize);
		sync.setPreferredSize(fsdbSize);
		sync.setToolTipText("Syncronise the movement with XYZ to keep the point centered");
		sync.addActionListener(new MotorWidget_actionAdapter(this));
		sync.setEnabled(true);
		sync.setSelected(true);
		//goLeft
		add(goLeft,null);
		goLeft.setText("<<");
		goLeft.setMinimumSize(fsdbSize);
		goLeft.setMaximumSize(fsdbSize);
		goLeft.setPreferredSize(fsdbSize);
		goLeft.setToolTipText("Decrease the Position by the Given Distance");
		goLeft.addActionListener(new MotorWidget_actionAdapter(this));
		goLeft.setEnabled(true);
		//mvThere
		add(mvThere,null);
        mvThere.addChangeListener(new MotorWidget_actionAdapter(this));
        mvThere.setPaintTicks(true);
        mvThere.setPaintLabels(true);
        mvThere.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        mvThere.setSnapToTicks(false);
        mvThere.setMinimumSize(slnSize);
        mvThere.setMaximumSize(slxSize);
        mvThere.setEnabled(true);
		//goRight
		add(goRight,null);
		goRight.setText(">>");
		goRight.setMinimumSize(fsdbSize);
		goRight.setMaximumSize(fsdbSize);
		goRight.setPreferredSize(fsdbSize);
		goRight.setToolTipText("Increase the Position by the Given Distance");
		goRight.addActionListener(new MotorWidget_actionAdapter(this));
		goRight.setEnabled(true);
		//stepSize
		add(stepSize,null);
		if (rotMotor)
			stepSize.setText("10.0");
		else
			stepSize.setText("0.05");
		stepSize.setToolTipText("Change the Size of the Steps");
		stepSize.setMinimumSize(rbSize);
		stepSize.setMaximumSize(rbSize);
		stepSize.setPreferredSize(rbSize);
		//getPos
		add(getPos,null);
		getPos.setText("GetPos");
		getPos.setMinimumSize(rbSize);
		getPos.setMaximumSize(rbSize);
		getPos.setPreferredSize(rbSize);
		getPos.setToolTipText("Get the Current Position");
		getPos.addActionListener(new MotorWidget_actionAdapter(this));
		getPos.setEnabled(true);
		//whereToGo
		add(whereToGo,null);
		whereToGo.setToolTipText("Current/[Candidate] Position");
		whereToGo.setMinimumSize(rbSize);
		whereToGo.setMaximumSize(rbSize);
		whereToGo.setPreferredSize(rbSize);
		//goThere
		add(goThere,null);
		goThere.setText("Mv to");
		goThere.setMinimumSize(rbSize);
		goThere.setMaximumSize(rbSize);
		goThere.setPreferredSize(rbSize);
		goThere.setToolTipText("Move to the Specified Position");
		goThere.addActionListener(new MotorWidget_actionAdapter(this));
		goThere.setEnabled(true);
		
		activate(motor,rotMotor,stacbcm);
		
		mvThere.setEnabled(true);
		
		stacbcm.registerMotorListener(motor,new MotorWidget_actionAdapter(this));
	}
	
	public void setTFwhereToGo(String text) {
		whereToGo.setText(text);
		return;
	}
	
	public String getTFwhereToGo() {
		return whereToGo.getText();
	}
	
	void changePosition(double chg,boolean relative){
		if(!mvThere.isEnabled())
			return;
		double pos=chg;
		if(relative){
			pos+=getPosition();
		}
		if (rotMotor && sync.isSelected() && !this.motorName.equalsIgnoreCase("omega")) {
			ParamTable posTable= new ParamTable();
	    	posTable.setSingleDoubleValue(this.motorName,pos);
			bcm.moveToNewDatumCenteredSync(posTable);
		} else {
			bcm.moveMotor(motorName,pos);
			getPosition();
		}
	}
	
	double getPosition() {
		double pos=bcm.getMotorPosition(motorName);
		
		monitorPosition(pos);
		
		return pos;
	}
	
	void monitorPosition(double pos) {
		mvThere.setEnabled(false);
		
		mvThere.setValue((int)pos);
		whereToGo.setText(new PrintfFormat("%.3f").sprintf(pos));
		
		mvThere.setEnabled(true);
		return ;
	}
	
	void activate(String motor,boolean rotMotor,StacBCM stacbcm) {
		//get init params
		motorName=motor;
		this.rotMotor=rotMotor;
		bcm=stacbcm;
		motorParams=bcm.getMotorParams(motorName);
		
		//gui
		
		mvThere.setEnabled(false);

	    title.setText(motorName+":");
		
		if (rotMotor) {
			//mvThere
			mvThere.setMajorTickSpacing(90); //degrees
			mvThere.setMinorTickSpacing(10);
	  		if ( (int) motorParams.getFirstDoubleValue("minpos")==0.0 && (int) motorParams.getFirstDoubleValue("maxpos")==0.0) {
				mvThere.setMinimum(0);
	  			mvThere.setMaximum(360);
	  		} else {
				mvThere.setMinimum(Math.max(-600,(int) motorParams.getFirstDoubleValue("minpos")));
	  			mvThere.setMaximum(Math.min(600,(int) motorParams.getFirstDoubleValue("maxpos")));
	  		}
			mvThere.setValue((int) motorParams.getFirstDoubleValue("pos"));
			//whereToGo
			whereToGo.setText(new PrintfFormat("%.3f").sprintf(motorParams.getFirstDoubleValue("pos")));
		} else {
			//mvThere
			mvThere.setMajorTickSpacing(2); //millimeters
			mvThere.setMinorTickSpacing(1);
			mvThere.setMinimum(Math.max(-50,(int) motorParams.getFirstDoubleValue("minpos")));
			mvThere.setMaximum(Math.min(50,(int) motorParams.getFirstDoubleValue("maxpos")));
			mvThere.setValue((int) motorParams.getFirstDoubleValue("pos"));
			//whereToGo
			whereToGo.setText(new PrintfFormat("%.3f").sprintf(motorParams.getFirstDoubleValue("pos")));
		}
		mvThere.setEnabled(true);
		
	}
}

class CollisionWidget extends JPanel implements STAC_GUI_Button_Panel {
	
    Dimension rbSize = new Dimension(100,30);
    Dimension rbdSize = new Dimension(200,30);
    Dimension fsbSize = new Dimension(30,30);
    Dimension fsdbSize = new Dimension(60,30);
    Dimension tfSize = new Dimension(1000,30);
    Dimension tfnSize = new Dimension(10,30);
    Dimension slnSize = new Dimension(50,50);
    Dimension slxSize = new Dimension(1000,50);

	  STAC_GUI_JLabel jLabelDet = new STAC_GUI_JLabel();
	  JTextField jTFDet = new JTextField("0.05 0.10 0.15 0.20");
	  STAC_GUI_JLabel jLabelCMRes = new STAC_GUI_JLabel();
	  JTextField jTFCMREs = new JTextField("10");
	  STAC_GUI_JButton jButtonCM = new STAC_GUI_JButton("CM Calculate",null);
	
	  StacBCM bcm=null;
    
	public CollisionWidget(Stac_Configuration conf,StacBCM bcm) {
		
		this.bcm=bcm;
		
		//gui
		
	    this.setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.LINE_AXIS));

	    add(jLabelDet, null);
	    jLabelDet.setText("CM Detector:");
	    jLabelDet.setMinimumSize(rbSize);
	    jLabelDet.setMaximumSize(rbSize);
	    jLabelDet.setPreferredSize(rbSize);
	    //jLabelDet.setToolTipText("Start to work with a new Xtal");
		jLabelDet.setConfig(conf);
	    
	    add(jTFDet, null);
	    jTFDet.setToolTipText("Space separated list of Detector distances for the Collision Map generation");
	    jTFDet.setMinimumSize(tfnSize);
	    jTFDet.setMaximumSize(tfSize);
	    jTFDet.setPreferredSize(tfSize);
	    jTFDet.setEditable(true);
		
		add(jLabelCMRes, null);
	    jLabelCMRes.setText("CM Resolution:");
	    jLabelCMRes.setMinimumSize(rbSize);
	    jLabelCMRes.setMaximumSize(rbSize);
	    jLabelCMRes.setPreferredSize(rbSize);
	    //jLabelCMRes.setToolTipText("Start to work with a new Xtal");
	    jLabelCMRes.setConfig(conf);
	    
	    add(jTFCMREs, null);
	    jTFCMREs.setToolTipText("Angular resolution for Collision Map generations (in degrees)");
	    jTFCMREs.setMinimumSize(tfnSize);
	    jTFCMREs.setMaximumSize(tfSize);
	    jTFCMREs.setPreferredSize(tfSize);
	    jTFCMREs.setEditable(true);

	    add(jButtonCM, null);
	    jButtonCM.addActionListener(new Stac_Button_actionAdapter(this));
	    jButtonCM.setMinimumSize(new Dimension(100,34));
	    jButtonCM.setMaximumSize(new Dimension(100,34));
	    jButtonCM.setPreferredSize(new Dimension(100,34));
	    //jButtonCM.setToolTipText("Calculate Collision Map\n + for the given Detector distances\n + with the given Rotational resolution");
	    jButtonCM.setToolTipText("Calculate Collision Map for the given Detector distances with the given Rotational resolution");
	    jButtonCM.setConfig(conf);
	    
	}


	public void Button_actionAdapter(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
	        Stac_Out.println("USER ACTION: Button pressed ("+((JButton)e.getSource()).getText()+")");			
		}
    	if (e.getSource()==this.jButtonCM) {
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
			StringTokenizer st=new StringTokenizer(jTFDet.getText());
			Double [] det=new Double[st.countTokens()];
			for(int i=0;i<det.length;i++) {
				det[i]=new Double(st.nextToken());
			}
    		bcm.generateCCPNCollisionMap("CM_STORAGE_"+actDateStr,det,new Double(jTFCMREs.getText()).doubleValue());
    	} 
    	
	}
	
}



class Stac_Button_actionAdapter implements java.awt.event.ActionListener {
	STAC_GUI_Button_Panel adaptee;

	  Stac_Button_actionAdapter(STAC_GUI_Button_Panel adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void actionPerformed(ActionEvent e) {
	  	adaptee.Button_actionAdapter(e);
	  }
}

class Stac_FileChooser implements java.awt.event.ActionListener {
	STAC_GUI_File_Chooser_Panel adaptee;
	
	Stac_FileChooser(STAC_GUI_File_Chooser_Panel adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.FileChooser_actionPerformed(e);
	}
}


class StacMainRespHandler extends Stac_RespHandler {
	StacMainWin mainWin=null;

	StacMainRespHandler(StacMainWin mainWin) {
		this.mainWin=mainWin;
	}
	
	
	@Override
	protected void handleResponse(Stac_RespMsg response) {
		if (response instanceof Stac_AlignmentRespMsg)
		{
			Stac_AlignmentRespMsg alignResp=(Stac_AlignmentRespMsg) response;
			String alignFile="";
			String workDir=alignResp.req.outdir;
			if (alignResp.status==alignResp.OK) {
				alignFile=workDir+alignResp.possibleDatumsFile;				
			    mainWin.ReadCalculatedAlignments(alignFile,response.requestId);
			} else {
				mainWin.session.addErrorMsg(response.requestId,"Response status = "+response.statusToString());
			}
		} else if (response instanceof Stac_StrategyRespMsg)
		{
			Stac_StrategyRespMsg resp=(Stac_StrategyRespMsg) response;
			String strFile="";
			String workDir=resp.req.outdir;
			if (resp.status==resp.OK) {
				strFile=workDir+resp.strategyFile;				
			    mainWin.ReadCalculatedStrategies(strFile,response.requestId);
			} else {
				mainWin.session.addErrorMsg(response.requestId,"Response status = "+response.statusToString());
			}
		} else if (response instanceof Stac_OptStrategyOrientRespMsg)
		{
//	    	Vector OM = utils.getOptAlign(".");
			
//	    	if (OM!=null && OM.size()>=3) {
//	    		for (int ct=0;ct+3<=OM.size();ct+=3) {
//	    			int rowct=this.jTableReorient.getRowCount();
//	    			ReorientTableModel tmodel = (ReorientTableModel) this.jTableReorient.getModel();
//	    			this.jTableReorient.setValueAt(OM.elementAt(ct+0), rowct, tmodel.getColId("V1"));
//	    			this.jTableReorient.setValueAt(OM.elementAt(ct+1), rowct, tmodel.getColId("V2"));
//	    			this.jTableReorient.setValueAt(OM.elementAt(ct+2), rowct, tmodel.getColId("Comment"));
//	    		}
//	    	}
	    	
	    	Stac_OptStrategyOrientRespMsg resp=(Stac_OptStrategyOrientRespMsg) response;
	    	
			if (resp.status==resp.OK) {
		    	Vector OM = resp.get_optAlign();
		    	mainWin.jPanelReorient.addOrientationRequest(OM);
			} else {
				mainWin.session.addErrorMsg(response.requestId,"Response status = "+response.statusToString());
			}
		}		
		
	}


}



public class StacMainWin
    extends JPanel 
	implements STAC_GUI_File_Chooser_Panel, STAC_GUI_Button_Panel {

	Stac_Session session =  new Stac_Session(this);
  //StacUtil module
  Util utils= new Util();
  StacBCM  bcm  = new StacBCM(session);
  StacAlignment alignment = new StacAlignment(session);
  StacStrategy  strategy  = new StacStrategy(session);

  StacMainRespHandler respHandler= new StacMainRespHandler(this);
  //Main UI panel
  //JPanel contentPane;

  //Tabbed pane and layouts for pane 1 and pane 2
  //Pane 3 uses a null layout
  JTabbedPane jTabbedPane1 = new JTabbedPane();
  
  //Motor Access
  //JPanel jPanelMotorAccess =  new JPanel();
  JButton jButtonMA_init = new JButton();
  JButton jButtonMA_getall = new JButton();
  JButton jButtonMA_clear = new JButton();
  JButton jButtonMA_mvsync = new JButton();  
  MotorWidget mvomega = new MotorWidget("Omega",true,bcm);
  MotorWidget mvkappa = new MotorWidget("Kappa",true,bcm);
  MotorWidget mvphi = new MotorWidget("Phi",true,bcm);
  MotorWidget mvx = new MotorWidget("X",false,bcm);
  MotorWidget mvy = new MotorWidget("Y",false,bcm);
  MotorWidget mvz = new MotorWidget("Z",false,bcm);
  JButton jButtonMA_TiltLeft = new JButton();  
  JButton jButtonMA_TiltRight = new JButton();
  JButton jButtonMA_AutoCenter = new JButton();
  CollisionWidget coll = new CollisionWidget(session.gui_config,bcm);
  
  
  //rest
  JPanel jPanelGonioCalib = new JPanel();
  JPanel jPanelGonioTCalib = new JPanel();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel1_1 = new JPanel();
  STAC_DatumPanel datumPanel = new STAC_DatumPanel(utils,bcm,this);
  JPanel jPanel4 = new JPanel();
  FlowLayout flowLayout1_panelGonioCalib = new FlowLayout();
  FlowLayout flowLayout1_panelGonioTCalib = new FlowLayout();
  FlowLayout flowLayout1_panel1 = new FlowLayout();
  FlowLayout flowLayout1_panel1_1 = new FlowLayout();
  FlowLayout flowLayout1_panel3 = new FlowLayout();
  FlowLayout flowLayout1_panel4 = new FlowLayout();
  JPanel panelGonioCalib = new JPanel(flowLayout1_panelGonioCalib);
  JPanel panelGonioCalibS = new JPanel();
  JPanel panelGonioCalibW = new JPanel();
  JPanel panelGonioCalibE = new JPanel();
  JPanel panelGonioCalibN = new JPanel();
  JPanel panelGonioTCalib = new JPanel(flowLayout1_panelGonioTCalib);
  JPanel panelGonioTCalibS = new JPanel();
  JPanel panelGonioTCalibW = new JPanel();
  JPanel panelGonioTCalibE = new JPanel();
  JPanel panelGonioTCalibN = new JPanel();
  JPanel panel1 = new JPanel(flowLayout1_panel1);
  JPanel panel1S = new JPanel();
  JPanel panel1W = new JPanel();
  JPanel panel1E = new JPanel();
  JPanel panel1N = new JPanel();
  JPanel panel1_1 = new JPanel(flowLayout1_panel1_1);
  JPanel panel1_1S = new JPanel();
  JPanel panel1_1W = new JPanel();
  JPanel panel1_1E = new JPanel();
  JPanel panel1_1N = new JPanel();
  JPanel panel3 = new JPanel(flowLayout1_panel3);
  JPanel panel3S = new JPanel();
  JPanel panel3W = new JPanel();
  JPanel panel3E = new JPanel();
  JPanel panel3N = new JPanel();
  JPanel panel4 = new JPanel(flowLayout1_panel4);
  JPanel panel4S = new JPanel();
  JPanel panel4W = new JPanel();
  JPanel panel4E = new JPanel();
  JPanel panel4N = new JPanel();


  //Radio buttons on pane 1; buttons are in a
  //mutually exclusive button group
//  JRadioButton jRadioButton1 = new JRadioButton();
//  ButtonGroup buttonGroup1 = new ButtonGroup();
//  JLabel jLabel1 = new JLabel();
//  JButton jButton1 = new JButton();
//  TitledBorder titledBorder1;
//  String[] columnNames = {"First Name", "Last Name"};
//  Object[][] dataTable = {{"First Name 1", "Last Name 1"},};
//  JTable jTable1 = new JTable(dataTable, columnNames);
//  JScrollPane scrollPane1 = new JScrollPane(jTable1);
  JLabel jLabelInputFormat = new JLabel();
  JButton jButton1 = new JButton();
  JButton jButton1_1 = new JButton();
  JButton jButtonGonioCalib = new JButton();
  JButton jButtonGnsdefUpdate = new JButton();
  JButton jButtonGonioAutoTCalib = new JButton();
  JButton jButtonGonioAutoRCalib = new JButton();
  JButton jButtonGonioTCalib = new JButton();
  JButton jButtonGnsdefTUpdate = new JButton();
  JLabel jLabel2 = new JLabel();
  JButton jButton2 = new JButton();
  JButton jButtonR_getPos = new JButton();
  ButtonGroup buttonGroupInputFormats = new ButtonGroup();
  ButtonGroup buttonGroupInputFormatsMat0 = new ButtonGroup();
  ButtonGroup buttonGroupInputFormatsMat1 = new ButtonGroup();
  ButtonGroup buttonGroupInputFormatsMat2 = new ButtonGroup();
  ButtonGroup buttonGroupInputFormatsMat3 = new ButtonGroup();
  JRadioButton jRadioButtonMosflm = new JRadioButton();
  JRadioButton jRadioButtonDenzo = new JRadioButton();
  JRadioButton jRadioButtonXDS = new JRadioButton();
  JLabel jLMat0 = new JLabel();
  JLabel jLMat1 = new JLabel();
  JLabel jLMat2 = new JLabel();
  JLabel jLMat3 = new JLabel();
  JRadioButton jRBMat0Mosflm = new JRadioButton();
  JRadioButton jRBMat0Denzo = new JRadioButton();
  JRadioButton jRBMat0XDS = new JRadioButton();
  JRadioButton jRBMat1Mosflm = new JRadioButton();
  JRadioButton jRBMat1Denzo = new JRadioButton();
  JRadioButton jRBMat1XDS = new JRadioButton();
  JRadioButton jRBMat2Mosflm = new JRadioButton();
  JRadioButton jRBMat2Denzo = new JRadioButton();
  JRadioButton jRBMat2XDS = new JRadioButton();
  JRadioButton jRBMat3Mosflm = new JRadioButton();
  JRadioButton jRBMat3Denzo = new JRadioButton();
  JRadioButton jRBMat3XDS = new JRadioButton();
  JTextField jTFMat0File =  new JTextField();
  JTextField jTFMat1File =  new JTextField();
  JTextField jTFMat2File =  new JTextField();
  JTextField jTFMat3File =  new JTextField();
  JButton jBMat0Sel = new JButton("",createImageIcon("images/folder.gif"));
  JButton jBMat1Sel = new JButton("",createImageIcon("images/folder.gif"));
  JButton jBMat2Sel = new JButton("",createImageIcon("images/folder.gif"));
  JButton jBMat3Sel = new JButton("",createImageIcon("images/folder.gif"));
  JTextField jTFMosflmSettingsFile =  new JTextField();
  JTextField jTFGnsDatFile =  new JTextField();
  JTextField jTextFieldDefFile = new JTextField();
  JLabel jLabel3 = new JLabel();
//  JTextField jTextFieldDefFile1 = new JTextField();
  JLabel jLabelA = new JLabel(); //Angles
  JTextField jTFOmega = new JTextField();
  JTextField jTFKappa = new JTextField();
  JTextField jTFPhi   = new JTextField();
  JTextField jTFTransX   = new JTextField();
  JTextField jTFTransY   = new JTextField();
  JTextField jTFTransZ   = new JTextField();
  JTextField jTFGCOmega = new JTextField();
  JTextField jTFGCKappa = new JTextField();
  JTextField jTFGCPhi = new JTextField();
//  //vector evaluation
//  JTextField jTFEvalO=new JTextField();
//  JTextField jTFEvalK=new JTextField();
//  JTextField jTFEvalP=new JTextField();
//  JTextField jTFEvalX=new JTextField();
//  JTextField jTFEvalY=new JTextField();
//  JTextField jTFEvalZ=new JTextField();  
//  JButton jButtonEval_getP = new JButton();
//  JTextField jTFEvalO2=new JTextField();
//  JTextField jTFEvalK2=new JTextField();
//  JTextField jTFEvalP2=new JTextField();
//  JTextField jTFEvalX2=new JTextField();
//  JTextField jTFEvalY2=new JTextField();
//  JTextField jTFEvalZ2=new JTextField();  
//  JButton jButtonEval_getP2 = new JButton();
  
  JTextField jTFCPar = new JTextField();    
  JTextField jTFCPar2 = new JTextField();    
  JTextField jTFCPar3 = new JTextField();    
  
  
  JButton jButtonGC_init = new JButton();
  JCheckBox jChkBoxGC_sync = new JCheckBox("Translation Corr");
  //Gonio Calibration Calculated Results
  JTextField jTFGCRomegaX=new JTextField();
  JTextField jTFGCRomegaY=new JTextField();
  JTextField jTFGCRomegaZ=new JTextField();
  JTextField jTFGCRomega=new JTextField();
  JTextField jTFGCRDomega=new JTextField();
  JTextField jTFGCRDomegaT=new JTextField();
  JTextField jTFGCRkappaX=new JTextField();
  JTextField jTFGCRkappaY=new JTextField();
  JTextField jTFGCRkappaZ=new JTextField();
  JTextField jTFGCRkappa=new JTextField();
  JTextField jTFGCRDkappa=new JTextField();
  JTextField jTFGCRDkappaT=new JTextField();
  JTextField jTFGCRphiX=new JTextField();
  JTextField jTFGCRphiY=new JTextField();
  JTextField jTFGCRphiZ=new JTextField();
  JTextField jTFGCRphi=new JTextField();
  JTextField jTFGCRDphi=new JTextField();
  JTextField jTFGCRDphiT=new JTextField();
  JButton jButton3 = new JButton("",createImageIcon("images/folder.gif"));
  //JButton jButton4 = new JButton("",createImageIcon("images/folder.gif"));
  JButton jButton5 = new JButton();
  JButton jBMosflmSettingsFile = new JButton("",createImageIcon("images/folder.gif"));
  JButton jBGnsDatFile = new JButton("",createImageIcon("images/folder.gif"));
//  AlignTableModel alignTableModel = new AlignTableModel();
//  JLabel jLabelAlignVector = new JLabel();
//  JTable jTable1 = new JTable(alignTableModel);
//  JScrollPane jpanelTable = new JScrollPane(jTable1);
  JButton jButton31 = new JButton();
  JButton jButton32 = new JButton();
  BorderLayout borderLayout1 = new BorderLayout();

  //GRC
  JButton jButtonGRC_mvN = new JButton();
  JButton jButtonGRC_mvP = new JButton();
  JButton jButtonGRC_mvK = new JButton();
  JButton jButtonGRC_mvO = new JButton();
  JButton jButtonGRC_getT = new JButton();
  

  //GTC
  JButton jButtonTGC_init = new JButton();  
  JButton jButtonGTC_mvreset = new JButton();
  JButton jButtonGTC_reset = new JButton();
  JButton jButtonGTC_mvP = new JButton();
  JButton jButtonGTC_getP = new JButton();
  JButton jButtonGTC_mvK = new JButton();
  JButton jButtonGTC_getK = new JButton();
  JButton jButtonGTC_calcRot = new JButton();
  JTextField jTFGTCInpP = new JTextField();
  JTextField jTFGTCInpPX = new JTextField();
  JTextField jTFGTCInpPY = new JTextField();
  JTextField jTFGTCInpPZ = new JTextField();
  JTextField jTFGTCInpK = new JTextField();
  JTextField jTFGTCInpKX = new JTextField();
  JTextField jTFGTCInpKY = new JTextField();
  JTextField jTFGTCInpKZ = new JTextField();
  JTextField jTFGTCRphi=new JTextField();
  JTextField jTFGTCRphiRecE=new JTextField();
  JTextField jTFGTCRphiX=new JTextField();
  JTextField jTFGTCRphiY=new JTextField();
  JTextField jTFGTCRphiZ=new JTextField();
  JTextField jTFGTCRkappa=new JTextField();
  JTextField jTFGTCRkappaRecE=new JTextField();
  JTextField jTFGTCRkappaX=new JTextField();
  JTextField jTFGTCRkappaY=new JTextField();
  JTextField jTFGTCRkappaZ=new JTextField();
  JTextField jTFGTCRkappaXR=new JTextField();
  JTextField jTFGTCRkappaYR=new JTextField();
  JTextField jTFGTCRkappaZR=new JTextField();
  JTextField jTFGTCRDkappaR=new JTextField();
  JTextField jTFGTCRDkappaTR=new JTextField();
  JTextField jTFGTCRphiXR=new JTextField();
  JTextField jTFGTCRphiYR=new JTextField();
  JTextField jTFGTCRphiZR=new JTextField();
  JTextField jTFGTCRDphiR=new JTextField();
  JTextField jTFGTCRDphiTR=new JTextField();
  
  TranslationWidget jPanelTransPhi= new TranslationWidget(bcm,utils,"Phi");
  TranslationWidget jPanelTransKappa= new TranslationWidget(bcm,utils,"Kappa");
  

  //Input panel
  JTextField jTextFieldDescDefFile=new JTextField();  
  JButton jButtonDesc = new JButton("",createImageIcon("images/folder.gif"));
  STAC_GUI_JButton jButtonNewXtal = new STAC_GUI_JButton("NEW XTAL",session.gui_config);
  JButton jButtonDescLoad = new JButton("Load");
  JButton jButtonDescSave = new JButton("Rewrite");
  STAC_OrientationPanel jPanelReorient = new STAC_OrientationPanel(utils,bcm,this,session.gui_config);  
//  JButton jButtonRLeftAdd = new JButton("Add New");
//  JButton jButtonRLeftDelete = new JButton("Del Selected");
//  JTextField jTextFieldRGF=new JTextField();  
//  JButton jButtonRGF = new JButton("",createImageIcon("images/folder.gif"));
//  JButton jButtonRLeftGetFile = new JButton("Get a new from file:");
//  ReorientTableModel reorientTableModel = new ReorientTableModel();
//  JTable jTableReorient = new JTable(reorientTableModel);
//  JScrollPane jpanelReorientTable = new JScrollPane(jTableReorient);

  
  
  
  
  JButton jButton41 = new JButton();
  JButton jButton42 = new JButton();
  StrategyTableCellRenderer strategyTableCellRenderer = new StrategyTableCellRenderer();
  StrategyTableCellRenderer generalTableCellRenderer = new StrategyTableCellRenderer(true);
  StrategyTableModel strategyTableModel = new StrategyTableModel();
//  JLabel jLabelStrategy = new JLabel();
//  JTable jTableStrategy = new JTable(strategyTableModel);
//  JScrollPane jpanelTableStrategy = new JScrollPane(jTableStrategy);
  STAC_StrategyPanel strategyWidget = new STAC_StrategyPanel(utils,bcm,this);

  
  //options
  JCheckBox chkboxCalibration = new JCheckBox("Activate Calibration Tabs");
  JButton buttonOpenJobCotrolWin = new JButton("Open Job Cotrol Window");
  
  //help
  
  //about  
  

  //Construct the frame
  public StacMainWin() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  public String getVersion() {
	  try {
		  return "v"+utils.opReadCl(System.getProperty("STACDIR")+File.separatorChar+"version").split("\n")[0];
	  } catch (Exception e) {
		  return "v2";
	  }	  
  }
  
  /**
   * Component initialization
   *
   * @throws Exception exception
   */
  private void jbInit() throws Exception {
    Dimension rbSize = new Dimension(100,30);
    Dimension rbdSize = new Dimension(200,30);
    Dimension fsbSize = new Dimension(30,30);
    Dimension tfSize = new Dimension(1000,30);
    Dimension tfnSize = new Dimension(10,30);



    //Initialize main UI frame
    //contentPane = (JPanel)this.getContentPane();
    //contentPane.setLayout(new BorderLayout());
    this.setLayout(new BorderLayout());
    this.setSize(new Dimension(1000, 700));

    //Initialize tabbed pane
    this.add(jTabbedPane1, BorderLayout.CENTER);
    //jTabbedPane1.setBounds(new Rectangle(30, 30, 630, 330));
    //jTabbedPane1.;
    JPanel panelMotorAccess = new JPanel();
    //JPanel panelGonioCalib= new JPanel();
    jTabbedPane1.addTab("Gonio TransCalib", panelGonioTCalib );
    jTabbedPane1.addTab("Gonio RotCalib", panelGonioCalib );
    jTabbedPane1.addTab("Motor Access", panelMotorAccess);
    jTabbedPane1.addTab("Input for Re-Orientation", panel1);
    //jTabbedPane1.addTab("Re-Orientation", panel1_1);
    jTabbedPane1.addTab("Vector Evaluation", panel3);
    jTabbedPane1.addTab("Strategy", panel4);

    //options
    JPanel panelO= new JPanel();
    jTabbedPane1.addTab("Options", panelO);
    panelO.setLayout(new GridLayout());
    panelO.add(buttonOpenJobCotrolWin);
    buttonOpenJobCotrolWin.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    buttonOpenJobCotrolWin.setEnabled(true);
    panelO.add(chkboxCalibration);
    chkboxCalibration.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    chkboxCalibration.setEnabled(true);
    chkboxCalibration.setSelected(true);    
    chkboxCalibration.doClick();
    
    //help
    JPanel panelH= new JPanel();
    jTabbedPane1.addTab("Help", panelH);
    panelH.setLayout(new GridLayout());
    jTabbedPane1.setEnabledAt(jTabbedPane1.getTabCount()-1,true);
    STAC_HelpPanel helpPanel = new STAC_HelpPanel();
    panelH.add(helpPanel);
    
    //about
    JPanel panelA= new JPanel();
    jTabbedPane1.addTab("About", panelA);
    panelA.setLayout(new GridLayout());
    JLabel aboutLabel = new JLabel("STAC is written by Sandor Brockhauser (brockhauser@embl.fr)");
    panelA.add(aboutLabel);
    
    
    
    
    //MotorAccess
    panelMotorAccess.setLayout(new BoxLayoutFixed(panelMotorAccess,BoxLayoutFixed.LINE_AXIS));
    panelMotorAccess.add(Box.createRigidArea(new Dimension(10,10)));
    JPanel panelMACenter = new JPanel();
    panelMotorAccess.add(panelMACenter);
    panelMotorAccess.add(Box.createRigidArea(new Dimension(10,10)));
    //Center
    panelMACenter.setLayout(new BoxLayoutFixed(panelMACenter,BoxLayoutFixed.PAGE_AXIS));
    panelMACenter.add(Box.createRigidArea(new Dimension(10,10)));
    JPanel panelMAC = new JPanel();
    JPanel panelMAHead = new JPanel();
    panelMAHead.setLayout(new BoxLayoutFixed(panelMAHead,BoxLayoutFixed.LINE_AXIS));
    panelMAHead.add(Box.createRigidArea(new Dimension(10,10)));
    //init
    jButtonMA_init.setText("Init Motors");
    jButtonMA_init.setPreferredSize(new Dimension(100,34));
    jButtonMA_init.setToolTipText("Initializing the Motors");
    jButtonMA_init.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonMA_init.setEnabled(true);
    panelMAHead.add(jButtonMA_init, null);
    panelMAHead.add(Box.createHorizontalGlue());
    //GetAll
    jButtonMA_getall.setText("GetAll");
    jButtonMA_getall.setPreferredSize(new Dimension(100,34));
    jButtonMA_getall.setToolTipText("Refreshing all the motor positions");
    jButtonMA_getall.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonMA_getall.setEnabled(true);
    panelMAHead.add(jButtonMA_getall, null);
    //Clear
    jButtonMA_clear.setText("Clear Datum");
    jButtonMA_clear.setPreferredSize(new Dimension(100,34));
    jButtonMA_clear.setToolTipText("Clear the intended Datum");
    jButtonMA_clear.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonMA_clear.setEnabled(true);
    panelMAHead.add(jButtonMA_clear, null);
    //MvSync
    jButtonMA_mvsync.setText("Mv Sync");
    jButtonMA_mvsync.setPreferredSize(new Dimension(100,34));
    jButtonMA_mvsync.setToolTipText("Move to new Datum with keeping the point centered");
    jButtonMA_mvsync.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonMA_mvsync.setEnabled(true);
    panelMAHead.add(jButtonMA_mvsync, null);
    panelMAHead.add(Box.createRigidArea(new Dimension(10,10)));
    //individual motor controllers
    panelMACenter.add(panelMAHead,null);
    panelMACenter.add(panelMAC,null);
    panelMACenter.add(Box.createRigidArea(new Dimension(10,10)));
    //place of button "update" (updating the motorwidgets) 
    panelMACenter.add(Box.createRigidArea(rbSize));    
    panelMACenter.add(Box.createRigidArea(new Dimension(10,10)));    
    //MAC
    panelMAC.setBorder(BorderFactory.createRaisedBevelBorder());
    panelMAC.setLayout(new BoxLayoutFixed(panelMAC,BoxLayoutFixed.PAGE_AXIS));
    panelMAC.add(mvomega,null);
    panelMAC.add(mvkappa,null);
    panelMAC.add(mvphi,null);
    panelMAC.add(mvx,null);
    panelMAC.add(mvy,null);
    panelMAC.add(mvz,null);
    // Tilt
    panelMAC.add(Box.createRigidArea(new Dimension(10,10)));
    JPanel panelMATilt = new JPanel();
    panelMAC.add(panelMATilt);
    panelMATilt.setLayout(new BoxLayoutFixed(panelMATilt,BoxLayoutFixed.LINE_AXIS));
    panelMATilt.add(Box.createRigidArea(new Dimension(10,10)));
    panelMATilt.add(new JLabel("Manual Synchronised Tilt Control: "));
    panelMATilt.add(Box.createRigidArea(new Dimension(10,10)));
    //tiltLeft
    jButtonMA_TiltLeft.setText("<<");
    jButtonMA_TiltLeft.setPreferredSize(new Dimension(60,30));
    jButtonMA_TiltLeft.setToolTipText("Tilt the xtal");
    jButtonMA_TiltLeft.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonMA_TiltLeft.setEnabled(true);
    panelMATilt.add(jButtonMA_TiltLeft, null);
    //tiltRight
    jButtonMA_TiltRight.setText(">>");
    jButtonMA_TiltRight.setPreferredSize(new Dimension(60,30));
    jButtonMA_TiltRight.setToolTipText("Tilt the xtal");
    jButtonMA_TiltRight.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonMA_TiltRight.setEnabled(true);
    panelMATilt.add(jButtonMA_TiltRight, null);
    panelMATilt.add(Box.createHorizontalGlue());
    // Center
    panelMAC.add(Box.createRigidArea(new Dimension(10,10)));
    JPanel panelMACentering = new JPanel();
    panelMAC.add(panelMACentering);
    panelMACentering.setLayout(new BoxLayoutFixed(panelMACentering,BoxLayoutFixed.LINE_AXIS));
    panelMACentering.add(Box.createRigidArea(new Dimension(10,10)));
    panelMACentering.add(new JLabel("Automatic centering: "));
    panelMACentering.add(Box.createRigidArea(new Dimension(10,10)));
    //centerneedle
    jButtonMA_AutoCenter.setText("CalibPeak");
    jButtonMA_AutoCenter.setPreferredSize(new Dimension(100,30));
    jButtonMA_AutoCenter.setToolTipText("Automatic Centering of the Calibration Needle");
    jButtonMA_AutoCenter.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonMA_AutoCenter.setEnabled(true);
    panelMACentering.add(jButtonMA_AutoCenter, null);
    panelMACentering.add(Box.createHorizontalGlue());
    //collision map
    panelMAC.add(Box.createRigidArea(new Dimension(10,10)));
    panelMAC.add(coll,null);
    
    
    
    panelMAC.add(Box.createVerticalGlue());
    
    
    //panelGonioCalib
    //layout
    panelGonioCalib.setLayout(new BorderLayout());
    panelGonioCalib.setBorder(BorderFactory.createRaisedBevelBorder());
    //Main button on South
    panelGonioCalib.add(panelGonioCalibS, BorderLayout.SOUTH);
    panelGonioCalibS.setLayout(new BoxLayoutFixed(panelGonioCalibS,BoxLayoutFixed.LINE_AXIS));
    panelGonioCalibS.add(Box.createHorizontalGlue());
    panelGonioCalibS.add(Box.createHorizontalGlue());
    panelGonioCalibS.add(jButtonGonioAutoRCalib);
    jButtonGonioAutoRCalib.setText("Auto RCalib");
    jButtonGonioAutoRCalib.setPreferredSize(new Dimension(100,34));
    jButtonGonioAutoRCalib.setToolTipText("Automatic Gonio Rotation Calibration");
    jButtonGonioAutoRCalib.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    panelGonioCalibS.add(jButtonGRC_getT);
    jButtonGRC_getT.setText("Get Trans");
    jButtonGRC_getT.setPreferredSize(new Dimension(100,34));
    jButtonGRC_getT.setToolTipText("Get the values from the Translation Calibration");
    jButtonGRC_getT.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    panelGonioCalibS.add(jButtonGonioCalib);
    jButtonGonioCalib.setText("Calculate");
    jButtonGonioCalib.setPreferredSize(new Dimension(100,34));
    jButtonGonioCalib.setToolTipText("Start the Gonio Calibration");
    jButtonGonioCalib.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    panelGonioCalibS.add(jButtonGnsdefUpdate);
    jButtonGnsdefUpdate.setText("Update");
    jButtonGnsdefUpdate.setPreferredSize(new Dimension(100,34));
    jButtonGnsdefUpdate.setToolTipText("Update the Gonio Settings");
    jButtonGnsdefUpdate.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGnsdefUpdate.setEnabled(false);
    panelGonioCalibS.add(Box.createHorizontalGlue());
    //layout
    panelGonioCalib.add(panelGonioCalibW,BorderLayout.WEST);
    panelGonioCalibW.setLayout(new BoxLayoutFixed(panelGonioCalibW,BoxLayoutFixed.PAGE_AXIS));
    panelGonioCalibW.add(Box.createRigidArea(new Dimension(10,10)));
    panelGonioCalib.add(panelGonioCalibE,BorderLayout.EAST);
    panelGonioCalibE.setLayout(new BoxLayoutFixed(panelGonioCalibE,BoxLayoutFixed.PAGE_AXIS));
    panelGonioCalibE.add(Box.createRigidArea(new Dimension(10,10)));
    panelGonioCalib.add(panelGonioCalibN,BorderLayout.NORTH);
    panelGonioCalibN.setLayout(new BoxLayoutFixed(panelGonioCalibN,BoxLayoutFixed.PAGE_AXIS));
    panelGonioCalibN.add(Box.createRigidArea(new Dimension(10,10)));
    panelGonioCalib.add(jPanelGonioCalib, BorderLayout.CENTER);
    jPanelGonioCalib.setBorder(BorderFactory.createRaisedBevelBorder());
    jPanelGonioCalib.setLayout(new BoxLayoutFixed(jPanelGonioCalib,BoxLayoutFixed.PAGE_AXIS));
    jPanelGonioCalib.setAlignmentX(LEFT_ALIGNMENT);
    jPanelGonioCalib.add(Box.createRigidArea(new Dimension(10,10)));
    //LINE Warning
    JPanel jpanelGonioCalibWarning = new JPanel();
    jPanelGonioCalib.add(jpanelGonioCalibWarning);
    jpanelGonioCalibWarning.setLayout(new BoxLayoutFixed(jpanelGonioCalibWarning,BoxLayoutFixed.LINE_AXIS));
    jpanelGonioCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    //L (0;0;0)
    JLabel jLabelGCW = new JLabel("Have you initialized your kappa system?");
    jpanelGonioCalibWarning.add(jLabelGCW, null);
    jpanelGonioCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    jButtonGC_init.setText("Init Motors");
    jButtonGC_init.setPreferredSize(new Dimension(150,34));
    jButtonGC_init.setToolTipText("Initializing the Motors");
    jButtonGC_init.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGC_init.setEnabled(true);
    jpanelGonioCalibWarning.add(jButtonGC_init, null);
    //
    jpanelGonioCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibWarning.add(Box.createHorizontalGlue());
	//jChkBoxGC_sync.setMinimumSize(fsdbSize);
	//jChkBoxGC_sync.setMaximumSize(fsdbSize);
	//jChkBoxGC_sync.setPreferredSize(fsdbSize);
	jChkBoxGC_sync.setToolTipText("Syncronise the rotations with XYZ to keep the point centered");
	jChkBoxGC_sync.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
	jChkBoxGC_sync.setEnabled(true);
	jChkBoxGC_sync.setSelected(true);
    jpanelGonioCalibWarning.add(jChkBoxGC_sync, null);
    jpanelGonioCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    
    //separator
    JPanel jPanelGCSepLine1 = new JPanel();
    jPanelGonioCalib.add(jPanelGCSepLine1);
    jPanelGCSepLine1.setLayout(new BoxLayoutFixed(jPanelGCSepLine1,BoxLayoutFixed.LINE_AXIS));
    jPanelGCSepLine1.add(Box.createRigidArea(new Dimension(10,3)));
    JTextField jTFGCSepL1 = new JTextField();
    jPanelGCSepLine1.add(jTFGCSepL1, null);
    jTFGCSepL1.setEditable(false);
    jTFGCSepL1.setMinimumSize(new Dimension(1,3));
    jTFGCSepL1.setMaximumSize(new Dimension(2000,3));
    jTFGCSepL1.setPreferredSize(new Dimension(1,3));
    jPanelGCSepLine1.add(Box.createRigidArea(new Dimension(10,3)));
    //LINE 0
    JPanel jpanelGonioCalibM = new JPanel();
    jPanelGonioCalib.add(jpanelGonioCalibM);
    jpanelGonioCalibM.setLayout(new BoxLayoutFixed(jpanelGonioCalibM,BoxLayoutFixed.LINE_AXIS));
    jpanelGonioCalibM.add(Box.createRigidArea(new Dimension(10,10)));
    //L (0;0;0)
    jpanelGonioCalibM.add(jLMat0, null);
    jLMat0.setText("Mat(-;-;-):");
    jLMat0.setMinimumSize(rbSize);
    jLMat0.setMaximumSize(rbSize);
    jLMat0.setPreferredSize(rbSize);
    //jPanelGonioCalib.add(Box.createRigidArea(new Dimension(10,10)));    
    //RB MOsflm
    jpanelGonioCalibM.add(jRBMat0Mosflm, null);
    jRBMat0Mosflm.setSelected(true);
    jRBMat0Mosflm.setText("Mosflm");
    jRBMat0Mosflm.setMinimumSize(rbSize);
    jRBMat0Mosflm.setMaximumSize(rbSize);
    jRBMat0Mosflm.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat0.add(jRBMat0Mosflm);
    //RB XDS
    jpanelGonioCalibM.add(jRBMat0XDS, null);
    jRBMat0XDS.setSelected(true);
    jRBMat0XDS.setText("XDS");
    jRBMat0XDS.setMinimumSize(rbSize);
    jRBMat0XDS.setMaximumSize(rbSize);
    jRBMat0XDS.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat0.add(jRBMat0XDS);
    //RB Denzo
    jpanelGonioCalibM.add(jRBMat0Denzo, null);
    jRBMat0Denzo.setSelected(true);
    jRBMat0Denzo.setText("Denzo");
    jRBMat0Denzo.setMinimumSize(rbSize);
    jRBMat0Denzo.setMaximumSize(rbSize);
    jRBMat0Denzo.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat0.add(jRBMat0Denzo);
    //TF MatrixFile
    jpanelGonioCalibM.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM.add(Box.createHorizontalGlue());
    jpanelGonioCalibM.add(jTFMat0File);
    jTFMat0File.setToolTipText("Matrix File (Omega=0; Kappa=0; Phi=0)");
    jTFMat0File.setMinimumSize(tfnSize);
    jTFMat0File.setMaximumSize(tfSize);
    jTFMat0File.setPreferredSize(tfSize);
    jTFMat0File.setAlignmentX((float)0.1);
    //B MatrixFile
    jpanelGonioCalibM.add(jBMat0Sel);
    jBMat0Sel.setToolTipText("Select the Matrix File (Omega=0; Kappa=0; Phi=0)");
    jBMat0Sel.addActionListener(new Stac_FileChooser(this));
    jBMat0Sel.setMinimumSize(fsbSize);
    jBMat0Sel.setMaximumSize(fsbSize);
    jBMat0Sel.setPreferredSize(fsbSize);
    //L (Used Calib Angles)
    jpanelGonioCalibM.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGCA = new JLabel();
    jpanelGonioCalibM.add(jLabelGCA, null);
    jLabelGCA.setText("Calibration (P/K/O):");
    jLabelGCA.setMinimumSize(rbdSize);
    jLabelGCA.setMaximumSize(rbdSize);
    jLabelGCA.setPreferredSize(rbdSize);
    //
    jpanelGonioCalibM.add(jButtonGRC_mvN, null);
    jButtonGRC_mvN.setText("Mv(0;0;0)");
    jButtonGRC_mvN.setMinimumSize(rbSize);
    jButtonGRC_mvN.setMaximumSize(rbSize);
    jButtonGRC_mvN.setPreferredSize(rbSize);
    jButtonGRC_mvN.setToolTipText("Moving to the zero Datum");
    jButtonGRC_mvN.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGRC_mvN.setEnabled(true);
    //
    jpanelGonioCalibM.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM.add(Box.createHorizontalGlue());
    //LINE 1
    JPanel jpanelGonioCalibM1 = new JPanel();
    jPanelGonioCalib.add(jpanelGonioCalibM1);
    jpanelGonioCalibM1.setLayout(new BoxLayoutFixed(jpanelGonioCalibM1,BoxLayoutFixed.LINE_AXIS));
    jpanelGonioCalibM1.add(Box.createRigidArea(new Dimension(10,10)));
    //L (0;0;PHI)
    jpanelGonioCalibM1.add(jLMat1, null);
    jLMat1.setText("Mat(-;-;P):");
    jLMat1.setMinimumSize(rbSize);
    jLMat1.setMaximumSize(rbSize);
    jLMat1.setPreferredSize(rbSize);
    //jPanelGonioCalib.add(Box.createRigidArea(new Dimension(10,10)));    
    //RB MOsflm
    jpanelGonioCalibM1.add(jRBMat1Mosflm, null);
    jRBMat1Mosflm.setSelected(true);
    jRBMat1Mosflm.setText("Mosflm");
    jRBMat1Mosflm.setMinimumSize(rbSize);
    jRBMat1Mosflm.setMaximumSize(rbSize);
    jRBMat1Mosflm.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat1.add(jRBMat1Mosflm);
    //RB XDS
    jpanelGonioCalibM1.add(jRBMat1XDS, null);
    jRBMat1XDS.setSelected(true);
    jRBMat1XDS.setText("XDS");
    jRBMat1XDS.setMinimumSize(rbSize);
    jRBMat1XDS.setMaximumSize(rbSize);
    jRBMat1XDS.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat1.add(jRBMat1XDS);
    //RB Denzo
    jpanelGonioCalibM1.add(jRBMat1Denzo, null);
    jRBMat1Denzo.setSelected(true);
    jRBMat1Denzo.setText("Denzo");
    jRBMat1Denzo.setMinimumSize(rbSize);
    jRBMat1Denzo.setMaximumSize(rbSize);
    jRBMat1Denzo.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat1.add(jRBMat1Denzo);
    //TF MatrixFile
    jpanelGonioCalibM1.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM1.add(Box.createHorizontalGlue());
    jpanelGonioCalibM1.add(jTFMat1File);
    jTFMat1File.setToolTipText("Matrix File (Omega=0; Kappa=0; Phi!=0)");
    jTFMat1File.setMinimumSize(tfnSize);
    jTFMat1File.setMaximumSize(tfSize);
    jTFMat1File.setPreferredSize(tfSize);
    jTFMat1File.setAlignmentX((float)0.1);
    //B MatrixFile
    jpanelGonioCalibM1.add(jBMat1Sel);
    jBMat1Sel.setToolTipText("Select the Matrix File (Omega=0; Kappa=0; Phi!=0)");
    jBMat1Sel.addActionListener(new Stac_FileChooser(this));
    jBMat1Sel.setMinimumSize(fsbSize);
    jBMat1Sel.setMaximumSize(fsbSize);
    jBMat1Sel.setPreferredSize(fsbSize);
    //TF used phi angle
    jpanelGonioCalibM1.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM1.add(jTFGCPhi, null);
    jTFGCPhi.setText("24");
    jTFGCPhi.setToolTipText("Phi Angle for the Matrix Calibration");
    jTFGCPhi.setMinimumSize(rbdSize);
    jTFGCPhi.setMaximumSize(rbdSize);
    jTFGCPhi.setPreferredSize(rbdSize);
    //
    jpanelGonioCalibM1.add(jButtonGRC_mvP, null);
    jButtonGRC_mvP.setText("Mv(0;0;P)");
    jButtonGRC_mvP.setMinimumSize(rbSize);
    jButtonGRC_mvP.setMaximumSize(rbSize);
    jButtonGRC_mvP.setPreferredSize(rbSize);
    jButtonGRC_mvP.setToolTipText("Move to (0;0;Phi)");
    jButtonGRC_mvP.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGRC_mvP.setEnabled(true);
    //
    jpanelGonioCalibM1.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM1.add(Box.createHorizontalGlue());
    //LINE 2
    JPanel jpanelGonioCalibM2 = new JPanel();
    jPanelGonioCalib.add(jpanelGonioCalibM2);
    jpanelGonioCalibM2.setLayout(new BoxLayoutFixed(jpanelGonioCalibM2,BoxLayoutFixed.LINE_AXIS));
    jpanelGonioCalibM2.add(Box.createRigidArea(new Dimension(10,10)));
    //L (0;0;PHI)
    jpanelGonioCalibM2.add(jLMat2, null);
    jLMat2.setText("Mat(-;K;P):");
    jLMat2.setMinimumSize(rbSize);
    jLMat2.setMaximumSize(rbSize);
    jLMat2.setPreferredSize(rbSize);
    //jPanelGonioCalib.add(Box.createRigidArea(new Dimension(10,10)));    
    //RB MOsflm
    jpanelGonioCalibM2.add(jRBMat2Mosflm, null);
    jRBMat2Mosflm.setSelected(true);
    jRBMat2Mosflm.setText("Mosflm");
    jRBMat2Mosflm.setMinimumSize(rbSize);
    jRBMat2Mosflm.setMaximumSize(rbSize);
    jRBMat2Mosflm.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat2.add(jRBMat2Mosflm);
    //RB XDS
    jpanelGonioCalibM2.add(jRBMat2XDS, null);
    jRBMat2XDS.setSelected(true);
    jRBMat2XDS.setText("XDS");
    jRBMat2XDS.setMinimumSize(rbSize);
    jRBMat2XDS.setMaximumSize(rbSize);
    jRBMat2XDS.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat2.add(jRBMat2XDS);
    //RB Denzo
    jpanelGonioCalibM2.add(jRBMat2Denzo, null);
    jRBMat2Denzo.setSelected(true);
    jRBMat2Denzo.setText("Denzo");
    jRBMat2Denzo.setMinimumSize(rbSize);
    jRBMat2Denzo.setMaximumSize(rbSize);
    jRBMat2Denzo.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat2.add(jRBMat2Denzo);
    //TF MatrixFile
    jpanelGonioCalibM2.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM2.add(Box.createHorizontalGlue());
    jpanelGonioCalibM2.add(jTFMat2File);
    jTFMat2File.setToolTipText("Matrix File (Omega=0; Kappa!=0; Phi!=0)");
    jTFMat2File.setMinimumSize(tfnSize);
    jTFMat2File.setMaximumSize(tfSize);
    jTFMat2File.setPreferredSize(tfSize);
    jTFMat2File.setAlignmentX((float)0.1);
    //B MatrixFile
    jpanelGonioCalibM2.add(jBMat2Sel);
    jBMat2Sel.setToolTipText("Select the Matrix File (Omega=0; Kappa!=0; Phi!=0)");
    jBMat2Sel.addActionListener(new Stac_FileChooser(this));
    jBMat2Sel.setMinimumSize(fsbSize);
    jBMat2Sel.setMaximumSize(fsbSize);
    jBMat2Sel.setPreferredSize(fsbSize);
    //TF used kappa angle
    jpanelGonioCalibM2.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM2.add(jTFGCKappa, null);
    jTFGCKappa.setText("24");
    jTFGCKappa.setToolTipText("Kappa Angle for the Matrix Calibration");
    jTFGCKappa.setMinimumSize(rbdSize);
    jTFGCKappa.setMaximumSize(rbdSize);
    jTFGCKappa.setPreferredSize(rbdSize);
    //
    jpanelGonioCalibM2.add(jButtonGRC_mvK, null);
    jButtonGRC_mvK.setText("Mv(0;K;P)");
    jButtonGRC_mvK.setMinimumSize(rbSize);
    jButtonGRC_mvK.setMaximumSize(rbSize);
    jButtonGRC_mvK.setPreferredSize(rbSize);
    jButtonGRC_mvK.setToolTipText("Move to (0;Kappa;Phi)");
    jButtonGRC_mvK.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGRC_mvK.setEnabled(true);
    //
    jpanelGonioCalibM2.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM2.add(Box.createHorizontalGlue());
    //LINE 3
    JPanel jpanelGonioCalibM3 = new JPanel();
    jPanelGonioCalib.add(jpanelGonioCalibM3);
    jpanelGonioCalibM3.setLayout(new BoxLayoutFixed(jpanelGonioCalibM3,BoxLayoutFixed.LINE_AXIS));
    jpanelGonioCalibM3.add(Box.createRigidArea(new Dimension(10,10)));
    //L (OMEGA;KAPPA;PHI)
    jpanelGonioCalibM3.add(jLMat3, null);
    jLMat3.setText("Mat(O;K;P):");
    jLMat3.setMinimumSize(rbSize);
    jLMat3.setMaximumSize(rbSize);
    jLMat3.setPreferredSize(rbSize);
    //jPanelGonioCalib.add(Box.createRigidArea(new Dimension(10,10)));    
    //RB MOsflm
    jpanelGonioCalibM3.add(jRBMat3Mosflm, null);
    jRBMat3Mosflm.setSelected(true);
    jRBMat3Mosflm.setText("Mosflm");
    jRBMat3Mosflm.setMinimumSize(rbSize);
    jRBMat3Mosflm.setMaximumSize(rbSize);
    jRBMat3Mosflm.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat3.add(jRBMat3Mosflm);
    //RB XDS
    jpanelGonioCalibM3.add(jRBMat3XDS, null);
    jRBMat3XDS.setSelected(true);
    jRBMat3XDS.setText("XDS");
    jRBMat3XDS.setMinimumSize(rbSize);
    jRBMat3XDS.setMaximumSize(rbSize);
    jRBMat3XDS.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat3.add(jRBMat3XDS);
    //RB Denzo
    jpanelGonioCalibM3.add(jRBMat3Denzo, null);
    jRBMat3Denzo.setSelected(true);
    jRBMat3Denzo.setText("Denzo");
    jRBMat3Denzo.setMinimumSize(rbSize);
    jRBMat3Denzo.setMaximumSize(rbSize);
    jRBMat3Denzo.setPreferredSize(rbSize);
    buttonGroupInputFormatsMat3.add(jRBMat3Denzo);
    //TF MatrixFile
    jpanelGonioCalibM3.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM3.add(Box.createHorizontalGlue());
    jpanelGonioCalibM3.add(jTFMat3File);
    jTFMat3File.setToolTipText("Matrix File (Omega!=0; Kappa!=0; Phi!=0)");
    jTFMat3File.setMinimumSize(tfnSize);
    jTFMat3File.setMaximumSize(tfSize);
    jTFMat3File.setPreferredSize(tfSize);
    jTFMat3File.setAlignmentX((float)0.1);
    //B MatrixFile
    jpanelGonioCalibM3.add(jBMat3Sel);
    jBMat3Sel.setToolTipText("Select the Matrix File (Omega!=0; Kappa!=0; Phi!=0)");
    jBMat3Sel.addActionListener(new Stac_FileChooser(this));
    jBMat3Sel.setMinimumSize(fsbSize);
    jBMat3Sel.setMaximumSize(fsbSize);
    jBMat3Sel.setPreferredSize(fsbSize);
    //TF used kappa angle
    jpanelGonioCalibM3.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM3.add(jTFGCOmega, null);
    jTFGCOmega.setText("24");
    jTFGCOmega.setToolTipText("Omega Angle for the Calibration");
    jTFGCOmega.setMinimumSize(rbdSize);
    jTFGCOmega.setMaximumSize(rbdSize);
    jTFGCOmega.setPreferredSize(rbdSize);
    //
    jpanelGonioCalibM3.add(jButtonGRC_mvO, null);
    jButtonGRC_mvO.setText("Mv(O;K;P)");
    jButtonGRC_mvO.setMinimumSize(rbSize);
    jButtonGRC_mvO.setMaximumSize(rbSize);
    jButtonGRC_mvO.setPreferredSize(rbSize);
    jButtonGRC_mvO.setToolTipText("Move to (Omega;Kappa;Phi)");
    jButtonGRC_mvO.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGRC_mvO.setEnabled(true);
    //
    jpanelGonioCalibM3.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioCalibM3.add(Box.createHorizontalGlue());
    //separator
    JPanel jPanelGCSepLine = new JPanel();
    jPanelGonioCalib.add(jPanelGCSepLine);
    jPanelGCSepLine.setLayout(new BoxLayoutFixed(jPanelGCSepLine,BoxLayoutFixed.LINE_AXIS));
    jPanelGCSepLine.add(Box.createRigidArea(new Dimension(10,3)));
    JTextField jTFGCSepL = new JTextField();
    jPanelGCSepLine.add(jTFGCSepL, null);
    jTFGCSepL.setEditable(false);
    jTFGCSepL.setMinimumSize(new Dimension(1,3));
    jTFGCSepL.setMaximumSize(new Dimension(2000,3));
    jTFGCSepL.setPreferredSize(new Dimension(1,3));
    jPanelGCSepLine.add(Box.createRigidArea(new Dimension(10,3)));
    JPanel jPanelGCSep = new JPanel();
    jPanelGonioCalib.add(jPanelGCSep);
    jPanelGCSep.setLayout(new BoxLayoutFixed(jPanelGCSep,BoxLayoutFixed.LINE_AXIS));
    jPanelGCSep.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGCSep = new JLabel();
    jPanelGCSep.add(jLabelGCSep, null);
    jLabelGCSep.setText("Calculated axes:");
    jLabelGCSep.setMinimumSize(rbSize);
    jLabelGCSep.setMaximumSize(rbSize);
    jLabelGCSep.setPreferredSize(rbSize);
    JLabel jLabelGCSepX = new JLabel();
    jPanelGCSep.add(jLabelGCSepX, null);
    jLabelGCSepX.setText("X");
    jLabelGCSepX.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGCSepX.setMinimumSize(tfnSize);
    jLabelGCSepX.setMaximumSize(tfSize);
    jLabelGCSepX.setPreferredSize(tfSize);
    JLabel jLabelGCSepY = new JLabel();
    jPanelGCSep.add(jLabelGCSepY, null);
    jLabelGCSepY.setText("Y");
    jLabelGCSepY.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGCSepY.setMinimumSize(tfnSize);
    jLabelGCSepY.setMaximumSize(tfSize);
    jLabelGCSepY.setPreferredSize(tfSize);
    JLabel jLabelGCSepZ = new JLabel();
    jPanelGCSep.add(jLabelGCSepZ, null);
    jLabelGCSepZ.setText("Z");
    jLabelGCSepZ.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGCSepZ.setMinimumSize(tfnSize);
    jLabelGCSepZ.setMaximumSize(tfSize);
    jLabelGCSepZ.setPreferredSize(tfSize);
    JLabel jLabelGCSepA = new JLabel();
    jPanelGCSep.add(jLabelGCSepA, null);
    jLabelGCSepA.setText("Angle");
    jLabelGCSepA.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGCSepA.setMinimumSize(tfnSize);
    jLabelGCSepA.setMaximumSize(tfSize);
    jLabelGCSepA.setPreferredSize(tfSize);
    JLabel jLabelGCSepD = new JLabel();
    jPanelGCSep.add(jLabelGCSepD, null);
    jLabelGCSepD.setText("Diff to current");
    jLabelGCSepD.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGCSepD.setMinimumSize(tfnSize);
    jLabelGCSepD.setMaximumSize(tfSize);
    jLabelGCSepD.setPreferredSize(tfSize);
    JLabel jLabelGCSepDT = new JLabel();
    jPanelGCSep.add(jLabelGCSepDT, null);
    jLabelGCSepDT.setText("Diff to Trans");
    jLabelGCSepDT.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGCSepDT.setMinimumSize(tfnSize);
    jLabelGCSepDT.setMaximumSize(tfSize);
    jLabelGCSepDT.setPreferredSize(tfSize);
    jPanelGCSep.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGCSep.add(Box.createHorizontalGlue());
    //LINE Calculated Phi
    JPanel jPanelGCRP = new JPanel();
    jPanelGonioCalib.add(jPanelGCRP);
    jPanelGCRP.setLayout(new BoxLayoutFixed(jPanelGCRP,BoxLayoutFixed.LINE_AXIS));
    jPanelGCRP.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGCRP = new JLabel();
    jPanelGCRP.add(jLabelGCRP, null);
    jLabelGCRP.setText("Phi:");
    jLabelGCRP.setMinimumSize(rbSize);
    jLabelGCRP.setMaximumSize(rbSize);
    jLabelGCRP.setPreferredSize(rbSize);
    jPanelGCRP.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGCRP.add(Box.createHorizontalGlue());
    jPanelGCRP.add(jTFGCRphiX, null);
    jTFGCRphiX.setToolTipText("Phi axis (x component)");
    jTFGCRphiX.setEditable(false);
    jTFGCRphiX.setMinimumSize(tfnSize);
    jTFGCRphiX.setMaximumSize(tfSize);
    jTFGCRphiX.setPreferredSize(tfSize);
    jPanelGCRP.add(jTFGCRphiY, null);
    jTFGCRphiY.setToolTipText("Phi axis (y component)");
    jTFGCRphiY.setEditable(false);
    jTFGCRphiY.setMinimumSize(tfnSize);
    jTFGCRphiY.setMaximumSize(tfSize);
    jTFGCRphiY.setPreferredSize(tfSize);
    jPanelGCRP.add(jTFGCRphiZ, null);
    jTFGCRphiZ.setToolTipText("Phi axis (z component)");
    jTFGCRphiZ.setEditable(false);
    jTFGCRphiZ.setMinimumSize(tfnSize);
    jTFGCRphiZ.setMaximumSize(tfSize);
    jTFGCRphiZ.setPreferredSize(tfSize);
    jPanelGCRP.add(jTFGCRphi, null);
    jTFGCRphi.setToolTipText("Phi axis rotation");
    jTFGCRphi.setEditable(false);
    jTFGCRphi.setMinimumSize(tfnSize);
    jTFGCRphi.setMaximumSize(tfSize);
    jTFGCRphi.setPreferredSize(tfSize);
    jPanelGCRP.add(jTFGCRDphi, null);
    jTFGCRDphi.setToolTipText("Diff angle between the previous settings");
    jTFGCRDphi.setEditable(false);
    jTFGCRDphi.setMinimumSize(tfnSize);
    jTFGCRDphi.setMaximumSize(tfSize);
    jTFGCRDphi.setPreferredSize(tfSize);
    jPanelGCRP.add(jTFGCRDphiT, null);
    jTFGCRDphiT.setToolTipText("Diff angle between the Translation settings");
    jTFGCRDphiT.setEditable(false);
    jTFGCRDphiT.setMinimumSize(tfnSize);
    jTFGCRDphiT.setMaximumSize(tfSize);
    jTFGCRDphiT.setPreferredSize(tfSize);
    jPanelGCRP.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGCRP.add(Box.createHorizontalGlue());
    //LINE Calculated Kappa
    JPanel jPanelGCRK = new JPanel();
    jPanelGonioCalib.add(jPanelGCRK);
    jPanelGCRK.setLayout(new BoxLayoutFixed(jPanelGCRK,BoxLayoutFixed.LINE_AXIS));
    jPanelGCRK.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGCRK = new JLabel();
    jPanelGCRK.add(jLabelGCRK, null);
    jLabelGCRK.setText("Kappa:");
    jLabelGCRK.setMinimumSize(rbSize);
    jLabelGCRK.setMaximumSize(rbSize);
    jLabelGCRK.setPreferredSize(rbSize);
    jPanelGCRK.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGCRK.add(Box.createHorizontalGlue());
    jPanelGCRK.add(jTFGCRkappaX, null);
    jTFGCRkappaX.setToolTipText("kappa axis (x component)");
    jTFGCRkappaX.setEditable(false);
    jTFGCRkappaX.setMinimumSize(tfnSize);
    jTFGCRkappaX.setMaximumSize(tfSize);
    jTFGCRkappaX.setPreferredSize(tfSize);
    jPanelGCRK.add(jTFGCRkappaY, null);
    jTFGCRkappaY.setToolTipText("kappa axis (y component)");
    jTFGCRkappaY.setEditable(false);
    jTFGCRkappaY.setMinimumSize(tfnSize);
    jTFGCRkappaY.setMaximumSize(tfSize);
    jTFGCRkappaY.setPreferredSize(tfSize);
    jPanelGCRK.add(jTFGCRkappaZ, null);
    jTFGCRkappaZ.setToolTipText("kappa axis (z component)");
    jTFGCRkappaZ.setEditable(false);
    jTFGCRkappaZ.setMinimumSize(tfnSize);
    jTFGCRkappaZ.setMaximumSize(tfSize);
    jTFGCRkappaZ.setPreferredSize(tfSize);
    jPanelGCRK.add(jTFGCRkappa, null);
    jTFGCRkappa.setToolTipText("kappa axis rotation");
    jTFGCRkappa.setEditable(false);
    jTFGCRkappa.setMinimumSize(tfnSize);
    jTFGCRkappa.setMaximumSize(tfSize);
    jTFGCRkappa.setPreferredSize(tfSize);
    jPanelGCRK.add(jTFGCRDkappa, null);
    jTFGCRDkappa.setToolTipText("Diff angle between the previous settings");
    jTFGCRDkappa.setEditable(false);
    jTFGCRDkappa.setMinimumSize(tfnSize);
    jTFGCRDkappa.setMaximumSize(tfSize);
    jTFGCRDkappa.setPreferredSize(tfSize);
    jPanelGCRK.add(jTFGCRDkappaT, null);
    jTFGCRDkappaT.setToolTipText("Diff angle between the Translation settings");
    jTFGCRDkappaT.setEditable(false);
    jTFGCRDkappaT.setMinimumSize(tfnSize);
    jTFGCRDkappaT.setMaximumSize(tfSize);
    jTFGCRDkappaT.setPreferredSize(tfSize);
    jPanelGCRK.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGCRK.add(Box.createHorizontalGlue());
    //LINE Calculated Omega
    JPanel jPanelGCRO = new JPanel();
    jPanelGonioCalib.add(jPanelGCRO);
    jPanelGCRO.setLayout(new BoxLayoutFixed(jPanelGCRO,BoxLayoutFixed.LINE_AXIS));
    jPanelGCRO.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGCRO = new JLabel();
    jPanelGCRO.add(jLabelGCRO, null);
    jLabelGCRO.setText("Omega:");
    jLabelGCRO.setMinimumSize(rbSize);
    jLabelGCRO.setMaximumSize(rbSize);
    jLabelGCRO.setPreferredSize(rbSize);
    jPanelGCRO.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGCRO.add(Box.createHorizontalGlue());
    jPanelGCRO.add(jTFGCRomegaX, null);
    jTFGCRomegaX.setToolTipText("omega axis (x component)");
    jTFGCRomegaX.setEditable(false);
    jTFGCRomegaX.setMinimumSize(tfnSize);
    jTFGCRomegaX.setMaximumSize(tfSize);
    jTFGCRomegaX.setPreferredSize(tfSize);
    jPanelGCRO.add(jTFGCRomegaY, null);
    jTFGCRomegaY.setToolTipText("omega axis (y component)");
    jTFGCRomegaY.setEditable(false);
    jTFGCRomegaY.setMinimumSize(tfnSize);
    jTFGCRomegaY.setMaximumSize(tfSize);
    jTFGCRomegaY.setPreferredSize(tfSize);
    jPanelGCRO.add(jTFGCRomegaZ, null);
    jTFGCRomegaZ.setToolTipText("omega axis (z component)");
    jTFGCRomegaZ.setEditable(false);
    jTFGCRomegaZ.setMinimumSize(tfnSize);
    jTFGCRomegaZ.setMaximumSize(tfSize);
    jTFGCRomegaZ.setPreferredSize(tfSize);
    jPanelGCRO.add(jTFGCRomega, null);
    jTFGCRomega.setToolTipText("omega axis rotation");
    jTFGCRomega.setEditable(false);
    jTFGCRomega.setMinimumSize(tfnSize);
    jTFGCRomega.setMaximumSize(tfSize);
    jTFGCRomega.setPreferredSize(tfSize);
    jPanelGCRO.add(jTFGCRDomega, null);
    jTFGCRDomega.setToolTipText("Diff angle between the previous settings");
    jTFGCRDomega.setEditable(false);
    jTFGCRDomega.setMinimumSize(tfnSize);
    jTFGCRDomega.setMaximumSize(tfSize);
    jTFGCRDomega.setPreferredSize(tfSize);
    jPanelGCRO.add(jTFGCRDomegaT, null);
    jTFGCRDomegaT.setToolTipText("Diff angle between the previous settings");
    jTFGCRDomegaT.setEditable(false);
    jTFGCRDomegaT.setMinimumSize(tfnSize);
    jTFGCRDomegaT.setMaximumSize(tfSize);
    jTFGCRDomegaT.setPreferredSize(tfSize);
    jPanelGCRO.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGCRO.add(Box.createHorizontalGlue());
    //bottom glue
    jPanelGonioCalib.add(Box.createVerticalGlue());
    
    //panelGonioTCalib
    //layout
    panelGonioTCalib.setLayout(new BorderLayout());
    panelGonioTCalib.setBorder(BorderFactory.createRaisedBevelBorder());
    //Main button on South
    panelGonioTCalib.add(panelGonioTCalibS, BorderLayout.SOUTH);
    panelGonioTCalibS.setLayout(new BoxLayoutFixed(panelGonioTCalibS,BoxLayoutFixed.LINE_AXIS));
    panelGonioTCalibS.add(Box.createHorizontalGlue());
    panelGonioTCalibS.add(Box.createHorizontalGlue());
    panelGonioTCalibS.add(jButtonGonioAutoTCalib);
    jButtonGonioAutoTCalib.setText("Auto TCalib");
    jButtonGonioAutoTCalib.setPreferredSize(new Dimension(100,34));
    jButtonGonioAutoTCalib.setToolTipText("Automatic Gonio Translation Calibration");
    jButtonGonioAutoTCalib.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    panelGonioTCalibS.add(jButtonGTC_calcRot);
    jButtonGTC_calcRot.setText("Calc with RotCalib");
    jButtonGTC_calcRot.setPreferredSize(new Dimension(100,34));
    jButtonGTC_calcRot.setToolTipText("Start the Gonio Translation Calibration using the rotation axes from RotCalib");
    jButtonGTC_calcRot.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    panelGonioTCalibS.add(jButtonGonioTCalib);
    jButtonGonioTCalib.setText("Calculate");
    jButtonGonioTCalib.setPreferredSize(new Dimension(100,34));
    jButtonGonioTCalib.setToolTipText("Start the Gonio Translation Calibration");
    jButtonGonioTCalib.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    panelGonioTCalibS.add(jButtonGnsdefTUpdate);
    jButtonGnsdefTUpdate.setText("Update");
    jButtonGnsdefTUpdate.setPreferredSize(new Dimension(100,34));
    jButtonGnsdefTUpdate.setToolTipText("Update the Gonio Translation Settings");
    jButtonGnsdefTUpdate.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGnsdefTUpdate.setEnabled(false);
    panelGonioTCalibS.add(Box.createHorizontalGlue());
    //layout
    panelGonioTCalib.add(panelGonioTCalibW,BorderLayout.WEST);
    panelGonioTCalibW.setLayout(new BoxLayoutFixed(panelGonioTCalibW,BoxLayoutFixed.PAGE_AXIS));
    panelGonioTCalibW.add(Box.createRigidArea(new Dimension(10,10)));
    panelGonioTCalib.add(panelGonioTCalibE,BorderLayout.EAST);
    panelGonioTCalibE.setLayout(new BoxLayoutFixed(panelGonioTCalibE,BoxLayoutFixed.PAGE_AXIS));
    panelGonioTCalibE.add(Box.createRigidArea(new Dimension(10,10)));
    panelGonioTCalib.add(panelGonioTCalibN,BorderLayout.NORTH);
    panelGonioTCalibN.setLayout(new BoxLayoutFixed(panelGonioTCalibN,BoxLayoutFixed.PAGE_AXIS));
    panelGonioTCalibN.add(Box.createRigidArea(new Dimension(10,10)));
    panelGonioTCalib.add(jPanelGonioTCalib, BorderLayout.CENTER);
    jPanelGonioTCalib.setBorder(BorderFactory.createRaisedBevelBorder());
    jPanelGonioTCalib.setLayout(new BoxLayoutFixed(jPanelGonioTCalib,BoxLayoutFixed.PAGE_AXIS));
    jPanelGonioTCalib.setAlignmentX(LEFT_ALIGNMENT);
    jPanelGonioTCalib.add(Box.createRigidArea(new Dimension(10,10)));
    //LINE Warning
    JPanel jpanelGonioTCalibWarning = new JPanel();
    jPanelGonioTCalib.add(jpanelGonioTCalibWarning);
    jpanelGonioTCalibWarning.setLayout(new BoxLayoutFixed(jpanelGonioTCalibWarning,BoxLayoutFixed.LINE_AXIS));
    jpanelGonioTCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    //L (0;0;0)
    JLabel jLabelGTCW = new JLabel("Have you initialized your kappa translation?");
    jpanelGonioTCalibWarning.add(jLabelGTCW, null);
    jpanelGonioTCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    jButtonTGC_init.setText("Init Motors");
    jButtonTGC_init.setPreferredSize(new Dimension(150,34));
    jButtonTGC_init.setToolTipText("Initializing the Motors");
    jButtonTGC_init.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonTGC_init.setEnabled(true);
    jpanelGonioTCalibWarning.add(jButtonTGC_init, null);
    jpanelGonioTCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    jButtonGTC_mvreset.setText("Mv to (0;0;0)");
    jButtonGTC_mvreset.setPreferredSize(new Dimension(150,34));
    jButtonGTC_mvreset.setToolTipText("Mv to datum (0;0;0) for centering and resetting translation");
    jButtonGTC_mvreset.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGTC_mvreset.setEnabled(true);
    jpanelGonioTCalibWarning.add(jButtonGTC_mvreset, null);
    jpanelGonioTCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    jButtonGTC_reset.setText("Reset Translation");
    jButtonGTC_reset.setPreferredSize(new Dimension(150,34));
    jButtonGTC_reset.setToolTipText("Resetting Translation Motors after centering. The previous TranslationCalibration becomes LOST!!!");
    jButtonGTC_reset.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonGTC_reset.setEnabled(true);
    jpanelGonioTCalibWarning.add(jButtonGTC_reset, null);
    //
    jpanelGonioTCalibWarning.add(Box.createRigidArea(new Dimension(10,10)));
    jpanelGonioTCalibWarning.add(Box.createHorizontalGlue());
    //separator
    JPanel jPanelGTCSepLine1 = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCSepLine1);
    jPanelGTCSepLine1.setLayout(new BoxLayoutFixed(jPanelGTCSepLine1,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCSepLine1.add(Box.createRigidArea(new Dimension(10,3)));
    JTextField jTFGTCSepL1 = new JTextField();
    jPanelGTCSepLine1.add(jTFGTCSepL1, null);
    jTFGTCSepL1.setEditable(false);
    jTFGTCSepL1.setMinimumSize(new Dimension(1,3));
    jTFGTCSepL1.setMaximumSize(new Dimension(2000,3));
    jTFGTCSepL1.setPreferredSize(new Dimension(1,3));
    jPanelGTCSepLine1.add(Box.createRigidArea(new Dimension(10,3)));
    JPanel jPanelGTCSep1 = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCSep1);
    jPanelGTCSep1.setLayout(new BoxLayoutFixed(jPanelGTCSep1,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCSep1.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGTCSep1 = new JLabel();
    jPanelGTCSep1.add(jLabelGTCSep1, null);
    jLabelGTCSep1.setText("");
    jLabelGTCSep1.setMinimumSize(rbdSize);
    jLabelGTCSep1.setMaximumSize(rbdSize);
    jLabelGTCSep1.setPreferredSize(rbdSize);
    JLabel jLabelGTCSep1A = new JLabel();
    jPanelGTCSep1.add(jLabelGTCSep1A, null);
    jLabelGTCSep1A.setText("Angle");
    jLabelGTCSep1A.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSep1A.setMinimumSize(tfnSize);
    jLabelGTCSep1A.setMaximumSize(tfSize);
    jLabelGTCSep1A.setPreferredSize(tfSize);
    JLabel jLabelGTCSep1X = new JLabel();
    jPanelGTCSep1.add(Box.createRigidArea(new Dimension(100,10)));
    jPanelGTCSep1.add(jLabelGTCSep1X, null);
    jLabelGTCSep1X.setText("X");
    jLabelGTCSep1X.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSep1X.setMinimumSize(tfnSize);
    jLabelGTCSep1X.setMaximumSize(tfSize);
    jLabelGTCSep1X.setPreferredSize(tfSize);
    JLabel jLabelGTCSep1Y = new JLabel();
    jPanelGTCSep1.add(jLabelGTCSep1Y, null);
    jLabelGTCSep1Y.setText("Y");
    jLabelGTCSep1Y.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSep1Y.setMinimumSize(tfnSize);
    jLabelGTCSep1Y.setMaximumSize(tfSize);
    jLabelGTCSep1Y.setPreferredSize(tfSize);
    JLabel jLabelGTCSep1Z = new JLabel();
    jPanelGTCSep1.add(jLabelGTCSep1Z, null);
    jLabelGTCSep1Z.setText("Z");
    jLabelGTCSep1Z.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSep1Z.setMinimumSize(tfnSize);
    jLabelGTCSep1Z.setMaximumSize(tfSize);
    jLabelGTCSep1Z.setPreferredSize(tfSize);
    jPanelGTCSep1.add(Box.createRigidArea(rbSize));
    jPanelGTCSep1.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCSep1.add(Box.createHorizontalGlue());
    //LINE Phi Translation

    //new Translation class!!!
    jPanelGonioTCalib.add(jPanelTransPhi);
    jPanelGonioTCalib.add(jPanelTransKappa);
    
//    JPanel jPanelGTCInpP = new JPanel();
//    jPanelGonioTCalib.add(jPanelGTCInpP);
//    jPanelGTCInpP.setLayout(new BoxLayoutFixed(jPanelGTCInpP,BoxLayoutFixed.LINE_AXIS));
//    jPanelGTCInpP.add(Box.createRigidArea(new Dimension(10,10)));
//    JLabel jLabelGTCInpP = new JLabel();
//    jPanelGTCInpP.add(jLabelGTCInpP, null);
//    jLabelGTCInpP.setText("Phi translation (0;0;P):");
//    jLabelGTCInpP.setMinimumSize(rbdSize);
//    jLabelGTCInpP.setMaximumSize(rbdSize);
//    jLabelGTCInpP.setPreferredSize(rbdSize);
//    jPanelGTCInpP.add(Box.createRigidArea(new Dimension(10,10)));
//    jPanelGTCInpP.add(Box.createHorizontalGlue());
//    jPanelGTCInpP.add(jTFGTCInpP, null);
//    jTFGTCInpP.setToolTipText("Phi Angle Used");
//    jTFGTCInpP.setMinimumSize(tfnSize);
//    jTFGTCInpP.setMaximumSize(tfSize);
//    jTFGTCInpP.setPreferredSize(tfSize);
//    jTFGTCInpP.setText("180.0");
//    jButtonGTC_mvP.setText("Mv Phi");
//    jButtonGTC_mvP.setMinimumSize(rbSize);
//    jButtonGTC_mvP.setMaximumSize(rbSize);
//    jButtonGTC_mvP.setPreferredSize(rbSize);
//    jButtonGTC_mvP.setToolTipText("Set Datum (0;0;P)");
//    jButtonGTC_mvP.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
//    jButtonGTC_mvP.setEnabled(true);
//    jPanelGTCInpP.add(jButtonGTC_mvP,null);    
//    jPanelGTCInpP.add(jTFGTCInpPX, null);
//    jTFGTCInpPX.setToolTipText("X Component of Phi Translation");
//    jTFGTCInpPX.setMinimumSize(tfnSize);
//    jTFGTCInpPX.setMaximumSize(tfSize);
//    jTFGTCInpPX.setPreferredSize(tfSize);
//    jPanelGTCInpP.add(jTFGTCInpPY, null);
//    jTFGTCInpPY.setToolTipText("Y Component of Phi Translation");
//    jTFGTCInpPY.setMinimumSize(tfnSize);
//    jTFGTCInpPY.setMaximumSize(tfSize);
//    jTFGTCInpPY.setPreferredSize(tfSize);
//    jPanelGTCInpP.add(jTFGTCInpPZ, null);
//    jTFGTCInpPZ.setToolTipText("Z Component of Phi Translation");
//    jTFGTCInpPZ.setMinimumSize(tfnSize);
//    jTFGTCInpPZ.setMaximumSize(tfSize);
//    jTFGTCInpPZ.setPreferredSize(tfSize);
//    jButtonGTC_getP.setText("Get Trn.");
//    jButtonGTC_getP.setMinimumSize(rbSize);
//    jButtonGTC_getP.setMaximumSize(rbSize);
//    jButtonGTC_getP.setPreferredSize(rbSize);
//    jButtonGTC_getP.setToolTipText("Get the Current Translation");
//    jButtonGTC_getP.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
//    jButtonGTC_getP.setEnabled(true);
//    jPanelGTCInpP.add(jButtonGTC_getP,null);
//    jPanelGTCInpP.add(Box.createRigidArea(new Dimension(10,10)));
//    jPanelGTCInpP.add(Box.createHorizontalGlue());
    //LINE Kappa Translation
//    JPanel jPanelGTCInpK = new JPanel();
//    jPanelGonioTCalib.add(jPanelGTCInpK);
//    jPanelGTCInpK.setLayout(new BoxLayoutFixed(jPanelGTCInpK,BoxLayoutFixed.LINE_AXIS));
//    jPanelGTCInpK.add(Box.createRigidArea(new Dimension(10,10)));
//    JLabel jLabelGTCInpK = new JLabel();
//    jPanelGTCInpK.add(jLabelGTCInpK, null);
//    jLabelGTCInpK.setText("Kappa translation (0;K;0):");
//    jLabelGTCInpK.setMinimumSize(rbdSize);
//    jLabelGTCInpK.setMaximumSize(rbdSize);
//    jLabelGTCInpK.setPreferredSize(rbdSize);
//    jPanelGTCInpK.add(Box.createRigidArea(new Dimension(10,10)));
//    jPanelGTCInpK.add(Box.createHorizontalGlue());
//    jPanelGTCInpK.add(jTFGTCInpK, null);
//    jTFGTCInpK.setToolTipText("Kappa Angle Used");
//    jTFGTCInpK.setMinimumSize(tfnSize);
//    jTFGTCInpK.setMaximumSize(tfSize);
//    jTFGTCInpK.setPreferredSize(tfSize);
//    jTFGTCInpK.setText("180.0");
//    jButtonGTC_mvK.setText("Mv Kappa");
//    jButtonGTC_mvK.setMinimumSize(rbSize);
//    jButtonGTC_mvK.setMaximumSize(rbSize);
//    jButtonGTC_mvK.setPreferredSize(rbSize);
//    jButtonGTC_mvK.setToolTipText("Set Datum (0;K;0)");
//    jButtonGTC_mvK.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
//    jButtonGTC_mvK.setEnabled(true);
//    jPanelGTCInpK.add(jButtonGTC_mvK,null);    
//    jPanelGTCInpK.add(jTFGTCInpKX, null);
//    jTFGTCInpKX.setToolTipText("X Component of Kappa Translation");
//    jTFGTCInpKX.setMinimumSize(tfnSize);
//    jTFGTCInpKX.setMaximumSize(tfSize);
//    jTFGTCInpKX.setPreferredSize(tfSize);
//    jPanelGTCInpK.add(jTFGTCInpKY, null);
//    jTFGTCInpKY.setToolTipText("Y Component of Kappa Translation");
//    jTFGTCInpKY.setMinimumSize(tfnSize);
//    jTFGTCInpKY.setMaximumSize(tfSize);
//    jTFGTCInpKY.setPreferredSize(tfSize);
//    jPanelGTCInpK.add(jTFGTCInpKZ, null);
//    jTFGTCInpKZ.setToolTipText("Z Component of Kappa Translation");
//    jTFGTCInpKZ.setMinimumSize(tfnSize);
//    jTFGTCInpKZ.setMaximumSize(tfSize);
//    jTFGTCInpKZ.setPreferredSize(tfSize);
//    jButtonGTC_getK.setText("Get Trn.");
//    jButtonGTC_getK.setMinimumSize(rbSize);
//    jButtonGTC_getK.setMaximumSize(rbSize);
//    jButtonGTC_getK.setPreferredSize(rbSize);
//    jButtonGTC_getK.setToolTipText("Get the Current Translation");
//    jButtonGTC_getK.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
//    jButtonGTC_getK.setEnabled(true);
//    jPanelGTCInpK.add(jButtonGTC_getK,null);
//    jPanelGTCInpK.add(Box.createRigidArea(new Dimension(10,10)));
//    jPanelGTCInpK.add(Box.createHorizontalGlue());
    
    
    //separator
    JPanel jPanelGTCSepLineR = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCSepLineR);
    jPanelGTCSepLineR.setLayout(new BoxLayoutFixed(jPanelGTCSepLineR,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCSepLineR.add(Box.createRigidArea(new Dimension(10,3)));
    JTextField jTFGTCSepLR = new JTextField();
    jPanelGTCSepLineR.add(jTFGTCSepLR, null);
    jTFGTCSepLR.setEditable(false);
    jTFGTCSepLR.setMinimumSize(new Dimension(1,3));
    jTFGTCSepLR.setMaximumSize(new Dimension(2000,3));
    jTFGTCSepLR.setPreferredSize(new Dimension(1,3));
    jPanelGTCSepLineR.add(Box.createRigidArea(new Dimension(10,3)));
    JPanel jPanelGTCSepR = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCSepR);
    jPanelGTCSepR.setLayout(new BoxLayoutFixed(jPanelGTCSepR,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCSepR.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGTCSepR = new JLabel();
    jPanelGTCSepR.add(jLabelGTCSepR, null);
    jLabelGTCSepR.setText("Calculated axes:");
    jLabelGTCSepR.setMinimumSize(rbSize);
    jLabelGTCSepR.setMaximumSize(rbSize);
    jLabelGTCSepR.setPreferredSize(rbSize);
    JLabel jLabelGTCSepXR = new JLabel();
    jPanelGTCSepR.add(jLabelGTCSepXR, null);
    jLabelGTCSepXR.setText("X");
    jLabelGTCSepXR.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepXR.setMinimumSize(tfnSize);
    jLabelGTCSepXR.setMaximumSize(tfSize);
    jLabelGTCSepXR.setPreferredSize(tfSize);
    JLabel jLabelGTCSepYR = new JLabel();
    jPanelGTCSepR.add(jLabelGTCSepYR, null);
    jLabelGTCSepYR.setText("Y");
    jLabelGTCSepYR.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepYR.setMinimumSize(tfnSize);
    jLabelGTCSepYR.setMaximumSize(tfSize);
    jLabelGTCSepYR.setPreferredSize(tfSize);
    JLabel jLabelGTCSepZR = new JLabel();
    jPanelGTCSepR.add(jLabelGTCSepZR, null);
    jLabelGTCSepZR.setText("Z");
    jLabelGTCSepZR.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepZR.setMinimumSize(tfnSize);
    jLabelGTCSepZR.setMaximumSize(tfSize);
    jLabelGTCSepZR.setPreferredSize(tfSize);
    JLabel jLabelGTCSepDR = new JLabel();
    jPanelGTCSepR.add(jLabelGTCSepDR, null);
    jLabelGTCSepDR.setText("Diff to current");
    jLabelGTCSepDR.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepDR.setMinimumSize(tfnSize);
    jLabelGTCSepDR.setMaximumSize(tfSize);
    jLabelGTCSepDR.setPreferredSize(tfSize);
    JLabel jLabelGTCSepDTR = new JLabel();
    jPanelGTCSepR.add(jLabelGTCSepDTR, null);
    jLabelGTCSepDTR.setText("Diff to RotCalib");
    jLabelGTCSepDTR.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepDTR.setMinimumSize(tfnSize);
    jLabelGTCSepDTR.setMaximumSize(tfSize);
    jLabelGTCSepDTR.setPreferredSize(tfSize);
    jPanelGTCSepR.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCSepR.add(Box.createHorizontalGlue());
    //LINE Calculated Phi
    JPanel jPanelGTCRPR = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCRPR);
    jPanelGTCRPR.setLayout(new BoxLayoutFixed(jPanelGTCRPR,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCRPR.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGTCRPR = new JLabel();
    jPanelGTCRPR.add(jLabelGTCRPR, null);
    jLabelGTCRPR.setText("Phi:");
    jLabelGTCRPR.setMinimumSize(rbSize);
    jLabelGTCRPR.setMaximumSize(rbSize);
    jLabelGTCRPR.setPreferredSize(rbSize);
    jPanelGTCRPR.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCRPR.add(Box.createHorizontalGlue());
    jPanelGTCRPR.add(jTFGTCRphiXR, null);
    jTFGTCRphiXR.setToolTipText("Phi axis (x component)");
    jTFGTCRphiXR.setEditable(false);
    jTFGTCRphiXR.setMinimumSize(tfnSize);
    jTFGTCRphiXR.setMaximumSize(tfSize);
    jTFGTCRphiXR.setPreferredSize(tfSize);
    jPanelGTCRPR.add(jTFGTCRphiYR, null);
    jTFGTCRphiYR.setToolTipText("Phi axis (y component)");
    jTFGTCRphiYR.setEditable(false);
    jTFGTCRphiYR.setMinimumSize(tfnSize);
    jTFGTCRphiYR.setMaximumSize(tfSize);
    jTFGTCRphiYR.setPreferredSize(tfSize);
    jPanelGTCRPR.add(jTFGTCRphiZR, null);
    jTFGTCRphiZR.setToolTipText("Phi axis (z component)");
    jTFGTCRphiZR.setEditable(false);
    jTFGTCRphiZR.setMinimumSize(tfnSize);
    jTFGTCRphiZR.setMaximumSize(tfSize);
    jTFGTCRphiZR.setPreferredSize(tfSize);
    jPanelGTCRPR.add(jTFGTCRDphiR, null);
    jTFGTCRDphiR.setToolTipText("Diff angle between the previous settings");
    jTFGTCRDphiR.setEditable(false);
    jTFGTCRDphiR.setMinimumSize(tfnSize);
    jTFGTCRDphiR.setMaximumSize(tfSize);
    jTFGTCRDphiR.setPreferredSize(tfSize);
    jPanelGTCRPR.add(jTFGTCRDphiTR, null);
    jTFGTCRDphiTR.setToolTipText("Diff angle between the RotCalib settings");
    jTFGTCRDphiTR.setEditable(false);
    jTFGTCRDphiTR.setMinimumSize(tfnSize);
    jTFGTCRDphiTR.setMaximumSize(tfSize);
    jTFGTCRDphiTR.setPreferredSize(tfSize);
    jPanelGTCRPR.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCRPR.add(Box.createHorizontalGlue());
    //LINE Calculated Kappa
    JPanel jPanelGTCRKR = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCRKR);
    jPanelGTCRKR.setLayout(new BoxLayoutFixed(jPanelGTCRKR,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCRKR.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGTCRKR = new JLabel();
    jPanelGTCRKR.add(jLabelGTCRKR, null);
    jLabelGTCRKR.setText("Kappa:");
    jLabelGTCRKR.setMinimumSize(rbSize);
    jLabelGTCRKR.setMaximumSize(rbSize);
    jLabelGTCRKR.setPreferredSize(rbSize);
    jPanelGTCRKR.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCRKR.add(Box.createHorizontalGlue());
    jPanelGTCRKR.add(jTFGTCRkappaXR, null);
    jTFGTCRkappaXR.setToolTipText("kappa axis (x component)");
    jTFGTCRkappaXR.setEditable(false);
    jTFGTCRkappaXR.setMinimumSize(tfnSize);
    jTFGTCRkappaXR.setMaximumSize(tfSize);
    jTFGTCRkappaXR.setPreferredSize(tfSize);
    jPanelGTCRKR.add(jTFGTCRkappaYR, null);
    jTFGTCRkappaYR.setToolTipText("kappa axis (y component)");
    jTFGTCRkappaYR.setEditable(false);
    jTFGTCRkappaYR.setMinimumSize(tfnSize);
    jTFGTCRkappaYR.setMaximumSize(tfSize);
    jTFGTCRkappaYR.setPreferredSize(tfSize);
    jPanelGTCRKR.add(jTFGTCRkappaZR, null);
    jTFGTCRkappaZR.setToolTipText("kappa axis (z component)");
    jTFGTCRkappaZR.setEditable(false);
    jTFGTCRkappaZR.setMinimumSize(tfnSize);
    jTFGTCRkappaZR.setMaximumSize(tfSize);
    jTFGTCRkappaZR.setPreferredSize(tfSize);
    jPanelGTCRKR.add(jTFGTCRDkappaR, null);
    jTFGTCRDkappaR.setToolTipText("Diff angle between the previous settings");
    jTFGTCRDkappaR.setEditable(false);
    jTFGTCRDkappaR.setMinimumSize(tfnSize);
    jTFGTCRDkappaR.setMaximumSize(tfSize);
    jTFGTCRDkappaR.setPreferredSize(tfSize);
    jPanelGTCRKR.add(jTFGTCRDkappaTR, null);
    jTFGTCRDkappaTR.setToolTipText("Diff angle between the RotCalib settings");
    jTFGTCRDkappaTR.setEditable(false);
    jTFGTCRDkappaTR.setMinimumSize(tfnSize);
    jTFGTCRDkappaTR.setMaximumSize(tfSize);
    jTFGTCRDkappaTR.setPreferredSize(tfSize);
    jPanelGTCRKR.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCRKR.add(Box.createHorizontalGlue());
    
    //separator
    JPanel jPanelGTCSepLine = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCSepLine);
    jPanelGTCSepLine.setLayout(new BoxLayoutFixed(jPanelGTCSepLine,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCSepLine.add(Box.createRigidArea(new Dimension(10,3)));
    JTextField jTFGTCSepL = new JTextField();
    jPanelGTCSepLine.add(jTFGTCSepL, null);
    jTFGTCSepL.setEditable(false);
    jTFGTCSepL.setMinimumSize(new Dimension(1,3));
    jTFGTCSepL.setMaximumSize(new Dimension(2000,3));
    jTFGTCSepL.setPreferredSize(new Dimension(1,3));
    jPanelGTCSepLine.add(Box.createRigidArea(new Dimension(10,3)));
    JPanel jPanelGTCSep = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCSep);
    jPanelGTCSep.setLayout(new BoxLayoutFixed(jPanelGTCSep,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCSep.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGTCSep = new JLabel();
    jPanelGTCSep.add(jLabelGTCSep, null);
    jLabelGTCSep.setText("Pts on axes:");
    jLabelGTCSep.setMinimumSize(rbSize);
    jLabelGTCSep.setMaximumSize(rbSize);
    jLabelGTCSep.setPreferredSize(rbSize);
    JLabel jLabelGTCSepX = new JLabel();
    jPanelGTCSep.add(jLabelGTCSepX, null);
    jLabelGTCSepX.setText("X");
    jLabelGTCSepX.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepX.setMinimumSize(tfnSize);
    jLabelGTCSepX.setMaximumSize(tfSize);
    jLabelGTCSepX.setPreferredSize(tfSize);
    JLabel jLabelGTCSepY = new JLabel();
    jPanelGTCSep.add(jLabelGTCSepY, null);
    jLabelGTCSepY.setText("Y");
    jLabelGTCSepY.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepY.setMinimumSize(tfnSize);
    jLabelGTCSepY.setMaximumSize(tfSize);
    jLabelGTCSepY.setPreferredSize(tfSize);
    JLabel jLabelGTCSepZ = new JLabel();
    jPanelGTCSep.add(jLabelGTCSepZ, null);
    jLabelGTCSepZ.setText("Z");
    jLabelGTCSepZ.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepZ.setMinimumSize(tfnSize);
    jLabelGTCSepZ.setMaximumSize(tfSize);
    jLabelGTCSepZ.setPreferredSize(tfSize);
    JLabel jLabelGTCSepA = new JLabel();
    jPanelGTCSep.add(jLabelGTCSepA, null);
    jLabelGTCSepA.setText("Axis dist");
    jLabelGTCSepA.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepA.setMinimumSize(tfnSize);
    jLabelGTCSepA.setMaximumSize(tfSize);
    jLabelGTCSepA.setPreferredSize(tfSize);
    JLabel jLabelGTCSepRecE = new JLabel();
    jPanelGTCSep.add(jLabelGTCSepRecE, null);
    jLabelGTCSepRecE.setText("Reconstr.Error");
    jLabelGTCSepRecE.setHorizontalAlignment(SwingConstants.CENTER);
    jLabelGTCSepRecE.setMinimumSize(tfnSize);
    jLabelGTCSepRecE.setMaximumSize(tfSize);
    jLabelGTCSepRecE.setPreferredSize(tfSize);
    jPanelGTCSep.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCSep.add(Box.createHorizontalGlue());
    //LINE Calculated Phi
    JPanel jPanelGTCRP = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCRP);
    jPanelGTCRP.setLayout(new BoxLayoutFixed(jPanelGTCRP,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCRP.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGTCRP = new JLabel();
    jPanelGTCRP.add(jLabelGTCRP, null);
    jLabelGTCRP.setText("Phi axis:");
    jLabelGTCRP.setMinimumSize(rbSize);
    jLabelGTCRP.setMaximumSize(rbSize);
    jLabelGTCRP.setPreferredSize(rbSize);
    jPanelGTCRP.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCRP.add(Box.createHorizontalGlue());
    jPanelGTCRP.add(jTFGTCRphiX, null);
    jTFGTCRphiX.setToolTipText("Phi axis (x component)");
    jTFGTCRphiX.setEditable(false);
    jTFGTCRphiX.setMinimumSize(tfnSize);
    jTFGTCRphiX.setMaximumSize(tfSize);
    jTFGTCRphiX.setPreferredSize(tfSize);
    jPanelGTCRP.add(jTFGTCRphiY, null);
    jTFGTCRphiY.setToolTipText("Phi axis (y component)");
    jTFGTCRphiY.setEditable(false);
    jTFGTCRphiY.setMinimumSize(tfnSize);
    jTFGTCRphiY.setMaximumSize(tfSize);
    jTFGTCRphiY.setPreferredSize(tfSize);
    jPanelGTCRP.add(jTFGTCRphiZ, null);
    jTFGTCRphiZ.setToolTipText("Phi axis (z component)");
    jTFGTCRphiZ.setEditable(false);
    jTFGTCRphiZ.setMinimumSize(tfnSize);
    jTFGTCRphiZ.setMaximumSize(tfSize);
    jTFGTCRphiZ.setPreferredSize(tfSize);
    jPanelGTCRP.add(jTFGTCRphi, null);
    jTFGTCRphi.setToolTipText("Phi axis distance from the current");
    jTFGTCRphi.setEditable(false);
    jTFGTCRphi.setMinimumSize(tfnSize);
    jTFGTCRphi.setMaximumSize(tfSize);
    jTFGTCRphi.setPreferredSize(tfSize);
    jPanelGTCRP.add(jTFGTCRphiRecE, null);
    jTFGTCRphiRecE.setToolTipText("Reconstruction error of the new settings");
    jTFGTCRphiRecE.setEditable(false);
    jTFGTCRphiRecE.setMinimumSize(tfnSize);
    jTFGTCRphiRecE.setMaximumSize(tfSize);
    jTFGTCRphiRecE.setPreferredSize(tfSize);
    jPanelGTCRP.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCRP.add(Box.createHorizontalGlue());
    //LINE Calculated Kappa
    JPanel jPanelGTCRK = new JPanel();
    jPanelGonioTCalib.add(jPanelGTCRK);
    jPanelGTCRK.setLayout(new BoxLayoutFixed(jPanelGTCRK,BoxLayoutFixed.LINE_AXIS));
    jPanelGTCRK.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelGTCRK = new JLabel();
    jPanelGTCRK.add(jLabelGTCRK, null);
    jLabelGTCRK.setText("Kappa axis:");
    jLabelGTCRK.setMinimumSize(rbSize);
    jLabelGTCRK.setMaximumSize(rbSize);
    jLabelGTCRK.setPreferredSize(rbSize);
    jPanelGTCRK.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCRK.add(Box.createHorizontalGlue());
    jPanelGTCRK.add(jTFGTCRkappaX, null);
    jTFGTCRkappaX.setToolTipText("kappa axis (x component)");
    jTFGTCRkappaX.setEditable(false);
    jTFGTCRkappaX.setMinimumSize(tfnSize);
    jTFGTCRkappaX.setMaximumSize(tfSize);
    jTFGTCRkappaX.setPreferredSize(tfSize);
    jPanelGTCRK.add(jTFGTCRkappaY, null);
    jTFGTCRkappaY.setToolTipText("kappa axis (y component)");
    jTFGTCRkappaY.setEditable(false);
    jTFGTCRkappaY.setMinimumSize(tfnSize);
    jTFGTCRkappaY.setMaximumSize(tfSize);
    jTFGTCRkappaY.setPreferredSize(tfSize);
    jPanelGTCRK.add(jTFGTCRkappaZ, null);
    jTFGTCRkappaZ.setToolTipText("kappa axis (z component)");
    jTFGTCRkappaZ.setEditable(false);
    jTFGTCRkappaZ.setMinimumSize(tfnSize);
    jTFGTCRkappaZ.setMaximumSize(tfSize);
    jTFGTCRkappaZ.setPreferredSize(tfSize);
    jPanelGTCRK.add(jTFGTCRkappa, null);
    jTFGTCRkappa.setToolTipText("kappa axis distance from the current");
    jTFGTCRkappa.setEditable(false);
    jTFGTCRkappa.setMinimumSize(tfnSize);
    jTFGTCRkappa.setMaximumSize(tfSize);
    jTFGTCRkappa.setPreferredSize(tfSize);
    jPanelGTCRK.add(jTFGTCRkappaRecE, null);
    jTFGTCRkappaRecE.setToolTipText("Reconstruction error of the new settings");
    jTFGTCRkappaRecE.setEditable(false);
    jTFGTCRkappaRecE.setMinimumSize(tfnSize);
    jTFGTCRkappaRecE.setMaximumSize(tfSize);
    jTFGTCRkappaRecE.setPreferredSize(tfSize);
    jPanelGTCRK.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelGTCRK.add(Box.createHorizontalGlue());
    //bottom glue
    jPanelGonioTCalib.add(Box.createVerticalGlue());
    
    //panel1
    panel1.setLayout(new BorderLayout());
    panel1.setBorder(BorderFactory.createRaisedBevelBorder());
    panel1.add(panel1S, BorderLayout.SOUTH);
    panel1S.setLayout(new BoxLayoutFixed(panel1S,BoxLayoutFixed.LINE_AXIS));
    panel1S.add(Box.createHorizontalGlue());
    panel1S.add(Box.createHorizontalGlue());
    panel1S.add(jButton1_1);
    panel1S.add(Box.createHorizontalGlue());
    jButton1_1.setText("Next >");
    jButton1_1.setPreferredSize(new Dimension(100,34));
    jButton1_1.setToolTipText("Start the Re-Orientation Calculation");
    jButton1_1.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    panel1.add(panel1W,BorderLayout.WEST);
    panel1W.setLayout(new BoxLayoutFixed(panel1W,BoxLayoutFixed.PAGE_AXIS));
    panel1W.add(Box.createRigidArea(new Dimension(10,10)));
    panel1.add(panel1E,BorderLayout.EAST);
    panel1E.setLayout(new BoxLayoutFixed(panel1E,BoxLayoutFixed.PAGE_AXIS));
    panel1E.add(Box.createRigidArea(new Dimension(10,10)));
    panel1.add(panel1N,BorderLayout.NORTH);
    panel1N.setLayout(new BoxLayoutFixed(panel1N,BoxLayoutFixed.PAGE_AXIS));
    panel1N.add(Box.createRigidArea(new Dimension(10,10)));
    panel1.add(jPanel1, BorderLayout.CENTER);
    jPanel1.setBorder(BorderFactory.createRaisedBevelBorder());
    //jPanel1.setBounds(new Rectangle(37, 17, 355, 196));
    jPanel1.setLayout(new BoxLayoutFixed(jPanel1,BoxLayoutFixed.PAGE_AXIS));
    jPanel1.setAlignmentX(LEFT_ALIGNMENT);
    jPanel1.add(Box.createRigidArea(new Dimension(10,10)));
    JPanel jPanel1Desc = new JPanel();
    jPanel1.add(jPanel1Desc, null);
    jPanel1Desc.setLayout(new BoxLayoutFixed(jPanel1Desc,BoxLayoutFixed.LINE_AXIS));
    jPanel1Desc.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1Desc.add(jButtonNewXtal, null);
    jButtonNewXtal.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonNewXtal.setMinimumSize(new Dimension(100,34));
    jButtonNewXtal.setMaximumSize(new Dimension(100,34));
    jButtonNewXtal.setPreferredSize(new Dimension(100,34));
    jButtonNewXtal.setToolTipText("Start to work with a new Xtal");
    jButtonNewXtal.setConfig(session.gui_config);
    jPanel1Desc.add(jLabelInputFormat, null);
    jLabelInputFormat.setText("Descriptor:");
    jLabelInputFormat.setMinimumSize(rbSize);
    jLabelInputFormat.setMaximumSize(rbSize);
    jLabelInputFormat.setPreferredSize(rbSize);
    jPanel1Desc.add(jTextFieldDescDefFile, null);
    jTextFieldDescDefFile.setToolTipText("Input descriptor file for Orienation Matrix determination");
    jTextFieldDescDefFile.setMinimumSize(tfnSize);
    jTextFieldDescDefFile.setMaximumSize(tfSize);
    jTextFieldDescDefFile.setPreferredSize(tfSize);
    jPanel1Desc.add(jButtonDesc, null);
    jButtonDesc.addActionListener(new Stac_FileChooser(this));
    jButtonDesc.setMinimumSize(fsbSize);
    jButtonDesc.setMaximumSize(fsbSize);
    jButtonDesc.setPreferredSize(fsbSize);
    jButtonDesc.setToolTipText("Select the Input Descriptor file");
    jPanel1Desc.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1Desc.add(jButtonDescLoad, null);
    jButtonDescLoad.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonDescLoad.setMinimumSize(new Dimension(100,34));
    jButtonDescLoad.setMaximumSize(new Dimension(100,34));
    jButtonDescLoad.setPreferredSize(new Dimension(100,34));
    jButtonDescLoad.setToolTipText("Load the Input Descriptor file");
    jPanel1Desc.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1Desc.add(jButtonDescSave, null);
    jButtonDescSave.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonDescSave.setMinimumSize(new Dimension(100,34));
    jButtonDescSave.setMaximumSize(new Dimension(100,34));
    jButtonDescSave.setPreferredSize(new Dimension(100,34));
    jButtonDescSave.setToolTipText("Rewrite the Input Descriptor file");
    jPanel1Desc.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1Desc.add(Box.createHorizontalGlue());
    //separator
    JPanel jPanelInpSepLine = new JPanel();
    jPanel1.add(jPanelInpSepLine);
    jPanelInpSepLine.setLayout(new BoxLayoutFixed(jPanelInpSepLine,BoxLayoutFixed.LINE_AXIS));
    jPanelInpSepLine.add(Box.createRigidArea(new Dimension(10,3)));
    JTextField jTFInpSepL = new JTextField();
    jPanelInpSepLine.add(jTFInpSepL, null);
    jTFInpSepL.setEditable(false);
    jTFInpSepL.setMinimumSize(new Dimension(1,1));
    jTFInpSepL.setMaximumSize(new Dimension(2000,1));
    jTFInpSepL.setPreferredSize(new Dimension(1,1));
    jPanelInpSepLine.add(Box.createRigidArea(new Dimension(10,1)));
    jPanel1.add(Box.createRigidArea(new Dimension(10,10)));
	//
    JPanel jPanel1M = new JPanel();
    //jPanel1.add(jPanel1M);
    jPanel1M.setLayout(new BoxLayoutFixed(jPanel1M,BoxLayoutFixed.LINE_AXIS));
    jPanel1M.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1M.add(jLabel2, null);
    jLabel2.setText("OM file:");
    jLabel2.setMinimumSize(rbSize);
    jLabel2.setMaximumSize(rbSize);
    jLabel2.setPreferredSize(rbSize);	
    jPanel1M.add(jRadioButtonMosflm, null);
    jRadioButtonMosflm.setSelected(true);
    jRadioButtonMosflm.setText("Mosflm");
    jRadioButtonMosflm.setMinimumSize(rbSize);
    jRadioButtonMosflm.setMaximumSize(rbSize);
    jRadioButtonMosflm.setPreferredSize(rbSize);
    buttonGroupInputFormats.add(jRadioButtonMosflm);
    jPanel1M.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1M.add(jRadioButtonXDS);
    jRadioButtonXDS.setAlignmentX(LEFT_ALIGNMENT);
    jRadioButtonXDS.setText("XDS");
    jRadioButtonXDS.setMinimumSize(rbSize);
    jRadioButtonXDS.setMaximumSize(rbSize);
    jRadioButtonXDS.setPreferredSize(rbSize);
    buttonGroupInputFormats.add(jRadioButtonXDS);
    jPanel1M.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1M.add(Box.createHorizontalGlue());
    jPanel1M.add(jRadioButtonDenzo);
    jRadioButtonDenzo.setText("Denzo");
    jRadioButtonDenzo.setMinimumSize(rbSize);
    jRadioButtonDenzo.setMaximumSize(rbSize);
    jRadioButtonDenzo.setPreferredSize(rbSize);
    buttonGroupInputFormats.add(jRadioButtonDenzo);
    jPanel1M.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1M.add(jTextFieldDefFile, null);
    jTextFieldDefFile.setToolTipText("Matrix Definiton file for Standard Alignment Calculation");
    jTextFieldDefFile.setMinimumSize(tfnSize);
    jTextFieldDefFile.setMaximumSize(tfSize);
    jTextFieldDefFile.setPreferredSize(tfSize);
    jPanel1M.add(jButton3, null);
    jButton3.addActionListener(new Stac_FileChooser(this));
    jButton3.setMinimumSize(fsbSize);
    jButton3.setMaximumSize(fsbSize);
    jButton3.setPreferredSize(fsbSize);
    jButton3.setToolTipText("Select the Matrix definition file");
    jPanel1M.add(jButton1);
    jButton1.setText("Load");
    jButton1.setMinimumSize(rbSize);
    jButton1.setMaximumSize(rbSize);
    jButton1.setPreferredSize(rbSize);
    jButton1.setToolTipText("Load the Crystal Characterisation Data");
    jButton1.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jPanel1M.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1M.add(Box.createHorizontalGlue());
    //Datum Angles
    JPanel jPanel1A = new JPanel();
    jPanel1.add(jPanel1A);
    jPanel1A.setLayout(new BoxLayoutFixed(jPanel1A,BoxLayoutFixed.LINE_AXIS));
    jPanel1A.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1A.add(jLabelA, null);
    jLabelA.setText("Current Angles:");
    jLabelA.setMinimumSize(rbSize);
    jLabelA.setMaximumSize(rbSize);
    jLabelA.setPreferredSize(rbSize);
    jPanel1A.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1A.add(Box.createHorizontalGlue());
    jPanel1A.add(jTFOmega, null);
    jTFOmega.setToolTipText("Omega Angle for the Matrix definition (should be at the oscillation start!)");
    jTFOmega.setMinimumSize(tfnSize);
    jTFOmega.setMaximumSize(tfSize);
    jTFOmega.setPreferredSize(tfSize);
    jPanel1A.add(jTFKappa, null);
    jTFKappa.setToolTipText("Kappa Angle for the Matrix definition");
    jTFKappa.setMinimumSize(tfnSize);
    jTFKappa.setMaximumSize(tfSize);
    jTFKappa.setPreferredSize(tfSize);
    jPanel1A.add(jTFPhi, null);
    jTFPhi.setToolTipText("Phi Angle for the Matrix definition");
    jTFPhi.setMinimumSize(tfnSize);
    jTFPhi.setMaximumSize(tfSize);
    jTFPhi.setPreferredSize(tfSize);
    jPanel1A.add(jButtonR_getPos,null);
    jButtonR_getPos.setText("Get Pos.");
    jButtonR_getPos.setMinimumSize(rbSize);
    jButtonR_getPos.setMaximumSize(rbSize);
    jButtonR_getPos.setPreferredSize(rbSize);
    jButtonR_getPos.setToolTipText("Get the Current Datum/Translation");
    jButtonR_getPos.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonR_getPos.setEnabled(true);
    jPanel1A.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1A.add(Box.createHorizontalGlue());
    //Current Translation
    JPanel jPanel1T = new JPanel();
    jPanel1.add(jPanel1T);
    jPanel1.add(jPanel1M);//its new place
    jPanel1T.setLayout(new BoxLayoutFixed(jPanel1T,BoxLayoutFixed.LINE_AXIS));
    jPanel1T.add(Box.createRigidArea(new Dimension(10,10)));
    JLabel jLabelT=new JLabel("Current Translation:");
    jPanel1T.add(jLabelT, null);
    jLabelT.setMinimumSize(rbSize);
    jLabelT.setMaximumSize(rbSize);
    jLabelT.setPreferredSize(rbSize);
    jPanel1T.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1T.add(Box.createHorizontalGlue());
    jPanel1T.add(jTFTransX, null);
    jTFTransX.setToolTipText("Current Translation X component");
    jTFTransX.setMinimumSize(tfnSize);
    jTFTransX.setMaximumSize(tfSize);
    jTFTransX.setPreferredSize(tfSize);
    jPanel1T.add(jTFTransY, null);
    jTFTransY.setToolTipText("Current Translation Y component");
    jTFTransY.setMinimumSize(tfnSize);
    jTFTransY.setMaximumSize(tfSize);
    jTFTransY.setPreferredSize(tfSize);
    jPanel1T.add(jTFTransZ, null);
    jTFTransZ.setToolTipText("Current Translation Z component");
    jTFTransZ.setMinimumSize(tfnSize);
    jTFTransZ.setMaximumSize(tfSize);
    jTFTransZ.setPreferredSize(tfSize);
    jPanel1T.add(Box.createRigidArea(rbSize));
    jPanel1T.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel1T.add(Box.createHorizontalGlue());
    jPanel1.add(Box.createRigidArea(new Dimension(10,10)));
    //separator
    JPanel jPanelInpSepLine2 = new JPanel();
    jPanel1.add(jPanelInpSepLine2);
    jPanelInpSepLine2.setLayout(new BoxLayoutFixed(jPanelInpSepLine2,BoxLayoutFixed.LINE_AXIS));
    jPanelInpSepLine2.add(Box.createRigidArea(new Dimension(10,3)));
    JTextField jTFInpSepL2 = new JTextField();
    jPanelInpSepLine2.add(jTFInpSepL2, null);
    jTFInpSepL2.setEditable(false);
    jTFInpSepL2.setMinimumSize(new Dimension(1,3));
    jTFInpSepL2.setMaximumSize(new Dimension(2000,3));
    jTFInpSepL2.setPreferredSize(new Dimension(1,3));
    jPanelInpSepLine2.add(Box.createRigidArea(new Dimension(10,3)));
//    jPanel1.add(Box.createRigidArea(new Dimension(10,10)));
    //Reorientation
//    OrientationPanel jPanelReorient = new OrientationPanel(utils,bcm);
//    jPanel1.add(jPanelReorient);
//    jPanelReorient.setConfig(session.gui_config);
//  JPanel jPanelReorient = new JPanel();
//  jPanel1.add(jPanelReorient);
//    jPanelReorient.setLayout(new BoxLayoutFixed(jPanelReorient,BoxLayoutFixed.LINE_AXIS));
//    jPanelReorient.add(Box.createRigidArea(new Dimension(10,10)));
//    //Left
//    JPanel jPanelRLeft = new JPanel();
//    jPanelReorient.add(jPanelRLeft);
//    jPanelRLeft.setLayout(new BoxLayoutFixed(jPanelRLeft,BoxLayoutFixed.PAGE_AXIS));
//    JLabel jLabelRLTitle=new JLabel("Reorientations:");
//    jPanelRLeft.add(jLabelRLTitle, null);
//    jLabelRLTitle.setMinimumSize(rbSize);
//    jLabelRLTitle.setMaximumSize(rbSize);
//    jLabelRLTitle.setPreferredSize(rbSize);
//    JPanel jPanelRLeftAD = new JPanel();
//    jPanelRLeft.add(jPanelRLeftAD, null);
//    jPanelRLeftAD.setLayout(new BoxLayoutFixed(jPanelRLeftAD,BoxLayoutFixed.LINE_AXIS));
//    jPanelRLeftAD.add(Box.createHorizontalGlue());
//    jPanelRLeftAD.add(jButtonRLeftAdd);
//    jButtonRLeftAdd.setMinimumSize(rbSize);
//    jButtonRLeftAdd.setMaximumSize(rbSize);
//    jButtonRLeftAdd.setPreferredSize(rbSize);
//    jButtonRLeftAdd.setToolTipText("Add a new entry to the Reorientation list");
//    jButtonRLeftAdd.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
//    jPanelRLeftAD.add(jButtonRLeftDelete);
//    jButtonRLeftDelete.setMinimumSize(rbSize);
//    jButtonRLeftDelete.setMaximumSize(rbSize);
//    jButtonRLeftDelete.setPreferredSize(rbSize);
//    jButtonRLeftDelete.setToolTipText("Remove the selected entries from the Reorientation list");
//    jButtonRLeftDelete.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
//    jPanelRLeftAD.add(Box.createHorizontalGlue());
//    JPanel jPanelRLeftGetFile= new JPanel();    
//    jPanelRLeft.add(jPanelRLeftGetFile);
//    jPanelRLeftGetFile.setLayout(new BoxLayoutFixed(jPanelRLeftGetFile,BoxLayoutFixed.LINE_AXIS));
//    jPanelRLeftGetFile.add(Box.createHorizontalGlue());
//    jPanelRLeftGetFile.add(jButtonRLeftGetFile);
//    jButtonRLeftGetFile.setMinimumSize(rbdSize);
//    jButtonRLeftGetFile.setMaximumSize(rbdSize);
//    jButtonRLeftGetFile.setPreferredSize(rbdSize);
//    jButtonRLeftGetFile.setToolTipText("Get the reorienation vectors from the file specified");
//    jButtonRLeftGetFile.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
//    jPanelRLeftGetFile.add(Box.createHorizontalGlue());
//    JPanel jPanelRLeftGF=new JPanel();
//    jPanelRLeft.add(jPanelRLeftGF, null);
//    jPanelRLeftGF.setLayout(new BoxLayoutFixed(jPanelRLeftGF,BoxLayoutFixed.LINE_AXIS));
//    jPanelRLeftGF.add(jTextFieldRGF, null);
//    jTextFieldRGF.setToolTipText("Input Descriptor, or Matrix Definiton file for old alignment");
//    jTextFieldRGF.setMinimumSize(tfnSize);
//    jTextFieldRGF.setMaximumSize(tfSize);
//    jTextFieldRGF.setPreferredSize(tfSize);
//    jPanelRLeftGF.add(jButtonRGF, null);
//    jButtonRGF.addActionListener(new StacMainWin_FileChooser(this));
//    jButtonRGF.setMinimumSize(fsbSize);
//    jButtonRGF.setMaximumSize(fsbSize);
//    jButtonRGF.setPreferredSize(fsbSize);
//    jButtonRGF.setToolTipText("Select the file");
//    jPanelRLeftGF.add(Box.createRigidArea(new Dimension(10,10)));
//    jPanelRLeftGF.add(Box.createHorizontalGlue());
//    jPanelReorient.add(Box.createRigidArea(new Dimension(10,10)));
//    //Right
//    jPanelReorient.add(jpanelReorientTable);
//    jPanelReorient.add(Box.createRigidArea(new Dimension(10,10)));
//    jPanelReorient.add(Box.createHorizontalGlue());
//    reorientTableModel.setTable(jTableReorient);    
	//
    
//    jPanel1.add(jPanel1_1);
//    jPanel1.add(Box.createVerticalGlue());

    
    /////////////////////////////////////////
    
    //panel1_1
//    panel1_1.setLayout(new BorderLayout());
//    panel1_1.setBorder(BorderFactory.createRaisedBevelBorder());
//    panel1_1.add(panel1_1S, BorderLayout.SOUTH);
//    panel1_1S.setLayout(new BoxLayoutFixed(panel1_1S,BoxLayoutFixed.LINE_AXIS));
//    panel1_1S.add(Box.createHorizontalGlue());
//    panel1_1S.add(Box.createHorizontalGlue());
//    panel1_1S.add(jButton1_1);
//    panel1_1S.add(Box.createHorizontalGlue());
//    jButton1_1.setText("Next >");
//    jButton1_1.setPreferredSize(new Dimension(100,34));
//    jButton1_1.setToolTipText("Load the Crystal Characterisation Data");
//    jButton1_1.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
//    panel1_1.add(panel1_1W,BorderLayout.WEST);
//    panel1_1W.setLayout(new BoxLayoutFixed(panel1_1W,BoxLayoutFixed.PAGE_AXIS));
//    panel1_1W.add(Box.createRigidArea(new Dimension(10,10)));
//    panel1_1.add(panel1_1E,BorderLayout.EAST);
//    panel1_1E.setLayout(new BoxLayoutFixed(panel1_1E,BoxLayoutFixed.PAGE_AXIS));
//    panel1_1E.add(Box.createRigidArea(new Dimension(10,10)));
//    panel1_1.add(panel1_1N,BorderLayout.NORTH);
//    panel1_1N.setLayout(new BoxLayoutFixed(panel1_1N,BoxLayoutFixed.PAGE_AXIS));
//    panel1_1N.add(Box.createRigidArea(new Dimension(10,10)));
//    panel1_1.add(jPanel1_1, BorderLayout.CENTER);
    jPanel1_1.setBorder(BorderFactory.createRaisedBevelBorder());
    //jPanel1.setBounds(new Rectangle(37, 17, 355, 196));
    jPanel1_1.setLayout(new BoxLayoutFixed(jPanel1_1,BoxLayoutFixed.PAGE_AXIS));
    //jPanel1_1.setAlignmentX(LEFT_ALIGNMENT);
    jPanel1_1.add(Box.createRigidArea(new Dimension(10,10)));
    //crystal params - cell
    JPanel jPanelCPar = new JPanel();
    jPanel1_1.add(jPanelCPar);
    jPanelCPar.setLayout(new BoxLayoutFixed(jPanelCPar,BoxLayoutFixed.LINE_AXIS));
    jPanelCPar.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelCPar.add(new JLabel("Cell: "), null);
    jPanelCPar.add(jTFCPar, null);
    jTFCPar.setEditable(false);
    jTFCPar.setMinimumSize(tfnSize);
    jTFCPar.setMaximumSize(tfSize);
    jTFCPar.setPreferredSize(tfSize);
    jPanelCPar.add(Box.createRigidArea(new Dimension(10,10)));
    //crystal params - spacegroup
    //JPanel jPanelCPar3 = new JPanel();
    //jPanel1_1.add(jPanelCPar3);
    ///jPanelCPar3.setLayout(new BoxLayoutFixed(jPanelCPar3,BoxLayoutFixed.LINE_AXIS));
    //jPanelCPar3.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelCPar.add(new JLabel("Space Group: "), null);
    jPanelCPar.add(jTFCPar3, null);
    jTFCPar3.setEditable(false);
    jTFCPar3.setMinimumSize(tfnSize);
    jTFCPar3.setMaximumSize(tfSize);
    jTFCPar3.setPreferredSize(rbdSize);
    //jPanelCPar3.add(Box.createRigidArea(new Dimension(10,10)));
    //crystal params - resolution
    //JPanel jPanelCPar2 = new JPanel();
    //jPanel1_1.add(jPanelCPar2);
    //jPanelCPar2.setLayout(new BoxLayoutFixed(jPanelCPar2,BoxLayoutFixed.LINE_AXIS));
    //jPanelCPar2.add(Box.createRigidArea(new Dimension(10,10)));
    jPanelCPar.add(new JLabel("Resolution: "), null);
    jPanelCPar.add(jTFCPar2, null);
    //TODO
    jTFCPar2.setEditable(true);
    jTFCPar2.setMinimumSize(tfnSize);
    jTFCPar2.setMaximumSize(tfSize);
    jTFCPar2.setPreferredSize(rbdSize);
    //jPanelCPar2.add(Box.createRigidArea(new Dimension(10,10)));
    //separator
    JPanel jPanelInpSepLine_1 = new JPanel();
    jPanel1_1.add(jPanelInpSepLine_1);
    jPanelInpSepLine_1.setLayout(new BoxLayoutFixed(jPanelInpSepLine_1,BoxLayoutFixed.LINE_AXIS));
    jPanelInpSepLine_1.add(Box.createRigidArea(new Dimension(10,3)));
    JTextField jTFInpSepL_1 = new JTextField();
    jPanelInpSepLine_1.add(jTFInpSepL_1, null);
    jTFInpSepL_1.setEditable(false);
    jTFInpSepL_1.setMinimumSize(new Dimension(1,1));
    jTFInpSepL_1.setMaximumSize(new Dimension(2000,1));
    jTFInpSepL_1.setPreferredSize(new Dimension(1,1));
    jPanelInpSepLine_1.add(Box.createRigidArea(new Dimension(10,1)));
    jPanel1_1.add(Box.createRigidArea(new Dimension(10,10)));
    //Reorientation
//    OrientationPanel jPanelReorient = new OrientationPanel(utils,bcm);
    jPanel1_1.add(jPanelReorient);
    jPanelReorient.setConfig(session.gui_config);
    jPanel1_1.add(Box.createVerticalGlue());

    jPanel1.add(jPanel1_1);
    jPanel1.add(Box.createVerticalGlue());
    
    
    /////////////////////////////////////////
    
    //panel3
    panel3.setLayout(new BorderLayout());
    panel3.setBorder(BorderFactory.createRaisedBevelBorder());
    panel3.add(panel3S, BorderLayout.SOUTH);
    panel3S.setLayout(new BoxLayoutFixed(panel3S,BoxLayoutFixed.LINE_AXIS));
    panel3S.add(Box.createHorizontalGlue());
    panel3S.add(Box.createHorizontalGlue());
    panel3S.add(jButton32);
    jButton32.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButton32.setText("Next >");
    jButton32.setPreferredSize(new Dimension(100,34));
    panel3S.add(Box.createHorizontalGlue());
    panel3.add(panel3W,BorderLayout.WEST);
    panel3W.setLayout(new BoxLayoutFixed(panel3W,BoxLayoutFixed.PAGE_AXIS));
    panel3W.add(Box.createRigidArea(new Dimension(10,10)));
    panel3.add(panel3E,BorderLayout.EAST);
    panel3E.setLayout(new BoxLayoutFixed(panel3E,BoxLayoutFixed.PAGE_AXIS));
    panel3E.add(Box.createRigidArea(new Dimension(10,10)));
    panel3.add(panel3N,BorderLayout.NORTH);
    panel3N.setLayout(new BoxLayoutFixed(panel3N,BoxLayoutFixed.PAGE_AXIS));
    panel3N.add(Box.createRigidArea(new Dimension(10,10)));
//  panel3.add(jPanel3, BorderLayout.CENTER);
    panel3.add(datumPanel, BorderLayout.CENTER);
    datumPanel.setConfig(session.gui_config);
/*    
    jPanel3.setLayout(new BoxLayoutFixed(jPanel3,BoxLayoutFixed.PAGE_AXIS));
    jPanel3.setBorder(BorderFactory.createRaisedBevelBorder());
    //jPanel3.setBounds(new Rectangle(37, 17, 575, 196));
    //jPanel3.setPreferredSize(new Dimension(140, 140));
    jPanel3.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel3.add(jLabelAlignVector, null);
    jLabelAlignVector.setText("Alignment Vectors:");
    jLabelAlignVector.setBounds(new Rectangle(10, 0, 123, 34));
    //jPanel3.add(jButton31, null);
    //jButton31.setText("Process");
    //jButton31.setBounds(new Rectangle(229, 151, 88, 34));
    jPanel3.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel3.add(jpanelTable, null);
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

    //trans settings labels
    JPanel jPanelEvalTrans = new JPanel();
    jPanel3.add(jPanelEvalTrans);
    jPanelEvalTrans.setLayout(new BoxLayoutFixed(jPanelEvalTrans,BoxLayoutFixed.LINE_AXIS));
    jPanelEvalTrans.add(Box.createRigidArea(new Dimension(10,10)));
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
    jPanel3.add(jPanelEvalTransf);
    jPanelEvalTransf.setLayout(new BoxLayoutFixed(jPanelEvalTransf,BoxLayoutFixed.LINE_AXIS));
    jPanelEvalTransf.add(Box.createRigidArea(new Dimension(10,10)));
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
    jButtonEval_getP.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonEval_getP.setEnabled(true);
    jPanelEvalTransf.add(jButtonEval_getP,null);
    jPanelEvalTransf.add(Box.createRigidArea(new Dimension(10,10)));
    //separator
    JPanel jPanel3SepLine = new JPanel();
    jPanel3.add(jPanel3SepLine);
    jPanel3SepLine.setLayout(new BoxLayoutFixed(jPanel3SepLine,BoxLayoutFixed.LINE_AXIS));
    jPanel3SepLine.add(Box.createRigidArea(new Dimension(10,10)));
    JTextField jTF3SepL = new JTextField();
    jPanel3SepLine.add(jTF3SepL, null);
    jTF3SepL.setEditable(false);
    jTF3SepL.setMinimumSize(new Dimension(1,3));
    jTF3SepL.setMaximumSize(new Dimension(2000,3));
    jTF3SepL.setPreferredSize(new Dimension(1,3));
    jPanel3SepLine.add(Box.createRigidArea(new Dimension(10,10)));
    //trans settings fields for arbitrary motion
    JPanel jPanelEvalTransf2 = new JPanel();
    jPanel3.add(jPanelEvalTransf2);
    jPanelEvalTransf2.setLayout(new BoxLayoutFixed(jPanelEvalTransf2,BoxLayoutFixed.LINE_AXIS));
    jPanelEvalTransf2.add(Box.createRigidArea(new Dimension(10,10)));
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
    jButtonEval_getP2.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButtonEval_getP2.setEnabled(true);
    jPanelEvalTransf2.add(jButtonEval_getP2,null);
    jPanelEvalTransf2.add(Box.createRigidArea(new Dimension(10,10)));
    jPanel3.add(Box.createRigidArea(new Dimension(10,10)));
*/    

    //panel4
    panel4.setLayout(new BorderLayout());
    panel4.setBorder(BorderFactory.createRaisedBevelBorder());
    panel4.add(panel4S, BorderLayout.SOUTH);
    panel4S.setLayout(new BoxLayoutFixed(panel4S,BoxLayoutFixed.LINE_AXIS));
    panel4S.add(Box.createHorizontalGlue());
    panel4S.add(Box.createHorizontalGlue());
    panel4S.add(jButton42);
    jButton42.addActionListener(new StacMainWin_jMainButton_actionAdapter(this));
    jButton42.setText("Finish");
    jButton42.setPreferredSize(new Dimension(100,34));
    panel4S.add(Box.createHorizontalGlue());
    panel4.add(panel4W,BorderLayout.WEST);
    panel4W.setLayout(new BoxLayoutFixed(panel4W,BoxLayoutFixed.PAGE_AXIS));
    panel4W.add(Box.createRigidArea(new Dimension(10,10)));
    panel4.add(panel4E,BorderLayout.EAST);
    panel4E.setLayout(new BoxLayoutFixed(panel4E,BoxLayoutFixed.PAGE_AXIS));
    panel4E.add(Box.createRigidArea(new Dimension(10,10)));
    panel4.add(panel4N,BorderLayout.NORTH);
    panel4N.setLayout(new BoxLayoutFixed(panel4N,BoxLayoutFixed.PAGE_AXIS));
    panel4N.add(Box.createRigidArea(new Dimension(10,10)));
    //panel4.add(jPanel4, BorderLayout.CENTER);
    panel4.add(strategyWidget, BorderLayout.CENTER);
    strategyWidget.setConfig(session.gui_config);
    
//    jPanel4.setLayout(new BoxLayoutFixed(jPanel4,BoxLayoutFixed.PAGE_AXIS));
//    jPanel4.setBorder(BorderFactory.createRaisedBevelBorder());
//    //jPanel4.setBounds(new Rectangle(37, 17, 575, 196));
//    //jPanel4.setPreferredSize(new Dimension(140, 140));
//    jPanel4.add(Box.createRigidArea(new Dimension(10,10)));
//    jPanel4.add(jLabelStrategy, null);
//    jLabelStrategy.setText("Multiple-Sweep Strategies:");
//    jLabelStrategy.setBounds(new Rectangle(10, 0, 123, 34));
//    //jPanel4.add(jButton41, null);
//    //jButton41.setText("Process");
//    //jButton41.setBounds(new Rectangle(229, 151, 88, 34));
//    jPanel4.add(Box.createRigidArea(new Dimension(10,10)));
//    jPanel4.add(jpanelTableStrategy, null);
//    jpanelTableStrategy.getViewport().add(jTableStrategy);
//    jpanelTableStrategy.setBounds(new Rectangle(9,38,558,99));
//    //jTableStrategy.setMaximumSize(new Dimension(1000, 10000));
//    //jTableStrategy.setPreferredSize(new Dimension(800, 10000));
//    jTableStrategy.setToolTipText("Multiple-Sweep Stretegyies with Completness");
//    jTableStrategy.setVerifyInputWhenFocusTarget(true);
//    jTableStrategy.setAutoscrolls(true);
//    jTableStrategy.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//    jTableStrategy.setDefaultRenderer(Object.class, strategyTableCellRenderer);
//    jTableStrategy.setDefaultRenderer(String.class, strategyTableCellRenderer);
//    jTableStrategy.setDefaultRenderer(Double.class, strategyTableCellRenderer);
//    jTableStrategy.setDefaultRenderer(Integer.class, strategyTableCellRenderer);

    

    jTabbedPane1.setSelectedIndex(jTabbedPane1.getSelectedIndex()+1);    
    jTabbedPane1.setSelectedIndex(jTabbedPane1.getSelectedIndex()+1);    
    jTabbedPane1.setSelectedIndex(jTabbedPane1.getSelectedIndex()+1);    
    
  }

  public void fillStrategyTable() {
    int numOfVecs = datumPanel.jTable1.getSelectedRowCount();
    int[] vecIDs = datumPanel.jTable1.getSelectedRows();

    //fill initial strategy table
    //if (strategyWidget.jTableStrategy.getRowCount()>0)
    //	strategyWidget.jTableStrategy.setValueAt("ClearTable",0,0);
//    StrategyTableModel tmodel = (StrategyTableModel) strategyWidget.jTableStrategy.getModel();
//    AlignTableModel amodel = (AlignTableModel) datumPanel.jTable1.getModel();
//    for (int i=0;i<numOfVecs;i++) {
//    	strategyWidget.jTableStrategy.setValueAt(new Double(i+1), i, tmodel.getColId("ID"));
//    	strategyWidget.jTableStrategy.setValueAt(new Double(0), i,   tmodel.getColId("1S:OmegaStart"));
//    	strategyWidget.jTableStrategy.setValueAt(new Double(0), i,   tmodel.getColId("1S:OmegaEnd"));
//    	strategyWidget.jTableStrategy.setValueAt(new Double(0), i,   tmodel.getColId("1s:Completeness"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("v1x")), i, tmodel.getColId("2S:v1x"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("v1y")), i, tmodel.getColId("v1y"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("v1z")), i, tmodel.getColId("v1z"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("v2x")), i, tmodel.getColId("v2x"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("v2y")), i, tmodel.getColId("v2y"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("v2z")), i, tmodel.getColId("v2z"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("omega")), i, tmodel.getColId("omegaStart"));
//    	strategyWidget.jTableStrategy.setValueAt(new Double(0), i,   tmodel.getColId("omegaEnd"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("kappa")), i, tmodel.getColId("kappa"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("phi")), i, tmodel.getColId("phi"));
//    	strategyWidget.jTableStrategy.setValueAt(datumPanel.jTable1.getValueAt(vecIDs[i],amodel.getColId("Trans")), i, tmodel.getColId("Trans"));
//    	strategyWidget.jTableStrategy.setValueAt(new Double(0), i,   tmodel.getColId("Completeness"));
//    	strategyWidget.jTableStrategy.setValueAt(new Double(0), i,   tmodel.getColId("Full Compl."));
//    	strategyWidget.jTableStrategy.setValueAt(new Double(0), i,   tmodel.getColId("Total Angle"));
//    	strategyWidget.jTableStrategy.setValueAt(new Double(0), i,   tmodel.getColId("Rank"));
//    }

    if (true) {
    
    //
//    //call strategy...(2sweep)...
//    numOfVecs = strategyWidget.jTableStrategy.getRowCount();
//    //foreach
    Vector DesiredOrientations = new Vector();
    Vector datums = datumPanel.getDatumRequest();
    
    for (int i=0;i<datums.size();) {
//    for (int i=0;i<numOfVecs;i++) {
      //write name1.x (with modified matrices)
      //======================================
      //get data
//      double[] v1=new double[] {((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("2s:v1x")))).doubleValue(),
//                                ((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("v1y")))).doubleValue(),
//                                ((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("v1z")))).doubleValue()};
//      double[] v2=new double[] {((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("v2x")))).doubleValue(),
//                                ((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("v2y")))).doubleValue(),
//                                ((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("v2z")))).doubleValue()};
//      double omega=((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("Omegastart")))).doubleValue();
//      double kappa=((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("kappa")))).doubleValue();
//      double phi  =((Double)(strategyWidget.jTableStrategy.getValueAt(i,tmodel.getColId("phi")))).doubleValue();

    	String v1=(String)datums.elementAt(i++);//V1
    	i++;//V2
    	double omega=((Double)datums.elementAt(i++)).doubleValue();
    	double kappa=((Double)datums.elementAt(i++)).doubleValue();
    	double phi=((Double)datums.elementAt(i++)).doubleValue();
    	i++;//trans
    	i++;//rank
    	String comment=(String)datums.elementAt(i++);
    	
      Vector3d newDatum = new Vector3d(omega,kappa,phi);
      DesiredOrientations.addElement(newDatum);      
      DesiredOrientations.addElement(comment+" "+v1);      
    }
    
    
    //get the current DATUM to be taken into account
	double omegaCurrent=0,kappaCurrent=0,phiCurrent=0;
  	try {
  	omegaCurrent=new Double(jTFOmega.getText()).doubleValue();
  	} catch (NumberFormatException nfe){
  	}
  	try {
  	kappaCurrent=new Double(jTFKappa.getText()).doubleValue();
	} catch (NumberFormatException nfe){
  	}
  	try {
  	phiCurrent  =new Double(jTFPhi.getText()).doubleValue();
  	} catch (NumberFormatException nfe){
  	}
  	
    Vector3d currDatum = new Vector3d(omegaCurrent,kappaCurrent,phiCurrent);
  	
//    String strategyFile=strategy.CalculateStrategy("./",currDatum,DesiredOrientations,bcm,utils);    
//    ReadCalculatedStrategies(strategyFile);
    
    String options="";
    try {
    	String res=jTFCPar2.getText();
    	if (res.length()>0) {
    		options=options+"%%%res="+res;
    	}
    } catch (NumberFormatException nfe){
    }

	Stac_StrategyReqMsg req = new Stac_StrategyReqMsg(session,respHandler,currDatum,DesiredOrientations,options,bcm,utils); 
	strategy.handle(req);
    
    
  } else {
      //ReadCalculatedStrategies("STAC_strategy.res");
  }
  
  }
  
  
  void ReadCalculatedStrategies(String myfile,int reqId) {
  	//StrategyTableModel tmodel = (StrategyTableModel) strategyWidget.jTableStrategy.getModel();
    //if (strategyWidget.jTableStrategy.getRowCount()>0)
    //	strategyWidget.jTableStrategy.setValueAt("ClearTable",0,0);
      try {
        String osName = System.getProperty("os.name");
        Stac_Out.println("File to be read: " + myfile);
        FileInputStream StrategyResultFile = new FileInputStream(myfile);
        DataInputStream grf = new DataInputStream(StrategyResultFile);
        //TextReader grf = new TextReader(StrategyResultFile);
        //get info for trans calc
        int counter=0;
        int strId=strategyWidget.getStrategyId();
        int maxStrId=0;
        try {
           do {
             int   strategyID = grf.readInt();//grf.readInt();
             double omegaStart = grf.readDouble();
             double omegaEnd   = grf.readDouble();
             double kappa      = grf.readDouble();
             double phi        = grf.readDouble();
             double completion = grf.readDouble();
             double res        = grf.readDouble();
             double rank       = grf.readDouble();
             String comment    = grf.readUTF();
             
             double incr=1.0;
             double expt=1.0;
             double scanstart=(int)omegaStart;
             double numimg=((int)((omegaEnd-scanstart)/incr))+1.0;
             double resolution = res;
             
             ParamTable cols= new ParamTable();
             maxStrId=Math.max(maxStrId,strId+strategyID);
             cols.setSingleValue("ID",new Integer(strId+strategyID));
             cols.setSingleValue("OmegaStart",new Double(scanstart));
             cols.setSingleValue("Incr",new Double(incr));
             cols.setSingleValue("Time",new Double(expt));
             cols.setSingleValue("Images",new Integer((int)numimg));
             cols.setSingleValue("1st img",new Integer(1));
             cols.setSingleValue("Resolution",new Double(resolution));
             cols.setSingleValue("Kappa",new Double(kappa));
             cols.setSingleValue("Phi",new Double(phi));
             cols.setSingleValue("Completeness",new Double(completion));
             cols.setSingleValue("rank",new Double(rank));
             cols.setSingleValue("Comment",comment);
             strategyWidget.addTableRow(cols);
             strategyWidget.setStrategyId(maxStrId);
             counter++;
           }
           while (true);
        } catch (Exception grfex) {
        	grf.close();
          	if (!(grfex instanceof IOException))
          		session.addErrorMsg(reqId,"Could not read result file ("+myfile+") entry #"+(counter+1));
        }
    	  StrategyResultFile.close();
      }
      catch (IOException ex2) {
    		session.addErrorMsg(reqId,"Could not read result file ("+myfile+")");
      }
  	
  }


  void ConvertInputFormat() {
  	ConvertInputFormat(jTextFieldDefFile,jTFMosflmSettingsFile,jRadioButtonMosflm,jRadioButtonXDS,jRadioButtonDenzo,session.getWorkDir()+"name0.x");
  }
  
  void ConvertInputFormat(JTextField jTF1,JTextField jTF2,JRadioButton jRBMosflm,JRadioButton jRBXDS,JRadioButton jRBDenzo,String outfile) {
      //add resolution request
	  String res=jTFCPar2.getText();
	  if (res!=null && res.length()!=0) {
		  res="%%%res="+res;
	  } else {
		  res="";
	  }
    if (jRBMosflm.isSelected()) {
      //Mosflm->Denzo (new conversion via DNZ ParamTable)
      try {
        String MosflmMatrixFileName=jTF1.getText();
        if(MosflmMatrixFileName.equals(""))
        	MosflmMatrixFileName=System.getProperty("STAC_DEF_MOS_MAT");
        
        String MosflmSettingsFileName;
        if(jTF2!=null && !jTF2.getText().equals("")) {
          MosflmSettingsFileName=jTF2.getText();
          if(MosflmSettingsFileName.equals(""))
        	MosflmSettingsFileName=System.getProperty("STAC_DEF_MOS_SETT");
        } else {
            File inFile = new File(MosflmMatrixFileName);
            String my_dir=inFile.getParent()+File.separator;
           	MosflmSettingsFileName=my_dir+"mosflm.inp";
        }
        
        utils.ConvertInputFormat(MosflmMatrixFileName,MosflmSettingsFileName+res,"Mosflm",outfile);
      }
      catch (Exception ex1) {
      }

    } else if (jRBXDS.isSelected()) {
      //infile must refer to the file CORRECT.LP
      String infile=jTF1.getText();
      if(infile.equals(""))
      	infile=System.getProperty("STAC_DEF_XDS");
      utils.ConvertInputFormat(infile,""+res,"XDS",outfile);

    }
    else {
        try {
            String infile=jTF1.getText();
            if (infile.equals("")) {
                infile=System.getProperty("STAC_DEF_HKL");
            }
            utils.ConvertInputFormat(infile,""+res,"Denzo",outfile);
        } catch (Exception e) {
        }
    }

  }
  
  void GonioUpdate() {
	  backupConfigFile();
  	
    Vector3d okp[]=new Vector3d[7];
    //utils.read_gnsdef(gnsfile,okp);
    bcm.getCalibration(okp);
	try {
		okp[0].x = new Double(jTFGCRomegaX.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[0].y = new Double(jTFGCRomegaY.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[0].z = new Double(jTFGCRomegaZ.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[1].x = new Double(jTFGCRkappaX.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[1].y = new Double(jTFGCRkappaY.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[1].z = new Double(jTFGCRkappaZ.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[2].x= new Double(jTFGCRphiX.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[2].y = new Double(jTFGCRphiY.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[2].z = new Double(jTFGCRphiZ.getText()).doubleValue();
	} catch (Exception e) {
	}
	//utils.write_gnsdef(gnsfile,okp);
	bcm.setCalibration(okp);
  	return;
  }
  
  void backupConfigFile() {
		//backup config file
 	  GregorianCalendar gc = new GregorianCalendar();	  		 	 
  	  String actDateStr = new PrintfFormat("%04d%02d%02d%02d%02d%02d.%03d").sprintf(new Object[] {
  				new Integer(gc.get(GregorianCalendar.YEAR)),
  				new Integer(gc.get(GregorianCalendar.MONTH)+1),
  				new Integer(gc.get(GregorianCalendar.DAY_OF_MONTH)),
  				new Integer(gc.get(GregorianCalendar.HOUR_OF_DAY)),
  				new Integer(gc.get(GregorianCalendar.MINUTE)),
  				new Integer(gc.get(GregorianCalendar.SECOND)),
  				new Integer(gc.get(GregorianCalendar.MILLISECOND)),
  				});

	try {
		utils.copyFile(new File(System.getProperty("BCMDEF")), new File(System.getProperty("BCMDEF")+"_calibBackup_"+actDateStr));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		session.errorMsg("Could not back up the config file:\n"+System.getProperty("BCMDEF"));
	}

  }
  
  void GonioTUpdate() {
	  backupConfigFile();
  	double kx=0,ky=0,kz=0,px=0,py=0,pz=0;
    Vector3d okp[]=new Vector3d[7];
    //utils.read_gnsdef(gnsfile,okp);
    bcm.getCalibration(okp);
	try {
		okp[3].x = new Double(jTFGTCRkappaX.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[3].y = new Double(jTFGTCRkappaY.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[3].z = new Double(jTFGTCRkappaZ.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[4].x = new Double(jTFGTCRphiX.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[4].y = new Double(jTFGTCRphiY.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[4].z = new Double(jTFGTCRphiZ.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[5].x = new Double(jTFGTCRkappaXR.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[5].y = new Double(jTFGTCRkappaYR.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[5].z = new Double(jTFGTCRkappaZR.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[6].x = new Double(jTFGTCRphiXR.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[6].y = new Double(jTFGTCRphiYR.getText()).doubleValue();
	} catch (Exception e) {
	}
	try {
		okp[6].z = new Double(jTFGTCRphiZR.getText()).doubleValue();
	} catch (Exception e) {
	}
	//utils.write_gnsdef(gnsfile, okp);
	bcm.setCalibration(okp);
  	return;
  }
  
  /**
   * calulate the calibration setting from the measured values
   * (do not copy the values simply from transcalib)
   * @param gnsfile
   */
  void GonioCalibration(){
    GonioCalibration(false);
  }
  void GonioCalibration(boolean copyfromTrans){
  	//oscillation start, as an omega rotation is assumed as rotation around (0,0,1)!

  	try {
  		
  		AxisAngle4d phi = new AxisAngle4d();
  		AxisAngle4d kappa = new AxisAngle4d();
  		AxisAngle4d omega = new AxisAngle4d();

  		//read
  		Vector3d okp[]=new Vector3d[7];
  		//utils.read_gnsdef(gnsfile,okp);
  		bcm.getCalibration(okp);
  		
  		if (copyfromTrans){
  			omega.set(new Vector3d(0,0,1),0);
  			kappa.set(okp[5],0);
  			phi.set(okp[6],0);
  			
  		} else {
  			
  			
  			double anglelimit=1.0;
  			
  			//[CA]=[C][A]
  			//Umat=Amat*[standard orientation matrix]
  			//
  			//what we need is: [RCA]=[C][R][A]=[C][R][C]^-1[CA]
  			//so the new Umat should be: Amat*EQmat*Amat_inv*Amat
  			double[] cell = new double[6];
  			double[] tcell = new double[6];
  			Matrix3d B = new Matrix3d();
  			Matrix3d Bm1t = new Matrix3d();
  			Matrix3d U = new Matrix3d();
  			Vector3d phixyz = new Vector3d();
  			
  			//get Matrix 0
  			ConvertInputFormat(jTFMat0File,(JTextField)null,jRBMat0Mosflm,jRBMat0XDS,jRBMat0Denzo,session.getWorkDir()+"Mat0.x");
  			ParamTable Mat0T = new ParamTable();
  			utils.read_denzo_x(session.getWorkDir()+"Mat0.x",Mat0T);
  			Matrix3d Mat0U = new Matrix3d(Mat0T.getDoubleVector("Amat"));
  			Matrix3d Mat0A = new Matrix3d(Mat0T.getDoubleVector("Umat"));
  			Matrix3d osc = new Matrix3d();
  			if (Mat0T.getFirstDoubleValue("phi_start")!=0.0) {
  				Stac_Out.println("WARNING: Omega oscillation is taken into account without verified omega axis!");
  			}
  			Matrix3d Mat0UB = new Matrix3d(Mat0A);
  			//comment the next line to use the denzo matrix
  			utils.denzo2mosflm_matrices_by_gonset(Mat0U,cell,tcell,B,Bm1t,U,Mat0UB,phixyz);
  			osc.set(new AxisAngle4d(0,0,1,Mat0T.getFirstDoubleValue("phi_start")/180*Math.PI));
  			Mat0UB.mul(osc,Mat0UB);
  			Stac_Out.println("Matrix 0:\n"+Mat0UB.toString());
  			//get Matrix 1
  			ConvertInputFormat(jTFMat1File,(JTextField)null,jRBMat1Mosflm,jRBMat1XDS,jRBMat1Denzo,session.getWorkDir()+"Mat1.x");
  			ParamTable Mat1T = new ParamTable();
  			utils.read_denzo_x(session.getWorkDir()+"Mat1.x",Mat1T);
  			Matrix3d Mat1U = new Matrix3d(Mat1T.getDoubleVector("Amat"));
  			Matrix3d Mat1A = new Matrix3d(Mat1T.getDoubleVector("Umat"));
  			Matrix3d Mat1Ai= new Matrix3d(Mat1A); Mat1Ai.invert();
  			Matrix3d Mat1UB = new Matrix3d(Mat1A);
  			//comment the next line to use the denzo matrix
  			utils.denzo2mosflm_matrices_by_gonset(Mat1U,cell,tcell,B,Bm1t,U,Mat1UB,phixyz);
  			osc.set(new AxisAngle4d(0,0,1,Mat1T.getFirstDoubleValue("phi_start")/180*Math.PI));
  			Mat1UB.mul(osc,Mat1UB);
  			Stac_Out.println("Matrix 1:\n"+Mat1UB.toString());
  			//get Matrix 2
  			ConvertInputFormat(jTFMat2File,(JTextField)null,jRBMat2Mosflm,jRBMat2XDS,jRBMat2Denzo,session.getWorkDir()+"Mat2.x");
  			ParamTable Mat2T = new ParamTable();
  			utils.read_denzo_x(session.getWorkDir()+"Mat2.x",Mat2T);
  			Matrix3d Mat2U = new Matrix3d(Mat2T.getDoubleVector("Amat"));
  			Matrix3d Mat2A = new Matrix3d(Mat2T.getDoubleVector("Umat"));
  			Matrix3d Mat2UB = new Matrix3d(Mat2A);
  			//comment the next line to use the denzo matrix
  			utils.denzo2mosflm_matrices_by_gonset(Mat2U,cell,tcell,B,Bm1t,U,Mat2UB,phixyz);
  			osc.set(new AxisAngle4d(0,0,1,Mat2T.getFirstDoubleValue("phi_start")/180*Math.PI));
  			Mat2UB.mul(osc,Mat2UB);
  			Stac_Out.println("Matrix 2:\n"+Mat2UB.toString());
  			//get Matrix 3
  			ConvertInputFormat(jTFMat3File,(JTextField)null,jRBMat3Mosflm,jRBMat3XDS,jRBMat3Denzo,session.getWorkDir()+"Mat3.x");
  			ParamTable Mat3T = new ParamTable();
  			utils.read_denzo_x(session.getWorkDir()+"Mat3.x",Mat3T);
  			Matrix3d Mat3U = new Matrix3d(Mat3T.getDoubleVector("Amat"));
  			Matrix3d Mat3A = new Matrix3d(Mat3T.getDoubleVector("Umat"));
  			Matrix3d Mat3Ai= new Matrix3d(Mat3A); Mat3Ai.invert();
  			Matrix3d Mat3UB = new Matrix3d(Mat3A);
  			//comment the next line to use the denzo matrix
  			utils.denzo2mosflm_matrices_by_gonset(Mat3U,cell,tcell,B,Bm1t,U,Mat3UB,phixyz);
  			osc.set(new AxisAngle4d(0,0,1,Mat3T.getFirstDoubleValue("phi_start")/180*Math.PI));
  			Mat3UB.mul(osc,Mat3UB);
  			Stac_Out.println("Matrix 3:\n"+Mat3UB.toString());
  			
  			SpaceGroupLib spcgrp = new SpaceGroupLib(-1,(Object)Mat0T.getFirstStringValue("spg"));
  			Vector VecEq = new Vector();
  			//select the eq matrices in the next two lines (laueeqs vs geom equs)
  			//spcgrp.lauegroupmatrices(spcgrp.i_lg[spcgrp.actgrp],VecEq);
  			utils.GenerateEquivalentMatrices(Mat0UB,VecEq);
  			//test: manipulate the eq matrices
  			for(int i=0;i<VecEq.size();i++)
  			{
  				//use the transpose
  				//((Matrix3d)VecEq.elementAt(i)).transpose();
  				//apply the matrices in GNS space if they were given in denzo space
  				//Denzo2GNS((Matrix3d)VecEq.elementAt(i));
  			}
  			//get the whole set of possible matrices 1
  			Vector Mat1Vec = new Vector();
  			Mat1Vec.addElement(Mat1UB);
  			Vector3d len1= new Vector3d();Vector3d ang1= new Vector3d();utils.moscel(1,Mat1UB,len1,ang1);Stac_Out.println("Mat1UB: "+len1+" "+ang1);
  			for(int i=0;i<VecEq.size();i++)
  			{
  				Matrix3d eqMat= new Matrix3d(); eqMat.mul((Matrix3d)Mat1Vec.elementAt(0),(Matrix3d)VecEq.elementAt(i));
  				Vector3d len= new Vector3d();Vector3d ang= new Vector3d();utils.moscel(1,eqMat,len,ang);Stac_Out.println("GEM #"+i+" "+len+" "+ang);
  				if (Math.abs(ang1.x-ang.x)>anglelimit || Math.abs(ang1.y-ang.y)>anglelimit || Math.abs(ang1.z-ang.z)>anglelimit) continue;
  				Stac_Out.println("Mat1Vec #"+Mat1Vec.size()+" is built using GEM #"+i);
  				Mat1Vec.addElement(eqMat);
  			}
  			//get the whole set of possible matrices 2
  			Vector Mat2Vec = new Vector();
  			Mat2Vec.addElement(Mat2UB);
  			utils.moscel(1,Mat2UB,len1,ang1);Stac_Out.println("Mat2UB: "+len1+" "+ang1);
  			for(int i=0;i<VecEq.size();i++)
  			{
  				Matrix3d eqMat= new Matrix3d(); eqMat.mul((Matrix3d)Mat2Vec.elementAt(0),(Matrix3d)VecEq.elementAt(i));
  				Vector3d len= new Vector3d();Vector3d ang= new Vector3d();utils.moscel(1,eqMat,len,ang);Stac_Out.println("GEM #"+i+" "+len+" "+ang);
  				if (Math.abs(ang1.x-ang.x)>anglelimit || Math.abs(ang1.y-ang.y)>anglelimit || Math.abs(ang1.z-ang.z)>anglelimit) continue;
  				Stac_Out.println("Mat2Vec #"+Mat2Vec.size()+" is built using GEM #"+i);
  				Mat2Vec.addElement(eqMat);
  			}
  			//get the whole set of possible matrices 3
  			Vector Mat3Vec = new Vector();
  			Mat3Vec.addElement(Mat3UB);
  			utils.moscel(1,Mat3UB,len1,ang1);Stac_Out.println("Mat3UB: "+len1+" "+ang1);
  			for(int i=0;i<VecEq.size();i++)
  			{
  				Matrix3d eqMat= new Matrix3d(); eqMat.mul((Matrix3d)Mat3Vec.elementAt(0),(Matrix3d)VecEq.elementAt(i));
  				Vector3d len= new Vector3d();Vector3d ang= new Vector3d();utils.moscel(1,eqMat,len,ang);Stac_Out.println("GEM #"+i+" "+len+" "+ang);
  				if (Math.abs(ang1.x-ang.x)>anglelimit || Math.abs(ang1.y-ang.y)>anglelimit || Math.abs(ang1.z-ang.z)>anglelimit) continue;
  				Stac_Out.println("Mat3Vec #"+Mat3Vec.size()+" is built using GEM #"+i);
  				Mat3Vec.addElement(eqMat);
  				
  				//debug:
  				//check the standard orientation matrix [A]=[C]^-1*[CA] (=[C]^-1*[C][A])
  				//(it was OK for insuline)
  				//Matrix3d eqMatA= new Matrix3d(Mat3Ai);
  				//eqMatA.mul(Mat3U);
  				//Stac_Out.println(eqMatA);
  			}
  			Stac_Out.println("Number of eq matrixes: "+VecEq.size()+"\n"+VecEq.toString());
  			
  			//PHI
  			int eqct;
  			double period = Math.PI*2;
  			double phiRef = 0;
  			try {
  				phiRef = new Double(jTFGCPhi.getText()).doubleValue()/180.0*Math.PI;
  			} catch (Exception e) {
  			}
  			Stac_Out.println("phi ref:"+phiRef*180/Math.PI);
  			double phiDiff = 400;
  			int phipt =0;
  			Matrix3d refMat = new Matrix3d(Mat0UB);
  			refMat.invert();
  			for (eqct=0;eqct<Mat1Vec.size();eqct++) {
  				//calculate all rotations (axis+angle)
  				AxisAngle4d actphi = new AxisAngle4d();
  				Matrix3d actMat = new Matrix3d();
  				actMat.mul((Matrix3d)(Mat1Vec.elementAt(eqct)),refMat);
  				//actphi.set(actMat);
  				Quat4d actQ = new Quat4d(); Matrix3d actMatNorm = new Matrix3d(actMat); actMatNorm.normalizeCP(); actQ.set(actMatNorm);
  				actphi.set(actQ);
  				//CalculateRotation(actMat,actphi,1e-5);
  				//check the rotation matrix if it is really able to transform the orig matrix to the new
  				//(symmetry eq matrices may confuse the calculatoin)
  				Matrix3d rotMat = new Matrix3d();
  				rotMat.set(actphi);
  				//the following test is very sensitive to the quality of the data! 
  				//  			if (!rotMat.epsilonEquals(actMat,1e-1)) {
  				//  				Stac_Out.println("No real rotation found");
  				//  				continue;
  				//  			}
  				if (actphi.angle*phiRef<0) {actphi.x*=-1;actphi.y*=-1;actphi.z*=-1;actphi.angle*=-1;}
  				//select the appropriate one
  				Stac_Out.println(" "+actphi+" "+actphi.angle/Math.PI*180.0);
  				double actphiDiff=(actphi.angle-phiRef)+6.0*Math.PI;
  				actphiDiff=actphiDiff-(double)((int)(actphiDiff/period))*period;
  				if (actphiDiff*2>period) actphiDiff=period-actphiDiff;
  				if (actphiDiff<phiDiff) {
  					phi=actphi;
  					phiDiff=actphiDiff;
  					phipt=eqct;
  				} 
  				actphiDiff=(-actphi.angle-phiRef)+6.0*Math.PI;
  				actphiDiff=actphiDiff-(double)((int)(actphiDiff/period))*period;
  				if (actphiDiff*2>period) actphiDiff=period-actphiDiff;
  				if (actphiDiff<phiDiff) {
  					//actphi.x*=-1;actphi.y*=-1;actphi.z*=-1;actphi.angle*=-1;
  					phi=actphi;
  					phiDiff=actphiDiff;
  					phipt=eqct;
  				}
  			}
  			
  			//KAPPA
  			double kappaRef = 0;
  			try {
  				kappaRef = new Double(jTFGCKappa.getText()).doubleValue()/180.0*Math.PI;
  			} catch (Exception e) {
  			}
  			Stac_Out.println("kappa ref:"+kappaRef*180/Math.PI);
  			double kappaDiff = 400;
  			int kappapt =0;
  			refMat.invert((Matrix3d)(Mat1Vec.elementAt(phipt)));
  			for (eqct=0;eqct<Mat2Vec.size();eqct++) {
  				//calculate all rotations (axis+angle)
  				AxisAngle4d actkappa = new AxisAngle4d();
  				Matrix3d actMat = new Matrix3d();
  				actMat.mul((Matrix3d)(Mat2Vec.elementAt(eqct)),refMat);
  				Quat4d actQ = new Quat4d(); Matrix3d actMatNorm = new Matrix3d(actMat); actMatNorm.normalizeCP(); actQ.set(actMatNorm);
  				actkappa.set(actQ);
  				//check the rotation matrix if it is really able to transform the orig matrix to the new
  				//(symmetry eq matrices may confuse the calculatoin)
  				Matrix3d rotMat = new Matrix3d();
  				rotMat.set(actkappa);
  				//the following test is very sensitive to the quality of the data! 
  				//  			if (!rotMat.epsilonEquals(actMat,1e-1)) {
  				//  				Stac_Out.println("No real rotation found");
  				//  				continue;
  				//  			}
  				if (actkappa.angle*kappaRef<0) {actkappa.x*=-1;actkappa.y*=-1;actkappa.z*=-1;actkappa.angle*=-1;}
  				//select the appropriate one
  				Stac_Out.println(" "+actkappa+" "+actkappa.angle/Math.PI*180.0);
  				double actkappaDiff=(actkappa.angle-kappaRef)+6.0*Math.PI;
  				actkappaDiff=actkappaDiff-(double)((int)(actkappaDiff/period))*period;
  				if (actkappaDiff*2>period) actkappaDiff=period-actkappaDiff;
  				if (actkappaDiff<kappaDiff) {
  					kappa=actkappa;
  					kappaDiff=actkappaDiff;
  					kappapt=eqct;
  				}
  				actkappaDiff=(-actkappa.angle-kappaRef)+6.0*Math.PI;
  				actkappaDiff=actkappaDiff-(double)((int)(actkappaDiff/period))*period;
  				if (actkappaDiff*2>period) actkappaDiff=period-actkappaDiff;
  				if (actkappaDiff<kappaDiff) {
  					kappa=actkappa;
  					kappaDiff=actkappaDiff;
  					kappapt=eqct;
  				}
  			}
  			
  			//OMEGA
  			double omegaRef = 0;
  			try {
  				omegaRef = new Double(jTFGCOmega.getText()).doubleValue()/180.0*Math.PI;
  			} catch (Exception e) {
  			}
  			Stac_Out.println("omega ref:"+omegaRef*180/Math.PI);
  			double omegaDiff = 400;
  			int omegapt =0;
  			refMat.invert((Matrix3d)(Mat2Vec.elementAt(kappapt)));
  			for (eqct=0;eqct<Mat3Vec.size();eqct++) {
  				//calculate all rotations (axis+angle)
  				AxisAngle4d actomega = new AxisAngle4d();
  				Matrix3d actMat = new Matrix3d();
  				actMat.mul((Matrix3d)(Mat3Vec.elementAt(eqct)),refMat);
  				//actomega.set(actMat);
  				Quat4d actQ = new Quat4d(); Matrix3d actMatNorm = new Matrix3d(actMat); actMatNorm.normalizeCP(); actQ.set(actMatNorm);
  				actomega.set(actQ);
  				//CalculateRotation(actMat,actomega,1e-5);
  				//check the rotation matrix if it is really able to transform the orig matrix to the new
  				//(symmetry eq matrices may confuse the calculatoin)
  				Matrix3d rotMat = new Matrix3d();
  				rotMat.set(actomega);
  				//the following test is very sensitive to the quality of the data! 
  				//  			if (!rotMat.epsilonEquals(actMat,1e-1)) {
  				//  				Stac_Out.println("No real rotation found");
  				//  				continue;
  				//  			}
  				if (actomega.angle*omegaRef<0) {actomega.x*=-1;actomega.y*=-1;actomega.z*=-1;actomega.angle*=-1;}
  				//select the appropriate one
  				Stac_Out.println(" "+actomega+" "+actomega.angle/Math.PI*180.0);
  				double actomegaDiff=(actomega.angle-omegaRef)+6.0*Math.PI;
  				actomegaDiff=actomegaDiff-(double)((int)(actomegaDiff/period))*period;
  				if (actomegaDiff*2>period) actomegaDiff=period-actomegaDiff;
  				if (actomegaDiff<omegaDiff) {
  					omega=actomega;
  					omegaDiff=actomegaDiff;
  					omegapt=eqct;
  				}
  				actomegaDiff=(-actomega.angle-omegaRef)+6.0*Math.PI;
  				actomegaDiff=actomegaDiff-(double)((int)(actomegaDiff/period))*period;
  				if (actomegaDiff*2>period) actomegaDiff=period-actomegaDiff;
  				if (actomegaDiff<omegaDiff) {
  					omega=actomega;
  					omegaDiff=actomegaDiff;
  					omegapt=eqct;
  				}
  			}
  			
  			//convert the axes from denzo coordinate system to GNS
  			
  		}
  		//write out the result
  		jTFGCRphiX.setText(""+new PrintfFormat("%.5f").sprintf(phi.x));
  		jTFGCRphiX.setEditable(true);
  		jTFGCRphiY.setText(""+new PrintfFormat("%.5f").sprintf(phi.y));
  		jTFGCRphiY.setEditable(true);
  		jTFGCRphiZ.setText(""+new PrintfFormat("%.5f").sprintf(phi.z));
  		jTFGCRphiZ.setEditable(true);
  		jTFGCRphi.setText(""+new PrintfFormat("%.3f").sprintf(phi.angle*180/Math.PI));
  		jTFGCRphi.setEditable(false);
  		jTFGCRkappaX.setText(""+new PrintfFormat("%.5f").sprintf(kappa.x));
  		jTFGCRkappaX.setEditable(true);
  		jTFGCRkappaY.setText(""+new PrintfFormat("%.5f").sprintf(kappa.y));
  		jTFGCRkappaY.setEditable(true);
  		jTFGCRkappaZ.setText(""+new PrintfFormat("%.5f").sprintf(kappa.z));
  		jTFGCRkappaZ.setEditable(true);
  		jTFGCRkappa.setText(""+new PrintfFormat("%.3f").sprintf(kappa.angle*180/Math.PI));
  		jTFGCRkappa.setEditable(false);
  		jTFGCRomegaX.setText(""+new PrintfFormat("%.5f").sprintf(omega.x));
  		jTFGCRomegaX.setEditable(true);
  		jTFGCRomegaY.setText(""+new PrintfFormat("%.5f").sprintf(omega.y));
  		jTFGCRomegaY.setEditable(true);
  		jTFGCRomegaZ.setText(""+new PrintfFormat("%.5f").sprintf(omega.z));
  		jTFGCRomegaZ.setEditable(true);
  		jTFGCRomega.setText(""+new PrintfFormat("%.3f").sprintf(omega.angle*180/Math.PI));
  		jTFGCRomega.setEditable(false);
  		
  		//write out the comparizm to the old gonio settings
  		double axis[]=new double[4];
  		double axisDiff[]=new double[3];
  		double axisDiffT[]=new double[3];
  		//calc diff
  		omega.get(axis);
  		axisDiff[0]=okp[0].angle(new Vector3d(axis));
  		axisDiffT[0]=new Vector3d(0,0,1).angle(new Vector3d(axis));
  		kappa.get(axis);
  		axisDiff[1]=okp[1].angle(new Vector3d(axis));
  		axisDiffT[1]=okp[5].angle(new Vector3d(axis));
  		phi.get(axis);
  		axisDiff[2]=okp[2].angle(new Vector3d(axis));
  		axisDiffT[2]=okp[6].angle(new Vector3d(axis));
  		//print the results
  		Stac_Out.println("previous axes:\n omega:"+okp[0].toString()+
  				"\n kappa:"+okp[1].toString()+
				"\n phi:"+okp[2].toString());
  		Stac_Out.println("previous Translation axes:\n omega:"+new Vector3d(0,0,1).toString()+
  				"\n kappa:"+okp[5].toString()+
				"\n phi:"+okp[6].toString());
  		jTFGCRDomega.setText(""+new PrintfFormat("%.3f").sprintf(axisDiff[0]*180/Math.PI));
  		jTFGCRDomega.setEditable(false);
  		jTFGCRDkappa.setText(""+new PrintfFormat("%.3f").sprintf(axisDiff[1]*180/Math.PI));
  		jTFGCRDkappa.setEditable(false);
  		jTFGCRDphi.setText  (""+new PrintfFormat("%.3f").sprintf(axisDiff[2]*180/Math.PI));
  		jTFGCRDphi.setEditable(false);
  		
  		jTFGCRDomegaT.setText(""+new PrintfFormat("%.3f").sprintf(axisDiffT[0]*180/Math.PI));
  		jTFGCRDomegaT.setEditable(false);
  		jTFGCRDkappaT.setText(""+new PrintfFormat("%.3f").sprintf(axisDiffT[1]*180/Math.PI));
  		jTFGCRDkappaT.setEditable(false);
  		jTFGCRDphiT.setText  (""+new PrintfFormat("%.3f").sprintf(axisDiffT[2]*180/Math.PI));
  		jTFGCRDphiT.setEditable(false);
  		
		//printout
		Stac_Out.println("           X         Y         Z     Angle  Diff to current   Diff to Trans");
		Stac_Out.println("phi   "+phi.x+" "+phi.y+" "+phi.z+" "+phi.angle*180/Math.PI+" "+axisDiff[2]*180/Math.PI+" "+axisDiffT[2]*180/Math.PI);
		Stac_Out.println("kappa "+kappa.x+" "+kappa.y+" "+kappa.z+" "+kappa.angle*180/Math.PI+" "+axisDiff[1]*180/Math.PI+" "+axisDiffT[1]*180/Math.PI);
		Stac_Out.println("omega "+omega.x+" "+omega.y+" "+omega.z+" "+omega.angle*180/Math.PI+" "+axisDiff[0]*180/Math.PI+" "+axisDiffT[0]*180/Math.PI);
		
		
		
  	    jButtonGnsdefUpdate.setEnabled(true);
  		
  	}
  	catch(Exception e) {
  	  	//write out the result
  	    jTFGCRphiX.setEditable(false);
  	    jTFGCRphiY.setEditable(false);
  	    jTFGCRphiZ.setEditable(false);
  	    jTFGCRphi.setEditable(false);
  	    jTFGCRkappaX.setEditable(false);
  	    jTFGCRkappaY.setEditable(false);
  	    jTFGCRkappaZ.setEditable(false);
  	    jTFGCRkappa.setEditable(false);
  	    jTFGCRomegaX.setEditable(false);
  	    jTFGCRomegaY.setEditable(false);
  	    jTFGCRomegaZ.setEditable(false);
  	    jTFGCRomega.setEditable(false);
  	    
  	    //write out the comparizm to the old gonio settings
  	    jTFGCRDomega.setEditable(false);
  	    jTFGCRDkappa.setEditable(false);
  	    jTFGCRDphi.setEditable(false);
  	    
  	    jButtonGnsdefUpdate.setEnabled(false);
  		
  	}
  	
  	return;
  }
  
  
  void GonioTCalibration(){
    GonioTCalibration(false);
  }
  void GonioTCalibration(boolean copyRotCalib){
  	
  	//determining the directions of the rotaion axes
	Vector3d phiAxis=new Vector3d();
	Vector3d kappaAxis=new Vector3d();
	    Vector3d okp[]=new Vector3d[7];

    Vector phiData = jPanelTransPhi.getMeasuredData();
    Vector phiDataOrdered = utils.orderMeasuredTransDataPoints(phiData,false);
  	Vector kappaData = jPanelTransKappa.getMeasuredData();
    Vector kappaDataOrdered = utils.orderMeasuredTransDataPoints(kappaData,false);
	    	
    
    //scale calculation (also update, but backup before!!!)
    
	Vector data;
	AxisAngle4d a,b;
	data=phiDataOrdered;
    try {
    	//assumes 3 180-pairs
    	
    	a=((AxisAngle4d)(data.elementAt(0)));
    	b=((AxisAngle4d)(data.elementAt(3)));
    	Vector3d v1=new Vector3d(a.x-b.x,a.y-b.y,a.z-b.z);
    	a=((AxisAngle4d)(data.elementAt(1)));
    	b=((AxisAngle4d)(data.elementAt(4)));
    	Vector3d v2=new Vector3d(a.x-b.x,a.y-b.y,a.z-b.z);
    	a=((AxisAngle4d)(data.elementAt(2)));
    	b=((AxisAngle4d)(data.elementAt(5)));
    	Vector3d v3=new Vector3d(a.x-b.x,a.y-b.y,a.z-b.z);    	
    	double x=1,y=1,z=1;
    	if (utils.sqr(v1.y)-utils.sqr(v2.y)!=0) {
    		double sa=(utils.sqr(v2.x)-utils.sqr(v1.x))/(utils.sqr(v1.y)-utils.sqr(v2.y));
    		double sb=(utils.sqr(v2.z)-utils.sqr(v1.z))/(utils.sqr(v1.y)-utils.sqr(v2.y));
    	
    		if ((sb*utils.sqr(v3.y)+utils.sqr(v3.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z))!=0 &&
    				(utils.sqr(v1.x)+sa*utils.sqr(v1.y)-utils.sqr(v3.x)-sa*utils.sqr(v3.y))/
					(sb*utils.sqr(v3.y)+utils.sqr(v3.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z))>=0)
    			z=Math.sqrt((utils.sqr(v1.x)+sa*utils.sqr(v1.y)-utils.sqr(v3.x)-sa*utils.sqr(v3.y))/
    					(sb*utils.sqr(v3.y)+utils.sqr(v3.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z)));
    		else
    			z=1;
    		y=Math.sqrt(sa+utils.sqr(z)*sb);
    		x=1;
    	} else if (utils.sqr(v1.y)-utils.sqr(v3.y)!=0) {
    		double sa=(utils.sqr(v3.x)-utils.sqr(v1.x))/(utils.sqr(v3.y)-utils.sqr(v3.y));
    		double sb=(utils.sqr(v3.z)-utils.sqr(v1.z))/(utils.sqr(v3.y)-utils.sqr(v3.y));
    	
    		if ((sb*utils.sqr(v2.y)+utils.sqr(v2.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z))!=0 &&
    				(utils.sqr(v1.x)+sa*utils.sqr(v1.y)-utils.sqr(v2.x)-sa*utils.sqr(v2.y))/
					(sb*utils.sqr(v2.y)+utils.sqr(v2.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z))>=0)
    			z=Math.sqrt((utils.sqr(v1.x)+sa*utils.sqr(v1.y)-utils.sqr(v2.x)-sa*utils.sqr(v2.y))/
    					(sb*utils.sqr(v2.y)+utils.sqr(v2.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z)));
    		else
    			z=1;
    		y=Math.sqrt(sa+utils.sqr(z)*sb);
    		x=1;    		
    	} else {
    		y=1;
    		if (utils.sqr(v2.z)-utils.sqr(v1.z)!=0 && 
    			(utils.sqr(v1.x)+utils.sqr(v2.x))/(utils.sqr(v2.z)-utils.sqr(v1.z))>=0)
    			z=Math.sqrt((utils.sqr(v1.x)+utils.sqr(v2.x))/
    					(utils.sqr(v2.z)-utils.sqr(v1.z)));
    		else
    			z=1;   		
    	}
    	Stac_Out.println("Scale Factors calculated from phi:");
    	Stac_Out.println("   X = "+x);
    	Stac_Out.println("   y = "+y);
    	Stac_Out.println("   Z = "+z);    	
    } catch (Exception e) {
    	Stac_Out.println("Error During Scale Factors Calculation");
	}
    data=kappaDataOrdered;
    try {
    	//assumes 3 180-pairs
    	
    	a=((AxisAngle4d)(data.elementAt(0)));
    	b=((AxisAngle4d)(data.elementAt(3)));
    	Vector3d v1=new Vector3d(a.x-b.x,a.y-b.y,a.z-b.z);
    	a=((AxisAngle4d)(data.elementAt(1)));
    	b=((AxisAngle4d)(data.elementAt(4)));
    	Vector3d v2=new Vector3d(a.x-b.x,a.y-b.y,a.z-b.z);
    	a=((AxisAngle4d)(data.elementAt(2)));
    	b=((AxisAngle4d)(data.elementAt(5)));
    	Vector3d v3=new Vector3d(a.x-b.x,a.y-b.y,a.z-b.z);
    	double x=1,y=1,z=1;
    	if (utils.sqr(v1.y)-utils.sqr(v2.y)!=0) {
    		double sa=(utils.sqr(v2.x)-utils.sqr(v1.x))/(utils.sqr(v1.y)-utils.sqr(v2.y));
    		double sb=(utils.sqr(v2.z)-utils.sqr(v1.z))/(utils.sqr(v1.y)-utils.sqr(v2.y));
    	
    		if ((sb*utils.sqr(v3.y)+utils.sqr(v3.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z))!=0 &&
    				(utils.sqr(v1.x)+sa*utils.sqr(v1.y)-utils.sqr(v3.x)-sa*utils.sqr(v3.y))/
					(sb*utils.sqr(v3.y)+utils.sqr(v3.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z))>=0)
    			z=Math.sqrt((utils.sqr(v1.x)+sa*utils.sqr(v1.y)-utils.sqr(v3.x)-sa*utils.sqr(v3.y))/
    					(sb*utils.sqr(v3.y)+utils.sqr(v3.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z)));
    		else
    			z=1;
    		y=Math.sqrt(sa+utils.sqr(z)*sb);
    		x=1;
    	} else if (utils.sqr(v1.y)-utils.sqr(v3.y)!=0) {
    		double sa=(utils.sqr(v3.x)-utils.sqr(v1.x))/(utils.sqr(v3.y)-utils.sqr(v3.y));
    		double sb=(utils.sqr(v3.z)-utils.sqr(v1.z))/(utils.sqr(v3.y)-utils.sqr(v3.y));
    	
    		if ((sb*utils.sqr(v2.y)+utils.sqr(v2.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z))!=0 &&
    				(utils.sqr(v1.x)+sa*utils.sqr(v1.y)-utils.sqr(v2.x)-sa*utils.sqr(v2.y))/
					(sb*utils.sqr(v2.y)+utils.sqr(v2.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z))>=0)
    			z=Math.sqrt((utils.sqr(v1.x)+sa*utils.sqr(v1.y)-utils.sqr(v2.x)-sa*utils.sqr(v2.y))/
    					(sb*utils.sqr(v2.y)+utils.sqr(v2.z)-sb*utils.sqr(v1.y)-utils.sqr(v1.z)));
    		else
    			z=1;
    		y=Math.sqrt(sa+utils.sqr(z)*sb);
    		x=1;    		
    	} else {
    		y=1;
    		if (utils.sqr(v2.z)-utils.sqr(v1.z)!=0 && 
    			(utils.sqr(v1.x)+utils.sqr(v2.x))/(utils.sqr(v2.z)-utils.sqr(v1.z))>=0)
    			z=Math.sqrt((utils.sqr(v1.x)+utils.sqr(v2.x))/
    					(utils.sqr(v2.z)-utils.sqr(v1.z)));
    		else
    			z=1;   		
    	}
    	Stac_Out.println("Scale Factors calculated from kappa:");
    	Stac_Out.println("   X = "+x);
    	Stac_Out.println("   y = "+y);
    	Stac_Out.println("   Z = "+z);    	
    } catch (Exception e) {
    	Stac_Out.println("Error During Scale Factors Calculation");
	}
    
    
    //convert to STAC space
    for (int i =0 ; i<phiDataOrdered.size();i++) {
    	((AxisAngle4d)(phiDataOrdered.elementAt(i))).x=bcm.convertMotorPosition_Plugin2Stac("X", ((AxisAngle4d)(phiDataOrdered.elementAt(i))).x);
    	((AxisAngle4d)(phiDataOrdered.elementAt(i))).y=bcm.convertMotorPosition_Plugin2Stac("Y", ((AxisAngle4d)(phiDataOrdered.elementAt(i))).y);
    	((AxisAngle4d)(phiDataOrdered.elementAt(i))).z=bcm.convertMotorPosition_Plugin2Stac("Z", ((AxisAngle4d)(phiDataOrdered.elementAt(i))).z);
    }
    for (int i =0 ; i<kappaDataOrdered.size();i++) {
    	((AxisAngle4d)(kappaDataOrdered.elementAt(i))).x=bcm.convertMotorPosition_Plugin2Stac("X", ((AxisAngle4d)(kappaDataOrdered.elementAt(i))).x);
    	((AxisAngle4d)(kappaDataOrdered.elementAt(i))).y=bcm.convertMotorPosition_Plugin2Stac("Y", ((AxisAngle4d)(kappaDataOrdered.elementAt(i))).y);
    	((AxisAngle4d)(kappaDataOrdered.elementAt(i))).z=bcm.convertMotorPosition_Plugin2Stac("Z", ((AxisAngle4d)(kappaDataOrdered.elementAt(i))).z);
    }
    
  	try {
//  		Vector3d okp[]=new Vector3d[7];
  		//utils.read_gnsdef(gnsfile,okp);
  		bcm.getCalibration(okp);
  	
  		if (copyRotCalib) {
  			kappaAxis.set(okp[1]);
  			phiAxis.set(okp[2]);
  		} else {
  			if (phiDataOrdered.size()<3) {
  				Stac_Out.println("ERROR: Not enough measured points to calculate Phi axis");
  				Stac_Out.println("The previous calibration is used");
  				phiAxis.set(okp[6]);
  			} else {
  				phiAxis=utils.calculateRegressionPlane_SVD2(phiDataOrdered);
  				//debug
  				{
  					Vector3d phiAxisDeb=new Vector3d();
  					phiAxisDeb=utils.calculateRegressionPlane(phiDataOrdered);
  					Stac_Out.println("Diff between regressions (degrees): "+phiAxisDeb.angle(phiAxis)*180/Math.PI);
  				}
  				
  				
  			}
  			if (kappaDataOrdered.size()<3) {
  				Stac_Out.println("ERROR: Not enough measured points to calculate Kappa axis");
  				Stac_Out.println("The previous calibration is used");
  				kappaAxis.set(okp[5]);
  			} else {
  				kappaAxis=utils.calculateRegressionPlane_SVD2(kappaDataOrdered);
  				//debug
  				{
  					Vector3d phiAxisDeb=new Vector3d();
  					phiAxisDeb=utils.calculateRegressionPlane(kappaDataOrdered);
  					Stac_Out.println("Diff between regressions: "+phiAxisDeb.angle(kappaAxis)*180/Math.PI);
  				}
  			}
  		}
  		
  		//updating the fields
  		jTFGTCRphiXR.setText(""+new PrintfFormat("%.5f").sprintf(phiAxis.x));
  		jTFGTCRphiXR.setEditable(true);
  		jTFGTCRphiYR.setText(""+new PrintfFormat("%.5f").sprintf(phiAxis.y));
  		jTFGTCRphiYR.setEditable(true);
  		jTFGTCRphiZR.setText(""+new PrintfFormat("%.5f").sprintf(phiAxis.z));
  		jTFGTCRphiZR.setEditable(true);
  		jTFGTCRkappaXR.setText(""+new PrintfFormat("%.5f").sprintf(kappaAxis.x));
  		jTFGTCRkappaXR.setEditable(true);
  		jTFGTCRkappaYR.setText(""+new PrintfFormat("%.5f").sprintf(kappaAxis.y));
  		jTFGTCRkappaYR.setEditable(true);
  		jTFGTCRkappaZR.setText(""+new PrintfFormat("%.5f").sprintf(kappaAxis.z));
  		jTFGTCRkappaZR.setEditable(true);
  		

  		//write out the comparizm to the old gonio settings
  		double axis[]=new double[4];
  		double axisDiff[]=new double[3];
  		double axisDiffT[]=new double[3];
  		//calc diff
  		//KAPPA
  		kappaAxis.get(axis);
  		//compare to current
  		axisDiff[1]=okp[5].angle(new Vector3d(axis));
  		//compare to RotCalib
  		axisDiffT[1]=okp[1].angle(new Vector3d(axis));
  		//PHI
  		phiAxis.get(axis);
  		//compare to current
  		axisDiff[2]=okp[6].angle(new Vector3d(axis));
  		//compare to RotCalib
  		axisDiffT[2]=okp[2].angle(new Vector3d(axis));
  		//print the results
  		Stac_Out.println("previous axes:\n omega:"+okp[0].toString()+
  				"\n kappa:"+okp[1].toString()+
				"\n phi:"+okp[2].toString());
  		Stac_Out.println("previous Translation axes:\n omega:"+new Vector3d(0,0,1).toString()+
  				"\n kappa:"+okp[5].toString()+
				"\n phi:"+okp[6].toString());
  		jTFGTCRDkappaR.setText(""+new PrintfFormat("%.3f").sprintf(axisDiff[1]*180/Math.PI));
  		jTFGTCRDkappaR.setEditable(false);
  		jTFGTCRDphiR.setText  (""+new PrintfFormat("%.3f").sprintf(axisDiff[2]*180/Math.PI));
  		jTFGTCRDphiR.setEditable(false);
  		
  		jTFGTCRDkappaTR.setText(""+new PrintfFormat("%.3f").sprintf(axisDiffT[1]*180/Math.PI));
  		jTFGTCRDkappaTR.setEditable(false);
  		jTFGTCRDphiTR.setText  (""+new PrintfFormat("%.3f").sprintf(axisDiffT[2]*180/Math.PI));
  		jTFGTCRDphiTR.setEditable(false);
  		
		//printout
		Stac_Out.println("           X         Y         Z     Diff to current   Diff to Trans");
		Stac_Out.println("phi   "+phiAxis.x+" "+phiAxis.y+" "+phiAxis.z+" "+axisDiff[2]*180/Math.PI+" "+axisDiffT[2]*180/Math.PI);
		Stac_Out.println("kappa "+kappaAxis.x+" "+kappaAxis.y+" "+kappaAxis.z+" "+axisDiff[1]*180/Math.PI+" "+axisDiffT[1]*180/Math.PI);
		
		
  		
  		
  	} catch (Exception e) {
  		
  	}
  	
  	//oscillation start, as an omega rotation is assumed as rotation around (0,0,1)!
  	//
  	// Translation initialisation, thta must be performed in advance:
  	//    The pin edge must be centered (with 0 angles of phi, and kappa), and at this position all 
  	//    the motors should be reset.
  	//    It must be done for later convinience, since we assume this centered position as the Origin.
  	//    ESRF-id14eh4
  	//    sampx  horizontal movement of XYtable on the omega axis (omega=0!) (parallel to the beam)
  	//    sampy  vertical   movement of XYtable on the omega exis (omega=0!)
  	//    phiz   vertical   movement of the omega axis
  	//    phiy   horizontal movement of the omega axis (along the axis; orthogonal to the beam)
  	//
  	// Calibration step: rotate around the given axis with a certain angle.
  	//    We ask for the translation vector from the new location of the previously centered point
  	//    to its previous location. (Since the previous location was the Origin, the new location
  	//    is the inverse of the place vector of the new location.)
  	//       It can be determined by recentering the point. The motor movments (generally) directly
  	//       tell the translation vector necessary to move a certain point to the Origin.
  	//       ESRF-id14eh4
  	//       with correct phi_setup after phi_init by applying "mv phi 9;set phi 0; set_dial phi 0"
  	//       the XY-table becomes (more or less) adjusted to the horzontal and vertical planes
  	//       phiz must be 0 (because we center)
  	//       (-sampx;sampy;phiy) gives the translation vector (X;Y;Z)
  	//       We assume the same coordinate system that is used in gonset:
  	//       X: along the beam
  	//       Y: vertical upwards
  	//       Z: horizontally to inner wall (in Europe)
  	//
  	// Point served:
  	//    we serve a point on the rotation axis ( so we give its place vector)
  	//    One can try to bring this point to the Origin to see that it is really on the rotation axis.
  	//    ESRF-id14eh4
  	//    if the place vector is (X;Y;Z), hence the translation we should use is (-X;-Y;-Z),
  	//    so we should apply (sampx=X;sampy=-Y;phiy=-Z) motor settings.
  	

  	
  	
  	
  	try {
  		Vector3d phi= new Vector3d();
  		Vector3d kappa= new Vector3d();

  		double kappaRecE=utils.calculatePointOnRotationAxis(kappaDataOrdered,kappaAxis,kappa);
  		double phiRecE=utils.calculatePointOnRotationAxis(phiDataOrdered,phiAxis,phi);
  		
  		//write out the result
  		jTFGTCRphiX.setText(""+new PrintfFormat("%.5f").sprintf(phi.x));
  		jTFGTCRphiX.setEditable(true);
  		jTFGTCRphiY.setText(""+new PrintfFormat("%.5f").sprintf(phi.y));
  		jTFGTCRphiY.setEditable(true);
  		jTFGTCRphiZ.setText(""+new PrintfFormat("%.5f").sprintf(phi.z));
  		jTFGTCRphiZ.setEditable(true);
  		jTFGTCRkappaX.setText(""+new PrintfFormat("%.5f").sprintf(kappa.x));
  		jTFGTCRkappaX.setEditable(true);
  		jTFGTCRkappaY.setText(""+new PrintfFormat("%.5f").sprintf(kappa.y));
  		jTFGTCRkappaY.setEditable(true);
  		jTFGTCRkappaZ.setText(""+new PrintfFormat("%.5f").sprintf(kappa.z));
  		jTFGTCRkappaZ.setEditable(true);
  		
  		jTFGTCRphiRecE.setText(""+new PrintfFormat("%.5f").sprintf(phiRecE));
  		jTFGTCRphiRecE.setEditable(false);
  		jTFGTCRkappaRecE.setText(""+new PrintfFormat("%.5f").sprintf(kappaRecE));
  		jTFGTCRkappaRecE.setEditable(false);
  		
  		//write out the comparizm to the old gonio settings
  		//calc diff
  		Vector3d axis= new Vector3d();
  		double axisDiff[]=new double[3];
  		
  		axis.set(kappa);
  		axis.sub(okp[3]);
  		if(axis.length()!=0.) {
  			axisDiff[1]=Math.sin(okp[1].angle(axis))*axis.length();
  		} else {
  			axisDiff[1]=0.;
  		}
  		axis.set(phi);
  		axis.sub(okp[4]);
  		if(axis.length()!=0.) {
  			axisDiff[2]=Math.sin(okp[2].angle(axis))*axis.length();
  		} else {
  			axisDiff[2]=0.;
  		}
  		//print the results
  		jTFGTCRkappa.setText(""+new PrintfFormat("%.3f").sprintf(axisDiff[1]));
  		jTFGTCRkappa.setEditable(false);
  		jTFGTCRphi.setText  (""+new PrintfFormat("%.3f").sprintf(axisDiff[2]));
  		jTFGTCRphi.setEditable(false);
  		
  	    jButtonGnsdefTUpdate.setEnabled(true);
  		
  	}
  	catch(Exception e) {
  	  	//write out the result
  	    jTFGTCRphiX.setEditable(false);
  	    jTFGTCRphiY.setEditable(false);
  	    jTFGTCRphiZ.setEditable(false);
  	    jTFGTCRkappaX.setEditable(false);
  	    jTFGTCRkappaY.setEditable(false);
  	    jTFGTCRkappaZ.setEditable(false);
  	    
  	    //write out the comparizm to the old gonio settings
  	    jTFGTCRkappa.setEditable(false);
  	    jTFGTCRphi.setEditable(false);
  	    
  	    jButtonGnsdefTUpdate.setEnabled(false);
  		
  	}
  	
  	return;
  }
  

  
  void ReadCalculatedAlignments(String myfile,int reqId) {
	  Vector res=utils.ReadCalculatedAlignmentFile(session, myfile, reqId);
	  for (int i=0;i<res.size();i++)
           datumPanel.addTableRow((Vector)res.elementAt(i));
  }
  
//  public Point3d CalculateCalibratedTranslation(double angles[]) {
//      //get info for trans calc
//      double actualKappa=0.,actualPhi=0.;
//      try {
//      	actualKappa = new Double(jTFKappa.getText()).doubleValue();
//      } catch (Exception e) {
//      }
//      try {
//      	actualPhi = new Double(jTFPhi.getText()).doubleValue();
//      } catch (Exception e) {
//      }
//      Point3d actualTrans = new Point3d();
//      try {
//      	actualTrans.x = new Double(jTFTransX.getText()).doubleValue();
//      } catch (Exception e) {
//      }
//      try {
//      	actualTrans.y = new Double(jTFTransY.getText()).doubleValue();
//      } catch (Exception e) {
//      }
//      try {
//      	actualTrans.z = new Double(jTFTransZ.getText()).doubleValue();
//      } catch (Exception e) {
//      }
//      double omega = angles[0]; 
//      double kappa = angles[1];
//      double phi   = angles[2];
//      //Stac_Out.println("omega: "+omega+"kappa: "+kappa+"phi  : "+phi);
//      Point3d trans;
//      trans= utils.CalculateTranslation(actualTrans,actualKappa,kappa,actualPhi,phi,bcm);
//      return trans;
//  }

  void writeFinalStrategies() {
    try {
      String fname="strategies.txt";
      Stac_Out.print("Writing out the result file: "+fname+" ...");
      FileWriter StrategyFile = new FileWriter(fname);
      StrategyFile.write("STrategy for Aligned Crystals (by Sandor Brockhauser)\n");
      StrategyFile.write("=====================================================\n");
      PrintfFormat numForm = new PrintfFormat("%13.6e");
      PrintfFormat txtForm = new PrintfFormat("%13s");
      
      Vector str=strategyWidget.getStrategyRequest();

      Vector nam=((ParamTable)(str.elementAt(0))).pnames;

      for (int j=0;j<nam.size();j++) {
        StrategyFile.write(" "+txtForm.sprintf(nam.elementAt(j)));
      }
      StrategyFile.write("\n");

      
      int numOfVecs = str.size();
      for (int i=0;i<numOfVecs;i++) {
    	  ParamTable val=((ParamTable)(str.elementAt(i)));
        for (int j=0;j<nam.size();j++) {
        	String prtval=new Double(val.getFirstDoubleValue(nam.elementAt(j))).toString();
          StrategyFile.write(" "+txtForm.sprintf(prtval));
        }
        StrategyFile.write("\n");
      }
      StrategyFile.close();
      Stac_Out.println(" finished");
    }
    catch (IOException ex) {
    }
  }

  void LoadCrystalData_actionPerformed(ActionEvent e) {
	    //input conversion
	    ConvertInputFormat();
	    //load info
	    ParamTable pTable = new ParamTable();
	    utils.read_denzo_x(session.getWorkDir()+"name0.x", pTable);
  	    double[] cell = new double[6];
  	    double[] tcell = new double[6];
  	    Matrix3d B = new Matrix3d();
  	    Matrix3d Bm1t = new Matrix3d();
  	    Matrix3d U = new Matrix3d();
  	    Vector3d phixyz = new Vector3d();
  		Matrix3d osc = new Matrix3d();
		Matrix3d OMU = new Matrix3d(pTable.getDoubleVector("Amat"));
		Matrix3d OMA = new Matrix3d(pTable.getDoubleVector("Umat"));
		Matrix3d OMAi= new Matrix3d(OMA); OMAi.invert();
		Matrix3d OMUB = new Matrix3d(OMA);
		utils.denzo2mosflm_matrices_by_gonset(OMU,cell,tcell,B,Bm1t,U,OMUB,phixyz);
	    //display params - cell
		PrintfFormat cellpf= new PrintfFormat("a: %.2f  b: %.2f  c: %.2f  -  A: %.0f  B: %.0f  G: %.0f");
		String cstr = new String(cellpf.sprintf(new Object[] { 
				new Double(cell[0]),
				new Double(cell[1]),
				new Double(cell[2]),
				new Double(cell[3]),
				new Double(cell[4]),
				new Double(cell[5])}));
		jTFCPar.setText(cstr);
	    //display params - space group
		cellpf= new PrintfFormat("%s");
		cstr = new String(cellpf.sprintf(new Object[] { 
				pTable.getFirstStringValue("spg")}));
		jTFCPar3.setText(cstr);
		jPanelReorient.setSPG(cstr);
	    //display params - resolution
		try {
			cellpf= new PrintfFormat("%.2f");
			String [] strs=pTable.getFirstStringValue("resolution_limits").split("\\s+");
			int off = 0;
			if (strs[0].length()==0)
				off++;
			cstr=cellpf.sprintf(new Object[] {new Double(strs[off+1])});
			jTFCPar2.setText(cstr);
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	  
  }

  
  
  void AllignmentCalculation_actionPerformed(ActionEvent e) {
    //input conversion
    ConvertInputFormat();
    Vector orientations = jPanelReorient.getOrientationRequest();
    
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

    
//    String alignFile=alignment.CalculateAlignment("./",datum,actualTrans,orientations,bcm,utils);
//    //calling gonset
//	//only case of not resetting omega,
//    // would be when indexing is performed assuming Oscillation start=0
//    //CallExternalGonset(true,orientations);
//    //fill the table from the file STAC_align.vec
//    ReadCalculatedAlignments(alignFile);
    
	Stac_AlignmentReqMsg req = new Stac_AlignmentReqMsg(session,respHandler,datum,actualTrans,orientations,bcm,utils); 
	alignment.handle(req);
    
    
  }
//
//  // 0 - mosflm or error
//  // 1 - XDS
//  // 2 - denzo
//  public String OMFileType(String filename) {
//    File file=new File(filename);
//    if (file.getAbsolutePath().endsWith(".x")) {
//    	return "denzo";
//      } else  if (file.getAbsolutePath().endsWith(".mat")) {
//    	return "mosflm";
//      } else  if (file.getAbsolutePath().endsWith(".LP")) {
//    	return "xds";
//      }
//  	return "mosflm";
//  }
  File activeFileSelectionDir=null;
  
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
        Stac_Out.printTimeln("USER ACTION: Button pressed (Fileselect: "+file.getAbsolutePath()+" - "+((JButton)e.getSource()).getText()+")");			
        activeFileSelectionDir=file.getParentFile();
	}
    //change the appropriate field
    if (e.getSource() == jBMosflmSettingsFile) {
      jTFMosflmSettingsFile.setText(file.getAbsolutePath());
      jRadioButtonMosflm.doClick();
    } else if (e.getSource() == jButton3) {
      jTextFieldDefFile.setText(file.getAbsolutePath());
      if (file.getAbsolutePath().endsWith(".x")) {
    	jRadioButtonDenzo.doClick();
      } else  if (file.getAbsolutePath().endsWith(".par")) {
    	jRadioButtonMosflm.doClick();
      } else  if (file.getAbsolutePath().endsWith(".LP")) {
    	jRadioButtonXDS.doClick();
      }
//    } else if (e.getSource() == jButton4) {
//        jTextFieldDefFile1.setText(file.getAbsolutePath());
    } else if (e.getSource() == jButtonDesc) {
        jTextFieldDescDefFile.setText(file.getAbsolutePath());
//    } else if (e.getSource() == jButtonRGF) {
//        jTextFieldRGF.setText(file.getAbsolutePath());
    } else if (e.getSource() == jBMat0Sel) {
        jTFMat0File.setText(file.getAbsolutePath());
        if (file.getAbsolutePath().endsWith(".x")) {
        	jRBMat0Denzo.doClick();
        } else  if (file.getAbsolutePath().endsWith(".par")) {
        	jRBMat0Mosflm.doClick();
        } else  if (file.getAbsolutePath().endsWith(".LP")) {
        	jRBMat0XDS.doClick();
        }
    } else if (e.getSource() == jBMat1Sel) {
    	jTFMat1File.setText(file.getAbsolutePath());
        if (file.getAbsolutePath().endsWith(".x")) {
        	jRBMat1Denzo.doClick();
        } else  if (file.getAbsolutePath().endsWith(".par")) {
        	jRBMat1Mosflm.doClick();
        } else  if (file.getAbsolutePath().endsWith(".LP")) {
        	jRBMat1XDS.doClick();
        }
    } else if (e.getSource() == jBMat2Sel) {
    	jTFMat2File.setText(file.getAbsolutePath());
        if (file.getAbsolutePath().endsWith(".x")) {
        	jRBMat2Denzo.doClick();
        } else  if (file.getAbsolutePath().endsWith(".par")) {
        	jRBMat2Mosflm.doClick();
        } else  if (file.getAbsolutePath().endsWith(".LP")) {
        	jRBMat2XDS.doClick();
        }
    } else if (e.getSource() == jBMat3Sel) {
    	jTFMat3File.setText(file.getAbsolutePath());
        if (file.getAbsolutePath().endsWith(".x")) {
        	jRBMat3Denzo.doClick();
        } else  if (file.getAbsolutePath().endsWith(".par")) {
        	jRBMat3Mosflm.doClick();
        } else  if (file.getAbsolutePath().endsWith(".LP")) {
        	jRBMat3XDS.doClick();
        }
    } 
  }

  /** Returns an ImageIcon, or null if the path was invalid. */
  protected static ImageIcon createImageIcon(String path) {
      java.net.URL imgURL = StacMainWin.class.getResource(path);
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


//	void GonioTGetTransK() {
//		Point3d trn= new Point3d();
//		Point3d dat= new Point3d();
//		bcm.getCurrentDatumTrans(dat,trn);
//		if (Math.abs(utils.angleDegreeDiff(dat.y,new Double(jTFGTCInpK.getText()).doubleValue()))<=1e-1 && Math.abs(utils.angleDegreeDiff(0,dat.z))<=1e-1) {
//			jTFGTCInpKX.setText(""+new PrintfFormat("%.4f").sprintf(trn.x));
//			jTFGTCInpKY.setText(""+new PrintfFormat("%.4f").sprintf(trn.y));
//			jTFGTCInpKZ.setText(""+new PrintfFormat("%.4f").sprintf(trn.z));
//		} else {
//			//error
//		}
//	}
//    void GonioTGetTransP() {
//		Point3d trn= new Point3d();
//		Point3d dat= new Point3d();
//		bcm.getCurrentDatumTrans(dat,trn);
//		if (Math.abs(utils.angleDegreeDiff(dat.z,new Double(jTFGTCInpP.getText()).doubleValue()))<=1e-1 && Math.abs(utils.angleDegreeDiff(0,dat.y))<=1e-1) {
//			jTFGTCInpPX.setText(""+new PrintfFormat("%.4f").sprintf(trn.x));
//			jTFGTCInpPY.setText(""+new PrintfFormat("%.4f").sprintf(trn.y));
//			jTFGTCInpPZ.setText(""+new PrintfFormat("%.4f").sprintf(trn.z));
//		} else {
//			//error
//		}
//    }
//    void GonioTMvDatumK() {
//    	double desiredAng=new Double(jTFGTCInpK.getText()).doubleValue();
//    	bcm.moveToDatum(0,desiredAng,0);
//    	double ang=bcm.getMotorPosition("Kappa");
//    	if (Math.abs((ang-desiredAng))>1e-1) {
//    		//error
//    	} else {
//			jTFGTCInpK.setText(""+new PrintfFormat("%.4f").sprintf(ang));    		
//    	}
//    }
//    void GonioTMvDatumP() {
//    	double desiredAng=new Double(jTFGTCInpP.getText()).doubleValue();
//    	bcm.moveToDatum(0,0,desiredAng);
//    	double ang=bcm.getMotorPosition("Phi");
//    	if (Math.abs((ang-desiredAng))>1e-1) {
//    		//error
//    	} else {
//			jTFGTCInpP.setText(""+new PrintfFormat("%.4f").sprintf(ang));    		
//    	}
//    }
    void GonioTMvDatumZero() {
    	bcm.moveToDatum(0,0,0);
    }
    void GonioTResetMotors(){
    	backupConfigFile();
    	bcm.resetTrans();
    }
    void GonioTsetRefPt(){
    	//phi
    	{
    		//set angle to 0
    		jPanelTransPhi.jTFGTCInpP.setText("0.0");			
    		//get value			
    		ActionEvent e=new ActionEvent(jPanelTransPhi.jButtonGTC_getP,0,"Simulated action");
    		jPanelTransPhi.Button_actionAdapter(e);		
    	}		
    	//kappa
    	{
    		//set angle to 0
    		jPanelTransKappa.jTFGTCInpP.setText("0.0");			
    		//get value			
    		ActionEvent e=new ActionEvent(jPanelTransKappa.jButtonGTC_getP,0,"Simulated action");
    		jPanelTransKappa.Button_actionAdapter(e);
    	}
    }
    void GonioMotorInit(){
    	if (false) {
    		//dancing MiniKappa
    		Thread wait=new Thread();
    		wait.start();
    		for (int i=0;i<5*60;i++) {
    			double o=Math.random();
    			double k=Math.random();
    			double p=Math.random();
    			
    			if (p<0.3)
    				bcm.moveMotor("Omega",o*80-40);
    			if (p<0.7)
    				bcm.moveMotor("Kappa",k*90);
    			bcm.moveMotor("Phi",p*360);
    			try {
					wait.sleep(200);
				} catch (InterruptedException e) {
				}
    		}
    		
    		wait.stop();
    		
    	
    		return;    	
    	}
    	
    	bcm.initMotors();
    	//bcm.resetDatum();
    }
    void InputGetCurrentDatumTrans(){
		Point3d trn= new Point3d();
		Point3d dat= new Point3d();
		bcm.getCurrentDatumTrans(dat,trn);
		jTFOmega.setText(""+new PrintfFormat("%.4f").sprintf(dat.x));
		jTFKappa.setText(""+new PrintfFormat("%.4f").sprintf(dat.y));
		jTFPhi.setText(""+new PrintfFormat("%.4f").sprintf(dat.z));
		jTFTransX.setText(""+new PrintfFormat("%.4f").sprintf(trn.x));
		jTFTransY.setText(""+new PrintfFormat("%.4f").sprintf(trn.y));
		jTFTransZ.setText(""+new PrintfFormat("%.4f").sprintf(trn.z));
    }
//    void EvalGetCurrentDatumTrans(){
//		Point3d trn= new Point3d();
//		Point3d dat= new Point3d();
//    	double o,k,p,x,y,z;
//    	//if something is selected
//        int[] vecIDs = jTable1.getSelectedRows();
//		//get the current positions
//		bcm.getCurrentDatumTrans(dat,trn);
//		o=dat.x;
//		k=dat.y;
//		p=dat.z;
//		x=trn.x;
//		y=trn.y;
//		z=trn.z;
//		boolean recalcTrans=false;
//        if(vecIDs.length>0)
//    	{
//            AlignTableModel amodel = (AlignTableModel) jTable1.getModel();
//        	o=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("omega"))).doubleValue();
//			k=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("kappa"))).doubleValue();
//			p=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("phi"))).doubleValue();
//			String tr = (String)jTable1.getValueAt(vecIDs[0],amodel.getColId("Trans"));
//			tr=tr.substring(1,tr.length()-1);
//			String tmp[]=tr.split(";");
//			try {
//				x = new Double(tmp[0]).doubleValue();
//				y = new Double(tmp[1]).doubleValue();
//				z = new Double(tmp[2]).doubleValue();
//			} catch (Exception e) {
//				recalcTrans=true;
//			}
//    	}
//        if (recalcTrans){
//        	double angles[]={o,k,p};
//        	Point3d pts= new Point3d();
//        	pts=CalculateCalibratedTranslation(angles);
//        	x=pts.x;y=pts.y;z=pts.z;
//        }
//        //move to the desired datum/trans
//    	bcm.moveToDatumTransSync(o,k,p,x,y,z);
//    	//update the field
//		bcm.getCurrentDatumTrans(dat,trn);
//		jTFEvalO.setText(""+new PrintfFormat("%.4f").sprintf(dat.x));
//		jTFEvalK.setText(""+new PrintfFormat("%.4f").sprintf(dat.y));
//		jTFEvalP.setText(""+new PrintfFormat("%.4f").sprintf(dat.z));
//		jTFEvalX.setText(""+new PrintfFormat("%.4f").sprintf(trn.x));
//		jTFEvalY.setText(""+new PrintfFormat("%.4f").sprintf(trn.y));
//		jTFEvalZ.setText(""+new PrintfFormat("%.4f").sprintf(trn.z));
//		
//		Stac_Out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		Stac_Out.println("!!!  Spec user oscillation start: !!!");//TODO
//		Stac_Out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//    	
//    }
//    void EvalGetCurrentDatumTrans2(){
//		Point3d trn= new Point3d();
//		Point3d dat= new Point3d();
//    	double o,k,p,x,y,z;
//		//get the current positions
//		bcm.getCurrentDatumTrans(dat,trn);
//		o=dat.x;
//		k=dat.y;
//		p=dat.z;
//		x=trn.x;
//		y=trn.y;
//		z=trn.z;
//		boolean recalcTrans=false;
//		{
//    		//otherwise set only specified values, so
//    		//modify them if specified
//			try {
//				o = new Double(jTFEvalO2.getText()).doubleValue();
//			} catch (Exception e) {
//			}
//			try {
//				k = new Double(jTFEvalK2.getText()).doubleValue();
//				recalcTrans=true;
//			} catch (Exception e) {
//			}
//			try {
//				p = new Double(jTFEvalP2.getText()).doubleValue();
//				recalcTrans=true;
//			} catch (Exception e) {
//			}
//			try {
//				x = new Double(jTFEvalX2.getText()).doubleValue();
//			} catch (Exception e) {
//			}
//			try {
//				y = new Double(jTFEvalY2.getText()).doubleValue();
//			} catch (Exception e) {
//			}
//			try {
//				z = new Double(jTFEvalZ2.getText()).doubleValue();
//			} catch (Exception e) {
//			}
//    	}
//        if (recalcTrans){
//        	double angles[]={o,k,p};
//        	Point3d pts= new Point3d();
//        	pts=CalculateCalibratedTranslation(angles);
//        	x=pts.x;y=pts.y;z=pts.z;
//        }
//        //move to the desired datum/trans
//    	bcm.moveToDatumTransSync(o,k,p,x,y,z);
//    	//update the field
//		bcm.getCurrentDatumTrans(dat,trn);
//		jTFEvalO2.setText(""+new PrintfFormat("%.4f").sprintf(dat.x));
//		jTFEvalK2.setText(""+new PrintfFormat("%.4f").sprintf(dat.y));
//		jTFEvalP2.setText(""+new PrintfFormat("%.4f").sprintf(dat.z));
//		jTFEvalX2.setText(""+new PrintfFormat("%.4f").sprintf(trn.x));
//		jTFEvalY2.setText(""+new PrintfFormat("%.4f").sprintf(trn.y));
//		jTFEvalZ2.setText(""+new PrintfFormat("%.4f").sprintf(trn.z));
//    	
//    }

	public void LoadOMDescriptor(String file){
		ParamTable omdesc = utils.LoadOMDescriptorTable(file,bcm);
		try {
			jTextFieldDefFile.setText(omdesc.getFirstStringValue("OMFILENAME"));
			String value = omdesc.getFirstStringValue("OMTYPE");
			switch (value.toCharArray()[0]) {
			case 'm':
				jRadioButtonMosflm.doClick();
				break;
			case 'x':
				jRadioButtonXDS.doClick();
				break;
			case 'd':
				jRadioButtonDenzo.doClick();
				break;
			}
			jTFOmega.setText(omdesc.getFirstStringValue("omega"));
			jTFKappa.setText(omdesc.getFirstStringValue("kappa"));
			jTFPhi.setText(omdesc.getFirstStringValue("phi"));
			jTFTransX.setText(omdesc.getFirstStringValue("X"));
			jTFTransY.setText(omdesc.getFirstStringValue("Y"));
			jTFTransZ.setText(omdesc.getFirstStringValue("Z"));
		}
		catch (Exception e) {
		}
		return;
	}
//
//	public ParamTable LoadOMDescriptorTable(String file){
//		ParamTable omdesc = new ParamTable();
//		try {
//	      	String corr;
//	      	StacUtil utils=new StacUtil();
//	      	boolean omtypeIsSpecified=false;
//	        //read spec.dat
//	      	corr = utils.opReadCl(file);
//	        String tmp1[] = corr.split("\n");
//	        for (int l=0;l<tmp1.length;l++) {
//	        	if (tmp1[l].startsWith("#",0))
//	        		continue;
//	        	String tmp2[] = tmp1[l].split("\\s+");
//	        	if(tmp2.length==0)
//	        		continue;
//	        	int offset=0;
//	        	if (tmp2[0].length()==0)
//	        		offset++;
//	        	if(offset>=tmp2.length)
//	        		continue;
//				String name = tmp2[0+offset];
//				if (name.equalsIgnoreCase("OMFILENAME")) {
//					String value =tmp2[1+offset];
//					omdesc.setSingleValue("OMFILENAME",value);
//					if(!omtypeIsSpecified){
//						omdesc.setSingleValue("OMTYPE",OMFileType(value).toLowerCase());
//					}
//				} else if (name.equalsIgnoreCase("OMTYPE")) {
//					String value =tmp2[1+offset];
//					omtypeIsSpecified=true;
//						omdesc.setSingleValue("OMTYPE",value.toLowerCase());
//				} else {
//					String value =tmp2[1+offset];
//					if (name.equalsIgnoreCase("omega")){
//						omdesc.setSingleValue("omega",new Double(bcm.activePlugin.convertMotorPosition_Plugin2Stac("Omega",new Double(value).doubleValue())).toString());
//					} else if (name.equalsIgnoreCase("kappa")){
//						omdesc.setSingleValue("kappa",new Double(bcm.activePlugin.convertMotorPosition_Plugin2Stac("Kappa",new Double(value).doubleValue())).toString());
//					} else if (name.equalsIgnoreCase("phi")){
//						omdesc.setSingleValue("phi",new Double(bcm.activePlugin.convertMotorPosition_Plugin2Stac("Phi",new Double(value).doubleValue())).toString());
//					} else if (name.equalsIgnoreCase("X")){
//						omdesc.setSingleValue("X",new Double(bcm.activePlugin.convertMotorPosition_Plugin2Stac("X",new Double(value).doubleValue())).toString());
//					} else if (name.equalsIgnoreCase("Y")){
//						omdesc.setSingleValue("Y",new Double(bcm.activePlugin.convertMotorPosition_Plugin2Stac("Y",new Double(value).doubleValue())).toString());
//					} else if (name.equalsIgnoreCase("Z")){
//						omdesc.setSingleValue("Z",new Double(bcm.activePlugin.convertMotorPosition_Plugin2Stac("Z",new Double(value).doubleValue())).toString());
//					}
//				}
//	        }
//		} catch (Exception e) {
//		}
//	      	
//		return omdesc;
//	}
	
	public void SaveOMDescriptor(String file) {
	      FileWriter OMFile;
		try {
			OMFile = new FileWriter(file);
			OMFile.write("#STAC OM descriptor (by Sandor Brockhauser)\n");
			OMFile.write("#==========================================\n");
			PrintfFormat numForm = new PrintfFormat("%13.6e");
			PrintfFormat txtForm = new PrintfFormat("%13s");
			
			OMFile.write("OMFILENAME "+jTextFieldDefFile.getText()+"\n");
			OMFile.write("OMTYPE     ");
			if (jRadioButtonMosflm.isSelected()) {
				OMFile.write("mosflm\n");
			} else if (jRadioButtonXDS.isSelected()) {
				OMFile.write("xds\n");
			} else {
				OMFile.write("denzo\n");
			}
			
			OMFile.write("STAC_Omega      "+jTFOmega.getText()+"\n");
			OMFile.write("STAC_Kappa      "+jTFKappa.getText()+"\n");
			OMFile.write("STAC_Phi        "+jTFPhi.getText()+"\n");
			OMFile.write("STAC_X          "+jTFTransX.getText()+"\n");
			OMFile.write("STAC_Y          "+jTFTransY.getText()+"\n");
			OMFile.write("STAC_Z          "+jTFTransZ.getText()+"\n");
			
			OMFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	public void Button_actionAdapter(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
	        Stac_Out.println("USER ACTION: Button pressed (STAC Orinetation Panel: "+((JButton)e.getSource()).getText()+")");			
		}
		if (e.getSource()==this.jPanelReorient.jButtonOptAlign) {
	        //panel 1
	    	ConvertInputFormat();
	    	Stac_OptStrategyOrientReqMsg req = new Stac_OptStrategyOrientReqMsg(session,respHandler); 
	    	strategy.handle(req);	    	
	    } else if (e.getSource()==this.jPanelReorient.jButtonSSS) {
	        //panel 1
	    	ConvertInputFormat();
	    	//TODO
	    	//calculate the smart spot separation
	    	Vector3d v= new Vector3d(0,0,1);

	    	//get best vector
	    	String kappa =this.jTFKappa.getText();
	    	if (kappa==null || kappa.length()==0)
	    		kappa="0";
	    	String phi =this.jTFPhi.getText();
	    	if (phi==null || phi.length()==0)
	    		phi="0";
	    	
	    	Vector OM = utils.getHKLforOrthogonalBeamAlignment(kappa,phi,session.getWorkDir()+"name0.x",bcm);

	    	if (OM!=null && OM.size()>=1) {
	    		
	    		int rowct=this.jPanelReorient.jTableReorient.getRowCount();
	    		ReorientTableModel tmodel = (ReorientTableModel) this.jPanelReorient.jTableReorient.getModel();
	    		this.jPanelReorient.jTableReorient.setValueAt(OM.elementAt(0), rowct, tmodel.getColId("V1"));
	    		this.jPanelReorient.jTableReorient.setValueAt("", rowct, tmodel.getColId("V2"));
	    		this.jPanelReorient.jTableReorient.setValueAt(true, rowct, tmodel.getColId("Close"));
	    		this.jPanelReorient.jTableReorient.setValueAt("Smart Spot Separation for "+OM.elementAt(1)+"Å _specific_", rowct, tmodel.getColId("Comment"));
	    	}
	    }

	}
	
	
/*	
	public class TCalib_Needle
	   extends WizardAction
	   {
	      private MyModel model;
	      private JPanel mainView;
	      private JCheckBox agreeCheckbox;
	      private JTextArea license;

	      public TCalib_Needle()
	      {
	         super("My First Step", "A summary of the first step");

	         // build and layout the components..
	         mainView = new JPanel();
	         agreeCheckbox = new JCheckBox("Agree");
	         license = new JTextArea();
	         mainView.setLayout(...);
	         mainView.add(agreeCheckbox);
	         ...

	         // listen to changes in the state..
	         agreeCheckbox.addItemListener(new ItemListener()
	         {
	            public void itemSelected(ItemEvent e)
	            {
	               // only continue if they agree
	               MyWizardStep.this.setComplete(agreeCheckbox.isSelected());
	            }
	         });
	      }

	      public void init(WizardModel model)
	      {
	         this.model = (MyModel) model;
	      }

	      public void prepare()
	      {
	         // load our view...
	         setView(mainView);
	      }

	      public void applyState()
	      throws InvalidStateException
	      {
	         // display a progress bar of some kind..
	         setView(myProgressView);

	         setBusy(true);
	         try
	         {
	            // do some work on another thread.. see Foxtrot
	            ...
	         }
	         finally
	         {
	            setBusy(false);
	         }

	         // if error then throw an exception
	         if (!ok)
	         {
	            // restore our original view..
	            setView(mainView)
	            // The wizard will display this message to the user.
	            // You can prevent this by calling invalidStateException.setShowUser(false)
	            throw new InvalidStateException("That didn't work!");
	         }

	         // all is well so update the model
	         model.setAcceptsLicense(agreeCheckbox.isSelected());
	      }

	      public void getPreferredSize()
	      {
	         // use the size of our main view...
	         mainView.getPreferredSize();
	      }
	   }
*/
	public void GonioAutoTCalibration() {


		double [] phi= {60,120,180,240,300};
		int phistartstatus=0;
		double [] kappa= {30,60,180,210,240};
		int kappastartstatus=0;

		String[] choicesGENERIC = {"< Back", "Next >", "CANCEL"};
		String[] choicesNOBACK = { "Next >", "CANCEL"};		
		boolean done=false;
		boolean forward=true;
		int status=0;

		while (!done) {
			
			String [] choices=choicesGENERIC;
			String wText="";
			
			try {
				if (status==0) {
					choices=choicesNOBACK;
					wText="Be sure that calibration needle is mounted";
				} else if (status==1) {
					//Init motors
					GonioMotorInit();
					//mv to 0,0,0
					GonioTMvDatumZero();
					//center
					bcm.centerNeedle();
					wText="Be sure that \n  + Kappa motors have been initialised\n  + Calibration needle is properly centered";
				} else if (status==2) {
					if (forward) {
						//delete the table
						jPanelTransPhi.jTableTrans.selectAll();
						ActionEvent e=new ActionEvent(jPanelTransPhi.jButtonGTC_delSelected,0,"Simulated action");
						jPanelTransPhi.Button_actionAdapter(e);		
						//delete the table
						jPanelTransKappa.jTableTrans.selectAll();
						e=new ActionEvent(jPanelTransKappa.jButtonGTC_delSelected,0,"Simulated action");
						jPanelTransKappa.Button_actionAdapter(e);		
						//reset pos
						//GonioTResetMotors();
						GonioTsetRefPt();
						phistartstatus=status;
					}
					//******************************
					jPanelTransPhi.jTFGTCInpP.setText(""+phi[status-phistartstatus]);			
					//mv 0,0,phi
					ActionEvent e=new ActionEvent(jPanelTransPhi.jButtonGTC_mvP,0,"Simulated action");
					jPanelTransPhi.Button_actionAdapter(e);		
					//center
					bcm.centerNeedle();
					wText="Be sure that calibration needle is properly centered";				
				} else if (status<phistartstatus+phi.length) {
					if (forward) {
						//get value			
						ActionEvent e=new ActionEvent(jPanelTransPhi.jButtonGTC_getP,0,"Simulated action");
						jPanelTransPhi.Button_actionAdapter(e);		
					}
					//******************************
					jPanelTransPhi.jTFGTCInpP.setText(""+phi[status-phistartstatus]);			
					//mv 0,0,phi
					ActionEvent e=new ActionEvent(jPanelTransPhi.jButtonGTC_mvP,0,"Simulated action");
					jPanelTransPhi.Button_actionAdapter(e);		
					//center
					bcm.centerNeedle();
					wText="Be sure that calibration needle is properly centered";				
				} else if (status==phistartstatus+phi.length) {
					if (forward) {
						//get value			
						ActionEvent e=new ActionEvent(jPanelTransPhi.jButtonGTC_getP,0,"Simulated action");
						jPanelTransPhi.Button_actionAdapter(e);		
						kappastartstatus=status;
					}
					//******************************
					jPanelTransKappa.jTFGTCInpP.setText(""+kappa[status-kappastartstatus]);			
					//mv 0,0,phi
					ActionEvent e=new ActionEvent(jPanelTransKappa.jButtonGTC_mvP,0,"Simulated action");
					jPanelTransKappa.Button_actionAdapter(e);		
					//center
					bcm.centerNeedle();
					wText="Be sure that calibration needle is properly centered";				
				} else if (status<kappastartstatus+kappa.length) {
					if (forward) {
						//get value			
						ActionEvent e=new ActionEvent(jPanelTransKappa.jButtonGTC_getP,0,"Simulated action");
						jPanelTransKappa.Button_actionAdapter(e);
					}
					//******************************
					jPanelTransKappa.jTFGTCInpP.setText(""+kappa[status-kappastartstatus]);			
					//mv 0,0,phi
					ActionEvent e=new ActionEvent(jPanelTransKappa.jButtonGTC_mvP,0,"Simulated action");
					jPanelTransKappa.Button_actionAdapter(e);		
					//center
					bcm.centerNeedle();
					wText="Be sure that calibration needle is properly centered";				
				} else if (status==kappastartstatus+kappa.length) {
					if (forward) {
						//get value			
						ActionEvent e=new ActionEvent(jPanelTransKappa.jButtonGTC_getP,0,"Simulated action");
						jPanelTransKappa.Button_actionAdapter(e);
					}
					//******************************
					//calculate
					GonioTCalibration();
					wText="Check if \n"+
							"Calculated axes: XYZ shows reasonable direction vectors\n"+
							"  Note that Phi axis is commonly mounted to be close to (0;0;1)\n"+
							"Pts on axes: Reconstr.Err. shows reasonably small values";
				} else if (status==kappastartstatus+kappa.length+1) {
					//update
					GonioTUpdate();
					done=true;
				}
			} catch (Exception e) {
				session.errorMsg("Problem during automatic Translation Calibration falling back to manual mode!");
				done=true;
			}
			
			int response = 2;
			
			if (!done) {
				response=JOptionPane.showOptionDialog(
						this                       // Center in window.
						, wText        // Message
						, "Translation Calibration Wizard"               // Title in titlebar
						, JOptionPane.YES_NO_OPTION  // Option type
						, JOptionPane.PLAIN_MESSAGE  // messageType
						, null                       // Icon (none)
						, choices                    // Button text as above.
						, "None of your business"    // Default button's label
				);
			}

			forward=true;
			//... Use a switch statement to check which button was clicked.
			switch (response) {
			case 0: 
				if (status==0)
					status++;
				else {
					//based on the status remove phi or kappa measurement
					if (phistartstatus!=0 && status>phistartstatus && status<=phistartstatus+phi.length) {
					    TranslationTableModel tmodel = (TranslationTableModel) jPanelTransPhi.jTableTrans.getModel();
					    tmodel.setValueAt("ActivateCells",0,0);
						int row=jPanelTransPhi.jTableTrans.getRowCount()-1;
			    		double	angle=new Double((String)jPanelTransPhi.jTableTrans.getValueAt(row,tmodel.getColId("Angle"))).doubleValue();
			    		if (Math.abs(angle-phi[status-phistartstatus-1])<2e-1) {
			    			tmodel.setValueAt("ClearRow",row,0);
			    		}			  
					}
					if (kappastartstatus!=0 && status>kappastartstatus && status<=kappastartstatus+kappa.length) {
					    TranslationTableModel tmodel = (TranslationTableModel) jPanelTransKappa.jTableTrans.getModel();
					    tmodel.setValueAt("ActivateCells",0,0);
						int row=jPanelTransKappa.jTableTrans.getRowCount()-1;
			    		double	angle=new Double((String)jPanelTransKappa.jTableTrans.getValueAt(row,tmodel.getColId("Angle"))).doubleValue();
			    		if (Math.abs(angle-kappa[status-kappastartstatus-1])<2e-1) {
			    			tmodel.setValueAt("ClearRow",row,0);
			    		}			  
					}
					status--;
					forward=false;
				}
				break;
			case 1:
				if (status==0)
					done=true;
				else
					status++;
				break;
			case 2:
				done=true;
				break;
			case -1:
				//... Both the quit button (3) and the close box(-1) handled here.
				done=true;
				break;
			default:
				//... If we get here, something is wrong.  Defensive programming.
				//JOptionPane.showMessageDialog(null, "Unexpected response " + response);
				done=true;
			}


		}
//		
//		//Ask for Needle
//		try {
//			session.errorMsg("Be sure that calibration needle is mounted");
//			//Init motors
//			GonioMotorInit();
//			//mv to 0,0,0
//			GonioTMvDatumZero();
//			//center
//			bcm.centerNeedle();
//			session.errorMsg("Be sure that calibration needle is properly centered");
//			//delete the table
//			jPanelTransPhi.jTableTrans.selectAll();
//			ActionEvent e=new ActionEvent(jPanelTransPhi.jButtonGTC_delSelected,0,"Simulated action");
//			jPanelTransPhi.Button_actionAdapter(e);		
//			//delete the table
//			jPanelTransKappa.jTableTrans.selectAll();
//			e=new ActionEvent(jPanelTransKappa.jButtonGTC_delSelected,0,"Simulated action");
//			jPanelTransKappa.Button_actionAdapter(e);		
//			//reset pos
//			//GonioTResetMotors();
//			GonioTsetRefPt();
//			//for phi=0,90,180,270
//			//double [] phi= {90,180,270};
//			double [] phi= {60,120,180,240,300};
//			for(int i=0;i<phi.length;i++) {
//				jPanelTransPhi.jTFGTCInpP.setText(""+phi[i]);			
//				//mv 0,0,phi
//				e=new ActionEvent(jPanelTransPhi.jButtonGTC_mvP,0,"Simulated action");
//				jPanelTransPhi.Button_actionAdapter(e);		
//				//center
//				bcm.centerNeedle();
//				session.errorMsg("Be sure that calibration needle is properly centered");
//				//get value			
//				e=new ActionEvent(jPanelTransPhi.jButtonGTC_getP,0,"Simulated action");
//				jPanelTransPhi.Button_actionAdapter(e);		
//			}
//			//for kappa=0,80,160,240
//			//double [] kappa= {80,160,240};
//			double [] kappa= {30,60,180,210,240};
//			for(int i=0;i<kappa.length;i++) {
//				jPanelTransKappa.jTFGTCInpP.setText(""+kappa[i]);			
//				//mv 0,0,phi
//				e=new ActionEvent(jPanelTransKappa.jButtonGTC_mvP,0,"Simulated action");
//				jPanelTransKappa.Button_actionAdapter(e);		
//				//center
//				bcm.centerNeedle();
//				session.errorMsg("Be sure that calibration needle is properly centered");
//				//get value			
//				e=new ActionEvent(jPanelTransKappa.jButtonGTC_getP,0,"Simulated action");
//				jPanelTransKappa.Button_actionAdapter(e);		
//			}
//			//calculate
//			GonioTCalibration();
//			session.errorMsg("Check if \n"+
//					"Calculated axes: XYZ shows reasonable direction vectors\n"+
//					"  Note that Phi axis is commonly mounted to be close to (0;0;1)\n"+
//					"Pts on axes: Reconstr.Err. shows reasonably small values");
//			//update
//			GonioTUpdate();
//			//debug replacement
//			//utils.copyFile( new File(System.getProperty("BCMDEF")+"_autoTcalibBackup_"+actDateStr),new File(System.getProperty("BCMDEF")));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			Stac_Out.println("Problem during automatic Translation Calibration falling back to manual mode!");
//		}
//		
		
	}
	
	public void GonioAutoRCalibration() {

		
		JButton movemot[]={jButtonGRC_mvN,jButtonGRC_mvP,jButtonGRC_mvK,jButtonGRC_mvO};
		JButton matselect[]={jBMat0Sel,jBMat1Sel,jBMat2Sel,jBMat3Sel};
		int cyclestartstatus=0;
		int cycleendstatus=0;

		String wText1="Be sure that the XTAL is properly centered";
		wText1=wText1+"\n-------------------------------\n";
		//data collection + indexing
		wText1=wText1+"Collect a few images at the CURRENT Omega as oscillation start\n"+
				"and index them in a separate directory\n"+
				"-------------------------------\n"+
				"Check that you have generated the following files in a new directory:\n"+
				"  - mosflm: [bestfile].par\n"+
				"  - xds   : CORRECT.LP\n"+
		"  - denzo : .x";
		wText1=wText1+"\n-------------------------------\n";
		//load result
		wText1=wText1+"Reference the result by selecting the file:\n"+
				"  - mosflm: [bestfile].par\n"+
				"  - xds   : CORRECT.LP\n"+
		"  - denzo : .x";

		
		String[] choicesGENERIC = {"< Back", "Next >", "CANCEL"};
		String[] choicesNOBACK = { "Next >", "CANCEL"};		
		boolean done=false;
		boolean forward=true;
		int status=0;

		while (!done) {
			
			String [] choices=choicesGENERIC;
			String wText="";
			
			try {
				if (status==0) {
					choices=choicesNOBACK;
					wText="Be sure that a well-diffracting XTAL is mounted";
				} else if (status==1) {
					//Init motors
					GonioMotorInit();
					wText="Be sure that Kappa motors have been initialised";
				} else if (status==2) {
					cyclestartstatus=status;
					//move
					movemot[status-cyclestartstatus].doClick();
					//center
					wText=wText1;
				} else if (status==3) {
					if (forward) {
						matselect[status-cyclestartstatus-1].doClick();
					}
					//move
					movemot[status-cyclestartstatus].doClick();
					//center
					wText=wText1;
				} else if (status==4) {
					if (forward) {
						matselect[status-cyclestartstatus-1].doClick();
					}
					//move
					movemot[status-cyclestartstatus].doClick();
					//center
					wText=wText1;
				} else if (status==5) {
					if (forward) {
						matselect[status-cyclestartstatus-1].doClick();
					}
					//move
					movemot[status-cyclestartstatus].doClick();
					//center
					wText=wText1;
				} else if (status==6) {
					if (forward) {
						matselect[status-cyclestartstatus-1].doClick();
					}
					//calculate
					GonioCalibration();
					wText="Check if \n"+
							"- Calculated axes: XYZ shows reasonable direction vectors\n"+
							"  Note that Omega and Phi are commonly mounted to be close to (0;0;1)\n"+
							"- Calculated ANGLEs are close to the Calibration P/K/O rotations applied";
				} else if (status==7) {
					//update
					GonioUpdate();
					done=true;
				}
			} catch (Exception e) {
				session.errorMsg("Problem during automatic Rotation Calibration falling back to manual mode!");
				done=true;
			}
			
			int response = 2;
			
			if (!done) {
				response=JOptionPane.showOptionDialog(
						this                       // Center in window.
						, wText        // Message
						, "Rotation Calibration Wizard"               // Title in titlebar
						, JOptionPane.YES_NO_OPTION  // Option type
						, JOptionPane.PLAIN_MESSAGE  // messageType
						, null                       // Icon (none)
						, choices                    // Button text as above.
						, "None of your business"    // Default button's label
				);
			}

			forward=true;
			//... Use a switch statement to check which button was clicked.
			switch (response) {
			case 0: 
				if (status==0)
					status++;
				else {
					status--;
					forward=false;
				}
				break;
			case 1:
				if (status==0)
					done=true;
				else
					status++;
				break;
			case 2:
				done=true;
				break;
			case -1:
				//... Both the quit button (3) and the close box(-1) handled here.
				done=true;
				break;
			default:
				//... If we get here, something is wrong.  Defensive programming.
				//JOptionPane.showMessageDialog(null, "Unexpected response " + response);
				done=true;
			}


		}
		
		
		
		
//		
//		//Ask for Needle
//		try {
//			session.errorMsg("Be sure that a well-diffracting XTAL is mounted");
//			//Init motors
//			GonioMotorInit();
//			
//			
//			JButton movemot[]={jButtonGRC_mvN,jButtonGRC_mvP,jButtonGRC_mvK,jButtonGRC_mvO};
//			JButton matselect[]={jBMat0Sel,jBMat1Sel,jBMat2Sel,jBMat3Sel};
//			
//			for (int i=0;i<4;i++) {
//				//move
//				movemot[i].doClick();
//				//center
//				session.errorMsg("Be sure that the XTAL is properly centered");
//				//data collection + indexing
//				session.errorMsg("Collect a few images at the CURRENT Omega as oscillation start\n"+
//						"and index them\n"+
//						"----\n"+
//						"Check that you have generated the following files in a new directory:\n"+
//						"  - mosflm: [bestfile].par\n"+
//						"  - xds   : CORRECT.LP\n"+
//				"  - denzo : .x");
//				//load result
//				session.errorMsg("Reference the result by selecting the file:\n"+
//						"  - mosflm: [bestfile].par\n"+
//						"  - xds   : CORRECT.LP\n"+
//				"  - denzo : .x");
//				matselect[i].doClick();
//			}
//			
//			//calculate
//			GonioCalibration();
//			session.errorMsg("Check if \n"+
//					"Calculated axes: XYZ shows reasonable direction vectors\n"+
//					"  Note that Omega and Phi are commonly mounted to be close to (0;0;1)\n"+
//					"Calculated ANGLEs are close to the Calibration P/K/O rotations applied");
//			//update
//			GonioUpdate();
//			//debug replacement
//			//utils.copyFile( new File(System.getProperty("BCMDEF")+"_autoTcalibBackup_"+actDateStr),new File(System.getProperty("BCMDEF")));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			Stac_Out.println("Problem during automatic Rotation Calibration falling back to manual mode!");
//		}
//		
		
	}
    
//	public Vector getHKLfromOMFile(String fname,String gnsfile){
//		try {
//		Vector omhkl=new Vector(0);
//		//get the OM filename
//		String OMfilename;
//		String OMtype=new String("mosflm");
//		
//		ParamTable omdesc = LoadOMDescriptorTable(fname);
//		if (omdesc.getFirstStringValue("OMFILENAME")!=null) {
//			OMfilename=omdesc.getFirstStringValue("OMFILENAME");
//			OMtype = omdesc.getFirstStringValue("OMTYPE");
//		}
//		else {
//			OMfilename=fname;
//			//since we do not have radiobuttons here, we must rely on the filename!!!
//			OMtype = OMFileType(OMfilename);
//		}
//		//convert it
//		String MosflmSettingsFileName="";
//		if (OMtype.equalsIgnoreCase("mosflm")) {
//			File inFile = new File(OMfilename);
//			String my_dir=inFile.getParent()+File.separator;
//			MosflmSettingsFileName=my_dir+"mosflm.inp";
//		}
//		utils.ConvertInputFormat(OMfilename,MosflmSettingsFileName,OMtype,"OMhkl.x");
//		//get the OM
//  		double anglelimit=1.0;
//  		
//  		        //[CA]=[C][A]
//  			//Umat=Amat*[standard orientation matrix]
//  			//
//  			//what we need is: [RCA]=[C][R][A]=[C][R][C]^-1[CA]
//  			//so the new Umat should be: Amat*EQmat*Amat_inv*Amat
//  	  	    double[] cell = new double[6];
//  	  	    double[] tcell = new double[6];
//  	  	    Matrix3d B = new Matrix3d();
//  	  	    Matrix3d Bm1t = new Matrix3d();
//  	  	    Matrix3d U = new Matrix3d();
//  	  	    Vector3d phixyz = new Vector3d();
//  	  		Matrix3d osc = new Matrix3d();
//
//  		ParamTable OMT = new ParamTable();
//  		utils.read_denzo_x("OMhkl.x",OMT);
//  		Matrix3d OMU = new Matrix3d(OMT.getDoubleVector("Amat"));
//  		Matrix3d OMA = new Matrix3d(OMT.getDoubleVector("Umat"));
//		Matrix3d OMAi= new Matrix3d(OMA); OMAi.invert();
//  	    Matrix3d OMUB = new Matrix3d(OMA);
//  	    utils.denzo2mosflm_matrices_by_gonset(OMU,cell,tcell,B,Bm1t,U,OMUB,phixyz);
//  	    //indexing software assumes the spindle orientation as perfect
//  		osc.set(new AxisAngle4d(0,0,1,OMT.getFirstDoubleValue("phi_start")/180*Math.PI));
//  		OMUB.mul(osc,OMUB);
//  		Stac_Out.println("OM hkl :\n"+OMUB.toString());
//
//  		//!!!
//  		//equivalent matrices must be trated by the alignment module
//  		//currently GONSET does not deal with this (this is why it offers only a*-b*
//  		//and not b*-a* in cases when a*-b* is equvivalent with b*-a*)
//  		//!!!
//  		
//  		//get the current gonio settings for the spindle axis
//  	    Vector3d okp[]=new Vector3d[7];
//  	    utils.read_gnsdef(gnsfile,okp);
//		//get hkl coords of spindle axis
//  		Matrix3d OMUBinv=new Matrix3d(OMUB); OMUBinv.invert();
//  		Vector3d spindle=new Vector3d(okp[0]); OMUBinv.transform(spindle);
//  		omhkl.addElement("("+spindle.x+" "+spindle.y+" "+spindle.z+")");
//		//get hkl coords of beam
//  		Vector3d beam=new Vector3d(1,0,0); OMUBinv.transform(beam);
//  		omhkl.addElement("("+beam.x+" "+beam.y+" "+beam.z+")");
//  		
//  		return omhkl;
//	      }
//	      catch (Exception ex1) {
//	      	Stac_Out.println(" ERROR!");
//	      }
//		
//		return null;
//	}

}

class StacMainWin_jMainButton_actionAdapter implements java.awt.event.ActionListener {
  StacMainWin adaptee;

  StacMainWin_jMainButton_actionAdapter(StacMainWin adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
	if (e.getSource() instanceof JButton) {
        Stac_Out.printTimeln("USER ACTION: Button pressed (Tab id "+adaptee.jTabbedPane1.getSelectedIndex()+": "+((JButton)e.getSource()).getText()+")");			
	} else if (e.getSource() instanceof JCheckBox) {
        Stac_Out.printTimeln("USER ACTION: CheckBox pressed (Tab id "+adaptee.jTabbedPane1.getSelectedIndex()+": "+((JCheckBox)e.getSource()).getText()+" : "+((JCheckBox)e.getSource()).isSelected()+")");					
	}
    if (e.getSource()==adaptee.buttonOpenJobCotrolWin) {
    	adaptee.session.setErrorGUI(adaptee);
    } else if (e.getSource()==adaptee.chkboxCalibration) {
    	//adaptee.chkboxCalibration.setSelected(!adaptee.chkboxCalibration.isSelected());
        if (adaptee.chkboxCalibration.isSelected()) {
        	adaptee.jTabbedPane1.setEnabledAt(0,true);
        	adaptee.jTabbedPane1.setEnabledAt(1,true);
        } else {
        	adaptee.jTabbedPane1.setEnabledAt(0,false);
        	adaptee.jTabbedPane1.setEnabledAt(1,false);
        }
    } else if (e.getSource()==adaptee.jButtonMA_init) {
        //panel MotorAccess
        adaptee.bcm.initMotors();
    } else if (e.getSource()==adaptee.jButtonMA_getall) {
        //panel MotorAccess
    	Point3d dat= new Point3d();
    	Point3d trn= new Point3d();
        adaptee.bcm.getCurrentDatumTrans(dat,trn);
    } else if (e.getSource()==adaptee.jButtonMA_clear) {
        //panel MotorAccess
        adaptee.mvomega.setTFwhereToGo("");
        adaptee.mvkappa.setTFwhereToGo("");
        adaptee.mvphi.setTFwhereToGo("");
    } else if (e.getSource()==adaptee.jButtonMA_mvsync) {
        //panel MotorAccess
        String omega=adaptee.mvomega.getTFwhereToGo();
        String kappa=adaptee.mvkappa.getTFwhereToGo();
        String phi=adaptee.mvphi.getTFwhereToGo();
        ParamTable pos=new ParamTable();
        try {
        	if (omega.length()!=0) {
        		pos.addValue("Omega",new Double(omega));
        	}
        } catch (Exception e1) {        	
        }
        try {
        	if (kappa.length()!=0) {
        		pos.addValue("Kappa",new Double(kappa));
        	}
        } catch (Exception e1) {        	
        }
        try {
        	if (phi.length()!=0) {
        		pos.addValue("Phi",new Double(phi));
        	}
        } catch (Exception e1) {        	
        }
        adaptee.bcm.moveToNewDatumCenteredSync(pos);
    } else if (e.getSource()==adaptee.jButtonMA_AutoCenter) {
        //panel MotorAccess
    	adaptee.bcm.centerNeedle();
    } else if (e.getSource()==adaptee.jButtonMA_TiltLeft || e.getSource()==adaptee.jButtonMA_TiltRight) {
        //panel MotorAccess
        ParamTable pos=new ParamTable();
        Point3d dat = new Point3d();
        adaptee.bcm.getCurrentDatum(dat);
        Vector3d okp[]=new Vector3d[7];
        adaptee.bcm.getCalibration(okp);
        //adaptee.bcm.getParams for motor limits
        
        
        double newOmega,newKappa,newPhi;
        
        //version (a)
        // - do the tilt on the Omega-Phi plane (if Omega=Phi then on Omega-R(Kappa about Omega by 90))  
        // - its implementation is simple but might not as usefull as the version (b)
        {        
        	double omegaBase=dat.x-dat.y/2.0;
        	double deltaOmega=10*((e.getSource()==adaptee.jButtonMA_TiltLeft)?(1):(-1));
        	newOmega=dat.x;
        	newKappa=dat.y;
        	//check limits
        	for (boolean stepGood=false;!stepGood && Math.abs(deltaOmega)>0.5;deltaOmega/=2.0) {
        		newOmega=dat.x+deltaOmega;
        		newKappa=dat.y-2*deltaOmega;
        		if (newKappa<0 || newKappa>250)
        			stepGood=false;
        		else
        			stepGood=true;
        	}
        	
        	//R-kappaNew
        	AxisAngle4d baseAA=new AxisAngle4d(new Vector3d(okp[1]),newKappa*Math.PI/180.0);
        	Matrix3d rotK= new Matrix3d();        
        	Matrix3d rotKI= new Matrix3d();        
        	rotK.set(baseAA);
        	rotKI.set(rotK);
        	rotKI.invert();
        	//R-omegaNew
        	baseAA=new AxisAngle4d(new Vector3d(okp[0]),newOmega*Math.PI/180.0);
        	Matrix3d rotO= new Matrix3d();
        	Matrix3d rotOI= new Matrix3d();
        	rotO.set(baseAA);
        	rotOI.set(rotO);
        	rotOI.invert();
        	//R-kappaAct
        	baseAA=new AxisAngle4d(new Vector3d(okp[1]),dat.y*Math.PI/180.0);
        	Matrix3d rotKA= new Matrix3d();        
        	Matrix3d rotKAI= new Matrix3d();        
        	rotKA.set(baseAA);
        	rotKAI.set(rotKA);
        	rotKAI.invert();
        	//R-omegaAct
        	baseAA=new AxisAngle4d(new Vector3d(okp[0]),dat.x*Math.PI/180.0);
        	Matrix3d rotOA= new Matrix3d();
        	Matrix3d rotOAI= new Matrix3d();
        	rotOA.set(baseAA);
        	rotOAI.set(rotOA);
        	rotOAI.invert();
        	//plane normal
        	//Vector3d plane=new Vector3d();
        	//plane.cross(new Vector3d(okp[1]),new Vector3d(okp[0]));
        	//plane.cross(new Vector3d(okp[0]),plane);
        	//baseAA=new AxisAngle4d(new Vector3d(okp[0]),omegaBase*Math.PI/180.0);
        	Matrix3d baseRot= new Matrix3d();
        	//baseRot.set(baseAA);
        	//baseRot.transform(plane);
        	//tilt=angle(phiOrig,ROmegaNew*RKappaNew*RKappaInvAct*ROmegaInvAct*phiOrig)
        	baseRot.mul(rotKAI,rotOAI);
        	baseRot.mul(rotK,baseRot);
        	baseRot.mul(rotO,baseRot);
        	Vector3d phi = new Vector3d(okp[2]);
        	Vector3d phiN = new Vector3d(phi);
        	baseRot.transform(phiN);
        	double tilt=phi.angle(phiN);
        	//check the orientation
        	Vector3d phinorm=new Vector3d();
        	phinorm.cross(phi,phiN);
        	//if (plane.dot(phinorm)<0) {
        	//	tilt*=-1;        	
        	//}
        	//tiltEq (why phinorm and plane are different???!!!)
        	//AxisAngle4d tiltEq=new AxisAngle4d(plane vs. phinorm,tilt);
        	AxisAngle4d tiltEq=new AxisAngle4d(phinorm,tilt);
        	Matrix3d rotT= new Matrix3d();        
        	rotT.set(tiltEq);
        	//check the orientation now!
        	Vector3d phi2=new Vector3d(phi);
        	rotT.transform(phi2);
        	if (phi2.dot(phiN)<0) {
        		tilt*=-1;
        		tiltEq=new AxisAngle4d(phinorm,tilt);
        		rotT.set(tiltEq);
        		//debug
        		phi2.set(phi);
        		rotT.transform(phi2);
        		if (phi2.dot(phiN)<0) {
        			//error
        			//System.out.println("ERROR");
        		}
        	}
        	//ROmegaNew*RKappaNew*RDeltaPhi*RKappaInvAct*ROmegaInvAct=RTilt
        	baseRot.mul(rotOA,rotKA);
        	baseRot.mul(rotT,baseRot);
        	baseRot.mul(rotOI,baseRot);
        	baseRot.mul(rotKI,baseRot);
        	//baseAA.set(baseRot);
        	Quat4d actQ = new Quat4d(); Matrix3d actMatNorm = new Matrix3d(baseRot); actMatNorm.normalizeCP(); actQ.set(actMatNorm);
        	baseAA.set(actQ);
        	newPhi=baseAA.angle*180.0/Math.PI;
        	//check the orientation
        	Vector3d phirotdir=new Vector3d(baseAA.x,baseAA.y,baseAA.z);
        	if (phirotdir.dot(phi)<0)
        		newPhi*=-1;
        	newPhi+=dat.z;
        	//WHY PHIROTDIR DOES DIFFER FROM PHI???!!!
        	double di=Math.abs(1-Math.abs(phirotdir.dot(phi)));
        	if (di>=1e-2) {
        		//System.out.println("Cannot not perform the tilt correctly!");
        	}
        }
        
        //version (b)
        // - do the tilt on the camera plane
        // - very useful to do alignment if no indexing is available:
        //     * tilt the xtal to be horizontal
        //     * rotate Omega by 90
        //     * tilt the xtal to be horizontal again
        // - might not be trivial how to implement
        
        
        Vector3d tiltPlane= new Vector3d(1,0,0); //camera plane

        
        //calc V1 axis to be parallel to the spindle
        //calc V2 axis on the beam plane
        //V1'=RPhiActInv*RkappaActInv*RomegaActInv*V1
        //V2'=RPhiActInv*RkappaActInv*RomegaActInv*V2
        //calc Phi, so Rphi*V1': on cone of spindle about kappa
        //calc the appropriate Kappa to mv the pt to the spindle: Rkappa*Rphi*V1'
        //calc Omega to keep Romega*Rkappa*Rphi*V2' on the beam plane
        
        
//        if (false)
//        {
//        	//get current settings
//        	//get the new Omega
//        	double omegaBase=dat.x-dat.y/2.0;
//        	double deltaOmega=10*((e.getSource()==adaptee.jButtonMA_TiltLeft)?(1):(-1));
//        	newOmega=dat.x;
//        	newKappa=dat.y;
//        	//check limits
//        	for (boolean stepGood=false;!stepGood && Math.abs(deltaOmega)>0.5;) {
//        		deltaOmega/=2.0;
//        		newOmega=dat.x+deltaOmega;
//        		//calculate the corresponding Kappa 
//        		{
//        			
//        			//(ROmegaNew*RKappaNew*RKappaInvAct*ROmegaInvAct*phiAct).dot(TiltNorm)=0
//        			baseRot.mul(rotOA,rotKA);
//        			baseRot.mul(rotT,baseRot);
//        			baseRot.mul(rotOI,baseRot);
//        			baseRot.mul(rotKI,baseRot);
//        			//baseAA.set(baseRot);
//        			Quat4d actQ = new Quat4d(); Matrix3d actMatNorm = new Matrix3d(baseRot); actMatNorm.normalizeCP(); actQ.set(actMatNorm);
//        			baseAA.set(actQ);
//        			newPhi=baseAA.angle*180.0/Math.PI;
//        			//check the orientation
//        			Vector3d phirotdir=new Vector3d(baseAA.x,baseAA.y,baseAA.z);
//        			if (phirotdir.dot(phi)<0)
//        				newPhi*=-1;
//        			
//        			
//        			newKappa=dat.y-2*deltaOmega;
//        			if (newKappa<0 || newKappa>250)
//        				stepGood=false;
//        			else
//        				stepGood=true;
//        			
//        		}
//        		//calculate the corresponding Phi
//        		{
//        			
//        			//R-kappaNew
//        			AxisAngle4d baseAA=new AxisAngle4d(new Vector3d(okp[1]),newKappa*Math.PI/180.0);
//        			Matrix3d rotK= new Matrix3d();        
//        			Matrix3d rotKI= new Matrix3d();        
//        			rotK.set(baseAA);
//        			rotKI.set(rotK);
//        			rotKI.invert();
//        			//R-omegaNew
//        			baseAA=new AxisAngle4d(new Vector3d(okp[0]),newOmega*Math.PI/180.0);
//        			Matrix3d rotO= new Matrix3d();
//        			Matrix3d rotOI= new Matrix3d();
//        			rotO.set(baseAA);
//        			rotOI.set(rotO);
//        			rotOI.invert();
//        			//R-kappaAct
//        			baseAA=new AxisAngle4d(new Vector3d(okp[1]),dat.y*Math.PI/180.0);
//        			Matrix3d rotKA= new Matrix3d();        
//        			Matrix3d rotKAI= new Matrix3d();        
//        			rotKA.set(baseAA);
//        			rotKAI.set(rotKA);
//        			rotKAI.invert();
//        			//R-omegaAct
//        			baseAA=new AxisAngle4d(new Vector3d(okp[0]),dat.x*Math.PI/180.0);
//        			Matrix3d rotOA= new Matrix3d();
//        			Matrix3d rotOAI= new Matrix3d();
//        			rotOA.set(baseAA);
//        			rotOAI.set(rotOA);
//        			rotOAI.invert();
//        			//plane normal
//        			//Vector3d plane=new Vector3d();
//        			//plane.cross(new Vector3d(okp[1]),new Vector3d(okp[0]));
//        			//plane.cross(new Vector3d(okp[0]),plane);
//        			//baseAA=new AxisAngle4d(new Vector3d(okp[0]),omegaBase*Math.PI/180.0);
//        			Matrix3d baseRot= new Matrix3d();
//        			//baseRot.set(baseAA);
//        			//baseRot.transform(plane);
//        			//tilt=angle(phiOrig,ROmegaNew*RKappaNew*RKappaInvAct*ROmegaInvAct*phiOrig)
//        			baseRot.mul(rotKAI,rotOAI);
//        			baseRot.mul(rotK,baseRot);
//        			baseRot.mul(rotO,baseRot);
//        			Vector3d phi = new Vector3d(okp[2]);
//        			Vector3d phiN = new Vector3d(phi);
//        			baseRot.transform(phiN);
//        			double tilt=phi.angle(phiN);
//        			//check the orientation
//        			Vector3d phinorm=new Vector3d();
//        			phinorm.cross(phi,phiN);
//        			//if (plane.dot(phinorm)<0) {
//        			//	tilt*=-1;        	
//        			//}
//        			//tiltEq (why phinorm and plane are different???!!!)
//        			//AxisAngle4d tiltEq=new AxisAngle4d(plane vs. phinorm,tilt);
//        			AxisAngle4d tiltEq=new AxisAngle4d(phinorm,tilt);
//        			Matrix3d rotT= new Matrix3d();        
//        			rotT.set(tiltEq);
//        			//check the orientation now!
//        			Vector3d phi2=new Vector3d(phi);
//        			rotT.transform(phi2);
//        			if (phi2.dot(phiN)<0) {
//        				tilt*=-1;
//        				tiltEq=new AxisAngle4d(phinorm,tilt);
//        				rotT.set(tiltEq);
//        				//debug
//        				phi2.set(phi);
//        				rotT.transform(phi2);
//        				if (phi2.dot(phiN)<0) {
//        					//error
//        					//System.out.println("ERROR");
//        				}
//        			}
//        			//ROmegaNew*RKappaNew*RDeltaPhi*RKappaInvAct*ROmegaInvAct=RTilt
//        			baseRot.mul(rotOA,rotKA);
//        			baseRot.mul(rotT,baseRot);
//        			baseRot.mul(rotOI,baseRot);
//        			baseRot.mul(rotKI,baseRot);
//        			//baseAA.set(baseRot);
//        			Quat4d actQ = new Quat4d(); Matrix3d actMatNorm = new Matrix3d(baseRot); actMatNorm.normalizeCP(); actQ.set(actMatNorm);
//        			baseAA.set(actQ);
//        			newPhi=baseAA.angle*180.0/Math.PI;
//        			//check the orientation
//        			Vector3d phirotdir=new Vector3d(baseAA.x,baseAA.y,baseAA.z);
//        			if (phirotdir.dot(phi)<0)
//        				newPhi*=-1;
//        			newPhi+=dat.z;
//        			//WHY PHIROTDIR DOES DIFFER FROM PHI???!!!
//        			double di=Math.abs(1-Math.abs(phirotdir.dot(phi)));
//        			if (di>=1e-2) {
//        				//System.out.println("Cannot not perform the tilt correctly!");
//        			}
//        			
//        		}
//        	}
//        }
        
		pos.addValue("Omega",new Double(newOmega));
		pos.addValue("Kappa",new Double(newKappa));
		pos.addValue("Phi",new Double(newPhi));
        
        adaptee.bcm.moveToNewDatumCenteredSync(pos);
    } else if (e.getSource()==adaptee.jButtonGonioCalib) {
        //panel GonioRCalibration
        adaptee.GonioCalibration();
    } else if (e.getSource()==adaptee.jButtonGRC_getT) {
        //panel GonioRCalibration copied from Transcalib
        adaptee.GonioCalibration(true);
    } else if (e.getSource()==adaptee.jButtonGnsdefUpdate) {
        //panel GonioRCalibration
        adaptee.GonioUpdate();//System.getProperty("GNSDEF"));
        adaptee.jTabbedPane1.setSelectedIndex(adaptee.jTabbedPane1.getSelectedIndex()+1);
    } else if (e.getSource()==adaptee.jButtonGonioAutoTCalib) {
        //panel GonioTCalibration
        adaptee.GonioAutoTCalibration();
        //adaptee.jTabbedPane1.setSelectedIndex(adaptee.jTabbedPane1.getSelectedIndex()+1);
    } else if (e.getSource()==adaptee.jButtonGonioAutoRCalib) {
        //panel GonioTCalibration
        adaptee.GonioAutoRCalibration();
        //adaptee.jTabbedPane1.setSelectedIndex(adaptee.jTabbedPane1.getSelectedIndex()+1);
    } else if (e.getSource()==adaptee.jButtonGonioTCalib) {
        //panel GonioTCalibration
        adaptee.GonioTCalibration();
    } else if (e.getSource()==adaptee.jButtonGTC_calcRot) {
        //panel GonioTCalibration using the RotCalib axes
        adaptee.GonioTCalibration(true);
    } else if (e.getSource()==adaptee.jButtonGnsdefTUpdate) {
        //panel GonioTCalibration
        adaptee.GonioTUpdate();//System.getProperty("GNSDEF"));
        adaptee.jTabbedPane1.setSelectedIndex(adaptee.jTabbedPane1.getSelectedIndex()+1);
    } else if (e.getSource()==adaptee.jButtonGRC_mvN) {
        //panel GonioTCalibration
    	adaptee.bcm.moveToDatum(0,0,0);
    } else if (e.getSource()==adaptee.jButtonGRC_mvP) {
        //panel GonioCalibration
    	if (adaptee.jChkBoxGC_sync.isSelected()) {
    		ParamTable pos= new ParamTable();
    		pos.addValue("Omega",new Double(0));
    		pos.addValue("Kappa",new Double(0));
    		pos.addValue("Phi",new Double(adaptee.jTFGCPhi.getText()));
    		adaptee.bcm.moveToNewDatumCenteredSync(pos);
    	} else
    		adaptee.bcm.moveToDatum(0,0,new Double(adaptee.jTFGCPhi.getText()).doubleValue());
    } else if (e.getSource()==adaptee.jButtonGRC_mvK) {
        //panel GonioCalibration
    	if (adaptee.jChkBoxGC_sync.isSelected()) {
    		ParamTable pos= new ParamTable();
    		pos.addValue("Omega",new Double(0));
    		pos.addValue("Kappa",new Double(adaptee.jTFGCKappa.getText()));
    		pos.addValue("Phi",new Double(adaptee.jTFGCPhi.getText()));
    		adaptee.bcm.moveToNewDatumCenteredSync(pos);
    	} else
    		adaptee.bcm.moveToDatum(0,new Double(adaptee.jTFGCKappa.getText()).doubleValue(),new Double(adaptee.jTFGCPhi.getText()).doubleValue());
    } else if (e.getSource()==adaptee.jButtonGRC_mvO) {
        //panel GonioCalibration
    	if (adaptee.jChkBoxGC_sync.isSelected()) {
    		ParamTable pos= new ParamTable();
    		pos.addValue("Omega",new Double(adaptee.jTFGCOmega.getText()));
    		pos.addValue("Kappa",new Double(adaptee.jTFGCKappa.getText()));
    		pos.addValue("Phi",new Double(adaptee.jTFGCPhi.getText()));
    		adaptee.bcm.moveToNewDatumCenteredSync(pos);
    	} else
    		adaptee.bcm.moveToDatum(new Double(adaptee.jTFGCOmega.getText()).doubleValue(),new Double(adaptee.jTFGCKappa.getText()).doubleValue(),new Double(adaptee.jTFGCPhi.getText()).doubleValue());
    } else if (e.getSource()==adaptee.jButtonTGC_init) {
        //panel GonioTCalibration
    	adaptee.GonioMotorInit();
    } else if (e.getSource()==adaptee.jButtonGC_init) {
        //panel GonioCalibration
    	adaptee.GonioMotorInit();
//    } else if (e.getSource()==adaptee.jButtonGTC_getK) {
//        //panel GonioTCalibration
//    	adaptee.GonioTGetTransK();
//    } else if (e.getSource()==adaptee.jButtonGTC_getP) {
//        //panel GonioTCalibration
//    	adaptee.GonioTGetTransP();
//    } else if (e.getSource()==adaptee.jButtonGTC_mvK) {
//        //panel GonioTCalibration
//    	adaptee.GonioTMvDatumK();
//    } else if (e.getSource()==adaptee.jButtonGTC_mvP) {
//        //panel GonioTCalibration
//    	adaptee.GonioTMvDatumP();
    } else if (e.getSource()==adaptee.jButtonGTC_mvreset) {
        //panel GonioTCalibration
    	adaptee.GonioTMvDatumZero();
    } else if (e.getSource()==adaptee.jButtonGTC_reset) {
        //panel GonioTCalibration
    	//adaptee.GonioTResetMotors();
    	adaptee.GonioTsetRefPt();
    } else if (e.getSource()==adaptee.jButton1) {
        //panel 1 - Load input crystal data
        adaptee.LoadCrystalData_actionPerformed(e);
        //adaptee.jTabbedPane1.setSelectedIndex(adaptee.jTabbedPane1.getSelectedIndex()+1);
    } else if (e.getSource()==adaptee.jButton1_1) {
        //panel 1_1 - Alignment Calc
    	//if OM file has not been loaded yet
    	if (adaptee.jTFCPar.getText().length()==0) {
    		adaptee.jButton1.doClick();    		
    	}
    	//if no alignment has been selected
    	Vector orientations = adaptee.jPanelReorient.getOrientationRequest();
    	if (orientations.size()==0) {
    		adaptee.jPanelReorient.jButtonSTD.doClick();    		
    	}
        adaptee.AllignmentCalculation_actionPerformed(e);
        adaptee.jTabbedPane1.setSelectedIndex(adaptee.jTabbedPane1.getSelectedIndex()+1);
    } else if (e.getSource()==adaptee.jButtonR_getPos) {
        //panel 1 - get datum trans
    	adaptee.InputGetCurrentDatumTrans();
    } else if (e.getSource()==adaptee.jButtonNewXtal) {
        //panel 1 - New Xtal to start working with
    	adaptee.jTextFieldDefFile.setText("");
    	adaptee.jTFOmega.setText("");
    	adaptee.jTFKappa.setText("");
    	adaptee.jTFPhi.setText("");
    	adaptee.jTFTransX.setText("");
    	adaptee.jTFTransY.setText("");
    	adaptee.jTFTransZ.setText("");
    	adaptee.jTFCPar.setText("");
    	adaptee.jTFCPar2.setText("");
    	adaptee.jTFCPar3.setText("");
    	adaptee.jPanelReorient.setSPG("");
    	adaptee.jPanelReorient.clearTable();
    	adaptee.datumPanel.clearTable();
    	adaptee.strategyWidget.clearTable();
    } else if (e.getSource()==adaptee.jButtonDescLoad) {
        //panel 1 - Load OM desc
    	adaptee.LoadOMDescriptor(adaptee.jTextFieldDescDefFile.getText());
    } else if (e.getSource()==adaptee.jButtonDescSave) {
        //panel 1 - Save OM desc
    	adaptee.SaveOMDescriptor(adaptee.jTextFieldDescDefFile.getText());
//    } else if (e.getSource()==adaptee.jButtonRLeftAdd) {
//        //panel 1
//    	int rowct=adaptee.jTableReorient.getRowCount();
//        ReorientTableModel tmodel = (ReorientTableModel) adaptee.jTableReorient.getModel();
//        adaptee.jTableReorient.setValueAt("", rowct, tmodel.getColId("V1"));
//        //adaptee.jTableReorient.setValueAt("", rowct, tmodel.getColId("V2"));
//    } else if (e.getSource()==adaptee.jButtonRLeftDelete) {
//        //panel 1
//        int numOfVecs = adaptee.jTableReorient.getSelectedRowCount();
//        int[] vecIDs = adaptee.jTableReorient.getSelectedRows();
//        ReorientTableModel tmodel = (ReorientTableModel) adaptee.jTableReorient.getModel();
//        for(int i=vecIDs.length-1;i>=0;i--){
//            tmodel.setValueAt("ClearRow",vecIDs[i],vecIDs[i]);
//        }
//    } else if (e.getSource()==adaptee.jButtonRLeftGetFile) {
//        //panel 1
//    	Vector OM = adaptee.getHKLfromOMFile(adaptee.jTextFieldRGF.getText(),System.getProperty("GNSDEF"));
//    	if (OM!=null && OM.size()>=2) {
//    		int rowct=adaptee.jTableReorient.getRowCount();
//    		ReorientTableModel tmodel = (ReorientTableModel) adaptee.jTableReorient.getModel();
//    		adaptee.jTableReorient.setValueAt(OM.elementAt(0), rowct, tmodel.getColId("V1"));
//    		adaptee.jTableReorient.setValueAt(OM.elementAt(1), rowct, tmodel.getColId("V2"));
//    	}
    } else if (e.getSource()==adaptee.jButton32) {
      //panel 3
      adaptee.fillStrategyTable();
      adaptee.jTabbedPane1.setSelectedIndex(adaptee.jTabbedPane1.getSelectedIndex()+1);
//    } else if (e.getSource()==adaptee.jButtonEval_getP) {
//        //panel 3
//    	adaptee.EvalGetCurrentDatumTrans();
//    } else if (e.getSource()==adaptee.jButtonEval_getP2) {
//        //panel 3
//    	adaptee.EvalGetCurrentDatumTrans2();
    } else if (e.getSource()==adaptee.jButton42) {
      //panel 4
      adaptee.writeFinalStrategies();
    }
  }
}

class StacMainWin_FileChooser implements java.awt.event.ActionListener {
	  StacMainWin adaptee;

	  StacMainWin_FileChooser(StacMainWin adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void actionPerformed(ActionEvent e) {
	    adaptee.FileChooser_actionPerformed(e);
	  }
	}

class ModifiedObject extends Object {
	private Object original=null;
	private Object newValue=null;
	
	ModifiedObject(Object original,Object newValue) {
		this.original=original;
		newValue(newValue);
	}
	
	public void newValue(Object newValue) {
		this.newValue=newValue;
	}
	
	public Object getOriginal() {
		return original;
	}
	
	public Object getNew() {
		return newValue;
	}
	
	public boolean equals(Object obj) {
		return actValue().equals(obj);
	}
	
	public Object actValue() {
		if (unchanged())
			return original;
		else
			return newValue;
		
	}
	
	public String toString() {
		String origStr=(original==null)?"":original.toString();
		String newStr=(newValue==null)?"":newValue.toString();
		if (changed())
			return newStr;
		else
			return origStr;		
	}
	
	public boolean unchanged() {
		if (newValue==null || newValue.equals(original) || newValue.toString().equals("") )
			return true;
		else
			return false;
	}
	
	public boolean changed() {
		return !unchanged();
	}
	
	public String toStringModification() {
		String origStr=(original==null)?"":original.toString();
		String newStr=(newValue==null)?"":newValue.toString();
		if (changed())
			return newStr+" ("+origStr+")";
		else
			return origStr;		
	}
}
class StrategyTableCellRenderer extends DefaultTableCellRenderer {
//class StrategyTableCellRenderer extends JLabel implements TableCellRenderer {
	boolean stripped;
	
	StrategyTableCellRenderer() {
		init(false);
	}	
	StrategyTableCellRenderer(boolean stripped) {
		init (stripped);
	}
	public void init(boolean stripped) {
	   this.stripped=stripped;	
	}
	public Component getTableCellRendererComponent(
			JTable table,
			java.lang.Object value,
			boolean isSelected,
			boolean hasFocus,
			int row, int column) {
		Color cf= table.getForeground();
		//StrategyTableModel tmodel = ((StrategyTableModel)table.getModel());
		TableModel t1model = table.getModel();
		TableModel t2model =null;
		if (t1model instanceof TableSorter)
			t2model=((TableSorter)t1model).getTableModel();
		else
			t2model=t1model;
		if( !isSelected ) {
			Color c = table.getBackground();
//			if( (!stripped && ((Integer)table.getValueAt(row,((StrategyTableModel)table.getModel()).getColId("ID"))).intValue()%2==1 ) ||
			if( (!stripped && ((Integer)table.getValueAt(row,((StrategyTableModel)t2model).getColId("ID"))).intValue()%2==1 ) ||
				( stripped && row%2==0))
				//setBackground(Color.lightGray);
				setBackground(new Color(230,230,230));
				/*new Color( (int)(c.getRed()/2),//-10,
						(int)(c.getGreen()/2),//-10,
						(int)(c.getBlue()/2)));//-10));*/
			else
				setBackground(c);
		} else {
			Color c = table.getSelectionBackground();
			if( (!stripped && ((Integer)table.getValueAt(row,((StrategyTableModel)t2model).getColId("ID"))).intValue()%2==1 ) ||
				( stripped && row%2==0))
				c=new Color(c.getRed(),c.getGreen(),Math.min(255,c.getBlue()+10));
			else 
				c=new Color(c.getRed(),c.getGreen(),c.getBlue());;
			setBackground(c);			
		}
		//foreground			
//		if ((table.getValueAt(row,column) instanceof ModifiedObject) &&
//				((ModifiedObject)table.getValueAt(row,column)).changed()
//				) {
		if (t2model instanceof StrategyTableModel && ((StrategyTableModel)t2model).modifiedValueAt(((TableSorter)t1model).modelIndex(row),column) ) {
			setForeground(new Color(255,0,0));
			//setToolTipText(((ModifiedObject)table.getValueAt(row,column)).toStringModification());
			setText(((StrategyTableModel)t2model).getModifiedObjectAt(((TableSorter)t1model).modelIndex(row),column).toStringModification());
		} else {
			setForeground(cf);				
			setText((value==null)?"":value.toString());
		}
		setFont(table.getFont());
		return this;
//		return super.getTableCellRendererComponent( table,
//				value,isSelected,hasFocus,row,column);
	}
	
/*
	public void setValue(Object value) {
	  	if(true) {
	  		setBackground(Color.white);
	  		setForeground(Color.black);
	  	} else {
	  		setBackground(Color.gray);
	  		setForeground(Color.black);
	  	}
        super.setValue(value);
	  }
*/	
}
class StrategyTableModel extends DatumTableModel {
	
	public void initColumns() {
  ColumnNames = new String[] {
      "ID",
      "OmegaStart",
      "Incr",
      "Time",
      "Images",
      "1st img",
      "Resolution",
      "Kappa",
      "Phi",
      "Completeness",
	  "Rank",
	  "Comment"};
	}
	public void initfill() {};
  public Object getValueAt(int row, int col) {
	  if (Data[col].elementAt(row) instanceof ModifiedObject)
		  return ((ModifiedObject)Data[col].elementAt(row)).actValue();
	  else
		  return Data[col].elementAt(row);
  }
  
  public boolean modifiedValueAt(int row, int col) {
	  if (Data[col].elementAt(row) instanceof ModifiedObject)
		  return ((ModifiedObject)Data[col].elementAt(row)).changed();
	  else
		  return false;	  
  }
  
  public ModifiedObject getModifiedObjectAt(int row, int col) {
	  if (modifiedValueAt(row,col))
		  return ((ModifiedObject)Data[col].elementAt(row));
	  else
		  return null;
  }
  
  public Object getOrigValueAt(int row, int col) {
	  if (modifiedValueAt(row,col))
		  return ((ModifiedObject)Data[col].elementAt(row)).getOriginal();
	  else
		  return Data[col].elementAt(row);
  }

  public Class getColumnClass(int c) {
	  if (c==getColId("rank")) {
		  return Double.class;
	  }
	  return getValueAt(0, c).getClass();
  }
  
  public double calculateStrategyRanking(Vector orientations) {
	  double totExp=0.;
	  for (int i=0;i<orientations.size();i++) {
		  totExp+=((ParamTable)orientations.elementAt(i)).getFirstIntegerValue("Images")*
		  ((ParamTable)orientations.elementAt(i)).getFirstDoubleValue("Time");
	  }

	  return totExp;
  }
  public double calculateStrategyRanking(Integer strId) {
	  return calculateStrategyRanking(strId,false);
  }
  public double calculateStrategyRanking(Integer strId,boolean orig) {
	  //get the actual data for the given strategy 
	  Vector orientations = new Vector();
	  for (int i=0;i<(getRowCount());i++) {
		  if (strId.equals((Integer)(getValueAt(i,getColId("ID"))))) {  			
			  //ParamTable cols= new ParamTable(origCols);
			  ParamTable cols= new ParamTable();
			  for (int j=0;j<getColumnCount();j++) {
				  cols.setSingleValue(getColumnName(j), (orig)?getOrigValueAt(i,j):getValueAt(i,j));
				  //cols.setSingleValue("strategy_data",strategyData.elementAt((vecIDs.length!=0)?vecIDs[i]:i));
			  }
			  orientations.addElement(cols);
		  }
	  }
	  
	  return calculateStrategyRanking(orientations);
  }
  /**
   * temporarily it is a simpla ranking scheme implementation
   * as calculating the total exposure time.
   * In a final implementation this should be moved to strategy plugin
   * to be able to select between different ranking schemes
   * @return the ranked value of the multiple sweep strategy
   */
  public void recalculateStrategyRanking(Integer strId) {
	  
	  //calculate the ranking value
	  double rank=calculateStrategyRanking(strId);
	  
	  //set up the new ranking value for each sweep
	  for (int i=0;i<(getRowCount());i++) {
		  if (strId.equals((Integer)(getValueAt(i,getColId("ID"))))) {  			
			  setNewValueAt(new Double(rank),i,getColId("rank"),true);
		  }
	  }
	  
	  
  }

  public void setValueAt(Object value, int row, int col) {
	  setNewValueAt(value,row,col,false);
	  return;
  }
  /*
   * Don't need to implement this method unless your table's
   * data can change.
   */
  public void setNewValueAt(Object value, int row, int col,boolean onlyModify) {
	  if (value == "RestartRanking") {
	      for (int i = 0; i < getRowCount(); i++) {
	    	  Double OrigValue= new Double(calculateStrategyRanking((Integer)(getValueAt(i,getColId("ID"))),true));
	    	  Double newOrigValue= new Double(calculateStrategyRanking((Integer)(getValueAt(i,getColId("ID")))));
	    	  if (OrigValue.equals(newOrigValue)) {
	  			  Data[getColId("rank")].setElementAt(OrigValue, i);
	    	  } else {
	    		  Data[getColId("rank")].setElementAt(new ModifiedObject(OrigValue,newOrigValue), i);
	    	  }
	      }
		  
	  } else if (value == "ClearTable") {
      //clear the whole table
      for (int i = 0; i < ColumnNames.length; i++)
        Data[i].removeAllElements();
      fireTableStructureChanged();
    }
  	else if (value == "ActivateCells") {
  		//NC, because the table is not editable
  	}
    else if (row >= 0 && row < getRowCount() && 
    		col >= 0 && col < getColumnCount() &&
    		col != getColId("ID")) {
      //modify an element
      //get the old value
    	Object oldValue=Data[col].elementAt(row);
//    	if (oldValue==null) {
//		alignData[col].setElementAt(value, row);
//	} else if (oldValue instanceof ModifiedObject) {
//		((ModifiedObject)oldValue).newValue(value);    		
//	} else {
//		ModifiedObject newVal=new ModifiedObject(oldValue,value);
//	    alignData[col].setElementAt(newVal, row);    		
//	}
    	//if (value!=null) {
    		if (!onlyModify && oldValue==null) {
    			if (value!=null) {
    				//ModifiedObject newVal=new ModifiedObject(value,value);
    				Data[col].setElementAt(value, row);
    			}
    		} else if (oldValue!=null) {
    			if (oldValue instanceof ModifiedObject) {
    				((ModifiedObject)oldValue).newValue(value);    		
    			} else {
    				ModifiedObject newVal=new ModifiedObject(oldValue,value);
    				Data[col].setElementAt(newVal, row);
    			}
    		} 
    	//}
    		
    	//alignData[row][col] = value;
    	//alignData[col].setElementAt(value, row);
    	fireTableCellUpdated(row, col);
    	if (getColId("Rank")!=col) {
    		recalculateStrategyRanking((Integer)(getValueAt(row,getColId("ID"))));
    	}
    }
    else if (row == getRowCount()) {
      //insert a new row
      for (int i = 0; i < ColumnNames.length; i++) {
    	  if (col==i)
    		  Data[i].addElement(value);
    	  else
    		  Data[i].addElement(null);
      }
        //fireTableRowsInserted(row,row);
      fireTableStructureChanged();      
    }
  }
  public boolean isCellEditable(int rowIndex, int columnIndex) {
	  if (
			  columnIndex != getColId("ID") &&
			  columnIndex != getColId("Completeness"))
		  return true;
	  else
		  return false;
  }

  @Override
  public double[] getSelectedDatumTrans(int vecID) {
//	  double o=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("omega"))).doubleValue();
//	  double k=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("kappa"))).doubleValue();
//	  double p=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("phi"))).doubleValue();
//	  String tr = (String)jTable1.getValueAt(vecIDs[0],amodel.getColId("Trans"));
//	  tr=tr.substring(1,tr.length()-1);
//	  String tmp[]=tr.split(";");
//	  try {
//	  x=datumTrans[3];
//	  y=datumTrans[4];
//	  z=datumTrans[5];

//	  x = new Double(tmp[0]).doubleValue();
//	  y = new Double(tmp[1]).doubleValue();
//	  z = new Double(tmp[2]).doubleValue();
//	  } catch (Exception e) {
//	  recalcTrans=true;
//	  }
//	  return null;
	  double o=((Double)this.getValueAt(vecID,this.getColId("OmegaStart"))).doubleValue();
	  double k=((Double)this.getValueAt(vecID,this.getColId("Kappa"))).doubleValue();
	  double p=((Double)this.getValueAt(vecID,this.getColId("Phi"))).doubleValue();
	  double x,y,z;
	  //double [] res=null;
	  try {
		  String tr = (String)this.getValueAt(vecID,this.getColId("Trans"));
		  tr=tr.substring(1,tr.length()-1);
		  String tmp[]=tr.split(";");

		  x = new Double(tmp[0]).doubleValue();
		  y = new Double(tmp[1]).doubleValue();
		  z = new Double(tmp[2]).doubleValue();
	  } catch (Exception e) {
		  double [] res= {o,k,p};
		  return res;
	  }
	  double [] res = {o,k,p,x,y,z};
	  return res;
  }
	@Override
	void createTableColumnModel() {
	      this.table.setAutoCreateColumnsFromModel(false);
		
	   	DefaultTableColumnModel columnModel= new DefaultTableColumnModel();
	      for (int modelColumnNumber = 0; modelColumnNumber < this.getColumnCount(); modelColumnNumber++)
	      {
	    	  TableColumn column = new TableColumn(modelColumnNumber);
	         column.setHeaderValue(this.getActColumnName(modelColumnNumber));
	         if (
	        		 ColumnNames[modelColumnNumber].equals("sajt") ) {
	        	 column.setMaxWidth(-1);
	         } else if (
	        		 ColumnNames[modelColumnNumber].equals("ID") ||
	        		 ColumnNames[modelColumnNumber].equals("Resolution") ||
	        		 ColumnNames[modelColumnNumber].equals("Images") ||
	        		 ColumnNames[modelColumnNumber].equals("Incr") ||
	        		 ColumnNames[modelColumnNumber].equals("1st img") ||
	        		 ColumnNames[modelColumnNumber].equals("Time") ) {
	        	 column.setMaxWidth(50);
	         } else if (
	        		 ColumnNames[modelColumnNumber].equals("Rank") ||
	        		 ColumnNames[modelColumnNumber].equals("Completeness") ) {
	        	 column.setPreferredWidth(70);
	         } else if (
	        		 ColumnNames[modelColumnNumber].equals("Comment") ) {
	        	 column.setPreferredWidth(200);
	         }
	         columnModel.addColumn(column);
	      }
	      
	      this.table.setColumnModel(columnModel);
	   
	}	  
}

abstract class DatumTableModel extends STAC_GUI_AbstractTableModel {
	abstract public double[] getSelectedDatumTrans(int vecID);
}

class AlignTableModel extends DatumTableModel {
//	  private String[] alignColumnNames = {
//	      "ID", "V1", "V2", "Omega", "Kappa", "Phi","Trans",
//	      "Rank"};
//	  private Vector[] alignData = new Vector[alignColumnNames.length];

	  public void initColumns() {
		  ColumnNames = new String [] {
			      "ID", "V1", "V2", "Omega", "Kappa", "Phi","Trans",
			      "Rank","Comment"};
	  }
	  public void initfill() {
	  }

//	  public int getColumnCount() {
//	    return alignColumnNames.length;
//	  }
//
//	  public int getRowCount() {
//	    //return alignData.length;
//	    return alignData[0].size();
//	  }
//
//	  public String getColumnName(int col) {
//	    return alignColumnNames[col];
//	  }
//
//	  public int getColId(String name) {
//	    for (int i = 0; i < alignColumnNames.length; i++)
//	        if (alignColumnNames[i].equalsIgnoreCase(name))
//	        	return i;
//	  	return 0;
//	  }
//
//	  public Object getValueAt(int row, int col) {
//	    return alignData[col].elementAt(row);
//	  }
//
//	  public Class getColumnClass(int c) {
//	    return getValueAt(0, c).getClass();
//	  }

	  /*
	   * Don't need to implement this method unless your table's
	   * data can change.
	   */
	  public void setValueAt(Object value, int row, int col) {
	    if (value == "ClearTable") {
	      //clear the whole table
	      for (int i = 0; i < ColumnNames.length; i++)
	        Data[i].removeAllElements();
	      fireTableStructureChanged();
	    }
	  	else if (value == "ActivateCells") {
	  		//NC, because the table is not editable
	  	}
	    else if (row >= 0 && row < getRowCount() && col >= 0 &&
	             col < getColumnCount()) {
	      //modify an element
	      //alignData[row][col] = value;
	      Data[col].setElementAt(value, row);
	      fireTableCellUpdated(row, col);
	    }
	    else if (row == getRowCount()) {
	      //insert a new row
	      for (int i = 0; i < ColumnNames.length; i++)
	        Data[i].addElement(value);
	        //fireTableRowsInserted(row,row);
	      fireTableStructureChanged();
	    }
	  }

	  public boolean isCellEditable(int rowIndex, int columnIndex) {
		  return false;
	  }

	  @Override
	  public double[] getSelectedDatumTrans(int vecID) {
//		  double o=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("omega"))).doubleValue();
//		  double k=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("kappa"))).doubleValue();
//		  double p=((Double)jTable1.getValueAt(vecIDs[0],amodel.getColId("phi"))).doubleValue();
//		  String tr = (String)jTable1.getValueAt(vecIDs[0],amodel.getColId("Trans"));
//		  tr=tr.substring(1,tr.length()-1);
//		  String tmp[]=tr.split(";");
//		  try {
//		  x=datumTrans[3];
//		  y=datumTrans[4];
//		  z=datumTrans[5];

//		  x = new Double(tmp[0]).doubleValue();
//		  y = new Double(tmp[1]).doubleValue();
//		  z = new Double(tmp[2]).doubleValue();
//		  } catch (Exception e) {
//		  recalcTrans=true;
//		  }
//		  return null;
		  double o=((Double)this.getValueAt(vecID,this.getColId("omega"))).doubleValue();
		  double k=((Double)this.getValueAt(vecID,this.getColId("kappa"))).doubleValue();
		  double p=((Double)this.getValueAt(vecID,this.getColId("phi"))).doubleValue();
		  double x,y,z;
		  //double [] res=null;
		  try {
			  String tr = (String)this.getValueAt(vecID,this.getColId("Trans"));
			  tr=tr.substring(1,tr.length()-1);
			  String tmp[]=tr.split(";");

			  x = new Double(tmp[0]).doubleValue();
			  y = new Double(tmp[1]).doubleValue();
			  z = new Double(tmp[2]).doubleValue();
		  } catch (Exception e) {
			  double [] res= {o,k,p};
			  return res;
		  }
		  double [] res = {o,k,p,x,y,z};
		  return res;
	  }
	@Override
	void createTableColumnModel() {
	      this.table.setAutoCreateColumnsFromModel(false);
		
	   	DefaultTableColumnModel columnModel= new DefaultTableColumnModel();
	      for (int modelColumnNumber = 0; modelColumnNumber < this.getColumnCount(); modelColumnNumber++)
	      {
	    	  TableColumn column = new TableColumn(modelColumnNumber);
	         column.setHeaderValue(this.getActColumnName(modelColumnNumber));
	         if (
	        		 ColumnNames[modelColumnNumber].equals("Trans") ||
	        		 ColumnNames[modelColumnNumber].equals("Rank") ) {
	        	 column.setMaxWidth(-1);
	         } else if (
	        		 ColumnNames[modelColumnNumber].equals("ID") ) {
	        	 column.setMaxWidth(50);
	         }
	         columnModel.addColumn(column);
	      }
	      
	      this.table.setColumnModel(columnModel);
	   
	}	  

}
/*
class UpdateImmediately implements TableModelListener {
	
	public void tableChanged(TableModelEvent e) {
      //getInfoTextField().setValue(...);
      
      if (e.getType() == TableModelEvent.UPDATE)
      {
          int row = e.getFirstRow();
          int col = e.getColumn();
          //JTable table=;
          AbstractTableModel tmodel=(AbstractTableModel)e.getSource();//table.getModel();
          tmodel..setValueat("",0,0);
          if( tabel.getCellEditor(row,col).getCellEditorValue() != tmodel.getValueAt(row,col) )
		  {
				//Stac_Out.println("..the cells value changed");
          	tmodel.setValueAt(tabell.getCellEditor(row,col).getCellEditorValue(),row,col);
		  }
		  else{
				//Stac_Out.println("..value of the cell didnt change");
		  }
      }      
      
	}

	}
*/
class ReorientTableModel extends STAC_GUI_AbstractTableModel {
	  //TableModelListener updateImmediately= new UpdateImmediately();
	
//	  private String[] ColumnNames = {
//	      "V1", "V2", "Close", "Comment",};
//	  private String[] actColumnNames = null;
//	  private Vector[] Data = new Vector[ColumnNames.length];

	  @Override
	void initColumns() {
		  ColumnNames = new String [] {
			      "V1", "V2", "Close", "Comment",};
	  }
	  void initfill() {
//	    setValueAt("a*",0,0);setValueAt("b*",0,1);setValueAt(Boolean.FALSE,0,2);setValueAt("Standard alignment",0,3);
//	    setValueAt("a*",1,0);setValueAt("c*",1,1);setValueAt(Boolean.FALSE,1,2);setValueAt("Standard alignment",1,3);
//	    setValueAt("b*",2,0);setValueAt("a*",2,1);setValueAt(Boolean.FALSE,2,2);setValueAt("Standard alignment",2,3);
//	    setValueAt("b*",3,0);setValueAt("c*",3,1);setValueAt(Boolean.FALSE,3,2);setValueAt("Standard alignment",3,3);
//	    setValueAt("c*",4,0);setValueAt("a*",4,1);setValueAt(Boolean.FALSE,4,2);setValueAt("Standard alignment",4,3);
//	    setValueAt("c*",5,0);setValueAt("b*",5,1);setValueAt(Boolean.FALSE,5,2);setValueAt("Standard alignment",5,3);
	  }


	  /*
	   * Don't need to implement this method unless your table's
	   * data can change.
	   */
	  public void setValueAt(Object value, int row, int col) {
	  	if (value == "ClearRow") {
		      //clear the whole table
		      for (int i = 0; i < ColumnNames.length; i++)
		        Data[i].removeElementAt(row);
		      fireTableStructureChanged();
		    }
	  	else if (value == "ClearTable") {
		      //clear the whole table
		      for (int i = 0; i < ColumnNames.length; i++)
		        Data[i].removeAllElements();
		      fireTableStructureChanged();
		    }
	  	else if (value == "ActivateCells" && table!=null) {
	  		//check the table data
		    if (table.getCellEditor() != null) {
		        table.getCellEditor().stopCellEditing();
		    }
	  	}
	    else if (row >= 0 && row < getRowCount() && col >= 0 &&
	             col < getColumnCount()) {
	      //modify an element
	      //alignData[row][col] = value;
	      Data[col].setElementAt(value, row);
	      fireTableCellUpdated(row, col);
	    }
	    else if (row == getRowCount()) {
	      //insert a new row
	      for (int i = 0; i < ColumnNames.length; i++)
	    	  if (i==getColId("Close")) {
	    		  Data[i].addElement(Boolean.FALSE);
	    	  } else {
	    		  Data[i].addElement(value);
	    	  }
	        //fireTableRowsInserted(row,row);
	      fireTableStructureChanged();
	    }
	  }

	  public boolean isCellEditable(int row, int col){
		  if ("Standard alignment".compareToIgnoreCase((String)(getValueAt(row,getColId("Comment"))))==0 ) {
			  if (col==getColId("Close"))
				  return true;
			  else
				  return false;
		  }
		  else
			  return true;
	  }
	  
	   public void createTableColumnModel()
			   {
		      this.table.setAutoCreateColumnsFromModel(false);

		   	DefaultTableColumnModel columnModel= new DefaultTableColumnModel();
			      for (int modelColumnNumber = 0; modelColumnNumber < this.getColumnCount(); modelColumnNumber++)
			      {
			    	  TableColumn column = new TableColumn(modelColumnNumber);
			         column.setHeaderValue(this.getActColumnName(modelColumnNumber));
			         switch (modelColumnNumber)
			         {
			            case 0:
			            case 1:
			               column.setMinWidth(100);
			               column.setPreferredWidth(100);
			               break;
			            case 2:
			                column.setMinWidth(10);
			                column.setMaxWidth(100);
			                column.setPreferredWidth(50);
			                break;
			            case 3:
			               column.setMinWidth(200);
			               column.setPreferredWidth(300);
			               break;
			         }
			         columnModel.addColumn(column);
			      }

			      this.table.setColumnModel(columnModel);
			   }


	}

class TranslationTableModel extends STAC_GUI_AbstractTableModel {
	  //TableModelListener updateImmediately= new UpdateImmediately();
	  
	  public void initColumns() {
		  ColumnNames = new String[] {
	      "Angle", "X", "Y", "Z"};
	  }
	  
	  public void initfill(){};

	  /*
	   * Don't need to implement this method unless your table's
	   * data can change.
	   */
	  public void setValueAt(Object value, int row, int col) {
	  	if (value == "ClearRow") {
		      //clear the whole table
		      for (int i = 0; i < ColumnNames.length; i++)
		        Data[i].removeElementAt(row);
		      fireTableStructureChanged();
		    }
	  	else if (value == "ClearTable") {
		      //clear the whole table
		      for (int i = 0; i < ColumnNames.length; i++)
		        Data[i].removeAllElements();
		      fireTableStructureChanged();
		    }
	  	else if (value == "ActivateCells" && table!=null) {
	  		//check the table data
		    if (table.getCellEditor() != null) {
		        table.getCellEditor().stopCellEditing();
		    }
	  	}
	    else if (row >= 0 && row < getRowCount() && col >= 0 &&
	             col < getColumnCount()) {
	      //modify an element
	      //alignData[row][col] = value;
	      Data[col].setElementAt(value, row);
	      fireTableCellUpdated(row, col);
	    }
	    else if (row == getRowCount()) {
	      //insert a new row
	      for (int i = 0; i < ColumnNames.length; i++)
	        Data[i].addElement(value);
	        //fireTableRowsInserted(row,row);
	      fireTableStructureChanged();
	    }
	  }

	  public boolean isCellEditable(int row, int col){
		  return true;
	  }

	@Override
	void createTableColumnModel() {
		// TODO Auto-generated method stub
		
	}
	  
	}


class TranslationWidget extends JPanel 
implements STAC_GUI_Button_Panel, STAC_GUI_File_Chooser_Panel {

	StacBCM bcm;
	Util utils;
	String motorName="";
	ParamTable motorParams;
	
	JTextField jTFGTCInpP = new JTextField();	
	JButton jButtonGTC_mvP  = new JButton ();
	JTextField jTFGTCInpPX = new JTextField();	
	JTextField jTFGTCInpPY = new JTextField();	
	JTextField jTFGTCInpPZ = new JTextField();	
	JButton jButtonGTC_getP  = new JButton ();
	TranslationTableModel translationTableModel = new TranslationTableModel();
	JTable jTableTrans = new JTable(translationTableModel);
	JScrollPane jpanelReorientTable = new JScrollPane(jTableTrans);
	JButton jButtonGTC_addLine  = new JButton ("Add Actual");
	JButton jButtonGTC_delSelected  = new JButton ("Del Selected");
	JButton jButtonLoad = new JButton ("L");
	JButton jButtonSave = new JButton ("S");
	
    Dimension rbSize = new Dimension(100,30);
    Dimension rbhSize = new Dimension(50,30);
    Dimension rbdSize = new Dimension(200,30);
    Dimension fsbSize = new Dimension(30,30);
    Dimension fsdbSize = new Dimension(60,30);
    Dimension tfSize = new Dimension(1000,30);
    Dimension tfnSize = new Dimension(10,30);
	
	public TranslationWidget(StacBCM stacbcm,Util stacutil,String rotAxis) {
		//get init params
		bcm=stacbcm;
		utils=stacutil;
		motorName=rotAxis;
		
		//gui
		
	    setLayout(new BoxLayoutFixed(this,BoxLayoutFixed.PAGE_AXIS));

	    JPanel jPanelGTCInpP = new JPanel();
	    add(jPanelGTCInpP,null);
	    jPanelGTCInpP.setLayout(new BoxLayoutFixed(jPanelGTCInpP,BoxLayoutFixed.LINE_AXIS));
	    jPanelGTCInpP.add(Box.createRigidArea(new Dimension(10,10)));
	    JLabel jLabelGTCInpP = new JLabel();
	    jPanelGTCInpP.add(jLabelGTCInpP, null);
	    jLabelGTCInpP.setText(motorName+" translation (datum = only "+motorName+"):");
	    jLabelGTCInpP.setMinimumSize(rbdSize);
	    jLabelGTCInpP.setMaximumSize(rbdSize);
	    jLabelGTCInpP.setPreferredSize(rbdSize);
	    jPanelGTCInpP.add(Box.createRigidArea(new Dimension(10,10)));
	    jPanelGTCInpP.add(Box.createHorizontalGlue());
	    jPanelGTCInpP.add(jTFGTCInpP, null);
	    jTFGTCInpP.setToolTipText(motorName+" Angle Used");
	    jTFGTCInpP.setMinimumSize(tfnSize);
	    jTFGTCInpP.setMaximumSize(tfSize);
	    jTFGTCInpP.setPreferredSize(tfSize);
	    jTFGTCInpP.setText("180.0");
	    jButtonGTC_mvP.setText("Mv "+motorName);
	    jButtonGTC_mvP.setMinimumSize(rbSize);
	    jButtonGTC_mvP.setMaximumSize(rbSize);
	    jButtonGTC_mvP.setPreferredSize(rbSize);
	    jButtonGTC_mvP.setToolTipText("Set Datum = only "+motorName);
	    jButtonGTC_mvP.addActionListener(new Stac_Button_actionAdapter(this));
	    jButtonGTC_mvP.setEnabled(true);
	    jPanelGTCInpP.add(jButtonGTC_mvP,null);    
	    jPanelGTCInpP.add(jTFGTCInpPX, null);
	    jTFGTCInpPX.setToolTipText("X Component of "+motorName+" Translation");
	    jTFGTCInpPX.setMinimumSize(tfnSize);
	    jTFGTCInpPX.setMaximumSize(tfSize);
	    jTFGTCInpPX.setPreferredSize(tfSize);
	    jPanelGTCInpP.add(jTFGTCInpPY, null);
	    jTFGTCInpPY.setToolTipText("Y Component of "+motorName+" Translation");
	    jTFGTCInpPY.setMinimumSize(tfnSize);
	    jTFGTCInpPY.setMaximumSize(tfSize);
	    jTFGTCInpPY.setPreferredSize(tfSize);
	    jPanelGTCInpP.add(jTFGTCInpPZ, null);
	    jTFGTCInpPZ.setToolTipText("Z Component of "+motorName+" Translation");
	    jTFGTCInpPZ.setMinimumSize(tfnSize);
	    jTFGTCInpPZ.setMaximumSize(tfSize);
	    jTFGTCInpPZ.setPreferredSize(tfSize);
	    jButtonGTC_getP.setText("Get Trn.");
	    jButtonGTC_getP.setMinimumSize(rbSize);
	    jButtonGTC_getP.setMaximumSize(rbSize);
	    jButtonGTC_getP.setPreferredSize(rbSize);
	    jButtonGTC_getP.setToolTipText("Get the Current Translation");
	    jButtonGTC_getP.addActionListener(new Stac_Button_actionAdapter(this));
	    jButtonGTC_getP.setEnabled(true);
	    jPanelGTCInpP.add(jButtonGTC_getP,null);
	    jPanelGTCInpP.add(Box.createRigidArea(new Dimension(10,10)));
	    jPanelGTCInpP.add(Box.createHorizontalGlue());
	    
	    JPanel jPanelGTCTableP = new JPanel();
	    add(jPanelGTCTableP,null);
	    jPanelGTCTableP.setLayout(new BoxLayoutFixed(jPanelGTCTableP,BoxLayoutFixed.LINE_AXIS));
	    jPanelGTCTableP.add(Box.createRigidArea(new Dimension(10,10)));
	    jPanelGTCTableP.add(jpanelReorientTable, null);
	    translationTableModel.setTable(jTableTrans);
	    jPanelGTCTableP.add(Box.createRigidArea(new Dimension(10,10)));
	    JPanel jPanelGTCTablePB = new JPanel();
	    jPanelGTCTableP.add(jPanelGTCTablePB,null);
	    jPanelGTCTablePB.setLayout(new BoxLayoutFixed(jPanelGTCTablePB,BoxLayoutFixed.PAGE_AXIS));	    
	    //jPanelGTCTablePB.add(jButtonGTC_addLine,null);
	    jButtonGTC_addLine.setMinimumSize(rbSize);
	    jButtonGTC_addLine.setMaximumSize(rbSize);
	    jButtonGTC_addLine.setPreferredSize(rbSize);
	    jButtonGTC_addLine.setToolTipText("Adds the Currently Set Values to the Table");
	    jButtonGTC_addLine.addActionListener(new Stac_Button_actionAdapter(this));
	    jButtonGTC_addLine.setEnabled(true);
	    jPanelGTCTablePB.add(jButtonGTC_delSelected,null);
	    jButtonGTC_delSelected.setMinimumSize(rbSize);
	    jButtonGTC_delSelected.setMaximumSize(rbSize);
	    jButtonGTC_delSelected.setPreferredSize(rbSize);
	    jButtonGTC_delSelected.setToolTipText("Removes the Selected Rows from the Table");
	    jButtonGTC_delSelected.addActionListener(new Stac_Button_actionAdapter(this));
	    jButtonGTC_delSelected.setEnabled(true);
	    JPanel jPanelLoadSave = new JPanel();
	    jPanelGTCTablePB.add(jPanelLoadSave,null);
	    jPanelLoadSave.setLayout(new BoxLayoutFixed(jPanelLoadSave,BoxLayoutFixed.LINE_AXIS));
	    jPanelLoadSave.add(jButtonLoad,null);
	    jButtonLoad.setMinimumSize(rbhSize);
	    jButtonLoad.setMaximumSize(rbhSize);
	    jButtonLoad.setPreferredSize(rbhSize);
	    jButtonLoad.setToolTipText("Loads data from file to the Table");
	    jButtonLoad.addActionListener(new Stac_FileChooser(this));
	    jButtonLoad.setEnabled(true);
	    jPanelLoadSave.add(Box.createVerticalGlue());
	    jPanelLoadSave.add(jButtonSave,null);
	    jButtonSave.setMinimumSize(rbhSize);
	    jButtonSave.setMaximumSize(rbhSize);
	    jButtonSave.setPreferredSize(rbhSize);
	    jButtonSave.setToolTipText("Saves the Table data to file");
	    jButtonSave.addActionListener(new Stac_FileChooser(this));
	    jButtonSave.setEnabled(true);
	    jPanelGTCTablePB.add(Box.createVerticalGlue());
	    jPanelGTCTableP.add(Box.createRigidArea(new Dimension(10,10)));
	    
	}

	public Vector getMeasuredData() {
		Vector data = new Vector();
	    int[] vecIDs = jTableTrans.getSelectedRows();
	    TranslationTableModel tmodel = (TranslationTableModel) jTableTrans.getModel();
	    tmodel.setValueAt("ActivateCells",0,0);
	    try {
	    	if (vecIDs.length!=0) {
	    		for (int i=0;i<vecIDs.length;i++) {
	    			AxisAngle4d d = new AxisAngle4d();
	    			d.x    =new Double((String)jTableTrans.getValueAt(vecIDs[i],tmodel.getColId("X"))).doubleValue();
	    			d.y    =new Double((String)jTableTrans.getValueAt(vecIDs[i],tmodel.getColId("Y"))).doubleValue();
	    			d.z    =new Double((String)jTableTrans.getValueAt(vecIDs[i],tmodel.getColId("Z"))).doubleValue();
	    			d.angle=new Double((String)jTableTrans.getValueAt(vecIDs[i],tmodel.getColId("Angle"))).doubleValue();
	    			data.addElement(d);
	    		}
	    	} else {
	    		for (int i=0;i<jTableTrans.getRowCount();i++) {
	    			AxisAngle4d d = new AxisAngle4d();
	    			d.x    =new Double((String)jTableTrans.getValueAt(i,tmodel.getColId("X"))).doubleValue();
	    			d.y    =new Double((String)jTableTrans.getValueAt(i,tmodel.getColId("Y"))).doubleValue();
	    			d.z    =new Double((String)jTableTrans.getValueAt(i,tmodel.getColId("Z"))).doubleValue();
	    			d.angle=new Double((String)jTableTrans.getValueAt(i,tmodel.getColId("Angle"))).doubleValue();
	    			data.addElement(d);
	    		}    	
	    	}
	    }catch(Exception e) {
	    }
	    return data;
	}

	/* (non-Javadoc)
	 * @see stacgui.STAC_GUI_Button_Panel#Button_actionAdapter(java.awt.event.ActionEvent)
	 */
	public void Button_actionAdapter(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
	        Stac_Out.printTimeln("USER ACTION: Button pressed (Translation: "+this.motorName+" - "+((JButton)e.getSource()).getText()+")");			
		}
		// TODO Auto-generated method stub
		if (e.getSource()==this.jButtonGTC_delSelected) {
	        int numOfVecs = this.jTableTrans.getSelectedRowCount();
	        int[] vecIDs = this.jTableTrans.getSelectedRows();
	        TranslationTableModel tmodel = (TranslationTableModel) this.jTableTrans.getModel();
	        for(int i=vecIDs.length-1;i>=0;i--){
	            tmodel.setValueAt("ClearRow",vecIDs[i],vecIDs[i]);
	        }
	    } else if (e.getSource()==this.jButtonGTC_mvP) {
	    	double desiredAng=new Double(jTFGTCInpP.getText()).doubleValue();
	    	if (motorName.compareToIgnoreCase("Phi")==0)
	    		bcm.moveToDatum(0,0,desiredAng);
	    	else if (motorName.compareToIgnoreCase("Kappa")==0)
	    		bcm.moveToDatum(0,desiredAng,0);
	    	else {
	    		bcm.moveToDatum(0,0,0);
	    		bcm.moveMotor(motorName,desiredAng);
	    	}
	    	double ang=bcm.getMotorPosition(motorName);
	    	if (Math.abs((ang-desiredAng))>1e-1) {
	    		//error
	    	} else {
				jTFGTCInpP.setText(""+new PrintfFormat("%.4f").sprintf(ang));    		
				jTFGTCInpPX.setText("");
				jTFGTCInpPY.setText("");
				jTFGTCInpPZ.setText("");
	    	}
	    } else if (e.getSource()==this.jButtonGTC_getP) {
			Point3d trn= new Point3d();
			Point3d dat= new Point3d();
			bcm.getCurrentDatumTrans(dat,trn,4);
			double actVal =motorName.compareToIgnoreCase("Phi")==0?dat.z:(motorName.compareToIgnoreCase("Kappa")==0?dat.y:0.0);
			double zeroVal=motorName.compareToIgnoreCase("Phi")==0?dat.y:(motorName.compareToIgnoreCase("Kappa")==0?dat.z:0.0);
			if (Math.abs(utils.angleDegreeDiff(actVal,new Double(jTFGTCInpP.getText()).doubleValue()))<=1e-1 && Math.abs(utils.angleDegreeDiff(0,zeroVal))<=1e-1) {
				jTFGTCInpPX.setText(""+trn.x);//new PrintfFormat("%f").sprintf(trn.x));
				jTFGTCInpPY.setText(""+trn.y);//new PrintfFormat("%f").sprintf(trn.y));
				jTFGTCInpPZ.setText(""+trn.z);//new PrintfFormat("%f").sprintf(trn.z));
//			    } else if (e.getSource()==this.jButtonGTC_addLine) {
				addline(jTFGTCInpP.getText(),jTFGTCInpPX.getText(),jTFGTCInpPY.getText(),jTFGTCInpPZ.getText());
			} else {
				//error
			}
	    }		
	}

	public void addline (String ang, String x, String y, String z) {
		int rowct=this.jTableTrans.getRowCount();
		TranslationTableModel tmodel = (TranslationTableModel) this.jTableTrans.getModel();
		this.jTableTrans.setValueAt(ang, rowct, tmodel.getColId("Angle"));
		this.jTableTrans.setValueAt(x, rowct, tmodel.getColId("X"));
		this.jTableTrans.setValueAt(y, rowct, tmodel.getColId("Y"));
		this.jTableTrans.setValueAt(z, rowct, tmodel.getColId("Z"));
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
		int returnVal = (e.getSource() == jButtonLoad)?fc.showOpenDialog(this):fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		}
		if(file==null){
			return;
		}
		if (e.getSource() instanceof JButton) {
	        Stac_Out.printTimeln("USER ACTION: Button pressed (Fileselect: "+file.getAbsolutePath()+" - "+((JButton)e.getSource()).getText()+")");			
	        activeFileSelectionDir=file.getParentFile();
		}
		//do action
		if (e.getSource() == jButtonLoad) {
			try {
				FileReader fr =new FileReader(file);
				double ang,x,y,z;
				BufferedReader buff = new BufferedReader(fr);
		        boolean eof = false;
		        while(!eof)
		        {
		                  String line = buff.readLine();
		                  if (line == null)
		                  {
		                      eof = true;
		                      break;
		                  }
		                  else
		                  {
		                      //System.out.println(line);
		                      StringTokenizer st = new StringTokenizer(line);
		                      ang = Double.parseDouble(st.nextToken());
		                      x = Double.parseDouble(st.nextToken());
		                      y = Double.parseDouble(st.nextToken());
		                      z = Double.parseDouble(st.nextToken());
		                      addline(""+ang, ""+x, ""+y, ""+z);
		                  }
				}
			}catch(Exception ex){
				Stac_Out.println("Error loading the file: "+file.getAbsolutePath());
			}			
		} else if (e.getSource() == jButtonSave) {
			try {
				FileWriter fw = new FileWriter(file);
				int rowct=this.jTableTrans.getRowCount();
				TranslationTableModel tmodel = (TranslationTableModel) this.jTableTrans.getModel();
				for (int i=0;i<rowct;i++) {
					String str="";
					str+=this.jTableTrans.getValueAt(i, tmodel.getColId("Angle"))+" ";
					str+=this.jTableTrans.getValueAt( i, tmodel.getColId("X"))+" ";
					str+=this.jTableTrans.getValueAt( i, tmodel.getColId("Y"))+" ";
					str+=this.jTableTrans.getValueAt( i, tmodel.getColId("Z"))+"\n";
					fw.write(str);
				}
				fw.flush();
				fw.close();
			}catch(Exception ex){
				Stac_Out.println("Error saving the file: "+file.getAbsolutePath());				
			}			
		}
	}
	
}

