package application;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.positionHold;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.motionModel.controlModeModel.JointImpedanceControlMode;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKey;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyBar;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyListener;
import com.kuka.roboticsAPI.uiModel.userKeys.UserKeyAlignment;
import com.kuka.roboticsAPI.uiModel.userKeys.UserKeyEvent;
import com.kuka.roboticsAPI.uiModel.userKeys.UserKeyLED;
import com.kuka.roboticsAPI.uiModel.userKeys.UserKeyLEDSize;

/**
 * 
 * @author Clément Bourdarie
 */
public class HoldAndDo extends RoboticsAPIApplication {
	@Inject
	private LBR robot;

	private JointImpedanceControlMode mode;
	private double[] jointPosition;
	
	private boolean moving = false;
	private boolean finished = false;
	private IUserKeyBar buttonBar;
	private IUserKey allowMovementKey;
	private IUserKey stopApplicationKey;
	
	@Override
	public void initialize() {
		mode = new JointImpedanceControlMode(10, 10, 10, 10, 10, 10, 1);
		mode.setStiffness(10, 10, 10, 10, 10, 10, 1);
		
		//The listener of the allowMovementKey button
		IUserKeyListener moveButtonListener = new IUserKeyListener() {
			@Override
			public void onKeyEvent(IUserKey key, UserKeyEvent event) {
				if(event.equals(UserKeyEvent.KeyUp)){
					moving = !moving;// Allow the robot to move, or stop it.
					key.setText(UserKeyAlignment.MiddleLeft, moving ? "OFF" : "ON");// If the robot was moving show OFF, else show ON.
				}
			}
		};
		
		//The listener of the stopApplicationKey button
		IUserKeyListener stopApplicationButtonListener = new IUserKeyListener() {
			@Override
			public void onKeyEvent(IUserKey key, UserKeyEvent event) {
				if(event.equals(UserKeyEvent.KeyUp)){
					finished = true;
				}
			}
		};
		//The container of the button we're going to create
		buttonBar = getApplicationUI().createUserKeyBar("Mouvement");
		
		//Button allowing the user to move the robot
		allowMovementKey = buttonBar.addUserKey(0, moveButtonListener, true);
		allowMovementKey.setLED(UserKeyAlignment.MiddleLeft, UserKeyLED.Red, UserKeyLEDSize.Small);
		
		//Button allowing the user to stop the application
		stopApplicationKey = buttonBar.addUserKey(1, stopApplicationButtonListener, true);
		stopApplicationKey.setLED(UserKeyAlignment.MiddleLeft, UserKeyLED.Red, UserKeyLEDSize.Small);
		
		//Make the buttons bar visible
		buttonBar.publish();
	}

	@Override
	public void run() {
		//While the user hasn't pressed the stopApplicationKey
		while(!finished){
			//If the user has pressed the allowMovementKey
			if(moving){
				//Make the allowMovementKey LED go RED
				getLogger().info("The robot is compliant.");
				allowMovementKey.setLED(UserKeyAlignment.MiddleLeft, UserKeyLED.Green, UserKeyLEDSize.Small);
				//Make the robot compliant according to "mode" which makes his stiffness low
				while (moving) {
					robot.move(positionHold(mode, 1, TimeUnit.SECONDS));
					jointPosition = robot.getCurrentJointPosition().get();// Register the current position
				}
				//Make the allowMovementKey LED go RED
				getLogger().info("Stop touching the robot.\nThe robot is not compliant anymore.");
				allowMovementKey.setLED(UserKeyAlignment.MiddleLeft, UserKeyLED.Red, UserKeyLEDSize.Small);
				
				//Move to the registered position so that the robot holds it
				robot.move(ptp(jointPosition));
			}
			
		}

	}
}