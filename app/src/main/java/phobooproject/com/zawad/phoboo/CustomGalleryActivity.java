package phobooproject.com.zawad.phoboo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import phobooproject.com.zawad.phoboo.Adapter.GridViewAdapter;
import phobooproject.com.zawad.phoboo.SessionHolder.SessionManager;

/**
 * Created by ws5103 on 11/8/17.
 */

public class CustomGalleryActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Button selectImages;
    private GridView galleryImagesGridView;
    private ArrayList<String> galleryImageUrls;
    private GridViewAdapter imagesAdapter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customgallery_activity);

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
               onload();

            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {
            // Code for Below 23 API Oriented Device
            // Do next code
            onload();
        }


    }

    //load the initial views and app components
    private void onload(){
        if (!session.isLoggedIn()) {
            logOutUser();
        }
        initViews();
        setListeners();
        fetchGalleryImages();
        setUpGridView();

    }

    //Init all views
    private void initViews() {
        selectImages = findViewById(R.id.selectImagesBtn);
        galleryImagesGridView = findViewById(R.id.galleryImagesGridView);

    }

    //fetch all images from gallery
    private void fetchGalleryImages() {
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};//get all columns of type images
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;//order data by date
        Cursor imagecursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy + " DESC");//get all data in Cursor by sorting in DESC order

        galleryImageUrls = new ArrayList<String>();//Init array


        //Loop to cursor count
        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);//get column index
            galleryImageUrls.add(imagecursor.getString(dataColumnIndex));//get Image from column index
            System.out.println("Array path" + galleryImageUrls.get(i));
        }


    }
    //Set Listeners method
    private void setListeners() {
        selectImages.setOnClickListener(this);
    }

    //Set Up GridView method
    private void setUpGridView() {
        imagesAdapter = new GridViewAdapter(CustomGalleryActivity.this, galleryImageUrls, true);
        galleryImagesGridView.setAdapter(imagesAdapter);
    }

    //Show hide select button if images are selected or deselected
    public void showSelectButton() {
        ArrayList<String> selectedItems = imagesAdapter.getCheckedItems();
        if (selectedItems.size() > 0) {
            selectImages.setText(selectedItems.size() + " - Images Selected");
            selectImages.setVisibility(View.VISIBLE);
        } else
            selectImages.setVisibility(View.GONE);

    }
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.selectImagesBtn:

                //When button is clicked then fill array with selected images
                ArrayList<String> selectedItems = imagesAdapter.getCheckedItems();

                //Send back result to MainActivity with selected images
                Intent intent = new Intent();
                intent.putExtra(HomeActivity.CustomGalleryIntentKey, selectedItems.toString());//Convert Array into string to pass data
                setResult(RESULT_OK, intent);//Set result OK
                finish();//finish activity
                break;

        }

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(CustomGalleryActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(CustomGalleryActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(CustomGalleryActivity.this, "Read External Storage permission allows us to do read and show images from your Gallery. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(CustomGalleryActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                    onload();
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    private void logOutUser(){
        session.setLogin(false);
        // Launching the login activity
        Intent intent = new Intent(CustomGalleryActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }
}
