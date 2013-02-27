package stac.core;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.border.*;
import java.io.*;

import Jama.*;
//import java2d.demos.Colors.Rotator3D.Objects3D.Matrix3D;
//import javax.swing.table.TableColumn;
import javax.swing.table.AbstractTableModel;
import javax.vecmath.*;

import java.util.Vector;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;

/**
 * Asynchronous management of jobs in a queue
 * also talks to JobDirector which can visualize and give acces to multiple JobManagers
 */
public abstract class Stac_JobManager {
	Vector activeJobs=new Vector(0);
	Vector percentageDone=new Vector(0);
	Stac_JobDirector director=null;
	boolean jobGate=true;
	
	//TODO
	//thredID msut be sent with the request, check if we are in good thread; each thread should chack for future tasks
	//thread=jobaction
	public void LOCK(){
		
	}
	
	public Stac_JobManager() {
		activeJobs=new Vector(0);		
	}
	
	public Stac_JobManager(Stac_JobDirector director) {
		activeJobs=new Vector(0);
		this.director=director;
	}
	
	public synchronized boolean addJob(Stac_JobAction newJob) {
		//TODO
		//simple sequencing of the jobs under this Manager
		//to avoid parallel threads, 
		//either the JobDirector should synchronize the jobs,
		//or reqHandler should be hold while the corresponding JobActivity is running
		//while (activeJobs.size()!=0) {}
		
		//if this manager is not allowed to take more jobs, it rejects new requests
		if (jobGate==false)
			return false;
		activeJobs.addElement(newJob);
		percentageDone.addElement(new Integer(0));
		if (director!=null)
			director.jobQueueModified(this,activeJobs.size());
		return true;
	}
	
	public synchronized void removeJob(Stac_JobAction oldJob) {
		int pos=activeJobs.indexOf(oldJob);
		if (pos>=0) {
			activeJobs.removeElementAt(pos);
			percentageDone.removeElementAt(pos);
		}
		if (director!=null)
			director.jobQueueModified(this,activeJobs.size());
	}
	
	public synchronized void stopJobs() {
		for (int i=activeJobs.size()-1; i>=0;i-- ) {
			((Stac_JobAction)activeJobs.elementAt(i)).setKilled();
			((Stac_JobAction)activeJobs.elementAt(i)).stop();
			//TODO put ampty msg with error log!!!
			//TODO session error msg!!!
			activeJobs.removeElementAt(i); 
			percentageDone.removeElementAt(i);
		}
		if (director!=null)
			director.jobQueueModified(this,activeJobs.size());
	}
	public synchronized void stopJob(Stac_JobAction job) {
		int pos=activeJobs.indexOf(job);
		job.setKilled();
		job.stop();
		//TODO put ampty msg with error log!!!
		//TODO session error msg!!!
		activeJobs.removeElementAt(pos); 
		percentageDone.removeElementAt(pos);
		if (director!=null)
			director.jobQueueModified(this,activeJobs.size());
	}
	
	abstract public boolean doAcceptJob(Object job);
	
	public int numOfJobs() {
		return activeJobs.size();
	}
	
	public void generateJobResponse(Stac_JobAction job,Stac_RespMsg response) {
		job.req.respHandler.handleResponseSynch(response);		
	}
	public void setPercentageDone(int percent,Stac_JobAction job) {
		int pos=activeJobs.indexOf(job);
		percentageDone.setElementAt(new Integer(percent),pos);
		//at the moment we do not inform the director
	}
	
	public void setJobGate(boolean flag) {
		jobGate=flag;
	}
	
	public abstract void closeJobManager();
}

