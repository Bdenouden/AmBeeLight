package com.example.bramd.ambeelight;


//adapter for the recycleView where the ip addresses are listed




import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private ArrayList<String> adresses;

    MainAdapter(ArrayList<String> Adresses) {
        adresses=Adresses;
    }

    @NonNull
    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder viewHolder, int i) {
        viewHolder.mTarget.setText(adresses.get(i));
    }

    @Override
    public int getItemCount() {
        return adresses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTarget;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTarget = itemView.findViewById(R.id.target);
        }
    }
}
