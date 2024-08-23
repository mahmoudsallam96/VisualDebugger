package no.hvl.tk.visual.debugger.debugging.visualization;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBScrollPane;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import no.hvl.tk.visual.debugger.SharedState;
import no.hvl.tk.visual.debugger.domain.TpsDebugData;
import no.hvl.tk.visual.debugger.ui.CopyPlantUMLDialog;

public class TpsRouteDebuggingVisualizer extends DebuggingInfoVisualizerBase {
    private static final Logger LOGGER = Logger.getInstance(TpsRouteDebuggingVisualizer.class);

    private final JPanel pluginUI;
    private JLabel imgLabel;
    private String currentImageRouteId;


    public TpsRouteDebuggingVisualizer(final JPanel jPanel) {
        this.pluginUI = jPanel;
    }


    @Override
    public void visualize(TpsDebugData routes) {
        try {
            SharedState.setLastRouteStringRepresentation(routes.toString());
            createButtonsAsManyAsThereAreRoutes(routes);
            // there can be an existing image from previous debug step, if so we need to update it
            if (currentImageRouteId != null) {
                String imagePath = routes.getRouteImageById().get(currentImageRouteId);
                byte[] image = createImage(imagePath);
                addOrUpdateImageOnUi(image, currentImageRouteId);
            }

        } catch (Exception e) {
            e.printStackTrace();
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

    private void addOrUpdateImageOnUi(final byte[] pngData, String routeId) {
        final var input = new ByteArrayInputStream(pngData);
        final ImageIcon imageIcon;
        try {
            imageIcon = new ImageIcon(ImageIO.read(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (this.imgLabel == null) {
            this.createImageAndAddToUI(imageIcon);
        } else {
            this.imgLabel.setIcon(imageIcon);
        }
        this.currentImageRouteId = routeId;
        this.pluginUI.revalidate();
    }

    private void createImageAndAddToUI(final ImageIcon imageIcon) {
        this.imgLabel = new JLabel(imageIcon);
        final var scrollPane = new JBScrollPane(this.imgLabel);
        this.pluginUI.add(scrollPane);
    }

    private void createButtonsAsManyAsThereAreRoutes(final TpsDebugData routes) {
        // Create a panel to hold the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // Add buttons to the panel
        for (Map.Entry<String, String> routeIdAndImagePath : routes.getRouteImageById().entrySet()) {
            String routeId = routeIdAndImagePath.getKey();
            JButton routeButton = new JButton(routeId);

            routeButton.addActionListener(actionEvent ->
                    addOrUpdateImageOnUi(createImage(routeIdAndImagePath.getValue()), routeId));

            buttonPanel.add(routeButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));  // 10px horizontal space
        }

        // Create a scroll pane to make the button panel scrollable
        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); // Always show horizontal scrollbar
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); // Hide vertical scrollbar

        // Set the preferred size of the scroll pane
        scrollPane.setPreferredSize(new Dimension(800, 100)); // Example fixed size (width x height)

        // Remove previous components from pluginUI and add the scroll pane
        this.pluginUI.removeAll();
        this.pluginUI.setLayout(new BorderLayout());
        this.pluginUI.add(scrollPane, BorderLayout.CENTER);

        this.pluginUI.revalidate();
        this.pluginUI.repaint();
    }


    private static byte[] createImage(String imagePath) {
        File imageFile = new File(imagePath);
        try {
            byte[] pngData = Files.readAllBytes(imageFile.toPath());
            return pngData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeButtons() {
        for (Component comp : pluginUI.getComponents()) {
            if (comp instanceof JButton) {
                pluginUI.remove(comp);
            }
        }
    }

    public void clearImage() {
        if (imgLabel != null && imgLabel.getIcon() != null) {
            imgLabel.setIcon(null);
            currentImageRouteId = null;
            pluginUI.repaint();
        }
    }
}
