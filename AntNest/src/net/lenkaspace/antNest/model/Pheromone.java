/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.lenkaspace.antNest.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import net.lenkaspace.creeper.vo.CRVector3d;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Miles
 */
public class Pheromone extends BaseWorldObject {
    private final int actW = 660;
    private final int gridW = 66;
    private final int d = 10;
    
    public double diffusion = 0.5;
    public double decay = 0.999;
    
    
    DoubleMatrix conc = DoubleMatrix.zeros(66,66);

    public Pheromone() {
        super(0, 0, new CRVector3d(0,0,0), new CRVector3d(660,660,0), 0, "");
        isRenderedByImage = false;
        addPheromoneGrid(50, 50);
    }

    public double getGradient(int x, int y) {
        int i,j;
        i = Math.round(y / d);
        j = Math.round(x / d);
        return conc.get(i,j);
    }
    
    public void addPheromoneAct(int x, int y) {
        int i,j;
        i = Math.round(y / d);
        j = Math.round(x / d);
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
    }
    
    public void step() {
        //decay
        conc.muli(decay);
    }
    

    public void paint(Graphics grphcs) {
        
        Graphics2D g2 = (Graphics2D)grphcs;
        
        for(int i = 0; i < conc.rows; i++) {
            for(int j = 0; j < conc.columns; j++) {
                if(conc.get(i,j) > 0) {
                    int x,y;
                    x = d * j;
                    y = d * i;
                    float cVal = (float) conc.get(i,j);
                    float alpha = cVal * 2f;
                    if (alpha > 1f)
                        alpha = 1f;
                    g2.setColor(new Color(
                            1f,1f - cVal ,1f ,alpha
                    ));
                    g2.fillRect(x+1, y+1, d-1, d-1);
                }
            }
        }
        
        g2.setColor(new Color(230,230,230));
        for(int x = 1; x < gridW;x++) {
            g2.drawLine(
                    (int)Math.round((float)x * d),
                    0, 
                    (int)Math.round((float)x * d), 
                    actW
            );
        }
        for(int y = 1; y < gridW; y++) {
            g2.drawLine(
                    0,
                    (int)Math.round((float)y * d),
                    actW, 
                    (int)Math.round((float)y * d)
            );
        }
        
    }

    @Override
    public void update() {
        super.update(); 
        step();
    }
    
    
}
