package alexproyect.noteAlex;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotesWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "notes_prefs";
    private static final String NOTES_KEY = "notes";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notes_widget);

        // Obtener las notas guardadas
        List<Note> noteList = loadNotes(context);

        // Si no hay notas, mostrar un mensaje predeterminado
        if (noteList.isEmpty()) {
            views.setTextViewText(R.id.widget_notes_text, "No tienes notas.");
        } else {
            StringBuilder notesString = new StringBuilder();
            for (Note note : noteList) {
                notesString.append(note.getTitle()).append("\n");
            }
            views.setTextViewText(R.id.widget_notes_text, notesString.toString());
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private List<Note> loadNotes(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(NOTES_KEY, null);
        Type type = new TypeToken<List<Note>>() {}.getType();
        List<Note> noteList = gson.fromJson(json, type);

        if (noteList == null) {
            noteList = new ArrayList<>();
        }

        return noteList;
    }
}
