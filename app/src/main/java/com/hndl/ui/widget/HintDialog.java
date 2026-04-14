package com.hndl.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;

import com.hndl.ui.R;

public class HintDialog extends ParentBaseDialog implements
		View.OnClickListener {
	private Context mContext;

	TextView content;
	TextView title;
	public TextView cancel;
	TextView confirm;
	View view;

	public HintDialogListener listeners;
	boolean isFromHtml = true;

	//
	public interface HintDialogListener {
		public void onClick(boolean isConfirm);
	}

	public void setButtonVisibility(int b) {//1代表确定按钮隐藏 2代表取消按钮隐藏3代表全部显示
		if(b==1){
			confirm.setVisibility(View.GONE);
			cancel.setVisibility(View.VISIBLE);
			view.setVisibility(View.GONE);
		}else if (b==2) {
			cancel.setVisibility(View.GONE);
			confirm.setVisibility(View.VISIBLE);
			view.setVisibility(View.GONE);
		}else{
			cancel.setVisibility(View.VISIBLE);
			confirm.setVisibility(View.VISIBLE);
		}

	}

//	传1 显示确定按钮 2显示取消按钮 3取消和确定都显示
	public void setButtonIsShow(int type) {
		if (type == 1) {
			cancel.setVisibility(View.GONE);
			confirm.setVisibility(View.VISIBLE);
			view.setVisibility(View.GONE);
		} else if (type == 2) {
			confirm.setVisibility(View.GONE);
			cancel.setVisibility(View.VISIBLE);
			view.setVisibility(View.GONE);
		} else {
			cancel.setVisibility(View.VISIBLE);
			confirm.setVisibility(View.VISIBLE);
			view.setVisibility(View.VISIBLE);
		}
	}

	public void setIsFromHtml(boolean isFromHtml) {
		this.isFromHtml = isFromHtml;
	}

	public void setTitle(String hintTitle) {
		if (isFromHtml) {
			title.setText(Html.fromHtml(hintTitle));
		} else {
			title.setText(hintTitle);
		}
	}

	public void setContent(String hintContent) {
		if (isFromHtml) {
			content.setText(Html.fromHtml(hintContent));
		} else {
			content.setText(hintContent);
		}
	}

	public void setCancelText(String text) {
		cancel.setText(text);
	}

	public void setConfirmText(String text) {
		confirm.setText(text);
	}

	View rootView;
	public HintDialog(Context context, String hintTitle, String hintContent,
                      HintDialogListener hintDialogListener) {
		super(context);
		mContext = context;
		this.listeners = hintDialogListener;
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setGravity(Gravity.CENTER_VERTICAL);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getWindow().setWindowAnimations(R.style.AnimBottom);
		rootView = getLayoutInflater().inflate(
				R.layout.hint_dialog_layout, null);

		int screenWidth = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay().getWidth();
		LayoutParams params = new LayoutParams((int) (screenWidth * 0.5),
				LayoutParams.MATCH_PARENT);
		content = (TextView) rootView.findViewById(R.id.content);
		content.setText(Html.fromHtml(hintContent));

		title = (TextView) rootView.findViewById(R.id.title);
		title.setText(Html.fromHtml(hintTitle));

		cancel = (TextView) rootView.findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

		confirm = (TextView) rootView.findViewById(R.id.confirm);
		confirm.setOnClickListener(this);

		view = rootView.findViewById(R.id.view);

		super.setContentView(rootView, params);
		rootView.setOnTouchListener(new OnTouchListener() {

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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.cancel) {
			if (listeners != null) {
				listeners.onClick(false);
			}
			dismiss();
		} else if (v.getId() == R.id.confirm) {
			if (listeners != null) {
				listeners.onClick(true);
			}
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
