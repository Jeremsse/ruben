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
	// Variables accessibles via le ProcessData"
	private Integer tempo,
					nbCycles;
	private Double angle,
				   angleSpeed; 
	//Création d'un entier qui récupère le choix de l'utilisateur
	private int answer;
	@Override
	public void initialize() {
		// initialize your application here
		legLift.attachTo(robot.getFlange());
		tempo=getApplicationData().getProcessData("tempo").getValue();
		nbCycles=getApplicationData().getProcessData("nbCycles").getValue();
		angle=getApplicationData().getProcessData("angle").getValue();
		angleSpeed=getApplicationData().getProcessData("angleSpeed").getValue();
		//initialisation de answer à une valeur non utilisée par la boîte de dialogue
		answer=-1;
	}

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome());
		legLift.getFrame("/dummy/pnpParent").move(ptp(getApplicationData().getFrame("/Knee/P1")).setJointVelocityRel(0.5));
		// initialisation de la variable answer avec le message
		answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe est elle en place ?", "Oui","Annuler");
		ThreadUtil.milliSleep(tempo);
		// Ancrage de la jambe à l'outil
		leg.getFrame("/PnpChild").attachTo(legLift.getFrame("/dummy/pnpParent"));
		while(answer==0){
			for (i=1;i<nbCycles;i++){
				leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(-angle),0, 0).setCartVelocity(angleSpeed));
				leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(angle),0, 0).setCartVelocity(angleSpeed));
			}
			answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Répéter le cycle ?", "Oui","Non");
		}
		ThreadUtil.milliSleep(tempo);
		leg.detach();
		robot.move(ptpHome().setJointVelocityRel(0.5));
		
		
		
	}
}