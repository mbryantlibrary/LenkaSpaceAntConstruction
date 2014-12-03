package net.lenkaspace.antNest.report;

import net.lenkaspace.antNest.model.Stone;
import net.lenkaspace.antNest.model.World;
import net.lenkaspace.creeper.CRController;
import net.lenkaspace.creeper.CRSettings;
import net.lenkaspace.creeper.helpers.CRFileIOHelper;
import net.lenkaspace.creeper.helpers.CRMaths;
import net.lenkaspace.creeper.report.CRReportController;
import net.lenkaspace.creeper.vo.CRVector3d;

public class ReportController extends CRReportController {

	public static String REPORT_WORLD_SNAPSHOT = "World";
	public static String REPORT_PUSHING_TIME = "External ant pushing time";
	
	public ReportController(CRController controller_) {
		super(controller_);
		
	}
	
	
	public void onTrialStart(int trialNumber_, int runNumber_) {
		super.onTrialStart(trialNumber_, runNumber_);
	}
	/**
	 * Called by CRController each time a trial ends.
	 * Saves all reports into a folder.
	 * @param trialNumber_ int ending trial number
	 * @param runNumber_ int current run number
	 */
	public void onTrialEnd(int trialNumber_, int runNumber_) {
		super.onTrialEnd(trialNumber_, runNumber_);
		
		if (CRSettings.getSingleton().getShouldPrintTextReports()) {
			//-- go through all stones and save their numbers based on their locations
			//   in quarter-circle bins going from the middle to the outside of the world
			World world = (World)controller.getWorld();
			int circleStepSize = 10; //by how much will radius of one cirlce be bigger than previous circle
			int numOfCircles = (int) (Math.hypot(world.getSize().x/2, world.getSize().y/2)/circleStepSize); //num of circles as max diameter divided by circle step
			int[][] numOfStones = new int[4][numOfCircles];
			for (int q=0;q<4;q++) {
				for (int c=0;c<numOfCircles;c++) {
					numOfStones[q][c] = 0;
				}
			}
			
			String stoneCartesianCoordinates = "";
			String stonePolarCoordinates = "";
			CRVector3d worldCenter = new CRVector3d(world.getSize().x/2,world.getSize().y/2,0);
			for (Stone stone : world.getStones()) {
				CRVector3d position = stone.getPosition();
				//-- get which quarter of the world the stone is in. 
				//   Quarters are numbered from north east, going anti-clockwise
				int quarterId = 0;
				if (position.x <= worldCenter.x) {
					if (position.y <= worldCenter.y) {
						quarterId = 1;
					} else {
						quarterId = 2;
					}
				} else {
					if (position.y > worldCenter.y) {
						quarterId = 3;
					}
				}
				
				//-- get which circle it belongs to, based on how far it is from the middle of the world
				int distanceToMiddle = (int)Math.round(CRMaths.getDistanceOfPoints(worldCenter.x, worldCenter.y, position.x, position.y));
				int circleNo = (int) Math.floor(distanceToMiddle/circleStepSize);
				if (circleNo >= numOfCircles) {
					circleNo = numOfCircles-1;
				}
				
				//-- record for this particular quarter of the circle that there is a stone
				numOfStones[quarterId][circleNo]++;
				
				//-- get angle to the middle
				double angle = Math.toDegrees(Math.atan2( worldCenter.y - position.y, position.x - worldCenter.x));
				
				//-- add to strings
				stoneCartesianCoordinates += position.x + "," + position.y + "\n";
				stonePolarCoordinates += distanceToMiddle + "," + angle + "\n";
			}
			
			//-- convert numOfStones array into csv
			String stoneDistributionString = "";
			for (int q=0;q<4;q++) {
				for (int c=0;c<numOfCircles;c++) {
					stoneDistributionString += numOfStones[q][c] + ",";
				}
				stoneDistributionString += "\n";
			}
			//System.out.println(outputString);
			CRFileIOHelper.stringToFile(stoneDistributionString, this.currentFilePath + "_StoneDistribution");
			CRFileIOHelper.stringToFile(stoneCartesianCoordinates, this.currentFilePath + "_StoneCartesian");
			CRFileIOHelper.stringToFile(stonePolarCoordinates, this.currentFilePath + "_StonePolar");
		}
		
	}

}
