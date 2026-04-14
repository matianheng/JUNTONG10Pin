package com.hndl.ui.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hndl.ui.R;
import com.quectel.qcarapi.stream.QCarCamera;

public class CameraView extends Fragment {

    private static final String TAG = "PreviewFragment";
    private SurfaceView preview;
    private SurfaceHolder surfaceHolder;
    private int mChannel;
    private QCarCamera qCarCamera;
    private int preWidth;
    private int preHeight;

    public CameraView(){

    }

    @SuppressLint("ValidFragment")
    public CameraView(QCarCamera qCarCamera, int channel) {
        this.qCarCamera = qCarCamera;
        this.mChannel = channel;
    }

    public void setPreviewSize(int width, int height) {
        preWidth = width;
        preHeight = height;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_surface_view, container, false);

        preview = (SurfaceView) rootView.findViewById(R.id.preview);
        surfaceHolder = preview.getHolder();

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //LogUtil.e("XJW","startPreviewmChannel:"+mChannel);
                qCarCamera.startPreview( mChannel,holder.getSurface(), preWidth, preHeight,QCarCamera.YUV420_NV21);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // 关闭预览时，需等待底层关闭成功
//                LogUtil.e("XJW","mChannel:"+mChannel);
                qCarCamera.stopPreview( mChannel);
                try{
                    Thread.sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onPause() {
        super.onPause();
        //LogUtil.e("XJW","mChannel:"+mChannel);
        if (qCarCamera!=null) {
            qCarCamera.stopPreview(mChannel);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (qCarCamera!=null) {
            qCarCamera.stopPreview( mChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
