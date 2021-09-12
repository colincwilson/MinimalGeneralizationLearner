import java.io.*;
import java.util.*;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
// https://mvnrepository.com/artifact/org.yaml/snakeyaml/1.29

public class LearnerCommandline {

    /* * * * * Copied from LearnerFrame * * * * */
    String dir, filename;
    LearnerTask learnerTask;
    Grammar outputGrammar;

    String featuresFileName;

    boolean phonologyOnFlag = true;
    boolean intermediateWugTests = false;
    boolean wugtimeLearning = true;
    boolean useFeatures = true;
    boolean useDoppelgaengers = true;
    boolean useImpugnment = true;
    boolean saveConFile = false;
    boolean saveC75File = true;
    boolean saveC90File = true;
    boolean saveScopeFile = true;
    boolean saveRawRelFile = true;
    boolean saveHitsFile = true;
    boolean saveWeightedFile = true;
    boolean saveFreqFile = false;
    boolean saveWeightedByFreqFile = false;
    boolean batch = false;
    boolean smallInitial = false;

    // readInputFile is called when we're opening a file, to read in the info
    private boolean readInputFile(String dir, String filename) {
        BufferedReader in;
        BufferedReader illegal;
        BufferedReader features;

        // the things we'll find in the file:
        String user_name = new String(); // the user's name
        String language_name = new String(); // the language name
        String creation_date = new String();
        String modification_date = new String();
        String global_comments = new String(); // for the global comments textarea
        String nextline = new String();

        // taking care of features:
        StringTokenizer st;
        String token = new String();
        int highest = 0;

        // this.learnerTask = new LearnerTask(filename);
        this.learnerTask = new LearnerTask(dir + filename.substring(0, filename.length() - 3));
        try {
            // now we try reading the validity check
            in = new BufferedReader(new FileReader(dir + filename));
            // in = new DataInputStream(new FileInputStream( dir + filename));

            System.out.println("File: " + dir + filename);
            String firstline = new String(in.readLine());

            boolean valid_file = checkFirstLine(firstline);
            if (!valid_file) {
                System.out.println("Invalid input file! Please try again...");
                return false;
            }

            nextline = in.readLine();
            if (!nextline.equals("Input forms:")) {
                user_name = nextline;
            }
            nextline = in.readLine();
            if (!nextline.equals("Input forms:")) {
                language_name = nextline;
            }

            nextline = in.readLine();
            if (!nextline.equals("Input forms:")) {
                creation_date = nextline;
            }
            nextline = in.readLine();
            if (!nextline.equals("Input forms:")) {
                modification_date = nextline;
            }
            nextline = in.readLine();
            if (!nextline.equals("Input forms:")) {
                global_comments = nextline;
            }

            // boolean use_phonology_flag = in.readLine(); // dunno how boolean vals work in
            // files yet

            // now read in the learner task
            // first we read in a line to make sure the mcats are coming:

            nextline = in.readLine();

            // see if it checks out:
            if (nextline.equals("Morphological categories:")) {
                String stringOfMCats = new String(in.readLine());
                // ship off the string to be made into the MCats array
                learnerTask.setMCats(stringOfMCats);
            }

            // probably want to do more sophisticated checking to ensure that we aren't
            // breezing by the input forms here...
            if (!nextline.equals("Input forms:"))
                nextline = in.readLine();

            // now make sure input forms are coming up
            if (nextline.equals("Input forms:")) {
                System.out.println("Found the input forms.");
                int counter = 0;
                // we'll keep reading in inputs until we hit the test forms
                nextline = in.readLine();
                while (!nextline.equals("Test forms:")) {
                    // now send it to be entered as an array in the input paradigms
                    learnerTask.addInputDeclension(counter, nextline);
                    counter++;

                    nextline = in.readLine();
                }
                // when we're done, have learnerTask remember how many forms we actually had
                // (this is sort of useless, we just use it as a check)
                learnerTask.setFormsCount(counter);
            } else
                System.out.println("No input forms found.");

            // once we run out of input forms (or never had any to begin with)
            // we are ready to try reading in the test forms
            if (nextline.equals("Test forms:")) {
                System.out.println("Found the test forms.");
                int counter = 0;

                // now we'll read forms until we hit the end of the file
                nextline = in.readLine();
                while (!nextline.equals("Illicit sequences:") && !nextline.equals("end"))
                // && !nextline.equals( "end" ) )

                {
                    // send it to be entered as a string in the learner task
                    learnerTask.addTestForm(counter, nextline);
                    // System.out.println(" And it's official; test form " + (counter+1) + " is now
                    // " + learnerTask.test_forms[counter] );
                    counter++;

                    // System.out.print(" (The first test form is: " + learnerTask.test_forms[0] +
                    // ")\r");

                    nextline = in.readLine();
                }
                learnerTask.setTestFormsCount(counter);
            } else
                System.out.println("No test forms found.");

            if (nextline.equals("Illicit sequences:")) {
                System.out.println("Reading illicit sequences.");

                nextline = in.readLine();
                while (!nextline.equals("end")) {
                    learnerTask.addIllicitSequence(nextline);
                    nextline = in.readLine();
                }
            }

            // [CW] comment out GUI
            // if we've gotten to this point, it means that a file has successfully
            // been read. now open a new learner window and display the info
            // setTitle(language_name);

            // textFieldUserNameDisplay.setText( user_name );
            // labelUserNameDisplay.setText(user_name);
            // textFieldLanguageNameDisplay.setText( language_name );
            // labelLanguageNameDisplay.setText(language_name);
            // labelFileNameDisplay.setText(filename);
            // labelCreateDateDisplay.setText( creation_date );
            // labelLastModifiedDisplay.setText( modification_date );
            // textAreaGlobalComments.setText( global_comments );
        }

        catch (FileNotFoundException e) {
            System.err.println("Can't open " + filename);
            return false;
        } catch (IOException e) {
            System.err.println("Error reading " + filename);
            return false;
        }

        if (phonologyOnFlag) {
            try {
                // next, we read in the illicit sequences from an .ill file:
                illegal = new BufferedReader(
                        new FileReader(dir + filename.substring(0, filename.length() - 3) + ".ill"));
                // illegal = new DataInputStream(new FileInputStream( dir +
                // filename.substring(0, filename.length() - 3) + ".ill"));
                System.out.println("Reading illicit sequences.");

                nextline = illegal.readLine();
                while (!nextline.equals("end")) {
                    learnerTask.addIllicitSequence(nextline);
                    nextline = illegal.readLine();
                }

            } catch (FileNotFoundException e) {
                System.err.println("WARNING: can't open illegal sequences file: "
                        + filename.substring(0, filename.length() - 3) + ".ill");
            } catch (IOException e) {
                System.err.println("Error reading illegal sequences file: "
                        + filename.substring(0, filename.length() - 3) + ".ill");
            }
        }
        if (useFeatures) {
            try {
                // now we read in the features

                // the first thing we need to do is see how high the values of the characters
                // used for
                // segments can get, because we need to make an array that contains them all;
                // ideally
                // this would be ASCII values and would stay under 256, but in the Java world
                // this is
                // Unicode values and can get to 65,000. the easiest thing to do here is to go
                // through the file
                // once first and see how big the array will need to be (i.e., what the value of
                // the highest
                // character used is). This could be VERY wasteful, because we could have a
                // bunch of characters
                // in the 0-255 range and then one in the 50,000 range, necessitating a 50,000
                // array.
                // BUT: speed difference should make it worth it, and we can be somewhat austere
                // in choosing
                // low characters if need by (via a funnybet)

                if (featuresFileName == null) {
                    featuresFileName = dir + filename.substring(0, filename.length() - 3) + ".fea";
                }

                features = new BufferedReader(new FileReader(featuresFileName));
                // ignore the first line:
                nextline = new String(features.readLine());
                // now read & ignore the "abbreviations" line:
                nextline = features.readLine();
                // now start the features lines
                nextline = features.readLine();
                while (nextline != null) {
                    // tokenize the line to get the segment
                    st = new StringTokenizer(nextline);
                    // the first thing is the (no obsolete) ascii value
                    token = st.nextToken();
                    // then the segment itself:
                    token = st.nextToken();

                    // if (Character.getNumericValue(token.charAt(0)) > highest)
                    if (token.charAt(0) > highest) {
                        // highest = Character.getNumericValue(token.charAt(0));
                        highest = token.charAt(0);
                    }
                    nextline = features.readLine();
                }
                // now theoretically "HIGHEST" should be the dimensions of the feature matrix
                // array.
                // we can close the file and reopen it, reading the values for real this time.
                learnerTask.initializeFeatureMatrix(highest + 1);
                features.close();

                features = new BufferedReader(
                        new FileReader(dir + filename.substring(0, filename.length() - 3) + ".fea"));
                // features = new DataInputStream(new FileInputStream( dir +
                // filename.substring(0, filename.length() - 3) + ".fea"));
                // ignore the first line:
                nextline = new String(features.readLine());
                // now read the "abbreviations" line:
                nextline = features.readLine();

                learnerTask.setFeatureNames(nextline);

                nextline = features.readLine();
                while (nextline != null) {
                    learnerTask.addSegment(nextline);
                    nextline = features.readLine();
                }

                // learnerTask.printFeatureMatrix();
                // learnerTask.printBounds();
                // now for a check: print out the bounds
                // learnerTask.printBounds();
                learnerTask.setBinarity();

                System.out.flush();

            }

            catch (FileNotFoundException e) {
                System.err.println(
                        "WARNING: can't open features file: " + filename.substring(0, filename.length() - 3) + ".fea");
            } catch (IOException e) {
                System.err.println("Error reading " + filename.substring(0, filename.length() - 3) + ".fea");
            }
        }
        return true;
    } // end of displayInfo method

