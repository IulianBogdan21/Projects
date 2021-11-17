package socialNetwork.utilitaries;

import java.util.*;

/**
 * Undirected Graph data structure
 * @param <T> - elements type of graph
 */
public class UndirectedGraph<T> {

    private Map<T, HashSet<T>> adjacencyMap = new HashMap<T, HashSet<T>>();
    enum NodeState{
        NOT_VISITED,
        VISITED,
        NEIGHBOURS_NOT_VISITED
    }
    private int verticesNumber = 0;
    private int edgesNumber = 0;

    /**
     * getter method for number of vertices
     * @return - int
     */
    public int getVerticesNumber(){
        return verticesNumber;
    }

    /**
     * getter method for number of edges
     * @return - int
     */
    public int getEdgesNumber(){
        return edgesNumber;
    }

    /**
     * default constructor
     */
    public UndirectedGraph() {}

    /**
     * constructor that creates a new graph with no edges and given vertices
     * @param vertices - list of vertices - nodes of graph
     */
    public UndirectedGraph(List<T> vertices){
        for(T vertex: vertices)
            addVertex(vertex);
    }

    /**
     * copy constructor for Graph class
     * @param other - undirected graph
     */
    public UndirectedGraph(UndirectedGraph<T> other){
        this.edgesNumber = other.edgesNumber;
        this.verticesNumber = other.verticesNumber;
        this.adjacencyMap = other.adjacencyMap;
    }

    /**
     * override equals method
     * @param o - Object instance
     * @return - boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UndirectedGraph<?> that = (UndirectedGraph<?>) o;
        return verticesNumber == that.verticesNumber &&
                edgesNumber == that.edgesNumber &&
                Objects.equals(adjacencyMap, that.adjacencyMap);
    }

    /**
     * override hashCode method
     * @return - int
     */
    @Override
    public int hashCode() {
        return Objects.hash(adjacencyMap, verticesNumber, edgesNumber);
    }

    /**
     * returns a set with neighbours of a specific vertex
     * @param vertex - node for which we find neighbours
     * @return - set or null if node does not exist
     */
    public Set<T> getNeighboursOf(T vertex){
        return adjacencyMap.get(vertex);
    }

    /**
     * adds a vertex to the graph
     * @param vertex - T generic type
     */
    public boolean addVertex(T vertex){
        if(adjacencyMap.containsKey(vertex))
            return false;
        adjacencyMap.put(vertex, new HashSet<>());
        verticesNumber++;
        return true;
    }

    /**
     * adds an edge to the undirected graph
     * @param vertex1 - T generic type
     * @param vertex2 -  T generic type
     * @return - true if edge was added, false otherwise
     */
    public boolean addEdge(T vertex1, T vertex2){
        if(vertex1.equals(vertex2))
            return false;
        if(hasEdge(vertex1, vertex2))
            return true;
        if(!hasVertex(vertex1))
            addVertex(vertex1);
        if(!hasVertex(vertex2))
            addVertex(vertex2);
        adjacencyMap.get(vertex1).add(vertex2);
        adjacencyMap.get(vertex2).add(vertex1);
        edgesNumber++;
        return true;
    }

    /**
     * checks if the given vertex exists
     * @param vertex - node
     * @return true if the vertex exists, false otherwise
     */
    public boolean hasVertex(T vertex){
        return adjacencyMap.containsKey(vertex);
    }

    /**
     * checks of graph is complete
     * @return - true if graph is complete, false otherwise
     */
    public boolean isGraphComplete(){
        return edgesNumber == verticesNumber * (verticesNumber - 1) / 2;
    }

    /**
     * returns all the edges from the graph
     * @return - a set with all edges in the graph
     */
    public Set<UnorderedPair<T, T>> getEdges(){
        Set<UnorderedPair<T, T>> allEdges = new HashSet<>();
        for(Map.Entry<T, HashSet<T>> entry: adjacencyMap.entrySet()){
            T vertex1 = entry.getKey();
            for(T vertex2: entry.getValue())
                allEdges.add(new UnorderedPair<>(vertex1, vertex2));
        }
        return allEdges;
    }

    /**
     * returns all the vertices of the graph
     * @return - List of vertices (generic type T)
     */
    public List<T> getVertices(){
        return adjacencyMap.keySet().stream().toList();
    }

    /**
     * @return - a map with all the vertices and their state(initially all nodes are not visited)
     */
    public Map<T, NodeState> createVisitedMap(){
        Map<T, NodeState> visitedMap = new HashMap<>();
        for(T node: getVertices())
            visitedMap.put(node, NodeState.NOT_VISITED);
        return visitedMap;
    }

    /**
     * verifies existence of edge between 2 vertices
     * @param vertex1 - vertex generic type
     * @param vertex2 - vertex generic type
     * @return - boolean - true if there is an edge, false otherwise
     */
    public boolean hasEdge(T vertex1, T vertex2){
        if(vertex1.equals(vertex2))
            return false;
        return adjacencyMap.containsKey(vertex1) &&
                adjacencyMap.get(vertex1).contains(vertex2) &&
                adjacencyMap.containsKey(vertex2) &&
                adjacencyMap.get(vertex2).contains(vertex1);
    }

