package Core;

import Matrix.MatrixObj;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class TestSuite {

    private MatrixObj matrix;

    private String mutantPool;
    private double samplingRate;
    private ArrayList<Integer> mutants;
    private ArrayList<String> testSuite;
    private HashMap<Integer, ArrayList<Integer>> distMutantMap;

    // number of maximally {distinguished, killed, subsuming} mutants for a given set of mutants
    private int kM_max;
    private int dM_max;
    private int sM_max;

    /**
     * Initialize a test suite by initializing the distinguished mutants map.
     * In other words, a set of mutants must be given as an input.
     * @param matrix
     * @param mutantPool
     * @param samplingRate
     */
    public TestSuite(MatrixObj matrix, String mutantPool, double samplingRate) {
        this.matrix = matrix;
        this.mutantPool = mutantPool;
        this.samplingRate = samplingRate;

        testSuite = new ArrayList<>();

        // mutant kill information is calculated using distMuatantMap.
        // Initial MIDs refers to the live mutants at first.
        // Live mutants that are saved in the 0 position of the distMuatantMap.
        // Killed mutants are calculated by TOTAL - live mutants.
        distMutantMap = new HashMap<>();
        if(mutantPool.equals("all")) {
            mutants = matrix.compactedMIDs; // all mutants in compactedMIDs are distinguishable
            distMutantMap.put(0, new ArrayList<>(mutants));

            kM_max = matrix.kMutants;
            dM_max = matrix.dMutants;
            sM_max = matrix.sMutants;
        }
        else if(mutantPool.equals("subsuming")) {
            mutants = matrix.subsumingMIDs;
            distMutantMap.put(0, new ArrayList<>(mutants));

            kM_max = mutants.size();
            dM_max = mutants.size()+1; // all mutants in subsumingMIDs are distinguishable
            sM_max = mutants.size();
        }
        else if(mutantPool.equals("hard")) {
            mutants = matrix.hardMIDs;
            distMutantMap.put(0, new ArrayList<>(mutants));

            dM_max = mutants.size()+1; // all mutants in hardMIDs are distinguishable because hMIDs come from compactedMIDs
            kM_max = 0;
            sM_max = 0;
            for(int h: mutants) {
                kM_max += 1 + matrix.duplicatedMutants.get(h).size(); // all hardMIDs are killable
                if(matrix.subsumingMIDs.contains(h)) sM_max++;
            }
        }
        else if(mutantPool.equals("random")) {
            mutants = new ArrayList<>();
            for(int i=1; i<=matrix.totalMutants; i++)
                mutants.add(i);
            Collections.shuffle(mutants);
            mutants = new ArrayList<>(mutants.subList(0, (int) (matrix.totalMutants*samplingRate)));
            distMutantMap.put(0, new ArrayList<>(mutants));

            kM_max = matrix.getKilledMIDs(matrix.allTests, mutants).size();
            dM_max = matrix.getDistinguishedMIDs(matrix.allTests, mutants).size()+1;
            sM_max = matrix.getSubsumingMIDs(matrix.allTests, mutants).size();
        }
        distMutantMap.get(0).remove(Integer.valueOf(0));
    }

    public TestSuite(MatrixObj matrix, ArrayList<Integer> mutantIDs) {
        this.matrix = matrix;
        this.mutantPool = "specific";
        this.samplingRate = 1;

        mutants = mutantIDs;
        testSuite = new ArrayList<>();
        distMutantMap = new HashMap<>();
        distMutantMap.put(0, new ArrayList<>(mutants));
        distMutantMap.get(0).remove(Integer.valueOf(0));

        kM_max = matrix.getKilledMIDs(matrix.allTests, mutants).size();
        dM_max = matrix.getDistinguishedMIDs(matrix.allTests, mutants).size()+1;
        sM_max = matrix.getSubsumingMIDs(matrix.allTests, mutants).size();
    }


    /**
     * ------------------------------------------------------------------------------------
     * Private methods
     * ------------------------------------------------------------------------------------
     */


    private boolean updateDistMutantMap(String test) {
        HashMap<Integer, ArrayList<Integer>> separatedMutantMap = new HashMap<>();

        for(int m: distMutantMap.keySet()) {
            ArrayList<Integer> separatedMIDs = new ArrayList<>();
            for(int um: distMutantMap.get(m)) {
                if(matrix.distinguish(test, m, um)) {
                    separatedMIDs.add(um);
                }
            }
            if(!separatedMIDs.isEmpty()) {
                distMutantMap.get(m).removeAll(separatedMIDs);
                int newKey = separatedMIDs.remove(0); // pop the top mID and use it as a key
                separatedMutantMap.put(newKey, separatedMIDs);
            }
        }
        distMutantMap.putAll(separatedMutantMap);
        return !separatedMutantMap.isEmpty();
    }


    /**
     * Return the number of distinguished mutants.
     * @return
     */
    private int getDMutants() {
        return distMutantMap.keySet().size();
    }


    /**
     * Return the number of killed mutants using distMutantMap.
     * Using "all" or "hard" mutants, we should consider duplicated mutants.
     * Using "subsuming", "random", or "specific" mutants, we do not need to consider duplicated mutants.
     * @return
     */
    private int getKMutants() {
        if(mutantPool.equals("all") || mutantPool.equals("hard")) {
            int killed = 0;
            for(int d: distMutantMap.keySet()) {
                if(d == 0) continue;
                killed += 1 + matrix.duplicatedMutants.get(d).size();
                for(int ud: distMutantMap.get(d))
                    killed += 1 + matrix.duplicatedMutants.get(ud).size();
            }
            return killed;
        }
        else if(mutantPool.equals("subsuming") || mutantPool.equals("random") || mutantPool.equals("specific")) {
            int killed = 0;
            for(int d: distMutantMap.keySet()) {
                if(d == 0) continue;
                killed += 1 + distMutantMap.get(d).size();
            }
            return killed;
        }
        else
            return -1;
    }


    /**
     * Return the number of subsuming mutants.
     * When calculating subsuming mutants, we do not need to consider duplicated mutants by definition,
     * because all duplicated mutants are not included in subsuming mutants.
     * @return
     */
    private int getSMutants() {
        return matrix.getSubsumingMIDs(testSuite, new ArrayList<>(distMutantMap.keySet())).size();
    }




    /**
     * ------------------------------------------------------------------------------------
     * Public methods
     * ------------------------------------------------------------------------------------
     */
    public String getMutantPool() { return mutantPool; }

    public double getSamplingRate() { return samplingRate; }

    public ArrayList<String> trigTests() {
        ArrayList<String> trigTests = new ArrayList<>();
        for(String test: testSuite)
            if(matrix.faultDetectionNames.contains(test))
                trigTests.add(test);
        return trigTests;
    }

    public int isDetect() {
        for(String test: testSuite)
            if(matrix.faultDetectionNames.contains(test))
                return 1;
        return 0;
    }

    public int size() { return testSuite.size(); }

    public ArrayList<String> getTestSuite() { return testSuite; }

    public String toString() {
        return "TestSuite information for "+matrix.p+"-"+matrix.v+"\n"
                +"kM: "+getKMutants()+"\n"
                +"dM: "+getDMutants()+"\t"+distMutantMap.keySet()+"\n"
                +"sM: "+getSMutants()+"\n"
                +"isDetect: "+isDetect();
    }


    /**
     * Return the current kScore
     * @return
     */
    public BigDecimal kScore() {
        return new BigDecimal(getKMutants() / (double) kM_max);
    }


    /**
     * Return the current dScore
     * @return
     */
    public BigDecimal dScore() {
        return new BigDecimal(getDMutants() / (double) dM_max);
    }


    /**
     * Return the current sScore
     * @return
     */
    public BigDecimal sScore() {
        return new BigDecimal(getSMutants() / (double) sM_max);
    }


    /**
     * Calculate additionally killed mutants by the given test.
     * Do not add the test.
     * Do not update the distMutantMap.
     * @param test
     * @return
     */
    public int killMore(String test) {
        int kill = 0;

        int org = 0;
        for(int um: distMutantMap.get(org))
            if(matrix.distinguish(test, org, um))
                kill++;

        return kill;
    }


    /**
     * Calculate additionally distinguished mutants by the given test.
     * Do not add the test.
     * Do not update the distMutantMap.
     * @param test
     * @return
     */
    public int distMore(String test) {
        int dist = 0;

        for(int m: distMutantMap.keySet()) {
            for (int um : distMutantMap.get(m)) {
                if (matrix.distinguish(test, m, um)) {
                    dist++;
                    break;
                }
            }
        }

        return dist;
    }


    /**
     * Add a test (string) in this test suite and update distMutantMap.
     * Adding allows redundant test cases.
     * @param test
     * @return
     */
    public void addTest(String test) {
        if(!testSuite.contains(test)) {
            updateDistMutantMap(test);
        }
        testSuite.add(test);
    }

}
