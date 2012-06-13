package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.Servo;
/**
 * Manages the minibot deployment system
 * @author Sam Crow
 */
public class MinibotDeployer {
   Servo servo;

    /**
     * Create a new MinibotDeployer object
     * @param servoChannel the PWM channel of a Digital Sidecar connected to slot 4 that the servo is connected to
     */
    public MinibotDeployer(int servoChannel){
        servo = new Servo(servoChannel);
        servo.set(0);
    }
    /**
     * Move the servo to unlatch the latch and deploy the minibot
     */
    public void deploy(){
        servo.set(1);
    }
    /**
     * Move the servo to latch the latch
     */
    public void reset(){
        servo.set(0);
    }
}
