package com.hndl.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelecteListDialog extends ParentBaseDialog implements
		View.OnClickListener, OnItemClickListener {
	private Context mContext;

	private TextView tv_xz;
	private String hint="";
	Button btn_cancel;
	ListView dialog_listview;

	public SelecteListListener listeners;
	Activity activity;
	private List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
    public CommonAdapter adapter;

	//
	public interface SelecteListListener {
		public void onClick(int index);
	}

	View rootView;
	public SelecteListDialog(Activity activity,
                             List<Map<String, Object>> listData, String hint,
                             SelecteListListener listener) {
		super(activity);
		mContext = activity;
		this.listeners = listener;
		this.activity = activity;
		this.listData= listData;
		this.hint = hint;
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setGravity(Gravity.CENTER_VERTICAL);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		getWindow().setWindowAnimations(R.style.AnimBottom);
		rootView = getLayoutInflater().inflate(
				R.layout.selecte_list_dialog_layout, null);

		int screenWidth = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay().getWidth();
		LayoutParams params = new LayoutParams((int) (screenWidth*0.7),
				LayoutParams.WRAP_CONTENT);
		btn_cancel = (Button) rootView.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(this);
		tv_xz = (TextView) rootView.findViewById(R.id.tv_xz);
		tv_xz.setText(hint);
		dialog_listview = (ListView) rootView
				.findViewById(R.id.dialog_listview);
		adapter = new CommonAdapter<Map<String, Object>>(mContext,
				R.layout.selecte_dialog_item, listData) {
			@Override
			protected void convert(ViewHolder viewHolder, Map<String, Object> item,
								   int position) {
				viewHolder.setText(R.id.key,item.get("KEY")+"");
			}
		};
		dialog_listview.setAdapter(adapter);
		dialog_listview.setOnItemClickListener(this);

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
		switch (v.getId()) {
		case R.id.btn_cancel: {
			dismiss();
			break;
		 }
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (listeners!=null) {
			listeners.onClick(position);
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
