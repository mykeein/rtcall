package rtc.lyuzik.com.rtcall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import rtc.lyuzik.com.rtcall.services.CallRecordingService;

/**
 * Created by static on 4/18/2015.
 */
public class RecordChooseActivity extends Activity {
    public static final String PHONE_NUMBER = "phone_number";
    private String phoneNumber = null;
    private Switch _switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordchoose);
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
        if(getIntent()!=null) {
            phoneNumber = getIntent().getStringExtra(PHONE_NUMBER);
            Log.d("RecordChooseActivity", "[onCreate] no:" + phoneNumber);
            _switch = (Switch) findViewById(R.id.record_call_switch);
            _switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Log.i("RecordChooseActivity", "[onCreate] start recording no:" + phoneNumber);
                        CallRecordingService.startRecord();
                        Toast.makeText(RecordChooseActivity.this, "call recording", Toast.LENGTH_LONG);
                        RecordChooseActivity.this.finish();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        Log.i("RecordChooseActivity", "[onResume] no:" + phoneNumber);
        if(phoneNumber==null){
            finish();
        }
        super.onResume();
    }
}
