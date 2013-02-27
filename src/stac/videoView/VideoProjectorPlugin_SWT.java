package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.glu.GLU;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import stac.core.Stac_Out;

import au.edu.jcu.v4l4j.V4L4JConstants;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;


public class VideoProjectorPlugin_SWT extends VideoProjectorPlugin {

	abstract class VideoComposite {
		boolean hoverstate=false;		
		ArrayList<VideoDisplay> displays=new ArrayList<VideoDisplay>();

		VideoComposite() {
			
		}
		abstract public void addDisplay(VideoDisplay vd);
		abstract public void removeDisplay(VideoDisplay vd);
		abstract public void draw(GL gl);
		abstract public void handleEvent(Event e);
		abstract public boolean inMotion();
	}
	
	class VideoThumb extends VideoComposite {
	
		@Override
		public void addDisplay(VideoDisplay vd) {
			displays.add(vd);
			
		}

		float oldarot=0;
		float rot=0,arot=0,drot=0;
		float maxdrot=5;
		float zoom=0;
		float tilt=10f;
		
		@Override
		public void draw(GL gl) {
			//acceleration
			if (drot!=0) { //in motion
				if (drot*arot>0) {//speed up
					drot*=1.01;
					if (Math.abs(drot)>maxdrot)
						drot=maxdrot*((drot<0)?-1:1);
				} else if (arot==0) {//slow down
					drot*=0.9;
					if (drot*drot<0.01)
						drot=0;
				} else //halt
					drot=0;
			} else //idle
				drot=arot;
			//apply velocity
			rot+=drot;
			
			int ct=displays.size();
			float radius=ct*1.8f;
			//float cost=(float)Math.cos(tilt);
			//float sint=(float)Math.sin(tilt);
			
			gl.glLoadIdentity();
			
			//special effect
			//gl.glScalef(0.5f,0.5f, 1f);
			//move to its final place
			//gl.glTranslatef(0.0f, -5f, (float)(-10-ct));
			gl.glTranslatef(0.0f, 0f, (float)(-10-radius+zoom));
			//tilt it
			gl.glRotatef(tilt, 1f, 0f, 0f);
			//rotate thumbs together
			gl.glRotatef(rot, 0f, 1, 0.5f);
			for (int i=0; i<ct;i++) {
				//rotate the model after drawing the new Display
				gl.glRotatef(360f/(float)ct, 0f, 1, 0.5f);
				//move the Display to the circular path
				gl.glTranslatef(0.0f, 0.0f, radius);
				//draw the Display
				((VideoDisplay)(displays.get(i))).renderStreaming();
				//translate back the cursor
				gl.glTranslatef(0.0f, 0.0f, -radius);
			}
		}

		@Override
		public void handleEvent(Event e) {
			if (e.type==SWT.MouseHover) {


				hoverstate=true;
				if (e.y>canvas.getClientArea().height*0.8) {
					if (e.x> canvas.getClientArea().width*0.7) {
						Stac_Out.println("HOVER scroll right");
						arot=1;
					} else if (e.x< canvas.getClientArea().width*0.3) {
						Stac_Out.println("HOVER scroll left");
						arot=-1;
					} else {
						Stac_Out.println("HOVER low");
					}
				} else {
					Stac_Out.println("HOVER");
					
				}
			} else if (e.type==SWT.MouseMove) {
				if (hoverstate) {
					hoverstate=false;
					Stac_Out.println("HOVER Finished");	
					arot=0;
				} else {
					Stac_Out.println("MOVE");
				}
			} else if (e.type==SWT.MouseDown) {
				if (hoverstate) {
					//hoverstate=false;
					Stac_Out.println("Down in HOVER");
					oldarot=arot;
					arot=0;
					drot=0;
				} else {
					Stac_Out.println("DOWN");
				}
			} else if (e.type==SWT.MouseUp) {
				if (hoverstate) {
					//hoverstate=false;
					Stac_Out.println("Up in HOVER");	
					arot=oldarot;
				} else {
					Stac_Out.println("DOWN");
				}
			} else if (e.type==SWT.MouseWheel) {
				if (hoverstate) {
					Stac_Out.println("Wheel in HOVER");	
				} else {
					Stac_Out.println("Wheel");
					zoom=Math.min(Math.max(zoom+e.count/2f,-20), 7);
				}
			} else if (e.type==SWT.KeyDown) {
				if (arot!=0) {
					Stac_Out.println("Key during motion");	
				} else {
					Stac_Out.println("Key");
					int val=e.keyCode-SWT.KEYPAD_0;
					if (val>0 && val<10) {
						rot=360f/(float)(displays.size())*(val-1);
					} else if (e.keyCode==SWT.KEYPAD_SUBTRACT) {
						val=(int)((((int)rot%360+360)%360)/(360f/(float)displays.size())+0.9);
						rot=360f/(float)(displays.size())*(val-1);						
					} else if (e.keyCode==SWT.KEYPAD_ADD) {
						val=(int)((((int)rot%360+360)%360)/(360f/(float)displays.size())+0.1);
						rot=360f/(float)(displays.size())*(val+1);						
					} else if (e.keyCode==SWT.KEYPAD_MULTIPLY) {
						tilt+=1f;						
					} else if (e.keyCode==SWT.KEYPAD_DIVIDE) {
						tilt-=1f;						
					} else if (e.keyCode==SWT.KEYPAD_DECIMAL) {
						tilt=10f;						
					}
				}

			}

		}

