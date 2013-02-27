package stac.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.TableModel;

import stac.core.Stac_Configuration;

import stac.gui.JMultiLineToolTip;

public class STAC_GUI_JTable extends JTable {
	
	public STAC_GUI_JTable(TableModel tm) {
		super(tm);
	}
	
    public JToolTip createToolTip()
    {
            return new JMultiLineToolTip();
    }
	
}
