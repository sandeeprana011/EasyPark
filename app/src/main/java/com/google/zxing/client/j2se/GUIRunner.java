package com.google.zxing.client.j2se;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public final class GUIRunner extends JFrame {
    private final JLabel imageLabel;
    private final JTextArea textArea;

    private GUIRunner() {
        this.imageLabel = new JLabel();
        this.textArea = new JTextArea();
        this.textArea.setEditable(false);
        this.textArea.setMaximumSize(new Dimension(400, 200));
        Container panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(this.imageLabel);
        panel.add(this.textArea);
        setTitle("ZXing");
        setSize(400, 400);
        setDefaultCloseOperation(3);
        setContentPane(panel);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) throws MalformedURLException {
        GUIRunner runner = new GUIRunner();
        runner.setVisible(true);
        runner.chooseImage();
    }

    private void chooseImage() throws MalformedURLException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(this);
        File file = fileChooser.getSelectedFile();
        Icon imageIcon = new ImageIcon(file.toURI().toURL());
        setSize(imageIcon.getIconWidth(), imageIcon.getIconHeight() + 100);
        this.imageLabel.setIcon(imageIcon);
        this.textArea.setText(getDecodeText(file));
    }

    private static String getDecodeText(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return "Could not decode image";
            }
            try {
                return String.valueOf(new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)))).getText());
            } catch (ReaderException re) {
                return re.toString();
            }
        } catch (IOException ioe) {
            return ioe.toString();
        }
    }
}
