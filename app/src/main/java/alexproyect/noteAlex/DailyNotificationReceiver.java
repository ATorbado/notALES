package alexproyect.noteAlex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class DailyNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Cargar las notas desde el almacenamiento
        List<Note> noteList = NoteStorage.loadNotes(context);

        // Enviar notificación con las notas
        NotificationHelper.sendNotesNotification(context, noteList);
    }
}