    public void startTheLearner() {
        boolean success;
        // [CW] comment out GUI
        // DoneDialog done;
        String start_date = new String(getNewCreationDate());
        System.out.println("OK, starting the learner at " + start_date + "...");
        outputGrammar = new Grammar(learnerTask);
        System.out.print("   Attempting to learn " + outputGrammar.learnerTask.realNumberOfForms);
        System.out.print(" forms, and to derive " + outputGrammar.learnerTask.test_forms.length + " test forms.\r\r");

        System.out.print("   Phonology is: ");
        if (phonologyOnFlag)
            System.out.print("ON\r");
        else
            System.out.print("OFF\r");
        System.out.println(); // [CW] add
        System.out.flush();
        outputGrammar.setPhonology(phonologyOnFlag);
        outputGrammar.setIntermediateWugTests(intermediateWugTests);
        outputGrammar.setWugtimeLearning(wugtimeLearning);
        outputGrammar.setSmallInitial(smallInitial);
        outputGrammar.setDoppelgaenging(useDoppelgaengers);
        outputGrammar.setSaveConstraints(saveConFile);
        outputGrammar.setSaveC75File(saveC75File);
        outputGrammar.setSaveC90File(saveC90File);
        outputGrammar.setSaveRawRelFile(saveRawRelFile);
        outputGrammar.setSaveHitsFile(saveHitsFile);
        outputGrammar.setSaveWeightedFile(saveWeightedFile);
        outputGrammar.setImpugnment(useImpugnment);
        outputGrammar.setSaveFreqFile(saveFreqFile);
        outputGrammar.setSaveWeightedByFreqFile(saveWeightedByFreqFile);

        success = outputGrammar.learn();

        System.out.print(getNewCreationDate());
        System.out.flush();

        // [CW] comment out GUI
        // if (!batch)
        // done = new DoneDialog(this);
    }

