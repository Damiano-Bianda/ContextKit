package it.cnr.iit.ck.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.cnr.iit.ck.model.Featurable;
import it.cnr.iit.ck.probes.BaseProbe;

public class FeatureMessage {

    private final BaseProbe sender;
    private final long creationTime;
    private final long validityTime;
    private final Featurable featurableData;
    private boolean defaultValue;

    /**
     * Create a new feature message with expiration
     * @param sender the sender reference
     * @param featurableData the object that implements Featurable interface
     * @param creationTime the time that identifies feature creation in milliseconds
     * @param validityTime the interval of time in milliseconds in which the message is valid, a value of Long.MAX_VALUE represent infinity
     */
    public FeatureMessage(BaseProbe sender, Featurable featurableData, long creationTime, long validityTime) {
        this.sender = sender;
        this.featurableData = featurableData;
        this.creationTime = creationTime;
        this.validityTime = validityTime;
    }

    /**
     * Create a new feature message without expiration
     * @param sender a string that represent the sender
     * @param featurableData the object that implements Featurable interface
     * @param creationTime the time that identifies feature creation in milliseconds
     */
    public FeatureMessage(BaseProbe sender, Featurable featurableData, long creationTime){
        this(sender, featurableData, creationTime, Long.MAX_VALUE);
    }

    public FeatureMessage(BaseProbe sender, Featurable featurableData, long creationTime, boolean b) {
        this(sender, featurableData, creationTime, Long.MAX_VALUE);
        this.defaultValue = true;
    }

    /**
     * Getter for sender
     * @return the name of the sender
     */
    public BaseProbe getSender() {
        return sender;
    }

    /**
     * Check if this message is expired or not respect to currentTimeStamp
     * @param currentTimestamp a timestamp in milliseconds
     * @return true if valid, false if expired
     */
    public boolean isValid(long currentTimestamp){
        if (validityTime == Long.MAX_VALUE){
            return true;
        }
        return currentTimestamp <= creationTime + validityTime;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    /**
     * Getter for featuresModuleActive
     * @return a list of featuresModuleActive
     */
    public Featurable getFeaturableData() {
        return featurableData;
    }
}