package reed.solomon;

import java.util.Arrays;
/**
 * Reed-Solomon Code.
 * @author manolis kalafatis.
 */
public class ReedSolomon {
    public static GaloisField GF;
    public static int [] PRs;
    public static int m;
    public static int er;
    public static int k;
    public static int [][] CombArr;
    public static int TNOfComb=0;
    public static int NOfComb;
    public static int [] PrPol = {11, 19, 37};
    public static int CharStrt=47;
    public static int size;

    
   
    /**
     * Puts in CombArr all the k-combinations from elements of the s array. 
     * @param s Array with the elements that we want to make the k-combinations.
     * @param k Number of the elements of the combinations.
     */
    public  static void GetCombArr(int [] s, int k) { TNOfComb=0; GetCombArr(s, new int [0], k); }
    private static void GetCombArr(int [] s, int [] BegOfArr, int k) {
        if (k == 0) { CombArr[TNOfComb]=BegOfArr; TNOfComb++;}
        else {
            for (int i = 0; i < s.length; i++)
                GetCombArr(SubArr(s, i + 1), ArConcat(BegOfArr, s[i]), k-1);
        }
    }  

    /**
     * Number of possible Κ-combinations from N elements.
     * @param N Number of the elements.
     * @param K Number of the combination elements.
     * @return Number of possible K-combinations.
     */
    public static int BinCo(int N, int K){
        int result = 1;  
        for (int i=1; i<=Math.min(K,N-K);i++){
            result *= N-i+1;
            result /= i;
        }
        return result;
    }
    
    /**
     * Creates an array with the numbers 0,1,2,...,n-1.
     * @param n The size of the array.
     * @return Array with the numbers 0,1,2,...,n-1.
     */
    static int [] IntArr(int n){
        int [] ia = new int [n];
        for (int i=0; i<n; i++){
            ia[i]=i;
        }
        return ia;
    }
    
    /**
     * Returns an array with elements of s from the element with index BIndex.
     * @param s Array from which the function will return elements.
     * @param BIndex The index of the element s from which the function will begin to return elements.
     * @return Array with the last elements of s, beginning from BIndex.
     */
    static int [] SubArr(int [] s, int BIndex){
        int [] result = new int [s.length-BIndex];
        for (int i = BIndex; i<s.length; i++){
            result[i-BIndex] = s[i]; 
        }
        return result;
    }
    
    /**
     * Concatenates the arrays A and B.
     * @param A First array.
     * @param B Second array.
     * @return Concatenated array.
     */
    static int [] ArConcat(int [] A, int B) {
        int [] C= new int [A.length+1];
        System.arraycopy(A, 0, C, 0, A.length);
        C[A.length]=B;

        return C;
    }

    /**
     * Encodes the text.
     * @param txt The text we want to encode.
     * @return Encoded text.
     */
    public static int [] Encode(String txt){
        GF = GaloisField.getInstance(size,PrPol[m-3]);
        PRs = MakePRs();
        CombArr = new int [NOfComb][k];
        
        int [] es = TextToIntAr(txt);
        int [] et = new int [k];
        for (int i=0; i<k;i++){
            if (i<es.length){
                et[i] = es[i];
            }else{
                et[i] = 0;
            }
        }
        int [] entxt=new int [size];
        for (int i = 0;i<size;i++){
            entxt[i]=GF.substitute(et, PRs[i]);
        }
        return entxt;
    }
    
    /**
     * Decodes the text.
     * @param codeintar The text we want to decode.
     * @return Decoded text.
     */
    public static String Decode(int [] codeintar){
        
        int [] comb;
        int [] t1=new int[k];
        int [] t2=new int[k];
        solutions sol=new solutions();
        
        GetCombArr(IntArr(size), k);
        for (int i = 0; i<NOfComb; i++){
            comb=CombArr[i];
            System.arraycopy(ArrSel(PRs,comb),0,t1,0,k);
            System.arraycopy(ArrSel(codeintar,comb), 0, t2, 0, k);
            solveVandermondeSystem(t1, t2);
            sol.AddSol(t2);
        }
        sol.prints();
        return IntArToText(sol.GetBestSol());
    }
    
    /**
     * Converts the text to an array of integers. Each element of the array represents a letter.
     * @param text The text we want to convert.
     * @return Array of integers the represent letters of the text.
     */
    public static int [] TextToIntAr(String text){  
        int [] intarray = new int [text.length()];
        for (int i=0; i<text.length(); i++){
            intarray[i]=text.charAt(i)-CharStrt;
        }
        return intarray;
    }
    
    /**
     * Converts an array of integers to text. Each element of the array is converted to a letter.
     * @param IntAr Array of integers that represent letters.
     * @return Text that converted from the IntAr.
     */
    public static String IntArToText(int [] IntAr){
        String text="";
        char c;
        for (int i=0; i<IntAr.length; i++){
            c = (char) (IntAr[i]+CharStrt);
            text+=c;
        }
        return text;
    }
    
