import java.util.*;
import java.awt.*;

public class LearnerTask {
  // the learning task includes a set of morphological categories, and real and
  // test forms;
  // for both real and test forms, the number of items in the declension equals
  // the number
  // of morphological categories

  String MCats[];
  InputParadigm input_paradigms[];
  int realNumberOfForms;
  int realNumberOfTestForms;
  String test_forms[];
  String illicit[];

  String featureNames[];
  int numberOfFeatures;
  String segments[];

  // Hashtable featureMatrix;
  int featureMatrix[][];
  int featureBounds[][];
  boolean binary[];

  String outputfile;

  String language;

  // the constructor
  public LearnerTask() {
    this("Untitled");
  }

  public LearnerTask(String lang) {
    language = new String(lang);
    // outputfile = new String( lang + ".out");
    MCats = new String[0];
    input_paradigms = new InputParadigm[0];
    test_forms = new String[0];
    illicit = new String[0];

    numberOfFeatures = 0;
    // segments = new String[256];
    // for (int i=0; i< 256; i++)
    // segments[i] = new String();

    // THREE APPROACHES TO THE FEATURE MATRIX:
    // (1) a 256 row array (naive approach that assumed ascii values; doesn't work
    // because unicode values go to 65,536
    /*
     * featureMatrix = new int[256][]; for (int i=0; i<256; i++) featureMatrix[i] =
     * new int[0];
     */

    // (2) a hash table (better for handle sparse assortment of indices between
    // 0 and 65,536 but unfortunately really SLOOOOOOOW)
    // featureMatrix = new Hashtable( 100 );

    // (3) an array which is only as large as empirically determined by inspecting
    // the features file for the highest unicode value of a segment employed there.
    // ***in this case, we don't know how big it will be quite yet, gotta
    // wait until we start adding the segments
    // ???? need this? featureMatrix = new int[0][];

    featureNames = new String[0];

  }

  // __________________________________________________________________________________
  public void setLanguage(String lang) {
    language = lang;
    return;
  }

  // __________________________________________________________________________________
  // the setMCats method takes a string of tab-delimited categories and loads that
  // into the MCats array
  public void setMCats(String stringOfMCats) {
    // this string tokenizer breaks up the input string
    StringTokenizer st = new StringTokenizer(stringOfMCats);
    // now we can initialize the MCats array to the correct size:
    MCats = new String[st.countTokens()];

    System.out.println("We have " + MCats.length + " morphological categories.");

    // also we now know the input paradigms & test forms need that many columns;
    // we'll start by assuming there's one of each, we can grow later
    input_paradigms = new InputParadigm[1];

    System.out.println("We start by assuming at least " + input_paradigms.length + " input declension.");

    for (int i = 0; i < input_paradigms.length; i++) {
      // System.out.println("Setting up input form " + i );
      input_paradigms[i] = new InputParadigm();
      // System.out.println("Now initializing declension for form " + i );
      input_paradigms[i].declension = new String[MCats.length];
    }
    test_forms = new String[100];

    // now we go through and store the tokens in the MCats array:
    for (int i = 0; i < st.countTokens(); i++)
      MCats[i] = st.nextToken();
  } // end of setMCats method

