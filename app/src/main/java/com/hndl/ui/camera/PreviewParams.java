package com.hndl.ui.camera;

public class PreviewParams {
    private int previewNum = 4;
    private int csiNum = 2; //default RM6864+N4
    private int width = 1280;
    private int height = 720;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPsPosition() {
        return psPosition;
    }

    public void setPsPosition(int psPosition) {
        this.psPosition = psPosition;
        if (csiNum == 1) {
            if (psPosition == 0) {
                width = 1920;
                height = 1080;
            } else if (psPosition == 1) {
                width = 1280;
                height = 720;
            } else if (psPosition == 2) {
                width = 640;
                height = 480;
            }
        } else {
            if (psPosition == 0) {
                width = 1280;
                height = 720;
            } else if (psPosition == 1) {
                width = 640;
                height = 480;
            }
        }
    }

    private int psPosition = 0;

    public int getPreviewNum() {
        int previewMaxNum = 8;
        return Math.min(previewNum, previewMaxNum);
    }

    public void setPreviewNum(int previewNum) {
        this.previewNum = previewNum;
    }

    public int getCsiNum() {
        return csiNum;
    }

    public void setCsiNum(int csiNum) {
        this.csiNum = csiNum;
        //restore default value
        previewNum = 4;
        psPosition = 0;
        width = 1280;
        height = 720;
    }
}
