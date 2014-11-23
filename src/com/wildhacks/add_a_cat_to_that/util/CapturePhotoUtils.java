package com.wildhacks.add_a_cat_to_that.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
 
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
 
/**
 * Android internals have been modified to store images in the media folder with 
 * the correct date meta data
 * @author samuelkirton
 */
public class CapturePhotoUtils {
	
	/**
	 * A copy of the Android internals  insertImage method, this method populates the 
	 * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media 
	 * that is inserted manually gets saved at the end of the gallery (because date is not populated).
	 * @see android.provider.MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String)
	 */
	public static final String insertImage(ContentResolver cr, 
			Bitmap source, 
			String title, 
			String description) {
		
		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, title);
		values.put(Images.Media.DISPLAY_NAME, title);
		values.put(Images.Media.DESCRIPTION, description);
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		// Add the date meta data to ensure the image is added at the front of the gallery
		values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
		values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
 
        Uri url = null;
        String stringUrl = null;    /* value to be returned */
 
        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
 
            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }
 
                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = Images.Thumbnails.getThumbnail(cr, id, Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F,Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }
 
        if (url != null) {
            stringUrl = url.toString();
        }
 
        return stringUrl;
	}
	
	/**
	 * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
	 * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
	 * meta data. The StoreThumbnail method is private so it must be duplicated here.
	 * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
	 */
	private static final Bitmap storeThumbnail(
			ContentResolver cr,
			Bitmap source,
			long id,
			float width, 
			float height,
			int kind) {
		
		// create the matrix to scale it
		Matrix matrix = new Matrix();
 
		float scaleX = width / source.getWidth();
		float scaleY = height / source.getHeight();
 
		matrix.setScale(scaleX, scaleY);
 
		Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
			source.getWidth(),
			source.getHeight(), matrix,
			true
		);
 
		ContentValues values = new ContentValues(4);
		values.put(Images.Thumbnails.KIND,kind);
		values.put(Images.Thumbnails.IMAGE_ID,(int)id);
		values.put(Images.Thumbnails.HEIGHT,thumb.getHeight());
		values.put(Images.Thumbnails.WIDTH,thumb.getWidth());
 
		Uri url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values);
 
		try {
			OutputStream thumbOut = cr.openOutputStream(url);
			thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
			thumbOut.close();
			return thumb;
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException ex) {
			return null;
		}
    }
	
	public static Bitmap convertToMutable(Bitmap imgIn) {
	    try {
	        //this is the file going to use temporally to save the bytes. 
	        // This file will not be a image, it will store the raw image data.
	        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

	        //Open an RandomAccessFile
	        //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
	        //into AndroidManifest.xml file
	        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

	        // get the width and height of the source bitmap.
	        int width = imgIn.getWidth();
	        int height = imgIn.getHeight();
	        Config type = imgIn.getConfig();

	        //Copy the byte to the file
	        //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
	        FileChannel channel = randomAccessFile.getChannel();
	        MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
	        imgIn.copyPixelsToBuffer(map);
	        //recycle the source bitmap, this will be no longer used.
	        imgIn.recycle();
	        System.gc();// try to force the bytes from the imgIn to be released

	        //Create a new bitmap to load the bitmap again. Probably the memory will be available. 
	        imgIn = Bitmap.createBitmap(width, height, type);
	        map.position(0);
	        //load it back from temporary 
	        imgIn.copyPixelsFromBuffer(map);
	        //close the temporary file and channel , then delete that also
	        channel.close();
	        randomAccessFile.close();

	        // delete the temp file
	        file.delete();

	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } 

	    return imgIn;
	}
}
