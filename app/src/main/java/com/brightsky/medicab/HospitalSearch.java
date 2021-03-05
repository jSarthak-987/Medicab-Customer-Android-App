package com.brightsky.medicab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brightsky.medicab.pickuppointmodel.PlacesSearchResponsePojo;
import com.google.android.gms.maps.model.LatLng;
import com.mecofarid.squeezeloader.SqueezeLoader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HospitalSearch extends AppCompatActivity {

    private RecyclerView hospitalSearchResultRecyclerView;
    private RecyclerView recentHospitalSearchRecyclerView;
    private List<PlaceSearchResultModel> hospitalSearchResults;

    private EditText hospitalSearchEditText;
    private TextView recentHospitalTitle;
    private LinearLayout mLocateOnMapButton;
    private ImageView backButton;

//    private HospitalSearchResultRecyclerviewAdapter hospitalSearchResultRecyclerviewAdapter = null;
    private SqueezeLoader squeezeLoader;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_search);

        backButton = findViewById(R.id.back_button);
        squeezeLoader = findViewById(R.id.pickup_loading_bar);
        recentHospitalTitle = findViewById(R.id.recent_search_title);
        mLocateOnMapButton = findViewById(R.id.pickup_locate_on_map_button);
        hospitalSearchEditText = findViewById(R.id.hospital_search_edit_text);
        hospitalSearchResultRecyclerView = findViewById(R.id.pickup_search_result_recycler_view);
        recentHospitalSearchRecyclerView = findViewById(R.id.recent_search_hospitals_recycler_view);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);

//        List<RecentHospitalSearchResult> recentHospitalSearchResults = getRecentSearches();
//        RecentSearchHospitalRecyclerViewAdapter recentSearchHospitalRecyclerViewAdapter = new RecentSearchHospitalRecyclerViewAdapter(HospitalSearch.this, recentHospitalSearchResults);
//
//
//        hospitalSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recentHospitalSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recentHospitalSearchRecyclerView.setAdapter(recentSearchHospitalRecyclerViewAdapter);
//
//        hospitalSearchEditText.addTextChangedListener(new TextWatcher() {
//            private Timer timer = new Timer();
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                squeezeLoader.setVisibility(View.VISIBLE);
//
//                timer.cancel();
//                timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        getHospitalLists(s.toString());
//                    }
//                }, 3000);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//
//
//
//        mLocateOnMapButton.setOnClickListener(v -> {
//            setResult(CustomerMapsActivity.HOSPITAL_LOCATION_MAP_SELECTION_RESULT_CODE);
//            finish();
//        });
//
//        backButton.setOnClickListener(v -> finish());
    }
