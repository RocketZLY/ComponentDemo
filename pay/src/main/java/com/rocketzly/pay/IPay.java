package com.rocketzly.pay;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IPay {

    int pay();

    interface Factory {
        int RANDOM_PAY = 1;

        @IntDef({RANDOM_PAY})
        @Retention(RetentionPolicy.SOURCE)
        @interface PayType {
        }

        IPay get(@PayType int type);
    }
}
