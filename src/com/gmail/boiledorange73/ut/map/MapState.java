package com.gmail.boiledorange73.ut.map;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The class which has the status of the map.
 * 
 * @author yellow
 * 
 */
public class MapState implements Parcelable {
    /** Map ID */
    public String id;
    /** Current longitude of the center. */
    public double lon;
    /** Current latitude of the center. */
    public double lat;
    /** Current zoom level */
    public int z;

    /**
     * Default constructor. {@link #id} is set by null, {@link #lon} and
     * {@link #lat} are set by NaN, and {@link #z} is set by -1.
     * 
     */
    public MapState() {
        this.id = null;
        this.lon = Double.NaN;
        this.lat = Double.NaN;
        this.z = -1;
    }

    /**
     * Returns the kinds of special objects. This always returns 0.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Writes to the parcel.
     * 
     * @param dest
     *            The parcel to be written.
     * @param flags
     *            Additional flags about how the object should be written.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeDouble(this.lon);
        dest.writeDouble(this.lat);
        dest.writeInt(this.z);
    }

    /**
     * Constructor for parcel.
     * 
     * @param in
     *            Incmoing parcel.
     */
    private MapState(Parcel in) {
        this.id = in.readString();
        this.lon = in.readDouble();
        this.lat = in.readDouble();
        this.z = in.readInt();
    }

    /**
     * Parcelable instance creator. See the Android API document.
     */
    public static final Parcelable.Creator<MapState> CREATOR = new Parcelable.Creator<MapState>() {
        @Override
        public MapState createFromParcel(Parcel in) {
            return new MapState(in);
        }

        @Override
        public MapState[] newArray(int size) {
            return new MapState[size];
        }
    };

}
