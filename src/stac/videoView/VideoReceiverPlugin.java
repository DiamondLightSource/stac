package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.awt.image.BufferedImage;
import java.util.ArrayList;


class StampedImage {
	public BufferedImage img;
	public long imageCt;
}


public abstract class VideoReceiverPlugin extends Thread {

	
	StampedImage imageAct=new StampedImage();
	int imageActCt=0;
	int imageLastDisplayed=0;
	
	protected Thread captureThread;
	protected boolean stop;
	
	public String title="VideoReceiver";
	
	int timerMin =10;
	public void setTimerMin(int timerMin) {
		this.timerMin=timerMin;
	}
	public int getTimerMin(int timerMin) {
		return timerMin;
	}
	
	public VideoReceiverPlugin(String title,ArrayList<Object> params) {
		this.title=title;
		if (params!=null){
			setTimerMin(((Integer)(params.get(0))).intValue());
			params.remove(0);
		}
		initFrameGrabber(params);
		stop = false;
		captureThread = new Thread(this, "Capture Thread ("+title+")");
		captureThread.start();
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
	abstract void initFrameGrabber(ArrayList<Object> params);
	

	/**
	 * Updates the image shown in the JLabel
	 * @param b
	 */
	public void setImage(BufferedImage image) {
		//l.getGraphics().drawImage(image, 0, 0, width, height, null);
		imageAct.img=image;//.getSubimage(0, 0,image.getWidth()-1, image.getHeight()-1);
		imageAct.imageCt=++imageActCt;
		
//		image1= new Image(display,convertToSWT(image));
	}

	public StampedImage getActImage() {
//		int lostimg=0;
//		if (imageActCt>0) {
//			if (imageActCt>imageLastDisplayed+1) {
//				lostimg=imageActCt-1-imageLastDisplayed;
//				Stac_Out.println(title+": "+(lostimg)+" lost image(s) from "+(imageLastDisplayed+1)+" to "+(imageActCt-1));
//			}
//			imageLastDisplayed=imageActCt;
//		}
		return imageAct;
	}

	public void dispose() {
		if(captureThread.isAlive()){
			stop = true;
			try {
				captureThread.join();
			} catch (InterruptedException e1) {}
		}
				
	}

	public String getTitle() {
		return title;
	}
	
		

}
