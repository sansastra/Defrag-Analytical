package com.defrag;

import com.auxiliarygraph.NetworkState;
import com.auxiliarygraph.elements.Connection;
import com.auxiliarygraph.elements.LightPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sandeep on 17-Nov-15.
 */
public class Reconfig {

    public Reconfig(){
        reconfigLightpath();
    }

    // will work only for a link and not for a network
    public List<Double> reconfigLightpath() {
        List<LightPath> listOfLPs = NetworkState.getListOfLightPaths();
        List<Double> holdingTime = new ArrayList<>();
        boolean check;
        double max;
        int start=1;
        // if (listOfLPs.size() > 0)
        while (start <= NetworkState.getTotalNumerOfSlots()) {
            check = false;
            for (LightPath lp : listOfLPs)
                if (lp.containsMiniGrid(start)) {
                    max=0;
                    for (Map.Entry<Double, Connection> entry : lp.getConnectionMap().entrySet()) {
                        for (int i = 0; i < entry.getValue().getBw(); i++) {
                            holdingTime.add(entry.getKey());
                            if(entry.getKey()>max)
                                max = entry.getKey();
                        }
                        start += entry.getValue().getBw();

                        check = true;
                    }
                    for (int i = 0; i <  NetworkState.getNumOfMiniGridsPerGB(); i++) {
                        //                    if(miniGrids.get(start)==2) {
                        holdingTime.add(max);
                        start++;
                        //                   }
                    }

                }
            if(!check) {
                start++;
            }
        }

        return holdingTime;
    }
}
