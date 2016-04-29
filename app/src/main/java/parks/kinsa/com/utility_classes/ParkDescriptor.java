package parks.kinsa.com.utility_classes;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oleg on 4/27/16.
 */
public class ParkDescriptor implements Comparable<ParkDescriptor>{
    String mParkName,  mManagerName, mEmail, mAddress, mAll;
    long mPhoneNumber;
    double mLongitude, mLatitude;
    Double mDistance = new Double(-1.0);

    /**
     * Utility function to parse JSON data so that if an exception is thrown, you can continue parsing.
     * @param data JSON data to parse
     * @param key the string key we're looking for.
     * @return the value associated with the key on success, null otherwise.
     */
    private  String parseStringObject(JSONObject data, String key){
        String retVal=null;
        try {
            retVal = data.getString(key);
        } catch (JSONException e) {
            retVal = null;
        }
        finally {
            return retVal;
        }
    }

    /**
     * This method extracts all data from JSON
     * @param root the JSON object that describes a single park entity
     * @return a new line separated string of all data
     */
    private String parseAllData(JSONObject root){
        String retVal = null;
        try{
            JSONObject location = root.getJSONObject("location_1");
            retVal = String.format("Park type: %s\nPark Name: %s\nEmail: %s\nZip: %s\nPark id: %s\nDistrict: %s\nPhone#: %s\nService Area: %s\nAcres: %s\nManager: %s\nAddress: %s\nLongitude: %s Latitude: %s\n",
                                        parseStringObject(root, "parktype"),
                                        parseStringObject(root, "parkname"),
                                        parseStringObject(root, "email"),
                                        parseStringObject(root, "zipcode"),
                                        parseStringObject(root, "parkid"),
                                        parseStringObject(root, "supdist"),
                                        parseStringObject(root, "number"),
                                        parseStringObject(root, "parkservicearea"),
                                        parseStringObject(root, "acreage"),
                                        parseStringObject(root, "psamanager"),
                                        parseAddress(location, root, "human_address"),
                                        parseStringObject(location, "longitude"),
                                        parseStringObject(location, "latitude"));
        }catch (JSONException e){
            retVal = null;
        }
        return retVal;
    }

    /**
     * This method parses the address object. A separate method is used because unlike the other data, this is an embedded JSON object.
     * @param partialAddress the JSON object representing the address
     * @param data the JSON object that represent a single park entity (it contains the zip code)
     * @param key the identifying the address JSON object
     * @return a single, space separated string that is the address of this park.
     */
    private String parseAddress(JSONObject partialAddress, JSONObject data, String key){
        String retVal = null;
        try{
            String str = partialAddress.getString(key);
            JSONObject obj = new JSONObject(str);
            retVal = String.format("%s %s %s %s", obj.getString("address"), obj.getString("city"), obj.getString("state"), data.getString("zipcode"));
        }catch (JSONException e){
            retVal = null;
        }
        return retVal;
    }

    /**
     * This class completely represents a park entity
     * @param park_data the JSON object that is the park.
     */
    public ParkDescriptor(JSONObject park_data)
    {
        //extract the required tokens from the JSON payload. A null value means the token was not found.
        mParkName = parseStringObject(park_data, "parkname");
        mManagerName = parseStringObject(park_data, "psamanager");
        mEmail = parseStringObject(park_data, "email");
        String temp = parseStringObject(park_data, "number");
        if (temp == null || temp.length() < 7)
            mPhoneNumber = -1;
        else
            mPhoneNumber = Long.parseLong(temp.replaceAll("\\D+",""));

        try {
            JSONObject location = park_data.getJSONObject("location_1");
            String lat = parseStringObject(location, "latitude");
            String lon = parseStringObject(location, "longitude");
            mAddress = parseAddress(location, park_data, "human_address");
            if (lat != null && lat != null) {
                mLatitude = Double.parseDouble(lat);
                mLongitude = Double.parseDouble(lon);
            }
            mAll = parseAllData(park_data);
        } catch (JSONException ignore) {
            mLongitude = mLatitude = -1.0;
        }
    }
    //these methods are self-documenting
    public String getParkName(){return mParkName;}
    public String getManagerName(){return  mManagerName;}
    public String getEmail() {return mEmail;}
    public String getPhoneNumber(){return String.valueOf(mPhoneNumber);}
    public Pair<Double, Double> getLocation(){return new Pair<>(mLatitude, mLongitude);}
    public String getAddress(){return mAddress;}
    public String getAll(){return mAll;}
    public double computeDistance(Pair<Double, Double> here){
        double R = 6371000; // metres
        double latitude1 = here.first;
        double latitude2 = mLatitude;
        double longitude1 = here.second;
        double longitude2 = mLongitude;

        double deltaLat = (latitude1-latitude2);
        double deltaLon = (longitude2-longitude1);

        double a = Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0) +
                Math.cos(latitude1) * Math.cos(latitude2) *
                        Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        mDistance = R*c;
        return mDistance;
    }

    @Override
    public int compareTo(ParkDescriptor another) {
        GlobalUtilities.eSortKey sortKey = GlobalUtilities.getInstance().getCurrentSortKey();
        switch (sortKey){
            case eDISTANCE:
                return mDistance.compareTo(another.mDistance);
            case ePARKNAME:
                return mParkName.compareTo(another.mParkName);
            case eMANAGERNAME:
                return mManagerName.compareTo(another.mManagerName);
        }
        return 0;
    }
}
