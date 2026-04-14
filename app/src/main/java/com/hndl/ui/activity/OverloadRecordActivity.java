package com.hndl.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.model.OverloadRecordModel;
import com.hndl.ui.utils.DatabaseManager;
import com.hndl.ui.utils.DateUtils;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MapUtil;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.MyListView;
import com.hndl.ui.widget.WorkingConditionDialog;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class OverloadRecordActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private MyListView listView;
    private TextView btnLastPage;
    private TextView tvNum;
    private TextView btnNextPage;
    private TextView tv1,tv2,tv3,tv4,tv5,tv6,tv7,tv8;

    List<Hashtable<String, Object>> listTotal = new ArrayList<>();
    List<Hashtable<String, Object>> listData = new ArrayList<>();

    private int page = 1;
    private int pages = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overload_record_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        ThreadUtils.runOnUiThreadDelayed(() -> {
            initData();
            dismissLoadingDialog();
        }, 500);
        createLoadingDialog("");
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        listView = findViewById(R.id.listView);
        btnLastPage = findViewById(R.id.btn_last_page);
        tvNum = findViewById(R.id.tv_num);
        btnNextPage = findViewById(R.id.btn_next_page);
        llReturn.setOnClickListener(this);
        btnLastPage.setOnClickListener(this);
        btnNextPage.setOnClickListener(this);

        tv1=findViewById(R.id.tv1);
        tv2=findViewById(R.id.tv2);
        tv3=findViewById(R.id.tv3);
        tv4=findViewById(R.id.tv4);
        tv5=findViewById(R.id.tv5);
        tv6=findViewById(R.id.tv6);
        tv7=findViewById(R.id.tv7);
        tv8=findViewById(R.id.tv8);
        MyUtils.setTextSizeByLanguage(tv1);
        MyUtils.setTextSizeByLanguage(tv2);
        MyUtils.setTextSizeByLanguage(tv3);
        MyUtils.setTextSizeByLanguage(tv4);
        MyUtils.setTextSizeByLanguage(tv5);
        MyUtils.setTextSizeByLanguage(tv6);
        MyUtils.setTextSizeByLanguage(tv7);
        MyUtils.setTextSizeByLanguage(tv8);
    }

    private String[] strName = {AppData.getInstance().getString(R.string.time_date), AppData.getInstance().getString(R.string.work_arm),
            AppData.getInstance().getString(R.string.magnification), AppData.getInstance().getString(R.string.leg),
            AppData.getInstance().getString(R.string.fifth_leg), AppData.getInstance().getString(R.string.work_area), "L1", "A", "P1", "P2",
            AppData.getInstance().getString(R.string.amplitude), AppData.getInstance().getString(R.string.frontal_weight),
            AppData.getInstance().getString(R.string.actual_weight), AppData.getInstance().getString(R.string.moment_percentage),
            AppData.getInstance().getString(R.string.duration) + "(min)"};
    private String[] strCode = {"time_date", "work_arm", "magnification", "leg", "fifth_leg", "work_area", "l1", "a", "p1", "p2", "amplitude", "frontal_weight",
            "actual_weight", "moment_percentage", "duration"};

    @Override
    public void initData() {
        List<OverloadRecordModel> queryList = DatabaseManager.getInstance().getQueryAll(OverloadRecordModel.class);
        if (queryList != null && queryList.size() > 0) {
            if (queryList.size()>1500){
                for (int i=0;i<queryList.size()-1500;i++){
                    DatabaseManager.getInstance().delete(queryList.get(i));
                }
            }
            int j=0;
            for (int i = queryList.size() - 1; i >= 0; i--) {
                j=j+1;
                Hashtable<String, Object> hashtable = new Hashtable<>();
                hashtable.put("no", j + "");
                hashtable.put("time_date", queryList.get(i).getStart_date());
                int workArm = queryList.get(i).getWork_arm();  //工作臂
                if (workArm == 1) {
                    hashtable.put("work_arm", getString(R.string.arm_end_pulley));
                } else if (workArm == 2) {
                    hashtable.put("work_arm", getString(R.string.jib));
                } else {
                    hashtable.put("work_arm", getString(R.string.main_arm));
                }
                hashtable.put("magnification", queryList.get(i).getMagnification());
                int legState = queryList.get(i).getLeg();  //支腿状态
                if (legState == 0) {
                    hashtable.put("leg", getString(R.string.full_extension));
                } else if (legState == 1) {
                    hashtable.put("leg", getString(R.string.half_extension));
                } else {
                    hashtable.put("leg", getString(R.string.unextended));
                }
                int fifthLegState = queryList.get(i).getFifth_leg();  //第五支腿状态
                if (fifthLegState == 0) {
                    hashtable.put("fifth_leg", getString(R.string.full_extension));
                } else {
                    hashtable.put("fifth_leg", getString(R.string.unextended));
                }
                int WorkArea = queryList.get(i).getWork_area();
                if (WorkArea == 0) {
                    hashtable.put("work_area", getString(R.string.rear_side));
                } else {
                    hashtable.put("work_area", getString(R.string.the_front));
                }
                hashtable.put("l1", queryList.get(i).getL1());
                hashtable.put("a", queryList.get(i).getA());
//                hashtable.put("p1", queryList.get(i).getP1());
//                hashtable.put("p2", queryList.get(i).getP2());
                hashtable.put("amplitude", queryList.get(i).getAmplitude());
                hashtable.put("frontal_weight", queryList.get(i).getFrontal_weight());
                hashtable.put("actual_weight", queryList.get(i).getActual_weight());
                hashtable.put("moment_percentage", queryList.get(i).getMoment_percentage());
                if ((queryList.get(i).getEnd_date() + "").equals("null") && (queryList.get(i).getEnd_date() + "").length() > 5) {
                    hashtable.put("duration", "-");
                } else {
                    hashtable.put("duration", DateUtils.calculateMinutes(queryList.get(i).getStart_date(), queryList.get(i).getEnd_date()) + "");
                }
                listTotal.add(hashtable);
            }
        }
        listView.setAdapter(adapter);
        //fillData(listData.toArray(),listTitle.toArray());
        getData();
    }

    private void getData() {
        if (listTotal != null && listTotal.size() > 0) {
            listData.clear();
            adapter.notifyDataSetChanged();
            int total = listTotal.size() / pages + 1;
            int nums = page * pages;
            if (listTotal.size() % pages == 0) {
                total = listTotal.size() / pages;
            }
            tvNum.setText(page + "");
            if (page == 1) {
                btnLastPage.setVisibility(View.INVISIBLE);
                btnNextPage.setVisibility(View.VISIBLE);
                if (total == 1) {
                    btnNextPage.setVisibility(View.INVISIBLE);
                    nums = listTotal.size();
                }
            } else if (page == total) {
                btnLastPage.setVisibility(View.VISIBLE);
                btnNextPage.setVisibility(View.INVISIBLE);
                nums = listTotal.size();
            } else {
                btnLastPage.setVisibility(View.VISIBLE);
                btnNextPage.setVisibility(View.VISIBLE);
            }
            for (int i = (page - 1) * pages; i < nums; i++) {
                listData.add(listTotal.get(i));
            }
            adapter.notifyDataSetChanged();
        }
    }

    CommonAdapter adapter = new CommonAdapter<Hashtable<String, Object>>(
            this, R.layout.overload_record_item, listData) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Hashtable<String, Object> item, final int position) {
            TextView tvSerialNumber = viewHolder.getView(R.id.tv_serial_number);
            TextView tvTimeDate = viewHolder.getView(R.id.tv_time_date);
            TextView tvAmplitude = viewHolder.getView(R.id.tv_amplitude);
            TextView tvFrontalWeight = viewHolder.getView(R.id.tv_frontal_weight);
            TextView tvActualWeight = viewHolder.getView(R.id.tv_actual_weight);
            TextView tvMomentPercentage = viewHolder.getView(R.id.tv_moment_percentage);
            TextView tvDuration = viewHolder.getView(R.id.tv_duration);
            TextView tvSee = viewHolder.getView(R.id.tv_see);

            tvSerialNumber.setText(item.get("no") + "");
            tvTimeDate.setText(item.get("time_date") + "");
            tvAmplitude.setText(item.get("amplitude") + "");
            tvFrontalWeight.setText(item.get("frontal_weight") + "");
            tvActualWeight.setText(item.get("actual_weight") + "");
            tvMomentPercentage.setText(item.get("moment_percentage") + "");
            tvDuration.setText(item.get("duration") + "");
            tvSee.setTag(position);
            tvSee.setOnClickListener(new seeClick());
        }
    };

    class seeClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int tag = (int) v.getTag();
            //LogUtil.e("XJW", "tag:" + tag);
            WorkingConditionDialog workingConditionDialog = new WorkingConditionDialog(OverloadRecordActivity.this, "", listData.get(tag));
            workingConditionDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.btn_last_page: {
                page = page - 1;
                getData();
                break;
            }
            case R.id.btn_next_page: {
                page = page + 1;
                getData();
                break;
            }
        }
    }
}
