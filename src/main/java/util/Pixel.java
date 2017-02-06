package util;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by jonas on 4/02/17.
 */
public class Pixel extends Point {
    private int greyLevel;

    public Pixel(int x, int y, int greyLevel) {
        super(x,y);
        this.greyLevel = greyLevel;
    }

    public int getGreyLevel() {
        return greyLevel;
    }

    public static int calculateMean(List<Pixel> pixels) {
        long sum = calculateSum(pixels);

        return Math.round(sum / pixels.size());
    }

    public static int calculateStdDev(List<Pixel> pixels, int mean) {
        long devSum = calculateDeviationDifferenceSum(pixels, mean);

        double var = devSum / pixels.size();
        long stdDev = Math.round(Math.sqrt(var));

        return (int)stdDev;
    }

    public static long calculateSum(List<Pixel> pixels) {
        long sum = 0;
        for (Pixel p: pixels) {
            sum += p.getGreyLevel();
        }

        return sum;
    }

    public static long calculateDeviationDifferenceSum(List<Pixel> pixels, int mean) {
        List<Long> diffs = new ArrayList<>();

        for (Pixel p : pixels) {
            long dev  = (long)Math.pow((p.getGreyLevel() - mean), 2);
            diffs.add(dev);
        }

        long devSum = diffs.stream().reduce(0L, (sum, next) -> sum + next);

        return devSum;
    }

    public static void test() {
        List<Pixel> pixels = new ArrayList<>();
        pixels.add(new Pixel(1,2,600));
        pixels.add(new Pixel(1,2,470));
        pixels.add(new Pixel(1,2,170));
        pixels.add(new Pixel(1,2,430));
        pixels.add(new Pixel(1,2,300));

        int mean = Pixel.calculateMean(pixels);
        int dev = Pixel.calculateStdDev(pixels, mean);
        return;
    }

}
