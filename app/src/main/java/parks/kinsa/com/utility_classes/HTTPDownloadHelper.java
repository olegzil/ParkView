package parks.kinsa.com.utility_classes;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import parks.kinsa.com.interfaces.DataReaderInterface;
import parks.kinsa.com.interfaces.NotifyDownloadCompleteInterface;

/**
 * Created by oleg on 4/26/16.
 * Class with static methods used to communicate with a server
 */
public class HTTPDownloadHelper {
    /**
     *
     * @param source the server URL used to retrieve the data from.
     * @param reader the reader object that reads server data.
     * @param callback the client that is interested in the success or failure of this call
     * @return true if successful, false otherwise
     */
    public static boolean getData(String source, DataReaderInterface reader, NotifyDownloadCompleteInterface callback){
        InputStream input;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(source); //create an URL object bound to the desired web address
            connection = (HttpURLConnection) url.openConnection(); //attempt to open it
            connection.connect();                                   //attempt to connect

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) //any response other than a 200 is considered to be an error.
            {
                callback.onFailure(String.format("Server returned HTTP %d. Server response string is %s", connection.getResponseCode(), connection.getResponseMessage()));
                return false;
            }
            input = connection.getInputStream(); //access the input stream
            String data = reader.execute(input); //call the client reader to download the data
            if (data == null) { //the reader failed. Let the client know what happened.
                callback.onFailure(reader.getError());
                return false;
            }
            callback.onSuccess(data);


        } catch (Exception e) {
            callback.onFailure(e.toString());
            return false;
        }
        if (connection != null)
            connection.disconnect();
        return  true;
        }
    }

