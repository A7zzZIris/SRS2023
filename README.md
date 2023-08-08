# SRS2023
## Ethnic Entrepreneurship, Assimilation and Integration Policy

## Changlog:

<<<<<<< HEAD
=======
Updated params.

I think we should modify Agent so that it directly corresponds to the variables, might make it easier later on when working with the equations.






# Overview of the Method
```
updateOccupationChoice()
updateApplications();
hireProcess();
updateUnemployment();
updateCapital();
updatePrice();
```

## Step1: updateOccupationChoice()
#### Variables that will be updated within the method:
```
agent.SwitchOccupation
```

```public int considerOccupation(Agent agent)```
This method will first compute different payoffs, then use them to compute and compare the utilities, and return the occupation choices that have maximum utility for the given agent.

The functions below will compute agents' different payoffs for different occupational choices.

``` 
computeEntrepreneurPayoff(Agent agent)
computeWorkinNativePayoff(Agent agent)
computeWorkinEthnicPayoff(Agent agent)
computeUnemployedPayoff(Agent agent)
```

The functions below will compute agents' different utilitiess for different occupational choices.

```
computeEntrepreneurUtility(double budget, double pE, Agent agent)
computeWorkinNativeUtility(double budget, double pE, Agent agent)
computeWorkinEthnicUtility(double budget, double pE, Agent agent)
computeUnemployedUtility(double budget, double pE, Agent agent)
```

## Step2.1: updateApplications();
#### Variables that will be updated within the method:
```
agent.applicants
```

The agent will use different methods to search jobs according to the race. The method will return the entrepreneur (agent) that they choose to send the application to.

```
public Agent nativeJobSearch(Agent agent)
public Agent ethnicJobSearch(Agent agent)
```
After finding the potential boss,  updateApplications() method will  update the boss's application list with the code:
```
boss.addApplicants(agent);
```

## Step2.2: hireProcess();
#### Variables that will be updated within the method:
```
# for agents who want to be workers
agent.boss 
agent.curOccupation;

# for agents who want to be entreprenuers
agent.applicants (change to be empty)
agent.employees
```
```hireProcess()```This method iterates through all the agent, update the ``` curOccupation``` for those who want to be entrepreneurs and let them start to hireworker.

```hireWorker(Agent agent)```Entrepreneurs will go through all their applicants and hire them if they can increase their payoff. After all the evaluations, the entrepreneurs' applicants will be clean. The entrepreneurs' employees list will be updated.

```payoff(Agent agent, ArrayList<Agent> workers)```This method will compute the agent's(entrepreneur) payoff with a given list of worker.

## Step3: updateUnemployment();
#### Variables that will be updated within the method:
```
unemployment
```

## Step4: updateCapital();
#### Variables that will be updated within the method:
```
agent.K
```
```updateCapital()```  Iterate through all the agents and with the probability to adjust entrepreneur's capital.

```changeCapital(Agent agent)``` Entrepreneur updated their capital to the optimal level.


## Step5: updatePrice();
#### Variables that will be updated within the method:
```
pE
```
>>>>>>> 2426115dbc93b3732982c75bf7ccfa3348565f6f