		@Override
		public void removeDisplay(VideoDisplay vd) {
			displays.remove(vd);			
		}

		@Override
		public boolean inMotion() {
			return (drot!=0);
		}
		
	}

	class VideoDisplay_SWT extends VideoDisplay {

		Texture t=null;

		void drawFrame(GL gl) {

			if (t != null) {
				xoffset=0;
				TextureCoords tc = t.getImageTexCoords();		
				float tx1 = tc.left();
				float ty1 = tc.top();
				float tx2 = tc.right();
				float ty2 = tc.bottom();
				int imgw = t.getImageWidth();
				int imgh = t.getImageHeight();
				float w=imgw/100f;
				float h=imgh/100f;
				float alpha = 1.0f;
				if (imgw > imgh)
					h *= ((float)imgh) / imgw;
				else
					w *= ((float)imgw) / imgh;
				float w2 = w/2f;
				float h2 = h/2f;
				gl.glColor4f(alpha, alpha, alpha, alpha);
				gl.glBegin(GL.GL_QUADS);
				gl.glTexCoord2f(tx1, ty1);
				gl.glVertex3f(-w2+xoffset, h2, 0f);
				gl.glTexCoord2f(tx2, ty1);
				gl.glVertex3f( w2+xoffset, h2, 0f);
				gl.glTexCoord2f(tx2, ty2);
				gl.glVertex3f( w2+xoffset, -h2, 0f);
				gl.glTexCoord2f(tx1, ty2);
				gl.glVertex3f(-w2+xoffset, -h2, 0f);

				gl.glEnd();
			} 
		}



		Image image1;

		BufferedImage imageAct;
		long imageActCt=0;
		long imageLastDisplayed=0;

		float rot=1;

		Display display;
		Shell shell;
		Composite comp;
		GLContext context;
		GLData data;
		GLCanvas canvas;

		public VideoDisplay_SWT(VideoProjectorPlugin vtk, VideoReceiverSlot vr) {
			super(vtk, vr);
			display=((VideoProjectorPlugin_SWT)vtk).display;
			shell=((VideoProjectorPlugin_SWT)vtk).shell;
			comp=((VideoProjectorPlugin_SWT)vtk).comp;
			context=((VideoProjectorPlugin_SWT)vtk).context;
			data=((VideoProjectorPlugin_SWT)vtk).data;
			canvas=((VideoProjectorPlugin_SWT)vtk).canvas;
		}

		private Texture createTexture(BufferedImage img)

		{

			Texture t1 = null;


			//manual dispose and disable:
			if (t!=null) {
				int [] id= {t.getTextureObject()};
				GL gl = context.getGL();				
				gl.glDeleteTextures(1, id, 0);
			}

			t1 = TextureIO.newTexture(img, false);
			//t1 = TextureIO.newTexture(img, true);
			t1.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
			t1.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
			return t1;

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
				t = createTexture(imageAct);
			}
			return lostimg;
		}

		void renderStreaming(){


			//JOGL Texture on SWT Canvas
			if (imageAct!=null) {
				rot++;

				t.enable();
				t.bind();						
				GL gl = context.getGL ();
				//				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				//				gl.glClearColor(.3f, .5f, .8f, 1.0f);
				//				gl.glLoadIdentity();
				//				gl.glTranslatef(0.0f, 0.0f, -10.0f);
				//				gl.glRotatef(0.15f * (rot*xoffset), 2.0f * rot, 10.0f * rot, 1.0f);
				drawFrame(gl);

			}
			return;
		}