  // __________________________________________________________________________________
  // addInputDeclension takes an integer n, and a declension given as a
  // tab-delimited
  // string, and adds it to the input_paradigms array, as the nth element.
  public void addInputDeclension(int n, String inputDeclensionString) {
    // as in setMCats, we'll need to do some string tokenizing:
    StringTokenizer st;
    String temp_string;
    InputParadigm temporary_storage[]; // temporary storage if we need to grow
    String new_inflection;

    // System.out.println ("Adding declension: [" + inputDeclensionString + "]");
    // when we add new input paradigms, we might need to expand the array first;
    if (n > (input_paradigms.length - 1)) { // we'll copy input_paradigms into a slightly larger temporary array
      int new_size = (n + 1200);
      temporary_storage = new InputParadigm[new_size];
      // System.out.println ("Increasing Input Paradigms array from " +
      // input_paradigms.length +
      // " to " + temporary_storage.length + ".");
      for (int i = 0; i < n; i++) {
        temporary_storage[i] = new InputParadigm();
        temporary_storage[i].declension = new String[MCats.length];
      }

      for (int i = 0; i < input_paradigms.length; i++) {
        System.arraycopy(input_paradigms, 0, temporary_storage, 0, input_paradigms.length);

        /*
         * for ( int j=0 ; j < MCats.length ; j++ ) { temporary_storage[i].declension[j]
         * = input_paradigms[i].declension[j]; }
         */
        temporary_storage[i].frequency = input_paradigms[i].frequency;
        temporary_storage[i].gloss = input_paradigms[i].gloss;
        temporary_storage[i].comments = input_paradigms[i].comments;
      }

      // now replace the input_paradigms with the bigger one
      input_paradigms = temporary_storage;
    }
    // initialize the new declension:
    input_paradigms[n] = new InputParadigm(MCats.length);

    // now tokenize the input declension:
    st = new StringTokenizer(inputDeclensionString);

    // now we add the new string to the input_paradigms array, as the nth element:
    for (int i = 0; i < MCats.length; i++) {
      new_inflection = new String(st.nextToken());
      // System.out.println ( "Adding inflection " + i + ": " + new_inflection );
      input_paradigms[n].declension[i] = new String(new_inflection);
    }

    findfreqandgloss: {
      // if there's another token, it might be a frequency
      if (st.hasMoreTokens()) {
        temp_string = st.nextToken();

        // should really check here to see if this produced something reasonable:
        try {
          input_paradigms[n].frequency = Integer.parseInt(temp_string);
        } catch (NumberFormatException e) {
          // oops, it must really be the gloss
          input_paradigms[n].gloss = temp_string;
          break findfreqandgloss;
        }
      }

      // if there's another token in st, it's the gloss
      if (st.hasMoreTokens())
        input_paradigms[n].gloss = st.nextToken();

    } // end of findfreqandgloss

    // if there's anything left, it's the comments
    if (st.hasMoreTokens()) {
      while (st.hasMoreTokens()) {
        // add all these extra tokens to the comments
        input_paradigms[n].comments.append(st.nextToken());
      }

    } // end of adding comments
  } // end of addInputDeclension method

  // __________________________________________________________________________________
  // addTestForm does the same thing as addInputDeclension, but for test forms;
  // this is probably an extra amount of bother since in most cases the test form
  // is just one string -- but larger tasks may want to do this...
  public void addTestForm(int n, String testDeclensionString) {

    // at the moment, we'll assume test forms are just a single string of the 1st
    // MCat
    // StringTokenizer st = new StringTokenizer( testDeclensionString );

    String temporary_storage[]; // room to grow if need be

    // System.out.println( "Adding test form: [" + testDeclensionString + "]" );
    // when we add new test forms, we may need to expand the array first:
    if (n > (test_forms.length - 1)) {
      // we'll make a new, slightly larger temporary array
      int new_size = (n * 3);
      temporary_storage = new String[new_size];
      // System.out.println ("Increasing Test Forms array from " + test_forms.length +
      // " to " + temporary_storage.length + "." );

      // copy the stuff from test_forms into it
      for (int i = 0; i < test_forms.length; i++) {
        temporary_storage[i] = new String(test_forms[i]);
      }
      // also initialize the last element:
      temporary_storage[test_forms.length] = new String();
      // now replace test_forms with the new, expanded array
      test_forms = temporary_storage;
    } // end of expanding the test_forms array

    // now we know there is room for the new test form
    test_forms[n] = testDeclensionString;
    // System.out.println("Now the "+ (n+1) +"th test form is: " + test_forms[n]);
  } // end of addTestForm method
  // __________________________________________________________________________________

  public void addIllicitSequence(String seq) {
    String temp_seqs[];

    // System.out.println( "Adding illicit sequence: [" + seq + "]");
    temp_seqs = new String[illicit.length + 1];

    System.arraycopy(illicit, 0, temp_seqs, 0, illicit.length);
    temp_seqs[temp_seqs.length - 1] = new String(seq);

    illicit = temp_seqs;

  }

  // __________________________________________________________________________________
  public void initializeFeatureMatrix(int highest) {
    this.featureMatrix = new int[highest][];
    this.segments = new String[highest];
  }

