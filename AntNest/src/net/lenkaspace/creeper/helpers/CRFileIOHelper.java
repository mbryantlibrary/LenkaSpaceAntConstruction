package net.lenkaspace.creeper.helpers;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import org.apache.commons.io.FileUtils;


/**
 * Provides static method for saving Java component images and text into files.
 * 
 * @author      Lenka Pitonakova contact@lenkaspace.net
 * @version     1.0                                      
 */
public class CRFileIOHelper {

	private static BufferedImage bi;
	private static ImageWriter iw;
	
	/**
	 * Print a Java Component into an image file
	 * @param component_ Component to print
	 * @param fileName String filename without an extension
	 */
	public static void componentToJpeg(Component component_ , String fileName){ 
		if (fileName.length() > 0) {
			//-- add .jpg to file name:
			fileName += ".jpg";
			try { 
				
				Rectangle rect = component_.getBounds() ; 
				if (bi == null || bi.getWidth() != rect.width || bi.getHeight() != rect.getHeight()) {
					bi = new BufferedImage( rect.width,rect.height, BufferedImage.TYPE_USHORT_565_RGB) ; 
				}
				component_.paint(bi.getGraphics() ) ; 
				
				ImageIO.write(bi, "jpg", new File(fileName));
				
			} catch (Exception e){
				System.err.println("Error in writing " + fileName +":" +e.getMessage()) ;
			} 
		}
	}
	
	/**
	 * Put a string into a new txt file
	 * @param stringToPrint_ String contents of the file
	 * @param fileName_ String filename without an extension
	 */
	public static void stringToFile (String stringToPrint_, String fileName_) {
		CRFileIOHelper.stringToFile(stringToPrint_, fileName_, "txt");
	}
	
	/**
	 * Put a string into a new file
	 * @param stringToPrint_ String contents of the file
	 * @param fileName_ String filename without an extension
	 * @param extension_ String extension without a dot. Set to 'txt' by default.
	 */
	public static void stringToFile (String stringToPrint_, String fileName_, String extension_) {
		if (fileName_.length() > 0) {
			//-- check extension
			if (extension_.length() == 0) {
				extension_ = "txt";
			}
			//-- add extension to file name:
			fileName_ += "." + extension_;
			try {
				System.out.println("PRINTING "+fileName_);
				File file = new File(fileName_);
				FileUtils.writeStringToFile(file, stringToPrint_);
			} catch (Exception e) {
				System.err.println("Error in writing " + fileName_ +":" +e.getMessage()) ;
			}
		}
	}
}
