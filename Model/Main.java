package SRS2023.Model;

import java.util.Arrays;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataRecorder;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.space.Object2DGrid;
import SRS2023.Model.Agent;

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
		
		double payoff1 = computeEntrepreneurUtility(computeEntrepreneurPayoff(agent), pE);
		double payoff2 = computeNativeWorkerUtility(computeNativeWorkerPayoff(agent), pE);
		double payoff3 = computeEthnicWorkerUtility(computeEthnicWorkerPayoff(agent), pE);
		double payoff4 = computeUnemployedUtility(computeUnemployedPayoff(agent), pE);
		
		
		//missing a condition: payoff is the same
		double[] numbers = {payoff1, payoff2, payoff3, payoff4};
		double max = Arrays.stream(numbers).max().getAsDouble();
		if (max == payoff1) {
			return 1;
		}
		else if (max == payoff2) {
			return 2;
		}
		else if (max == payoff3) {
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
		double wage = betaEE*B + (1-betaEE)*pE;
		double payoff = (1-unemployment) * wage + unemployment*B + r*agent.getBI();
		return payoff;
		
	}
	public double computeUnemployedPayoff(Agent agent) {
		double payoff = B + r*agent.getBI();
		return payoff;
		
	}
	
	public double computeEntrepreneurUtility(double budget, double pE) {
		
		return 0.0;
		
	}
	public double computeNativeWorkerUtility(double budget, double pE) {
		return 0.0;
		
	}
	
	public double computeEthnicWorkerUtility(double budget, double pE) {
		return 0.0;
		
	}
	public double computeUnemployedUtility(double budget, double pE) {
		return 0.0;
		
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
			if (agentList.get(i).getRace() ==1 && agentList.get(i).getcurtOccupation()==1) {
				firms.add(agentList.get(i));
			}
		}
		
		int random = (int) (Math.random() * firms.size()); 
		return firms.get(random);
		
	}
	public Agent ethnicJobsearch (Agent agent) {
		
	Agent boss = new Agent();
		return boss;
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
