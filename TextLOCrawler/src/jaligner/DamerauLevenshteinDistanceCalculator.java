package jaligner;

/**
* The Damerau-Levenshtein Algorithm is an extension to the Levenshtein
* Algorithm which solves the edit distance problem between a source string and
* a target string with the following operations:
*
* <ul>
* <li>Character Insertion</li>
* <li>Character Deletion</li>
* <li>Character Replacement</li>
* <li>Adjacent Character Swap</li>
* </ul>
*
* Note that the adjacent character swap operation is an edit that may be
* applied when two adjacent characters in the source string match two adjacent
* characters in the target string, but in reverse order, rather than a general
* allowance for adjacent character swaps.
* <p>
*
* This implementation allows the client to specify the costs of the various
* edit operations with the restriction that the cost of two swap operations
* must not be less than the cost of a delete operation followed by an insert
* operation. This restriction is required to preclude two swaps involving the
* same character being required for optimality which, in turn, enables a fast
* dynamic programming solution.
* <p>
*
* The running time of the Damerau-Levenshtein algorithm is O(n*m) where n is
* the length of the source string and m is the length of the target string.
* This implementation consumes O(n*m) space.
*
* @author Kevin L. Stern
*/
public class DamerauLevenshteinDistanceCalculator {
	 private String compOne;
	    private String compTwo;
	    private int[][] matrix;
	    private Boolean calculated = false;
	 
	    public DamerauLevenshteinDistanceCalculator(String a, String b)
	    {
	        if ((a.length() > 0 || !a.isEmpty())  || (b.length() > 0 || !b.isEmpty()))
	        {
	            compOne = a;
	            compTwo = b;
	        }       
	    }
	 
	    public int[][] getMatrix()
	    {
	        setupMatrix();
	        return matrix;
	    }
	 
	    /*
	     * Optomal string global alignemnt
	     * */
	    public int getOSASimilarity()
	    {
	        if (!calculated) setupMatrix();
	 
	        return matrix[compOne.length()][compTwo.length()];
	    }
	    public float getOSASimilarityMetric()
	    {
	        if (!calculated) setupMatrix();
	 
	        return (float)matrix[compOne.length()][compTwo.length()]/(float)(Math.max(compOne.length(), compTwo.length()));
	    }
	 
	    public int getDHSimilarity()
	    {
	            int INF = compOne.length() + compTwo.length();
	     
	            matrix = new int[compOne.length()+1][compTwo.length()+1];
	     
	            for (int i = 0; i < compOne.length(); i++)
	            {
	                matrix[i+1][1] = i;
	                matrix[i+1][0] = INF;
	            }
	     
	            for (int i = 0; i < compTwo.length(); i++)
	            {
	                matrix[1][i+1] = i;
	                matrix[0][i+1] = INF;
	            }
	     
	            int[] DA = new int[24];
	     
	            for (int i = 0; i < 24; i++)
	            {
	                DA[i] = 0;
	            }
	     
	            for (int i = 1; i < compOne.length(); i++)
	            {
	                int db = 0;
	     
	                for (int j = 1; j < compTwo.length(); j++)
	                {
	     
	                    int i1 = DA[compTwo.indexOf(compTwo.charAt(j-1))];
	                    int j1 = db;
	                    int d = ((compOne.charAt(i-1)==compTwo.charAt(j-1))?0:1);
	                    if (d == 0) db = j;
	     
	                    matrix[i+1][j+1] = Math.min(Math.min(matrix[i][j]+d, matrix[i+1][j]+1),Math.min(matrix[i][j+1]+1,matrix[i1][j1]+(i - i1-1)+1+(j-j1-1)));
	                }
	                DA[compOne.indexOf(compOne.charAt(i-1))] = i;
	            }
	             
	            return matrix[compOne.length()][compTwo.length()];
	    }
	    
	    private void setupMatrix()
	    {
	        int cost = -1;
	        int del, sub, ins;
	         
	        matrix = new int[compOne.length()+1][compTwo.length()+1];
	 
	        for (int i = 0; i <= compOne.length(); i++)
	        {
	            matrix[i][0] = i;
	        }
	 
	        for (int i = 0; i <= compTwo.length(); i++)
	        {
	            matrix[0][i] = i;
	        }
	 
	        for (int i = 1; i <= compOne.length(); i++)
	        {
	            for (int j = 1; j <= compTwo.length(); j++)
	            {
	                if (compOne.charAt(i-1) == compTwo.charAt(j-1))
	                {
	                    cost = 0;
	                }
	                else
	                {
	                    cost = 1;
	                }
	 
	                del = matrix[i-1][j]+1;
	                ins = matrix[i][j-1]+1;
	                sub = matrix[i-1][j-1]+cost;
	 
	                matrix[i][j] = minimum(del,ins,sub);
	 
	                if ((i > 1) && (j > 1) && (compOne.charAt(i-1) == compTwo.charAt(j-2)) && (compOne.charAt(i-2) == compTwo.charAt(j-1)))
	                {
	                    matrix[i][j] = minimum(matrix[i][j], matrix[i-2][j-2]+cost);
	                }
	            }
	        }
	 
	        calculated = true;
	        displayMatrix();
	    }
	     
	    private void displayMatrix()
	    {
	        System.out.println("  "+compOne);
	        for (int y = 0; y <= compTwo.length(); y++)
	        {
	            if (y-1 < 0) System.out.print(" "); else System.out.print(compTwo.charAt(y-1));
	            for (int x = 0; x <= compOne.length(); x++)
	            {
	                System.out.print(matrix[x][y]);
	            }
	            System.out.println();
	        }
	    }
	 
	    private int minimum(int d, int i, int s)
	    {
	        int m = Integer.MAX_VALUE;
	 
	        if (d < m) m = d;
	        if (i < m) m = i;
	        if (s < m) m = s;
	 
	        return m;
	    }
	 
	    private int minimum(int d, int t)
	    {
	        int m = Integer.MAX_VALUE;
	 
	        if (d < m) m = d;
	        if (t < m) m = t;
	 
	        return m;
	    }
}