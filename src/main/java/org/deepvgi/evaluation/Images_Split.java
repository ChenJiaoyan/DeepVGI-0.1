package org.deepvgi.evaluation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by John on 12/22/16.
 * Split images into training and testing sets
 */
public class Images_Split {
    private static double rate = 0.25; //testing images: 0.25; training images: 0.75
    public static void main(String args[]) throws IOException {
        File f = new File(System.getProperty("user.dir"), "src/main/resources/imagery/");
        String [] images = f.list();
        ArrayList<String> train_imgs = new ArrayList<>();
        ArrayList<String> test_imgs = new ArrayList<>();
        for(int i=0;i<images.length;i++){
            if(Math.random()>rate){
                train_imgs.add(images[i]);
            }else{
                test_imgs.add(images[i]);
            }
        }
        store(train_imgs,"train_images");
        System.out.println("train images #: " + train_imgs.size());
        store(test_imgs,"test_images");
        System.out.println("test images #: " + test_imgs.size());
    }

    private static void store(ArrayList<String> d, String file_name) throws IOException {
        File f = new File(System.getProperty("user.dir"), "src/main/resources/"+file_name);
        FileWriter writer = new FileWriter(f);
        for (int i=0;i<d.size();i++){
            writer.write(d.get(i) + "\n");
        }
        writer.flush();
        writer.close();
    }
}
