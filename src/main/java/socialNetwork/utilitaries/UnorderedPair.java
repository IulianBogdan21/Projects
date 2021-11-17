package socialNetwork.utilitaries;

import java.util.Objects;

/**
 * Unordered pair container
 * @param <T1> - generic type for first element of pair
 * @param <T2> - generic type for second element of pair
 * Order is irrelevant
 */
public class UnorderedPair<T1, T2>{
    public T1 left;
    public T2 right;

    /**
     * constructor - creates a new pair
     * @param left  - T1 type first element
     * @param right - T2 type second element
     */
    public UnorderedPair(T1 left, T2 right) {
        this.left = left;
        this.right = right;
    }

    /**
     * copy constructor - creates a new pair equal to the given one (parameter)
     * @param other - pair to be copied
     */
    public UnorderedPair(UnorderedPair<T1, T2> other){
        this.left = other.left;
        this.right = other.right;
    }

    /**
     * overrides equals method - checks if this and obj are equal
     * @param obj - instance of Object class
     * @return - boolean - true if objects are equal, false otherwise
     * order does not matter
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if(!(obj instanceof UnorderedPair)) return false;
        UnorderedPair<?, ?> pair = (UnorderedPair<?, ?>)obj;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right) ||
                Objects.equals(left, pair.right) && Objects.equals(right, pair.left);
    }

    /**
     * overrides hashCode method
     * @return - int - hashCode of this
     */
    @Override
    public int hashCode() {
        int leftHash = Objects.hashCode(left);
        int rightHash = Objects.hashCode(right);
        int minimumHash = Math.min(leftHash, rightHash);
        int maximumHash = Math.max(leftHash, rightHash);
        return Objects.hash(minimumHash, maximumHash);
    }
}
