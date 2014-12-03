package net.lenkaspace.creeper;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import net.lenkaspace.creeper.images.CRImageProvider;
import net.lenkaspace.creeper.model.CRWorld;
import net.lenkaspace.creeper.report.CRReportController;
import net.lenkaspace.creeper.view.CRComponentFactory;
import net.lenkaspace.creeper.view.CRControlPanel;
import net.lenkaspace.creeper.view.CROutputPopup;
import net.lenkaspace.creeper.view.CRRenderer;
import net.lenkaspace.creeper.vo.CRVector3d;

/**
 * The main controller holding all the parts together.
 * Maintains the main update loop and other threads
 * 
 * @author      Lenka Pitonakova contact@lenkaspace.net
 * @version     1.0                                      
 */
public class CRController implements Runnable  {

	//-- objects
	protected JFrame mainFrame;
	protected Thread updateThread;
	protected CRRenderer renderer;
	protected CRImageProvider imageProvider;
	protected Timer renderTimer;
	protected Timer outputTimer;
	protected CRWorld world;
	protected CRControlPanel controlPanel;
	protected CRReportController reportController;
	protected JPanel controlPanelPlaceHolder; 
	
	//-- time, runs, trials
	protected int timeCounter; 
	protected int timeUnits;
	protected int updateDelay;
	protected int trialNumber;
	protected int totalTrials;
	protected int trialDuration;
	protected int runNumber;
	protected int totalRuns;
	
	/**
	 * Constructor
	 * @param applicationName_ Name of the application that will appear on the application window
	 */
	public CRController(String applicationName_) { 
		mainFrame = new JFrame();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle(applicationName_);
        
		//-- tell CRSettings init hasn't been completed yet
		CRSettings settings = CRSettings.getSingleton();
        settings.setIsInitDone(false);
        
		if (settings.checkSettings()) {
			
			//-- set variables
			updateDelay = settings.getMinUpdateDelay();
			totalTrials = 1;
			
	        //-- create UI
	        try {
	        	mainFrame.getContentPane().setBounds(0, 0, settings.getWindowSize().width, settings.getWindowSize().height);
	        	mainFrame.setSize(settings.getWindowSize().width, settings.getWindowSize().height);
	        	mainFrame.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));        
		        
		    	//--- create renderer:
		    	renderer = new CRRenderer();
		    	mainFrame.add("West", renderer);
		       
		        //--- create controls panel on the right:
		    	controlPanelPlaceHolder = CRComponentFactory.createFlowLayoutJPanel(485, 620, mainFrame);
	        } catch (Exception e) {
	            System.err.println("Cannot initialise GUI");
	        }
	       
	        //-- create reference to basic image provider
	        imageProvider = new CRImageProvider();
	        this.setImageProvider(imageProvider);
	        
	        //-- create reference to report controller and register self with it
	        reportController = new CRReportController(this);
	        
	        //-- create basic world and set it to renderer
	        world = new CRWorld(0, new CRVector3d(700,650,0), this);
	        renderer.setWorld(world);
	        
	        //-- create control panel
	        createDefaultControlPanel();
	        
	        //-- show the ui
	        mainFrame.setVisible(true);
	        
