package br.ufpe.cin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.iotivity.base.OcResource;

import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.R;

/**
 * Created by davinomjr on 8/20/17.
 */

public class ResourceListAdapter extends RecyclerView.Adapter<ResourceListAdapter.ViewHolder> {

    private static final String TAG = ResourceListAdapter.class.getName();

    private LayoutInflater inflater;
    private ArrayList<String> mResources;

    public ResourceListAdapter(Context context, ArrayList<String> resources){
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = resources;
    }


    @Override
    public ResourceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resource, parent, false);
        Log.i(TAG, "creating");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResourceListAdapter.ViewHolder holder, int position) {
        String item = getItem(position);
        holder.txtView.setText(item);
        Log.i(TAG, "item = " + item);
    }

    @Override
    public int getItemCount() {
        return mResources.size();
    }

    public void addResource(String resource){
        Log.i(TAG, "RESOURCE BEING ADDED = " + resource);
        mResources.add(resource);
    }

    private String getItem(int pos){
        return getItemCount() > 0 ? mResources.get(pos) : "";
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtView;


        public ViewHolder(View view){
            super(view);
            txtView = view.findViewById(R.id.resource_item);
        }
    }
}
