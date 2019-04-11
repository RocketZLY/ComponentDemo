package com.rocketzly.resident;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.rocketzly.pay.PayFactory;
import com.rocketzly.resident_api.PayResultService;
import com.rocketzly.resident_api.ResidentRouter;

@Route(path = ResidentRouter.Path.SERVICE_PAY_RESULT)
public class PayResultServiceImpl implements PayResultService {
    @Override
    public int getPayResult() {
        return PayFactory.create().get(PayFactory.RANDOM_PAY).pay();
    }

    @Override
    public void init(Context context) {

    }
}
