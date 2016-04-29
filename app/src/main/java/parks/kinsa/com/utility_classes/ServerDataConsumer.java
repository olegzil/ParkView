package parks.kinsa.com.utility_classes;

/**
 * Created by oleg on 4/26/16.
 */

import android.widget.TextView;

import parks.kinsa.com.interfaces.NotifyDownloadCompleteInterface;

/**
 * An object of this class will be called in case of a successful (or failed) data download from a server.
 */
public class ServerDataConsumer implements NotifyDownloadCompleteInterface {
    TextView mText;
    public ServerDataConsumer(TextView view){
        mText = view;
    }
    /**
     * This method is called if the server data was retrieved successfully.
     * @param data represents server data. Must be cast to the correct object type
     */
    @Override
    public void onSuccess(final Object payload) {
        //the data object is guaranteed to be a valid string. No need to check here.
        //Additionally, because this is a callback, it may have been called from a thread that is not the U.I. thread.
        //To affect the U.I. we must do so on the U.I. thread
        GlobalUtilities.getInstance().getThreadManager().ScheduleWorkerOnUIThread(new Runnable() {
            @Override
            public void run() {
                String data = (String)payload;
                mText.setText(data);
            }
        });
    }

    /**
     * This method is called in the case of a failed server call.
     * @param message a string describing the error.
     */
    @Override
    public void onFailure(final String message) {
        //Same as onSuccess
        GlobalUtilities.getInstance().getThreadManager().ScheduleWorkerOnUIThread(new Runnable() {
            @Override
            public void run() {
                mText.setText(message);
            }
        });
    }
}
