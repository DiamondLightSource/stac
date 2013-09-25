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


public class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    OutputStream os;
    public FileOutputStream fos;
    
    public StreamGobbler(InputStream is, String type)
    {
        this(is, type, (OutputStream)null);
    }

    StreamGobbler(InputStream is, String type, OutputStream redirect)
    {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }
    public StreamGobbler(InputStream is, String type, String redirect) throws FileNotFoundException
    {
        this.is = is;
        this.type = type;
        try {
        	this.fos = new FileOutputStream(redirect);
        } catch (Exception e) {
        }
        this.os = fos;
    }
    
    public void run()
    {
        try
        {
            PrintWriter pw = null;
            if (this.os != null)
                pw = new PrintWriter(os);
                
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
            	if (pw != null) {
            		pw.println(line);
            		pw.flush();
            	}
            	//Stac_Out.println(type + ">" + line);    
            }
            if (pw != null)
                pw.flush();
            
//            BufferedInputStream br = new BufferedInputStream(is);
//            boolean streamEof=false;
//            int len=br.available();
//            long l1 = System.currentTimeMillis();
//            while ( !streamEof )
//            {
////            	if (br.available()>len) {
////            		len=br.available();
////                    l1 = System.currentTimeMillis();
////            	} else {
////                    long l2 = System.currentTimeMillis();
////                    long difference = l2 - l1;
////                    if (difference>2000) {
////                    	streamEof=true;
////                    	if (fos!=null) {
////                    		fos.write(len);
////                    		int value=0;
////                    		while ((value=br.read())!=-1) {
////                    			fos.write(value);
////                    		}
////                    		fos.flush();
////                    		fos.close();
////                    	}
////                    }
////            		
////            	}
//            		
//            	if (br.available()>0) {
//            		int value=br.read();
//            		if (value==-1) {
//            			streamEof=true;
//            		} else if (fos!=null) {
//            			fos.write(value);
//            			fos.flush();
//            		}
//            	}
//            	//Stac_Out.println(type + ">" + line);    
//            }
//            if (fos != null)
//            	fos.flush();
        } catch (IOException ioe)
            {
            	//ioe.printStackTrace();  
            }
    }
}
