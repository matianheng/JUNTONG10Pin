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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hndl.ui.R;

import java.util.Hashtable;

public class WorkingConditionDialog extends ParentBaseDialog implements
		View.OnClickListener {
	private Context mContext;

	private LinearLayout popLayout;
	private TextView title;
	private TextView tvWorkArm;
	private TextView tvMagnification;
	private TextView tvLeg;
	private TextView tvFifthLeg;
	private TextView tvWorkArea;
	private TextView tvL1;
	private TextView tvA;
	private TextView confirm;

	View rootView;
	public WorkingConditionDialog(Context context, String hintTitle, Hashtable<String, Object> hashtable) {
		super(context);
		mContext = context;
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setGravity(Gravity.CENTER_VERTICAL);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getWindow().setWindowAnimations(R.style.AnimBottom);
		rootView = getLayoutInflater().inflate(
				R.layout.working_condition_layout, null);

		int screenWidth = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay().getWidth();
		LayoutParams params = new LayoutParams((int) (screenWidth * 0.5),
				LayoutParams.MATCH_PARENT);

		popLayout = rootView.findViewById(R.id.pop_layout);
		title = rootView.findViewById(R.id.title);
		tvWorkArm = rootView.findViewById(R.id.tv_work_arm);
		tvMagnification = rootView.findViewById(R.id.tv_magnification);
		tvLeg = rootView.findViewById(R.id.tv_leg);
		tvFifthLeg = rootView.findViewById(R.id.tv_fifth_leg);
		tvWorkArea = rootView.findViewById(R.id.tv_work_area);
		tvL1 = rootView.findViewById(R.id.tv_l1);
		tvA = rootView.findViewById(R.id.tv_a);

		confirm = (TextView) rootView.findViewById(R.id.confirm);
		confirm.setOnClickListener(this);

		//title.setText();
		tvWorkArm.setText(hashtable.get("work_arm")+"");
		tvMagnification.setText(hashtable.get("magnification")+"");
		tvLeg.setText(hashtable.get("leg")+"");
		tvFifthLeg.setText(hashtable.get("fifth_leg")+"");
		tvWorkArea.setText(hashtable.get("work_area")+"");
		tvL1.setText(hashtable.get("l1")+" m");
		tvA.setText(hashtable.get("a")+"°");
//		tvP1.setText(hashtable.get("p1")+"");
//		tvP2.setText(hashtable.get("p2")+"");
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
