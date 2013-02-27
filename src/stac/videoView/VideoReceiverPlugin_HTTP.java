package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


public class VideoReceiverPlugin_HTTP extends VideoReceiverPlugin {

	
	public VideoReceiverPlugin_HTTP(String title, ArrayList<Object> params) {
		super(title, params);
	}

		String HTTPURL;
	
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
		
 		if (params!=null){
			HTTPURL=(String)(params.get(0));
		} else {
			HTTPURL="http://id144video1.esrf.fr/jpg/quad/image.jpg";
		}
   				
	}
	
	/**
	 * Implements the capture thread: get a frame from the FrameGrabber, and display it
	 */
	public void run(){
			while(!stop){
				try {
					Thread.sleep(timerMin);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				URL myURL=null;
				try {
					myURL = new URL( HTTPURL );
				} catch (MalformedURLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				HttpURLConnection conn = null;
				try {
					conn = (HttpURLConnection) myURL.openConnection();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				try {
					conn.setRequestMethod( "GET" );
				} catch (ProtocolException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				conn.setDoInput( true );
				conn.setDoOutput( false );
				conn.setUseCaches(false);
				
				BufferedImage bi=null;
				try {
					bi = javax.imageio.ImageIO.read(conn.getInputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				
				//if (bi!=null)
					setImage(bi);
			}
	}

}
