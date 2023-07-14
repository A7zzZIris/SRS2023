package Model;

import java.util.Arrays;
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
	private int ethnicNumber;
	private int nativeNumber;
	private int period;
	private double occupancy;
	private double unemployment;
	private double r; //interest rate
	private double B; //outside option
	private double pE; //price of ethnic good
	
	private int betaNE;
	private int betaEE;
	private int lambdaO;
	private int lambdaF;
	
	
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
		ethnicNumber = (int)(numAgents * minorityShares);
		nativeNumber = numAgents - ethnicNumber;
		schedule = new Schedule(1);
	}
	
	public void buildModel() {
		
		Grid = new Object2DGrid(gridWidth, gridHeight);
		
		//randomly allocate the minority
		for (int id = 0; id < ethnicNumber; id ++) {
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
				ag.setcurOccupation("Entrepreneur");
				
			}
			else if(1.0/3 <= random && random < 2.0/3){
				ag.setcurOccupation("Unemployed");
				ag.setswitchOccupation("Work in Native Firm");
				
			}
			else {
				ag.setcurOccupation("Unemployed");
				ag.setswitchOccupation("Work in  Ethnic Firm");
				
			}
	
			
			ag.setCoordinate(new int[] {randomX, randomY});
			ag.setEntrepreneurialAbility(x_i);
			ag.setCostofAssimulation(c_i);
			ag.setProductivity(p_i);
			ag.setWealthToBusiness(b_i);
			agentList.add(ag);
			
			
			
			
		}
		
		
		//randomly allocate the native
		for (int id = ethnicNumber; id < nativeNumber; id++) {
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
				ag.setcurOccupation("Entrepreneur");
				
			}
			else {
				ag.setcurOccupation("Unemployed");
				ag.setswitchOccupation("Work in  Native Firm");
			}
			
			ag.setCoordinate(new int[] {randomX, randomY});
			ag.setEntrepreneurialAbility(x_i);
			ag.setCostofAssimulation(c_i);
			ag.setProductivity(p_i);
			ag.setWealthToBusiness(b_i);
			agentList.add(ag);
			
		}
	}
	
	
	public int considerOccupation(Agent agent){
		
		double payoff1 = computeEntrepreneurUtility(computeEntrepreneurPayoff(agent), pE);
		double payoff2 = computeNativeworkerUtility(computeNativeworkerPayoff(agent), pE);
		double payoff3 = computeEthnicworkerUtility(computeEthnicworkerPayoff(agent), pE);
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
	
	public double computeNativeworkerPayoff(Agent agent) {
		return 0.0;
		
	}
	
	
	public double computeEthnicworkerPayoff(Agent agent) {
		
		double wage = betaEE*B + (1-betaEE)*p;
		double payoff = (1-unemployment) * wage + unemployment*B + r*agent.getbI();
		return payoff;
		
	}
	public double computeUnemployedPayoff(Agent agent) {
		double payoff = B + r*agent.getbI();
		return payoff;
		
	}
	
	public double computeEntrepreneurUtility(double budget, double pE) {
		
		return 0.0;
		
	}
	public double computeNativeworkerUtility(double budget, double pE) {
		return 0.0;
		
	}
	
	public double computeEthnicworkerUtility(double budget, double pE) {
		return 0.0;
		
	}
	public double computeUnemployedUtility(double budget, double pE) {
		return 0.0;
		
	}
	
	
	public Agent jobSearch (Agent agent) {
		//whether we need to add the potential boss inside the Agent class?
		//if native
		//return nativeJobsearch (Agent agent);
		//if ethnic 
		//return ethnicJobsearch (Agent agent)
		
	}
	
	public Agent nativeJobsearch (Agent agent) {
		//whether we need to add the potential boss inside the Agent class?
		//
		
		return boss;
		
	}
	public Agent ethnicJobsearch (Agent agent) {
		//whether we need to add the potential boss inside the Agent class?
		//
		
		return boss;
	}
	
	public double calUnemployed () {
		return 0.0;
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
