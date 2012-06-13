package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.Timer;

/**
 * Manages the responsiveness of encoders.
 * Create a new object of this class for each encoder.
 * When you set the target for the encoder-controlled PID loop, pass that target into setTarget.
 * Then, in your loop, call shouldMove to see if you should move the motor or if the encoder is not responding.
 * This assumes that the encoder will move more than 2 steps over 1 second in all normal conditions
 * <br /><br />
 * (This class is still in development. It has not been tested.)
 */
public class EncoderWatchdog {

    int targetValue;
    Timer timer;

    /**
     * Constructor
     */
    public EncoderWatchdog(){

    }

    /**
     * Tell the Encoder Watchdog that you are targeting this encoder value
     * This also starts the watchdog timer.
     * @param inTarget the encoder value, in encoder steps (defined by you) to target
     */
    public void setTarget(int inTarget){
        targetValue = inTarget;
        timer.reset();
        timer.start();
    }

    /**
     * Check if the encoder has responded to motion
     * @param encoderValue the encoder value, in encoder steps (also defined by you) that the encoder currently reports
     * @return if the encoder is responding
     */
    public boolean shouldMove(int encoderValue){
        double time = timer.get();
        //expect some encoder movement within 1 second
        if(time > 1){//if the motor has been moving long enough
            if(Math.abs(targetValue - encoderValue) > 2){//expect more than 2 encoder steps over one second
                return true;
            }else{
                return false;//the encoder hasn't responded in a second - something is wrong. Return false
            }

        }else{//if the motor hasn't been moving long enough
            return true;
        }
    }
}
