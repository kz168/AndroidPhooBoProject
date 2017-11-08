package phobooproject.com.zawad.phoboo;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

import phobooproject.com.zawad.phoboo.Adapter.GridViewAdapter;

/**
 * Created by ws5103 on 11/8/17.
 */

public class CustomGalleryActivity extends AppCompatActivity implements View.OnClickListener {
    private Button selectImages;
    private GridView galleryImagesGridView;
    private ArrayList<String> galleryImageUrls;
    private GridViewAdapter imagesAdapter;

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

    }
}
