package de.conradowatz.essenimkepler;


import android.os.Parcel;
import android.os.Parcelable;

public class Essen {

    public String desc;
    public String price;

    public Essen(String price, String desc) {
        this.price = price;
        this.desc = desc;
    }

    public Essen() {

    }
}
