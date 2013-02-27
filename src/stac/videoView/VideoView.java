package stac.videoView;

import java.util.ArrayList;

import stac.core.Stac_Out;
import stac.core.Stac_Out_logger;
import stac.core.Stac_Session;



public class VideoView {

	static int VV_SWING=1;
	static int VV_SWT=2;
	static int VV_JOGL=4;
	static int VV_J3D=8;
	static int VV_HTTPproxy=16;
	static int VV_SWTgrid=32;
	static int VV_SWTemb=64;
	static int VV_Archiver=128;

	static int VV_V4L4J=1;
	static int VV_TANGO=2;
	static int VV_MOVIE=3;
	static int VV_HTTP=4;
	
	public static void help() {
				Stac_Out.println(" videoView.VideoView <params>");
				Stac_Out.println("");
				Stac_Out.println("  help           : this help screen");
				Stac_Out.println("");
				Stac_Out.println(" Receiver params:");
				Stac_Out.println("  V4L4J_test     : start webcam @ /dev/video0");
				Stac_Out.println("  MOVIE_test     : start demo movie");
				Stac_Out.println("  TANGO_test     : connect to default tango image server");
				Stac_Out.println("  HTTP_test      : connect to default http image server");
				Stac_Out.println("");
				Stac_Out.println("  V4L4J <settings> - ");
				Stac_Out.println("        timer   timerMin to adjust max framerate");
				Stac_Out.println("        dev     the video device file to capture from");
				Stac_Out.println("        w       the desired capture width");
				Stac_Out.println("        h       the desired capture height");
				Stac_Out.println("        std     the capture standard");
				Stac_Out.println("        channel the capture channel");
				Stac_Out.println("        qty     the JPEG compression quality");
				Stac_Out.println("  TANGO <settings> - ");
				Stac_Out.println("        timer   timerMin to adjust max framerate");
				Stac_Out.println("        dev     the TANGOURL of the Device Server");
				Stac_Out.println("  MOVIE <settings> - ");
				Stac_Out.println("        timer   timerMin to adjust max framerate");
				Stac_Out.println("        fname   the filename of the movie");
				Stac_Out.println("  HTTP <settings> - ");
				Stac_Out.println("        timer   timerMin to adjust max framerate");
				Stac_Out.println("        url     the url to get the video");
				Stac_Out.println("");
				Stac_Out.println(" Projector params:");
				Stac_Out.println("  SWT_test       : show streams in SWT 3D canvas");
				Stac_Out.println("  SWTgrid_test   : show streams in SWT 2D canvas");
				Stac_Out.println("  SWING_test     : show streams in JPanel");
				Stac_Out.println("  HTTPproxy_test : supply streams via HTTP");
				Stac_Out.println("  Archiver_test  : saves the stream");
				Stac_Out.println("");
				Stac_Out.println("");
				System.exit(1);
		
	}
		
