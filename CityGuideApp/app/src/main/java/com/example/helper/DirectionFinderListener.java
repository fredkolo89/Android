package com.example.helper;

import com.example.models.Route;

import java.util.List;

public interface DirectionFinderListener {

    void onDirectionFinderStart();

    void onDirectionFinderSuccess(List<Route> route);
}
