package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceProxy;

public class VideoReceiverPlugin_TANGO extends VideoReceiverPlugin {

	DeviceProxy dev;

  	byte [] tango_getAttrList (String attr_name){
    	
    		DeviceAttribute da;
			try {
				da = dev.read_attribute(attr_name);
				short [] s=da.extractUCharArray();
				byte [] b = new byte[s.length];
				for (int i=0;i<s.length;i++)
					b[i]=(byte)s[i];
				return b;
			} catch (DevFailed e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return null;
    }
    
	
	public VideoReceiverPlugin_TANGO(String title, ArrayList<Object> params) {
		super(title, params);
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
		
   		String tangoURL;
		if (params!=null){
			tangoURL=(String)(params.get(0));
		} else {
			//"tango:̣̣̣//deino:20000/id14/prosilica_minidiff/4"
			tangoURL="id14/prosilica_minidiff/4";
		}
   		
  		try {
			dev= new DeviceProxy(tangoURL);
		} catch (DevFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				byte[] res=tango_getAttrList("JpegImage");
				ByteArrayInputStream bais = new ByteArrayInputStream(res);
				
				BufferedImage bi=null;
				try {
					bi = javax.imageio.ImageIO.read(bais);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				
				if (bi!=null)
					setImage(bi);
			}
	}

}
