package Matrix;

import java.io.Serializable;
import java.util.*;

public class MatrixObj implements Serializable {

    private static final long serialVersionUID = -3105216225736593557L;

    public String p, v, n, testPoolName;
    public int totalTests;
    public int totalMutants;
    public int kMutants;
    public int dMutants;
    public int sMutants;
    public int hMutants;

    public ArrayList<Integer> coupledMIDs;
    public ArrayList<Integer> compactedMIDs;
    public ArrayList<Integer> killableMIDs; // among compatedMIDs
    public ArrayList<Integer> subsumingMIDs; // among compatedMIDs
    public ArrayList<Integer> hardMIDs; // among compatedMIDs

    public HashMap<Integer, ArrayList<Integer>> duplicatedMutants; // compactedMID -> duplicateMIDs (list)
    public ArrayList<String> allTests;
    public ArrayList<String> faultDetectionNames;

    private HashMap<String, ArrayList<Integer>> fullKillMap;


    public MatrixObj(String p, String v, String n, String testPoolName,
                     int totalMutants, ArrayList<String> testNames,
                     ArrayList<String> trigTestNames,
                     HashMap<String, ArrayList<Integer>> fullKillMap) {

        this.p = p;
        this.v = v;
        this.n = n; // {integer, big}
        this.testPoolName = testPoolName; // {all, dev, randoop, evosuite-branch, evosuite-strongmutation, evosuite-weakmutation}
        this.fullKillMap = fullKillMap;
        this.totalMutants = totalMutants;


        // Initialize the working test pool
        allTests = new ArrayList<>();
        faultDetectionNames = new ArrayList<>();
        for (String test : testNames) {
            if(testPoolName.equals("all") || test.substring(0, test.indexOf("__")).equals(testPoolName)) {
                if(n.equals("big") || (!n.equals("big") && test.startsWith(n+"__"))) {
                    allTests.add(test);
                    if (trigTestNames.contains(test))
                        faultDetectionNames.add(test);
                }
            }
        }
        totalTests = allTests.size();


        // CoupledMIDs calculation
        coupledMIDs = new ArrayList<>();
        for(int i=1; i<=totalMutants; i++)
            for(String test: allTests)
                if(faultDetectionNames.contains(test) && kill(test, i))
                    coupledMIDs.add(i);


        // Matrix compaction
        duplicatedMutants = getDuplicatedMutants(allTests, totalMutants);
        compactedMIDs = getCompactedMIDs(duplicatedMutants);
        dMutants = compactedMIDs.size();
        killableMIDs = getKilledMIDs(allTests, compactedMIDs);
        kMutants = 0;
        for(int k: killableMIDs)
            kMutants += 1 + duplicatedMutants.get(k).size();

        // Matrix subsumption calculation
        subsumingMIDs = getSubsumingMIDs(allTests, compactedMIDs);
        sMutants = subsumingMIDs.size();


        // Hard to kill mutants (each of which are killed by at most 10% of test pool)
        int hardThresholdNumberOfTests = (int) (totalTests*0.01);
        if(hardThresholdNumberOfTests < 1) hardThresholdNumberOfTests = 1;
        hardMIDs = getHardMIDs(allTests, compactedMIDs, hardThresholdNumberOfTests);
        hMutants = hardMIDs.size();
    }


    /**
     * @param test test as its name (String)
     * @param mID mutant as its id (int)
     * @return whether the test kills the mutant or not
     */
    public boolean kill(String test, int mID) {
        if(fullKillMap.containsKey(test))
            return fullKillMap.get(test).contains(mID);
        else
            return false;
    }

    /**
     * @param test test as its name (String)
     * @param m_x mutant x
     * @param m_y mutant y
     * @return whether the test distinguishes m_x and m_y or not
     */
    public boolean distinguish(String test, int m_x, int m_y) {
        return kill(test, m_x) != kill(test, m_y);
    }

    /**
     * @param testSuite test pool
     * @param m_x mutant x
     * @param m_y mutant y
     * @return whether m_x subsumes m_y for the given test pool
     */
    public boolean subsume(ArrayList<String> testSuite, int m_x, int m_y) {
        for(String test: testSuite)
            if(kill(test, m_x) && !kill(test, m_y))
                return false;
        return true; // Note that x subsumes y when x and y are duplicated!
    }

    /**
     * @param testSuite
     * @param mID
     * @return whether the mID is equivalent in terms of the given testSuite or not
     */
    public boolean equivalent(ArrayList<String> testSuite, int mID) {
        for(String test: testSuite)
            if(kill(test, mID))
                return false;
        return true;
    }

    /**
     * @param testSuite
     * @param m_x
     * @param m_y
     * @return whether the two mutants are duplicated in terms of the given testSuite or not
     */
    public boolean duplicate(ArrayList<String> testSuite, int m_x, int m_y) {
        for(String test: testSuite)
            if(distinguish(test, m_x, m_y))
                return false;
        return true;
    }

    /**
     * @return [similarity, hard2kill] for all mutants
     */
    public ArrayList<Double[]> similarity() {

        ArrayList<Double[]> results = new ArrayList<>();
        results.add(new Double[]{-1.0, -1.0});

        for(int mID=1; mID<=totalMutants; mID++) { // zero mutant = original program
            /**
             * Calculate similarity between each mutant and the real fault (using kill matrix)
             */

            int fail_Kill = 0;  // failed and killed
            int pass_kill = 0;  // passed and killed
            int totalFail = 0;  // total failed (killed + lived)

            for (String test : allTests) {
                if (faultDetectionNames.contains(test)) {
                    totalFail++;
                    if (kill(test, mID))
                        fail_Kill++;
                } else {
                    if (kill(test, mID))
                        pass_kill++;
                }
            }

            double similarity = fail_Kill / Math.sqrt(totalFail * (fail_Kill + pass_kill));


            /**
             * Calculate hard2kill rate (i.e., % of killing tests for each mutant)
             */

            int killingTests = 0;
            for (String test : allTests) {
                if(kill(test, mID))
                    killingTests++;
            }

            double hard2kill = killingTests / (double) totalTests;


            results.add(new Double[]{similarity, hard2kill});
        }

        return results;
    }

