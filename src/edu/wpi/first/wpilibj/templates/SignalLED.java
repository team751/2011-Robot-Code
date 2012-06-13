package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Timer;

/**
 * Manages the LED for signaling
 */
public class SignalLED {
    private DigitalOutput output;
    private Timer timer;
    private DriverStation ds;
    
    public SignalLED(int LEDChannel){
        output = new DigitalOutput(LEDChannel);
        timer = new Timer();
        timer.start();

        ds = DriverStation.getInstance();
    }
    
    public void set(boolean on){
        output.set(on);
    }

    public void loop(SimpleRobot robot){

        if(robot.isDisabled()){
            double fraction = timer.get() % 1;
            if(fraction > .2 && fraction < .25){
                output.set(false);//false = on
            }else{
                output.set(true);//true = off
            }

            //if the robot is connected to the FMS
            if(ds.isFMSAttached()){
                //a second flash each second
                if(fraction > .6 && fraction < .65){
                    output.set(false);
                }else{
                    output.set(true);
                }
            }
        }
        else if(robot.isAutonomous()){
            output.set(false);//turn the LED on
        }
    }
}
