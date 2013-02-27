package stac.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JToolTip;

import stac.core.Stac_Configuration;
import stac.core.Stac_Out;

import stac.gui.JMultiLineToolTip;

public class STAC_GUI_JButton extends JButton implements STAC_GUI_Widget {
	Stac_Configuration config=null;
	String [] confID=null;
	
	public STAC_GUI_JButton(String text,Stac_Configuration config) {
		super(text);
		setConfig(config);
	}
	
	public void setConfig(Stac_Configuration config) {
		if (config==null)
			return;
     	if (confID==null) {
    		//get instance specific IDs
    		confID=new String[2];
    		confID[0]=this.getText();
    		confID[0]=confID[0].replaceAll(" ", "_");
    		confID[1]=confID[0].concat("_TOOLTIP");
    	}
    	
    	//read actual config foreach ID

    	String newtext=null;
    	//button text
    	newtext=config.Descriptor.getConcatenatedStringValue(confID[0]);
    	if (newtext!=null && newtext.length()>0) {
    		this.setText(newtext);
    	}
    	//tooltip text
    	newtext=config.Descriptor.getConcatenatedStringValue(confID[1]);
    	if (newtext!=null && newtext.length()>0) {
    		this.setToolTipText(newtext);
    	}
	}
	
    public JToolTip createToolTip()
    {
            return new JMultiLineToolTip();
    }
    
}
