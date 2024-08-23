package no.hvl.tk.visual.debugger.debugging.stackframe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import no.hvl.tk.visual.debugger.debugging.visualization.TpsRouteDebuggingVisualizer;
import no.hvl.tk.visual.debugger.domain.TpsDebugData;

public class TpsStackFrameAnalyzer {
    private static final String EVALUATE_SCHEDULE_JSON_COMMAND = "new com.picnic.config.GeneralConfig().objectMapper(true).writeValueAsString(copiedSchedule.getRouteById())";
    private final XDebugSession debugSession;
    private final TpsRouteDebuggingVisualizer tpsRouteDebuggingVisualizer;
    private final ObjectMapper objectMapper;
    private final File pythonScriptFile;

    public TpsStackFrameAnalyzer(
            XDebugSession debugSession, TpsRouteDebuggingVisualizer tpsRouteDebuggingVisualizer) {
        this.debugSession = debugSession;
        pipenvInstall();

        InputStream pythonScriptInputStream = TpsRouteDebuggingVisualizer.class.getClassLoader().getResourceAsStream("scripts/single_route_visualizer.py");
        this.pythonScriptFile = copyToTempFile(pythonScriptInputStream, "script", ".py");

        this.tpsRouteDebuggingVisualizer = tpsRouteDebuggingVisualizer;
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }


    public void extractAndVisualizeSchedule() {
        XDebuggerEvaluator evaluator = debugSession.getDebugProcess().getEvaluator();
        ApplicationManager.getApplication().executeOnPooledThread(() ->
                evaluator.evaluate(
                        XExpressionImpl.fromText(EVALUATE_SCHEDULE_JSON_COMMAND),
                        new XDebuggerEvaluator.XEvaluationCallback() {
                            @Override
                            public void evaluated(XValue result) {
                                try {
                                    // had problems passing json directly, let's put it in a file for now
                                    File jsonFile = buildJsonFile(result);
                                    ProcessBuilder runScriptTask = new ProcessBuilder("pipenv", "run", "python3", pythonScriptFile.getAbsolutePath(), "--file_path", jsonFile.getAbsolutePath());
                                    TpsDebugData tpsDebugData = runPythonScriptAndGetTpsDebugData(runScriptTask);
                                    tpsRouteDebuggingVisualizer.visualize(tpsDebugData);
                                } catch (Exception e) {
                                }
                            }

                            @Override
                            public void errorOccurred(String errorMessage) {
                                System.out.println("Error evaluating expression: " + errorMessage);
                                // it means that we could not evaluate the tps debug data, therefore, don't show anything on UI.
                                tpsRouteDebuggingVisualizer.removeButtons();
                                tpsRouteDebuggingVisualizer.clearImage();
                                // and also clear the image maybe
                            }
                        },
                        null
                ));
    }

    private File buildJsonFile(XValue result) {
        var json = ((JavaValue) result).getDescriptor().getValue().toString();
        InputStream jsonInputStream = new ByteArrayInputStream(removeExtraWrappingDoubleQuotes(json).getBytes());
        return copyToTempFile(jsonInputStream, "json", ".json");
    }


    private static void runPythonTask(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        // Capture the output if needed
        process.waitFor();
        System.out.println();


        // Read standard output from the script (stdout)
        InputStream stdout = process.getInputStream();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(stdout));

        // Read standard error from the script (stderr)
        InputStream stderr = process.getErrorStream();
        BufferedReader stdError = new BufferedReader(new InputStreamReader(stderr));

        String s;
        System.out.println("Standard Output:");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        System.out.println("Error Output (if any):");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
        System.out.println();
    }

    private TpsDebugData runPythonScriptAndGetTpsDebugData(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        process.waitFor();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        BufferedReader errorInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));


        System.out.println("Error Output:");
        String s;
        while ((s = errorInput.readLine()) != null) {
            System.out.println(s);
        }

        String scriptOutput;
        System.out.println("Standard Output:");
        while ((scriptOutput = stdInput.readLine()) != null) {
            TpsDebugData routes = objectMapper.readValue(scriptOutput, TpsDebugData.class);
            return routes;
        }
        return null;
    }


    private File copyToTempFile(InputStream inputStream, String fileName, String fileType) {
        try {
            return doCopyToTempFile(inputStream, fileName, fileType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File doCopyToTempFile(InputStream inputStream, String fileName, String fileType) throws IOException {
        File tempFile = File.createTempFile(fileName, fileType);
        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }


    private static void pipenvInstall() {
        ProcessBuilder processBuilder = new ProcessBuilder("pipenv", "install", "matplotlib");
        try {
            runPythonTask(processBuilder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    String removeExtraWrappingDoubleQuotes(String input) {
        if (input != null && input.length() > 2) {
            return input.substring(1, input.length() - 1);
        }
        return null;
    }
}
