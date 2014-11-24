package com.wildhacks.add_a_cat_to_that;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;


import com.wildhacks.add_a_cat_to_that.model.HumanFace;
import com.wildhacks.add_a_cat_to_that.util.CapturePhotoUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int TAKE_PICTURE = 0;
	protected Uri imageUri;
	boolean lotsOfCats=false;
	boolean hasBeard=false;
	boolean hasHalo=false;
	boolean hasCage=false;
	
	public void setLotsOfCats(View view){
		lotsOfCats=!lotsOfCats;
	}
	public void setBeard(View view){
		hasBeard=!hasBeard;
	}
	public void setHalo(View view){
		hasHalo=!hasHalo;
	}
	public void setCage(View view){
		hasCage=!hasCage;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Called when the user clicks the Send button */
	public void takeImage(View view) {
		// Do something in response to button
		System.out.println("Button clicked!");
		takePicture(view);
		// Intent intent = new Intent(this, DisplayMessageActivity.class);
	}

	public void takePicture(View view) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo = new File(Environment.getExternalStorageDirectory(),
				"Pic.jpg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
		imageUri = Uri.fromFile(photo);
		startActivityForResult(intent, TAKE_PICTURE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case TAKE_PICTURE:
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = imageUri;
				getContentResolver().notifyChange(selectedImage, null);
				ImageView imageView = (ImageView) findViewById(R.id.imageReturn);
				ContentResolver cr = getContentResolver();
				Bitmap bitmap;
				Bitmap cage;
				try {
					System.out.println("build image");
					bitmap = android.provider.MediaStore.Images.Media
							.getBitmap(cr, selectedImage);
					bitmap = CapturePhotoUtils.convertToMutable(bitmap);
					//String imageUri = "drawable://" + R.drawable.cat_above;
					bitmap = getResizedBitmap(bitmap, 800);
					
					ArrayList<Face> hFaces = HumanFace.getFaces(bitmap);
					
					
					
					
					
					
					System.out.println("num faces " + hFaces.size());
					PointF p = new PointF();
					System.out.println("point " + p.x + "," + p.y);
					for(int i = 0; i < hFaces.size(); i++) {
						hFaces.get(i).getMidPoint(p);
						if(hasCage){
							bitmap = buildCage(bitmap, (int)p.x,(int)p.y,hFaces.get(i).eyesDistance());
						}
						bitmap = buildKitties(bitmap, (int)p.x,(int)p.y , (int)(1.7*hFaces.get(i).eyesDistance()) ,35, lotsOfCats ,hasBeard, hasHalo);
					}
					
					CapturePhotoUtils.insertImage(cr, bitmap, "cat_beard_" + System.currentTimeMillis(), "cat cat cat cat");
					imageView.setImageBitmap(bitmap);
					Toast.makeText(this, selectedImage.toString(),
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
							.show();
					Log.e("Camera", e.toString());
				}
			}
		}
	}

	
	public Bitmap getResizedBitmap(Bitmap bm, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float aspectRatio = (float) width / (float) height;
		int newHeight = (int) Math
				.round((float) newWidth / (float) aspectRatio);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, newWidth,
				newHeight, false);
		return resizedBitmap;
	}

	public Bitmap compositeBitmap(Bitmap bm, Bitmap compBm, Matrix m) {
		System.out.println("composite");
		Bitmap tempBitmap = bm;
		Bitmap overlay = compBm;

		Bitmap finalBitmap = Bitmap.createBitmap(tempBitmap.getWidth(),
				tempBitmap.getHeight(), tempBitmap.getConfig());

		Canvas canvas = new Canvas(finalBitmap);
		canvas.drawBitmap(tempBitmap, new Matrix(), null);
		canvas.drawBitmap(overlay, m, null);
		return finalBitmap;
	}
	
	public Bitmap buildCage(Bitmap bm, int w, int h, float e) {
		int kitties[] = primeKitties();
		
		//Random random = new Random(System.currentTimeMillis());
		
		Matrix matrix = new Matrix();
		// Move the matrix to the middle
		matrix.postTranslate(h, w);
		
		Bitmap overlay = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.cage2);

		//overlay = getResizedBitmap(overlay, (int)(1.1*e/hFaces.get(0).eyesDistance()));
		overlay = getResizedBitmap(overlay, (int)(3.2*(int)e));
		
		Matrix mat2 = new Matrix();
		int x =  w-(overlay.getWidth()/2);
		int y =  h-(overlay.getHeight()/2);
		mat2.postTranslate(x,y);
		bm = compositeBitmap(bm, overlay, mat2);	

		return bm;
	}
	
	// eheheheh
	public Bitmap buildKitties(Bitmap bm, int w, int h, int radius, int num, boolean lotsOfCats, boolean hasBeard, boolean hasHalo) {
		int kitties[] = primeKitties();
		
		Random random = new Random(System.currentTimeMillis());
		
		Matrix matrix = new Matrix();
		// Move the matrix to the middle
		matrix.postTranslate(h, w);
		
		Bitmap overlay = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.empty);
		overlay = getResizedBitmap(overlay, 75);
	
		int randomCat = kitties[0];;
		if(!lotsOfCats){
			randomCat = kitties[random.nextInt(kitties.length - 1)];
		}
		int angle_offset = Math.round((float)360/num);
		
		if(hasBeard){
			
			for(int i = (int) (num/1.6f)+1; i < num; i+=1.7) {
				//System.out.println("new cat");
				Matrix mat2 = new Matrix();
				int x = (int) Math.round(radius*.7 * Math.cos(Math.toRadians(angle_offset * i-190))) + w-(overlay.getWidth()/2);
				int y = (int) Math.round(radius *1.2* Math.sin(Math.toRadians(angle_offset * i-190))) + h-(overlay.getHeight()/2);
				mat2.postTranslate(x,y);
				if(!lotsOfCats){
					overlay = BitmapFactory.decodeResource(this.getResources(),
						randomCat);
				}else{
					overlay = BitmapFactory.decodeResource(this.getResources(),
						kitties[random.nextInt(kitties.length - 1)]);	
				}
				overlay = getResizedBitmap(overlay, (int)(radius/1.9));
				bm = compositeBitmap(bm, overlay, mat2);
				// flush bitmap ?
			}
		}
		
		
		if(hasHalo){
			for(int i = 0; i < num; i++) {
				System.out.println("new cat");
				Matrix mat2 = new Matrix();
				int x = (int) Math.round(radius* Math.cos(Math.toRadians(angle_offset * i-90))) + w-(overlay.getWidth()/2);
				int y = (int) Math.round(radius* Math.sin(Math.toRadians(angle_offset * i-90))) + h-(overlay.getHeight()/2);
				mat2.postTranslate(x,y);
				if(!lotsOfCats){
					overlay = BitmapFactory.decodeResource(this.getResources(),
						randomCat);
				}else{
					overlay = BitmapFactory.decodeResource(this.getResources(),
						kitties[random.nextInt(kitties.length - 1)]);	
				}
				overlay = getResizedBitmap(overlay, (int)(radius/1.9));
				bm = compositeBitmap(bm, overlay, mat2);
			}
		}
		
		//$y = round($r * cos(deg2rad($angle_offset * $count - 90)) + $r*3, 3);
		//$x = round($r * sin(deg2rad($angle_offset * $count - 90)) + $r*3, 3);
		
		return bm;
	}
	
	public int[] primeKitties() {
		int[] images = {
			    R.drawable.c1, R.drawable.c2,
			    R.drawable.c3, R.drawable.c4,
			    //R.drawable.c5, R.drawable.c6,
			    /*R.drawable.c7,*/ R.drawable.c8,
			    //R.drawable.c9, /*R.drawable.c10,*/
			    R.drawable.c11, R.drawable.c12,
			    R.drawable.c13, /*R.drawable.c14,*/
			    R.drawable.c15, R.drawable.c16,
			    R.drawable.c17, R.drawable.c18,
			    /*R.drawable.c19,*/ R.drawable.c20,
			    R.drawable.c21, R.drawable.c22,
			    R.drawable.c23, R.drawable.c24,
			    R.drawable.c25, R.drawable.c26,
			    R.drawable.c27, R.drawable.c28,
			    R.drawable.cage
			    };	
		
		return images;
	}
	
}
