package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Relay.Value;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.DigitalInput;
/*
 * Important encoder information: The return value from Encoder.get(): 1 unit = 1 step.
 * For the US Digital rotary encoders, this is one degree.
 *  (only the 360-count ones. These are the ones that we are using.)
 * For other encoders, it is different.
 */
/*
 * If an encoder becomes unresponsive, the forklift will continue moving forever.
 * (at least until it reaches the top or bottom)
 * As we saw in qualification match 18 in Seattle, this can be bad.
 */

/**
 * This class manages the forklift, including the relays, encoders, and limit switches.
 * @author Sam
 */
public class Forklift {

    private static final boolean USE_ENCODER_WATCHDOG = false;

    Encoder encoder;
    Relay relay1;
    Relay relay2;
    /*
     * This digital input returns true when the switch is not pressed
     * and false when the switch is pressed
     */
    DigitalInput bottomLimitSwitch;
    DigitalInput topLimitSwitch;
    
    /**
     * the height enumeration that the forklift is currently moving to (or is currently at)
     */
    public ForkliftHeight targetHeight;

    private EncoderWatchdog watchdog;

    //enumerations representing heights of the forklift
    //The numbers of degrees are currently arbitrary and will be changed.
    //What fixed heights will we actually be using?
    /**
     * Enumeration representing the lowest that the forklift can go
     */
    public static final ForkliftHeight BOTTOM_LIMIT_HEIGHT = new ForkliftHeight(0);

    /**
     * Enumeration representing a good height to use to place a game piece on a scoring peg in the bottom row
     * (not one of the center pegs that is slightly higher)
     */
    public static final ForkliftHeight BOTTOM_PEG_HEIGHT = new ForkliftHeight(0);//will this be used?
    /**
     * Enumeration representing a good height to use to place a game piece on a scoring peg in the bottom row
     * (a center peg that is slightly higher)
     */
    public static final ForkliftHeight BOTTOM_CENTER_PEG_HEIGHT = new ForkliftHeight(1466);//will this be used?
    /**
     * Enumeration representing a good height to use to place a game piece on a scoring peg in the middle row
     * (not one of the center pegs that is slightly higher)
     */
    public static final ForkliftHeight MID_PEG_HEIGHT = new ForkliftHeight(1080);
    /**
     * Enumeration representing a good height to use to place a game piece on a scoring peg in the middle row
     * (a center peg that is slightly higher)
     */
    public static final ForkliftHeight MID_CENTER_PEG_HEIGHT = new ForkliftHeight(1440);
    /**
     * Enumeration representing a good height to use to place a game piece on a scoring peg in the top row
     * (not one of the center pegs that is slightly higher)
     */
    public static final ForkliftHeight TOP_PEG_HEIGHT = new ForkliftHeight(12685);
    /**
     * Enumeration representing a good height to use to place a game piece on a scoring peg in the top row
     * (a center peg that is slightly higher)
     */
    public static final ForkliftHeight TOP_CENTER_PEG_HEIGHT = new ForkliftHeight(12685);
    //misc. height definitions
    /**
     * The highest that the forklift can go while remaining within the 60" starting configuration height limit
     */
    public static final ForkliftHeight STARTING_LIMIT_HEIGHT = new ForkliftHeight(2520);
    /**
     * The absolute highest that the forklift can go
     */
    public static final ForkliftHeight TOP_LIMIT_HEIGHT = new ForkliftHeight(2880);
    
    /**
     * A good height to get a game piece from a feeder station
     */
    public static final ForkliftHeight INTAKE_HEIGHT = new ForkliftHeight(800);

    //custom relay direction definitions
    public static final Value RELAY_UP = Value.kReverse;
    public static final Value RELAY_DOWN = Value.kForward;
    public static final Value RELAY_STOP = Value.kOff;

    //mode definitions
    private int mode;
    private static final int WAITING = 0;
    private static final int TOP_LIMIT_SWITCH = 1;
    private static final int BOTTOM_LIMIT_SWITCH = 2;
    private static final int ENCODER = 3;

    /**
     * Constructor
     * @param relayChannel1 the relay channel on the digital sidecar that relay/spike 1 is connected to
     * @param relayChannel2 the relay channel that relay/spike 2 is connected to
     * @param encoderChannelA the digital I/O channel that the A encoder input is connected to
     * @param encoderChannelB the digital I/O channel that the B encoder input is connected to
     * @param topLimitSwitchChannel the digital I/O channel that the top limit switch is connected to
     * @param bottomLimitSwitchChannel the digital I/O channel that the bottom limit switch is connected to
     */
    public Forklift(int relayChannel1, int relayChannel2, int encoderChannelA, int encoderChannelB, int topLimitSwitchChannel, int bottomLimitSwitchChannel){
        relay1 = new Relay(relayChannel1);
        relay2 = new Relay(relayChannel2);
        bottomLimitSwitch = new DigitalInput(bottomLimitSwitchChannel);
        topLimitSwitch = new DigitalInput(topLimitSwitchChannel);
        encoder = new Encoder(encoderChannelA, encoderChannelB);
        encoder.start();//start counting encoder pulses
        encoder.reset();//set the encoder value to 0

        watchdog = new EncoderWatchdog();

        mode = WAITING;
    }

