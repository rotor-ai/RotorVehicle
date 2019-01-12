package ai.rotor.rotorvehicle;

import android.annotation.SuppressLint;
import android.util.Log;
import java.lang.*;

import timber.log.Timber;


/**
 * This service class provides all high level control over the rotor vehicle
 */
public class RotorCtlService {
    private static final String TAG = "RotorCtlServ";
    private double steerTrim, steerPWMMin, steerPWMMax, steerPWMVal;
    private double throtTrim, throtPWMMin, throtPWMMax, throtPWMVal;
    private int steerRev;
    private final double steerTrimInc = .001;
    private final double throtTrimInc = .001;

    public RotorCtlService() {
        Timber.d("Creating rotor control service instance...");
        steerTrim = 7.5;
        throtTrim = 7;
        steerRev = 1;
    }

    public void steerTrim(boolean left) {
        if (left) {
            steerTrim += steerTrimInc;
        } else {
            steerTrim -= steerTrimInc;
        }
    }

    public void throtTrim(boolean slow) {
        if (slow) {
            throtTrim -= throtTrimInc;
        } else {
            throtTrim += throtTrimInc;
        }
    }

    public void setSteering(int steerVal) {
        // assert steerVal is between -100 and 100
        if (steerVal * steerRev > 0) {
            steerPWMVal = Math.abs(steerVal) / 100 * (steerPWMMax - steerTrim) + steerTrim;
        } else {
            steerPWMVal = - Math.abs(steerVal) / 100 * (steerTrim - steerPWMMin) + steerTrim;
        }

        // set steering
    }

    public void setThrottle(int throtVal) {

    }
}
