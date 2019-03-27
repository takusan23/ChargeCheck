package io.github.takusan23.chargecheck;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.widget.Toast;

public class ChargeCheckService extends Service {

    private BroadcastReceiver receiver;
    //充電状況を置いておく
    private String charging_text;
    //初回起動
    private int count = 0;
    //ばいぶ
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("サービス");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Service実行のために通知を出す
        String notification_id = "charge_check";
        String name = getString(R.string.app_name);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(notification_id, name, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            //通知作成
            Notification notification = new Notification.Builder(this, notification_id)
                    .setContentTitle("充電状態通知サービス")
                    .setSmallIcon(R.drawable.ic_battery_charging_80_black_24dp)
                    .setContentText("充電器に接続、外したときにバイブがなります。")
                    .build();
            startForeground(R.string.app_name, notification);
        }

        //バイブレーション
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //ブロードキャスト受け取り
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //ここに来るよ
                String status = batteryInfo(intent.getIntExtra("status", 0));
                intent.getBooleanExtra(BatteryManager.ACTION_CHARGING, false);
                //初回起動
                if (count == 0) {
                    charging_text = batteryInfo(intent.getIntExtra("status", 0));
                    count++;
                }

                //できる限りバイブレーターがならないようにする
                //充電開始、終了、外されたときのみ動くように
                if (status.contains("充電開始") || status.contains("充電解除") || status.contains("充電終了")) {
                    if (!charging_text.contains(batteryInfo(intent.getIntExtra("status", 0)))) {
                        //前回のバッテリー状態と違うときに動く
                        charging_text = batteryInfo(intent.getIntExtra("status", 0));
                        Toast.makeText(ChargeCheckService.this, batteryInfo(intent.getIntExtra("status", 0)), Toast.LENGTH_SHORT).show();
                        //鳴らす
                        vibrator.vibrate(200);
                    }
                }
            }
        };
        //ブロードキャスト登録
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //最後にレシーバー閉じておく
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //充電、解除、充電完了、は？
    //を分けるやつ
    private String batteryInfo(int getIntExtra) {
        String type = "不明";
        switch (getIntExtra) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                type = "充電開始";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                type = "充電解除";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                type = "充電終了";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                type = "充電してません";
                break;
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                type = "不明";
                break;
        }
        return type;
    }

}
