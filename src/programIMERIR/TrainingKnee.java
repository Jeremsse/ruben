package programIMERIR;


import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.Workpiece;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
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
	private String nom;
	//create d'un int qui recupere le choix de l'utilisateur
	private int answer;
	
	private CartesianImpedanceControlMode ctrlMode;
	
	@Override
	public void initialize() {
		// initialize your application here
		legLift.attachTo(robot.getFlange());
		tempo = getApplicationData().getProcessData("tempo").getValue();
		nbcycles = getApplicationData().getProcessData("nbcycles").getValue();
		angle = getApplicationData().getProcessData("angle").getValue();
		anglespeed = getApplicationData().getProcessData("anglespeed").getValue();
		nom = getApplicationData().getProcessData("Nom").getValue();
		answer = -1;
		ctrlMode = new CartesianImpedanceControlMode();
		ctrlMode.parametrize(CartDOF.X, CartDOF.Z).setStiffness(100);
		ctrlMode.parametrize(CartDOF.Y).setStiffness(3000);
		ctrlMode.parametrize(CartDOF.ALL).setDamping(0.7);
	}

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome());
		legLift.getFrame("/dummy/pnpParent").move(ptp(getApplicationData().getFrame("/Knee/P1")).setJointVelocityRel(0.5).setMode(ctrlMode));
		
		//initialiser answer avec le message
		answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe est-elle en place ?", "oui");
		
		ThreadUtil.milliSleep(tempo);
		// Ancrage de la jambe à l'outil
		leg.getFrame("/PnpChild").attachTo(legLift.getFrame("/dummy/pnpParent"));
		
		do {
			for (i=1;i<nbcycles;i++){
				leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(-angle),0, 0).setCartVelocity(anglespeed));
				leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(angle),0, 0).setCartVelocity(anglespeed));
			}
		answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, 
				   "Voulez vous refaire un cycle Mr/Mme :"+nom+"?", "oui", "non");			
		} while (answer == 0);
		
			answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, 
					   "Avez vous enlever la jambe Mr/Mme:"+nom+"?", "oui");
			ThreadUtil.milliSleep(tempo);
			leg.detach();			
		robot.move(ptpHome().setJointVelocityRel(0.5));
	}
}