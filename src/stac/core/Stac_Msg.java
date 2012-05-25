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


public abstract class Stac_Msg {
	public Stac_Session session;
	public int msgId;
	//versionId;
	
    public Stac_Msg (Stac_Session session) {
    	this.session=session;
    	msgId=session.getNewMsgId();
    }    

    /**
     * registers the same message under a new Id into the session
     * @return
     * @throws CloneNotSupportedException 
     */
    public Stac_Msg duplicate() throws CloneNotSupportedException {
    	Stac_Msg copy = null;
    	copy=(Stac_Msg)this.clone();
    	copy.msgId=session.getNewMsgId();
    	copy.updateMsg();
    	return copy;
    }
    
    abstract public void updateMsg();
    
    /**
     * Must be implemented by subclass to
     * fetch its content
     * It should not be called directly by
     * other methods!
     * Instead loadMsg can be used
     * @param in
     * @throws IOException
     */
    protected abstract void load(DataInputStream in) throws IOException ;
    /**
     * Must be implemented by subclass to
     * store its content
     * It should not be called directly by
     * other methods!
     * Instead saveMsg can be used
     * @param out
     * @throws IOException
     */
    protected abstract void save(DataOutputStream out) throws IOException ;
    
    /**
     * Generic public function to fetch a message
     * @param in
     * @throws IOException
     */
    public synchronized void loadMsg(DataInputStream in) throws IOException {
    	loadMsgHeader(in);
    	load(in);
    }
    /**
     * Generic public function to store a message
     * @param out
     * @throws IOException
     */
    public synchronized void saveMsg(DataOutputStream out) throws IOException {
    	saveMsgHeader(out);
    	save(out);
    }
    
    /**
     * loads extendable message header
     * @param in
     * @throws IOException
     */
    public void loadMsgHeader(DataInputStream in) throws IOException {
    	msgId=in.readInt();    	
    }
    
    /**
     * stores extendable message header
     * @param out
     * @throws IOException
     */
    public void saveMsgHeader(DataOutputStream out) throws IOException {
    	out.writeInt(msgId);
    }
    
    public void send() {
    	
    }
    
}



