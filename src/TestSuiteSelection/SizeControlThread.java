package TestSuiteSelection;

import Core.TestSuite;
import IO.IOUtil;
import IO.Writer;
import Matrix.InitMatrix;
import Matrix.MatrixObj;

/**
 * Size control experiments for the investigation with Mike
 */
public class SizeControlThread implements Runnable {

    String workingDir;
    String p;
    String v;
    String n;
    String testPool;
    boolean printAll;
    int MAX_REP = 1000;

    public SizeControlThread(String workingDir, String p, String v, String n, String testPool, boolean printAll) {
        this.workingDir = workingDir;
        this.p = p;
        this.v = v;
        this.n = n;
        this.testPool = testPool;
        this.printAll = printAll;
    }

    public void run() {
        MatrixObj matrix = InitMatrix.readMatrix(workingDir, p, v, n, testPool);

        String file = workingDir+"/testSuiteSelection_sizeControl/"+p+"/"+p+"."+v+"f."+n+"."+testPool+".sizeControl.csv";
//        if(IOUtil.isFile(file)) {
//            System.out.println(p+"-"+v+"-"+n+": skip");
//            return; // TODO: override the existing file
//        }

        Writer w = new Writer(file);
        if(printAll)
            w.write("tScore,p,v,n,testPoolName,aT,mutantPool,%M,aM,eff_kM,eff_dM,eff_sM,eff_hM,criterion,kScore,dScore,sScore,size,isDetect,trigTests\n");
        else
            w.write("tScore,p,v,criterion,aM,kM,kScore,aT,size,isDetect,numTrigs\n");

        if (matrix.totalTests >= 40) {
            for (int i = 1; i <= 20; i++) {
                int tSize = (int) Math.round(matrix.totalTests * i * 0.025);
//                if(tSize < 1) tSize = 1;
                for (int rep = 0; rep < MAX_REP; rep++) {
                    TestSuite testSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, "all", 1, "r", null, tSize);
                    TestSuiteSelection.printResult(w, matrix, testSuite, "r", null, printAll);
                }
            }
        } else {
            for (int tSize = 1; tSize <= matrix.totalTests / 2; tSize++) {
                for (int rep = 0; rep < MAX_REP; rep++) {
                    TestSuite testSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, "all", 1, "r", null, tSize);
                    TestSuiteSelection.printResult(w, matrix, testSuite, "r", null, printAll);
                }
            }
        }

        w.close();

        System.out.println(p+"-"+v+"-"+n+": done");
    }


}
