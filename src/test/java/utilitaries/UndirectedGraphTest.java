package utilitaries;


import config.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import socialNetwork.utilitaries.UndirectedGraph;
import socialNetwork.utilitaries.UnorderedPair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class UndirectedGraphTest {
    public UndirectedGraph<Integer> readGraphFromFile(String filePath){
        UndirectedGraph<Integer> g = new UndirectedGraph<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){

            String line;
            while((line = reader.readLine()) != null){
                String[] edgeStrStrAttr = line.split(" ");
                if(edgeStrStrAttr.length == 1)
                    g.addVertex(Integer.parseInt(edgeStrStrAttr[0]));
                else if(edgeStrStrAttr.length == 2)
                    g.addEdge(Integer.parseInt(edgeStrStrAttr[0]), Integer.parseInt(edgeStrStrAttr[1]));
            }
        } catch (IOException io){
            System.out.println(io.getMessage());
        }
        return g;
    }

    @Test
    void longestComponentOnEmptyGraph(){
        UndirectedGraph<Integer> g = new UndirectedGraph<>();
        UndirectedGraph<Integer> g1 = g.findConnectedComponentWithLongestPath();

        Assertions.assertEquals(0, g1.getVerticesNumber());
        Assertions.assertEquals(0, g1.getEdgesNumber());
        Assertions.assertEquals(0, g1.getEdges().size());
        Assertions.assertEquals(0, g1.getVertices().size());
    }

    @Test
    void constructorWithVerticesTest(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);

        Assertions.assertEquals(6, g.getVerticesNumber());
        Assertions.assertEquals(0, g.getEdgesNumber());
        List<Integer> actualVertices = g.getVertices();

        Assertions.assertEquals(vertices.size(), actualVertices.size());
        for(Integer actualVertex : actualVertices)
            Assertions.assertTrue(vertices.contains(actualVertex));
    }

    @Test
    void addEdgeWithEqualNodesTest(){
        UndirectedGraph<Integer> g = new UndirectedGraph<>();
        Assertions.assertFalse(g.addEdge(1, 1));
    }

    @Test
    void verticesAndEdgesNumTestWhenAddingSameEdgeMoreThanOnce(){
        UndirectedGraph<Integer> g = new UndirectedGraph<>();

        Assertions.assertEquals(0, g.getEdgesNumber());
        Assertions.assertEquals(0, g.getVerticesNumber());

        g.addEdge(1, 2);
        g.addEdge(1, 2);
        g.addEdge(1, 2);
        g.addEdge(1, 2);
        g.addEdge(1, 2);

        Assertions.assertEquals(1, g.getEdgesNumber());
        Assertions.assertEquals(2, g.getVerticesNumber());

        g.addEdge(2, 3);
        g.addEdge(2, 3);
        g.addEdge(3, 4);

        g.addVertex(4);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(5);
        g.addVertex(5);
        Assertions.assertEquals(3, g.getEdgesNumber());
        Assertions.assertEquals(5, g.getVerticesNumber());
    }

    @Test
    void addEdgeWithNonExistingNodesTest(){
        UndirectedGraph<Integer> g = new UndirectedGraph<>(Arrays.asList(1, 2, 3));

        Assertions.assertTrue(g.addEdge(5, 6));
        Assertions.assertTrue(g.addEdge(5, 7));
        Assertions.assertTrue(g.addEdge(8, 7));

        Set<UnorderedPair<Integer, Integer>> edges = g.getEdges();
        Assertions.assertEquals(3, edges.size());
        Assertions.assertEquals(7, g.getVerticesNumber());
        Assertions.assertTrue(edges.contains(new UnorderedPair<Integer, Integer>(5, 6)));
        Assertions.assertTrue(edges.contains(new UnorderedPair<Integer, Integer>(5, 7)));
        Assertions.assertTrue(edges.contains(new UnorderedPair<Integer, Integer>(7, 8)));

        List<Integer> vertices = g.getVertices();
        for(Integer vertex : Arrays.asList(1, 2, 3, 5, 6, 7, 8))
            Assertions.assertTrue(vertices.contains(vertex));
    }

    @Test
    void getNeighboursOfNodeReturnsNullWhenNodeDoesntExist(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);

        Assertions.assertNull(g.getNeighboursOf(1000));
    }

    @Test
    void getNeighboursWithNodeInGraph(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);

        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(1, 7);

        Set<Integer> neighboursOf1 = g.getNeighboursOf(1);
        Assertions.assertEquals(3, neighboursOf1.size());
        Assertions.assertTrue(neighboursOf1.containsAll(Arrays.asList(3 , 5, 7)));
        Assertions.assertEquals(0, g.getNeighboursOf(11).size());
    }

    @Test
    void addingExistingVertex(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);
        Assertions.assertFalse(g.addVertex(11));
    }

    @Test
    void addingNonExistingVertex(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);
        Assertions.assertTrue(g.addVertex(12));
        Assertions.assertTrue(g.getVertices().contains(12));
    }

    @Test
    void hasVertexTest(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);
        Assertions.assertTrue(g.hasVertex(11));
        Assertions.assertFalse(g.hasVertex(2000));
    }

    @Test
    void graphIsCompleteTest(){
        UndirectedGraph<Integer> g = new UndirectedGraph<>();

        int size = 30;
        for(int i = 1; i  < size; i++)
            for(int j = i + 1; j <= size; j++)
                g.addEdge(i, j);
        Assertions.assertTrue(g.isGraphComplete());
    }

    @Test
    void graphIsNotCompleteTest(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(1, 7);
        Assertions.assertFalse(g.isGraphComplete());
    }

    @Test
    void containsEdgeTest(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(1, 7);
        Assertions.assertTrue(g.hasEdge(5, 1));
    }

    @Test
    void graphDoesntContainEdgeTest(){
        List<Integer> vertices = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));
        UndirectedGraph<Integer> g = new UndirectedGraph<>(vertices);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(1, 7);
        Assertions.assertFalse(g.hasEdge(1000, 2000));
        Assertions.assertFalse(g.hasEdge(1000, 1000));
    }

    @Test
    void connectedComponentsTest(){
        UndirectedGraph<Integer> g = readGraphFromFile(ApplicationContext.getProperty("graph.connected_components.input"));
        UndirectedGraph<Integer> g1 = readGraphFromFile(ApplicationContext.getProperty("graph.connected_components.component1"));
        UndirectedGraph<Integer> g2 = readGraphFromFile(ApplicationContext.getProperty("graph.connected_components.component2"));
        UndirectedGraph<Integer> g3 = readGraphFromFile(ApplicationContext.getProperty("graph.connected_components.component3"));
        UndirectedGraph<Integer> g4 = readGraphFromFile(ApplicationContext.getProperty("graph.connected_components.component4"));

        Assertions.assertEquals(4, g.findNumberOfConnectedComponents());

        List<UndirectedGraph<Integer>> connectedComponents = g.getConnectedComponents();
        Assertions.assertEquals(4, connectedComponents.size());

        Assertions.assertTrue(connectedComponents.contains(g1));
        Assertions.assertTrue(connectedComponents.contains(g2));
        Assertions.assertTrue(connectedComponents.contains(g3));
        Assertions.assertTrue(connectedComponents.contains(g4));
    }

    @Test
    void connectedComponentWithLongestPathOnGraphWithNonCompleteComponentsTest(){
        UndirectedGraph<Integer> g = readGraphFromFile(ApplicationContext.getProperty("graph.longest_path.input"));
        Assertions.assertEquals(2, g.findNumberOfConnectedComponents());

        UndirectedGraph<Integer> compMax = readGraphFromFile(ApplicationContext.getProperty("graph.longest_path.component_max"));

        Assertions.assertEquals(compMax, g.findConnectedComponentWithLongestPath());
    }

    @Test
    void longestPathTest(){
        UndirectedGraph<Integer> g1 = readGraphFromFile(ApplicationContext.getProperty("graph.longest_path.component_max"));
        UndirectedGraph<Integer> g2 = readGraphFromFile(ApplicationContext.getProperty("graph.longest_path.component_min"));
        Assertions.assertEquals(15, g1.findLongestPath());
        Assertions.assertEquals(8, g2.findLongestPath());
    }

    @Test
    void connectedComponentWithLongestPathOnGraphWithOneComponentTest(){
        UndirectedGraph<Integer> g = readGraphFromFile(ApplicationContext.getProperty("graph.longest_path.component_max"));
        Assertions.assertEquals(g, g.findConnectedComponentWithLongestPath());
    }

    @Test
    void longestPathOnOddCompleteGraphTest(){
        UndirectedGraph<Integer> g = new UndirectedGraph<>();

        int size = 5;

        for(int i = 1; i < size; i++)
            for(int j = i + 1; j <= size; j++)
                g.addEdge(i, j);
        Assertions.assertTrue(g.isGraphComplete());
        Assertions.assertEquals(size * (size - 1) / 2, g.findLongestPath());
    }

    @Test
    void longestPathOnEvenCompleteGraphTest(){
        UndirectedGraph<Integer> g = new UndirectedGraph<>();

        int size = 20;

        for(int i = 1; i < size; i++)
            for(int j = i + 1; j <= size; j++)
                g.addEdge(i, j);
        Assertions.assertTrue(g.isGraphComplete());
        Assertions.assertEquals(size * (size - 2) / 2 + 1, g.findLongestPath());
    }
}
