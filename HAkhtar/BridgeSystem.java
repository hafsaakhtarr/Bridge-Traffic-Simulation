package HAkhtar;
import BridgeSegment.*;

public class BridgeSystem extends AbstractBridgeSystem {

    protected BridgeSegment bridgeSegment1;  // East
    protected BridgeSegment bridgeSegment2;  // Middle
    protected BridgeSegment bridgeSegment3;  // West

    public BridgeSystem() {
        this("BridgeSystem");
    }

    public BridgeSystem(String name) {
        super(name);
        bridgeSegment1 = new BridgeSegment("BridgeSegment1");
        bridgeSegment2 = new BridgeSegment("BridgeSegment2");
        bridgeSegment3 = new BridgeSegment("BridgeSegment3");
        add(this.westCarGenerator);
        add(this.eastCarGenerator);
        add(bridgeSegment1);
        add(bridgeSegment2);
        add(bridgeSegment3);
        add(this.transduser);

        setupConnections();
    }

    private void setupConnections() {
        // Car generator
        addCoupling(westCarGenerator, "out", bridgeSegment3, "eastbound_in");  // Cars go west→east
        addCoupling(eastCarGenerator, "out", bridgeSegment1, "westbound_in");  // Cars go east→west

        // Bridge (west→east flow)
        addCoupling(bridgeSegment3, "eastbound_out", bridgeSegment2, "eastbound_in");
        addCoupling(bridgeSegment2, "eastbound_out", bridgeSegment1, "eastbound_in");

        // Bridge (east→west flow)
        addCoupling(bridgeSegment1, "westbound_out", bridgeSegment2, "westbound_in");
        addCoupling(bridgeSegment2, "westbound_out", bridgeSegment3, "westbound_in");

        // Transducer connections
        addCoupling(bridgeSegment1, "eastbound_out", this.transduser, "Bridge1_EastOut");
        addCoupling(bridgeSegment1, "westbound_out", this.transduser, "Bridge1_WestOut");
        addCoupling(bridgeSegment2, "eastbound_out", this.transduser, "Bridge2_EastOut");
        addCoupling(bridgeSegment2, "westbound_out", this.transduser, "Bridge2_WestOut");
        addCoupling(bridgeSegment3, "eastbound_out", this.transduser, "Bridge3_EastOut");
        addCoupling(bridgeSegment3, "westbound_out", this.transduser, "Bridge3_WestOut");
    }

}
