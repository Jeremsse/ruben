package programIMERIR;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.positionHold;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPITask;
import com.kuka.roboticsAPI.applicationModel.tasks.UseRoboticsAPIContext;
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
 * Implementation of a robot application.
 * <p>
 * The application provides a {@link RoboticsAPITask#initialize()} and a 
 * {@link RoboticsAPITask#run()} method, which will be called successively in 
 * the application lifecycle. The application will terminate automatically after 
 * the {@link RoboticsAPITask#run()} method has finished or after stopping the 
 * task. The {@link RoboticsAPITask#dispose()} method will be called, even if an 
 * exception is thrown during initialization or run. 
 * <p>
 * <b>It is imperative to call <code>super.dispose()</code> when overriding the 
 * {@link RoboticsAPITask#dispose()} method.</b> 
 * 
 * @see UseRoboticsAPIContext
 * @see #initialize()
 * @see #run()
 * @see #dispose()
 */
public class HoldAndDo extends RoboticsAPIApplication {
	@Inject
	private LBR robot;

//	CartesianImpedanceControlMode mode;
	JointImpedanceControlMode mode;
	
	int onPosition = -1;
	double[] jointPosition;
	
	private boolean moving = false;
	private boolean finished = false;
	IUserKeyBar buttonBar;
	IUserKey key;
	
	@Override
	public void initialize() {
		onPosition = -1;

//		mode = new CartesianImpedanceControlMode();
//		mode.parametrize(CartDOF.TRANSL).setStiffness(100);
//		mode.parametrize(CartDOF.ROT).setStiffness(10);

		mode = new JointImpedanceControlMode(10, 10, 10, 10, 10, 10, 5);
		mode.setStiffness(10, 10, 10, 10, 10, 10, 5);
		
		IUserKeyListener listener = new IUserKeyListener() {
			@Override
			public void onKeyEvent(IUserKey key, UserKeyEvent event) {
				if(event.equals(UserKeyEvent.KeyUp)){
					moving = !moving;
					key.setText(UserKeyAlignment.MiddleLeft, moving ? "ON" : "OFF");
				}
			}
		};
		
		buttonBar = getApplicationUI().createUserKeyBar("Mouvement");
		
		key = buttonBar.addUserKey(0, listener, true);
		key.setText(UserKeyAlignment.MiddleLeft, "Bouger le robot");
		key.setLED(UserKeyAlignment.MiddleLeft, UserKeyLED.Red, UserKeyLEDSize.Small);
		//key.setCriticalText("Critical problem?");
		
		buttonBar.publish();
	}

	@Override
	public void run() {
		
		while(!finished){
			
			if(moving){
				getLogger().info("The robot is compliant.");
				key.setLED(UserKeyAlignment.MiddleLeft, UserKeyLED.Green, UserKeyLEDSize.Small);
				
				while (moving) {
					robot.move(positionHold(mode, 1, TimeUnit.SECONDS));
				}
				
				getLogger().info("Stop touching the robot.\nThe robot is not compliant anymore.");
				key.setLED(UserKeyAlignment.MiddleLeft, UserKeyLED.Red, UserKeyLEDSize.Small);
				
				jointPosition = robot.getCurrentJointPosition().get();
				robot.move(ptp(jointPosition));
			}
			
		}

	}
}