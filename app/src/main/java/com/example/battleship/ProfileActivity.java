package com.example.battleship;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarDefaultImage;
import com.timgroup.jgravatar.GravatarRating;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    private ListView scores;
    private ImageView userImage;
    private EditText nickname;

    private Uri filePath;
    private UserConnection userConnection;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userConnection = (UserConnection)getIntent().getSerializableExtra("UserConnection");
        initWidgets();
        if (userConnection.getUser().getNickname() != null)
            nickname.setText(userConnection.getUser().getNickname());

        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final Context context = this;
        db.collection("scores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> result;
                            HashMap<String, String> map;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                result = document.getData();
                                map = new HashMap<>();
                                if (result.get("winner").toString().equals(userConnection.getUser().getEmail())) {
                                    map.put("email", "Opponent: " + result.get("looser").toString());
                                    map.put("result", "Result: Won");
                                }
                                else if (result.get("looser").toString().equals(userConnection.getUser().getEmail()))
                                {
                                    map.put("email", "Opponent: " + result.get("winner").toString());
                                    map.put("result", "Result: Lost");
                                }
                                arrayList.add(map);
                            }
                        }

                        SimpleAdapter adapter = new SimpleAdapter(context, arrayList, android.R.layout.simple_list_item_2,
                                new String[]{"email", "result"},
                                new int[]{android.R.id.text1, android.R.id.text2});
                        scores.setAdapter(adapter);
                    }
                });

        if(userConnection.getUser().getImageUrl() != null)
        {
            StorageReference photoReference = storageReference.child(userConnection.getUser().getImageUrl());

            final long ONE_MEGABYTE = 1024 * 1024;
            photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    userImage.setImageBitmap(bmp);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            Gravatar gravatar = new Gravatar();
            gravatar = gravatar.setSize(100);
            gravatar = gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
            gravatar = gravatar.setDefaultImage(GravatarDefaultImage.IDENTICON);
            String url = gravatar.getUrl(userConnection.getUser().getEmail());
            try {
                Picasso.with(context).load(url).into(userImage);
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "No Such gravatar profile or image doesn't exist!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initWidgets()
    {
        nickname = (EditText) findViewById(R.id.Nickname);
        scores = (ListView) findViewById(R.id.Results);
        userImage = (ImageView) findViewById(R.id.UserImage);
    }

    public void saveNickname(View view) {
        userConnection.getUser().setNickname(nickname.getText().toString());
        uploadImage();

        final Context context = this;
        db.collection("users")
                .whereEqualTo("email", userConnection.getUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       String reference = "";
                       if (task.isSuccessful()) {
                           for (QueryDocumentSnapshot document : task.getResult()) {
                               reference = document.getId();
                           }
                           Map<String, Object> nickname = new HashMap<>();
                           nickname.put("nickname", userConnection.getUser().getNickname());
                           if (userConnection.getUser().getImageUrl() != null)
                               nickname.put("imageUrl", userConnection.getUser().getImageUrl());
                           db.collection("users").document(reference).update(nickname);
                       }
                   }
               });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        intent.putExtra("UserConnection", userConnection);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("key", nickname.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        nickname.setText(savedInstanceState.getString("key"));
    }

    public void chooseImage(View view) {
        selectImage();
    }

    private void selectImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                userImage.setImageBitmap(bitmap);
            }
            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void uploadImage()
    {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            userConnection.getUser().setImageUrl("images/" + UUID.randomUUID().toString());
            StorageReference ref = storageReference.child(userConnection.getUser().getImageUrl());
            ref.putFile(filePath).addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this,"Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                                }
                            });
        }
    }
}
