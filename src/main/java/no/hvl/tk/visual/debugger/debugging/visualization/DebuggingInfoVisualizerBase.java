package no.hvl.tk.visual.debugger.debugging.visualization;

import no.hvl.tk.visual.debugger.SharedState;

public abstract class DebuggingInfoVisualizerBase implements DebuggingInfoVisualizer {
    private TpsDebugData route;

    protected DebuggingInfoVisualizerBase() {
        this.route = new TpsDebugData("adsf");
    }

    @Override
    public void addMetadata(String fileName, Integer line) {
        SharedState.setDebugLine(line);
        SharedState.setDebugFileName(fileName);
    }

    public void reprintRoute() {
        this.visualizeFurther(route);
    }


    protected abstract void visualizeFurther(TpsDebugData route);


    @Override
    public void sessionStopped() {
        SharedState.getManuallyExploredObjects().clear();
    }


    // todo: implement
    public void visualize(TpsDebugData route) {
        this.route = route;
        this.visualizeFurther(route);
    }
}
