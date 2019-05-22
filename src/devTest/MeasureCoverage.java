package devTest;

import IO.IOUtil;
import IO.Reader;
import IO.Writer;
import TerminalCore.Terminal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by donghwan on 2017-09-08.
 *
 * Measure the coverage of each of the developer-written tests
 * using the 'defects4j coverage -t testName' command in the checked out directory.
 *
 * The output file is the form of a list that contains the pairs of testName and coveredLineNo.
 *
 * In case of unexpected abort in operation,
 * the output list file is generated as a temporal file at first,
 * and then renamed as the final file when the operation is done.
 *
 */
public class MeasureCoverage {

    static String D4J = "/home/donghwan/defects4j/framework/bin/defects4j";
    static String workindDir = "/home/donghwan/Dropbox/mutation_v4";

    public static void main(String[] args) {

        DocumentBuilder parser = getXMLParser();

//        for(int i=1; i<=26; i++)
//            measureCoverage("Chart", i, parser);
        for(int i=1; i<=40; i++)
            measureCoverage("Closure", i, parser);
//        for(int i=1; i<=65; i++)
//            measureCoverage("Lang", i, parser);
//        for(int i=1; i<=106; i++)
//            measureCoverage("Math", i, parser);
//        for(int i=1; i<=27; i++)
//            measureCoverage("Time", i, parser);

    }


    static void measureCoverage(String p, int v, DocumentBuilder parser) {
        String covMapFile = workindDir+"/defects4j.results/coverage_log/"+p+"/dev/"+v+"f.covMap.csv";
        String covMapFile_tmp = workindDir+"/defects4j.results/coverage_log/"+p+"/dev/"+v+"f.covMap.tmp";

        // Check whether there is a compete file - if true, skip
        if(IOUtil.isFile(covMapFile)) {
            return;
        }

        // Check whether there is a temporal file - if true, continue on that file
        boolean isContinue = false;
        if(IOUtil.isFile(covMapFile_tmp)) {
            isContinue = true;
        }

        // Read the list of testNames from $v.tests
        String testNameFile = workindDir+"/defects4j.projects/"+p+"/relevant_tests/"+v+".tests";
        ArrayList<String> testNames = new ArrayList<>();
        if(IOUtil.isFile(testNameFile)) {
            Reader r = new Reader(testNameFile);
            String line;
            while((line = r.readLine()) != null) {
                if(!line.trim().equals("")) {
                    testNames.add(line.trim());
                }
            }
            r.close();
        } else {
            System.err.println("There is no such file: "+testNameFile);
            return;
        }

        // Checkout the p-v in 'tmpDir'
        String tmpDir = Terminal.checkout(D4J, p, v+"f", null);

        // Build a list of testNames that are already done
        ArrayList<String> testNames_tmp = new ArrayList<>();
        if(isContinue) {
            // There is previous data in 'covMap.tmp' file

            // Read all the previous data in the temporal file
            Reader r = new Reader(covMapFile_tmp);
            String line;
            while((line = r.readLine()) != null) {
                String[] tokens = line.split(",");
                if(!testNames_tmp.contains(tokens[0]))
                    testNames_tmp.add(tokens[0]);
            }
            r.close();
        }

        // Run 'defects4j coverage -t testName' for each testName
        for(String testName: testNames) {
            // skip if the 'testName' is already in the temporal file
            if(testNames_tmp.contains(testName))
                continue;

            String[] cmd = {D4J, "coverage", "-t", testName};
            System.out.println("\n"+testName);
            Terminal.bash(cmd, tmpDir, null);

            // Parse 'coverage.xml' file and build a list of 'testName,coveredLineNo' as a temporal file
            XML2CovMap(testName, tmpDir+"/coverage.xml", covMapFile_tmp, parser);

        }

        // When it is done, change the name of the file
        File tmpFile = new File(covMapFile_tmp);
        File covMap = new File(covMapFile);
        if(tmpFile.renameTo(covMap) == false) {
            System.err.println("FILE RENAME ERROR");
            System.exit(-1);
        }

        String[] cmd = {"rm", "-rf", tmpDir};
        Terminal.bash(cmd, null, null);

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
