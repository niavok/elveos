package com.bloatit.web.pages.tools;

import java.util.HashMap;
import java.util.Map;

import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.HighlightFeature;

public class HightlightedFeaturesTools {


    public static Map<String, String> getReasonsMap() {
        Map<String, String> reasonMap = new HashMap<String, String>();

        reasonMap.put("popular", Context.tr("Popular"));
        reasonMap.put("recent", Context.tr("Recent"));
        reasonMap.put("in_development", Context.tr("In development"));
        reasonMap.put("need_help", Context.tr("Need your help quickly"));
        reasonMap.put("random", Context.tr("Random"));
        reasonMap.put("success", Context.tr("Success"));


        return reasonMap;

    }


    public static String getReason(HighlightFeature feature) {
        Map<String, String> reasonMap = getReasonsMap();
        if(reasonMap.containsKey(feature.getReason())) {
            return reasonMap.get(feature.getReason());
        }
        return feature.getReason();
    }

}
