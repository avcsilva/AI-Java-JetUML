package br.uefs.umleditor.model.edges;

/**
 * A call from a caller activation to the activation it creates on the callee
 * (RF27). A constructor call is labeled «create» and goes to the box of the
 * implicit parameter it creates (RF29).
 */
public class CallEdge extends LabeledEdge {

    public static final String CREATE_LABEL = "«create»";

    private boolean constructorCall;

    public boolean isConstructorCall() {
        return constructorCall;
    }

    public void setConstructorCall(boolean constructorCall) {
        this.constructorCall = constructorCall;
    }
}
