package SRS2023.Model;
//package Model;

import SRS2023.Model.Agent;
//import Model.Agent;

import java.util.Arrays;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import uchicago.src.sim.analysis.DataRecorder;
import uchicago.src.sim.analysis.NumericDataSource;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplayConstants;
//import uchicago.src.sim.gui.Drawable;
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
    private double minorityShare;
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
    private double totalS, totalD;
    private boolean init;

    private DataRecorder data1;

    @Override
    public void begin() {
        buildModel();
        buildSchedule();
        //buildDisplay();
    }

    @Override
    public String[] getInitParam() {
        return new String[]{"gridWidth", "gridHeight", "period", "occupancy", "alpha", "beta", "betaN", "betaNE", "betaEE", "lambdaO", "gammaN", "gammaE", "gammEA", "theta"};
    }

    @Override
    public String getName() {
        return "Entrepreneurship Model";
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public void setup() {
        // TODO Auto-generated method stub
        agentList = new ArrayList<Agent>();
        period = 1;
        gridWidth = 50;
        gridHeight = 50;
        occupancy = 0.6;

        minorityShare = 0.3;
        r = 0.1; 
        B = 2; 
        pE = 0.8; 
        alpha = 0.2;
        beta = 0.7;
        betaN = 0.5;
        betaNE = 0.4; 
        betaEE = 0.3; 
        lambdaO = 0.8;
        gammaN = 0.3;
        gammaE = 0.7;
        gammaEA = 0.5;
        theta = 5;

        numAgents = (int) (gridWidth * gridHeight * occupancy);
        numEthnic = (int) (numAgents * minorityShare);
        numNative = numAgents - numEthnic;

        schedule = new Schedule(1);
        
        dsurf = new DisplaySurface(this, "test");
        registerDisplaySurface("test", dsurf);
        DisplayConstants.CELL_WIDTH = 50;
        DisplayConstants.CELL_HEIGHT = 50;

        init = true;
        System.out.println("Num Agents: " + numAgents);
        System.out.println("Ethnic: " + numEthnic);
        System.out.println("Native: " + numNative);
    }

    public void buildSchedule() {
        schedule.scheduleActionBeginning(1.0, new eachPeriod());
        schedule.scheduleActionAt((double) period, new finalPeriod());
    }

    class eachPeriod extends BasicAction {
        public void execute() {
            if(!init) updateOccupationChoice();
            else init = false;

            System.out.println("finish updateOccupationChoice");
            updateApplications();
            
            
            System.out.println("finish updateApplications");
            
            
           
            
            hireProcess();
            
           // for ( int i = 0;i<agentList.size();i++) {
            	//if (agentList.get(i).getcurOccupation()==1) {
            	//	System.out.println("id"+ i);
            	//	System.out.println("employees:"+agentList.get(i).getEmployees().size());
            	//	System.out.println("applicants:"+agentList.get(i).getApplicants().size());
            	//}
           // }
            System.out.println("finish hireProcess");
            updateUnemployment();
            System.out.println("unemployment: " + unemployment);
            updateCapital();
            System.out.println("finish updateCapital");
            updatePrice();
            // record every round agents' average ethnic percentage for the neighborhood
            // percentages of entrepreneurs by race
            // measure of segregation
            data1.record();
            data1.write();
        }
    }

    class finalPeriod extends BasicAction {
        public void execute() {
            stop();
        }
    }

    public void buildDisplay() {
        Object2DDisplay agentDisplay = new Object2DDisplay(Grid);
        agentDisplay.setObjectList(agentList);
        dsurf.addDisplayableProbeable(agentDisplay, "Agents");
        addSimEventListener(dsurf);
        dsurf.display();
    }

    public void buildModel() {
        Grid = new Object2DGrid(gridWidth, gridHeight);
        double sumP = 0.0;
        
        //randomly allocate the minority
        for (int id = 0; id < numEthnic; id++) {
            Agent ag = new Agent();
            ag.setID(id);
            //Race 1 refers to native and 2 refers to ethnic
            ag.setRace(2);
            //ethnic minorities will be located in the lower third of the lattice.
            int min = (int) (gridHeight * (2.0 / 3.0));
            int max = gridHeight;
            int randomY = (int) (Math.random() * (max - min)) + min;
            int randomX = (int) (Math.random() * gridWidth);
            //System.out.println("here " + String.valueOf(randomX) + " " + String.valueOf(randomY));
            while (Grid.getObjectAt(randomX, randomY) != null) {
                randomY = (int) (Math.random() * (max - min)) + min;
                randomX = (int) (Math.random() * gridWidth);
            }
            //System.out.println(String.valueOf(randomX) + " " + String.valueOf(randomY));
            Grid.putObjectAt(randomX, randomY, ag);
            //Allocate agents uniform randomly to initial occupations, all workers will unemployed and entrepreneurs will have no employees yet.
            double random = Math.random();
            //String occupation;
            //Occupation 1 is "Entrepreneur", 2 is "Work in Native Firm", 3 is "Work in  Ethnic Firm", 4 is "unemployed"
            if (random < 1.0 / 3) {
                ag.setcurOccupation(1);
                ag.setSwitchOccupation(1);
            } else if (1.0 / 3 <= random && random < 2.0 / 3) {
                ag.setcurOccupation(4);
                ag.setSwitchOccupation(2);
            } else {
                ag.setcurOccupation(4);
                ag.setSwitchOccupation(3);
            }
            ag.setCoords(new int[]{randomX, randomY});
            ag.setXI(Math.random()*10); //Entrepreneurial spirit/ability
            ag.setCI(Math.random()); //Cost of Assimilation
            ag.setPI(Math.random()); //Productivity
            ag.setBI(Math.random()); //Capital
            ag.setK(ag.getBI());
            //System.out.println(ag.getK());
            agentList.add(ag);
            sumP+= ag.getPI();
        }

        //randomly allocate the native
        for (int id = numEthnic; id < numEthnic+numNative; id++) {
            //System.out.println("id" + id);
            Agent ag = new Agent();
            ag.setID(id);

            ag.setRace(1); //native
            int randomX = (int) (Math.random() * gridWidth); // random X
            int randomY = (int) (Math.random() * gridHeight); // random Y

            while (Grid.getObjectAt(randomX, randomY) != null) {
                randomX = (int) (Math.random() * gridWidth);
                randomY = (int) (Math.random() * gridHeight);
            }
            Grid.putObjectAt(randomX, randomY, ag);
            //Allocate agents uniform randomly to initial occupations, all workers will unemployed and entrepreneurs will have no employees yet.
            double random = Math.random();

            if (random < 1.0 / 2) {
                ag.setcurOccupation(1);
                ag.setSwitchOccupation(1);
            } else {
                ag.setcurOccupation(4);
                ag.setSwitchOccupation(2);
            }
            ag.setCoords(new int[]{randomX, randomY});
            ag.setXI(Math.random()*10); //Entrepreneurial spirit/ability
            ag.setCI(Math.random()); //Cost of Assimilation
            ag.setPI(Math.random()); //Productivity
            ag.setBI(Math.random()); //Capital
            ag.setK(ag.getBI());
            
            agentList.add(ag);
            sumP+= ag.getPI();
        }
        
        averp = sumP/numAgents;

        data1 = new DataRecorder("/Users/m/Desktop/output.txt", this);
        data1.addNumericDataSource("Supply", new getTotalS());
        data1.addNumericDataSource("Demand", new getTotalD());
        data1.addNumericDataSource("Price", new getPrice());
    }

    //step1 Consider the occupation
    public void updateOccupationChoice() {
        for (int i = 0; i < agentList.size(); i++) {
            double random = Math.random();
            Agent agent = agentList.get(i);
            
            System.out.println("id: " + i);
            System.out.println("Race: "+ agent.getRace());
            System.out.println("cur:"+agent.getcurOccupation());
            System.out.println("next:"+agent.getSwitchOccupation());
            
            if (random < lambdaO) {
                Agent boss = agent.getBoss();
                int next = considerOccupation(agent);
                agent.setSwitchOccupation(next);
                //System.out.println(agent.getSwitchOccupation());

                //cut the link with its boss if its current occupation is a worker and gonna change the job
                if (agent.getSwitchOccupation() != agent.getcurOccupation() && boss != null) {
                    agent.setBoss(null);
                    boss.removeEmployee(agent);
                }
                //initialize Entrepreneurs' capital;
                //if (agent.getSwitchOccupation() != agent.getcurOccupation() && agent.getSwitchOccupation() == 1) {
                   // agent.setK(agent.getBI());
                //}
            } else {
                agent.setSwitchOccupation(agent.getcurOccupation());
                System.out.println();
            }
        }
    }

    public int considerOccupation(Agent agent) {
        double u1 = computeEntrepreneurUtility(computeEntrepreneurPayoff(agent), pE, agent);
        double u2 = computeWorkinNativeUtility(computeWorkinNativePayoff(agent), pE, agent);
        double u3;
        if (agent.getRace() == 1) {
            u3 = 0;
        } else {
            u3 = computeWorkinEthnicUtility(computeWorkinEthnicPayoff(agent), pE, agent);
        }
        double u4 = computeUnemployedUtility(computeUnemployedPayoff(agent), pE, agent);
        //missing a condition: utility is the same
        double[] numbers = {u1, u2, u3, u4};
        double max = Arrays.stream(numbers).max().getAsDouble();
        
        System.out.println("Payoff:");
        System.out.println("Entrepreneur:"+computeEntrepreneurPayoff(agent));
        System.out.println("WorkinNative:"+computeWorkinNativePayoff(agent));
        System.out.println("WorkinEthnic:"+computeWorkinEthnicPayoff(agent));
        System.out.println("Unemployed:"+computeUnemployedPayoff(agent));
        System.out.println("Utility:");
        System.out.println("Entrepreneur:"+u1);
        System.out.println("WorkinNative:"+u2);
        System.out.println("WorkinEthnic:"+u3);
        System.out.println("Unemployed:"+u4);
        System.out.println();
        
        if (max == u1) {
            return 1;
        } else if (max == u2) {
            return 2;
        } else if (max == u3) {
            return 3;
        } else {
            return 4;
        }
    }


    public double computeEntrepreneurPayoff(Agent agent) {
        double wage;

        if (agent.getRace() == 1) {
            wage = (betaNE * B + (1 - betaNE) * averp);
        } else {
            wage = (betaEE * B + (1 - betaNE) * averp);
        }

        double x = agent.getXI();

        double numeratorN = x * Math.pow(averp, beta) * Math.pow(beta, 1 - alpha) * Math.pow(alpha, alpha);
        double denominatorN = Math.pow(wage, 1 - alpha) * Math.pow(r, alpha);
        double exponentN = 1 / (1 - alpha - beta);

        double numeratorK = x * Math.pow(averp, beta) * Math.pow(alpha, 1 - beta) * Math.pow(beta, beta);
        double denominatorK = Math.pow(wage, beta) * Math.pow(r, 1 - beta);
        double exponentK = 1 / (1 - alpha - beta);

        double n = Math.pow(numeratorN / denominatorN, exponentN);
        double k = Math.pow(numeratorK / denominatorK, exponentK);

        if (agent.getRace() == 1) {
            n = Math.pow(numeratorN / denominatorN, exponentN);
            k = Math.pow(numeratorK / denominatorK, exponentK);

        } else {
            n = Math.pow((pE * numeratorN) / denominatorN, exponentN);
            k = Math.pow((pE * numeratorK) / denominatorK, exponentK);
        }
        double payoff = x * Math.pow(averp, beta) * Math.pow(k, alpha) * Math.pow(n, beta) - n * wage - r * k;
  
        return payoff;
    }

    public double computeWorkinNativePayoff(Agent agent) {
        double wage = betaN * B + (1 - betaN) * (agent.getPI());
        double payoff = (1 - unemployment) * wage + unemployment * B + r * agent.getBI();
        return payoff;
    }

    public double computeWorkinEthnicPayoff(Agent agent) {
        double wage = betaEE * B + (1 - betaEE) * agent.getPI();
        double payoff = (1 - unemployment) * wage + unemployment * B + r * agent.getBI();
        return payoff;
    }

    public double computeUnemployedPayoff(Agent agent) {
        double payoff = B + r * agent.getBI();
        return payoff;
    }

    public double computeEntrepreneurUtility(double budget, double pE, Agent agent) {
        //two conditions: native individuals and ethnic individuals
        double u;
        if (agent.getRace() == 1) {
            double cNE;
            double cNG;

            cNE = (budget * gammaN) / pE;
            cNG = (1 - gammaN) * budget;
            u = Math.pow(cNE, gammaN) * Math.pow(cNG, 1 - gammaN);
       
        } else {
            double cEE;
            double cEG;

            cEE = (budget * gammaE) / pE;
            cEG = (1 - gammaE) * budget;
            u = Math.pow(cEE, gammaE) * Math.pow(cEG, 1 - gammaE);
        }
        return u;
    }

    public double computeWorkinNativeUtility(double budget, double pE, Agent agent) {
        //two conditions: native individuals and ethnic individuals
        double u;
        if (agent.getRace() == 1) {
            double cNE;
            double cNG;

            cNE = (budget * gammaN) / pE;
            cNG = (1 - gammaN) * budget;
            u = Math.pow(cNE, gammaN) * Math.pow(cNG, 1 - gammaN);

        } else {
            double cEE;
            double cEG;
            double x;
            cEE = ((budget * gammaEA) / pE);
            cEG = (1 - gammaEA) * budget;

            Vector<Agent> neighborList;
            neighborList = Grid.getMooreNeighbors(agent.getCoords()[0], agent.getCoords()[1], false);
            double assimilation = 0;
            for (int i = 0; i < neighborList.size(); i++) {
                if (neighborList.get(i).getSwitchOccupation() == 2 && neighborList.get(i).getRace() == 2) {
                    assimilation += 1;
                }
            }
            x = assimilation / neighborList.size();
            u = Math.pow(cEE, gammaEA) * Math.pow(cEG, 1 - gammaEA) + theta * x;
        }
        return u;
    }

    public double computeWorkinEthnicUtility(double budget, double pE, Agent agent) {
        //Only ethnic individuals can become workers in ethnic firms
        double cEE;
        double cEG;
        double u;

        cEE = (budget * gammaE) / pE;
        cEG = (1 - gammaE) * budget;
        u = Math.pow(cEE, gammaE) * Math.pow(cEG, 1 - gammaE);
        return u;
    }

    public double computeUnemployedUtility(double budget, double pE, Agent agent) {
        double u;
        //two conditions: native individuals and ethnic individuals
        if (agent.getRace() == 1) {
            double cNE;
            double cNG;
            cNE = (budget * gammaN) / pE;
            cNG = (1 - gammaN) * budget;
            u = Math.pow(cNE, gammaN) * Math.pow(cNG, 1 - gammaN);
        } else {
            double cEE;
            double cEG;
            cEE = (budget * gammaE) / pE;
            cEG = (1 - gammaE) * budget;
            u = Math.pow(cEE, gammaE) * Math.pow(cEG, 1 - gammaE);
        }
        return u;
    }

//step 2.1: sending the applications.
//This method iterate through every agent, and the individuals that want to switch to the workers 
//will send the application to the entrepreneur.
    public void updateApplications() {
        for (int i = 0; i < agentList.size(); i++) {
            if (agentList.get(i).getSwitchOccupation() == 2 || agentList.get(i).getSwitchOccupation() == 3) {
                Agent agent = agentList.get(i);
              //  System.out.println("id:"+ i);
              //  System.out.println("race:"+agentList.get(i).getRace());
                Agent boss = jobSearch(agent);
                if (boss!= null) {
                	boss.addApplicants(agent); 
                }
                
            }
        }
    }

//the agent search for job in different ways, according to their races.
// It will return the entrepreneur that he gonna send the application to.
    public Agent jobSearch(Agent agent) {
        if (agent.getSwitchOccupation() == 2) {
            return nativeJobSearch(agent);
        } else {
            return ethnicJobSearch(agent);
        }
    }



//In this function, the input agent is the native individual that looking for the firms,
//and it will return the entrepreneur that the agent chooses to send the application
    public Agent nativeJobSearch(Agent agent) {
        ArrayList<Agent> firms = new ArrayList<Agent>();
        for (int i = 0; i < numAgents; i++) {
        	//debug to see the occupation
        	//if (agentList.get(i).getRace() == 1) {
        	//	System.out.println(i + ": Job:" +agentList.get(i).getSwitchOccupation());
        	//}
        	
            if (agentList.get(i).getRace() == 1 && agentList.get(i).getSwitchOccupation() == 1) {
                firms.add(agentList.get(i));
            }
        }
        System.out.println("size of firms: " + firms.size());
        int random = (int) (Math.random() * firms.size());
        System.out.println("random" + random);
        return firms.get(random);
    }

//in this function, the input agent is the ethnic individual that looking for the firms, 
//and it will return the entrepreneur that the agent chooses to send the application
    public Agent ethnicJobSearch(Agent agent) {
        ArrayList<Agent> firms = new ArrayList<Agent>();
        int x = agent.getCoords()[0];
        int y = agent.getCoords()[0];
        int radius = 2;
        //make sure the coordinates are inside the boundaries of the grid.
        int minX = Math.max(x - radius, 0);
        int minY = Math.max(y - radius, 0);
        int maxX = Math.min(x + radius, Grid.getSizeX() - 1);
        int maxY = Math.min(y + radius, Grid.getSizeY() - 1);
        // finding all the potential firms that they can send the applications
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                if (i == x && j == y) {
                    continue;
                }
                if (agentList.get(i).getRace() == 2 && agentList.get(i).getSwitchOccupation() == 1) {
                    firms.add(agentList.get(i));
                }
            }
        }
        // randomly choose one entrepreneur within all the potential choices, and update the entreprenuer's applications

        if (firms.size()==0) {
        	agent.setcurOccupation(4); // become unemployed?
        	return null;
        }
        else {
        	 int random = (int) (Math.random() * firms.size());
             Agent boss = firms.get(random);
             boss.addApplicants(agent);
             return boss;
        }
       

    }

    // step2.2: Enterpreneur's decision
