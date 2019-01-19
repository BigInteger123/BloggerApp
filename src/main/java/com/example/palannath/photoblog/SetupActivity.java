package com.example.palannath.photoblog;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupimage;
    private EditText setupname;
    private Button setupbtn;
    private String user_id;


    private Uri mainimageuri=null;
    private  boolean ischanged=false;

    private ProgressBar setupprogress;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setupimage=(CircleImageView)findViewById(R.id.setup_profile);
        Toolbar setupToolbar =findViewById(R.id.tb);
        setupname =(EditText)findViewById(R.id.setup_name);
        setupbtn =(Button)findViewById(R.id.setup_btn);
        setupprogress=(ProgressBar)findViewById(R.id.setup_progress);

        firebaseFirestore =FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        storageReference= FirebaseStorage.getInstance().getReference();

        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        setupprogress.setVisibility(View.VISIBLE);
        setupbtn.setEnabled(false);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    if(task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");
                        mainimageuri =Uri.parse(image);
                        setupname.setText(name);
                        RequestOptions placeholderrequest = new RequestOptions();
                        placeholderrequest.placeholder(R.drawable.defalut_image2);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderrequest).load(image).into(setupimage);
                    }
                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"Firestore Retrived Error"+error,Toast.LENGTH_LONG).show();
                }
                setupprogress.setVisibility(View.INVISIBLE);
                setupbtn.setEnabled(true);
            }
        });

        setupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = setupname.getText().toString();
                setupprogress.setVisibility(View.VISIBLE);
                if(ischanged){


                    if (!TextUtils.isEmpty(username) && mainimageuri != null) {
                        user_id = firebaseAuth.getCurrentUser().getUid();


                        StorageReference image_path = storageReference.child("profile images").child(user_id + ".jpg");

                        image_path.putFile(mainimageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    storeFirestore(task, username);

                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Image Error" + error, Toast.LENGTH_LONG).show();
                                    setupprogress.setVisibility(View.INVISIBLE);
                                }

                            }
                        });
                    }
                }
                else{
                    storeFirestore(null,username);
                }
            }
        });

        setupimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetupActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else {
                            BringImagePicker();
                    }
                }
                else{
                    BringImagePicker();
                    
                }
            }
        });
    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String username){
        Uri download_uri;
        if(task != null){
            download_uri= task.getResult().getDownloadUrl();
        }
        else{
            download_uri = mainimageuri;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name",username);
        userMap.put("image",download_uri.toString());
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    Toast.makeText(SetupActivity.this,"User Settings Updated",Toast.LENGTH_LONG).show();
                    Intent mainintent = new Intent(SetupActivity.this,MainActivity.class);
                    startActivity(mainintent);
                    finish();
                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"Firestore Error"+error,Toast.LENGTH_LONG).show();
                }
                setupprogress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void BringImagePicker(){

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainimageuri= result.getUri();

                setupimage.setImageURI(mainimageuri);
                ischanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
