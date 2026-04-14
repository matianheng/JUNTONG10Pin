package com.hndl.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hndl.ui.R;

public class NumKeyboard extends FrameLayout implements View.OnClickListener {
    public NumKeyboard(@NonNull Context context) {
        super(context);
        initView();
    }

    public NumKeyboard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public NumKeyboard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @SuppressLint("MissingInflatedId")
    private void initView() {
        View numView = LayoutInflater.from(this.getContext()).inflate(R.layout.num_key_board, null);
        numView.findViewById(R.id.tv_0).setOnClickListener(this);
        numView.findViewById(R.id.tv_1).setOnClickListener(this);
        numView.findViewById(R.id.tv_2).setOnClickListener(this);
        numView.findViewById(R.id.tv_3).setOnClickListener(this);
        numView.findViewById(R.id.tv_4).setOnClickListener(this);
        numView.findViewById(R.id.tv_5).setOnClickListener(this);
        numView.findViewById(R.id.tv_6).setOnClickListener(this);
        numView.findViewById(R.id.tv_7).setOnClickListener(this);
        numView.findViewById(R.id.tv_8).setOnClickListener(this);
        numView.findViewById(R.id.tv_9).setOnClickListener(this);
        numView.findViewById(R.id.tv_del).setOnClickListener(this);
        numView.findViewById(R.id.tv_done).setOnClickListener(this);
        LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        this.addView(numView, params);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (onNumKeyBoardLister == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.tv_0:
                onNumKeyBoardLister.onNumLister(0);
                break;
            case R.id.tv_1:
                onNumKeyBoardLister.onNumLister(1);
                break;
            case R.id.tv_2:
                onNumKeyBoardLister.onNumLister(2);
                break;
            case R.id.tv_3:
                onNumKeyBoardLister.onNumLister(3);
                break;
            case R.id.tv_4:
                onNumKeyBoardLister.onNumLister(4);
                break;
            case R.id.tv_5:
                onNumKeyBoardLister.onNumLister(5);
                break;
            case R.id.tv_6:
                onNumKeyBoardLister.onNumLister(6);
                break;
            case R.id.tv_7:
                onNumKeyBoardLister.onNumLister(7);
                break;
            case R.id.tv_8:
                onNumKeyBoardLister.onNumLister(8);
                break;
            case R.id.tv_9:
                onNumKeyBoardLister.onNumLister(9);
                break;
            case R.id.tv_del:
                onNumKeyBoardLister.onDelLister();
                break;
            case R.id.tv_done:
                onNumKeyBoardLister.onDownLister();
                break;
            default:
                break;
        }
    }

    public void setOnNumKeyBoardLister(NumKeyBoardLister onNumKeyBoardLister) {
        this.onNumKeyBoardLister = onNumKeyBoardLister;
    }

    public NumKeyBoardLister onNumKeyBoardLister;

    //事件监听-接口
    public interface NumKeyBoardLister {
        //点击数字按钮时的监听回调
        void onNumLister(int num);
        //点击删除按钮时的监听回调
        void onDelLister();
        //点击完成按钮时的监听回调
        void onDownLister();
    }
}
