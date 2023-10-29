package SRS2023.Model;

// package Model;
// import Model.Agent;

import java.util.Arrays;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import uchicago.src.sim.analysis.DataRecorder;
import uchicago.src.sim.analysis.NumericDataSource;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplayConstants;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.space.Object2DGrid;

/**
 *
 */
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
    private boolean init; //first period

    public double[] stats;
    public HashMap<String, Double> stats1;

    private DataRecorder d;

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
        agentList = new ArrayList<Agent>();
        period = 10;
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
    }

    public void buildSchedule() {
        schedule.scheduleActionBeginning(1.0, new eachPeriod());
        schedule.scheduleActionAt((double) period, new finalPeriod());
    }

    class eachPeriod extends BasicAction {
        public void execute() {
            if (!init) updateOccupationChoice(); //fixed in method below
            else init = false;
            updateApplications();
            hireProcess();
            updateUnemployment();
            //updateCapital();
            updatePrice(); //price fixed in update price
            d.record();
            d.write();
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


    /**
     * Builds the simulation model by initializing agents, their attributes, and grid placement.
     * This method populates the grid with agents of ethnic and native backgrounds, assigns their initial attributes
     * such as race, occupation, coordinates, entrepreneurial spirit, cost of assimilation, productivity, wealth,
     * and capital. The method also calculates the average productivity of the agents.
     */
    public void buildModel() {
        Grid = new Object2DGrid(gridWidth, gridHeight);
        double sumP = 0.0;
        int numEnt = 0;
        int numEthEnt = 0;
        int numNatEnt = 0;
        int numEthUn = 0;
        int numNatUn = 0;
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
                numEthEnt++;
            } else if (1.0 / 3 <= random && random < 2.0 / 3) {
                ag.setcurOccupation(4);
                ag.setSwitchOccupation(2);
                numEthUn++;
            } else {
                ag.setcurOccupation(4);
                ag.setSwitchOccupation(3);
                numEthUn++;
            }
            ag.setCoords(new int[]{randomX, randomY});
            ag.setXI(Math.random() * 10); //Entrepreneurial spirit/ability
            ag.setCI(Math.random()); //Cost of Assimilation
            ag.setPI(Math.random()); //Productivity
            ag.setBI(Math.random()); //Wealth to begin business
            ag.setK(ag.getBI());  // Capital is equal to wealth to begin business at the beginning
            agentList.add(ag);
            sumP += ag.getPI();
        }
        //randomly allocate the native
        for (int id = numEthnic; id < numEthnic + numNative; id++) {
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
                numNatEnt++;
            } else {
                ag.setcurOccupation(4);
                ag.setSwitchOccupation(2);
                numNatUn++;
            }
            ag.setCoords(new int[]{randomX, randomY});
            ag.setXI(Math.random() * 10); //Entrepreneurial spirit/ability
            ag.setCI(Math.random()); //Cost of Assimilation
            ag.setPI(Math.random()); //Productivity
            ag.setBI(Math.random()); //Capital
            ag.setK(ag.getBI());
            agentList.add(ag);
            sumP += ag.getPI();
        }
        System.out.println("Num Native Entrepreneur: " + numNatEnt);
        System.out.println("Num Ethnic Entrepreneur: " + numEthEnt);
        System.out.println("Num Native Unemployed: " + numNatUn);
        System.out.println("Num Ethnic Unemployed: " + numEthUn);
        System.out.println("Percentage Unemployed: " + (double)(numNatUn + numEthUn)/numAgents);
        updateUnemployment();
        System.out.println("Unemployment: " +unemployment);

        averp = sumP / numAgents;

        d = new DataRecorder("/Users/m/Desktop/output.txt", this);
        //stats1 = getStats1();
        d.addNumericDataSource("Stats1", new getStats1());
        d.addNumericDataSource("Percentage of Entrepreneurs", new getPercEntrepreneurs());
        d.addNumericDataSource("Percentage of Native Entrepreneurs", new getPercNativeEntrepreneurs());
        d.addNumericDataSource("Percentage of Ethnic Entrepreneurs", new getPercEthnicEntrepreneurs());
        d.addNumericDataSource("Percentage of Native Workers", new getPercNativeWorkers());
        d.addNumericDataSource("Percentage of Ethnic Workers", new getPercEthnicWorkers());
        d.addNumericDataSource("Unemployment Rate", new getUnemploymentRate());
        d.addNumericDataSource("Supply", new getTotalS());
        d.addNumericDataSource("Demand", new getTotalD());
        d.addNumericDataSource("Price", new getPrice());
        d.addNumericDataSource("Average Payoff", new getAverageWage());
        d.addNumericDataSource("Average Payoff NE", new getAveragePayoffNE());
        d.addNumericDataSource("Average Payoff EE", new getAveragePayoffEE());
        d.addNumericDataSource("Average Payoff NW", new getAveragePayoffNW());
        d.addNumericDataSource("Average Payoff EW", new getAveragePayoffEW());
        d.addNumericDataSource("Average Payoff U", new getAveragePayoffU());
        d.addNumericDataSource("Average Utility NE", new getAverageUtilityNE());
        d.addNumericDataSource("Average Utility EE", new getAverageUtilityEE());
        d.addNumericDataSource("Average Utility NW", new getAverageUtilityNW());
        d.addNumericDataSource("Average Utility EW", new getAverageUtilityEW());
        d.addNumericDataSource("Average Utility U", new getAverageUtilityU());
        d.addNumericDataSource("Average Entrepreneur Capital", new getAverageEntrepreneurCapital());
    }


    /**
     * Step1: update every agents' next occupational choice
     * Updates the occupation choice for each agent in the agent list based on certain criteria.
     * The method iterates through each agent, evaluates their current occupation, and decides whether
     * they should switch to a new occupation or keep their current one based on a random probability
     * and utility considerations.
     */
    public void updateOccupationChoice() {
        for (int i = 0; i < agentList.size(); i++) {
            double random = Math.random();
            Agent agent = agentList.get(i);

            /*
            if (random < lambdaO) {
                Agent boss = agent.getBoss();
                int next = considerOccupation(agent);
                agent.setSwitchOccupation(next);
                //cut the link with this agent's current boss if he want to change the job
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
            }
             */
            agent.setSwitchOccupation(agent.getcurOccupation()); //FIX OCC CHOICE
        }
    }


    /**
     * This method considers the best occupation choice for an agent based on their utility calculations
     * in different occupation scenarios: Entrepreneur, WorkinNative, WorkinEthnic, and Unemployed.
     *
     * @param agent The agent for whom the occupation choice is being considered.
     * @return An integer representing the recommended occupation choice:
     * 1 for Entrepreneur, 2 for WorkinNative, 3 for WorkinEthnic, 4 for Unemployed.
     */
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
        //System.out.println("max" + max);
        agent.setUtility(max);
        System.out.println("utility" + agent.getUtility());
        /*
        System.out.println("Payoff:");
        System.out.println("Entrepreneur:" + computeEntrepreneurPayoff(agent));
        System.out.println("WorkinNative:" + computeWorkinNativePayoff(agent));
        System.out.println("WorkinEthnic:" + computeWorkinEthnicPayoff(agent));
        System.out.println("Unemployed:" + computeUnemployedPayoff(agent));
        System.out.println("Utility:");
        System.out.println("Entrepreneur:" + u1);
        System.out.println("WorkinNative:" + u2);
        System.out.println("WorkinEthnic:" + u3);
        System.out.println("Unemployed:" + u4);
        System.out.println();
        */
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

    /*
     * The following functions will compute different occupational choices' payoff and utility
     * according to different races.
     * @param agent
     */
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

    /*
     *
     * This method iterate through every agent, and the individuals that want to become workers
     * will send the application to the randomly chosen entrepreneur.
     */


    /**
     * Step 2.1: sending the applications.
     * Updates job applications for agents who are switching to occupation types 2 (WorkinNative) or 3 (WorkinEthnic).
     * The method iterates through the agent list, and if an agent plans to switch to workers,
     * it searches for potential job opportunities and sends applications to randomly chosen entrepreneurs.
     */
    public void updateApplications() {
        for (int i = 0; i < agentList.size(); i++) {
            if (agentList.get(i).getSwitchOccupation() == 2 || agentList.get(i).getSwitchOccupation() == 3) {
                Agent agent = agentList.get(i);
                //  System.out.println("id:"+ i);
                //  System.out.println("race:"+agentList.get(i).getRace());
                Agent boss = jobSearch(agent);
                if (boss != null) {
                    boss.addApplicants(agent);
                }

            }
        }
    }

    /**
     * Searches for job opportunities for the given agent based on their chosen occupation type.
     * This function returns the entrepreneur to whom the agent decides to send the job application.
     *
     * @param agent The agent who is searching for a job opportunity.
     * @return The entrepreneur to whom the job application is sent.
     */
    public Agent jobSearch(Agent agent) {
        if (agent.getSwitchOccupation() == 2) {
            return nativeJobSearch(agent);
        } else {
            return ethnicJobSearch(agent);
        }
    }


    /**
     * Searches for job opportunities for the given agent who intends to work in native firms.
     * This function returns an entrepreneur to whom the agent sends the job application.
     *
     * @param agent The agent who wants to work in native firms.
     * @return The entrepreneur to whom the job application is sent.
     */


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

        int random = (int) (Math.random() * firms.size());

        //System.out.println("size of firms: " + firms.size());
        //System.out.println("random" + random);

        return firms.get(random);

    }


    /**
     * Searches for job opportunities for the given agent who intends to work in ethnic firms.
     * This function returns an entrepreneur to whom the agent sends the job application.
     * The search is performed within a certain radius around the agent's current location.
     * If suitable job opportunities are found, the agent sends an application to a randomly chosen entrepreneur.
     * If no suitable opportunities are found, the agent becomes unemployed (occupation code 4).
     *
     * @param agent The agent who wants to work in ethnic firms.
     * @return The entrepreneur to whom the job application is sent, or null if no suitable opportunities are found.
     */

    public Agent ethnicJobSearch(Agent agent) {
        ArrayList<Agent> firms = new ArrayList<Agent>();
        int x = agent.getCoords()[0];
        int y = agent.getCoords()[1];
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

        if (firms.size() == 0) {
            agent.setcurOccupation(4); // become unemployed if there is no firm around him?
            return null;
        } else {
            int random = (int) (Math.random() * firms.size());
            Agent boss = firms.get(random);
            boss.addApplicants(agent);
            return boss;
        }


    }


    /**
     * Step2.2: Enterpreneur's decision
     * Conducts the hiring process for entrepreneurs. This method first updates the current occupation of individuals
     * who choose to switch to entrepreneurs, and then both old and new entrepreneurs start to hire workers.
     */

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


    /**
     * Evaluates all the applicants for an entrepreneur and hires each of them if they can increase the entrepreneur's profit.
     * Applicants are ordered by productivity from high to low, and the entrepreneur compares the payoff before and after hiring.
     * If hiring a worker would decrease the entrepreneur's profit, the worker remains unemployed; otherwise, the worker is hired.
     *
     * @param agent The entrepreneur who is conducting the hiring process.
     */
    public void hireWorker(Agent agent) {
        //order all the applicants from high productivity to low productivity
        ArrayList<Agent> applicants = agent.getApplicants();
        Collections.sort(applicants, new AgentIDComparator());
        Collections.reverse(applicants);
        for (Agent a : applicants) {
            ArrayList<Agent> curEmployees = agent.getEmployees();
            ArrayList<Agent> newEmployees = new ArrayList<>(curEmployees);  // a copy of curEmployees
            newEmployees.add(a);
            //System.out.println(curEmployees.size());
            //System.out.println(newEmployees.size());
            //compare the payoff
            double payoff0 = payoff(agent, curEmployees);
            double payoff1 = payoff(agent, newEmployees);
            //System.out.println("payoff0:"+payoff0);
            //System.out.println("payoff1:"+payoff1);
            if (payoff0 > payoff1) {
                a.setcurOccupation(4); //applicant becomes unemployed.
                //System.out.println("not hire");
            } else {
                //System.out.println("yes hire");
                agent.addEmployee(a);
                a.setBoss(agent);
                a.setcurOccupation(a.getSwitchOccupation());
            }
        }
        agent.cleanApplicants();
    }


    /**
     * Computes the entrepreneur's payoff with the given list of workers.
     * This method calculates the entrepreneur's payoff based on the workers' wages and productivity.
     *
     * @param agent   The entrepreneur for whom the payoff is being computed.
     * @param workers The list of workers employed by the entrepreneur.
     * @return The computed payoff for the entrepreneur.
     */

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


    /**
     * Comparator class used to compare agents based on their productivity (PI).
     * This class implements the Comparator interface to compare agents' productivity for sorting purposes.
     */
    static class AgentIDComparator implements Comparator<Agent> {
        @Override
        public int compare(Agent agent1, Agent agent2) { // compare agent's productivity 
            return Double.compare(agent1.getPI(), agent2.getPI());
        }
    }

    /**
     * Step3: calculate and update the new unemployment rate
     * Updates the unemployment rate based on the current occupation status of agents.
     * This method calculates and updates the unemployment rate using the number of unemployed agents.
     */
    public void updateUnemployment() {
        //System.out.println(agentList.size());
        double num = 0;
        for (int i = 0; i < numAgents; i++) {
            if (agentList.get(i).getcurOccupation() == 4) {
                num += 1;
            }
        }
        unemployment = num / agentList.size();
        //System.out.println(num);
    }


    /**
     * step4: update Entrepreneur's capital
     * Updates the capital of entrepreneurs with a certain probability.
     * This method iterates through every agent, and entrepreneurs adjust their capital with a probability lambdaO.
     */

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

    //Agent(entrepreneur) will update their capital to the optimal level
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
        //System.out.println("k: " + k);
    }

    public void updatePayoff(Agent a) {
        int race = a.getRace();
        //native = 1, ethnic = 2
        int occupation = a.getcurOccupation();
        double payoff;
        //Occupation 1 is "Entrepreneur", 2 is "Work in Native Firm", 3 is "Work in  Ethnic Firm", 4 is "unemployed"
        if (race == 1) {
            if (occupation == 1) {
                a.setCurrPayoff(computeEntrepreneurPayoff(a));
            } else if (occupation == 2) {
                a.setCurrPayoff(computeWorkinNativePayoff(a));
            } else if (occupation == 3) {
                a.setCurrPayoff(computeWorkinEthnicPayoff(a));
            } else { // unemployed
                a.setCurrPayoff(computeUnemployedPayoff(a));
            }
        } else {
            if (occupation == 1) {
                a.setCurrPayoff(computeEntrepreneurPayoff(a));
            } else if (occupation == 2) {
                a.setCurrPayoff(computeWorkinNativePayoff(a));
            } else if (occupation == 3) {
                a.setCurrPayoff(computeWorkinEthnicPayoff(a));
            } else { // unemployed
                a.setCurrPayoff(computeUnemployedPayoff(a));
            }
        }
    }

    /**
     * Updates the price of ethnic goods, based on the aggregate supply and demand.
     */
    public void updatePrice() {
        totalS = 0;
        //totalS
        for (Agent a : agentList) {
            updatePayoff(a);
            if (a.getRace() == 2 && a.getcurOccupation() == 1) { //ethnic entrepreneurs
                double agentKI = a.getK();
                double sumPK = 0;
                ArrayList<Agent> employees = a.getEmployees();
                for (Agent employee : employees) sumPK += employee.getPI();
                totalS += Math.pow(agentKI, alpha) * Math.pow(sumPK, 1 - alpha);
                //System.out.println("AgentKI: " + agentKI);
                //System.out.println("Alpha: " + alpha);
                //System.out.println("Aggregate Supply: " + totalS);
            }
        }
        updateDemand();
        //System.out.println("Initial Supply: " + totalS);
        //System.out.println("Initial Demand: " + totalD);
        //System.out.println("Initial Error" + Math.abs(totalS - totalD) / totalS);

        /*
        while (Math.abs(totalS - totalD) / totalS > 0.1) {
            if (totalD > totalS) pE += 100;
            else pE -= 100;
            updateDemand();
         */
            //System.out.println("Supply: " + totalS);
            //System.out.println("Demand: " + totalD);
            //System.out.println("Error: " +Math.abs(totalS-totalD)/totalS);
            //System.out.println("Price: " + pE);
        //}
        //System.out.println("Final Supply: " + totalS);
        //System.out.println("Final Demand: " + totalD);
        //System.out.println("Final Price: " + pE);

        // ### FIXED PRICE
        pE = 10;
    }

    /**
     * Updates the aggregate demand, which would be used to determine the amount of ethnic good that would maximize utility.
     */
    public void updateDemand() {
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

    public double getMinorityShare() {
        return minorityShare;
    }

    public void setMinorityShare(double m) {
        minorityShare = m;
    }

    class getStats1 implements NumericDataSource {
        public double execute() {
            stats1 = new HashMap<String, Double>();
            int entrepreneur = 0;
            int ethnicEntrepreneur = 0;
            int nativeEntrepreneur = 0;
            int nativeWorker = 0;
            int ethnicWorker = 0;
            int unemployed = 0;
            double payoffEE = 0;
            double payoffNE = 0;
            double payoffNW = 0;
            double payoffEW = 0;
            double payoffU = 0;
            double utilityNE = 0;
            double utilityEE = 0;
            double utilityNW = 0;
            double utilityEW = 0;
            double utilityU = 0;
            double entrepreneurCapital=0;
            //double payoffE = 0;
            for(Agent a: agentList){
                int occupation = a.getcurOccupation();
                int race = a.getRace();
                if(occupation == 1){
                    // 1 "Entrepreneur", 2 "Native Worker", 3 "Ethnic Worker", 4 "Unemployed"
                    // 1 "Native" 2 "Ethnic"
                    entrepreneur++;
                    entrepreneurCapital+=a.getK();
                    if(race==1) {
                        nativeEntrepreneur++;
                        payoffNE+=a.getCurrPayoff();
                        utilityNE+=a.getUtility();
                    }
                    else {
                        ethnicEntrepreneur++;
                        System.out.println(a.getUtility());
                        payoffEE+=a.getCurrPayoff();
                        utilityEE+=a.getUtility();
                    }
                }
                else if (occupation == 2) {
                    nativeWorker++;
                    payoffNW+=a.getCurrPayoff();
                    utilityNW+=a.getUtility();
                }
                else if (occupation == 3) {
                    ethnicWorker++;
                    payoffEW+=a.getCurrPayoff();
                    utilityEW+=a.getUtility();
                }
                else {
                    unemployed++;
                    payoffU+=a.getCurrPayoff();
                    utilityU+=a.getUtility();
                }
            }
            stats1.put("percEntrepreneur", (double)entrepreneur/(double) numAgents);
            stats1.put("percNativeEntrepreneur", (double)nativeEntrepreneur/(double)numAgents);
            stats1.put("percEthnicEntrepreneur", (double)ethnicEntrepreneur/(double)numAgents);
            stats1.put("percNativeWorker", (double)nativeWorker/(double)numAgents);
            stats1.put("percEthnicWorker", (double)ethnicWorker/(double)numAgents);
            stats1.put("percUnemployed", (double)unemployed/(double)numAgents);
            stats1.put("averageEntrepreneurCapital", entrepreneurCapital/entrepreneur);
            stats1.put("averagePayoffEE", payoffEE/ethnicEntrepreneur);
            stats1.put("averagePayoffNE", payoffNE/nativeEntrepreneur);
            stats1.put("averagePayoffNW", payoffNW/nativeWorker);
            stats1.put("averagePayoffEW", payoffEW/ethnicWorker);
            stats1.put("averagePayoffU", payoffU/unemployed);
            stats1.put("averageUtilityEE", utilityEE/ethnicEntrepreneur);
            System.out.println("utilityEE: " + utilityEE);
            stats1.put("averageUtilityNE", utilityNE/nativeEntrepreneur);
            stats1.put("averageUtilityNW", utilityNW/nativeWorker);
            stats1.put("averageUtilityEW", utilityEW/ethnicWorker);
            stats1.put("averageUtilityU", utilityU/unemployed);

            return 1;
        }
    }

    class getTotalS implements NumericDataSource {
        public double execute() {
            return totalS;
        }
    }

    class getTotalD implements NumericDataSource {
        public double execute() {
            return totalD;
        }
    }

    class getPrice implements NumericDataSource {
        public double execute() {
            return pE;
        }
    }

    class getAverageWage implements NumericDataSource {
        public double execute() {
            double totalWage = 0;
            for (Agent a : agentList) {
                totalWage += a.getCurrPayoff();
            }
            return totalWage / agentList.size();
        }
    }

    class getPercEntrepreneurs implements NumericDataSource{
        public double execute() {
            return stats1.get("percEntrepreneur");
        }
    }

    class getPercEthnicEntrepreneurs implements NumericDataSource{
        public double execute() {
            return stats1.get("percEthnicEntrepreneur");
        }
    }

    class getPercNativeEntrepreneurs implements NumericDataSource{
        public double execute() {
            return stats1.get("percNativeEntrepreneur");
        }
    }

    class getPercNativeWorkers implements NumericDataSource{
        public double execute() {
            return stats1.get("percNativeWorker");
        }
    }

    class getPercEthnicWorkers implements NumericDataSource{
        public double execute() {
            return stats1.get("percEthnicWorker");
        }
    }

    class getUnemploymentRate implements NumericDataSource{
        public double execute() {
            //updateUnemployment();
            //System.out.println("Unemployment: " + unemployment);
            //return unemployment;
            return stats1.get("percUnemployed");
        }
    }

    class getAverageEntrepreneurCapital implements NumericDataSource{
        public double execute() {
            return stats1.get("averageEntrepreneurCapital");
        }
    }


    class getAveragePayoffEE implements NumericDataSource{
        public double execute(){
            return stats1.get("averagePayoffEE");
        }
    }

    class getAveragePayoffNE implements NumericDataSource{
        public double execute(){
            return stats1.get("averagePayoffNE");
        }
    }

    class getAveragePayoffNW implements NumericDataSource{
        public double execute(){
            return stats1.get("averagePayoffNW");
        }
    }

    class getAveragePayoffEW implements NumericDataSource{
        public double execute(){
            return stats1.get("averagePayoffEW");
        }
    }

    class getAveragePayoffU implements NumericDataSource{
        public double execute(){
            return stats1.get("averagePayoffU");
        }
    }

    class getAverageUtilityEE implements NumericDataSource{
        public double execute(){
            return stats1.get("averageUtilityEE");
        }
    }

    class getAverageUtilityNE implements NumericDataSource{
        public double execute(){
            return stats1.get("averageUtilityNE");
        }
    }

    class getAverageUtilityNW implements NumericDataSource{
        public double execute(){
            return stats1.get("averageUtilityNW");
        }
    }

    class getAverageUtilityEW implements NumericDataSource{
        public double execute(){
            return stats1.get("averageUtilityEW");
        }
    }

    class getAverageUtilityU implements NumericDataSource{
        public double execute(){
            return stats1.get("averageUtilityU");
        }
    }
}