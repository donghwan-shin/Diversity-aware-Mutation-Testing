package IO;

public class Path {

    private String BASE_DIR = null;
    private String WORK_DIR = null;
    private String prtList = null;

    public Path(String file) {
        Reader path = new Reader(file);
        String line;
        while((line = path.readLine()) != null) {
            String[] tokens = line.split(",");
            if(tokens[0].equals("BASE_DIR")) BASE_DIR = tokens[1];
            if(tokens[0].equals("WORK_DIR")) WORK_DIR = tokens[1];
            if(tokens[0].equals("prtList")) prtList = tokens[1];
        }
        path.close();

        if(BASE_DIR == null) System.err.println("BASE_DIR == null");
        if(WORK_DIR == null) System.err.println("WORK_DIR == null");
        if(prtList == null) System.err.println("prtList == null");
    }

    public String getBaseDir() { return BASE_DIR; }

    public String getWorkDir() { return WORK_DIR; }

    public String getPrtList() { return prtList; }

}
