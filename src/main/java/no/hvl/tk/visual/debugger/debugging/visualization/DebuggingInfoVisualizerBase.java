package no.hvl.tk.visual.debugger.debugging.visualization;

import no.hvl.tk.visual.debugger.SharedState;
import no.hvl.tk.visual.debugger.domain.TpsDebugData;

public abstract class DebuggingInfoVisualizerBase implements DebuggingInfoVisualizer {
    private TpsDebugData tpsDebugData;

    protected DebuggingInfoVisualizerBase() {
        this.tpsDebugData = new TpsDebugData();
    }

    @Override
    public void addMetadata(String fileName, Integer line) {
        SharedState.setDebugLine(line);
        SharedState.setDebugFileName(fileName);
    }

    public void reprintRoute() {
        this.visualize(tpsDebugData);
    }


    protected abstract void visualize(TpsDebugData route);


    @Override
    public void sessionStopped() {
        SharedState.getManuallyExploredObjects().clear();
    }
}
