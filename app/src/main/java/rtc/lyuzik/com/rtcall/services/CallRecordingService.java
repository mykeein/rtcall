package rtc.lyuzik.com.rtcall.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

import rtc.lyuzik.com.rtcall.R;
import rtc.lyuzik.com.rtcall.RecordChooseActivity;

public class CallRecordingService extends Service implements Runnable {

    private boolean voiceRecordFailed;
    private String phoneNumber = null;
    private MediaRecorder recorder;
    private final Handler mHandler = new Handler();
    private static boolean recording = false;
    private String INCOMING_CALL_ACTION = "android.intent.action.PHONE_STATE";
    private static Thread recordReadyThread;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            MyPhoneStateListener phoneListener = new MyPhoneStateListener(
                    context);
            telephonyManager.listen(phoneListener,
                    PhoneStateListener.LISTEN_CALL_STATE);
            Log.d("BroadcastReceiver", "[onReceive]");
        }

        class MyPhoneStateListener extends PhoneStateListener {

            private Context context;

            MyPhoneStateListener(Context c) {
                super();
                context = c;
            }

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d("MyPhoneStateListener", "[CALL_STATE_IDLE] no:" + incomingNumber);
                        if (recording) {
                            NotificationManager mNotificationManager = (NotificationManager) context
                                    .getSystemService(Context.NOTIFICATION_SERVICE);
                            Notification not;

                            not = new Notification(R.drawable.ic_launcher,
                                    "Phone call being processed",
                                    System.currentTimeMillis());
                            Intent notIntent = new Intent();
                            PendingIntent contentIntent = PendingIntent
                                    .getActivity(context, 0, notIntent, 0);
                            not.setLatestEventInfo(context, "CallRecordingService",
                                    "Notification from idle",
                                    contentIntent);
                            mNotificationManager.notify(NOTIFICATION_ID_RECEIVED,
                                    not);
                            stopRecording();
                        }
                        break;
                    //If call is answered, run recording service. Also pass "phone_number" variable with incomingNumber to shared prefs, so service will be able to access that via shared prefs.
                    case TelephonyManager.CALL_STATE_OFFHOOK: // The call is answered
                        Log.d("MyPhoneStateListener", "[CALL_STATE_OFFHOOK] no:" + incomingNumber);
                        if(!recording && incomingNumber!=null && incomingNumber.length()>0 && phoneNumber==null){
                            phoneNumber=incomingNumber;
                            Intent recordChooseActivity = new Intent(context,RecordChooseActivity.class);
                            recordChooseActivity.putExtra(RecordChooseActivity.PHONE_NUMBER,phoneNumber);
                            recordChooseActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            recordReadyThread = new Thread(CallRecordingService.this);
                            try {
                                Thread.sleep(2000);
                                context.startActivity(recordChooseActivity);
                            } catch (Exception e) {
                                e.getLocalizedMessage();
                            }
                        }
                        break;
                    //If phone is ringing, save phone_number. This is done because incomingNumber is not saved on CALL_STATE_OFFHOOK
                    case TelephonyManager.CALL_STATE_RINGING:
                        Log.d("MyPhoneStateListener", "[CALL_STATE_RINGING] no:" + incomingNumber);
                        break;
                }
            }

        }
    };

    @Override
    public void onCreate() {
        IntentFilter intentToReceiveFilter = new IntentFilter();
        intentToReceiveFilter.addAction(INCOMING_CALL_ACTION);
        registerReceiver(broadcastReceiver, intentToReceiveFilter, null, mHandler);
        Log.d("CallRecordingService", "[onCreate] - registerReceiver");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        Log.d("CallRecordingService", "[onDestroy] - unregisterReceiver");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public static final int NOTIFICATION_ID_RECEIVED = 0x1221;

    @Override
    public void run() {
        Looper.myLooper();
        Looper.prepare();
        String absolutePath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/RTCall_" + phoneNumber + "_"+System.currentTimeMillis()+".3gp";
        startVoiceRecord(absolutePath);
        if(voiceRecordFailed){
            startMicRecord(absolutePath);
        }
    }

    private void startMicRecord(String absolutePath) {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(absolutePath);

            recorder.prepare();
            recording = true;
            Log.d("CallRecordingService", "Start Mic Recording absolute path:" + absolutePath);
            //Activate loudspeaker
            AudioManager audioManager = (AudioManager)
                    getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
            recorder.start();
        } catch (Exception e) {
            Log.e("CallRecordingService", "Start Mic Recording absolute path:" + absolutePath, e);
        }
    }

    private void startVoiceRecord(String absolutePath){
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(absolutePath);
            recorder.prepare();
            recording = true;
            Log.d("CallRecordingService", "Start Voice Recording absolute path:" + absolutePath);
            recorder.start();
        }catch (RuntimeException rte){
            voiceRecordFailed = true;
            Log.i("CallRecordingService", "Start Voice Recording absolute path:" + absolutePath, rte);
        }catch (Exception e){
            voiceRecordFailed = true;
            Log.i("CallRecordingService", "Start Voice Recording absolute path:" + absolutePath, e);
        }
    }

    void stopRecording() {
        Log.d("CallRecordingService", "Stop Recording no:" + phoneNumber);
        try {
            recorder.stop();
            recorder.release();
        }catch (Exception e){e.printStackTrace();}
        recording=false;
        //Deactivate loudspeaker
        AudioManager audioManager = (AudioManager)
                getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if(audioManager.isSpeakerphoneOn())
            audioManager.setSpeakerphoneOn(false);
        phoneNumber=null;
    }

    public static void startRecord() {
        if(recordReadyThread!=null){
            recordReadyThread.start();
        }
    }
}