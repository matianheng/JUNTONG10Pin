package com.hndl.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hndl.ui.R;

/**
 * 数字键盘
 *
 * @author zhongdaxia 2014-02-26 10:04:56
 * @example 方法1 ：
 * 控件写在activity
 * <com.mcx.terminal.view.Keyboard
 * android:id="@+id/ly_keyboard"
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content" />
 * <p>
 * 方法2：
 * 当键盘嵌套到非activity  如 dialog时，((Activity)mContext).getCurrentFocus();出错
 * 无法自动获取非继承activity的焦点所以采用最蠢的方法，分情况直接给文本框对象,等待后续更新改进
 * <p>
 * XML:
 * <com.mcx.terminal.view.Keyboard
 * android:id="@+id/ly_keyboard"
 * android:layout_width="wrap_content"
 * android:layout_height="wrap_content" />
 * <p>
 * 初始化文本框设置，et_username设置为默认对象
 * et_username.setFocusable(true);
 * et_username.requestFocus();
 * ly_keyboard.setEditText(et_username);
 * <p>
 * 点击不同文本框，对象替换
 * et_psw.setOnTouchListener(new OnTouchListener() {
 * @Override public boolean onTouch(View v, MotionEvent event) {
 * ly_keyboard.setEditText(et_psw);
 * return false;
 * }
 * });
 */

public class Keyboard extends LinearLayout {

    Button btn_one;
    Button btn_two;
    Button btn_three;
    Button btn_four;
    Button btn_five;
    Button btn_six;
    Button btn_seven;
    Button btn_eight;
    Button btn_nine;
    Button btn_clear;
    Button btn_zero;
    Button btn_del;

    Context mContext;
    EditText mEt;
    boolean mIsAutoEditText;

    public Keyboard(Context context) {
        super(context);
    }

    public Keyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_keyboard, this);

        this.mContext = context;
        mIsAutoEditText = true;

        findView();
        setClick();
        init();
    }

    /**
     * @param et
     */
    public void setEditText(EditText et) {
        mIsAutoEditText = false;
        if (null != et) {
            this.mEt = et;
        }
    }

    private void findView() {
        btn_one = (Button) findViewById(R.id.btn_one);
        btn_two = (Button) findViewById(R.id.btn_two);
        btn_three = (Button) findViewById(R.id.btn_three);
        btn_four = (Button) findViewById(R.id.btn_four);
        btn_five = (Button) findViewById(R.id.btn_five);
        btn_six = (Button) findViewById(R.id.btn_six);
        btn_seven = (Button) findViewById(R.id.btn_seven);
        btn_eight = (Button) findViewById(R.id.btn_eight);
        btn_nine = (Button) findViewById(R.id.btn_nine);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_zero = (Button) findViewById(R.id.btn_zero);
        btn_del = (Button) findViewById(R.id.btn_del);
    }

    private void setClick() {
        // ��ť1
        btn_one.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "1");
                insertChar("1");

            }
        });
        // ��ť2
        btn_two.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "2");
                insertChar("2");

            }
        });
        // ��ť3
        btn_three.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "3");
                insertChar("3");

            }
        });
        // ��ť4
        btn_four.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "4");
                insertChar("4");

            }
        });
        // ��ť5
        btn_five.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "5");
                insertChar("5");

            }
        });
        // ��ť6
        btn_six.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "6");
                insertChar("6");

            }
        });
        // ��ť7
        btn_seven.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "7");
                insertChar("7");

            }
        });
        // ��ť8
        btn_eight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "8");
                insertChar("8");

            }
        });
        // ��ť9
        btn_nine.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "9");
                insertChar("9");

            }
        });
        // ��ťclear
        btn_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "clear");
                clearChar();

            }
        });
        // ��ť0
        btn_zero.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "0");
                insertChar("0");

            }
        });
        // ��ťdel
        btn_del.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.v("Keyboard", "del");
                deleteChar();
            }
        });
    }

    private void init() {

    }

    /**
     * �����ַ�
     *
     * @param value
     */
    private void insertChar(String value) {
        setAutoEditText(mIsAutoEditText);
        if (null != mEt) {
            int index = mEt.getSelectionStart();
            Editable editable = mEt.getText();
            editable.insert(index, value);
        }
    }

    /**
     * ɾ���ַ�
     */
    private void deleteChar() {
        if (null != mEt) {
            int index = mEt.getSelectionStart();
            if (index > 0) {
                Editable editable = mEt.getText();
                editable.delete(index - 1, index);
            }
        }
    }

    /**
     * ����ַ�
     */
    private void clearChar() {
        if (null != mEt) {
            mEt.setText("");
            moveCursor(-1);
        }
    }

    /**
     * ������������
     */
    private void setAutoEditText(boolean isAutoEditText) {
        // ������Ƕ����activity�ϣ��������ã��������������
        if (isAutoEditText) {
            View view = ((Activity) mContext).getCurrentFocus();
            if (view instanceof EditText) {
                this.mEt = (EditText) view;
            }
        }
    }

    /**
     * �ƶ����
     *
     * @param type [-1][�ƶ���ĩλ]    [0>=][�ƶ���ָ��λ]
     */
    private void moveCursor(int type) {
        if (null != mEt) {
            int position = 0;
            switch (type) {
                // �ƶ���ĩλ
                case -1:
                    position = mEt.length();
                    break;
                // �ƶ���ָ��λ
                default:
                    int length = mEt.length();
                    // type���ʹ��󳤶ȹ���,Ĭ���ַ���󳤶�
                    if (type > length) {
                        position = length;
                    }
                    // type���ʹ��󳤶ȹ�С��Ĭ����ʼλ
                    else if (type < length) {
                        position = 0;
                    }
                    // ����
                    else {
                        position = type;
                    }
                    break;
            }
            Editable etext = mEt.getText();
            Selection.setSelection(etext, position);
        }
    }


}