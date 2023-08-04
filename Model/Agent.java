package SRS2023.Model;
//package Model;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;

import java.awt.Color;
import java.util.ArrayList;

public class Agent implements Drawable {
    private int id;
    private int race;
    private int[] coords;
    private int cur_occupation;
    private int switch_occupation;
    private Agent boss; //?
    private ArrayList<Agent> employees = new ArrayList<>();
    private ArrayList<Agent> applicants = new ArrayList<>();

    private double xI; // entrepreneurial spirit/ability
    private double cI; // cost of assimilation
    private double pI; // productivity
    private double bI; // wealth to start the business
    private double K; // current capital

    private int cE; // amount for ethnic good
    private int cG; // amount for general good

    private double currPayoff;

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

    public int getcurOccupation() {
        return cur_occupation;
    }

    public void setSwitchOccupation(int switch_occupation) {
        this.switch_occupation = switch_occupation;
    }

    public int getSwitchOccupation() {
        return switch_occupation;
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

    public double getK() {
        return K;
    }

    public void setK(double K) {
        this.K = K;
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

    public void addApplicants(Agent applicant) {
        this.applicants.add(applicant);
    }

    public ArrayList<Agent> getApplicants() {
        return applicants;
    }

    public void cleanApplicants() {
        applicants.clear();
    }

    public void removeEmployee(Agent agent) {
        employees.remove(agent);
    }

    public int getdemandE() {
        return cE;
    }

    public void setdemandE(int cE) {
        this.cE = cE;
    }

    public int getdemandG() {
        return cG;
    }

    public void setdemandG(int cG) {
        this.cG = cG;
    }

    public double getCurrPayoff() {return this.currPayoff;}

    public void setCurrPayoff(double p) {this.currPayoff = p;}


    @Override
    public void draw(SimGraphics simGraphics) {
        //key: different shades to signify native/ethnic, entrepreneur, employed, unemployed
        if (this.race == 1) {//native
            //Occupation 1 is "Entrepreneur", 2 is "Work in Native Firm", 3 is "Work in  Ethnic Firm", 4 is "unemployed"
            if (this.cur_occupation == 1) {//entrepreneur
                //simGraphics.drawFastRoundRect(new Color());
            }
            simGraphics.drawFastRoundRect(Color.RED);
        } else {//ethnic
            if (this.cur_occupation == 1) {

            } else if (this.cur_occupation == 2) {

            }
            simGraphics.drawFastRoundRect(Color.BLUE);
        }
    }

    @Override
    public int getX() {
        return this.coords[0];
    }

    @Override
    public int getY() {
        return this.coords[1];
    }
}