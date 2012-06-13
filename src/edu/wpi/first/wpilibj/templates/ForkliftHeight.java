
package edu.wpi.first.wpilibj.templates;

/**
 * Utility class to hold a vertical position for the forklift.
 * @author Sam
 */
public class ForkliftHeight {
    /**
     * Constructor
     * @param inDegrees The number of degrees of (motor?) rotation from the lowest position to target
     */
    public ForkliftHeight(int inDegrees){
        degrees = inDegrees;
    }
    /**
     * Degrees from the lowest possible position that this position targets
     */
    public int degrees;
}