    /**
     * Converts alphanumeric to Array of integers.
     * @param text alphanumeric.
     * @return Array of integers.
     */
    public static int [] NumStrToIntAr(String text){ 
        int[] results = new int[text.length()];
        System.out.println(text);
        for (int i = 0; i < text.length(); i++) {
            System.out.print(text.charAt(i)-CharStrt+",");
            results[i] = text.charAt(i)-CharStrt;
            System.out.println(results[i]);
        }
        return results;
    }
    
    /**
     * Converts Array of integers to alphanumeric.
     * @param IntAr Array of integers.
     * @return alphanumeric.
     */
    public static String IntArToNumStr(int [] IntAr){ 
        String NumStr="";
        char c;
        for (int i=0;i<IntAr.length;i++){            
            c=(char)(IntAr[i]+CharStrt);
            NumStr+=c;
        }
        return NumStr;
    }

    /**
     * Creates the array of the primary roots.
     * @return Array of primary roots.
     */
    public static int[] MakePRs() {
        int [] PRsta = new int [size];
        for (int i=0; i<size; i++){
            PRsta[i]=GF.power(2, i);
        }
        return PRsta;
    }

    /**
     * Returns the rows of the codeintar with indexes from comb.
     * @param codeintar The array from which we want to take the rows.
     * @param comb Array with the indexes of the rows that we want to take.
     * @return Array with rows of codeintar.
     */
    private static int[] ArrSel(int[] codeintar, int[] comb) {
        int [] Sel=new int [comb.length];
        for (int i = 0; i<comb.length; i++){
            Sel[i]=codeintar[comb[i]];
        }
        return Sel;
    }

    /**
     * Solves the  Vandermonde system that described by t1 and t2. 
     * The root, after the procedure, will be in t2.
     * @param t1 Array that describes the system.
     * @param t2 Array that describes the system.
     */
    private static void solveVandermondeSystem(int[] t1, int[] t2) {
        int [][] varrey=new int [k][k];
        int [] ssol=new int [k];
        int l;
        for (int i=0; i<k; i++){
            for (int j=0; j<k; j++){
                varrey[i][j]=GF.power(t1[i], j);
            }
        }
        for (int i=0; i<k; i++){
            l=varrey[i][i];
            for (int j=0; j<k; j++){
                varrey[i][j]=GF.divide(varrey[i][j], l);
            }
            t2[i]=GF.divide(t2[i],l);
            for (int j=i+1; j<k; j++){
                l = varrey[j][i];
                for (int n=0; n<k; n++){
                    varrey[j][n]=varrey[j][n] ^ GF.multiply(varrey[i][n], l);
                }
                t2[j]=t2[j] ^ GF.multiply(t2[i], l);
            }
        }
        for (int i=k-1; i>0; i--){
            for (int j=i-1; j>=0; j--){
                l = varrey[j][i];
                varrey[j][i]= varrey[j][i] ^ GF.multiply(varrey[i][i], l);
                t2[j]=t2[j] ^ GF.multiply(t2[i],l);
            } 
        }
    }
}

    /** 
     * The class solutions is used to store the solutions of the equation systems 
     * in array and finds the most frequent solution.
     */
class solutions {
    public int [][] SolArr = new int [ReedSolomon.NOfComb][ReedSolomon.k];
    public int NOfSol;
    public int [] SolScores = new int [ReedSolomon.NOfComb];
    
    /**
     * Constructor. 
     */
    public solutions(){
        NOfSol=0;
    }
    
    /**
     * Checks if the solution exists, if not, adds it in the array.
     * @param sol The solution we want to add.
     */
    public void AddSol(int [] sol){
        int SolPos=SolExists(sol);
        if (SolPos==-1){
            AddNewSol(sol);
        }else{
            SolScores[SolPos]+=1;
        }
    }
    
    /**
     * Adds new solution. 
     * @param sol The solution we want to add.
     */
    private void AddNewSol(int [] sol){       
        System.arraycopy(sol, 0, SolArr[NOfSol], 0, ReedSolomon.k);
        SolScores[NOfSol]=1;
        NOfSol++;
    }

    /**
     * Check if the solution exists. 
     * @param sol The solution we want to add.
     * @return Position of the solution in the array or -1 if solution does not exist.
     */
    private int SolExists(int[] sol) {
        for (int i = 0; i<NOfSol; i++){
            if (Arrays.equals(sol, SolArr[i])){ 
            return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns the most frequent solution.
     * @return Most frequent solution.
     */
    public int [] GetBestSol(){
        int max = 0;
        for (int i = 0; i<NOfSol; i++ ){
            if (SolScores[i]>SolScores[max]) max = i;
        }
        return SolArr[max];
    }
    
    /**
     * Prints the array of the solutions.
     */
    public void prints(){
        for (int i=0;i<=NOfSol;i++){
            System.out.print(SolScores[i]+":");
            for (int j=0;j<ReedSolomon.k;j++){
                System.out.print(SolArr[i][j]+",");
            }
            System.out.println();
        }
    }
    
    
}
