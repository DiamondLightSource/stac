package stac.gui;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.io.PrintStream;
import java.util.Enumeration;

import javax.swing.BoxLayout;

public class BoxLayoutFixed extends javax.swing.BoxLayout {

	private int MAX_RETRIES = 20;

	public BoxLayoutFixed(Container target, int axis) {
		super(target,axis);
	}
	
	public void layoutContainer(Container target) {

		NullPointerException ex1 = null;
		int retryCount = 0;
		boolean bad=true;
		while (retryCount < MAX_RETRIES && bad==true) {
		try {

			super.layoutContainer(target);		
			bad=false;

		} catch (NullPointerException ex) {

			retryCount++;
			System.out.println("retryCount == " + retryCount);
			ex1=ex;
			Thread sleep= new Thread();
			sleep.start();
			try {
				sleep.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			sleep.stop();
			
		}
		}
		if (bad) {
			throw ex1;
		}

	}
}

//public class BoxLayoutFixed extends FlowLayout {
//
//	private int MAX_RETRIES = 20;
//	int axis;
//	public static int LINE_AXIS = BoxLayout.LINE_AXIS;
//	public static int PAGE_AXIS = BoxLayout.PAGE_AXIS;
//
//	public BoxLayoutFixed(Container target, int axis) {
//		super();
//		this.axis=axis;
//		if (axis==BoxLayout.LINE_AXIS) {
//			//this.
//		}
//		//super(target,axis);
//	}
//	
//	
//	
//}