    // __________________________________________________________________________________
    String getNewCreationDate() {
        // we'll need to set a new creation date, so here's a variable for that
        Date new_create_date;
        new_create_date = new Date();
        return new_create_date.toString();
    }

    // __________________________________________________________________________________
    boolean checkFirstLine(String firstline) {
        if (firstline.equals("Phonological Learner File"))
            return true;
        else
            return false;
    }
    /* * * * * (end copy) * * * * */

    // Initialize from yaml config file
    public void initComponents(String configFile) throws Exception {
        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(new FileInputStream(configFile));
        System.out.println(config);

        // Boolean settings
        if (config.containsKey("phonologyOn"))
            phonologyOnFlag = (boolean) config.get("phonologyOn");
        if (config.containsKey("intermediateWugTests"))
            intermediateWugTests = (boolean) config.get("intermediateWugTests");
        if (config.containsKey("wugtimeLearning"))
            wugtimeLearning = (boolean) config.get("wugtimeLearning");
        if (config.containsKey("useFeatures"))
            useFeatures = (boolean) config.get("useFeatures");
        if (config.containsKey("useDoppelgaengers"))
            useDoppelgaengers = (boolean) config.get("useDoppelgaengers");
        if (config.containsKey("useImpugnment"))
            useImpugnment = (boolean) config.get("useImpugnment");
        if (config.containsKey("saveConFile"))
            saveConFile = (boolean) config.get("saveConFile");
        if (config.containsKey("saveC75File"))
            saveC75File = (boolean) config.get("saveC75File");
        if (config.containsKey("saveC90File"))
            saveC90File = (boolean) config.get("saveC90File");
        if (config.containsKey("saveScopeFile"))
            saveScopeFile = (boolean) config.get("saveScopeFile");
        if (config.containsKey("saveRawRelFile"))
            saveRawRelFile = (boolean) config.get("saveRawRelFile");
        if (config.containsKey("saveHitsFile"))
            saveHitsFile = (boolean) config.get("saveHitsFile");
        if (config.containsKey("saveWeightedFile"))
            saveWeightedFile = (boolean) config.get("saveWeightedFile");
        if (config.containsKey("saveFreqFile"))
            saveFreqFile = (boolean) config.get("saveFreqFile");
        if (config.containsKey("saveWeightedByFreqFile"))
            saveWeightedByFreqFile = (boolean) config.get("saveWeightedByFreqFile");
        if (config.containsKey("batch"))
            saveWeightedByFreqFile = (boolean) config.get("batch");
        if (config.containsKey("smallInitial"))
            saveWeightedByFreqFile = (boolean) config.get("smallInitial");

        // Input file directory and filename options
        String inputDirectory = "";
        String inputFilename = "";
        if (config.containsKey("inputDirectory"))
            inputDirectory = (String) config.get("inputDirectory");
        else
            System.out.println("inputDirectory must be specified in config file");
        if (config.containsKey("inputFilename"))
            inputFilename = (String) config.get("inputFilename");
        else
            System.out.println("inputFilename must be specified in config file");

        // Read inputs and learn
        readInputFile(inputDirectory, inputFilename);
        startTheLearner();
    }

    static public void main(String[] args) throws Exception {
        LearnerCommandline lc = new LearnerCommandline();
        lc.initComponents(args[0]);
    }
}
