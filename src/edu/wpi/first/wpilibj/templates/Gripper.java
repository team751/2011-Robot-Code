package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Jaguar;

/**
 * Manages the gripper, its motor controllers, and its encoders
 * @author Sam
 */
public class Gripper {

    DriverStation ds;

    private static final double ERROR_CONVERSION = 150;

    private Encoder topEncoder;
    private Encoder bottomEncoder;

    private Jaguar topMotor;
    private Jaguar bottomMotor;

    //rotation targets
    private int topTarget;
    private int bottomTarget;

    DigitalInput limitSwitch;

    /**
     * If the robot is in the process of accepting a game piece
     */
    private boolean isAccepting = false;

    /**
     * Constructor
     * @param topJaguarChannel The PWM channel for the Jaguar for the top motor
     * @param bottomJaguarChannel The PWM channel for the Jaguar for the bottom motor;
     * @param topEncoderA The Digital I/O channel for channel A of the encoder for the top motor
     * @param topEncoderB The Digital I/O channel for channel B of the encoder for the top motor
     * @param bottomEncoderA The Digital I/O channel for channel A of the encoder for the bottom motor
     * @param bottomEncoderB The Digital I/O channel for channel B of the encoder for the bottom motor
     * @param limitSwitchChannel The Digital I/O channel for the gripper limit switch
     * @param signalLightChannel the Digital I/O channel for the signal light
     */
    public Gripper(int topJaguarChannel, int bottomJaguarChannel, int topEncoderA, int topEncoderB, int bottomEncoderA, int bottomEncoderB, int limitSwitchChannel){
        topMotor = new Jaguar(topJaguarChannel);
        bottomMotor = new Jaguar(bottomJaguarChannel);

        topEncoder = new Encoder(topEncoderA, topEncoderB);
        topEncoder.start();
        topEncoder.reset();
        bottomEncoder = new Encoder(bottomEncoderA, bottomEncoderB);
        bottomEncoder.start();
        bottomEncoder.reset();

        topTarget = 0;
        bottomTarget = 0;

        limitSwitch = new DigitalInput(limitSwitchChannel);


        ds = DriverStation.getInstance();
    }
    
    /**
     * Handles periodic operations. Should be called periodically.
     */
    public void loop(){


        ds.setDigitalOut(8, limitSwitch.get());

        final int tolerance = 10;//the number of degrees, in either direction, of tolerance for motor movement

        if(!isAccepting){
            if(topTarget > topEncoder.get() && Math.abs(topTarget - topEncoder.get()) > tolerance){
                int error = Math.abs(topTarget - topEncoder.get());

                topMotor.set(-limit(error / ERROR_CONVERSION));
                //SmartDashboard.log("Backwards", "Gripper Top Motor");
            }
            else if(topTarget < topEncoder.get() && Math.abs(topTarget - topEncoder.get()) > tolerance){
                double error = Math.abs(topTarget - topEncoder.get());

                topMotor.set(limit(error / ERROR_CONVERSION));
                //SmartDashboard.log("Forward", "Gripper Top Motor");
            }else{
                topMotor.set(0);
            }

            if(bottomTarget > bottomEncoder.get() && Math.abs(bottomTarget - bottomEncoder.get()) > tolerance){
                double error = Math.abs(bottomTarget - bottomEncoder.get());
                bottomMotor.set(-limit(error / ERROR_CONVERSION));
                //SmartDashboard.log("Backwards", "Gripper Bottom Motor");
            }
            else if(bottomTarget < bottomEncoder.get() && Math.abs(bottomTarget - bottomEncoder.get()) > tolerance){
                double error = Math.abs(bottomTarget - bottomEncoder.get());
                bottomMotor.set(limit(error / ERROR_CONVERSION));
                //SmartDashboard.log("Forward", "Gripper Bottom Motor");
            }
            else{
                bottomMotor.set(0);
                //SmartDashboard.log("Stopped", "Gripper Bottom Motor");
            }
        }

        //if the gripper is in accept mode, it needs to stop when the limit switch is pressed
        if(isAccepting && !limitSwitch.get()){
            //and also by simply stopping them
            topMotor.set(0);
            bottomMotor.set(0);
            isAccepting = false;
            //stop the motors by making the target positions equal to the current positions
            topTarget = topEncoder.get();
            bottomTarget = bottomEncoder.get();
        }else if(isAccepting && limitSwitch.get()){
            //if it is accepting and the limit switch is not pressed
            topMotor.set(1);
            bottomMotor.set(-1);
        }

                
    }

    /**
     * Tilt the possessed game piece a specified amount
     * @param degrees the number of degrees, with negative tilting the game piece up, to tilt the game piece
     */
    public void tilt(int degrees){
        if(!isAccepting){
            topTarget += degrees;
            bottomTarget += degrees;
            //System.out.println("Tilting a game piece "+degrees+" degrees. The new targets: top: "+topTarget+", bottom "+bottomTarget);
        }
    }

    /**
     * Accepts a game piece.
     */

    //change for last year's robot: changed += to -= in the two next methods
    public void accept(){
        isAccepting = true;
        //set the targets to absurdly large amounts to pull the game piece in
        //The loop will reset these to the current actual values when the limit switch is pressed
        topTarget -= 999999999;
        bottomTarget += 999999999;
    }
    /**
     * Ejects the possessed game piece by turning both sets of wheels 180 degrees
     */
    public void eject(){
        if(!isAccepting){
            topTarget += 5;
            bottomTarget -= 5;
        }
    }

    /**
     * Synchronous eject function.
     */
    public void onceEject(){
        topTarget += 180;
        bottomTarget -= 180;
    }

    /**
     * Limit a given value to +1 to -1.
     * Also ensures that it is greater than the bottom limit, which is defined internally
     * @param input the value to limit
     * @return the value, limited to between 1 and -1
     */
    private double limit(double input){
        final double lowLimit = 0.2;
        if(input > 1){
            return 1;
        }
        if(input < -1){
            return -1;
        }
        else {
            if(Math.abs(input) < lowLimit){
                return lowLimit;
            }else{
                return input;
            }
        }
    }

    /**
     * sends logging data to the dashboard
     */
    public void log(){
       
    }
}
