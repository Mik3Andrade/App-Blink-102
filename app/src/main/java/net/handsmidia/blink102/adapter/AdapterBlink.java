package net.handsmidia.blink102.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.handsmidia.blink102.R;

import java.util.List;

public class AdapterBlink extends RecyclerView.Adapter<AdapterBlink.ViewHolder> {

    List<String> listImages;
    private Callback mCall;
    Context context;

    public AdapterBlink(List<String> list, Context context) {
        this.listImages = list;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterBlink.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflate = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflate.inflate(R.layout.img_edit, parent, false));

    }

    @Override
    public void onBindViewHolder(AdapterBlink.ViewHolder holder, final int position) {

        final String urlImage = listImages.get(position);

        if (context != null && !((Activity) context).isFinishing()) {
            Glide.with(context)
                    .load(urlImage)
                    .into(holder.imgEdit);
        }

        holder.imgEdit.setOnClickListener(v -> mCall.onItemChecked(urlImage));
    }

    @Override
    public int getItemCount() {
        return listImages.size();
    }

    public void update(List<String> listaImg) {

        listImages = listaImg;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEdit;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imgEdit = itemView.findViewById(R.id.imgEdit);
        }
    }

    public interface Callback {
        void onItemChecked(String imageClicable);
    }

    public void setCall(final Callback call) {
        mCall = call;
    }

}
