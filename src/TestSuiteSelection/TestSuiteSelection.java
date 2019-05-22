package TestSuiteSelection;

import Core.TestSuite;
import IO.Writer;
import Matrix.MatrixObj;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Test suite selection algorithms.
 * greedy() is for an additional study.
 */
public class TestSuiteSelection {

    static int MAX_TRY = 100;

    public static TestSuite tryToGenerateTestSuite(MatrixObj matrix, String mutantPool, double samplingRate, String crt, BigDecimal tScore, int tSize) {
        for(int i=0; i<MAX_TRY; i++) {
            TestSuite testSuite = generateTestSuite(matrix, mutantPool, samplingRate, crt, tScore, tSize);
            if(testSuite != null) {
                return testSuite;
            }
        }
        return null;
    }

    public static TestSuite tryToGenerateTestSuite(MatrixObj matrix, ArrayList<Integer> mIDs, String crt, BigDecimal tScore, int tSize) {
        for(int i=0; i<MAX_TRY; i++) {
            TestSuite testSuite = generateTestSuite(matrix, mIDs, crt, tScore, tSize);
            if(testSuite != null) {
                return testSuite;
            }
        }
        return null;
    }


    /**
     * Generate a test suite with additional-greedy algorithm.
     * It iteratively selects a test that maximizes the additional number of [killed/distinguished] mutants.
     * @param matrix
     * @param mutantPool
     * @param criterion
     * @param tSize
     * @return
     */
    public static TestSuite greedy(MatrixObj matrix, String mutantPool, double samplingRate, String criterion, int tSize) {
        TestSuite testSuite = new TestSuite(matrix, mutantPool, samplingRate);
        ArrayList<String> testPool = (ArrayList<String>) matrix.allTests.clone();
        Collections.shuffle(testPool);

        if(criterion.equals("r")) {
            for(String test: testPool) {
                if(testSuite.size() == tSize) break;
                testSuite.addTest(test);
            }
        } else if(criterion.equals("d")) {
            while(testSuite.dScore().compareTo(new BigDecimal("1")) < 0 && testPool.size() > 0) {
                int maxAddDist = 0;
                String selectedTest = null;
                for(String test: testPool) {
                    int distMore = testSuite.distMore(test);
                    if(distMore > maxAddDist) {
                        maxAddDist = distMore;
                        selectedTest = test;
                    }
                }
                testSuite.addTest(selectedTest);
                testPool.remove(selectedTest);
            }
        } else if(criterion.equals("k") || criterion.equals("ks")) {
            while(testSuite.kScore().compareTo(new BigDecimal("1")) < 0 && testPool.size() > 0) {
                int maxAddKill = 0;
                String selectedTest = null;
                for(String test: testPool) {
                    int killMore = testSuite.killMore(test);
                    if(killMore > maxAddKill) {
                        maxAddKill = killMore;
                        selectedTest = test;
                    }
                }
                testSuite.addTest(selectedTest);
                testPool.remove(selectedTest);
            }
        }

        /**
         * When [criterion == ks], stack k-suites until [ks-suite's size == tSize].
         */
        if(criterion.equals("ks") && testSuite.size() < tSize) {
            TestSuite testSuite_tmp = new TestSuite(matrix, mutantPool, samplingRate);
            ArrayList<String> testPool_tmp = (ArrayList<String>) matrix.allTests.clone();
            while(testSuite.size() < tSize && testPool_tmp.size() > 0) {
                int maxAddKill = 0;
                String selectedTest = null;
                for(String test: testPool_tmp) {
                    int killMore = testSuite_tmp.killMore(test);
                    if(killMore > maxAddKill) {
                        maxAddKill = killMore;
                        selectedTest = test;
                    }
                }
                testSuite_tmp.addTest(selectedTest);
                testPool_tmp.remove(selectedTest);
                testSuite.addTest(selectedTest);
            }
        }

        return testSuite;
    }


    public static void printResult(Writer w, MatrixObj matrix, TestSuite testSuite, String criterion, BigDecimal tScore, boolean printAll) {
        String str;

        if(printAll) {
            str = tScore+","+
                    matrix.p+","+
                    matrix.v+","+
                    matrix.n+","+
                    matrix.testPoolName +","+
                    matrix.totalTests+","+
                    testSuite.getMutantPool()+","+
                    testSuite.getSamplingRate()+","+
                    matrix.totalMutants+","+
                    matrix.kMutants+","+
                    matrix.dMutants+","+
                    matrix.sMutants+","+
                    matrix.hMutants+","+
                    criterion+","+
                    testSuite.kScore()+","+
                    testSuite.dScore()+","+
                    testSuite.sScore()+","+
                    testSuite.size()+","+
                    testSuite.isDetect()+","+
                    testSuite.trigTests().size()+"\n";
        } else {
            str = tScore+","+
                    matrix.p+","+
                    matrix.v+","+
                    criterion+","+
                    matrix.totalMutants+","+
                    matrix.kMutants+","+
                    testSuite.kScore()+","+
                    matrix.totalTests+","+
                    testSuite.size()+","+
                    testSuite.isDetect()+","+
                    testSuite.trigTests().size()+"\n";
        }


        w.write(str);
//        System.out.println(str);
    }


