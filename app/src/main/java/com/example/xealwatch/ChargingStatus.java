package com.example.xealwatch;

import android.content.Intent;
import android.os.BatteryManager;

public class ChargingStatus {
    public float percent;
    public boolean isCharging = false;
    public boolean receiverRegistered = false;
    private Intent batteryStatusIntent = null;

    public void SetBatteryStatusIntent(Intent batteryStatus) {
        this.batteryStatusIntent = batteryStatus;
        this.Update();
    }

    /**
     * Updates the percentage and battery status
     */
    public void Update() {
        if (this.batteryStatusIntent == null) return;
        // Whether we are charging
        int status = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL);

        //Battery percent
        int level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        percent = level * 100 / (float) scale;
    }
}
