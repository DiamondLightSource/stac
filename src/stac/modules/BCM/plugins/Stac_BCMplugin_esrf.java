package stac.modules.BCM.plugins;

import org.sudol.sun.util.*;
import stac.core.*;
import stac.gui.*;
import stac.modules.BCM.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Vector;

import javax.vecmath.Point3d;
//import java.sql.Timestamp;
//import com.braju.format.*;
//import Jama.Matrix;


/**
 * generic class to start external python interpreter to execute
 * a spec communication command using SpecClient
 * 
 */
class spec_communicator {
	Stac_Session session;
	spec_communicator(Stac_Session session) {
		this.session=session;
	}
	
    int spec_exec(String command){
    	int exitVal=1;
    	
        try {
            String osName = System.getProperty("os.name" );
            String[] cmd = new String[3];
            String mycmd = new String("ssh sudol@pc156 \"cd work/dev/STAC/gui/stacgui; export PYTHONPATH=../../thirdparty/SpecClient/SpecClient-dist ; python spec_cmd.py "+command+"\"");

            if( osName.equals( "Windows NT" ) || osName.equals( "Windows XP" ) )
            {
                cmd[0] = "cmd.exe" ;
                cmd[1] = "/C" ;
            }
            else if( osName.equals( "Windows 95" ) )
            {
                cmd[0] = "command.com" ;
                cmd[1] = "/C" ;
            }
            else
            {
                //mycmd="rm STAC_align.vec ; export GNSDEF=/user/sudol/work/test/gns.dat ; $STACDIR/gonset/gonset < $STACDIR/gonset/gonset.todo";
                mycmd="cd "+session.getWorkDir()+" ; export PYTHONPATH="+System.getProperty("STACDIR")+"/thirdparty/SpecClient/SpecClient-dist ; python spec_cmd.py \""+command+"\"";
                cmd[0] = "sh";
                cmd[1] = "-c";
            }
            cmd[2] = mycmd;
            
            {
            	File fp = new File(session.getWorkDir()+"spec_cmd.out");
            	fp.delete();
            }
            
            Runtime rt = Runtime.getRuntime();
            //Stac_Out.println("Executing " + cmd[0] + " " + cmd[1]+ " " + cmd[2]);
            Process proc = rt.exec(cmd);

            try
            {            
              // any error message?
              StreamGobbler errorGobbler = new 
                  StreamGobbler(proc.getErrorStream(), "ERROR");            
              // any output?
              StreamGobbler outputGobbler = new 
                  StreamGobbler(proc.getInputStream(), "OUTPUT","spec_cmd.outj");
              // kick them off
              errorGobbler.start();
              outputGobbler.start();                                
              // any error???
              exitVal = proc.waitFor();        
              errorGobbler.fos.flush();
              outputGobbler.fos.flush();
              
            } catch (Throwable t)
            {
                //t.printStackTrace();
            }
            try {
                // Create channel on the source
                FileChannel srcChannel = new FileInputStream(session.getWorkDir()+"spec_cmd.outj").getChannel();        
                // Create channel on the destination
                FileChannel dstChannel = new FileOutputStream(session.getWorkDir()+"spec_cmd.out").getChannel();
                // Copy file contents from source to destination
                dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
                // Close the channels
                srcChannel.close();
                dstChannel.close();
            } catch (IOException e) {
            }
            
            Stac_Out.println("Process exitValue: " + exitVal);
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
          
          return exitVal;
    	
    }
	
	
}

/**
 * class to execute a spec communication command via
 * handshaking with a third party file checker:
 * PythonListener
 * 
 * @author sudol
 *
 */
class spec_communicator_listener extends spec_communicator {

	double timelimit;
	Process pythonListener;
	StreamGobbler PyListenerError;
	StreamGobbler PyListenerOut;
	
    void start_pythonListener(String command){
        try {
            String osName = System.getProperty("os.name" );
            String[] cmd = new String[3];
            String mycmd = new String("p:\\work\\dev\\STAC\\gonset\\argtest.exe sutike < argtest.log");

            if( osName.equals( "Windows NT" ) || osName.equals( "Windows XP" ) )
            {
                cmd[0] = "cmd.exe" ;
                cmd[1] = "/C" ;
            }
            else if( osName.equals( "Windows 95" ) )
            {
                cmd[0] = "command.com" ;
                cmd[1] = "/C" ;
            }
            else
            {
                //mycmd="rm STAC_align.vec ; export GNSDEF=/user/sudol/work/test/gns.dat ; $STACDIR/gonset/gonset < $STACDIR/gonset/gonset.todo";
                mycmd="cd "+session.getWorkDir()+" ; export PYTHONPATH="+System.getProperty("STACDIR")+"/thirdparty/SpecClient/SpecClient-dist:"+System.getProperty("STACDIR")+"/thirdparty/PythonListener ; python "+System.getProperty("STACDIR")+"/thirdparty/PythonListener/PythonListener.py ./ spec_cmd.py";
                cmd[0] = "sh";
                cmd[1] = "-c";
            }
            cmd[2] = mycmd;
            
            
            Runtime rt = Runtime.getRuntime();
            Stac_Out.println("Executing " + cmd[0] + " " + cmd[1]
                               + " " + cmd[2]);
            pythonListener = rt.exec(cmd);

            // any error???
            int exitVal = 0;
            try
            {            
              // any error message?
              PyListenerError = new 
                  StreamGobbler(pythonListener.getErrorStream(), "ERROR");            
              // any output?
              PyListenerOut = new 
                  StreamGobbler(pythonListener.getInputStream(), "OUTPUT","pythonlistener.out");
              // kick them off
              PyListenerError.start();
              PyListenerOut.start();                                
            } catch (Throwable t)
            {
                //t.printStackTrace();
            }
            
            //Stac_Out.println("Process exitValue: " + exitVal);
          }
          catch (IOException ex) {
            ex.printStackTrace();
          }
    	
    }
    
	
	
