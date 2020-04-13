package it.cnr.iit.ck.model;

import android.content.Context;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.cnr.iit.R;
import it.cnr.iit.ck.commons.Utils;
import it.cnr.iit.ck.logs.FileLogger;

public class FoursquareSearchData implements MultiLoggable, Featurable
{
    private Meta meta;
    private Response response;

    @Override
    public List<Double> getFeatures(Context context) {
        List<Double> features = extractVenuesCategories(response.venues, context);
        return features;
    }

    /*@Override
    public String getRowToLog() {
        ArrayList<Object> tokens = new ArrayList<>();
        for(String token: extractVenuesCategoriesIds(extractVenuesInfo(response.venues)))
            tokens.add(Utils.formatStringForCSV(token));
        return TextUtils.join(FileLogger.SEP, tokens);
    }*/

    @Override
    public List<String> getRowsToLog() {
        List<FoursquareVenueData> foursquareVenueDatas = extractVenuesInfo(response.venues);
        List<String> venuesRows = new ArrayList<>();
        for(FoursquareVenueData foursquareVenueData: foursquareVenueDatas)
            venuesRows.add(foursquareVenueData.getRowToLog());
        return venuesRows;
    }

    @Override
    public boolean isEmpty() {
        return extractVenuesInfo(response.venues).isEmpty();
    }

    /**
     * Create a feature vector in 1 hot encoding format, where 1s indicates venues' categories
     * @param venues associated with the coordinates.
     * @param context application context
     * @return 1 hot encoding vector
     */
    private List<Double> extractVenuesCategories(Venues[] venues, Context context) {
        Set<String> mainVenuesCategoryIds = extractVenuesCategoriesIds(extractVenuesInfo(venues));
        String[] categoryIds = context.getResources().getStringArray(R.array.venue_category_ids);
        List<Double> features = new ArrayList<>();
        for(String venueId: categoryIds){
            features.add(mainVenuesCategoryIds.contains(venueId)? 1.0d : 0.0d);
        }
        return features;
    }

    /**
     * Given an array of venues extract 5 or less most significant venues and convert them to a
     * simpler object FoursquareVenueData with venue's name, category and category id.
     * @param venues
     * @return A set of 5 or less FoursquareVenueData objects
     */
    private List<FoursquareVenueData> extractVenuesInfo(Venues[] venues) {
        List<FoursquareVenueData> mainVenues = new ArrayList<>();
        for(int i = 0; i < Math.min(venues.length, 5); i++) {
            Categories[] categories = venues[i].categories;
            for (int j = 0; j < categories.length; j++) {
                Categories category = categories[j];
                if (category.primary) {
                    mainVenues.add(new FoursquareVenueData(venues[i].name, category.id, category.name));
                    break;
                }
            }
        }
        return mainVenues;
    }

    /**
     * Given a list of FoursquareVenueData objects extract a set of associated category's id.
     * @param foursquareVenueDatas
     * @return A set containing category's ids.
     */
    private Set<String> extractVenuesCategoriesIds(List<FoursquareVenueData> foursquareVenueDatas){
        Set<String> ids = new HashSet<>();
        for (FoursquareVenueData foursquareVenueData: foursquareVenueDatas) {
            ids.add(foursquareVenueData.categoryId);
        }
        return ids;
    }

    class Meta { String code; String requestId;}
    class Response { Venues[] venues;}
    class Venues { VenuePage venuePage; String name; Location location; String id; Categories[] categories;}
    class VenuePage { String id;}
    class Categories { String pluralName; String name; Icon icon; String id; String shortName; boolean primary;}
    class Icon { String prefix; String suffix; }
    class Location { String cc; String country; String address; LabeledLatLngs[] labeledLatLngs;
    String lng; String distance; String[] formattedAddress; String city; String postalCode; String state;
    String crossStreet; String lat;}
    class LabeledLatLngs { String lng; String label; String lat;}

    private class FoursquareVenueData implements Loggable{

        private final String venueName;
        private final String categoryId;
        private final String categoryName;

        public FoursquareVenueData(String venueName, String categoryId, String categoryName) {
            this.venueName = venueName;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        @Override
        public String getRowToLog() {
            return Utils.formatStringForCSV(venueName) + FileLogger.SEP + Utils.formatStringForCSV(categoryId) + FileLogger.SEP + Utils.formatStringForCSV(categoryName);
        }

    }
}
