package com.mmussol.pistormsandroidthings.pistorms.display;

import android.graphics.Point;

public enum ImageRotation {
    NONE, 
    RIGHT_90(true), 
    RIGHT_180, 
    LEFT_90(true);
    
    private final boolean invertDimension;
    
    private ImageRotation() {
        this(false);
    }
    
    private ImageRotation(boolean invert) {
        this.invertDimension = invert;
    }
    
    public Point getDimension(int width, int height) {
        return invertDimension ? new Point(height, width) : new Point(width, height);
    }
}
