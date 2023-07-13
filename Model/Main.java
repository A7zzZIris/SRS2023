package Model;

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
	private int agentNumber;
	private int minorityNumber;
	private int majorityNumber;
	private int period;

	private DataRecorder data1;

	@Override
	public void begin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getInitParam() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Schedule getSchedule() {
		// TODO Auto-generated method stub
		return schedule;
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub
		agentList = new ArrayList<Agent>();
		period = 1000;
		gridWidth = 50;
		gridHeight = 50;
		agentNumber = (int) (gridWidth * gridHeight * 0.8);
		minorityNumber = (int)(agentNumber * 0.3);
		majorityNumber = agentNumber - minorityNumber;
		schedule = new Schedule(1);
		
		
	}
	
	public void buildModel() {
		
		Grid = new Object2DGrid(gridWidth, gridHeight);
		
		//randomly allocate the minority
		for (int id = 0; id < minorityNumber; id ++) {
			Agent ag = new Agent();
			ag.setID(id);
			//Race 1 refers to majority and 2 refers to minority
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
	
			ag.setCoordinate(new int[] {randomX, randomY});
			ag.setEntrepreneurialAbility(x_i);
			ag.setCostofAssimulation(c_i);
			ag.setProductivity(p_i);
			ag.setWealthToBusiness(b_i);
			agentList.add(ag);
			
		}
		
		
		//randomly allocate the majority
		for (int id = minorityNumber; id < majorityNumber; id++) {
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
			
			ag.setCoordinate(new int[] {randomX, randomY});
			ag.setEntrepreneurialAbility(x_i);
			ag.setCostofAssimulation(c_i);
			ag.setProductivity(p_i);
			ag.setWealthToBusiness(b_i);
			agentList.add(ag);
			
		}
	}
}
