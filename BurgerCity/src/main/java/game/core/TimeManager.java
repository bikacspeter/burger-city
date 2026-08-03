package game.core;


public class TimeManager {

    public enum TimeSpeed {
        PAUSED(0.0, "⏸ Szünet"),
        NORMAL(1.0, "▶ Normál"),
        FAST(2.0, "▶▶ Gyors (2x)"),
        VERY_FAST(4.0, "▶▶▶ Nagyon gyors (4x)");

        private final double multiplier;
        private final String displayName;

        TimeSpeed(double multiplier, String displayName) {
            this.multiplier = multiplier;
            this.displayName = displayName;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private TimeSpeed currentSpeed;
    private long totalTicks;
    private double accumulatedGameTime; 

    public TimeManager() {
        this.currentSpeed = TimeSpeed.NORMAL;
        this.totalTicks = 0;
        this.accumulatedGameTime = 0.0;
    }

    


    public void restore(TimeSpeed speed, long totalTicks, double accumulatedGameTimeSeconds) {
        if (speed == null) speed = TimeSpeed.NORMAL;
        this.currentSpeed = speed;
        this.totalTicks = Math.max(0, totalTicks);
        this.accumulatedGameTime = Math.max(0.0, accumulatedGameTimeSeconds);
    }

    
    public double update(double realDeltaSeconds) {
        double gameDelta = realDeltaSeconds * currentSpeed.getMultiplier();
        accumulatedGameTime += gameDelta;
        if (currentSpeed != TimeSpeed.PAUSED) {
            totalTicks++;
        }
        return gameDelta;
    }

    public void setSpeed(TimeSpeed speed) {
        this.currentSpeed = speed;
    }

    public TimeSpeed getCurrentSpeed() {
        return currentSpeed;
    }

    public void pause() {
        setSpeed(TimeSpeed.PAUSED);
    }

    public void setNormal() {
        setSpeed(TimeSpeed.NORMAL);
    }

    public void setFast() {
        setSpeed(TimeSpeed.FAST);
    }

    public void setVeryFast() {
        setSpeed(TimeSpeed.VERY_FAST);
    }

    public boolean isPaused() {
        return currentSpeed == TimeSpeed.PAUSED;
    }

    


    public long getTotalTicks() {
        return totalTicks;
    }

    


    public double getGameTimeSeconds() {
        return accumulatedGameTime;
    }

    



    public String getFormattedGameTime() {
        int totalGameSeconds = (int) accumulatedGameTime;
        int secondsPerDay = 120; 
        int days = totalGameSeconds / secondsPerDay;
        int remainingSeconds = totalGameSeconds % secondsPerDay;
        int hours = (remainingSeconds * 24) / secondsPerDay;
        
        return String.format("Nap %d, Óra %d", days, hours);
    }
}
