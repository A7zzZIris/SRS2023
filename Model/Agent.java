package Model;
import java.util.ArrayList;

public class Agent {
    private int id;
    private int race;
    private int[] coordinate;
    private int cur_occupation;
    private int swicth_occupation;
    private Agent boss;
    private ArrayList<Agent>employees = new ArrayList<>();
    
    


    private double x_i;  //Entrepreneurial spirit/ability
    private double c_i; //Cost of assimilation
    private double p_i; //Productivity as a worker
    private double b_i; //Wealth used to be starting a business


    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public void setRace(int race) {
        this.race = race;

    }

    public int getRace() {
        return race;

    }

    public void setCoordinate(int[] coordinate) {
        this.coordinate = coordinate;
    }

    public int[] getCoordinate() {
        return coordinate;
    }

    public void setcurOccupation(int cur_occupation) {
        this.cur_occupation = cur_occupation;
    }

    public int getcurtOccupation() {
        return cur_occupation;
    }
    public void setswitchOccupation(int swicth_occupation) {
        this.swicth_occupation = swicth_occupation;
    }

    public int getswitchOccupation() {
        return swicth_occupation;
    }


    public void setEntrepreneurialAbility(double ability) {
        this.x_i = ability;
    }

    public double getEntrepreneurialAbility() {
        return x_i;
    }

    public double getCostofAssimulation() {
        return c_i;
    }

    public void setCostofAssimulation(double cost) {
        this.c_i = cost;
    }

    public double getProductivity() {
        return p_i;
    }

    public void setProductivity(double productivity) {
        this.p_i = productivity;
    }

    public double getWealthToBusiness() {
        return b_i;
    }

    public void setWealthToBusiness(double wealth) {
        this.b_i = wealth;
    }
    
    public void setBoss(Agent boss) {
        this.boss = boss;
    }

    public Agent getBoss() {
        return boss;
    }
    public void addEmployee(Agent employee) {
        this.employees.add(employee);
    }

    public ArrayList<Agent> getEmployees() {
        return employees;
    }


}

