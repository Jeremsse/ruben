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
	@Inject
	@Named("Leg1k5")
	private Workpiece leg1k5;
	
	//variable accessible via process data
	private Integer tempo, 
					nbcycle;
	private Double angle;
	private Double vitesse;
	private String name;
	
	//---------------------
	private int answer;
	private String nom;
	/*
	static enum name{
		JACK,
		MARIE,
		PIERRE;
	}
	*/
	
	private enum Personne {
	    PIERRE, PAUL, JACK;
	}
	
	@Override
	public void initialize() {
		legLift.attachTo(robot.getFlange());//"Fixation" de l'outil à la bride du robot.
		nom = getApplicationData().getProcessData("name").getValue();
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
		//choix nom

		Personne personne = Personne.valueOf(nom); // surround with try/catch
		
		switch (personne) {
	    case PIERRE:
	    	tempo = getApplicationData().getProcessData("tempo").getValue();
			nbcycle = getApplicationData().getProcessData("nbcycle").getValue();
			angle = getApplicationData().getProcessData("angle").getValue();
			vitesse = getApplicationData().getProcessData("vitesse").getValue();
			
			// your application execution starts here
			robot.move(ptpHome().setJointVelocityRel(0.5));
			legLift.getFrame("/Dummy/PNP_parent").move(ptp(getApplicationData().getFrame("/Genoux/P1")));
			//message variable answeranswer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est elle en place ?", "Oui","Non");
			while(answer != 0){
				ThreadUtil.milliSleep(5000);//attache la jambe
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe de "+nom+" est elle en place ?", "Oui","Non");
			}
			answer = -1;
			leg.getFrame("/PNP_enfant").attachTo(legLift.getFrame("/Dummy/PNP_parent"));
			while(answer !=1){
				for(int i=0;i<nbcycle;i++){
					leg.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(-angle),0,0).setCartVelocity(vitesse));
					leg.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(angle),0,0).setCartVelocity(vitesse));
				}
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Voulez vous refaire un cycle ?", "Oui","Non");
			}
			answer = -1;
			ThreadUtil.milliSleep(tempo);//10 sec pour détacher sa jambe
			while(answer != 0){
				ThreadUtil.milliSleep(5000);//détache la jambe
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe de "+nom+" est elle enlevée ?", "Oui","Non");
			}
			answer = -1;
			leg.detach();//detache la jambe de l'outil en logiciel 
			robot.move(ptpHome().setJointVelocityRel(0.5));
	        break;
	        
	    case PAUL:
	    	tempo = getApplicationData().getProcessData("tempo").getValue();
			nbcycle = getApplicationData().getProcessData("nbcycle").getValue();
			angle = getApplicationData().getProcessData("angle").getValue();
			vitesse = getApplicationData().getProcessData("vitesse").getValue();
			
			// your application execution starts here
			robot.move(ptpHome().setJointVelocityRel(0.5));
			legLift.getFrame("/Dummy/PNP_parent").move(ptp(getApplicationData().getFrame("/Genoux/P1")));
			//message variable answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est elle en place ?", "Oui","Non");
			while(answer != 0){
				ThreadUtil.milliSleep(5000);//attache la jambe
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe de "+nom+" est elle en place ?", "Oui","Non");
			}
			answer = -1;
			leg1k5.getFrame("/PNP_enfant").attachTo(legLift.getFrame("/Dummy/PNP_parent"));
			robot.setSafetyWorkpiece(leg1k5); //déclare en sécurité
			while(answer !=1){
				for(int i=0;i<nbcycle;i++){
					leg1k5.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(-angle),0,0).setCartVelocity(vitesse));
					leg1k5.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(angle),0,0).setCartVelocity(vitesse));
				}
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Voulez vous refaire un cycle ?", "Oui","Non");
			}
			answer = -1;
			ThreadUtil.milliSleep(tempo);//10 sec pour détacher sa jambe
			while(answer != 0){
				ThreadUtil.milliSleep(5000);//détache la jambe
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe de "+nom+" est elle enlevée ?", "Oui","Non");
			}
			answer = -1;
			leg1k5.detach();//detache la jambe de l'outil en logiciel
			//robot.setSafetyWorkpiece(null);
			robot.move(ptpHome().setJointVelocityRel(0.5));
	        break;
	        
	    case JACK:
	    	tempo = getApplicationData().getProcessData("tempo").getValue();
			nbcycle = getApplicationData().getProcessData("nbcycle").getValue();
			angle = getApplicationData().getProcessData("angle").getValue();
			vitesse = getApplicationData().getProcessData("vitesse").getValue();
	        break;
	        
	    default :
	    	getApplicationUI().displayModalDialog(ApplicationDialogType.ERROR, "Le nom séléctionné n'existe pas, pensez à écrire le nom en majuscule");
	    	break;
		}
		/*
		if (nom.equals("Pierre")){
			tempo = getApplicationData().getProcessData("tempo").getValue();
			nbcycle = getApplicationData().getProcessData("nbcycle").getValue();
			angle = getApplicationData().getProcessData("angle").getValue();
			vitesse = getApplicationData().getProcessData("vitesse").getValue();
			
			// your application execution starts here
			robot.move(ptpHome().setJointVelocityRel(0.5));
			legLift.getFrame("/Dummy/PNP_parent").move(ptp(getApplicationData().getFrame("/Genoux/P1")));
			//message variable answeranswer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est elle en place ?", "Oui","Non");
			while(answer != 0){
				ThreadUtil.milliSleep(5000);//attache la jambe
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe de "+nom+" est elle en place ?", "Oui","Non");
			}
			answer = -1;
			leg.getFrame("/PNP_enfant").attachTo(legLift.getFrame("/Dummy/PNP_parent"));
			while(answer !=1){
				for(int i=0;i<nbcycle;i++){
					leg.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(-angle),0,0).setCartVelocity(vitesse));
					leg.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(angle),0,0).setCartVelocity(vitesse));
				}
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Voulez vous refaire un cycle ?", "Oui","Non");
			}
			answer = -1;
			ThreadUtil.milliSleep(tempo);//10 sec pour détacher sa jambe
			while(answer != 0){
				ThreadUtil.milliSleep(5000);//détache la jambe
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe de "+nom+" est elle enlevé ?", "Oui","Non");
			}
			answer = -1;
			leg.detach();//detache la jambe de l'outil en logiciel 
			robot.move(ptpHome().setJointVelocityRel(0.5));
		}
		
		else if (nom.equals("Paul")){
			tempo = getApplicationData().getProcessData("tempo").getValue();
			nbcycle = getApplicationData().getProcessData("nbcycle").getValue();
			angle = getApplicationData().getProcessData("angle").getValue();
			vitesse = getApplicationData().getProcessData("vitesse").getValue();
			
			// your application execution starts here
			robot.move(ptpHome().setJointVelocityRel(0.5));
			legLift.getFrame("/Dummy/PNP_parent").move(ptp(getApplicationData().getFrame("/Genoux/P1")));
			//message variable answeranswer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe du patient est elle en place ?", "Oui","Non");
			while(answer != 0){
				ThreadUtil.milliSleep(5000);//attache la jambe
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe de "+nom+" est elle en place ?", "Oui","Non");
			}
			answer = -1;
			leg1k5.getFrame("/PNP_enfant").attachTo(legLift.getFrame("/Dummy/PNP_parent"));
			while(answer !=1){
				for(int i=0;i<nbcycle;i++){
					leg1k5.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(-angle),0,0).setCartVelocity(vitesse));
					leg1k5.getFrame("Genoux").move(linRel(0,0,0,Math.toRadians(angle),0,0).setCartVelocity(vitesse));
				}
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "Voulez vous refaire un cycle ?", "Oui","Non");
			}
			answer = -1;
			ThreadUtil.milliSleep(tempo);//10 sec pour détacher sa jambe
			while(answer != 0){
				ThreadUtil.milliSleep(5000);//détache la jambe
				answer=getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "La jambe de "+nom+" est elle enlevé ?", "Oui","Non");
			}
			answer = -1;
			leg1k5.detach();//detache la jambe de l'outil en logiciel 
			robot.move(ptpHome().setJointVelocityRel(0.5));
		}
		else if (nom.equals("Jack")){
			tempo = getApplicationData().getProcessData("tempo").getValue();
			nbcycle = getApplicationData().getProcessData("nbcycle").getValue();
			angle = getApplicationData().getProcessData("angle").getValue();
			vitesse = getApplicationData().getProcessData("vitesse").getValue();
		}
		*/
	}
}