    /**
     * handles looping forklift actions. Should be called periodically when enabled.
     */
    public void loop(){
        final int tolerance = 80;//number of steps in either direction of tolerance
        switch(mode){
            case WAITING:
                //initially: do nothing
                break;
            case TOP_LIMIT_SWITCH:
                //go up unless the top limit switch is pressed
                if(topLimitSwitch.get()){
                    relay1.set(RELAY_UP);
                    relay2.set(RELAY_UP);
                }else{
                    relay1.set(RELAY_STOP);
                    relay2.set(RELAY_STOP);
                }
                break;
            case BOTTOM_LIMIT_SWITCH:
                //go down unless the bottom limit switch is pressed
                if(bottomLimitSwitch.get()){
                    relay1.set(RELAY_DOWN);
                    relay2.set(RELAY_DOWN);
                }else{
                    relay1.set(RELAY_STOP);
                    relay2.set(RELAY_STOP);
                }
                break;
            case ENCODER:
                //target the encoder position
                //check if the actual position differs from the target position significantly
                if(Math.abs(targetHeight.degrees - encoder.get()) > tolerance){
                    if(targetHeight.degrees > encoder.get()){
                        if(topLimitSwitch.get()){//ensure that the limit switch is not pressed
                            relay1.set(RELAY_UP);
                            relay2.set(RELAY_UP);
                        }
                    }else if(targetHeight.degrees < encoder.get()){
                        if(bottomLimitSwitch.get()){
                            relay1.set(RELAY_DOWN);
                            relay2.set(RELAY_DOWN);
                        }
                    }
                }else{//target and actual positions not significantly different
                    //stop the motors
                    relay1.set(RELAY_STOP);
                    relay2.set(RELAY_STOP);
                }
                break;
        }
        cleanup();//clean stuff up
    }
    /**
     * Tell the forklift class to move asynchronously to the position specified in <code>height</code>.
     * The forklift will only actually move to this position if the <code>loop</code> method is called periodically.
     * @param height the {@link ForkliftHeight} object that the forklift targets
     */
    public void setHeight(ForkliftHeight height){
        targetHeight = height;
        mode = ENCODER;
        //System.out.println("Asynchronously setting the forklfit to a hieght of "+height.degrees+" degrees.");
    }

    public void setTop(){
        mode = TOP_LIMIT_SWITCH;
        //System.out.println("Asynchronously setting the forklift to target the top limit switch");
    }

    public void setBottom(){
        mode = BOTTOM_LIMIT_SWITCH;
        //System.out.println("Asynchronously setting the forklift to target the bottom limit switch");
    }

    public void setManual(Value relayValue){
        if(relayValue == RELAY_UP){
            //System.out.println("Manually moving the forklift up");
        }else if(relayValue == RELAY_DOWN){
            //System.out.println("Manually moving the forklift down");
        }else if(relayValue == RELAY_STOP){
            //System.out.println("Manually stopping the forklift");
        }
        mode = WAITING;//set this to WAITING so that the forklift won't do anything else in the loop() method until it is set to another target
        //check limit switches
        if(relayValue == RELAY_UP){
            //operator wants it to go up
           if(topLimitSwitch.get()){//if the switch isn't pressed
                relay1.set(relayValue);
                relay2.set(relayValue);
            }else{
                //stop the motors
                relay1.set(RELAY_STOP);
                relay2.set(RELAY_STOP);
            }
        }else if(relayValue == RELAY_DOWN){
            //operator wants it to go down
            if(bottomLimitSwitch.get()){//if the bottom limit switch isn't pressed
                relay1.set(relayValue);
                relay2.set(relayValue);
            }else{
                //stop the motors
                relay1.set(RELAY_STOP);
                relay2.set(RELAY_STOP);
            }
        }else{
            //stop the motors
            relay1.set(RELAY_STOP);
            relay2.set(RELAY_STOP);
        }
    }

    /**
     * Clean up the system values.
     * If the forklift is at the bottom position, reset the encoder.
     */
    private void cleanup(){
        switch(mode){
            case BOTTOM_LIMIT_SWITCH:
                if(!bottomLimitSwitch.get()){//if it is targeting the bottom limit switch and it is pressed
                    mode = WAITING;
                    encoder.reset();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Create a String representing the current operating mode of this forklift
     * @return a string representing that
     */
    private String modeDebugString(){
        String retval;
        switch(mode){
            case WAITING:
                retval = "waiting";
                break;
            case TOP_LIMIT_SWITCH:
                retval = "top limit switch";
                break;
            case BOTTOM_LIMIT_SWITCH:
                retval = "bottom limit switch";
                break;
            case ENCODER:
                retval = "encoder, target "+targetHeight.degrees+" actual "+encoder.get();
                break;
            default:
                retval = "other";
                break;
        }
        return retval;
    }

    /**
     * Sends logging data to the dashboard
     */
    public void log(){
        //System.out.println("Mode: "+modeDebugString()+" Top limit switch: "+topLimitSwitch.get()+" Bottom limit switch:"+bottomLimitSwitch.get());
    }
}
