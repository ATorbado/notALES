package alexproyect.noteAlex;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

public class NotificationHelper {

    private static final String CHANNEL_ID = "notes_channel";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notes Channel";
            String description = "Channel for notes notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Prioridad alta
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void sendNotesNotification(Context context, List<Note> noteList) {
        StringBuilder notesString = new StringBuilder();
        for (Note note : noteList) {
            notesString.append(note.getTitle()).append("\n");
        }

        // Crear un Intent para abrir la MainActivity al hacer clic en la notificación
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Crear un PendingIntent con FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE // Especificar FLAG_IMMUTABLE
        );

        // Estilo BigTextStyle para mostrar las notas completas
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(notesString.toString()) // El contenido completo de las notas
                .setBigContentTitle("Tus Notas") // Título grande cuando está expandida
                .setSummaryText("Todas tus notas"); // Texto resumen si se expande más

        // Configuración de la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Icono de la notificación
                .setContentTitle("Tus Notas") // Título cuando está comprimido
                .setStyle(bigTextStyle) // Usar el estilo BigTextStyle
                .setPriority(NotificationCompat.PRIORITY_MAX) // Prioridad alta para evitar que se comprima
                .setContentIntent(pendingIntent) // Acción al hacer clic en la notificación
                .setAutoCancel(false) // No se cancela automáticamente
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Hacer que sea pública (visible)
                .setGroup("notes_group"); // Agrupar notificaciones si es necesario

        // Establecer una prioridad alta para evitar que la notificación sea comprimida
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
