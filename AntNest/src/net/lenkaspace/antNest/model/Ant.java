package net.lenkaspace.antNest.model;

import java.util.ArrayList;

import net.lenkaspace.antNest.Settings;
import net.lenkaspace.antNest.report.ReportController;
import net.lenkaspace.creeper.CRSettings;
import net.lenkaspace.creeper.helpers.CRMaths;
import net.lenkaspace.creeper.model.CRBinWorld;
import net.lenkaspace.creeper.report.CRTimeSeriesReport;
import net.lenkaspace.creeper.view.CROutputPopup;
import net.lenkaspace.creeper.view.CRRenderer;
import net.lenkaspace.creeper.vo.CRVector3d;

public class Ant extends BaseWorldObject {

	protected static final double DROPPING_PROBABILITY_MULTIPLIER = 1.0 / (0.5 * CRSettings.getSingleton().getTimeUnitInterval());
	
	//-- random walk parameters, i.e. ant is deliberately walking randomly
	protected static final double RANDOM_WALK_ANGLE_CHANGE_PROBABILITY = 0.2 ;//1.0 / (1 * CRSettings.getSingleton().getTimeUnitInterval());
	protected static final int RANDOM_WALK_ANGLE_CHANGE_MIN = 140;
	protected static final int RANDOM_WALK_ANGLE_CHANGE_MAX = 180;
	
	//-- walk randomisation parameters, i.e. ant is walking towards a target but some randommness is always added
	protected static final double WALK_RANDOMISATION_ANGLE_CHANGE_PROBABILITY = 0.2 ;//1.0 / (1 * CRSettings.getSingleton().getTimeUnitInterval());
	protected static final int WALK_RANDOMISATION_ANGLE_CHANGE_MIN = 0;
	protected static final int WALK_RANDOMISATION_ANGLE_CHANGE_MAX = 45;
	protected static final double ANGLE_CHANGE_MULTIPLIER = 0.1;
	
	public enum KIND {
		EXTERNAL,
		INTERNAL,
		PASSIVE,
		TEST,
	}
	
	protected KIND kind;
	protected ArrayList<BaseWorldObject> pushedObjects;
	protected boolean isMovingRandomly;
	protected boolean isWithinNest;
	protected boolean canDrop;
	protected int startedPushingTimeCounter;
	
	protected double lastHigherEqualPheromoneConcentration;
	protected CRVector3d lastHigherEqualPheromoneConcentrationPosition;
	protected double lastPheromoneConcentration;
	
	
	protected double resistanceMultiplier;
	protected double feltResistance;
	
	protected boolean shouldRecordCarryTime;
	
