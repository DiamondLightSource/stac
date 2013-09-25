package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import stac.videoView.VideoProjectorPlugin.VideoDisplay;


public class VideoProjectorPlugin_SWTemb extends VideoProjectorPlugin {

	VideoProjectorPlugin_SWING emb;
	
	@Override
	public VideoDisplay createVideoDisplay(VideoReceiverSlot vr) {
		return emb.createVideoDisplay(vr);
	}

	public VideoDisplay addDisplay(VideoReceiverSlot vr) {
		return emb.addDisplay(vr);
	}

	
	@Override
	public void initGUI() {
		emb.initGUI();		
	}

	@Override
	public void registerRendering() {
		emb.registerRendering();		
	}

	@Override
	public void test(VideoProjectorSlot vcs) {
		shell.setText("VideoView Test");
		shell.setSize(640, 480);
		shell.open();

		for(int i = 0; i<vr.size();i++) {
			vcs.addDisplay((VideoReceiverSlot)vr.get(i));
		}
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		
	}

	Display display;
	Shell shell;
	Composite comp;
	ArrayList<Object> vr ;
	boolean stop=false;
	Frame f;
	JPanel panel;

	public VideoProjectorPlugin_SWTemb(ArrayList<Object> params) {
		if (!(params.get(0) instanceof VideoReceiverSlot)) {
			display=(Display)params.get(0);
			shell=(Shell)params.get(1);
			comp=(Composite)params.get(2);
		} else {
			vr=params;
			display = new Display();
			shell = new Shell(display);
			shell.setLayout(new FillLayout());
			comp = new Composite(shell, SWT.NONE);
			comp.setLayout(new FillLayout());
		}
		
		Composite videoComposite = new Composite(comp, 
                SWT.EMBEDDED | SWT.BORDER | SWT.NO_BACKGROUND | SWT.FILL);
        videoComposite.setLayout(new GridLayout(1,true));
        videoComposite.setLayoutData(new GridData(
                SWT.FILL, SWT.FILL, true, true));

        //CLabel videoImage = new CLabel(videoComposite, SWT.SHADOW_IN);
        //videoImage.setImage(dummyVideo);

        f = SWT_AWT.new_Frame(videoComposite);

        panel = new JPanel();
        f.setVisible(true);
        f.add(panel);
        //f.setSize(640,480);       
        
        ArrayList<Object> newparams= new ArrayList<Object>();
		newparams.add(panel);
		newparams.add(f);
		emb= new VideoProjectorPlugin_SWING(newparams);
	}


}
