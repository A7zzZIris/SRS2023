package SRS2023.Model;

public class Agent {
    private int id;
    private int race;
    private int[] coordinate;
    private String cur_occupation;
    private String swicth_occupation;


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

    public void setcurOccupation(String cur_occupation) {
        this.cur_occupation = cur_occupation;
    }

    public String getcurtOccupation() {
        return cur_occupation;
    }
    public void setswitchOccupation(String swicth_occupation) {
        this.swicth_occupation = swicth_occupation;
    }

    public String getswitchcurtOccupation() {
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


}

