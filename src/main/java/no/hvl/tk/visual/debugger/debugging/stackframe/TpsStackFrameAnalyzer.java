package no.hvl.tk.visual.debugger.debugging.stackframe;

import com.intellij.debugger.engine.JavaValue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import no.hvl.tk.visual.debugger.debugging.visualization.TpsDebugData;
import no.hvl.tk.visual.debugger.debugging.visualization.TpsRouteDebuggingVisualizer;

public class TpsStackFrameAnalyzer {
    private static final String EVALUATE_SCHEDULE_JSON_COMMAND = "new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(copiedSchedule.getRouteById())";
    private final XDebugSession debugSession;
    private final TpsRouteDebuggingVisualizer tpsRouteDebuggingVisualizer;


    public TpsStackFrameAnalyzer(
            XDebugSession debugSession, TpsRouteDebuggingVisualizer tpsRouteDebuggingVisualizer) {
        this.debugSession = debugSession;
        this.tpsRouteDebuggingVisualizer = tpsRouteDebuggingVisualizer;
    }


    public void extractAndVisualizeSchedule() {
        XDebuggerEvaluator evaluator = debugSession.getDebugProcess().getEvaluator();
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                evaluator.evaluate(
                        XExpressionImpl.fromText(EVALUATE_SCHEDULE_JSON_COMMAND),
                        new XDebuggerEvaluator.XEvaluationCallback() {
                            @Override
                            public void evaluated(XValue result) {
                                var copiedScheduleJson = ((JavaValue) result).getDescriptor().getValue().toString();
                                // probably should draw here
                                tpsRouteDebuggingVisualizer.visualizeFurther(new TpsDebugData(copiedScheduleJson));
                            }

                            @Override
                            public void errorOccurred(String errorMessage) {
                                System.out.println("Error evaluating expression: " + errorMessage);
                            }
                        },
                        null
                ));

    }


    String trimJson(String input) {
        if (input != null && input.length() > 2) {
            return input.substring(1, input.length() - 1);
        }
        return null;
    }
}
