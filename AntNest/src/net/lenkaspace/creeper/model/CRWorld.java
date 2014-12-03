package net.lenkaspace.creeper.model;

import java.util.ArrayList;

import net.lenkaspace.creeper.CRController;
import net.lenkaspace.creeper.vo.CRVector3d;

/**
 * Represents a world and maintains all sublcasses of CRBaseSituatedObject
 * 
 * @author      Lenka Pitonakova contact@lenkaspace.net
 * @version     1.0                                      
 */
public class CRWorld extends CRBaseModel  {
	
	protected CRController controller;
	protected CRVector3d size;
	protected boolean isBorderless; //if true, an object reaching edge of the world will appear on the opposite edge.

	private ArrayList<CRBaseSituatedModel> situatedModels; // CRBaseSituatedModels that can be rendered and hit
	private ArrayList<CRBaseDynamicModel> dynamicModels; //CRBaseDynamicModels that can be renderer, hit and move
	
	/**
	 * Constructor.
	 * @param id_ int unique id
	 * @param size_ CRVector3d size [width, height, depth]
	 * @param controller_ CRController holding controller instance
	 */
	public CRWorld(int id_, CRVector3d size_, CRController controller_) {
		super(id_);
		controller = controller_;
		situatedModels = new ArrayList<CRBaseSituatedModel>();
		dynamicModels = new ArrayList<CRBaseDynamicModel>();
		size = new CRVector3d(size_);
		isBorderless = true;
	}
	
	/**
	 * Main update function. Called from the mainThread of CRController. Update all dynamicModels
	 */
	public void update() {
		try {
			for (CRBaseDynamicModel dynamicModel : dynamicModels) {
				dynamicModel.onUpdateLoopStart();
			}
			for (CRBaseDynamicModel dynamicModel : dynamicModels) {
				dynamicModel.update();
			}
			for (CRBaseDynamicModel dynamicModel : dynamicModels) {
				dynamicModel.onUpdateLoopEnd();
			}
		} catch (Exception e) {
			if (e.getMessage() != null) {
				System.err.println(e.getMessage());
			}
			System.err.println(e.toString());
		}
	}
	
	
	//==================================== OBJECT MANIPULATION ==================================
	/**
	 * Add a CRBaseSituatedModel to the list of situatedModels. Sets world pointer of the situatedModel to this.
	 * @param situatedModel_ CRBaseSituatedModel to add
	 */
	public void addSituatedModel(CRBaseSituatedModel situatedModel_) {
		if (!situatedModels.contains(situatedModel_)) {
			//-- add to child models
			addChildModel(situatedModel_);
			//-- set its world to this
			situatedModel_.setWorld(this);
			//-- add it to the list
			situatedModels.add(situatedModel_);			
		}
	}
	
	/**
	 * Add a CRBaseDynamicModel to the list of dynamicModels and situatedModels. Sets world pointer of the dynamicModel to this.
	 * @param dynamicModel_ CRBaseDynamicModel to add
	 */
	public void addDynamicModel(CRBaseDynamicModel dynamicModel_) {
		if (!dynamicModels.contains(dynamicModel_)) {
			//-- add to the situated models, this will also set the world pointer of the objects to this.
			this.addSituatedModel(dynamicModel_);
			//-- add to the dynamic models
			dynamicModels.add(dynamicModel_);
		}
	}
	
	/**
	 * Clear all arrays that store any child objects.
	 * Subclasses should override this to implement clearing of any additional arrays
	 */
	public void clearChildren() {
		if (dynamicModels != null) {
			dynamicModels.clear();
		}
		if (situatedModels != null) {
			situatedModels.clear();
		}
		super.clearChildren();
	}

	//==================================== GETTERS / SETTERS ====================================
	public ArrayList<CRBaseSituatedModel> getSituatedModels() { return situatedModels; }
	public void setSituatedModels(ArrayList<CRBaseSituatedModel> situatedModels) { this.situatedModels = situatedModels; }
	
	public ArrayList<CRBaseDynamicModel> getDynamicModels() { return dynamicModels; }
	public void setDynamicModels(ArrayList<CRBaseDynamicModel> situatedModels) { this.dynamicModels = situatedModels; }

	public CRVector3d getSize() { return size; }
	public void setSize(CRVector3d size_) { size = new CRVector3d(size_); }
	

	public boolean isBorderless() { return isBorderless; }
	public void setBorderless(boolean isBorderless) { this.isBorderless = isBorderless;	}
	
	public CRController getController() { return controller; }
	
}
