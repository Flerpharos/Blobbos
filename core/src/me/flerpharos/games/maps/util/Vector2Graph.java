package me.flerpharos.games.maps.util;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import me.flerpharos.games.maps.map.ConnectionData;

import java.util.*;

public class Vector2Graph implements IndexedGraph<Vector2> {

    HashMap<Vector2, Integer> nodes;
    Array<Connection<Vector2>> connections;

    public Vector2Graph(Vector2[] nodes, ConnectionData[] connections) {
        this.nodes = new HashMap<>();
        this.connections = new Array<>(connections.length);

        this.connections.setSize(connections.length);

        int index = 0;
        for (ConnectionData c : connections)
            this.connections.set(index ++, new Vector2Connection(nodes[c.a], nodes[c.b]));

        index = 0;
        for (Vector2 node : nodes) {
            this.nodes.put(node, index ++);
        }
    }

    @Override
    public Array<Connection<Vector2>> getConnections(Vector2 fromNode) {
        List<Connection<Vector2>> list = new ArrayList<>();
        for (Connection<Vector2> n : connections.toArray()) {
            if (n.getFromNode().equals(fromNode)) {
                list.add(n);
            }
        }

        return new Array<>();
    }

    public Vector2 getClosestNode(Vector2 position) {
        float closestDistance = Float.MAX_VALUE;
        Vector2 closest = null;

        for (Map.Entry<Vector2, Integer> entry : nodes.entrySet()) {
            if (closest == null) {
                closest = entry.getKey();
                closestDistance = position.dst2(closest);
            } else {
                float temp = position.dst2(entry.getKey());
                if (temp < closestDistance) {
                    closest = entry.getKey();
                    closestDistance = temp;
                }
            }
        }

        return closest;
    }

    @Override
    public int getIndex(Vector2 node) {
        return nodes.get(node);
    }

    @Override
    public int getNodeCount() {
        return nodes.size();
    }
}
