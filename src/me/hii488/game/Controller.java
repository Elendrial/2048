package me.hii488.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JOptionPane;

import me.hii488.ArtificialIntelligence;
import me.hii488.BackpropAlg;
import me.hii488.GeneticAlg;
import me.hii488.game.display.Window;

public class Controller implements KeyListener, Runnable{
	
	public static Session[] sessions;
	public static Window win;
	public static int renderedSession = 0;
	
	public static boolean paused = false;
	public static boolean isRunning = false;
	public static long timeBetweenAITurns = 0;
	
	public static Random rand = new Random();
	public static ArtificialIntelligence AI;
	public static BackpropAlg bpAgent;
	
	public static void setup(){
		win = new Window("2048", 600, 600);
		
		sessions = new Session[1];
		sessions[0] = Session.makeNewSession();
		renderedSession = 0;
	}
	
	public static void setupWithAI(){
		win = new Window("2048", 600, 600);
		
		AI = new ArtificialIntelligence();
		
		AI.learningAlg = new GeneticAlg();
		
		((GeneticAlg)AI.learningAlg).genSettings.childrenPerGeneration = 200;
		((GeneticAlg)AI.learningAlg).genSettings.additionalTopChildrenKept = 20;
		((GeneticAlg)AI.learningAlg).genSettings.mutationChance = 0.02f;
		((GeneticAlg)AI.learningAlg).genSettings.mixTop = 30;
		((GeneticAlg)AI.learningAlg).genSettings.insureDifferent = true;
		
		AI.settings.neuralSettings.inputs = 17; // needs to be the grid area +1
		AI.settings.neuralSettings.nodesInHiddenLayers = new int[]{32, 8};
		AI.settings.neuralSettings.outputs = new String[]{"r","l","u","d"};
		AI.settings.neuralSettings.cutoffThreshhold = 0.5f;
		AI.settings.neuralSettings.outputsAsFloats = true;
		
		AI.settings.loggingSettings.printAll = false;
		AI.settings.loggingSettings.printTop = false;
		AI.settings.loggingSettings.topAmount = 10;
		
		Session.trials = 10;
		
		AI.initialSetup();
		
		
		bpAgent = new BackpropAlg(); // No reason to call setup() as that only creates the base child.
		bpAgent.settings =  ((GeneticAlg)AI.learningAlg).settings;
		bpAgent.neuralNet = ((GeneticAlg)AI.learningAlg).neuralNet;
		bpAgent.momentum = 0f;
		bpAgent.learningRate = 0.01f;
		
		
		sessions = new Session[((GeneticAlg)AI.learningAlg).genSettings.childrenPerGeneration + ((GeneticAlg)AI.learningAlg).genSettings.additionalTopChildrenKept];
		for(int i = 0; i < ((GeneticAlg) AI.learningAlg).children.size(); i++){
			sessions[i] = Session.makeNewSession();
		}
		renderedSession = 0;
	}
	
	public static void start(){
		isRunning = true;
		win.start();
	}
	
	public static void stop(){
		isRunning = false;
	}
	
	
	public static void startWithAI(){
		isRunning = true;
		win.start();
		
		for(int i = 0; i < Session.amountOfSessions; i++){
			sessions[i].startAutoRun();
		}
		
		new Thread(new Controller()).start();
	}
	
	public static void render(Graphics g) {
		sessions[renderedSession].render(g);
		if(Session.amountOfSessions > 2){
			Color c = g.getColor();
			g.setColor(Color.red);
			g.drawString("Session #" + renderedSession + "    Generation #" + ((GeneticAlg) AI.learningAlg).generation, 5, 10);
			g.setColor(c);
		}
	}

	
	
	@Override
	public void keyPressed(KeyEvent arg0) {
		switch(arg0.getKeyCode()){
		case KeyEvent.VK_RIGHT:
			sessions[renderedSession].moveRight();
			break;
		case KeyEvent.VK_LEFT:
			sessions[renderedSession].moveLeft();
			break;
		case KeyEvent.VK_UP:
			sessions[renderedSession].moveUp();
			break;
		case KeyEvent.VK_DOWN:
			sessions[renderedSession].moveDown();
			break;
		case KeyEvent.VK_N:
			if(renderedSession < Session.amountOfSessions-1) renderedSession++;
			break;
		case KeyEvent.VK_B:
			if(renderedSession > 0) renderedSession--;
			break;
		case KeyEvent.VK_F:
			if(timeBetweenAITurns >= 1)timeBetweenAITurns*=10;
			else timeBetweenAITurns = 1;
			break;
		case KeyEvent.VK_S:
			if(timeBetweenAITurns > 1)timeBetweenAITurns/=10;
			else timeBetweenAITurns = 0;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}

	public static boolean outputToExcel = false;
	public static String runData = "";
	public static int gensBetweenBP = 5;
	public static int bpGenLowerLimit = 100;
	
	@Override
	public void run() {
		while (isRunning) {
			boolean nextGen = true;
			for (int j = 0; j < Session.amountOfSessions && nextGen; j++) {
				if (sessions[j].isAutoRunning)
					nextGen = false;
			}

			if (nextGen && ((GeneticAlg) AI.learningAlg).generation != 1001) {
				runData += ((GeneticAlg) AI.learningAlg).getGenerationInfoAsString();
				AI.iterate();

				while (Session.amountOfSessions < ((GeneticAlg) AI.learningAlg).children.size()) {
					sessions[Session.amountOfSessions] = Session.makeNewSession();
				}

				for (int j = 0; j < Session.amountOfSessions; j++) {
					sessions[j].startAutoRun();
				}
				
			} else if (((GeneticAlg) AI.learningAlg).generation == 1001) {
				JOptionPane.showConfirmDialog(null, "Run Finished");
				AI.settings.printSettings(true, true, false);
				System.out.println("Trials: " + Session.trials);
				
				isRunning = false;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	
}
