package com.mad.pogoenhancer.gpx.christofides;

import java.util.ArrayList;

public class Node {
    private boolean isRoot;
    private int number;
    private ArrayList<Node> children;

    public Node(int _n) {
        number = _n;
        children = null;
        isRoot = false;
    }

    Node(int _n, boolean _isRoot) {
        number = _n;
        children = null;
        this.isRoot = _isRoot;
    }

    void addChild(Node _node) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(_node);
    }


    void visitBuildRoute(ArrayList<Integer> _route) {
        _route.add(number);
        if (children == null) return;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).visitBuildRoute(_route);
        }
    }


    void visitFindOddDegreeNodes(ArrayList<Integer> _oddNodes) {
        if (children == null) {
            _oddNodes.add(number);
            return;
        }
        if (isRoot && children.size() % 2 != 0) _oddNodes.add(number);
        if (!isRoot && children.size() % 2 == 0) _oddNodes.add(number);
        for (int i = 0; i < children.size(); i++) {
            children.get(i).visitFindOddDegreeNodes(_oddNodes);
        }
    }

    public int getNumber() {
        return number;
    }
}
