package com.hndl.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hndl.ui.R;
import com.king.zxing.util.CodeUtils;


public class QRCodeDialog extends ParentBaseDialog implements
        View.OnClickListener {
    private Context mContext;

    ImageView ivQr;
    TextView confirm;

    View rootView;

    public QRCodeDialog(Context context) {
        super(context);
        mContext = context;
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setGravity(Gravity.CENTER_VERTICAL);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setWindowAnimations(R.style.AnimBottom);
        rootView = getLayoutInflater().inflate(
                R.layout.qr_code_layout, null);

        int screenWidth = ((Activity) mContext).getWindowManager()
                .getDefaultDisplay().getWidth();
        LayoutParams params = new LayoutParams((int) (screenWidth * 0.5),
                LayoutParams.MATCH_PARENT);

        ivQr = (ImageView) rootView.findViewById(R.id.iv_qr);
        confirm = (TextView) rootView.findViewById(R.id.confirm);
        confirm.setOnClickListener(this);
        super.setContentView(rootView, params);
        rootView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = rootView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    public void setQr(String qrPath){
        //生成二维码
        Bitmap qrCode = CodeUtils.createQRCode(qrPath, 200, null);
        ivQr.setImageBitmap(qrCode);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.confirm) {
            dismiss();
        }
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