    public static void printDynTestSuiteResult(Writer w, MatrixObj matrix, int percent, int baseTSSize, int sMIDs, TestSuite testSuite, String crt, BigDecimal tScore, double kScore, double dScore) {
        String str = matrix.p+","
                +matrix.v+","
                +matrix.n+","
                +matrix.testPoolName+","
                +matrix.totalTests+","
                +percent+","
                +baseTSSize+","
                +matrix.totalMutants+","
                +matrix.kMutants+","
                +matrix.dMutants+","
                +matrix.sMutants+","
                +sMIDs+","
                +crt+","
                +tScore+","
                +kScore+","
                +dScore+","
                +testSuite.size()+","
                +testSuite.isDetect()+","
                +testSuite.trigTests().size()+"\n";
        w.write(str);
    }

    public static void printDynTestSuiteResult(Writer w, MatrixObj matrix, int percent, int baseTSSize, ArrayList<Integer> mIDs, ArrayList<Integer> dynamicallySubsumingMIDs, ArrayList<Integer> dyn_cMIDs, String criterion, BigDecimal tScore, TestSuite mut_ade_ts) {
        String str = matrix.p+","
                +matrix.v+","
                +matrix.n+","
                +matrix.testPoolName+","
                +matrix.totalTests+","
                +percent+","
                +baseTSSize+","
                +matrix.totalMutants+","
                +matrix.kMutants+","
                +matrix.dMutants+","
                +matrix.sMutants+","
                +matrix.coupledMIDs.size()+","
                +dynamicallySubsumingMIDs.size()+","
                +dyn_cMIDs.size()+","
                +criterion+","
                +tScore+","
                +(double)matrix.getKilledMIDs(mut_ade_ts.getTestSuite(), mIDs).size() / matrix.kMutants+","
                +(double)matrix.getDistinguishedMIDs(mut_ade_ts.getTestSuite(), mIDs).size() / matrix.dMutants+","
                +mut_ade_ts.size()+","
                +mut_ade_ts.isDetect()+","
                +mut_ade_ts.trigTests().size()+"\n";
        w.write(str);
    }



    /**
     * ------------------------------------------------------------------------------------
     * Private functions
     * ------------------------------------------------------------------------------------
     */


    /**
     * Generate a test suite satisfying the criterion with [tScore] and/or [tSize].
     * May return 'null' if it failed to generate a test suite for [tScore, tScore+0.05].
     * @param matrix
     * @param mutantPool
     * @param criterion
     * @param tScore
     * @param tSize
     * @return
     */
    private static TestSuite generateTestSuite(MatrixObj matrix, String mutantPool, double samplingRate, String criterion, BigDecimal tScore, int tSize) {
        TestSuite testSuite = new TestSuite(matrix, mutantPool, samplingRate);

        Collections.shuffle(matrix.allTests);

        /**
         * exit conditions
         * k: tScore <= kScore < tScore+0.05
         * kr: tScore <= kScore && tSize <= size
         * d: tScore <= dScore < tScore+0.05
         * r: tSize <= size
         */

        for(String test: matrix.allTests) {
            if(criterion.equals("r")) {
                testSuite.addTest(test);
                if(tSize == testSuite.size())
                    return testSuite;
            }

            if(criterion.equals("d")) {
                if(testSuite.distMore(test) > 0)
                    testSuite.addTest(test);
                if(tScore.compareTo(testSuite.dScore()) <= 0 && testSuite.dScore().compareTo(tScore.add(new BigDecimal("0.05"))) < 0)
                    return testSuite;
                else if(testSuite.dScore().compareTo(tScore.add(new BigDecimal("0.05"))) >= 0)
                    return null;
            }

            if(criterion.equals("k")) {
                if(testSuite.killMore(test) > 0)
                    testSuite.addTest(test);
                if(tScore.compareTo(testSuite.kScore()) <= 0 && testSuite.kScore().compareTo(tScore.add(new BigDecimal("0.05"))) < 0)
                    return testSuite;
                else if(testSuite.kScore().compareTo(tScore.add(new BigDecimal("0.05"))) >= 0)
                    return null;
            }

            if(criterion.equals("ks")) {

                // Fist, add a test case if it additionally kills live mutants
                if(testSuite.killMore(test) > 0)
                    testSuite.addTest(test);

                // Then, check whether the test suite satisfies the score interval
                if(tScore.compareTo(testSuite.kScore()) <= 0 && testSuite.kScore().compareTo(tScore.add(new BigDecimal("0.05"))) < 0) {

                    if(testSuite.size() <= tSize) {

                        int counter = 0;
                        while(testSuite.size() < tSize && counter < MAX_TRY) {
                            // use stacking, if TS satisfies the score interval with smaller size
                            TestSuite tmpTestSuite = generateTestSuite(matrix, mutantPool, samplingRate, "k", tScore, tSize);
                            if(tmpTestSuite == null) continue;
                            else counter++;
                            for (String t : tmpTestSuite.getTestSuite()) {
                                testSuite.addTest(t);
                                if (testSuite.size() == tSize)
                                    break;
                            }
                        }

                        if(testSuite.size() == tSize)
                            return testSuite;
                        else
                            return null;
//                        System.err.println(i+" "+matrix.p+"-"+matrix.v+": Stacking failed ["+tScore+", "+tSize+"]");

                    } else
                        return null;

                } else if(testSuite.kScore().compareTo(tScore.add(new BigDecimal("0.05"))) >= 0)
                    return null;

            }

        }

        System.err.println("generateTestSuite(): Cannot reach [tScore, tSize]: ["+tScore+", "+tSize+"] for "+criterion);
        return null;
    }

