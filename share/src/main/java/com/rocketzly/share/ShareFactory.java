package com.rocketzly.share;

public class ShareFactory implements IShare.Factory {

    public static ShareFactory create() {
        return new ShareFactory();
    }

    @Override
    public IShare get(int type) {
        if (IShare.Factory.TYPE_CUSTOM == type) {
            return new CustomShareImpl();
        }
        return null;
    }
}
