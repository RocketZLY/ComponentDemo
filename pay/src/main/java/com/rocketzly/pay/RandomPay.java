package com.rocketzly.pay;

import java.util.Random;

class RandomPay implements IPay {
    @Override
    public int pay() {
        return new Random().nextInt(100);
    }
}
