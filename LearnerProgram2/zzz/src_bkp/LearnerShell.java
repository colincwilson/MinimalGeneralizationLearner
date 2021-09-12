
import java.awt.*;
import java.io.*;
import java.util.*;
//import com.mw.Profiler.*;

public class LearnerShell extends Frame {
    boolean success;

    // __________________________________________________________________________________

    public static void main(String args[]) {
        // start with a new, blank learning task
        String untitled = "Untitled";

        // now set up an untitled learner window
        Learner learner = new Learner();
        // learner.setup(); // CW

    } // end of main method

    /*
     * public static void main( String args[] ) { // start with a new, blank
     * learning task Profiler.Init(200,10); String untitled = "Untitled";
     * 
     * // now set up an untitled learner window Learner learner = new Learner();
     * 
     * Profiler.StartProfiling(); learner.setup();
     * 
     * Profiler.StopProfiling(); Profiler.Dump("LearnerData.prof");
     * Profiler.Terminate(); } // end of main method
     */
}