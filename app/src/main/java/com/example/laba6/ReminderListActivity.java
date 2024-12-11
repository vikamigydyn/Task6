package com.example.laba6;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class ReminderListActivity extends AppCompatActivity {

    private ListView listViewReminders;
    private ArrayAdapter<String> reminderAdapter;
    private final List<String> reminders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        listViewReminders = findViewById(R.id.listViewReminders);

        // Получаем данные из базы данных
        loadReminders();

        reminderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reminders);
        listViewReminders.setAdapter(reminderAdapter);

        // Обработчик кликов для удаления напоминаний
        listViewReminders.setOnItemClickListener((parent, view, position, id) -> {
            String reminder = reminders.get(position);
            deleteReminder(reminder);
            reminders.remove(position);
            reminderAdapter.notifyDataSetChanged();
        });
    }

    private void loadReminders() {
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        String query = "SELECT * FROM " + ReminderDatabaseHelper.TABLE_NAME;

        try (android.database.Cursor cursor = dbHelper.getReadableDatabase().rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(ReminderDatabaseHelper.COLUMN_TITLE));
                reminders.add(title);
            }
        }
    }

    private void deleteReminder(String title) {
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        dbHelper.getWritableDatabase().delete(
                ReminderDatabaseHelper.TABLE_NAME,
                ReminderDatabaseHelper.COLUMN_TITLE + " = ?",
                new String[]{title}
        );
    }
}