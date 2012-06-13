package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.Servo;

/**
 * manages the camera pan/tilt bracket thing.
 * @author Sam Crow
 */
public class CameraPanTilt {
    Servo panServo;
    Servo tiltServo;

    /**
     * The default value (0-1) to set the tilt servo to. This makes the camera point roughly forward.
     */
    public static final double SERVO_TILT_DEFAULT = .3;
    /**
     * The default value (0-1) to set the pan servo to. This makes the camera roughly level.
     */
    public static final double SERVO_PAN_DEFAULT = .56;
    /**
     * Constructor. Assumes a digital sidecar connected to an NI 9403 in slot 4 of the cRIO
     * @param panServoChannel The PWM channel that the pan servo is connected to
     * @param tiltServoChannel The PWM channel that the tilt servo is connected to
     */
    public CameraPanTilt(int panServoChannel, int tiltServoChannel){
        panServo = new Servo(panServoChannel);
        tiltServo = new Servo(tiltServoChannel);

        //set them to the default position
        panServo.set(SERVO_PAN_DEFAULT);
        tiltServo.set(SERVO_TILT_DEFAULT);
    }
    /**
     * set the servos to the specified values
     * @param pan ratio 0-1 where 0 is left and 1 is right to pan the camera to
     * @param tilt ratio 0-1 to tilt the camera to
     */
    public void set(double pan, double tilt){
        panServo.set(pan);
        tiltServo.set(tilt);
    }
    /**
     * move the camera to the default position
     */
    public void setDefaults(){
        panServo.set(SERVO_PAN_DEFAULT);
        tiltServo.set(SERVO_TILT_DEFAULT);
    }
}
