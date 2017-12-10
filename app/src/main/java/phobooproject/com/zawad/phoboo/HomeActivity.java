package phobooproject.com.zawad.phoboo;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

//import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
//import com.android.volley.VolleyError;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.StringRequest;
import com.android.volley.request.StringRequest;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import phobooproject.com.zawad.phoboo.Adapter.GridViewAdapter;
import phobooproject.com.zawad.phoboo.RequestUtils.CommandExec;
import phobooproject.com.zawad.phoboo.RequestUtils.RequestSingleton;
import phobooproject.com.zawad.phoboo.SessionHolder.SessionManager;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Button openCustomGallery,uploadButton,logoutButton;
    private GridView selectedImageGridView;
    private ArrayList<String> uploadArraylist;

    private static final int CustomGallerySelectId = 1;//Set Intent Id
    public static final String CustomGalleryIntentKey = "ImageArray";//Set Intent Key Value
    private static final int progressId = 100;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        setListeners();
        getSharedImages();

        session = SessionManager.getInstance(getApplicationContext());

        if (!session.isLoggedIn()) {
            logOutUser();
        }
    }

    //Init all views
    private void initViews() {
        openCustomGallery = (Button) findViewById(R.id.openCustomGallery);
        uploadButton = (Button) findViewById(R.id.uploadImageButton);
        selectedImageGridView = (GridView) findViewById(R.id.selectedImagesGridView);
        logoutButton = (Button) findViewById(R.id.logoutButton);
    }

    //set Listeners
    private void setListeners() {
        openCustomGallery.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openCustomGallery:
                //Start Custom Gallery Activity by passing intent id
                startActivityForResult(new Intent(HomeActivity.this, CustomGalleryActivity.class), CustomGallerySelectId);
                break;
            case R.id.uploadImageButton:
                uploadImage();
                break;
            case R.id.logoutButton:
                logOutUser();
                break;
        }

    }

    protected void onActivityResult(int requestcode, int resultcode,
                                    Intent imagereturnintent) {
        super.onActivityResult(requestcode, resultcode, imagereturnintent);
        switch (requestcode) {
            case CustomGallerySelectId:
                if (resultcode == RESULT_OK) {
                    String imagesArray = imagereturnintent.getStringExtra(CustomGalleryIntentKey);//get Intent data
                    //Convert string array into List by splitting by ',' and substring after '[' and before ']'
                    //List<String> selectedImages = Arrays.asList(imagesArray.substring(1, imagesArray.length() - 1).split(", "));
                    uploadArraylist = new ArrayList<String>(Arrays.asList(imagesArray.substring(1, imagesArray.length() - 1).split(", ")));
                    //loadGridView(new ArrayList<String>(selectedImages)); //call load gridview method by passing converted list into arrayList
                    loadGridView(uploadArraylist);
                    showUploadButton();
                }
                break;

        }
    }

    //Load GridView
    private void loadGridView(ArrayList<String> imagesArray) {
        GridViewAdapter adapter = new GridViewAdapter(HomeActivity.this, imagesArray, false);
        selectedImageGridView.setAdapter(adapter);
    }

    //Read Shared Images
    private void getSharedImages() {

        //If Intent Action equals then proceed
        if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())
                && getIntent().hasExtra(Intent.EXTRA_STREAM)) {
            ArrayList<Parcelable> list =
                    getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);//get Parcelabe list
            ArrayList<String> selectedImages = new ArrayList<>();

            //Loop to all parcelable list
            for (Parcelable parcel : list) {
                Uri uri = (Uri) parcel;//get URI
                String sourcepath = getPath(uri);//Get Path of URI
                selectedImages.add(sourcepath);//add images to arraylist
            }

            loadGridView(selectedImages);//call load gridview
        }
    }


    //get actual path of uri
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void showUploadButton(){
        if(uploadArraylist.size() > 0){
            uploadButton.setText("Upload "+uploadArraylist.size()+" Images");
            uploadButton.setVisibility(View.VISIBLE);
        }else {
            uploadButton.setVisibility(View.GONE);
        }
    }

    private void uploadImage(){
        String serverUrl = "http://192.168.0.105/UploadExample/upload.php";
        CommandExec command = new CommandExec(getApplicationContext());

        final NotificationManager mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Picture Upload")
                .setContentText("Upload in progress")
                .setSmallIcon(R.mipmap.ic_launcher_round);
        final int maxUploadSize = uploadArraylist.size();
        mBuilder.setProgress(maxUploadSize, 0, false);
        mNotifyManager.notify(progressId, mBuilder.build());
        final int[] counter = {1};

        for(String imagePath : uploadArraylist) {
            String imageUri = "file://" + imagePath;
            Bitmap ImageBitmap = ImageLoader.getInstance().loadImageSync(imageUri);
            final String enocodedImage = imageToString(ImageBitmap);



            StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                    mBuilder.setProgress(maxUploadSize, counter[0], false);
                    mNotifyManager.notify(progressId, mBuilder.build());
                    counter[0] = counter[0] + 1;

                    if(counter[0] == (maxUploadSize + 1)){
                        mBuilder.setContentText("Upload complete")
                                .setProgress(0,0,false);
                        mNotifyManager.notify(progressId, mBuilder.build());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),"Error while uploading image",Toast.LENGTH_LONG).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> paramsMap = new HashMap<>();
                    paramsMap.put("image",enocodedImage);
                   // paramsMap.put("name","zawad"); //TODO :Uncomment when using DB
                    return paramsMap;
                }
            };
         command.add(stringRequest);
       /*     SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, serverUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);
                            try {
                                JSONObject jObj = new JSONObject(response);
                                String message = jObj.getString("message");

                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                            } catch (JSONException e) {
                                // JSON error
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            smr.addFile("image",imagePath);
            command.add(smr);*/
        }


        command.execute();



    }

    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);

    }

    private void logOutUser(){
        session.setLogin(false);
        // Launching the login activity
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }
}
