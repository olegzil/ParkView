package parks.kinsa.com.addapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import parks.kinsa.com.kinsatest.R;
import parks.kinsa.com.utility_classes.ParkDescriptor;

/**
 * Created by oleg on 4/27/16.
 */
public class ParkDataAdapter extends RecyclerView.Adapter<ParkDataAdapter.ViewHolder>  {
    ParkDescriptor[] mData;
    public static ParkDataAdapter newInstance(ParkDescriptor[] data){
        return new ParkDataAdapter(data);
    }
    private ParkDataAdapter(ParkDescriptor[] data){
        mData = data;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_main, parent, false);
        LinearLayout linearLayout = (LinearLayout)v.findViewById(R.id.container);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ParkDescriptor item = mData[position];
        String message = String.format("%s\n%s\n%s\n%s", item.getParkName(), item.getManagerName(), item.getEmail(), item.getPhoneNumber());
        holder.mItemDetail.setText(message);
        holder.setIndex(position);
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    /**
     * This object represents a sing park item on the screen. Used by the system.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button mItemDetail;
        int position;
        public ViewHolder(final View itemView) {
            super(itemView);
            mItemDetail = (Button)itemView.findViewById(R.id.item_detail);
            mItemDetail.setOnClickListener(this);
        }
        public void setIndex(int i){position = i;}

        /**
         * This method displays all available info about a park item. A better implementation would be
         * to display a nicely designed floating view.
         * @param v Item view
         */
        @Override
        public void onClick(View v) {
            final ParkDescriptor item = mData[position];
            Toast.makeText(itemView.getContext(), item.getAll(), Toast.LENGTH_LONG).show();
        }
    }
}
