package TestSuiteSelection;

import Core.TestSuite;
import IO.Writer;
import Matrix.InitMatrix;
import Matrix.MatrixObj;

/**
 * Use different Mutant Sampling Rates (MSP) for a specific test suite size
 */
public class MutantSamplingThread implements Runnable
{

    String workingDir;
    String p;
    String v;
    String n;
    String testPool;
    boolean printAll;
    int MAX_REP = 100;

    public MutantSamplingThread(String workingDir, String p, String v, String n, String testPool, boolean printAll) {
        this.workingDir = workingDir;
        this.p = p;
        this.v = v;
        this.n = n;
        this.testPool = testPool;
        this.printAll = printAll;
    }

    public void run() {
        MatrixObj matrix = InitMatrix.readMatrix(workingDir, p, v, n, testPool);

        String file = workingDir+"/mutantSampling/"+p+"/"+p+"."+v+"f."+n+"."+testPool+".mutantSampling.csv";
//            if(IOUtil.isFile(file)) return; // TODO: override the existing file

        Writer w = new Writer(file);
        w.write("tScore,p,v,n,testPoolName,aT,mutantPool,%M,aM,eff_kM,eff_dM,eff_sM,criterion,kScore,dScore,sScore,size,isDetect,trigTests\n");


        for(int s=1; s<=100; s++) {
            int tSize = 10; // (int) Math.round(matrix.totalTests * 0.1);
            for (double rate = 0.05; rate <= 0.25; rate += 0.05) {
                for (int rep = 0; rep < MAX_REP; rep++) {
                    TestSuite testSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, "random", rate, "r", null, tSize);
                    TestSuiteSelection.printResult(w, matrix, testSuite, "r", null, printAll);
                }
            }
        }
        w.close();

        System.out.println(p+"-"+v+"-"+n+": done");
    }

}