    public ArrayList<Integer> getCompactedMIDs(HashMap<Integer, ArrayList<Integer>> duplicatedMutants) {
        ArrayList<Integer> compactedMIDs = new ArrayList<>();
        for(int i: duplicatedMutants.keySet())
            compactedMIDs.add(i);
        return compactedMIDs;
    }

    public HashMap<Integer, ArrayList<Integer>> getDuplicatedMutants(ArrayList<String> testSuite, int totalMutants) {
        HashMap<Integer, ArrayList<Integer>> duplicatedMutants = new HashMap<>();

        boolean[] isCompacted = new boolean[totalMutants+1]; // zero mutant = original program
        for(int i=0; i<=totalMutants; i++) { // zero mutant = original program
            if(isCompacted[i] == false) {
                ArrayList<Integer> duplicates = new ArrayList<>();
                for(int j=i+1; j<=totalMutants; j++) {
                    boolean equal = true;
                    for(String test: testSuite) {
                        if(distinguish(test, i, j)) {
                            equal = false;
                            break;
                        }
                    }
                    if(equal) {
                        isCompacted[j] = true;
                        duplicates.add(j);
                    }
                }
                duplicatedMutants.put(i, duplicates);
            }
        }

        return duplicatedMutants;
    }

    public ArrayList<Integer> getKilledMIDs(ArrayList<String> testSuite, ArrayList<Integer> mIDs) {
        ArrayList<Integer> killedMIDs = new ArrayList<>();
        for(int m: mIDs) {
            for(String test: testSuite)
                if(kill(test, m)) {
                    killedMIDs.add(m);
                    break;
                }
        }
        return killedMIDs;
    }

    public ArrayList<Integer> getDistinguishedMIDs(ArrayList<String> testSuite, ArrayList<Integer> mIDs) {
        Collections.sort(mIDs);
        ArrayList<Integer> distinguishableMIDs = new ArrayList<>();
        ArrayList<Integer> duplicated = new ArrayList<>();
        for(int i=0; i<mIDs.size(); i++) {
            if(!duplicated.contains(mIDs.get(i))) {
                distinguishableMIDs.add(mIDs.get(i));
                for(int j=i+1; j<mIDs.size(); j++) {
                    boolean duplicate = true;
                    for(String test: testSuite) {
                        if (distinguish(test, mIDs.get(i), mIDs.get(j))) {
                            duplicate = false;
                            break;
                        }
                    }
                    if(duplicate)
                        duplicated.add(mIDs.get(j));
                }
            }
        }
        return distinguishableMIDs;
    }

    public ArrayList<Integer> getSubsumingMIDs(ArrayList<String> testSuite, ArrayList<Integer> mIDs) {
        // find subsumed mutants
        ArrayList<Integer> subsumingMIDs = new ArrayList<>();
        for(int x: mIDs) {
            if(equivalent(testSuite, x)) continue;
            boolean subsumed = false;
            for(int y: mIDs) {
                if(x==y || equivalent(testSuite, y)) continue;
                if(subsume(testSuite, y, x) && !duplicate(testSuite, x, y)) {
                    subsumed = true;
                    break;
                }
            }
            if(!subsumed) {
                boolean duplicated = false;
                for(int m: subsumingMIDs) {
                    if (duplicate(testSuite, m, x)) {
                        duplicated = true;
                        break;
                    }
                }
                if(!duplicated)
                    subsumingMIDs.add(x);
            }
        }
        return subsumingMIDs;
    }

    public ArrayList<Integer> getHardMIDs(ArrayList<String> testSuite, ArrayList<Integer> mIDs, int hardThresholdNumberOfTests) {
        ArrayList<Integer> hardMIDs = new ArrayList<>();
        for(int m: mIDs) {
            int killCount = 0;
            for(String test: testSuite) {
                if(kill(test, m)) {
                    killCount++;
                }
            }
            if(killCount <= hardThresholdNumberOfTests && killCount != 0) {
                hardMIDs.add(m);
            }
        }
        return hardMIDs;
    }

    /**
     * Return a sub-matrix reduced with respect to the subTestSuite.
     * @param subTestSuite
     * @return
     */
    public MatrixObj getSubMatrix(ArrayList<String> subTestSuite) {
        return new MatrixObj(p, v, n, testPoolName, totalMutants, subTestSuite, faultDetectionNames, fullKillMap);
    }

    public String toString() {
        String ret = "_______________________________\n"
                +p+"-"+v+"-"+n+"-"+ testPoolName +"'s matrix"+"\n"
                + "-------------------------------\n"
                + "allTests:\t"+totalTests+"\n"
                + "aMutants:\t"+totalMutants+"\n"
                + "kMutants:\t"+kMutants+"\n"
                + "dMutants:\t"+dMutants+"\t"+compactedMIDs+"\n"
                + "sMutants:\t"+sMutants+"\t"+subsumingMIDs+"\n"
                + "hMutants:\t"+hMutants+"\t"+hardMIDs+"\n"
                + "cMutants:\t"+coupledMIDs+"\n"
                + "TrigTests:\t"+faultDetectionNames+"\n";
        return ret;
    }
}
