package SRS2023.Model;

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
	private int ethnicNumber;
	private int nativeNumber;
	private int period;
	
	//private int lambda;
	//private int beta;
	//private int alpha;


    private DataRecorder data1;

    @Override
    public void begin() {
        // TODO Auto-generated method stub

    }

    @Override
	public String[] getInitParam() {
		return new String[]{"gridWidth", "gridHeight", "numAgents", "period"};
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
		numAgents = (int) (gridWidth * gridHeight * 0.8);
		ethnicNumber = (int)(numAgents * 0.3);
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
			if (random < 1.0/3) {
				ag.setcurOccupation("Entreperneur");
				
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
				ag.setcurOccupation("Entreperneur");
				
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
	
	
	public String considerOccupation(){
		
		//first compute the payoff, then use the payoff to compute the utility
		//use the utility to decide the occupation
		//return "Work in Native Firm", "Work in  Ethnic Firm" , "Entrepreneur" or "unemployed"
		//is the βEE Heterogeneous? what about the outside option B, interest rate r?
		
		return "";
		
	}
	
	public double computeEntrepreneurPayoff(Agent agent) {
		
		return 0.0;
		
	}
	
	public double computeNativeworkerPayoff(Agent agent) {
		return 0.0;
		
	}
	
	public double computeEthnicworkerPayoff(Agent agent) {
		return 0.0;
		
	}
	public double computeUnemployedPayoff(Agent agent) {
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

   
}
