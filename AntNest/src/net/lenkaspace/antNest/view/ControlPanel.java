package net.lenkaspace.antNest.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

import net.lenkaspace.antNest.Settings;
import net.lenkaspace.antNest.model.World;
import net.lenkaspace.creeper.CRController;
import net.lenkaspace.creeper.view.CRComponentFactory;
import net.lenkaspace.creeper.view.CRControlPanel;

@SuppressWarnings("serial")
public class ControlPanel extends CRControlPanel {

	private JTextField numOfPassiveAntsField;
	private JTextField numOfExternalWorkersField;
	private JTextField numOfInternalWorkersField;
	private JComboBox worldType;
	private JCheckBox usePheromoneTemplate;
	private JTextField pheromoneMovementThreshold;
	private JTextField normalBroodClusterDiam;
	private JTextField broodClusterDistance;
	
	public ControlPanel(int width_, int height_, CRController controller_) {
		super(width_, height_, controller_);
		
		Settings settings = Settings.getSingleton();
		
		//-- change default values of textfields
		trialDurationField.setText("6000");
		
		//-- add start simulation settings,
		preStartSimulationPanel.setPreferredSize(new Dimension(preStartSimulationPanel.getPreferredSize().width, 180));
		
		Dimension newSize = startSimulationPanel.getPreferredSize();
		newSize.height = 60;
		startSimulationPanel.setPreferredSize(newSize);
		
		CRComponentFactory.createJLabel("World: ", preStartSimulationPanel);
		worldType = CRComponentFactory.createJComboBox(250, 
				new String[] {World.RANDOM_WORLD, World.TWO_HORIZ_CLUSTERS, World.TWO_VERT_CLUSTERS,World.TRIANGLE_WORLD, World.SQUARE_WORLD}, 
				0, null,preStartSimulationPanel);
		CRComponentFactory.createJLabel("                                    ", preStartSimulationPanel);
		
		CRComponentFactory.createJLabel("External workers: ", preStartSimulationPanel);
		numOfExternalWorkersField = CRComponentFactory.createJTextField("10", 50, preStartSimulationPanel);
		CRComponentFactory.createJLabel(" Internal workers: ", preStartSimulationPanel);
		numOfInternalWorkersField = CRComponentFactory.createJTextField("10", 50, preStartSimulationPanel);
		CRComponentFactory.createJLabel(" Brood items: ", preStartSimulationPanel);
		numOfPassiveAntsField = CRComponentFactory.createJTextField("50", 50, preStartSimulationPanel);
		
		CRComponentFactory.createJLabel("Internal ants pheromone movement threshold (%):", preStartSimulationPanel);
		pheromoneMovementThreshold = CRComponentFactory.createJTextField(String.valueOf((int)settings.pheromoneMovementThreshold*100), 50, preStartSimulationPanel);
		
		usePheromoneTemplate = CRComponentFactory.createJCheckBox("Internal ants use pheromone as a building template        ", settings.usePheromoneTemplate, null, preStartSimulationPanel);
		
		CRComponentFactory.createFlowLayoutJPanel(-1, 15, preStartSimulationPanel);
		
		CRComponentFactory.createJLabel("Standard pheromone cloud diameter:", preStartSimulationPanel);
		normalBroodClusterDiam = CRComponentFactory.createJTextField(String.valueOf((int)settings.normalBroodClusterDiam), 50, preStartSimulationPanel);
		CRComponentFactory.createJLabel("    Cloud distance:", preStartSimulationPanel);
		broodClusterDistance = CRComponentFactory.createJTextField(String.valueOf((int)settings.broodClusterDistance), 50, preStartSimulationPanel);
		
		
		//-- add rendering settings
		JPanel renderingPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 30, this);
		
		CRComponentFactory.createJCheckBox("Render ants", settings.showAnts, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.getSingleton().showAnts = ((JCheckBox)e.getSource()).isSelected();				
			}
		}, renderingPanel);
		
		CRComponentFactory.createJCheckBox("Render stones", settings.showStones, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.getSingleton().showStones = ((JCheckBox)e.getSource()).isSelected();				
			}
		}, renderingPanel);
		
		CRComponentFactory.createJCheckBox("Render pheromone", settings.showPheromone, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.getSingleton().showPheromone = ((JCheckBox)e.getSource()).isSelected();				
			}
		}, renderingPanel);
	}
	
	//==================================== BUTTON ACTIONS =================================
	protected void onStartSimulationClicked(ActionEvent e) {
		Settings settings = Settings.getSingleton();
		settings.numOfExternalWorkers = Integer.parseInt(numOfExternalWorkersField.getText());
		settings.numOfInternalWokers = Integer.parseInt(numOfInternalWorkersField.getText());
		settings.numOfPassiveAnts = Integer.parseInt(numOfPassiveAntsField.getText());
		settings.currentWorld = worldType.getSelectedItem().toString();
		settings.usePheromoneTemplate = usePheromoneTemplate.isSelected();
		settings.pheromoneMovementThreshold = Integer.valueOf(pheromoneMovementThreshold.getText())/100.0;
		settings.normalBroodClusterDiam = Integer.parseInt(normalBroodClusterDiam.getText());
		settings.broodClusterDistance = Integer.parseInt(broodClusterDistance.getText());
		super.onStartSimulationClicked(e);
	}
	

}
