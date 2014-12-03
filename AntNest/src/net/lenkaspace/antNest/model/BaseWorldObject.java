package net.lenkaspace.antNest.model;

import java.util.ArrayList;

import net.lenkaspace.creeper.model.CRBaseDynamicModel;
import net.lenkaspace.creeper.model.CRBaseSituatedModel;
import net.lenkaspace.creeper.vo.CRVector3d;

public class BaseWorldObject extends CRBaseDynamicModel {
	
	protected double resistance;
	protected double maxPushAngleFraction; // max difference in x component of relative vector towards object to be pushed
	protected double basePickUpProbability; //probability that object in the front will be pushed
	private boolean isBeingPushed;
	
	
	public BaseWorldObject(double resistance_, int id_, CRVector3d pos_, CRVector3d size_, double rotation_, String imageName_) {
		super(id_, pos_, size_, rotation_, CRBaseSituatedModel.SHAPE.RECTANGLE, imageName_);
		resistance = resistance_;
		maxPushAngleFraction = 0.2;
		basePickUpProbability = 1;
		isBeingPushed = false;
		
	}
	
	
	//==================================== SIMULATION EVENTS ====================================
	
	/**
	 * Find objects that should be pushed, i.e. which ones are attached
	 * @param apartFromObjects_
	 * @return
	 */
	public void findPushedObjects(ArrayList<BaseWorldObject> chainOfObjects_) {
		ArrayList<CRBaseSituatedModel> worldObjects = ((World)world).getSituatedModelsAroundPosition(position, size.y);
		for (CRBaseSituatedModel situatedObject: worldObjects) {
			//-- find out of object is a stone or ant, if yes cast them to their common super class
			if (situatedObject.getClass() == Ant.class || situatedObject.getClass() == Stone.class) {
				BaseWorldObject worldObject = (BaseWorldObject) situatedObject;
				if (!chainOfObjects_.contains(worldObject) && worldObject != this && worldObject.getIsBeingPushed() == false) {
					//-- find out if colliding with stone and stone is ahead
					CRVector3d relVectorToObject = this.getRelativeVectorTo(worldObject.getPosition());
					boolean collidesWith = this.isCollidingWith(worldObject);
							
					if (collidesWith  && relVectorToObject.y > 0) { // && relVectorToObject.y > 0// && relVectorToObject.x <= maxPushAngleFraction //relVectorToObject.y > 0 && relVectorToObject.y < this.getSize().y / 2 + worldObject.getSize().y /2
	
						//-- put the object right in front of self, in case it is a bit misaligned
						//if (worldObject.getClass() != Ant.class) {
							//-- bumped into stone, pick up based on probability
							//if (Math.random() <= basePickUpProbability) {
								
								//-- tell ant that is pushing the queue that new object was added
								Ant pushingAnt = (Ant)chainOfObjects_.get(0);
								
								if (!pushingAnt.decideIfSlide(worldObject)) {
									if (pushingAnt.decideIfPickup(worldObject)) {
										
										/*CRVector3d otherObjectSize = worldObject.getSize();
										CRVector3d toBePosition = new CRVector3d(position);
										toBePosition.y += size.y/2.0 + otherObjectSize.y/2.0;
										rotateVectorInLocalCoordinates(toBePosition);
										worldObject.setPosition(toBePosition);
										((CRBinWorld)world).sortObjectToABin(worldObject);*/
										
										worldObject.setIsBeingPushed(true);
										chainOfObjects_.add(worldObject);
										this.onPickedUp(worldObject);
										if (this != pushingAnt) {
											pushingAnt.onPickedUp(worldObject);
										}
										
										//-- find next one in the chain
										worldObject.findPushedObjects(chainOfObjects_);
										
										
										
										
										//System.out.println(this.id + position.toString() + " rot " + rotation + ") pushing " + worldObject.getId() + "(" + worldObject.getPosition().toString() + " rot " + worldObject.getRotation() + ")");
									} else {
										pushingAnt.onNotPickedUp(worldObject);
									}
								}
								
								
								
							//} else {
							//	this.onNotStartedPushingObject(worldObject);
							//}
						//} else {
							//-- this is an ant, tell the ant at the beginning of the queue
							//Ant pushingAnt = (Ant)chainOfObjects_.get(0);
							//pushingAnt.onNotStartedPushingObject(worldObject);	
						//}
						
						continue;
					}
				}
			}
		}
	}
	
	/**
	 * Triggered when an object was just added to the queue of pushed objects.
	 * @param object_ BaseWorldObject object
	 */
	protected void onPickedUp(BaseWorldObject object_) {
		
	}
	
	/**
	 * Triggered when an object should have pushed (i.e. is colliding) but basePickUpProbability decided not to push it
	 * @param object_ BaseWorldObject object
	 */
	protected void onNotPickedUp(BaseWorldObject object_) {
		
	}
	
	
	//==================================== GETTERS / SETTERS ====================================
	public double getResistance() { return resistance; }
	
	public boolean getIsBeingPushed() { return isBeingPushed; }
	public void setIsBeingPushed(boolean val_) { isBeingPushed = val_; }
	
}
