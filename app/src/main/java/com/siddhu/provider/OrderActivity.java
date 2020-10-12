package com.siddhu.provider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;

public class OrderActivity extends AppCompatActivity {
    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;
    private TextView dateTextView;
    private TextView timeTextView;

    private EditText sourecEditText;
    private EditText destinationEditText;
    private EditText noteEditText;

    private RadioGroup truckRadioGroup;

    private String source;
    private String destination;
    private String note;
    private String date;
    private String time;
    private String phoneNumber;

    private int truckType;

    private boolean dateFlag = false;
    private boolean timeFlag = false;
    private boolean truckFlag = false;

    private Map trucksId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);


        trucksId = new HashMap<String, Integer>();
        addTruckId();

        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);

        sourecEditText = findViewById(R.id.sourceEditText);
        destinationEditText= findViewById(R.id.destinationEditText);

        truckRadioGroup = findViewById(R.id.truckTypeRadioGroup);

        noteEditText = findViewById(R.id.noteEditText);


        dateTextView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.DAY_OF_MONTH);
                int year = calendar.get(Calendar.YEAR);

                datePicker = new DatePickerDialog(OrderActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateTextView.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                        date = dayOfMonth + " " + (month + 1) + " " + year;
                        dateFlag = true;
                    }
                },year,month,day);
                datePicker.show();
            }
        });

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(OrderActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeTextView.setText(selectedHour + ":" + selectedMinute);
                        time = selectedHour +" "+ selectedHour;
                        timeFlag = false;
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

                truckRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                truckType = (Integer) trucksId.get(radioButton.getText().toString());
                truckFlag = true ;
//                Toast.makeText(getApplicationContext(),String.valueOf(truckType),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean GetAllRequestInfo(){
        if(sourecEditText.getText() == null ||sourecEditText.getText().toString().trim().length() ==0){
            String msg = "Enter Source";
            showMsg(msg);
            return false;
        }
        source = sourecEditText.getText().toString();

        if(destinationEditText.getText() == null || destinationEditText.getText().toString().trim().length() ==0){
            String msg = "Enter Destination";
            showMsg(msg);
            return false;
        }
        destination = destinationEditText.getText().toString();

        if(noteEditText.getText() == null || noteEditText.getText().toString().trim().length() ==0){
            String msg = "Enter note";
            showMsg(msg);
            return false;
        }
        note = noteEditText.getText().toString();

        if(!dateFlag){
            String msg = "Enter date";
            showMsg(msg);
            return false;
        }

        if(!timeFlag){
            String msg = "Enter time";
            showMsg(msg);
            return false;
        }


        return true;
    }


    private void addTruckId(){
        trucksId.put("3 Wheeler Tempo", new Integer(1));
        trucksId.put("Tata Ace / Chota Hathi", new Integer(2));
        trucksId.put("Bolero Pickup 5 seater", new Integer(3));
        trucksId.put("Bolero Pickup 3 seater", new Integer(4));
        trucksId.put("Tata Tempo", new Integer(5));
        trucksId.put("Truck", new Integer(6));
    }



    private void showMsg(String msg){
        //Snackbar.make(findViewById(android.R.id.content).getRootView(),msg, BaseTransientBottomBar.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

}
