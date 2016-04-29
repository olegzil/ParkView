package parks.kinsa.com.interfaces;

/**
 * Created by oleg on 4/26/16.
 */
public interface NotifyDownloadCompleteInterface {
    void onSuccess(Object data);
    void onFailure(String message);
}
