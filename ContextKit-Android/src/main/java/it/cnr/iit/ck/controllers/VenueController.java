package it.cnr.iit.ck.controllers;

import android.content.Context;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import it.cnr.iit.ck.controllers.network.GsonRequest;
import it.cnr.iit.ck.model.FoursquareSearchData;
import it.cnr.iit.ck.model.LocationInfoEnriched;

public class VenueController {

    private final static String BASE_URL = "https://api.foursquare.com/v2/venues/search?";
    private final static String CLIENT_ID = "3WA2ILSNFOT0TT23J0XGCJ21DGKXXSJ1ADIAEPN4ON0SQ2SS";
    private final static String CLIENT_SECRET = "ODJE2KMLNG0XXZV24JMFTWKXFYZMYPB05ZNGZ20CVCUDIXLD";
    private final static String API_DATE = "20191005";

    private final RequestQueue requestQueue;

    public VenueController(Context context){
        requestQueue = Volley.newRequestQueue(context);
    }

    public void getVenueByCoordinate(final Location location, final VenueListener venueListener){
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        StringBuilder sb = new StringBuilder();
        String url = sb.append(BASE_URL)
                .append("client_id=").append(CLIENT_ID)
                .append("&client_secret=").append(CLIENT_SECRET)
                .append("&v=").append(API_DATE)
                .append("&ll=").append(latitude).append(",").append(longitude)
                .toString();

        GsonRequest<FoursquareSearchData> requestToFoursquare =
                new GsonRequest<>(url, FoursquareSearchData.class, null,
                response -> venueListener.onVenueAvailable(response, location),
                error -> venueListener.onVenueFailed(error.getMessage(), location));

        requestQueue.add(requestToFoursquare);
    }

    public void close(){
        requestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override public boolean apply(Request<?> request) { return true; }});
        requestQueue.stop();
    }

    public interface VenueListener{
        void onVenueAvailable(FoursquareSearchData data, Location location);
        void onVenueFailed(String reason, Location location);
    }
}
