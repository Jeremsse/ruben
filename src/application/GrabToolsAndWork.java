package application;


import static com.kuka.roboticsAPI.motionModel.BasicMotions.linRel;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptpHome;

import javax.inject.Inject;
import javax.inject.Named;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.applicationModel.tasks.RoboticsAPITask;
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
 * Cette application permettra de r�cup�rer les outils qui seront d�pos�s � une position particuli�rement pr�cise
 * Cela permettra au robot de prendre l'outil n�cessaire et de bien les utiliser.
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
	private Tool pliers;//Cr�ation d'un objet outil de type pince
	
	double x, y, z, x2, y2, z2, x3, y3, z3, x4, y4, z4, i;
	
	double largeurOutil = 60;
	
	@Override
	public void initialize() 
	{
		pliers.attachTo(robot.getFlange());//"Fixation" de l'outil � la bride du robot.
	}

	@Override
	public void run() 
	{
		// your application execution starts here
		x = getFrame("/Workspace/P1").getX();
		y = getFrame("/Workspace/P1").getY();
		z = getFrame("/Workspace/P1").getZ();
		x2 = getFrame("/Workspace/P2").getX();
		y2 = getFrame("/Workspace/P2").getY();
		z2 = getFrame("/Workspace/P2").getZ();
		x3 = getFrame("/Workspace/P3").getX();
		y3 = getFrame("/Workspace/P3").getY();
		z3 = getFrame("/Workspace/P3").getZ();
		x4 = getFrame("/Workspace/P4").getX();
		y4 = getFrame("/Workspace/P4").getY();
		z4 = getFrame("/Workspace/P4").getZ();
		
		/*double largeur;
		largeur = x4-x;
		double pourcentage;
		pourcentage = largeurOutil / largeur;*/
		
		
		robot.move(ptpHome());  //

		pliers.getFrame("/Sander").move(ptp(getApplicationData().getFrame("/Workspace/P1")).setJointVelocityRel(1.0));
		for(i = x; i<x4; i+=largeurOutil)
		{
			pliers.getFrame("/Sander").move(linRel(0.0, 0.0, -10.0).setJointVelocityRel(1.0));
			pliers.getFrame("/Sander").move(linRel(0.0, y2, 0.0).setJointVelocityRel(1.0));
			pliers.getFrame("/Sander").move(linRel(0.0, 0.0, 60.0).setJointVelocityRel(1.0));
			pliers.getFrame("/Sander").move(linRel(0.0, -y2, 0.0).setJointVelocityRel(1.0));
			pliers.getFrame("/Sander").move(linRel(0.0, 0.0, -50.0).setJointVelocityRel(1.0));
			pliers.getFrame("/Sander").move(linRel(i, 0.0, 0.0).setJointVelocityRel(1.0));
		}
		robot.move(ptpHome());
	}
}