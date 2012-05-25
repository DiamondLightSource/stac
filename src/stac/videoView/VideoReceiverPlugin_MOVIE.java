package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.util.ArrayList;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.FPSAnimator;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.Control;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.NoProcessorException;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.ResourceUnavailableException;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.renderer.VideoRenderer;

import stac.core.Stac_Out;

//import org.pirelenito.multimedia.jmf.plugin.IGLTextureRenderer;


public class VideoReceiverPlugin_MOVIE extends VideoReceiverPlugin implements VideoRenderer, Control ,ControllerListener{

	/**
	 * Buffer containing the converted pixels ready to be ploted
	 */
	private ByteBuffer byteBuffer;

	/**
	 * Flag indicating if the buffer was updated
	 */
	private boolean byteBufferUpdated;

	private int height;
	private int width;
	@Override
	public Rectangle getBounds() {
		return null;
	}

	@Override
	public Component getComponent() {
		return null;
	}

	@Override
	public void setBounds(Rectangle arg0) {
	}

	@Override
	public boolean setComponent(Component arg0) {
		return false;
	}

	@Override
	public Format[] getSupportedInputFormats() {
		// RGB is the easy one to work
		return new Format[] { new RGBFormat() };
	}

	@Override
	public Format setInputFormat(Format format) {
		return format;
	}

	@Override
	public void start() {
	}

//	@Override
//	public void stop() {
//	}

	@Override
	public void close() {
	}

//	@Override
//	public String getName() {
//		return "RGB Renderer for VideoCanvas";
//	}

	@Override
	public void open() throws ResourceUnavailableException {
	}

	@Override
	public void reset() {
	}

	@Override
	public Object getControl(String control) {
		if( control.equals("javax.media.renderer.VideoRenderer") || 
				control.equals("videoView.VideoReceiverPlugin_MOVIE") )
			return this;

		return null; 
	}

	@Override
	public Object[] getControls() {
		Object[] obj = { this }; 
		return obj; 
	}

	public void swapAndScaleRGB(byte [] jmfData,int width, int height) {

		int op, ip, x, y;

		op = 0;
		int lineStride = 3 * width;
		for ( int i = 0; i < height; i++ ) {
			for ( int j = 0; j < width; j++) {
				x = (width*j) >> 7;
			y = (height*i) >> 7;

			if ( x >= width || y >= height ) {
				textureData[op++]  = 0;
				textureData[op++]  = 0;
				textureData[op++]  = 0;
			} else {
				ip = y*lineStride + x*3;
				textureData[op++] = jmfData[ip++];
				textureData[op++] = jmfData[ip++];
				textureData[op++] = jmfData[ip++];
			}
			}
		}
	}


	public void swapAndScaleARGB(byte [] jmfData,int width, int height) {

		int op, ip, x, y;

		op = 0;
		int lineStride = 3 * width;
		for ( int i = 0; i < width; i++ )
			for ( int j = 0; j < height; j++) {
				x = (width*j) >> 7;
			y = (height*i) >> 7;

			if ( x >= width || y >= height ) {
				textureData[op++] = (byte)0xff;
				textureData[op++]  = 0;
				textureData[op++]  = 0;
				textureData[op++]  = 0;
			} else {
				ip = y*lineStride + x*3;
				textureData[op++] = (byte)0xff;
				textureData[op++] = jmfData[ip++];
				textureData[op++] = jmfData[ip++];
				textureData[op++] = jmfData[ip++];
			}
			}
	}

	byte [] textureData;

