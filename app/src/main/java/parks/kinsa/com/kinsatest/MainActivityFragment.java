package parks.kinsa.com.kinsatest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import parks.kinsa.com.addapters.ParkDataAdapter;
import parks.kinsa.com.interfaces.NotifyDownloadCompleteInterface;
import parks.kinsa.com.interfaces.NotifyLocationObtained;
import parks.kinsa.com.utility_classes.GlobalUtilities;
import parks.kinsa.com.utility_classes.HTTPDownloadHelper;
import parks.kinsa.com.utility_classes.LocationTracker;
import parks.kinsa.com.utility_classes.Pair;
import parks.kinsa.com.utility_classes.ParkDescriptor;
import parks.kinsa.com.utility_classes.StringDataReader;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements NotifyLocationObtained {
    private static final String mDataSource=" https://data.sfgov.org/resource/z76i-7s65.json";
    private boolean mSorted = false;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    LocationTracker mLocationTracker;
    ParkDescriptor[] mParks;
    public MainActivityFragment() {
    }

    private void downloadServerData(NotifyDownloadCompleteInterface callback){
        StringDataReader stringDataReader = new StringDataReader(); //create the actual reader that will pull the data from the server
        HTTPDownloadHelper.getData(mDataSource, stringDataReader, callback); //contact the server and start the download
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        //Once the view highrearchy has been created, it is safe to retrieve the server data for display.
        //The network call cannot be done on the U.I. thread. The call is posted to the U.I. thread.
        GlobalUtilities.getInstance().getThreadManager().ScheduleWorker(new Runnable() {
            @Override
            public void run() {
                /**
                 * This call does all the magic. The call to downloadServerData is performed on a non-UI thread, because it resolves into a network call.
                 * The callback onSuccess() is issued after a successful network call to the server. The body of that function is then posted to the main U.I. thread
                 * because it has to touch U.I. widgets and therefor, must be done on the U.I. thread.
                 */
                downloadServerData(new NotifyDownloadCompleteInterface() {
                    @Override
                    public void onSuccess(final Object data) {
                        GlobalUtilities.getInstance().getThreadManager().ScheduleWorkerOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    TextView temp =(TextView) view.findViewById(R.id.debug_text_view);
                                    temp.setVisibility(View.GONE);
                                    JSONArray root = new JSONArray((String)data);
                                    mParks = new ParkDescriptor[root.length()-1];
                                    for (int i=1; i<root.length(); i++) //the first node in the JSON payload is invalid data. Have to skip it.
                                    {
                                        JSONObject item = root.getJSONObject(i);
                                        mParks[i-1] = new ParkDescriptor(item); //adjust index because we started at i=1 not i=0
                                    }
                                    mRecyclerView = (RecyclerView)view.findViewById(R.id.main_recycler_view); //access the recycler view
                                    mLayoutManager = new LinearLayoutManager(getContext()); //we'll be using a linear layout
                                    mRecyclerView.setLayoutManager(mLayoutManager);
                                    mAdapter = ParkDataAdapter.newInstance(mParks); //create the adapter
                                    mRecyclerView.setAdapter(mAdapter);             //set it
                                    initLocationTracker();
                                } catch (JSONException e) {
                                    String message = String.format("Please make sure you are connected to the internet then restart the app\n%s", e.toString());
                                    onFailure(message);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(String message) {
                        TextView textView = (TextView)view.findViewById(R.id.debug_text_view);
                        textView.setText(message);
                        textView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    /**
     * This methods sorts the park data based on the selected key.The method has to be synchronized, because it can be executed
     * while a location update is in progress.
     * @param force if false then sorting is done based on a global flag, otherwise the sort is performed unconditinally
     */
    private synchronized void sortParkData(boolean force){
        Runnable predicate=new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        };
        if (mParks == null)
            return;
        if (force){
             Arrays.sort(mParks);
            GlobalUtilities.getInstance().getThreadManager().ScheduleWorkerOnUIThread(predicate);
         }
        else if(!mSorted){
             mSorted=true;
             Arrays.sort(mParks);
            GlobalUtilities.getInstance().getThreadManager().ScheduleWorkerOnUIThread(predicate);
         }
    }

    /**
     * Method that initializes the LocationTracker and then starts it.
     */
    private void initLocationTracker(){
        mLocationTracker = new LocationTracker(getContext(), this);
        mLocationTracker.start();
    }
    @Override
    public void onStart(){
        super.onStart();
        if (mLocationTracker == null)
            return;
        mLocationTracker.start();
    }
    @Override
    public void onStop(){
        super.onStop();
        if (mLocationTracker == null)
            return;
        mLocationTracker.stop();
    }

    /**
     * This method is called by the LocationTracker object once a location update is ready
     * @param location
     */
    @Override
    public void onSuccess(Pair<Double, Double> location) {
        if (mParks == null)
            return;
        //update distance to parks based on new location data
        for (ParkDescriptor park : mParks){
            park.computeDistance(location);
        }
        sortParkData(false);
    }
}
