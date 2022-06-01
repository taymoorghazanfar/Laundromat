package com.laundromat.customer.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.customer.R;
import com.laundromat.customer.model.util.LaundryRecyclerItem;
import com.laundromat.customer.ui.interfaces.IAllLaundryClickListener;
import com.laundromat.customer.ui.interfaces.IAllLaundryFilterListener;
import com.laundromat.customer.ui.interfaces.IHomeBasedLaundryFilterListener;
import com.laundromat.customer.utils.TimeUtils;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllLaundriesRecyclerAdapter
        extends RecyclerView.Adapter<AllLaundriesRecyclerAdapter.ViewHolder> implements Filterable {

    private List<LaundryRecyclerItem> laundries;
    private List<LaundryRecyclerItem> laundriesFull;
    private IAllLaundryClickListener laundryClickListener;
    private IAllLaundryFilterListener laundryFilterListener;

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<LaundryRecyclerItem> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(laundriesFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (LaundryRecyclerItem item : laundriesFull) {

                    if (item.getLaundry().getName().toLowerCase().contains(stringPattern)) {

                        filteredList.add(item);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            if (((List) filterResults.values).isEmpty()) {

                if (laundryFilterListener != null) {

                    laundryFilterListener.onAllLaundriesFiltered(true);
                }
            } else {

                if (laundryFilterListener != null) {

                    laundryFilterListener.onAllLaundriesFiltered(false);
                }
            }
            laundries.clear();
            laundries.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public AllLaundriesRecyclerAdapter(List<LaundryRecyclerItem> laundries) {
        this.laundries = laundries;
        this.laundriesFull = new ArrayList<>(laundries);
    }

    public void setLaundryClickListener(IAllLaundryClickListener laundryClickListener) {
        this.laundryClickListener = laundryClickListener;
    }

    public void setLaundryFilterListener(IAllLaundryFilterListener laundryFilterListener) {
        this.laundryFilterListener = laundryFilterListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_laundry, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllLaundriesRecyclerAdapter.ViewHolder holder, int position) {

        LaundryRecyclerItem laundry = laundries.get(position);

        holder.imageViewHomeBased
                .setVisibility(laundry.getLaundry().isHomeBased() ? View.VISIBLE : View.GONE);

        Picasso.get()
                .load(laundry.getLaundry().getLogoUrl())
                .into(holder.imageViewLogo);

        holder.textViewName.setText(laundry.getLaundry().getName());

        holder.textViewDistance.setText(MessageFormat
                .format("Distance: {0} KM", laundry.getDistance()));

        holder.textViewTiming.setText(MessageFormat
                .format("Timings: {0} - {1}",
                        laundry.getLaundry().getTimings().getOpeningTime(),
                        laundry.getLaundry().getTimings().getClosingTime()));

        if (laundry.getDuration() == 0) {

            holder.textViewDelivery.setText("Est. Delivery: Not Known");

        } else {

            holder.textViewDelivery.setText(MessageFormat
                    .format("Estimated Delivery: {0} Day(s)", laundry.getDuration()));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String currentTime = dateFormat.format(new Date());

        String openingTime = laundry.getLaundry().getTimings().getOpeningTime();
        String closingTime = laundry.getLaundry().getTimings().getClosingTime();

        boolean laundryOpen;
        try {

            laundryOpen = TimeUtils.isTimeBetweenTwoTime(openingTime, closingTime, currentTime);

            holder.textViewClosed.setVisibility(!laundryOpen ? View.VISIBLE : View.GONE);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return laundries.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageViewLogo;
        private final ImageView imageViewHomeBased;
        private final TextView textViewName;
        private final TextView textViewDistance;
        private final TextView textViewTiming;
        private final TextView textViewDelivery;
        private final TextView textViewClosed;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewLogo = itemView.findViewById(R.id.image_view_logo);
            imageViewHomeBased = itemView.findViewById(R.id.image_view_home_based);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewDistance = itemView.findViewById(R.id.text_view_distance);
            textViewTiming = itemView.findViewById(R.id.text_view_timing);
            textViewDelivery = itemView.findViewById(R.id.text_view_delivery);
            textViewClosed = itemView.findViewById(R.id.text_view_closed);

            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (laundryClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    LaundryRecyclerItem laundry = laundries.get(position);

                    laundryClickListener.onAllLaundryClick(laundry.getLaundry());
                }
            });
        }
    }
}
