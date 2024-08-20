package no.hvl.tk.visual.debugger.settings;

public enum DebuggingVisualizerOption {
    /**
     * Embedded visualizer using plant uml.
     */
    EMBEDDED;

    @Override
    public String toString() {
        return switch (this) {
            case EMBEDDED -> "Embedded visualizer (no interaction)";
        };
    }
}
