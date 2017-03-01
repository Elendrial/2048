package me.hii488.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import me.hii488.ArtificialIntelligence;
import me.hii488.BackpropAlg;
import me.hii488.GeneticAlgB;
import me.hii488.NeuralNetwork.Child;
import me.hii488.game.display.Window;
import me.hii488.other.Data;
import me.hii488.other.ExcelHandler;
import me.leliel.stopwatch.Stopwatch;

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
	public static GeneticAlgB genAlg = new GeneticAlgB();
	
	public static Stopwatch stopwatch = new Stopwatch();
	
	public static void setup(){
		win = new Window("2048", 600, 600);
		
		sessions = new Session[1];
		sessions[0] = Session.makeNewSession();
		sessions[0].spawnRand();
		renderedSession = 0;
	}
	
	public static void setupWithAI(){
		win = new Window("2048", 600, 600);
		
		AI = new ArtificialIntelligence();
		
		AI.learningAlg = genAlg;
		
		genAlg.genSettings.childrenPerGeneration = 200;
		genAlg.genSettings.additionalTopChildrenKept = 20;
		genAlg.genSettings.mutationChance = 0.02f;
		genAlg.genSettings.mixTop = 30;
		genAlg.genSettings.insureDifferent = true;
		
		genAlg.genSettingsB.additionalRandKept = 20;
		genAlg.genSettingsB.lowestXPotentiallyKept = 80;
		genAlg.genSettingsB.additionalToMix = 20;
		
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
		
		
		bpAgent = new BackpropAlg();
		bpAgent.settings = genAlg.settings;
		bpAgent.neuralNet = genAlg.neuralNet;
		bpAgent.momentum = 0f;
		bpAgent.learningRate = 0.01f;
		bpAgent.setup();
		
		gensBetweenBP = 5;
		amountToBP = 20;
		bpGenLowerLimit = 100;
		
		gens = 1000;
		runChangeRate = 5;
		
		outputToExcel = true;
		
		sessions = new Session[genAlg.genSettings.childrenPerGeneration + genAlg.genSettings.additionalTopChildrenKept + amountToBP + genAlg.genSettingsB.additionalRandKept];
		for(int i = 0; i < genAlg.children.size(); i++){
			sessions[i] = Session.makeNewSession();
		}
		renderedSession = 0;
		
		if(outputToExcel){
			ExcelHandler.setup("C:\\Users\\Hii\\Documents\\_Documents\\Programming\\2048ExcelTesting.xlsx");
		}
		
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
		
		new Thread(new Controller()).start();
	}
	
	public static void render(Graphics g) {
		try{
			sessions[renderedSession].render(g);
			if(Session.amountOfSessions > 2){
				Color c = g.getColor();
				g.setColor(Color.red);
				g.drawString("Session #" + renderedSession + "    Generation #" + genAlg.generation + "    Run # " + run, 5, 10);
				g.setColor(c);
			}
		}
		catch(Exception e){e.printStackTrace();}
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
	public static int gensBetweenBP;
	public static int bpGenLowerLimit;
	public static int amountToBP;
	public static int gens;
	public static int run;
	public static int runChangeRate;
	
	@Override
	public void run() {
		ArrayList<Child> additionalChildren = new ArrayList<Child>();
		boolean nextGen, runInProgress;
		while (isRunning) {
			runInProgress = true;
			stopwatch.start();
			for(int i = 0; i < Session.amountOfSessions; i++){
				sessions[i].startAutoRun();
				sessions[i].spawnRand();
			}
			
			while(runInProgress){
				nextGen = true;
				for (int i = 0; i < Session.amountOfSessions && nextGen; i++) {
					if (sessions[i].isAutoRunning)
						nextGen = false;
				}
	
				if (nextGen && genAlg.generation < gens) {
					additionalChildren = new ArrayList<Child>();
					if((genAlg.generation - bpGenLowerLimit) % gensBetweenBP == 0 && genAlg.generation >= bpGenLowerLimit){
						System.out.print("BP Run...");
						genAlg.sortedChildren = genAlg.fitnessSortedChildren(genAlg.children);
						int highestIndex = 0;
						float[] outputs;
						boolean containsHighest;
						for(int i = 0; i < amountToBP; i++){
							bpAgent.c = genAlg.sortedChildren.get(i).clone();
							
							for(int j = 0; j < bpData.length; j++){
								outputs = bpAgent.getOutputs(bpData[j].input, null);
								highestIndex = 0;
								containsHighest = false;
								
								for(int k = 1 ; k < outputs.length; k++) if(outputs[k] > outputs[highestIndex]) highestIndex = k;
								for(int k = 0; k < bpData[j].expectedOutputs.length; k++) if(bpData[j].expectedOutputs[k] == highestIndex) containsHighest = true;
								
								if(!containsHighest){
									bpAgent.updateNodes(bpData[j].trainingOutputs);
								}
							}
	
							additionalChildren.add(bpAgent.c.clone());
						}
					}
					
					if(outputToExcel){
						ExcelHandler.writeGeneration(genAlg.generation, genAlg.getGenerationInfoAsString(), run);
					}
					
					AI.iterate();
	
					for(Child c : additionalChildren){
						genAlg.children.add(c);
					}
					
					while (Session.amountOfSessions < genAlg.children.size()) {
						sessions[Session.amountOfSessions] = Session.makeNewSession();
					}
	
					for (int j = 0; j < genAlg.children.size(); j++) {
						sessions[j].startAutoRun();
					}

					
				} else if (genAlg.generation == gens) {
					stopwatch.stop();
					AI.settings.printSettings(true, true, false);
					System.out.println("Trials: " + Session.trials);
					System.out.println("Gens Between BP: " + gensBetweenBP);
					System.out.println("BP start Gen: " + bpGenLowerLimit);
					System.out.println("Time taken for run: " + stopwatch.totalTime);

					ExcelHandler.writeGenInfo(run, AI.settings.settingsAsString(true, true, false) + "\n:" + Session.trials + "\n:" + gensBetweenBP + "\n:" + bpGenLowerLimit + "\n:"+ stopwatch.totalTime);
					
					runInProgress = false;
				}
	
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			run++;
			
			if(run%runChangeRate == 0) updateVariables();
			
			sessions = new Session[genAlg.genSettings.childrenPerGeneration + genAlg.genSettings.additionalTopChildrenKept + amountToBP + genAlg.genSettingsB.additionalRandKept];
			Session.amountOfSessions = 0;
			for(int i = 0; i < genAlg.genSettings.childrenPerGeneration; i++){
				sessions[i] = Session.makeNewSession();
			}
			
			genAlg.makeRandomGeneration();
			bpAgent.setup();
			
			bpAgent.settings = genAlg.settings;
			bpAgent.neuralNet = genAlg.neuralNet;
		}
	}
	
	public static float percentChange = 0.05f;
	
	public static void updateVariables(){
		switch(rand.nextInt(12)){
		case 0:
			genAlg.genSettings.childrenPerGeneration += (Math.ceil(genAlg.genSettings.childrenPerGeneration * percentChange) * (rand.nextBoolean() && genAlg.genSettings.childrenPerGeneration > 50 ? -1 : 1));
			break;
		case 1:
			genAlg.genSettings.additionalTopChildrenKept += (Math.ceil(genAlg.genSettings.additionalTopChildrenKept * percentChange) * (rand.nextBoolean() && genAlg.genSettings.additionalTopChildrenKept > 0 ? -1 : 1));
			break;
		case 2:
			genAlg.genSettings.mutationChance += (genAlg.genSettings.mutationChance * percentChange * (rand.nextBoolean() && genAlg.genSettings.mutationChance > 0.0000001 ? -1 : 1));
			break;
		case 3:
			genAlg.genSettings.mixTop += (Math.ceil(genAlg.genSettings.mixTop * percentChange) * (rand.nextBoolean() && genAlg.genSettings.mixTop > 0 ? -1 : 1));
			break;
		case 4:
			bpGenLowerLimit += (Math.ceil(bpGenLowerLimit * percentChange) * (rand.nextBoolean() && bpGenLowerLimit > 1? -1 : 1));
			break;
		case 5:
			AI.settings.neuralSettings.cutoffThreshhold += (AI.settings.neuralSettings.cutoffThreshhold * percentChange * (rand.nextBoolean() && AI.settings.neuralSettings.cutoffThreshhold > 0.01 ? -1 : 1));
			break;
		case 6:
			bpAgent.learningRate += (bpAgent.learningRate * percentChange * (rand.nextBoolean() && bpAgent.learningRate > 0 ? -1 : 1));
			break;
		case 7:
			gensBetweenBP += (1 * (rand.nextBoolean() && gensBetweenBP > 1 ? -1 : 1));
			break;
		case 8:
			amountToBP += (Math.ceil(amountToBP * percentChange) * (rand.nextBoolean() && amountToBP > 1? -1 : 1));
			break;
		case 9:
			genAlg.genSettingsB.additionalRandKept += (Math.ceil(genAlg.genSettingsB.additionalRandKept * percentChange) * (rand.nextBoolean() && genAlg.genSettingsB.additionalRandKept > 0 ? -1 : 1));
			break;
		case 10:
			genAlg.genSettingsB.lowestXPotentiallyKept += (Math.ceil(genAlg.genSettingsB.lowestXPotentiallyKept * percentChange) * (rand.nextBoolean() && genAlg.genSettingsB.lowestXPotentiallyKept > genAlg.genSettingsB.additionalRandKept ? -1 : 1));
			break;
		case 11:
			genAlg.genSettingsB.additionalToMix += (Math.ceil(genAlg.genSettingsB.additionalToMix * percentChange) * (rand.nextBoolean() && genAlg.genSettingsB.additionalToMix > 0 ? -1 : 1));
			break;
		}
		
	}
	
	public static Data[] bpData = { // 0 : R, 		1 : L, 		2 : U, 		3 : D
			new Data(new float[]{0,0,4,256,64,0,0,64,2,0,8,4,0,2,4,4,0}, new float[]{0}, new float[]{1,0,0.5f,0,0}),
			new Data(new float[]{2,0,4,512,64,0,0,64,2,0,8,4,0,2,4,4,0}, new float[]{0,3}, new float[]{0.5f,0,0,1}),
			new Data(new float[]{64,128,512,1024,8,2,2,2,4,4,0,0,0,2,2,0,0}, new float[]{0,1}, new float[]{0.5f,1,0,0}),
			new Data(new float[]{0,0,0,4,0,0,2,4,0,4,2,8,0,2,4,16,0}, new float[]{3}, new float[]{0,0,0,1}),
			new Data(new float[]{2,0,0,0,4,0,2,0,8,4,2,0,16,4,2,0,0}, new float[]{3,1}, new float[]{0,0.2f,0,1}),
			new Data(new float[]{0,0,2,0,2,0,0,0,16,2,0,0,16,8,8,0,0}, new float[]{}, new float[]{0,0,0,0}),
			new Data(new float[]{4,2,8,16,2,0,2,16,0,0,4,0,0,0,0,0,0}, new float[]{0,2}, new float[]{0.7f,0,1,0}),
			new Data(new float[]{4,0,0,32,0,0,2,16,0,0,16,4,0,0,4,4,0}, new float[]{2}, new float[]{0,1,0,0}),
			new Data(new float[]{2,2,16,64,0,8,2,16,0,2,0,16,0,0,0,0,0}, new float[]{0,2}, new float[]{0.7f,1,0,0}),
			new Data(new float[]{32,8,4,2,8,4,2,0,0,2,0,0,0,0,2,0,0}, new float[]{0}, new float[]{1,0,0.1f,0}),
			new Data(new float[]{64,0,0,0,8,8,0,0,4,4,2,0,2,2,2,0,0}, new float[]{1}, new float[]{0.1f,1,0,0}),
			new Data(new float[]{64,4,2,0,16,0,0,0,8,4,0,0,4,0,2,0,0}, new float[]{3}, new float[]{0,0,0.5f,1}),
			new Data(new float[]{0,0,4,16,0,0,4,4,0,0,0,4,4,0,0,0,0}, new float[]{0}, new float[]{1,0,0,0}),
			new Data(new float[]{2,16,32,64,0,0,16,4,0,0,0,4,0,0,0,2,0}, new float[]{2}, new float[]{0,0.1f,1,0}),
			new Data(new float[]{0,2,0,128,0,0,2,16,0,0,0,8,2,8,2,8,0}, new float[]{2}, new float[]{0.4f,0,1,0}),
			new Data(new float[]{4,2,2,128,2,4,8,64,0,2,2,8,0,0,0,2,0}, new float[]{0,3}, new float[]{1,0,0,0.9f}),
			new Data(new float[]{128,0,0,0,64,8,2,0,16,8,0,0,8,4,0,4,0}, new float[]{2,3}, new float[]{0,0,0.7f,1}),
			new Data(new float[]{8,32,256,2,8,4,16,2,2,0,0,0,0,0,0,0,0}, new float[]{2}, new float[]{0,0,1,0.2f}),
			new Data(new float[]{4,16,8,512,4,16,8,0,4,2,0,0,0,0,0,2,0}, new float[]{23}, new float[]{0.1f,0,1,0.5f}),
			new Data(new float[]{8,32,16,512,0,2,16,2,0,2,0,4,0,0,0,4,0}, new float[]{2,3}, new float[]{0,0,1,0.5f}),
			new Data(new float[]{8,32,32,512,0,4,0,2,0,2,0,8,0,0,0,0,0}, new float[]{0}, new float[]{1,0.2f,0,0}),
			new Data(new float[]{16,32,64,512,8,4,4,0,0,0,0,0,2,0,0,0,0}, new float[]{1}, new float[]{0.5f,1,0,0}),
			new Data(new float[]{16,16,128,512,4,0,2,2,0,0,0,0,0,0,0,0,0}, new float[]{0}, new float[]{1,0.5f,0,0}),
			new Data(new float[]{8,64,128,512,8,8,4,0,0,0,2,0,0,2,0,0,0}, new float[]{0}, new float[]{1,0.5f,0,0}),
			new Data(new float[]{2,2,0,0,4,2,0,0,8,8,0,0,16,4,0,0,0}, new float[]{1}, new float[]{0.4f,1,0,0}),
			new Data(new float[]{2,0,0,0,4,0,0,0,8,4,2,0,32,8,4,2,0}, new float[]{0}, new float[]{1,0,0,0}),
			new Data(new float[]{2,2,0,0,0,0,0,0,16,4,2,0,64,16,4,2,0}, new float[]{}, new float[]{0,0,0,0}),
	//		new Data(new float[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, new float[]{}, new float[]{0,0,0,0}),
	};
	
}