	public int process(Buffer buffer) {

		// byteBuffer initialization
		if (byteBuffer == null) {
			byteBuffer = BufferUtil.newByteBuffer(buffer.getLength() * 3);

			width = ((VideoFormat) buffer.getFormat()).getSize().width;
			height = ((VideoFormat) buffer.getFormat()).getSize().height;
		}

		// byteBuffer processing
		//		synchronized (byteBuffer) {
		//			
		//			int[] conversionBuffer = (int[])buffer.getData();
		//			
		//			byteBuffer.rewind();
		//			
		//			// type conversion improvement 
		//			// as sugested by cwoffenden@googlemail.com
		//			byte[] line = new byte[width * 3];
		//			int srcN = 0;
		//			for (int y = height; y > 0; y--) {
		//				int dstN = 0;
		//				
		//				for (int x = width; x > 0; x--) {
		//					int rgb = conversionBuffer[srcN++];
		//					line[dstN++] = (byte) (rgb >> 16);
		//					line[dstN++] = (byte) (rgb >> 8);
		//					line[dstN++] = (byte) (rgb >> 0);
		//				}
		//				byteBuffer.put(line);
		//			}
		//			
		//			byteBuffer.rewind();
		//			
		//			byteBufferUpdated = true;

		//get the video data
		BufferedImage bi = new BufferedImage(height,
				width,
				BufferedImage.TYPE_3BYTE_BGR);

		textureData=((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
		//jmfData =(byte[])(buffer.getData());

		if (platformSpecificImageType == BufferedImage.TYPE_3BYTE_BGR) {
			swapAndScaleRGB((byte[])(buffer.getData()),width,height);
		} else {
			swapAndScaleARGB((byte[])(buffer.getData()),width,height);
		}

		setImage(bi);

		//		}

		return BUFFER_PROCESSED_OK;
	}


	/**
	 * Interface to JMF player and has helper functions to work
	 * with the GL renderer
	 * 
	 * @author Paulo Ragonha
	 */
	class MoviePlayer implements ControllerListener  {

		/**
		 * JMF player
		 */
		private Player player;

		/**
		 * Loop flag
		 */
		private boolean loop;
		VideoReceiverPlugin_MOVIE renderer;
		
		public MoviePlayer (String filename, VideoReceiverPlugin_MOVIE renderer) throws Exception {
			this.renderer=renderer;
			Manager.setHint(Manager.PLUGIN_PLAYER, true);

			// open the file.
			player = Manager.createRealizedPlayer( new URL("file:" + filename) );
			player.addControllerListener(this);
			
			
			player.prefetch();

			// wait for it to be done.
			while(player.getTargetState() != Player.Prefetched );
		}

		/**
		 * @return texture renderer
		 */
		public VideoReceiverPlugin_MOVIE getRenderer () {
			//return renderer;
			return (VideoReceiverPlugin_MOVIE) player.getControl("javax.media.renderer.VideoRenderer");
		}

		public void play() {
			player.start();
			while(player.getTargetState() != Player.Started );
		}

		public void pause() {
			player.stop();
		}

		public void stop() {
			pause();
			rewind();
		}

		public void rewind() {
			player.setMediaTime(new Time(0));
		}

		/**
		 * Set player to auto loop
		 * @param loop
		 */
		public void setLoop (boolean loop) {
			this.loop = loop;
		}

		/**
		 * Check current loop condition
		 * @return
		 */
		public boolean isLoop () {
			return loop;
		}

		public void controllerUpdate(ControllerEvent event) {
			// this players is auto loop!
			if (event instanceof EndOfMediaEvent && loop) {
				rewind();
				play();
			}
		}

	}

	int platformSpecificImageType;


	public VideoReceiverPlugin_MOVIE(String title, ArrayList<Object> params) {
		super(title, params);

		String os = System.getProperty("os.name");

		if ( os.startsWith("W") || os.startsWith("w")) {
			platformSpecificImageType = BufferedImage.TYPE_3BYTE_BGR;
		} else if (os.startsWith("S") || os.startsWith("s")){
			platformSpecificImageType = BufferedImage.TYPE_4BYTE_ABGR;
		} else {
			platformSpecificImageType = BufferedImage.TYPE_3BYTE_BGR;
		}


	}

	/**
	 * Used to do rendering calls at propper FPS.
	 */
	FPSAnimator animator;

	private boolean initialized = false;

	/**
	 * Renderer from which we get the texture
	 */
	private VideoReceiverPlugin_MOVIE renderer;

	/**
	 * Here we play the movie!
	 */
	private MoviePlayer player;
	  int[] waitSync = new int[0];
	    boolean stateTransOK = true;
	
    public boolean waitForState(Processor p,int state) {
    	synchronized (waitSync) {
    		try {
    			while ( p.getState() != state && stateTransOK ) {

    			}
    		} catch (Exception ex) {}

    		return stateTransOK;
    	}
    }
    public synchronized void controllerUpdate(ControllerEvent evt) {
        if ( evt instanceof ConfigureCompleteEvent ||
             evt instanceof RealizeCompleteEvent ||
             evt instanceof PrefetchCompleteEvent ) {
            synchronized (waitSync) {
            stateTransOK = true;
            waitSync.notifyAll();
            }
        } else if ( evt instanceof ResourceUnavailableEvent) {
            synchronized (waitSync) {
            stateTransOK = false;
            waitSync.notifyAll();
            }
        }
        }

	/**
	 * Initialises the FrameGrabber object with the given parameters
	 * @param dev the video device file to capture from
	 * @param w the desired capture width
	 * @param h the desired capture height
	 * @param std the capture standard
	 * @param channel the capture channel
	 * @param qty the JPEG compression quality
	 * @throws V4L4JException if any parameter if invalid
	 */
	@Override
	void initFrameGrabber(ArrayList<Object> params) {
		String movieFilename = (String)params.get(0);

		// first thing is trying to open the movie
//		try {
//			player = new MoviePlayer (movieFilename,this);
//			player.setLoop(true);
//			player.play();
//			renderer = player.getRenderer();
//			
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		Processor p;
		try {
			p = Manager.createProcessor(new URL("file:" + movieFilename) );
			p.configure();
	        if ( !waitForState(p,p.Configured)) {
	            Stac_Out.println("Failed to configure the processor");
	            //return false;
	        } else {
	                Stac_Out.println("waiting for state");
	            }
			 p.setContentDescriptor(null);
			 TrackControl[] tc = p.getTrackControls();

			    if ( tc == null ) {
			        Stac_Out.println("Failed to get the track control from processor");
			        //return false;
			    }

			    TrackControl vtc = null;

			    for ( int i =0; i < tc.length; i++ ) {
			        if (tc[i].getFormat() instanceof VideoFormat ) {
			        vtc = tc[i];
			        break;
			        }

			    }

			    if ( vtc == null ) {
			         Stac_Out.println("can't find video track");
			        //return false;
			    }

			    try {
			        vtc.setRenderer(this);
			    } catch ( Exception ex) {
			        ex.printStackTrace();
			        Stac_Out.println("the processor does not support effect");
			        //return false;
			    }
			    p.setContentDescriptor(null);

		} catch (NoProcessorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}


	public void dispose() {
		super.dispose();

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Component getControlComponent() {
		return null;
	}



}
