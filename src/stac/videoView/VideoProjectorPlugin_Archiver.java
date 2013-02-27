package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import stac.core.PrintfFormat;
import stac.core.Stac_Out;


public class VideoProjectorPlugin_Archiver extends VideoProjectorPlugin {


	int dispct=0;

	class VideoDisplay_Archiver extends VideoDisplay {



		JPanel panel;
		JLabel label;

		BufferedImage imageAct;
		long imageActCt=0;
		long imageLastDisplayed=0;
		long lostimg=0;

		public VideoDisplay_Archiver(VideoProjectorPlugin vtk, VideoReceiverSlot vr) {
			super(vtk, vr);
			panel=((VideoProjectorPlugin_Archiver)vtk).panel;
			//default framerate
			//setTimerMin(500);
		}

		/** 
		 * updates the screen to follow the video stream
		 */
		long prerenderStreaming(){


			//JOGL Texture on SWT Canvas
			StampedImage simage=vr.getActImage();
			imageActCt=simage.imageCt;
			imageAct=simage.img;
			lostimg=imageActCt-1-imageLastDisplayed;
			if (imageActCt>0) {
				if (imageActCt>imageLastDisplayed+1) {
					Stac_Out.println(vr.getTitle()+": "+(lostimg)+" lost image(s) from "+(imageLastDisplayed+1)+" to "+(imageActCt-1));
				}
				imageLastDisplayed=imageActCt;
			}
			if (imageAct!=null) {
				//t = createTexture(imageAct);
			}
			return lostimg;
		}

		//save counter
		int ict=0;
		
		void renderStreaming(){

			//JOGL Texture on SWT Canvas
			if (imageAct!=null && label.getGraphics()!=null) {
                label.getGraphics().drawImage(imageAct, 0, 0, label.getWidth(), label.getHeight(), 0, 0, imageAct.getWidth(), imageAct.getHeight(), null);
                //label.getGraphics().drawImage(imageAct, 0, 0, imageAct.getWidth(), imageAct.getHeight(), null);

                //and then save it to a file
                //if this img has not yet been saved
                //worth checking when Receivers with different refresh rate are running parallel
                if (lostimg>=0) {
                	String outdir="savedProjector";
                	String fname = new PrintfFormat(outdir+"/"+vr.getTitle()+"_"+dispct+"_%04d.jpg").sprintf(ict++);

                	if (ict==100000) ict=0;
                	FileOutputStream fo;
                	try {
                		File oDir= new File(outdir);
                		oDir.mkdirs();						
                		fo = new FileOutputStream(fname);
                		javax.imageio.ImageIO.write(imageAct, "jpg", fo);
                		fo.flush();
                		fo.close();
                	} catch (Exception e) {
                		// TODO Auto-generated catch block
                	}
                }
			}
			return;
		}


		/** 
		 * Creates the graphical interface components and initialises them
		 */
		void initGUI(ArrayList<Object> params){
			label=(JLabel)(params.get(0));


		}

	}


JFrame f;
JPanel panel;
GridLayout grid;


	public VideoDisplay createVideoDisplay(VideoReceiverSlot vr) {
		VideoDisplay_Archiver vd = new VideoDisplay_Archiver(this,vr);
		JLabel label = new JLabel();
		panel.add(label);
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(label);
		vd.initGUI(params);
		return vd;
	}

	public VideoDisplay addDisplay(VideoReceiverSlot vr) {
		VideoDisplay vs= createVideoDisplay(vr);
		int it=displayList.size()+1;
		int gx=(int)Math.ceil(Math.sqrt(it));
		//int gy=(int)Math.ceil(((double)it)/gx);
		grid.setColumns(0);
		grid.setRows(gx);//(gx-1+gy-1>0)?gy-1:1);
		displayList.add(vs);
		panel.setVisible(false);
		panel.setVisible(true);
		return vs;
	}
	
	
	ArrayList<Object> vr ;
	boolean stop=false;
	
	public VideoProjectorPlugin_Archiver(ArrayList<Object> params) {
		if (!(params.get(0) instanceof VideoReceiverSlot)) {
			panel=(JPanel)params.get(0);
			try {
				f=(JFrame)params.get(1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			vr=params;
	        f = new JFrame();
	        panel = new JPanel();
	        f.getContentPane().setLayout(new java.awt.GridLayout(0,1));
	        f.getContentPane().add(panel);
	        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        f.setVisible(true);
	        f.setSize(640,480);       
	        f.setTitle("VideoView Test");
	        f.addWindowListener(new WindowListener() {
				
				@Override
				public void windowOpened(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void windowIconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void windowDeiconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void windowDeactivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void windowClosing(WindowEvent e) {
					// TODO Auto-generated method stub
					stop = true;
				}
				
				@Override
				public void windowClosed(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void windowActivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}

	/** 
	 * updates the screen to follow the video stream
	 */
	public void registerRendering(){


		//display.asyncExec(new Runnable() {
		//display.syncExec(new Runnable() {
		Thread refresh=new Thread() {
			int timerMax=1000;
			int timer=timerMin;
			public void run() {
				do {
					try {
						sleep(timer);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					long lostimg=0;
					for (int i=0; i<displayList.size();i++) {
						lostimg=Math.max(lostimg, ((VideoDisplay)(displayList.get(i))).prerenderStreaming());
					}

					for (dispct=0; dispct<displayList.size();dispct++) {
						((VideoDisplay)(displayList.get(dispct))).renderStreaming();
					}

					if (lostimg>0) {
						timer/=2;
					} else {
						timer=Math.min(timerMax,timer+10);
					}
					timer=Math.max(timerMin,timer);
					//Stac_Out.println(""+timer);
				} while (!stop);
			}
		};
		refresh.start();


	}


	boolean hoverstate=false;		


	/** 
	 * Creates the graphical interface components and initialises them
	 */
	public void initGUI(){
		grid=new GridLayout(1, 1);
		panel.setLayout(grid);
		//panel.setPreferredSize(f.getSize());
//		panel.setLayout(new FlowLayout());
	}

	@Override
	public void test(VideoProjectorSlot vcs) {
		
		
		for(int i = 0; i<vr.size();i++) {
			vcs.addDisplay((VideoReceiverSlot)vr.get(i));
		}
		
		while (!stop) { 
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
