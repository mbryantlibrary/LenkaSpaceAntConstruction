package net.lenkaspace.creeper.model;

import java.util.ArrayList;

/**
 * Maintains an ID and can optionally hold children of the same class
 * 
 * @author      Lenka Pitonakova contact@lenkaspace.net
 * @version     1.0  
 */
public class CRBaseModel {
	
	public int id;
	public ArrayList<CRBaseModel> childModels;
	
	/**
	 * Constructor
	 * @param id_ int unique id
	 */
	public CRBaseModel(int id_) {
		id = id_;
	}
	
	//==================================== SIMULATION EVENTS ====================================
	
	/**
	 * Called by CRController each time a run starts. 
	 * Use this to reset self for a brand new simulation run and call onNewRun of children models.
	 * @param runNumber_ int new run number
	 */
	public void onRunStart(int runNumber_) {
		//-- cascade to children
		if (childModels != null) {
			for (CRBaseModel child : childModels) {
				child.onRunStart(runNumber_);
			}
		}
	}
	
	/**
	 * Called by CRController each time a run ends.
	 * Use this to save any necessary data.
	 * @param runNumber_ int ending run number
	 */
	public void onRunEnd(int runNumber_) {
		//-- cascade to children
		if (childModels != null) {
			for (CRBaseModel child : childModels) {
				child.onRunEnd(runNumber_);
			}
		}
	}
	
	/**
	 * Called by CRController each time a trial starts. 
	 * Use this to reset self for a new world environment and call onNewTrial of children models.
	 * @param trialNumber_ int new trial number
	 * @param runNumber_ int current run number
	 */
	public void onTrialStart(int trialNumber_, int runNumber_) {
		//-- cascade to children
		if (childModels != null) {
			for (CRBaseModel child : childModels) {
				child.onTrialStart(trialNumber_, runNumber_);
			}
		}
	}
	
	/**
	 * Called by CRController each time a trial ends.
	 * Use this to save any necessary data.
	 * @param trialNumber_ int ending trial number
	 * @param runNumber_ int current run number
	 */
	public void onTrialEnd(int trialNumber_, int runNumber_) {
		//-- cascade to children
		if (childModels != null) {
			for (CRBaseModel child : childModels) {
				child.onTrialEnd(trialNumber_, runNumber_);
			}
		}
	}
	
	//==================================== CHILDREN MANIPULATION ================================
	
	/**
	 * Add child model to a maintained list. Use this to automatize cascade of events like onNewRun
	 * or onNewTrial.
	 * @param model_
	 */
	public void addChildModel(CRBaseModel model_) {
		if (childModels == null) {
			childModels = new ArrayList<CRBaseModel>();
		}
		if (!childModels.contains(model_)) {
			childModels.add(model_);
		}
	}
	
	/**
	 * Clear all arrays that store any child objects.
	 * Subclasses should override this to implement clearing of any additional arrays
	 */
	public void clearChildren() {
		if (childModels != null) {
			childModels.clear();
		}
	}
		
	//==================================== GETTERS / SETTERS ====================================
	
	public int getId() { return id;	}
	public void setId(int id) {	this.id = id; }
}