  // __________________________________________________________________________________
  public void setFeatureNames(String feature_names) {
    /*
     * The goal here is to make an array of Strings which contains the names of the
     * features.
     */
    String temp_names[];

    StringTokenizer st = new StringTokenizer(feature_names);
    numberOfFeatures = st.countTokens();

    System.out.print("Reading values for " + numberOfFeatures + " features.\r");
    System.out.print("( ");

    temp_names = new String[numberOfFeatures];
    for (int i = 0; i < numberOfFeatures; i++) {
      temp_names[i] = new String(st.nextToken());

      System.out.print(i + "." + temp_names[i] + " ");
    }

    System.out.print(")\r");

    featureNames = temp_names;
    initializeBounds();

  } // end of setFeatureNames method

  // __________________________________________________________________________________
  public void addSegment(String segmentValues) {
    StringTokenizer st = new StringTokenizer(segmentValues);
    int ascii;
    String seg;
    int numberOfValues;
    int newval;
    int featureValues[] = new int[numberOfFeatures];

    // The featureBounds should always have been initialized first, because
    // addSegment is meant to be used _after_ the feature names have been
    // read in. However, Java doesn't know that, so we have to satisfy it
    // that featureBounds is _guaranteed_ to have been initialized:

    if (featureBounds == null)
      initializeBounds();

    // the first token is the ASCII value of this segment-- for now we'll just
    // ignore this first ascii value, and use the real one computed
    // from the string itself

    ascii = Integer.parseInt(st.nextToken()); // 8/6/99 going back to arrays

    /*
     * if (ascii>255) { // not a legal ASCII! just return, i guess
     * System.out.println("WARNING: illegal ascii character: " + ascii); return; }
     */
    // now we read in the String value associated with this segment
    seg = new String(st.nextToken());
    ascii = seg.charAt(0);
    // ascii = Character.getNumericValue( seg.charAt(0) ); // can't remember why not
    // just .charAt()
    /*
     * ascii = seg.charAt(0); // this part is false now because we are in
     * unicode-land if (ascii>255 || ascii < 0) { // not a legal ASCII! just return,
     * i guess System.out.println("WARNING: illegal ascii character: ["+seg+"] (" +
     * ascii+")"); return; }
     */
    try {
      this.segments[ascii] = new String(seg);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.print("Trying to add segment " + ascii + ", but only room for " + segments.length + ".\r");
    }
    System.out.flush();
    // let's add some code to check for writing over a previously added value

    /*
     * // DUPLICATE CHECK FOR USING HASH TABLE if (featureMatrix.get(seg) != null) {
     * System.out.println("Warning: trying to add [" + seg +
     * "] to the feature matrix twice."); return; }
     */

    // DUPLICATE CHECK FOR USING ARRAY
    if (featureMatrix[ascii] != null) {
      System.out.println("Warning: trying to add [" + seg + "] to the feature matrix twice.");
      return;
    }

    // System.out.println("Adding segment [" + segments[ascii] + "] (ASCII = " +
    // ascii + ")");

    numberOfValues = st.countTokens();
    // now in theory, the number of values should equal the number of features!
    // i'm not sure what to do if they aren't -- i guess we'll be trusting for
    // now, but maybe a warning would be good if they're not equal...
    // if ( (numberOfValues + 1) != numberOfFeatures )
    if (numberOfValues != numberOfFeatures) {
      System.out.println("WARNING: number of values (" + numberOfValues + ") for segment w/ASCII value " + ascii
          + " does not equal the number of features ( " + numberOfFeatures + ").");
      numberOfValues = (numberOfValues < numberOfFeatures ? numberOfValues : numberOfFeatures);
    }
    for (int i = 0; i < numberOfValues; i++) {
      // this could throw an exception which i'm not catching right now,
      // if the value isn't really an integer.
      featureValues[i] = Integer.parseInt(st.nextToken());
      newval = featureValues[i];

      /*
       * // hashtable version: newval = Integer.parseInt(st.nextToken());
       * featureValues.setFeature( i, newval );
       */
      // now see if this value could serve as a bound:
      // first, lower the lower bound if need be
      // 2/11/99 WARNING: we need to make sure the lower bound never goes below 0!
      // make sure we don't set it to -1:
      if (-1 < newval && newval < featureBounds[0][i])
        featureBounds[0][i] = newval;
      // then increase the upper bound
      if (newval > featureBounds[1][i])
        featureBounds[1][i] = newval;

    }
    // featureMatrix.put( seg, featureValues ); // hash table version!

    this.featureMatrix[ascii] = featureValues;

  }

