import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Trigger a notification or perform some action
        Toast.makeText(context, "Exact Alarm Triggered!", Toast.LENGTH_SHORT).show();
    }
}
