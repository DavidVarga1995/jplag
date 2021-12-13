package jplag.clustering;

public class Cluster implements Comparable<Cluster> {
    private final float similarity; // similarity threshold for this cluster

    private final int submissionNr;
    // !=-1 -> this cluster consists of one document only

    private final Cluster left;
    private final Cluster right;

    private int[] allSubmissions = null;
    private final Clusters clusters;// Warum habe ich nicht instanziert? ist es
                                // schon woanders instanziert? Emeric
    public int x = -1, y = -1; // Coordinates for the dendrogram

    public Cluster(int submissionNr, Clusters clusters) {
        this.clusters = clusters;
        this.submissionNr = submissionNr;
        left = right = null;
        similarity = 100;
  }

    // changed by Emeric Kwemou now, a cluster muss belong to a specific Objekt
    // of Type Clusters ->Clusters object added in Constructor

    public Cluster(Cluster left, Cluster right, float similarity,
            Clusters clusters) {
        this.clusters = clusters;
        this.submissionNr = -1;
        this.left = left;
        this.right = right;
        this.similarity = similarity;

        calculateAllSubmissions();
    }

    public final int size() {
        return (submissionNr == -1 ? allSubmissions.length : 1);
    }

    public final int getSubmissionAt(int i) {
        return (submissionNr == -1 ? allSubmissions[i] : submissionNr);
    }

    public final float getSimilarity() {
        return similarity;
    }

    public final Cluster getLeft() {
        return left;
    }

    public final Cluster getRight() {
        return right;
    }

    private void calculateAllSubmissions() {
        if(submissionNr == -1) {
            int rSize = right.size();
            int lSize = left.size();
            allSubmissions = new int[lSize + rSize];

            if(left.allSubmissions != null)
                System.arraycopy(left.allSubmissions, 0, allSubmissions, 0,
                    lSize);
            else
                allSubmissions[0] = left.submissionNr;

            if(right.allSubmissions != null)
                System.arraycopy(right.allSubmissions, 0, allSubmissions,
                    lSize, rSize);
            else
                allSubmissions[lSize] = right.submissionNr;

            // int i, li, ri;
            // i = ri = li = 0;

            // insert-sort

        } else
            allSubmissions = null;
    }

    // for MIN clustering
    public final float maxSimilarity(Cluster other, SimilarityMatrix simMatrix) {
        int sizeThis = size();
        int sizeOther = other.size();
        float maxSim = -1;

        for(int a = 0; a < sizeThis; a++)
            for(int b = 0; b < sizeOther; b++) {
                float sim = simMatrix.getSimilarity(this.getSubmissionAt(a),
                    other.getSubmissionAt(b));
                if(sim > maxSim)
                    maxSim = sim;
            }

        return maxSim;
    }

    // for MAX clustering
    public final float minSimilarity(Cluster other, SimilarityMatrix simMatrix) {
        int sizeThis = size();
        int sizeOther = other.size();
        float minSim = 100;

        for(int a = 0; a < sizeThis; a++)
            for(int b = 0; b < sizeOther; b++) {
                float sim = simMatrix.getSimilarity(this.getSubmissionAt(a),
                    other.getSubmissionAt(b));
                if(sim < minSim)
                    minSim = sim;
            }

        return minSim;
    }

    // for AVR clustering
    public final float avrSimilarity(Cluster other, SimilarityMatrix simMatrix) {
        int sizeThis = size();
        int sizeOther = other.size();
        float summedSim = 0;

        for(int a = 0; a < sizeThis; a++)
            for(int b = 0; b < sizeOther; b++)
                summedSim += simMatrix.getSimilarity(this.getSubmissionAt(a),
                    other.getSubmissionAt(b));
        return summedSim / (float) (sizeThis * sizeOther);
    }

    public final String toString() {
        StringBuilder text = new StringBuilder((submissionNr == -1 ? "Similarity: " + similarity + ":"
                : "Submission:"));
        for(int i = 0; i < size(); i++)
            text.append(" ").append(clusters.getSubmissions().get(getSubmissionAt(i)).getName());
        text.append("\n\n");
        if(left != null)
            text.append(left);
        if(right != null)
            text.append(right);
        return text.toString();
    }

    public final int compareTo(Cluster cluster2) {
        if(equals(cluster2))
            return 0;

        int s1Size = size();
        int s2Size = cluster2.size();

        if(s1Size != s2Size)
            return (s1Size < s2Size ? +1 : -1);

        int size = 0;
        for(int i = 0; i < s1Size; i++) {
            String name1 = clusters.getSubmissions().get(getSubmissionAt(i)).getName();
            String name2 = clusters.getSubmissions().get(cluster2.getSubmissionAt(i)).getName();
            if(name1.compareTo(name2) != 0)
                return name1.compareTo(name2);
            size += name1.length();
            size -= name2.length();
        }

        return (size >= 0 ? 1 : -1);
    }
}
