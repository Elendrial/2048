package me.hii488.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;


public class Grid {
	
	private int[][] grid; // [y][x]
	public int[] spawnList;
	private Point dimensions;
	
	public Grid setup(){
		return this.setup(4, 4, new int[]{2,4});
	}
	
	
	public int[][] copyGrid(){
		int[][] copy = new int[grid.length][grid[0].length];
		
		for(int i = 0; i < copy.length; i++) for(int j = 0; j < copy[0].length; j++) copy[i][j] = grid[i][j];
		
		return copy;
	}
	
	@SuppressWarnings("unused")
	public Grid setup(int x, int y, int[] canSpawn){
		grid = new int[y][x];
		
		for(int[] b : grid)	for(int b2 : b)	b2 = 0;
		
		dimensions = new Point(x,y);
		
		spawnList = canSpawn;
		
		return this;
	}
	
	public Point getDimensions(){
		return dimensions;
	}
	
	public void clear(){
		for(int i = 0; i < this.getDimensions().getX(); i++){
			for(int j = 0; j < this.getDimensions().getY(); j++){
				this.setCell(0, i,j);
			}
		}
	}
	
	public void setCell(int i, int x, int y){
		try{grid[y][x] = i;}
		catch(Exception e){e.printStackTrace();}
	}
	
	public void setCell(int i, Point p){
		try{grid[p.y][p.x] = i;}
		catch(Exception e){e.printStackTrace();}
	}
	
	public int getCell(int x, int y){
		try{return grid[y][x];}
		catch(Exception e){e.printStackTrace(); return -1;}
	}
	
	public int getCell(Point p){
		try{return grid[p.y][p.x];}
		catch(Exception e){e.printStackTrace(); return -1;}
	}

	public static Font f = new Font("Calibri", Font.PLAIN, 24);
	public void render(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, Controller.win.WIDTH, Controller.win.HEIGHT);
		
		g.setColor(Color.GRAY);
		
		for(int i = 0; i < grid.length; i++){
			for(int j = 0; j < grid[0].length; j++){
				g.fillRect(i * Controller.win.WIDTH/this.grid.length + 10, j * Controller.win.HEIGHT/this.grid.length + 10, Controller.win.WIDTH/this.grid.length -20, Controller.win.HEIGHT/this.grid.length - 20);
			}
		}
		
		Font f2 = g.getFont();
		g.setFont(f);
		
		for(int i = 0; i < grid.length; i++){
			for(int j = 0; j < grid[0].length; j++){
				g.setColor(new Color((Math.log(getCell(j,i)) * 30 < 255 ? (int) Math.log(getCell(j,i)) * 30 : 255), Math.log(getCell(j,i)) * 10 < 255 ? 255 - (int) Math.log(getCell(j,i)) * 10 : 0, 0));
				g.drawString(this.getCell(j, i) + "", j * Controller.win.WIDTH/this.grid.length +  Controller.win.WIDTH/this.grid.length/2, i * Controller.win.HEIGHT/this.grid.length + 5 + Controller.win.HEIGHT/this.grid.length/2);
			}
		}
		
		g.setColor(c);
		g.setFont(f2);
	}	
	
	@Override
	public boolean equals(Object o){
		if(o instanceof int[][]){
			boolean similar = true;
			try{for(int i = 0; i < grid.length && similar; i++){
				for(int j = 0; j < grid[0].length && similar; j++){
					if(((int[][])o)[i][j] != grid[i][j]) similar = false;
				}
			}}catch(Exception e){similar = false;}
			return similar;
		}
		else{
			return super.equals(o);
		}
	}
	
}
