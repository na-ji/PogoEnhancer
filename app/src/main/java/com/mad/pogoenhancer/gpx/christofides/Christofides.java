package com.mad.pogoenhancer.gpx.christofides;


import com.mad.pogoenhancer.Logger;
import com.mad.shared.gpx.LatLon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class Christofides {

    private boolean verbose;

    /**
     * Constructor that sets verbose to the given value.
     *
     * @param verbose True or false depending on the users wish of seeing values that are processed.
     * @since 1.0
     */

    public Christofides(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * This is the method that starts the algorithm, and gives back the answer.
     *
     * @return The path of the travelling salesman.
     * @since 1.0
     */

//    public int[] solve(double[] x, double[] y){
    public List<LatLon> solve(List<LatLon> locations) {
        double[][] weightMatrix = buildWeightMatrix(locations);
        int[] mst = prim(weightMatrix);

        int[][] match = greadyMatch(mst, weightMatrix);

        GraphNode[] nodes = buildMultiGraph(match, mst);

        int[] route = getEulerCircuit(nodes);

        double sum = 0;

        for (int i = 1; i < route.length; i++) {
            sum += weightMatrix[route[i - 1]][route[i]];
        }
        sum += weightMatrix[route[0]][route[route.length - 1]];
        Logger.debug("PogoEnhancerJ","Summan: " + sum);

        // TODO: return route
        return buildRoute(route, locations);
    }

    private List<LatLon> buildRoute(int[] route, List<LatLon> locations) {
        List<LatLon> adjustedRoute = new ArrayList<>(route.length);
        for (int i = 0; i < route.length; i++) {
            adjustedRoute.add(i, locations.get(route[i]));
        }
        return adjustedRoute;
    }

    /**
     * Builds the union of MST and MATCH, which is a multi graph
     *
     * @param nodes Multigraph with only even degree nodes.
     * @return Euler circuit with shortcuts
     * @since 1.0
     */

    private int[] getEulerCircuit(GraphNode[] nodes) {
        LinkedList<Integer> path = new LinkedList<>();
        Vector<Integer> tmpPath = new Vector<>();
        int j = 0;

        //lägg in första cykeln i path, getNextChild går djupet först och retu
        nodes[0].getNextChild(nodes[0].getName(), tmpPath, true);
        path.addAll(0, tmpPath);

        //gå igenom alla noder i vår path, om noden har fler utgående kanter så kolla cykler efter denna. stopp in cykeln på rätt plats
        while (j < path.size()) {
            if (nodes[path.get(j)].hasMoreChilds()) {
                nodes[path.get(j)].getNextChild(nodes[path.get(j)].getName(), tmpPath, true);
                if (tmpPath.size() > 0) {
                    //sätt ihop path och tmpPath
                    for (int i = 0; i < path.size(); i++) {
                        if (path.get(i).intValue() == tmpPath.elementAt(0).intValue()) {
                            path.addAll(i, tmpPath);
                            break;
                        }
                    }
                    tmpPath.clear();
                }
                j = 0;
            } else j++;
        }

        //hitta genvägar på Euler-turen
        boolean[] inPath = new boolean[nodes.length];
        int[] route = new int[nodes.length];
        j = 0;
        for (int i = 0; i < path.size(); i++) {
            if (!inPath[path.get(i)]) {
                route[j] = path.get(i);
                j++;
                inPath[path.get(i)] = true;
            }
        }
        //if(j!=nodes.length) System.out.println("Warning! constructed route does not contain all nodes");

        return route;
    }

    /**
     * Builds the union of MST and MATCH, which is a multi graph
     *
     * @param match The "minimum" perfect match on the set of odd nodes.
     * @param mst   The minimal spanning tree
     * @return One dimensional nodes matrix representing the multi graph
     * @since 1.0
     */

    private GraphNode[] buildMultiGraph(int[][] match, int[] mst) {
        GraphNode[] nodes = new GraphNode[mst.length];
        //skapa tomma noder
        for (int i = 0; i < mst.length; i++) {
            nodes[i] = new GraphNode(i);
        }

        //lägg till noder och kanter från MST, symmetriska kanter!
        for (int i = 1; i < mst.length; i++) {
            nodes[i].addChild(nodes[mst[i]]);
            nodes[mst[i]].addChild(nodes[i]);
        }

        //lägg till noder och kanter från MATCHNING, symmetriska kanter!
        for (int[] ints : match) {
            nodes[ints[0]].addChild(nodes[ints[1]]);
            nodes[ints[1]].addChild(nodes[ints[0]]);
            if (verbose) System.out.println(ints[0] + "-" + ints[1]);
        }

        return nodes;
    }

    /**
     * Builds up the weightmatrix from the coordinates. Calculates distance between all pairs.
     *
     * @return Two-dimensional weightmatrix.
     * @since 1.0
     */

    private double[][] buildWeightMatrix(List<LatLon> locations) {
        int dim = locations.size();
        double[][] wt = new double[dim][dim];
        double dist;

        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                if (x == y) {
                    wt[x][y] = 0.0;
                    wt[y][x] = 0.0;
                    continue;
                }
                dist = locations.get(x).distance(locations.get(y));
                wt[x][y] = dist;
                wt[y][x] = dist;
            }
        }

        return wt;
    }

    /**
     * Using Prim's algorithm to find the Minimal Spanning Tree.
     *
     * @return The parentvector. p[i] gives the parent of node i.
     * @since 1.0
     */

    private int[] prim(double[][] weightMatrix) {
        int dim = weightMatrix.length;
        LinkedList<Integer> queue = new LinkedList<>();
        for (int i = 0; i < dim; i++) {
            queue.add(i);
        }

        // Prim's algorithm
        boolean[] isInTree = new boolean[dim];
        double[] key = new double[dim]; //avstånd från nod i och nod parent[i].
        int[] p = new int[dim]; //parent

        for (int i = 0; i < dim; i++) {
            key[i] = Integer.MAX_VALUE;
        }

        key[0] = 0; // root-node
        int u = 0;

        double temp;
        Integer elem;
        do {
            isInTree[u] = true; //lägg till noden i trädet
            Integer next = queue.poll();
            if (next == null) {
                break;
            }
            for (int v = 0; v < dim; v++) { // kan forenkles om det ikke er en komplett graf!
                if (!isInTree[v] && weightMatrix[u][v] < key[v]) {
                    p[v] = u;
                    key[v] = weightMatrix[u][v];
                }
            }

            // ExtractMin, går igenom alla kvarvarande noder och tar ut den med kortast avstånd till trädet
            double mint = Double.MAX_VALUE;
            for (int i = 0; i < queue.size(); i++) {
                elem = queue.get(i); //ineffektivt
                temp = key[elem];
                if (temp < mint) {
                    u = elem;
                    mint = temp;
                }
            }
        } while (!queue.isEmpty());

        if (verbose) {
            System.out.print("Key-vektor: ");
            for (int i = 0; i < dim; i++) {
                System.out.print(key[i] + " ");
            }
            System.out.print("\n\n");
            System.out.print("Parent:     ");
            for (int i = 0; i < dim; i++) {
                System.out.print(p[i] + " ");
            }
            System.out.print("\n");
            double sum = 0;
            for (int g = 0; g < dim; g++) {
                sum += key[g];
            }

            System.out.println("\n\n" + sum);
        }

        return p;
    }

    /**
     * Finds a match between the nodes that hava odd number of edges. Not perfect that gready, that is take the
     * shortest distance found first. Then the next shortest of the remaining i chosen.
     *
     * @param p   Parentvector. p[i] gives the parent of node i.
     * @param wt  Weightmatrix of the complete graph.
     * @return Twodimensional matrix containing the pairs. Two columns where each row represent a pair.
     * @since 1.0
     */

    private int[][] greadyMatch(int[] p, double[][] wt) {

        Node[] nodes = new Node[p.length];

        //skapa en skog
        nodes[0] = new Node(0, true); //roten
        for (int i = 1; i < p.length; i++) {
            nodes[i] = new Node(i, false);
        }

        //bygg ett träd av skogen
        for (int i = 0; i < p.length; i++) {
            if (p[i] != i)
                nodes[p[i]].addChild(nodes[i]);
        }

        //hitta udda noder
        ArrayList<Integer> oddDegreeNodes = findOddDegreeNodes(nodes[0]);
        int nOdd = oddDegreeNodes.size();

        if (verbose) {
            System.out.println("Udda noder:");
            for (int i = 0; i < nOdd; i++)
                System.out.print(oddDegreeNodes.get(i) + ", ");
            System.out.println();
        }

        //försök hitta en så minimal matchning som möjligt med en girig metod
        //sortera alla kanter mellan de udda hörnen
        Edge[][] edges = new Edge[nOdd][nOdd];
        for (int i = 0; i < nOdd; i++) {
            for (int j = 0; j < nOdd; j++) {
                if (oddDegreeNodes.get(i).intValue() != oddDegreeNodes.get(j).intValue())
                    edges[i][j] = new Edge(oddDegreeNodes.get(i),
                            oddDegreeNodes.get(j),
                            wt[oddDegreeNodes.get(i)][oddDegreeNodes.get(j)]);
                else
                    edges[i][j] = new Edge(oddDegreeNodes.get(i),
                            oddDegreeNodes.get(j), Double.MAX_VALUE);
            }
            Arrays.sort(edges[i]); //sortera alla kanter från nod i
        }

        boolean[] matched = new boolean[wt.length];
        int[][] match = new int[(nOdd / 2)][2];

        // för varje hörn plocka ut den kortaste kanten
        // vid krock välj den kortaste av de näst kortaste.
        // antalet noder med udda gradtal alltid delbart med 2
        int k = 0;
        for (int i = 0; i < nOdd; i++) {
            for (int j = 0; j < nOdd; j++) {
                if (!(matched[edges[i][j].getFrom()] || matched[edges[i][j].getTo()])) {
                    matched[edges[i][j].getFrom()] = true;
                    matched[edges[i][j].getTo()] = true;
                    match[k][0] = edges[i][j].getFrom();
                    match[k][1] = edges[i][j].getTo();
                    k++;
                }
            }
        }

        if (verbose) {
            System.out.println("Matchning");
            for (int i = 0; i < nOdd / 2; i++) {
                System.out.println(match[i][0] + "-" + match[i][1]);
            }
        }

        return match;
    }

    /**
     * Activates the treetraversing-routine that builds the path given by DFS.
     *
     * @param _root The root which is the start node of the route.
     * @return The route which is the order of nodes after the traversing.
     * @since 1.0
     */

    private ArrayList<Integer> buildRoute(Node _root) {
        ArrayList<Integer> route = new ArrayList<>();
        _root.visitBuildRoute(route);
        return route;
    }


    /**
     * Activates the routine that finds vertexes which have odd number of edges.
     *
     * @param _root Startnode.
     * @return List of nodes with odd number of edges.
     * @since 1.0
     */

    private ArrayList<Integer> findOddDegreeNodes(Node _root) {
        ArrayList<Integer> oddNodes = new ArrayList<>();
        _root.visitFindOddDegreeNodes(oddNodes);
        return oddNodes;
    }
}