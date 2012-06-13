
package edu.wpi.first.wpilibj.templates;

/**
 * Holds an instance of line tracker status.
 * This was intended for use in the new {@link LineTracker} class, which was
 * not finished.
 */
public class LineTrackerStatus {
    /**
     * If the left line sensor has detected a line
     */
    public boolean left;
    /**
     * If the middle line sensor has detected a line
     */
    public boolean mid;
    /**
     * If the right line sensor has detected a line
     */
    public boolean right;

    /**
     * This is part of extending Object.
     * All classes are recommended to provide their own version of this.
     * @return A String representing this object
     */
    public String toString(){
        return "Left: "+left+" Middle: "+mid+" Right: "+right;
    }
}
