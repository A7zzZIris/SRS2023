package SRS2023.Model;
//package Model;
import java.util.ArrayList;

public class Agent {
    private int id;
    private int race;
    private int[] coords;
    private int cur_occupation;
    private int swicth_occupation;
    private Agent boss; //?
    private ArrayList<Agent> employees = new ArrayList<>();

    public double xI; // entrepreneurial spirit/ability
    public double cI; // cost of assimilation
    public double pI; // productivity
    public double bI; // capital

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

    public void setCoords(int[] coords) {
        this.coords = coords;
    }

    public int[] getCoords() {
        return coords;
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


    public double getXI() {
        return xI;
    }

    public void setXI(double xI) {
        this.xI = xI;
    }

    public double getCI() {
        return cI;
    }

    public void setCI(double cI) {
        this.cI = cI;
    }

    public double getPI() {
        return pI;
    }

    public void setPI(double pI) {
        this.pI = pI;
    }

    public double getBI() {
        return bI;
    }

    public void setBI(double bI) {
        this.bI = bI;
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

