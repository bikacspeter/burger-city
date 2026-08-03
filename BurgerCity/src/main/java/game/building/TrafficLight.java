package game.building;

import game.save.GameSnapshot;

public class TrafficLight extends Building {

    public static final int COST = 300;

    
    private String currentState;
    private double timeInCurrentState; 
    private double greenDurationMain;   
    private double greenDurationCross;  

    public TrafficLight(int x, int y) {
        super("Forgalmi lámpa", COST, x, y);
        this.currentState = "MAIN_GREEN";
        this.timeInCurrentState = 0.0;
        this.greenDurationMain = 5.0;
        this.greenDurationCross = 5.0;
    }

    



    public void update(double deltaSeconds) {
        if (deltaSeconds <= 0) return;

        timeInCurrentState += deltaSeconds;

        
        if (currentState.equals("MAIN_GREEN") && timeInCurrentState >= greenDurationMain) {
            switchToState("CROSS_GREEN");
        } else if (currentState.equals("CROSS_GREEN") && timeInCurrentState >= greenDurationCross) {
            switchToState("MAIN_GREEN");
        }
    }

    private void switchToState(String newState) {
        this.currentState = newState;
        this.timeInCurrentState = 0.0;
    }

    




    public void setDurations(double main, double cross) {
        this.greenDurationMain = Math.max(1.0, main);  
        this.greenDurationCross = Math.max(1.0, cross);
    }

    



    public String getCurrentState() {
        return currentState;
    }

    




    public boolean isGreen(int direction) {
        if (direction == 0) return true; 

        
        
        if (currentState.equals("MAIN_GREEN")) {
            return direction == 1 || direction == 3;
        } else if (currentState.equals("CROSS_GREEN")) {
            return direction == 2 || direction == 4;
        }
        return false;
    }

    public double getGreenDurationMain() {
        return greenDurationMain;
    }

    public double getGreenDurationCross() {
        return greenDurationCross;
    }

    public double getTimeInCurrentState() {
        return timeInCurrentState;
    }

    


    public void switchLight() {
        if (currentState.equals("MAIN_GREEN")) {
            switchToState("CROSS_GREEN");
        } else {
            switchToState("MAIN_GREEN");
        }
    }

    public GameSnapshot.TrafficLightData exportSaveData() {
        return new GameSnapshot.TrafficLightData(
                getX(),
                getY(),
                getCurrentState(),
                getTimeInCurrentState(),
                getGreenDurationMain(),
                getGreenDurationCross()
        );
    }

    


    public void restore(String state, double timeInState, double mainDuration, double crossDuration) {
        setDurations(mainDuration, crossDuration);
        if (state == null) state = "MAIN_GREEN";
        if (!state.equals("MAIN_GREEN") && !state.equals("CROSS_GREEN")) {
            state = "MAIN_GREEN";
        }
        this.currentState = state;
        this.timeInCurrentState = Math.max(0.0, timeInState);
    }
}