	spec_communicator_listener(Stac_Session session,double timelimit) {
		super(session);
		this.timelimit=timelimit;
	}
	
    int spec_exec(String command){
    	int exitVal=1;
    	spec_fastExecPrepare(timelimit);
    	
    	try {
            //send the START signal to pythonlistener
    		{
    			FileWriter dstStream = new FileWriter(session.getWorkDir()+"spec_cmd.py_new");
    			dstStream.write("ready");
    			dstStream.flush();
    			dstStream.close();
    		}
    		boolean alive=false;
    		try{
        		if (pythonListener!=null) {
        			pythonListener.exitValue();
        		}
    		} catch (IllegalThreadStateException e) {
    			alive=true;
    		}
    		if (!alive) {
    			//test: external listener
    			start_pythonListener("");
    		}
    		//check for output
    		File fp;
    		int ct=0;
    		int tsleep=10;
    		int tlimit=(int)(timelimit/tsleep);
    		fp = new File(session.getWorkDir()+"spec_cmd.out_ready");
    		while (!fp.exists() && ct<=tlimit) {
    			Thread.sleep(tsleep);
    			//fp = new File("spec_cmd.out_ready");
    			ct++;
    		} ;
    		if(ct>tlimit)
    			Stac_Out.println("Fast execution with TimeLimit ("+timelimit+") failed");
    		
        	fp = new File(session.getWorkDir()+"spec_cmd.py_new");
        	fp.delete();
        	//check that the flag files has been really removed
        	while (fp.exists() && ct<=tlimit) {
    			Thread.sleep(tsleep);
    			//fp = new File("spec_cmd.py_new");
    			ct++;
    		} ;
    		if(ct>tlimit)
    			Stac_Out.println("Confirmation of the Fast execution with TimeLimit ("+timelimit+") failed");
        	
    		//flush pythonlistener -test
    		PyListenerError.fos.flush();
    		PyListenerOut.fos.flush();
    		
    	} catch (Exception e) {
    	}
    	
    	return exitVal;
    }
    
    void spec_fastExecPrepare(double timelimit){
        try {
        	//delete old output file
            {
        		int ct=0;
        		int tsleep=1;
        		int tlimit=(int)(timelimit/tsleep);
        		
            	File fp = new File(session.getWorkDir()+"spec_cmd.out");
            	fp.delete();
            	//check that the flag files has been really removed
            	while (fp.exists()) {
        			Thread.sleep(tsleep);
        			//fp = new File("spec_cmd.out");
        			ct++;
        		} ; /*&& ct<=tlimit);
        		if(ct>tlimit)
        			Stac_Out.println("Preparation of the Fast execution with TimeLimit ("+timelimit+") failed");
            	*/
        		
            	fp = new File(session.getWorkDir()+"spec_cmd.out_ready");
            	fp.delete();
            	//check that the flag files has been really removed
            	while (fp.exists()) {
        			Thread.sleep(tsleep);
        			//fp = new File("spec_cmd.out_ready");
        			ct++;
        		} ; /*&& ct<=tlimit);
        		if(ct>tlimit)
        			Stac_Out.println("Preparation of the Fast execution with TimeLimit ("+timelimit+") failed");
            	*/
        		
            	fp = new File(session.getWorkDir()+"spec_cmd.py_new");
            	fp.delete();
            	//check that the flag files has been really removed
            	while (fp.exists()) {
        			Thread.sleep(tsleep);
        			//fp = new File("spec_cmd.py_new");
        			ct++;
        		} ; /*&& ct<=tlimit);
        		if(ct>tlimit)
        			Stac_Out.println("Preparation of the Fast execution with TimeLimit ("+timelimit+") failed");
            	*/
        		//Stac_Out.println("preparation time:"+ct);
            }
        } catch (Exception e) {
        }
    	
    }
	
	
}

class STAC_spec_serverD extends Thread {
	Process Stac_spec_serverIO;
	StreamGobbler Stac_spec_server_Error;
	StreamGobbler Stac_spec_server_Out;
	Stac_Session session;
	//Runtime rt;
	
	STAC_spec_serverD(Stac_Session session) {
		this.session=session;
		this.setDaemon(true);
	}
	
	public void stopServer() {
		if (Stac_spec_serverIO!=null)
			Stac_spec_serverIO.destroy();
	}
	
