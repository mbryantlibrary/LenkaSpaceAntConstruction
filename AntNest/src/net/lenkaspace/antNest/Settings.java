package net.lenkaspace.antNest;

import net.lenkaspace.antNest.model.World;


public class Settings {

	public boolean showAnts;
	public boolean showStones;
	public boolean showPheromone;
	
	public int numOfPassiveAnts;
	public int numOfExternalWorkers;
	public int numOfInternalWokers;
	public boolean usePheromoneTemplate;
	public double pheromoneMovementThreshold;
	public int normalBroodClusterDiam;
	public int broodClusterDistance;
        
        public boolean useBroodPheromones;
	
	
	public String currentWorld;
	
	//==================================== SINGLETON ====================================
    private static Settings singletonReference;
    
	
	public static Settings getSingleton()	  {
	  if (singletonReference == null)
		  singletonReference = new Settings();		
	  return singletonReference;
	}
	
	private Settings() {
		showAnts = true;
		showStones = true;
		showPheromone = true;
		usePheromoneTemplate = true;
		currentWorld = World.TEST_INTERNAL_WORLD;
		pheromoneMovementThreshold = 1;
		normalBroodClusterDiam = 300;
		broodClusterDistance=100;
                useBroodPheromones = false;
                
	}
}
