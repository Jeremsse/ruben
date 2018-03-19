package programIMERIR;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.linRel;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptpHome;

import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.applicationModel.tasks.UseRoboticsAPIContext;
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
	private Tool legLift;//Cr�ation d'un objet outil
	@Inject
	@Named("Leg")
	private Workpiece leg;
	private int i;
	// Variables accessibles via le ProcessData"
	private Integer tempo,
					nbCycles;
	private Double angle,
				   angleSpeed; 
	//Cr�ation d'un entier qui r�cup�re le choix de l'utilisateur
	private int answer;
	private int cycle;
	
	@Override
	public void initialize() {
		// initialize your application here
		legLift.attachTo(robot.getFlange());
		tempo=getApplicationData().getProcessData("tempo").getValue();
		nbCycles=getApplicationData().getProcessData("nbCycles").getValue();
		angle=getApplicationData().getProcessData("angle").getValue();
		angleSpeed=getApplicationData().getProcessData("angleSpeed").getValue();
		//initialisation de answer � une valeur non utilis�e par la bo�te de dialogue
		answer=-1;
		cycle = -1;
	}

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome());
		legLift.getFrame("/dummy/pnpParent").move(ptp(getApplicationData().getFrame("/Knee/P1")).setJointVelocityRel(0.5));
		// initialisation de la variable answer avec le message

		do {
			cycle = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Voulez-vous lancer un cycle?", "Oui", "Non");
			
			if(cycle == 0){
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe est elle en place?", "Oui");
				ThreadUtil.milliSleep(tempo);
				// Ancrage de la jambe � l'outil
				leg.getFrame("/PnpChild").attachTo(legLift.getFrame("/dummy/pnpParent"));
				for (i = 1 ; i < nbCycles ; i++){
					leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(-angle),0, 0).setCartVelocity(angleSpeed));
					leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(angle),0, 0).setCartVelocity(angleSpeed));
				}
				ThreadUtil.milliSleep(tempo);
			}
			
		} while (cycle == 0);
		
		//fin
		leg.detach();
		robot.move(ptpHome().setJointVelocityRel(0.5));
	}
}