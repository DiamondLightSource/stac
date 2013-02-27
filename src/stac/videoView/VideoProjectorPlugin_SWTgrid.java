package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

import javax.media.opengl.GL;

import org.eclipse.swt.SWT;
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

import stac.core.Stac_Out;
import stac.videoView.VideoProjectorPlugin.VideoDisplay;


public class VideoProjectorPlugin_SWTgrid extends VideoProjectorPlugin {



	class VideoDisplay_SWTgrid extends VideoDisplay {


		Display display;
		Shell shell;
		//Composite comp;
		Composite mycomp;
		Canvas canvas;
		BufferedImage imageAct;
		long imageActCt=0;
		long imageLastDisplayed=0;

		public VideoDisplay_SWTgrid(VideoProjectorPlugin vtk, VideoReceiverSlot vr) {
			super(vtk, vr);
			display=((VideoProjectorPlugin_SWTgrid)vtk).display;
			shell=((VideoProjectorPlugin_SWTgrid)vtk).shell;
			comp=((VideoProjectorPlugin_SWTgrid)vtk).comp;
			mycomp=((VideoProjectorPlugin_SWTgrid)vtk).mycomp;
		}

		/** 
		 * updates the screen to follow the video stream
		 */
		long prerenderStreaming(){


			//JOGL Texture on SWT Canvas
			StampedImage simage=vr.getActImage();
			imageActCt=simage.imageCt;
			imageAct=simage.img;
			long lostimg=0;
			if (imageActCt>0) {
				if (imageActCt>imageLastDisplayed+1) {
					lostimg=imageActCt-1-imageLastDisplayed;
					Stac_Out.println(vr.getTitle()+": "+(lostimg)+" lost image(s) from "+(imageLastDisplayed+1)+" to "+(imageActCt-1));
				}
				imageLastDisplayed=imageActCt;
			}
			if (imageAct!=null) {
				//t = createTexture(imageAct);
			}
			return lostimg;
		}
		
		Image img=null;
		ImageData idat;

		void renderStreaming(){

			//JOGL Texture on SWT Canvas
			if (imageAct!=null && canvas!=null && !canvas.isDisposed()) {
				if (img!=null && !img.isDisposed()) {
					img.dispose();
				}

				long timeInMillis = System.currentTimeMillis();
				
				idat=convertToSWT(imageAct);
				img = new Image(display,idat);
				
				long time2InMillis = System.currentTimeMillis();				
				//Stac_Out.println(""+(time2InMillis-timeInMillis));
				
				Rectangle clientArea = canvas.getClientArea();
				if (img!=null) {
					GC gc= new GC(canvas);
					gc.drawImage(img, 0, 0, img.getBounds().width, img.getBounds().height, 0, 0, clientArea.width, clientArea.height);
					gc.dispose();
				}
				
			}
			return;
		}


		/** 
		 * Creates the graphical interface components and initialises them
		 */
		void initGUI(ArrayList<Object> params){
			canvas=(Canvas)(params.get(0));
			canvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					Rectangle clientArea = canvas.getClientArea();
					//e.gc.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
//					if (img!=null)
//						e.gc.drawImage(img, 0, 0, img.getBounds().width, img.getBounds().height, 0, 0, clientArea.width, clientArea.height);
				}
			}); 

		}

	}


	Display display;
	Shell shell;
	Composite comp;


	public VideoDisplay createVideoDisplay(VideoReceiverSlot vr) {
		VideoDisplay_SWTgrid vd = new VideoDisplay_SWTgrid(this,vr);
		final Canvas canvas = new Canvas(mycomp,SWT.NONE);
		GridData gdata = new GridData(SWT.FILL,SWT.FILL,true, true);
		//canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		canvas.setLayoutData(gdata);
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(canvas);
		vd.initGUI(params);
		return vd;
	}

	public VideoDisplay addDisplay(VideoReceiverSlot vr) {
		VideoDisplay vs= createVideoDisplay(vr);
		int it=displayList.size()+1;
		int gx=(int)Math.ceil(Math.sqrt(it));
		int gy=(int)Math.ceil(((double)it)/gx);
		gridLayout.numColumns = gy;
		displayList.add(vs);
		
		mycomp.layout(true, true);
		
		return vs;
	}
	
	
	ArrayList<Object> vr ;
	boolean stop=false;
	
	public VideoProjectorPlugin_SWTgrid(ArrayList<Object> params) {
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
	}

	/** 
	 * updates the screen to follow the video stream
	 */
	public void registerRendering(){

		//display.asyncExec(new Runnable() {
		//display.syncExec(new Runnable() {
		display.timerExec(500,new Runnable() {
			int rot = 0;
			int timerMax=1000;
			int timer=timerMin;
			public void run() {
				if (!mycomp.isDisposed()) {
					long lostimg=0;
					for (int i=0; i<displayList.size();i++) {
						lostimg=Math.max(lostimg, ((VideoDisplay)(displayList.get(i))).prerenderStreaming());
					}


					for (int i=0; i<displayList.size();i++) {
						((VideoDisplay)(displayList.get(i))).renderStreaming();
					}

					//display.asyncExec(this);
					//display.syncExec(this);
					if (lostimg>0) {
						timer/=2;
					} else {
						timer=Math.min(timerMax,timer+10);
					}
					timer=Math.max(timerMin,timer);
					//Stac_Out.println(""+timer);
					display.timerExec(timer,this);
				}
			}
		});



	}


	boolean hoverstate=false;		

	GridLayout gridLayout;
	Composite mycomp;
	
	/** 
	 * Creates the graphical interface components and initialises them
	 */
	public void initGUI(){
	    gridLayout = new GridLayout();
	    gridLayout.numColumns = 1;
	    gridLayout.makeColumnsEqualWidth = true;
	    
	    mycomp= new Composite(comp, SWT.NONE|SWT.FILL);
	    mycomp.setLayout(gridLayout);
		mycomp.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				mycomp.layout(true, true);
			}
		}); 
		
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
	static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage
			.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0],
							pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
			int w=bufferedImage.getWidth(),h=bufferedImage.getHeight();
			int [] rgbs= new int[w*h];
			rgbs=bufferedImage.getRGB(0, 0, w, h, rgbs, 0, w);

			DirectColorModel colorModel=(DirectColorModel)ColorModel.getRGBdefault();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(w,
					h, colorModel.getPixelSize(),
					palette);
			//	              WritableRaster raster = bufferedImage.getRaster();
			//	              int[] pixelArray = new int[3];
			for (int y = 0,ct=0; y < h; y++) {
				for (int x = 0; x < w; x++,ct++) {
					//	                  raster.getPixel(x, y, pixelArray);
					int pixel = rgbs[ct];
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
			.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}	        


}