    /**
     * runs BFS on graph
     * @param source - vertex T generic type
     * @param visitedMap - map that contains the state of a node
     * @return - list with all vertices of connected component
     */
    private List<T> runBFS(T source, Map<T, NodeState> visitedMap){
        visitedMap.put(source, NodeState.NEIGHBOURS_NOT_VISITED);
        Queue<T> nodeQueue = new ArrayDeque<>();
        nodeQueue.add(source);
        List<T> verticesOfConnectedComponent = new ArrayList<>();
        while (!nodeQueue.isEmpty()){
            T currentNode = nodeQueue.remove();
            verticesOfConnectedComponent.add(currentNode);
            if(visitedMap.get(currentNode) == NodeState.NEIGHBOURS_NOT_VISITED)
                for(T neighbour: adjacencyMap.get(currentNode))
                    if(visitedMap.get(neighbour) == NodeState.NOT_VISITED){
                        visitedMap.put(neighbour, NodeState.NEIGHBOURS_NOT_VISITED);
                        nodeQueue.add(neighbour);
                    }
            visitedMap.put(currentNode, NodeState.VISITED);
        }
        return verticesOfConnectedComponent;
    }

    /**
     * @return int - the number of connected components in the graph
     */
    public int findNumberOfConnectedComponents(){
        Map<T, NodeState> visitedMap = createVisitedMap();
        int numberOfConnectedComponents = 0;
        for(T node: getVertices()){
            if(visitedMap.get(node) == NodeState.NOT_VISITED){
                numberOfConnectedComponents++;
                runBFS(node, visitedMap);
            }
        }
        return numberOfConnectedComponents;
    }

    /**
     * returns all the connected components of a graph in a list
     * @return a list of graphs
     */
    public List<UndirectedGraph <T>> getConnectedComponents(){
        Map<T, NodeState> visitedMap = createVisitedMap();
        List<UndirectedGraph <T>> connectedComponents = new ArrayList<>();

        for(T node: getVertices())
            if(visitedMap.get(node) == NodeState.NOT_VISITED){
                List<T> vertices = runBFS(node, visitedMap);
                UndirectedGraph<T> component = new UndirectedGraph<>(vertices);
                for(T vertex: vertices)
                    for(T neighbour: adjacencyMap.get(vertex))
                        component.addEdge(vertex, neighbour);
                connectedComponents.add(component);
            }
        return connectedComponents;
    }

    /**
     * working with references of an integer
     */
    private static class IntRef{
        int value;
    }

    /**
     * recursive function to find the longest path in a component, given a source
     * @param source - generic type
     * @param visitedEdges - a set of edges that have been visited
     * @param currentPathSize - the length of the current path
     * @param maxSizePath - the maximum length of a path
     */
    private void findLongestPathRec(T source,
                                    Set<UnorderedPair<T, T>> visitedEdges,
                                    IntRef currentPathSize,
                                    IntRef maxSizePath){
        for(var neighbour: adjacencyMap.get(source)){
            var currentEdge = new UnorderedPair<>(source, neighbour);
            if(!visitedEdges.contains(currentEdge)){
                visitedEdges.add(currentEdge);
                int oldSize = currentPathSize.value;
                currentPathSize.value++;
                findLongestPathRec(neighbour,
                        visitedEdges,
                        currentPathSize,
                        maxSizePath);

                if(maxSizePath.value < currentPathSize.value)
                    maxSizePath.value = currentPathSize.value;
                visitedEdges.remove(currentEdge);
                currentPathSize.value = oldSize;
            }
        }
    }

    /**
     * finds the longest path in a graph
     * @return - int - the length of the longest path in a graph
     */
    public int findLongestPath(){
        if(verticesNumber >= 5 && isGraphComplete()){
            if(verticesNumber % 2 != 0)
                return verticesNumber * (verticesNumber - 1) / 2;
            else
                return verticesNumber * (verticesNumber - 2) / 2 + 1;
        }

        IntRef maxRef = new IntRef();
        maxRef.value = 0;
        IntRef pathSize = new IntRef();
        Set<UnorderedPair<T, T>> visitedEdges = new HashSet<>();
        for(var source: getVertices()){
            pathSize.value = 0;
            findLongestPathRec(source, visitedEdges, pathSize, maxRef);
            visitedEdges.clear();
        }
        return maxRef.value;
    }

    /**
     * goes through all components and returns the one with the longest path
     * @return - the component with the longest path
     */
    public UndirectedGraph<T> findConnectedComponentWithLongestPath(){
        int numberOfConnectedComponents = findNumberOfConnectedComponents();
        if(numberOfConnectedComponents == 0)
            return new UndirectedGraph<>();
        if(numberOfConnectedComponents == 1)
            return new UndirectedGraph<>(this);
        List<UndirectedGraph<T>> connectedComponents = getConnectedComponents();

        UndirectedGraph<T> maxComponent = connectedComponents.get(0);
        int maxPath = maxComponent.findLongestPath();
        for(int i = 1; i < connectedComponents.size(); i++){
            int path = connectedComponents.get(i).findLongestPath();
            if(maxPath < path){
                maxComponent = connectedComponents.get(i);
                maxPath = path;
            }
        }
        return maxComponent;
    }
}
