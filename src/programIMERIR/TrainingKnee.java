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
import java.sql.*;

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
	private Tool legLift;// Création d'un objet outil
	@Inject
	@Named("Leg")
	private Workpiece leg;
	private int i;
	// var accessible via le processdata

	private Integer tempo, nbcycles;
	private Double angle, anglespeed;
	private String nom;
	// create d'un int qui recupere le choix de l'utilisateur
	private int answer;
	private String URL;
	private String login;
	private String password;
	private String sql;
	private Connection connection;
	private Statement stmt;
	private ResultSet resultat;
	private  String current_nom;

	@Override
	public void initialize() {
		// initialize your application here
		legLift.attachTo(robot.getFlange());
		tempo = getApplicationData().getProcessData("tempo").getValue();
		nbcycles = getApplicationData().getProcessData("nbcycles").getValue();
		angle = getApplicationData().getProcessData("angle").getValue();
		anglespeed = getApplicationData().getProcessData("anglespeed")
				.getValue();
		nom = getApplicationData().getProcessData("Nom").getValue();
		answer = -1;
		URL = "jdbc:mysql://172.31.1.66:3306/infos_patients";
		login = "root";
		password = "";
		connection = null;
		stmt = null;
		resultat =  null;
	}

	@Override
	public void run() {
		// se connecter a la base de données
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(URL, login, password);
			// interaction avec la base
			sql = "SELECT Prenom FROM infos_patients WHERE Nom = '$nom'";
			stmt = connection.createStatement();
			resultat = stmt.executeQuery(sql);		
			/* Récupération des données du résultat de la requête de lecture */
			while ( resultat.next() ) {
				current_nom = resultat.getString( "Prenom" );
			}
		} catch (SQLException sqle) {			
			answer = getApplicationUI().displayModalDialog(
					ApplicationDialogType.ERROR, "erreur connection", "Ok");
			sqle.printStackTrace();
		} catch (ClassNotFoundException e) {			
			answer = getApplicationUI().displayModalDialog(
					ApplicationDialogType.ERROR, "erreur ClassNotFoundException", "Ok");
			e.printStackTrace();
		} finally {
			try {
				connection.close();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		answer = getApplicationUI().displayModalDialog(
				ApplicationDialogType.QUESTION, "bonjour mm" + nom + current_nom, "Ok");
		
		// your application execution starts here
		robot.move(ptpHome());
		legLift.getFrame("/dummy/pnpParent").move(
				ptp(getApplicationData().getFrame("/Knee/P1"))
						.setJointVelocityRel(0.5));

		// initialiser answer avec le message
		answer = getApplicationUI()
				.displayModalDialog(ApplicationDialogType.QUESTION,
						"la jambe est en place?", "oui");
		ThreadUtil.milliSleep(tempo);
		// Ancrage de la jambe à l'outil
		leg.getFrame("/PnpChild")
				.attachTo(legLift.getFrame("/dummy/pnpParent"));

		while (answer != 1) {
			for (i = 1; i < nbcycles; i++) {
				leg.getFrame("TCPKnee").move(
						linRel(0, 0, 0, Math.toRadians(-angle), 0, 0)
								.setCartVelocity(anglespeed));
				leg.getFrame("TCPKnee").move(
						linRel(0, 0, 0, Math.toRadians(angle), 0, 0)
								.setCartVelocity(anglespeed));
			}
			answer = getApplicationUI().displayModalDialog(
					ApplicationDialogType.QUESTION,
					"voulez vous refaire un cycle Mr/Mme :" + nom + "?", "oui",
					"non");
		}
		answer = -1;

		answer = getApplicationUI().displayModalDialog(
				ApplicationDialogType.QUESTION,
				"Avez vous enlever la jambe Mr/Mmme:" + nom + "?", "oui");
		ThreadUtil.milliSleep(tempo);
		leg.detach();
		robot.move(ptpHome().setJointVelocityRel(0.5));
	}
}