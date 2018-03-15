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
public class TrainingLegs extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	@Inject
	@Named("LegLift")
	private Tool legLift;//Création d'un objet outil
	@Inject
	@Named("Leg")
	private Workpiece leg;
	
	//variable accessible via process data
	private Integer tempo, 
					nbcycle;
	private Double angle;
	private Double vitesse;
	private String name;
	
	//---------------------
	private int answer;
	
	//private String nom;
	/*
	static enum name{
		JACK,
		MARIE,
		PIERRE;
	}
	*/
	
	@Override
	public void initialize() {
		name = getApplicationData().getProcessData("personne").getValue();
		if (name.equals("Jack")){
			tempo = 10000;
			nbcycle = 5;
			angle = 30.0;
			vitesse = 25.0;
		}
		
		if (name.equals("Pierre")){
			tempo = 10000;
			nbcycle = 10;
			angle = 25.0;
			vitesse = 35.0;
		}
		if (name.equals("Paul")){
			tempo = 10000;
			nbcycle = 7;
			angle = 15.0;
			vitesse = 40.0;
		}
			/*
			getApplicationData().getProcessData("tempo").setValue(10000);
			getApplicationData().getProcessData("nbcycle").setValue(5);
			getApplicationData().getProcessData("angle").setValue(30);
			getApplicationData().getProcessData("vitesse").setValue(25);
		}
		else if (name.equals("Marine")){	
			getApplicationData().getProcessData("tempo").setValue(10000);
			getApplicationData().getProcessData("nbcycle").setValue(10);
			getApplicationData().getProcessData("angle").setValue(35);
			getApplicationData().getProcessData("vitesse").setValue(15);
		}
		else if (name.equals("Pierre")){	
			getApplicationData().getProcessData("tempo").setValue(10000);
			getApplicationData().getProcessData("nbcycle").setValue(3);
			getApplicationData().getProcessData("angle").setValue(40);
			getApplicationData().getProcessData("vitesse").setValue(30);
		}
		else if (name.equals("Paul")){	
			getApplicationData().getProcessData("tempo").setValue(10000);
			getApplicationData().getProcessData("nbcycle").setValue(20);
			getApplicationData().getProcessData("angle").setValue(15);
			getApplicationData().getProcessData("vitesse").setValue(30);
		}
		
		*/
		answer = -1; //initialize a une valeur nom utilisable par la boite de dialogue
	} 
		/*
		name Nom = 
		switch (name)
		{
		  case name.:
			  //tempo = getApplicationData().getProcessData("tempo").setValue(10000);
			    getApplicationData().getProcessData("tempo").setValue(10000);
		    break;        
		  default:
		    /*Action/;             
		}
		
		/*
		 <processData displayName="Temporisation" dataType="java.lang.Integer" defaultValue="10000" id="tempo" min="100" max="100000" value="10000" unit="milliseconde" comment="Temporisation pour attendre la jambe du patient"/>
      <processData displayName="Nombre_cycle" dataType="java.lang.Integer" defaultValue="5" id="nbcycle" min="2" max="10" value="5" unit="cycle" comment="Nombre de cycle"/>
  	  <processData displayName="Debatement_angulaire" dataType="java.lang.Double" defaultValue="20" id="angle" min="5" max="40" value="30" unit="°" comment="Angle de deplacement de la jambe pendant la scéance"/>
  	  <processData displayName="Vitesse_angulaire" dataType="java.lang.Double" defaultValue="20" id="vitesse" min="5" max="40" value="30" unit="mm/s" comment="Vitesse de deplacement de la jambe pendant la scéance"/>
		 
		 */
		
		/*
		// initialize your application here
		legLift.attachTo(robot.getFlange());//"Fixation" de l'outil à la bride du robot.	
		tempo = getApplicationData().getProcessData("tempo").getValue();
		nbcycle = getApplicationData().getProcessData("nbcycle").getValue();
		angle = getApplicationData().getProcessData("angle").getValue();
		vitesse = getApplicationData().getProcessData("vitesse").getValue();
		}*/

	@Override
	public void run() {
		// your application execution starts here
		robot.move(ptpHome().setJointVelocityRel(0.5));
		legLift.getFrame("/Dummy/PNP_parent").move(ptp(getApplicationData().getFrame("/Genou/P1")));
		//message variable answeranswer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est elle en place ?", "Oui","Non");
		while(answer != 0){
			ThreadUtil.milliSleep(5000);//attache la jambe
			answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est elle en place ?", "Oui","Non");
		}
		answer = -1;
		leg.getFrame("/PNP_enfant").attachTo(legLift.getFrame("/Dummy/PNP_parent"));
		for(int i=0;i<nbcycle;i++){
			leg.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(-angle),0,0).setCartVelocity(vitesse));
			leg.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(angle),0,0).setCartVelocity(vitesse));
		}
		ThreadUtil.milliSleep(tempo);//10 sec pour détacher sa jambe
		while(answer != 0){
			ThreadUtil.milliSleep(5000);//détache la jambe
			answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est elle enlevé ?", "Oui","Non");
		}
		leg.detach();//detache la jambe de l'outil en logiciel 
		robot.move(ptpHome().setJointVelocityRel(0.5));
	}
}