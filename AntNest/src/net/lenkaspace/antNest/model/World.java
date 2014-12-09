package net.lenkaspace.antNest.model;

import java.util.ArrayList;

import net.lenkaspace.antNest.Settings;
import net.lenkaspace.antNest.model.Ant.KIND;
import net.lenkaspace.creeper.CRController;
import net.lenkaspace.creeper.helpers.CRMaths;
import net.lenkaspace.creeper.model.CRBinWorld;
import net.lenkaspace.creeper.vo.CRVector3d;

public class World extends CRBinWorld {

	public static final String TEST_WORLD = "Test world";
	public static final String FOUR_SITES_WORLD = "World with 4 sites";
	public static final String TEST_EXTERNAL_WORLD = "External ant test";
	public static final String TEST_EXTERNAL_RANDOM_WORLD = "External ant test random";
	public static final String TEST_INTERNAL_WORLD = "Internal ant test";
	public static final String RANDOM_WORLD = "Random world";
	public static final String TRIANGLE_WORLD = "Triangle";
	public static final String TWO_HORIZ_CLUSTERS = "Rectangle - horizontal";
	public static final String TWO_VERT_CLUSTERS = "Rectangle - vertical";
	public static final String SQUARE_WORLD = "Square";
	
	private ArrayList<Stone> stones;
	private ArrayList<Ant> ants;
	private BroodCluster broodCluster;
        private Pheromone pheromone;
	
	public World(CRController controller_) {
		super(new CRVector3d(25,25,0), 0, new CRVector3d(660,660,0), controller_);
		stones = new ArrayList<Stone>();
		ants = new ArrayList<Ant>();
	}
	
	
	//==================================== SIMULATION EVENTS ====================================
	
