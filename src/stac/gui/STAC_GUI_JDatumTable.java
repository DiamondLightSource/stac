package stac.gui;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.TableModel;

import stac.core.Stac_Configuration;

import stac.gui.JMultiLineToolTip;

public class STAC_GUI_JDatumTable extends STAC_GUI_JTable {
	DatumTableModel datumTableModel = null;
	
	public STAC_GUI_JDatumTable(TableModel tm,DatumTableModel datumTableModel) {
		super(tm);
		this.datumTableModel= datumTableModel;
	}
	
	DatumTableModel getDatumTableModel() {
		return datumTableModel;
	}
	
}