		/** 
		 * Creates the graphical interface components and initialises them
		 */
		void initGUI(ArrayList<Object> params){


		}

	}






	Display display;
	Shell shell;
	Composite comp;
	GLContext context;
	GLData data;
	GLCanvas canvas;

	ArrayList<Object> vr ;
	
	public VideoDisplay createVideoDisplay(VideoReceiverSlot vr) {
		return new VideoDisplay_SWT(this,vr);
	}

	public VideoProjectorPlugin_SWT(ArrayList<Object> params) {
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

	public VideoDisplay addDisplay(VideoReceiverSlot vr) {
		VideoDisplay vd=super.addDisplay(vr);
		thumb.addDisplay(vd);
		return vd;
	}
	
	/** 
	 * updates the screen to follow the video stream
	 */
	public void registerRendering(){


		//display.asyncExec(new Runnable() {
		//display.syncExec(new Runnable() {
		display.timerExec(500,new Runnable() {
			int rot = 0;
			//screen drawing is done by the graphics card, so not a lot of CPU power is used
			//so we can go for higher output rate when needed by handling mouseevents
			int timerMax=1000;
			int timer=timerMin;
			public void run() {
				if (thumb.inMotion())
					timerMax=30;
				else
					timerMax=1000;
				if (!canvas.isDisposed()) {
					canvas.setCurrent();
					context.makeCurrent();
					//float frot = rot*5;
					rot++;

					long lostimg=0;
					for (int i=0; i<displayList.size();i++) {
						lostimg=Math.max(lostimg, ((VideoDisplay)(displayList.get(i))).prerenderStreaming());
					}

					GL gl = context.getGL ();	
					gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
					gl.glClearColor(.3f, .5f, .8f, 1.0f);
					gl.glLoadIdentity();
					//gl.glTranslatef(0.0f, 0.0f, -10.0f);
					//gl.glRotatef(0.15f * (rot*1), 2.0f * rot, 10.0f * rot, 1.0f);

					thumb.draw(gl);
//					for (int i=0; i<displayList.size();i++) {
//						//for (int i=displayList.size()-1;i>=0;i--) {
//						gl.glRotatef(0.15f * (rot*1), 2.0f * rot, 10.0f * rot, 3.0f*(i+1));
//						((VideoDisplay)(displayList.get(i))).renderStreaming();
//					}

					//do not dispose and disable textures before glEnd!!!
					gl.glEnd();

					canvas.swapBuffers();
					context.release();
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


	VideoComposite thumb;


	/** 
	 * Creates the graphical interface components and initialises them
	 */
	public void initGUI(){
		data = new GLData ();
		data.doubleBuffer = true;
		data.depthSize=8;
		canvas = new GLCanvas(comp, SWT.NONE, data);
		canvas.setToolTipText(
				"Mouse Controls:\n"+
				"  hover lolwer corners  :   rotation control\n"+
				"  click during rotation :   immediate stop\n"+
				"  wheel                 :   zoom control\n"+
				"KEYPAD Controls:\n"+
				"  [1]-[9]               :   fast screen access\n"+
				"  [+]/[-]               :   step screens\n"+
				"  [/]/[*]               :   tilt control\n"+
				"  [.]                   :   set default tilt"
				);
		
		thumb=new VideoThumb();

		//canvasSimple = new Canvas(shell,SWT.NONE);
		//label = new Label(shell,SWT.NONE);

//		canvas.addPaintListener(new PaintListener() {
//			public void paintControl(PaintEvent e) {
//				//Rectangle clientArea = canvas.getClientArea();
//			}
//		}); 

		canvas.setCurrent();
		context = GLDrawableFactory.getFactory().createExternalGLContext();

		canvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				Rectangle bounds = canvas.getBounds();
				float fAspect = (float) bounds.width / (float) bounds.height;
				canvas.setCurrent();
				context.makeCurrent();
				GL gl = context.getGL ();
				gl.glViewport(0, 0, bounds.width, bounds.height);
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glLoadIdentity();
				GLU glu = new GLU();
				glu.gluPerspective(45.0f, fAspect, 0.5f, 400.0f);
				gl.glMatrixMode(GL.GL_MODELVIEW);
				gl.glLoadIdentity();
				context.release();
			}
		});


		canvas.addListener(SWT.MouseHover, new Listener() {
			public void handleEvent(Event e) {
				thumb.handleEvent(e);
			}
		});

		canvas.addListener(SWT.MouseMove, new Listener() {
			public void handleEvent(Event e) {
				thumb.handleEvent(e);
			}
		});

		canvas.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				thumb.handleEvent(e);
			}
		});

		canvas.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event e) {
				thumb.handleEvent(e);
			}
		});

		canvas.addListener(SWT.MouseWheel, new Listener() {
			public void handleEvent(Event e) {
				thumb.handleEvent(e);
			}
		});

		canvas.addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				thumb.handleEvent(e);
			}
		});

		context.makeCurrent();
		GL gl = context.getGL ();
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		gl.glClearDepth(1.0);
		gl.glLineWidth(2);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_TEXTURE_2D);

		// Clear color and depth buffers
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);


		gl.glFlush();

		context.release();
	}

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

}