	public void run () {		
        String osName = System.getProperty("os.name" );
        String[] cmd = new String[3];
        String mycmd = new String("p:\\work\\dev\\STAC\\gonset\\argtest.exe sutike < argtest.log");

        if( osName.equals( "Windows NT" ) || osName.equals( "Windows XP" ) )
        {
            cmd[0] = "cmd.exe" ;
            cmd[1] = "/C" ;
        }
        else if( osName.equals( "Windows 95" ) )
        {
            cmd[0] = "command.com" ;
            cmd[1] = "/C" ;
        }
        else
        {
            //mycmd="\"export PYTHONPATH=$STACDIR/thirdparty/SpecClient/SpecClient-dist ; python $STACDIR/thirdparty/STAC_spec_server/STAC_spec_server.py ./ spec_cmd.py\"";
            mycmd="cd "+session.getWorkDir()+" ; export PYTHONPATH="+System.getProperty("STACDIR")+"/thirdparty/SpecClient/SpecClient-dist ; python "+System.getProperty("STACDIR")+"/thirdparty/STAC_spec_server/STAC_spec_server.py ./ spec_cmd.py";
            //mycmd="cd /user/sudol/work/dev/STAC/gui/stacgui; export PYTHONPATH=$STACDIR/thirdparty/SpecClient/SpecClient-dist ; python $STACDIR/thirdparty/STAC_spec_server/STAC_spec_server.py ./ spec_cmd.py > spec_com_resp";
            //mycmd="export PYTHONPATH=$STACDIR/thirdparty/SpecClient/SpecClient-dist ; python -V";
            //mycmd="/user/sudol/work/dev/STAC/gui/stacgui/start_server";
            cmd[0] = "sh";
            cmd[1] = "-c";
        }
        cmd[2] = mycmd;
        

        try {
        if (false) {
        	ProcessBuilder pb = new ProcessBuilder(cmd[0],cmd[1],cmd[2]);
        	////ProcessBuilder pb = new ProcessBuilder("ssh","localhost","cd $STACDIR/gui/stacgui ; /bin/sh -c \""+mycmd+"\"");
        	//ProcessBuilder pb = new ProcessBuilder("xterm","-e","cd $STACDIR/gui/stacgui ; /bin/sh -c \""+mycmd+"\"");
        	//pb.redirectErrorStream(true);
        	Stac_spec_serverIO = pb.start();
        	//Stac_Out.println("Executing " + "ssh localhost cd $STACDIR/gui/stacgui ; /bin/sh -c \""+mycmd+"\"");
        } else {
        	//if JVM loads a lib that is incompatible with the application to be started
        	//it is inherited to the exec environment and therefore will not allow
        	//the child application to run correctly.
        	//in our case python socket reading becomes blocked if startedfrom this environment
        	//Note that there is no problem with this python application if started from a new terminal
        	//or by ssh!
        	//The real problam was a Thread-unsafety bug in SpecClient. As soon as I have upgarded, the problem simply disappeared
        	Runtime rt = Runtime.getRuntime();
        	Stac_Out.println("Executing " + cmd[0] + " " + cmd[1]
        	                                                   + " " + cmd[2]);
        	Stac_spec_serverIO = rt.exec(cmd);
        }
        } catch (Exception e) {}
        
        
        
        
//        //ProcessBuilder pb = new ProcessBuilder(cmd[0],cmd[1],cmd[2]);
//        ProcessBuilder pb = new ProcessBuilder("ssh","localhost","cd $STACDIR/gui/stacgui ; /bin/sh -c \""+mycmd+"\"");
//        pb.redirectErrorStream(true);
//        try {
//			Stac_spec_serverIO = pb.start();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
////        Runtime rt = Runtime.getRuntime();
//        Stac_Out.println("Executing " + cmd[0] + " " + cmd[1]
//                           + " " + cmd[2]);
////        Stac_spec_server = rt.exec(cmd);
////
        // any error???
        int exitVal = 0;
        try
        {            
          // any error message?
        	Stac_spec_server_Error = new 
        	StreamGobbler(Stac_spec_serverIO.getErrorStream(), "ERROR", "spec_com_respE");            
        	// any output?
        	Stac_spec_server_Out = new 
        	StreamGobbler(Stac_spec_serverIO.getInputStream(), "OUTPUT", "spec_com_resp");
        	//kick them off
        	Stac_spec_server_Error.start();
        	Stac_spec_server_Out.start();                                
        } catch (Throwable t)
        {
            t.printStackTrace();
        }
        //while(true) {}
	}
}

/**
 * class to execute a spec communication command via
 * HTTP connection to STAC_spec_server
 * Note that STAC_spec_server is limited to use only 
 * two variables for communicating with spec:
 * m   - specmotor
 * cmd - speccommand
 * the limitation is coming from specclient that
 * does not accept arbitrary independent connections 
 * in the same python session.
 * 
 * @author sudol
 *
 */
class spec_communicator_HTTP extends spec_communicator {

