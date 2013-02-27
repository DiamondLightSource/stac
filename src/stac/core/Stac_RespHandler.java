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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.io.File;
//import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


public abstract class Stac_RespHandler {
	
    /**
     * Must be implemented by subclass to
     * handle the response message.
     * It should not be called directly by
     * other methods!
     * Instead handleResponseSynch can be used
     * @param response - to be handled
     */	
	protected abstract void handleResponse(Stac_RespMsg response) ;
    	
	/**
	 * synchronized method:
	 * it is SWING specific by telling swing that a response has been calculated and
	 * we are ready to tell the results in handleResponse
	 * @param response
	 */
	public void handleResponseSynch(Stac_RespMsg response) {
		

	    if (!SwingUtilities.isEventDispatchThread()) {
            final Stac_RespMsg resp=response;
            Callable callable =
                new Callable() {
                    public Object call() throws Exception {
                        handleResponseSynch(resp);
                        return null;
                    }
                };
            FutureTask future = new FutureTask(callable);
            SwingUtilities.invokeLater(future);
            try {
                future.get();
                return;
            } catch (InterruptedException e) {
                //unlikely to happen
              throw new RuntimeException(e);
            } catch (ExecutionException e) {
                //rethrow all exceptions properly
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } /*else if(cause instaceof one the checked exceptions of the method) {
                    throw (this exception)cause;
                } */else {
                    throw new RuntimeException(cause);
                }
             }
         } else {
             handleResponse(response) ;
             return;
         } 
		
	
	}

    
}



