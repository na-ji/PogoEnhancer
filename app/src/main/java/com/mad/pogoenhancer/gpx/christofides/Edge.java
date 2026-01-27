package com.mad.pogoenhancer.gpx.christofides;

public class Edge implements Comparable {
    private int from, to;
    private double cost;

    Edge(int _from, int _to, double _cost) {
        from = _from;
        to = _to;
        cost = _cost;
    }

    public int compareTo(Object _o) {
        Edge e = (Edge)_o;
        return Double.compare(this.cost, e.cost);
    }

    public int getTo() {return to;}


    public int getFrom() {return from;}
}