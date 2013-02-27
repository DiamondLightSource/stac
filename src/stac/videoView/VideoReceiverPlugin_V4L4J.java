package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.util.ArrayList;

import stac.core.Stac_Out;

import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.PushSourceCallback;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

public class VideoReceiverPlugin_V4L4J extends VideoReceiverPlugin {

	
	public VideoReceiverPlugin_V4L4J(String title, ArrayList<Object> params) {
		super(title, params);
	}

	private VideoDevice vd;

	VideoFrame frame;
	private FrameGrabber fg;
	
	boolean getImageNow=false;
	Object imageSync= new Object();
	
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
		String dev;
		int w;
		int h;
		int std;
		int channel;
		int qty;
		if (params!=null){
			dev=		(String)params.get(0);
			w=		((Integer)params.get(1)).intValue();
			h=		((Integer)params.get(2)).intValue();
			std=		((Integer)params.get(3)).intValue();
			channel=		((Integer)params.get(4)).intValue();
			qty=		((Integer)params.get(5)).intValue();
		} else {
			dev="/dev/video0";
			w=320;
			h=240;
			std=V4L4JConstants.STANDARD_WEBCAM;
			channel = 0;
			qty = 60;
		}
		
			try {
				vd = new VideoDevice(dev);
				fg = vd.getJPEGFrameGrabber(w, h, channel, std, qty);
				fg.setPushSourceMode(new PushSourceCallback() {
					@Override
					public void exceptionReceived(V4L4JException arg0) {
					}
					@Override
					public void nextFrame(VideoFrame frame) {
						if (getImageNow) {
							getImageNow=false;
							setImage(frame.getBufferedImage());
							synchronized (imageSync) {
								imageSync.notify();
							}
						}
						try {
							sleep(80);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						frame.recycle();
					}
					
				});
				fg.startCapture();
				Stac_Out.println("Starting capture at "+fg.getWidth()+"x"+fg.getHeight());            
			} catch (V4L4JException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	

	/**
	 * Implements the capture thread: get a frame from the FrameGrabber, and display it
	 */
	public void run(){
		try {                   
			while(!stop){
				try {
					sleep(timerMin);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				frame = fg.getVideoFrame();
//				setImage(frame.getBufferedImage());
//				frame.recycle();
				getImageNow=true;
				try {
					synchronized (imageSync) {
						imageSync.wait();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Stac_Out.println("Failed to capture image");
		}

	}

	public void dispose() {
		super.dispose();

		fg.stopCapture();
		vd.releaseFrameGrabber();
		vd.release();
		
	}

}
