package reed.solomon;

import java.util.Arrays;
/**
 * Κώδικας Reed-Solomon.
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
     * Εισάγει στον πίνακα CombArr όλους τους συνδυασμούς k-άδων απο στοιχεία 
     * που υπάρχουν στον πίνακα s.
     * @param s Πίνακας των στοιχείων απο τα οποία θέλουμε να σχηματιστούν οι k-άδες.
     * @param k Αριθμός των στοιχείων των συνδυασμών.
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
     * Επιστρέφει το πλήθος των δυνατών Κ-άδων απο N στοιχεία.
     * @param N Ο αριθμός όλων των στοιχείων.
     * @param K Ο αριθμός των στοιχείων των συνδυασμών.
     * @return Ο αριθμός των δυνατών συνδιασμών.
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
     * Δημιουργεί ενα πίνακα που περιέχει τους αριθμούς 0,1,2,...,n-1.
     * @param n Το μέγεθος του πίνακα.
     * @return Πίνακας με τους αριθμούς 0,1,2,...,n-1.
     */
    static int [] IntArr(int n){
        int [] ia = new int [n];
        for (int i=0; i<n; i++){
            ia[i]=i;
        }
        return ia;
    }
    
    /**
     * Επιστρέφει πίνακα με τα στοιχεία του s απο το στοιχείο στη θέση BIndex και μετά.
     * @param s Ο πίνακας απο τον οποίο θα αντιγραφούν τα στοιχεία.
     * @param BIndex Το σημείο του s απο το οποίο θα αντιγραφούν τα στοιχεία.
     * @return Ο πίνακας με τα τελευταία στοιχεία του s.
     */
    static int [] SubArr(int [] s, int BIndex){
        int [] result = new int [s.length-BIndex];
        for (int i = BIndex; i<s.length; i++){
            result[i-BIndex] = s[i]; 
        }
        return result;
    }
    
    /**
     * Ενώνει τους πίνακες A και B.
     * @param A Ο πρώτος πίνακας.
     * @param B Ο δεύτερος πίνακας.
     * @return Το αποτέλεσμα.
     */
    static int [] ArConcat(int [] A, int B) {
        int [] C= new int [A.length+1];
        System.arraycopy(A, 0, C, 0, A.length);
        C[A.length]=B;

        return C;
    }

    /**
     * Κωδικοποιεί το κείμενο.
     * @param txt Το κείμενο που θέλουμε να κωδικοποιήσουμε.
     * @return Κωδικοποιημένο κείμενο.
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
     * Αποκωδικοποιεί το κείμενο.
     * @param codeintar Το κείμενο που θέλουμε να αποκωδικοποιήσουμε.
     * @return Αποκωδικοποιημένο κείμενο.
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
     * Μετατρέπει κείμενο σε πίνακα με ακεραιους. Κάθε στοιχείο του πίνακα
     * αντιστοιχεί σε γράμμα του κειμένου.
     * @param text Το κείμενο που θέλουμε να μετατρέψουμε.
     * @return Πίνακα με ακεραίους που αντιστοιχούν στα γράμματα του κειμένου.
     */
    public static int [] TextToIntAr(String text){  
        int [] intarray = new int [text.length()];
        for (int i=0; i<text.length(); i++){
            intarray[i]=text.charAt(i)-CharStrt;
        }
        return intarray;
    }
    
    /**
     * Μετατρέπει πίνακα με ακεραίους σε κείμενο. Κάθε στοιχείο του πίνακα 
     * το μετατρέπει σε γράμμα.
     * @param IntAr Ο πίνακας που περιέχει τους αριθμούς που αντιστοιχούν
     *      στα γράμματα.
     * @return Κείμενο που δημιουργήθηκε απο το πίνακα.
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
     * Μετατροπή αλφαριθμητικού κώδικα σε πίνακα ακεραίων.
     * @param text Ο αλφαριθμητικός κώδικας.
     * @return Πίνακας ακεραίων.
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
     * Μετατροπή πίνακα ακεραίων σε αλφαριθμητικό κώδικα.
     * @param IntAr Ο πίνακας.
     * @return Αλφαριθμητικός κώδικας.
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
     * Δημιουργεί τον πίνακα των πρωταρχικών ριζών.
     * @return Πίνακας πρωταρχικών ριζών.
     */
    public static int[] MakePRs() {
        int [] PRsta = new int [size];
        for (int i=0; i<size; i++){
            PRsta[i]=GF.power(2, i);
        }
        return PRsta;
    }

    /**
     * Επιστρέφει επιλογή των γραμμών του πίνακα codeintar που ορίζονται απο τον comb.
     * @param codeintar Ο πίνακας απο τον οποίο θα πάρει τις γραμμές.
     * @param comb Ο πίνακας που περιέχει τις θέσεις των γραμμών που θέλουμε.
     * @return Πίνακας με γραμμές απο τον πίνακα codeintar.
     */
    private static int[] ArrSel(int[] codeintar, int[] comb) {
        int [] Sel=new int [comb.length];
        for (int i = 0; i<comb.length; i++){
            Sel[i]=codeintar[comb[i]];
        }
        return Sel;
    }

    /**
     * Λύνει το Vandermonde σύστημα που ορίζεται απο το t1 και το t2. 
     * Η λύση μετά την ολοκλήρωση, θα περιέχεται στο t2.
     * @param t1 Πίνακας που περιγράφει το σύστημα.
     * @param t2 Πίνακας που περιγράφει το σύστημα.
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
     * Η κλάση solutions χρησιμοποιείται για την καταχώρηση των λύσεων των
     * συστημάτων εξισώσεων σε πίνακα και την εύρεση της συχνότερης λύσης.
     */
class solutions {
    public int [][] SolArr = new int [ReedSolomon.NOfComb][ReedSolomon.k];
    public int NOfSol;
    public int [] SolScores = new int [ReedSolomon.NOfComb];
    
    /**
     * Αρχικοποίηση του αντικειμένου. 
     */
    public solutions(){
        NOfSol=0;
    }
    
    /**
     * Εισαγωγή λύσης. Ελέγχει πρώτα αν υπάρχει. Αν δεν υπάρχει την προσθέτει.
     * Αν υπάρχει αυξάνει τον μετρητή που της αντιστοιχεί.
     * @param sol Η εξίσωση που θέλουμε να εισάγουμε.
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
     * Εισαγωγή λύσης. 
     * @param sol Η εξίσωση που θέλουμε να εισάγουμε.
     */
    private void AddNewSol(int [] sol){       
        System.arraycopy(sol, 0, SolArr[NOfSol], 0, ReedSolomon.k);
        SolScores[NOfSol]=1;
        NOfSol++;
    }

    /**
     * Έλεγχος ύπαρξης λύσης. 
     * @param sol Η εξίσωση που θέλουμε να ελέγξουμε.
     * @return θέση της εξίσωσης ή -1 αν δεν υπάρχει στον πίνακα.
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
     * Επιστρέφει την συχνότερη λύση.
     * @return συχνότερη λύση.
     */
    public int [] GetBestSol(){
        int max = 0;
        for (int i = 0; i<NOfSol; i++ ){
            if (SolScores[i]>SolScores[max]) max = i;
        }
        return SolArr[max];
    }
    
    /**
     * Εκτυπώνει τον πίνακα των λύσεων μαζί με τις συχνότητες.
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
