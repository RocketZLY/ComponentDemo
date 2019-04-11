package com.rocketzly.jd_api;

import com.alibaba.android.arouter.launcher.ARouter;

public class JDRouter {
    public interface Path {
        String JD_ACTIVITY = "/jd/activity";
    }

    public interface Params {
    }

    public static void toJDActivity() {
        ARouter.getInstance().build(Path.JD_ACTIVITY).navigation();
    }
}
