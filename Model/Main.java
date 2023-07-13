package SRS2023.Model;

import java.util.ArrayList;

import uchicago.src.sim.analysis.DataRecorder;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.space.Object2DGrid;

public class Main extends SimModelImpl {
    private ArrayList<Agent> agentList;
    private Schedule schedule;
    private Object2DGrid Grid;
    private int gridWidth;
    private int gridHeight;
    private int agentNumber;
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
        agentNumber = 100;
        period = 1000;
        gridWidth = 40;
        gridHeight = 40;
        schedule = new Schedule(1);


    }

    public void buildModel() {
        Grid = new Object2DGrid(gridWidth, gridHeight);

        for (int id = 0; id < agentNumber; id++) {
            Agent ag = new Agent();
            ag.setID(id);

            //Race 1 refers to majority and 2 refers to minority
            int race = (int) (Math.random() * 2) + 1;
            ag.setRace(race);
            if (race == 1) {
                int randomX = (int) (Math.random() * gridWidth); // random X
                int randomY = (int) (Math.random() * gridHeight); // random Y
                while (Grid.getObjectAt(randomX, randomY) != null) {
                    randomX = (int) (Math.random() * gridWidth);
                    randomY = (int) (Math.random() * gridHeight);
                }

                Grid.putObjectAt(randomX, randomY, ag);
                ag.setCoordinate(new int[]{randomX, randomY});
            } else {
                //ethnic minorities will be located in the lower third of the lattice.
                int min = (int) (gridHeight * (1.0 / 3.0));
                int max = gridHeight;
                int randomY = (int) (Math.random() * (max - min + 1)) + min;
                int randomX = (int) (Math.random() * gridWidth);

                while (Grid.getObjectAt(randomX, randomY) != null) {
                    randomY = (int) (Math.random() * (max - min + 1)) + min;
                    randomX = (int) (Math.random() * gridWidth);
                }
                Grid.putObjectAt(randomX, randomY, ag);
                ag.setCoordinate(new int[]{randomX, randomY});
            }
            agentList.add(ag);


        }
    }
}
