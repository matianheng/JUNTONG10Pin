package com.hndl.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hndl.core.update.AbsCheckListener;
import com.hndl.core.update.Package;
import com.hndl.core.update.UpdateManager;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.adapter.CommonAdapter;
import com.hndl.ui.adapter.ViewHolder;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.IpGetUtil;
import com.hndl.ui.utils.MyUtils;
import com.hndl.ui.widget.EditDialog;
import com.hndl.ui.widget.HintDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

public class FunctionMenuActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private GridView gridView;

    private int[] nameStr = {R.string.work_settings, R.string.motor_status_display, R.string.virtual_wall,
            R.string.brightness_adjustment, R.string.wifi_name, R.string.online_upgrade};
    private int[] iconId = {R.drawable.work_settings_icon, R.drawable.motor_display_icon, R.drawable.virtual_wall_icon,
            R.drawable.brightness_adjust_icon, R.drawable.wifi_icon, R.drawable.update_icon};
    private Class[] classFrom = {null, MotorStatusDisplayActivity.class, VirtualWallActivity.class,
            BrightnessAdjustmentActivity.class, WifiActivity.class, null};

    private List<Map<String, Object>> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.function_menu_layout);
        initView();
        initData();

        UpdateManager.setCustomCheckListener(new AbsCheckListener() {
            @Nullable
            @Override
            public Object onStart(boolean isAutoCheckPeriodic, @NonNull Continuation<? super Unit> $completion) {
//                return super.onStart(isAutoCheckPeriodic, $completion);
                return null;
            }

            @Nullable
            @Override
            public Object onUpdateAvailable(boolean isAutoCheckPeriodic, @NonNull Package pkg, @NonNull Continuation<? super Unit> $completion) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HintDialog hintDialog = new HintDialog(FunctionMenuActivity.this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
                            @Override
                            public void onClick(boolean isConfirm) {
                                if (isConfirm) {
                                    UpdateManager.download(pkg.getDownloadUrl());
                                }
                            }
                        });
                        hintDialog.setCancelable(false);
                        hintDialog.setConfirmText(getString(R.string.update));
                        hintDialog.setContent(getString(R.string.discovering_new_versions)+"V"+pkg.getVersionName()+"!");
                        hintDialog.setButtonIsShow(3);
                        hintDialog.show();
                    }
                });
                return null;
            }

            @Nullable
            @Override
            public Object onUpdateNotAvailable(boolean isAutoCheckPeriodic, @NonNull Continuation<? super Unit> $completion) {
                ToastUtils.showShort(R.string.it_is_already_the_latest_version);
                return null;
            }

            @Nullable
            @Override
            public Object onUpdateCheckFailed(boolean isAutoCheckPeriodic, @NonNull Exception exception, @NonNull Continuation<? super Unit> $completion) {
                ToastUtils.showShort(R.string.it_is_already_the_latest_version);
                return null;
            }
        });
    }

    @Override
    public void initView() {
        llReturn = findViewById(R.id.ll_return);
        gridView = findViewById(R.id.gridView);

        llReturn.setOnClickListener(this);
    }

    @Override
    public void initData() {
        for (int i = 0; i < iconId.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("icon", iconId[i]);
            map.put("name", nameStr[i]);
            map.put("funClass", classFrom[i]);
            list.add(map);
        }
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    AppData.isShowAdmin = false;
                    EditDialog editDialog = new EditDialog(FunctionMenuActivity.this, getString(R.string.password), new EditDialog.HintDialogListener() {
                        @Override
                        public void onClick(boolean isConfirm, String string) {
                            if (isConfirm) {
                                if (string.equals("101010")) {
                                    ActivityUtils.startActivity(FunctionSelectionActivity.class);
                                    overridePendingTransition(0, 0);
                                } else if (string.equals("210890")) {
                                    ActivityUtils.startActivity(ModelSelectionMenuActivity.class);
                                    overridePendingTransition(0, 0);
                                } else if (string.equals("190613")) {
                                    ActivityUtils.startActivity(OverloadRecordActivity.class);
                                    overridePendingTransition(0, 0);
                                } else if (string.equals("130619")) {
                                    ActivityUtils.startActivity(CoefficienSettingsActivity.class);
                                    overridePendingTransition(0, 0);
                                } else if (string.equals("240301")) {
                                    ActivityUtils.startActivity(ToolActivity.class);
                                    overridePendingTransition(0, 0);
                                } else {
                                    ToastUtils.showShort(getString(R.string.password_error));
                                }
                            }
                        }
                    });
                    editDialog.show();
                } else if (position == list.size()-1) {
                    String ipStr = IpGetUtil.getIpAddress(FunctionMenuActivity.this);
                    if (ipStr.equals("")) {
                        ToastUtils.showShort(getString(R.string.please_check_the_network));
                        return;
                    }
                    UpdateManager.check();
                } else {
                    Class class1 = null;
                    class1 = (Class) list.get(position).get("funClass");
                    if (class1 != null) {
                        ActivityUtils.startActivity(class1);
                        overridePendingTransition(0, 0);
                    } else {
                        ToastUtils.showShort(getString(R.string.function_to_be_developed));
                    }
                }
            }
        });
    }

    CommonAdapter adapter = new CommonAdapter<Map<String, Object>>(
            this, R.layout.function_menu_item, list) {
        @Override
        protected void convert(ViewHolder viewHolder,
                               final Map<String, Object> item, final int position) {
            ImageView ivIcon = viewHolder.getView(R.id.iv_icon);
            TextView tvName = viewHolder.getView(R.id.tv_name);

            MyUtils.setTextSizeByLanguage(tvName);
            ivIcon.setImageResource((Integer) item.get("icon"));
            tvName.setText(getString((Integer) (item.get("name"))) + "");
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_return: {
                finish();
                overridePendingTransition(0, 0);
                break;
            }
        }
    }
}
