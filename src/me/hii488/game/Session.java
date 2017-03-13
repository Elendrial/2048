package me.hii488.game;

import java.awt.Color;
import java.awt.Graphics;

import me.hii488.GeneticAlg;

public class Session implements Runnable{
	
	public static int amountOfSessions = 0;
	
	public static Session makeNewSession(){
		if(Controller.sessions.length > amountOfSessions){
			Session s = new Session();
			s.sessionNumber = amountOfSessions;
			s.grid = new Grid().setup();
			amountOfSessions++;
			return s;
		}
		return null;
	}
	
	public static Session makeNewSession(int x, int y, int[] canSpawn){
		if(Controller.sessions.length > amountOfSessions){
			Session s = new Session();
			s.sessionNumber = amountOfSessions +1;
			s.grid = new Grid().setup(x, y, canSpawn);
			amountOfSessions += 1;
			return s;
		}
		return null;
	}
	
	public int score;
	public int sessionNumber;
	public Grid grid;
	
	public void moveUp(){
		slideUp();
		combineUp();
		slideUp();
		spawnRand();
	}
	
	public void slideUp(){
		for(int a = 0; a < grid.getDimensions().y-1; a++){
			for(int i = 1; i < grid.getDimensions().y; i++){
				for(int j = 0; j < grid.getDimensions().x; j++){
					if(grid.getCell(j,i-1) == 0){
						grid.setCell(grid.getCell(j, i), j, i-1);
						grid.setCell(0, j, i);
					}
				}
			}
		}
	}
	
	public void combineUp(){
		for(int i = 1; i < grid.getDimensions().y; i++){
			for(int j = 0; j < grid.getDimensions().x; j++){
				if(grid.getCell(j,i-1) == grid.getCell(j,i)){
					grid.setCell(grid.getCell(j, i) * 2, j ,i-1);
					grid.setCell(0, j, i);
					score += grid.getCell(j, i-1);
				}
			}
		}
	}
	
	
	
	public void moveDown(){
		slideDown();
		combineDown();
		slideDown();
		spawnRand();
	}
	
	public void slideDown(){
		for(int a = 0; a < grid.getDimensions().y-1; a++){
			for(int i = grid.getDimensions().y-2; i >= 0; i--){
				for(int j = 0; j < grid.getDimensions().x; j++){
					if(grid.getCell(j,i+1) == 0){
						grid.setCell(grid.getCell(j, i), j, i+1);
						grid.setCell(0, j, i);
					}
				}
			}
		}
	}
	
	public void combineDown(){
		for(int i = grid.getDimensions().y-2; i >= 0; i--){
			for(int j = 0; j < grid.getDimensions().x; j++){
				if(grid.getCell(j,i+1) == grid.getCell(j,i)){
					grid.setCell(grid.getCell(j, i) * 2, j ,i+1);
					grid.setCell(0, j, i);
					score += grid.getCell(j, i+1);
				}
			}
		}
	}
	
	
	
	public void moveRight(){
		slideRight();
		combineRight();
		slideRight();
		spawnRand();
	}
	
	public void slideRight(){
		for(int a = 0; a < grid.getDimensions().x-1; a++){
			for(int i = 0; i < grid.getDimensions().y; i++){
				for(int j = grid.getDimensions().x-2; j >= 0; j--){
					if(grid.getCell(j+1,i) == 0){
						grid.setCell(grid.getCell(j, i), j+1,i);
						grid.setCell(0, j, i);
					}
				}
			}
		}
	}
	
	public void combineRight(){
		for(int i = 0; i < grid.getDimensions().y; i++){
			for(int j = grid.getDimensions().x-2; j >= 0; j--){
				if(grid.getCell(j+1,i) == grid.getCell(j,i)){
					grid.setCell(grid.getCell(j, i) * 2, j+1,i);
					grid.setCell(0, j, i);
					score += grid.getCell(j+1, i);
				}
			}
		}
	}
	
	
	
	public void moveLeft(){
		slideLeft();
		combineLeft();
		slideLeft();
		spawnRand();
	}
	
	public void slideLeft(){
		for(int a = 0; a < grid.getDimensions().x-1; a++){
			for(int i = 0; i < grid.getDimensions().y; i++){
				for(int j = 1; j < grid.getDimensions().x; j++){
					if(grid.getCell(j-1,i) == 0){
						grid.setCell(grid.getCell(j, i), j-1,i);
						grid.setCell(0, j, i);
					}
				}
			}
		}
	}
	
