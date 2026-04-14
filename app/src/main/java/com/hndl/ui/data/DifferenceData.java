package com.hndl.ui.data;

import com.hndl.ui.AppData;

public class DifferenceData {

    private static String[] strModel={"JPC100E5-Ⅰ","JPC100E5-Ⅱ","JPC100E5-Ⅲ","JPC100H5","JPC100H5Ⅱ","JPC120H5","JPC120H5Ⅱ","JPC160H5","JPC160H5Ⅱ",
            "JPC120H5-1","JPC120H5Ⅱ-1","JPC160H5-1","JPC160H5Ⅱ-1","JTC16H6","JTC12H5","JTC12H6","JPC100H5B-I","JTC16E5-I","JTC16E5II-I","JTC12H5A","JTC12H6A","JTC16H6A"};
    public static String getModelName(int type){
        switch (type){
            case 2:
                return "JPC100E5-Ⅰ";
            case 3:
                return "JPC100E5-Ⅱ";
            case 4:
                return "JPC100E5-Ⅲ";
            case 5:
                return "JPC100H5";
            case 6:
                return "JPC100H5Ⅱ";
            case 7:
                return "JPC120H5";
            case 8:
                return "JPC120H5Ⅱ";
            case 9:
                return "JPC160H5";
            case 10:
                return "JPC160H5Ⅱ";
            case 11:
                return "JPC120H5-1";
            case 12:
                return "JPC120H5Ⅱ-1";
            case 13:
                return "JPC160H5-1";
            case 14:
                return "JPC160H5Ⅱ-1";
            case 15:
                return "JTC16H6";
            case 17:
                return "JTC12H5";
            case 18:
                return "JTC12H6";
            case 20:
                return "JPC100H5B-I";
            case 21:
                return "JTC16E5-I";
            case 22:
                return "JTC16E5II-I";
            case 26:
                return "JTC12H5A";
            case 27:
                return "JTC12H6A";
            case 29:
                return "JTC16H6A";
            case 32:
                return "JTC12H6B-I";
            case 36:
                return "JTC12H5B-I";
            default:
                return "JPC100E5-Ⅰ";
        }
    }

    public static int getMagnification(){
        int magnification=6;
        if (AppData.modelType==9||AppData.modelType==10||AppData.modelType==13||AppData.modelType==14||AppData.modelType==22){
            magnification=7;
        }else if (AppData.modelType==15||AppData.modelType==29){
            magnification=8;
        }
        return magnification;
    }
}
