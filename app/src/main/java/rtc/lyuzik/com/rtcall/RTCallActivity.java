package rtc.lyuzik.com.rtcall;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import rtc.lyuzik.com.rtcall.services.CallRecordingService;


public class RTCallActivity extends ActionBarActivity {

    private Switch _switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtcall);
        _switch = (Switch) findViewById(R.id.rtcall_switch);
        _switch.setChecked(isMyServiceRunning(CallRecordingService.class));
        _switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RTCallActivity.this);
                    builder.setTitle("Record Listener is On");
                    builder.setMessage("Each input call you will bean asking If you want record next Call Conversation");
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(RTCallActivity.this, CallRecordingService.class);
                            intent.setAction("android.intent.action.PHONE_STATE");
                            ComponentName cName = startService(intent);
                            Log.d("RTCallActivity", "[startService] cName:" + cName);
                            dialog.dismiss();
                        }
                    });
                    builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            _switch.setChecked(isMyServiceRunning(CallRecordingService.class));
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }else{
                    Intent intent = new Intent(RTCallActivity.this, CallRecordingService.class);
                    intent.setAction("android.intent.action.PHONE_STATE");
                    boolean isStop = stopService(intent);
                    Log.d("RTCallActivity", "[stopService] isStop:" + isStop);
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rtcall, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
