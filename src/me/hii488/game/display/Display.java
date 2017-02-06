package me.hii488.game.display;

import java.awt.Canvas;
import java.awt.Graphics;

import me.hii488.game.Controller;

@SuppressWarnings("serial")
public class Display extends Canvas{
	
	public int cameraSpeed = 2;
	public int scale = 1;
	
	public Display(Window window) {
		setBounds(0, 0, window.WIDTH, window.HEIGHT);
		this.addKeyListener(new Controller());
	}
	
	public void render(Graphics g){
		
		Controller.render(g);
		
		if(Controller.paused) g.drawString("Paused", 5, 30);
	}


}
