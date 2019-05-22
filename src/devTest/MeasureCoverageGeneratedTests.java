package devTest;

import IO.IOUtil;
import IO.Reader;
import IO.Writer;
import TerminalCore.Terminal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by donghwan on 2018-05-05.
 *
 * Measure the coverage of each of the generated tests
 * using the 'defects4j coverage -s testSuite' command in the checked out directory.
 *
 * The output file is the form of a list that contains the pairs of testName and coveredLineNo.
 *
 * In case of unexpected abort in operation,
 * the output list file is generated as a temporal file at first,
 * and then renamed as the final file when the operation is done.
 *
 */

public class MeasureCoverageGeneratedTests {

    static String D4J = "/home/donghwan/defects4j/framework/bin/defects4j";
    static String workindDir = "/home/donghwan/Dropbox/mutation_v4";
    static String tmpZipFileName = "tmpZip.tar.bz2";

    public static void main(String[] args) {

        DocumentBuilder parser = getXMLParser();

        String pool = "randoop";

        for(int n=1; n<=1; n++) {
//            for (int v = 1; v <= 26; v++)
//                measureCoverage(pool, "Chart", v, n, parser);
//            for (int v = 1; v <= 133; v++)
//                measureCoverage(pool, "Closure", v, n, parser);
//            for (int v = 1; v <= 65; v++)
//                measureCoverage(pool, "Lang", v, n, parser);
//            for (int v = 1; v <= 106; v++)
//                measureCoverage(pool, "Math", v, n, parser);
            for (int v = 1; v <= 27; v++)
                measureCoverage(pool, "Time", v, n, parser);
        }

//        measureCoverage(pool, "Chart", 1, 1, parser);

    }


    static void measureCoverage(String pool, String p, int v, int n, DocumentBuilder parser) {
        System.out.println(pool+"-"+p+"-"+v+"-"+n);

        String covMapFile = workindDir+"/defects4j.results/coverage_log/"+p+"/"+pool+"/"+v+"f."+n+".covMap.csv";
        String covMapFile_tmp = workindDir+"/defects4j.results/coverage_log/"+p+"/"+pool+"/"+v+"f."+n+".covMap.tmp";

        // Check whether there is a compete file - if true, skip
        if(IOUtil.isFile(covMapFile)) {
            return;
        }

        // Check whether there is a unzip directory
        String unzipDir = "/tmp/unzip-"+pool+"-"+p+"-"+v+"-"+n;
        File unzipTmpDir = new File(unzipDir);
        if(unzipTmpDir.exists()) {
            Terminal.bash(new String[]{"rm", "-rf", unzipDir}, null, null);
            System.out.println("CLEAN: "+unzipDir);
        }
        if(!unzipTmpDir.mkdir()) {
            System.err.println("FAILED: mkdir() - "+unzipDir);
            System.exit(-1);
        }

        // Check whether there is a temporal file - if true, continue on that file
        boolean isContinue = false;
        if(IOUtil.isFile(covMapFile_tmp)) {
            isContinue = true;
        }

        // Build a list of testNames that are already done
        ArrayList<String> testNames_tmp = new ArrayList<>();
        if(isContinue) {
            // Read all the previous data in the temporal file
            Reader r = new Reader(covMapFile_tmp);
            String line;
            while ((line = r.readLine()) != null) {
                String[] tokens = line.split(",");
                if (!testNames_tmp.contains(tokens[0]))
                    testNames_tmp.add(tokens[0]);
            }
            r.close();
        }

        // Unzip the test suites into the unzipDir directory.
        String testSuite = workindDir+"/defects4j.testSuites/"+p+"/"+pool+"/"+n+"/"+p+"-"+v+"f-"+pool+"."+n+".tar.bz2";
        Terminal.bash(new String[]{"tar", "-xjf", testSuite, "-C", unzipDir}, null, null);

        // Search files in the paths
        ArrayList<String> testFiles = null;
        if(pool.equals("randoop")) {
            testFiles = findRDTestFiles(unzipDir);
        } else if(pool.startsWith("evosuite")) {
            testFiles = findESTestFiles(unzipDir);
        }

        if(testFiles == null || testFiles.isEmpty())
            return;

        String scaffoldingFiles="";
        for(String f: testFiles) if(f.contains("_scaffolding")) {
            // replace "/tmp/org" → "."
            f = f.replace(unzipDir, ".");

            // make a string [s s s ...]
            scaffoldingFiles += " "+f;
        }

        // Checkout the p-v in 'tmpDir'
        String tmpDir = Terminal.checkout(D4J, p, v+"f", null);

        Collections.sort(testFiles);
        for(String file: testFiles) {
            if(file.contains("_scaffolding")) continue;
            if(file.endsWith(".bak") || file.endsWith("broken")) continue;

            // skip if the 'testName' is already in the temporal file
            String testName = file.replace(unzipDir+"/", "").replaceAll("/",".").replace(".java", "");
            if(testNames_tmp.contains(testName))
                continue;

            // tar -cjf zipFileName.tar.bz2 [file file file ...]
            String zipFiles = file.replace(unzipDir, ".")+scaffoldingFiles;
            String cmd = "tar -cjf "+tmpZipFileName+" "+zipFiles;
            Terminal.bash(cmd.split(" "), unzipDir, null);

            System.out.println("\n"+testName);
            Terminal.bash(new String[]{D4J, "coverage", "-s", unzipDir+"/"+tmpZipFileName}, tmpDir, null);

            // Parse 'coverage.xml' file and build a list of 'testName,coveredLineNo' as a temporal file
            XML2CovMap(testName, tmpDir+"/coverage.xml", covMapFile_tmp, parser);
        }

        // When it is done, change the name of the file
        File tmpFile = new File(covMapFile_tmp);
        File covMap = new File(covMapFile);
        if(tmpFile.exists() && tmpFile.renameTo(covMap) == false) {
            System.err.println("FILE RENAME ERROR");
            System.exit(-1);
        }

        Terminal.bash(new String[]{"rm", "-rf", unzipDir, tmpDir}, null, null);
        return;
    }


