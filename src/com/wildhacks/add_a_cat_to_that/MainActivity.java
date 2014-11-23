package com.wildhacks.add_a_cat_to_that;

import java.io.File;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
		//Intent intent = new Intent(this, DisplayMessageActivity.class);
	}
	
	public void takePicture(View view) {
	    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
	    File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
	    intent.putExtra(MediaStore.EXTRA_OUTPUT,
	            Uri.fromFile(photo));
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
	            try {
	                bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
	                bitmap = getResizedBitmap(bitmap, 500);

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
	    float aspectRatio = (float)width/(float)height;
	    int newHeight = (int)Math.round((float)newWidth/(float)aspectRatio);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, newWidth, newHeight, false);
	    return resizedBitmap;
	}
}