	public Ant(int id_, CRVector3d pos_, double rotation_, KIND kind_) {
		super(1, id_, pos_, new CRVector3d(2,10,0), rotation_, CRRenderer.CR_RED_DOT);
	
		kind = kind_;
		resistance = 100;
		thrustForce = 1;
		minThrustForce = 0;
		maxSpeed = 2;
		isMovingRandomly = true;
		isWithinNest = false;
		canDrop = false;
		feltResistance = 0;
		startedPushingTimeCounter = 0;
		
		lastHigherEqualPheromoneConcentration = -1;
		lastHigherEqualPheromoneConcentrationPosition = new CRVector3d(position);
		lastPheromoneConcentration = -1;
		
		if (kind == KIND.INTERNAL) {
			resistanceMultiplier = 1; //0.5;
		} else if (kind == KIND.EXTERNAL) {
			resistanceMultiplier = 1; //0.5;
			imageFileName = CRRenderer.CR_GREEN_DOT;
		} else if (kind == KIND.TEST) {
			resistanceMultiplier = 1;
			imageFileName = CRRenderer.CR_BLUE_DOT;
		} else {
			maxThrustForce = 0;
			resistanceMultiplier = 1;
			imageFileName = CRRenderer.CR_PURPLE_DOT;
		}
		
		maxPushAngleFraction = 0.5;
		if (Settings.getSingleton().currentWorld == World.TEST_WORLD) {
			basePickUpProbability = 1;
		} else {
			basePickUpProbability = 0.5 ;//0.1;
		}
		
		
		
		//pushingTimeMultiplier = 0.5;
		
		pushedObjects = new ArrayList<BaseWorldObject>();
		
		if (Settings.getSingleton().currentWorld == World.TEST_WORLD) {
			if (this.id == 1) {
				maxThrustForce = 0;
			}
		}
	}
	
	
	//==================================== UPDATE LOOP ==========================================
	public void onUpdateLoopStart() {
		isVisible = Settings.getSingleton().showAnts;
		//feltResistance = 0;
		
		
		super.onUpdateLoopStart();
		
		//---- find out if it is within nest
		if ( ((World)world).getBroodCluster().getPheromoneConcentrationAt(position) > 0) {
			//-- external ant should remember when it entered nest carrying something
			if (isWithinNest == false && kind == KIND.EXTERNAL && pushedObjects.size() >= 2) {
				startedPushingTimeCounter = world.getController().getTimeCounterSinceTrialStart();
				//System.out.println("STARTED at " + startedPushingTimeCounter);
			}
			isWithinNest = true;
		} else {
			isWithinNest = false;
		}
		
		//-- create chain of objects that are being pushed, starting with self:
		if (pushedObjects.size() == 0) {
			pushedObjects.add(this);
		}
		
		
		if (kind != KIND.PASSIVE) {
			//---- find any other objects attached to this chain, based on the object in the front of the chain:
			pushedObjects.get(pushedObjects.size() - 1).findPushedObjects(pushedObjects);
				
			//---- decide to drop stones
			decideIfDrop(false);
			
			//---- adjust thrust
			if (kind == KIND.INTERNAL || kind == KIND.EXTERNAL) {
				if (pushedObjects.size() > 1) {
					maxThrustForce = 1;
				} else {
					maxThrustForce = 0.5;
				}
			}
			this.setThrustForce(1 - feltResistance);
			
			//---- adjust rotation
			BroodCluster broodCluster = ((World)world).getBroodCluster();
			if (kind != KIND.TEST) {
				int rotationChange = 0;
				double currentPheromoneConcentration = broodCluster.getPheromoneConcentrationAt(position);
				if (kind == KIND.INTERNAL) {
					isMovingRandomly = true;
					
					//-- check if last second pheromone concentration was higher than now, if yes, rotate towards location in last second
					if (lastHigherEqualPheromoneConcentration >= 0 && currentPheromoneConcentration <= Settings.getSingleton().pheromoneMovementThreshold) {
						if (lastHigherEqualPheromoneConcentration - currentPheromoneConcentration > 0) {
							rotationChange = -(int) Math.toDegrees(this.getRelativeVectorTo(lastHigherEqualPheromoneConcentrationPosition).x);
						}
					}
						
					if (pushedObjects.size() > 1) {
						//-- carrying something, rotate towards outside of the cirlce, but keep random movement
						if (lastHigherEqualPheromoneConcentration - currentPheromoneConcentration < 0) {
							rotationChange = (int) Math.toDegrees(this.getRelativeVectorTo(lastHigherEqualPheromoneConcentrationPosition).x);
							
						}	
					}
					
					
				} else if (kind == KIND.EXTERNAL) {
					if (pushedObjects.size() > 1) {
						//-- pushing a stone, orient itself towards the nest if not roughly oriented
						//CRVector3d clusterMiddle = ((World)world).getBroodCluster().getPosition();
						//CRVector3d vecToCluster = this.getRelativeVectorTo(clusterMiddle);
						if (!isMovingRandomly) {
							if (lastHigherEqualPheromoneConcentration - currentPheromoneConcentration > 0) {
								rotationChange = -(int) Math.toDegrees(this.getRelativeVectorTo(lastHigherEqualPheromoneConcentrationPosition).x);
							}
							/*if (vecToCluster.y < 0 || Math.abs(vecToCluster.x) > 0.3) {
								rotationChange = -(int) Math.toDegrees(vecToCluster.x);	
							}*/
							
						}
					}
					
				}
				
				if (lastHigherEqualPheromoneConcentration - currentPheromoneConcentration <= 0){
					//-- current pherom conc is bigger that the greatest measured, update the memory
					lastHigherEqualPheromoneConcentration = currentPheromoneConcentration;
					lastHigherEqualPheromoneConcentrationPosition.copyFrom(this.getPosition());
				}
				
			
				
				
				//-- do strict random walk, add big random rotation
				if (isMovingRandomly) {
					if (Math.random() < RANDOM_WALK_ANGLE_CHANGE_PROBABILITY) {
						rotationChange += CRMaths.getRandomInteger(RANDOM_WALK_ANGLE_CHANGE_MIN, RANDOM_WALK_ANGLE_CHANGE_MAX, true);	
					}
				}
				//-- add a bit of randomness to every movement
				if (Math.random() < WALK_RANDOMISATION_ANGLE_CHANGE_PROBABILITY) {
					rotationChange += CRMaths.getRandomInteger(WALK_RANDOMISATION_ANGLE_CHANGE_MIN, WALK_RANDOMISATION_ANGLE_CHANGE_MAX, true);
				}
				
				if (kind != KIND.PASSIVE) {
					this.setRotation(rotation + ANGLE_CHANGE_MULTIPLIER*rotationChange); 
				}
				
				
				
			}
			
			
			for (BaseWorldObject pushedObject : pushedObjects) {
				if (pushedObject.getClass() != Ant.class) {
					pushedObject.setRotation(this.rotation);
					pushedObject.setThrustForce(this.thrustForce);
				}
			}
		} else {
			this.setThrustForce(maxThrustForce);
		}
		
		
				
		//CROutputPopup.getSingleton().displayOutput(id + " NUM: " + pushedObjects.size() + "   RES: " + feltResistance);
		
		
	}
	