    /**
     * Find all *ESTest.java files in the working directory and its sub-directory.
     *
     * @param workingDir working directory
     * @return List of ESTest.java files including its path
     */
    private static ArrayList<String> findESTestFiles(String workingDir) {
        ArrayList<String> testFiles = new ArrayList<String>();

        File dir = new File(workingDir);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().contains("_ESTest")) {
                testFiles.add(file.getPath());
            } else if (file.isDirectory()) {
                testFiles.addAll(findESTestFiles(file.getPath()));
            }
        }

        return testFiles;
    }


    private static ArrayList<String> findRDTestFiles(String workingDir) {
        ArrayList<String> testFiles = new ArrayList<String>();
        File dir = new File(workingDir);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith("RegressionTest")) {
                testFiles.add(file.getPath());
            }
        }
        return testFiles;
    }


    static DocumentBuilder getXMLParser() {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        f.setNamespaceAware(true);
        try {
//            f.setFeature("http://cobertura.sourceforge.net/xml/coverage-04.dtd", false);
//            f.setFeature("http://xml.org/sax/features/namespaces", false);
//            f.setFeature("http://xml.org/sax/features/validation", false);
//            f.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder parser = f.newDocumentBuilder();
            return parser;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }


    /**
     * Parse XML to find the statement coverage information for all classes for the 'testName'.
     * Record the statements covered by the 'testName' in 'covMapFile_tmp'.
     * @param testName testName
     * @param coverageFileXML coverage XML file
     * @param coverMapFile_tmp covMap file that records the testName,covLine pairs
     */
    static void XML2CovMap(String testName, String coverageFileXML, String coverMapFile_tmp, DocumentBuilder parser) {

        String coverage = "";

        try {
            Document xml = parser.parse(coverageFileXML);

            // Access classes
            NodeList classNodes = xml.getElementsByTagName("class");
            for(int i=0; i<classNodes.getLength(); i++) {
                // for each class, get the line coverages
                Element classNode = (Element) classNodes.item(i);
                NodeList children = classNode.getChildNodes();

                // Find a node 'root-class-lines' subtree
                Element lines = null;
                for(int k=0; k<children.getLength(); k++) {
                    if(children.item(k).getNodeName().equals("lines")) {
                        lines = (Element) children.item(k);
                        break;
                    }
                }

                // Find coverage information under 'lines'
                NodeList coverages = lines.getElementsByTagName("line");
                for(int j=0; j<coverages.getLength(); j++) {
                    Element line = (Element) coverages.item(j);
                    int hits = Integer.parseInt(line.getAttribute("hits"));

                    // if a line is covered by the test, write it in covMap
                    if(hits>0) {
                        coverage += testName+","+i+"_"+line.getAttribute("number")+"\n";
//                        System.out.println(testName+","+i+"_"+line.getAttribute("number"));
                    }
                }
            }

        } catch (SAXException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
//            System.exit(-1);
        }

        // append or write the covMap information
        if(IOUtil.isFile(coverMapFile_tmp)) {
            try {
                Files.write(Paths.get(coverMapFile_tmp), coverage.getBytes(), StandardOpenOption.APPEND);
            }catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            Writer w = new Writer(coverMapFile_tmp);
            w.write(coverage);
            w.close();
        }

    }



}
