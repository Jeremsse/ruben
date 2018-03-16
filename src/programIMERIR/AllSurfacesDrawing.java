package programIMERIR;


import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.motionModel.PositionHold;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;

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
public class AllSurfacesDrawing extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	
	private CartesianImpedanceControlMode freeMoveMode;

	@Override
	public void initialize() {
		// initialize your application here
		freeMoveMode = new CartesianImpedanceControlMode();
	}

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptp(getApplicationData().getFrame("/WorkingTable/WaitingPoint")));
		robot.move(positionHold(freeMoveMode, -1, TimeUnit.SECONDS));
	}
}