package parks.kinsa.com.utility_classes;

/**
 * Created by oleg on 4/26/16.
 */

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class. Provides access to various presistant data and objects
 * Usage: GlobalUtilities.getInstance().<the method you want to call>
 */
public class GlobalUtilities {
    public enum eSortKey {eDISTANCE, ePARKNAME, eMANAGERNAME}; //a list of possible sort keys
    private static GlobalUtilities theOneAndOnlyInstance = new GlobalUtilities(); // the one and only singleton instance.
    private static ThreadPoolManager mThreadManager = new ThreadPoolManager();  //the one and only thread pool manager instance.
    private eSortKey mSortKey=eSortKey.eDISTANCE;

    /**
     * @return the Singleton instance
     */
    public static GlobalUtilities getInstance()
    {
        return theOneAndOnlyInstance;
    }

    /**
     * ThreadPoolManager does what its name suggests. It uses a fixed thread pool. The number of threads is limited by the number of
     * available cores. All attempts to use threads should be funneled through this manager to avoid system degradation caused by overly optimistic
     * thread usage.
     */
    static public class ThreadPoolManager {
        public static final String TAG_DEBUG="=-=-=-=-="; //Tag to be used in Log.i() calls. Can be used in conjunction with grep to filter messages via adb logcat
        private ExecutorService mThreadPool;    //the service that does all the work.
        private ScheduledThreadPoolExecutor mScheduledThreadPool;   // the thread pool
        Handler mHandler = null;    //The MAIN handler. This is the handler for the main U.I. thread.

        /**
         * Constructor.
         * Performs one-time initialization
         */
        private  ThreadPoolManager(){
            mThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); //number of available cores (not processors, as the call sugests)
            mScheduledThreadPool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors()); //create the thread pool
            mHandler = new Handler(Looper.getMainLooper()); //access the U.I. thread handler.
        }

        /**
         * This method is used to create worker threads.
         * @param worker a Runnable type object that preforms the actual work
         * @return a Future object representing the running thread.
         */
        public Future ScheduleWorker(Runnable worker) {
            return mThreadPool.submit(worker);
        }

        /**
         * This method is used to post a runnable to the main U.I. thread.
         * If your code touches U.I. elements, it must use this call to post a runnable. Using any other call will result in a runtime exception or unpredictable
         * behavior depending on which part of the U.I. you touch.
         * @param worker the runnable that prefroms the actual work.
         */
        public void ScheduleWorkerOnUIThread(Runnable worker)
        {
            mHandler.removeCallbacks(worker); //Make sure this thing is not run twice.
            mHandler.post(worker);  //post the client runnable to the U.I. thread.
        }

        /**
         *
         * @return the number of active threads, if any.
         */
        public int GetBuisyThreadCount(){
            return ((ThreadPoolExecutor)mThreadPool).getActiveCount();
        }

        /**
         * This method alows delayed execution of threads. DO NOT use this call if your runnable touches U.I. objects.
         * @param worker The runnable object that does the actual work.
         * @param when  Time in milliseconds to delay the start of the execution of the runnable. This value is a delta starting from the current time.
         * @return a future that represents this thread.
         */
        public Future ScheduleWorkerDelayed(Runnable worker, long when){
            return mScheduledThreadPool.schedule(worker, when, TimeUnit.MILLISECONDS);
        }
    }
    private GlobalUtilities() {}

    /**
     *
     * @return a reference to the one and only threading manager.
     */
    public ThreadPoolManager getThreadManager(){return mThreadManager;}

    public static double DistanceFromLatitudeLongitude(double lat1, double lat2, double lon1, double lon2){
        double R = 6371000; //Earth radius in meters
        double deltaLat = (lat2-lat1);
        double deltaLon = (lon2-lon1);

        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    /**
     *     Method returns the current sort key. In production the sort key would be stored with the individual Adapter, but
     *     I ran out of time to do anything more fancy than synchronize of which I am NOT a fan.
     */

    public synchronized eSortKey getCurrentSortKey(){
        return mSortKey;
    }

    /**
     * Change the sort key value. Same problem as getCurrentSortKey(). Should be part of the Adapter.
     * @param newSortKey
     */
    public synchronized void setSortKey(eSortKey newSortKey)
    {
        mSortKey = newSortKey;
    }
}
