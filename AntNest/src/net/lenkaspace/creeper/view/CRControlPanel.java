package net.lenkaspace.creeper.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import net.lenkaspace.creeper.CRController;
import net.lenkaspace.creeper.CRSettings;
import net.lenkaspace.creeper.report.CRBaseReport;

/**
 * The control panel displayed on the right of the applet by default. 
 * 
 * @author      Lenka Pitonakova contact@lenkaspace.net
 * @version     1.0                                      
 */
@SuppressWarnings("serial")
public class CRControlPanel extends JPanel {

	protected CRController controller;
	
	protected JPanel helpPanel;
	protected CRTextPopup helpPopup;
	
	protected JPanel reportFolderPanel;
	protected JTextField reportFolderField;
	
	protected JPanel preStartSimulationPanel;
	
	protected JPanel startSimulationPanel;
	protected JTextField numberOfRunsField;
	protected JTextField trialDurationField;
	
	protected JPanel timeSettingsPanel;
	protected JButton timeStartPauseButton;
	protected JSlider timeSpeedSlider;
	
	protected JPanel basicOutputPanel;
	
	protected JPanel reportOutputPanel;
	protected JComboBox reportTypeComboBox;
	
	/**
	 * Constructor
	 * @param width_ int width
	 * @param height_ int height
	 * @param controller_ CRController holding controller instance
	 */
	public CRControlPanel(int width_, int height_, CRController controller_) {
		controller = controller_;
		this.setBounds(0, 0, width_, height_);
		this.setSize (width_,height_);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		//---- create help panel
		helpPopup = new CRTextPopup("Help", new Dimension(300,300));
		helpPopup.setText("Powered by Creeper. \nhttp://lenkaspace.net/code/java/creeper");
		
		helpPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 50, this);
		helpPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
		CRComponentFactory.createJButton("?", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helpPopup.open();
			}
		}, helpPanel);	

		CRSettings settings = CRSettings.getSingleton();
		
		//---- create report folder panel if this is not build for online applet
		reportFolderPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 80, this);
		CRComponentFactory.createJLabel("Saved reports folder name: ", reportFolderPanel);
		DateFormat dateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
		Date date = new Date();
		reportFolderField = CRComponentFactory.createJTextField("reports" + dateFormat.format(date) , this.getSize().width - 190, reportFolderPanel);
		
		CRComponentFactory.createFlowLayoutJPanel(-1, 10, reportFolderPanel);
		
		CRComponentFactory.createJCheckBox("Save graphic reports ", settings.getShouldPrintGraphicReports(), new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CRSettings.getSingleton().setShouldPrintGraphicReports(((JCheckBox)e.getSource()).isSelected());	
			}
		}, reportFolderPanel);
		CRComponentFactory.createJCheckBox("Save text reports       ", settings.getShouldPrintGraphicReports(), new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CRSettings.getSingleton().setShouldPrintTextReports(((JCheckBox)e.getSource()).isSelected());	
			}
		}, reportFolderPanel);
		CRComponentFactory.createJCheckBox("Quit when done", settings.getShouldPrintGraphicReports(), new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CRSettings.getSingleton().setShouldQuitAfterDone(((JCheckBox)e.getSource()).isSelected());	
			}
		}, reportFolderPanel);
		
	    
		//---- create panel above the start simulation panel
		preStartSimulationPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 0, this);
		
		//---- create the start simulation button in a separate panel
		startSimulationPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 40, this);
		
		//-- number of runs
		CRComponentFactory.createJLabel("Number of runs: ", startSimulationPanel);
		numberOfRunsField = CRComponentFactory.createJTextField("1", 50, startSimulationPanel);
		
		//-- trial duration
		CRComponentFactory.createJLabel("  Trial duration: ", startSimulationPanel);
		trialDurationField = CRComponentFactory.createJTextField("120", 50, startSimulationPanel);
		
		//-- start button
		CRComponentFactory.createJLabel("     ", startSimulationPanel);
		CRComponentFactory.createJButton("Start simulation", new ActionListener() {	public void actionPerformed(ActionEvent e) {
                onStartSimulationClicked(e);
        }}, startSimulationPanel);
		
		//---- create divider
		CRComponentFactory.createDividerJPanel(-1, 5, this);
		        
		//---- create time settings
		timeSettingsPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 60, this);
		
		//-- play / pause
		timeStartPauseButton = CRComponentFactory.createJButton(">", new ActionListener() {	public void actionPerformed(ActionEvent e) {
			CRSettings settings = CRSettings.getSingleton();
			if (settings.isTimeRunning()) {
				//---- pause clicked:
				controller.stopTime();
				settings.setIsTimeRunning(false);
				onTimePaused();	
			} else {
				//----- play clicked:	
				controller.startTime();
				settings.setIsTimeRunning(true);
				onTimeStarted();
			}
		}}, timeSettingsPanel);
		timeStartPauseButton.setEnabled(false);
		
		CRComponentFactory.createJLabel("   Time speed:", timeSettingsPanel);
		
		//-- slider
		timeSpeedSlider = CRComponentFactory.createJSlider(this.getSize().width - 170, 0, 100, settings.getInitialTimeSpeed(), new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		if (CRSettings.getSingleton().isTimeRunning()) {
        			controller.setUpdateDelay(timeSpeedSlider.getValue()/100.0);
        		}
        	}
        }, timeSettingsPanel);
		
		setTimeControlsEnabled(false);
		
		
		//---- create basic output panel: console + draw area
		basicOutputPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 40, this);
		
		//-- open console button
		CRComponentFactory.createJButton("Open output console", new ActionListener() { public void actionPerformed(ActionEvent e) {
			CROutputPopup.getSingleton().open();
		}}, basicOutputPanel);
		
		//-- draw check box
		CRComponentFactory.createJCheckBox("Draw world", true, new ActionListener() { public void actionPerformed(ActionEvent e) {
			CRSettings.getSingleton().setShouldDraw(((JCheckBox)e.getSource()).isSelected());
		}}, basicOutputPanel);
		
		
		
		//---- create report output panel
		String[] reportNames = controller.getReportController().getReportNames(true);
		if (reportNames.length > 0) {
			reportOutputPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 40, this);
			
			//-- combo box
			reportTypeComboBox = CRComponentFactory.createJComboBox(this.getSize().width - 135,reportNames, 0, null, reportOutputPanel);
			
			//-- button
			CRComponentFactory.createJButton("Open report", new ActionListener() { public void actionPerformed(ActionEvent e) {
				onOpenReportClicked(reportTypeComboBox.getSelectedItem().toString());
			}}, reportOutputPanel);
		}
		
		
		
	}
	
	//==================================== BUTTON ACTIONS =================================
	
	/**
	 * Called when Start Simulation button is pressed
	 * @param e ActionEvent action event
	 */
	protected void onStartSimulationClicked(ActionEvent e) {
		String reportFolderName = "";
        if (reportFolderField != null) {
        	reportFolderName = reportFolderField.getText();
        }
        controller.setUpdateDelay(timeSpeedSlider.getValue()/100.0);
		controller.startSimulation(reportFolderName, Integer.parseInt(trialDurationField.getText()), Integer.parseInt(numberOfRunsField.getText()));
        setTimeControlsEnabled(true);
        onTimeStarted();
	}
	
	/**
	 * Called when the Open Report button is pressed.
	 * Tell CRReportController what to do.
	 * @param reportName_ String selected report name
	 */
	protected void onOpenReportClicked(String reportName_) {
		CRBaseReport report = controller.getReportController().getReport(reportName_);
		if (report != null) {
			report.display();	
		}
	}
	
	/**
	 * Make the time slider and the '>' / '||' button enabled / disabled
	 * @param enabled_ boolean true to enable
	 */
	private void setTimeControlsEnabled(boolean enabled_) {
		timeStartPauseButton.setEnabled(enabled_);
		timeSpeedSlider.setEnabled(enabled_);
	}
	
	/**
	 * Called when time is started.
	 * Set the timeStartPauseButton text to '||'
	 */
	public void onTimeStarted() {
		timeStartPauseButton.setText("||");
	}
	
	/**
	 * Called when time is paused.
	 * Set the timeStartPauseButton text to '>'
	 */
	public void onTimePaused() {
		timeStartPauseButton.setText(">");
	}

	//==================================== GETTERS / SETTERS  =============================
	
	public JPanel getHelpPanel() { return helpPanel; }
	public CRTextPopup getHelpPopup() { return helpPopup; }
	public JPanel getReportFolderPanel() { return reportFolderPanel; }
	public JTextField getReportFolderField() { return reportFolderField; }
	public JPanel getStartSimulationPanel() { return startSimulationPanel; }
	public JPanel getPreStartSimulationPanel() { return preStartSimulationPanel; }
	public JTextField getNumberOfRunsField() { return numberOfRunsField; }
	public JTextField getTrialDurationField() {	return trialDurationField; }
	public JPanel getTimeSettingsPanel() { return timeSettingsPanel; }
	public JButton getTimeStartPauseButton() { return timeStartPauseButton;	}
	public JSlider getTimeSpeedSlider() { return timeSpeedSlider;  }
	public JPanel getBasicOutputPanel() { return basicOutputPanel; }	
	public JPanel getReportOutputPanel() { return reportOutputPanel; }
	public JComboBox getReportTypeComboBox() { return reportTypeComboBox; }

		
}
