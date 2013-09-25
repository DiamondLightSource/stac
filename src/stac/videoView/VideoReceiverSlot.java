package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.util.ArrayList;

public class VideoReceiverSlot {

	VideoReceiverPlugin vrp;	

	public VideoReceiverSlot(int type,String title,ArrayList<Object> params) {
		
		if (type==VideoView.VV_V4L4J) {
			vrp=new VideoReceiverPlugin_V4L4J(title,params);
		} else if (type==VideoView.VV_TANGO) {
			vrp=new VideoReceiverPlugin_TANGO(title,params);
		} else if (type==VideoView.VV_MOVIE) {
			vrp=new VideoReceiverPlugin_MOVIE(title,params);
		} else if (type==VideoView.VV_HTTP) {
			vrp=new VideoReceiverPlugin_HTTP(title,params);
		} 
		
	}

	public StampedImage getActImage() {
		return vrp.getActImage();
	}

	public void dispose() {
		vrp.dispose();
	}

	public String getTitle() {
		return vrp.getTitle();
	}
	
	public void setTimerMin(int timerMin) {
		vrp.setTimerMin(timerMin);
	}
	public int getTimerMin(int timerMin) {
		return vrp.getTimerMin(timerMin);
	}

}
