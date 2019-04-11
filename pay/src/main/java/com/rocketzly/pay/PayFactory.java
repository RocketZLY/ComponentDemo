package com.rocketzly.pay;

public class PayFactory implements IPay.Factory {

    public static PayFactory create() {
        return new PayFactory();
    }

    @Override
    public IPay get(int type) {
        if (type == IPay.Factory.RANDOM_PAY) {
            return new RandomPay();
        }
        return null;
    }
}
