package parks.kinsa.com.interfaces;

import java.io.InputStream;

/**
 * Created by oleg on 4/26/16.
 */
public interface DataReaderInterface {
    String execute(InputStream input);
    String getError();
    String getData();
}
