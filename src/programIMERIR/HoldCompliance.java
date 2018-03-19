package programIMERIR;


import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.math.*;

import javax.inject.Inject;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.conditionModel.ConditionObserver;
import com.kuka.roboticsAPI.conditionModel.ForceCondition;
import com.kuka.roboticsAPI.conditionModel.JointTorqueCondition;
import com.kuka.roboticsAPI.deviceModel.JointEnum;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.executionModel.IFiredTriggerInfo;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.math.CoordinateAxis;
import com.kuka.roboticsAPI.motionModel.IMotion;
import com.kuka.roboticsAPI.motionModel.Motion;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.roboticsAPI.motionModel.controlModeModel.JointImpedanceControlMode;
import com.kuka.roboticsAPI.sensorModel.ForceSensorData;
import com.sun.org.apache.xerces.internal.parsers.AbstractDOMParser;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import com.kuka.roboticsAPI.conditionModel.*;
import com.kuka.roboticsAPI.geometricModel.math.*;

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
public class HoldCompliance extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	
	@Inject
	private CartesianImpedanceControlMode freeMode;
	private ForceCondition grabForce;
	private ConditionObserver grabForceObserver;
	private ForceSensorData data;
	private boolean moving = false;

	private ICallbackAction triggerFreeMove = new ICallbackAction() {
		
		@Override
		public void onTriggerFired(IFiredTriggerInfo triggerInformation) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private IRisingEdgeListener grabForceListener = new IRisingEdgeListener() {
		@Override
		public void onRisingEdge(ConditionObserver conditionObserver, Date time,
				int missedEvents) {
			// Méthode appeler lorsque une force plus fote a 10 N est appliquée
			getLogger().info("Action triggered");
			//robot.move(ptp(getApplicationData().getFrame("/Foam/P1")));
			moving = true;
			freeMovementRobot();
		}
	};
	
	@Override
	public void initialize() {
		// initialize your application here
		freeMode = new CartesianImpedanceControlMode();
		freeMode.parametrize(CartDOF.X,CartDOF.Y,CartDOF.Z).setStiffness(10);
		freeMode.parametrize(CartDOF.A,CartDOF.B,CartDOF.C).setStiffness(5);
		grabForce = ForceCondition.createSpatialForceCondition(robot.getFlange(), 10);
		grabForceObserver = getObserverManager().createConditionObserver(grabForce, NotificationType.EdgesOnly,grabForceListener);
	}

	@Override
	public void run() {
		// your application execution starts here
		
		robot.move(ptp(getApplicationData().getFrame("/WorkingTable/WaitingPoint")));
		grabForceObserver.enable();
		//getObserverManager().waitFor(grabForce);
		while(true){
			ThreadUtil.milliSleep(1000);
		}
			
		
		//lBR_iiwa_14_R820_1.move(positionHold(mode, -1, TimeUnit.SECONDS));
		//getLogger().info("Fin de l'application");
	}
	
	public void freeMovementRobot(){
		Vector force;
		double sumForces;
		do{
			data = robot.getExternalForceTorque(robot.getFlange());
			force = data.getForce();
			sumForces = Math.abs(force.getX()) + Math.abs(force.getY())	+ Math.abs(force.getZ());
			getLogger().info("forces : " + force.getX() + " , " + force.getY() + " , " + force.getZ());
			getLogger().info("Somme forces = " + sumForces);
			robot.move(positionHold(freeMode,1, TimeUnit.SECONDS));
		}while(sumForces >= 5);
		
		getLogger().info("Sortie du while moving freely");
		double[] jointPosition = robot.getCurrentJointPosition().get();
		robot.move(ptp(jointPosition));
	}
}