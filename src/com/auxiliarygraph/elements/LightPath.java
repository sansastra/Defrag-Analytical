package com.auxiliarygraph.elements;

import com.auxiliarygraph.NetworkState;
import com.graph.elements.edge.EdgeElement;
import com.graph.path.PathElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Fran on 6/11/2015.
 */
public class LightPath {

    private PathElement pathElement;
    private List<Integer> miniGridIds;
    private Map<Double, Connection> connectionMap;
    private static final Logger log = LoggerFactory.getLogger(LightPath.class);
    private final int GUARD_BANDS;

    public LightPath(PathElement pathElement, int initialMiniGrid, int bwWithGB, int bw, Connection connection) {
        this.pathElement = pathElement;
        this.miniGridIds = new ArrayList<>();
        this.connectionMap = new HashMap<>();
        this.GUARD_BANDS = bwWithGB - bw;

        for (int i = initialMiniGrid; i < initialMiniGrid + bwWithGB; i++)
            miniGridIds.add(i);

        connectionMap.put(connection.getStartingTime(), connection);

        for (EdgeElement e : pathElement.getTraversedEdges()) {
            NetworkState.getFiberLinksMap().get(e.getEdgeID()).setGuardBandMiniGrid(miniGridIds.get(0));
            for (int i = 1; i <  bwWithGB-1; i++)
                NetworkState.getFiberLinksMap().get(e.getEdgeID()).setUsedMiniGrid(miniGridIds.get(i));
            NetworkState.getFiberLinksMap().get(e.getEdgeID()).setGuardBandMiniGrid(miniGridIds.get(bwWithGB-1));
        }
    }

    public PathElement getPathElement() {
        return pathElement;
    }

    public int getLPbandwidth(){return miniGridIds.size();}

    public void expandLightPathOnLeftSide(int bw, Connection connection) {

        int firstFreeMiniGrid = miniGridIds.get(0) - 1;
        for (int i = firstFreeMiniGrid; i > firstFreeMiniGrid - bw; i--) {
            miniGridIds.add(i);
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setUsedMiniGrid(i);
        }
        Collections.sort(miniGridIds);

        connectionMap.put(connection.getStartingTime(), connection);
    }

    public void expandLightPathOnRightSide(int bw, Connection connection) {

        /** Expand the fiber links */
        int miniGrid = miniGridIds.get(miniGridIds.size() - 1 - GUARD_BANDS) + 1;

        for (int i = miniGrid; i < miniGrid + bw; i++)
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setUsedMiniGrid(i);

        /** Move Guard Bands*/
        for (int i = miniGrid + bw; i < miniGrid + bw + GUARD_BANDS; i++)
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setGuardBandMiniGrid(i);

        /** Assign Mini-Grids to the LightPath*/
        miniGrid = miniGridIds.get(miniGridIds.size() - 1) + 1;
        for (int i = miniGrid; i < connection.getBw() + miniGrid; i++)
            miniGridIds.add(i);

        /** Add the connection*/
        connectionMap.put(connection.getStartingTime(), connection);

    }


    public boolean containsMiniGrid(int miniGrid) {
        for (Integer i : miniGridIds)
            if (i.equals(miniGrid))
                return true;
        return false;
    }

    public int getNumberOfMiniGridsUsedAlongLP() {
        int usedMiniGrids = 0;

        for (EdgeElement e : pathElement.getTraversedEdges()) {
            FiberLink fl = NetworkState.getFiberLink(e.getEdgeID());
            usedMiniGrids += fl.getNumberOfMiniGridsUsed();
        }

        return usedMiniGrids;

    }

   /* public double getFragmentationIndexAlongLP() {
        double fragmentationIndex = 0;

        for (EdgeElement e : pathElement.getTraversedEdges()) {
            FiberLink fl = NetworkState.getFiberLink(e.getEdgeID());
            fragmentationIndex += fl.getLinkFragmentationIndex();
        }

        return fragmentationIndex;

    }
*/
    public Map<Double, Connection> getConnectionMap() {
        return connectionMap;
    }

    public void releaseAllMiniGrids() {
        for (EdgeElement e : pathElement.getTraversedEdges())
            for (Integer i : miniGridIds)
                NetworkState.getFiberLink(e.getEdgeID()).setFreeMiniGrid(i);
    }

    public void removeConnectionOnLeftSide(Connection connection) {

        connectionMap.remove(connection.getStartingTime());

        for (int i = 0; i < connection.getBw(); i++)
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setFreeMiniGrid(miniGridIds.get(i));

        for (int i = 0; i < connection.getBw(); i++)
            miniGridIds.remove(0);

    }

    public void removeConnectionAndCompress(Connection connection) {

        connectionMap.remove(connection.getStartingTime());
// set free mini grids on the fiber links traversed by this connection
        int lastMiniGrid = miniGridIds.size() - 1;
        for (int i = lastMiniGrid; i > lastMiniGrid - connection.getBw(); i--)
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setFreeMiniGrid(miniGridIds.get(i));
// replace guard band also
        lastMiniGrid = miniGridIds.size() - 1 - (int) connection.getBw();
        for (int i = lastMiniGrid; i > lastMiniGrid - GUARD_BANDS; i--)
            for (EdgeElement e : pathElement.getTraversedEdges())
                NetworkState.getFiberLink(e.getEdgeID()).setGuardBandMiniGrid(miniGridIds.get(i));
// remove minigrid IDs
        for (int i = 0; i < connection.getBw(); i++)
            miniGridIds.remove(miniGridIds.size() - 1);

    }

    public int getFirstMiniGrid() {
        return miniGridIds.get(0);
    }

// new code
    public void removeAllMinigridIDs(){
            miniGridIds.clear();
    }
    public void setMinigridIDs(int initialgrid, int bw){
        for (int i = initialgrid; i < initialgrid + bw; i++)
            miniGridIds.add(i);
        Collections.sort(miniGridIds);
    }

    public void setAllMiniGrids() {
        for (EdgeElement e : pathElement.getTraversedEdges())
            for (Integer i : miniGridIds)
                NetworkState.getFiberLink(e.getEdgeID()).setUsedMiniGrid(i);
    }

    public void reconfigureAllConnections(int initialGrid, int demand){
        Map<Double, Connection> treeMap = new TreeMap<Double, Connection>(new Comparator<Double>() {

                    @Override
                    public int compare(Double o1, Double o2) {
                        return o2.compareTo(o1);
                    }

                });
        treeMap.putAll(connectionMap);
        for (Map.Entry<Double, Connection> entry : treeMap.entrySet()){
            entry.getValue().setMiniGrid(initialGrid);
            initialGrid = initialGrid + entry.getValue().getBw()-1;
        }
    }

}
