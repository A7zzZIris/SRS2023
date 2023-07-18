package SRS2023.Model;
//package Model;

import SRS2023.Model.Agent;
//import Model.Agent;

import java.util.Arrays;
import java.util.Vector;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataRecorder;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplayConstants;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.space.Object2DGrid;

public class Main extends SimModelImpl {

	private ArrayList<Agent> agentList;
	// private ArrayList agentList = new ArrayList<Agent>();
	private Schedule schedule;
	private Object2DGrid Grid;
	private DisplaySurface dsurf;
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

	private int betaNE; //bargaining power
	private int betaEE; //bargaining power
	private int lambdaO;
	private double gammaN;
	private double gammaE;
	private double gammaEA;
	private double theta;

	//private int alpha;


    private DataRecorder data1;

    @Override
    public void begin() {
        buildModel();
		buildSchedule();
		buildDisplay();
    }

    @Override
	public String[] getInitParam() {return new String[]{"gridWidth", "gridHeight", "period", "occupancy"};}

    @Override
    public String getName() {return "Entrepreneurship Model";}

    @Override
	public Schedule getSchedule() {
		return schedule;
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
		dsurf = new DisplaySurface(this, "test");
		registerDisplaySurface("test", dsurf);
		DisplayConstants.CELL_WIDTH = 50;
		DisplayConstants.CELL_HEIGHT = 50;
	}

	public void buildSchedule() {
		schedule.scheduleActionBeginning(1.0, new eachPeriod());
		schedule.scheduleActionAt((double) period, new finalPeriod());
	}

	class eachPeriod extends BasicAction {
		public void execute() {
			calcUnemployment();
			//move();
			// record every round agents' average ethnic percentage for the neighborhood
			//data1.record();
			//data1.write();

		}
	}

	class finalPeriod extends BasicAction {
		public void execute() {
			stop();
		}
	}

	public void buildDisplay() {
		System.out.println(Grid.getSizeX());
		System.out.println(Grid.getSizeY());
		Object2DDisplay agentDisplay = new Object2DDisplay(Grid);
		agentDisplay.setObjectList(agentList);

		dsurf.addDisplayableProbeable(agentDisplay, "Agents");
		addSimEventListener(dsurf);
		dsurf.display();
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
			int randomY = (int) (Math.random() * (max - min)) + min;
			int randomX = (int) (Math.random() * gridWidth);
			System.out.println("here " + String.valueOf(randomX) + " " + String.valueOf(randomY) );


			while (Grid.getObjectAt(randomX, randomY) != null) {
				randomY = (int) (Math.random() * (max - min)) + min;
				randomX = (int) (Math.random() * gridWidth);
			}
			System.out.println(String.valueOf(randomX) + " " + String.valueOf(randomY) );
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
				ag.setSwitchOccupation(2);
			}
			else {
				ag.setcurOccupation(4);
				ag.setSwitchOccupation(3);
			}

			ag.setCoords(new int[] {randomX, randomY});
			//System.out.println(ag.getCoords()[0]);
			//System.out.println(ag.getCoords()[1]);
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
				ag.setSwitchOccupation(2);
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
		double u2 = computeWorkinNativeUtility(computeWorkinNativePayoff(agent), pE, agent);
		double u3;
		if (agent.getRace()==1){
			u3 = 0;
		}
		else {
			u3 = computeWorkinEthnicUtility(computeWorkinEthnicPayoff(agent), pE, agent);
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

	public double computeEntrepreneurPayoff(Agent a) {
		//pExIkI * sum of p< - sum
		int maxK = 0;
		double maxProfit = 0.0;
		for(int k = 0; k < 100; k++){ //iterate through workers
			//for(int)
		}
		return 0.0;
	}

	public double computeWorkinNativePayoff(Agent a) {
		double wage = betaNE*B + (1-betaNE)*(a.getPI());
		double investments = r * a.getBI();
		return (1-unemployment) * wage +unemployment*B + investments - a.cI;
	}


	public double computeWorkinEthnicPayoff(Agent agent) {
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
	public double computeWorkinNativeUtility(double budget, double pE, Agent agent) {

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

				if (neighborlist.get(i).getSwitchOccupation() == 2 && neighborlist.get(i).getRace() == 2) {
					assimilation += 1;
				}

			}
			x = assimilation/neighborlist.size();
			u = Math.pow(cEE, gammaEA) * Math.pow(cEG, 1-gammaEA)+ theta * x;

		}

		return u;

	}

	public double computeWorkinEthnicUtility(double budget, double pE, Agent agent) {
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

		if (agent.getSwitchOccupation() == 2) {
			return nativeJobsearch (agent);
		}
		else {
			return ethnicJobsearch (agent);
		}

	}

	public Agent nativeJobsearch (Agent agent) {

		ArrayList<Agent> firms = new ArrayList<Agent>();

		for (int i = 0;i< numAgents;i++) {
			if (agentList.get(i).getRace() ==1 && agentList.get(i).getSwitchOccupation()==1) {
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
		        if (agentList.get(i).getRace() == 2 && agentList.get(i).getSwitchOccupation()== 1) {
					firms.add(agentList.get(i));
				}
		    }
		}
		int random = (int) (Math.random() * firms.size());
		Agent boss = firms.get(random);
		boss.addApplicants(agent);
		return boss;
	}




	//we are missing the process of boss to decide who they gonna hire for step2.

	//Step3:calculate the new unemployment rate

	public void calcUnemployment () { //modified so would update the unemployment rate
		double num = 0;
		for (int i = 0;i<numAgents;i++) {
			if (agentList.get(i).getcurtOccupation()==4) {
				num+=1;
			};

		}
		unemployment =  num/numAgents;
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
