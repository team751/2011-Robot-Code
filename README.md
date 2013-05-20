# Team 751 2011 Robot Code #

This code controls Team 751's 2011 competition robot. It is based on the SimpleRobot template.

This code started as one file containing all the robot logic.

Over the season, the code became complicated as autonomous logic, closed-loop forklift control, and other features were added. Some of these features were separated into their own files.

The SimpleRobot system became cumbersome as the code developed. For future robot code, it is suggested to instead use the Command-Based Robot system. The 2012 and 2013 code are examples that use this system.

Everything is located in the `edu.wpi.first.wpilibj.templates` package.

## Files ##

### RobotTemplate.java ###

This file is the main robot class.

Its constructor `RobotTemplate()` is where the the robot components are initialized.

The system calls the `disabled()`, `autonomous()`, and `operatorControl()` methods once each time the robot enters the respective phase of operation.

The RobotTemplate class contains the three joysticks that are used to operate the robot. The operator console for this robot had three joysticks, so that two of them could be used for driving the robot with one drive joystick for forward/back motion and the other drive joystick for turning.

It also contains:
* The `RobotDrive` object that does calculations to determine motor output values from joystick inputs
* Two `edu.wpi.first.wpilibj.Timer` timers that are used to measure elapsed time during the match
* The `Forklift`, `Gripper`, `MinibotDeployer`, `CameraPanTilt`, and `EnhancedIOInterface` objects that control different subsystems

#### Autonomous mode ####

The autonomous code runs once when autonomous mode begins. The `autonomous()` method is called when the autonomous period begins and returns several seconds later, near the end of the autonomous period.

Note that each loop in the autonomous method checks for the `isAutonomous()` condition. This is important. If the autonomous code gets stuck in a loop, it will remain in the loop throughout the end of autonomous mode and the rest of the match until the robot is restarted. If there is ever a loop in the autonomous code, it should check `isAutonomous()` to protect against this situation.

This autonomous code is somewhat difficult to maintain. A better solution would be to use a set of CommandGroups in the command-based robot system (like in the 2012 code) or a state machine autonomous (like in the most recent revision of the 2013 code).

### Forklift.java ###

This class holds the `Relay` objects that control the forklift motors and the `Encoder` object that uses the forklift encoder to measure the position of the forklift. It keeps track of the position of the forklift and operates the motors to maintain the target position.

Because the forklift encoder encountered mechanical issues frequently during competitions, the code transitioned to no longer use the encoder. It used only the top and bottom limit switches.

### Gripper.java ###

This class holds the `Jaguar` objects and `Encoder` objects that control and monitor the gripper wheels. It keeps track of the gripper wheel positions.

### MinibotDeployer.java ###

This class holds a `Servo` object that actuates the minibot deployer.

### CameraPanTilt.java ###

This class holds two `Servo` objects that actuate the pan/tilt bracket that the camera was mounted on. (The camera was removed from the robot to reduce the robot's weight at its first competition that year.)

### EnhancedIOInterface.java ###

This class provides abstractions for the buttons, switches, and LEDs that were on the operator console. The Cypress IO board was used to interface with the driver station computer for digital inputs and outputs.

### Other files ###

Other files are unfinished and/or unused.
