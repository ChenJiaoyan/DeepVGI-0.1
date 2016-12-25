package org.deepvgi.evaluation;

import org.datavec.api.util.ClassPathResource;
import org.deepvgi.vgi.VGI_Files;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by john on 24.12.16.
 * Manually label the ground truth of the testing data
 */
public class GroundTruth {

    private String gt_file;
    private ArrayList<String> test_images;
    private ArrayList<String> p_test_images;
    private ArrayList<String> n_test_images;
    private ArrayList<String[]> p_pixels;

    private static int tile_height;
    private static int tile_width;

    public GroundTruth(String file_name) throws IOException {
        this.gt_file = new ClassPathResource(file_name).getFile().getPath();
        this.test_images = VGI_Files.loadImageName("test");
        this.p_test_images = new ArrayList<>();
        this.n_test_images = new ArrayList<>();
        this.p_pixels = new ArrayList<>();

        Properties properties = new Properties();
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
        properties.load(inputStream);
        tile_height = Integer.parseInt(properties.getProperty("tile_height"));
        tile_width = Integer.parseInt(properties.getProperty("tile_width"));

        File f = new File(this.gt_file);
        InputStreamReader read = new InputStreamReader(new FileInputStream(f));
        BufferedReader bufferedReader = new BufferedReader(read);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] items = line.split(";");
            this.p_test_images.add(items[0]);
            this.p_pixels.add(items);
        }
        bufferedReader.close();
        read.close();
        for (int i = 0; i < this.test_images.size(); i++) {
            if (!this.p_test_images.contains(test_images.get(i))) {
                this.n_test_images.add(test_images.get(i));
            }
        }
    }

    public void cut_gt_tiles() throws IOException {
        String p_filename = new ClassPathResource("/gt_tiles/positive/").getFile().getPath();
        File p_f = new File(p_filename);
        if (!p_f.exists()) {
            p_f.createNewFile();
        }
        String n_filename = new ClassPathResource("/gt_tiles/negative/").getFile().getPath();
        File n_f = new File(n_filename);
        if (!n_f.exists()) {
            n_f.createNewFile();
        }
        if (p_f.listFiles().length > 0 || n_f.listFiles().length > 0) {
            System.err.println("ground truth tiles already exit");
            System.exit(0);
        }
        for (int i = 0; i < p_pixels.size(); i++) {
            String[] pixel = p_pixels.get(i);
            File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + pixel[0]);
            int pixel_x = Integer.parseInt(pixel[1]);
            int pixel_y = Integer.parseInt(pixel[2]);
            String[] tmp = pixel[0].split("_");
            String des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y + ".jpeg";
            File des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/positive/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y, tile_width, tile_height);
        }
        for (int i = 0; i < n_test_images.size(); i++) {
            String filename = n_test_images.get(i);
            File src_f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + filename);
            int pixel_x = tile_width / 2;
            int pixel_y = tile_height / 2;
            String[] tmp = filename.split("_");
            String des_filename = tmp[0] + "_" + tmp[1] + "_" + pixel_x + "_" + pixel_y + ".jpeg";
            File des_f = new File(System.getProperty("user.dir"), "src/main/resources/gt_tiles/negative/" + des_filename);
            VGI_Files.cut_tile(src_f, des_f, pixel_x, pixel_y, tile_width, tile_height);
        }

    }


    public ArrayList<String> getP_test_images() {
        return this.getP_test_images();
    }

    public ArrayList<String> getN_test_images() {
        return this.getN_test_images();
    }

    public static void main(String args[]) throws IOException {
        String [] imgs ={
                "156407_142694_18.jpeg","156407_142696_18.jpeg",
                "156408_142694_18.jpeg","156409_142696_18.jpeg",
                "156416_142695_18.jpeg","156434_142695_18.jpeg",
                "156443_142694_18.jpeg","156448_142694_18.jpeg",
                "156448_142695_18.jpeg","156450_142695_18.jpeg",
                "156455_142694_18.jpeg","156457_142696_18.jpeg",
                "156458_142696_18.jpeg","156459_142694_18.jpeg",
                "156461_142694_18.jpeg","156471_142696_18.jpeg",
                "156472_142696_18.jpeg","156476_142696_18.jpeg",
                "156477_142696_18.jpeg","156479_142694_18.jpeg",
                "156481_142694_18.jpeg","156482_142694_18.jpeg",
                "156482_142695_18.jpeg","156483_142694_18.jpeg",
                "156484_142694_18.jpeg","156488_142695_18.jpeg",
                "156489_142695_18.jpeg","156490_142694_18.jpeg",
                "156494_142694_18.jpeg","156495_142694_18.jpeg",
                "156496_142694_18.jpeg","156517_142694_18.jpeg",
                "156539_142694_18.jpeg","156539_142695_18.jpeg",
                "156542_142695_18.jpeg","156548_142694_18.jpeg",
                "156548_142695_18.jpeg","156549_142694_18.jpeg",
                "156553_142694_18.jpeg","156555_142695_18.jpeg",
                "156558_142694_18.jpeg","156560_142695_18.jpeg",
                "156567_142695_18.jpeg","156575_142694_18.jpeg"};
        String img_f = "156407_142694_18.jpeg";
        File f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + img_f);
        BufferedImage image = ImageIO.read(f);
        Image dimg = image.getScaledInstance(tile_width, tile_height, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(dimg);
        JLabel imageLabel = new JLabel(icon);
        imageLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
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
