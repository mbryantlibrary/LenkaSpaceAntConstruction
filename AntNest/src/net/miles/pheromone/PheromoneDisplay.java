/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.miles.pheromone;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Miles
 */
public class PheromoneDisplay extends JPanel{
    
    private final int actW = 660;
    private final int gridW = 66;
    private final int d = 10;
    
    public double diffusion = 0.5;
    public double decay = 0.9;
    
    
    DoubleMatrix conc = DoubleMatrix.zeros(66,66);

    public PheromoneDisplay() {
        
        setPreferredSize(new Dimension(actW, actW));
        
        addPheromoneGrid(0, 0);
        
    }
    
    public void addPheromoneAct(int x, int y) {
        int i,j;
        i = Math.round(y / d);
        j = Math.round(x / d);
        System.out.printf("Adding at point %d,%d in grid %d,%d\n",x,y,i,j);
        addPheromoneGrid(i, j);
    }
    
    public void addPheromoneGrid(int i, int j) {
        //put in center
        conc.put(i,j,0.5);
        
        //put around center
        if(i > 0)
            conc.put(i-1,j,0.5*diffusion);
        if(j > 0)
            conc.put(i,j-1,0.5*diffusion);
        if(i < gridW)
            conc.put(i+1,j,0.5*diffusion);
        if(j < gridW)
            conc.put(i,j+1,0.5*diffusion);
        
        repaint();
    }
    
    public void step() {
        //decay
        conc.muli(decay);
        repaint();
    }
    

    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs); //To change body of generated methods, choose Tools | Templates.
        
        Graphics2D g2 = (Graphics2D)grphcs;
        
        for(int i = 0; i < conc.rows; i++) {
            for(int j = 0; j < conc.columns; j++) {
                if(conc.get(i,j) > 0) {
                    int x,y;
                    x = d * j;
                    y = d * i;
                    int cVal = (int)Math.round(conc.get(i,j)*255);
                    g2.setColor(new Color(
                            255, 255-cVal, 255
                    ));
                    g2.fillRect(x+1, y+1, d-1, d-1);
                }
            }
        }
        
        g2.setColor(Color.GRAY);
        for(int x = 1; x < gridW;x++) {
            g2.drawLine(
                    (int)Math.round((float)x * d),
                    0, 
                    (int)Math.round((float)x * d), 
                    getSize().height
            );
        }
        for(int y = 1; y < gridW; y++) {
            g2.drawLine(
                    0,
                    (int)Math.round((float)y * d),
                    getSize().width, 
                    (int)Math.round((float)y * d)
            );
        }
        
    }
    
    
    
    
}
