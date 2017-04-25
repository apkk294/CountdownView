package com.lk.countdownview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lk.library.CountdownView;

/**
 * 创建者： lk
 * 时间：2017/4/25
 * Description：.
 */

public class MainActivity extends AppCompatActivity {

    private CountdownView mCountdownView;
    private Button        mBtnStartCountdown;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mCountdownView.setDuration(5000);
        mCountdownView.setProgressColor(0xFF3F51B5);
        mCountdownView.setProgressLightColor(0xFFFF4081);
        mCountdownView.setText("跳过");
        mCountdownView.setTextSize(12);
        mCountdownView.setTextColor(0xFFFF4081);
        mCountdownView.setCountdownListener(new CountdownView.CountdownListener() {
            @Override
            public void onProgressListener(int progress, boolean isFinish) {
                if (isFinish) {
                    Toast.makeText(MainActivity.this, "倒计时完成", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mCountdownView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "跳过", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnStartCountdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountdownView.start();
            }
        });
    }

    private void initViews() {
        mCountdownView = (CountdownView) findViewById(R.id.countdown);
        mBtnStartCountdown = (Button) findViewById(R.id.btn_start_countdown);
    }

}
