package com.rocketzly.common;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;

public class BaseApplication extends Application {

    private static Application sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        initARouter(this);
    }

    public void initARouter(Application application) {
        if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
            ARouter.printStackTrace(); // 打印日志的时候打印线程堆栈
        }
        ARouter.init(application); // 尽可能早，推荐在Application中初始化
    }

    public static Application getApplication() {
        return sApplication;
    }


}
