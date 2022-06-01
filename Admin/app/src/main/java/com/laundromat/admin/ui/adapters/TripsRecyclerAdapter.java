package com.laundromat.admin.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.laundromat.admin.R;
import com.laundromat.admin.model.Trip;
import com.laundromat.admin.model.util.PaymentMethod;
import com.laundromat.admin.ui.interfaces.ITripClickListener;
import com.laundromat.admin.utils.LocationUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class TripsRecyclerAdapter
        extends RecyclerView.Adapter<TripsRecyclerAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<Trip> trips;
    private List<Trip> tripsFull;
    private ITripClickListener tripClickListener;
    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Trip> filteredList = new ArrayList<>();

            if (charSequence.equals(null) || charSequence.length() == 0) {

                filteredList.addAll(tripsFull);
            } else {

                String stringPattern = charSequence.toString().toLowerCase().trim();

                for (Trip item : tripsFull) {

                    if (item.getId().substring(item.getId().length() - 10).toLowerCase().contains(stringPattern)) {

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

            trips.clear();
            trips.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public TripsRecyclerAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
        this.tripsFull = new ArrayList<>(this.trips);
    }

    public void setTrips(List<Trip> trips) {

        this.trips.clear();
        this.trips = trips;
    }

    public void setTripClickListener(ITripClickListener tripClickListener) {
        this.tripClickListener = tripClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_trip, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripsRecyclerAdapter.ViewHolder holder, int position) {

        Trip trip = trips.get(position);

        String tripId = "Trip ID: " + trip.getId().substring(trip.getId().length() - 10);
        holder.textViewTripId.setText(tripId);

        holder.textViewDistance.setText(MessageFormat.format("{0} KM", trip.getDistance()));

        holder.textViewCost.setText(MessageFormat.format("PKR {0}", trip.getCost()));

        holder.textViewDate.setText(trip.getDateCreated());

        String paymentMethod;

        if (trip.getOrder().getPaymentMethod() == PaymentMethod.JAZZ_CASH) {

            paymentMethod = "JAZZ CASH";
            holder.textViewPaymentMethod.setBackgroundColor(Color.parseColor("#F44336"));

        } else {

            paymentMethod = "CASH";
            holder.textViewPaymentMethod.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        holder.textViewPaymentMethod.setText(paymentMethod);

        holder.textViewStatus.setText(trip.getStatus().toString());

        String sourceAddress = LocationUtils.getAddressFromLatLng(context,
                trip.getSource().latitude, trip.getSource().longitude);

        String destinationAddress = LocationUtils.getAddressFromLatLng(context,
                trip.getDestination().latitude, trip.getDestination().longitude);

        holder.textViewSource.setText(sourceAddress);

        holder.textViewDestination.setText(destinationAddress);

        holder.textViewTripType.setText(trip.getType().toString().replace("_", " "));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTripId;
        TextView textViewSource;
        TextView textViewDestination;
        TextView textViewTripType;
        TextView textViewDistance;
        TextView textViewCost;
        TextView textViewDate;
        TextView textViewPaymentMethod;
        TextView textViewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTripId = itemView.findViewById(R.id.text_view_trip_id);
            textViewSource = itemView.findViewById(R.id.text_view_source_address);
            textViewDestination = itemView.findViewById(R.id.text_view_destination_address);
            textViewTripType = itemView.findViewById(R.id.text_view_trip_type);
            textViewDistance = itemView.findViewById(R.id.text_view_distance);
            textViewCost = itemView.findViewById(R.id.text_view_cost);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewPaymentMethod = itemView.findViewById(R.id.text_view_payment_method);
            textViewStatus = itemView.findViewById(R.id.text_view_status);

            itemView.setOnClickListener(view -> {

                int position = getAdapterPosition();

                if (tripClickListener != null
                        && position != RecyclerView.NO_POSITION) {

                    Trip trip = trips.get(position);

                    tripClickListener.onTripClick(trip);
                }
            });
        }
    }
}