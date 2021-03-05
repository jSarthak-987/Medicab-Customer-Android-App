package com.brightsky.medicab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mecofarid.squeezeloader.SqueezeLoader;

import java.util.List;

public class PickupSearch extends AppCompatActivity {

    private RecyclerView pickupSearchResultRecyclerView;
    private List<PlaceSearchResultModel> pickupSearchResults;

//    private PickupSearchResultRecyclerviewAdapter pickupSearchResultRecyclerviewAdapter = null;
    private SqueezeLoader squeezeLoader;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup_address_search);

        backButton = findViewById(R.id.back_button);
        squeezeLoader = findViewById(R.id.pickup_loading_bar);
        EditText pickupSearchEditText = findViewById(R.id.pickup_search_edit_text);
        pickupSearchResultRecyclerView = findViewById(R.id.pickup_search_result_recycler_view);
        LinearLayout mLocateOnMapButton = findViewById(R.id.pickup_locate_on_map_button);

        backButton.setOnClickListener(v -> finish());
        pickupSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(this));

//        pickupSearchEditText.addTextChangedListener(new TextWatcher() {
//            private Timer timer = new Timer();
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                squeezeLoader.setVisibility(View.VISIBLE);
//                timer.cancel();
//                timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
////                        getPickupLists(s.toString());
//                    }
//                }, 1000);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//        mLocateOnMapButton.setOnClickListener(v -> {
//            setResult(CustomerMapsActivity.PICKUP_LOCATION_MAP_SELECTION_RESULT_CODE);
//            finish();
//        });
    }

//    private void getPickupLists(String text) {
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
//                pickupSearchResults = new ArrayList<>();
//
//                if (pickups != null) {
//                    for(int i = 0; i < pickups.getCandidates().size(); i++) {
//                        pickupSearchResults.add(new PlaceSearchResultModel(pickups.getCandidates().get(i), pickups.getCandidates().get(i).getFormattedAddress()));
//                    }
//
////                    if (pickupSearchResultRecyclerviewAdapter == null) {
////                        pickupSearchResultRecyclerviewAdapter = new PickupSearchResultRecyclerviewAdapter(pickupSearchResults, PickupSearch.this);
////                        pickupSearchResultRecyclerView.setAdapter(pickupSearchResultRecyclerviewAdapter);
//                    } else {
//                        pickupSearchResultRecyclerviewAdapter.updateResults(pickupSearchResults);
//                        pickupSearchResultRecyclerviewAdapter.notifyDataSetChanged();
//                    }
//                }
//
//                else {
//                    Toast.makeText(PickupSearch.this, "No Result Found", Toast.LENGTH_SHORT).show();
//                }
//
//                squeezeLoader.setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<PlacesSearchResponsePojo> call, @NotNull Throwable t) {
//                t.printStackTrace();
//                Toast.makeText(PickupSearch.this, "Error Fetching Results", Toast.LENGTH_SHORT).show();
//
//                squeezeLoader.setVisibility(View.INVISIBLE);
//            }
//        });
//    }
//
////    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(data != null) {
//            if (requestCode == LOCATION_INTENT) {
//                LatLng pickupLatLng = data.getExtras().getParcelable("MarkedLatLng");
//                String pickupLocationAddress = data.getStringExtra("MarkedLocationName");
//                String pickupLocationCity = data.getStringExtra("MarkedLocationCity");
//
//                if(pickupLocationAddress.equals("") || pickupLocationCity.equals("")) {
//                    try {
//                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//                        List<Address> addresses = addresses = geocoder.getFromLocation(pickupLatLng.latitude, pickupLatLng.longitude, 1);
//
//                        String address = addresses.get(0).getAddressLine(0);
//                        String city = addresses.get(0).getLocality();
//
//                        Intent returnIntent = new Intent();
//                        returnIntent.putExtra("PickupName", address);
//                        returnIntent.putExtra("PickupLatLng", pickupLatLng);
//                        returnIntent.putExtra("PickupCity", city);
//
//
//                        setResult(1, returnIntent);
//                        finish();
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Intent returnIntent = new Intent();
//                    returnIntent.putExtra("PickupName", pickupLocationAddress);
//                    returnIntent.putExtra("PickupLatLng", pickupLatLng);
//                    returnIntent.putExtra("PickupCity", pickupLocationCity);
//
//                    setResult(1, returnIntent);
//                    finish();
//                }
//            }
//        }
//    }


}