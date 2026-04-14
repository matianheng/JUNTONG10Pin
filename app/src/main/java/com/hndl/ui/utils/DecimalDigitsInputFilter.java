package com.hndl.ui.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

public class DecimalDigitsInputFilter implements InputFilter {

    // 正则匹配
    private final Pattern mPattern;

    /**
     * 构建一个用于控制EditText最大输入小数点前位数和小数点后位数的输入过滤器
     *
     * @param digitsBeforeZero 小数点之前的位数
     * @param digitsAfterZero  小数点之后的位数
     */
    public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
        String pattern = "^\\-?(\\d{0," + (digitsBeforeZero) + "}|\\d{0," + (digitsBeforeZero) + "}\\.\\d{0," + digitsAfterZero + "})$";
        mPattern = Pattern.compile(pattern);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned destination, int destinationStart, int destinationEnd) {
        if (end > start) {
            // adding: filter
            // build the resulting text
            String destinationString = destination.toString();
            String resultingTxt = destinationString.substring(0, destinationStart)
                    + source.subSequence(start, end)
                    + destinationString.substring(destinationEnd);
            // return null to accept the input or empty to reject it
            return resultingTxt.matches(this.mPattern.toString()) ? null : "";
        }
        // removing: always accept
        return null;
    }

    public static InputFilter[] getFilters(InputFilter inputFilter) {
        return new InputFilter[]{inputFilter};
    }
}