	public static void main(String[] args) {
		ArrayList<VideoReceiverSlot> vr = new ArrayList<VideoReceiverSlot>();
		ArrayList<VideoProjectorSlot> vc = new ArrayList<VideoProjectorSlot>();
		
		Stac_Out.setStac_Session(new Stac_Session(false) {
			public void print(String msg) {
				System.out.println(Stac_Out.getTimeStr()+"VideoView Message: "+msg);
			}
		});
//		Stac_Out.setOutput(false);
//		Stac_Out.addLogger(new Stac_Out_logger() {
//			public void print(String msg) {
//				System.out.println(Stac_Out.getTimeStr()+"VideoView Message: "+msg);
//			}
//		});
		
		if (args.length==0)
			help();

		for (int i = 0 ; i < args.length; i++) {
			
			if ("help".endsWith(args[i].toLowerCase())) {
				help();
			} else if ("V4L4J_test".equalsIgnoreCase(args[i])) {
				vr.add(new VideoReceiverSlot(VideoView.VV_V4L4J,"V4L4J_test Stream",null));
			} else if ("V4L4J".equalsIgnoreCase(args[i])) {
				ArrayList<Object> pars=new ArrayList<Object>();
				pars.add(new Integer(args[++i]));
				pars.add(args[++i]);
				pars.add(new Integer(args[++i]));
				pars.add(new Integer(args[++i]));
				pars.add(new Integer(args[++i]));
				pars.add(new Integer(args[++i]));
				pars.add(new Integer(args[++i]));
				vr.add(new VideoReceiverSlot(VideoView.VV_V4L4J,"V4L4J Stream",pars));
			} else if ("MOVIE_test".equalsIgnoreCase(args[i])) {
				vr.add(new VideoReceiverSlot(VideoView.VV_MOVIE,"MOVIE_test Stream",null));
			} else if ("MOVIE".equalsIgnoreCase(args[i])) {
				ArrayList<Object> pars=new ArrayList<Object>();
				pars.add(new Integer(args[++i]));
				pars.add(args[++i]);
				vr.add(new VideoReceiverSlot(VideoView.VV_MOVIE,"MOVIE Stream",pars));
			} else if ("TANGO_test".equalsIgnoreCase(args[i])) {
				vr.add(new VideoReceiverSlot(VideoView.VV_TANGO,"TANGO_test Stream",null));
			} else if ("TANGO".equalsIgnoreCase(args[i])) {
				ArrayList<Object> pars=new ArrayList<Object>();
				pars.add(new Integer(args[++i]));
				pars.add(args[++i]);
				vr.add(new VideoReceiverSlot(VideoView.VV_TANGO,"TANGO Stream",pars));
			} else if ("HTTP_test".equalsIgnoreCase(args[i])) {
				vr.add(new VideoReceiverSlot(VideoView.VV_HTTP,"HTTP_test Stream",null));
			} else if ("HTTP".equalsIgnoreCase(args[i])) {
				ArrayList<Object> pars=new ArrayList<Object>();
				pars.add(new Integer(args[++i]));
				pars.add(args[++i]);
				vr.add(new VideoReceiverSlot(VideoView.VV_HTTP,"HTTP Stream",pars));
			}
		}
		
//		for(int i=0;i<vr.size();i++) {
//			vr.get(i).setTimerMin(50);
//		}
		
		for (int i = 0 ; i < args.length; i++) {
			if ("SWT_test".equalsIgnoreCase(args[i])) {
				vc.add(new VideoProjectorSlot(VideoView.VV_SWT,new ArrayList<Object>(vr)));
			} else	if ("SWTgrid_test".equalsIgnoreCase(args[i])) {
				vc.add(new VideoProjectorSlot(VideoView.VV_SWTgrid,new ArrayList<Object>(vr)));
			} else	if ("SWTemb_test".equalsIgnoreCase(args[i])) {
				vc.add(new VideoProjectorSlot(VideoView.VV_SWTemb,new ArrayList<Object>(vr)));
			} else	if ("SWING_test".equalsIgnoreCase(args[i])) {
				vc.add(new VideoProjectorSlot(VideoView.VV_SWING,new ArrayList<Object>(vr)));
			} else	if ("HTTPproxy_test".equalsIgnoreCase(args[i])) {
				vc.add(new VideoProjectorSlot(VideoView.VV_HTTPproxy,new ArrayList<Object>(vr)));
			} else	if ("Archiver_test".equalsIgnoreCase(args[i])) {
				vc.add(new VideoProjectorSlot(VideoView.VV_Archiver,new ArrayList<Object>(vr)));
			}
		}
		
		for (int j =0 ; j<vr.size();j++) {
			vr.get(j).dispose();
		}
		
		//VideoView_test vv = new VideoView_SWTtest();
		//vv.test();
		//VideoView_test vv = new VideoView_SWINGtest();
		//vv.test();
		
	}


}
