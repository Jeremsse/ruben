package programIMERIR;


import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.Workpiece;

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
public class TrainingKnee extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	@Inject
	@Named("LegLift")
	private Tool legLift;//Création d'un objet outil
	@Inject
	@Named("Leg")
	private Workpiece leg;
	private int i;
	@Override
	public void initialize() {
		// initialize your application here
		legLift.attachTo(robot.getFlange());
	}

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome());
		legLift.getFrame("/dummy/pnpParent").move(ptp(getApplicationData().getFrame("/Knee/P1")).setJointVelocityRel(0.5));
		ThreadUtil.milliSleep(10000);
		// Ancrage de la jambe à l'outil
		leg.getFrame("/PnpChild").attachTo(legLift.getFrame("/dummy/pnpParent"));
		for (i=1;i<5;i++){
			leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(-20),0, 0).setCartVelocity(30));
			leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(20),0, 0).setCartVelocity(30));
		}
		ThreadUtil.milliSleep(10000);
		leg.detach();
		robot.move(ptpHome().setJointVelocityRel(0.5));
		
		robot.move(ptpHome().setJointVelocityRel(0.5));
		
	}
}