import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

/**
 * Created by jonas on 29/11/16.
 */
public class Application {

    public static void main(String[] args) {
        Class<?> clazz = AdaptiveThresholding.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        new ImageJ();

        ImagePlus image = IJ.openImage("/home/jonas/Pictures/Soil_slices0000.tif");
        image.show();

        IJ.runPlugIn(clazz.getName(), "");
    }
}
