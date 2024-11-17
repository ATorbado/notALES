package alexproyect.noteAlex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private List<Note> noteList;
    private static final String PREFS_NAME = "notes_prefs";
    private static final String NOTES_KEY = "notes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crear el canal de notificación
        NotificationHelper.createNotificationChannel(this);

        // Inicializa la lista de notas
        noteList = new ArrayList<>();
        loadNotes();  // Carga las notas al iniciar

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crea el adaptador con la lista de notas
        notesAdapter = new NotesAdapter(noteList, this);
        recyclerView.setAdapter(notesAdapter);

        // Habilitar el swipe para eliminar
        notesAdapter.attachSwipeToDelete(recyclerView);

        // Programar la notificación diaria
        scheduleDailyNotification();

        Button addButton = findViewById(R.id.button_add_note);
        addButton.setOnClickListener(v -> showAddNoteDialog());
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNotes();  // Guarda las notas cuando la actividad se pausa
    }

    private void scheduleDailyNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DailyNotificationReceiver.class);

        // Crear un PendingIntent con FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Especificar FLAG_IMMUTABLE
        );

        // Configura la hora para las 10:30 AM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);

        // Si la hora ya pasó hoy, programa para mañana
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Configura el alarm
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Nota");

        final EditText input = new EditText(this);
        input.requestFocus();
        builder.setView(input);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String noteTitle = input.getText().toString();
            if (!noteTitle.isEmpty()) {
                noteList.add(new Note(noteTitle));
                notesAdapter.notifyItemInserted(noteList.size() - 1);
                // Notificar después de agregar
                NotificationHelper.sendNotesNotification(this, noteList);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void showEditNoteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Nota");

        final EditText input = new EditText(this);
        input.setText(noteList.get(position).getTitle());
        builder.setView(input);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String newTitle = input.getText().toString();
            if (!newTitle.isEmpty()) {
                noteList.get(position).setTitle(newTitle);
                notesAdapter.notifyItemChanged(position);
                // Notificar después de editar
                NotificationHelper.sendNotesNotification(this, noteList);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void saveNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(noteList);
        editor.putString(NOTES_KEY, json);
        editor.apply();
    }

    private void loadNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(NOTES_KEY, null);
        Type type = new TypeToken<List<Note>>() {}.getType();
        noteList = gson.fromJson(json, type);

        if (noteList == null) {
            noteList = new ArrayList<>();
        }
    }
}