//
//    private void getHospitalLists(String text) {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://maps.googleapis.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        RetrofitCallbacks callbacks = retrofit.create(RetrofitCallbacks.class);
//
//        Call<PlacesSearchResponsePojo> pickupSearchResultCallback = callbacks.getPlaceSearchResult(text, "textquery", "formatted_address,name,geometry", getString(R.string.google_maps_key));
//        pickupSearchResultCallback.enqueue(new Callback<PlacesSearchResponsePojo>() {
//            @Override
//            public void onResponse(@NotNull Call<PlacesSearchResponsePojo> call, @NotNull Response<PlacesSearchResponsePojo> response) {
//                PlacesSearchResponsePojo pickups = response.body();
//                hospitalSearchResults = new ArrayList<>();
//
//                if (pickups != null) {
//                    for(int i = 0; i < pickups.getCandidates().size(); i++) {
//                        hospitalSearchResults.add(new PlaceSearchResultModel(pickups.getCandidates().get(i), pickups.getCandidates().get(i).getFormattedAddress()));
//                    }
//
//                    if (hospitalSearchResultRecyclerviewAdapter == null) {
//                        hospitalSearchResultRecyclerviewAdapter = new HospitalSearchResultRecyclerviewAdapter(hospitalSearchResults, HospitalSearch.this, pref);
//                        hospitalSearchResultRecyclerView.setAdapter(hospitalSearchResultRecyclerviewAdapter);
//                    } else {
//                        hospitalSearchResultRecyclerviewAdapter.updateResults(hospitalSearchResults);
//                        hospitalSearchResultRecyclerviewAdapter.notifyDataSetChanged();
//                    }
//                }
//
//                else {
//                    Toast.makeText(HospitalSearch.this, "No Result Found", Toast.LENGTH_SHORT).show();
//                }
//
//                squeezeLoader.setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<PlacesSearchResponsePojo> call, @NotNull Throwable t) {
//                t.printStackTrace();
//                Toast.makeText(HospitalSearch.this, "Error Fetching Results", Toast.LENGTH_SHORT).show();
//
//                squeezeLoader.setVisibility(View.INVISIBLE);
//            }
//        });
//    }
//
//    private List<RecentHospitalSearchResult> getRecentSearches() {
//        Set<String> hospitalNameSet = pref.getStringSet("HospitalName", null);
//        Set<String> hospitalAddressSet = pref.getStringSet("HospitalAddress", null);
//        Set<String> hospitalLatSet = pref.getStringSet("HospitalLat", null);
//        Set<String> hospitalLongSet = pref.getStringSet("HospitalLong", null);
//
//        List<RecentHospitalSearchResult> recentHospitalSearchResults = new ArrayList<>();
//
//        if(hospitalNameSet != null && hospitalAddressSet != null && hospitalLatSet != null && hospitalLongSet != null) {
//            List<String> hospitalNameList = new ArrayList<>(hospitalNameSet);
//            List<String> hospitalAddressList = new ArrayList<>(hospitalAddressSet);
//            List<String> hospitalLatList = new ArrayList<>(hospitalLatSet);
//            List<String> hospitalLongList = new ArrayList<>(hospitalLongSet);
//
//            if(hospitalNameList.size() > 0) {
//                recentHospitalTitle.setVisibility(View.VISIBLE);
//            }
//
//            for (int i = 0; i < hospitalNameList.size() && i < 5; i++) {
//                String hospitalName = hospitalNameList.get(i);
//                String hospitalAddress = hospitalAddressList.get(i);
//                String hospitalLat = hospitalLatList.get(i);
//                String hospitalLong = hospitalLongList.get(i);
//
//                RecentHospitalSearchResult recentHospitalSearchResult = new RecentHospitalSearchResult(hospitalName, hospitalAddress, hospitalLat, hospitalLong);
//                recentHospitalSearchResults.add(recentHospitalSearchResult);
//            }
//        }
//
//        return recentHospitalSearchResults;
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(data != null) {
//            if (requestCode == LOCATION_INTENT) {
//                LatLng markedHospitalLatLng = data.getExtras().getParcelable("MarkedLatLng");
//                String markedHospitalAddress = data.getStringExtra("MarkedLocationName");
//                String markedHospitalCity = data.getStringExtra("MarkedLocationCity");
//
//                if(markedHospitalAddress.equals("")) {
//                    try {
//                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//                        List<Address> addresses = geocoder.getFromLocation(markedHospitalLatLng.latitude, markedHospitalLatLng.longitude, 1);
//
//                        String address = addresses.get(0).getAddressLine(0);
//                        String city = addresses.get(0).getLocality();
//
//                        Intent returnIntent = new Intent();
//                        returnIntent.putExtra("HospitalName", address);
//                        returnIntent.putExtra("HospitalLatLng", markedHospitalLatLng);
//                        returnIntent.putExtra("HospitalCity", city);
//
//                        setResult(1, returnIntent);
//                        finish();
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Intent returnIntent = new Intent();
//                    returnIntent.putExtra("HospitalName", markedHospitalAddress);
//                    returnIntent.putExtra("HospitalLatLng", markedHospitalLatLng);
//                    returnIntent.putExtra("HospitalCity", markedHospitalCity);
//
//                    setResult(1, returnIntent);
//                    finish();
//                }
//            }
//        }
//    }
//
//    private String getHospitalAddress(double lat, double lon) {
//        try {
//            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
//            return addresses.get(0).getAddressLine(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "";
//        }
//    }

//    static class RecentSearchHospitalRecyclerViewAdapter extends RecyclerView.Adapter<RecentSearchHospitalRecyclerViewAdapter.ViewHolder> {
//
//        Activity parentActivity;
//        List<RecentHospitalSearchResult> recentHospitalSearchResults;
//
//        public RecentSearchHospitalRecyclerViewAdapter(Activity parentActivity, List<RecentHospitalSearchResult> recentHospitalSearchResults) {
//            this.recentHospitalSearchResults = recentHospitalSearchResults;
//            this.parentActivity = parentActivity;
//        }
//
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hospital_search_result_layout, parent, false);
//            return new ViewHolder(v);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            TextView hospitalName = holder.getHospitalName();
//            TextView hospitalAddress = holder.getHospitalAddress();
//
//            hospitalAddress.setText(recentHospitalSearchResults.get(position).getHospitalAddress());
//            hospitalName.setText(recentHospitalSearchResults.get(position).getHospitalName());
//        }
//
//        @Override
//        public int getItemCount() {
//            return recentHospitalSearchResults.size();
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//
//            private final TextView hospitalName;
//            private final TextView hospitalAddress;
//
//            public ViewHolder(@NonNull View itemView) {
//                super(itemView);
//
//                hospitalName = itemView.findViewById(R.id.hospital_name);
//                hospitalAddress = itemView.findViewById(R.id.hospital_address);
//
//                itemView.setOnClickListener(this);
//            }
//
//            public TextView getHospitalAddress() {
//                return hospitalAddress;
//            }
//
//            public TextView getHospitalName() {
//                return hospitalName;
//            }
//
//            @Override
//            public void onClick(View v) {
//                Intent returnIntent = new Intent();
//
//                LatLng hospitalLatLng = new LatLng(
//                        Double.parseDouble(recentHospitalSearchResults.get(getLayoutPosition()).getHospitalLat()),
//                        Double.parseDouble(recentHospitalSearchResults.get(getLayoutPosition()).getHospitalLong()));
//
//                returnIntent.putExtra("HospitalName", recentHospitalSearchResults.get(getLayoutPosition()).getHospitalName());
//                returnIntent.putExtra("HospitalLatLng", hospitalLatLng);
//                parentActivity.setResult(CustomerMapsActivity.HOSPITAL_LOCATION_LIST_SELECTION_RESULT_CODE, returnIntent);
//                parentActivity.finish();
//            }
//        }
//    }
}