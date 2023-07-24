//package SRS2023.Model;
package Model;

//import SRS2023.Model.Agent;
import Model.Agent;

import java.util.Arrays;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
    private double averp; // workers' average productivity
    private double alpha;
    private double beta;
    private double betaN;
	private double betaNE; //bargaining power
	private double betaEE; //bargaining power
	private double lambdaO;
	private double gammaN;
	private double gammaE;
	private double gammaEA;
	private double theta;



    private DataRecorder data1;

    @Override
    public void begin() {
        buildModel();
		buildSchedule();
		buildDisplay();
    }

    @Override
	public String[] getInitParam() {return new String[]{"gridWidth", "gridHeight", "period", "occupancy", "alpha","beta","betaN","betaNE","betaEE","lambdaO","gammaN","gammaE","gammEA","theta"};}

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
			updateOccupationChoice();
			updateApplications();
			hireProcess();
			updateUnemployment();
			updateCaptical();
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
	
	//step1 Consider the occupation
	public void updateOccupationChoice(){
		for (int i = 0; i< agentList.size();i++) {
			double random = Math.random();
			Agent agent = agentList.get(i);
			if (random < lambdaO) {
				Agent boss = agent.getBoss();
				int next = considerOccupation(agent);
				agent.setSwitchOccupation(next);
				
				//cut the link with its boss if its current occupation is a worker and gonna change the job
				if (agent.getSwitchOccupation()!= agent.getcurOccupation() && boss!= null) {
					agent.setBoss(null);
					boss.removeEmployee(agent);
				}
				//initialize Entrepreneurs' capital;
				if (agent.getSwitchOccupation()!= agent.getcurOccupation() && agent.getSwitchOccupation()==1) {
					agent.setK(agent.getBI());
				}
			}
			
			else {
				agent.setSwitchOccupation(agent.getcurOccupation());
			}				
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


	public double computeEntrepreneurPayoff(Agent agent) {
		double wage;
		
		if (agent.getRace()==1) {
			wage = (betaNE * B + (1-betaNE)* averp);
		}
		else {
			wage = (betaEE * B + (1-betaNE)* averp);
			
		}
		
		
		double x = agent.getXI();
		
		double numeratorN = x * Math.pow(averp, beta)*Math.pow(beta, 1-alpha)*Math.pow(alpha, alpha);
		double denominatorN = Math.pow(wage, 1-alpha)* Math.pow(r, alpha);
		double exponentN = 1/(1-alpha-beta);
		
		double numeratorK = x * Math.pow(averp, beta)*Math.pow(alpha, 1-beta)*Math.pow(beta, beta);
		double denominatorK = Math.pow(wage, beta)* Math.pow(r, 1-beta);
		double exponentK = 1/(1-alpha-beta);
		
		
		double n = Math.pow(numeratorN/denominatorN,exponentN);
		double k = Math.pow(numeratorK/denominatorK,exponentK);
		
		if (agent.getRace()==1) {
			n = Math.pow(numeratorN/denominatorN,exponentN);
			k = Math.pow(numeratorK/denominatorK,exponentK);
			
		}
		else {
			n = Math.pow((pE*numeratorN)/denominatorN,exponentN);
			k = Math.pow((pE*numeratorK)/denominatorK,exponentK);
		}
		double payoff = x * Math.pow(averp,beta) * Math.pow(k,alpha)* Math.pow(n,beta)- n * wage - r * k;

		return payoff;
	}


	public double computeWorkinNativePayoff(Agent agent) {
		double wage = betaN*B + (1-betaN)*(agent.getPI());
		//double investments = r * a.getBI();
		double payoff = (1-unemployment) * wage + unemployment*B + r*agent.getBI();
		return payoff;
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



	//step 2.1: sending the applications
	public void updateApplications() {
		for (int i = 0;i<agentList.size();i++) {
			if (agentList.get(i).getSwitchOccupation()== 2 || agentList.get(i).getSwitchOccupation()== 3) {
				Agent agent = agentList.get(i);
				Agent boss = jobSearch(agent);
				boss.addApplicants(agent);
			}
		}
	}

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

	
	
// step2.2: Enterpreneur's decision
	
	public void hireProcess() {
		for (int i = 0; i< agentList.size();i++) {
			Agent agent = agentList.get(i);
			
			if (agent.getSwitchOccupation() == 1) {
				//become entrepreneur and hire worker
				agent.setcurOccupation(1);
				hireWorker(agent); 
				}
			}
		}
	
	

	
	public void hireWorker(Agent agent) {
		//order all the applicants from high productivity to low productivity
		
		ArrayList<Agent> applicants = agent.getApplicants();
		Collections.sort(applicants, new AgentIDComparator());
		Collections.reverse(applicants);

        
        for (Agent a : applicants) {

        	ArrayList<Agent> curEmployees = agent.getEmployees();
        	ArrayList<Agent> newEmployees = new ArrayList<>(curEmployees);  // a copy of curEmployees 
        	newEmployees.add(a);
        	double payoff0 = payoff (agent, curEmployees);
        	double payoff1 = payoff (agent, newEmployees);
        	
        	if (payoff0>payoff1) {
        		a.setcurOccupation(4); //applicant becomes unemployed.
        		
        	}
        	else {
        		agent.addEmployee(a);
        		a.setBoss(agent);
        		a.setcurOccupation(a.getSwitchOccupation());
        	}
        }
        agent.cleanApplicants();
        
    }
    
    public double payoff (Agent agent, ArrayList<Agent> workers ) {
    	double sumW = 0.0;
    	double sumP = 0.0;
    	double payoff;
    	for (Agent a : workers) {
    		double wage;
    		if (a.getRace()== 1 && agent.getRace()== 1) {
    			wage = betaN * B +(1 - betaN) * a.getPI();
    			
    		}
    		else if (a.getRace()== 2 && agent.getRace()== 1){
    			wage = betaNE * B +(1 - betaNE) * a.getPI();
    		}
    		else {
    			wage = betaEE * B +(1 - betaEE) * a.getPI();
    		}
    		
    		sumP += a.getPI();
    		sumW += wage;
    	}
    	
    	
    	if (agent.getRace()==1) {
    		payoff = agent.getXI()* Math.pow(agent.getK(),alpha) * Math.pow(sumP,beta) - sumW - r * agent.getK();
    	}
    	else {
    		payoff = pE * agent.getXI()* Math.pow(agent.getK(),alpha) * Math.pow(sumP,beta) - sumW - r * agent.getK();
    	}

    	
    	return payoff;
    	
    }
    
    
    
    static class AgentIDComparator implements Comparator<Agent> {
        @Override
        public int compare(Agent agent1, Agent agent2) {
            // 根据 Agent 的 ID 属性进行比较
            return Double.compare(agent1.getPI(), agent2.getPI());
        }
    }
		
		


	//Step3:calculate and update the new unemployment rate

	public void updateUnemployment () { 
		double num = 0;
		for (int i = 0;i<numAgents;i++) {
			if (agentList.get(i).getcurOccupation()==4) {
				num+=1;
			};

		}
		unemployment =  num/numAgents;
	}
	
	//step4: update Entrepreneur's capital
	public void updateCaptical() {
		for (int i = 0; i< agentList.size();i++) {
			Agent agent = agentList.get(i);
			if (agent.getSwitchOccupation() == 1) {
				double random = Math.random();
				if (random < lambdaO) {
					changeCapital(agent);
				}
			}
		}
	}
	

    public void changeCapital(Agent agent) {
    	double sumW = 0.0;
    	double sumP = 0.0;
    	ArrayList<Agent> employees = agent.getEmployees();
    	for (Agent a : employees) {
    		double wage;
    		if (a.getRace()== 1 && agent.getRace()== 1) {
    			wage = betaN * B +(1 - betaN) * a.getPI();
    			
    		}
    		else if (a.getRace()== 2 && agent.getRace()== 1){
    			wage = betaNE * B +(1 - betaNE) * a.getPI();
    		}
    		else {
    			//ethnic firms and ethnic workers
    			wage = betaEE * B +(1 - betaEE) * a.getPI();
    		}
    		
    		sumP += a.getPI();
    		sumW += wage;
    	}
    	
    	double numerator = sumW + r * agent.getK();
		double denominator = alpha * Math.pow(sumP, beta) * agent.getXI();
		double k = Math.pow(numerator/denominator, 1/(alpha-1));
		agent.setK(k); 	
    }
    
    
    //step5: update the price of ethnic good

    
    public void updatePrice(){
    	int totalD;
    	int totalS;
    	
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

	
	public double getAlpha(){
		return alpha;
	}

	public void setAlpha(double alpha){
		this.alpha = alpha;
	}
	public double getBeta(){
		return beta;
	}

	public void setBeta(double beta){
		this.beta = beta;
	}
	public double getBetaN(){
		return betaN;
	}

	public void setBetaN(double betaN){
		this.betaN = betaN;
	}
	public double getBetaNE(){
		return betaNE;
	}

	public void setBetaNE(double betaNE){
		this.betaNE = betaNE;
	}
	public double getBetaEE(){
		return betaEE;
	}

	public void setBetaEE(double betaEE){
		this.betaEE = betaEE;
	}
	public double getLambdaO(){
		return lambdaO;
	}

	public void setLambdaO(double lambdaO){
		this.lambdaO = lambdaO;
	}
	public double getGammaN(){
		return gammaN;
	}

	public void setGammaN(double gammaN){
		this.gammaN = gammaN;
	}
	public double getGammaE(){
		return gammaE;
	}

	public void setGammaE(double gammaE){
		this.gammaE = gammaE;
	}
	public double getGammaEA(){
		return gammaEA;
	}

	public void setGammaEA(double gammaEA){
		this.gammaEA = gammaEA;
	}
	public double getTheta(){
		return theta;
	}

	public void setTheta(double theta){
		this.theta = theta;
	}

}
