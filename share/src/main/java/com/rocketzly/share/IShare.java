package com.rocketzly.share;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IShare {

    String shareString();

    interface Factory {

        int TYPE_CUSTOM = 0;

        @IntDef({TYPE_CUSTOM})
        @Retention(RetentionPolicy.SOURCE)
        @interface ShareType {
        }

        IShare get(@ShareType int type);

    }
}
