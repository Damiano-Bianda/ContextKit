package it.cnr.iit.ck.features;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;

public class PlayStoreNetworking {

    private final Context context;
    private final RequestQueue requestQueue;

    public PlayStoreNetworking(Context context){
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Cancel all the requests in the queue, request that are in progress will however finish
     */
    public void cancelPendingHttpRequests() {
        requestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override public boolean apply(Request<?> request) { return true; }});
    }

    /**
     * Perform an asynchronous HTTP request to retrieve the app category from Play Store and
     * eventually store the pair (packageName,main category) locally in PlayStoreStorage.
     * If HTTP request return an error:
     * - HTTP 404: Play Store doesn't had the resource, (packageName,PlayStoreStorage.UNKNOWN_APP_CATEGORY) is stored.
     * - Other cases: Another error has occurred (no connectivity, server error, ...), no pair is stored.
     * @param packageName App's package
     */
    public void cacheAppCategory(final String packageName) {
        PlayStoreRequest playStoreRequest = new PlayStoreRequest(packageName,
                response -> PlayStoreStorage.storeAppCategory(context, response),
                error -> {
                    NetworkResponse networkResponse = error.networkResponse;


                    if(networkResponse != null){
                        String appPackage = PlayStoreRequest.getPackage(networkResponse);
                        if (networkResponse.statusCode == 404) {
                            PlayStoreStorage.storeAppCategory(context, new AppCategory(appPackage, PlayStoreStorage.UNKNOWN_APP_CATEGORY));
                        } else if(PlayStoreRequest.hasParseError(networkResponse)){
                            Utils.logWarning(R.string.
                                    problem_during_play_store_parse_warning_message, context, appPackage);
                        }
                    }
                });

        requestQueue.add(playStoreRequest);
    }

    /**
     * Perform a synchronous HTTP request to retrieve the app category from Play Store and
     * eventually store the pair (packageName,main category) locally in PlayStoreStorage.
     * If HTTP request return an error:
     * - HTTP 404: Play Store doesn't had the resource, (packageName,PlayStoreStorage.UNKNOWN_APP_CATEGORY) is stored.
     * - Other cases: see exceptions.
     * @param packageName App's package
     * @param timeoutMilliSeconds How long this call block calling thread
     * @return The category associated with packageName
     * @throws TimeoutException timeoutMilliSeconds expired before call completes, no pair is stored
     * @throws InterruptedException calling thread is interrupted while waiting response, no pair is stored
     * @throws ExecutionException an error has occurred (no connectivity, server error, ...), no pair is stored
     */
    public String cacheAppCategorySinchronously(String packageName, long timeoutMilliSeconds) throws TimeoutException, InterruptedException, ExecutionException {
        RequestFuture<AppCategory> future = RequestFuture.newFuture();
        requestQueue.add(new PlayStoreRequest(packageName, future, future));
        AppCategory response;
        try {
            response = future.get(timeoutMilliSeconds, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {                Log.e(Utils.TAG, e.getMessage());
            e.printStackTrace();
            if (e.getCause() instanceof VolleyError){
                VolleyError volleyError = (VolleyError) e.getCause();
                if (volleyError.networkResponse != null && volleyError.networkResponse.statusCode == 404){
                    PlayStoreStorage.storeAppCategory(context, new AppCategory(packageName, PlayStoreStorage.UNKNOWN_APP_CATEGORY));
                    return PlayStoreStorage.UNKNOWN_APP_CATEGORY;
                }
            }
            throw e;
        }
        PlayStoreStorage.storeAppCategory(context, response);
        return response.getCategory();
    }
}
