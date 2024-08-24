package org.mishach.ServiceInfo;

import java.awt.*;

public class GraphicWorker {

    private Graphics graphics;
    public GraphicWorker(Graphics graphics){
        this.graphics = graphics;
        setUpGraphics();
    }

    private void setUpGraphics() {
        Font font = new Font("Calibre", Font.PLAIN, 56);
        graphics.setFont(font);
        graphics.setColor(Color.BLACK);
    }

    public Graphics getGraphics(){
        return graphics;
    }
}
