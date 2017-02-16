import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Created by jonas on 16/02/17.
 */
public class CalculateBinaryImagePorosity implements PlugInFilter {
    private boolean isPoresBlack = true;
    private static final int BLACK = 0;

    public int setup(String arg, ImagePlus imagePlus) {
        return DOES_16 | DOES_8G | DOES_STACKS;
    }

    public void run(ImageProcessor ip) {
        //PluginInFilter will run all stacks under the hood if setup like this
        process(ip);
    }

    private void process(ImageProcessor ip) {
        final int[] histogram = ip.getHistogram();
        double totalPixels = ip.getWidth() * ip.getHeight();
        int white = histogram.length -1;
        double porosity = -1.0;

        if (isPoresBlack) {
            porosity = histogram[BLACK] / totalPixels * 100.0;
        } else {
            porosity = histogram[white] / totalPixels * 100.0;
        }

        double porosity2dp = Math.round(porosity * 100.0) / 100.0;

        //IJ.log("Black Pixels: " + histogram[BLACK] + " White Pixels: " + histogram[white] + "Total Pixels: " + totalPixels);
        IJ.log(porosity2dp + "%");
    }

}
