package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.RobotDrive;

/**
 * Manages line tracking. This was intended as a new, more intelligent way
 * to follow a line. It is currently unfinished.
 * @author Sam Crow
 */
public class LineTracker {

    DigitalInput leftLineSensor;
    DigitalInput midLineSensor;
    DigitalInput rightLineSensor;

    /**
     * The number of tracker states to store in history
     */
    private static final int STATE_HISTORY_COUNT = 100;
    public LineTrackerStatus[] pastStates;

    public LineTrackerStatus currentState;

    public double motorPower = 0.5;

    public LineTracker(int leftLineSensorChannel, int midLineSensorChannel, int rightLineSensorChannel){
        leftLineSensor = new DigitalInput(leftLineSensorChannel);
        midLineSensor = new DigitalInput(midLineSensorChannel);
        rightLineSensor = new DigitalInput(rightLineSensorChannel);
    }

    /**
     * Drive following a line.
     * Accounts for lots of stuff.
     * Takes control of the given RobotDrive, so don't give it conflicting drive commands.
     * @param drive
     */
    public void drive(RobotDrive drive){
        currentState.left = !leftLineSensor.get();
        currentState.mid = !midLineSensor.get();
        currentState.right = !rightLineSensor.get();

        //update the state history
        for(int i = 0; i < pastStates.length; i++){
            pastStates[i] = pastStates[i+1];//make each index equal to the value of the next index
            //So it would change it from {1, 2, 5, 6, 4} to {2, 5, 6, 4, 4}
            //This discards the value of the first element.
            //This also makes the last two elements equal. This is fixed in the next statement
        }
        pastStates[STATE_HISTORY_COUNT] = currentState;

        double turnAmount = 0.8;//amount (0-1, 1=full) of turn to use when following lines

        //System.out.println("Following a line. Power="+motorPower+", Turn="+turnAmount);
        motorPower = -motorPower;//invert the motor power. A negative value into RobotDrive::drive makes the robot go forward.
        boolean leftSensorReading = leftLineSensor.get();
        boolean midSensorReading = midLineSensor.get();
        boolean rightSensorReading = rightLineSensor.get();


       if ((leftSensorReading && rightSensorReading) && !midSensorReading) {
            //side sensors detect darkenss, middle sensor detects lightness
            //the robot seems to be on a line
            //System.out.println("The robot is on a line. Go straight.");
            drive.arcadeDrive(motorPower, 0.0, true);//go straight at full line speed
        }
        else if(!rightSensorReading && !midSensorReading){
            //right sensor and middle sensor detect light - it's slightly left of the line
            //System.out.println("The right sensor detects lightness (the line). Turn right.");
            drive.arcadeDrive(motorPower, turnAmount / 2, true);//turn right at half the usual magnitude
        }
        else if(!leftSensorReading && !midSensorReading){
            //left sensor and middle sensor detect light - it's slightly right of the line
            //System.out.println("The left sensor detects lightness (the line). Turn left.");
            drive.arcadeDrive(motorPower, -turnAmount / 2, true);//turn left at half the usual magnitude
        }
        else if(!rightSensorReading){
            //right sensor detects light. It's left of the line
            drive.arcadeDrive(motorPower, turnAmount, true);//turn right
        }
        else if(!leftSensorReading){
            //left sensor detects light. It's right of the line
            drive.arcadeDrive(motorPower, -turnAmount, true);//turn left
        }
        if(!leftSensorReading && !midSensorReading && !rightSensorReading){
            drive.arcadeDrive(0, 0, true);
        }
        else {
            //System.out.println("Sensors detect something else.");
            //proceed with caution
            drive.arcadeDrive(motorPower, 0.0, true);//go straight at half line speed
        }

    }

    /**
     * Stop the drive motors
     * @param drive the RobotDrive object that you desire to stop
     */
    public void stop(RobotDrive drive){
        drive.drive(0, 0);
    }

    private static final int TURN_LEFT = -1;
    private static final int TURN_RIGHT = 1;
    private static final int NO_LINE_HISTORY = 0;

    /**
     * Find if the robot should turn left or right to get to the line, or if no line has been detected yet
     * @return either {@link TURN_LEFT}, {@link TURN_RIGHT}, or {@link NO_LINE_HISTORY}.
     */
    private int turnDirection(){
        //iterate through the history
        for(int i = 0; i < pastStates.length; i++){
            if(pastStates[i].left || pastStates[i].mid || pastStates[i].right){
                //if a line was detected in any of the past states
            }
        }

        return NO_LINE_HISTORY;
    }
}
