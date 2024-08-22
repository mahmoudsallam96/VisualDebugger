package no.hvl.tk.visual.debugger.debugging.stackframe;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Key;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import java.awt.*;
import javax.swing.*;
import no.hvl.tk.visual.debugger.SharedState;
import no.hvl.tk.visual.debugger.debugging.visualization.DebuggingInfoVisualizer;
import no.hvl.tk.visual.debugger.debugging.visualization.TpsRouteDebuggingVisualizer;
import no.hvl.tk.visual.debugger.ui.VisualDebuggerIcons;
import org.jetbrains.annotations.NotNull;

public class TpsStackFrameSessionListener implements XDebugSessionListener {

    private static final Logger LOGGER = Logger.getInstance(TpsStackFrameSessionListener.class);

    // UI constants
    private static final String CONTENT_ID = "no.hvl.tk.VisualDebugger";
    private static final String TOOLBAR_ACTION = "VisualDebugger.VisualizerToolbar"; // has to match with plugin.xml

    private JPanel userInterface;

    private final XDebugSession debugSession;
    private TpsRouteDebuggingVisualizer tpsRouteDebuggingVisualizer;

    public TpsStackFrameSessionListener(@NotNull XDebugProcess debugProcess) {
        this.debugSession = debugProcess.getSession();
        debugProcess.getProcessHandler().addProcessListener(new ProcessListener() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                TpsStackFrameSessionListener.this.initUIIfNeeded();
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                SharedState.setLastDiagramJSON("");
            }

            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                // not relevant
            }
        });
        SharedState.setDebugListener(this);
    }

    @Override
    public void sessionStopped() {
        this.tpsRouteDebuggingVisualizer.sessionStopped();
    }

    @Override
    public void sessionPaused() {
        this.startVisualDebugging();
    }

    private void startVisualDebugging() {
        if (!SharedState.isDebuggingActive()) {
            return;
        }

        if (debugSession.getCurrentPosition() != null) {
            String fileName = debugSession.getCurrentPosition().getFile().getNameWithoutExtension();
            int line = debugSession.getCurrentPosition().getLine() + 1;
            tpsRouteDebuggingVisualizer.addMetadata(fileName, line);
        }
        TpsStackFrameAnalyzer tpsStackFrameAnalyzer = new TpsStackFrameAnalyzer(debugSession, tpsRouteDebuggingVisualizer);
        tpsStackFrameAnalyzer.extractAndVisualizeSchedule();
    }


    private void initUIIfNeeded() {
        if (this.userInterface != null) {
            return;
        }
        this.userInterface = new JPanel();
        userInterface.setLayout(new BorderLayout());
        this.getOrCreateDebuggingInfoVisualizer(); // make sure visualizer is initialized
        if (!SharedState.isDebuggingActive()) {
            this.resetUIAndAddActivateDebuggingButton();
        } else {
            this.tpsRouteDebuggingVisualizer.debuggingActivated();
        }
        final var uiContainer = new SimpleToolWindowPanel(false, true);

        final var actionManager = ActionManager.getInstance();
        final var actionToolbar = actionManager.createActionToolbar(TOOLBAR_ACTION, (DefaultActionGroup) actionManager.getAction(TOOLBAR_ACTION), false);
        actionToolbar.setTargetComponent(this.userInterface);
        uiContainer.setToolbar(actionToolbar.getComponent());
        uiContainer.setContent(this.userInterface);

        final RunnerLayoutUi ui = this.debugSession.getUI();
        final var content = ui.createContent(CONTENT_ID, uiContainer, "Visual Debugger", VisualDebuggerIcons.VD_ICON, null);
        content.setCloseable(false);
        UIUtil.invokeLaterIfNeeded(() -> ui.addContent(content));
        LOGGER.debug("UI initialized!");
    }

    public void resetUIAndAddActivateDebuggingButton() {
        this.userInterface.removeAll();
        SharedState.setEmbeddedBrowserActive(false);
        userInterface.setLayout(new BorderLayout());

        final var activateButton = new JButton("Activate visual debugger");
        activateButton.addActionListener(actionEvent -> {
            SharedState.setDebuggingActive(true);
            this.userInterface.remove(activateButton);
            this.tpsRouteDebuggingVisualizer.debuggingActivated();
            this.userInterface.revalidate();
        });
        this.userInterface.add(activateButton, BorderLayout.NORTH);

        this.userInterface.revalidate();
        this.userInterface.repaint();
    }

    @NotNull
    public DebuggingInfoVisualizer getOrCreateDebuggingInfoVisualizer() {
        if (this.tpsRouteDebuggingVisualizer == null) {
            this.tpsRouteDebuggingVisualizer = new TpsRouteDebuggingVisualizer(this.userInterface);
        }
        return tpsRouteDebuggingVisualizer;
    }

    public void reprintRoute() {
        this.tpsRouteDebuggingVisualizer.reprintRoute();
    }
}
