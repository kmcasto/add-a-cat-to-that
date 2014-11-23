package com.wildhacks.add_a_cat_to_that.model;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.FaceDetector.Face;
import android.media.FaceDetector;

public class HumanFace {
	private static final int MAX_FACES = 50;
	public HumanFace(){
		
	}
	static public ArrayList<Face> getFaces(Bitmap bmp){
		Face[] faces = new Face[MAX_FACES];
		BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap img = bmp.copy(bitmap_options.inPreferredConfig, true);
        FaceDetector face_detector = new FaceDetector(
                        img.getWidth(), img.getHeight(),
                        MAX_FACES);
        faces = new FaceDetector.Face[MAX_FACES];
        // The bitmap must be in 565 format (for now).
        int numOfFaces = face_detector.findFaces(img, faces);
        
        ArrayList<Face> faceList = new ArrayList<Face>();
		for(int i =0; i<numOfFaces; i++){
			faceList.add(faces[i]);
			if(faces[i]==null){
				System.out.println("Null");
			}
		}
		return faceList;
	}
	
}
