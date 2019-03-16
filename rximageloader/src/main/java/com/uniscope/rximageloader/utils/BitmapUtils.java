package com.uniscope.rximageloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {

    public static Bitmap getSuitableBitmap(byte[] byteData,int length) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteData, 0, byteData.length, options);
        int actualWidth = options.outWidth;
        int actualHeight = options.outHeight;
        options.inJustDecodeBounds = false;
        options.inSampleSize = findBestSampleSize(actualWidth, actualHeight, length, length);
        return BitmapFactory.decodeByteArray(byteData, 0, byteData.length, options);
    }

    public static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }
        return (int) n;
    }

    public static Bitmap compressBitmap(Bitmap bmp){
        // 尺寸压缩倍数,值越大，图片尺寸越小
        int ratio = 2;
        // 压缩Bitmap到对应尺寸
        Bitmap result = Bitmap.createBitmap(bmp.getWidth() / ratio, bmp.getHeight() / ratio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, bmp.getWidth() / ratio, bmp.getHeight() / ratio);
        canvas.drawBitmap(bmp, null, rect, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 把压缩后的数据存放到baos中
        result.compress(Bitmap.CompressFormat.JPEG, 90 ,baos);
        byte[] bytes = baos.toByteArray();
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    public static void recycleBitmap(Bitmap bm) {
        if (bm != null && !bm.isRecycled()) {
            bm.recycle();
        }
    }

}
