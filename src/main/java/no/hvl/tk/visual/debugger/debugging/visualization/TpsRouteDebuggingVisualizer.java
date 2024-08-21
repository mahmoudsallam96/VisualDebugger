package no.hvl.tk.visual.debugger.debugging.visualization;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBScrollPane;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import no.hvl.tk.visual.debugger.SharedState;
import no.hvl.tk.visual.debugger.ui.CopyPlantUMLDialog;

public class TpsRouteDebuggingVisualizer extends DebuggingInfoVisualizerBase {
    private static final Logger LOGGER = Logger.getInstance(TpsRouteDebuggingVisualizer.class);
    private static final String NULL = "null";
    private static final String KEY = "key";
    private static final String VALUE = "value";

    private final JPanel pluginUI;
    private JLabel imgLabel;

    public TpsRouteDebuggingVisualizer(final JPanel jPanel) {
        this.pluginUI = jPanel;
    }


    @Override
    public void visualizeFurther(TpsDebugData route) {
        // todo: process the tps objects here (write logic here)
        // aka:
        // call a python script, and build the image here, that should be it.
        try {
            // Extract Python script from resources to a temporary file
            InputStream pythonScript = TpsRouteDebuggingVisualizer.class.getClassLoader().getResourceAsStream("scripts/single_route_visualizer.py");
            InputStream sample = TpsRouteDebuggingVisualizer.class.getClassLoader().getResourceAsStream("route_model_from_algorithm_sample.json");
            if (pythonScript != null) {
                File scriptFile = copyToTempFile(pythonScript, "script", ".py");
                File sampleFile = copyToTempFile(sample, "sample", ".json");

                ProcessBuilder processBuilder = new ProcessBuilder("pipenv", "install", "matplotlib");
                ProcessBuilder processBuilder2 = new ProcessBuilder("pipenv", "run", "python3", scriptFile.getAbsolutePath(), "--file_path", sampleFile.getAbsolutePath());
                runPythonTask(processBuilder);
                String imagePath = runPythonTaskAndGetImagePath(processBuilder2);
//                Optional<BufferedImage> bufferedImage = loadPng(imagePath);
                File fi = new File(imagePath);
                byte[] pngData = Files.readAllBytes(fi.toPath());
                final var routeString = route.toString();
                SharedState.setLastRouteStringRepresentation(routeString);
//                final byte[] pngData = toImage(routeString, FileFormat.PNG);
                this.addImageToUI(pngData);
            } else {
                System.out.println("files not found in resources");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // read the image saved by the script here


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


    private Optional<BufferedImage> loadPng(String imagePath) {
        BufferedImage image = null;
        try {
            // Assume you have the path to the image (e.g., from Python script output)

            // Create a File object representing the image path
            File imageFile = new File(imagePath);

            // Load the image into a BufferedImage object
            image = ImageIO.read(imageFile);

            if (image != null) {
                // Successfully loaded the image
                System.out.println("Image loaded successfully.");
                System.out.println("Image Width: " + image.getWidth());
                System.out.println("Image Height: " + image.getHeight());

                // Further processing of the image if needed
            } else {
                System.out.println("Failed to load the image.");
            }
        } catch (IOException e) {
            // Handle exceptions when reading the image file
            System.out.println("Error loading the image: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.ofNullable(image);
    }
}
