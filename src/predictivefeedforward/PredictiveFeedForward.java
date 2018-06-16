package predictivefeedforward;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.imageio.ImageIO;

/**
 *
 * @author HDrmi
 */
public class PredictiveFeedForward {

    private int[] pixels;
    private List<Integer> difference;
    int width, height;

    public PredictiveFeedForward() {
        this.difference = new ArrayList<>();
    }

    private void convert2D(int[][] array) {
        int counter = 0;
        pixels = new int[width * height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[counter++] = array[i][j];
            }
        }
    }

    private void calculatedifference() {
        int tmp = pixels[0];
        difference.add(tmp);
        for (int i = 1; i < pixels.length; i++) {
            difference.add(pixels[i] - tmp);
            tmp = pixels[i];
        }
    }

    private int[][] readImage(String filePath) {
        int width = 0;
        int height = 0;
        File file = new File(filePath);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
        }
        width = image.getWidth();
        height = image.getHeight();
        int[][] pixels = new int[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb) & 0xff;
                pixels[y][x] = r;
            }
        }
        return pixels;
    }

    
    private void writeImage(int[][] pixels, String outputFilePath, int width, int height) {
        File fileout = new File(outputFilePath);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, (pixels[y][x] << 16) | (pixels[y][x] << 8) | (pixels[y][x]));
            }
        }
        try {
            ImageIO.write(image, "jpg", fileout);
        } catch (IOException e) {
        }
    }

    private void WriteInFile(Map<Integer, Integer> map, List<Integer> Integers) {
        try {
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream("compress.txt"))) {
                int size = map.size();
                out.writeInt(width);
                out.writeInt(height);
                out.writeInt(size);
                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    out.writeInt(entry.getKey());
                    out.writeInt(entry.getValue());
                }
                size = Integers.size();
                out.writeInt(size);
                for (int n : Integers) {
                    out.writeInt(n);
                }
                out.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PredictiveFeedForward.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PredictiveFeedForward.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void Read(Map<Integer, Integer> map, List<Integer> Integers) {
        try {
            try (DataInputStream in = new DataInputStream(new FileInputStream("compress.txt"))) {
                width = in.readInt();
                height = in.readInt();
                int size = in.readInt();
                for (int i = 0; i < size; i++) {
                    int a = in.readInt();
                    int b = in.readInt();
                    map.put(a, b);
                }
                size = in.readInt();
                for (int i = 0; i < size; i++) {
                    Integers.add(in.readInt());
                }
                in.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PredictiveFeedForward.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PredictiveFeedForward.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void compress(String path, int level) {
        int[][] array = readImage(path);
        width = array.length;
        height = array[0].length;
        Map<Integer, Integer> quantizer = new HashMap<>();
        List<Pair<Integer, Integer>> ranges = new ArrayList<>();
        List<Integer> compressdata = new ArrayList<>();
        convert2D(array);
        calculatedifference();
        List<Integer> tmp = new ArrayList<>(difference);
        Collections.sort(tmp);
        int mn = tmp.get(0);
        int mx = tmp.get(tmp.size() - 1);
        System.out.println(mn + " " + mx);
        int step = (int) Math.ceil((mx - mn) / level);
        for (int i = 0; i < level; i++) {
            ranges.add(new Pair<>(mn, mn + step));
            quantizer.put(i, (int) Math.ceil(mn + (mn + step)) / 2);
            mn += step + 1;
        }
        int i = 0, cnt = 0;
        compressdata.add(difference.get(i++));
        for (; i < difference.size(); i++) {
            for (int j = 0; j < ranges.size(); j++) {
                if (difference.get(i) >= ranges.get(j).getKey() && difference.get(i) <= ranges.get(j).getValue()) {
                    compressdata.add(j);
                    cnt++;
                    break;
                }
            }
        }
        WriteInFile(quantizer, compressdata);
    }

    public void decompress(String name) {
        Map<Integer, Integer> map = new HashMap<>();
        List<Integer> Integers = new ArrayList<>();
        Read(map, Integers);
        int[][] pixels = new int[width][height];
        for(Map.Entry<Integer,Integer> en : map.entrySet()){
            System.out.println(en.getKey()+ " " + en.getValue());
        }
        boolean flag = false;
        int cnt = 0;
        System.out.println(width * height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (!flag) {
                    pixels[i][j] = Integers.get(cnt);
                    cnt++;
                    flag = true;
                    continue;
                }
                int tmp = map.get(Integers.get(cnt));
                pixels[i][j] = tmp + Integers.get(cnt - 1);
//                System.out.println(pixels[i][j]);
                cnt++;
            }
        }
        writeImage(pixels, "C:\\Users\\HDrmi\\Documents\\NetBeansProjects\\PredictiveFeedForward\\"+name+".jpg", width, height);
    }

}
