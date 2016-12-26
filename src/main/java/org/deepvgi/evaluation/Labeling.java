package org.deepvgi.evaluation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by John on 12/26/16.
 * help manually label the ground truth of the testing data
 */
public class Labeling {
    private static int image_height;
    private static int image_width;
    private static int rate = 2; //enlarge or narrow the size

    public static void main(String args[]) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        image_height = Integer.parseInt(properties.getProperty("image_height"));
        image_width = Integer.parseInt(properties.getProperty("image_width"));

        String img_f = "156943_142695_18.jpeg";
        File f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + img_f);
        BufferedImage image = ImageIO.read(f);

        Image dimg = image.getScaledInstance(image_width*rate, image_height*rate, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(dimg);
        JLabel imageLabel = new JLabel(icon);
        imageLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX()/rate;
                int y = e.getY()/rate;
                String s = img_f + ";" + x + ";" + y;
                System.out.println(s);
                JOptionPane.showMessageDialog(imageLabel, "x: " + x + ", y: " + y);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

        });
        imageLabel.validate();
        JFrame frame = new JFrame("Image Labeling Swing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(imageLabel);
        frame.pack();
        frame.setVisible(true);
    }
}
