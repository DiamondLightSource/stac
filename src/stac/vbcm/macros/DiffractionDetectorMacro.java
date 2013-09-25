package stac.vbcm.macros;

import java.util.Vector;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.Material;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

//import org.j3d.aviatrix3d.Appearance;

import stac.core.ParamTable;
import stac.vbcm.macros.MacroInterface;

public class DiffractionDetectorMacro extends BasicMacro implements MacroInterface {

	Transform3D InitTrafo = null;
	Transform3D InitTrafo_inv = null;
	double DetW=1.0,DetH=1.0,DetDist=0.0;
	Transform3D DetTrf = new Transform3D();
	Transform3D ConeTrf = new Transform3D();
	
	public DiffractionDetectorMacro() {
		String[] PreferredMotorNames={"Detector"};
		String[] PreferredDeviceNames={"DETECTOR","CONE"};
	}

	//CONFIG
    ///////////////////////////
	/**
	 * config params:
	 * inittrafo 
	 * width
	 * height
	 */
    public void setConfig(String param) throws Exception {
    	String [] strs=param.split(" ");
    	double [] nums= new double[strs.length];
    	for (int i=0; i<strs.length;i++)
    		nums[i]=new Double(strs[i]).doubleValue();   	
        InitTrafo=new Transform3D(nums);  	
    	InitTrafo_inv=new Transform3D(InitTrafo);
    	InitTrafo_inv.invert();
    	DetW=nums[16];
    	DetH=nums[17];
    }
	
	
    //VIRTUAL MOTORS
    ///////////////////////////
	
	public void moveSingleMotor(String motorName, double Value, boolean absolute) throws Exception {
		//get motorId
		int i;
		for(i=0;i<registeredMotorNames.size();i++) {
			if (motorName.equals((String)registeredMotorNames.elementAt(i)))
				break;
		}
		if (i<registeredMotorNames.size()) {
			//perform motion
			switch (i) {
			case 0: 
				// Detector
				if (absolute)
					DetDist=Value;
				else {
					DetDist+=Value;
				}
				break;
			}
			Transform3D trf=new Transform3D();

			//apply new matrices
			for (int j=0;j<registeredDevices.size();j++) {
				boolean changeTrafo=true;
				switch (j) {
				case 0:
					// "Detector"
					trf=new Transform3D();
					trf.setTranslation(new Vector3d(0,0,DetDist));
					break;
				case 1:
					// "Cone"
					double det=(DetDist==0)?1e-4:DetDist;
					//trf=new Transform3D();
					//trf.setScale(new Vector3d(DetW,DetH,det));
					
					//change the VRML
					changeTrafo=false;
					DiffCone bg=new DiffCone((float)(DetW/2.0), (float)(DetH/2.0), (float)det);
					TransformGroup DeviceTransformgroup=((TransformGroup)(registeredDevices.elementAt(j)));
					frame.changeCollVRML(DeviceTransformgroup,bg);
					
					break;
				}
				
				if (changeTrafo) {
					//TODO: 
					//Prigo calculations are done in a certain lab frame
					//if we transform the object somewhere else,
					//the calculated prigo transformations must also be transformed!
					//apply the inittrafo:
					//inittrafo_again*freshly_calculated*inverse_back
					if (InitTrafo!=null) {
						trf.mul(trf,InitTrafo_inv);
						trf.mul(InitTrafo,trf);
					}

					//apply the new trafo
					((TransformGroup)(registeredDevices.elementAt(j))).setTransform(trf);
				}
			}
		} else {
			//System.out.println("Could not find motor "+motorName);
		}
		
	}
	
	public double getMotorPos(String motorName) throws Exception {
		//get motorId
		int i;
		for(i=0;i<registeredMotorNames.size();i++) {
			if (motorName.equals((String)registeredMotorNames.elementAt(i)))
				break;
		}
		if (i<registeredMotorNames.size()) {
			//perform motion
			switch (i) {
			case 0: 
				// Detector
				return DetDist;
			}
		}
		return 0;
	}


	
	
	//
	//////////////////////////
	public class DiffCone extends BranchGroup {

	    private Shape3D shape;

	    public DiffCone(float dx,float dy,float dist) {
	    	
	    	IndexedQuadArray indexedCube = new IndexedQuadArray(8,
	    			IndexedQuadArray.COORDINATES | IndexedQuadArray.NORMALS, 24);
	    	//The vertex coordinates defined as an array of points.
	    	Point3f[] cubeCoordinates = { 
	    			new Point3f( 1e-5f, 1e-5f, 1e-5f),
	    			new Point3f(-1e-5f, 1e-5f, 1e-5f),
	    			new Point3f(-1e-5f,-1e-5f, 1e-5f),
	    			new Point3f( 1e-5f,-1e-5f, 1e-5f),
	    			new Point3f( dx, dy,dist),
	    			new Point3f(-dx, dy,dist),
	    			new Point3f(-dx,-dy,dist),
	    			new Point3f( dx,-dy,dist)
	    	};
	    	//Define the indices used to reference vertex array
	    	int coordIndices[] = { 
	    			0, 3, 2, 1, 
	    			0, 1, 5, 4, 
	    			1, 2, 6, 5, 
	    			2, 3, 7, 6, 
	    			3, 0, 4, 7, 
	    			7, 4, 5, 6  };
	    	//Set the data
	    	indexedCube.setCoordinates(0, cubeCoordinates);
	    	//indexedCube.setNormals(0, normals);
	    	indexedCube.setCoordinateIndices(0, coordIndices);
	    	//Define an appearance for the shape
	    	Appearance app = new Appearance();
	    	app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.5f));
	    	//app.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 1.0f));

	    	//Create and return the shape
	    	shape= new Shape3D(indexedCube, app);

	        addChild(shape);
	        //compile();
	        
	    }

	}
	
}
