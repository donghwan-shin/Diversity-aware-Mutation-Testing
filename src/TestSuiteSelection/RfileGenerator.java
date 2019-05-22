package TestSuiteSelection;

import IO.Reader;
import IO.Writer;
import Matrix.MatrixInfo;

import java.util.ArrayList;

public class RfileGenerator {

	static String workingDir = null;

	public static void main(String[] args) {

		// Set path information
		String OS = System.getProperty("os.name").toLowerCase();
		if(OS.contains("win")) {
			workingDir = "e:/Dropbox/mutation_v4";
		} else if(OS.contains("mac")) {
			workingDir = "/Users/donghwan.shin/Dropbox/mutation_v5";
		} else {
			System.err.println("ERROR: not for the current OS: "+OS);
			System.exit(-1);
		}

		String testPool = "all_Chart";
		String experiment = "scoreControl_dyn";
		String folder = "testSuiteSelection";
        scrapper(testPool, experiment, folder,workingDir+"/matrices/generatedMatrices_"+testPool+".csv", false);

	}

    private static void scrapper(String testPool, String experiment, String folder, String programListFile, boolean printAll) {

        Writer w = new Writer(workingDir+"/"+folder+"/"+testPool+"."+experiment+".csv");
        if(experiment.equals("similarity"))
            w.write("p,v,n,testPoolName,aM,mID,similarity,hard2kill\n");
//        if(printAll)
//            w.write("tScore,p,v,n,testPoolName,aT,mutantPool,%M,aM,eff_kM,eff_dM,eff_sM,eff_hM,criterion,kScore,dScore,sScore,size,isDetect,trigTests\n");
//        else
//            w.write("tScore,p,v,criterion,aM,kM,kScore,aT,size,isDetect,numTrigs\n");
        w.write("p,v,n,testPoolName,aT,pT,baseTS,aM,kM,dM,sM,cM,dy_sM,dy_cM,criterion,tScore,kScore,dScore,size,isDetect,trigTests\n");

        int i = 1;
        ArrayList<String> faultList = MatrixInfo.readMatrixInfo(programListFile, false);
        for(String fault: faultList) {
            String[] tokens = fault.split(",");
            String p = tokens[0];
            String v = tokens[1];
            String n = tokens[2];
            String pool = tokens[3];

            System.out.print(i+++"\t");

            directReadWriteFile(w, workingDir+"/"+folder+"/"+p+"/"+p+"."+v+"f."+n+"."+pool+"."+experiment+".csv");
        }
        w.close();
    }

    private static void directReadWriteFile(Writer w, String fileName) {
        Reader r = new Reader(fileName);
        String line;
        while ((line = r.readLine()) != null) {
            if (line.trim().isEmpty() || line.contains("p,v,")) continue;
            w.write(line.trim()+"\n");
        }
        r.close();

        System.out.println("directReadWriteFile(): "+fileName);
    }


}
