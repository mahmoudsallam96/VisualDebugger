package no.hvl.tk.visual.debugger.debugging.visualization;

public interface DebuggingInfoVisualizer {
    void addMetadata(String fileName, Integer line);

    void debuggingActivated();

    void debuggingDeactivated();

    void sessionStopped();
}
