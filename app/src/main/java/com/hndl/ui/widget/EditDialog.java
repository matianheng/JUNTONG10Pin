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

import com.hndl.ui.R;


public class EditDialog extends ParentBaseDialog implements
        View.OnClickListener {
    private Context mContext;

    TextView title;
    private EditText content;
    private NumKeyboard numKeyboard;

    public HintDialogListener listeners;

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

    public interface HintDialogListener {
        public void onClick(boolean isConfirm, String string);
    }

    View rootView;

    public EditDialog(Context context, String hintTitle, HintDialogListener HintDialogListener) {
        super(context);
        mContext = context;
        this.listeners = HintDialogListener;
        this.hintTitle = hintTitle;
        this.hintContent = hintContent;
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setGravity(Gravity.CENTER_VERTICAL);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setWindowAnimations(R.style.AnimBottom);
        rootView = getLayoutInflater().inflate(
                R.layout.edit_dialog_layout, null);

        int screenWidth = ((Activity) mContext).getWindowManager()
                .getDefaultDisplay().getWidth();
        LayoutParams params = new LayoutParams((int) (screenWidth * 0.5),
                LayoutParams.MATCH_PARENT);

        content = (EditText) rootView.findViewById(R.id.content);

        title = (TextView) rootView.findViewById(R.id.title);
        title.setText(Html.fromHtml(hintTitle));
        numKeyboard=rootView.findViewById(R.id.numKeyboard);
        numKeyboard.setOnNumKeyBoardLister(new NumKeyboard.NumKeyBoardLister() {
            @Override
            public void onNumLister(int num) {
                //将输入的数字填写输入框中
                content.setText(content.getText().append(num+""));
                //将光标设置到尾部
                content.setSelection(content.getText().toString().length());
            }

            @Override
            public void onDelLister() {
                if (content.getText().toString().length()>0) {
                    int length = content.getText().toString().length();
                    //从后往前逐个删除
                    content.setText(content.getText().delete(length - 1, length));
                    //将光标设置到尾部
                    content.setSelection(content.getText().toString().length());
                }
            }

            @Override
            public void onDownLister() {
                if (content.getText().toString().length()>0) {
                    if (listeners != null) {
                        listeners.onClick(true, content.getText().toString());
                    }
                }
                dismiss();
            }
        });
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