	/**
	 * Called by CRController each time a trial starts. 
	 * Use this to reset self for a new world environment and call onNewTrial of children models.
	 * @param trialNumber_ int new trial number
	 * @param runNumber_ int current run number
	 */
	public void onTrialStart(int trialNumber_, int runNumber_) {
		this.clearChildren();
		
		Settings settings = Settings.getSingleton();
		String currentWorld = settings.currentWorld;
		
		//-- create brood cluster
		ArrayList<CRVector3d> positions = new ArrayList<CRVector3d>();
		ArrayList<CRVector3d> sizes = new ArrayList<CRVector3d>();
		
		if (currentWorld == TWO_HORIZ_CLUSTERS) {
			int radius = settings.broodClusterDistance/2;
			positions.add(new CRVector3d(330-radius,330,0));
			positions.add(new CRVector3d(330+radius,330,0));
			for (int i=0; i<2; i++) {
				sizes.add(new CRVector3d(settings.normalBroodClusterDiam,settings.normalBroodClusterDiam,0));
			}	
		} else if (currentWorld == TWO_VERT_CLUSTERS) {
			int radius = settings.broodClusterDistance/2;
			positions.add(new CRVector3d(330,330-radius,0));
			positions.add(new CRVector3d(330,330+radius,0));
			for (int i=0; i<2; i++) {
				sizes.add(new CRVector3d(settings.normalBroodClusterDiam,settings.normalBroodClusterDiam,0));
			}
		} 
		else if (currentWorld == TRIANGLE_WORLD) {
			//-- 3 around the middle
			int distance = settings.broodClusterDistance;
			double height = Math.sqrt(Math.pow(distance,2) - Math.pow(distance*0.5,2));
			
			//-- top corner
			positions.add(new CRVector3d(330,330-height*2/3.0,0));
			
			//-- base
			positions.add(new CRVector3d(330-distance/2.0,330+height*1/3.0,0));
			positions.add(new CRVector3d(330+distance/2.0,330+height*1/3.0,0));
			//int deltaX = (int) (Math.sin(Math.toRadians(60)) * radius);
			//int deltaY = (int) (Math.cos(Math.toRadians(60)) * radius);
			//positions.add(new CRVector3d(330-deltaX,330+deltaY,0));
			//positions.add(new CRVector3d(330+deltaX,330+deltaY,0));
		
			for (int i=0; i<3; i++) {
				sizes.add(new CRVector3d(settings.normalBroodClusterDiam,settings.normalBroodClusterDiam,0));
			}
		} else if (currentWorld == SQUARE_WORLD) {
			int radius = settings.broodClusterDistance/2;
			positions.add(new CRVector3d(330-radius,330-radius,0));
			positions.add(new CRVector3d(330-radius,330+radius,0));
			positions.add(new CRVector3d(330+radius,330-radius,0));
			positions.add(new CRVector3d(330+radius,330+radius,0));
			for (int i=0; i<4; i++) {
				sizes.add(new CRVector3d(settings.normalBroodClusterDiam,settings.normalBroodClusterDiam,0));
			}
		} else {
			//-- 1 in the middle
			positions.add(new CRVector3d(330,330,0));
			sizes.add(new CRVector3d(settings.normalBroodClusterDiam,settings.normalBroodClusterDiam,0));
		}
		
		
		broodCluster = new BroodCluster(positions,sizes, this);
		this.addDynamicModel(broodCluster);
		
                pheromone = new Pheromone();
                pheromone.setIsVisible(true);
                this.addSituatedModel(pheromone);

		//-- create stones
		if (currentWorld != TEST_INTERNAL_WORLD && currentWorld != TEST_EXTERNAL_WORLD && currentWorld != FOUR_SITES_WORLD && currentWorld != TEST_WORLD) {
			for (int i=0; i<3000; i++) {
				double xPos = CRMaths.getRandomInteger(0, (int)(size.x));
				double yPos = CRMaths.getRandomInteger(0, (int)(size.y));
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				this.addStone(stone);
			}
		}
		
		if (currentWorld == TEST_INTERNAL_WORLD) {
			for (int i=0; i<1000; i++) {
				double xPos = CRMaths.getRandomInteger((int)(size.x/2 - broodCluster.getSize().x/2), (int)(size.x/2 + broodCluster.getSize().x/2));
				double yPos = CRMaths.getRandomInteger((int)(size.y/2 - broodCluster.getSize().y/2), (int)(size.y/2 + broodCluster.getSize().y/2));
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				this.addStone(stone);
			}
		}
		if (currentWorld == TEST_EXTERNAL_WORLD || currentWorld == TEST_INTERNAL_WORLD) {
			for (int i=0; i<100; i++) {
				double xPos = CRMaths.getRandomGaussian(240, 2);
				double yPos = CRMaths.getRandomGaussian(240, 2);
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				this.addStone(stone);
			}
		
			for (int i=0; i<100; i++) {
				double xPos = CRMaths.getRandomGaussian(230, 2);
				double yPos = CRMaths.getRandomGaussian(250, 2);
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				this.addStone(stone);
			}
		}
		if (currentWorld == FOUR_SITES_WORLD || currentWorld == TEST_EXTERNAL_WORLD) {
			for (int i=0; i<500; i++) {
				double xPos = CRMaths.getRandomGaussian(120, 45);
				double yPos = CRMaths.getRandomGaussian(120, 45);
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				this.addStone(stone);
			}
		}
		
		if (currentWorld == FOUR_SITES_WORLD) {
			for (int i=0; i<500; i++) {
				double xPos = CRMaths.getRandomGaussian(540, 45);
				double yPos = CRMaths.getRandomGaussian(540, 45);
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				this.addStone(stone);
			}
			
			for (int i=0; i<500; i++) {
				double xPos = CRMaths.getRandomGaussian(540, 45);
				double yPos = CRMaths.getRandomGaussian(120, 45);
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				this.addStone(stone);
			}
			for (int i=0; i<500; i++) {
				double xPos = CRMaths.getRandomGaussian(120, 45);
				double yPos = CRMaths.getRandomGaussian(540, 45);
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				this.addStone(stone);
			}
		}
			
		//-- create internal ants
		if (currentWorld != TEST_WORLD && currentWorld != TEST_EXTERNAL_WORLD  && currentWorld != TEST_EXTERNAL_RANDOM_WORLD) {
			for (int i=0; i<settings.numOfInternalWokers; i++) {
				double xPos = CRMaths.getRandomInteger((int)size.x/2-50, (int)size.x/2+50);
				double yPos = CRMaths.getRandomInteger((int)size.y/2-50, (int)size.y/2+50);
				double rotation = CRMaths.getRandomInteger(-180, 180);
				Ant ant = new Ant(500+i,new CRVector3d(xPos, yPos, 0),rotation, KIND.INTERNAL);
				this.addAnt(ant);
			}
		}
		//-- create external ants
		if (currentWorld != TEST_WORLD && currentWorld != TEST_INTERNAL_WORLD) {
			for (int i=0; i<settings.numOfExternalWorkers; i++) {
				double xPos = CRMaths.getRandomInteger((int)(3.5*size.x/8), (int)(4.5*size.x/8));
				double yPos = CRMaths.getRandomInteger((int)(3.5*size.x/8), (int)(4.5*size.x/8));
				double rotation = CRMaths.getRandomInteger(-180, 180);
				Ant ant = new Ant(600+i,new CRVector3d(xPos, yPos, 0),rotation, KIND.EXTERNAL);
				this.addAnt(ant);
			}
		}
		
			
		if (Settings.getSingleton().currentWorld == TEST_WORLD) {
			
			/*Stone stone1 = new Stone(0, new CRVector3d(350,350,0));
			//this.addStone(stone1);
			Stone stone2 = new Stone(1, new CRVector3d(350,300,0));
			//this.addStone(stone2);	
			Stone stone3 = new Stone(2, new CRVector3d(351,300,0));
			//this.addStone(stone3);	
			Stone stone4 = new Stone(3, new CRVector3d(350,250,0));
			//this.addStone(stone4);
			Stone stone5 = new Stone(4, new CRVector3d(350,200,0));
			//this.addStone(stone5);*/
			/*for (int i=0; i<500; i++) {
				//double xPos = CRMaths.getRandomInteger((int)(3.5*size.x/8), (int)(4.5*size.x/8));
				//double yPos = CRMaths.getRandomInteger((int)(3.5*size.y/8), (int)(4.5*size.y/8));
				double xPos = CRMaths.getRandomInteger((int)(0.5*size.x/8), (int)(7.5*size.x/8));
				double yPos = CRMaths.getRandomInteger((int)(0.5*size.y/8), (int)(7.5*size.y/8));
				Stone stone = new Stone(i,new CRVector3d(xPos, yPos, 0));
				//this.addStone(stone);
			}*/
			
			//-- cross walls
			/*for (int i=180; i<480; i++) {
				if (i < 280 || i> 380) {
					this.addStone(new Stone(i,new CRVector3d(i, 280, 0)));
					this.addStone(new Stone(i,new CRVector3d(i, 380, 0)));
					this.addStone(new Stone(i,new CRVector3d(280, i, 0)));
					this.addStone(new Stone(i,new CRVector3d(380, i, 0)));
				}
			}*/
			
			//-- bin world test
			/*this.addStone(new Stone(1,new CRVector3d(259,395,0)));
			this.addStone(new Stone(2,new CRVector3d(264,393,0)));
			this.addStone(new Stone(3,new CRVector3d(305,394,0)));
			this.addStone(new Stone(4,new CRVector3d(307,391,0)));
			this.addStone(new Stone(5,new CRVector3d(320,392,0)));
			this.addStone(new Stone(6,new CRVector3d(336,396,0)));
			this.addStone(new Stone(7,new CRVector3d(389,393,0)));*/
			
			//-- stone position report test
			for (int i=0; i<=660; i+=10) {
				//this.addStone(new Stone(2,new CRVector3d(i,330,0)));
				//this.addStone(new Stone(2,new CRVector3d(i,300,0)));
				//this.addStone(new Stone(2,new CRVector3d(i,360,0)));
				this.addStone(new Stone(2,new CRVector3d(360-i,i,0)));
				//System.out.println("Stone at " + i);
			}
			
			
			//Ant ant = new Ant(50,new CRVector3d(350,400,0), 0, KIND.TEST);
			//this.addAnt(ant);
			//Ant ant2 = new Ant(52, new CRVector3d(350,200,0),-45, KIND.INTERNAL);
			//this.addAnt(ant2);	
			//Ant ant3 = new Ant(53, new CRVector3d(350,300,0),45, KIND.INTERNAL);
			//this.addAnt(ant3);
			
			 
			
			/*Stone stone1 = new Stone(0, new CRVector3d(153.0, 405, -64));
			this.addStone(stone1);
			Stone stone2 = new Stone(0, new CRVector3d(105.0, 379.0, -64));
			this.addStone(stone2);
			Ant ant = new Ant(50,new CRVector3d(149.594,400.599,0), 136, KIND.INTERNAL);
			this.addAnt(ant);
			*/
			
			/*Stone stone1 = new Stone(0, new CRVector3d(105.0, 379.0, 0));
			this.addStone(stone1);
			Ant ant = new Ant(50,new CRVector3d(219.407,472.893,0), -35, KIND.INTERNAL);
			this.addAnt(ant);*/
			
			
			
		}
		
		
		super.onTrialStart(trialNumber_, runNumber_);
	}
	
	
	
