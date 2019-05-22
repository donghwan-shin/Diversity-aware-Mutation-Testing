package Matrix;

import IO.Reader;
import IO.Writer;

import java.util.ArrayList;

public class MatrixInfo {


    /**
     * Write [generatedMatrices_testPool.csv] file.
     * @param args
     */
    public static void main(String[] args) {

        String n = "big";
        String testPool = "randoop";

        String workingDir = "resources";
        Writer w = new Writer(workingDir+"/matrices/generatedMatrices_"+testPool+".csv");
        w.write("p,v,n,pool,aT,aM,kM,dM,sM,hM,trigTests\n");


        for(int i=1; i<=26; i++)
            getMatrixInfoAndWrite(workingDir, w, "Chart", i+"", n, testPool);

        for(int i=1; i<=133; i++)
            getMatrixInfoAndWrite(workingDir, w, "Closure", i+"", n, testPool);

        for(int i=1; i<=65; i++)
            getMatrixInfoAndWrite(workingDir, w, "Lang", i+"", n, testPool);

        for(int i=1; i<=106; i++)
            getMatrixInfoAndWrite(workingDir, w, "Math", i+"", n, testPool);

        for(int i=1; i<=27; i++)
            getMatrixInfoAndWrite(workingDir, w, "Time", i+"", n, testPool);

        w.close();

    }

    private static void getMatrixInfoAndWrite(String workingDir, Writer w, String p, String v, String n, String testPool) {
        System.out.println("getMatrixInfoAndWrite(): "+p+"-"+v);
        MatrixObj matrix = InitMatrix.readMatrix(workingDir, p, v, n, testPool);
        if(matrix == null) {
            System.err.println("getMatrixInfoAndWrite(): SKIP "+p+"-"+v);
            return;
        }
        int aT = matrix.totalTests;
        int aM = matrix.totalMutants;
        int kM = matrix.kMutants;
        int dM = matrix.dMutants;
        int sM = matrix.sMutants;
        int hM = matrix.hMutants;
        int trigTests = matrix.faultDetectionNames.size();

        w.write(p+","+v+","+n+","+testPool+","+aT+","+aM+","+kM+","+dM+","+sM+","+hM+","+trigTests+"\n");
    }

    /**
     * Read a list of matrices from the @file.
     * Skip useless matrices (i.e., totalTests=0 || totalMutants=0 || trigTests=0).
     * Additionally, if (skipH2KZero == true) then skip when hard-to-kill=0.
     * Generate a list of matrices, form [p,v,n,pool].
     *
     * @param file
     * @param skipH2KZero
     * @return
     */
    public static ArrayList<String> readMatrixInfo(String file, boolean skipH2KZero) {
        ArrayList<String> result = new ArrayList<>();

        Reader info = new Reader(file);
        String line;
        while((line = info.readLine()) != null) {
            // Empty line skip, header skip
            if (line.trim().isEmpty() || line.contains("p,v")) continue;

            // Split tokens for get p,v,n,pool information
            if(line.contains("p,v,")) continue;
            String[] tokens = line.split(",");
            String p = tokens[0];
            String v = tokens[1];
            String n = tokens[2];
            String pool = tokens[3];
            int aT = Integer.parseInt(tokens[4]); // total tests
            int kM = Integer.parseInt(tokens[6]); // total killable mutants (w.r.t. all tests)
            int hM = Integer.parseInt(tokens[9]); // total hard-to-kill mutants (i.e., killed by at most 10% of the all tests)
            int trigTests = Integer.parseInt(tokens[10]);

            if(aT==0 || kM==0 || trigTests==0) continue;
            if(skipH2KZero && hM==0) continue;

            result.add(p+","+v+","+n+","+pool);
        }
        info.close();

        return result;
    }

}
