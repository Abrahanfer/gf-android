package me.abrahanfer.geniusfeed.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;

import com.einmalfel.earl.AtomEntry;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by abrahan on 19/03/16.
 */
public class FeedItemAtom extends FeedItem implements Parcelable{
    final static public String FEED_ITEM_ATOM_MODEL_TAG = "FeedItemAtomModel";

    private String atomEntryURL;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;
    private String value;

    public FeedItemAtom(AtomEntry entry) {
        super(entry);
        super.setPublicationDate(entry.getPublicationDate());
        atomEntryURL = entry.links.get(0).toString();
        type = entry.content.type;
        value = entry.content.value;
    }

    private FeedItemAtom(Parcel in) {
        super(in);
        atomEntryURL = in.readString();
        type = in.readString();
        value = in.readString();
    }

    public String getAtomEntryURI() {
        return atomEntryURL;
    }

    public void setAtomEntryURI(String atomEntryURL) {
        this.atomEntryURL = atomEntryURL;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(atomEntryURL.toString());
        out.writeString(type);
        out.writeString(value);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
        public FeedItemAtom createFromParcel(Parcel in) {
            return new FeedItemAtom(in);
        }

        public FeedItemAtom[] newArray(int size) {
            return new FeedItemAtom[size];
        }
    };
}
