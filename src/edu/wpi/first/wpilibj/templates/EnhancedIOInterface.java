package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationEnhancedIO;
import edu.wpi.first.wpilibj.DriverStationEnhancedIO.EnhancedIOException;
/**
 * Provides abstraction between the robot program and the driver station I/O
 */
public class EnhancedIOInterface {
    DriverStationEnhancedIO io;

    public EnhancedIOInterface(){
        io = DriverStation.getInstance().getEnhancedIO();
    }

    /**
     * Get the value from the top center button
     * @return if the top center button is pressed
     */
    public boolean topCenter(){
        try {
            return !io.getDigital(4);
        } catch (EnhancedIOException ex) {
            //.printStackTrace();
            return false;
        }
    }

    /**
     * Get the value from the top side button
     * @return if the top side button is pressed
     */
    public boolean topSide(){
        try {
            return !io.getDigital(2);
        } catch (EnhancedIOException ex) {
            //ex.printStackTrace();
            return false;
        }
    }

    /**
     * Get the value from the middle center button
     * @return if the middle center button is pressed
     */
    public boolean midCenter(){
        try {
            return !io.getDigital(5);
        } catch (EnhancedIOException ex) {
            //ex.printStackTrace();
            return false;
        }
    }

    /**
     * Get the value from the middle side button
     * @return if the middle side button is pressed
     */
    public boolean midSide(){
        try {
            return !io.getDigital(1);
        } catch (EnhancedIOException ex) {
            //ex.printStackTrace();
            return false;
        }
    }

    /**
     * Get the value from the bottom button
     * @return if the bottom button is pressed
     */
    public boolean bottom(){
        try {
            return !io.getDigital(3);
        } catch (EnhancedIOException ex) {
            //ex.printStackTrace();
            return false;
        }
    }

    /**
     * Get the value from the minibot deployment switch
     * @return if the minibot deployment switch is in the on position
     */
    public boolean deployMinibot(){
        try {
            return io.getDigital(6);
        } catch (EnhancedIOException ex) {
            //ex.printStackTrace();
            return false;
        }
    }

    /**
     * Set power to the LED on the driver's left
     * @param on if the LED should be turned on
     */
    public void setLeftLED(boolean on){
        try {
            io.setDigitalOutput(10, on);
        } catch (EnhancedIOException ex) {
            //ex.printStackTrace();
        }
    }

    /**
     * Set power to the LED in the middle
     * @param on if the LED should be turned on
     */
    public void setMiddleLED(boolean on){
        try {
            io.setDigitalOutput(12, on);
        } catch (EnhancedIOException ex) {
            //ex.printStackTrace();
        }
    }

    /**
     * Set power to the LED on the driver's right
     * @param on if the LED should be turned on
     */
    public void setRightLED(boolean on){
        try {
            io.setDigitalOutput(14, on);
        } catch (EnhancedIOException ex) {
            //ex.printStackTrace();
        }
    }
}
