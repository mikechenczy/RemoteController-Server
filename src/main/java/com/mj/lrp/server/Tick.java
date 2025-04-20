package com.mj.lrp.server;

public class Tick extends Thread {
    private final long tickRate;
    private final OnTick onTick;
    private boolean activated;

    public static abstract class OnTick{
        public abstract void onTick();
    }

    public Tick(long tickRate, OnTick onTick) {
        this.tickRate = tickRate;
        if(onTick==null)
            onTick = new OnTick() {
                @Override
                public void onTick() {

                }
            };
        this.onTick = onTick;
    }

    public boolean get() {
        if (!activated)
            return false;
        activated = false;
        return true;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(tickRate);
            } catch (InterruptedException interruptedException) {
                break;
            }
            activated = true;
            onTick.onTick();
        }
    }
}