import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import util.Pixel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonas on 3/02/17.
 */
public class SeededRegionGrowing implements PlugInFilter {

    private static final String TITLE = "Seeded Region Growing";
    private static final int MAX_REGIONS = 5;
    private int width = 0;
    private int height = 0;
    private int[][] seedPixels;
    private boolean eightConnected = true;
    private ImageProcessor ip;
    private int currentMean;
    private int currentVariance;

    public int setup(String arg, ImagePlus imagePlus) {
        this.width = imagePlus.getWidth();
        this.height = imagePlus.getHeight();
        return DOES_16 | DOES_8G;
    }

    public void run(ImageProcessor ip) {
        this.ip = ip;
        ImagePlus seedImage = getSeedImage();

        if (seedImage == null) {
            return;
        }

        ImageProcessor seedIp = seedImage.getProcessor();
        this.seedPixels = seedIp.getIntArray();

        Map<Integer, List<Pixel>> regions = new HashMap<>();

        for (int x = 0; x < seedIp.getWidth(); x++) {
            for (int y = 0; y < seedIp.getHeight(); y++) {
                int value = this.seedPixels[x][y];
                if (value > 0) {
                    if (regions.containsKey(value)) {
                        regions.get(value).add(new Pixel(x, y, value));
                    } else {
                        if (regions.size() <= MAX_REGIONS) {
                            regions.put(value, new ArrayList<>());
                        } else {
                            IJ.error(TITLE, "Too many regions, only " + MAX_REGIONS + " supported");
                            return;
                        }
                    }
                }
            }
        }

        for (Map.Entry<Integer, List<Pixel>> entry : regions.entrySet()) {
            IJ.log("Growing region with grey level: " + entry.getKey());
            List<Pixel> pixels = entry.getValue();
            this.currentMean = Pixel.calculateMean(pixels);
            this.currentVariance = Pixel.calculateVariance(pixels, this.currentMean);

            for (Pixel p : pixels) {
                growNeighbours(p, entry.getKey());
            }
        }



    }

    private void growPixel(Pixel p, int regionGreyLevel) {
        if (belongsToARegion(p) || !isInBounds(p) || !meetsGrowCriteria(p)) {
            return;
        }

        this.seedPixels[p.x][p.y] = regionGreyLevel;
        growNeighbours(p, regionGreyLevel);
    }

    private void growNeighbours(Pixel p, int regionGreyLevel) {
        growPixel(new Pixel(p.x++, p.y, this.ip.getPixel(p.x++, p.y)), regionGreyLevel);
        growPixel(new Pixel(p.x--, p.y, this.ip.getPixel(p.x--, p.y)), regionGreyLevel);
        growPixel(new Pixel(p.x, p.y++, this.ip.getPixel(p.x, p.y++)), regionGreyLevel);
        growPixel(new Pixel(p.x, p.y--, this.ip.getPixel(p.x, p.y--)), regionGreyLevel);

        if (this.eightConnected) {
            growPixel(new Pixel(p.x++, p.y++, this.ip.getPixel(p.x++, p.y++)), regionGreyLevel);
            growPixel(new Pixel(p.x--, p.y--, this.ip.getPixel(p.x--, p.y--)), regionGreyLevel);
            growPixel(new Pixel(p.x++, p.y--, this.ip.getPixel(p.x++, p.y--)), regionGreyLevel);
            growPixel(new Pixel(p.x--, p.y++, this.ip.getPixel(p.x--, p.y++)), regionGreyLevel);
        }
    }

    private boolean meetsGrowCriteria(Pixel p) {
        int min = this.currentMean - this.currentVariance;
        int max = this.currentMean + this.currentVariance;
        return p.getGreyLevel() >= min && p.getGreyLevel() <= max;
    }

    private boolean belongsToARegion(Pixel p) {
        return this.seedPixels[p.x][p.y] > 0;
    }

    private boolean isInBounds(Pixel p) {
        return p.x > 0 && p.y > 0 && p.x < this.width && p.y < this.height;
    }

    private ImagePlus getSeedImage() {
        final int[] wList = WindowManager.getIDList();
        if (wList == null) {
            IJ.noImage();
            return null;
        }

        final List<String> seedTitleList = new ArrayList<>();
        for (final int id : wList) {
            final ImagePlus imp = WindowManager.getImage(id);
            final int type = imp.getType();
            if (!imp.getTitle().trim().isEmpty()) {
                if (type == ImagePlus.GRAY8) {
                    seedTitleList.add(imp.getTitle());
                }
            }
        }

        if (seedTitleList.size() < 1) {
            IJ.error(TITLE, "No supported seed images open.");
            return null;
        }

        final String[] seedTitles = seedTitleList.toArray(new String[seedTitleList.size()]);

        final GenericDialog gd = new GenericDialog(TITLE, IJ.getInstance());
        gd.addChoice("Seeds:", seedTitles, seedTitles[0]);
        gd.showDialog();

        final ImagePlus seeds = WindowManager.getImage(seedTitles[gd.getNextChoiceIndex()]);

        if (seeds.getHeight() != this.height || seeds.getWidth() != this.width) {
            IJ.error(TITLE, "Seed image has different size to image. This is the wrong seed for this image.");
            return null;
        }

        return seeds;
    }

}
