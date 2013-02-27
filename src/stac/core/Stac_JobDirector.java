package stac.core;

import java.util.Vector;

/**
 * Gives GUI access to JobManagers
 * Note:
 * JobDirector is not thread safe, so only just one instance can run at a time
 * @author sudol
 *
 */
public abstract class Stac_JobDirector {
	
	Vector jobManagers=new Vector(0);
	Vector jobControllers=new Vector(0);
	JobControlWindow jobWindow=null;

//maybe synchronized on each relevant methods solves the locking
//if the synchronized do synch the use of ALL synchronized methodes
//of the object! (and does not synch the methods independently)
//	volatile boolean locked=false;
//	
//	synchronized public boolean lock() {
//		if (locked) {
//			try {
//				wait();
//			} catch (InterruptedException e) {
//				return false;
//			}
//		}
//		locked=true;
//		return true;		
//	}
//	
//	public void unlock() {
//		locked=false;
//		notify();
//	}

	synchronized public void setNewJobWindow(JobControlWindow newJobWindow) {
//		if (!lock())
//			return;
		if (jobWindow!=null) {
			jobWindow.setVisible(false);
			jobWindow.removeAll();
		}
		jobWindow=newJobWindow;
		//jobWindow.removeAllJobManagers();
		jobControllers.removeAllElements();
		for(int i=0;i<jobManagers.size();i++)
    		jobControllers.addElement(jobWindow.addJobManager((Stac_JobManager) jobManagers.elementAt(i)));
			
//		unlock();
		
	}

	/**
	 * finds the first ReqHandler that is ready to process the request
	 */
	public Stac_ReqHandler findJobManager (Stac_ReqMsg msg) {
		for(int i=0;i<jobManagers.size();i++) {
    		if (((Stac_JobManager) jobManagers.elementAt(i)).doAcceptJob(msg)) {
    			return ((Stac_ReqHandler) jobManagers.elementAt(i));
    		}
		}
		return null;
	}
	
	/**
	 * finds the first ReqHandler that is ready to process the request
	 */
	public void handle (Stac_ReqMsg msg) {
		Stac_ReqHandler actJobManager=findJobManager(msg);
		if (actJobManager!=null) {
			actJobManager.handle(msg);
		} else {
			msg.session.addErrorMsg(msg.msgId, "No appropriate JobManager found for the request ("+msg.msgId+" - "+msg.getClass().getSimpleName()+")");
			msg.session.printErrorMsg(msg.msgId);
			msg.respHandler.handleResponseSynch(null);
		}
	}
	
	synchronized public void addReqHandler(Stac_JobManager jobManager) {
//		if (!lock())
//			return;
    	jobManagers.addElement(jobManager);
    	if (jobWindow!=null)
    		jobControllers.addElement(jobWindow.addJobManager(jobManager));
//    	unlock();
    }	
	
	public void closeReqHandlers() {
		//stop accepting new jobs
		for (int i=0;i<jobManagers.size();i++) {
			((Stac_JobManager)(jobManagers.elementAt(i))).setJobGate(false);
		}
		//waiting for all job finished
		Thread sleep = null;
		sleep= new Thread();
		sleep.start();
		for (int i=0;i<jobManagers.size();i++) {
			while (((Stac_JobManager)(jobManagers.elementAt(i))).numOfJobs()>0) {
				//sleep
				try {
					sleep.sleep(200);
				} catch (InterruptedException e) {
				}
			}
		}
		sleep.stop();
		//close the managers
		for (int i=0;i<jobManagers.size();i++) {
			((Stac_JobManager)(jobManagers.elementAt(i))).closeJobManager();
		}
	}
	
	synchronized public void jobQueueModified(Stac_JobManager jobManager,int numofActiveJobs) {
//		if (!lock())
//			return;
		
		for(int i=0;i<jobManagers.size();i++)
			if (((Stac_JobManager)jobManagers.elementAt(i)).equals((Object)jobManager)) {
				if(jobWindow!=null) {
				if (numofActiveJobs==0) {
					((JobControlWidget)(jobControllers.elementAt(i))).jobsGone();
				} else {
					((JobControlWidget)(jobControllers.elementAt(i))).jobStarted(numofActiveJobs);
				}
				}
				break;
			}
//		unlock();
	}
	
	//public abstract JobControlWindow getJobWindow();

}
