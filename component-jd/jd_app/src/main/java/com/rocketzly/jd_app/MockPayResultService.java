package com.rocketzly.jd_app;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.rocketzly.resident_api.PayResultService;
import com.rocketzly.resident_api.ResidentRouter;

@Route(path = ResidentRouter.Path.SERVICE_PAY_RESULT)
public class MockPayResultService implements PayResultService {
    @Override
    public int getPayResult() {
        return 100;
    }

    @Override
    public void init(Context context) {

    }
}