    private static TestSuite generateTestSuite(MatrixObj matrix, ArrayList<Integer> mIDs, String criterion, BigDecimal tScore, int tSize) {
        TestSuite testSuite = new TestSuite(matrix, mIDs);

        Collections.shuffle(matrix.allTests);

        /**
         * exit conditions
         * k: tScore <= kScore < tScore+0.05
         * kr: tScore <= kScore && tSize <= size
         * d: tScore <= dScore < tScore+0.05
         * r: tSize <= size
         */

        for(String test: matrix.allTests) {
            if(criterion.equals("r")) {
                testSuite.addTest(test);
                if(tSize == testSuite.size())
                    return testSuite;
            }

            if(criterion.equals("d")) {
                if(testSuite.distMore(test) > 0)
                    testSuite.addTest(test);
                if(tScore.compareTo(testSuite.dScore()) <= 0 && testSuite.dScore().compareTo(tScore.add(new BigDecimal("0.05"))) < 0)
                    return testSuite;
                else if(testSuite.dScore().compareTo(tScore.add(new BigDecimal("0.05"))) >= 0)
                    return null;
            }

            if(criterion.equals("k")) {
                if(testSuite.killMore(test) > 0)
                    testSuite.addTest(test);
                if(tScore.compareTo(testSuite.kScore()) <= 0 && testSuite.kScore().compareTo(tScore.add(new BigDecimal("0.05"))) < 0)
                    return testSuite;
                else if(testSuite.kScore().compareTo(tScore.add(new BigDecimal("0.05"))) >= 0)
                    return null;
            }

            if(criterion.equals("ks")) {

                // Fist, add a test case if it additionally kills live mutants
                if(testSuite.killMore(test) > 0)
                    testSuite.addTest(test);

                // Then, check whether the test suite satisfies the score interval
                if(tScore.compareTo(testSuite.kScore()) <= 0 && testSuite.kScore().compareTo(tScore.add(new BigDecimal("0.05"))) < 0) {

                    if(testSuite.size() <= tSize) {

                        int counter = 0;
                        while(testSuite.size() < tSize && counter < MAX_TRY) {
                            // use stacking, if TS satisfies the score interval with smaller size
                            TestSuite tmpTestSuite = generateTestSuite(matrix, mIDs,"k", tScore, tSize);
                            if(tmpTestSuite == null) continue;
                            else counter++;
                            for (String t : tmpTestSuite.getTestSuite()) {
                                testSuite.addTest(t);
                                if (testSuite.size() == tSize)
                                    break;
                            }
                        }

                        if(testSuite.size() == tSize)
                            return testSuite;
                        else
                            return null;
//                        System.err.println(i+" "+matrix.p+"-"+matrix.v+": Stacking failed ["+tScore+", "+tSize+"]");

                    } else
                        return null;

                } else if(testSuite.kScore().compareTo(tScore.add(new BigDecimal("0.05"))) >= 0)
                    return null;

            }

        }

        System.err.println("generateTestSuite(): Cannot reach [tScore, tSize]: ["+tScore+", "+tSize+"] for "+criterion);
        return null;
    }


}
