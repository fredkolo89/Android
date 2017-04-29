package com.example.helper;

import com.example.models.Route;

import java.util.List;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