	//==================================== OBJECT MANIPULATION ==================================
	/**
	 * Add ant to the world
	 * @param ant_ Ant to add
	 */
	public void addAnt(Ant ant_) {
		if (!ants.contains(ant_)) {
			//-- add to child models
			addDynamicModel(ant_);
			//-- set its world to this
			ant_.setWorld(this);
			//-- add it to the list
			ants.add(ant_);			
		}
	}
	
	/**
	 * Add ant to the world
	 * @param ant_ Ant to add
	 */
	public void addStone(Stone stone_) {
		if (!stones.contains(stone_)) {
			//-- add to child models
			addDynamicModel(stone_);
			//-- set its world to this
			stone_.setWorld(this);
			//-- add it to the list
			stones.add(stone_);			
		}
	}
	
	public void clearChildren() {
		ants.clear();
		stones.clear();
		super.clearChildren();
	}
	
	//==================================== UPDATE LOOP ====================================

    @Override
    public void update() {
        super.update();
        for(Ant ant : ants)
            if(ant.isDroppingPheromone)
                pheromone.addPheromoneAct((int)Math.round(ant.getPosition().x), (int)Math.round(ant.getPosition().y));
        pheromone.update();
    }
	
    public double getPheromoneConc(CRVector3d location) {
        return pheromone.getGradient(Math.round((float) location.x), (int) Math.round(location.y));
    }
	
	
	//==================================== GETTERS / SETTERS ====================================
	
	public ArrayList<Stone> getStones() { return stones; }
	public ArrayList<Ant> getAnts() { return ants; }
	public BroodCluster getBroodCluster() { return broodCluster; }
	
	

}
