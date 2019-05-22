package TestSuiteSelection;

import Core.TestSuite;
import IO.Writer;
import Matrix.InitMatrix;
import Matrix.MatrixObj;

import java.math.BigDecimal;

/**
 * RQ1, RQ2
 */
public class ScoreControlThread implements Runnable {

    String workingDir;
    String p;
    String v;
    String n;
    String testPool;
    boolean printAll;

    public ScoreControlThread(String workingDir, String p, String v, String n, String testPool, boolean printAll) {
        this.workingDir = workingDir;
        this.p = p;
        this.v = v;
        this.n = n;
        this.testPool = testPool;
        this.printAll = printAll;
    }

    public void run() {

        String file = workingDir+"/testSuiteSelection_scoreControl/"+p+"/"+p+"."+v+"f."+n+"."+testPool+".scoreControl.csv";
//        if(IOUtil.isFile(file)) return; // TODO: override the existing file

        Writer w = new Writer(file);
        if(printAll)
            w.write("tScore,p,v,n,testPoolName,aT,mutantPool,%M,aM,eff_kM,eff_dM,eff_sM,eff_hM,criterion,kScore,dScore,sScore,size,isDetect,trigTests\n");
        else
            w.write("tScore,p,v,criterion,aM,kM,kScore,aT,size,isDetect,numTrigs\n");

        MatrixObj matrix = InitMatrix.readMatrix(workingDir, p, v, n, testPool);
        String[] mutantPools = new String[]{"all"}; // all, subsuming, hard, random

        BigDecimal delta = new BigDecimal("0.05");
        for (BigDecimal tScore = delta; tScore.compareTo(new BigDecimal("1")) <= 0; tScore = tScore.add(delta)) {
            for (String mutantPool : mutantPools) {
//                System.out.println(p+"-"+v+"-"+tScore);

                TestSuite kSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, mutantPool, 1,"k", tScore, 0);
                if(kSuite == null) continue;
                TestSuite dSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, mutantPool, 1,"d", tScore, 0);
                if(dSuite == null) continue;
                TestSuite ksSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, mutantPool, 1,"ks", tScore, dSuite.size());
                if(ksSuite == null) continue;
                TestSuite rSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, mutantPool, 1,"r", tScore, dSuite.size());
                if(rSuite == null) continue;

                int count = 0;
                while(count < 100) {
                    while((kSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, mutantPool, 1,"k", tScore, 0)) == null) {}

                    while((dSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, mutantPool, 1,"d", tScore, 0)) == null
                            || (ksSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, mutantPool, 1,"ks", tScore, dSuite.size())) == null) {}

                    while((rSuite = TestSuiteSelection.tryToGenerateTestSuite(matrix, mutantPool, 1,"r", tScore, dSuite.size())) == null) {}

                    TestSuiteSelection.printResult(w, matrix, kSuite, "k", tScore, printAll);
                    TestSuiteSelection.printResult(w, matrix, dSuite, "d", tScore, printAll);
                    TestSuiteSelection.printResult(w, matrix, ksSuite, "ks", tScore, printAll);
                    TestSuiteSelection.printResult(w, matrix, rSuite, "r", tScore, printAll);
                    count++;
                }
            }
        }

        w.close();
        System.out.println(p+"-"+v+": done");

    }

}
