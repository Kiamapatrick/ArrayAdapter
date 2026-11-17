# Mushroom Collection Point Picker

An Android app demonstrating a Spinner with a custom ArrayAdapter, stable ID selection, and state restoration across configuration changes.

## What the app does

The user selects a mushroom collection point from a dropdown (Spinner), optionally ticks a "Remind me to bring a field guide" checkbox, taps Submit, and sees a confirmation card with the selected location, its stable ID, and the reminder state. Test controls allow reversing the list, removing the selected item, or emptying the list to verify robust state handling.

## Data flow: source list → adapter → row layout → Spinner

1. **Source list**: A `List<CollectionPoint>` is created in code with stable IDs (101–105) and labels. The first item is a prompt row (id = -1).
2. **Adapter**: `CollectionPointAdapter` extends `ArrayAdapter<CollectionPoint>` and overrides `getView()` and `getDropDownView()` to inflate `row_collection_point.xml` for each item.
3. **Row layout**: Each row shows a mushroom emoji, the label in bold, and a muted "ID 101" line. The prompt row is grey, italic, has no ID line, and is disabled in the dropdown via `isEnabled(position)`.
4. **Spinner**: The adapter is set on the Spinner. Selection returns the `CollectionPoint` object, so we read its stable `id` — never the list position.

## Why store the ID instead of the position?

Positions change when the list is reordered, filtered, or items are removed. The stable ID (101–105) identifies the same logical collection point regardless of its position in the list. On rotation or list mutations, we restore the selection by scanning the current list for the saved ID — this survives reversal, removal, and empty/restore cycles.