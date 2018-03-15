package programIMERIR;


import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.Workpiece;
import com.kuka.roboticsAPI.persistenceModel.processDataModel.IProcessData;
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
	
	//Variable accessible via les preocessData
	private Integer tempo, nbCycle;
	private Double angle, angleSpeed;
	
	private int answer;
	
	@Override
	public void initialize() {
		// initialize your application here
		legLift.attachTo(robot.getFlange());
		tempo = getApplicationData().getProcessData("tempo").getValue();
		nbCycle = getApplicationData().getProcessData("nbCycle").getValue();
		angle = getApplicationData().getProcessData("angle").getValue();
		angleSpeed = getApplicationData().getProcessData("angleSpeed").getValue();
		
		// Initialisation de answer : une valeur non utilisée par la boite de dialogue
		answer = -1;
	}

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome());
		legLift.getFrame("/dummy/pnpParent").move(ptp(getApplicationData().getFrame("/Knee/P1")).setJointVelocityRel(0.5));
		
		// Initialisation de la variable answer
		answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est-elle en place ?", "Oui");
		
		ThreadUtil.milliSleep(tempo);
		
		do {
			// Ancrage de la jambe à l'outil
			leg.getFrame("/PnpChild").attachTo(legLift.getFrame("/dummy/pnpParent"));
			
			for (i = 1; i < nbCycle; i++){
				leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(-angle),0, 0).setCartVelocity(angleSpeed));
				leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(angle),0, 0).setCartVelocity(angleSpeed));
			}
	
			answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Voulez-vous continuer ?", "Oui", "Non");
		} while (answer == 0);

		answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est-elle enlevé ?", "Oui");
		ThreadUtil.milliSleep(tempo);
		leg.detach();
		
		robot.move(ptpHome().setJointVelocityRel(0.5));
		
	}
}