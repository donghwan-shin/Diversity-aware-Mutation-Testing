package TerminalCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This class is for the command-line execution in Java.
 */
public class Terminal {

    /**
     * Checkout p, v at tmpDir
     *
     * @param p
     * @param v
     * @return
     */
    public static String checkout(String D4J, String p, String v, String ID) {
        int seed = (int) (Math.random()*100000);
        String tmpDir = "/tmp/"+p+"-"+v+"."+seed;
        System.out.println(ID+",Checkout: "+tmpDir);

        // checkout program
        String[] cmd = {D4J, "checkout", "-p", p, "-v", v, "-w", tmpDir};
        bash(cmd, null, ID);

        return tmpDir;
    }

    /**
     * Execute system call (command)
     *
     * @param cmd working directory for the command
     */
    public static boolean bash(String[] cmd, String dir, String ID) {
        boolean isTimedOut = false;
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            if(dir != null)
                builder.directory(new File(dir));
            Process process = builder.start();
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                if(ID==null) {
                    System.out.println(line);
                } else {
                    System.out.println(ID+","+line);
                }
                if(line.contains("Parallel execution timed out")) isTimedOut = true;
            }
            isr.close();
            reader.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return isTimedOut;
    }


    public static ArrayList<String> bash(String[] cmd, String dir) {
        ArrayList<String> result = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            if(dir != null)
                builder.directory(new File(dir));
            Process process = builder.start();
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            isr.close();
            reader.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Delete tmpDir
     *
     * @param dir
     */
    public static void delete(String dir, String ID) {
        String[] cmd = new String[]{"rm", "-rf", dir};
        bash(cmd, null, ID);
    }


}
