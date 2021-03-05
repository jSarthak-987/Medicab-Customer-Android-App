package com.brightsky.medicab;

import com.brightsky.medicab.distance.DistanceMatrixResponsePojo;
import com.brightsky.medicab.firebasemessaging.DriverBookingMessagingRequestModel;
import com.brightsky.medicab.firebasemessaging.DriverBookingMessagingResponseModel;
import com.brightsky.medicab.hospitalsearchmodel.HospitalSearchResponsePojo;
import com.brightsky.medicab.paytm.PaytmTransactionIdResponseModel;
import com.brightsky.medicab.pickuppointmodel.PlacesSearchResponsePojo;
import com.brightsky.medicab.routesmodel.RoutesResponsePojo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitCallbacks {

    @GET("maps/api/place/findplacefromtext/json")
    Call<PlacesSearchResponsePojo> getPlaceSearchResult(@Query("input") String location, @Query ("inputtype") String inputType, @Query ("fields") String fields, @Query("key") String apikey);

    @GET("maps/api/directions/json")
    Call<RoutesResponsePojo> getRoute(@Query("origin") String origin, @Query ("destination") String destination, @Query("key") String apikey);

    @POST("/notification/send/driver")
    Call<DriverBookingMessagingResponseModel> sendNotificationDemo(@Body DriverBookingMessagingRequestModel demoRequest);

    @GET("maps/api/distancematrix/json")
    Call<DistanceMatrixResponsePojo> getJourneyTime(@Query("origins") String origins, @Query("destinations") String destinations, @Query("key") String key);
}