	        //-- tell settings that init is done
	        settings.setIsInitDone(true);
		}  
	}
	
	/**
	 * Create an instance of CRControlPanel and add it to the applet
	 */
	public void createDefaultControlPanel() {
		try {
			setControlPanel(new CRControlPanel(485,500,this));
		} catch (Exception e) {
            System.err.println("Cannot create default control panel");
        }
	}
	
	//==================================== INITIALIZATION ====================================

	/**
	 * Start all threads.
	 * Call after everything has been initialised.
	 */
	public void startSimulation(String reportFolderName_, int trialDuration_, int totalRuns_) {
		if (CRSettings.getSingleton().getIsInitDone()) {
			System.out.println("===================== SIMULATION STARTED ====================");
			//-- create update thread
			if (updateThread == null) {
				updateThread = new Thread(this);
			    updateThread.setPriority(Thread.NORM_PRIORITY);
			    updateThread.start();
			}
		    
		    //-- set the variables
		    timeCounter = 0;
			timeUnits = 0;
			
		    trialDuration = trialDuration_;
		    totalRuns = totalRuns_;
		    runNumber = 1;

		    //-- init the first run and start it
		    reportController.setReportFolderName(reportFolderName_);
			reportController.onSimulationStart();
		    initRun();
		    
		} else {
			System.err.println("CRController::start - initialisation not completed");
		}
		
		
	}
	
	/**
	 * Initialise a run (a collection of trials). 
	 */
	public void initRun() {
    	trialNumber = 1;
    	
    	//-- tell objects that a new run started
    	reportController.onRunStart(runNumber);
    	world.onRunStart(runNumber);
    	
		initTrial();
	}
	
	/**
	 * Initialise a trial. Create a new world with some new settings. 
	 */
	public void initTrial() {
		//-- tell objects that a new trial started
		reportController.onTrialStart(trialNumber, runNumber);
		world.onTrialStart(trialNumber, runNumber);
	
		//-- start the time
		timeCounter = 0;
    	timeUnits = 0;
		startTime();
	}
      
	//==================================== UPDATE LOOP ====================================
        
	/**
     * Main thread 
     */
    public void run() {
    	while (true) {
        	if (CRSettings.getSingleton().isTimeRunning()) {
        		//-- clear output panel so that new output can be shown
        		CROutputPopup outputPanel = CROutputPopup.getSingleton();
        		outputPanel.clearOutput();
	        	
        		//-- update all world objects
        		reportController.onUpdateLoopStart(timeCounter, timeUnits);
        		world.update();
        		reportController.onUpdateLoopEnd(timeCounter, timeUnits);
        		
        		//-- update time counter
        		timeCounter++;
	        	if (timeCounter == CRSettings.getSingleton().getTimeUnitInterval()) { 
	        		timeUnits++;
	        		timeCounter = 0;	
	        	}
	        	
	        	//-- show current time
	        	outputPanel.displayTimeOutput(" TIME ELAPSED: "+timeUnits + "  "+timeCounter + "    TRIAL:"+trialNumber+"/"+totalTrials + "     RUN:"+runNumber+"/"+totalRuns);
	        	
	        	//---- end trial?
	        	if (timeUnits - trialDuration == 0 && timeUnits > 0 ) {
	        		//-- tell the objects a trial ended
	        		world.onTrialEnd(trialNumber, runNumber);
	        		reportController.onTrialEnd(trialNumber, runNumber);
	        		//-- pause time
	        		stopTime();
	        		
	        		//--- end run (a collection of trials) as well?
	        		if (trialNumber == totalTrials) {
	        			//-- tell the objects a run ended
	        			world.onRunEnd(runNumber);
	        			reportController.onRunEnd(runNumber);
	        			//--- end simulation?
	        			if (runNumber >= totalRuns) {
	        				reportController.onSimulationEnd();
	        				System.out.println("===================== SIMULATION ENDED ====================");
	        				if (CRSettings.getSingleton().getShouldQuitAfterDone()) {
	        					System.exit(0);
	        				}
	        			} else {
	        				runNumber++;
	        				initRun();
	        			}
	        		} else {
	        			//--- just init a new trial
	        			trialNumber ++;
	        			this.initTrial();
	        		}
	        	}
        	}
        	//-- make update thread sleep
        	try {
	    		Thread.sleep(updateDelay);
	    	} catch (InterruptedException e) { 
	    		e.printStackTrace();
	    	};
    	}
    }
    
    
    //==================================== TIME CONTROL ====================================
    
    /**
     * Starts timers
     */
    public void startTime() {
    	
    	CRSettings settings = CRSettings.getSingleton();
    	
    	//-- create render thread
    	if (renderTimer != null && settings.isTimeRunning()) {
    		renderTimer.stop();
    	}
    	renderTimer = new Timer(settings.getRenderingDelay(), this.renderer);
    	renderTimer.setInitialDelay(0);
    	renderTimer.start();
    	
    	//-- create output thread
    	if (outputTimer != null && CRSettings.getSingleton().isTimeRunning()) {
    		outputTimer.stop();
    	}
    	outputTimer = new Timer(settings.getOutputDelay(), CROutputPopup.getSingleton());
    	outputTimer.setInitialDelay(0);
    	outputTimer.start();
    	
    	settings.setIsTimeRunning(true);
    }
    
    /**
     * Stop painter timer
     */
    public void stopTime() {
    	renderTimer.stop();
    	outputTimer.stop();
    	CRSettings.getSingleton().setIsTimeRunning(false);
    }	
    
   
    
   //==================================== GETTERS / SETTERS ==================================
    
    /**
     * Sets update timer delay to a value from interval specified by CRSettings's maxUpdateDelay and minUpdateDelay
     * @param value_ <0;1> where 0 = slowest
     */
    public void setUpdateDelay(double value_) {
    	CRSettings settings = CRSettings.getSingleton();
    	updateDelay = (settings.getMaxUpdateDelay() - (int)((settings.getMaxUpdateDelay()-settings.getMinUpdateDelay())*value_));
    }
    
    
    /**
     * Remove old control panel, store reference to the new one and place it instead of the old one
     * @param controlPanel_ CRControlPanel new control panel
     */
    public void setControlPanel(CRControlPanel controlPanel_) {
    	//-- remove the new one:
    	controlPanelPlaceHolder.removeAll();
    	controlPanel = null;
    	
    	
    	//-- resize the placeholder based on new width
    	controlPanel = controlPanel_;
    	if (controlPanel_ != null) {
    		int height = controlPanelPlaceHolder.getSize().height;
    		if (controlPanel.getSize().height > height) {
    			height = controlPanel.getSize().height;
    		}
    		Dimension newPanelSize = new Dimension(controlPanel.getSize().width, height);
    		controlPanelPlaceHolder.setSize(newPanelSize);
    		controlPanelPlaceHolder.setPreferredSize(newPanelSize);
    		controlPanelPlaceHolder.add(controlPanel);
    	}
    	
    	//-- add powered by
    	CRComponentFactory.createDividerJPanel(-1, 0, controlPanelPlaceHolder);
    	JPanel poweredByPanel = CRComponentFactory.createFlowLayoutJPanel(-1, 20, controlPanelPlaceHolder);
    	poweredByPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 10));
    	JLabel poweredBy = CRComponentFactory.createJLabel("Powered by Creeper [http://lenkaspace.net/code/java/creeper]", poweredByPanel);
    	poweredBy.setFont(new Font("Monospaced",Font.PLAIN, 9)); 
    	
    }
    
    public CRWorld getWorld() { return world; }
    public void setWorld(CRWorld world_) {
    	world = world_;
    	renderer.setWorld(world);
    }
    
    public CRReportController getReportController() { return reportController; }
    public void setReportController(CRReportController reportController_) { 
    	reportController = reportController_; 
    }
    
    public CRImageProvider getImageProvider() { return imageProvider; }
    public void setImageProvider(CRImageProvider imageProvider_) { 
    	imageProvider = imageProvider_;
    	if (renderer != null) {
    		renderer.setImageProvider(imageProvider_);
    	}
    }
    
    public int getTimeUnits() { return timeUnits; }
    public int getTimeCounter() { return timeCounter; }
    public int getTimeCounterSinceTrialStart() { return timeUnits*CRSettings.getSingleton().getTimeUnitInterval() + timeCounter; }
    public int getTotalTrials() { return totalTrials; }
    public void setTotalTrials(int value_) { totalTrials = value_; }
    public int getTotalRuns() { return totalRuns; }
    public int getTrialDuration() { return trialDuration; }
    public int getTrialNumber() { return trialNumber; }
    public int getRunNumber() { return runNumber; }
    
    public CRRenderer getRenderer() { return renderer; }
   
}
