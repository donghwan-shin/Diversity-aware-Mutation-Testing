package TestSuiteSelection;

import Core.TestSuite;
import IO.Writer;
import Matrix.InitMatrix;
import Matrix.MatrixObj;

/**
 * Generate pure random test suites, without control anything, for RQ3.
 */
public class UnControlThread implements Runnable {

    String workingDir;
    String p;
    String v;
    String n;
    String testPool;
    boolean printAll;
    int MAX_REP = 1000;

    public UnControlThread(String workingDir, String p, String v, String n, String testPool, boolean printAll) {
        this.workingDir = workingDir;
        this.p = p;
        this.v = v;
        this.n = n;
        this.testPool = testPool;
        this.printAll = printAll;
    }

    public void run() {
        MatrixObj matrix = InitMatrix.readMatrix(workingDir, p, v, n, testPool);

        String file = workingDir+"/testSuiteSelection_unControl/"+p+"/"+p+"."+v+"f."+n+"."+testPool+".unControl.csv";
//            if(IOUtil.isFile(file)) return; // TODO: override the existing file

        Writer w = new Writer(file);
        if(printAll)
            w.write("tScore,p,v,n,testPoolName,aT,mutantPool,%M,aM,eff_kM,eff_dM,eff_sM,eff_hM,criterion,kScore,dScore,sScore,size,isDetect,trigTests\n");
        else
            w.write("tScore,p,v,criterion,aM,kM,kScore,aT,size,isDetect,numTrigs\n");

        for(int rep = 0; rep < MAX_REP; rep++) {
            int tSize = (int) (Math.random() * matrix.totalTests)+1;
            TestSuite testSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, "all", 1, "r", null, tSize);
            TestSuiteSelection.printResult(w, matrix, testSuite, "r", null, printAll);
        }

        w.close();

        System.out.println(p+"-"+v+": done");
    }


}
