package com.xlzhen.aikidsstory.config;

public class SettingsConfig {
    private float speed;
    private boolean man;

    public SettingsConfig(float speed, boolean man) {
        this.speed = speed;
        this.man = man;
    }

    public SettingsConfig() {
        this.speed = 1f;
        this.man = true;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isMan() {
        return man;
    }

    public void setMan(boolean man) {
        this.man = man;
    }
}
