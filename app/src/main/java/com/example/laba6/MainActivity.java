package com.example.laba6;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextMessage;
    private Button buttonSetDate, buttonSetTime, buttonSaveReminder, buttonViewReminders;
    private Calendar reminderDate;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSetDate = findViewById(R.id.buttonSetDate);
        buttonSetTime = findViewById(R.id.buttonSetTime);
        buttonSaveReminder = findViewById(R.id.buttonSaveReminder);
        buttonViewReminders = findViewById(R.id.buttonViewReminders);

        reminderDate = Calendar.getInstance();

        buttonSetDate.setOnClickListener(v -> showDatePicker());
        buttonSetTime.setOnClickListener(v -> showTimePicker());
        buttonSaveReminder.setOnClickListener(v -> saveReminder());
        buttonViewReminders.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderListActivity.class);
            startActivity(intent);
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    reminderDate.set(Calendar.YEAR, year);
                    reminderDate.set(Calendar.MONTH, month);
                    reminderDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                },
                reminderDate.get(Calendar.YEAR),
                reminderDate.get(Calendar.MONTH),
                reminderDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    reminderDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    reminderDate.set(Calendar.MINUTE, minute);
                },
                reminderDate.get(Calendar.HOUR_OF_DAY),
                reminderDate.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void saveReminder() {
        String title = editTextTitle.getText().toString();
        String message = editTextMessage.getText().toString();
        long date = reminderDate.getTimeInMillis();

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        ContentValues values = new ContentValues();
        values.put(ReminderDatabaseHelper.COLUMN_TITLE, title);
        values.put(ReminderDatabaseHelper.COLUMN_MESSAGE, message);
        values.put(ReminderDatabaseHelper.COLUMN_DATE, String.valueOf(date));

        long newRowId = dbHelper.getWritableDatabase().insert(ReminderDatabaseHelper.TABLE_NAME, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Напоминание сохранено", Toast.LENGTH_SHORT).show();
            setReminderAlarm(date, title, message);
        } else {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    private void setReminderAlarm(long reminderTime, String title, String message) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) reminderTime,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
    }
}