	/**
	 * Main update function.
	 */
	public void update() {
            
            System.out.println(((World)world).getPheromoneConc(position));
		
		//-- output carrying info
		if (kind == KIND.EXTERNAL) {
			CROutputPopup.getSingleton().displayOutput(this.id + " carrying" + pushedObjects.size() + "resistance:  " + feltResistance + " random: " + isMovingRandomly);
			for (int i=0; i<pushedObjects.size(); i++) {
				BaseWorldObject object = pushedObjects.get(i);
				if (object != this) {
					CROutputPopup.getSingleton().displayOutput("    " + object.getId() + "   rot " + object.getRotation() + "  pos" + object.getPosition().x + " " + object.getPosition().y);
				}
			}
		} else {
                    isDroppingPheromone = true;
                }
			
		
		
		//-- move, etc.
		super.update();
		
		//-- sort self and all pushed objects to a possibly new bin
		CRBinWorld binWorld = (CRBinWorld)world;
		for (BaseWorldObject pushedObject : pushedObjects) {
			binWorld.sortObjectToABin(pushedObject);
		}
		
		if (Settings.getSingleton().currentWorld == World.TEST_WORLD) {
			//System.out.println("ANT " + id + " AT " + position.x + " " + position.y);
		}
	}
	
	/**
	 * End of update loop
	 */
	public void onUpdateLoopEnd() {
		super.onUpdateLoopEnd();
		
		
	}
	
	/**
	 * Decide with some probability to rotate and walk along the object rather than start carrying it
	 * @param object_
	 * @return
	 */
	protected boolean decideIfSlide(BaseWorldObject object_) {
		
		double slideProbability = feltResistance; //0.5 ;// 0; //(pushedObjects.size()-2)*0.33;
		
		if (object_.getClass() != Ant.class) {
			if (feltResistance == 0 || kind == KIND.TEST) {
				slideProbability = -1; //don't slide if this is the first stone it encountered
			}
			//slideProbability = -1;
			if (Math.random() <= slideProbability) {				
				//-- rotate by about 90 deg
				double angleChange = CRMaths.getRandomInteger(15,35, true); //CRMaths.getRandomInteger(15,45, true); // CRMaths.getRandomGaussian(15, 5);  //CRMaths.getRandomInteger(15,25, true);  //getRandomGaussian(90, 10); //getRandomInteger(20,90)
				this.setRotation(rotation + angleChange);
				
				feltResistance += object_.getResistance();
				//System.out.println(kind.toString() + " SLIDING with prob " + slideProbability +" by " + angleChange + " deg");
				return true;	
			}
		}
		return false;
	}
	
