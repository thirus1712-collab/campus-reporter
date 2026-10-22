package com.campusreporter.model;

import java.util.*;

public class CampusLocations {

    public static final List<String> BLOCKS = List.of(
        "Academic Block",
        "Civil Block",
        "Mechanical Block",
        "Block 3",
        "Electronics Block"
    );

    public static final Map<String, String> FLOOR_PREFIXES = Map.of(
        "1st Floor", "LH-1",
        "2nd Floor", "LH-2",
        "3rd Floor", "LH-3",
        "4th Floor", "LH-4"
    );

    public static final List<String> SHARED_FLOOR_LOCATIONS = List.of(
        "Male Bathroom",
        "Female Bathroom",
        "Staff Room"
    );

    public static boolean isValidBlock(String block) {
        if (block == null) return false;
        return BLOCKS.stream().anyMatch(b -> b.equalsIgnoreCase(block.trim()));
    }

    public static boolean isValidFloor(String floor) {
        if (floor == null) return false;
        return FLOOR_PREFIXES.keySet().stream().anyMatch(f -> f.equalsIgnoreCase(floor.trim()));
    }

    public static boolean isValidLocation(String floor, String location) {
        if (floor == null || location == null) return false;
        String normalizedFloor = FLOOR_PREFIXES.keySet().stream()
            .filter(f -> f.equalsIgnoreCase(floor.trim()))
            .findFirst()
            .orElse(null);

        if (normalizedFloor == null) return false;

        String locTrim = location.trim();
        for (String shared : SHARED_FLOOR_LOCATIONS) {
            if (shared.equalsIgnoreCase(locTrim)) {
                return true;
            }
        }

        String prefix = FLOOR_PREFIXES.get(normalizedFloor);
        if (prefix != null && locTrim.startsWith(prefix)) {
            try {
                int num = Integer.parseInt(locTrim.substring(prefix.length()));
                return num >= 1 && num <= 10;
            } catch (NumberFormatException ignored) {}
        }

        return false;
    }
}
