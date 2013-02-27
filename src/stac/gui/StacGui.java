package stac.gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import stac.core.Stac_RespMsg;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class MainFrame extends JFrame {
	/**
	 * Overridden so we can exit when window is closed
	 *
	 * @param e WindowEvent
	 */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			System.exit(0);
		}
	}
	
}


public class StacGui {
  boolean packFrame = false;
  public MainFrame frame;
  static StacMainWin main_win;

  /**
   * Construct the application
   */

  public StacGui() {
	  this(false);
  }
  public StacGui(boolean verbose) {
	  
	  //create Stac_session...
    frame = new MainFrame();
    
    frame.setTitle("STAC: STrategy for Aligned Crystals");
    frame.setSize(new Dimension(1000, 700));

    main_win = new StacMainWin();
    main_win.session.setVerbose(verbose);
    frame.add(main_win);
    frame.setTitle("STAC: STrategy for Aligned Crystals "+main_win.getVersion());
    
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation( (screenSize.width - frameSize.width) / 2,
                      (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  /**
   * Main method
   * synchronized method:
   * it is SWING specific by telling swing that a response has been calculated and
   * we are ready to tell the results in handleResponse
   *
   * @param args String[]
   */
  public static void main(String[] args) {

	  if (args!=null) {
		  for (int i=0;i<args.length;i++) {
			  if (args[i].equalsIgnoreCase("--help")) {
				  System.out.println(" command line options:");
				  System.out.println("  -d <file>    : load the descriptor file");
				  System.out.println("  -o <file>    : load the OM file (denzo:.x;mosflm:.par;xds:.LP)");
				  System.out.println("  -l           : load only and do not autoStart the processing with default CELL options");
				  System.out.println("  -cell        : process with CELL option");
				  System.out.println("  -anom        : process with ANOMALOUS option");
				  System.out.println("  -spot        : process with SMART SPOT SEPARATION option");
				  System.out.println("  -mino        : process with MIN OVERALL OSCILLATION option");
				  System.out.println("  -refo <file> : process with REFERENCE ORIENTATION option using the ref. file");
			  }
		  }
	  }
  
	  
	  
	    if (!SwingUtilities.isEventDispatchThread()) {
            final String[] argsF=args;
            Callable callable =
                new Callable() {
                    public Object call() throws Exception {
                    	startGUI(argsF);
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
        	 startGUI(args);
         } 
		
	  
	  
	  
  }
  
  
	  
  public static void startGUI(String[] args) {
	  
	try {
      //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    if (args!=null && args.length>=1 && args[0].equalsIgnoreCase("-v")) {
        new StacGui(true);    	
    } else {
    	new StacGui();
    }    
    if (args!=null) {
    	//set up autoprocess
    	boolean autoProcess=true;
    	for (int i=0;i<args.length;i++) {
			if (args[i].equalsIgnoreCase("-l")) {
				autoProcess=false;
			}
    	}
    	for (int i=0;i<args.length;i++) {
    		boolean process=false;
    		try {
				if (args[i].equalsIgnoreCase("-d")) {
					i++;
				    //set up requested descriptor filename
					main_win.jTextFieldDescDefFile.setText(args[i]);
					//load it
					main_win.jButtonDescLoad.doClick();
					process=autoProcess;
				} else if (args[i].equalsIgnoreCase("-o")) {
					i++;
				    //set up requested orientation matrix filename
					main_win.jTextFieldDescDefFile.setText(args[i]);
				    //change the appropriate field
					main_win.jTextFieldDefFile.setText(args[i]);
					if (args[i].endsWith(".x")) {
						main_win.jRadioButtonDenzo.doClick();
					} else  if (args[i].endsWith(".par")) {
						main_win.jRadioButtonMosflm.doClick();
					} else  if (args[i].endsWith(".LP")) {
						main_win.jRadioButtonXDS.doClick();
					}
					process=autoProcess;
				} else if (args[i].equalsIgnoreCase("-cell")) {
					main_win.jPanelReorient.jButtonSTD.doClick();
					process=true;
				} else if (args[i].equalsIgnoreCase("-anom")) {
					main_win.jPanelReorient.jButtonSAD.doClick();
					process=true;
				} else if (args[i].equalsIgnoreCase("-spot")) {
					main_win.jPanelReorient.jButtonSSS.doClick();
					process=true;
				} else if (args[i].equalsIgnoreCase("-mino")) {
					//may not work if optalign is handled async
					main_win.jPanelReorient.jButtonOptAlign.doClick();
					process=true;
				} else if (args[i].equalsIgnoreCase("-refo")) {
					//set up reference
					i++;
					main_win.jPanelReorient.jTextFieldRGF.setText(args[i]);
					//action
					main_win.jPanelReorient.jButtonRLeftGetFile.doClick();
					process=true;
				}
	    		if (process) {
	    		    //default action sequence
	    		    //=======================
	    		    //we keep GUI feedback functionality (error popups, etc.)
	    		    //load file and get std orientation request
	    		    main_win.jButton1_1.doClick();
	    		    //get the first solution selected
	    		    //send strategy request
	    		}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
  }
}
