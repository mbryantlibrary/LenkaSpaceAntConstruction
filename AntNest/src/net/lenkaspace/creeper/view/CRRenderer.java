package net.lenkaspace.creeper.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import net.lenkaspace.creeper.CRSettings;
import net.lenkaspace.creeper.images.CRImageProvider;
import net.lenkaspace.creeper.model.CRBaseSituatedModel;
import net.lenkaspace.creeper.model.CRWorld;
import net.lenkaspace.creeper.vo.CRVector3d;

/**
 * Uses Java's native rendering techniques to display CRBaseSituatedModel objects in the world.
 * 
 * @author      Lenka Pitonakova contact@lenkaspace.net
 * @version     1.0                                      
 */
@SuppressWarnings("serial")
public class CRRenderer extends Component implements ActionListener {
	
	//-- image name constants
	public static String CR_BLACK_DOT 	= "crBlackDot.png";
	public static String CR_BLUE_DOT 	= "crBlueDot.png";
	public static String CR_GRAY_DOT 	= "crGrayDot.png";
	public static String CR_GREEN_DOT 	= "crGreenDot.png";
	public static String CR_PURPLE_DOT 	= "crPurpleDot.png";
	public static String CR_RED_DOT 	= "crRedDot.png";
	//--
	
	protected boolean shouldOverrideNoPaint = false; //useful when CRRendered needs re-painting even though it normally shouldn't
    protected CRWorld world;    
    protected Dimension size;
    protected CRImageProvider imageProvider;
    protected CRImageProvider crImageProvider;
    
    
    public CRRenderer() {
    	this.setPreferredSize(new Dimension(700,650));
    	crImageProvider = new CRImageProvider();
    }
    
    /**
     * Called each time interval. Calls repaint.
     */
    public void actionPerformed(ActionEvent e) {
    	this.repaint();
	}
    
    /**
     * Main rendering function
     */
    public void paint(Graphics g) {
    	
    	Dimension size = getSize();
    	//-------- draw background
 	    g.setColor(Color.WHITE);   
 	    g.fillRect(0, 0, size.width, size.height);
  
	    CRSettings settings = CRSettings.getSingleton();
	    if ((settings.getIsInitDone() && settings.getShouldDraw() && settings.isTimeRunning()) || shouldOverrideNoPaint) {
		    if (shouldOverrideNoPaint) {
		    	//make sure only paints once..
		    	shouldOverrideNoPaint = false;
		    }
	    	//-- draw all renderable objects in the world
		    for (CRBaseSituatedModel situatedModel : world.getSituatedModels() ) {
		    	String imageFileName = situatedModel.getImageFileName();
		    	if (situatedModel.isVisible()) {
                            if (situatedModel.isRenderedByImage()) {
                                if(imageFileName != null && imageFileName != "") {
                                    //-- get the image either from Creeper native CRImageProvider or from a specified image provider
                                    BufferedImage image;
                                    if (imageFileName == CR_BLACK_DOT || imageFileName == CR_BLUE_DOT || imageFileName == CR_GRAY_DOT || imageFileName == CR_GREEN_DOT || imageFileName == CR_PURPLE_DOT || imageFileName == CR_RED_DOT) {
                                            image = crImageProvider.getImage(imageFileName);
                                    } else {
                                            image = imageProvider.getImage(imageFileName);
                                    }
                                    if (image != null) {
                                            drawImageAt(image, situatedModel.getPosition(), situatedModel.getSize(), Math.toRadians(situatedModel.getRotation()), g);
                                    }
                                }
                            } else {
                                situatedModel.paint(g);
                            }
		    	}
		    }
	    }
	    
 	    //-------- draw border
 	    g.setColor(new Color(0,0,0 ));   
 	    g.drawRect(0, 0, size.width-1, size.height-2); 
 
    }
    
    
    //==================================== IMAGES ========================================
        
    /**
     * Draw an image
     * @param img_ BufferedImage to draw
     * @param pos_ CRVector3d position [x,y,z]
     * @param rot_ double rotation in radians
     * @param g Graphics object
     */
    public void drawImageAt(BufferedImage img_, CRVector3d pos_, CRVector3d size_, double rot_, Graphics g ) {
    	if (img_ != null) {
	    	Graphics2D g2d = (Graphics2D)g;
	        AffineTransform origXform = g2d.getTransform();
	        //--- rotate about image's origin:
	        g2d.rotate(rot_, pos_.x , pos_.y );
	        //-- adjust x and y based on image size:
	        int imgX = (int) (pos_.x - size_.x/2);
	    	int imgY = (int) (pos_.y - size_.y/2);
	        g2d.drawImage(img_, imgX, imgY, (int)size_.x, (int)size_.y, this);
	        //---- prevent the transformation from affecting other images:
	        g2d.setTransform(origXform);	
    	}
    }
       
    //==================================== GETTERS / SETTERS =================================
    
    public void setShouldOverrideNoPaint(boolean value_) { shouldOverrideNoPaint = value_; }
    public void setWorld(CRWorld world_) { world = world_; }
    
    public CRImageProvider getImageProvider() { return imageProvider; }
    public void setImageProvider(CRImageProvider imageProvider_) { imageProvider = imageProvider_; }
}
