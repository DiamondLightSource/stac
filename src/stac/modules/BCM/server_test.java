package stac.modules.BCM;

import stac.core.*;


import java.io.File;


class server_test {

	
    server_test(){
    	
        try {
    		int ct=0;
    		int tsleep=1;
    		//int tlimit=(int)(timelimit/tsleep);
    		
        	File fp = new File("spec_cmd.port_ready");
        	fp.delete();
        	//check that the flag files has been really removed
        	while (fp.exists()) {
    			Thread.sleep(tsleep);
    			ct++;
    		} ;
    		
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
                mycmd="export PYTHONPATH="+System.getProperty("STACDIR")+"/thirdparty/SpecClient/SpecClient-dist ; python "+System.getProperty("STACDIR")+"/thirdparty/STAC_spec_server/STAC_spec_server.py ./ spec_cmd.py";
                cmd[0] = "sh";
                cmd[1] = "-c";
            }
            cmd[2] = mycmd;
            
            
            Runtime rt = Runtime.getRuntime();
            Stac_Out.println("Executing " + cmd[0] + " " + cmd[1]
                               + " " + cmd[2]);
            Process Stac_spec_server = rt.exec(cmd);
        	StreamGobbler Stac_spec_server_Error; 
        	StreamGobbler Stac_spec_server_Out; 

            // any error???
            try
            {            
              // any error message?
            	Stac_spec_server_Error = new 
                  StreamGobbler(Stac_spec_server.getErrorStream(), "ERROR");            
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
            
            //waitfor port_ready
        	//check that the flag files has been really removed
        	while (!fp.exists()) {
    			Thread.sleep(tsleep);
    			ct++;
    		} ;            
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
          Stac_Out.println("Test is OK!");
    	
    }
	
    /**
     * Main method
     *
     * @param args String[]
     */
    public static void main(String[] args) {
      new server_test();
    }
	
	
}

