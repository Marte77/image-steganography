package pt.sapo.dynip.martinho;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class BufferedImageRGB  {
    public File fich;
    public BufferedImage imagem;
    public byte[] pixels;
    public int width, height;
    public boolean hasAlpha;
    public int pixelLen = 3;
    BufferedImageRGB(File ficheiro) throws IOException {
        imagem = ImageIO.read(ficheiro);
        pixels = ((DataBufferByte) imagem.getRaster().getDataBuffer()).getData();
        width = imagem.getWidth(); height = imagem.getHeight();
        hasAlpha = imagem.getAlphaRaster() != null;
        if(hasAlpha)
            pixelLen = 4;
    }
    int[] getPixel(int x, int y){
        int pixel = getRGB(x,y);
        int[] pixelArr;
        if(hasAlpha){
             pixelArr = new int[4];
             pixelArr[0] = (pixel & 0xff000000) >>> 24;
             pixelArr[1] = (pixel & 0xff0000) >> 16;
             pixelArr[2] = (pixel & 0xff00) >> 8;
             pixelArr[3] = pixel & 0xff;
        }else{
            pixelArr = new int[3];
            pixelArr[0] = (pixel & 0xff0000) >> 16;
            pixelArr[1] = (pixel & 0xff00) >> 8;
            pixelArr[2] = pixel & 0xff;
        }
        return pixelArr;
    }

    /**
     * by: https://stackoverflow.com/a/26713029/10676498
     *
     */
    private int getRGB(int x, int y)
    {
        int pos = (y * pixelLen * width) + (x * pixelLen);

        int argb = -16777216; // 255 alpha
        if (hasAlpha)
        {
            argb = (((int) pixels[pos++] & 0xff) << 24); // alpha
        }

        argb += ((int) pixels[pos++] & 0xff); // blue
        argb += (((int) pixels[pos++] & 0xff) << 8); // green
        argb += (((int) pixels[pos++] & 0xff) << 16); // red
        return argb;
    }

    public static int convertPixelArrayToInt(int[] pixelArr){
        int pixel = 0;
        if(pixelArr.length == 3){
            pixel += ((int) pixelArr[2] & 0xff); // blue
            pixel += (((int) pixelArr[1] & 0xff) << 8); // green
            pixel += (((int) pixelArr[0] & 0xff) << 16); // red
        }else if(pixelArr.length == 4){
            pixel = (((int) pixelArr[0] & 0xff) << 24); // alpha
            pixel += ((int) pixelArr[3] & 0xff); // blue
            pixel += (((int) pixelArr[2] & 0xff) << 8); // green
            pixel += (((int) pixelArr[1] & 0xff) << 16); // red
        }else return -1000;
        return pixel;
    }
}
