package org.example;

import org.datavec.image.loader.BaseImageLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by john on 21.12.16.
 */
public class Test {
    protected static final long seed = 12345;
    protected static final String[] allowedExtensions = BaseImageLoader.ALLOWED_FORMATS;
    public static final Random randNumGen = new Random(seed);
    protected static int height = 50;
    protected static int width = 50;
    protected static int channels = 3;
    protected static int outputNum = 3;
    protected static int batchSize = 1;

    public static void main(String args[]) throws IOException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("d", 2);
        map.put("c", 1);
        map.put("b", 1);
        map.put("a", 3);

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, (o1, o2) -> o1.getValue() - o2.getValue());

        for (int i = 0; i < entries.size(); i++) {
            String entry = entries.get(i).toString();
            System.out.println(entry.replace("=",";"));
        }


//        int [] d = {1,2,3};
//       double [] dd = Doubles.toArray(Ints.asList(d));
//        System.out.println(Arrays.toString(dd));
//        INDArray id = Nd4j.create(dd);
//        System.out.println(id);
//        INDArray i = Nd4j.zeros(2,5);
//        i.getRow(0).getColumn(2).assign(0.1);
//        i.getColumn(3).assign(0.2);
//        i.getColumn(1).assign(0.4);
//        System.out.println(i);
//        Double d = i.getRow(0).max(1).getDouble(0);
//        System.out.println(d);
//        for (int j=0;j<5;j++){
//            if (i.getDouble(0,j)==d){
//                System.out.println(j);
//            }
//        }

//        String filename = new ClassPathResource("/DataExamples/ImagePipeline/labelA/image_0010.jpg").getFile().getPath();
//        File img = new File(filename);
//
//        FileSplit filesInDir = new FileSplit(img, allowedExtensions, randNumGen);
//        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
//        BalancedPathFilter pathFilter = new BalancedPathFilter(randNumGen, allowedExtensions, labelMaker);
//        InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter);
//        InputSplit d = filesInDirSplit[0];
//
//        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
//        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);
//        recordReader.initialize(d);
//        DataSetIterator it = new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
//
//        scaler.fit(it);
//        it.setPreProcessor(scaler);
//
//        DataSet ds = it.next();
//        System.out.println(Arrays.toString(ds.getFeatureMatrix().shape()));
//        INDArray tmp = ds.getFeatureMatrix();
//        tmp = tmp.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(0, 2), NDArrayIndex.interval(0, 2, true));
//        System.out.println(tmp);
//        System.out.println(Arrays.toString(tmp.shape()));
//        INDArray tmp2 = Nd4j.vstack(tmp, tmp);
//        System.out.println(Arrays.toString(tmp2.shape()));

    }
}
