package parks.kinsa.com.utility_classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import parks.kinsa.com.interfaces.DataReaderInterface;

/**
 * Created by oleg on 4/26/16.
 */
public class StringDataReader implements DataReaderInterface {
    String mError = null;
    StringBuilder mReadBuffer = new StringBuilder();
    @Override
    public String execute(InputStream input) {
        BufferedReader r = new BufferedReader(new InputStreamReader(input));
        String line;
        try {
            while ((line = r.readLine()) != null)
                mReadBuffer.append(line).append('\n');
            return mReadBuffer.toString();
        } catch (IOException e) {
            mError = e.toString();
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getError() {
        return mError;
    }

    @Override
    public String getData() {
        return mReadBuffer.toString();
    }

}
