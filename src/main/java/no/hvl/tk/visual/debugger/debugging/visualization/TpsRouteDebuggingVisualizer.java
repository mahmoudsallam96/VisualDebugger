package no.hvl.tk.visual.debugger.debugging.visualization;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBScrollPane;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import no.hvl.tk.visual.debugger.SharedState;
import no.hvl.tk.visual.debugger.domain.ODAttributeValue;
import no.hvl.tk.visual.debugger.domain.ODLink;
import no.hvl.tk.visual.debugger.domain.ODObject;
import no.hvl.tk.visual.debugger.domain.ObjectDiagram;
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
        // todo: process the tps objects here
        // aka:
        // call a python script, and build the image here, that should be it.
        final var routeString = route.toString();
        SharedState.setLastRouteStringRepresentation(routeString);
        try {
            final byte[] pngData = toImage(routeString, FileFormat.PNG);
            this.addImageToUI(pngData);
        } catch (final IOException e) {
            LOGGER.error(e);
        }
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
}
