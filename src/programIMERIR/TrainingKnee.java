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
	//var accessible via le processdata

	private Integer   tempo, nbcycles;
	private Double angle, anglespeed;
	//create d'un int qui recupere le choix de l'utilisateur
	private int answer;
	
	@Override
	public void initialize() {
		// initialize your application here
		legLift.attachTo(robot.getFlange());
		tempo = getApplicationData().getProcessData("tempo").getValue();
		nbcycles = getApplicationData().getProcessData("nbcycles").getValue();
		angle = getApplicationData().getProcessData("angle").getValue();
		anglespeed = getApplicationData().getProcessData("anglespeed").getValue();
		answer = -1;
	}

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome());
		legLift.getFrame("/dummy/pnpParent").move(ptp(getApplicationData().getFrame("/Knee/P1")).setJointVelocityRel(0.5));
		//initialiser answer avec le message
		answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, 
													   "la jambe est en place?", "oui");
		
		ThreadUtil.milliSleep(tempo);
		// Ancrage de la jambe à l'outil
		leg.getFrame("/PnpChild").attachTo(legLift.getFrame("/dummy/pnpParent"));
		for (i=1;i<nbcycles;i++){
			leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(-angle),0, 0).setCartVelocity(anglespeed));
			leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(angle),0, 0).setCartVelocity(anglespeed));
		}
		answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, 
				   "voulez vous refaire un cycle?", "oui", "non");
		switch (answer) {
		case 0:
			for (i=1;i<nbcycles;i++){
				leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(-angle),0, 0).setCartVelocity(anglespeed));
				leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(angle),0, 0).setCartVelocity(anglespeed));
			}
			break;
		case 1:
			answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, 
					   "Avez vou enlever la jambe", "oui");
			ThreadUtil.milliSleep(tempo);
			leg.detach();
		default:
			break;
		}			
		robot.move(ptpHome().setJointVelocityRel(0.5));
	}
}