//First update the individual's current occupation if they choose to switch to entrepreneurs;
//Both old and new entrepreneurs start to hire the workers.
    public void hireProcess() {
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            if (agent.getSwitchOccupation() == 1) {
                agent.setcurOccupation(1);
            }
            if (agent.getcurOccupation() == 1) {
                hireWorker(agent);
            }
        }
    }

    // This method will evaluate all the applicants of the entrepreneurs and hire each of them if they can increase entrepreneur's profit.
    public void hireWorker(Agent agent) {
        //order all the applicants from high productivity to low productivity
        ArrayList<Agent> applicants = agent.getApplicants();
        Collections.sort(applicants, new AgentIDComparator());
        Collections.reverse(applicants);
        for (Agent a : applicants) {
            ArrayList<Agent> curEmployees = agent.getEmployees();
            ArrayList<Agent> newEmployees = new ArrayList<>(curEmployees);  // a copy of curEmployees
            newEmployees.add(a);
            System.out.println(curEmployees.size());
            System.out.println(newEmployees.size());
            //compare the payoff
            double payoff0 = payoff(agent, curEmployees);
            double payoff1 = payoff(agent, newEmployees);
            System.out.println("payoff0:"+payoff0);
            System.out.println("payoff1:"+payoff1);
            if (payoff0 > payoff1) {
                a.setcurOccupation(4); //applicant becomes unemployed.
                System.out.println("not hire");
            } else {
            	System.out.println("yes hire");
                agent.addEmployee(a);
                a.setBoss(agent);
                a.setcurOccupation(a.getSwitchOccupation());
            }
        }
        agent.cleanApplicants();
    }

    //this method will compute the agent's(entrepreneur) payoff with a specific list of worker
    public double payoff(Agent agent, ArrayList<Agent> workers) {
        double sumW = 0.0;
        double sumP = 0.0;
        double payoff;
        
        for (Agent a : workers) {
            double wage;
            if (a.getRace() == 1 && agent.getRace() == 1) {
                wage = betaN * B + (1 - betaN) * a.getPI();

            } else if (a.getRace() == 2 && agent.getRace() == 1) {
                wage = betaNE * B + (1 - betaNE) * a.getPI();
            } else {
                wage = betaEE * B + (1 - betaEE) * a.getPI();
            }
            sumP += a.getPI();
            sumW += wage;
        }
        
        if (agent.getRace() == 1) {
        	//System.out.println("k:"+agent.getK());
        	//System.out.println("alpha:"+alpha);
        	//System.out.println("sumP:"+sumP);
        	//System.out.println("sumW:"+sumW);
        	//System.out.println("size:"+workers.size());
        	//System.out.println(agent.getXI() * Math.pow(agent.getK(), alpha) * Math.pow(sumP, beta) );
        	//System.out.println(r * agent.getK());
            payoff = agent.getXI() * Math.pow(agent.getK(), alpha) * Math.pow(sumP, beta) - sumW - r * agent.getK();
   
        } else {
            payoff = pE * agent.getXI() * Math.pow(agent.getK(), alpha) * Math.pow(sumP, beta) - sumW - r * agent.getK();
        }
        return payoff;
    }

    static class AgentIDComparator implements Comparator<Agent> {
        @Override
        public int compare(Agent agent1, Agent agent2) { // 根据 Agent 的 productivity 属性进行比较
            return Double.compare(agent1.getPI(), agent2.getPI());
        }
    }

    //Step3:calculate and update the new unemployment rate
    public void updateUnemployment() {
        System.out.println(agentList.size());
        double num = 0;
        for (int i = 0; i < numAgents; i++) {
            if (agentList.get(i).getcurOccupation() == 4) {
                num += 1;
            }
        }
        unemployment = num / numAgents;
    }

    //step4: update Entrepreneur's capital
    //This method will iterate through every agent and let the entrepreneurs adjust their capital will possibility lambdaO.
    public void updateCapital() {
        for (int i = 0; i < agentList.size(); i++) {
            Agent agent = agentList.get(i);
            if (agent.getSwitchOccupation() == 1) {
                double random = Math.random();
                if (random < lambdaO) {
                    changeCapital(agent);
                }
            }
        }
    }

    //agent(entrepreneur) will adjust their capital
    public void changeCapital(Agent agent) {
        double sumW = 0.0;
        double sumP = 0.0;
        ArrayList<Agent> employees = agent.getEmployees();
        //System.out.println("employees:"+employees.size());
        for (Agent a : employees) {
            double wage;
            if (a.getRace() == 1 && agent.getRace() == 1) {
                wage = betaN * B + (1 - betaN) * a.getPI();
            } else if (a.getRace() == 2 && agent.getRace() == 1) {
                wage = betaNE * B + (1 - betaNE) * a.getPI();
            } else {
                //ethnic firms and ethnic workers
                wage = betaEE * B + (1 - betaEE) * a.getPI();
            }
            sumP += a.getPI();
            sumW += wage;
        }
        double numerator = sumW + r * agent.getK();
        double denominator = alpha * Math.pow(sumP, beta) * agent.getXI();
       // System.out.println("sumW:"+sumW);
        //System.out.println("r:"+r);
        //System.out.println("k:"+agent.getK());
       // System.out.println(numerator);
       // System.out.println(denominator);
        double k = Math.pow(numerator / denominator, 1 / (alpha - 1));
        agent.setK(k);
        System.out.println("k: " + k);
    }

    public void updatePayoff(Agent a){
        int race = a.getRace();
        //native = 1, ethnic = 2
        int occupation = a.getcurOccupation();
        double payoff;
        //Occupation 1 is "Entrepreneur", 2 is "Work in Native Firm", 3 is "Work in  Ethnic Firm", 4 is "unemployed"
        if(race == 1){
            if (occupation==1){
                a.setCurrPayoff(computeEntrepreneurPayoff(a));
            } else if (occupation==2) {
                a.setCurrPayoff(computeWorkinNativePayoff(a));
            } else if (occupation==3) {
                a.setCurrPayoff(computeWorkinEthnicPayoff(a));
            } else { // unemployed
                a.setCurrPayoff(computeUnemployedPayoff(a));
            }
        } else {
            if (occupation==1){
                a.setCurrPayoff(computeEntrepreneurPayoff(a));
            } else if (occupation==2) {
                a.setCurrPayoff(computeWorkinNativePayoff(a));
            } else if (occupation==3) {
                a.setCurrPayoff(computeWorkinEthnicPayoff(a));
            } else { // unemployed
                a.setCurrPayoff(computeUnemployedPayoff(a));
            }
        }

    }
    //step5: update the price of ethnic good

    //4.3 compute total supply and demand
    // aggregate demand - agents maximizing utility given current income
    // demand as a function of price
    // supply - total production of entrepreneurs assuming current entrepreneurs and labor holdings
    public void updatePrice() {
        // agg demand - for all agents - demand for ethnic goods under different prices
        // can use prev aggregate supply - iterate through all ethnic entrepreneurs -> calc using production function
        // price = 0.5 -> 0.6
        totalD = 0;
        totalS = 0;
        System.out.println(1);
        //calc totalS / fixed supply
        for (Agent a : agentList) {
            updatePayoff(a);
            if (a.getRace() == 2 && a.getcurOccupation() == 1) { //ethnic entrepreneurs
                double agentKI = a.getK();
                double sumPK = 0;
                ArrayList<Agent> employees = a.getEmployees();
                for (Agent employee : employees) sumPK += employee.getPI();
                totalS += Math.pow(agentKI, alpha) * Math.pow(sumPK, 1 - alpha);
                System.out.println("AgentKI: "+agentKI);
                System.out.println("Alpha: " + alpha);
                System.out.println("Aggregate Supply: " + totalS);
            }
        }
        //calc totalD
        // both ethnic and native
        // iterate through all agents, take into account utility
        // amount of ethnic good that would maximize utility
        for (Agent agent : agentList)
            if (agent.getRace() == 1) totalD += agent.getCurrPayoff() * gammaN / pE; //native
            else totalD += agent.getCurrPayoff() * gammaE / pE; //ethnic

        updateDemand();
        System.out.println("Initial Supply: " + totalS);
        System.out.println("Initial Demand: " + totalD);
        System.out.println("Initial Error" +Math.abs(totalS-totalD)/totalS);
        while(Math.abs(totalS-totalD)/totalS > 0.1){
            if(totalD>totalS) pE += 100;
            else pE -= 100;
            updateDemand();
            //System.out.println("Supply: " + totalS);
            //System.out.println("Demand: " + totalD);
            //System.out.println("Error: " +Math.abs(totalS-totalD)/totalS);
            //System.out.println("Price: " + pE);
        }

        System.out.println("Final Supply: " + totalS);
        System.out.println("Final Demand: " + totalD);
        System.out.println("Final Price: " + pE);
        // change price -> change in demand
        // compare S/D
        //pE =
        // y = price
    }

    public void updateDemand(){
        //calc totalD
        // both ethnic and native
        // iterate through all agents, take into account utility
        // amount of ethnic good that would maximize utility
        totalD = 0;
        for (Agent agent : agentList)
            if (agent.getRace() == 1) totalD += agent.getCurrPayoff() * gammaN / pE; //native
            else totalD += agent.getCurrPayoff() * gammaE / pE; //ethnic
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

    public double getOccupancy() {
        return occupancy;
    }
    public void setOccupancy(double o) {
        this.occupancy = o;
    }

    public double getAlpha() {
        return alpha;
    }
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }
    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getBetaN() {
        return betaN;
    }
    public void setBetaN(double betaN) {
        this.betaN = betaN;
    }

    public double getBetaNE() {
        return betaNE;
    }
    public void setBetaNE(double betaNE) {
        this.betaNE = betaNE;
    }

    public double getBetaEE() {
        return betaEE;
    }
    public void setBetaEE(double betaEE) {
        this.betaEE = betaEE;
    }

    public double getLambdaO() {
        return lambdaO;
    }
    public void setLambdaO(double lambdaO) {
        this.lambdaO = lambdaO;
    }

    public double getGammaN() {
        return gammaN;
    }
    public void setGammaN(double gammaN) {
        this.gammaN = gammaN;
    }

    public double getGammaE() {
        return gammaE;
    }
    public void setGammaE(double gammaE) {
        this.gammaE = gammaE;
    }

    public double getGammaEA() {
        return gammaEA;
    }
    public void setGammaEA(double gammaEA) {
        this.gammaEA = gammaEA;
    }

    public double getTheta() {
        return theta;
    }
    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getMinorityShare() {return minorityShare;}
    public void setMinorityShare(double m) {minorityShare = m;}

    class getTotalS implements NumericDataSource{
        public double execute() {
            return totalS;
        }
    }

    class getTotalD implements NumericDataSource{
        public double execute() {
            return totalD;
        }
    }

    class getPrice implements NumericDataSource{
        public double execute() {return pE;}
    }
}