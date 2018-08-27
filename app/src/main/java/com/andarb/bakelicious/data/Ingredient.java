package com.andarb.bakelicious.data;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * POJO for one of the ingredients used in a Recipe.
 */
public class Ingredient implements Parcelable {

    private float quantity;
    private String measure;
    private String ingredient;

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }


    /**
     * Parcelable interface implementation.
     */

    // Store values in a Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(quantity);
        out.writeString(measure);
        out.writeString(ingredient);
    }

    // Read values previously stored in a Parcel
    private Ingredient(Parcel in) {
        quantity = in.readFloat();
        measure = in.readString();
        ingredient = in.readString();
    }

    // Not applicable in our case, so return `0`
    @Override
    public int describeContents() {
        return 0;
    }

    // Helper constant and methods for creating a Parcel
    public static final Parcelable.Creator<Ingredient> CREATOR
            = new Parcelable.Creator<Ingredient>() {

        @Override
        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };
}