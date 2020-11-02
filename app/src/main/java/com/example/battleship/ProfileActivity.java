package com.example.battleship;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    ListView scores;
    ImageView userImage;
    EditText nickname;
    User user;
    FirebaseFirestore db;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = (User)getIntent().getSerializableExtra("User");
        initWidgets();
        if (user.getNickname() != null)
            nickname.setText(user.getNickname());

        db = FirebaseFirestore.getInstance();
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
                                if (result.get("winner").toString().equals(user.getEmail())) {
                                    map.put("email", result.get("looser").toString());
                                    map.put("result", "Won");
                                }
                                else if (result.get("looser").toString().equals(user.getEmail()))
                                {
                                    map.put("email", result.get("winner").toString());
                                    map.put("result", "Lost");
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
    }

    private void initWidgets()
    {
        nickname = (EditText) findViewById(R.id.Nickname);
        scores = (ListView) findViewById(R.id.Results);
        userImage = (ImageView) findViewById(R.id.UserImage);
    }

    public void saveNickname(View view) {
        user.setNickname(nickname.getText().toString());

        final Context context = this;
        db.collection("users")
                .whereEqualTo("email", user.getEmail())
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
                           nickname.put("nickname", user.getNickname());
                           db.collection("users").document(reference).update(nickname);
                       }
                   }
               });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        intent.putExtra("User", user);
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
}