  // __________________________________________________________________________________
  public void initializeBounds() {
    // This method is meant to be called once, once the feature names have
    // been read in. It simply initializes the featureBounds variable, once the
    // number of features is known
    featureBounds = new int[2][numberOfFeatures];
    binary = new boolean[numberOfFeatures];

    // i think we need to initialize everything in the featureBounds array
    // at the beginning, so here goes...

    for (int i = 0; i < numberOfFeatures; i++) {
      // we'll set the min too high...
      featureBounds[0][i] = 10;
      // and the max too low
      featureBounds[1][i] = 0;

      binary[i] = true;
    }

  }

  // __________________________________________________________________________________
  public void printBounds() {
    // this method just prints out the bounds for the features, which will be
    // used later to determine if a feature is "active" or not

    System.out.print("Feature bounds: \r");
    for (int i = 0; i < numberOfFeatures; i++) {
      System.out.println("\t" + featureNames[i] + "\t" + "(" + featureBounds[0][i] + "," + featureBounds[1][i] + ")");
    }
    System.out.print("\r");
  }

  public void printFeatureMatrix() {
    /*
     * // HASH TABLE-BASED VARS: Enumeration segments = featureMatrix.keys(); String
     * current_segment = new String(); FeatureDecomposition current_row;
     */

    System.out.print("Feature matrix: \rSeg\t");
    for (int i = 0; i < numberOfFeatures; i++) {
      System.out.print(featureNames[i] + "\t");
    }
    System.out.print("\r");

    /*
     * while( segments.hasMoreElements() ) { current_segment = (String)
     * segments.nextElement(); current_row = (FeatureDecomposition)
     * featureMatrix.get( current_segment ); System.out.print( current_segment +
     * "\t" ); // for (int j = 0; j<numberOfFeatures; j++) // (actually, this way
     * should be the same, but safer:) for (int j = 0;
     * j<current_row.features.length; j++) System.out.print(
     * String.valueOf(current_row.features[j]) + "\t"); System.out.print("\r"); }
     */
    // the array-based feature matrix:
    for (int i = 0; i < featureMatrix.length; i++) {
      try {
        if (featureMatrix[i].length != 0) {
          System.out.print(i + "\t" + segments[i] + "\t");
          for (int j = 0; j < numberOfFeatures; j++)
            System.out.print(featureMatrix[i][j] + "\t");
          System.out.print("\r");
        }
      } catch (NullPointerException e) {
        // just means no segment had that value, so no row ever got
        // initialized there in the array...
      }
    }
    System.out.println("Height of feature matrix: " + featureMatrix.length);

  }

  public void printFeatureMatrix(char seg) {

    // HT Integer[] feature_values = (Integer[]) featureMatrix.get(
    // String.valueOf(seg));

    System.out.print("Feature values for " + seg + ":\r");
    if (featureMatrix[seg].length != 0) {
      // if (feature_values != null)
      // {
      for (int j = 0; j < numberOfFeatures; j++)
        System.out.print(featureMatrix[seg][j] + "\t");

      // HT for (int j = 0; j<feature_values.length; j++)
      // HT System.out.print( String.valueOf(feature_values[j]) + "\t");

      System.out.print("\r");

    } else
      System.out.println("Warning! tried to print features for unknown segment: [" + String.valueOf(seg) + "]");

  }

  public void setBinarity() {
    for (int i = 0; i < numberOfFeatures; i++) {
      if (featureBounds[1][i] > 1)
        binary[i] = false;
    }
  }

  public void setFormsCount(int n) {
    realNumberOfForms = n;
  }

  public void setTestFormsCount(int n) {
    realNumberOfTestForms = n;
  }
} // end of LearnerTask class
