package ru.vologda.myfirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.timessquare.CalendarPickerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText email, pass;
    private FirebaseAuth mAuth;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // показываем стартовое окошко для логина, спустя 2 секунды
                setContentView(R.layout.activity_main);
            }
        }, 2000);

        // показываем заставку
        setContentView(R.layout.welcome);
        mAuth = FirebaseAuth.getInstance();

    }
    // обрабатываем попытку логина пользователя
    public void login(View view) {
        email = findViewById(R.id.edit_text_email);
        pass = findViewById(R.id.edit_text_password);
        String mail = email.getText().toString();
        String pwd = pass.getText().toString();
        if(!mail.equals("") && !pwd.equals("")){
            mAuth.signInWithEmailAndPassword(mail, pwd)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                showCalendar();

                            } else {
                                Toast.makeText(MainActivity.this, "Ошибка входа.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

        } else{
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
    }
    // показываем календарь на экране. Основной рабочий экран.
    public void showCalendar(){
        //получаем массив дат уже имеющихся в базе
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ArrayList<String> dates = new ArrayList<>();
        db.collection("Days info")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                dates.add(document.getId());
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            //when dates are ready = show them

                            //показываем календарь дат
                            setContentView(R.layout.calendar_layout);
                            Date today = new Date();
                            Calendar nextYear = Calendar.getInstance();

                            //ставим стартовый день, раньше него выбрать будет нельзя
                            Calendar nextDay = Calendar.getInstance();
                            nextDay.set(2020,5,23);
                              nextDay.add(Calendar.DAY_OF_YEAR,-30);

                            ///генерируем массив дат, уже имеющихся в базе, для подсвечивания
                            List <Date> myDates = converter(dates);

                            //указываем до какой даты должен показываться календарь
                            nextYear.add(Calendar.YEAR,1);
                            CalendarPickerView datePicker = findViewById(R.id.calendar);
                            datePicker.init(nextDay.getTime(), nextYear.getTime()).withSelectedDate(today)
                                    .withHighlightedDates(myDates);




                            datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                                @Override
                                public void onDateSelected(Date date) {

                                    Calendar calSelected = Calendar.getInstance();
                                    calSelected.setTime(date);

                                    String selectedDate = ""+ calSelected.get(Calendar.DAY_OF_MONTH) + " "+
                                            (calSelected.get(Calendar.MONTH)+1)+" "+calSelected.get(Calendar.YEAR);

                                    Toast.makeText(MainActivity.this, selectedDate, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                                    intent.putExtra("date",selectedDate);
                                    startActivityForResult(intent,2);
                                }

                                @Override
                                public void onDateUnselected(Date date) {

                                }
                            });

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    //нам неважен ответ возвращаемый другой активностью, мы просто обновляем календарь
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showCalendar();
    }

    // метод для перевода массива строк в массив дат
    public ArrayList<Date> converter(ArrayList<String> list ){
        ArrayList<Date> result = new ArrayList<>();
        Log.d(TAG, "converter: working");
        for( String s: list) {
            String []  arr = s.split(" ");
            Log.d(TAG, "converter: "+arr.toString());
            Calendar day = Calendar.getInstance();
            day.set(Integer.parseInt(arr[2]),Integer.parseInt(arr[1])-1,Integer.parseInt(arr[0]));
            result.add(day.getTime());
        }
        return result;
    }


}
