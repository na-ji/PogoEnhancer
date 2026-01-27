package com.mad.pogoenhancer.gpx.christofides;

import java.util.ArrayList;
import java.util.Vector;

public class GraphNode {
    private ArrayList<GraphNode> childList;
    private int name;
    private boolean visited;

    GraphNode(int name) {
        this.name = name;
        childList = new ArrayList<>();
        visited = false;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited() {
        visited = true;
    }

    public int getNumberOfChilds() {
        return childList.size();
    }

    public void setNotVisited() {
        visited = false;
    }

    void addChild(GraphNode node) {
        if (!(this.getName() == node.getName())) {
            childList.add(node);
        }
    }

    private void removeChild(GraphNode node) {
        childList.remove(node);
    }


    boolean hasMoreChilds() {
        return childList.size() > 0;
    }

    public int getName() {
        return name;
    }

    void getNextChild(int goal, Vector<Integer> path, boolean firstTime) {
        //om vi nått vårt mål så avsluta
        if (this.getName() == goal && !firstTime) {
            path.add(this.getName());
        } else {
            //om fler vägar från denna nod, lägg till noden och fortsätt längs den första bästa av kanterna, plocka dessutom bort kanten.
            if (childList.size() > 0) {
                GraphNode tmpNode = (GraphNode) childList.remove(0);
                tmpNode.removeChild(this); //ta bort kanten från andra hållet
                path.add(this.getName());
                tmpNode.getNextChild(goal, path, false);
            }
        }
    }
}