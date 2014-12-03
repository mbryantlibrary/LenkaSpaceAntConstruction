package net.lenkaspace.antNest;

import java.awt.Dimension;


import net.lenkaspace.antNest.images.ImageProvider;
import net.lenkaspace.antNest.model.World;
import net.lenkaspace.antNest.report.ReportController;
import net.lenkaspace.antNest.view.ControlPanel;
import net.lenkaspace.creeper.CRController;
import net.lenkaspace.creeper.CRSettings;
import net.lenkaspace.creeper.report.CRTimeSeriesReport;
import net.lenkaspace.creeper.report.CRWorldSnapshotReport;


public class AntNest {
	
	private CRController crController;
	
	/**
	 * Initialize UI: top level
	 */
	public static void main(String[] args) {
		new AntNest();
	}
	
	public AntNest() {
		CRSettings settings = CRSettings.getSingleton();
		
		
		//-- setup CRSettings:
		settings.setWindowSize(new Dimension(1190,720));
		settings.setInitialTimeSpeed(75);
		settings.setRenderingDelay(1);
		settings.setTimeUnitInterval(50);
		
		//-- create CRController
		crController = new CRController("Controlling Ant-Based Construction");
		
		//-- create image provider
		ImageProvider imageProvider = new ImageProvider();
		crController.setImageProvider(imageProvider);
		
		
		//-- setup new world and renderer size
		World world = new World(crController);
		crController.setWorld(world);
		crController.getRenderer().setPreferredSize(new Dimension(660,660));
		
		//-- setup reports
		ReportController reportController = new ReportController(crController);
		crController.setReportController(reportController);
		
		CRWorldSnapshotReport worldSnapshotReport = new CRWorldSnapshotReport(ReportController.REPORT_WORLD_SNAPSHOT, new Dimension((int)world.getSize().x, (int)world.getSize().y),500);
		reportController.addReport(ReportController.REPORT_WORLD_SNAPSHOT, worldSnapshotReport);
		
		CRTimeSeriesReport pushingTimeReport = new CRTimeSeriesReport(ReportController.REPORT_PUSHING_TIME, new String[] {"Time"}, new Dimension(800,400));
		pushingTimeReport.setIsScatterPlot(true);
		reportController.addReport(ReportController.REPORT_PUSHING_TIME, pushingTimeReport);
		

		//-- create new control panel that extends from CRControlPanel and set it
		ControlPanel controlPanel = new ControlPanel(485, 600, crController);
		crController.setControlPanel(controlPanel);
		
		String helpText = "";
		helpText += "********************************************************************************************************************\n";
		helpText += "**************   C O N T R O L L I N G    A N T - B A S E D    C O N S T R U C T I O N  **************\n";
		helpText += "********************************************************************************************************************\n";
		helpText += "\n";
		helpText += "Demonstration of ant-based construction algorithm. A colony conists of 'external' (green) ants that search ";
		helpText += "for stones and bring them towards the pheromone cloud (blue) and of 'internal' (red) ants that sort ";
		helpText += "stones to form a ring around them. The brood items (purple) serve as an initial physicial template. ";
		helpText += "Experiments where non-ciruclar nests are formed can be performed.\n";
		helpText += "\n";
		helpText += "For more info visit http://lenkaspace.net/lab/swarmSystems/controllingAntConstruction";
		helpText += "\n\n";
		helpText += "\n";
		helpText += "BASIC SETTINGS\n";	
		helpText += "Choose the world and the number of ants of each type. Press START SIMULATION.\n";
		helpText += "\n";
		helpText += "ANT MOVEMENT\n";
		helpText += "The movement of internal ants can be controlled via the 'pheromone movement threshold'. ";
		helpText += "This value dictates at which point an internal ant turns back towards the brood cluster. ";
		helpText += "Set this to lower values to make the movement less restricted.\n";
		helpText += "\n";
		helpText += "PHEROMONE AS A BUILDING TEMPLATE\n";
		helpText += "The pheromone cloud normally doesn't influence the probability of dropping stones by internal ants. ";
		helpText += "This behaviour can be changed by ticking the box 'Internal ants use pheromone as a building template'\n";
		helpText += "\n";
		helpText += "PHEROMONE CLOUD PROPERTIES\n";
		helpText += "It is possible to set diameter of a pheromone cloud. In case when more than 1 pheromone clouds are present, ";
		helpText += "it is also possible to set the distance between them.\n";
		controlPanel.getHelpPopup().setSize(new Dimension(600, 500));
		controlPanel.getHelpPopup().setText(helpText);
		
		
      
	}
}
