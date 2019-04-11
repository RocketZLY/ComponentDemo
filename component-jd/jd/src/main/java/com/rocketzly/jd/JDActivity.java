package com.rocketzly.jd;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.rocketzly.common.BaseActivity;
import com.rocketzly.jd_api.JDRouter;
import com.rocketzly.resident_api.PayResultService;
import com.rocketzly.resident_api.ResidentRouter;
import com.rocketzly.share.IShare;
import com.rocketzly.share.ShareFactory;

@Route(path = JDRouter.Path.JD_ACTIVITY)
public class JDActivity extends BaseActivity {

    @Autowired(name = ResidentRouter.Path.SERVICE_PAY_RESULT)
    PayResultService service;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jd_activity_main);

        TextView tv = findViewById(R.id.tv);
        IShare iShare = ShareFactory.create().get(ShareFactory.TYPE_CUSTOM);
        tv.setText("模块:JD" +
                "\n从分享组件中获取了一段文字：" + iShare.shareString() +
                "\n从resident组件获取需要支付金额：" + service.getPayResult());
    }
}
