package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import stac.core.Stac_Out;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class VideoProjectorPlugin_HTTPproxy extends VideoProjectorPlugin {



	class VideoDisplay_HTTPproxy extends VideoDisplay {



//		JPanel panel;
//		JLabel label;

		BufferedImage imageAct;
		long imageActCt=0;
		long imageLastDisplayed=0;

		public VideoDisplay_HTTPproxy(VideoProjectorPlugin vtk, VideoReceiverSlot vr) {
			super(vtk, vr);
//			panel=((VideoProjectorPlugin_HTTPproxy)vtk).panel;
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

		void renderStreaming(){

			//JOGL Texture on SWT Canvas
//			if (imageAct!=null && label.getGraphics()!=null) {
//̣               label.getGraphics().drawImage(imageAct, 0, 0, imageAct.getWidth(), imageAct.getHeight(), null);
//			}
			return;
		}


		/** 
		 * Creates the graphical interface components and initialises them
		 */
		void initGUI(ArrayList<Object> params){
//			label=(JLabel)(params.get(0));


		}

	}


//JFrame f;
//JPanel panel;
//GridLayout grid;


	public VideoDisplay createVideoDisplay(VideoReceiverSlot vr) {
		VideoDisplay_HTTPproxy vd = new VideoDisplay_HTTPproxy(this,vr);
//		JLabel label = new JLabel();
//		panel.add(label);
//		ArrayList<Object> params = new ArrayList<Object>();
//		params.add(label);
//		vd.initGUI(params);
		return vd;
	}

	public VideoDisplay addDisplay(VideoReceiverSlot vr) {
		VideoDisplay vs= createVideoDisplay(vr);
		int it=displayList.size()+1;
//		int gx=(int)Math.ceil(Math.sqrt(it));
//		//int gy=(int)Math.ceil(((double)it)/gx);
//		grid.setColumns(0);
//		grid.setRows(gx);//(gx-1+gy-1>0)?gy-1:1);
		displayList.add(vs);
//		panel.setVisible(false);
//		panel.setVisible(true);
		return vs;
	}
	
	
	ArrayList<Object> vr ;
	boolean stop=false;
	
	public VideoProjectorPlugin_HTTPproxy(ArrayList<Object> params) {
		if (!(params.get(0) instanceof VideoReceiverSlot)) {
		} else {
			vr=params;
		}
	}


	
	class httpServer implements HttpHandler {
		public void handle(HttpExchange exchange) throws IOException {
			String requestMethod = exchange.getRequestMethod();
			String [] str=exchange.getRequestURI().toString().split("/");
			int id=new Integer(str[str.length-1]).intValue();
			if (id<=displayList.size() && requestMethod.equalsIgnoreCase("GET")) {
				Headers responseHeaders = exchange.getResponseHeaders();
				responseHeaders.set("Content-Type", "image/jpeg");
				exchange.sendResponseHeaders(200, 0);

				OutputStream responseBody = exchange.getResponseBody();

				try {
					long lostimg=((VideoDisplay)(displayList.get(id-1))).prerenderStreaming();
					BufferedImage image=((VideoDisplay_HTTPproxy)(displayList.get(id-1))).imageAct;

					javax.imageio.ImageIO.write(image, "jpg", responseBody);
				} catch (Exception e) {
					e.printStackTrace();
				}

				responseBody.close();
			}
		}
	}

	
	/** 
	 * updates the screen to follow the video stream
	 */
	public void registerRendering(){
		
		int port=5980;

	    InetSocketAddress addr = new InetSocketAddress(port);
	    HttpServer server = null;
	    int portmax=port+20;
	    for(;port<=portmax;port++) {
	    	try {
	    		server = HttpServer.create(addr, 0);
	    	    server.createContext("/", new httpServer());
	    	    server.setExecutor(Executors.newCachedThreadPool());
	    	    server.start();
	    	    Stac_Out.println("Server is listening on port "+port );
	    	    System.out.println("Server is listening on port "+port );
	    		break;
	    	} catch (IOException e1) {
	    		// TODO Auto-generated catch block
	    		Stac_Out.println("Could not connect to port # "+port);
	    	}
	    }
	    

	}


	/** 
	 * Creates the graphical interface components and initialises them
	 */
	public void initGUI(){
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
