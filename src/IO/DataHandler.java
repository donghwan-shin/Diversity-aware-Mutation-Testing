package IO;

import java.util.ArrayList;

public class DataHandler {

    String BASE_DIR = null, RET_DIR = null, WORK_DIR = null, MUT_DIR = null;

    public DataHandler() {
        // Set path information
        String OS = System.getProperty("os.name").toLowerCase();
        if(OS.contains("win")) {
            BASE_DIR = "C:/Users/donghwan/Dropbox/mutation_v3/";
        } else if(OS.contains("mac")) {
            BASE_DIR = "/Users/donghwan/Dropbox/mutation_v3/";
        } else {
            System.err.println("ERROR: not for the current OS: "+OS);
            System.exit(-1);
        }
        WORK_DIR = BASE_DIR + "eMSG/";
        RET_DIR = BASE_DIR + "defects4j.results/";
        MUT_DIR = BASE_DIR + "defects4j.results/mutation_log/";
    }

    public String getBASE_DIR() { return BASE_DIR; }
    public String getRET_DIR() { return RET_DIR; }
    public String getWORK_DIR() { return WORK_DIR; }
    public String getMUT_DIR() { return MUT_DIR; }

    public ArrayList<Fault> getFaultList(String targetFile) {
        ArrayList<Fault> faults = new ArrayList<>();

        String p; // Project Name
        String v; // Version id
        String n; // Expr phase
        String pool; // Total testPoolName (for fileSelection)

        Reader r = new Reader(RET_DIR + targetFile);
        String line;
        while ((line = r.readLine()) != null) {
            // Empty line skip, header skip
            if (line.trim().isEmpty() || line.contains("project")) continue;

            // Split tokens for get p,v,n,pool information
            String[] tokens = line.split(",");
            if (!(tokens[6].equals("0") || tokens[6].equals("-"))) { // kM > 0
                p = tokens[0];
                v = tokens[1].replaceAll("[^0-9]", "");
                pool = tokens[2];
                n = tokens[3];
                faults.add(new Fault(p,v,n,pool));
            }
        }
        r.close();

        return faults;
    }

    public class Fault {
        String p; // Project Name
        String v; // Version id
        String n; // Expr phase
        String pool; // Total testPoolName (for fileSelection)

        public Fault(String p, String v, String n, String pool) {
            this.p = p;
            this.v = v;
            this.n = n;
            this.pool = pool;
        }

        public String getP() { return p; }
        public String getV() { return v; }
        public String getN() { return n; }
        public String getPool() { return pool; }
    }

}
