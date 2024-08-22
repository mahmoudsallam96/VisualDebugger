package no.hvl.tk.visual.debugger.debugging.visualization;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBScrollPane;
import java.awt.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import no.hvl.tk.visual.debugger.SharedState;
import no.hvl.tk.visual.debugger.ui.CopyPlantUMLDialog;

public class TpsRouteDebuggingVisualizer extends DebuggingInfoVisualizerBase {
    private static final Logger LOGGER = Logger.getInstance(TpsRouteDebuggingVisualizer.class);

    private final JPanel pluginUI;
    private JLabel imgLabel;
    private final File pythonScriptFile;

    public TpsRouteDebuggingVisualizer(final JPanel jPanel) {
        this.pluginUI = jPanel;
        pipenvInstall();
        InputStream pythonScriptInputStream = TpsRouteDebuggingVisualizer.class.getClassLoader().getResourceAsStream("scripts/single_route_visualizer.py");
        this.pythonScriptFile = copyToTempFile(pythonScriptInputStream);

    }


    @Override
    public void visualizeFurther(TpsDebugData route) {
        try {
            // Extract Python script from resources to a temporary file
            InputStream sample = TpsRouteDebuggingVisualizer.class.getClassLoader().getResourceAsStream("route_model_from_algorithm_sample.json");
//            File sampleFile = copyToTempFile(sample, "sample", ".json");
            ProcessBuilder processBuilder2 = new ProcessBuilder("pipenv", "run", "python3", pythonScriptFile.getAbsolutePath(), "--json", route.copiedScheduleJson());
            String imagePath = runPythonTaskAndGetImagePath(processBuilder2);
            File imageFile = new File(imagePath);
            byte[] pngData = Files.readAllBytes(imageFile.toPath());
            final var routeString = route.toString();
            SharedState.setLastRouteStringRepresentation(routeString);
            this.addImageToUI(pngData);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static File copyToTempFile(InputStream inputStream, String fileName, String fileType) throws IOException {
        File tempFile = File.createTempFile(fileName, fileType);
        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
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

    private static String runPythonTaskAndGetImagePath(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        process.waitFor();

        InputStream stdout = process.getInputStream();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(stdout));


        String imagePath;
        System.out.println("Standard Output:");
        while ((imagePath = stdInput.readLine()) != null) {
            System.out.println(imagePath);
            return imagePath;
        }
        return null;
    }


    @Override
    public void debuggingActivated() {
        final var printDiagram = new JButton("Copy diagram");
        printDiagram.addActionListener(e -> new CopyPlantUMLDialog().show());
        this.pluginUI.add(printDiagram, BorderLayout.SOUTH);
    }

    @Override
    public void debuggingDeactivated() {
        // NOOP
    }

    private void addImageToUI(final byte[] pngData) throws IOException {
        final var input = new ByteArrayInputStream(pngData);
        final var imageIcon = new ImageIcon(ImageIO.read(input));

        if (this.imgLabel == null) {
            this.createImageAndAddToUI(imageIcon);
        } else {
            this.imgLabel.setIcon(imageIcon);
        }
        this.pluginUI.revalidate();
    }

    private void createImageAndAddToUI(final ImageIcon imageIcon) {
        this.imgLabel = new JLabel(imageIcon);
        final var scrollPane = new JBScrollPane(this.imgLabel);
        this.pluginUI.add(scrollPane);
    }


    public static byte[] toImage(final String plantUMLDescription, final FileFormat format)
            throws IOException {
        final var reader = new SourceStringReader(plantUMLDescription);
        try (final var outputStream = new ByteArrayOutputStream()) {
            reader.outputImage(outputStream, new FileFormatOption(format));
            return outputStream.toByteArray();
        }
    }

    private File copyToTempFile(InputStream inputStream) {
        try {
            return copyToTempFile(inputStream, "script", ".py");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

}
