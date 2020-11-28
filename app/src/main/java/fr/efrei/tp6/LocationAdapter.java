package fr.efrei.tp6;

import java.util.List;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import fr.efrei.tp6.LocationAdapter.LocationViewHolder;

public final class LocationAdapter
    extends Adapter<LocationViewHolder>
{

  public static final class LocationViewHolder
      extends ViewHolder
  {

    private final TextView textview;

    public LocationViewHolder(@NonNull View itemView)
    {
      super(itemView);
      textview = itemView.findViewById(android.R.id.text1);
    }

    public void update(Location location)
    {
      textview.setText(location.getLatitude() + " // " + location.getLongitude());
    }

  }

  private final List<Location> locations;

  public LocationAdapter(List<Location> locations)
  {
    this.locations = locations;
  }

  @NonNull
  @Override
  public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
  {
    final View rootView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
    return new LocationViewHolder(rootView);
  }

  @Override
  public void onBindViewHolder(@NonNull LocationViewHolder holder, int position)
  {
    holder.update(locations.get(position));
  }

  @Override
  public int getItemCount()
  {
    return locations.size();
  }

}
