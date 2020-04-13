package it.cnr.iit.ck.features;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;

public class PlayStoreRequest extends Request<AppCategory> {


    // partial URL
    private static final String CATEGORY_APPLICATION_URL = "https://play.google.com/store/apps/details?id=";

    // query constants
    private static final String CATEGORIES_ATTRIBUTE_KEY = "href";
    private static final String CATEGORIES_ATTRIBUTE_VALUE_PREFIX = "/store/apps/category/";
    private static final String CATEGORY_ELEMENTS_QUERY = "a[href^=" + CATEGORIES_ATTRIBUTE_VALUE_PREFIX
            + "][itemprop=genre]";

    private final String packageName;
    private final Response.Listener<AppCategory> listener;

    public static final String PACKAGE_KEY = "package key";
    private static final String PARSE_ERROR_KEY = "parse error key";

    public PlayStoreRequest(String packageName, Response.Listener<AppCategory> listener, Response.ErrorListener errorListener) {
        super(Method.GET, CATEGORY_APPLICATION_URL + packageName, errorListener);
        this.packageName = packageName;
        this.listener = listener;
    }


    @Override
    protected Response<AppCategory> parseNetworkResponse(NetworkResponse response) {
        try {
            String htmlResponse = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            Document document = Jsoup.parse(htmlResponse);
            Elements appCategoryElements = document.select(CATEGORY_ELEMENTS_QUERY);

            if (!appCategoryElements.isEmpty()) {
                Element mainCategoryElement = appCategoryElements.get(0);
                String mainCategoryAttrValue = mainCategoryElement.attr(CATEGORIES_ATTRIBUTE_KEY);
                if (!mainCategoryAttrValue.isEmpty()) {
                    String mainCategory = mainCategoryAttrValue.replace(CATEGORIES_ATTRIBUTE_VALUE_PREFIX, "");
                    if(mainCategory.length() > 0 && !mainCategory.equals(mainCategoryAttrValue)){
                        return Response.success(new AppCategory(packageName, mainCategory),HttpHeaderParser.parseCacheHeaders(response));
                    }
                }
            }

            ParseError error = new ParseError(response);
            error.networkResponse.headers.put(PARSE_ERROR_KEY, "");
            return Response.error(error);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(AppCategory response) {
        if(null != listener){
            listener.onResponse(response);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        if(networkResponse != null) { // null se device offline o indirizzo non esiste, non null se il server torna una risposta con un qualsiasi codice per esempio pagina non trovata 404
            networkResponse.headers.put(PACKAGE_KEY, packageName);
        }
        super.deliverError(error);
    }

    public static String getPackage(NetworkResponse networkResponse){
        return networkResponse.headers.get(PACKAGE_KEY);
    }

    public static boolean hasParseError(NetworkResponse networkResponse){
        return networkResponse.headers.get(PARSE_ERROR_KEY) == null;
    }
}