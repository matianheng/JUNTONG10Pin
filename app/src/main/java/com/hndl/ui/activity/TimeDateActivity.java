package com.hndl.ui.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.DateUtils;
import com.hndl.ui.widget.MyDatePickerDialog;
import com.hndl.ui.widget.MyTimePickerDialog;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class TimeDateActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private RelativeLayout rlDate;
    private TextView tvDateName;
    private TextView tvDate;
    private RelativeLayout rlTime;
    private TextView tvTimeName;
    private TextView tvTime;

    private int hour,minute,year,month,day;
    private Timer timerDate = new Timer();
    private TimerTask taskDate;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                setDate();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_date_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        android.provider.Settings.System.putString(AppData.getInstance().getContentResolver(),
                android.provider.Settings.System.TIME_12_24, "24");
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        rlDate = findViewById(R.id.rl_date);
        tvDateName = findViewById(R.id.tv_date_name);
        tvDate = findViewById(R.id.tv_date);
        rlTime = findViewById(R.id.rl_time);
        tvTimeName = findViewById(R.id.tv_time_name);
        tvTime = findViewById(R.id.tv_time);

        llReturn.setOnClickListener(this);
        rlDate.setOnClickListener(this);
        rlTime.setOnClickListener(this);
    }

    private void setDate(){
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        tvDate.setText(year+getString(R.string.year)+(month+1)+getString(R.string.month)+day+getString(R.string.day));

        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        String timeStr=hour+"";
        if (minute<10){
            timeStr+=":0"+minute;
        }else {
            timeStr+=":"+minute;
        }
        if (cal.get(Calendar.AM_PM) == Calendar.AM) {
            tvTime.setText(getString(R.string.morning)+timeStr);
        } else {
            tvTime.setText(getString(R.string.afternoon)+timeStr);
        }
    }

    @Override
    public void initData() {
        taskDate = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 100;
                handler.sendMessage(message);
            }
        };
        timerDate.schedule(taskDate, 0, 10*1000);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.rl_date:{
                MyDatePickerDialog datePicker = new MyDatePickerDialog(this, DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    }
                }, year, month, day);
                datePicker.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DateUtils.setSysDate(TimeDateActivity.this, datePicker.getDatePicker().getYear(), datePicker.getDatePicker().getMonth(), datePicker.getDatePicker().getDayOfMonth());
                        setDate();
                    }
                });
                datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        datePicker.dismiss();
                    }
                });
                datePicker.getDatePicker().setCalendarViewShown(false); //隐藏日历
                datePicker.setCancelable(false);
                datePicker.setCanceledOnTouchOutside(false);
                datePicker.show();
                break;
            }
            case R.id.rl_time:{
                MyTimePickerDialog timePicker = new MyTimePickerDialog(this, TimePickerDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        DateUtils.setSysTime(TimeDateActivity.this, hourOfDay, minute);
                        setDate();
                    }
                }, hour, minute, true);
                timePicker.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                timePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        timePicker.dismiss();
                    }
                });
                timePicker.setCancelable(false);
                timePicker.setCanceledOnTouchOutside(false);
                timePicker.show();
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        taskDate.cancel();
        timerDate.cancel();
        timerDate=null;
        taskDate=null;
    }
}
