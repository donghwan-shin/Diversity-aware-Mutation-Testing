import IO.Reader;
import IO.Writer;

import java.io.File;

public class Summarizer {

    public static void main(String[] args) {

        String pool = "evosuite-branch";
        String RET = "c:/Users/donghwan/Dropbox/mutation_v3/defects4j.results/mutation."+pool;
        Writer w = new Writer(RET);
        w.write("project_id,version_id,test_suite_source,test_id,mut_generated,mut_covered, mut_killed");
        mutationResultSummary(w, "Chart", 26, pool);
        mutationResultSummary(w, "Lang", 65, pool);
        mutationResultSummary(w, "Time", 27, pool);
        mutationResultSummary(w, "Math", 106, pool);
        mutationResultSummary(w, "Closure", 133, pool);
        w.close();

    }

    /**
     * Bind summary.csv files
     *
     * @param p
     * @param max
     */
    private static void mutationResultSummary(Writer w, String p, int max, String pool) {
        String MUT_DIR = "c:/Users/donghwan/Dropbox/mutation_v3/defects4j.results/mutation_log";

        StringBuffer sb = new StringBuffer();
        sb.append("project_id,version_id,test_suite_source,test_id,mut_generated,mut_covered,mut_killed\n");

        for(int v=1; v<=max; v++) {
            File testFile = new File(MUT_DIR+"/"+p+"/"+pool+"/"+v+"f.0.summary.csv");
            if(!testFile.exists()) continue;
            Reader summary_csv = new Reader(MUT_DIR+"/"+p+"/"+pool+"/"+v+"f.0.summary.csv");
            String line;
            while((line = summary_csv.readLine()) != null) {
                if(line.contains("Mutants")) continue;
                String[] tokens = line.split(",");
                w.write("\n"+p+","+v+"f,"+pool+",0,"+tokens[0]+","+tokens[1]+","+tokens[2]);
            }
            summary_csv.close();
        }
    }

}
