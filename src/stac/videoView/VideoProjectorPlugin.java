package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.util.ArrayList;


	
	public abstract class VideoProjectorPlugin {

		int timerMin =10;
		public void setTimerMin(int timerMin) {
			this.timerMin=timerMin;
		}
		public int getTimerMin(int timerMin) {
			return timerMin;
		}

		
		abstract class VideoDisplay {
			VideoReceiverSlot vr;
			VideoProjectorPlugin vtk;
			abstract void initGUI(ArrayList<Object> params);
			abstract long prerenderStreaming();
			abstract void renderStreaming();
			float xoffset=0;
			
			public VideoDisplay(VideoProjectorPlugin vtk,VideoReceiverSlot vr) {
				this.vtk=vtk;
				this.vr=vr;
			}
		}
		
		//abstract public VideoCanvasPlugin(ArrayList<Object> params);
		abstract public void initGUI();
		abstract public void registerRendering();
		abstract public VideoDisplay createVideoDisplay(VideoReceiverSlot vr);
		abstract public void test(VideoProjectorSlot vcs);

		ArrayList<VideoDisplay> displayList = new ArrayList<VideoDisplay>();

		public VideoDisplay addDisplay(VideoReceiverSlot vr) {
			VideoDisplay vs= createVideoDisplay(vr);
			vs.xoffset=-3+displayList.size()*3;
			displayList.add(vs);
			return vs;
		}


	}

