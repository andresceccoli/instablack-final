package com.androidutn.instablack1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

/**
 * Created by andres on 10/23/17.
 */

public class ImageUtils {

    public static Bitmap cargarBitmap(Context context, Uri uri, int maxSize) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

            int width = options.outWidth;
            int height = options.outHeight;

            int scale = 1;
            while (true) {
                if (width / 2 < maxSize || height / 2 < maxSize) {
                    break;
                }

                width /= 2;
                height /= 2;
                scale *= 2;
            }

            BitmapFactory.Options optionsOut = new BitmapFactory.Options();
            optionsOut.inSampleSize = scale;
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, optionsOut);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap convertirGrayscale(Bitmap bitmap) {
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(0f));
        return filter.processFilter(bitmap);
    }

    public static Bitmap generarThumbnail(Context context, Bitmap bitmap) {
        return generarThumbnail(bitmap, context.getResources().getDimension(R.dimen.miniaturas));
    }

    public static Bitmap generarThumbnail(Bitmap bitmap, float size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min(size / width, size / height);

        return Bitmap.createScaledBitmap(bitmap, (int) (scale * width), (int) (scale * height), false);
    }

}
