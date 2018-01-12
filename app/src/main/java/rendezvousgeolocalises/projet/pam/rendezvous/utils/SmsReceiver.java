package rendezvousgeolocalises.projet.pam.rendezvous.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import java.io.IOException;

import rendezvousgeolocalises.projet.pam.rendezvous.R;
import rendezvousgeolocalises.projet.pam.rendezvous.activities.MainActivity;
import rendezvousgeolocalises.projet.pam.rendezvous.model.RendezVous;

public class SmsReceiver extends BroadcastReceiver
{
    private final String   ACTION_RECEIVE_SMS  = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(ACTION_RECEIVE_SMS))
        {
            Bundle bundle = intent.getExtras();
            if (bundle != null)
            {
                Object[] pdus = (Object[]) bundle.get("pdus");

                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++)  {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);  }  if (messages.length > -1){
                    final String messageBody = messages[0].getMessageBody();

                    String content = context.getResources().getString(R.string.message_header);
                    if(messageBody.startsWith(content)){
                        try {
                            RendezVous.deserialize(context, messageBody.substring(content.length()));
                            createNotification(context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        }

    }

    private final void createNotification(Context context){
        final NotificationManager mNotification = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        final Intent launchNotifiactionIntent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, launchNotifiactionIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder builder = new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_menu_map)
                .setContentTitle(context.getResources().getString(R.string.notification_title))
                .setContentText(context.getResources().getString(R.string.notification_desc))
                .setContentIntent(pendingIntent);

        mNotification.notify(0, builder.build());
    }

}