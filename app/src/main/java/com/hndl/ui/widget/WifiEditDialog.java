package com.hndl.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.hndl.ui.R;


public class WifiEditDialog extends ParentBaseDialog implements
        View.OnClickListener {
    private Context mContext;

    TextView title;
    TextView cancel;
    TextView confirm;
    EditText content;
    View xian;

    public WifiEditListener listeners;

    private String hintTitle = "";
    private String hintContent = "";

    public String getHintTitle() {
        return hintTitle;
    }

    public void setHintTitle(String hintTitle) {
        this.hintTitle = hintTitle;
        title.setText(Html.fromHtml(hintTitle));
    }

    public void setHintContent(String hintContent) {
        content.setHint(hintContent);
    }

    public void setEditType(int type){
        content.setInputType(type);
    }

    public void setCancelText(String text) {
        cancel.setText(text);
    }

    public void setConfirmText(String text) {
        confirm.setText(text);
    }

    public void setButtonVisibility(int b) {//1代表确定按钮隐藏 2代表取消按钮隐藏3代表全部显示
        if (b == 1) {
            confirm.setVisibility(View.GONE);
            xian.setVisibility(View.GONE);
        } else if (b == 2) {
            cancel.setVisibility(View.GONE);
            xian.setVisibility(View.GONE);
        } else {
            cancel.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
            xian.setVisibility(View.VISIBLE);
        }

    }

    public interface WifiEditListener {
        public void onClick(boolean isConfirm, String string);
    }

    View rootView;

    public WifiEditDialog(Context context, String hintTitle, WifiEditListener WifiEditListener) {
        super(context);
        mContext = context;
        this.listeners = WifiEditListener;
        this.hintTitle = hintTitle;
        this.hintContent = hintContent;
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setGravity(Gravity.CENTER_VERTICAL);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setWindowAnimations(R.style.AnimBottom);
        rootView = getLayoutInflater().inflate(
                R.layout.wifi_edit_dialog_layout, null);

        int screenWidth = ((Activity) mContext).getWindowManager()
                .getDefaultDisplay().getWidth();
        LayoutParams params = new LayoutParams((int) (screenWidth * 0.7),
                LayoutParams.MATCH_PARENT);

        content = (EditText) rootView.findViewById(R.id.content);

        title = (TextView) rootView.findViewById(R.id.title);
        title.setText(Html.fromHtml(hintTitle));

        cancel = (TextView) rootView.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);

        confirm = (TextView) rootView.findViewById(R.id.confirm);
        confirm.setOnClickListener(this);

        xian = rootView.findViewById(R.id.xian);

        super.setContentView(rootView, params);
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.cancel) {
            if (listeners != null) {
                listeners.onClick(false, "");
            }
        } else if (v.getId() == R.id.confirm) {
            if (listeners != null) {
                if (content.getText().toString().length()<8){
                    ToastUtils.showShort(mContext.getString(R.string.please_enter_minimum_digits_password));
                    return;
                }
                listeners.onClick(true, content.getText().toString());
            }
        }
        dismiss();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
