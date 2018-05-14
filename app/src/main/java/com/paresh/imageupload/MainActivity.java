package com.paresh.imageupload;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.support.design.widget.Snackbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import java.io.IOException;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity{


    private ImageView iv_profile;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference storageImageRef;
    private UploadTask uploadTask;
    private RelativeLayout rl_profile;
    private Button bt_load_image;
    public ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircleImageView add_image = (CircleImageView) findViewById(R.id.add_image);
        iv_profile = (ImageView) findViewById(R.id.iv_profile);
        rl_profile = (RelativeLayout)findViewById(R.id.rl_profile);
        bt_load_image = (Button)findViewById(R.id.bt_load_image);
        progressBar = (ProgressBar)findViewById(R.id.progressbar);

        // Create a storage reference from our app
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Create a reference to 'images/sample.jpg'
        storageImageRef = storageRef.child("images/sample.jpg");

        // While the file names are the same, the references point to different files
        storageImageRef.getName().equals(storageImageRef.getName());    // true
        storageImageRef.getPath().equals(storageImageRef.getPath());    // false

        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(getWindow().getDecorView());
            }
        });

        bt_load_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImageFromFirebaseSotrage();
            }
        });

    }

    private void loadImageFromFirebaseSotrage() {
        Snackbar.make(getWindow().getDecorView(), "Downloading...", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        File localFile = null;
        try {
            localFile = File.createTempFile("images", ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final File finalLocalFile = localFile;
        storageImageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                //Log.d("uri", finalLocalFile.getPath().toString());
                //Loading temp image to imageview
                if(finalLocalFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                    iv_profile.setImageBitmap(myBitmap);
                    rl_profile.setVisibility(View.VISIBLE);
                    Snackbar.make(getWindow().getDecorView(), "Your photo has been loaded successfully.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                //Log.d("failed", "to load image from storage");
                Snackbar.make(getWindow().getDecorView(), "Your photo could not be loaded.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void onSelectImageClick(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("Crop")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("Done")
                .setRequestedSize(400, 400)
                .setAspectRatio(1,1)
                .setCropMenuCropButtonIcon(R.drawable.ic_done)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //upload cropped image to firebase storage
                Snackbar.make(getWindow().getDecorView(), "Uploading...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                uploadTask = storageImageRef.putFile(result.getUri());
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        //Log.d("uplaod","unsuccessful");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(getWindow().getDecorView(), "Your photo has been uploaded.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        bt_load_image.setVisibility(View.VISIBLE);
                    }
                });
                //Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                Snackbar.make(getWindow().getDecorView(), "Your photo could not be uploaded.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

}
