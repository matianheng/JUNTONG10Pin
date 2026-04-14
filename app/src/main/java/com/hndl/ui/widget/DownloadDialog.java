package com.hndl.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hndl.ui.R;

public class DownloadDialog extends ParentBaseDialog implements
		View.OnClickListener {
	private Context mContext;


	public DownloaDialogListener listeners;
	public String downloadUrl = "";
	public ProgressBar progress_bar;
	public LinearLayout ll_pro;
	public TextView tv_qx,tv_bfz,tv_title;

	//
	public interface DownloaDialogListener {
		public void onClick(boolean isConfirm);
	}
	View rootView;
	public DownloadDialog(Context context, String hintTitle, String hintContent, DownloaDialogListener downloaDialogListener) {
		super(context);
		mContext = context;
		this.listeners = downloaDialogListener;
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setGravity(Gravity.CENTER_VERTICAL);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getWindow().setWindowAnimations(R.style.AnimBottom);
		rootView = getLayoutInflater().inflate(
				R.layout.progressbar_dialog, null);

		int screenWidth = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay().getWidth();
		LayoutParams params = new LayoutParams((int) (screenWidth*0.5),
				LayoutParams.MATCH_PARENT);

		tv_qx = (TextView) rootView.findViewById(R.id.tv_qx);
		//tv_qx.setText(Html.fromHtml(hintContent));
		tv_bfz = (TextView) rootView.findViewById(R.id.tv_bfz);
		tv_qx.setOnClickListener(this);
		progress_bar = (ProgressBar) rootView.findViewById(R.id.progress_bar);

		tv_title = (TextView) rootView.findViewById(R.id.tv_title);
		tv_title.setText(hintTitle);
		if (hintTitle.equals("")) {
			tv_title.setVisibility(View.GONE);
		}

		/*cancel = (android.widget.TextView) rootView.findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

		confirm = (android.widget.TextView) rootView.findViewById(R.id.confirm);
		confirm.setOnClickListener(this);*/



		super.setContentView(rootView, params);
//		rootView.setOnTouchListener(new OnTouchListener() {
//
//			public boolean onTouch(View v, MotionEvent event) {
//
//				int height = rootView.findViewById(R.id.pop_layout).getTop();
//				int y = (int) event.getY();
//				if (event.getAction() == MotionEvent.ACTION_UP) {
//					if (y < height) {
//						dismiss();
//					}
//				}
//				return true;
//			}
//		});
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId()==R.id.tv_qx) {
			if (listeners!=null) {
				listeners.onClick(false);
			}
			dismiss();
		}else if (v.getId()==R.id.confirm) {
			if (listeners!=null) {
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
