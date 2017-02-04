package util;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
        int sum = 0;
        for (Pixel p: pixels) {
            sum += p.getGreyLevel();
        }

        return Math.round(sum / pixels.size());
    }

    public static int calculateVariance(List<Pixel> pixels, int mean) {
        List<Integer> diffs = new ArrayList<>();

        for (Pixel p : pixels) {
            int dev  = (int)Math.pow((p.getGreyLevel() - mean), 2);
            diffs.add(dev);
        }

        int devSum = diffs.stream().reduce(0, (sum, next) -> sum + next);

        return Math.round(devSum / diffs.size());
    }

}
