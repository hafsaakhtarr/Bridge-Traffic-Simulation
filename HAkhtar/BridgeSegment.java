package HAkhtar;
import BridgeSegment.*;
import simView.*;
import genDevs.modeling.*;
import GenCol.*;

public class BridgeSegment extends ViewableAtomic {

    protected DEVSQueue eastboundQueue, westboundQueue;
    protected entity currentVehicle = null;
    protected double greenTime = 100;
    protected double crossTime = 10;
    protected String currentGreenDirection = "WEST_TO_EAST";
    protected double lightSwitchTime; //The time when light has to switch

    public BridgeSegment() {
        this("BridgeSegment");
    }

    public BridgeSegment(String name) {
        super(name);

        addInport("eastbound_in");
        addInport("westbound_in");
        addOutport("eastbound_out");
        addOutport("westbound_out");

        eastboundQueue = new DEVSQueue();
        westboundQueue = new DEVSQueue();
    }

    public void initialize() {
        if (name.equals("BridgeSegment1")) {
            currentGreenDirection = (AbstractBridgeSystem.BridgeSystemSetting.Bridge1InitialState ==
                    AbstractBridgeSystem.BridgeState.WEST_TO_EAST) ? "WEST_TO_EAST" : "EAST_TO_WEST";
            greenTime = AbstractBridgeSystem.BridgeSystemSetting.Bridge1TrafficLightDurationTime;
        } else if (name.equals("BridgeSegment2")) {
            currentGreenDirection = (AbstractBridgeSystem.BridgeSystemSetting.Bridge2InitialState ==
                    AbstractBridgeSystem.BridgeState.WEST_TO_EAST) ? "WEST_TO_EAST" : "EAST_TO_WEST";
            greenTime = AbstractBridgeSystem.BridgeSystemSetting.Bridge2TrafficLightDurationTime;
        } else if (name.equals("BridgeSegment3")) {
            currentGreenDirection = (AbstractBridgeSystem.BridgeSystemSetting.Bridge3InitialState ==
                    AbstractBridgeSystem.BridgeState.WEST_TO_EAST) ? "WEST_TO_EAST" : "EAST_TO_WEST";
            greenTime = AbstractBridgeSystem.BridgeSystemSetting.Bridge3TrafficLightDurationTime;
        }

        lightSwitchTime = greenTime;
        holdIn("idle", greenTime);
    }

    public void deltext(double e, message x) {
        Continue(e);
        for (int i = 0; i < x.getLength(); i++) {
            if (messageOnPort(x, "eastbound_in", i)) {
                entity vehicle = x.getValOnPort("eastbound_in", i);
                eastboundQueue.add(vehicle);
            }
            if (messageOnPort(x, "westbound_in", i)) {
                entity vehicle = x.getValOnPort("westbound_in", i);
                westboundQueue.add(vehicle);
            }
        }

        if (phaseIs("idle")) {
            tryStartCrossing();
        }
    }

    private void tryStartCrossing() {
        double currentTime = getSimulationTime();
        double timeUntilSwitch = lightSwitchTime - currentTime;

        if (timeUntilSwitch >= crossTime) {
            if (currentGreenDirection.equals("WEST_TO_EAST") && !eastboundQueue.isEmpty()) {
                currentVehicle = (entity) eastboundQueue.removeFirst();
                holdIn("crossing", crossTime);
            } else if (currentGreenDirection.equals("EAST_TO_WEST") && !westboundQueue.isEmpty()) {
                currentVehicle = (entity) westboundQueue.removeFirst();
                holdIn("crossing", crossTime);
            } else {
                // Vehicle cannot cross so wait for light switch
                holdIn("idle", timeUntilSwitch);
            }
        } else {
            // Not enough time, wait for light switch to change to other direction
            holdIn("idle", timeUntilSwitch);
        }
    }

    public void deltint() {
        if (phaseIs("crossing")) {
            // Crossing complete of previous vehicle
            currentVehicle = null;

            double currentTime = getSimulationTime();
            double timeUntilSwitch = lightSwitchTime - currentTime;

            if (timeUntilSwitch <= 0) {
                // Time to switch light since the length of signal time has finished
                switchTrafficLight();
            } else {
                // Try another crossing if there is still time remaining to switch signal
                tryStartCrossing();
            }

        } else if (phaseIs("idle")) {
            switchTrafficLight();
        }
    }

    private void switchTrafficLight() {
        // Switch direction of traffic light
        if (currentGreenDirection.equals("WEST_TO_EAST")) {
            currentGreenDirection = "EAST_TO_WEST";
        } else {
            currentGreenDirection = "WEST_TO_EAST";
        }

        lightSwitchTime = getSimulationTime() + greenTime;
        holdIn("idle", greenTime);
        tryStartCrossing();
    }

    public void deltcon(double e, message x) {
        deltext(e, x);
        deltint();
    }

    public message out() {
        message m = new message();

        if (currentVehicle != null && phaseIs("crossing")) {
            if (currentGreenDirection.equals("WEST_TO_EAST")) {
                content con = makeContent("eastbound_out", currentVehicle);
                m.add(con);
            } else {
                content con = makeContent("westbound_out", currentVehicle);
                m.add(con);
            }
        }

        return m;
    }

    public String getTooltipText() {
        return super.getTooltipText() +
                "\nEastbound queue: " + eastboundQueue.size() +
                "\nWestbound queue: " + westboundQueue.size() +
                "\nDirection: " + currentGreenDirection +
                "\nCrossing: " + (currentVehicle != null ? currentVehicle.getName() : "none");
    }
}