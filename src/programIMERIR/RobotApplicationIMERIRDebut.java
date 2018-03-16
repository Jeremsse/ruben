package programIMERIR;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.lin;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptpHome;

import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.applicationModel.tasks.UseRoboticsAPIContext;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;

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
public class RobotApplicationIMERIRDebut extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	@Inject
	@Named("LegLift")
	private Tool legLift;//Création d'un objet outil
	private int i;

	@Override
	public void initialize() {
		// initialize your application here
		legLift.attachTo(robot.getFlange());//"Fixation" de l'outil à la bride du robot.
	}

	
	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome());
		for (i=1;i < 7;i++){
			if (i==1){
				legLift.getFrame("TCP").move(ptp(getApplicationData().getFrame("/Foam/P"+i)));
			}else{
				legLift.getFrame("TCP").move(lin(getApplicationData().getFrame("/Foam/P"+i)));
			}
			
		}
		legLift.getFrame("TCP").move(lin(getApplicationData().getFrame("/Foam/P1")));
		robot.move(ptpHome());
	}
}