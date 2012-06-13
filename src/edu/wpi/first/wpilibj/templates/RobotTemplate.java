/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Relay.Value;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.image.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends SimpleRobot {

    Joystick rightDriveStick;
    Joystick leftDriveStick;
    Joystick forkliftGripperStick;
    RobotDrive robotDrive;

    AxisCamera cam;
    Timer timer;//general-purpose timer
    Timer teleopTimer;//tracks the time elapsed in the teleoperated period
    //new forklift class
    Forklift forklift;
    //new gripper class
    Gripper gripper;

    MinibotDeployer deployer;

    /**
     * Arbitrary definition of lineSwitch: If true, the robot is on the left or right line and will proceed straight ahead to the center rack in the left or right grid.
     * If false, the robot is on the center line and will turn at the fork to go to a side peg of a rack
     */
    boolean lineSwitch; // will be used to determine the inital position of robot

    CameraPanTilt cameraBracket;

    EnhancedIOInterface io;

    /*
     * The line sensor works!
     * it returns true for darkness and false for lightness.
     */
    DigitalInput leftLineSensor;
    DigitalInput midLineSensor;
    DigitalInput rightLineSensor;

    boolean isDone = false;

    int deploydelay = 100;

    public RobotTemplate(){
        System.out.println("The constructor was called.");
        robotDrive = new RobotDrive(1, 2);
        rightDriveStick = new Joystick(1);
        leftDriveStick = new Joystick(2);
        forkliftGripperStick = new Joystick(3);

        leftLineSensor = new DigitalInput(1);//initialize the digital input for the left line sensor on digital I/O port 1
        midLineSensor = new DigitalInput(2);//the same for the middle line sensor and digital input 2
        rightLineSensor = new DigitalInput(3);//again for the right sensor 3

        //new forklift class
        forklift = new Forklift(1, 2, 4, 5, 14, 12);
        //new gripper class
        gripper = new Gripper(
                                 3 //Jaguar for top motor on channel 3
                                ,4 //Jaguar for bottom motor on channel 4
                                ,6 // Top encoder channel A
                                ,7 // Top encoder channel B
                                ,8 //Bottom encoder channel A
                                ,9 //Bottom encoder channel B
                                ,13//Gripper limit switch
                                );
        deployer = new MinibotDeployer(7);//PWM channel for deployment servo

        timer = new Timer();
        teleopTimer = new Timer();

        cameraBracket = new CameraPanTilt(5, 6);

        io = new EnhancedIOInterface();
    }

    /**
     * Executed once after the robot powers on
     */
    public void robotInit() {
        System.out.println("robotInit was called.");
        cam = AxisCamera.getInstance();
        cam.writeResolution(AxisCamera.ResolutionT.k640x480);
        cam.writeBrightness(0);
    }

    /*
     * autonomous mode: drives the robot forward along a line to the rack, then raises the forklift and places an uber tube
     * this method does some other shit that sam could tell you about
     */
    public void autonomous() {
        /*
         * Remember: the drivetrain is not reversed. Passing a positive value into driveOnLine will drive forward.
         */
        if(!isDone){
            isDone = true;
            //gripper.tilt(-45);
            forklift.setTop();//tell the forklift to raise
            //go forward until the end of the line
            while(!isAtEnd() && isAutonomous()){
                //System.out.println("At end: "+isAtEnd());
                //loop
                forklift.loop();
                gripper.loop();
                driveOnLine(0.55);//go forward on the line at specified power
                updateDashboard();
            }
            forklift.loop();
            updateDashboard();
            robotDrive.drive(0, 0);//stop
            gripper.onceEject();//eject game piece

            //at this point, the forklift is at the top and the gripper is targeted to release the ubertube.
            Timer autoTimer = new Timer();
            //eject for 1 second
            autoTimer.start();
            while(autoTimer.get() < 3 && isAutonomous()){
                gripper.loop();//eject
            }

            //back up and lower the forklift for 5 seconds
            autoTimer.reset();
            autoTimer.start();
            forklift.setBottom();
            while(autoTimer.get() < 3 && isAutonomous()){
                forklift.loop();
                gripper.loop();
                robotDrive.arcadeDrive(0.5, 0, true);
            }

            robotDrive.drive(0,0);//stop
            //System.out.println("Autonomous done");
        }
        gripper.loop();
        //clean up autonomous stuff that may not have finished
        robotDrive.drive(0, 0);//stop the drivetrain
    }

    /**
     * Called periodically when the robot is disabled
     */
    public void disabled(){
        isDone = false;//reset the autonomous mode so that after the robot has been disabled, it will run autonomous again before it is restarted
        while(isDisabled()){
            updateDashboard();
        }
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {

        Value lastRelayValue = Forklift.RELAY_STOP;

        double currentPan = CameraPanTilt.SERVO_PAN_DEFAULT;
        double currentTilt = CameraPanTilt.SERVO_TILT_DEFAULT;
        cameraBracket.setDefaults();

        //start the teleoperated timer
        teleopTimer.start();
        teleopTimer.reset();

        while (isOperatorControl() && isEnabled()) {
            Watchdog.getInstance().feed();      //Feed the Watchdog//
            //DriverStationLCD.getInstance().updateLCD();
            updateDashboard();

            if(forkliftGripperStick.getRawButton(8)){
                deploydelay = 0;
            }
            else{
                deploydelay = 100;
            }

            //forklift.loop();
            gripper.loop();

            //check the line following status
            //button 2, when held down, enables line following
            boolean followingLine = rightDriveStick.getRawButton(1);
            //now done checking if the robot should be following a line
            //now actually follow the line if it should
            if(followingLine){
                driveOnLine(-rightDriveStick.getY());//follow the line with the speed specified by the joystick
            }
            else{
                //use cheesy drive with regular joystick input

                double x = rightDriveStick.getX();
                double y = rightDriveStick.getY();
                if(rightDriveStick.getRawButton(3)){
                    x *= .9;//reduce  (not as much, to retain steering sensitivity)
                    y *= .5;//reduce y
                }

                robotDrive.arcadeDrive(y, x, true);
            }


            //servo code pan schtuff
            if (rightDriveStick.getRawButton(5)) {
                currentPan += .01;
            }
            if (rightDriveStick.getRawButton(4)) {
                currentPan -= .01;
            }

            if(currentPan > 1){
                currentPan = 1;
            }
            if(currentPan < 0){
                currentPan = 0;
            }

            //the same for the tilt schtuff
            if(rightDriveStick.getRawButton(3)){
                currentTilt += 0.01;
            }
            if(rightDriveStick.getRawButton(2)){
                currentTilt -= 0.01;
            }
            if(currentTilt > 1){
                currentTilt = 1;
            }
            if(currentTilt < 0){
                currentTilt = 0;
            }
            //set the servo values
            cameraBracket.set(currentPan, currentTilt);

            /*
             * Note about this: We only want to set these once. If we set them
             * continuously, the forklift will be told to stop many times per
             * second and it will not move. The variable lastRelayValue
             * in this operatorControl method holds the value previously
             * sent to the forklift. Only set it if it is different.
             */
            if (forkliftGripperStick.getY() > 0.5){//lowers the forklift when the joystick is moved back
                //if(lastRelayValue != Forklift.RELAY_DOWN){
                    if(forklift.bottomLimitSwitch.get()){
                        forklift.setManual(Forklift.RELAY_DOWN);
                        lastRelayValue = Forklift.RELAY_DOWN;
                        //System.out.println("Moving forklift down");
                    }else{//limit switch pressed
                        forklift.setManual(Forklift.RELAY_STOP);
                    }
                //}
            } else if (forkliftGripperStick.getY() < -0.5){//raises the forklift when the joystick is moved forward
                //if(lastRelayValue != Forklift.RELAY_UP){
                    if(forklift.topLimitSwitch.get()){
                        forklift.setManual(Forklift.RELAY_UP);
                        lastRelayValue = Forklift.RELAY_UP;
                        //System.out.println("Moving forklift up");
                    }else{//limit switch pressed
                        forklift.setManual(Forklift.RELAY_STOP);
                    }
                //}
            }else{
                if(lastRelayValue != Forklift.RELAY_STOP){
                    forklift.setManual(Forklift.RELAY_STOP);
                    lastRelayValue = Forklift.RELAY_STOP;
                    //System.out.println("Stopping forklift");
                }
            }
            //maps joystick buttons 3 and 2 to eject and take in a tube, respectively
            if (forkliftGripperStick.getRawButton(3)){
                    gripper.eject();
            }
            if (forkliftGripperStick.getRawButton(2)){
                    gripper.accept();
            }
            //maps joystick buttons 4 and 5 to tilt tube up and down respectively
            if (forkliftGripperStick.getRawButton(4)){
                gripper.tilt(2);
            }
            if (forkliftGripperStick.getRawButton(5)){
                gripper.tilt(-2);
            }

            /*
             * Forklift presets have returned!
             */
            if(io.topCenter()){
                //set the forklift to the top center height
                forklift.setTop();
            }
            if(io.topSide()){
                //set the forklift to the top side peg height
                forklift.setHeight(Forklift.TOP_PEG_HEIGHT);
            }
            if(io.midCenter()){
                //set the forklift to the middle center peg height
                forklift.setHeight(Forklift.MID_CENTER_PEG_HEIGHT);
            }
            if(io.midSide()){
                forklift.setHeight(Forklift.MID_PEG_HEIGHT);
            }
            if(io.bottom()){
                forklift.setBottom();
            }

            //Check minibot deployment
            //the minibot can only be deployed in the last 20 seconds of the match
            //That corresponds to when at least 100 seconds have passed in teleop
            if(io.deployMinibot()//if the switch on the I/O board is in the deploy position
                    && teleopTimer.get() >= deploydelay
                    ){
                deployer.deploy();
                System.out.println("Deploying Minibot");
            }else{
                deployer.reset();
            }

            //done with main stuff
            try {
                Thread.sleep(5);//delay 5ms to limit the loop frequency for progressive servo
                //and for limiting consecutive button presses
            } catch (InterruptedException ex) {
            }
        }//end loop
    }//end teleop

    /**
     * Drive while following a line.
     * takes over steering
	 * @param motorPower How much power to apply to the motors to go forwards/backwards. Ratio -1-1 where 1 is full forward
     */
    private void driveOnLine(double motorPower){
        double turnAmount = 0.8;//amount (0-1, 1=full) of turn to use when following lines
        if(isOperatorControl()){
            turnAmount = 0.8;
        }

        //System.out.println("Following a line. Power="+motorPower+", Turn="+turnAmount);
        motorPower = -motorPower;//invert the motor power
        boolean leftSensorReading = leftLineSensor.get();
        boolean midSensorReading = midLineSensor.get();
        boolean rightSensorReading = rightLineSensor.get();


       if ((leftSensorReading && rightSensorReading) && !midSensorReading) {
            //side sensors detect darkenss, middle sensor detects lightness
            //the robot seems to be on a line
            //System.out.println("The robot is on a line. Go straight.");
            robotDrive.arcadeDrive(motorPower, 0.0, true);//go straight at full line speed
            Timer.delay(0.01);
        }
        else if(!rightSensorReading && !midSensorReading){
            //right sensor and middle sensor detect light - it's slightly left of the line
            //System.out.println("The right sensor detects lightness (the line). Turn right.");
            robotDrive.arcadeDrive(motorPower, turnAmount / 2, true);//turn right at half the usual magnitude
            Timer.delay(0.01);
        }
        else if(!leftSensorReading && !midSensorReading){
            //left sensor and middle sensor detect light - it's slightly right of the line
            //System.out.println("The left sensor detects lightness (the line). Turn left.");
            robotDrive.arcadeDrive(motorPower, -turnAmount / 2, true);//turn left at half the usual magnitude
            Timer.delay(0.01);
        }
        else if(!rightSensorReading){
            //right sensor detects light. It's left of the line
            robotDrive.arcadeDrive(motorPower, turnAmount, true);//turn right
            Timer.delay(0.01);
        }
        else if(!leftSensorReading){
            //left sensor detects light. It's right of the line
            robotDrive.arcadeDrive(motorPower, -turnAmount, true);//turn left
            Timer.delay(0.01);
        }
        if(!leftSensorReading && !midSensorReading && !rightSensorReading){
            robotDrive.arcadeDrive(0, 0, true);
        }
        else {
            //System.out.println("Sensors detect something else.");
            //proceed with caution
            robotDrive.arcadeDrive(motorPower, 0.0, true);//go straight at half line speed
        }
    }

    private double getDistance(){
        AxisCamera camera = AxisCamera.getInstance();
        ColorImage colorImage = null;
        try {
            colorImage = camera.getImage();
        } catch (AxisCameraException ex) {
            ex.printStackTrace();
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        }
        MonoImage monoImage = null;
        try {
            monoImage = colorImage.getIntensityPlane();
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        }
        //at this point, we have the grayscale image from the intensity.
        EllipseDescriptor descriptor = new EllipseDescriptor(5, 50, 5, 50);
        EllipseMatch[] match = null;
        try {
            match = monoImage.detectEllipses(descriptor);
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        }
        //Now we have an array of EllipseMatches
        //debug: loop through the arrays
        for(int i = 0; i < match.length; i++){
            //output information about it
            System.out.println("Detected an elipse. major radius: "+match[i].m_majorRadius
                    +" minor radius: "+match[i].m_minorRadius
                    +" rotation: "+match[i].m_rotation
                    +" score: "+match[i].m_score
                    +" xPos: "+match[i].m_xPos
                    +" yPos: "+match[i].m_yPos);
        }

        return -1;
    }

    /**
     *
     * @return
     */
    private boolean isAtEnd(){
        //System.out.println("Left line sensor: "+leftLineSensor.get()+" Middle line sensor: "+midLineSensor.get()+" Right line sensor: "+rightLineSensor.get());
        return !leftLineSensor.get() && !midLineSensor.get() && !rightLineSensor.get();
    }

    /**
     * Call this periodically to push data to the dashboard on the driver station
     */
    private void updateDashboard(){
        io.setLeftLED(!leftLineSensor.get());
        io.setMiddleLED(!midLineSensor.get());
        io.setRightLED(!rightLineSensor.get());

        gripper.log();
        forklift.log();
    }
}
