package org.deepvgi.vgi;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by john on 23.12.16.
 * APIs to read VGI data including OSM, MapSwipe, satellite tiles
 */
public class VGI_Files {

    public static ArrayList<String> loadImageName(String type) throws IOException {
        ArrayList<String> names = new ArrayList<>();
        File f;
        if (type.equals("train")) {
            f = new File(System.getProperty("user.dir"), "src/main/resources/train_images");
        } else {
            f = new File(System.getProperty("user.dir"), "src/main/resources/test_images");
        }
        InputStreamReader read = new InputStreamReader(new FileInputStream(f));
        BufferedReader bufferedReader = new BufferedReader(read);
        String name;
        while ((name = bufferedReader.readLine()) != null) {
            names.add(name);
        }
        bufferedReader.close();
        read.close();
        return names;
    }

    public static ArrayList<int []> loadOSMBuilding() throws IOException {
        ArrayList<int []> OSMBuildings = new ArrayList<>();
        File f = new File(System.getProperty("user.dir"), "src/main/resources/OSM_buildings");
        InputStreamReader read = new InputStreamReader(new FileInputStream(f));
        BufferedReader bufferedReader = new BufferedReader(read);
        String line = bufferedReader.readLine(); //skip the first line
        while((line=bufferedReader.readLine())!=null){
            String [] items = line.split(";");
            int task_x = Integer.parseInt(items[5]);
            int task_y = Integer.parseInt(items[6]);
            int pixel_x = Integer.parseInt(items[7]);
            int pixel_y = Integer.parseInt(items[8]);
            int [] building = {task_x,task_y,pixel_x,pixel_y};
            OSMBuildings.add(building);
        }
        bufferedReader.close();
        read.close();
        return OSMBuildings;
    }

    public static HashMap<String, int []> loadMapSwipeLabel() throws IOException {
        HashMap<String, int []> MapSwipeLabels = new HashMap<>();
        File f = new File(System.getProperty("user.dir"), "src/main/resources/MapSwipe_labels");
        InputStreamReader read = new InputStreamReader(new FileInputStream(f));
        BufferedReader bufferedReader = new BufferedReader(read);
        String line = bufferedReader.readLine();      //skip the first line
        while((line=bufferedReader.readLine())!=null){
            String [] items = line.split(";");
            String task_x = items[5];
            String task_y = items[6];
            int yes_count = Integer.parseInt(items[9]);
            int maybe_count = Integer.parseInt(items[10]);
            int bad_imagery_count = Integer.parseInt(items[11]);
            String k = task_x + "_" + task_y;
            int [] v = {yes_count,maybe_count,bad_imagery_count};
            MapSwipeLabels.put(k,v);
        }
        bufferedReader.close();
        read.close();
        return MapSwipeLabels;
    }

    // cut a tile from src_f with the center (x,y), and save it in des_f
    public static void cut_tile(File src_f, File des_f, int x, int y, int tile_width,int tile_height) throws IOException {
        FileInputStream is = null;
        ImageInputStream iis = null;
        try {
            is = new FileInputStream(src_f);
            Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("jpeg");
            ImageReader reader = it.next();
            iis = ImageIO.createImageInputStream(is);
            reader.setInput(iis, true);
            ImageReadParam param = reader.getDefaultReadParam();
            Rectangle rect = new Rectangle(x-tile_width/2, y-tile_height/2, tile_width, tile_height);
            param.setSourceRegion(rect);
            BufferedImage bi = reader.read(0, param);
            ImageIO.write(bi, "jpeg", des_f);
        } finally {
            if (is != null)
                is.close();
            if (iis != null)
                iis.close();
        }
    }

    public static INDArray slide(String predict_f,int image_height,int image_width,int tile_height,int tile_width,
                                  int slide_stride,int channels,int labelNum,String[] allowedExtensions,
                                 Random randNumGen) throws IOException {
        int batchSize = 1;

        File img = new File(System.getProperty("user.dir"), "src/main/resources/imagery/" + predict_f);
        FileSplit filesInDir = new FileSplit(img, allowedExtensions, randNumGen);
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        ImageRecordReader recordReader = new ImageRecordReader(image_height, image_width, channels, labelMaker);
        recordReader.initialize(filesInDir);
        DataSetIterator it = new RecordReaderDataSetIterator(recordReader, batchSize, 1, labelNum);
        scaler.fit(it);
        it.setPreProcessor(scaler);
        DataSet ds = it.next();

        int row_n = (int) Math.ceil((image_height - tile_height) / (double) slide_stride);
        int col_n = (int) Math.ceil((image_width - tile_width) / (double) slide_stride);
        INDArray m = ds.getFeatures().getRow(0);
        INDArray out = Nd4j.zeros(row_n, col_n, channels, tile_height, tile_width);
        for (int y = 0, r = 0; y < image_height - tile_height; y = y + slide_stride, r = r + 1) {
            for (int x = 0, c = 0; x < image_width - tile_width; x = x + slide_stride, c = c + 1) {
                INDArray tile = m.get(NDArrayIndex.all(), NDArrayIndex.interval(y, y + tile_height),
                        NDArrayIndex.interval(x, x + tile_width));
                out.get(NDArrayIndex.point(r), NDArrayIndex.point(c)).assign(tile);
            }
        }
        return out;
    }
}
