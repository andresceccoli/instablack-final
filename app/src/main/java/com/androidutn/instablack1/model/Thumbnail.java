package com.androidutn.instablack1.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.androidutn.instablack1.R;
import com.zomato.photofilters.geometry.Point;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.SubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.VignetteSubfilter;

/**
 * Created by andres on 10/24/17.
 */

public class Thumbnail {

    public Bitmap bitmap;
    public String nombre;
    public Filter filtro;

    public static Thumbnail getBase(Context context, Bitmap bitmap) {
        return new Thumbnail(Bitmap.createBitmap(bitmap), context.getResources().getString(R.string.filtro_original), null);
    }

    public static Thumbnail getVintage(Context context, Bitmap bitmap) {
        Filter filter = new Filter();
        Point[] rgb = new Point[] {
                new Point(0, 64),
                new Point(255, 210)
        };
        filter.addSubFilter(new ContrastSubfilter(1.2f));
        filter.addSubFilter(new ToneCurveSubfilter(rgb, null, null, null));
        filter.addSubFilter(new VignetteSubfilter(context, 70));
        return new Thumbnail(filter.processFilter(Bitmap.createBitmap(bitmap)), context.getResources().getString(R.string.filtro_vintage), filter);
    }

    public static Thumbnail getContraste(Context context, Bitmap bitmap) {
        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubfilter(1.5f));
        return new Thumbnail(filter.processFilter(Bitmap.createBitmap(bitmap)), context.getResources().getString(R.string.filtro_contraste), filter);
    }

    public static Thumbnail getBrillo(Context context, Bitmap bitmap) {
        Filter filter = new Filter();
        filter.addSubFilter(new BrightnessSubfilter(50));
        filter.addSubFilter(new ContrastSubfilter(0.8f));
        return new Thumbnail(filter.processFilter(Bitmap.createBitmap(bitmap)), context.getString(R.string.filtro_brillo), filter);
    }

    public static Thumbnail getTinta(Context context, Bitmap bitmap) {
        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubfilter(3f));
        return new Thumbnail(filter.processFilter(Bitmap.createBitmap(bitmap)), context.getString(R.string.filtro_tinta), filter);
    }

    public static Thumbnail getHighlight(Context context, Bitmap bitmap) {
        Filter filter = new Filter();
        filter.addSubFilter(new SubFilter() {
            private Object tag = "";

            @Override
            public Bitmap process(Bitmap src) {
                final long time = System.currentTimeMillis();
                Bitmap bitmapOut = Bitmap.createBitmap(src);

                final int width = src.getWidth();
                final int height = src.getHeight();
                final int[] pixels = new int[width * height];
                bitmapOut.getPixels(pixels, 0, width, 0, 0, width, height);
                bitmapOut.recycle();
                double grey;
                int alpha, red, green, blue;

                for (int i = 0; i < pixels.length; i++) {

                    alpha = Color.alpha(pixels[i]);
                    red = Color.red(pixels[i]);
                    green = Color.green(pixels[i]);
                    blue = Color.blue(pixels[i]);
                    grey = ((red * 0.3f) + (green * 0.59f) + (blue * 0.11f));

                    red = green = blue = (int) hardLightLayer(grey, grey);
                    pixels[i] = Color.argb(alpha, red, green, blue);
                }
                bitmapOut = Bitmap.createBitmap(width, height, src.getConfig());
                bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height);
                System.out.println("Finished @ " + (System.currentTimeMillis() - time) + "ms");

                return bitmapOut;
            }

            /**
             * Hard light layer.
             *
             * @param maskVal the mask val
             * @param imgVal  the img val
             * @return the double
             */
            private double hardLightLayer(double maskVal, double imgVal) {
                if (maskVal > 128)
                    return 255 - (((255 - (2 * (maskVal - 128))) * (255 - imgVal)) / 256);
                else
                    return (2 * maskVal * imgVal) / 256;
            }

            @Override
            public Object getTag() {
                return tag;
            }

            @Override
            public void setTag(Object tag) {
                this.tag = tag;
            }
        });
        return new Thumbnail(filter.processFilter(bitmap), context.getString(R.string.filtro_highlight), filter);
    }

    private Thumbnail(Bitmap bitmap, String nombre, Filter filtro) {
        this.bitmap = bitmap;
        this.nombre = nombre;
        this.filtro = filtro;
    }
}
