package jp.co.miraishonen.cycro.helper;

import java.io.FileNotFoundException;

import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PixelXorXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.AvoidXfermode.Mode;
import android.net.Uri;

public class BitmapHelper {
	public static Bitmap decodeUri(Uri selectedImage, Context context, int requiredSize) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while ((width_tmp / scale > requiredSize && height_tmp / scale > requiredSize)) {
            scale ++;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);
    }
	
	public static Bitmap createRegularBitmap(Bitmap bm, int dstSize) {
		int srcWidth = bm.getWidth();
		int srcHeight = bm.getHeight();
		Bitmap tmpBitmap = Bitmap.createBitmap(dstSize, dstSize, Bitmap.Config.ARGB_8888);
		Bitmap dstBitmap = Bitmap.createBitmap(dstSize, dstSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(tmpBitmap);
		Canvas dstCanvas = new Canvas(dstBitmap);
		Rect srcRect;
		if (srcWidth < srcHeight) {
			srcRect = new Rect(0, (srcHeight - srcWidth) / 2, srcWidth, (srcHeight + srcWidth) / 2);
		} else {
			srcRect = new Rect((srcWidth - srcHeight) / 2, 0, (srcWidth + srcHeight) / 2, srcHeight);
		}
		
		canvas.drawBitmap(bm, srcRect, new Rect(0, 0, dstSize, dstSize), null);
		//dstCanvas.drawBitmap(bm, srcRect, new Rect(0, 0, dstSize, dstSize), null);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		dstCanvas.drawCircle(dstSize / 2, dstSize / 2, dstSize / 2, paint);
		for (int i = 0; i < dstBitmap.getWidth(); i ++) {
			for (int j = 0; j < dstBitmap.getHeight(); j ++) {
				int pixel = dstBitmap.getPixel(i, j);
				if (pixel == Color.BLACK) {
					dstBitmap.setPixel(i, j, tmpBitmap.getPixel(i, j));
				}
			}
		}

		return tmpBitmap;
	}
	
	public static Bitmap getPhotoBitmap(String photoPath) {
		return BitmapFactory.decodeFile(photoPath);
	}
}
