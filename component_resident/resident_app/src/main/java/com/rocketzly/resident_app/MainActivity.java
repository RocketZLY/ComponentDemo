package com.rocketzly.resident_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.launcher.ARouter;
import com.rocketzly.resident_api.PayResultService;
import com.rocketzly.resident_api.ResidentRouter;

public class MainActivity extends AppCompatActivity {

    @Autowired(name = ResidentRouter.Path.SERVICE_PAY_RESULT)
    PayResultService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().inject(this);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.tv);
        tv.setText("需要支付金额：" + String.valueOf(service.getPayResult()));
    }
}