	protected boolean decideIfPickup(BaseWorldObject worldObject_) {
		if (worldObject_.getClass() != Ant.class) {
			if (feltResistance < 1) {
				double pickUpProbability = basePickUpProbability;
				if (kind == KIND.TEST) {
					pickUpProbability = 1;
				}
				/*if (kind == KIND.EXTERNAL && isWithinNest && feltResistance == 0) {
					pickUpProbability = -1;
				}*/
			//	pickUpProbability = 1;
				if (Math.random() <= pickUpProbability) {
					return true;
				}
					
			}
			return false;
		}
		return false;
	}
	
	/**
	 * Triggered when an object was added to the chain.
	 * @param object_ BaseWorldObject object
	 */
	protected void onPickedUp(BaseWorldObject object_) {
		super.onPickedUp(object_);
		if (kind == KIND.EXTERNAL) {
			//-- external ant should stop moving randomly if it is outside of nest. 
			if (!isWithinNest) {
				isMovingRandomly = false;
			}
			
			//-- external ant should decide to record carry time when 1st stone added to the queue, 
			//   based on whether it's outside or inside of nest. Only do this the first time it enters nest from outside
			if (pushedObjects.size() == 2 && !shouldRecordCarryTime) {
				if (isWithinNest) {
					shouldRecordCarryTime = false;
				} else {
					shouldRecordCarryTime = true;
				}
			}
			
			
		}
		feltResistance += object_.getResistance();
	}
	
	/**
	 * Triggered when an object should have pushed (i.e. is colliding) but basePickUpProbability decided not to push it.
	 * Rotate away from the object in a random fahsion.
	 * @param object_ BaseWorldObject object
	 */
	protected void onNotPickedUp(BaseWorldObject worldObject_) {
		if (worldObject_.getClass() == Ant.class) {
			moveAwayFromDroppedObject(worldObject_, false);
			//-- the following will result in dropping the stone a little away from an ants it bumbped into
			if (kind == KIND.EXTERNAL) {
				isMovingRandomly = true;
			}
		}
	}
	
	protected boolean decideIfDrop(boolean justPickedUp_) {
		canDrop = true;
		
		if (kind == KIND.EXTERNAL) {
			//-- external ants cannot drop if carrying something but not moving randomly (haven't touched anybody yet)
			if (!isMovingRandomly) {
				canDrop = false;
			}
		}
		
		if (feltResistance == 0) {
			//-- no point of drop testing, not carrying anything
			canDrop = false;
		} else if (feltResistance >= 1) {
			//-- all ants can drop if resistance too high
			canDrop = true;
		}
				
		if (canDrop ) {
			BroodCluster broodCluster = ((World)world).getBroodCluster();
			//if (world.getController().getTimeCounter() == 0 || world.getController().getTimeCounter() == CRSettings.getSingleton().getTimeUnitInterval()/2) {
			//if (world.getController().getTimeCounter() % CRSettings.getSingleton().getTimeUnitInterval()/10 == 0 || feltResistance >=1 ) {
				//double dropProbability = (resistanceMultiplier*Math.pow(feltResistance,3) + (1-resistanceMultiplier));
				if (feltResistance > 1) {
					feltResistance = 1;
				}
				double dropProbability = 1/1.6*Math.abs(Math.log(1-0.8*(feltResistance+0.00000000001)));
				double hormoneFactor = 0;
				if (Settings.getSingleton().usePheromoneTemplate && kind == KIND.INTERNAL) {
					hormoneFactor = broodCluster.getPheromoneConcentrationAt(position);
					//dropProbability = resistanceMultiplier*Math.pow(feltResistance,3)  + Math.pow(hormoneFactor,3);
					//dropProbability = resistanceMultiplier*Math.pow(feltResistance,3) + Math.abs(Math.pow(1/7.0 * Math.log(hormoneFactor),2));
					dropProbability += 	Math.abs(Math.pow(1/7.0 * Math.log(hormoneFactor),1));
					//System.out.println(id + "  " + position.toString() + "  horm  " + hormoneFactor + "   drop prob" + dropProbability);
				}
				if (kind == KIND.PASSIVE) {
					dropProbability = 1;
				}
				if (Math.random() <= dropProbability) {
					//System.out.println(kind.toString() + " DROPPING with prob " + dropProbability + "  resistance " + feltResistance + "   hormone factor " + hormoneFactor + "  at " + position.toString());
					//if (feltResistance >= 1) {
					dropPushedObjects();
					//}
					return true;
				}
			//}
		}
		return false; 
	}
	
	
	
