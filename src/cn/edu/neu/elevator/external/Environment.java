package cn.edu.neu.elevator.external;

import cn.edu.neu.elevator.Main;
import cn.edu.neu.elevator.control.ElevatorController;
import cn.edu.neu.elevator.external.DoorSensor.DoorState;
import cn.edu.neu.elevator.util.ElevatorLogger;

/**
 * Elevator Environment Simulation Class
 * As there is only one environment for one independent elevator system
 * Singleton design is applied to the environment simulation class
 */
public class Environment {
    private static Environment INSTANCE;

    private DoorSensor doorSensor;
    private ElevatorPanel elevatorPanel;
    private FloorSensor floorSensor;
    private Thread elevatorMotorThread;
    private Thread doorMotorThread;

    private Environment() {
        doorSensor = new DoorSensor();
        elevatorPanel = new ElevatorPanel();
        floorSensor = new FloorSensor();
    }

    /**
     * Get singleton instance of the Environment
     *
     * @return the instance reference
     */
    public static Environment getInstance() {
        if (INSTANCE == null) {
            synchronized (Environment.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Environment();
                }
            }
        }
        return INSTANCE;
    }

    public void registerElevatorController(ElevatorController controller) {
        doorSensor.attachListener(controller);
        elevatorPanel.attachListener(controller);
        floorSensor.attachListener(controller);
    }

    /**
     * Simulation the running state of motor in environment
     *
     * @param isGoingUp if the running direction upwards
     */
    public void runElevatorMotor(boolean isGoingUp) {
        final double runDistancePerStep = isGoingUp ? 0.2 : -0.2;
        elevatorMotorThread = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    Main.getDisplay().adjustSliderHeight(runDistancePerStep);
                    // simulate the time delay of elevator motor in physical environment
                    Thread.sleep(100);
                }
                floorSensor.floorReached();
            } catch (InterruptedException e) {
            }
        });
        elevatorMotorThread.start();
    }

    public void interruptElevatorMotor() {
        elevatorMotorThread.interrupt();
    }

    /**
     * Simulation the running state of motor in environment
     *
     * @param isGoingOpen if the door is going to openDoor
     */
    public void runDoorMotor(boolean isGoingOpen) {
        final double doorMoveDistancePerStep = isGoingOpen ? 0.1 : -0.1;
        doorMotorThread = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    Main.getDisplay().adjustDoorWidth(doorMoveDistancePerStep);
                    // simulate the time delay of door motor in physical environment
                    Thread.sleep(50);
                }
                doorSensor.setCurrentDoorState(isGoingOpen ? DoorState.OPEN : DoorState.CLOSED);
            } catch (InterruptedException e) {
                ElevatorLogger.info("Door Motor", "Motor has been interrupted by a block signal.");
            }
        });
        doorMotorThread.start();
    }

    public void interruptDoorMotor() {
        doorMotorThread.interrupt();
    }

    /**
     * Notify the panel that a button has been pressed by user
     *
     * @param buttonPressed the button pressed
     */
    public void pressButton(ElevatorButton buttonPressed) {
        elevatorPanel.press(buttonPressed);
    }

    /**
     * Trigger the door sensor to simulation the block of door
     */
    public void onDoorBlocked() {
        doorSensor.setCurrentDoorState(DoorState.BLOCKED);
    }
}
