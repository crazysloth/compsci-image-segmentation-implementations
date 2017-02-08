import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.FolderOpener;

/**
 * Created by jonas on 29/11/16.
 */
public class Application {

    public static void main(String[] args) {
        Class<?> clazz = SeededRegionGrowing.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        new ImageJ();

        ImagePlus seedImage = IJ.openImage("/home/jonas/Documents/Seeds.tif");
        seedImage.show();
        //ImagePlus image = IJ.openImage("/home/jonas/Pictures/Soil_slices0000.tif");
        ImagePlus image = FolderOpener.open("/home/jonas/Documents/soil-samples");
        image.show();
        IJ.selectWindow("soil-samples");

        IJ.runPlugIn(clazz.getName(), "");
    }
}
