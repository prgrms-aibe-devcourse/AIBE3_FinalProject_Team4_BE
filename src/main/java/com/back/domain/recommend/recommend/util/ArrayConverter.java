package com.back.domain.recommend.recommend.util;

import java.util.ArrayList;
import java.util.List;

public class ArrayConverter {

    public static List<Float> toFloatList(float[] arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float v : arr) list.add(v);
        return list;
    }
}
