package application;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptpHome;

import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPITask;
import com.kuka.roboticsAPI.applicationModel.tasks.UseRoboticsAPIContext;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.motionModel.controlModeModel.JointImpedanceControlMode;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKey;
import com.kuka.roboticsAPI.uiModel.userKeys.IUserKeyBar;

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
 * Cette application permettra de récupérer les outils qui seront déposés à une position particulièrement précise
 * Cela permettra au robot de prendre l'outil nécessaire et de bien les utiliser.
 * 
 * @see UseRoboticsAPIContext
 * @see #initialize()
 * @see #run()
 * @see #dispose()
 */
public class GrabToolsAndWork extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	@Inject
	@Named("Pliers")
	private Tool pliers;//Création d'un objet outil de type pince
	private int i;

//	CartesianImpedanceControlMode mode;
	JointImpedanceControlMode mode;
	
	int onPosition = -1;
	double[] jointPosition;
	
	private boolean moving = false;
	private boolean finished = false;
	IUserKeyBar buttonBar;
	IUserKey key;
	
	@Override
	public void initialize() 
	{
		pliers.attachTo(robot.getFlange());//"Fixation" de l'outil à la bride du robot.
	}

	@Override
	public void run() 
	{
	// your application execution starts here
	robot.move(ptpHome());  //
	pliers.getFrame("ClampingArea").move(ptp(getApplicationData().getFrame("/Workshop/Brush")).setJointVelocityRel(1.0));
	robot.move(ptpHome());
	
	//programme détection 4 extrémités de la planche
}
}