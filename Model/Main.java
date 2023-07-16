//package SRS2023.Model;

package Model;

//import SRS2023.Model.Agent;
import Model.Agent;

import java.util.Arrays;
import java.util.Vector;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataRecorder;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.space.Object2DGrid;




public class Main extends SimModelImpl {

	private ArrayList<Agent> agentList;
	// private ArrayList agentList = new ArrayList<Agent>();
	private Schedule schedule;
	private Object2DGrid Grid;
	private int gridWidth;
	private int gridHeight;
	private int numAgents;
	private double minorityShares;
	private int numEthnic;
	private int numNative;
	private int period;
	private double occupancy;
	private double unemployment;
	private double r; //interest rate
	private double B; //outside option
	private double pE; //price of ethnic good
	
	private int betaNE;
	private int betaEE;
	private int lambdaO;
	private double gammaN;
	private double gammaE;
	private double gammaEA;
	private double theta;

	//private int alpha;


    private DataRecorder data1;

    @Override
    public void begin() {
        // TODO Auto-generated method stub

    }

    @Override
	public String[] getInitParam() {
		return new String[]{"gridWidth", "gridHeight", "period", "occupancy"};
	}

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Entrepreneurship Model";
    }
    
    @Override
	public Schedule getSchedule() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setup() {
		// TODO Auto-generated method stub
		agentList = new ArrayList<Agent>();
		period = 1000;
		gridWidth = 50;
		gridHeight = 50;
		occupancy = 0.8;
		minorityShares = 0.3;
		numAgents = (int) (gridWidth * gridHeight * occupancy);
		numEthnic = (int)(numAgents * minorityShares);
		numNative = numAgents - numEthnic;
		schedule = new Schedule(1);
	}
	
	public void buildModel() {
		
		Grid = new Object2DGrid(gridWidth, gridHeight);
		
		//randomly allocate the minority
		for (int id = 0; id < numEthnic; id ++) {
			Agent ag = new Agent();
			ag.setID(id);
			//Race 1 refers to native and 2 refers to ethnic
			ag.setRace(2);
			
			double x_i =  Math.random();//Entrepreneurial spirit/ability
			double c_i = Math.random(); //Cost of assimilation
			double p_i = Math.random(); //Productivity as a worker
			double b_i = Math.random(); //Wealth used to be starting a business
			
			//ethnic minorities will be located in the lower third of the lattice.
			int min = (int) (gridHeight*(1.0/3.0)); 
			int max = gridHeight; 
			int randomY = (int) (Math.random() * (max - min + 1)) + min;
			int randomX = (int) (Math.random() * gridWidth); 
			
			while (Grid.getObjectAt(randomX, randomY) != null) {
				randomY = (int) (Math.random() * (max - min + 1)) + min;
				randomX = (int) (Math.random() * gridWidth); 
			}
			
			Grid.putObjectAt(randomX, randomY, ag);
			
			//Allocate agents uniform randomly to initial occupations, all workers will unemployed and entrepreneurs will have no employees yet.
			double random = Math.random();
			//String occupation;
			//Occupation 1 is "Entrepreneur", 2 is "Work in Native Firm", 3 is "Work in  Ethnic Firm", 4 is "unemployed"
			//gonna change this one after max change the agent class
			if (random < 1.0/3) {
				ag.setcurOccupation(1);
				
			}
			else if(1.0/3 <= random && random < 2.0/3){
				ag.setcurOccupation(4);
				ag.setswitchOccupation(2);
				
			}
			else {
				ag.setcurOccupation(4);
				ag.setswitchOccupation(3);
				
			}
	
			
			ag.setCoords(new int[] {randomX, randomY});
			ag.setXI(x_i);
			ag.setCI(c_i);
			ag.setPI(p_i);
			ag.setBI(b_i);
			agentList.add(ag);
			
			
			
			
		}
		
		
		//randomly allocate the native
		for (int id = numEthnic; id < numNative; id++) {
			Agent ag = new Agent();
			ag.setID(id);
			//Race 1 refers to majority
			ag.setRace(1);
			
			double x_i =  Math.random();//Entrepreneurial spirit/ability
			double c_i = Math.random(); //Cost of assimilation
			double p_i = Math.random(); //Productivity as a worker
			double b_i = Math.random(); //Wealth used to be starting a business
			int randomX = (int) (Math.random() * gridWidth); // random X
			int randomY = (int) (Math.random() * gridHeight); // random Y
			while (Grid.getObjectAt(randomX, randomY) != null) {
				randomX = (int) (Math.random() * gridWidth);
				randomY = (int) (Math.random() * gridHeight);
			}
			
			Grid.putObjectAt(randomX, randomY, ag);
			
			
			//Allocate agents uniform randomly to initial occupations, all workers will unemployed and entrepreneurs will have no employees yet.
			double random = Math.random();
			//String occupation;
			if (random < 1.0/2) {
				ag.setcurOccupation(1);
				
			}
			else {
				ag.setcurOccupation(4);
				ag.setswitchOccupation(2);
			}
			
			ag.setCoords(new int[] {randomX, randomY});
			ag.setXI(x_i);
			ag.setCI(c_i);
			ag.setPI(p_i);
			ag.setBI(b_i);
			agentList.add(ag);
			
		}
	}
	
	
	public int considerOccupation(Agent agent){
		
		double u1 = computeEntrepreneurUtility(computeEntrepreneurPayoff(agent), pE, agent);
		double u2 = computeNativeWorkerUtility(computeNativeWorkerPayoff(agent), pE, agent);
		double u3;
		if (agent.getRace()==1){
			u3 = 0;
		}
		else {
			u3 = computeEthnicWorkerUtility(computeEthnicWorkerPayoff(agent), pE, agent);
		}
		
		double u4 = computeUnemployedUtility(computeUnemployedPayoff(agent), pE, agent);
		
		
		//missing a condition: utility is the same
		double[] numbers = {u1, u2, u3, u4};
		double max = Arrays.stream(numbers).max().getAsDouble();
		if (max == u1) {
			return 1;
		}
		else if (max == u2) {
			return 2;
		}
		else if (max == u3) {
			return 3;
		}
		else {
			return 4;
		}
		
	}
	
	public double computeEntrepreneurPayoff(Agent agent) {
		
		return 0.0;
		
	}
	
	public double computeNativeWorkerPayoff(Agent agent) {
		double wage = 0;
		return 0.0;
	}
	
	
	public double computeEthnicWorkerPayoff(Agent agent) {
		double wage = betaEE*B + (1-betaEE)*agent.getPI();
		double payoff = (1-unemployment) * wage + unemployment*B + r*agent.getBI();
		return payoff;
		
	}
	public double computeUnemployedPayoff(Agent agent) {
		double payoff = B + r*agent.getBI();
		return payoff;
		
	}
	
	public double computeEntrepreneurUtility(double budget, double pE, Agent agent) {
		//two conditions: native individuals and ethnic individuals
		double u; 
		if (agent.getRace()==1) {
			int cNE; 
			int cNG; 
			
			cNE = (int)((budget * gammaN)/pE);
			cNG = (int)((1-gammaN)*budget);
			u = Math.pow(cNE, gammaN) * Math.pow(cNG, 1-gammaN);
			
		}
		else {
			int cEE; 
			int cEG; 
			
			cEE = (int)((budget * gammaE)/pE);
			cEG = (int)((1-gammaE)*budget);
			u = Math.pow(cEE, gammaE) * Math.pow(cEG, 1-gammaE);
			
			return u;
			
		}
		
		
		return u;
		
	}
	public double computeNativeWorkerUtility(double budget, double pE, Agent agent) {
		
		//two conditions: native individuals and ethnic individuals
		
		double u; 
		
		if (agent.getRace()==1) {
			int cNE; 
			int cNG; 
			
			cNE = (int)((budget * gammaN)/pE);
			cNG = (int)((1-gammaN)*budget);
			u = Math.pow(cNE, gammaN) * Math.pow(cNG, 1-gammaN);
			
		}
		else {
			int cEE; 
			int cEG; 
			double x; 
			cEE = (int)((budget * gammaEA)/pE);
			cEG = (int)((1-gammaEA)*budget);
			
			Vector<Agent> neighborlist;
			neighborlist = Grid.getMooreNeighbors(agent.getCoords()[0], agent.getCoords()[1], false);
			double assimilation = 0;
			for (int i = 0; i < neighborlist.size(); i++) {

				if (neighborlist.get(i).getswitchOccupation() == 2 && neighborlist.get(i).getRace() == 2) {
					assimilation += 1;
				}
		
			}
			x = assimilation/neighborlist.size();
			u = Math.pow(cEE, gammaEA) * Math.pow(cEG, 1-gammaEA)+ theta * x;
			
		}
	
		return u;
		
	}
	
	public double computeEthnicWorkerUtility(double budget, double pE, Agent agent) {
		//Only ethnic individuals can become workers in ethnic firms
		int cEE; 
		int cEG; 
		double u; 
		
		cEE = (int)((budget * gammaE)/pE);
		cEG = (int)((1-gammaE)*budget);
		u = Math.pow(cEE, gammaE) * Math.pow(cEG, 1-gammaE);
		
		return u;
		
	}
	
	public double computeUnemployedUtility(double budget, double pE, Agent agent) {
		double u; 
		
		//two conditions: native individuals and ethnic individuals
		
		if (agent.getRace()==1) {
			int cNE; 
			int cNG; 
			
			cNE = (int)((budget * gammaN)/pE);
			cNG = (int)((1-gammaN)*budget);
			u = Math.pow(cNE, gammaN) * Math.pow(cNG, 1-gammaN);
			
		}
		else {
			int cEE; 
			int cEG; 
			
			cEE = (int)((budget * gammaE)/pE);
			cEG = (int)((1-gammaE)*budget);
			u = Math.pow(cEE, gammaE) * Math.pow(cEG, 1-gammaE);
		}
			
		return u;
		
	}
	
	
	//in the excute() method, only agent with switchOccupation() == 2 or 3 will go through the method jobSearch.
	//Occupation 2 is native firm work; 3 is ethnic firm worker.
	
	public Agent jobSearch (Agent agent) {
		
		if (agent.getswitchOccupation() == 2) {
			return nativeJobsearch (agent);
		}
		else {
			return ethnicJobsearch (agent);
		}
		
	}
	
	public Agent nativeJobsearch (Agent agent) {
		
		ArrayList<Agent> firms = new ArrayList<Agent>();
		
		for (int i = 0;i< numAgents;i++) {
			if (agentList.get(i).getRace() ==1 && agentList.get(i).getswitchOccupation()==1) {
				firms.add(agentList.get(i));
			}
		}
		
		int random = (int) (Math.random() * firms.size()); 
		return firms.get(random);
		
	}
	
	
	public Agent ethnicJobsearch (Agent agent) {
		ArrayList<Agent> firms = new ArrayList<Agent>();
		int x = agent.getCoords()[0]; 
		int y = agent.getCoords()[0]; 

		// 获取目标格子周围两圈的邻居格子
		int radius = 2; 
		
		
		//make sure the coordinates are inside the boundaries of the grid.
		int minX = Math.max(x - radius, 0);
		int minY = Math.max(y - radius, 0);
		int maxX = Math.min(x + radius, Grid.getSizeX() - 1);
		int maxY = Math.min(y + radius, Grid.getSizeY() - 1);

		for (int i = minX; i <= maxX; i++) {
		    for (int j = minY; j <= maxY; j++) {
		        if (i == x && j == y) {
		            continue; // exclude the agent itself
		        }
		        if (agentList.get(i).getRace() == 2 && agentList.get(i).getswitchOccupation()== 1) {
					firms.add(agentList.get(i));
				}
		    }
		}
		int random = (int) (Math.random() * firms.size()); 
		return firms.get(random);
	}
		


	
	//we are missing the process of boss to decide who they gonna hire for step2.
	
	//Step3:calculate the new unemployment rate
	
	public double calcUnemployed () {
		double num = 0;
		for (int i = 0;i<numAgents;i++) {
			if (agentList.get(i).getcurtOccupation()==4) {
				num+=1;
			};
			
		}
		return num/numAgents;
	}
	
	
	
	

	public int getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(int n) {
		this.gridHeight = n;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(int n) {
		this.gridWidth = n;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public int getNumAgents() {
		return numAgents;
	}

	public void setNumAgents(int numAgents) {
		this.numAgents = numAgents;
	}

	public double getOccupancy(){
		return occupancy;
	}

	public void setOccupancy(double o){
		this.occupancy = o;
	}
   
}
