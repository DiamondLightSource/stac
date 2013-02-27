package stac.videoView;


/*******************************************************************************
 *******************************************************************************/
import java.util.ArrayList;


public class VideoProjectorSlot {
	VideoProjectorPlugin vtk;	

	public VideoProjectorSlot(int type,ArrayList<Object> params) {
		
		if ((type&VideoView.VV_SWT)!=0) {
			vtk=new VideoProjectorPlugin_SWT(params);
		} else if ((type&VideoView.VV_SWTgrid)!=0) {
			vtk=new VideoProjectorPlugin_SWTgrid(params);
		} else if ((type&VideoView.VV_SWTemb)!=0) {
			vtk=new VideoProjectorPlugin_SWTemb(params);
		} else if ((type&VideoView.VV_SWING)!=0) {
			vtk=new VideoProjectorPlugin_SWING(params);
		} else if ((type&VideoView.VV_HTTPproxy)!=0) {
			vtk=new VideoProjectorPlugin_HTTPproxy(params);
		} else if ((type&VideoView.VV_Archiver)!=0) {
			vtk=new VideoProjectorPlugin_Archiver(params);
		}
		
		vtk.initGUI();
		
		vtk.registerRendering();
		if (params.get(0) instanceof VideoReceiverSlot)
			vtk.test(this);
	}

	public void addDisplay(VideoReceiverSlot vr) {
		vtk.addDisplay(vr);
	}

	public void setTimerMin(int timerMin) {
		vtk.setTimerMin(timerMin);
	}
	public int getTimerMin(int timerMin) {
		return vtk.getTimerMin(timerMin);
	}


	
}
