package me.abrahanfer.geniusfeed.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by abrahan on 25/08/16.
 */
public class Category implements Parcelable {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Add default public constructor for Realm
    public Category() {

    }

    public Category(String name) {
        this.name = name;
    }

    private Category(Parcel in) {
        name = in.readString();
    }

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
    }

    // this is used to regenerate your object.
    // All Parcelables must have a CREATOR that implements
    // these two methods
    public static final Parcelable.Creator<Category> CREATOR;

    static {
        CREATOR = new Creator<Category>() {
            public Category createFromParcel(Parcel in) {
                return new Category(in);
            }

            public Category[] newArray(int size) {
                return new Category[size];
            }
        };
    }

    // Override equal method
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Category))return false;
        Category otherCategory = (Category) other;
        if (otherCategory.getName().equals(this.getName())) {
            return true;
        } else {
            return false;
        }
    }
}
