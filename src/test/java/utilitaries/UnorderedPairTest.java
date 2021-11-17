package utilitaries;

import socialNetwork.utilitaries.UnorderedPair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnorderedPairTest {
    @Test
    void constructorTest(){
        UnorderedPair<Long, String> exampleUnorderedPair= new UnorderedPair<>(1L, "Mary");
        assertEquals(1L, exampleUnorderedPair.left);
        assertEquals("Mary", exampleUnorderedPair.right);
    }

    @Test
    void equalsAndHashCodeTest(){
        UnorderedPair<Long, String> firstExample = new UnorderedPair<>(1L,"Christian");
        UnorderedPair<String, Long> secondExample = new UnorderedPair<>("Christian", 1L);
        assertEquals(firstExample, secondExample);
        assertEquals(firstExample.hashCode(), secondExample.hashCode());
    }
}