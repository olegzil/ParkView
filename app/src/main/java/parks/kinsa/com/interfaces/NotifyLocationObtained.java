package parks.kinsa.com.interfaces;

import parks.kinsa.com.utility_classes.Pair;

/**
 * Created by oleg on 4/29/16.
 * Interface used by the LocationTracker to notify the registered client
 * of a new location. Called asynchronously.
 */
public interface NotifyLocationObtained {
    void onSuccess(Pair<Double, Double> location);
}