	int Stac_spec_server_port;
	static String Stac_spec_server_URL;
	static Process Stac_spec_server;
	StreamGobbler Stac_spec_server_Error;
	StreamGobbler Stac_spec_server_Out;
	//Stac_Session session;
	STAC_spec_serverD server;
	
	
	static public void stopserver() throws Throwable {
		// Invoke the finalizer of our superclass
		// We haven't discussed superclasses or this syntax yet
		//super.finalize();
		
		if (Stac_spec_server!=null)
			Stac_spec_server.destroy();
		
		// Send kill signal to the started httpd
		BufferedReader dataInStream = null;
		InputStreamReader dataInStream_unbuff =null;
		OutputStreamWriter dataOutStream = null;
		Stac_Out.println("stopping the STACspec_server...");
		try {
			URL myURL = null;
			HttpURLConnection conn = null;
			
			myURL = new URL( Stac_spec_server_URL );
			conn = (HttpURLConnection) myURL.openConnection();
			//conn.setRequestMethod( "QUIT" );
			conn.setRequestMethod( "POST" );
			//conn.set
			conn.setDoInput( true );
			conn.setDoOutput( true );
			//conn.setUseCaches(false);
			dataOutStream = new OutputStreamWriter( conn.getOutputStream() ); //must precede getInputStream!
			dataOutStream.write( "KillServer" ); //sending true means we send event, not opposite
			dataOutStream.flush();
			dataInStream_unbuff = new InputStreamReader( conn.getInputStream() );
			dataInStream = new BufferedReader(dataInStream_unbuff);
			String result = "";
			String line;
			while ( (line = dataInStream.readLine()) != null)
			{
				result=result.concat("\n"+line);
			}
			Stac_Out.println("STACspec_server has stopped");
		} catch( Exception e ) {
			Stac_Out.println( "HTTP exception: " + e );
		} finally {
			if( dataOutStream != null ) {
				try {
					dataOutStream.close();
				} catch( IOException e ) {
				}
			}
			if( dataInStream != null ) {
				try {
					dataInStream.close();
				} catch( IOException e ) {
				}
			}
			if( dataInStream_unbuff != null ) {
				try {
					dataInStream_unbuff.close();
				} catch( IOException e ) {
				}
			}
		}
	}
	
	
    void start_server(String command){
    	
        try {
        	int timelimit=2000;
    		int ct=0;
    		int tsleep=1;
    		int tlimit=(int)(timelimit/tsleep);
    		
        	File fp = new File(session.getWorkDir()+"spec_cmd.port_ready");
        	fp.delete();
        	//check that the flag files has been really removed
        	while (ct<tlimit && fp.exists()) {
    			Thread.sleep(tsleep);
    			ct++;
    		} ;

    		if (true) {
    			//daemon thread
    			server = new STAC_spec_serverD(session);
    			server.start();
    		} else {
    			//simple startup
    			
    			String osName = System.getProperty("os.name" );
    			String[] cmd = new String[3];
    			String mycmd = new String("p:\\work\\dev\\STAC\\gonset\\argtest.exe sutike < argtest.log");
    			
    			if( osName.equals( "Windows NT" ) || osName.equals( "Windows XP" ) )
    			{
    				cmd[0] = "cmd.exe" ;
    				cmd[1] = "/C" ;
    			}
    			else if( osName.equals( "Windows 95" ) )
    			{
    				cmd[0] = "command.com" ;
    				cmd[1] = "/C" ;
    			}
    			else
    			{
    				//mycmd="\"export PYTHONPATH=$STACDIR/thirdparty/SpecClient/SpecClient-dist ; python $STACDIR/thirdparty/STAC_spec_server/STAC_spec_server.py ./ spec_cmd.py\"";
    				mycmd="export PYTHONPATH="+System.getProperty("STACDIR")+"/thirdparty/SpecClient/SpecClient-dist ; python "+System.getProperty("STACDIR")+"/thirdparty/STAC_spec_server/STAC_spec_server.py ./ spec_cmd.py";
    				//mycmd="which python ; ldd /sware/exp/scisoft/Automation/suse82/Program-Files/Python-2.4.2/bin/python";
    				//mycmd="export PYTHONPATH=$STACDIR/thirdparty/SpecClient/SpecClient-dist ; python -V";
    				//mycmd="/user/sudol/work/dev/STAC/gui/stacgui/start_server";
    				cmd[0] = "sh";
    				cmd[1] = "-c";
    			}
    			cmd[2] = mycmd;
    			
    			
    			if (false) {
    				ProcessBuilder pb = new ProcessBuilder(cmd[0],cmd[1],cmd[2]);
    				////ProcessBuilder pb = new ProcessBuilder("ssh","localhost","cd $STACDIR/gui/stacgui ; /bin/sh -c \""+mycmd+"\"");
    				//ProcessBuilder pb = new ProcessBuilder("xterm","-e","cd $STACDIR/gui/stacgui ; /bin/sh -c \""+mycmd+"\"");
    				//pb.redirectErrorStream(true);
    				Stac_spec_server = pb.start();
    				//Stac_Out.println("Executing " + "ssh localhost cd $STACDIR/gui/stacgui ; /bin/sh -c \""+mycmd+"\"");
    			} else {
    				//if JVM loads a lib that is incompatible with the application to be started
    				//it is inherited to the exec environment and therefore will not allow
    				//the child application to run correctly.
    				//in our case python socket reading becomes blocked if startedfrom this environment
    				//Note that there is no problem with this python application if started from a new terminal
    				//or by ssh!
    				//The real problam was a Thread-unsafety bug in SpecClient. As soon as I have upgarded, the problem simply disappeared
    				Runtime rt = Runtime.getRuntime();
    				Stac_Out.println("Executing " + cmd[0] + " " + cmd[1]
    				                                                   + " " + cmd[2]);
    				Stac_spec_server = rt.exec(cmd);
    			}
    			
    			// any error???
    			int exitVal = 0;
    			try
    			{            
    				// any error message?
    				Stac_spec_server_Error = new 
    				StreamGobbler(Stac_spec_server.getErrorStream(), "ERROR", "spec_com_respE");            
    				// any output?
    				Stac_spec_server_Out = new 
    				StreamGobbler(Stac_spec_server.getInputStream(), "OUTPUT", "spec_com_resp");
    				// kick them off
    				Stac_spec_server_Error.start();
    				Stac_spec_server_Out.start();                                
    			} catch (Throwable t)
    			{
    				t.printStackTrace();
    			}
    		}
    		
    		//waitfor port_ready
    		//check that the flag files has been really removed
    		
    		while (ct<tlimit && !fp.exists()) {
    			Thread.sleep(tsleep);
    			ct++;
    		} ;            
    		//read the port (must be prepared before port_ready is written out)
    		Util utils=new Util();
    		Stac_spec_server_URL=utils.opReadCl(session.getWorkDir()+"spec_cmd.port");
    		//Stac_spec_server_URL="http://localhost:8088/";
    		//Stac_spec_server_port=new Integer(portStr).intValue();
    		if (ct>=tlimit) {
    			session.errorMsg("Timeout Error during the initialization of the spec communication!");
    		}

    		Runtime.getRuntime().addShutdownHook(new Thread() {public void run() {try {
    			//setDaemon(true);
				spec_communicator_HTTP.stopserver();
				if (server!=null) {
					server.stopServer();
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}}});
        }
        catch (Exception ex) {
        	session.errorMsg("Error during initialization of the spec communication!");
        }
    	
    }
	
	
	spec_communicator_HTTP(Stac_Session session) {
		super(session);
		start_server("");
		//after starting the server we have to wait a "bit" otherwise 
		//the server(?) becomes locked and does not serve an input stream
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
    synchronized int spec_exec(String command){
    	int exitVal=1;

    	String commandF="";

        URL myURL = null;
        HttpURLConnection conn = null;
        BufferedReader dataInStream = null;
        InputStreamReader dataInStream_unbuff =null;
        OutputStreamWriter dataOutStream = null;
        boolean accepted = false;

        try {
          myURL = new URL( Stac_spec_server_URL );
          conn = (HttpURLConnection) myURL.openConnection();
          conn.setRequestMethod( "POST" );
          conn.setDoInput( true );
          conn.setDoOutput( true );
          conn.setUseCaches(false);
          dataOutStream = new OutputStreamWriter( conn.getOutputStream() ); //must precede getInputStream!
          dataOutStream.write( command ); //sending true means we send event, not opposite
          dataOutStream.flush();
          dataOutStream.close();
          dataInStream_unbuff = new InputStreamReader( conn.getInputStream() );
          dataInStream = new BufferedReader(dataInStream_unbuff);
          String result = "";
          String line;
          while ( (line = dataInStream.readLine()) != null)
          {
              result=result.concat("\n"+line);
          }
          {
	            String outfile=session.getWorkDir()+"spec_cmd.out";
	            // Create channel on the destination
	            FileWriter dstStream = new FileWriter(outfile);
	            // Copy file contents from source to destination
	            dstStream.write(result);
	            // Close the file
	            dstStream.flush();
	            dstStream.close();
	            //debug
	            if(false)
	            {
	            	Stac_Out.println(result);
	            }
          }
          exitVal=0;
        } catch( Exception e ) {
          Stac_Out.println( "HTTP exception: " + e );
        } finally {
          if( dataOutStream != null ) {
            try {
              dataOutStream.close();
            } catch( IOException e ) {
            }
          }
          if( dataInStream != null ) {
              try {
                dataInStream.close();
              } catch( IOException e ) {
              }
            }
          if( dataInStream_unbuff != null ) {
              try {
                dataInStream_unbuff.close();
              } catch( IOException e ) {
              }
            }
        }
        return exitVal;
    	
    }


	
	
}


/**
 * 
 * esrf: BCM plugin for spec at ESRF
 * It is using the spec user values ONLY, but gives a base class for
 * all further spec plugins
 * NOTE: that this plugin is SETting the user values instead of managing
 * offsets between the spec and STAC values   
 *
 * @author sudol
 *
 */

public class Stac_BCMplugin_esrf extends Stac_BCMplugin_base implements Stac_BCMplugin {
	/*
	 * motorDescriptor:
	 * eg:
	 * motorName   motorName  multiplication factor 
	 * (in STAC)   (in spec)  (specValue=StacValue*f)
	 * 
	 *     X         sampx           -1
	 *     Y         sampy            1
	 *     Z         phiy             1   
	 *    Omega      phi              1   
	 *    Kappa      kap1             1   
	 *    Phi        kap2             1   
	 * 
	 * remark:
	 * mulfac assumes that calibration has been done, and the
	 * motors are aligned to the lab axes, otherwise
	 * [XYZ] <-> [spec translation] would require a 3d transformation
	 * 
	 */
	//ParamTable motorDescriptor=config.Descriptor;
	String     specversion;
	final int specName =0;
	final int specMulf =1;
	
	String spec_mv() {
		return new String("mv");
	}
	String spec_set() {
		return new String("set");
	}
	String spec_getPosition() {
		return new String("getPosition");
	}
	String ProDC_MotorNameSuffix() {
		return new String("");		
	}
	
	
	//final boolean fastexec=false;
	//final boolean fastexec=true;
	
	//spec_communicator spec_comm = new spec_communicator();
	//spec_communicator_listener spec_comm = new spec_communicator_listener(10000);
	spec_communicator_HTTP spec_comm = null;//new spec_communicator_HTTP();
	
	
    public Stac_BCMplugin_esrf () {}
    
	@Override
	public String getCreditString() {
		// TODO Auto-generated method stub
		return "using BCM-esrf from S. Brockhauser";
	}
    
	/**
	 * at ESRF Minikappa the kappa angle is limited between 0-240
	 */
	public boolean checkDatumTrans(Point3d dat, Point3d trans) {
		//at ESRF, MK has these limits:
		if (dat.y<0 || dat.y>240)
			return false;
		//at ESRF, 
		//+ there are no software limits set correctly for Centring Table Motors
		//+ spec connection is very slow to get full motor params
		//  that should be changed to remember orig settings, 
		//  and do not ask these params always
		//return super.checkDatumTrans(dat, trans);
		return true;
	}
   
    @Override
    public void initPlugin() {
    	spec_comm = new spec_communicator_HTTP(session);
    	
    	
    	//read the actual motorDescriptor
    	//motorDescriptor= new ParamTable();
    	//read the actual motorDescriptor
//    	{
//    		//X motor
//    		{
//    			String name ="X";
//    			Vector params = new Vector();
//    			String localid ="sampx";                 params.addElement(localid);
//    			Double mulfac = new Double(-1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Y motor
//    		{
//    			String name ="Y";
//    			Vector params = new Vector();
//    			String localid ="sampy";                 params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Z motor
//    		{
//    			String name ="Z";
//    			Vector params = new Vector();
//    			String localid ="phiy";                 params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Omega motor
//    		{
//    			String name ="Omega";
//    			Vector params = new Vector();
//    			String localid ="phi";                   params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Kappa motor
//    		{
//    			String name ="Kappa";
//    			Vector params = new Vector();
//    			String localid ="kap1";                  params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    		//Phi motor
//    		{
//    			String name ="Phi";
//    			Vector params = new Vector();
//    			String localid ="kap2";                  params.addElement(localid);
//    			Double mulfac = new Double( 1.);         params.addElement(mulfac);
//    			motorDescriptor.setValueList(name,params);
//    		}
//    	}
    	//read the specversion
    	//read_specdef(System.getProperty("BCMDEF"));
    	config.getConfiguration();
    	try {
    		specversion=config.Descriptor.getFirstStringValue("SPECVERSION");
    		//specversion="artemis2:sandor";
    		//specversion="artemis2:eh4";
    	} catch (Exception e) {
    	}
    }

//    public void read_specdef(String specfile) {
//      	String corr;
//      	Util utils=new Util();
//        //read spec.dat
//      	corr = utils.opReadCl(specfile);
//        String tmp1[] = corr.split("\n");
//        for (int l=0;l<tmp1.length;l++) {
//        	if (tmp1[l].startsWith("#",0))
//        		continue;
//        	String tmp2[] = tmp1[l].split("\\s+");
//        	if(tmp2.length==0)
//        		continue;
//        	int offset=0;
//        	if (tmp2[0].length()==0)
//        		offset++;
//			String name = tmp2[0+offset];
//			if (name.equals("SPECVERSION")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else if (name.equals("BCM_Plugin")) {
//				String value =tmp2[1+offset];
//				motorDescriptor.setSingleValue(name,value);
//			} else if (name.equals("INITSTRING")) {
//				String value=new String("");
//				for(int i=1+offset;i<tmp2.length;i++){
//					value=value.concat(tmp2[i]+" ");
//				}
//				motorDescriptor.setSingleValue(name,value);
//			} else {
//				Vector params = new Vector();
//				String localid =tmp2[1+offset];            params.addElement(localid);
//				Double mulfac = new Double(tmp2[2+offset]);params.addElement(mulfac);
//				motorDescriptor.setValueList(name,params);				
//			}
//        }
//      	
//      }
//    
    
    
    
    void spec_cmd(String command){
    	//if(fastexec)
    	//	spec_fastExecPrepare(1000);
        String outfile;
        String PyCommand=new String();
        try {            
            outfile=session.getWorkDir()+"spec_cmd.py";
            // Create channel on the destination
            FileWriter dstStream = new FileWriter(outfile);
            // Copy file contents from source to destination
            PyCommand=new PrintfFormat("#! /usr/bin/env python\n"+
            		"import sys\n"+
					"from SpecClient import *\n"+
					"try:\n"+
					"  cmd=SpecCommand.SpecCommand('','%s',500)\n"+
					"  cmd=SpecCommand.SpecCommand('','%s')\n"+
					"  cmd.executeCommand(\"%s\")\n"+
					"except:\n"+
					"  print 'Could not connect to the spec server!'\n"
					).sprintf(new Object[] {specversion,specversion,command});
            dstStream.write(PyCommand);
            // Close the file
            dstStream.flush();
            dstStream.close();
        } catch (IOException e) {
        	Stac_Out.println("Problem with generating the spec_cmd.py file");
        }
    	Stac_Out.println("Sending Spec Command ("+command+")");
        //spec_exec(command);
    	int exitval=1;
    	for(int i=0;i<3 && exitval!=0;i++)
   			exitval=spec_comm.spec_exec(PyCommand);
    }
    
    void spec_getMotor(String motorName){
    	//if(fastexec)
    	//	spec_fastExecPrepare(1000);
        String outfile;
        String PyCommand= new String();
        try {
            outfile=session.getWorkDir()+"spec_cmd.py";
            // Create channel on the destination
            FileWriter dstStream = new FileWriter(outfile);
            // Copy file contents from source to destination
            PyCommand=new PrintfFormat("#! /usr/bin/env python\n"+
                		"import sys\n"+
    					"from SpecClient import *\n"+
    					"m=SpecMotor.SpecMotor('%s','%s',500)\n"+
    					"p=m."+spec_getPosition()+"()\n"+
    					"print 'position_of_the_specified_motor_is: ',p\n"+
    					"cmd=SpecCommand.SpecCommand('','%s',500)\n"+
    					"print 'speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"velocity\")')\n"+
    					"print 'lowest_speed_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"base_rate\")')\n"+
    					"print 'stepsize_of_the_specified_motor_is: ' , cmd.executeCommand('motor_par(%s,\"step_size\")')\n"+
    					"print 'low_limit_of_the_specified_motor_is: ',cmd.executeCommand('"+spec_to_plugin_value(motorName,"get_lim(%s,-1)")+"')\n"+
    					"print 'high_limit_of_the_specified_motor_is: ',cmd.executeCommand('"+spec_to_plugin_value(motorName,"get_lim(%s,1)")+"')\n"+
    					"print 'name of the specified motor is: ','%s'").sprintf(new Object[] {motorName,specversion,specversion,motorName,motorName,motorName,motorName,motorName,motorName});
            dstStream.write(PyCommand);
            // Close the file
            dstStream.flush();
            dstStream.close();
        } catch (IOException e) {
        	Stac_Out.println("Problem with generating the spec_cmd.py file");
        }
    	Stac_Out.println("Get MotorProperties ("+motorName+")");
    	int exitval=1;
    	for(int i=0;i<3 && exitval!=0;i++)
   			exitval=spec_comm.spec_exec(PyCommand);
    }

    String getParam(String mask) throws IOException {
    	String value=new String("0");
    	boolean set=false;
        //read the results from spec_cmd.out
      	String corr;
      	Util utils=new Util();
        //read spec.dat
      	corr = utils.opReadCl(session.getWorkDir()+"spec_cmd.out");
        String tmp1[] = corr.split("\n");
        for (int l=0;l<tmp1.length;l++) {
        	if (tmp1[l].startsWith("#",0))
        		continue;
        	String tmp2[] = tmp1[l].split("\\s+");
        	int offset=0;
        	//if (tmp2[0].length()==0)
        	//	offset++;
			String name = tmp2[0+offset];
			if (name.equals(mask)) {
				value=tmp2[1+offset];
				set=true;
			}
        }
        if(!set) {
        	throw new IOException("Failed to read spec parameter");
        }
        
        return value;
    }
    
    double spec_getMotorParam(String motorName,String mask){
    	String pos=null;
    	int exitval=1;
    	for(int i=0;i<3 && exitval!=0;i++) {
    			try {
    		    	spec_getMotor(motorName);
					pos=getParam(mask);
					exitval=0;
				} catch (IOException e) {
					Stac_Out.println(e.getMessage());
					pos=new String("0");
				}
    	}
    	return new Double(pos).doubleValue();
    }
    
    public double getMotorPosition(String motorName) throws Exception {
    	return convertMotorPosition_Plugin2Stac(motorName,spec_getMotorParam(config.Descriptor.getFirstStringValue(motorName),"position_of_the_specified_motor_is:"));
    }
    
    public void setMotorPosition(String motorName,double newValue) throws Exception {
    	spec_cmd(spec_set()+" "+config.Descriptor.getFirstStringValue(motorName)+" "+newValue*config.Descriptor.getDoubleValueAt(motorName,specMulf));
    	return;
    }
    
    public void moveMotor(String motorName,double newValue) throws Exception {
    	spec_cmd(spec_mv()+" "+config.Descriptor.getFirstStringValue(motorName)+" "+convertMotorPosition_Stac2Plugin(motorName,newValue));
    	return;
    }    
    
    //we possibily can give speed/step_size back to be compatible with other systems, 
    //like getMotorParams!
    public double getMotorSpeed(String motorName) throws Exception {
    	return spec_getMotorParam(config.Descriptor.getFirstStringValue(motorName),"speed_of_the_specified_motor_is:")/config.Descriptor.getDoubleValueAt(motorName,specMulf);
    }
    
    //standard values served:
    // + pos
    // + minpos
    // + maxpos
    // + speed
    // + minspeed
    // additionally we serve the spec-specific value:
    // - steps
    public ParamTable getMotorParams(String motorName) throws Exception {
    	ParamTable params=new ParamTable();
    	spec_getMotor(config.Descriptor.getFirstStringValue(motorName));
    	double minpos=convertMotorPosition_Plugin2Stac(motorName,new Double(getParam("low_limit_of_the_specified_motor_is:")).doubleValue());
    	double maxpos=convertMotorPosition_Plugin2Stac(motorName,new Double(getParam("high_limit_of_the_specified_motor_is:")).doubleValue());
		params.setSingleDoubleValue("pos",convertMotorPosition_Plugin2Stac(motorName,new Double(getParam("position_of_the_specified_motor_is:")).doubleValue()));
		params.setSingleDoubleValue("minpos",Math.min(minpos,maxpos));
		params.setSingleDoubleValue("maxpos",Math.max(minpos,maxpos));
		params.setSingleDoubleValue("speed",new Double(getParam("speed_of_the_specified_motor_is:")).doubleValue()/new Double(getParam("stepsize_of_the_specified_motor_is:")).doubleValue());
		params.setSingleDoubleValue("minspeed",new Double(getParam("lowest_speed_of_the_specified_motor_is:")).doubleValue()/new Double(getParam("stepsize_of_the_specified_motor_is:")).doubleValue());
		params.setSingleDoubleValue("steps",new Double(getParam("stepsize_of_the_specified_motor_is:")).doubleValue());
    	return params;
    }
    
    public double getMotorSpeedLowLimit(String motorName) throws Exception {
    	return spec_getMotorParam(config.Descriptor.getFirstStringValue(motorName),"lowest_speed_of_the_specified_motor_is:")/config.Descriptor.getDoubleValueAt(motorName,specMulf);
    }
    
    public void setMotorSpeed(String motorName,double newValue) throws Exception {
    	spec_cmd("motor_par("+config.Descriptor.getFirstStringValue(motorName)+",'velocity',"+newValue+")");
    	return;
    }
    
    public void moveMotors(ParamTable newPositions) throws Exception {
    	String cmd = "";
    	for(int i=0;i<newPositions.pnames.size();i++){
    		if (i!=0) {
    			cmd=cmd.concat(";");
    		}
    		String motorName = (String)newPositions.pnames.elementAt(i);
    		double newValue  = newPositions.getFirstDoubleValue(motorName);
    		//cmd=cmd.concat("mv "+motorDescriptor.getFirstStringValue(motorName)+" "+newValue*motorDescriptor.getDoubleValueAt(motorName,specMulf));
    		cmd=cmd.concat("A["+config.Descriptor.getFirstStringValue(motorName)+"]="+spec_A_value(config.Descriptor.getFirstStringValue(motorName),convertMotorPosition_Stac2Plugin(motorName,newValue)));
    	}
	    cmd=cmd.concat(";move_all;wait(0)");
    	spec_cmd(cmd);
    	return;
    	
    }
    
    /**
     * converts the standard plugin input (here, it is the user value) to user value
     */
	public String spec_A_value (String specMotorName, double value) {
		String A_value= new String(""+value);
		return A_value;
	}
	
	/**
	 * converts the dial input to the standard plugin value (here, it is user)
	 */
	public String spec_to_plugin_value (String specMotorName, String value) {
		String plugin_value= new String("user("+specMotorName+","+value+")");
		return plugin_value;
	}
    
    public void moveMotorsSyncronized_old(ParamTable newPositions) throws Exception {
    	//move motors
    	moveMotors(newPositions);
    	
    	return;
    	
    }
    
    public void moveMotorsSyncronized(ParamTable newPositions) throws Exception {
    	String cmd = "";
		cmd=config.Descriptor.getFirstStringValue("MOVESYNC");
    	if (cmd==null || cmd.length()==0) {
    		moveMotorsSyncronized_stepbystep(newPositions);
    	} else {
    		cmd=cmd+"('";
        	for(int i=0;i<newPositions.pnames.size();i++){
        		String motorName = (String)newPositions.pnames.elementAt(i);
        		cmd=cmd+config.Descriptor.getFirstStringValue(motorName)+" ";
        	}
    		cmd=cmd+"','";
        	for(int i=0;i<newPositions.pnames.size();i++){
        		String motorName = (String)newPositions.pnames.elementAt(i);
        		double newValue = newPositions.getFirstDoubleValue(motorName);
        		String newpos = spec_A_value(config.Descriptor.getFirstStringValue(motorName),convertMotorPosition_Stac2Plugin(motorName,newValue));
        		cmd=cmd+newpos+" ";
        	}
    		cmd=cmd+"')";
    		spec_cmd(cmd);
    	}
    
    }
    
    public void moveMotorsSyncronized_stepbystep(ParamTable newPositions) throws Exception {
        //calculate avg speed
    	//stepsize=step/distance
    	//velocity=step/time
    	//speed=distance/time=velocity/stepsize
    	//time=abs(newpos-oldpos)/abs(speed)=abs(newpos-oldpos)/abs(velocity/stepsize)=abs(stepsize)*abs(newpos-oldpos)/abs(velocity)
    	//velocity_new=abs(stepsize)*abs(newpos-oldpos)/time_max*sgn(velocity)
    	//so we ca store: A=abs(newpos-oldpos)*abs(stepsize)
    	//time=A/abs(velocity)
    	//velocity_new=A/time_max*sgn(velocity)
    	
    	ParamTable oldvalues = new ParamTable();
        //get motor distances
        //get (and store) original motor speeds
    	double time=0;
    	for(int i=0;i<newPositions.pnames.size();i++){
    		String motorName = (String)newPositions.pnames.elementAt(i);
        	String pos=new String("0");
        	String steps=new String("0");
        	String vel=new String("0");
        	String velLimit=new String("0");
        	int exitval=1;
        	for(int j=0;j<3 && exitval!=0;j++) {
    			try {
    	        	spec_getMotor(config.Descriptor.getFirstStringValue(motorName));
    				pos=getParam("position_of_the_specified_motor_is:");
            		steps=getParam("stepsize_of_the_specified_motor_is:");
            		vel=getParam("speed_of_the_specified_motor_is:");
            		velLimit=getParam("lowest_speed_of_the_specified_motor_is:");
    				exitval=0;
    			} catch (IOException e) {
    				Stac_Out.println(e.getMessage());
    			}        		
        	}
        	double oldpos=convertMotorPosition_Plugin2Stac(motorName,new Double(pos).doubleValue());
        	double newpos=newPositions.getFirstDoubleValue(motorName);
        	double stepsize=new Double(steps).doubleValue()*config.Descriptor.getDoubleValueAt(motorName,specMulf);
        	double velocity=new Double(vel).doubleValue();
        	double velocityLimit=new Double(velLimit).doubleValue();
        	double A=Math.abs(newpos-oldpos)*Math.abs(stepsize);
        	oldvalues.setDoubleValueList(motorName, new double[] {velocity,A,velocityLimit});
        	time=Math.max(time,A/Math.abs(velocity));
    	}
    	double time_max=time;
        //get motor limits
        //calculate and set speeds
    	for(int i=0;i<newPositions.pnames.size();i++){
    		String motorName = (String)newPositions.pnames.elementAt(i);
        	double velocity=oldvalues.getDoubleValueAt(motorName,0);//get velocity
        	double A=oldvalues.getDoubleValueAt(motorName,1);//get A
        	double velocityLimit=oldvalues.getDoubleValueAt(motorName,2);//get velocityLimit
        	//since velocity is treated as integer, we make it bigger then 1
        	double velocity_new=Math.max(A/time_max,1)*((velocity<0)?(-1):(1));
        	setMotorSpeed(motorName,Math.min(velocity,Math.max(velocity_new,velocityLimit)));
    	}
    	//move motors
    	moveMotors(newPositions);
    	//set motor speeds back
    	for(int i=0;i<newPositions.pnames.size();i++){
    		String motorName = (String)newPositions.pnames.elementAt(i);
        	double velocity=oldvalues.getDoubleValueAt(motorName,0);//get velocity
        	setMotorSpeed(motorName,velocity);
    	}
    	
    	return;
    	
    }
    
    public void initMotors() throws Exception {
    	String cmd = config.Descriptor.getFirstStringValue("INITSTRING");
    	spec_cmd(cmd);
    	//setMotorPosition("Omega",0.0);
    	//setMotorPosition("Kappa",0.0);
    	//setMotorPosition("Phi",0.0);
    	return;
    }
    
    public double convertMotorPosition_Plugin2Stac(String motorName,double pluginPos) {
    	double stacPos=pluginPos/config.Descriptor.getDoubleValueAt(motorName,specMulf);
    	return stacPos;
    }
    
    public double convertMotorPosition_Stac2Plugin(String motorName,double stacPos) {
    	double pluginPos=stacPos*config.Descriptor.getDoubleValueAt(motorName,specMulf);
    	return pluginPos;
    }
	/* (non-Javadoc)
	 * @see stacgui.Stac_BCMplugin#loadMotorPosition(java.lang.String, java.lang.String)
	 */
	public double loadMotorPosition(String motorName, String data) {
		String spec_motorName= new String(config.Descriptor.getFirstStringValue(motorName));
        String tmp1[] = data.split("\n");
        for (int l=0;l<tmp1.length;l++) {
        	if (tmp1[l].startsWith("#",0))
        		continue;
        	String tmp2[] = tmp1[l].split("\\s+");
        	if(tmp2.length==0)
        		continue;
        	int offset=0;
        	if (tmp2[0].length()==0)
        		offset++;
        	if(offset>=tmp2.length)
        		continue;
			String name = tmp2[0+offset];
			{
				String value =tmp2[1+offset];
				if (name.equalsIgnoreCase(spec_motorName+ProDC_MotorNameSuffix())){
					return convertMotorPosition_Plugin2Stac(motorName,new Double(value).doubleValue());
				}
			}
        }
		
		return 0;
	}
	/* (non-Javadoc)
	 * @see stacgui.Stac_BCMplugin#saveMotorPositions(stacgui.ParamTable, java.lang.String)
	 */
	public String  saveMotorPositions(ParamTable positions, String data) {
    	for(int i=0;i<positions.pnames.size();i++){
    		String motorName = (String)positions.pnames.elementAt(i);
    		double newValue  = positions.getFirstDoubleValue(motorName);
    		data=data.concat(config.Descriptor.getFirstStringValue(motorName)+"  "+convertMotorPosition_Stac2Plugin(motorName,newValue)+"\n");
    	}
    	return data;
	}
    
    
}