	public void combineLeft(){
		for(int i = 0; i < grid.getDimensions().y; i++){
			for(int j = 1; j < grid.getDimensions().x; j++){
				if(grid.getCell(j-1,i) == grid.getCell(j,i)){
					grid.setCell(grid.getCell(j, i) * 2, j-1,i);
					grid.setCell(0, j, i);
					score += grid.getCell(j-1, i);
				}
			}
		}
	}
	
	
	public void spawnRand(){
		boolean success = false;
		
		int x, y, count = 0;
		
		while(!success && count < 20){
			x = Controller.rand.nextInt(grid.getDimensions().x);
			y = Controller.rand.nextInt(grid.getDimensions().y);
			count++;
			
			if(grid.getCell(x, y) == 0){
				success = true;
				grid.setCell(grid.spawnList[Controller.rand.nextInt(grid.spawnList.length)], x, y);
			}
		}
	}

	
	public void render(Graphics g){
		grid.render(g);
		
		Color c = g.getColor();
		g.setColor(Color.GREEN);
		g.drawString("Total Score : " + totalScore + "    Score : " + score + "    Auto Running : " + isAutoRunning + "    Similar : " + similarBoards, 5, Controller.win.HEIGHT - 5);
		g.setColor(c);
	}
	
	// Only to be used with AI stuff, otherwise it'll all break.
	public void startAutoRun(){
		isAutoRunning = true;
		new Thread(this).start();
	}
	
	public void stopAutoRun(){
		isAutoRunning = false;
	}

	public boolean isAutoRunning = false;
	private boolean isRunningRound = false;
	private int similarBoards = 0;
	private int totalScore = 0;
	public static int trials = 5;
	
	@Override
	public void run() {
		totalScore = 0;
		for(int count = 0; count < trials && isAutoRunning; count++){
			int[][] prevGrid;
			similarBoards = 0;
			isRunningRound = true;
			
			grid.clear();
			
			while(Controller.isRunning && isRunningRound && isAutoRunning){
				prevGrid = grid.copyGrid();
				
				String[] outputs = Controller.AI.getOutputs(inputifyGrid(), sessionNumber);
				int highestIndex = 0;
				
				for(int i = 1 ; i < outputs.length; i++) if(Float.parseFloat(outputs[i]) > Float.parseFloat(outputs[highestIndex])) highestIndex = i;
				
				switch(highestIndex){
				case 0:
					moveRight();
					break;
				case 1:
					moveLeft();
					break;
				case 2:
					moveUp();
					break;
				case 3:
					moveDown();
					break;
				}
				
				boolean emptyCell = false;
				for(int i = 0; i < grid.getDimensions().x && !emptyCell; i++){
					for(int j = 0; j < grid.getDimensions().y && !emptyCell; j++){
						if(grid.getCell(i, j) == 0) emptyCell = true;
					}
				}
				
				if(!emptyCell){
					for(int i = 0; i < grid.getDimensions().x && !emptyCell; i++){
						for(int j = 0; j < grid.getDimensions().y && !emptyCell; j++){
							for(int a = -1; a <= 1 && !emptyCell; a++){
								for(int b = -1; b <= 1 && !emptyCell; b++){
									if((a!=0 || b!= 0) && i+a >=0 && i+a < grid.getDimensions().x && j+b > 0 && j+b < grid.getDimensions().y) if(grid.getCell(i+a, j+b) == grid.getCell(i, j)) emptyCell = true;
								}
							}
						}
					}
				}
				
				if(!emptyCell){
					isRunningRound = false;
				}
				
				
				if(grid.equals(prevGrid)){
					similarBoards++;
	
					if(similarBoards >= 10){
						isRunningRound = false;
					}
				}
				else similarBoards = 0;
				
				try {Thread.sleep(Controller.timeBetweenAITurns);} catch (InterruptedException e) {e.printStackTrace();} // Sleep to limit loop and not fry computers.
			}
			totalScore += score;
		}
		((GeneticAlg) Controller.AI.learningAlg).setFitness(sessionNumber, totalScore);
		isAutoRunning = false;
	}
	
	public float[] inputifyGrid(){
		float[] f = new float[ grid.getDimensions().x *  grid.getDimensions().y +1];
		
		for(int i = 0; i < grid.getDimensions().x; i++){
			for(int j = 0; j < grid.getDimensions().y; j++){
				f[i*4 + j] = grid.getCell(i, j);
			}
		}
		
		f[f.length-1] = similarBoards;
		
		return f;
	}
	
}
