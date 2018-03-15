package programIMERIR;


import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.Workpiece;
import com.kuka.roboticsAPI.uiModel.ApplicationDialogType;

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
public class LegLifter extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	@Inject
	@Named ("LegLift")
	private Tool leglift;
	@Inject
	@Named ("Bottle")
	private Workpiece bottle;
	private int i;
	//variables accessibles via le ProcessData
	private Integer tempo, nbCycles;
	private Double angle, angleSpeed;
	//Création d'un entier qui récupère le choix de l'utilisation
	private int answer;
	@Override
	public void initialize() {
		// initialize your application here
		leglift.attachTo(robot.getFlange());
		tempo = getApplicationData().getProcessData("tempo").getValue();
		nbCycles = getApplicationData().getProcessData("nbCycles").getValue();
		angle = getApplicationData().getProcessData("angle").getValue();
		angleSpeed = getApplicationData().getProcessData("angleSpeed").getValue();
		//initialisation de answer à une valeur non utilisée par la boite de dialogue
		answer = -1;
	}

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome());
		leglift.getFrame("/Dummy/PNPParent").move(ptp(getApplicationData().getFrame("/Knee/P1")).setJointVelocityRel(0.5));
		//Initialisation de la variable answer avec le message
		answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Vous etes pret à ?", "Oui, mon Capitaie!", "Non, mon Capitaine.");
		ThreadUtil.milliSleep(tempo);
		bottle.getFrame("PNPChild").attachTo(leglift.getFrame("/Dummy/PNPParent"));
		for(i=1;i<nbCycles;i++){
			bottle.getFrame("TCTKnee").move(linRel(0, 0, 0, Math.toRadians(-angle), 0, 0).setCartVelocity(angleSpeed));
			bottle.getFrame("TCTKnee").move(linRel(0, 0, 0, Math.toRadians(angle), 0, 0).setCartVelocity(angleSpeed));
		}
		ThreadUtil.milliSleep(10000);
		bottle.detach();
		robot.move(ptpHome().setJointVelocityRel(0.5));
	}
}