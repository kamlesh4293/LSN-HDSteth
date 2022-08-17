package com.app.lsnhdsteth.hdsteth;

import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.XYSeries;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ECGModel implements XYSeries {

    public ArrayList<Number> data;
//    public ArrayList<Number> data;
    private final long delayMs;
    private final int blipInteral;
//    private final Thread thread;
    private boolean keepRunning;
    private int latestIndex;

    private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

    /**
     * @param size         Sample size contained within this model
     * @param updateFreqHz Frequency at which new samples are added to the model
     */
    public ECGModel(int size, int updateFreqHz) {
//        data = new Number[size];
        data = new ArrayList<Number>();
        for (int i = 0; i < data.size(); i++) {
//            data[i] = 0;
            data.add(0);
        }

        // translate hz into delay (ms):
        delayMs = 1000 / updateFreqHz;

        // add 7 "blips" into the signal:
        blipInteral = size / 7;

//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    while (keepRunning) {
//                        if (latestIndex >= data.length) {
//                            latestIndex = 0;
//                        }
//
//                        // generate some random data:
//                        if (latestIndex % blipInteral == 0) {
//                            // insert a "blip" to simulate a heartbeat:
//                            data[latestIndex] = (Math.random() * 10) + 3;
//                        } else {
//                            // insert a random sample:
//                            data[latestIndex] = Math.random() * 2;
//                        }
//
//                        if (latestIndex < data.length - 1) {
//                            // null out the point immediately following i, to disable
//                            // connecting i and i+1 with a line:
//                            data[latestIndex + 1] = null;
//                        }
//
//                        if (rendererRef.get() != null) {
//                            rendererRef.get().setLatestIndex(latestIndex);
//                            Thread.sleep(delayMs);
//                        } else {
//                            keepRunning = false;
//                        }
//                        latestIndex++;
//                    }
//                } catch (InterruptedException e) {
//                    keepRunning = false;
//                }
//            }
//        });
    }

    public void start(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
        this.rendererRef = rendererRef;
        keepRunning = true;
//        thread.start();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Number getX(int index) {
        return index;
    }

    @Override
    public Number getY(int index) {
        return data.get(index);
    }

    @Override
    public String getTitle() {
        return "Signal";
    }
}