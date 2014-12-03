package net.lenkaspace.antNest.model;

import java.util.ArrayList;

import net.lenkaspace.antNest.Settings;
import net.lenkaspace.antNest.model.Ant.KIND;
import net.lenkaspace.creeper.helpers.CRMaths;
import net.lenkaspace.creeper.model.CRBaseDynamicModel;
import net.lenkaspace.creeper.model.CRBaseSituatedModel;
import net.lenkaspace.creeper.vo.CRVector3d;

public class BroodCluster extends CRBaseDynamicModel {

	private ArrayList<CRBaseSituatedModel> pheromoneClouds;
	
	/**
	 * Constructor.
	 * @param positions_ ArrayList<CRVector3d> positions of individual pheromone clouds
	 * @param sizes_ ArrayList<CRVector3d> sizes of individual pheromone clouds. Must be the same size than positions_, otherwise [300,300,0] is used
	 * @param world_ World parent world - individual pheromone clouds will be added to it
	 */
	public BroodCluster(ArrayList<CRVector3d> positions_, ArrayList<CRVector3d> sizes_, World world_) {
		super(1000, new CRVector3d(330,330,0), new CRVector3d(300,300,0), 0, CRBaseSituatedModel.SHAPE.CIRCLE, "cloud.png");
		//-- the cluster itself is invisible, but its pheromone clouds will be
		this.isVisible = false;
		
		//-- create clouds:
		Settings settings = Settings.getSingleton();
		pheromoneClouds = new ArrayList<CRBaseSituatedModel>();
		int counter = 0;
		for (CRVector3d pos : positions_) {
			//-- check if size available
			CRVector3d siz = new CRVector3d(settings.normalBroodClusterDiam,settings.normalBroodClusterDiam,0);
			if (sizes_.size() > counter) {
				siz.copyFrom(sizes_.get(counter));
			}
			CRBaseSituatedModel cloud = new CRBaseSituatedModel(1000 + counter, pos, siz, 0, CRBaseSituatedModel.SHAPE.CIRCLE, "cloud.png");
			pheromoneClouds.add(cloud);
			world_.addSituatedModel(cloud);
			
			//-- create passive ants in the middle of the pheromone cloud
			if (settings.currentWorld != World.TEST_WORLD) {
				for (int i=0; i<Settings.getSingleton().numOfPassiveAnts; i++) {
					double xPos = CRMaths.getRandomGaussian(pos.x, 5); 
					double yPos = CRMaths.getRandomGaussian(pos.y, 5);
					double rot = CRMaths.getRandomInteger(0, 180);
					Ant ant = new Ant(700+counter*100 + i,new CRVector3d(xPos, yPos, 0), rot, KIND.PASSIVE);
					world_.addAnt(ant);
				}
			}
			
			counter++;
			
		}
		
		
		
		
	}
	
	public void onUpdateLoopStart() {
		for (CRBaseSituatedModel cloud : pheromoneClouds) {
			cloud.setIsVisible(Settings.getSingleton().showPheromone); 
		}
		super.onUpdateLoopStart();
	}
	
	
	
	/**
	 * Get hormone concentration at a specific location 
	 * @param position_ CRVector3d at measured position
	 * @return double concentration in range <0;1>
	 */
	public double getPheromoneConcentrationAt(CRVector3d position_) {
		double returnVal = 0;
		for (CRBaseSituatedModel cloud : pheromoneClouds) {
			double distance = Math.hypot(cloud.getPosition().x- position_.x, cloud.getPosition().y- position_.y);
			double radius = cloud.getSize().x / 2;
			if (distance <= radius ) {
				//-- 1- (percentage of possible distance within circle), i.e. the closer to the middle the higher the concentration 
				returnVal += 1-(distance/radius);
			}
		}
		
	
		
		/*double distance = Math.hypot(position.x- position_.x, position.y- position_.y);
		double radius = size.x / 2;
		if (distance <= radius ) {
			//-- 1- (percentage of possible distance within circle), i.e. the closer to the middle the higher the concentration 
			returnVal += (1-(distance/radius));
		}*/
		return returnVal;
	}
	
	
	

}
