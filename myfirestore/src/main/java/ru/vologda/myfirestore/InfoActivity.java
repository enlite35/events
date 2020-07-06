package ru.vologda.myfirestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InfoActivity extends AppCompatActivity {

    private EditText editText;
    private Button send, cancel;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String date;

    // ключи полей
    private static final String KEY_INFO = "text";
    private static final String KEY_EDIT = "editable";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_activity);
        editText = findViewById(R.id.edit_text_info);
        send = findViewById(R.id.send);
        cancel = findViewById(R.id.cancel);
        //получаем дату из интента
        date = getIntent().getStringExtra("date");

        db.collection("Days info").document(date).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String text = documentSnapshot.getString(KEY_INFO);
                    Boolean editable = documentSnapshot.getBoolean(KEY_EDIT);
                   editText.setText(text);
                } else {
                   editText.setText("Пусто");
                }
            }
        });

    }



    public void back(View view) {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }

    public void send(View view) {
        String text = editText.getText().toString();
        Boolean editable = true;
        Map<String, Object> day = new HashMap<>();
        day.put(KEY_INFO, text);
        day.put(KEY_EDIT, editable);
        db.collection("Days info").document(date).set(day)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(InfoActivity.this, "Сохранено!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(InfoActivity.this, "Что-то пошло не так...", Toast.LENGTH_SHORT).show();
                Log.d("myError",e.toString());
            }
        });
        //возвращаемся в календарь
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }
}
