import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import util.Pixel;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Created by jonas on 29/11/16.
 */

public class AdaptiveThresholding implements PlugInFilter {

    public int setup(String arg, ImagePlus imagePlus) {
        IJ.log("Running Adaptive Thresholding");
        return DOES_16 | DOES_8G | DOES_STACKS;
    }

    public void run(ImageProcessor ip) {
        //PluginInFilter will run all stacks under the hood if setup like this
        process(ip);
    }

    private void process(ImageProcessor ip) {
        final int[] histogram = ip.getHistogram();
        int totalPixels = ip.getWidth() * ip.getHeight();

        int[] cumHistogram = new int[histogram.length];

        cumHistogram[0] = histogram[0];
        for (int i = 1; i < cumHistogram.length; i++) {
            cumHistogram[i] = cumHistogram[i - 1] + histogram[i];
        }

        PlotWindow.noGridLines = true;
        Plot plot = new Plot("Orig Histo", "Intensity", "Freq", IntStream.range(0,histogram.length).mapToDouble(a -> a).toArray(), Arrays.stream(histogram).mapToDouble(a -> a).toArray());

        long thresh = LongStream.range(0, histogram.length).reduce(0, (sum, next) ->  sum + (next * histogram[(int) next])) / totalPixels;

        long threshNext = -1;
        double[] yAxis = {0.0, Arrays.stream(histogram).mapToDouble(a -> a).max().getAsDouble()};

        Map<Long, Integer> thresholdCount = new HashMap<>();

        while (thresh != threshNext) {

            long meanObj = LongStream.range(0, thresh).reduce(0, (sum, next) ->  sum + (next * histogram[(int) next])) / cumHistogram[(int) thresh];
            long meanBg = LongStream.range(thresh, histogram.length).reduce(0, (sum, next) ->  sum + (next * histogram[(int) next])) / (cumHistogram[histogram.length - 1] - cumHistogram[(int) thresh]);
            threshNext = thresh;
            thresh = (meanObj + meanBg) / 2;

            if (thresholdCount.containsKey(thresh)) {
                thresholdCount.put(thresh, thresholdCount.get(thresh) + 1);
            } else {
                thresholdCount.put(thresh, 1);
            }


            if (thresholdCount.get(thresh) > 2) {
                List<Long> duplicatedThreshs = new ArrayList<>();

                thresholdCount.forEach( (key, value) -> {
                    if (value > 1) {
                        duplicatedThreshs.add(key);
                    }
                });

                thresh = Math.round(duplicatedThreshs.stream().reduce(0L, (sum, next) -> sum + next) / duplicatedThreshs.size());
                break;
            }

        }

        double[] xAxis = {(double) thresh, (double) thresh};
        plot.addPoints(xAxis, yAxis, PlotWindow.X);
        plot.addPoints(xAxis, yAxis, PlotWindow.LINE);

        plot.show();
        IJ.log("" + thresh);

        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                if (ip.getPixel(x,y) <= thresh) {
                    ip.putPixel(x,y,0);
                } else {
                    ip.putPixel(x,y, histogram.length - 1);
                }
            }
        }

    }

}
