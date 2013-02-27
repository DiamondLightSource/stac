package stac.gui;

import stac.core.*;
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

import java.util.GregorianCalendar;
import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


public class MotorWidget_actionAdapter implements java.awt.event.ActionListener, 
                                           javax.swing.event.ChangeListener {
	  MotorWidget adaptee;

	  MotorWidget_actionAdapter(MotorWidget adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
		        Stac_Out.printTimeln("USER ACTION: Button pressed ("+adaptee.motorName+" - "+((JButton)e.getSource()).getText()+")");			
		} else if (e.getSource() instanceof JCheckBox) {
		        Stac_Out.printTimeln("USER ACTION: CheckBox pressed ("+adaptee.motorName+" - "+((JCheckBox)e.getSource()).getText()+" : "+((JCheckBox)e.getSource()).isSelected()+")");					
		}
		  
		if (e.getSource()==adaptee.title) {
		   	//reactivate
	        //Stac_Out.println("USER ACTION: Button pressed ("+adaptee.motorName+" - reactivate)");
		   	adaptee.activate(adaptee.motorName,adaptee.rotMotor,adaptee.bcm);
		} else if (e.getSource()==adaptee.goLeft) {
	    	//go Left
	        //Stac_Out.println("USER ACTION: Button pressed ("+adaptee.motorName+" - goLeft)");
	    	//adaptee.changePosition(adaptee.mvThere.getValue()-(new Double(adaptee.stepSize.getText()).doubleValue()),false);
	    	adaptee.changePosition(-(new Double(adaptee.stepSize.getText()).doubleValue()),true);
	    } else if (e.getSource()==adaptee.goRight) {
	    	//go Right
	        //Stac_Out.println("USER ACTION: Button pressed ("+adaptee.motorName+" - goRight)");
	    	//adaptee.changePosition(adaptee.mvThere.getValue()+(new Double(adaptee.stepSize.getText()).doubleValue()),false);
	    	adaptee.changePosition(+(new Double(adaptee.stepSize.getText()).doubleValue()),true);
	    } else if (e.getSource()==adaptee.goThere) {
	    	//go To
	        //Stac_Out.println("USER ACTION: Button pressed ("+adaptee.motorName+" - goTo)");
	    	adaptee.changePosition((new Double(adaptee.whereToGo.getText()).doubleValue()),false);
	    } else if (e.getSource()==adaptee.getPos) {
	    	//get Pos
	        //Stac_Out.println("USER ACTION: Button pressed ("+adaptee.motorName+" - getPos)");
	    	adaptee.getPosition();
	    }
	  }
	  public void stateChanged(ChangeEvent e) {
	        JSlider source = (JSlider)e.getSource();
	        Stac_Out.printTimeln("USER ACTION: Slider changed ("+adaptee.motorName+")");
	        if (!source.getValueIsAdjusting()) {
	        	//go To
		    	adaptee.changePosition((double)source.getValue(),false);
		    	//trick to force the update of the slider
		    	source.setInverted(!source.getInverted());
		    	source.setInverted(!source.getInverted());
	        }
	  }
	  public void monitorPosition(double newPos) {
		  adaptee.monitorPosition(newPos);
	  }
}


