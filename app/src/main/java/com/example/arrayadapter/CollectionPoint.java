package com.example.arrayadapter;

/**
 * Represents a mushroom collection point with a stable ID and label.
 * The ID is stable and never the list position.
 */
public class CollectionPoint {
    private final int id;
    private final String label;

    public CollectionPoint(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}