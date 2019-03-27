package io.github.takusan23.chargecheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private BroadcastReceiver receiver;
    private Vibrator vibrator;
    private TextView textView;
    //充電状況を置いておく
    private String charging_text;
    //初回起動
    private int count = 0;
    //設定
    private SharedPreferences pref_setting;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref_setting.edit();

        button = findViewById(R.id.battery_button);
        textView = findViewById(R.id.battery_textview);
        //バイブレーション
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //押したらサービス起動
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //起動
                if (!pref_setting.getBoolean("info", false)) {
                    Toast.makeText(MainActivity.this, "起動", Toast.LENGTH_SHORT).show();
                    //起動
                    editor.putBoolean("info", true);
                    editor.apply();
                    Intent intent = new Intent(getApplication(), ChargeCheckService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                } else {
                    //終了
                    Toast.makeText(MainActivity.this, "終了", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("info", false);
                    editor.apply();
                    Intent intent = new Intent(getApplication(), ChargeCheckService.class);
                    stopService(intent);
                }
            }
        });

/*
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
                        Toast.makeText(MainActivity.this, batteryInfo(intent.getIntExtra("status", 0)), Toast.LENGTH_SHORT).show();
                        textView.append("\n" + batteryInfo(intent.getIntExtra("status", 0)));
                        //鳴らす
                        vibrator.vibrate(200);
                    }
                }
            }
        };
        //ブロードキャスト登録
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
*/

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //最後にレシーバー閉じておく
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
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
