package programIMERIR;


import java.util.Random;

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
public class JanKenPon extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	@Inject
	@Named ("LegLift")
	private Tool leglift;
	@Inject
	@Named ("Leg")
	private Workpiece leg;
	private int i;
	//variables accessibles via le ProcessData
	private Integer tempo, nbCycles;
	private Double angle, angleSpeed;
	private Random rand;
	//Création d'un entier qui récupère le choix de l'utilisation
	private int answer, play, probMove, move;
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
		play = 0;
		rand = new Random();
	}

	public void moveArm(){
		for(i=1;i<nbCycles;i++){
			leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(-angle), 0, 0).setCartVelocity(angleSpeed));
			leg.getFrame("TCPKnee").move(linRel(0, 0, 0, Math.toRadians(angle), 0, 0).setCartVelocity(angleSpeed));
		}
	}
	
	@Override
	public void run() {
		robot.move(ptpHome());
		// your application execution starts here
		while(play == 0){
			leglift.getFrame("/Dummy/PNPParent").move(ptp(getApplicationData().getFrame("/Knee/P1")).setJointVelocityRel(0.5));
			//Initialisation de la variable answer avec le message
			answer = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Vous etes pret à ?", "Oui, mon Capitaine!", "Non, mon Capitaine.");
			ThreadUtil.milliSleep(tempo);
			//bottle.getFrame("PNPChild").attachTo(leglift.getFrame("/Dummy/PNPParent"));
			probMove = rand.nextInt(100) + 1;
			if((probMove>1) && (probMove <= 33))move = 1;
			if((probMove>33) && (probMove <= 66))move = 2;
			if((probMove>66) && (probMove <= 100))move = 3;
			moveArm();
			switch(move){
			case 1 : play = getApplicationUI().displayModalDialog(ApplicationDialogType.INFORMATION, "Pierre", "Again?", "Stop");
			case 2 : play = getApplicationUI().displayModalDialog(ApplicationDialogType.INFORMATION, "Papier", "Again?", "Stop");
			case 3 : play = getApplicationUI().displayModalDialog(ApplicationDialogType.INFORMATION, "Ciseaux", "Again?", "Stop");
			}
			ThreadUtil.milliSleep(tempo);
			//bottle.detach();
		}
		robot.move(ptpHome().setJointVelocityRel(0.5));
	}
}