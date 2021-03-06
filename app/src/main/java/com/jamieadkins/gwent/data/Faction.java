package com.jamieadkins.gwent.data;

import android.content.Context;

import com.jamieadkins.gwent.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains all of the possible factions.
 */

public class Faction {
    public static final String NORTHERN_REALMS = "Northern Realms";
    public static final String SKELLIGE = "Skellige";
    public static final String MONSTERS = "Monster";
    public static final String SCOIATAEL = "Scoiatael";
    public static final String NEUTRAL = "Neutral";
    public static final String NILFGAARD = "Nilfgaard";

    public static final String[] ALL_FACTIONS = new String[] {
            MONSTERS, NORTHERN_REALMS, SCOIATAEL, SKELLIGE, NILFGAARD, NEUTRAL};

    public static final Map<Integer, String> CONVERT_INT;
    public static final Map<String, Integer> CONVERT_STRING;

    static {
        Map<Integer, String> intToString = new HashMap<>();
        intToString.put(0, MONSTERS);
        intToString.put(1, NORTHERN_REALMS);
        intToString.put(2, SCOIATAEL);
        intToString.put(3, SKELLIGE);
        intToString.put(4, NILFGAARD);
        intToString.put(5, NEUTRAL);
        CONVERT_INT = Collections.unmodifiableMap(intToString);

        Map<String, Integer> stringToInt = new HashMap<>();
        stringToInt.put(MONSTERS, 0);
        stringToInt.put(NORTHERN_REALMS, 1);
        stringToInt.put(SCOIATAEL, 2);
        stringToInt.put(SKELLIGE, 3);
        stringToInt.put(NILFGAARD, 4);
        stringToInt.put(NEUTRAL, 5);
        CONVERT_STRING = Collections.unmodifiableMap(stringToInt);
    }
}
