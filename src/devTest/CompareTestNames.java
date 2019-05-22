package devTest;

import IO.Reader;
import IO.Writer;

import java.util.ArrayList;

/**
 * Created by donghwan on 2017-09-05.
 *
 * Compare developer-written test names between different files [$v.tests vs. $v.methods]
 */
public class CompareTestNames {

    static String workingDir = "c:/Users/donghwan/Dropbox/mutation_v4/defects4j.projects";

    public static void main(String[] args) {

        Writer w = new Writer(workingDir+"/compareTestNameResults");

        for(int i=1; i<=26; i++)
            compare(w,"Chart", i);
        for(int i=1; i<=133; i++)
            compare(w,"Closure", i);
        for(int i=1; i<=65; i++)
            compare(w,"Lang", i);
        for(int i=1; i<=106; i++)
            compare(w,"Math", i);
        for(int i=1; i<=27; i++)
            compare(w,"Time", i);

        w.close();

    }

    public static void compare(Writer w, String p, int v) {
        System.out.println("compare(): "+p+"-"+v);

        // read $v.methods file
        String line;
        ArrayList<String> oldTests = new ArrayList<>();
        Reader methods = new Reader(workingDir+"/"+p+"/relevant_tests/"+v+".methods");
        while((line = methods.readLine()) != null) {
            if(line.trim().equals("")) continue;
            oldTests.add(line);
        }
        methods.close();

        // read $v.tests file
        ArrayList<String> newTests = new ArrayList<>();
        Reader tests = new Reader(workingDir+"/"+p+"/relevant_tests/"+v+".tests");
        while((line = tests.readLine()) != null) {
            if(line.trim().equals("")) continue;
            newTests.add(line);
        }
        tests.close();

        // compare the two array
        w.writeln(p+"-"+v);
        ArrayList<String> tmp = (ArrayList<String>) oldTests.clone();
        tmp.removeAll(newTests);
        for(String t: tmp) {
            w.writeln("REMOVE="+t);
        }
        newTests.removeAll(oldTests);
        for(String t: newTests) {
            w.writeln("INCLUDE="+t);
        }

    }
}