	/**
	 * Set thrust force of all pushed stones (not ants) to 0 and clear the pushedObjects array.
	 */
	protected void dropPushedObjects() {
		for (BaseWorldObject pushedObject : pushedObjects) {
			if (pushedObject.getClass() != Ant.class) {
				pushedObject.setThrustForce(0);
				pushedObject.setRotation(0);
				pushedObject.setIsBeingPushed(false);
			}
		}
		pushedObjects.clear();
		moveAwayFromDroppedObject(null, true);
		isMovingRandomly = true;
		feltResistance = 0;
		
		//-- external ant should report on how long this object was pushed, if it was taken outside of nest and put down inside of nest
		if (kind == KIND.EXTERNAL && shouldRecordCarryTime && isWithinNest) {
			//-- difference between started and ended pushing, times time unit interval, as the report will average over the whole time unit
			int timePassed = (world.getController().getTimeCounterSinceTrialStart() - startedPushingTimeCounter) ;
			if (timePassed > 0) {
				//System.out.println(world.getController().getTimeUnits() + id + "  " + kind + "  TIME PASSED " + timePassed + "   " + world.getController().getTimeCounterSinceTrialStart());
				((CRTimeSeriesReport)world.getController().getReportController().getReport(ReportController.REPORT_PUSHING_TIME)).addValue(timePassed);
			}
		}
		shouldRecordCarryTime = false;
		//System.out.println("DROPPING OBJECTS AT " + position.toString());
	}
	
	/**
	 * Adjust own position in a borderless world but also position of all pushed stones.
	 * This is to prevent stones from getting away when their transition happens.
	 */
	public void adjustPositionInBorderlessWorld() {
		CRVector3d oldPosition = new CRVector3d(position);
		super.adjustPositionInBorderlessWorld();
		double yDifference = position.y - oldPosition.y;
		double xDifference = position.x - oldPosition.x;
		if (Math.abs(yDifference) > maxSpeed) {
			for (BaseWorldObject pushedObject : pushedObjects) {
				if (pushedObject.getClass() != Ant.class) {
					pushedObject.setPositionY(pushedObject.getPosition().y + yDifference);
					((CRBinWorld)world).sortObjectToABin(pushedObject);
				}
			}
		}
		if (Math.abs(xDifference) > maxSpeed) {
			for (BaseWorldObject pushedObject : pushedObjects) {
				if (pushedObject.getClass() != Ant.class) {
					pushedObject.setPositionX(pushedObject.getPosition().x + xDifference);
					((CRBinWorld)world).sortObjectToABin(pushedObject);
				}
			}
		}
	}
	
	/**
	 * Override set rotation to translate pushed objects
	 */
	public void setRotation(double rotation_) {
		double oldRotation = rotation;
		super.setRotation(rotation_);
		double change = rotation - oldRotation;
		for (BaseWorldObject pushedObject : pushedObjects) {
			if (pushedObject.getClass() != Ant.class) {
				pushedObject.setRotation(rotation_);
				double distanceXDiff = position.x - pushedObject.getPosition().x;
				double distanceYDiff = position.y - pushedObject.getPosition().y;
				double deltaX = Math.cos(Math.toRadians(change))*distanceXDiff - Math.sin(Math.toRadians(change))*distanceYDiff;
				double deltaY = Math.sin(Math.toRadians(change))*distanceXDiff + Math.cos(Math.toRadians(change))*distanceYDiff;
				pushedObject.setPosition(new CRVector3d(position.x - deltaX, position.y - deltaY, 0));
				((CRBinWorld)world).sortObjectToABin(pushedObject);
				//System.out.println(this.id + "  rotating " + pushedObject.getId());
			}
		}
	}
	
	
	
	/**
	 * Rotate away from the object in a random fahsion.
	 * @param object_
	 */
	protected void moveAwayFromDroppedObject(BaseWorldObject object_, boolean randomly_) {
		int angleChange = 180;
		if (randomly_) {
			angleChange = CRMaths.getRandomInteger(140,180, true);
		}
		this.setRotation(rotation + angleChange);
	}
        
        public boolean isDroppingPheromone = false;

}
