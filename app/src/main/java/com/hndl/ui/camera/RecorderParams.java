package com.hndl.ui.camera;

public class RecorderParams {
    private int codecTypePosition = 0;
    private int width = 1280;
    private int height = 720;
    private int csiNum = 2;

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

    public int getCodecTypePosition() {
        return codecTypePosition;
    }
    public void setCodecTypePosition(int codecTypePosition) {
        this.codecTypePosition = codecTypePosition;
    }

    private int ssPosition = 0;  //分段录像位置
    public int getSegmentSizePosition() {
        return ssPosition;
    }
    public void setSegmentSizePosition(int ssPosition) {
        this.ssPosition = ssPosition;
    }

    private int child_size = 0; //default 640x480
    private int vsPosition = 0; //video分辨率位置

    public int getVsPosition() {
        return vsPosition;
    }

    public void setVsPosition(int vsPosition) {
        this.vsPosition = vsPosition;
    }

    public void adjustResolution(int vsPosition) {
        if (csiNum == 1) {
            if (vsPosition == 0) {
                width = 1920;
                height = 1080;
            } else if (vsPosition == 1) {
                width = 1280;
                height = 720;
            } else if (vsPosition == 2) {
                width = 640;
                height = 480;
            }
        } else {
            if (vsPosition == 0) {
                width = 1280;
                height = 720;
            } else if (vsPosition == 1) {
                width = 640;
                height = 480;
            }
        }
    }

    public void adjustMergeResolution(int vsPosition) {
        if (vsPosition == 0) {
            width = 1920;
            height = 1080;
        } else if (vsPosition == 1) {
            width = 1280;
            height = 720;
        } else if (vsPosition == 2) {
            width = 640;
            height = 480;
        }
    }

    public void adjustResolutionWidthReValue(String value) {
        if (value.startsWith("1080")) {
            width = 1920;
            height = 1080;
        } else if (value.startsWith("720")) {
            width = 1280;
            height = 720;
        } else if (value.startsWith("480")) {
            width = 640;
            height = 480;
        }
    }

    public int getChild_size() {
        return child_size;
    }
    public void setChild_size(int child_size) {
        this.child_size = child_size;
    }

    private int recorderNums = 0; //默认录像的个数为0，需要选标准录像或者四合一录像
    public int getRecorderNums() {
        return recorderNums;
    }
    public void setRecorderNums(int recorderNums) {
        this.recorderNums = recorderNums;
    }

    private boolean childRecordEnable = false; //默认不开启录像子码流
    public boolean isChildRecordEnable() {
        return childRecordEnable;
    }

    public void setChildRecordEnable(boolean childRecordEnable) {
        this.childRecordEnable = childRecordEnable;
    }

    private boolean audioRecordEnable = false; //默认不开启录像子码流
    public boolean isAudioRecordEnable() {
        return audioRecordEnable;
    }

    public void setAudioRecordEnable(boolean audioRecordEnable) {
        this.audioRecordEnable = audioRecordEnable;
    }

    private int ctPosition = 0; //录像文件格式位置
    public int getCtPosition() {
        return ctPosition;
    }
    public void setCtPosition(int ctPosition) {
        this.ctPosition = ctPosition;
    }

    private int mainRatePosition = 0; //主码率
    public int getMainRatePosition() {
        return mainRatePosition;
    }
    public void setMainRatePosition(int mrPosition) {
        this.mainRatePosition = mrPosition;
    }

    private int subRatePosition = 0; //主码率
    public int getSubRatePosition() {
        return subRatePosition;
    }
    public void setSubRatePosition(int srPosition) {
        this.subRatePosition = srPosition;
    }

    public void setCsiNum(int csiNum) {
        this.csiNum = csiNum;
    }

    public int getCsiNum() {
        return this.csiNum;
    }
}
