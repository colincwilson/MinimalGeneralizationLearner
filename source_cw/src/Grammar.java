import java.util.*;
import java.io.*;

public class Grammar
{
 LearnerTask learnerTask;
 MappingConstraint mapping_constraints[];
 PhonologicalRule phonological_rules[];
 boolean intermediateWugTests;
 boolean phonologyOnFlag;
 boolean useFeatures;
 int numberOfFeatures;
 
 // some handy mnemonic guys:
 final int YES = 1, NO = 0, NA = -1;
 final int SUFFIX = 0, PREFIX = 1, INFIX = 2, ABLAUT = 3, SUPPLETION = 4;
 final int INACTIVE = -2;
 boolean verbose = false;
 boolean debug = false;
// boolean fullcompare = false;
 boolean stillLearning;
 boolean wug_learning_on = true;
 boolean doppelgaengers = true;
 boolean homophoneprotection = false;
 boolean saveconstraints = true;
 boolean saveC75File = true;
 boolean saveC90File = true;
 boolean saveRawRelFile = true;
 boolean saveHitsFile = true;
 boolean useImpugnment = true;
 boolean saveWeightedFile = true;
 boolean saveFreqFile = false; // for now this will control both freq and rel_freq files
 boolean saveWeightedByFreqFile = false;
 boolean smallinitial = false;
 
 int newestform;
 int newestconstraint;
 int firstWugtimeConstraint;
 
 // We'll need a way of indexing rows in the tableaux, and it might also
 // be useful to see what words the learner has already considered -- so the 
 // the "known_forms" array is something like a lexicon-- a collection of the 
 // mappings which it has already considered.
 
 char known_forms[][][];
 int related_forms[][];
 int exceptions[][];
 int frequencies[];

 // also an efficiency measure: keep a separate list of all the known struc changes
 // (the A->B's) and a list of which mapping constraints use them-- this means that
 // we don't have to do nearly as much searching to find which constraints to
 // generalize against.
 
 // I'm going to do this as a hashtable, with the struc desc's as the keys
 // and arrays of integers as the values.  (This will be slightly a pain
 // because we have to use wrapper classes to put them into the hastable)

Hashtable changes; 
// HashMap changes; 

 // 3/29/99 also, a hashtable to keep an index of constraints sorted by scope.
 // this sounds really inefficient, but it will make it easier to figure out
 // what the most general derivors are for a test form.  I will not attempt to 
 // keep it up-to-date, but rather we can clone the changes hashtable after it's 
 // already been sorted.  

 Hashtable changes_by_scope;
 Hashtable changes_by_c75;
 Hashtable changes_by_c90;
 Hashtable changes_by_raw_rel;
 Hashtable changes_by_hits;
 Hashtable changes_by_weighted;
 Hashtable changes_by_freq;
 Hashtable changes_by_weighted_by_freq;
 Hashtable changes_by_rel_freq;
  
 // and bookkeeping: 
 Hashtable changes_index;

/*
 HashMap changes_by_scope;
 HashMap changes_by_c75;
 HashMap changes_by_c90;
 HashMap changes_by_raw_rel;
 HashMap changes_by_hits;
 HashMap changes_by_weighted;
 HashMap changes_by_freq;
 HashMap changes_by_weighted_by_freq;
 HashMap changes_by_rel_freq;
  
 // and bookkeeping: 
 HashMap changes_index;
*/
 DataOutputStream constraintsfile;
 DataOutputStream rulesfile;
 PrintWriter outputfile;
 PrintWriter summaryfile;
 
 PrintWriter c75file;
 PrintWriter c90file;
 PrintWriter rawrelfile;
 PrintWriter hitsfile;
 PrintWriter weightedfile;
 PrintWriter freqfile;
 PrintWriter relfreqfile;
 PrintWriter weightedbyfreqfile;


 // the constructor takes a LearnerTask as an argument
 public Grammar ( LearnerTask task )
 {
 
	System.out.println("setting up a new grammar"); 
	System.out.flush();
 
	this.learnerTask = new LearnerTask( task.language );
	this.learnerTask = task;

	System.out.println("setting up a new grammar"); 
	System.out.flush();

	if (smallinitial)
	{
		mapping_constraints = new MappingConstraint[30000];
		related_forms = new int[30000][21];
		exceptions = new int[30000][21];
	}
	else
	{
		mapping_constraints = new MappingConstraint[120000];
		related_forms = new int[120000][21];
		exceptions = new int[120000][21];
  	}
	System.out.println("successfully installed " + mapping_constraints.length + " mapping constraints."); 
	System.out.flush();

  if (useFeatures)
   mapping_constraints[0] = new MappingConstraint( learnerTask.MCats.length, learnerTask.numberOfFeatures );  
  else
   mapping_constraints[0] = new MappingConstraint( learnerTask.MCats.length );
  
  // we start with no rules, no forms, and no violations   (ah, bliss...)
  phonological_rules = new PhonologicalRule[0];
  known_forms = new char[1][1][0];
  frequencies = new int[1];
  
  System.out.println("setting up hash tables");
  System.out.flush();

  changes = new Hashtable( 60 );
  changes_index = new Hashtable( 60 );
  // we won't use the changes_by_scope until more or less the end, but initialize it now
  changes_by_scope = new Hashtable( 60 );
  changes_by_c75 = new Hashtable( 60 );
  changes_by_c90 = new Hashtable( 60 );
  changes_by_raw_rel = new Hashtable( 60 );
  changes_by_hits = new Hashtable( 60 );
  changes_by_weighted = new Hashtable( 60 );
  changes_by_freq = new Hashtable( 60 );
  changes_by_weighted_by_freq = new Hashtable( 60);
  changes_by_rel_freq = new Hashtable(60);

/*  
  changes = new HashMap( 60 );
  changes_index = new HashMap( 60 );
  // we won't use the changes_by_scope until more or less the end, but initialize it now
  changes_by_scope = new HashMap( 60 );
  changes_by_c75 = new HashMap( 60 );
  changes_by_c90 = new HashMap( 60 );
  changes_by_raw_rel = new HashMap( 60 );
  changes_by_hits = new HashMap( 60 );
  changes_by_weighted = new HashMap( 60 );
  changes_by_freq = new HashMap( 60 );
  changes_by_weighted_by_freq = new HashMap( 60 );
  changes_by_rel_freq = new HashMap( 60 );
 */ 
  System.out.println("done with hash tables");
  
  System.out.flush();
  
  newestform = 0;
  newestconstraint = 0;  
  
  intermediateWugTests = false;
  phonologyOnFlag = true;
  useFeatures = true;
  numberOfFeatures = learnerTask.numberOfFeatures;
  stillLearning = true;
  wug_learning_on = true;
  doppelgaengers = true;
  useImpugnment = true;
  homophoneprotection = false;
  saveconstraints = true;
  
  saveC75File = true;
  saveC90File = true;
  saveRawRelFile = true;
  saveHitsFile = true;
  saveWeightedFile = true;
  saveFreqFile = false;
  saveWeightedByFreqFile = false;
  
  try{
	rulesfile = new DataOutputStream(new FileOutputStream( learnerTask.language + ".rul" ));
	outputfile = new PrintWriter(new FileWriter(learnerTask.language + ".tmp" ));

  } catch (IOException e)
  {System.out.println("trouble initializing files");}
  
 }
 
 public void setPhonology (boolean phono )
 {
  phonologyOnFlag = phono;
 }

 public void activateFeatures (boolean value )
 {
  useFeatures = value;
  if (!useFeatures)
  	numberOfFeatures = 0;
 }
 
 public void setIntermediateWugTests( boolean wug)
 {
	intermediateWugTests = wug; 
 }
 
  public void setWugtimeLearning( boolean wug)
 {
	wug_learning_on = wug; 
 }
  public void setSmallInitial( boolean small)
 {
	smallinitial = small; 
 }
 
  public void setDoppelgaenging( boolean doppel)
 {
	doppelgaengers = doppel; 
 }
 
 public void setImpugnment( boolean impugn )
 {
 	useImpugnment = impugn;
 }
 
  public void setHomophoneProtection( boolean homophone)
 {
	homophoneprotection = homophone;
 }
  public void setSaveConstraints( boolean save)
 {
	saveconstraints = save; 
 }
 public void setSaveC75File( boolean save)
 {
 	saveC75File = save;
 }
 public void setSaveC90File( boolean save)
 {
 	saveC90File = save;
 }
 public void setSaveRawRelFile( boolean save)
 {
 	saveRawRelFile = save;
 }
 public void setSaveHitsFile( boolean save)
 {
 	saveHitsFile = save;
 }
 public void setSaveWeightedFile( boolean save )
 {
 	saveWeightedFile = save;
 }
 public void setSaveFreqFile( boolean save )
 {
 	saveFreqFile = save;
 }
 public void setSaveWeightedByFreqFile( boolean save )
 {
 	saveWeightedByFreqFile = save;
 }


 // __________________________________________________________________________________
 /*  Here is a "learn" method-- it takes the input forms one at a time
     and learns (from) them.  The basic scheme is that learning involves two steps:
     (1) listen to a datum, and (2) digest it.    We also store the form in a list 
     which acts as a sort of lexicon.
 
  In theory, there are two different ways to measure the learning process.
  We can either stop when there are no more forms to hear (NOT VERY REALISTIC),
  or we can learn for a set number of trials and have a special "Parent" module
  feed the learner its forms through the "listen" method.  (A STATISTICALLY MORE
  REALISTIC METHOD).
  
  For now, we'll use the "hear everything once" method, but this should be 
  changed eventually.
 */
 public boolean learn() // throws IOException 
 {  
  // we'll hear each form exactly once...
  // The input_paradigms array can actually be larger than the number of forms,
  // so we assume that the forms are all stored at the beginning of the array,
  // and once we hit an initialized member, we stop:
  try{
  
	PrintWriter tempfile;

  
  for (int formindex = 0; formindex < learnerTask.realNumberOfForms ; formindex++ )
  {

   if (verbose) {
    outputfile.write( "\n"+ formindex + "\tProcessing form:  [" + learnerTask.input_paradigms[formindex].declension[0] +
        "] --> [" + learnerTask.input_paradigms[formindex].declension[1] + "].\n");
   }
   System.out.print(".");
   outputfile.write(".");
    if ( formindex % 25 == 0)
    {
    	System.out.print( new StringBuffer("\n").append(formindex).append(" ("+newestconstraint+")\t").toString());
    	outputfile.write( new StringBuffer("\n").append(formindex).append(" ("+newestconstraint+")\t").toString());
    	System.gc();
    }
	if (intermediateWugTests)
	{
	    if (formindex > 0 && formindex % 500 == 0)
		{
		  System.out.print("\rOutputting intermediate wug test... ("+ formindex + ")\n");

		  outputfile = new PrintWriter(new FileWriter(learnerTask.language + "(" + formindex + ")" +".out" ));
		  summaryfile = new PrintWriter(new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".sum" ));
		  sortChanges();
		  sortChangesByScope();
		  if(saveHitsFile)
		  {
			  sortChangesByHits();		  
			  hitsfile = new PrintWriter(new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".hit" ));
		  }

		  if( saveC75File || saveC90File || saveHitsFile || saveRawRelFile || saveWeightedFile || saveFreqFile || saveWeightedByFreqFile)	
			  sortChangesByC75();
		  
		  if( saveC75File)	
		  {
			  c75file = new PrintWriter(new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".c75" ));
		  }
		  if( saveC90File )
		  {
			  sortChangesByC90();
			  c90file = new PrintWriter(new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".c90" ));
		  }
		  if( saveRawRelFile )
		  {
			  sortChangesByRawRel();
			  rawrelfile = new PrintWriter(new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".raw" ));
		  }
		  if( saveWeightedFile )
		  {
		  	sortChangesByWeighted();
			weightedfile = new PrintWriter(new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".wbl" ));
		  }
		  if( saveFreqFile || saveWeightedByFreqFile)
		  {
		  	sortChangesByFreq();
		  	sortChangesByRelFreq();
			freqfile = new PrintWriter(new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".frq" ));
			relfreqfile = new PrintWriter(new BufferedWriter( new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".rfrq" )));
		  }
		  if( saveWeightedByFreqFile )
		  {
		  	sortChangesByWeightedByFreq();
			weightedbyfreqfile = new PrintWriter(new FileWriter(learnerTask.language+ "(" + formindex + ")" + ".wbf" ));
		  }

		  if (useImpugnment)
		  {
			  System.out.print("... now impugning confidences.\n");
			  calculateImpugnedConfidences();
			  System.out.flush();
				outputfile.write("impugning...\n");
				outputfile.flush();
		
			  System.out.println("Done calculating impugned confidences at: "+ new Date());
		  }
		
		  System.out.println("Now calculating lower confidences");
		  outputfile.write("lower c75...\n");
		  outputfile.flush();
		  for (int c = 0; c < newestconstraint; c++)
		  {
				mapping_constraints[c].calculateLowerConfidence( );
				mapping_constraints[c].calculateOverallConfidence( useImpugnment );
				if (mapping_constraints[c].hits == 0)
					mapping_constraints[c].setKeep( false );
		  }
		  System.out.println("Done calculating lower confidences at: "+ new Date());
		
//		  if (saveFreqFile || saveWeightedByFreqFile)
//		  {
			  System.out.println("Now calculating relative token frequencies");
			  
			  outputfile.write("relative token frequencies...\n");
			  outputfile.flush();
			  for (int c = 0; c < newestconstraint; c++)
			  {
					mapping_constraints[c].calculateRelFrequency();
					mapping_constraints[c].calculateWeightedByFreq();
			  }
			  System.out.println("Done calculating relative token frequencies at: "+ new Date());  
//		  } 
		
		
		  if (saveWeightedFile)
		  {
			  System.out.println("Now calculating weighted confidences");
			  outputfile.write("weighted confidences...\n");
			  outputfile.flush();
			  for (int c = 0; c < newestconstraint; c++)
			  {
					mapping_constraints[c].calculateWeightedConfidence( .2 );
			  }
			  System.out.println("Done calculating weighted confidences at: "+ new Date());  
		  }  
		  System.out.flush();
		//  printViolationsGrid();
		  printPhonologicalRules();
		  // now sort the changes
		  System.out.println("Now sorting the constraints by confidence");
		  outputfile.write("sorting by confidence...\n");
		  outputfile.flush();
		  sortChanges();
		  System.out.println("Done sorting by confidence at: "+ new Date());
		
		
		  System.out.println("Now sorting the constraints by scope");
		    outputfile.write("sorting by scope...\n");
		  outputfile.flush();
		  sortChangesByScope();
		  System.out.println("Done sorting by scope at: "+ new Date());
		
		  if (saveHitsFile)
		  {
			  System.out.println("Now sorting the constraints by hits");
			  outputfile.write("sorting by hits...\n");
			  sortChangesByHits();
			  System.out.println("Done sorting by hits at: "+ new Date());
		  System.gc();
		  }
		  if( saveC75File || saveC90File || saveHitsFile || saveRawRelFile || saveWeightedFile || saveFreqFile || saveWeightedByFreqFile)	
		  {
			  System.out.println("Now sorting the constraints by c75");
			  outputfile.write("sorting by c75...\n");
			  sortChangesByC75();
			  System.out.println("Done sorting by c75 at: "+ new Date());
		  System.gc();
		  }
		  if (saveC90File)
		  {
			  System.out.println("Now sorting the constraints by c90");
			  outputfile.write("sorting by c90...\n");
		  	  sortChangesByC90();
			  System.out.println("Done sorting by c90 at: "+ new Date());
		  System.gc();
		  }
		  if (saveRawRelFile)
		  {
			  System.out.println("Now sorting the constraints by raw reliability");
			  outputfile.write("sorting by raw reliability...\n");
			  sortChangesByRawRel();
			  System.out.println("Done sorting by raw reliability at: "+ new Date());
		  System.gc();
		  }
		  if (saveWeightedFile)
		  {
			  System.out.println("Now sorting the constraints by weighted confidence");
			  outputfile.write("sorting by weighted confidence...\n");
			  sortChangesByWeighted();
			  System.out.println("Done sorting by weighted confidence at: "+ new Date());
		  
		  System.gc();
		  }
		  if (saveFreqFile)
		  {
			  System.out.println("Now sorting the constraints by token frequency");
			  outputfile.write("sorting by token frequency...\n");
			  sortChangesByFreq();
			  System.out.println("Done sorting by token frequency at: "+ new Date());
		
			  System.out.println("Now sorting the constraints by relative token frequency");
			  outputfile.write("sorting by relative token frequency...\n");
			  sortChangesByRelFreq();
			  System.out.println("Done sorting by relative token frequency at: "+ new Date());  
		  System.gc();
		  }
		  if (saveWeightedByFreqFile)
		  {
			  System.out.println("Now sorting the constraints by confidence weighted by token frequency");
			  outputfile.write("sorting by type x token...\n");
			  sortChangesByWeightedByFreq();
			  System.out.println("Done sorting by confidence weighted by token frequency at: "+ new Date());
		  
		  System.gc();
		  }
		  System.out.flush();
		  outputfile.flush();

		  stillLearning = false;
		  // 8/15/00 wug-testing should be faster with no doppelgaengers...
		  setDoppelgaenging(false);

		  firstWugtimeConstraint = newestconstraint; // should this be newestConstraint - 1? 

		  System.gc();
		  
		  wugTestWithLearning();

		  if (saveconstraints)
		  {
			  constraintsfile = new DataOutputStream(new FileOutputStream( learnerTask.language + "(" + formindex + ")" + ".con" ));
			  printConstraintList();
			  constraintsfile.flush();
			  constraintsfile.close();

		  }

		  outputfile.flush();
		  outputfile.close();
		  summaryfile.flush();
		  summaryfile.close();
		  
		 if (saveC75File)
		 {
			c75file.flush();
			c75file.close();
		 }
		 if (saveC90File)
		 {
			c90file.flush();
			c90file.close();
		 }
		 if (saveRawRelFile)
		 {
			rawrelfile.flush();
			rawrelfile.close();		  
		 }
		 if( saveWeightedFile)
		 {
		 	weightedfile.flush();
		 	weightedfile.close();
		 }
		 if( saveFreqFile)
		 {
		 	freqfile.flush();
		 	freqfile.close();
			relfreqfile.flush();
			relfreqfile.close();

		 }		 
		 if( saveWeightedByFreqFile)
		 {
		 	weightedbyfreqfile.flush();
		 	weightedbyfreqfile.close();
		 }		 
		 outputfile = new PrintWriter(new BufferedWriter( new FileWriter(learnerTask.language + "(" + formindex + ")" +".tmp" )));

	  } // end of one relevant milestone in intermediate wug-testing

	  // now back to our regularly scheduled programming:
	 stillLearning = true;

	} // end of if(intermediateWugTests)
	
   digest( learnerTask.input_paradigms[formindex].declension, formindex );
  }

//used to be end try, catch nullpointerexception

   // we're done with the forms, don't need to do anything special here.
   System.out.print("\n");
   outputfile.print("\n");
  
  // now we're done learning, we can turn stillLearning to false:
  stillLearning = false;
  firstWugtimeConstraint = newestconstraint; // should this be newestConstraint - 1? 

  outputfile.flush();
  outputfile.close();
  outputfile = new PrintWriter(new FileWriter(learnerTask.language + ".out" ));
//  outputfile = new PrintWriter(new BufferedWriter( new FileWriter(learnerTask.language + ".out" )));

  System.out.print("Done with learning at: " + new Date() + "\n");
  outputfile.write("Done with learning at: " + new Date() + "\n");  
  
  if (useImpugnment)
  {
	  System.out.print("... now impugning confidences.\n");
	  calculateImpugnedConfidences();
	  System.out.flush();
		outputfile.write("impugning...\n");
		outputfile.flush();

	  System.out.println("Done calculating impugned confidences at: "+ new Date());
  }

  System.out.println("Now calculating lower confidences");
  outputfile.write("lower c75...\n");
  outputfile.flush();
  for (int c = 0; c < newestconstraint; c++)
  {
		mapping_constraints[c].calculateLowerConfidence( );
		mapping_constraints[c].calculateOverallConfidence( useImpugnment );
		if (mapping_constraints[c].hits == 0)
			mapping_constraints[c].setKeep( false );
  }
  System.out.println("Done calculating lower confidences at: "+ new Date());

	  System.out.println("Now calculating relative token frequencies");
	  
	  outputfile.write("relative token frequencies...\n");
	  outputfile.flush();
	  for (int c = 0; c < newestconstraint; c++)
	  {
			mapping_constraints[c].calculateRelFrequency();
			mapping_constraints[c].calculateWeightedByFreq();
	  }
	System.out.println("Done calculating relative token frequencies at: "+ new Date());  


  if (saveWeightedFile)
  {
	  System.out.println("Now calculating weighted confidences");
	  outputfile.write("weighted confidences...\n");
	  outputfile.flush();
	  for (int c = 0; c < newestconstraint; c++)
	  {
			mapping_constraints[c].calculateWeightedConfidence( .2 );
	  }
	  System.out.println("Done calculating weighted confidences at: "+ new Date());  
  }  
  System.out.flush();
  printPhonologicalRules();
  // now sort the changes
  System.out.println("Now sorting the constraints by confidence");
  outputfile.write("sorting by confidence...\n");
  sortChanges();
  System.out.println("Done sorting by confidence at: "+ new Date());


  System.out.println("Now sorting the constraints by scope");
    outputfile.write("sorting by scope...\n");
  sortChangesByScope();
  System.out.println("Done sorting by scope at: "+ new Date());

  if (saveHitsFile)
  {
	  System.out.println("Now sorting the constraints by hits");
  outputfile.write("sorting by hits...\n");
	  sortChangesByHits();
	  System.out.println("Done sorting by hits at: "+ new Date());
  }
  if( saveC75File || saveC90File || saveHitsFile || saveRawRelFile || saveWeightedFile || saveFreqFile || saveWeightedByFreqFile)	
  {
	  System.out.println("Now sorting the constraints by c75");
	  outputfile.write("sorting by c75...\n");
	  sortChangesByC75();
	  System.out.println("Done sorting by c75 at: "+ new Date());
  }
  if (saveC90File)
  {
	  System.out.println("Now sorting the constraints by c90");
	  outputfile.write("sorting by c90...\n");
  	  sortChangesByC90();
	  System.out.println("Done sorting by c90 at: "+ new Date());
  }
  if (saveRawRelFile)
  {
	  System.out.println("Now sorting the constraints by raw reliability");
	  outputfile.write("sorting by raw reliability...\n");
	  sortChangesByRawRel();
	  System.out.println("Done sorting by raw reliability at: "+ new Date());
  }
  if (saveWeightedFile)
  {
	  System.out.println("Now sorting the constraints by weighted confidence");
	  outputfile.write("sorting by weighted confidence...\n");
	  sortChangesByWeighted();
	  System.out.println("Done sorting by weighted confidence at: "+ new Date());
  
  }
  if (saveFreqFile)
  {
	  System.out.println("Now sorting the constraints by token frequency");
	  outputfile.write("sorting by token frequency...\n");
	  sortChangesByFreq();
	  System.out.println("Done sorting by token frequency at: "+ new Date());

	  System.out.println("Now sorting the constraints by relative token frequency");
	  outputfile.write("sorting by relative token frequency...\n");
	  sortChangesByRelFreq();
	  System.out.println("Done sorting by relative token frequency at: "+ new Date());  
  }
  if (saveWeightedByFreqFile)
  {
	  System.out.println("Now sorting the constraints by confidence weighted by token frequency");
	  outputfile.write("sorting by type x token...\n");
	  sortChangesByWeightedByFreq();
	  System.out.println("Done sorting by confidence weighted by token frequency at: "+ new Date());
  
  }
  System.out.flush();
  outputfile.flush();

  summarizeChanges();

  // and output the constraints
  System.out.println("...now writing constraints to file and deriving test forms.");
  outputfile.write("done summarizing changes...\n");
  outputfile.flush();  
	if(saveconstraints)
	{
	  constraintsfile = new DataOutputStream(new FileOutputStream( learnerTask.language + ".con" ));  
		  printConstraintList();
	}

	summaryfile = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".sum" )));
	if (saveC75File )
		c75file = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".c75" )));
	if (saveC90File)
		c90file = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".c90" )));
	if (saveRawRelFile)
		rawrelfile = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".raw" )));
	if (saveHitsFile)
		hitsfile = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".hit" )));
	if (saveWeightedFile)
		weightedfile = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".wbl" )));
	if (saveFreqFile) {
		freqfile = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".frq" )));
		relfreqfile = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".rfrq" )));
	}
	if (saveWeightedByFreqFile)
		weightedbyfreqfile = new PrintWriter(new BufferedWriter(new FileWriter(learnerTask.language + ".wbf" )));
	
	outputfile.write("Now starting wug-testing...\n");
	outputfile.flush();
	System.out.println("Now starting wug-testing...\n");

  wugTestWithLearning();

	outputfile.write("Now done wug-testing...\n");
	outputfile.flush();

  outputfile.flush();
  outputfile.close();
  summaryfile.flush();
  summaryfile.close();
  
	if (saveHitsFile)
	{
		hitsfile.flush();
		hitsfile.close();
	}
	if (saveC75File)
	{
		c75file.flush();
		c75file.close();
	}
	if (saveC90File)
	{
		c90file.flush();
		c90file.close();
	}
	if (saveRawRelFile)
	{
		rawrelfile.flush();
		rawrelfile.close();
	}
	if (saveWeightedFile)
	{
		weightedfile.flush();
		weightedfile.close();
	}
	if (saveFreqFile)
	{
		freqfile.flush();
		freqfile.close();
		relfreqfile.flush();
		relfreqfile.close();
	}
	if (saveWeightedByFreqFile)
	{
		weightedbyfreqfile.flush();
		weightedbyfreqfile.close();
	}

  System.out.print("Finished learning at: "+ new Date() + ".\n");
  System.out.flush();
  
}
catch(IOException e){System.out.println(e.toString());}
  return true;
 }
 // __________________________________________________________________________________
 /*  The "digest" method takes a new pair of forms, checks to see if it is novel, and
  processes it if it is.   Processing here means (1) create a new mapping constraint
  which encodes our new-found knowledge about this one form, and (2) try to build 
  more general schemas by finding similarities with other known mappings.
 */
 
 public void digest( String new_decl[], int formindex ) throws IOException
 {
  char new_declension[][];
  new_declension = new char[2][];
  
  new_declension[0] = new_decl[0].toCharArray();
  
  new_declension[1] = new_decl[1].toCharArray();
  
//  new_declension[1] = new char[ new_decl[1].length() ];
//  new_decl[1].getChars( 0, new_decl[1].length(), new_declension[1], 0);
  
 
  // first, we need to remember what mapping is currently at the end of the
  // array, since this is the last one which has been generalized:
//  int last_generalized = newestconstraint - 1;
  
//  if ( novelform( new_declension ) )
//  {
   // first, the bookkeeping: add this form to the list of things we've heard,
   // and see what known constraints it violates.
   remember( new_declension , formindex );
   
   // We start by assuming the least generality possible: install a degenerate
   // mapping for this novel form.
   // For now we will assume declensions with just 2 members-- this will have
   // to be fixed to accomodate larger declensions.

   // NEW 5/21/99 check uniqueness to make sure we don't end up with multiple "degenerate" constraints

	if (homophoneprotection)
	{
		if (novelform(new_decl))
		{
	//	   System.out.print("�");
		   findDegenerateMappings( new_declension[0], new_declension[1] );
		}
	//	else System.out.print("!");
	}
	else 
	{
		findDegenerateMappings( new_declension[0], new_declension[1] ); 
	}
//   outputfile.write("Done finding degenerate mappings for " + new_declension[0] );
//   outputfile.write(" -> " + new_declension[1] + ".\n");   

//  }
  return;
 }
 // __________________________________________________________________________________
 /*  The "novelform" method checks to see if we've already seen a declension before.  
  
     We check this by looking through the known forms (i.e., the lexicon) to see if
     another identical word is already there.
 */
 public boolean novelform( String new_declension[] ) throws IOException
 { 
 // a form is novel if it doesn't match anyone in the lexicon:

	 for (int f = 0; f < (newestform-1); f++)
	 {
		if (new_declension[0].equals( String.valueOf(known_forms[f][0]) ))
		{
			if (new_declension[1].equals( String.valueOf(known_forms[f][1]) ))
			{
				return false;
			}
		} 
	 }
	 // if we got here, no forms matched
	 
	 return true;
 }
 // __________________________________________________________________________________
 /*
  The "remember" method is mainly for bookkeeping-- it takes a novel form and adds
  it to the list of things which we have heard.
  We need to do this because this list serves as an index for rows in the table
  of forms and mapping constraints.
  Also, the list serves as what we might traditionally call the "lexicon"
 */
 public void remember( char new_declension[][], int formindex ) throws IOException
 {
//  outputfile.write ( newestform + "\n\n");
  
  // now, we may need to increase the known_forms array:
  if (newestform >= (known_forms.length - 1))
  {
   char temp_forms[][][] = new char[ this.known_forms.length + 1700][][];
   for (int i = 0; i < newestform ; i++ )
   {
    temp_forms[i] = new 
       char[ this.known_forms[i].length ][];
    temp_forms[i] = this.known_forms[i];
   }

   // now replace the known_forms with the new, expanded one:
   known_forms = temp_forms;
    
    int temp_freq[] = new int[ this.frequencies.length + 1700];
	System.arraycopy( frequencies, 0, temp_freq, 0, newestform );  
	frequencies = temp_freq;
  }

  // then add the new form:
  known_forms[ newestform ] = new char[ new_declension.length ][];
  for ( int i = 0; i < new_declension.length ; i++ )
  {
   known_forms[ newestform ][ i ] = new_declension[ i ];
  }
  
  frequencies[ newestform ] = learnerTask.input_paradigms[formindex].frequency;

  
  // finally, the new guy has to "catch up" on his violations:
  assessFormViolations( newestform );

  newestform++;  
  return;
 }

 // __________________________________________________________________________________
 /* The "assessFormViolations" method assesses violations of new forms that we
  are hearing for the first time to old constraints which are already in place.  
  In principle, this could be where we model things like "surprise" because 
  this is the first time that new forms confront the grammar.  At the moment, 
  it does not do anything nearly so interesting.
 */
 public void assessFormViolations( int form ) throws IOException
 {
  // first, we need to add a row to the Violations grid:
  int violation;

//  expandViolationsGrid();
  
  /*  We are assessing violations for the form:  known_forms[ form ].
      This is done by cycling through the known constraints, and comparing them
      to the new form.
  */
  
//  outputfile.write("Assessing form " + newestform + " against " + mapping_constraints.length + " constraints.\n");
  
  for ( int c = 0; c < newestconstraint; c++ )
  {
//   outputfile.write("  Structural change: " + mapping_constraints[c].mappings[0] + "\n");
//   if (mapping_constraints[c].scope  > 1) // new 5/21/99 trying assessing against degenerates, too
    violation = assessViolation( form, c);


//    Violations[form][c] = assessViolation( form, c );
//   else Violations[form][c] = 1;
  }

  return;
 }

 // __________________________________________________________________________________
 /*
  assessViolation takes a form and a constraint as arguments (or, more precisely, 
   the integer indices in the arrays of forms and constraints).  It then checks
   to see if the given form violates the given constraint, and returns a boolean
   yes/no answer.
 */
 public int assessViolation( int f, int c) throws IOException
 {
  char predicted[];
  int change_location;
  if (debug)
  {
   outputfile.write("\tAssessing violations for the form [" + known_forms[f][0]
    + "] under the constraint [" + mapping_constraints[c].mappings[0] + "] -> ["
    + mapping_constraints[c].mappings[1] + "].\n");
  }
  
  change_location = strucDescMet( known_forms[f][0] , c );

  if (change_location >= 0)
  {
   mapping_constraints[c].inc_scope();
   if (saveFreqFile || saveWeightedByFreqFile)
   {
   	mapping_constraints[c].incScopeFrequency( frequencies[f] );
   }

   // Build the predicted form by applying the constraint to the left side;
   // If phonology is on, then also apply it.
   //   (if not, we can save a call to the phonology method)
   calculatePredicted:
   {
   if (phonologyOnFlag)
   {
    predicted = applyPhonology( applyConstraint( known_forms[f][0] ,c, change_location));
   }
   else
   {
    predicted = applyConstraint( known_forms[f][0] ,c, change_location);
   }
   }

   if (debug)
    outputfile.write("\t\t...the predicted output is: [" + predicted + "].\n");

   if ( match(known_forms[f][1], predicted ))
   {
    // yay! a hit.
//    if(verbose)
    if (debug)
    {
     outputfile.write("\t\t["+predicted+"] matches [" + known_forms[f][1]+"].  (No violation.)\n");
    } 
    
    mapping_constraints[c].scoreHit();    
	if (saveFreqFile || saveWeightedByFreqFile)
		mapping_constraints[c].incHitsFrequency( frequencies[f] );
   
   if (related_forms[c][0] < 20)
   {
   		// catalog this form as a relevant one for this constraint
   		related_forms[c][ related_forms[c][0] ] = f;
   		// we now know one more relevant form
   		related_forms[c][0] = related_forms[c][0] + 1;
   }


    return NO;
   }
   else if (phonologyOnFlag)
   {
    // OK, it didn't work the first time, but here's a chance to redeem
    // this constraint:  try to find phonology which might change the output
    // 

//    if(verbose)
    if(verbose)
    {
     outputfile.write("\t\tTrying to discover phonology to turn [" +
         String.valueOf(predicted) + "] into [" + String.valueOf(known_forms[f][1]) + "].\n");
    }
        
     discoverPhonology( predicted, known_forms[f][1] );
     predicted = applyPhonology( applyConstraint( known_forms[f][0] ,c, change_location));

    // now see if this helped:
    if (match(known_forms[f][1],predicted))
    {
     if(debug)
//     if(verbose)
     {
      outputfile.write("\t\t["+predicted+"] matches [" + known_forms[f][1]+"].  (No violation.)\n");
     } 

//     mapping_constraints[c].inc_scope();     // GACK!! this shouldn't be here, should it?
     mapping_constraints[c].scoreHit();
	 if (saveFreqFile || saveWeightedByFreqFile)
	 	    mapping_constraints[c].incHitsFrequency( frequencies[f] );
	   if (related_forms[c][0] < 20)
	   {
	   		// catalog this form as a relevant one for this constraint
	   		related_forms[c][ related_forms[c][0] ] = f;
	   		// we now know one more relevant form
	   		related_forms[c][0] = related_forms[c][0] + 1;
	   }
     
     
     return NO;
    }    
     else
     {
     if (debug)
//      if (verbose)
      {
       outputfile.write("\t\tPredicted and actual do not match -- a violation.\n");
      }

	   if (exceptions[c][0] < 20)
	   {
	   		// catalog this form as a MISS for this constraint
	   		exceptions[c][ exceptions[c][0] ] = f;
	   		// we now know one more relevant form
	   		exceptions[c][0] += 1;
	   }
	   
       return YES;
      }
   }
   
   // Alternatively, if it didn't work the first time and we've opted
   // not to try for phonology, then to hell with it.  A violation.
   else
   {
    if (debug)
//     if (verbose)
     {
      outputfile.write("\t\tPredicted and actual do not match -- a violation.\n");
     }  
	   if (exceptions[c][0] < 20)
	   {
	   		// catalog this form as a MISS for this constraint
	   		exceptions[c][ exceptions[c][0] ] = f;
	   		// we now know one more relevant form
	   		exceptions[c][0] += 1;
	   }
    return YES;
    }
  }
  else
  {
//   if (verbose)
   if (debug)
    outputfile.write("\t\tStructural description is not met...\n");
  
    return NA;
   }
 }
 // __________________________________________________________________________________
 /*
  strucDescMet takes a char array and (the index of) a constraint, and returns
  an integer value for the LOCATION of the left side of where the change is located
  in the form, e.g.:
  		prefixation of []->[something]: strucDesc at 0
		suffixation of []->[something]: strucDesc at form.length()
        change last 2 characters ([at]->[in]/_#): strucDesc at (form.length - 2)
        strucDesc NOT met: return -1
 */
 public int strucDescMet ( char f[], int c ) throws IOException
 { 
  int mlength, flength;
  int mustStartBy; // we'll use this to keep track of loops in internal morphology
  // these are used to avoid headaches with internal changes & features:
  boolean leftOK, rightOK;
  
  // we'll use these for adding a "buffer" when we need to leave space for decomposed segs
  int pfeat, qfeat;
  
  // for convenience, let's put the target mapping in a new char array:
  char mapping[] = new char[ mapping_constraints[c].mappings[0].length ];
  mapping = mapping_constraints[c].mappings[0];

   mlength = mapping.length;
   flength = f.length;


  // we can save the amount of searching we have to do by making
  // use of the type of affixation involved


  // if the mapping is longer than the form, then it is automatically NOT met:
  if (mlength > flength)
  {
   if (debug)
    outputfile.write("\t\t\tMapping is longer than the form.\n");
   
   return -1;
  }

  // otherwise, check to see where the mapping should apply 
  else if ( mapping_constraints[c].affixType == SUFFIX )
  {
  
   if (debug)
    outputfile.write("\t\t\tSUFFIXATION\n");
    
   // first check strings
   for (int i = 1 ; i <= mlength; i++)
   {
    if ( mapping[ mlength - i ] !=  f[ flength - i ])
     return -1;
   }

	// OK, so the strings match.  now see if the constraint has active P-side features:
	if (mapping_constraints[c].P_features_active)
	{
		// P-features ARE active; if the form is string-subsumed, we have no hope
		if ((flength - mlength) == 0)
		{
			return -1; // *** for some reason this used to be return TRUE
		}
		else // otherwise, we try decomposing the next segment
		if (!featuresInBounds( mapping_constraints[c].P_feat, decomposeSeg( f[ flength - (mlength + 1)] )))
		{
//    		outputfile.write("The segment "+f[flength - (mlength+1)]+" does not meet constraint " + c +".\n");
			return -1; // no match
		}
		else // if we got here, the segment matched the features; now we just
		     // need to be sure that the constraint doesn't demand a word edge
		     // where the form has more material left
		if (!mapping_constraints[c].P_residue && (flength - mlength > 1) )
		{ // no residue allowed, but more segments exist
			return -1;
		}
		else // otherwise, either the form is used up or the constraint allows residue
			// the change location will be at the end of the form, as far back from the 
			// right as the length of the mapping:
			return (flength - mlength);
	} // end of if P_features_active
	else // if no active P_features, just need to make sure that it doesn't happen that
		 // the constraint demands an edge but the form has material left...
		 // Note that in this case, we haven't decomposed a seg, so criterion is >0, not 1
	if (!mapping_constraints[c].P_residue && (flength - mlength) > 0) 
		return -1;
	// otherwise there is either residue, or the form is consumed
	else return (flength - mlength);

  } // end of SUFFIXATION
  else if ( mapping_constraints[c].affixType == PREFIX )
  {

   if (debug)
    outputfile.write("\t\t\tPREFIXATION\n");

	// first check strings
   mlength = mapping.length;
   for ( int i = 0 ; i < mlength; i++ )
   {
    if ( f[i] != mapping[i])
     return -1;
   }
   
   // strings matched, so see if there are active Q-side features
   if (mapping_constraints[c].Q_features_active)
   {
		// Q-features ARE active; if the form is string-subsumed, we have no hope.
		if ((flength - mlength) == 0)
		{
			return -1; // *** for some reason we never really used to check this
		}
		else // otherwise, try decomposing the next segment
		if(!featuresInBounds( mapping_constraints[c].Q_feat, decomposeSeg( f[ mlength ])))
		{
			return -1; // the segment doesn't meet the constraint's features
		} 
		else // if we got here, the segment met the features; now we just 
			 // need to be sure that the constraint doesn't demand a word edge
			 // where the form has more material left
		if (!mapping_constraints[c].Q_residue && (flength - mlength > 1))
		{
			// uh-oh, no residue allowed but there are more segments
			return -1;
		}
		else // otherwise, either the form is used up, or the constraint allows residue...
			 // since this is prefixation, the change is at the beginning (0)
			return 0;
		
   } // end of if Q_features_active
	else // if no active Q_features, just need to make sure that it doesn't happen that
		 // the constraint demands an edge but the form has material left...
		 // Note that in this case, we haven't decomposed a seg, so criterion is >0, not 1
	if (!mapping_constraints[c].Q_residue && (flength - mlength) > 0) 
		return -1;
	// otherwise there is either residue, or the form is consumed
	else return 0;

  } // end of PREFIXATION
  
  else if ( mapping_constraints[c].affixType == SUPPLETION )
  {
//	   flength = f.length;
	   if (debug)
	    outputfile.write("\t\t\tSUPPLETION\n");

	   // if form is longer than the mapping, then not met:
	   if (f.length > mapping.length)
	    return -1;

	   for (int i = 0; i < flength; i++ )
	   {
	    if  (f[i] != mapping[i])
	     return -1;
	   }
	   
	   // if we got here, everything matched
	   return 0;
  }
  else  // otherwise, it's internal morphology.
  {

   if (debug)
    outputfile.write("\t\t\tINTERNAL MORPHOLOGY\n");
   // in the last case, we are looking for internal morphology-- the 
   // string must be present, but NOT as a prefix or suffix.
	if (mapping_constraints[c].P_features_active)
		pfeat = 1;
	else pfeat = 0;
	if (mapping_constraints[c].Q_features_active)
		qfeat = 1;
	else qfeat = 0;
	
	if (!mapping_constraints[c].P_residue)
		mustStartBy = 0;
	else mustStartBy = flength - (pfeat + mlength + qfeat);
	
	
	try{
		// now loop through potential starting points, from the 0 to mustStartBy:
		for (int i = 0; i<= mustStartBy; i++)
		{
			trystartpoint:
			{
				// first, make sure P features match if active
				if (mapping_constraints[c].P_features_active)
				{
					if (!featuresInBounds(mapping_constraints[c].P_feat, decomposeSeg(f[i])))
					{
						// the P features don't work, this starting point is a failure
						break trystartpoint;
					} // end of if mismatch in P features
				}
				// ok, either there are no P features, or they matched
				// now try the string mapping
				for (int n = 0; n < mlength; n++)
				{
					if (f[n+pfeat+i] != mapping[n]) // make sure this seg matches
						break trystartpoint; // out of luck for this start point
				} // end of loop through mapping's segs
				// if we got here, all the segments matched-- now match Q features
				if (mapping_constraints[c].Q_features_active)
				{
					if (!featuresInBounds(mapping_constraints[c].Q_feat, decomposeSeg(f[i+pfeat+mlength])))
						// the Q features don't work
						break trystartpoint;
				} 
				// everything that exists matches-- now just make sure there isn't too much
				// material left (i.e., if no Q res)
				if (!mapping_constraints[c].Q_residue)
				{
					if (flength > (i+pfeat+mlength+qfeat))
					{
						//oops, stuff left in the form that we haven't accounted for with mapping
						break trystartpoint;
					}
				}
				// great! everything checked out-- the mapping started at i+pfeat
				return i+pfeat;
			
			} // end of trystartpoint block
		
		} // end of loop through potential starting points
	} // end of try
	catch (ArrayIndexOutOfBoundsException e){} // do nothing here for now
	// if we got through the loop and never hit a spot where everything checked out,
	// then we failed to meet the structure description...
	return -1;

  }

  // never get here, so no return statement needed...
 }

 // __________________________________________________________________________________
 /*
  applyConstraint takes a string and a constraint, and applies the constraint
  to the string.  it is meant to be called after you have already checked that
  the string meets the structural description of the constraint.
  (bad things might happen otherwise!)
 */

 public char[] applyConstraint( char f[], int c, int change_location ) throws IOException
 {
  char output[];

  int f_length = f.length;
  int m0_length = mapping_constraints[c].mappings[0].length;
  int m1_length = mapping_constraints[c].mappings[1].length;

/*	System.out.println("Applying constraint "+c+" (["+ String.valueOf(mapping_constraints[c].mappings[0])+"]->["+ 
						String.valueOf(mapping_constraints[c].mappings[1]) + "]) to the form [" + String.valueOf(f) + 
						"].\n\t(m0_length = " + m0_length + ", m1_length = " + m1_length + ", f_length = "
						+ f_length + ", change location = " + change_location + ")");
						
	System.out.flush();
*/
	// now that we make explicit use of the previously discovered change location,
	// we do not have to divide this task into very many sub-tasks.  In fact,
	// suppletion is the only case which we have to handle separately, because we are
	// not making a substitution/insertion in that case

	if (mapping_constraints[c].affixType == SUPPLETION)
	{
		return mapping_constraints[c].mappings[1];
	}

	// in most cases, we will just build the output from three parts:
	// (1) the part of the form up until the change (A)
	// (2) the changed part of the mapping (B).
	// (3) the part of the form after the change (A)
	
	else { // build the output from other bits:
		output = new char[ (f_length - m0_length) + m1_length];

//		System.out.println("\t\t(building left side)");
//		System.out.flush();
		for (int i = 0; i < change_location; i++)
		{
			output[i] = f[i];
		}
		
//		System.out.println("\t\t(adding the change)");
//		System.out.flush();			
		for (int i = change_location; i < (change_location+m1_length); i++)
		{
			output[i] = mapping_constraints[c].mappings[1][i-change_location];
		}

		// now the parts that remain after the change:
//		System.out.println("\t\t(building right side)");
//		System.out.flush();			
		for (int i= change_location + m0_length; i < f_length; i++)
		{
			output[ i+(m1_length - m0_length)] = f[i];
		}

//		System.out.println("\tResult: " + String.valueOf(output) );
//		System.out.flush();
		return output;
	}
 }

 // __________________________________________________________________________________
 /* the "findDegenerateMappings" method takes a declension and hypothesizes possible 
  degenerate mapping constraints for it;  note that not only does each new
  form get its own degenerate mapping, but some forms even end up with
  more than one!  This happens when it is no unique correspondence between segments
  in MCat1 and MCat2 (like English wed->wedded, etc.)
 
 */
 
 public void findDegenerateMappings( char form1[], char form2[] ) throws IOException
 {
  // In the Kiparskian tradition, we will represent mappings as being composed of
  // concatenations of substrings, in the form:
  //    PAQ ->  PBQ  (where A->B is the actual structural change,
  //     and P and Q are the left- and right-side environments)
  // We will be computing 2 (possibly) different decompositions for each 
  // mapping [form1]->[form2], and storing a degenerate mapping constraint
  // for each one.
    
  char P[];
  char Q[];
  char A[];
  char B[];
  
  // we won't always use the second set of these, so i'll just declare them
  // but we won't actually initialize them until they prove necessary:
  
  char P2[], Q2[], A2[], B2[];
  
  
  // These integers store positions in the strings:
  int maxP, minQ, maxQ, minP;

  int latest = newestconstraint;

  int shorterFormLength = (form1.length <= form2.length ? form1.length : form2.length);

  /* In principle, for a mapping [form1]->[form2], there could be several
     possible solutions for P, A, and Q.  
     The "best" (and possibly also most common?) solutions are ones
     in which either P or Q is null-- that is, either vanilla prefixation
     or suffixation.  
     We concentrate on edges by using the algorithm: "Maximize one side 
     primarily, and the other side secondarily"
     Since suffixation is a bit more common cross-linguistically, we'll
     start with "Maximize P primarily, and Q secondarily."      
  */
  
  // i'm going to define this code as a block, so we can escape from it when
  // we know what kind of a mapping we are dealing with:
  findmappings:
  {
    
  // First, we'll maximize P and then (secondarily) Q
  // We do this with an unbounded left match and a bounded right match:
  
  // Maximize P by finding the first character which doesn't match:
  maxP = matchLeft( form1, form2, shorterFormLength );

  if (verbose)
  {
   outputfile.write("FIRST THE MAX P AND SECONDARILY Q:\n");
   outputfile.write("      Going from the left, the first " + maxP + " characters match.\n");
  }
  // Then, unless maxP already subsumes one form, find 
  // the right side match from what's left:
  if (maxP < shorterFormLength)
  {
   minQ = matchRight( form1, form2, maxP );
   
//   // and an aesthetic fix:
//   if (form1.charAt( form1.length() - minQ ) == '�')
//    minQ--;
  } 
  else minQ = 0;
  
  
  
  if (verbose)
   outputfile.write("      And the last " + minQ + " characters match.\n");
  
  
  // so now we can calculate the relevant "strings":
  P = new char[maxP];
  for (int i = 0; i < maxP; i++)
   P[i] = form1[i];
   
  Q = new char[minQ];
  for (int i = 1; i <= minQ; i++)
   Q[ minQ - i ] = form1[ form1.length - i ];
  
  A = new char[ (form1.length-minQ)-maxP ];
  for (int i = 0; i < A.length; i++)
   A[i] = form1[ maxP + i ];
   
  B = new char[ (form2.length-minQ)-maxP ];
  for (int i = 0; i < B.length; i++)
   B[i] = form2[ maxP + i ];
  
  
  
  // if P or Q (or both) are null, then we have a (nearly) safe case of
  // "effixation."  we'll stick with it and store that as a mapping constraint.
  if (P.length == 0 || Q.length == 0)
  {
   if (verbose)
    outputfile.write("\t\tP = ["+String.valueOf(P)+"], Q = ["+String.valueOf(Q)+"], A = ["+
    					 String.valueOf(A)+"], B = ["+String.valueOf(B)+"].\n");
   latest = addMappingConstraint( form1, form2 , P, Q, A, B, maxP, false, false );
   mapping_constraints[ latest ].inc_scope();
   // yikes, do we need to increase scope frequency here? or will this only be degenerates so we don't care?
   
   mapping_constraints[ latest ].scoreHit();
   mapping_constraints[ latest ].setDegenerate(true);
   
   if (Q.length == 0)
   		mapping_constraints[latest].setAffixType( SUFFIX );
	else if (P.length == 0)
		mapping_constraints[latest].setAffixType( PREFIX );


   // in addition, we want to add this newest guy to the "directory" of struc changes:
	   addStrucChange( A , B , (latest) );   //********

   // and then our task here is done.
   break findmappings;
  }
  
  /* if neither P nor Q is null under "max P then Q", then we have one of several
     cases:
    a. we have clean effixation but haplology is obscuring that in the
        "max P then Q" search
    b. we have ablaut
    c. we have some kind of infixation
     In any case, we proceed by doing a "max Q then P" search.
  */
  
  // Maximize Q and secondarily P.
  //    first, find Q:
  if (verbose)
   outputfile.write("NOW TRYING MAX Q AND SECONDARILY P\n");
   
  maxQ = matchRight( form1, form2, 0 );
  if (verbose)
   outputfile.write("       Starting on the right, the last " + maxQ + " characters match.\n");

  
  // Now, this might be the same as the result from above, in which case
  //  it would be pointless to do a lot more laborious calculations.
  //  If we got to the point of needing to do the "max Q then P" search, but
  //  it turns out that the results are the same, then we must have 
  //  ablaut (or something REALLY odd like substituting infixation).
  // In any case, the best thing to do here is to store the results from above
  //  without any further ado, since extra calculations would be fruitless:
  if ( maxQ == minQ )
  {
   if (verbose)
    outputfile.write("      (left and right solutions for P and Q are the same...)\n");
   if (verbose)
    outputfile.write("\t\tP = ["+String.valueOf(P)+"], Q = ["+String.valueOf(Q)+"], A = ["+String.valueOf(A)+"], B = ["+String.valueOf(B)+"].\n");
   latest = addMappingConstraint( form1, form2 , P, Q, A, B, maxP, false, false );
   mapping_constraints[ latest ].inc_scope();
   // again, do we need to increase scope frequency here?
   
   mapping_constraints[ latest ].scoreHit();
   mapping_constraints[ latest ].setDegenerate(true);   
   
   mapping_constraints[latest].setAffixType(INFIX);
   
   // in addition, we want to add this newest guy to the "directory" of struc changes:
	   addStrucChange( A , B, (latest) ); //********

   // and so our task here is done.
   break findmappings;
  }    
  
  // Otherwise, this is heading somewhere new.  Keep going...
  // if maxQ doesn't subsume whole form, then find a left side match
  if (shorterFormLength - maxQ > 0 )
   minP = matchLeft( form1, form2, (shorterFormLength - maxQ) );
  else minP = 0;
  
  if (verbose)
   outputfile.write("       and the first " + minP + " characters do.\n");
  
  // so now initialize the second line of variables,
  //     and calculate the relevant strings:

  P2 = new char[minP];
  for (int i = 0; i < minP; i++)
   P2[i] = form1[i];
   
  Q2 = new char[maxQ];
  for (int i = 1; i <= maxQ; i++)
   Q2[ maxQ - i ] = 
       form1[ form1.length - i ];
  
  A2 = new char[ (form1.length-maxQ)-minP ];
  for (int i = 0; i < A.length; i++)
   A2[i] = form1[ minP + i ];
   
  B2 = new char[ (form2.length-maxQ)-minP ];
  for (int i = 0; i < B.length; i++)
   B2[i] = form2[ minP + i ];


  // Now we want to know if all this work has cleaned up an erstwhile mess.
  // If P2 or Q2 is null, then it turns out that we have straight effixation
  // after all, so just store this latter result.

  if ( P2.length == 0 || Q2.length == 0)
  {
   if (verbose)
    outputfile.write("\t\tP = ["+String.valueOf(P2)+"], Q = ["+String.valueOf(Q2)+"], A = ["+String.valueOf(A2)+"], B = ["+String.valueOf(B2)+"].\n");
   latest = addMappingConstraint( form1, form2, P2, Q2, A2, B2, minP, false, false );
   mapping_constraints[ latest ].inc_scope();
   // scope frequency???
   
   mapping_constraints[ latest ].scoreHit();   
   mapping_constraints[ latest ].setDegenerate(true);

	if (Q2.length == 0)
		mapping_constraints[latest].setAffixType( SUFFIX );
	else if (P2.length == 0)
		mapping_constraints[latest].setAffixType( PREFIX );

   // in addition, we want to add this newest guy to the "directory" of struc changes:
	   addStrucChange( A2 , B2, (latest) ); //********
	// 4/2/99 these used to be just A,B, not A2, B2-- a bug?
  }


/*  // OTHERWISE, we have mess in both directions.  This is some kind of infixation,
  // and since finding the true source of infixation is a tricky business,
  // we will just store BOTH possibilities for now:
    

  else
  {
   // the "max P then Q" result:
   if (verbose)
    outputfile.write("\t\tP = ["+P+"], Q = ["+Q+"], A = ["+A+"], B = ["+B+"].\n");
   addMappingConstraint( form1, form2 , P, Q, A, B, maxP, false, false );

   // and also the "max Q then P" result:
   if (verbose)
    outputfile.write("\t\tP = ["+P2+"], Q = ["+Q2+"], A = ["+A2+"], B = ["+B2+"].\n");
   addMappingConstraint( form1, form2, P2, Q2, A2, B2, minP, false, false );
  }
*/  
  // WRONG-- let's try just storing the first result, since it's closer to suffixation
  else
  {
   if (verbose)
    outputfile.write("\t\tP = ["+String.valueOf(P)+"], Q = ["+String.valueOf(Q)+"], A = ["+String.valueOf(A)+"], B = ["+String.valueOf(B)+"].\n");
   latest = addMappingConstraint( form1, form2 , P, Q, A, B, maxP, false, false );
   mapping_constraints[ latest ].inc_scope();
   // scope frequency?
   
   mapping_constraints[ latest ].scoreHit();   
   mapping_constraints[ latest ].setDegenerate(true);

	mapping_constraints[latest].setAffixType(INFIX);

   // in addition, we want to add this newest guy to the "directory" of struc changes:
	   addStrucChange( A , B, (latest) ); //********
  }
  
  } // end of the findmappings block
  
  
//  // now we bring up the trawling nets and generalize on whatever we caught:
  if (verbose)
  	outputfile.write("Degenerate mapping has been added as constraint #"+ latest+"\n");
  return;
 }
 
 // __________________________________________________________________________________
 /* The "addMappingConstraint" method takes two strings and installs a new mapping
  constraint of the form [string1]->[string2].  It also takes various infos
  about the structure of the strings � namely, the terms which we are calling P,
  Q, A and B (as well as the location of the A term) and stores them so we can
  use them in future comparisons.
 */
  
 public int addMappingConstraint( char string1[] , char string2[],
          char P[], int P_features[][], int Q_features[][], char Q[],
          char A[], char B[],
          int change_location,
          boolean P_res, boolean Q_res, boolean P_f_active, boolean Q_f_active)  
 {
//  int latest;
  int numberOfRules;

//  outputfile.write("Newest constraint = " + newestconstraint + ", and there are "
//        + mapping_constraints.length + " constraints.\n");


  //  to add a new constraint, we may need a bigger array:
  if (newestconstraint >= (mapping_constraints.length - 1))
  {
   System.out.println("\n(expanding memory array size from " + mapping_constraints.length + " to " + (mapping_constraints.length + 20000) + ")");
   // in order to add a mapping constraint, we need a bigger array
   MappingConstraint temporary_constraints[] = new MappingConstraint[ mapping_constraints.length + 20000 ];
   // copy currently known mappings into the new array
   System.arraycopy(mapping_constraints,0,temporary_constraints,0,mapping_constraints.length);
   mapping_constraints = temporary_constraints;    
   
   // also, we need to expand the relevant forms array
   int temp_relevant[][] = new int[ related_forms.length+20000][21];
   System.arraycopy(related_forms,0,temp_relevant,0,related_forms.length);
   related_forms = temp_relevant; 
   
   int temp_misses[][] = new int[ exceptions.length+20000][21];
   System.arraycopy(exceptions,0,temp_misses,0,exceptions.length);
   exceptions = temp_misses; 
   
  }
  
  if (useFeatures)
   mapping_constraints[ newestconstraint ] = new MappingConstraint( learnerTask.MCats.length, learnerTask.numberOfFeatures );
  else
   mapping_constraints[ newestconstraint ] = new MappingConstraint( learnerTask.MCats.length );
  
  related_forms[newestconstraint][0] = 1;
  exceptions[newestconstraint][0] = 1;
  mapping_constraints[ newestconstraint ].mappings[0] = string1;
  mapping_constraints[ newestconstraint ].mappings[1] = string2;
  
  
  mapping_constraints[ newestconstraint ].setP(P);
  mapping_constraints[ newestconstraint ].setQ(Q);
  mapping_constraints[ newestconstraint ].setChangeLocation(A,B,change_location);
  

  if (numberOfFeatures > 0)
  {
   mapping_constraints[ newestconstraint ].setP_features( P_features );
   mapping_constraints[ newestconstraint ].setQ_features( Q_features );  
  }
  
  mapping_constraints[ newestconstraint ].setP_residue(P_res);
  mapping_constraints[ newestconstraint ].setQ_residue(Q_res);
  
  
//  System.out.println("Mapping constraint " + newestconstraint + " BEFORE feature deactivation:");
//  mapping_constraints[ newestconstraint ].report();
  
  
  if (!P_f_active)
  {
  	mapping_constraints[newestconstraint].deactivate_P_features();
  }
  if (!Q_f_active)
  {
  	mapping_constraints[newestconstraint].deactivate_Q_features();
  }

//  System.out.println("Mapping constraint " + newestconstraint + " AFTER feature deactivation:");
//  mapping_constraints[ newestconstraint ].report();


//  outputfile.write("We now have "+mapping_constraints.length+" constraints.\n");

  // ... then report the new constraint to the console:
//  if (verbose)
try{
  if (debug)
    reportNewConstraint( newestconstraint );
} catch(IOException e){ System.out.println("Warning: trouble reporting new constraint!");}
  // also, it's efficient to diagnose and store the type of affixation:
  mapping_constraints[ newestconstraint ].diagnoseAffixType();
  mapping_constraints[ newestconstraint ].setChronological( newestconstraint );

  // Next, we need to assess violations for this constraint:
/*  if (debug)
   outputfile.write("Assessing violations for constraint #" + latest+"\n");
*/ 
//  assessConstraintViolations( newestconstraint ); 
  
     
  newestconstraint++;
  return (newestconstraint - 1);
 }
 

 // also, a version of addMappingConstraint with no features, which just supplies default
 // null features
 public int addMappingConstraint( char string1[] , char string2[],
          char P[], char Q[],
          char A[], char B[],
          int change_location,
          boolean P_res, boolean Q_res)  throws IOException
 {
  int latest;
  int null_features[][] = new int[2][numberOfFeatures];
  for (int i = 0; i < numberOfFeatures; i++)
  {
   null_features[0][i] = INACTIVE;
   null_features[1][i] = INACTIVE;
  }
  latest =  addMappingConstraint( string1, string2, P, null_features, null_features, Q, A, B, change_location,
        P_res, Q_res, false, false);
  return latest;
 } 
 
 
 // __________________________________________________________________________________
 /*
  The "addStrucChange" method is merely for efficiency-- it adds a constraint's 
  structural description (it's 'A') to the hashtable of descriptions, so we 
  can search for descriptions more efficiently.
 */
 public void addStrucChange( char A[], char B[], int constraint ) throws IOException
 {
  String newdesc = new String( ""+ String.valueOf(A) + File.separatorChar + String.valueOf(B) 
  								+ File.separatorChar + mapping_constraints[constraint].affixType );
  ConstraintBatch existing_matches = ( ConstraintBatch ) changes.get( newdesc );
  ConstraintBatch matches;
  int numberOfChanges;
  int x, compare, c, latest;
  String related_changes[];
  String temp[];
  int lastChange;
  String anotherMapping1;
  
  if (verbose)
  {
  	outputfile.write("examining structural description.... [" + newdesc + "]\n");
  }
  
  if (debug)
   outputfile.write("adding structural description.... [" + newdesc + "]\n");

  if (existing_matches == null && stillLearning)
  {
	   // this is a new struc desc-- one we haven't seen before
		if( verbose )
		   outputfile.write(" (existing matches with this description is NULL, so adding space for [" + newdesc+"] )\n" );
	   matches = new ConstraintBatch(500);
	   
	   matches.add_element(constraint);
	   changes.put( newdesc, matches );
	   
	   // also, we will need to add this to the index of changes:
	   related_changes = (String[]) changes_index.get( new String(String.valueOf(A) 
	   					  + File.separatorChar + mapping_constraints[constraint].affixType));
	   if (related_changes == null )
	   {
		   	// need to create a line for this A... I'm going to make it start out with 
		   	// 100 elements, which should certainly be enough B's for each A (I hope!)
		   	related_changes = new String[ 100 ];
			// we'll use the first value in the row to keep track of how many B's are actually
			// in this row.  (this is a little funny, because we are using the String which corresponds
			// to the ascii value of the number of changes -- not very elegant, but easier than all of 
			// the other options which I have toyed with....)
		   	related_changes[0] = new String( Integer.toString(2) );
		   	related_changes[1] = new String( String.valueOf(B) );
		   	
		   	changes_index.put(new String(String.valueOf(A)+File.separatorChar + mapping_constraints[constraint].affixType), 
		   					   related_changes);
	   					   
		   	if (verbose)	
		   	{			   
			   	outputfile.write("...and adding a spot in the index for " + new String(String.valueOf(A)+File.separatorChar + mapping_constraints[constraint].affixType) + "\n");
		   	}
	   }
	   else
	   {
	   	// need to add this B to the line for this A
	   	// first, see how many changes were previously in this line:
		lastChange = Integer.parseInt( related_changes[0]);
		
		if (verbose)
		{
			outputfile.write("...there are already " + lastChange + " other changes with the description " + new String(String.valueOf(A)+File.separatorChar + mapping_constraints[constraint].affixType)+"\n");
		}
		
		// i really doubt we will ever need more than 100 different outcomes for
		// one A, but just in case, here is code to grow it if necessary:
		if (lastChange >= 99) // xxxxxx ack this is wrong -- needs to be an actual length value!
		{
			temp = new String[ related_changes.length * 2 ];
			for (int i=0; i < lastChange; i++)
				temp[i] = related_changes[i];
			related_changes = temp;
		}
		
		// now add the new B:
		related_changes[ lastChange ] = new String( String.valueOf(B));

		if (doppelgaengers)
		{
			// NOW, "catch up" this change w.r.t its "doppelgaenger" constraints.
			for (int i=1; i<lastChange;i++)
			{
				// we need to make copies of the constraints for A->related_changes[i],
				// with the change A->B
				// First, get the list of constraints for this change
				// (I'll reuse the matches[] array to get the list for this, since we're
				// done with it for purposes of checking whether there were existing matches)
				matches = (ConstraintBatch) changes.get( ""+ String.valueOf(A) + File.separatorChar 
													 + String.valueOf(related_changes[i]) 
													 + File.separatorChar + mapping_constraints[constraint].affixType);
													 
				for (int n=1;n<matches.constraints[0];n++)
				{
					// the constraint we will consider cloning is: mapping_constraints[ matches.constraints[n] ].
					// we will clone it if it is generalized.
					if (!mapping_constraints[ matches.constraints[n]].degenerate)
					{
						// we'll add a copy of this constraint, but using B instead of related_changes[i]:
						// first, build the changes PAQ->PBQ
						c = matches.constraints[n];
						anotherMapping1 = new String("" + String.valueOf(mapping_constraints[c].P) +
											String.valueOf(B) + String.valueOf(mapping_constraints[c].Q));
											
					     latest = addMappingConstraint( mapping_constraints[c].mappings[0],
					     				anotherMapping1.toCharArray(), 
					     				mapping_constraints[c].P, mapping_constraints[c].P_feat,
					     				mapping_constraints[c].Q_feat, mapping_constraints[c].Q,
					     				mapping_constraints[c].A, B,
					     				mapping_constraints[c].change_location,
					     				mapping_constraints[c].P_residue,
					     				mapping_constraints[c].Q_residue,
					     				mapping_constraints[c].P_features_active,
					     				mapping_constraints[c].Q_features_active);
					     mapping_constraints[ latest ].setDoppel(true);
					     assessConstraintViolations( latest );
					   				  
					     addStrucChange( mapping_constraints[c].A , B , (latest) );   //********

					}
				}			
			} // end of "catch-up" code
		}
		// and increment the lastChange value:
		lastChange++;
		// and replace the "placeholder" value:
		related_changes[0] = Integer.toString( lastChange );
		changes_index.put(new String(String.valueOf(A)+ File.separatorChar + mapping_constraints[constraint].affixType), 
							related_changes);
	   }
	   
  }
  else if (existing_matches == null && !stillLearning)
  {
  	// yikes, this shouldn't happen! this means something screwed up somewhere, yielding 
  	// a new structural change, when we should have simply applied an existing
  	// structural change
  	System.out.println("WARNING!!! something screwed up, to create a new structural change: " 
  				+ String.valueOf(A) + " -> " + String.valueOf(B));
  	return;
  }
  else 
  { // existing_matches is an array of integers, each of which is the index
	   // of a mapping constraint with the relevant struc desc

	 if( stillLearning )
	 {
	   // First, we need to add this guy to the pantheon
	   // Before we do, see if we need to add on to the pantheon.
	   existing_matches.add_element( constraint );

	   changes.put( newdesc, existing_matches );

	   	   
	 }// end of if(stillLearning)
	   x = existing_matches.size();
	   if (verbose)
	   {
	   		outputfile.write("...there are already "+x+" constraints with the description "+newdesc+"\n");
	   }

	   /* Now, we want to generalize against this batch.
	      There are two different cases here:
	     	(1) If the new constraint is degenerate, then we want to generalize
	   			against all existing constraints (degenerate or generalized)
	   		(2) If the new constraint is NOT degenerate, then by hypothesis, we do not
	   		    need to generalize from it, because we will gain nothing from
	   		    comparing it with its ancestors.  It will only be useful to compare
	   		    it with NEW degenerate constraints in the future.
	   */
	   
		// case (1), degenerate
		if (mapping_constraints[constraint].degenerate && !mapping_constraints[constraint].doppelgaenger) // case (1), degenerate
		{
		    for (int con = 1; con < x; con++)
		    {
			     compare = existing_matches.constraints[con];
					// generalize if the changes also match, as long as it's not
					// actually itself:
					if (compare != constraint 
//					&& match(mapping_constraints[compare].B, mapping_constraints[constraint].B)
			      			  )
					{
					    if(verbose)
					    {
					       outputfile.write("Generalizing constraints " + constraint + " and " + compare + ".\n");
					       outputfile.flush();
					    }					
							// order of arguments puts degenerate one first, in case
							// comparison constraint is itself generalized:	
							   generalize( constraint, compare); 
					
					}
			} // end of going through all relevant constraints	
			
		} // end of case (1)

/*
		else // case (2), generalized; only compare w/degenerates
		{
		    for (int c = 1; c < x; c++)
		    {
			     compare = existing_matches[c].intValue();
			     if (  mapping_constraints[ compare ].scope == 1 && compare != constraint)
			     {
		//      		System.out.print("Constraint " + compare + ": scope is 1");

						if (match(mapping_constraints[compare].B, 
				      			  mapping_constraints[constraint].B))
						{
		//				       outputfile.write("Generalizing constraints " + constraint + " and " + compare + ".\n");

								// order of arguments puts degenerate one first:	
							   generalize( compare, constraint); 
						
						}
				 } // end of checking one constraint
			} // end of going through all relevant constraints	
		} // end of case (2)

*/
	//   outputfile.write("Adding constraint "+constraint+ " to matches for [" + newdesc +"]\n");

  }
  
//  outputfile.write("... finished adding struc desc to the pantheon\n");
  return;
 }



 // __________________________________________________________________________________
 /*
  The "assessConstraintViolations" is the vertical version of assessFormViolations;
  it takes a new constraint and enters the violations for all known forms. 
 */
 public void assessConstraintViolations( int constraint ) throws IOException
 {
  int violation;
 
  if (debug)
   outputfile.write("...assessing violations for constraint #" + constraint + ".\n");
 
  // first, we should make sure the Violations grid is big enough:
//  expandViolationsGrid();
  
  /* We are assessing violations for the constraint: 
     mapping_constraints( constraint )
   This is done by cycling through the known forms, and comparing it
   with the new constraint.
  */
  for ( int f = 0; f < newestform; f++ )
  {
//   if (mapping_constraints[constraint].scope > 1 )
//   {
    if (debug)
     outputfile.write("...(confronting the constraint with form #" + f + ".)\n");
    violation = assessViolation( f, constraint );
//   }
//    Violations[f][constraint] = assessViolation( f, constraint );
//   else Violations[f][constraint] = 1;
  }
/*  
  // 3/25/99 new: trying axing the new constraint if it's scope is now 0:
  if (mapping_constraints[constraint].scope == 0)
  {
  	newestconstraint--;  
  }
*/
  return;
 } 
 
 // __________________________________________________________________________________
 /*
  The "reportNewConstraint" method prints to the console the information about
  a constraint which has just been added.
 */ 
 
 public void reportNewConstraint( int constraint ) throws IOException
 {
  outputfile.write("\t\tNew Mapping Constraint:\n");
  outputfile.write("\t\t\t[");
  if (mapping_constraints[constraint].P_residue)
   outputfile.write("X");
  outputfile.write(String.valueOf(mapping_constraints[constraint].mappings[0]));
  if (mapping_constraints[constraint].Q_residue)
   outputfile.write("Y");
  outputfile.write("] -> [");  
  if (mapping_constraints[constraint].P_residue)
   outputfile.write("X");
  outputfile.write(String.valueOf(mapping_constraints[constraint].mappings[1]));
  if (mapping_constraints[constraint].Q_residue)
   outputfile.write("Y");  
  outputfile.write( "].\n" );
  outputfile.write("\t\t\tActual change: [");
//  if (mapping_constraints[constraint].P.length() > 0)
//   outputfile.write("X");
  outputfile.write( String.valueOf( mapping_constraints[constraint].A));
//  if (mapping_constraints[constraint].Q.length() > 0)
//   outputfile.write("X");
  outputfile.write("] -> [");
//  if (mapping_constraints[constraint].P.length() > 0)
//   outputfile.write("X");
  outputfile.write( String.valueOf(mapping_constraints[constraint].B));
//  if (mapping_constraints[constraint].Q.length() > 0)
//   outputfile.write("X");
  outputfile.write("] / [");
  if (mapping_constraints[constraint].P_residue)
   outputfile.write("X");
  outputfile.write( mapping_constraints[constraint].P + "__");
  outputfile.write( String.valueOf(mapping_constraints[constraint].Q));
  if (mapping_constraints[constraint].Q_residue)
   outputfile.write("Y");
  outputfile.write("].\t");
  
  // We can also do some diagnosis of what KIND of affix we have.
  // Note that this is DIFFERENT from the diagnosis which is stored with the
  // mapping constraint, because that one tags how the WHOLE MAPPING works.
  // Here, we are kind of "peeking" by looking at A and B and seeing what
  // sort of process we are "aiming for" given enough generalization...
  if ( mapping_constraints[constraint].P.length == 0 && 
    !mapping_constraints[constraint].P_residue &&
    !mapping_constraints[constraint].P_features_active)
  {
   if ( mapping_constraints[constraint].Q.length == 0 && 
     !mapping_constraints[constraint].Q_residue &&
     !mapping_constraints[constraint].Q_features_active)
   {
    // ok, both sides are null-- either suppletion or circumfixation
    outputfile.write("(Suppletion)    (or...  *shudder*.. circumfixation)");
   }
   else
   {
    // just prefixation
    outputfile.write("(Prefixation)");
   }
  }
  else if ( mapping_constraints[constraint].Q.length == 0 && 
           !mapping_constraints[constraint].Q_residue &&
           !mapping_constraints[constraint].Q_features_active)
  {
   // so if we've gotten here, P is non-null but Q is.  Suffixation.
   outputfile.write("(Suffixation)");
  }
  else if ( mapping_constraints[constraint].A.length == 0 || 
      mapping_constraints[constraint].B.length == 0 )
  {
   // something inserted medially-- an infix (or ablaut w/zero grade)
   outputfile.write("(Infixation)    (or... ablaut w/zero grade)");
  }
  else outputfile.write("(Ablaut/Umlaut)");  
  
/*  
  outputfile.write("\n\t\tP residue = ");
  if ( mapping_constraints[constraint].P_residue)
   outputfile.write("true");
  else outputfile.write("false");
  outputfile.write(", Q residue = ");
  if ( mapping_constraints[constraint].Q_residue)
   outputfile.write("true");
  else outputfile.write("false");
  outputfile.write(".");
*/  
  outputfile.write("\n");
    
  return;
 }

 // __________________________________________________________________________________
 /*
  The "generalize" method is where we do the real work of learning abstract
  schemas.  It takes a mapping constraint as an argument (or, more precisely,
  the index of a constraint in the mapping_constraints array), and prowls around 
  for other mapping constraints which share material.  It then generates new
  mapping constraints based on the shared material.
  
  NEWSFLASH: (5/5/98) now generalize takes two ints, and just checks one constraint
  against another.  this eliminates the step of prowling thru all existing constraints,
  which is now done elsewhere in a more efficient manner.
 */ 
 public void generalize( int constraint, int c ) throws IOException
 {
  // In order to generalize, we will need to do some structural factorization:
  // In principle, we could need up to 7 different factors:
  // (I will use 'fshare' to mean the features that one corresponding segment
  //  shares, and I will use 'sshare' to mean one or more segments which strictly
  //  match between the two strings)

  // constraint 1:  
  //      P1_residue P_fshare P_sshare A Q_sshare Q_fshare Q1_residue
  // constraint 2:
  //      P2_residue P_fshare P_sshare A Q_sshare Q_fshare Q2_residue
  
  // Therefore, we need a hellish number of variables here:
    
  boolean P1_residue = false;
  boolean P_features_active = false;
  int P_seg_decompose[] = new int[numberOfFeatures];
  int P_fshare[][] = new int[2][numberOfFeatures];
  char P_sshare[];
  char Q_sshare[];
  int Q_seg_decompose[] = new int[numberOfFeatures];
  int Q_fshare[][] = new int[2][numberOfFeatures];
  boolean Q1_residue = false;
  boolean Q_features_active = false;
  
  // and some variables to help calculate all of this:
  int shorter;
  int P_sshare_index, Q_sshare_index;
  
  boolean unique = true;
  StringBuffer new_constraint0 = new StringBuffer();
  StringBuffer new_constraint1 = new StringBuffer();
  char new_const[][] = new char[2][];
  boolean match;
  ConstraintBatch existing_matches;
  int numberOfInactiveFeatures = 0;
  
  int latest = 0;
  int temp = 0, con = 0;
  String[] otherChanges;
//  String anotherMapping0, anotherMapping1;


  if (debug)  
   outputfile.write(" ... OK.  trying to generalize.\n");
   generalizeform:{

    if (verbose)
    {
     outputfile.write("Generalizing:  ["+ String.valueOf(mapping_constraints[constraint].mappings[0])
        + "] -> ["+ String.valueOf(mapping_constraints[constraint].mappings[1]));
     outputfile.write("] has the same change as ["+ String.valueOf(mapping_constraints[c].mappings[0])
        + "] -> ["+String.valueOf(mapping_constraints[c].mappings[1])+"]   (" + String.valueOf(constraint) + "," +
        c + ").\n");
    }

    // Now we need to see what they have in common.
    
    // First, the P side:
    if (verbose)
    {
     outputfile.write("\t\tComparing ["+String.valueOf(mapping_constraints[c].P)+"] with ["
         +String.valueOf(mapping_constraints[constraint].P)+"].\n");
    }
    
    P_sshare_index = matchRight( mapping_constraints[c].P, 
          mapping_constraints[constraint].P, 
          0);          
    
    // build the shared P segments
	P_sshare = new char[P_sshare_index];
    System.arraycopy( mapping_constraints[c].P, 
          (mapping_constraints[c].P.length - P_sshare_index),
          P_sshare, 0, P_sshare_index);

    if (verbose)
//    if (debug)
    {          
     outputfile.write("\t\t\t"+ P_sshare_index +" characters match on the left.\n");

     outputfile.write("\t\tShared segmental material before the structural change: "+String.valueOf(P_sshare)+"\n");
//     outputfile.write("\t\t  Residue from first form:  "+
//         mapping_constraints[c].P.substring(0,(P_sshare.length()-P_sshare_index)) + "\n");
    }  

    
    P_features:{
    
    // and now look for shared P-side features:
	/* There is a slew (slue?) of possible cases here, depending on whether 
	   the constraints are "string-subsumed" (i.e. the entire segmental portion
	   of the P side has been matched up), whether the constraints also had
	   P-side features which could be matched up, and whether the constraints
	   had a P-side residue.
	   
	   The formal definition of "string-subsumed" for a left-side P is:
			   P.length <= (P_sshare_index + 1)
	   
	   It is convenient to talk about the "new constraint" as the one which
	   spawned this generalization (i.e. mapping_constraints[constraint]),
	   and the "comparison constraint" as the one which is being generalized
	   against (i.e. mapping_constraints[c]).  Note that only the comparison
	   constraint could possibly have active features or residue on either
	   side, because the new constraint is degenerate and only has segments.
	   
	   The cases are:
	  
	   1. The comparison constraint (mappings_constraints[c]) is string-subsumed,
	      and so is the new (degenerate) constraint (mapping_constraints[constraint]).
	      
	      a. if the comparison constraint has active P_features or P_residue,
	      	 then this information needs to be carried over:
	      	 *	P features inactive, P residue TRUE.
	      b. if the comparison constraint has neither P residue nor active
	      	 P features, then there is nothing to carry over:
	      	 *	P features inactive, P residue FALSE.
	      	 
	     NOTE: it is impossible to have the case where both sides are string-subsumed
	     	   but each has features to compare, because this would require that we
	     	   compare two generalized constraints, but in fact at most one of the 
	     	   constraints (the comparison constraint) will be a generalized constraint.

	   2. The comparison constraint is string-subsumed, but the new constraint
	      is NOT string-subsumed.
	      
	      a. if the comparison constraint has active P features, then
	      	 use them, and compare with the next remaining segment from the 
	      	 new constraint:
	      	 	P_fshare[0] = comparison constraint's P features
	      	 	P_fshare[1] = decomposeSeg( first unique seg in new constraint's
	      	 								P, looking right to left )
	      	 	(P features active if any features are still specified after this
	      	 	 comparison)							
	      	 								
	      	 	(P residue contingent on whether there was more than one seg
	      	 	 left in new constraint, or if the comparison constraint had
	      	 	 P_residue = true)
	      	 	 
	      b. if the comparison constraint does not have active P features, then 
	      	 we have no chance of creating P features here: just leave the rest
	      	 as residue:
	      	 	* P features inactive, P residue TRUE
	      	 (NB it does not matter whether the comparison constraint already has
	      	  P_residue true or not, because the remaining segments in the new
	      	  constraint provide a P residue if it doesn't already exist.)


	    3. The comparison constraint is NOT string-subsumed, but the new constraint
	       IS string-subsumed.
	       Since the new constraint is consumed, the rest is just residue:
	       		* P features inactive, P residue TRUE

		4. The comparison constraint is not string-subsumed, and neither is
		   the new constraint.
		   There are segments left in both constraints, so this works just like
		   it did when only degenerate constraints where considered for 
		   generalizing:
		   		P_fshare[0] = decomposeSeg( 1st non-matching seg in compare con)
		   		P_fshare[1] = decomposeSeg( 1st non-matching seg in new con)
		   (P features active if any features are found to be in common between
		    the two segments)
		   	
		   (P residue contingent on whether there was more than one segment left
		    in either constraint, or whether the comparison constraint already had
		    a P residue or P features active)	

	*/
	
	// First, see if the comparison constraint has been string-subsumed:
	if (mapping_constraints[c].P.length < (P_sshare_index + 1) )
	{
		// Also, see if the degenerate constraint has been string-subsumed
		if (mapping_constraints[constraint].P.length < (P_sshare_index + 1))
		{
			// This is case (1) above  (both sides string-subsumed).
			// Now there is P residue if the comparison constraint has
			// either active P features or P residue.
			if (mapping_constraints[c].P_features_active ||
				mapping_constraints[constraint].P_residue)
			{
				if (verbose)
					outputfile.write("\t\tP side: generalization case (1a)\n");

				// there is residue (case (1a)):
				P1_residue = true;
				// but no material to create P features:
				P_features_active = false;
			}	
			else
			{
				if (verbose)
					outputfile.write("\t\tP side: generalization case (1b)\n");

				// there is no residue (case (1b))
				P1_residue = false;
				// and also no material to create P features:
			    P_features_active = false;
			    
			} // end of case 1, both constraints string-subsumed
		} // end of degenerate constraint string-subsumed
		else  //  the degenerate constraint is NOT string-subsumed
		if (mapping_constraints[c].P_features_active)
		{
			// this is case (2a), where there is a segmental material
			// left in the degenerate constraint, but only features
			// left in the comparison constraint.

			if (verbose)
				outputfile.write("\t\tP side: generalization case (2a)\n");


			// the best way to do this is to figure out the upper and lower
			// bounds feature by feature:
			// The bounds will be the bounds from the comparison constraint,
			// unless the feature from the degenerate constraint's segment
			// happens to fall outside of them, in which case we use it.
			
			// first, we get the features for that segment:
			// Since these values could end up as either the upper or lower bounds,
			// start with them in both:
			P_seg_decompose = decomposeSeg(mapping_constraints[constraint].P[ mapping_constraints[constraint].P.length - (P_sshare_index + 1)]);
			
			// If the segment happens to be unknown (i.e., not in the
			// features table), then just give up and deactivate the 
			// features:
			if (P_seg_decompose == null)
			 {
			 	System.out.print("Warning: unknown segment: "
			 						+ mapping_constraints[constraint].P[ mapping_constraints[constraint].P.length - (P_sshare_index + 1)] + "\n");

			 	outputfile.write("Warning: unknown segment: "
			 						+ mapping_constraints[constraint].P[ mapping_constraints[constraint].P.length - (P_sshare_index + 1)] + "\n");
			 	P_features_active = false;
			 	P1_residue = true;
			 	break P_features;
			 }
			System.arraycopy(P_seg_decompose,0,P_fshare[0],0,numberOfFeatures);
			System.arraycopy(P_seg_decompose,0,P_fshare[1],0,numberOfFeatures);
			


			/*
			*** NEED TO DO A THREE-WAY COMPARISON HERE  ***
			(existing LB, existing UB, and features of new segment)
			*** evil evil evil evil evil evil evil evil ***
			*/
			
			// now we go through, feature by feature, and compare:
			//    (this is a three-way version of the "compare and flip" algorithm used below)
			for (int i =0; i < numberOfFeatures; i++)
			{
				// use the lower bound from the comparison constraint, unless the value from
				// the degenerate constraint happens to be lower:
				P_fshare[0][i] = ( P_fshare[0][i] <= mapping_constraints[c].P_feat[0][i] ? 
								   P_fshare[0][i] : mapping_constraints[c].P_feat[0][i] );
								   
				// use the upper bound from the comparison constraint, unless the value from
				// the degenerate constraint happens to be higher:
				P_fshare[1][i] = ( P_fshare[1][i] >= mapping_constraints[c].P_feat[1][i] ?
								   P_fshare[1][i] : mapping_constraints[c].P_feat[1][i] );


				// also, while we're here, we should "deactivate" features which 
				// don't apply, or which now use the range exhaustively:
			      if (P_fshare[0][i] == -1 )
			      {
				       P_fshare[0][i] = INACTIVE;
				       P_fshare[1][i] = INACTIVE;
				       numberOfInactiveFeatures++;
			      }
			      else if (P_fshare[0][i] == INACTIVE)
			      {
			      	   P_fshare[1][i] = INACTIVE;
				       numberOfInactiveFeatures++;
			      }
			      else if ( P_fshare[0][i] == learnerTask.featureBounds[0][i]
			       && P_fshare[1][i] == learnerTask.featureBounds[1][i])
			      {
				       P_fshare[0][i] = INACTIVE;
				       P_fshare[1][i] = INACTIVE;
				       numberOfInactiveFeatures++;
			      }

			}
			// finally, if all features were deactivated, then just make residue:
			if( numberOfInactiveFeatures == numberOfFeatures )
			{
				P1_residue = true;
				P_features_active = false;
			}
			
			// otherwise, note that P features are active, and see if there's still residue:
			// 2/21/99 NOTE: residue could come either from additional segments here,
			// or inherited from above!
			else
			{
				P_features_active = true;
				
				if ( (mapping_constraints[constraint].P.length - (P_sshare_index + 1)) > 0 )
				{
					P1_residue = true;
				}
				else if (mapping_constraints[c].P_residue)
				{
					P1_residue = true;
				}
				else P1_residue = false;
			}
							 
		} // end of case (2a)
	
		else // this is case (2b), where there is no material to
		     // create P-features from
		{
			if (verbose)
				outputfile.write("\t\tP side: generalization case (2b)\n");

			// Leftover material from degenerate becomes P_residue
			P1_residue = true;
			// the features are inactive
			P_features_active = false;
		} // end of case (2b)
		
	} // end of cases (1)-(2)
	else // the comparison constraint is NOT string-subsumed
	{
		// is the degenerate constraint also string-subsumed?
		if (mapping_constraints[constraint].P.length < (P_sshare_index + 1))
		{
			// this is case (3) above, where the comparison constraint
			// is not string-subsumed, but the degenerate one is.
			// No material to create features, so the rest of the 
			// string becomes P residue.

			if (verbose)
				outputfile.write("\t\tP side: generalization case (3)\n");
			
			P1_residue = true;
			P_features_active = false;
		} // end of case (3)
		else // both constraints have segments left (case 4)
		{
			if (verbose)
				outputfile.write("\t\tP side: generalization case (4)\n");		
		
			 P_seg_decompose = decomposeSeg(mapping_constraints[c].P[ mapping_constraints[c].P.length - (P_sshare_index + 1)]);
		     if (P_seg_decompose == null)
			 {
			 	System.out.print("Warning: unknown segment: "
			 						+ mapping_constraints[c].P[ mapping_constraints[c].P.length - (P_sshare_index + 1)] + "\n");

			 	outputfile.write("Warning: unknown segment: "
			 						+ mapping_constraints[c].P[ mapping_constraints[c].P.length - (P_sshare_index + 1)] + "\n");
			 	P_features_active = false;
			 	P1_residue = true;
			 	break P_features;
			 }
			 
			 System.arraycopy(P_seg_decompose,0,P_fshare[0],0,numberOfFeatures);

			 P_seg_decompose = decomposeSeg(mapping_constraints[constraint].P[ mapping_constraints[constraint].P.length - (P_sshare_index + 1)]);
		     //  We need to handle unknown segments in some way-- for now, I'll just
		     //  output a warning and give up on features for this constraint:
			 if (P_fshare[1] == null)
			 {
			 	System.out.print("Warning: unknown segment: "
			 						+ mapping_constraints[constraint].P[ mapping_constraints[constraint].P.length - (P_sshare_index + 1)] + "\n");
			 	outputfile.write("Warning: unknown segment: "
			 						+ mapping_constraints[constraint].P[ mapping_constraints[constraint].P.length - (P_sshare_index + 1)] + "\n");
			 	P_features_active = false;
			 	P1_residue = true;
			 	break P_features;
			 }
			 else P_features_active = true; // safe to do this now that both sides are not null
			 System.arraycopy(P_seg_decompose,0,P_fshare[1],0,numberOfFeatures);

		     // next, we sort the values so that the lower bound is in P_fshare[0], and the 
		     // upper bound is in P_fshare[1]
		     // At the same time, we "deactivate" features which use the whole range, by
		     // setting the values to INACTIVE
		     for (int i = 0; i < numberOfFeatures; i++)
		     {
			      // is this the most efficient way to swap the two?
			      if (P_fshare[0][i] > P_fshare[1][i])
			      {
			       temp = P_fshare[0][i];
			       P_fshare[0][i] = P_fshare[1][i];
			       P_fshare[1][i] = temp;
			      }
			      
			      // "Deactivate" features which don't apply or which use the range exhaustively
			      if (P_fshare[0][i] == -1 )
			      {
				       P_fshare[0][i] = INACTIVE;
				       P_fshare[1][i] = INACTIVE;
					   numberOfInactiveFeatures++;				       
			      }
			      else if (P_fshare[0][i] == INACTIVE)
			      {
			     		P_fshare[1][i] = INACTIVE;
					   numberOfInactiveFeatures++;				       
			      }
			      
			      
			      else if ( P_fshare[0][i] == learnerTask.featureBounds[0][i]
			       && P_fshare[1][i] == learnerTask.featureBounds[1][i])
			      {
				       P_fshare[0][i] = INACTIVE;
				       P_fshare[1][i] = INACTIVE;
					   numberOfInactiveFeatures++;				       
			      }
		     } // end of sorting & deactivating
			if (numberOfInactiveFeatures == numberOfFeatures)
			{
				P1_residue = true;
				P_features_active = false;
			}

			// also, there is still P residue if there is at least one more segment left
			//  in either constraint:
			else if ( (mapping_constraints[c].P.length - (P_sshare_index + 1)) > 0 ||
				 (mapping_constraints[constraint].P.length - (P_sshare_index + 1)) > 0 )
			{
				P1_residue = true;
			}
			// or, if the comparison constraint had P features or P residue, then there
			// is also P residue:
			else if ( mapping_constraints[c].P_features_active ||
					  mapping_constraints[c].P_residue )
			{
				P1_residue = true;
			}
			// otherwise, no residue 
			else P1_residue = false;

			/*     
				// Some code to print out the results for debugging:
				
			     outputfile.write("Result is: \n\t[");
			     for (int x = 0; x<numberOfFeatures; x++)
			     {
			      outputfile.write( P_fshare[0][x] + " ");
			     }
			     outputfile.write("]\n\t[");
			     for (int x = 0; x<numberOfFeatures; x++)
			     {
			      outputfile.write( P_fshare[1][x] + " ");
			     }
			     outputfile.write("]\n");     
			*/ 
			//     learnerTask.printFeatureMatrix( mapping_constraints[c].P[ mapping_constraints[c].P.length - (P_sshare_index + 1)]);
			//     learnerTask.printFeatureMatrix( mapping_constraints[constraint].P[ mapping_constraints[constraint].P.length - (P_sshare_index + 1)]);


			
		} // end of case (4) 
	} // end of cases (3-4)
      
    } // end of search for P-side features (P_features block)
	if (verbose)
	{
		outputfile.write("\t\tP residue: ");
		if (P1_residue)
			outputfile.write("TRUE");
		else
			outputfile.write("FALSE");
		outputfile.write(",\tP features active: ");
		if (P_features_active)
		{
			outputfile.write("TRUE:\n");
			for (int x = 0; x < numberOfFeatures; x++)
			{
				outputfile.write("\t\t\t(" + P_fshare[0][x] + "," + P_fshare[1][x] + ")  ");
			}
		}		else 
			outputfile.write("FALSE");
		outputfile.write("\n");
	}

	// if we have emerged without active P_features, then we need to provide "inactive" values:
	if (!P_features_active)
	{
	    for (int i = 0; i < numberOfFeatures; i++)
	    {
	     	P_fshare[0][i] = INACTIVE;
	     	P_fshare[1][i] = INACTIVE;
	    }
	}

    // reset the numberOfInactiveFeatures 
    numberOfInactiveFeatures = 0;
         
    // Then, the Q side segments:      
    shorter = (mapping_constraints[c].Q.length <= mapping_constraints[constraint].Q.length 
       ? mapping_constraints[c].Q.length : mapping_constraints[constraint].Q.length);
                         
    Q_sshare_index = matchLeft( mapping_constraints[c].Q,
           mapping_constraints[constraint].Q,
           shorter );
           
    Q_sshare = new char[Q_sshare_index];
    for (int i=0; i<Q_sshare_index; i++)
  	{
  	   Q_sshare[i] = mapping_constraints[c].Q[i];
	}
	
    if (verbose)
//    if (debug)
    {          
     outputfile.write("\t\t\t"+ Q_sshare_index +" characters match on the right.\n");

     outputfile.write("\t\tShared segmental material after the structural change: "+String.valueOf(Q_sshare) + "\n");
//     outputfile.write("\t\t  Residue from first form:  "+
//         mapping_constraints[c].P.substring(0,(P_sshare.length()-P_sshare_index)) + "\n");
    }  
	
	// and once we've tried to match segments, then try for features:
    
    Q_features:{
   	/* 
		The possible cases here are the same as those for P_features above.
		
		For the Q side, the definition of "string-subsumed" is:	
			Q_sshare_index >= Q.length - 1

		OR, easier to think about:  Q.length <= Q_sshare_index
		(that is, everything is shared, including the last character -- in
		fact, Q.length can never be less than Q_sshare_index, because that
		would mean that segments were found to match after the last char of
		the string!   but might as well be exhaustive in our checks...)			
	*/
	
	// First, see if the comparison constraint has been string-subsumed:
	if ((mapping_constraints[c].Q.length) <= Q_sshare_index )
	{
		// Also, see if the degenerate constraint has been string-subsumed
		if ((mapping_constraints[constraint].Q.length) <= Q_sshare_index)
		{
			// This is case (1) above  (both sides string-subsumed).
			// Now there is Q residue if the comparison constraint has
			// either active Q features or Q residue.
			if (mapping_constraints[c].Q_features_active ||
				mapping_constraints[constraint].Q_residue)
			{
				// there is residue (case (1a)):
				if (verbose)
					outputfile.write("\t\tQ side: generalization case (1a)");
				Q1_residue = true;
				// but no material to create Q features:
				Q_features_active = false;
			}	
			else
			{
				// there is no residue (case (1b))
				if (verbose)
					outputfile.write("\t\tQ side: generalization case (1b)\n");

				Q1_residue = false;
				// and also no material to create Q features:
			    Q_features_active = false;
			    
			} // end of case 1, both constraints string-subsumed
		} // end of degenerate constraint string-subsumed
		else  //  the degenerate constraint is NOT string-subsumed
		if (mapping_constraints[c].Q_features_active)
		{
			// this is case (2a), where there is a segmental material
			// left in the degenerate constraint, but only features
			// left in the comparison constraint.

			if (verbose)
				outputfile.write("\t\tQ side: generalization case (2a)\n");


			// the best way to do this is to figure out the upper and lower
			// bounds feature by feature:
			// The bounds will be the bounds from the comparison constraint,
			// unless the feature from the degenerate constraint's segment
			// happens to fall outside of them, in which case we use it.
			
			// first, we get the features for that segment:
			// Since these values could end up as either the upper or lower bounds,
			// start with them in both:
			
			Q_seg_decompose = decomposeSeg(mapping_constraints[constraint].Q[Q_sshare_index]);
			// If the segment happens to be unknown (i.e., not in the
			// features table), then just give up and deactivate the 
			// features:
			if (Q_seg_decompose == null)
			 {
			 	System.out.print("Warning: unknown segment: "
			 						+ mapping_constraints[constraint].Q[Q_sshare_index] + "\n");
			 	outputfile.write("Warning: unknown segment: "
			 						+ mapping_constraints[constraint].Q[Q_sshare_index] + "\n");
			 	Q_features_active = false;
			 	Q1_residue = true;
			 	break Q_features;
			 }
			// otherwise, we're safe			
			System.arraycopy(Q_seg_decompose,0,Q_fshare[0],0,numberOfFeatures);
			System.arraycopy(Q_seg_decompose,0,Q_fshare[1],0,numberOfFeatures);



			/*
			*** NEED TO DO A THREE-WAY COMPARISON HERE  ***
			(existing LB, existing UB, and features of new segment)
			*** evil evil evil evil evil evil evil evil ***
			*/
			
			// now we go through, feature by feature, and compare:
			//    (this is a three-way version of the "compare and flip" algorithm used below)
			for (int i =0; i < numberOfFeatures; i++)
			{
				// use the lower bound from the comparison constraint, unless the value from
				// the degenerate constraint happens to be lower:
				Q_fshare[0][i] = ( Q_fshare[0][i] <= mapping_constraints[c].Q_feat[0][i] ? 
								   Q_fshare[0][i] : mapping_constraints[c].Q_feat[0][i] );
								   
				// use the upper bound from the comparison constraint, unless the value from
				// the degenerate constraint happens to be higher:
				Q_fshare[1][i] = ( Q_fshare[1][i] >= mapping_constraints[c].Q_feat[1][i] ?
								   Q_fshare[1][i] : mapping_constraints[c].Q_feat[1][i] );

				// also, while we're here, we should "deactivate" features which 
				// don't apply, or which now use the range exhaustively:
			      if (Q_fshare[0][i] == -1 )
			      {
				       Q_fshare[0][i] = INACTIVE;
				       Q_fshare[1][i] = INACTIVE;
				       numberOfInactiveFeatures++;
			      }
			      
			      else if (Q_fshare[0][i] == INACTIVE)
			      {
			      		Q_fshare[1][i] = INACTIVE;
			      		numberOfInactiveFeatures++;
			      }
			      
			      else if ( Q_fshare[0][i] == learnerTask.featureBounds[0][i]
			       && Q_fshare[1][i] == learnerTask.featureBounds[1][i])
			      {
				       Q_fshare[0][i] = INACTIVE;
				       Q_fshare[1][i] = INACTIVE;
				       numberOfInactiveFeatures++;
			      }

			}
			// finally, if all features were deactivated, then just make residue:
			if( numberOfInactiveFeatures == numberOfFeatures )
			{
				Q1_residue = true;
				Q_features_active = false;
			}

			// otherwise, note that Q features are active, and see if there's still residue:
			else
			{
				Q_features_active = true;
				
				if ( (Q_sshare_index + 1) < mapping_constraints[constraint].Q.length )
				{
					Q1_residue = true;
				}
				else if (mapping_constraints[c].Q_residue) // new 2/21/99
					Q1_residue = true;
				else Q1_residue = false;
			}							 
		} // end of case (2a)
		
		else // this is case (2b), where there is no material to
		     // create Q-features from
		{
			if (verbose)
				outputfile.write("\t\tQ side: generalization case (2b)\n");

			// Leftover material from degenerate becomes Q_residue
			Q1_residue = true;
			// the features are inactive
			Q_features_active = false;
		} // end of case (2b)
	
	} // end of cases (1)-(2)
	else // the comparison constraint is NOT string-subsumed
	{
		// is the degenerate constraint also string-subsumed?
		if ( Q_sshare_index > (mapping_constraints[constraint].Q.length - 1))
		{
			// this is case (3) above, where the comparison constraint
			// is not string-subsumed, but the degenerate one is.
			// No material to create features, so the rest of the 
			// string becomes Q residue.

			if (verbose)
				outputfile.write("\t\tQ side: generalization case (3)\n");

			Q1_residue = true;
			Q_features_active = false;
		} // end of case (3)
		else // both constraints have segments left (case 4)
		{
		
			if (verbose)
				outputfile.write("\t\tQ side: generalization case (4)\n");

			 Q_seg_decompose = decomposeSeg(mapping_constraints[c].Q[Q_sshare_index]);
		     //  We need to handle unknown segments in some way-- for now, I'll just
		     //  output a warning and give up on features for this constraint:
		     if (Q_seg_decompose == null)
			 {
			 	System.out.print("Warning: unknown segment: "
			 						+ mapping_constraints[c].Q[Q_sshare_index] + "\n");
			 	outputfile.write("Warning: unknown segment: "
			 						+ mapping_constraints[c].Q[Q_sshare_index] + "\n");
			 	Q_features_active = false;
			 	Q1_residue = true;
			 	break Q_features;
			 }
		     System.arraycopy( Q_seg_decompose,0,Q_fshare[0],0,numberOfFeatures);
		     
			 Q_seg_decompose = decomposeSeg(mapping_constraints[constraint].Q[Q_sshare_index]);
		     //  We need to handle unknown segments in some way-- for now, I'll just
		     //  output a warning and give up on features for this constraint:
			 if (Q_seg_decompose == null)
			 {
			 	System.out.print("Warning: unknown segment: "
			 						+ mapping_constraints[constraint].Q[Q_sshare_index] + "\n");
			 	outputfile.write("Warning: unknown segment: "
			 						+ mapping_constraints[constraint].Q[Q_sshare_index] + "\n");
			 	Q_features_active = false;
			 	Q1_residue = true;
			 	break Q_features;
			 }
			 else Q_features_active = true; // safe to do this now that neither is null

		     System.arraycopy( Q_seg_decompose,0,Q_fshare[1],0,numberOfFeatures);

		     // next, we sort the values so that the lower bound is in Q_fshare[0], and the 
		     // upper bound is in Q_fshare[1]
		     // At the same time, we "deactivate" features which use the whole range, by
		     // setting the values to INACTIVE
		     for (int i = 0; i < numberOfFeatures; i++)
		     {
			      // is this the most efficient way to swap the two?
			      if (Q_fshare[0][i] > Q_fshare[1][i])
			      {
			       temp = Q_fshare[0][i];
			       Q_fshare[0][i] = Q_fshare[1][i];
			       Q_fshare[1][i] = temp;
			      }
			      
			      // "Deactivate" features which don't apply or which use the range exhaustively
			      if (Q_fshare[0][i] == -1 )
			      {
				       Q_fshare[0][i] = INACTIVE;
				       Q_fshare[1][i] = INACTIVE;
				       numberOfInactiveFeatures++;
			      }
			      else if (Q_fshare[0][i] == INACTIVE)
			      {
			      		Q_fshare[1][i] = INACTIVE;
			      		numberOfInactiveFeatures++;
			      }
			      else if ( Q_fshare[0][i] == learnerTask.featureBounds[0][i]
			       && Q_fshare[1][i] == learnerTask.featureBounds[1][i])
			      {
				       Q_fshare[0][i] = INACTIVE;
				       Q_fshare[1][i] = INACTIVE;
				       numberOfInactiveFeatures++;
			      }
		     } // end of sorting & deactivating

			// finally, there is still Q residue if there is at least one more segment left
			//  in either constraint:
			
			if (numberOfInactiveFeatures == numberOfFeatures)
			{
				Q1_residue = true;
				Q_features_active = false;
			}


			else if ( ((Q_sshare_index + 1) < mapping_constraints[c].Q.length) ||
				 ((Q_sshare_index + 1) < mapping_constraints[constraint].Q.length) )
			{
				Q1_residue = true;
			}
			// or, if the comparison constraint had Q features or Q residue, then there
			// is also P residue:
			else if ( mapping_constraints[c].Q_features_active ||
					  mapping_constraints[c].Q_residue )
			{
				Q1_residue = true;
			}
			// otherwise, no residue 

			else Q1_residue = false;

			/*     
				// Some code to print out the results for debugging:
				
			     outputfile.write("Result is: \n\t[");
			     for (int x = 0; x<numberOfFeatures; x++)
			     {
			      outputfile.write( Q_fshare[0][x] + " ");
			     }
			     outputfile.write("]\n\t[");
			     for (int x = 0; x<numberOfFeatures; x++)
			     {
			      outputfile.write( Q_fshare[1][x] + " ");
			     }
			     outputfile.write("]\n");     
			*/ 
			//     learnerTask.printFeatureMatrix( mapping_constraints[constraint].Q[Q_sshare_index]);
			//     learnerTask.printFeatureMatrix( mapping_constraints[c].Q[Q_sshare_index]);


			
		} // end of case (4) 
	} // end of cases (3-4)
      
    } // end of search for Q-side features (Q_features block)

	if (verbose)
	{
		outputfile.write("\t\tQ residue: ");
		if (Q1_residue)
			outputfile.write("TRUE");
		else
			outputfile.write("FALSE");
		outputfile.write(",\tQ features active: ");
		if (Q_features_active)
		{
			outputfile.write("TRUE:\n");
			for (int x = 0; x < numberOfFeatures; x++)
			{
				outputfile.write("\t\t\t(" + Q_fshare[0][x] + "," + Q_fshare[1][x] + ")  ");
			}
		}
		else 
			outputfile.write("FALSE");
		outputfile.write("\n");
	}

	
	
	// if we have emerged without active Q_features, then we need to provide "inactive" values:
	if (!Q_features_active)
	{
	    for (int i = 0; i < numberOfFeatures; i++)
	    {
	     	Q_fshare[0][i] = INACTIVE;
	     	Q_fshare[1][i] = INACTIVE;
	    }
	}
	    

        
    //  now if both P_sshare and Q_sshare are null, then we are trying
    // to do too much:  create a mapping constraint with a blanket context,
    // X_Y.  bad things happen when we allow this to be true, so
    // we will intercept this now:

/*    if (!P_features_active && !Q_features_active
        && P1_residue && Q1_residue)  */   // ACK! a bug, i think -- too restrictive, need to check if there are segs
    if (!P_features_active && !Q_features_active
        && P1_residue && Q1_residue && Q_sshare.length == 0 && P_sshare.length == 0)
        
    {
     if (verbose)
//     if (debug)
      outputfile.write("-> The hypothesized generalization involves a null context.  Abort mission.\n");
     break generalizeform;
    }


    new_constraint0 = new StringBuffer(P_sshare.length + mapping_constraints[c].A.length + Q_sshare.length);
    new_constraint0.append( P_sshare );
    new_constraint0.append(mapping_constraints[c].A);
    new_constraint0.append( Q_sshare );

    new_constraint1 = new StringBuffer(P_sshare.length + mapping_constraints[c].B.length + Q_sshare.length);
    new_constraint1.append( P_sshare );
    new_constraint1.append( mapping_constraints[c].B );
    new_constraint1.append( Q_sshare );
    
    
	/*
		Now that we have computed all the pieces of a new constraints, we need
		to know if it is unique.
		(Perhaps there is a better way to do this????)
		For now we do this looking at all the constraints who live in the 
		relevant quadrant of the structural descriptions pantheon, and 
		seeing if any of them matches the new challenger.
	*/    
   
    new_const[0] = new char[ new_constraint0.length() ];
    if (new_constraint0.length() != 0 )
     new_constraint0.getChars( 0, new_constraint0.length(), new_const[0], 0);
    
    new_const[1] = new char[ new_constraint1.length() ];
    if (new_constraint1.length() != 0 )
     new_constraint1.getChars( 0, new_constraint1.length(), new_const[1], 0);
     
    
    if (verbose)
     outputfile.write("Checking new constraint to see if it is unique...\n");
    
	// by default we start by assuming that both P & Q sides are unique; we then 
	// try to disprove this hypothesis by seeing if they match.
    existing_matches = ( ConstraintBatch ) changes.get( new String(""+ String.valueOf(mapping_constraints[c].A ) + 
    											File.separatorChar + String.valueOf(mapping_constraints[c].B) 
    											+ File.separatorChar + mapping_constraints[c].affixType));
    if (existing_matches != null )
    {
	     temp = existing_matches.size();

		if (verbose)
			outputfile.write("\t\tChecking against constraints:\n");

	     uniquenesscheck:{
	     
//	     if(stillLearning)
//	     {
	     // while we're actually learning, we'll
	     // we'll look at all the constraints which share the same description:
		     for (int i = 1; i < temp; i++)
		     {
			    con = existing_matches.constraints[i];

			    if (verbose)
			    	outputfile.write( "\t\t" + con + "\t" );

				checkparticularconstraint:{
			    // a constraint is threatened by another constraint with the 
			    // same PAQ->PBQ  (mappings[x] encodes the entire PXQ)
			    if (match(mapping_constraints[con].mappings[0], new_const[0]) &&
			     match(mapping_constraints[con].mappings[1], new_const[1]) )
			     {
					 // the "core" of the constraint matches (i.e., the segmental change);
					 // so now check the stuff at the edges:
				     P_side_unique:{
						// first, check the residues:
						if (mapping_constraints[con].P_residue == P1_residue)
				     	{
				     		// now check the features: as always, 4 possibilities
				     		if (P_features_active)
				     		{
				     			if (mapping_constraints[con].P_features_active)
				     			{
				     				// both are active, so see if the features match:
				     				if (featuresMatch(P_fshare[0], mapping_constraints[con].P_feat[0]) &&
				     					featuresMatch(P_fshare[1], mapping_constraints[con].P_feat[1]))
				     					{
				     						// EVERYTHING matches on the P_side (residues, features)
				     						unique = false;
				     						if (debug)
				     							outputfile.write("the constraints are the same on the P side");
				     					}
				     				// otherwise only P has active features, they are different.
				     				// (do nothing, leave unique = true)
				     				else if (debug)
					     				outputfile.write("only the new constraint has active P-side features.");
				     			}
				     		}
				     		else if (!mapping_constraints[con].P_features_active)
				     		{
								// in this case, neither side has active P_features, so the
								// constraints are the same on the P side:
								unique = false;
								if (debug)
									outputfile.write("the constraints are the same on the P side.");
				     		}
			     			// otherwise, new constraint doesn't have active P features, but 
			     			// the old one does -- they are different.
			     			// (do nothing, leave unique = true)
			     			else if (debug)
			     			{
			     				outputfile.write("only the old constraint has active P-side features.");
			     			}
				     	} 
				     	// else the residues were different, so 
				     	// the constraints are different on the P side (do nothing)
				     	else if (debug)
				     		outputfile.write("only one constraint has P-side residue.");
				     } // end of P_side_unique
				     
				     // if the P side is unique, then this particular constraint is different --
				     // leave unique=true, and loop to the next constraint
				     if (unique)
				     	break checkparticularconstraint;
				     	
				     // (if P is not unique, we need to check the Q side to see if it also matches)
				     // we'll start again by assume they are different:
				     unique = true;
				     
				     Q_side_unique:{
						// first, check the residues:
						if (mapping_constraints[con].Q_residue == Q1_residue)
				     	{
				     		// now check the features: as always, 4 possibilities
				     		if (Q_features_active)
				     		{
				     			if (mapping_constraints[con].Q_features_active)
				     			{
				     				// both are active, so see if the features match:
				     				if (featuresMatch(Q_fshare[0], mapping_constraints[con].Q_feat[0]) &&
				     					featuresMatch(Q_fshare[1], mapping_constraints[con].Q_feat[1]))
				     					{
				     						// EVERYTHING matches on the Q_side (residues, features)
				     						unique = false;
				     						if (debug)
				     							outputfile.write("the constraints are the same on the Q side");
				     						// 3/31/99 NOW THAT WE KEEP TRACK OF WHO IS A DOPPELGAENGER, 
				     						// we need a way to "vindicate" erstwhile doppelgaengers which are 
				     						// now instantiated by forms.  so if the old guy was a doppelgaenger,
				     						// we now have evidence that it is really a supported constraint.
				     						if (mapping_constraints[con].doppelgaenger)
				     							mapping_constraints[con].setDoppel(false);
				     					}
				     				// otherwise only Q has active features, they are different.
				     				// (do nothing, leave unique = true)
				     				else if (debug)
					     				outputfile.write("only the new constraint has active Q-side features.");
				     			}
				     		}
				     		else if (!mapping_constraints[con].Q_features_active)
				     		{
								// in this case, neither side has active Q_features, so the
								// constraints are the same on the Q side:
								unique = false;
								if (debug)
									outputfile.write("the constraints are the same on the Q side.");
	     						if (mapping_constraints[con].doppelgaenger)
	     							mapping_constraints[con].setDoppel(false);								
				     		}
			     			// otherwise, new constraint doesn't have active Q features, but 
			     			// the old one does -- they are different.
			     			// (do nothing, leave unique = true)
			     			else if (debug)
			     			{
			     				outputfile.write("only the old constraint has active Q-side features.");
			     			}
				     	} 
				     	// else the residues were different, so 
				     	// the constraints are different on the Q side (do nothing)
				     	else if (debug)
				     		outputfile.write("only one constraint has Q-side residue.");			     
				     }	// end of Q_side_unique
				     		     
				     // if unique is NOT true at this point, then it means that the P side matched
				     // and the Q side did too: the constraints are the same.
				     if (!unique)
				     	break uniquenesscheck;
				     // if unique IS still true from this side, then the P side matched but 
				     // the Q side didn't; the constraints are different, keep going in the loop.
				     
				} // end of match [mapping1]->[mapping2]
					if (debug)
						outputfile.write("the constraints have different segments.");
				
				} // end of checkparticularconstraint
				
		     } // end of loop through constraint
//		} // end of if(stillLearning)
//		else // if we're not still learning, then we have to do the same thing, but only over
			 // constraints which have been learned since real learning stopped.
			 // for now i will do this in a shamelessly inefficient way-- repeat the inside
			 // code, because the control for the loop will be different.
		if (!stillLearning)
		{
		     for (int i = firstWugtimeConstraint; i < newestconstraint; i++)
		     {
			    con = i;

			    if (verbose)
			    	outputfile.write( "\t\t" + con + "\t" );

				checkparticularconstraint:{
			    // a constraint is threatened by another constraint with the 
			    // same PAQ->PBQ  (mappings[x] encodes the entire PXQ)
			    if (match(mapping_constraints[con].mappings[0], new_const[0]) &&
			     match(mapping_constraints[con].mappings[1], new_const[1]) )
			     {
					 // the "core" of the constraint matches (i.e., the segmental change);
					 // so now check the stuff at the edges:
				     P_side_unique:{
						// first, check the residues:
						if (mapping_constraints[con].P_residue == P1_residue)
				     	{
				     		// now check the features: as always, 4 possibilities
				     		if (P_features_active)
				     		{
				     			if (mapping_constraints[con].P_features_active)
				     			{
				     				// both are active, so see if the features match:
				     				if (featuresMatch(P_fshare[0], mapping_constraints[con].P_feat[0]) &&
				     					featuresMatch(P_fshare[1], mapping_constraints[con].P_feat[1]))
				     					{
				     						// EVERYTHING matches on the P_side (residues, features)
				     						unique = false;
				     						if (debug)
				     							outputfile.write("the constraints are the same on the P side");
				     					}
				     				// otherwise only P has active features, they are different.
				     				// (do nothing, leave unique = true)
				     				else if (debug)
					     				outputfile.write("only the new constraint has active P-side features.");
				     			}
				     		}
				     		else if (!mapping_constraints[con].P_features_active)
				     		{
								// in this case, neither side has active P_features, so the
								// constraints are the same on the P side:
								unique = false;
								if (debug)
									outputfile.write("the constraints are the same on the P side.");
				     		}
			     			// otherwise, new constraint doesn't have active P features, but 
			     			// the old one does -- they are different.
			     			// (do nothing, leave unique = true)
			     			else if (debug)
			     			{
			     				outputfile.write("only the old constraint has active P-side features.");
			     			}
				     	} 
				     	// else the residues were different, so 
				     	// the constraints are different on the P side (do nothing)
				     	else if (debug)
				     		outputfile.write("only one constraint has P-side residue.");
				     } // end of P_side_unique
				     
				     // if the P side is unique, then this particular constraint is different --
				     // leave unique=true, and loop to the next constraint
				     if (unique)
				     	break checkparticularconstraint;
				     	
				     // (if P is not unique, we need to check the Q side to see if it also matches)
				     // we'll start again by assume they are different:
				     unique = true;
				     
				     Q_side_unique:{
						// first, check the residues:
						if (mapping_constraints[con].Q_residue == Q1_residue)
				     	{
				     		// now check the features: as always, 4 possibilities
				     		if (Q_features_active)
				     		{
				     			if (mapping_constraints[con].Q_features_active)
				     			{
				     				// both are active, so see if the features match:
				     				if (featuresMatch(Q_fshare[0], mapping_constraints[con].Q_feat[0]) &&
				     					featuresMatch(Q_fshare[1], mapping_constraints[con].Q_feat[1]))
				     					{
				     						// EVERYTHING matches on the Q_side (residues, features)
				     						unique = false;
				     						if (debug)
				     							outputfile.write("the constraints are the same on the Q side");
				     						// 3/31/99 NOW THAT WE KEEP TRACK OF WHO IS A DOPPELGAENGER, 
				     						// we need a way to "vindicate" erstwhile doppelgaengers which are 
				     						// now instantiated by forms.  so if the old guy was a doppelgaenger,
				     						// we now have evidence that it is really a supported constraint.
				     						if (mapping_constraints[con].doppelgaenger)
				     							mapping_constraints[con].setDoppel(false);
				     					}
				     				// otherwise only Q has active features, they are different.
				     				// (do nothing, leave unique = true)
				     				else if (debug)
					     				outputfile.write("only the new constraint has active Q-side features.");
				     			}
				     		}
				     		else if (!mapping_constraints[con].Q_features_active)
				     		{
								// in this case, neither side has active Q_features, so the
								// constraints are the same on the Q side:
								unique = false;
								if (debug)
									outputfile.write("the constraints are the same on the Q side.");
	     						if (mapping_constraints[con].doppelgaenger)
	     							mapping_constraints[con].setDoppel(false);								
				     		}
			     			// otherwise, new constraint doesn't have active Q features, but 
			     			// the old one does -- they are different.
			     			// (do nothing, leave unique = true)
			     			else if (debug)
			     			{
			     				outputfile.write("only the old constraint has active Q-side features.");
			     			}
				     	} 
				     	// else the residues were different, so 
				     	// the constraints are different on the Q side (do nothing)
				     	else if (debug)
				     		outputfile.write("only one constraint has Q-side residue.");			     
				     }	// end of Q_side_unique
				     		     
				     // if unique is NOT true at this point, then it means that the P side matched
				     // and the Q side did too: the constraints are the same.
				     if (!unique)
				     	break uniquenesscheck;
				     // if unique IS still true from this side, then the P side matched but 
				     // the Q side didn't; the constraints are different, keep going in the loop.
				     
				} // end of match [mapping1]->[mapping2]
					if (debug)
						outputfile.write("the constraints have different segments.");
				
				} // end of checkparticularconstraint
				
		     } // end of loop through constraint		     
		} // end of NOT stillLearning
	    } // end of uniquenesscheck; 
	    }
//	    else System.out.println("Warning: uniqueness check is pulling up zilch to compare with...");


    // if we got this far and unique is still true, then we can
    // create a new mapping constraint:
    
    if (verbose)
    {
     outputfile.write("\rResults of uniqueness check: the constraint ["+ 
     					new_constraint0 + "] -> [" + new_constraint1 + "] is ");
     if (!unique)
      outputfile.write("NOT ");
     outputfile.write("unique...\n");
    }
    
    if (unique)
    {
	     latest = addMappingConstraint( new_const[0], new_const[1],
	            P_sshare, P_fshare, Q_fshare, Q_sshare, 
	            mapping_constraints[c].A, 
	            mapping_constraints[c].B, 
	            P_sshare.length ,
	            P1_residue,
	            Q1_residue, P_features_active, Q_features_active);
	     assessConstraintViolations( latest );
          
	     // we only need to add the structural change of this guy when we are actually learning,
	     // because this enshrines the constraint in the pantheon and causes further generalization
	     if (stillLearning)
		 {    
		 	addStrucChange( mapping_constraints[c].A , mapping_constraints[c].B, (latest) );      
				 
		     if (verbose)
		     {
		     outputfile.write("Generalized constraint " + latest + " has a scope of: " + mapping_constraints[latest].scope + "\n\n"); 
		      outputfile.write("\t(It has been added as constraint #" + latest + "-- parents " + c + " and " + constraint + ")  \n");
		     }
       
       		if (doppelgaengers)
       		{
		     // 2/25/99 BRUCE'S IDEA: we should also add constraints with the same environment and A,
		     // but with other possible B's.  
		     otherChanges = (String[]) changes_index.get( ""+ String.valueOf( mapping_constraints[c].A ) 
		     				+ File.separatorChar + mapping_constraints[c].affixType);
		     temp = Integer.parseInt( otherChanges[0] );
		     for (int i = 1; i<temp; i++)
		     {
		     	// we might have another change at: otherChanges[i]
		     	if (!otherChanges[i].equals( String.valueOf(mapping_constraints[c].B)))
		     	{
		    		String anotherMapping1 = new String (String.valueOf(P_sshare) + String.valueOf(otherChanges[i]) + String.valueOf(Q_sshare));
		     	
		     		// this one is unique, so add a constraint with it.
				     latest = addMappingConstraint(new_const[0], anotherMapping1.toCharArray(),
				            P_sshare, P_fshare, Q_fshare, Q_sshare, 
				            mapping_constraints[c].A, 
				            otherChanges[i].toCharArray(), 
				            P_sshare.length ,
				            P1_residue,
				            Q1_residue, P_features_active, Q_features_active);
				     mapping_constraints[latest].setDoppel(true);
				     assessConstraintViolations( latest );
				     
				     addStrucChange( mapping_constraints[c].A , otherChanges[i].toCharArray(), (latest) ); 
		     	}
		     }
		    } // end of if(doppelgaengers)
	     } // end of if(stillLearning)
     } // end of if (unique)

    
//    outputfile.write("\n");


 //  } // end of A-HA!!!
   } // end of generalizeform block
 //  }
 // } 
 
  return;
  
 }

 
 // __________________________________________________________________________________
// matchleft returns the index to the right of the last matching character -- that is,
// it returns the index of the beginning of the mismatch.  (i.e., if nothing matches,
// it returns 0; if everything matches, it returns the entire "length")

 public int matchLeft (String string1,
        String string2,
        int length ) throws IOException
 {
  int sameUntil = length;
//  if (verbose)
//   outputfile.write("Finding maximal left string\n");
  // Find the length of the maximal left-side string in
  //   which the two strings are the same.
  for ( int pos=1; pos <= length; pos++)
  {
   if (!string1.regionMatches(0,string2,0,pos))
   {
    sameUntil = pos - 1;
    break;
   }
  }
  return sameUntil;
 }

 public int matchLeft (char string1[],
        char string2[],
        int length ) throws IOException
 {
  int sameUntil = length;
//  if (verbose)
//   outputfile.write("Finding maximal left string\n");
  // Find the length of the maximal left-side string in
  //   which the two strings are the same.
  for ( int pos=0; pos < length; pos++)
  {
   if (string1[pos] != string2[pos])
   {
    sameUntil = pos;
    break;  
   }
  }
  return sameUntil;
 }


 //_____________________________________________________________________________ 
 public int matchRight (String string1,
         String string2,
         int sameUntil ) throws IOException
 {
  //  Finds the length of the maximal right-side string in
  //  which the two strings are the same.
  int sameAtEnd = 0;
  int shorterFormLength = shorterString( string1, string2 );
  
  rightstring:
  {
   for (int pos = 1; pos <= (shorterFormLength-sameUntil) ; pos++)
   {
    if (!string2.regionMatches((string2.length()-pos), string1, 
           (string1.length()-pos), pos))
    {
     sameAtEnd = ( pos - 1 );
     break rightstring;     
    }
   } 
   // if we got this far, we have compared everything up to the left edge
   // of the smaller string and we haven't hit a mismatch! the whole thing matched.
   sameAtEnd = (shorterFormLength-sameUntil);
   
  } // end rightstring
  return sameAtEnd;
 }

 public int matchRight (char string1[],
         char string2[],
         int sameUntil ) throws IOException
 {
  //  Finds the length of the maximal right-side string in
  //  which the two strings are the same.
  int sameAtEnd = 0;
  int shorterFormLength = (string1.length <= string2.length ? string1.length : string2.length);
  
  rightstring:
  {
   for (int pos = 1; pos <= (shorterFormLength-sameUntil) ; pos++)
   {
    if ( string2[ string2.length - pos] != string1[ string1.length - pos])
    {
     sameAtEnd = ( pos - 1 );
     break rightstring;     
    }
   } 
   // if we got this far, we have compared everything up to the left edge
   // of the smaller string and we haven't hit a mismatch! the whole thing matched.
   sameAtEnd = (shorterFormLength-sameUntil);
   
  } // end rightstring
  return sameAtEnd;
 }

 // __________________________________________________________________________________
 /*
  shorterString takes two strings and tell you the length of the shorter one
 */
 public int shorterString( String s1, String s2 ) throws IOException
 {
  int minLength;
  minLength = ( s1.length()<=s2.length() ?
      s1.length() :
      s2.length() );
  return minLength;
 }

 // __________________________________________________________________________________
 public boolean match ( char chars1[], char chars2[] ) throws IOException
 {
  int length1, length2;
  
  length1 = chars1.length;
  
  if ( length1 != chars2.length )
   return false;
  else 
  {
   for (int i = 0; i < length1; i++)
   {
    if (chars1[i] != chars2[i])
     return false;
   }
   return true; 
  }
 }
 // __________________________________________________________________________________
 /*
  printConstraint just returns a string summary of a constraint
 */
 public String printConstraint( int c ) throws IOException
 {
 	StringBuffer summary = new StringBuffer();
    summary.append(c+":\t[" + String.valueOf(mapping_constraints[c].A) +
          "] -> [" + String.valueOf(mapping_constraints[c].B) + "] / [");
    if (mapping_constraints[c].P_residue)
     summary.append("X");
    if (mapping_constraints[c].P_features_active) 
	 summary.append("{" + compatibleSegments(c,true) + "}");    
    summary.append(String.valueOf(mapping_constraints[c].P) + "__"+ String.valueOf(mapping_constraints[c].Q));
    if (mapping_constraints[c].Q_features_active) 
	 summary.append("{" + compatibleSegments(c,false) + "}");
    if (mapping_constraints[c].Q_residue)
     summary.append("Y");
    summary.append("]");
    return summary.toString();
 }

// __________________________________________________________________________________
 /*
  printConstraintList just prints out a list of all the constraints known so far
 */
 
 public void printConstraintList() throws IOException
 {
 
 	ConstraintBatch current_batch;
 	Enumeration all_changes = changes.keys();
 	int top, c;
 	int counter = 1;
 	
  if (verbose) {
   outputfile.write("\n\n");
   outputfile.write("        SUMMARY OF MAPPING CONSTRAINTS\n");
  outputfile.write("There are a total of " + newestconstraint + " mapping constraints.\n");
  }  
  try{
   constraintsfile.writeInt( numberOfFeatures );
  }
  catch (IOException e)
  {
   System.err.println("Couldn't write constraints file\n" + e.toString() );
   System.exit(1);  
  }
  
	// 3/27 new: instead of outputting constraints in chronological order,
	// we will save them grouped by change, and in order of decreasing c75:
  	while (all_changes.hasMoreElements() )
  	{
  		current_batch = (ConstraintBatch) changes.get( all_changes.nextElement() );
  		
  		top = current_batch.size();
	  		for (int i = 1; i < top; i++)
	  		{
	  			c = current_batch.constraints[i];
	  			// print this constraint
				if (mapping_constraints[c].keep)
				{
					if (verbose)
					{
						outputfile.write("Constraint "+c+":\t[" + String.valueOf(mapping_constraints[c].A) +
						      "] -> [" + String.valueOf(mapping_constraints[c].B) + "] / [");
						if (mapping_constraints[c].P_residue)
						 outputfile.write("X");
						outputfile.write(String.valueOf(mapping_constraints[c].P) + "__"+ String.valueOf(mapping_constraints[c].Q));
						if (mapping_constraints[c].Q_residue)
						 outputfile.write("Y");
						outputfile.write("].");
						outputfile.write("\t( Scope =\t"+mapping_constraints[c].scope+
						    "\tReliability =\t"+
						    (Math.floor(mapping_constraints[c].reliability * 1000 + .5) / 1000) 
						    +"\t)\n");

						outputfile.write("\t(affix type = " + mapping_constraints[c].affixType
						     + ", change location = " + mapping_constraints[c].change_location + ")\n");
					}  
					try{    
					mapping_constraints[c].write( constraintsfile, numberOfFeatures );}
					catch (IOException e)
					{
					System.err.println("Couldn't write constraints file\n" + e.toString() );
					System.exit(1);  
					}
			   
			  } // end of if keep

	  		}
  	} // end of loop thru constraints
  
 	outputfile.flush();
	constraintsfile.flush();
 } // end of printConstraintList method
 
 // __________________________________________________________________________________ 
 public void printPhonologicalRules() throws IOException
 {
  for (int r=0; r < phonological_rules.length; r++) 
  {
    phonological_rules[r].write(rulesfile);
  } 
 } // end of printPhonologicalRules method 
 
 // __________________________________________________________________________________
 /*
  printViolationsGrid outputs the tableau for all forms known so far
 */
 
 public void printViolationsGrid() throws IOException
 {
  int longest;
  
  // since there are more constraints than forms, we'll do forms on top 
  // and constraints down the side.
  // in addition, in order to save space, we'll just list forms by number.
  outputfile.write("\n\n");
  outputfile.write("_______________________________________________\n");
  outputfile.write("             CURRENT VIOLATIONS GRID\n");
  outputfile.write("_______________________________________________\n");
  outputfile.write("\rKnown forms:\n");
  
  // So first, print out the list of known forms
  for (int f=0; f<newestform; f++)
  {
   if (f >= 9)
    outputfile.write("\t  ");
   else outputfile.write("\t   ");
   outputfile.write( (f+1) + ". "+known_forms[f][0] + " -> "
          +known_forms[f][1]+"\n");
  }
  outputfile.write("\n");
  
  // Now for the table proper.  
  
  /* first, figure out what the longest form we have is.  (this is purely
     for aesthetic reasons, and i feel slightly guilty that such a computationally
     inefficient thing should be done for such a frivolous purpose--  but we
     can rationalize it by pointing out that in the end, this procedure will only
     be done once at the very end of everything.   For debugging, readability will
     be worth the modest speed hit!)
  */
  longest = 0;
  for (int f=0; f<(newestconstraint-1); f++)
  {
   if (mapping_constraints[f].mappings[0].length > longest )
    longest = mapping_constraints[f].mappings[0].length;
    
   if (mapping_constraints[f].mappings[1].length > longest )
    longest = mapping_constraints[f].mappings[1].length;
  }
  
  // OK.  now we can output the grid.  
  // Start with top headings, which will be the indices of known forms.
  for (int i=0;i<(longest + 11); i++)
   outputfile.write(" ");
   
  outputfile.write("|");
  
  for (int i=0;i<newestform;i++)
  {
   outputfile.write(" ");
   if (i<9)
    outputfile.write(" ");
   outputfile.write( (i+1) + " ");
  }
  outputfile.write("\n");
  
  for (int i=0; i<(longest + 11); i++)
   outputfile.write("_");
  outputfile.write("|");
  for (int i=0; i<(newestform * 4); i++)
   outputfile.write("_");
  outputfile.write("\n");  
  
  
  // So now we can proceed to make rows.  Rows will actually list the 
  // mapping constraints on the left so we can read them!
  // However, since this could make the leftmost column kind of longish,
  // we'll split them across 2 lines.
  for (int m=0; m<(newestconstraint-1); m++)
  {
   // this lines up the numbers
   if (m<9)
    outputfile.write(" ");
   // this lists the left half of the constraint:
   outputfile.write( (m+1) + ". [" + mapping_constraints[m].mappings[0]
         + "] -> ");
   // this is the aesthetic bit to make sure everything lines up:
   for (int x=0; x<=(longest - mapping_constraints[m].mappings[0].length );x++)
    outputfile.write(" ");
   
   // make a column break and start a new line.
   outputfile.write("|\n");
   
   // this lists the right side of the constraint:
   outputfile.write("    ["+mapping_constraints[m].mappings[1] + "]    ");
   
   // the aesthetic bit again:
   for (int x=0; x<=(longest - mapping_constraints[m].mappings[1].length );x++)
    outputfile.write(" ");
    
   // the column break again:
   outputfile.write("|");
   
   // finally, we can print out the actual violations.
   for (int f=0; f<newestform; f++)
   {
//    outputfile.write("  "+ Violations[f][m] + " ");
   }
   outputfile.write("\n");
  }  // end of cycling through mapping constraints
  
  outputfile.write("\n\n");
  outputfile.write("_______________________________________________\n\n");
 
 } // end of printVIolationsGrid method

 // __________________________________________________________________________________
 /*
  applyPhonology() simply takes a string as an argument, runs it through all
  of the currently known phonological mappings, and returns the result.
 */
 
 public char[] applyPhonology( char underlying[] ) throws IOException
 {
  String UR = new String( underlying );
  String SR = new String();
//  char SR = new char[];
  StringBuffer intermediateSR = new StringBuffer();
  boolean keepApplyingPhonology;
  int changeloc = 0, no_of_rules = 0;
  String desc = new String();
  StringBuffer clearBuffer = new StringBuffer();
  
  if (!phonologyOnFlag)
   return underlying;
  
  else // time to apply some phonology
  {
   keepApplyingPhonology = true; 
   SR = String.valueOf(underlying);
     
   do 
   {
    keepApplyingPhonology = false;
    no_of_rules = phonological_rules.length;
    
    for ( int i = 0; i < no_of_rules ; i++ )
    { // test if struc desc for this rule is contained in the UR
     
     desc = String.valueOf( phonological_rules[i].structuralDescription );
     changeloc = SR.indexOf( desc );

     if ( changeloc >= 0 )
     {
      /* this hideous thing builds the new SR -- the first bit is the 
      old SR up to the struc desc of the rule, the next bit is the structural
      change pulled from the rule, and the last bit is what's left of
      the UR */
      intermediateSR.append( SR.substring(0,changeloc )).append( phonological_rules[i].structuralChange ).append( SR.substring( changeloc + phonological_rules[i].structuralDescription.length, SR.length()));
     
      SR = String.valueOf(intermediateSR);
      intermediateSR = clearBuffer;
            
      keepApplyingPhonology = true;
     } // end of looking for rule's structural desc in the SR
    } // end of loop through the rules
   } while ( keepApplyingPhonology == true );
  }  
  return SR.toCharArray();
 } // end of applyPhonology method


 // __________________________________________________________________________________
 /*
  discoverPhonology() takes 2 strings (a predicted form and the actual form),
  and sees if it can't find some phonology that changes one into the other.

  The idea here is to try to make mapping constraints more useful
  by discovering phonology which obscures their applicability to
  forms.
  We can see if phonology will help a given form and mapping by
  comparing the actual output with the predicted output.
  If there is a simple phonological process involved, then the
  predicted and actual outputs should start out the same, and end
  with the same ending, but have a discrepancy in the middle 
  which prevents us from recognizing that the known constraint
  accounts for this form.
  So we isolate the discrepancy, by breaking down the forms:
  
  Predicted:    LeftMatch + residuePredicted + RightMatch
  Actual:       LeftMatch + residueActual  + RightMatch
  
  Then, examine the discrepancy, and see if we can formulate a 
  rule from one to the other.

 */
  public void discoverPhonology( char predictedOutput[], char realOutput[] ) throws IOException
  {

   int minLength;

   int sameUntil = 0;
   int sameAtEnd = 0;
   String residuePredicted;
   char residuePredictedChars[];
   String residueActual;
   char residueActualChars[];
   boolean plausibleTarget;

   /* The first step is to localize the error.  
   The approach is to find the maximal left-side match, then find
   the maximal right-side match of what's left.  This should leave us
   with a residue of stuff that doesn't match in the middle.
   The residue from the predicted form may not be the same size as
   the residue of the actual form;  this could be caused by
   a phonological process (epenthesis, deletion, coalescence,
   diphthongization, etc.) or else we might be trying to use the 
   wrong suffix.
   The goal is to compare the residue in the predicted form to the
   residue in the actual form, and explain it with a phonological
   alternation which affects a target string 2 characters long.
   If this fails, then we have to assume that this is the wrong
   mapping constraint for this form.
   */

   // First, set minLength to the length of whichever is 
   // shorter-- the real or predicted output
   minLength = ( predictedOutput.length <=realOutput.length  ?
        predictedOutput.length :
        realOutput.length );

   if (debug)
    outputfile.write(" (minLength = " + minLength + ")\n");
   
   // now find out how much matches on the left     
   sameUntil = matchLeft( predictedOutput, realOutput, minLength );


   // if we get through the whole form, it means that 
   // the forms are identical, in which case we wouldn't have
   // gotten to this point.  march straight back home.
   if (sameUntil >= minLength)
    return;

   sameAtEnd = matchRight( predictedOutput, realOutput, sameUntil);

   if (debug)
    outputfile.write("   sameUntil = " + sameUntil + ", sameAtEnd = " + sameAtEnd
         + ", predictedOutput.length = " + predictedOutput.length + "\n");   


   // let whatever is left be the target segment of the rule:

   if (predictedOutput.length != 0)
    residuePredicted = new String(predictedOutput, sameUntil, ((predictedOutput.length - sameAtEnd) - sameUntil));

   else residuePredicted = new String();
   
   if (realOutput.length != 0)
    residueActual = new String( realOutput, sameUntil,
           ((realOutput.length - sameAtEnd)-sameUntil));
   else residueActual = new String();
   
   residuePredictedChars = new char[ residuePredicted.length() ];        
   residuePredicted.getChars(0, residuePredicted.length(), residuePredictedChars, 0);
   
   residueActualChars = new char[ residueActual.length() ];
   residueActual.getChars(0, residueActual.length(), residueActualChars, 0);
           
   

   if (debug) {
    outputfile.write("... the crux of the matter is turning [" + 
        residuePredicted + "] into [" + residueActual + "].\n");
   }     


   // now that we know the residues, we can hypothesize phonology.
   // The kinds of phonology we can hypothesize will depend on 
   // the respective lengths of the residues; the hypothesizePhonology
   // method checks out the residuals and returns true if
   // they were at least of valid lengths.  (even if no relevant
   // rule was discovered)
   
   plausibleTarget = hypothesizePhonology( predictedOutput, residuePredictedChars,
                realOutput, residueActualChars,
                sameUntil, sameAtEnd);
                   
  }

 // __________________________________________________________________________________
 /*
  hypothesizePhonology() is the method which actually does the dirty work
  of looking at two strings and seeing if we have any hope of transforming 
  the one into the other.  It takes a whole mess of arguments;  I can't
  remember exactly why we really need to pass around all this information,
  all I know is that it made sense at the time (9/97) and it seemed to work.
  (possibly legacy structure?)
 */
 public boolean hypothesizePhonology( char predOut[],
           char resPred[],
           char realOut[], 
           char resActual[],
           int sameUntil,
           int sameAtEnd ) throws IOException
 {
  String predictedOutput = new String( predOut );
  String residuePredicted = new String( resPred );
  String realOutput = new String( realOut );
  String residueActual = new String( resActual );
  

  int newest = (newestconstraint - 1);
  char environment, environment2; 
  String strucDesc = new String();
  StringBuffer strucChange = new StringBuffer();  
  boolean goodStrucDesc, compatibleRule, plausibleTarget = true; 
  
  /*
  This method hypothesizes phonological rules, based on the predicted
  and actual residues.
  
  The residues may not match in length, in which case we might 
  have deletion, coalescence, or epenthesis.
  There are six possible cases:
  
  1. we have an actual segment where we predicted nothing.
   (residuePredicted = 0 chars, residueActual = 1)
   Posit an epenthesis rule, conditioned bilaterally
   (i.e. both neighbors form the environment)
  
  2. we have predicted a segment where none exists 
   (residuePredicted = 1 char, residueActual = none)
   Posit a deletion rule -- conditioned by either one
   side or the other.   (Or both????????  Target string would
   be 3 segs, but this seems like a plausible case)
   
  3. we have a predicted segment and an actual one, but 
   the don't match. (residuePredicted = 1, residueActual=1).
   This is an ordinary mutation rule; consider conditioning
   environments from the left and from the right.
   
  4. we have one segment predicted which corresponds to two
   segments in the actual output. (residuePredicted = 1,
   residueActual = 2)  This is something like diphthong-
   ization.  We'll assume for now that we are only looking
   for context-dependent cases (since "context-free" ones 
   would not really be context-free, but rather conditioned
   by prosody and other stuff not represented here yet).
   So check for environments on either left or right.
   
  5. we have predicted two segments, but there is only one
   in the actual output.  (residuePredicted = 2 segs, 
   residueActual = 1 seg) This is shortening coalescence.
   Here we consider the entire residuePredicted as the
   target string.
   
  6. we have predicted two segments and there are two segments,
   but they do not match.  (residuePredicted = 2 segs, 
   residueActual = 2 segs)  This may be length-preserving
   coalescence (XZ -> YY) or metathesis (XZ -> ZX).
   As with #5, the entire residuePredicted must be considered
   as the target string.
  
  
  In all cases, once we've hypothesized a bit of phonology, we must
  submit it to the "structural description evaluater", which checks
  through all forms to see if it's really a plausible rule.  if it
  is, then it is added to the list of known phonological rules.
  */

  if (debug)
  {
   outputfile.write("We are trying to convert " + residuePredicted.length() +
      " segments into " + residueActual.length() + " segments; so we have ");
  }
  
  // CASE 1: bilateral epenthesis
  if (residuePredicted.length() == 0 && residueActual.length() == 1)
  {
   if (debug)
    outputfile.write("bilateral epenthesis.\n");
    
   // now we need to formulate a structural description using 
   // both sides from the predicted output
   if (sameUntil > 0)
   {
    strucDesc = String.valueOf(predictedOutput.charAt(sameUntil-1))
     + String.valueOf(predictedOutput.charAt(predictedOutput.length() - sameAtEnd));
   }
   else 
   {
    strucDesc = String.valueOf(String.valueOf(predictedOutput.charAt(predictedOutput.length() - sameAtEnd)));   
   }

   // check validity of this structural description
   goodStrucDesc = evaluateStrucDesc( strucDesc );
   
   
   // if so, then the strucChange is an epenthesis:
   if (goodStrucDesc)
   {
    if (sameUntil > 0)
    {
	strucChange = new StringBuffer(String.valueOf(predictedOutput.charAt(sameUntil-1)));
	strucChange.append(residueActual).append(String.valueOf(predictedOutput.charAt(predictedOutput.length() - sameAtEnd)));
    } 
    else {
	strucChange = new StringBuffer(String.valueOf( residueActual));
	strucChange.append(String.valueOf(predictedOutput.charAt(predictedOutput.length() - sameAtEnd)));
    }
    compatibleRule = newRule(mapping_constraints[newest].mappings[0],
        mapping_constraints[newest].mappings[1],
        residuePredicted, predictedOutput, 
        realOutput, strucDesc, strucChange.toString(), newest);
   }    
  }  // end of CASE 1


  // CASES 2, 3 and 4 have residuePredicted length of 1
  else if (residuePredicted.length() == 1 && residueActual.length() <= 2)
  {
   if (debug)
    outputfile.write("ordinary mutation, conditioned deletion, etc.\n");
    
   /* in all 3 of these cases, the conditioning environment
    could be from the left or from the right.  For now, strict
    locality is imposed, with only one-segment environments
    allowed. */
   
   // For environments that precede the target:
   if (sameUntil > 1)
   {
    // the environment is the character before the target
    environment = predictedOutput.charAt(sameUntil-1);
    
    // the desc is the environment plus target string
    strucDesc = String.valueOf(environment) + residuePredicted;
    
    // now check if it's really a valid struc. desc.
    goodStrucDesc = evaluateStrucDesc( strucDesc );
    
    // if so, compute the change and add the rule
    if (goodStrucDesc)
    {
     strucChange = new StringBuffer(String.valueOf(environment)).append(residueActual);
     compatibleRule = newRule(mapping_constraints[newest].mappings[0],
         mapping_constraints[newest].mappings[1],
         residuePredicted, predictedOutput, 
         realOutput, strucDesc, strucChange.toString(), newest);
    }
   } // end of finding left-side rules
   
   // For environments that follow the target
   if (sameAtEnd > 0)
   {
    // the environment is the character after the target
    environment = predictedOutput.charAt(predictedOutput.length() - sameAtEnd);

    // the desc is the target string + the environment
    strucDesc = residuePredicted + String.valueOf(environment);
    
    // now check if it's really a valid struc. desc.
    goodStrucDesc = evaluateStrucDesc( strucDesc );
    
    // if so, compute the change and add the rule
    if (goodStrucDesc)
    {
     strucChange = new StringBuffer(residueActual).append(String.valueOf(environment));
     compatibleRule = newRule(mapping_constraints[newest].mappings[0],
       mapping_constraints[newest].mappings[1],
       residuePredicted, predictedOutput,
       realOutput, strucDesc, strucChange.toString(), newest);
    }
   } // end of finding right-side rules
   
   // If the residueActual is of length 0, then we can also
   // consider contextual deletion conditioned from BOTH sides
   // The resulting strucDesc will have THREE segments
//   if (residueActual.length() == 0)
   if (residueActual.length() == 0 && sameUntil >= 1) // new 5/14/99 trying to eradicate a stringoutofbounds exception
   {
    strucDesc = String.valueOf(predictedOutput.charAt(sameUntil-1))
      + String.valueOf(predictedOutput.charAt(sameUntil))
      + String.valueOf(predictedOutput.charAt(predictedOutput.length() - sameAtEnd));
    goodStrucDesc = evaluateStrucDesc( strucDesc );
    if (goodStrucDesc)
    {
     strucChange = new StringBuffer(String.valueOf(predictedOutput.charAt(sameUntil-1))).append(String.valueOf(predictedOutput.charAt(predictedOutput.length() - sameAtEnd)));
     compatibleRule = newRule(mapping_constraints[newest].mappings[0],
       mapping_constraints[newest].mappings[1],
       residuePredicted, predictedOutput,
       realOutput, strucDesc, strucChange.toString(), newest);
    }  
   } 
  } // end if for CASES 2,3,4

  // CASES 5 and 6 have residuePredicted length of 2
  else if (residuePredicted.length() == 2 && residueActual.length() > 0
    && residueActual.length() <=2 )
  {
   if (debug)
    outputfile.write("coalescence or methathesis.\n");

   // for both of these cases, the structural description is just
   // the residuePredicted, since it is already 2 segments.
   
   strucDesc = residuePredicted;
   goodStrucDesc = evaluateStrucDesc( strucDesc );
   if (goodStrucDesc)
   {
    strucChange = new StringBuffer(residueActual);
    compatibleRule = newRule(mapping_constraints[newest].mappings[0],
      mapping_constraints[newest].mappings[1],
      residuePredicted, predictedOutput,
      realOutput, strucDesc, strucChange.toString(), newest);
   }
  } // end if for CASES 5,6
  
  else 
  {
   // the lengths don't indicate a plausible phonological rule
   if (debug)
    outputfile.write("no plausible phonology...\n");
   plausibleTarget = false;
  }
  return plausibleTarget;
      
 } // end hypothesizePhonology method
 
 //_____________________________________________________________________________
 public boolean evaluateStrucDesc( String strucDesc ) throws IOException
 {
  // We are given a Structural Description Candidate, which at present
  //   must always be two segments in length.
  // We want to know if the structural description is unpronounceable
  //   in this language. 
  // THIS IS PROBABLY NOT THE BEST WAY OF DOING THIS -- in previous
  //   avatars of the program, this used to look through all the known
  //   forms to decide if something was "pronounceable" or not.
  // Since now we only gradually learn new forms, this is not a very
  //   promising strategy; instead, we rely on the wealth of phonotactic
  //   constraints which we inherit from the time when the world is
  //   a seething mass of gavagais.  

  boolean validSD = false;
  
  if (debug)
   outputfile.write("Checking for phonology which targets the string: [" + strucDesc + "].\n");
   
  validate:{
  for (int seq = 0; seq < learnerTask.illicit.length; seq++)
  {
   // If the proposed strucDesc contains one of the known
   //   bad sequences, then thumbs up-- it is a good target for 
   //   phonological rules.
   if (strucDesc.indexOf(learnerTask.illicit[seq]) >= 0)
   {
    validSD = true;
    break validate;
   }
  }
  } // end of validate block

  if (debug)
  {
   outputfile.write("... the structural desc is ");
   if (!validSD)
    outputfile.write("NOT ");
   outputfile.write("valid.\n");
  }

  return validSD;
 } // end of evaluateStrucDesc method

 //_____________________________________________________________________________
 
 // newRule prints the new rule and then checks if the rule contradicts any
 // previously discovered rules; it returns false if the rule conflicts,
 // true if the rule is compatible (whether it is novel or not)  
 
 public boolean newRule( char peelOff[], char addOn[], // from offending constraint
       String target,       // the target string
       String predicted,       // wrongly predicted output
       String real,        // the surprising real output
       String strucDescString,       // structural description
       String strucChangeString,      // structural change
       int constraint )      // index of relevant constraint
       throws IOException
 {
  
  char strucDesc[] = strucDescString.toCharArray();
  char strucChange[] = strucChangeString.toCharArray();
  
//  *** i think the following line is extremely obsolete:  
//  ConstraintAssessment new_assessment = new ConstraintAssessment();


  // Proclaim the new rule joyously and store it
//  outputfile.write("Error-generating mapping constraint is [" + peelOff + "] ~ [" + addOn + "]\n");
//  outputfile.write("Target string is /"+target+"/, based on /"+predicted+"/ --> ["+real+"]\n");

  outputfile.write("\n  New phonological rule: ["+String.valueOf(strucDesc)+"] --> ["+String.valueOf(strucChange)+"].\n\n");

  // now determine if the new rule is novel.
  // if there are no previous rules, then by default it's novel
  if (phonological_rules.length == 0 )
   addNewRule( strucDesc, strucChange );

  // otherwise, we're going to have to do some checking
  else
  {
   for (int r=0; r<phonological_rules.length; r++)
   {
    if (match(phonological_rules[r].structuralDescription, strucDesc))
    {
     // structural description is not novel.  that's OK, as long as
     // the structural change is also the same
     if (match(phonological_rules[r].structuralChange, strucChange))
     {  // It's exactly the same rule; don't worry, just return
      outputfile.write("  Rule is not novel.\n");
      return true;
     }
     else
     {  // It's a *contradictory* rule, with same structural desc,
        //   but with a different structural change.
        outputfile.write("  Warning: conflicting phonological rules\n" );
        return false;
     }
    } 
   } // end of cycling through known rules 
   
   // if we've gotten this far, the rule is novel
   addNewRule(strucDesc, strucChange );
   
  }
  return true;
 }  // end of the newRule() method

 //_____________________________________________________________________________ 
 public void addNewRule (char strucDesc[], char strucChange[]) throws IOException
 {
  PhonologicalRule newSetOfRules[];
 
  newSetOfRules = new PhonologicalRule[ phonological_rules.length + 1 ];
  System.arraycopy(phonological_rules,0,newSetOfRules,0,phonological_rules.length);

  newSetOfRules[ phonological_rules.length ] = new PhonologicalRule( strucDesc, strucChange );
  outputfile.write("  Rule is novel, and has been added to set of known rules.\n");
  
  // now we'll replace the old rules with the newly expanded ones:
  phonological_rules = newSetOfRules;
   
 }
 
 // __________________________________________________________________________________
 // __________________________________________________________________________________
 /*
  sortConstraints() is a little experiment: I think that sorting the entire list of
  constraints may be extremely time-consuming, but if we do it once, we may save a lot
  of time wug testing.  
 */
public void sortChanges()
{
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes.get( current_change );
		
//		System.out.println("Now sorting the change " + current_change + "\rPasses:");
//		System.out.flush();
		
		// now we need to sort this batch

		top = sortbatch.size() - 1; 
		for (int pass = 1; pass < top; pass++)
		{

			for (int i=1; i < top; i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch[i] + " and " + sortbatch[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].overallconfidence < mapping_constraints[ sortbatch.constraints[i+1] ].overallconfidence )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;			
				}
			} // end of one pass

// the next part is correct, however -- why didn't it work?

			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;
		}
//		System.out.print("\n");

/*		System.out.print("Results of sort for "+current_change+": ");
try{	for (int x=1;x< sortbatch[0].intValue();x++)
		{
			System.out.print(sortbatch[x].toString() + " ");
		}
	} catch (NullPointerException e) {}
		System.out.print("\n");
*/
		// and now put the sorted row back in the hashtable:
		changes.put( current_change, sortbatch );
	}
	
}
 // __________________________________________________________________________________
 /*
  sortChangesByScope() is a follow-up to sortChanges() -- it clones the changes Hashtable
  and then resorts it based on scope instead of on confidence.
  NOTE: this will only run efficiently if sortChanges() has been run first!
 */
  public void sortChangesByScope() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_scope hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;
//			System.out.println(".");


			for (int i=1; i < (top-1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch[i] + " and " + sortbatch[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].scope < mapping_constraints[ sortbatch.constraints[i+1] ].scope )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;
					changed = true;		
				}
			} // end of one pass


			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_scope.put( current_change, sortbatch );
	} // end of while more changes
	
}
 // __________________________________________________________________________________
 /*
  sortChangesByHits() is a follow-up to sortChangesByScope() -- it clones the changes Hashtable
  and then resorts it based on hits instead of on overall confidence
  NOTE: this will only run efficiently if sortChanges() has been run first!
 */
  public void sortChangesByHits() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes_by_scope.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_hits hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;
//			System.out.println(".");

			for (int i=1; i < (top - 1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch[i] + " and " + sortbatch[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].hits < mapping_constraints[ sortbatch.constraints[i+1] ].hits )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;	
					changed = true;		
				}
			} // end of one pass

			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_hits.put( current_change, sortbatch );
	} // end of while more changes
	
}
 // __________________________________________________________________________________
 /*
  sortChangesByC75() is a follow-up to sortChanges() -- it clones the changes Hashtable
  and then resorts it based on c75 instead of on overall confidence
  NOTE: this will only run efficiently if sortChanges() has been run first!
 */
  public void sortChangesByC75() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_scope hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;

			for (int i=1; i < (top-1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch[i] + " and " + sortbatch[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].lowerConfidence.p75 < mapping_constraints[ sortbatch.constraints[i+1] ].lowerConfidence.p75 )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;	
					changed = true;		
				}
			} // end of one pass

			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_c75.put( current_change, sortbatch );
	} // end of while more changes
	
}
 // __________________________________________________________________________________
 /*
  sortChangesByC90() is a follow-up to sortChanges() -- it clones the changes_by_c75 Hashtable
  and then re-sorts it based on c90.
  NOTE: this will only work if sortChangesByC75() has been run first!
 */
  public void sortChangesByC90() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes_by_c75.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_scope hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;

			for (int i=1; i < (top-1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch[i] + " and " + sortbatch[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].lowerConfidence.p90 < mapping_constraints[ sortbatch.constraints[i+1] ].lowerConfidence.p90 )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;	
					changed = true;		
				}
			} // end of one pass

			// now the constraint with the smallest c90 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_c90.put( current_change, sortbatch );
	} // end of while more changes
	
}
 // __________________________________________________________________________________
 /*
  sortChangesByRawRel() is a follow-up to sortChanges() -- it clones the changes_by_c75 Hashtable
  and then resorts it based on scope instead of on confidence.
  NOTE: this will only work if sortChangesByC75() has been run first!
 */
  public void sortChangesByRawRel() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes_by_c75.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_scope hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;

			for (int i=1; i < (top-1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch.constraints[i] + " and " + sortbatch.constraints[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].reliability < mapping_constraints[ sortbatch.constraints[i+1] ].reliability )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;	
					changed = true;		
				}
			} // end of one pass

			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_raw_rel.put( current_change, sortbatch );
	} // end of while more changes
	
}
 // __________________________________________________________________________________
 /*
  sortChangesByWeighted() is a follow-up to sortChanges() -- it clones the changes_by_c75 Hashtable
  and then resorts it based on "weighted confidence" instead of on confidence.
  NOTE: this will only work if sortChangesByC75() has been run first!
 */
  public void sortChangesByWeighted() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes_by_c75.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_scope hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;

			for (int i=1; i < (top-1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch.constraints[i] + " and " + sortbatch.constraints[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].weightedConfidence < mapping_constraints[ sortbatch.constraints[i+1] ].weightedConfidence )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;	
					changed = true;		
				}
			} // end of one pass


			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_weighted.put( current_change, sortbatch );
	} // end of while more changes
	
}
 // __________________________________________________________________________________
 /*
  sortChangesByFreq() is a follow-up to sortChanges() -- it clones the changes_by_c75 Hashtable
  and then resorts it based on token frequency instead of on confidence.
  NOTE: this will only work if sortChangesByC75() has been run first!
 */
  public void sortChangesByFreq() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes_by_c75.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_scope hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;
//			System.out.println(".");

			for (int i=1; i < (top-1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch.constraints[i] + " and " + sortbatch.constraints[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].hits_frequency < mapping_constraints[ sortbatch.constraints[i+1] ].hits_frequency )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;	
					changed = true;		
				}
			} // end of one pass

			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_freq.put( current_change, sortbatch );
	} // end of while more changes
	
}
 // __________________________________________________________________________________
 /*
  sortChangesByRelFreq() is a follow-up to sortChanges() -- it clones the changes_by_c75 Hashtable
  and then resorts it based on RELATIVE token frequency instead of on confidence.
  NOTE: this will only work if sortChangesByFreq() has been run first!
 */
  public void sortChangesByRelFreq() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes_by_freq.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_scope hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;
//			System.out.println(".");

			for (int i=1; i < (top-1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch.constraints[i] + " and " + sortbatch.constraints[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].rel_frequency < mapping_constraints[ sortbatch.constraints[i+1] ].rel_frequency )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;	
					changed = true;		
				}
			} // end of one pass

			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_rel_freq.put( current_change, sortbatch );
	} // end of while more changes
	
}


 // __________________________________________________________________________________
 /*
  sortChangesByWeightedByFreq() is a follow-up to sortChanges() -- it clones the changes_by_c75 Hashtable
  and then resorts it based on confidence weighted by token frequency instead of on confidence.
  NOTE: this will only work if sortChangesByC75() has been run first!
 */
  public void sortChangesByWeightedByFreq() 
  {
	ConstraintBatch sortbatch;
	Enumeration all_changes = changes.keys();
	String current_change;
	int top = 0;
	int hold = 0;
	boolean changed;

	// first, clone the changes hashtable
	
	while (all_changes.hasMoreElements() )
	{
		current_change =  (String) all_changes.nextElement();
		sortbatch = ( ConstraintBatch ) changes_by_c75.get( current_change );
		// now, because of the way java tries to save time & memory with 
		// hash tables, the sortbatch variable is actually still a reference to
		// the row in the changes hashtable.  therefore, before we sort it, we 
		// want to break this link, because the row will really go into the 
		// changes_by_scope hashtable
		sortbatch = ( ConstraintBatch ) sortbatch.clone();
		
		// now we need to sort this batch
		top = sortbatch.size() - 1;
		sort:{
		for (int pass = 1; pass < top; pass++)
		{
			changed = false;
//			System.out.println(".");

			for (int i=1; i < (top-1); i++) // one pass
			{
//				System.out.println("Comparing constraints " + sortbatch.constraints[i] + " and " + sortbatch.constraints[i+1] );			
				if ( mapping_constraints[ sortbatch.constraints[i] ].weightedByFreq < mapping_constraints[ sortbatch.constraints[i+1] ].weightedByFreq )
				{
					// swap
					hold = sortbatch.constraints[i];
					sortbatch.constraints[i] = sortbatch.constraints[i+1];
					sortbatch.constraints[i+1] = hold;	
					changed = true;		
				}
			} // end of one pass

			// now the constraint with the smallest c75 should be at the end,
			// and we can ignore it from now on:
//			top--;

			// if we got through this whole pass and didn't make any changes, we're done
			if (!changed)
				break sort;
		}
		} // end of sort block
		changes_by_weighted_by_freq.put( current_change, sortbatch );
	} // end of while more changes
	
}

 // __________________________________________________________________________________ 
 /*
 	compatibleSegments is purely for human readibility: it prints out a list of
 	all the segments in the inventory which are compatible with a set of features.
 	Its first argument is the constraint whose features we should examine,
 	and the second argument is boolean for which side (0=P, 1=Q)
 */
 
 public String compatibleSegments( int c, boolean left_side) throws IOException
 {
 	boolean applies = true;
 	boolean first = true;
 	StringBuffer compatiblesegments = new StringBuffer();
 	
 	for (int i = 0; i < learnerTask.featureMatrix.length; i++)
 	{	
 		applies = true;
 		try
 		{
 			checkfeatures:
 			{
 				for (int j = 0; j < numberOfFeatures; j++)
 				{
					if (left_side)
					{
						// we're checking the P side
						if (learnerTask.featureMatrix[i][j]
								< mapping_constraints[c].P_feat[0][j])
						{
							applies = false;
							break checkfeatures;
						}
						else if (mapping_constraints[c].P_feat[1][j] != INACTIVE
//								 && current_row.features[j] > mapping_constraints[c].P_feat[1][j])
								 && learnerTask.featureMatrix[i][j] > mapping_constraints[c].P_feat[1][j])
						{
							applies = false;
							break checkfeatures;
						}
						
					}
					else 
					{
						// we're checking the Q side
//						if (current_row.features[j] < mapping_constraints[c].Q_feat[0][j])
						if (learnerTask.featureMatrix[i][j] < mapping_constraints[c].Q_feat[0][j])
						{
							applies = false;
							break checkfeatures;
						}
						else if (mapping_constraints[c].Q_feat[1][j] != INACTIVE
//								 && current_row.features[j] > mapping_constraints[c].Q_feat[1][j])
								 && learnerTask.featureMatrix[i][j] > mapping_constraints[c].Q_feat[1][j])
						{
							applies = false;
							break checkfeatures;
						}
					}
 				
 				} // end of checkfeatures block
	
 				// now if 'applies' is still true, output the segment
 				if (applies)
 				{
 					if (first)
 					{
 						compatiblesegments.append(learnerTask.segments[i]);
 						first = false;
 					}
 					else compatiblesegments.append(","+learnerTask.segments[i]);
 				}
 			} 		
 		}
 		catch (NullPointerException e)
 		{
 			// just means there was no segment, so keep going
 		}
 	}
 	return String.valueOf(compatiblesegments);
 }

 // __________________________________________________________________________________
 public boolean featuresMatch( int[] array1, int[] array2) throws IOException
 {
  boolean match = true;
  int values;
  values = (array1.length <= array2.length ? array1.length : array2.length);
  
  if (debug)
  {
  	outputfile.write("\t\t\tComparing features:\n");
  	outputfile.write("\t\t\t\t");
  	for (int i = 0; i < values; i++)
  	{
  		outputfile.write("(" + array1[i] + "," + array2[i] + ")   ");
  	}
  	outputfile.write("\n");	
  }
  
  for (int i = 0; i < values; i++)
  {
	    if( array1[i] != array2[i])
	   {
		    match = false;
		    if (debug)
		    	outputfile.write("feature " + i + " does not match (" + array1[i] + "," + array2[i] + ") ");
		    break;
	   }
  }
  if (debug && match)
  	outputfile.write("\t\t\tThe features match.");
  
  
  return match;
 }
 // __________________________________________________________________________________
 public boolean featuresInBounds( int[][] bounds, int[] features ) throws IOException
 {
  boolean inBounds = true;
  int values;
  values = (bounds[0].length <= features.length ? bounds[0].length : features.length);
  
  if (features == null)
   return false;
  for (int i=0; i < values; i++)
  {
   if (bounds[0][i] != INACTIVE && ( features[i] < bounds[0][i] || features[i] > bounds[1][i]))
   {
    inBounds = false;
    break;
   }
  }
  return inBounds;
 }
// __________________________________________________________________________________
// boundsInBounds() takes two sets of bounds as its arguments, and returns an integer 
// reflecting their subset relation (0 = neither bounds the other, 1 = the
// first bounds the second, and 2 = the second bounds the first, 3=
// they're identical (each bounds the other)) 
// *** a set of bounds is bounded than another when: its bounds are contained
// within the bounds of the other.

 public int boundsInBounds( int[][] b1, int[][] b2 ) throws IOException
 {
	boolean b1Boundsb2 = true, b2Boundsb1 = true;
//	The following would be more reusable, but involves more calculations:
//	int values = (b1[0].length <= b2[0].length ? b1[0].length : b2[0].length);
	int values = numberOfFeatures;
		
	for (int i = 0; i < values; i++) 
	{
		// it's possible that this feature isn't active for one bounds or 
		// the other (inactivity is defined by bounds[0][x]==INACTIVE)
		// there are, as always, 4 cases:
		//		a. feature is active for both
		//		b. feature active for b1 but not b2
		//		c. feature active for b2 but not b1
		//		d. feature inactive for both
		
		if (b1[0][i] != INACTIVE) // feature active for b1, cases a & b
		{
			if (b2[0][i] != INACTIVE) // feature active for b2, case a
			{
				// check the lower bounds
				if (b1[0][i] < b2[0][i])
				{
					b2Boundsb1 = false;
				} else if (b2[0][i] < b1[0][i])
				{
					b1Boundsb2 = false;
				}
				
				// check the upper bounds
				if (b1[1][i] > b2[1][i])
				{
					b2Boundsb1 = false;
				} else if (b2[1][i] > b1[1][i])
				{
					b1Boundsb2 = false;
				}				
			} else { // feature inactive for b2, case b
				b1Boundsb2 = false;
			}
		}
		else // feature inactive for b1, cases c & d
		{
			if (b2[0][i] != INACTIVE) // feature active for b2, case c
			{
				b2Boundsb1 = false;
			} else { // feature inactive for b2, case d
				// nothing happens here.
			}
		}
		
		// a consistency check here will let us not go through all the features
		// if we already know we'll have to return a 0
		if (!b1Boundsb2 && !b2Boundsb1)
			return 0;
		
	} // end of looping through features
	
	if (b1Boundsb2)
	{
		if (b2Boundsb1)
		{
			// they're identical, return 3
			return 3;
		}
		else return 1; // b1 bounds b2, but not vice versa
	}
	else{ // b2 doesn't bound b2,
		if (b2Boundsb1) // but if b2 bounds b1, then
			return 2;	// return 2
	}
	// if we got this far, neither bounds the other.
	return 0;
 } 
 // __________________________________________________________________________________
 public int[] decomposeSeg( char seg ) throws IOException
 {
 	int featuresToReturn[] = new int[ numberOfFeatures ];

	try{
	  for (int i = 0; i < numberOfFeatures; i++)
	   featuresToReturn[i] = learnerTask.featureMatrix[ seg ][i];
	  }
	  catch(ArrayIndexOutOfBoundsException e )
	  {
	   outputfile.write("Warning: segment [" + seg + "] is not in feature table!");
	   featuresToReturn = null;
	  }
	  catch(NullPointerException e)
	  {
	   System.out.print("Error trying to decompose segment: [" + seg + "]\n");
	  }
	  return featuresToReturn; 

 }
 // __________________________________________________________________________________
 public void calculateImpugnedConfidences() throws IOException
 {
 	// we will need to consider all the mapping constraints, to see who is taking
 	// credit for work which their subsets are doing;
 	// the best way to do this is to consider the mapping constraints in little batches;
 	// namely, those which share the same change A->B, since those are the ones which
 	// we need to compare against each other.
 	// The changes (A->B) are stored in a hash table, so the way to get them in batches
 	// is to have the hash table tell us an Enumeration of all of the changes.
 	// Then, we will work our way through that enumeration, getting the list of relevant
 	// mapping constraints for each change, and looking for subset relations.
 	ConstraintBatch current_batch;
 	int specificity_relation = 0; 
 	int new_hits = 0;
 	int new_scope = 1;
 	String current_change = new String();
 	// we're going to need to calculate some upper confidence values for comparison:
 	// i'm going to instantiate it now even though we may not need it, because
 	// we may actually need it many times so it is more efficient to instantiate
 	// it just once.
 	UpperConfidenceAdjustor allegation = new UpperConfidenceAdjustor();
		System.out.println("\rThere are " + changes.size() + " descriptions in the pantheon.");
 		System.out.flush();
 	
//	Set all_changesSet = changes.keySet();
//	Iterator all_changes = all_changesSet.iterator();
 	Enumeration all_changes = changes.keys();

 	// Work through the enumeration, as long as there are changes left:
// 	while ( all_changes.hasNext() )
 	while ( all_changes.hasMoreElements() )
 	{
 		// first, we'll get a list of all the constraints which have this change
 		current_change = (String) all_changes.nextElement();
// 		current_change = (String) all_changes.next();
 		current_batch = ( ConstraintBatch ) changes.get( current_change );

 		System.out.println( "There are " + (current_batch.size() ) + " constraints for the change " + current_change);
 		System.out.flush();
 		
 		// cycle through this list of constraints:
 		for (int i = 1; i < current_batch.size(); i++) // note: used to be current_batch.size() - 1 ???
 		{
 		
  			// Now, we will take the current constraint and look for other constraints
 			// which are more or less specific than it. 
 			// the current constraint is: mapping_constraints[ current_batch[i] ]
 			// we'll look among the remaining constraints:
 			// for all j, from j=(i+1) -> j=(current_batch.length-1)
 			// comparison constraint: mapping_constraints[ current_batch[j] ]
 			// NOTE: we'll only do this on generalized constraints!  
 			// ***inefficiency: we are constantly checking the same constraints to
 			// see if they are generalized or not-- perhaps we want to store 
 			// generalized and specific constraints separately somehow?
 			
 			if (mapping_constraints[ current_batch.constraints[i] ].keep &&
 				!mapping_constraints[ current_batch.constraints[i] ].degenerate)
 			{ // this guy's generalized, try comparing it: 			
	 			for (int j = i+1; j < (current_batch.constraints[0]); j++)
	 			{
//	 				if (mapping_constraints[ current_batch.constraints[j]].scope > 1)
	 				if (mapping_constraints[ current_batch.constraints[j]].keep &&
	 					!mapping_constraints[ current_batch.constraints[j]].degenerate)
	 				{	// this guy is also generalized, assess specificity rel.
	 					switch (assessSpecificity( current_batch.constraints[i], current_batch.constraints[j]))
	 					{
	 						case 0: // neither constraint more specific: do nothing
	 								break;
	 						case 1: // constraint i is more specific -- 
//									System.out.println("\t\tConstraint " + current_batch.constraints[i] + 
//													" is more specific than constraint " + current_batch.constraints[j] + ".");
	 								allegation.calculate( (mapping_constraints[current_batch.constraints[j]].hits - mapping_constraints[current_batch.constraints[i]].hits),
	 													  (mapping_constraints[current_batch.constraints[j]].scope - mapping_constraints[current_batch.constraints[i]].scope) );

//									System.out.println("\t\t\tcalculating upper confidence limit of the ratio " + 
//									(mapping_constraints[current_batch.constraints[j]].hits - mapping_constraints[current_batch.constraints[i]].hits) + "/" + 
//									(mapping_constraints[current_batch.constraints[j]].scope - mapping_constraints[current_batch.constraints[i]].scope) );

	 								if (allegation.p95 < mapping_constraints[current_batch.constraints[j]].impugnedConfidence.p95)
	 								{
	 									mapping_constraints[current_batch.constraints[j]].impugnedConfidence.set_p95(allegation.p95);
//	 									System.out.println("\t\t\t(impugning p95)");
	 								}
	 								if (allegation.p90 < mapping_constraints[current_batch.constraints[j]].impugnedConfidence.p90)
	 								{
	 									mapping_constraints[current_batch.constraints[j]].impugnedConfidence.set_p90(allegation.p90);
//	 									System.out.println("\t\t\t(impugning p90)");
	 								}
	 								if (allegation.p75 < mapping_constraints[current_batch.constraints[j]].impugnedConfidence.p75)
	 								{
	 									mapping_constraints[current_batch.constraints[j]].impugnedConfidence.set_p75(allegation.p75);
//	 									System.out.println("\t\t\t(impugning p75)");
										// new 2/25/99 also remember who the impugner is!	
										mapping_constraints[current_batch.constraints[j]].setImpugner(current_batch.constraints[i] );
	 								}
//	 								else System.out.println("\t\t\t" + allegation.p75 + " isn't lower than " + 
//	 										mapping_constraints[current_batch.constraints[j]].impugnedConfidence.p75 + ", so doing nothing.");
	 								break;
	 						case 2: // constraint j is more specific
//									System.out.println("\t\tConstraint " + current_batch.constraints[j] + 
//													" is more specific than constraint " + current_batch.constraints[i] + ".");

	 								allegation.calculate( (mapping_constraints[current_batch.constraints[i]].hits - mapping_constraints[current_batch.constraints[j]].hits),
	 													  (mapping_constraints[current_batch.constraints[i]].scope - mapping_constraints[current_batch.constraints[j]].scope) );

//									System.out.println("\t\t\tcalculating upper confidence limit of the ratio " + 
//									(mapping_constraints[current_batch.constraints[i]].hits - mapping_constraints[current_batch.constraints[j]].hits) + "/" + 
//									(mapping_constraints[current_batch.constraints[i]].scope - mapping_constraints[current_batch.constraints[j]].scope) );

	 								if (allegation.p95 < mapping_constraints[current_batch.constraints[i]].impugnedConfidence.p95)
	 								{
	 									mapping_constraints[current_batch.constraints[i]].impugnedConfidence.set_p95(allegation.p95);
//	 									System.out.println("\t\t\t(impugning p95)");
	 								}	 								
	 								if (allegation.p90 < mapping_constraints[current_batch.constraints[i]].impugnedConfidence.p90)
	 								{
	 									mapping_constraints[current_batch.constraints[i]].impugnedConfidence.set_p90(allegation.p90);
//	 									System.out.println("\t\t\t(impugning p90)");
	 								}	 								
	 								if (allegation.p75 < mapping_constraints[current_batch.constraints[i]].impugnedConfidence.p75)
	 								{
	 									mapping_constraints[current_batch.constraints[i]].impugnedConfidence.set_p75(allegation.p75);
//	 									System.out.println("\t\t\t(impugning p75)");
										mapping_constraints[current_batch.constraints[i]].setImpugner(current_batch.constraints[j] );
	 								}	 								
//	 								else System.out.println("\t\t\t" + allegation.p75 + " isn't lower than " + 
//	 										mapping_constraints[current_batch.constraints[i]].impugnedConfidence.p75 + ", so doing nothing.");

	 								break;
	 					} // end of switch on specificity relation
	 				} // fi
	 			} // end loop through comparison constraints
 			} // fi
 		} // end loop through constraints		
 	} // end cycle through all changes
 }	// end calculateImpugnedConfidences() method
 // __________________________________________________________________________________ 
	// assessSpecificity takes two (generalized) constraints, and sees
	// if one is more specific the other -- that is, if it applies to a proper
	// subset of the target forms.  

	// the assessSpecificity method takes two int arguments (the indices
	// of two mapping constraints in the mapping_constraints array),
	// and it returns an int with the value 0 (neither constraint more specific than 
	// the other), 1 (c1 is more specific), or 2 (c2 is more specific than c1)

	public int assessSpecificity( int c1, int c2 ) throws IOException
	{
		// the calculation of subsets is very similar to the 
		// calculation of shared material in the generalize()
		// method;  however, we actually don't need to save
		// much information from the search, so there are fewer
		// variables to start with.

		int shorter;
		// we'll do this by trying to prove that each constraint is 
		// more specific than the other
		boolean c1_more_specific = false,
			c2_more_specific = false;
		
		int P_sshare_index, Q_sshare_index;
			
//		System.out.println("\tassessSpecificity: comparing constraints " + c1 + " and " + c2 + ".");
/*		if (mapping_constraints[c1] == null) {
			System.out.println("\t...trouble! constraint " + c1 + " doesn't exist!");
			return 0;
			}

		if (mapping_constraints[c2] == null) {
			System.out.println("\t...trouble! constraint " + c2 + " doesn't exist!");
			return 0;
			}			
*/
		// first, see how much string they have in common on the left
		// (we'll use matchRight() to see where they diverge)
		P_sshare_index = matchRight( mapping_constraints[c1].P,
					     mapping_constraints[c2].P, 0);
		
//		System.out.println("\t\t c1 P len: " + mapping_constraints[c1].P.length +
//						   ", c2 P len: " + mapping_constraints[c2].P.length + 
//						    ", P_sshare_index: " + P_sshare_index);
					     
		// if we take away the shared string, there are 
		// four possibilities:
		// 	1. c1 and c2 are both string-subsumed
		// 	2. c1 is not string-subsumed, but c2 is
		// 	3. c1 is string-subsumed, but c2 is not
		// 	4. neither c1 nor c2 is string-subsumed
		// In cases 1-3, we need to go on and check features
		// and residues; in case 4, we're sunk because
		// there is string material left in both constraints,
		// so neither can be less specific than the other.
		
		// *** NOTE: i would rather switch cases 2 and 3 and test
		// for them in the opposite order, but i will stick with 
		// this order to be consistent with the generalize() method
		
		if ( mapping_constraints[c2].P.length <= (P_sshare_index))
		    {  // c2 is string-subsumed (case 1 or case 2)  
		    	if (mapping_constraints[c1].P.length <= (P_sshare_index))
			    {	// both c1 and c2 string-subsumed (case 1)
			    
//			    System.out.print("\t\t...P side case 1");
			    
				// there are many subcases here, depending on
				// whether or not both sides have features, 
				// residues, etc.
			
				// the cases are:
				// a. both sides have P-side features 
			  	// 	(subcases: one, both, neither side
				//	 has P_residue)
				// b. only c1 has P-side features
				//	(need to check if c2 has residue)
				// c. only c2 has P-side features
				//	(need to check if c1 has residue)
				// d. neither constraint has P-side features
				//	(need to check residues)
				if (mapping_constraints[c1].P_features_active)
				    { // c1 does have P-side features
				    	if (mapping_constraints[c2].P_features_active) 
					    {	// this is case a -- we need to compare the features;
					    	// we'll call the boundsInBounds() method, which returns
					    	// 0 if neither is bounded by the other, 1 if the first
					    	// bounds the second, 2 if the second bounds
					    	// the first, and 3 if they are equal.  A constraint is more
					    	// general than another when it bounds its features.
//							System.out.print("a\n");
					    	switch( boundsInBounds(mapping_constraints[c1].P_feat,
					    						   mapping_constraints[c2].P_feat) )
					    	{
						    	case 0: return 0;
						    	case 1: if (mapping_constraints[c1].P_residue || !mapping_constraints[c2].P_residue)
						    				c2_more_specific = true;
						    			else return 0;
						    			break;
						    	case 2: if (!mapping_constraints[c1].P_residue || mapping_constraints[c2].P_residue)
						    				c1_more_specific = true;
						    			else return 0;
						    			break;
								case 3: if (mapping_constraints[c1].P_residue)
										{
											if (!mapping_constraints[c2].P_residue)
											{	// c2 doesn't allow residue, so it is more specific:
												c2_more_specific = true;
												break;
											}
											// otherwise, they really ARE the same (yes residue), do nothing.
											else break;
										}
										else if (mapping_constraints[c2].P_residue)
										{  // c1 doesn't allow residue, so it is more specific:
											c1_more_specific = true;
											break;
										}
										// otherwise, they are the same (no residue), do nothing
										else break;
//						    	case 3: // nothing learned so far, they are the same
//						    			break;				    					    	
					    	} 
					    	    
					    }
					else
					    {	// this is case b: c1 has P-side
					    	// features, but c2 does not.  here,
							// our only hope is that c2 has
							// P_residue; otherwise, we're sunk.
//							System.out.print("b\n");
							if (mapping_constraints[c2].P_residue)
						    {
						    	// in this case, c1 is more
						    	// marked than c2, it is more specific
								c1_more_specific = true;
						    }					    
							else return 0;					   
					    }
				    
				    } // end of cases a and b
				else // c1 doesn't have P-side features
				    {
				    	if (mapping_constraints[c2].P_features_active) 
					    {	// this is case c, the mirror of b
//							System.out.print("c\n");
					        if (mapping_constraints[c1].P_residue)
						    {
							    	// in this case, c2 is more
							    	// marked than c1, it is more specific
									c2_more_specific = true;
						    }
							 else return 0;
					    }
						else
					    {	// this is case d (neither constraint
							// has active P features)
					        // here, we just need to check residues;
					        // we are looking for a mismatch.
//							System.out.print("d\n");
							if (!mapping_constraints[c2].P_residue)
						    {
						        if (mapping_constraints[c1].P_residue)
							    { // they don't match, and c2 is more specific
							      c2_more_specific = true;
							    }
						    }
							 else // otherwise, c2 does have a P-side residue
						     {				     	    
						        if (!mapping_constraints[c1].P_residue)
							    { // they don't match, and c1 is more specific
							    	c1_more_specific = true;
							    }
						     }
							  // note that in case 1, this is the only way to 
							  // assign specificity relations, so we don't need
							  // to check compatibility here.
					    }			    
				    }
			    }
			else
			    {	// c1 is not string-subsumed (case 2)
			    	// In this case, there are segments left in c1, but not c2.
					// therefore, we need to look first to see if there are
					// features which we should be considering in c2, and then 
					// if there is residue.
					
//					System.out.println("\t\t...P side case 2");
					
					if (mapping_constraints[c2].P_features_active)
						{
							// since c2 has active P features, we need to pit these
							// against the next segment in c1's P side segments
							
							// segment: mapping_constraints[c1].P[ mapping_constraints[c1].P.length - (P_sshare_index + 1) ]
							// its features: decomposeSeg( " )
							// bounds:	mapping_constraints[c2].P_feat
							
							// check if the features are within the bounds:
							if (featuresInBounds(mapping_constraints[c2].P_feat,
								decomposeSeg(mapping_constraints[c1].P[ mapping_constraints[c1].P.length - (P_sshare_index + 1) ])))
								{ // the features are within the bounds, so c1 may be more specific than c2
								  // What we need to know now is whether c1 has even more segments left,
								  // lurking to be unanswered by P-side residue in c2.
								  // checK: now is c1 string-subsumed?
									if (mapping_constraints[c1].P.length > (P_sshare_index+1) || // if segs left
										mapping_constraints[c1].P_features_active || // or if features left
										mapping_constraints[c1].P_residue) // or if residue left
									{ // then we need to enforce c2's P_residue to cover c1's remainder
									  	if (mapping_constraints[c2].P_residue)
									  	{
									  		// we're ok, the P_residue will cover it, but c1 is more specific
									  		c1_more_specific = true;
									  	} else { // we're sunk.
									  		return 0;
									  	}
									}
									else 
									{ // otherwise, c1 has no tricks up its sleeve, it is more specific
										c1_more_specific = true;														
									}
								} else {
									// the segment doesn't lie within the features bounds,
									// the 2 constraints are incompatible
									return 0;
								}
						}
					else
						{	// no active features, so now we have to hope there is residue
							if (mapping_constraints[c2].P_residue)
								{	// the residue saves us, but we know that c1 is
									// more specific than c2
									c1_more_specific = true;
								}
							else	// we're sunk. 
								{
									return 0;
								}
						}
			    }
			    
		  } // end cases 1 and 2 
		 else 
	     { // c2 is NOT string-subsumed (case 3 or 4)
			if (mapping_constraints[c1].P.length < (P_sshare_index+1))
			    {	// c1 is string-subsumed (case 3)
			    
//					System.out.println("\t\t...P side case 3");			    
					
			    	// In this case, there are segments left in c2, but not c1.
					// therefore, we need to look first to see if there are
					// features which we should be considering in c1, and then 
					// if there is residue.
					if (mapping_constraints[c1].P_features_active)
						{
							// since c1 has active P features, we need to pit these
							// against the next segment in c2's P side segments
							
							// segment: mapping_constraints[c2].P[ mapping_constraints[c2].P.length - (P_sshare_index + 1) ]
							// its features: decomposeSeg( " )
							// bounds:	mapping_constraints[c1].P_feat
							
							// check if the features are within the bounds:
							if (featuresInBounds(mapping_constraints[c1].P_feat,
								decomposeSeg(mapping_constraints[c2].P[ mapping_constraints[c2].P.length - (P_sshare_index + 1) ])))
								{ // the features are within the bounds, so c2 may be more specific than c1
								  // What we need to know now is whether c2 has even more segments left,
								  // lurking to be unanswered by P-side residue in c1.
								  // checK: now is c2 string-subsumed?
									if (mapping_constraints[c2].P.length > (P_sshare_index+1) || // if segs left
										mapping_constraints[c2].P_features_active || // or if features left
										mapping_constraints[c2].P_residue) // or if residue left
									{ // then we need to enforce c1's P_residue to cover c2's remainder
									  	if (mapping_constraints[c1].P_residue)
									  	{
									  		// we're ok, the P_residue will cover it, but c2 is more specific
									  		c2_more_specific = true;
									  	} else { // we're sunk.
									  		return 0;
									  	}
									}
									else 
									{ // otherwise, c2 has no tricks up its sleeve, it is more specific
										c2_more_specific = true;														
									}
								} else {
									// the segment doesn't lie within the features bounds,
									// the 2 constraints are incompatible
									return 0;
								}
						}
					else
						{	// no active features, so now we have to hope there is residue
							if (mapping_constraints[c1].P_residue)
								{	// the residue saves us, but we know that c2 is
									// more specific than c1
									c2_more_specific = true;
								}
							else	// we're sunk. 
								{
									return 0;
								}
						}
			    }		
			    else 
			    { // neither c1 nor c2 is string-subsumed (case 4)

//					System.out.println("\t\t...P side case 4");

			    	// in case 4, we're sunk:
			    	 return 0;
			    }
		 } // end cases 3 and 4
		 
		// ************ DONE WITH THE P SIDE, ON TO Q
		// NEXT, see how much string they have in common on the right
		// (we'll use matchRight() to see where they diverge)
	    shorter = (mapping_constraints[c1].Q.length <= mapping_constraints[c2].Q.length 
	       ? mapping_constraints[c1].Q.length : mapping_constraints[c2].Q.length);

	    Q_sshare_index = matchLeft( mapping_constraints[c1].Q,
	           mapping_constraints[c2].Q, shorter );
					     
//		System.out.println("\t\t c1 Q len: " + mapping_constraints[c1].Q.length +
//						   ", c2 Q len: " + mapping_constraints[c2].Q.length + 
//						    ", Q_sshare_index: " + Q_sshare_index);

		// if we take away the shared string, there are 
		// four possibilities:
		// 	1. c1 and c2 are both string-subsumed
		// 	2. c1 is not string-subsumed, but c2 is
		// 	3. c1 is string-subsumed, but c2 is not
		// 	4. neither c1 nor c2 is string-subsumed
		// In cases 1-3, we need to go on and check features
		// and residues; in case 4, we're sunk because
		// there is string material left in both constraints,
		// so neither can be less specific than the other.

		/*	For the Q side, the definition of "string-subsumed" is:	
				Q.length <= Q_sshare_index
			(that is, everything is shared, including the last character)		
		*/

		if (mapping_constraints[c2].Q.length <= Q_sshare_index)
		    {  // c2 is string-subsumed (case 1 or case 2)  
				if (mapping_constraints[c1].Q.length <= Q_sshare_index)
//		    	if (Q_sshare_index >= (mapping_constraints[c1].Q.length - 1)) // ... and here...
			    {	// both c1 and c2 string-subsumed (case 1)
//					System.out.print("\t\t...Q side case 1");			    
				// there are many subcases here, depending on
				// whether or not both sides have features, 
				// residues, etc.
			
				// the cases are:
				// a. both sides have Q-side features 
			  	// 	(subcases: one, both, neither side
				//	 has Q_residue)
				// b. only c1 has Q-side features
				//	(need to check if c2 has residue)
				// c. only c2 has Q-side features
				//	(need to check if c1 has residue)
				// d. neither constraint has Q-side features
				//	(need to check residues)
				if (mapping_constraints[c1].Q_features_active)
				    { // c1 does have Q-side features
				    	if (mapping_constraints[c2].Q_features_active) 
					    {	// this is case a -- we need to compare the features;
					    	// we'll call the boundsInBounds() method, which returns
					    	// 0 if neither is bounded by the other, 1 if the first
					    	// bounds the second, 2 if the second bounds
					    	// the first, and 3 if they are equal.
//							System.out.print("a");
					    	switch( boundsInBounds(mapping_constraints[c1].Q_feat,
					    						   mapping_constraints[c2].Q_feat) )
					    	{

						    	case 0: // System.out.print(", subcase 0\n");
						    			return 0;
						    	case 1: // System.out.print(", subcase 1\n");
						    			if (mapping_constraints[c1].Q_residue || !mapping_constraints[c2].Q_residue)
						    			{
						    				c2_more_specific = true;
											// see if this contradicts something we found out
											// on the P side
							    			if (c1_more_specific)
							    				return 0;					    	
						    			}
						    			else return 0;
						    			break;
						    	case 2: // System.out.print(", subcase 2\n");
						    			if (!mapping_constraints[c1].Q_residue || mapping_constraints[c2].Q_residue)
										{
						    				c1_more_specific = true;
											// see if this contradicts something we found out
											// on the P side
						    				if (c2_more_specific)
						    					return 0;
						    			}
						    			else return 0;
						    			break;
								case 3: // System.out.print(", subcase 3\n");
										if (mapping_constraints[c1].Q_residue)
										{
											if (!mapping_constraints[c2].Q_residue)
											{	// c2 doesn't allow residue, so it is more specific:
												c2_more_specific = true;
												// see if this contradicts something we found out
												// on the P side
								    			if (c1_more_specific)
								    				return 0;					    													
												break;
											}
											// otherwise, they really ARE the same (yes residue), do nothing.
											else break;
										}
										else if (mapping_constraints[c2].Q_residue)
										{  // c1 doesn't allow residue, so it is more specific:
											c1_more_specific = true;
											// see if this contradicts something we found out
											// on the P side
						    				if (c2_more_specific)
						    					return 0;
											break;
										}
										// otherwise, they are the same (no residue), do nothing
										else break;
					    	} 					    	    
					    }
					else
					    {	// this is case b: c1 has Q-side
					    	// features, but c2 does not.  here,
							// our only hope is that c2 has
							// Q_residue; otherwise, we're sunk.
//							System.out.print("b\n");							
							if (mapping_constraints[c2].Q_residue)
						    {
						    	// in this case, c1 is more
						    	// marked than c2, it is more specific
								c1_more_specific = true;
								// see if this contradicts something we found out
								// on the P side
				    			if (c2_more_specific)
				    				return 0;

						    }					    
							else return 0;					   
					    }
				    
				    } // end of cases a and b
				else // c1 doesn't have Q-side features
				    {
				    	if (mapping_constraints[c2].Q_features_active) 
					    {	// this is case c, the mirror of b
//							System.out.print("c\n");
					        if (mapping_constraints[c1].Q_residue)
						    {
							    	// in this case, c2 is more
							    	// marked than c1, it is more specific
									c2_more_specific = true;
									// see if this contradicts something we found out
									// on the P side
					    			if (c1_more_specific)
					    				return 0;					    	
						    }
							 else return 0;
					    }
						else
					    {	// this is case d (neither constraint
							// has active Q features)
					        // here, we just need to check residues;
					        // we are looking for a mismatch.
//							System.out.print("d\n");
							if (!mapping_constraints[c2].Q_residue)
						    {
						        if (mapping_constraints[c1].Q_residue)
							    { // they don't match, and c2 is more specific
							     	c2_more_specific = true;
									// see if this contradicts something we found out
									// on the P side
					    			if (c1_more_specific)
					    				return 0;					    	
							    }
						    }
							 else // otherwise, c2 does have a Q-side residue
						     {				     	    
						        if (!mapping_constraints[c1].Q_residue)
							    { // they don't match, and c1 is more specific
							    	c1_more_specific = true;
									// see if this contradicts something we found out
									// on the P side
					    			if (c2_more_specific)
					    				return 0;					    	
							    }
						     }
					    }			    
				    }
			    }
			else
			    {	// c1 is not string-subsumed (case 2)
//					System.out.println("\t\t...Q side case 2");			    			    
			    
			    	// In this case, there are segments left in c1, but not c2.
					// therefore, we need to look first to see if there are
					// features which we should be considering in c2, and then 
					// if there is residue.
					if (mapping_constraints[c2].Q_features_active)
						{
							// since c2 has active Q features, we need to pit these
							// against the next segment in c1's Q side segments
							
							// segment: mapping_constraints[c1].Q[ Q_sshare_index ]
							// its features: decomposeSeg( " )
							// bounds:	mapping_constraints[c2].Q_feat
							
							// check if the features are within the bounds:
							if (featuresInBounds(mapping_constraints[c2].Q_feat,
								decomposeSeg(mapping_constraints[c1].Q[ Q_sshare_index ])))
								{ // the features are within the bounds, so c1 may be more specific than c2
								  // What we need to know now is whether c1 has even more segments left,
								  // lurking to be unanswered by Q-side residue in c2.
								  // checK: now is c1 string-subsumed?
									if ((Q_sshare_index + 1) < mapping_constraints[c1].Q.length || // if segs left
										mapping_constraints[c1].Q_features_active || // or if features left
										mapping_constraints[c1].Q_residue) // or if residue left
									{ // then we need to enforce c2's Q_residue to cover c1's remainder
									  	if (mapping_constraints[c2].Q_residue)
									  	{
									  		// we're ok, the Q_residue will cover it, but c1 is more specific
									  		c1_more_specific = true;
											// see if this contradicts something we found out
											// on the P side
							    			if (c2_more_specific)
							    				return 0;
									  	} else { // we're sunk.
									  		return 0;
									  	}
									}
									else 
									{ // otherwise, c1 has no tricks up its sleeve, it is more specific
										c1_more_specific = true;														
										// see if this contradicts something we found out
										// on the P side
						    			if (c2_more_specific)
						    				return 0;
									}
								} else {
									// the segment doesn't lie within the features bounds,
									// the 2 constraints are incompatible
									return 0;
								}
						}
					else
						{	// no active features, so now we have to hope there is residue
							if (mapping_constraints[c2].Q_residue)
								{	// the residue saves us, but we know that c1 is
									// more specific than c2
									c1_more_specific = true;
									// see if this contradicts something we found out
									// on the P side
					    			if (c2_more_specific)
					    				return 0;
								}
							else	// we're sunk. 
								{
									return 0;
								}
						}
			    }
			    
		  } // end cases 1 and 2 
		 else 
		 { // c2 is NOT string-subsumed (cases 3 and 4)
		    	if (Q_sshare_index > (mapping_constraints[c1].Q.length - 1))
			    {	// ...but c1 IS string-subsumed
			        // this is case 3
//					System.out.println("\t\t...Q side case 3");			    			        
			    	// In this case, there are segments left in c2, but not c1.
					// therefore, we need to look first to see if there are
					// features which we should be considering in c1, and then 
					// if there is residue.
					if (mapping_constraints[c1].Q_features_active)
						{
							// since c1 has active Q features, we need to pit these
							// against the next segment in c2's Q side segments
							
							// segment: mapping_constraints[c2].Q[ Q_sshare_index ]
							// its features: decomposeSeg( " )
							// bounds:	mapping_constraints[c1].Q_feat
							
							// check if the features are within the bounds:
							if (featuresInBounds(mapping_constraints[c1].Q_feat,
								decomposeSeg(mapping_constraints[c2].Q[ Q_sshare_index ])))
								{ // the features are within the bounds, so c2 may be more specific than c1
								  // What we need to know now is whether c2 has even more segments left,
								  // lurking to be unanswered by Q-side residue in c1.
								  // checK: now is c2 string-subsumed?
									if ((Q_sshare_index + 1) < mapping_constraints[c2].Q.length || // if segs left
										mapping_constraints[c2].Q_features_active || // or if features left
										mapping_constraints[c2].Q_residue) // or if residue left
									{ // then we need to enforce c1's Q_residue to cover c2's remainder
									  	if (mapping_constraints[c1].Q_residue)
									  	{
									  		// we're ok, the Q_residue will cover it, but c2 is more specific
									  		c2_more_specific = true;
											// see if this contradicts something we found out
											// on the P side
							    			if (c1_more_specific)
							    				return 0;
									  	} else { // we're sunk.
									  		return 0;
									  	}
									}
									else 
									{ // otherwise, c2 has no tricks up its sleeve, it is more specific
										c2_more_specific = true;														
										// see if this contradicts something we found out
										// on the P side
						    			if (c1_more_specific)
						    				return 0;
									}
								} else {
									// the segment doesn't lie within the features bounds,
									// the 2 constraints are incompatible
									return 0;
								}
						}
					else
						{	// no active features, so now we have to hope there is residue
							if (mapping_constraints[c1].Q_residue)
								{	// the residue saves us, but we know that c2 is
									// more specific than c1
									c2_more_specific = true;
									// see if this contradicts something we found out
									// on the P side
					    			if (c1_more_specific)
					    				return 0;
								}
							else	// we're sunk. 
								{
									return 0;
								}
						}
			    }		    
			    else 
			    { // neither c1 nor c2 is string-subsumed (case 4)
			    
//					System.out.println("\t\t...Q side case 4");			    			    
			    	// in case 4, we're sunk:
			    	 return 0;
			    }
		 } // end cases 3 and 4
		// ***  DONE WITH THE Q SIDE
		
		if (c1_more_specific)
		{
			if (c2_more_specific)
				return 0;
			else return 1; // c1 is more specific
		}
		else 
		{
			if (c2_more_specific)
				return 2; // c2 is more specific
			else 
			{
				// the two constraints are the same -- this ideally would not happen, but 
				// it does seem to happen because we don't check for uniqueness very rigorously
				// when adding doppelgaengers.  the solution for now is to simply ignore one
				// of the two, we'll flag it for later ignoral.
				mapping_constraints[c2].setKeep( false );
				
//				System.out.println("WARNING: the assessSpecificity method thinks that constraints " + c1 + " and " + c2 + " are the same constraint.");
//				printConstraint(c1);
//				printConstraint(c2);
				return 0;
			}
		}
			 
	}
	public void summarizeChanges()
	{
//		Set all_changesSet = changes.keySet();
//		Iterator all_changes = all_changesSet.iterator();
		Enumeration all_changes = changes.keys();
		String current_change = new String();
		ConstraintBatch current_row;
		int numberOfConstraints;
		
		outputfile.write("\rIndex of Changes:\n");
		
//		while (all_changes.hasNext())
		while (all_changes.hasMoreElements())
		{
			current_change = (String) all_changes.nextElement();
//			current_change = (String) all_changes.next();
			current_row = (ConstraintBatch) changes.get(current_change);
			numberOfConstraints = current_row.size();
			
			outputfile.write("\t"+ current_change + ":\t");
			for (int i = 1; i < numberOfConstraints; i++)
			{
				outputfile.write(String.valueOf(current_row.constraints[i])+" ");
			}
			outputfile.write("\n");
		}
		
		outputfile.write("\n");
	}
	
	 // __________________________________________________________________________________
 /*
  wugTestWithLearning() is a version of the wug tester which treats the wug forms
  as pseudo-data, pretending they were real words and trying to generalize by comparing
  it to existing, bona fide constraints.   It does something very similar to learning, 
  but doesn't hang on to the stuff it makes up.  Therefore, unfortunately, it can't share
  as much code as we might like.
 */
public void wugTestWithLearning()  throws IOException
{
	ConstraintBatch current_batch;

//	Set all_changesSet;
//	Iterator all_changes;
	Enumeration all_changes;
	int localoutputscounter = 0;
	char UR[] = new char[0];
	String URString;
	String localoutput = new String();
	int localchangescounter;
	String current_change = new String();
	String current_desc = new String();

	StringBuffer output = new StringBuffer(2000);
	StringBuffer sumoutput = new StringBuffer(500);
	StringBuffer c75output = new StringBuffer(500);
	StringBuffer c90output = new StringBuffer(500);
	StringBuffer hitsoutput = new StringBuffer(500);
	StringBuffer rawoutput = new StringBuffer(500);
	StringBuffer wbloutput = new StringBuffer(500);
	StringBuffer freqoutput = new StringBuffer(500);
	StringBuffer relfreqoutput = new StringBuffer(500);
	StringBuffer wbfoutput = new StringBuffer(500);
			
	int c, i;
	int change_location; // for keeping track of where struc descs are met
	char testFormChars[] = new char[0];
	
	// and for impugning, should we need to do wug-time learning:
	int specificity_relation = 0; 
 	int new_hits = 0;
 	int new_scope = 1;
 	UpperConfidenceAdjustor allegation = new UpperConfidenceAdjustor();
	// also, to keep track of the 10 best if we do wug-time learning
	int bestWugtimeConstraints[] = new int[10];
	for (int b=0; b<10; b++)
		bestWugtimeConstraints[b] = 0;
	int hold = 0;
	
	int highest_wug_c75,
		highest_wug_c90,
		highest_wug_hits,
		highest_wug_raw,
		highest_wug_wbl,
		highest_wug_freq,
		highest_wug_rfreq,
		highest_wug_wbf;
	double val_to_beat = 0;
		
	if (phonologyOnFlag)
	{
		System.out.println("Phonology is ON, with "+ phonological_rules.length+" rules.");
	}
	else System.out.println("Phonology is OFF");
	System.out.flush();
	
//	output.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\tiScope\tiHits\timpugner info\n");
	sumoutput.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\tiScope\tiHits\timpugner info\n");

	if (saveHitsFile)
		hitsoutput.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\timpugner info\n");
	if (saveC75File)
		c75output.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\timpugner info\n");
	if (saveC90File)
		c90output.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\timpugner info\n");
	if (saveRawRelFile)
		rawoutput.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\timpugner info\n");	
	if (saveWeightedFile)
		wbloutput.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\timpugner info\n");	
	if (saveFreqFile)
	{
		freqoutput.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\timpugner info\n");	
		relfreqoutput.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\timpugner info\n");	
	}
	if (saveWeightedByFreqFile)
		wbfoutput.append("form\tpattern\toutput\tmcat1\t\tmcat2\t\tA\t\tB\tChange\t\tPres\tPfeat\tP\t\tQ\tQfeat\tQres\tdoppel\tscope\ttypefreq\ttokenfreq\trel tokenfreq\toverall\twbf\twbl\tc75\tc90\tc95\ti75\ti90\ti95\tconstraint\trelated forms\texceptions\timpugner\timpugner info\n");	
		
	// go through all the test forms and (hopefully!) derive something for each one
	for (int w=0; w<learnerTask.realNumberOfTestForms; w++)
	{
		// the change to allow test_forms[] to be longer than the actual number of forms
		// has screwed this up now; the principled thing to do would be to set an integer
		// for the actual number of test forms; as a hack, I'm going to try just getting
		// out of here if we hit a null, but this should be fixed later.   xxxxxxx
		if (learnerTask.test_forms[w] == null)
			break;

	
		System.out.println("\tderiving outputs for test form ["+learnerTask.test_forms[w] +"]");	
//		System.out.flush();
		// grab a fresh list of all changes to work through:
//		output.append(w+1).append("\t0\t\t").append(String.valueOf(learnerTask.test_forms[w])).append("\n");

//		all_changesSet = changes.keySet();
//		all_changes = all_changesSet.iterator();
		all_changes = changes.keys();

		localchangescounter = 0;

		// now we will do outputs change-by-change, only outputting the top 10 for each change.
		// since we can stop after finding 10 that match, actually the most time-consuming changes
		// would be ones that don't apply-- so we should also do a check for each change to see
		// if it applies at all.

//		while (all_changes.hasNext() )
		while (all_changes.hasMoreElements() )
		{

		deriveonechange:{
			current_change = (String) all_changes.nextElement();
//			current_change = (String) all_changes.next();
			current_batch = (ConstraintBatch) changes.get( current_change );

			// now do a preliminary check to see if this change is potentially relevant			
			current_desc = String.valueOf(mapping_constraints[ current_batch.constraints[1] ].A);
			change_location = learnerTask.test_forms[w].indexOf(current_desc);
			if ( change_location >= 0 )
			{
				// ok, this form contains the A for this batch
				localchangescounter++;

				// we will set the localoutput to null for now, until we find a constraint to build it
				localoutput = null;
				localoutputscounter = 1;
				val_to_beat=-50;			
				i = 1;

				findtop10:{				
					// now we find the top 10 constraints which derive this output (if there are 10)
					while (localoutputscounter < 2 && i < current_batch.size()) // doing just 1 for now to save time
					{
							change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
							if (change_location >= 0)
							{
								// we will save time by building the output form only once
								// (DANGER: this would cause a problem if there are two homophonous changes
								//  with different locations -- like prefix n- and suffix -n)
								if (localoutput == null)
								{
								     UR = applyConstraint(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i], change_location);
								     if (phonologyOnFlag)
								     {
										localoutput = String.valueOf(applyPhonology(UR));
								     }
								     else
								     {
										localoutput = String.valueOf(UR);
								     }	
								     // 8/22/00  also, this guy is the best bona fide, so wug forms have to beat it
								     val_to_beat = mapping_constraints[current_batch.constraints[i]].overallconfidence;
//									output.append(w+1).append("\t").append(localchangescounter).append("\t0\t").append(learnerTask.test_forms[w]).append("\t->\t").append(String.valueOf(UR)).append("\t->\t").append(localoutput).append("\t").append(current_change).append("\t").append(change_location).append(" (legit)\n");
								}
								
								c=current_batch.constraints[i];
								
								// 4/11/99 now that we are ignoring various constraints (either for having hits
								// of 0, or for being a duplicate), we need to make sure that we are not digging so
								// deep as to reach these:
								// (8/22/00 why was this commented out? uncommenting to see what crashes)
								if (mapping_constraints[c].keep == false)
									break findtop10;
								
//								output.append((w+1)).append("\t").append(localchangescounter).append("\t").append(localoutputscounter).append("\t"
//														).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
								if (localoutputscounter == 1)
								{
									sumoutput.append(w+1).append("\t").append(localchangescounter).append("\t").append(localoutputscounter).append("\t").append(learnerTask.test_forms[w]).append("\t->\t").append(localoutput).append("\tby\t");
								}


								if (mapping_constraints[c].A.length == 0)
								{
//									output.append("[]");
									if (localoutputscounter == 1)
										sumoutput.append("[]");
								}
								else
//									output.append(String.valueOf(mapping_constraints[c].A)) ;
									if (localoutputscounter == 1)
										sumoutput.append(String.valueOf(mapping_constraints[c].A)) ;
										
//								output.append( "\t->\t");
								if (localoutputscounter == 1)
									sumoutput.append( "\t->\t");
								
								if (mapping_constraints[c].B.length == 0)
								{	// output.append("[]");
									if (localoutputscounter == 1)
										sumoutput.append("[]");
								}
								else
								{
//									output.append( String.valueOf(mapping_constraints[c].B));
									if (localoutputscounter == 1)
										sumoutput.append( String.valueOf(mapping_constraints[c].B));
									
								}
								
								// now the summarized A->B, which Bruce finds convenient
//								output.append( "\t").append(current_change);
								if (localoutputscounter == 1)
									sumoutput.append( "\t").append( current_change );
								
//								output.append("\t/\t");
								if (localoutputscounter == 1)
									sumoutput.append("\t/\t");
								
								// P residue
								if (mapping_constraints[c].P_residue)
								{
//									output.append("X");
									if (localoutputscounter == 1)
										sumoutput.append("X");
								}
//								output.append("\t");
								if (localoutputscounter == 1)
									sumoutput.append("\t");
								
								// P features
							    if (mapping_constraints[c].P_features_active)
							    {
									// we want to print a list of the compatible segments, for human readability
//									output.append(compatibleSegments( c, true ));
									if (localoutputscounter == 1)
										sumoutput.append(compatibleSegments( c, true ));
							    }
							    // P__Q
//								output.append("\t").append(String.valueOf(mapping_constraints[c].P)).append("\t___\t").append(String.valueOf(mapping_constraints[c].Q)).append("\t");
								
								if (localoutputscounter == 1)
								{
									sumoutput.append("\t").append( String.valueOf(mapping_constraints[c].P)).append( "\t___\t").append(String.valueOf(mapping_constraints[c].Q)).append("\t");
								}
								
								// Q features
								if (mapping_constraints[c].Q_features_active)
								{
//								  	output.append(compatibleSegments( c, false ));
									if (localoutputscounter == 1)
										sumoutput.append(compatibleSegments( c, false ));
								}
//								output.append("\t");
								if (localoutputscounter == 1)
									sumoutput.append("\t");
								// Q residue
								if (mapping_constraints[c].Q_residue)
								{
//								  output.append("Y");
								  if (localoutputscounter == 1)
								  	sumoutput.append("Y");
								  
								}
//								output.append("\t");
								if (localoutputscounter == 1)
									sumoutput.append("\t");
									
								// an indication of doppelgaengerhood	
								if (mapping_constraints[c].doppelgaenger)
								{
//									output.append("D\t");
									if (localoutputscounter == 1)
										sumoutput.append("D\t");
								}
								else
								{
//									output.append("\t");
									if (localoutputscounter == 1)
										sumoutput.append("\t");								
								}

									
								// scope and reliability						
//								output.append( mapping_constraints[c].scope).append("\t").append(mapping_constraints[c].hits).append("\t");
								if (localoutputscounter == 1)
									sumoutput.append( mapping_constraints[c].scope).append("\t").append(mapping_constraints[c].hits).append("\t");
								
								
								// type frequency for this guy:
//								output.append( mapping_constraints[c].hits_frequency).append( "\t");
								if (localoutputscounter == 1)
									sumoutput.append( mapping_constraints[c].hits_frequency).append("\t");

								// rel token frequency for this guy:
							if (saveFreqFile || saveWeightedByFreqFile)
							{
//								output.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
								if (localoutputscounter == 1)
									sumoutput.append(  (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000)).append("\t");
							}
							else
							{
//								output.append("n/a\t");
								if (localoutputscounter == 1)
									sumoutput.append("n/a\t");
							}	
								// now output the overall confidence for this guy:
//								output.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000) ).append( "\t");
								if (localoutputscounter == 1)
									sumoutput.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000)).append("\t");

								// and weighted by freq
							if (saveFreqFile || saveWeightedByFreqFile)
							{
//								output.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
								if (localoutputscounter == 1)
									sumoutput.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 )).append("\t");
							}
							else
							{
//								output.append("n/a\t");
								if (localoutputscounter == 1)
									sumoutput.append("n/a\t");
							}	
								// and weighted by length:
//								output.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");
								if (localoutputscounter == 1)
									sumoutput.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000)).append("\t");
								

								// and the particular confidences
//								output.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
//									 ).append( "\t" ).append( (Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
//									 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
//									 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
//									 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
//									 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
//									 ).append( "\t" ).append( c ).append( "\t");	 
									 
								if (localoutputscounter == 1)
								{
									sumoutput.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
									 ).append("\t").append((Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
									 ).append("\t").append((Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
									 ).append("\t").append((Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
									 ).append("\t").append((Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
									 ).append("\t").append((Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
									 ).append("\t").append(c).append("\t");	
								}
								
								
								// next come the related forms:
								for (int f = 1; f< related_forms[c][0]; f++)
								{
									if (f > 1)
									{
//										output.append(", ");
										if (localoutputscounter == 1)
											sumoutput.append(", ");
									}
//									output.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
									if (localoutputscounter == 1)
										sumoutput.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
									
									
								}
//								output.append("\t");
								if (localoutputscounter == 1)
									sumoutput.append("\t");
								
								// next come the exceptions to this constraint:
								for (int f = 1; f< exceptions[c][0]; f++)
								{
									if (f > 1)
									{
//										output.append(", ");
										if (localoutputscounter == 1)
											sumoutput.append(", ");
									}	
//									output.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
									if (localoutputscounter == 1)
										sumoutput.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
									
								}
//								output.append("\t");
								if (localoutputscounter == 1)
									sumoutput.append("\t");
								
								
								// now print out the impugner info
								if (mapping_constraints[c].impugner > -1)
								{
//									output.append( mapping_constraints[c].impugner ).append( "\t" 
//													).append( mapping_constraints[mapping_constraints[c].impugner].scope ).append( "\t"
//													).append( mapping_constraints[mapping_constraints[c].impugner].hits ).append( "\t");
//									output.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[c].impugner) ).append( "\n");
									if (localoutputscounter == 1)
									{
										sumoutput.append( mapping_constraints[c].impugner).append("\t" 
													).append(mapping_constraints[mapping_constraints[c].impugner].scope).append("\t"
													).append(mapping_constraints[mapping_constraints[c].impugner].hits).append("\t");
										sumoutput.append("Impugned by constraint ").append(printConstraint(mapping_constraints[c].impugner)).append("\n");
									}
									
								
								}
								else{
//								 	output.append("\t\t\n");
									if (localoutputscounter == 1)
										sumoutput.append("\t\t\n");
								}
								localoutputscounter++;
							}
							i++;

						} // end while to find 10
					} // end findtop10
					
					if (wug_learning_on)
					{
						// the change location was screwed up from trying real constraints, so reset:
						change_location = learnerTask.test_forms[w].indexOf(current_desc);
						// also, need to reset from previous wug-time learning:
						for (int b=0; b<10;b++)
						{
							bestWugtimeConstraints[b]=0;
						}
						
						
						// First, we treat the form we expected as an actual input:
						
						/*
						(actually, since we already know the change location, it would
						 be more efficient to compute the new constraint here directly, 
						 rather than recalculating the change location � but since I think
						 the number of times we will do this is very small, it is easier
						 to use pre-existing code here...)
						*/
						
						// WE NEED TO BUILD THE LOCALOUTPUT HERE!
						// in fact, this mimics how we used to apply a constraint before features; 
						// so I will use old code from that to do the trick:
						if(localoutput == null)
						{
							UR = applyConstraintChange(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[1]);

							if (UR != null)
							{
							     if (phonologyOnFlag)
							     {
									localoutput = String.valueOf(applyPhonology(UR));
							     }
							     else
							     {
									localoutput = String.valueOf(UR);
							     }	
							}
							else 
							{
								break deriveonechange;
							}
						} // end if(localoutput == null)

						findDegenerateMappings(learnerTask.test_forms[w].toCharArray(), 
											   localoutput.toCharArray());
						
					
						if (useImpugnment)
						{					   
							// now we have a new batch of constraints which should apply, so we need to
							// impugn them.  this is tricky! who impugns who?  the old guys impugn the
							// new guys.  do the new guys impugn the new guys?  I guess not, because for
							// every new guy, there is an old guy which handles all the same real forms.
							
							// the new constraints live at:
							// mapping_constraints[firstWugtimeConstraint] to mapping_constraints[newestConstraint]					   

					 		// cycle through the constraints:
					 		// the outer loop is of the pre-learned, "bona fide" constraints, which are 
					 		// contained in the current_batch
					 		for (int p = 1; p < current_batch.size(); p++) // 
					 		{
					 			// Now, we will take the current REAL constraint and look for wug-time constraints
					 			// which are less specific than it. 
					 			// the current constraint is: mapping_constraints[ current_batch[i] ]
					 			//  (we will only loop up to the end of the real constraints here)
					 							 			
					 			if (mapping_constraints[ current_batch.constraints[p] ].keep &&
					 				!mapping_constraints[ current_batch.constraints[p] ].degenerate)
					 			{ // this guy's generalized, try comparing it: 			
						 			// we will compare the real constraints with all of the wug-time constraints,
						 			// which live at:
									// mapping_constraints[firstWugtimeConstraint] to mapping_constraints[newestConstraint]					   
						 			
						 			// so: the inner loop (j) is from firstWugtimeConstraint to newestConstraint,
						 			// and the comparison constraint is: mapping_constraints[j]
						 			
						 			// NOTE: we only do this on generalized constraints!  
						 			// so: since the new wug-time batch starts with a hypothetical degenerate,
						 			// we actually should start j off one AFTER firstWugtimeConstraint

						 			for (int j = (firstWugtimeConstraint + 1); j<newestconstraint; j++)
						 			{

						 				if (mapping_constraints[j].keep &&
						 					!mapping_constraints[j].degenerate)
						 				{	// this guy is also generalized, assess specificity rel.
						 					switch (assessSpecificity( current_batch.constraints[p], j))
						 					{
						 						case 0: // neither constraint more specific: do nothing
						 								break;
						 						case 1: // constraint i is more specific -- 
						 								allegation.calculate( (mapping_constraints[j].hits - mapping_constraints[current_batch.constraints[p]].hits),
						 													  (mapping_constraints[j].scope - mapping_constraints[current_batch.constraints[p]].scope) );


						 								if (allegation.p95 < mapping_constraints[j].impugnedConfidence.p95)
						 								{
						 									mapping_constraints[j].impugnedConfidence.set_p95(allegation.p95);
						 								}
						 								if (allegation.p90 < mapping_constraints[j].impugnedConfidence.p90)
						 								{
						 									mapping_constraints[j].impugnedConfidence.set_p90(allegation.p90);
						 								}
						 								if (allegation.p75 < mapping_constraints[j].impugnedConfidence.p75)
						 								{
						 									mapping_constraints[j].impugnedConfidence.set_p75(allegation.p75);
															mapping_constraints[j].setImpugner(current_batch.constraints[p] );
						 								}
						 								break;
						 						case 2: // constraint j is more specific
														// BUT, since the new guys don't really exist, they don't impugn
						 								break;
						 					} // end of switch on specificity relation
						 				} // fi
						 			} // end loop through comparison constraints
					 			} // fi
					 		} // end loop through constraints		
							// DONE IMPUGNING THE WUG-TIME CONSTRAINTS
						} // end of if (useImpugnment)


						// now we compute their lower confidence and overall confidence
						  for (int u = firstWugtimeConstraint; u < newestconstraint; u++)
						  {
								mapping_constraints[u].calculateLowerConfidence( );
								mapping_constraints[u].calculateOverallConfidence( useImpugnment );

								if (saveFreqFile || saveWeightedByFreqFile)
								{
									mapping_constraints[u].calculateRelFrequency();
									mapping_constraints[u].calculateWeightedByFreq();
								}
								if (saveWeightedFile)
									mapping_constraints[u].calculateWeightedConfidence(.2);
						  }

						// now, we'll find the best wug-time constraint
						for (int u = firstWugtimeConstraint; u<newestconstraint; u++) // 8/22/00 was firstWC + 1 ?!?!
						{
							if ( mapping_constraints[u].overallconfidence > val_to_beat)
							{
								// this guy is better than anything so far (bona fide or wug)
								bestWugtimeConstraints[0] = u;
								val_to_beat = mapping_constraints[u].overallconfidence;
							} // end of if this guy qualifies
						} // end of looping through wug time constraints

/*
this block is lots of extra work if we really only want the top one
						// NOW: we know that all of these constraints apply to the test form, so 
						// all we have to do to get the 10 best outputs is sort them by decreasing
						// overall confidence, and then apply the first 10:
						for (int u = (firstWugtimeConstraint+1); u<newestconstraint; u++)
						{
							if ( bestWugtimeConstraints[9] == 0 ||
						 		mapping_constraints[u].overallconfidence > mapping_constraints[bestWugtimeConstraints[9]].overallconfidence)
							{
								// this guy is one of the 10 best so far;
								bestWugtimeConstraints[9] = u;
								// now sort him into the correct place:
								for (int k=9; k>0; k--)
								{
									if( bestWugtimeConstraints[k-1] == 0 || 
										mapping_constraints[bestWugtimeConstraints[k]].overallconfidence > mapping_constraints[bestWugtimeConstraints[k-1]].overallconfidence)
									{
										// move this guy up:
										hold = bestWugtimeConstraints[k-1];
										bestWugtimeConstraints[k-1] = bestWugtimeConstraints[k];
										bestWugtimeConstraints[k] = hold;
									}
									// if the next one is not smaller, this one has gone as far as it can:
									else break;
								} // end of sorting new guy into place
							} // end of if this guy qualifies
						} // end of looping through wug time constraints
*/

//						output.append((w+1)).append("\t").append(localchangescounter).append("\t0\t" ).append( learnerTask.test_forms[w] ).append( "\t->\t").append(String.valueOf(UR)).append("\t->\t" ).append( localoutput ).append( "\t" ).append( current_change ).append( "\t" ).append( change_location ).append(" (wug-time, ").append( (newestconstraint-firstWugtimeConstraint)).append(" constraints)\n");
											   


						// Now, finding 10 outputs should be as easy as pulling off the top 10 constraints
						// (if there are so many, which I guess there usually will be)
//grah! a bug, i think!  should be counting 1-10 in wugtime constraints, should be pulling from bestWugtimeConstraints
//						for (int u = (firstWugtimeConstraint); u < (firstWugtimeConstraint + 10) && u < newestconstraint; u++)

// anyway, just going to do 1 for now
						if (bestWugtimeConstraints[0] > 0)
						{
							int u = bestWugtimeConstraints[0];

							change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), u);
							if (change_location >= 0)
							{
//								output.append((w+1)).append("\t").append(localchangescounter).append("\t").append(localoutputscounter).append("\t"
//														).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
//								if (localoutputscounter == 1)
//								{
									sumoutput.append((w+1)).append("\t").append(localchangescounter).append("\t").append(localoutputscounter).append("\t"
											).append(learnerTask.test_forms[w]).append("\t->\t").append(localoutput).append("\tby\t");
//								}

								if (mapping_constraints[u].A.length == 0)
								{
//									output.append("[]");
//									if (localoutputscounter == 1)
										sumoutput.append("[]");
								}
								else
//									output.append(String.valueOf(mapping_constraints[u].A)) ;
//									if (localoutputscounter == 1)
										sumoutput.append(String.valueOf(mapping_constraints[u].A)) ;
										
//								output.append( "\t->\t");
//								if (localoutputscounter == 1)
									sumoutput.append( "\t->\t");
								
								if (mapping_constraints[u].B.length == 0)
								{	// output.append("[]");
//									if (localoutputscounter == 1)
										sumoutput.append("[]");
								}
								else
								{
//									output.append( String.valueOf(mapping_constraints[u].B));
//									if (localoutputscounter == 1)
										sumoutput.append( String.valueOf(mapping_constraints[u].B));
									
								}
								
//								output.append("\t" ).append( current_change);
//								if (localoutputscounter == 1)
									sumoutput.append("\t").append(current_change);
								
//								output.append("\t/\t");
//								if (localoutputscounter == 1)
									sumoutput.append("\t/\t");
								
								// P residue
								if (mapping_constraints[u].P_residue)
								{
//									output.append("X");
//									if (localoutputscounter == 1)
										sumoutput.append("X");
								}
//								output.append("\t");
//								if (localoutputscounter == 1)
									sumoutput.append("\t");
								
								// P features
							    if (mapping_constraints[u].P_features_active)
							    {
									// we want to print a list of the compatible segments, for human readability
									output.append(compatibleSegments( u, true ));
//									if (localoutputscounter == 1)
										sumoutput.append(compatibleSegments( u, true ));
							    }
							    // P__Q
//								output.append("\t" ).append( String.valueOf(mapping_constraints[u].P) ).append( "\t___\t"
//												  ).append( String.valueOf(mapping_constraints[u].Q) ).append( "\t");
								
//								if (localoutputscounter == 1)
//
									sumoutput.append("\t").append(String.valueOf(mapping_constraints[u].P)).append("\t___\t").append(String.valueOf(mapping_constraints[u].Q)).append("\t");
								
								// Q features
								if (mapping_constraints[u].Q_features_active)
								{
//								  	output.append(compatibleSegments( u, false ));
//									if (localoutputscounter == 1)
										sumoutput.append(compatibleSegments( u, false ));
								}
//								output.append("\t");
//								if (localoutputscounter == 1)
									sumoutput.append("\t");
								// Q residue
								if (mapping_constraints[u].Q_residue)
								{
//								  output.append("Y");
//								  if (localoutputscounter == 1)
								  	sumoutput.append("Y");
								  
								}
//								output.append("\t");
//								if (localoutputscounter == 1)
									sumoutput.append("\t");
									
								// an indication of wug-time status:
//									output.append("W\t");
//									if (localoutputscounter == 1)
										sumoutput.append("W\t");

									
								// scope and reliability						
//								output.append( mapping_constraints[u].scope
//												).append( "\t" ).append( mapping_constraints[u].hits).append( "\t");
//								if (localoutputscounter == 1)
									sumoutput.append( mapping_constraints[u].scope
												).append("\t").append(mapping_constraints[u].hits).append("\t");
								
								// the token frequency
//								output.append( mapping_constraints[u].hits_frequency ).append( "\t");
//								if (localoutputscounter == 1)
									sumoutput.append( mapping_constraints[u].hits_frequency).append("\t");
								// rel token frequency for this guy:
//								output.append( (Math.floor(mapping_constraints[u].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
//								if (localoutputscounter == 1)
									sumoutput.append(  (Math.floor(mapping_constraints[u].rel_frequency * 1000 + .5) / 1000)).append("\t");
								
								// now output the overall confidence for this guy:
//								output.append( (Math.floor(mapping_constraints[u].overallconfidence * 1000 + .5) / 1000) ).append( "\t");
//								if (localoutputscounter == 1)
									sumoutput.append( (Math.floor(mapping_constraints[u].overallconfidence * 1000 + .5) / 1000)).append("\t");

								// and weighted by freq
//								output.append( (Math.floor( mapping_constraints[u].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
//								if (localoutputscounter == 1)
									sumoutput.append( (Math.floor( mapping_constraints[u].weightedByFreq * 1000 + .5) / 1000 )).append("\t");

								// and weighted by length:
//								output.append( (Math.floor(mapping_constraints[u].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");
//								if (localoutputscounter == 1)
									sumoutput.append( (Math.floor(mapping_constraints[u].weightedConfidence * 1000 + .5) / 1000)).append("\t");


								// and the particular confidences
/*								output.append( (Math.floor(mapping_constraints[u].lowerConfidence.p75 * 1000 + .5) / 1000 )
									 ).append( "\t" ).append( (Math.floor( mapping_constraints[u].lowerConfidence.p90 * 1000 + .5) / 1000 )
									 ).append( "\t" ).append( (Math.floor(mapping_constraints[u].lowerConfidence.p95 * 1000 + .5 ) /1000 )
									 ).append( "\t" ).append( (Math.floor(mapping_constraints[u].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
									 ).append( "\t" ).append( (Math.floor(mapping_constraints[u].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
									 ).append( "\t" ).append( (Math.floor(mapping_constraints[u].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
									 ).append( "\t" ).append( u ).append( "\t");	 
*/									 
//								if (localoutputscounter == 1)
//								{
									sumoutput.append( (Math.floor(mapping_constraints[u].lowerConfidence.p75 * 1000 + .5) / 1000 )
									 ).append("\t").append((Math.floor( mapping_constraints[u].lowerConfidence.p90 * 1000 + .5) / 1000 )
									 ).append("\t").append((Math.floor(mapping_constraints[u].lowerConfidence.p95 * 1000 + .5 ) /1000 )
									 ).append("\t").append((Math.floor(mapping_constraints[u].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
									 ).append("\t").append((Math.floor(mapping_constraints[u].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
									 ).append("\t").append((Math.floor(mapping_constraints[u].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
									 ).append("\t").append(u).append("\t");	
//								}
								
								// next come the related forms:
								
								for (int f = 1; f< related_forms[u][0]; f++)
								{
									if (f > 1)
									{
//										output.append(", ");
//										if (localoutputscounter == 1)
											sumoutput.append(", ");
									}
//									output.append( String.valueOf(known_forms[ related_forms[u][f] ][0]));
//									if (localoutputscounter == 1)
										sumoutput.append( String.valueOf(known_forms[ related_forms[u][f] ][0]));
									
									
								}
//								output.append("\t");
//								if (localoutputscounter == 1)
									sumoutput.append("\t");

								// next come the exceptions to this constraint:
								for (int f = 1; f< exceptions[u][0]; f++)
								{
									if (f > 1)
									{
//										output.append(", ");
//										if (localoutputscounter == 1)
											sumoutput.append(", ");
									}	
//									output.append( String.valueOf(known_forms[ exceptions[u][f] ][0]));
//									if (localoutputscounter == 1)
										sumoutput.append( String.valueOf(known_forms[ exceptions[u][f] ][0]));
									
								}

//								output.append("\t");
//								if (localoutputscounter == 1)
									sumoutput.append("\t");
								
								// now print out the impugner info
								if (mapping_constraints[u].impugner > -1)
								{
//									output.append( mapping_constraints[u].impugner ).append( "\t" 
//													).append( mapping_constraints[mapping_constraints[u].impugner].scope ).append( "\t"
//													).append( mapping_constraints[mapping_constraints[u].impugner].hits ).append( "\t");
//									output.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[u].impugner) ).append( "\n");
//									if (localoutputscounter == 1)
//									{
										sumoutput.append( mapping_constraints[u].impugner).append("\t" 
													).append(mapping_constraints[mapping_constraints[u].impugner].scope).append("\t"
													).append(mapping_constraints[mapping_constraints[u].impugner].hits).append("\t");
										sumoutput.append("Impugned by constraint ").append(printConstraint(mapping_constraints[u].impugner)).append("\n");
//									}
									
								
								}
								else{
//								 	output.append("\t\t\n");
//									if (localoutputscounter == 1)
										sumoutput.append("\t\t\n");
								}
																
								localoutputscounter++;
								
							} // fi change_location >=0
						} // end loop thru first 10 constraints
						// END OF WUG-TIME LEARNING to derive this form
						// now we need to forget that this ever happened:
//						newestconstraint = firstWugtimeConstraint;
								
					} // end of if(wug_time_learning)

					// here used to be code to output the most general constraints, saved in file findmostgeneral.txt


						summaryfile.write(sumoutput.toString());
						sumoutput = new StringBuffer(500);


						if (saveC75File)
						{
						findbestc75:{
							current_batch = (ConstraintBatch) changes_by_c75.get( current_change );
							
							localoutputscounter = 1;
							i=1;	
							val_to_beat = -50;					


							while (localoutputscounter <= 1 && i < current_batch.size() )  // used to be <=
							{
//								try{ // find the best c75 for this change
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
									if (change_location >=0)
									{
										// localoutput was already built above, no need to redo
										c=current_batch.constraints[i];
										val_to_beat = mapping_constraints[c].lowerConfidence.p75;

										c75output.append((w+1)).append("\t").append(localchangescounter).append("\tc75").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");

										if (mapping_constraints[c].A.length == 0)
											c75output.append("[]");
										else
											c75output.append(String.valueOf(mapping_constraints[c].A)) ;
										c75output.append( "\t->\t");
										if (mapping_constraints[c].B.length == 0)
											c75output.append("[]");
										else
											c75output.append( String.valueOf(mapping_constraints[c].B));
											
										c75output.append("\t" ).append( current_change );	
											
										c75output.append("\t/\t");
										// P residue
										if (mapping_constraints[c].P_residue)
										{
											c75output.append("X");
										}
										c75output.append("\t");
										// P features
									    if (mapping_constraints[c].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		c75output.append("{");
											c75output.append(compatibleSegments( c, true ));
									//		c75output.append("}");
									    }
									    // P__Q
										c75output.append("\t" ).append( String.valueOf(mapping_constraints[c].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[c].Q) ).append( "\t");
										// Q features
										if (mapping_constraints[c].Q_features_active)
										{
										//			c75output.append("{");
										  	c75output.append(compatibleSegments( c, false ));
										//		   	c75output.append("}");
										}
										c75output.append("\t");
										// Q residue
										if (mapping_constraints[c].Q_residue)
										{
										  c75output.append("Y");
										}
										c75output.append("\t");
										
										// an indication of doppelgaengerhood	
										if (mapping_constraints[c].doppelgaenger)
										{
											c75output.append("D\t");
										}
										else
										{
											c75output.append("\t");
										}
										
										
										// scope and reliability						
										c75output.append( mapping_constraints[c].scope
												).append( "\t" ).append( mapping_constraints[c].hits ).append( "\t");
												
										// the token frequency 
										c75output.append( mapping_constraints[c].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										c75output.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
												
										// now output the overall confidence for this guy:
										c75output.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										c75output.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
										// and weighted by length:
										c75output.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");

										// and the particular confidences
										c75output.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( c ).append( "\t");

										// next come the related forms:
										for (int f = 1; f< related_forms[c][0]; f++)
										{
											if (f > 1)
												c75output.append(",");
											c75output.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
											
										}
										c75output.append("\t");
											 
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[c][0]; f++)
										{
											if (f > 1)
												c75output.append(",");
											c75output.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
											
										}
										c75output.append("\t");

										if (mapping_constraints[c].impugner > -1)
										{
											c75output.append( mapping_constraints[c].impugner ).append( "\t");
											c75output.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[c].impugner) ).append( "\n");
										}
										else c75output.append("\t\t\n");
										localoutputscounter++;
									}
									i++;								
//							} // end of try
						} // end of while (loop through i's)							
							// now we also want to know what the best c75 among wug-time constraints is
							if (wug_learning_on)
							{
								// find wug constraint with highest c75
								highest_wug_c75 = 0;
								for (int u = (firstWugtimeConstraint + 1); u < newestconstraint; u++)
								{
									if (mapping_constraints[u].lowerConfidence.p75 > val_to_beat)
										highest_wug_c75 = u;
										val_to_beat = mapping_constraints[u].lowerConfidence.p75;
								}
								
								// now if there was one (which I guess must always be the case), use it to derive the output:
								if (highest_wug_c75 > 0)
								{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), highest_wug_c75);
									if (change_location >= 0)
									{
										c75output.append((w+1)).append("\t").append(localchangescounter).append("\tc75").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
										if (mapping_constraints[highest_wug_c75].A.length == 0)
										{
											c75output.append("[]");
										}
										else
											c75output.append(String.valueOf(mapping_constraints[highest_wug_c75].A)) ;
												
										c75output.append( "\t->\t");
										
										if (mapping_constraints[highest_wug_c75].B.length == 0)
										{	c75output.append("[]");
										}
										else
										{
											c75output.append( String.valueOf(mapping_constraints[highest_wug_c75].B));											
										}
										
										c75output.append("\t" ).append( current_change);
										c75output.append("\t/\t");
										
										// P residue
										if (mapping_constraints[highest_wug_c75].P_residue)
										{
											c75output.append("X");
										}
										c75output.append("\t");
										
										// P features
									    if (mapping_constraints[highest_wug_c75].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		c75output.append("{");
											c75output.append(compatibleSegments( highest_wug_c75, true ));
									//		c75output.append("}");
									    }
									    // P__Q
										c75output.append("\t" ).append( String.valueOf(mapping_constraints[highest_wug_c75].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[highest_wug_c75].Q) ).append( "\t");
																				
										// Q features
										if (mapping_constraints[highest_wug_c75].Q_features_active)
										{
										//			c75output.append("{");
										  	c75output.append(compatibleSegments( highest_wug_c75, false ));
										//		   	c75output.append("}");
										}
										c75output.append("\t");
										// Q residue
										if (mapping_constraints[highest_wug_c75].Q_residue)
										{
										  c75output.append("Y");										  
										}
										c75output.append("\t");
											
										// an indication of wug-time status:
											c75output.append("W\t");
											
										// scope and reliability						
										c75output.append( mapping_constraints[highest_wug_c75].scope
														).append( "\t" ).append( mapping_constraints[highest_wug_c75].hits).append( "\t");
										
										// the token frequency
										c75output.append( mapping_constraints[highest_wug_c75].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										c75output.append( (Math.floor(mapping_constraints[highest_wug_c75].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
										
										// now output the overall confidence for this guy:
										c75output.append( (Math.floor(mapping_constraints[highest_wug_c75].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										c75output.append( (Math.floor( mapping_constraints[highest_wug_c75].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");

										// and weighted by length:
										c75output.append( (Math.floor(mapping_constraints[highest_wug_c75].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");


										// and the particular confidences
										c75output.append( (Math.floor(mapping_constraints[highest_wug_c75].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[highest_wug_c75].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_c75].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_c75].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_c75].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_c75].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( highest_wug_c75 ).append( "\t");	 
										
										// next come the related forms:
										for (int f = 1; f< related_forms[highest_wug_c75][0]; f++)
										{
											if (f > 1)
											{
												c75output.append(", ");
											}
											c75output.append( String.valueOf(known_forms[ related_forms[highest_wug_c75][f] ][0]));											
											
										}
										c75output.append("\t");
										
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[highest_wug_c75][0]; f++)
										{
											if (f > 1)
											{
												c75output.append(", ");
											}	
											c75output.append( String.valueOf(known_forms[ exceptions[highest_wug_c75][f] ][0]));											
										}
										c75output.append("\t");
										
										// now print out the impugner info
										if (mapping_constraints[highest_wug_c75].impugner > -1)
										{
											c75output.append( mapping_constraints[highest_wug_c75].impugner ).append( "\t" 
															).append( mapping_constraints[mapping_constraints[highest_wug_c75].impugner].scope ).append( "\t"
															).append( mapping_constraints[mapping_constraints[highest_wug_c75].impugner].hits ).append( "\t");
											c75output.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[highest_wug_c75].impugner) ).append( "\n");											
										}
										else{
										 	c75output.append("\t\t\n");
										}
										localoutputscounter++;
										
									} // fi change_location >=0
									
								} // fi  (highest_wug_c75 > 0)
							} // fi (wug_learning_on)	
						} // end of findbestc75 block

						c75file.write(c75output.toString());
						c75output = new StringBuffer(500);

						} // end if saveC75File
						if (saveHitsFile)
						{
						findbesthits:{
							current_batch = (ConstraintBatch) changes_by_hits.get( current_change );
							
							localoutputscounter = 1;
							i=1;	
							val_to_beat = -50;					


							while (localoutputscounter <= 1 && i < current_batch.size() ) // used to be <=
							{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
									if (change_location >=0)
									{
										// localoutput was already built above, no need to redo
										c=current_batch.constraints[i];
										val_to_beat = mapping_constraints[c].hits;
										
										hitsoutput.append((w+1)).append("\t").append(localchangescounter).append("\thits").append(localoutputscounter).append("\t"
																).append(learnerTask.test_forms[w]).append("\t->\t").append(localoutput).append("\tby\t");

										if (mapping_constraints[c].A.length == 0)
											hitsoutput.append("[]");
										else
											hitsoutput.append(String.valueOf(mapping_constraints[c].A)) ;
										hitsoutput.append( "\t->\t");
										if (mapping_constraints[c].B.length == 0)
											hitsoutput.append("[]");
										else
											hitsoutput.append( String.valueOf(mapping_constraints[c].B));
										hitsoutput.append("\t").append(current_change);	
											
										hitsoutput.append("\t/\t");
										// P residue
										if (mapping_constraints[c].P_residue)
										{
											hitsoutput.append("X");
										}
										hitsoutput.append("\t");
										// P features
									    if (mapping_constraints[c].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		hitsoutput.append("{");
											hitsoutput.append(compatibleSegments( c, true ));
									//		hitsoutput.append("}");
									    }
									    // P__Q
										hitsoutput.append("\t").append(String.valueOf(mapping_constraints[c].P)).append("\t___\t"
														  ).append(String.valueOf(mapping_constraints[c].Q)).append("\t");
										// Q features
										if (mapping_constraints[c].Q_features_active)
										{
										//			hitsoutput.append("{");
										  	hitsoutput.append(compatibleSegments( c, false ));
										//		   	hitsoutput.append("}");
										}
										hitsoutput.append("\t");
										// Q residue
										if (mapping_constraints[c].Q_residue)
										{
										  hitsoutput.append("Y");
										}
										hitsoutput.append("\t");
										
										// an indication of doppelgaengerhood	
										if (mapping_constraints[c].doppelgaenger)
										{
											hitsoutput.append("D\t");
										}
										else
										{
											hitsoutput.append("\t");
										}
										
										
										// scope and reliability						
										hitsoutput.append( mapping_constraints[c].scope
												).append("\t").append(mapping_constraints[c].hits).append("\t");

										// the token frequency
										hitsoutput.append( mapping_constraints[c].hits_frequency).append("\t");
										// rel token frequency for this guy:
										hitsoutput.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000)).append("\t");
										
										// now output the overall confidence for this guy:
										hitsoutput.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000)).append("\t");
										// and weighted by freq
										hitsoutput.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 )).append("\t");
										// and weighted by length:
										hitsoutput.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 )).append("\t");

										// and the particular confidences
										hitsoutput.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append("\t").append((Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append("\t").append((Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append("\t").append((Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append("\t").append((Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append("\t").append((Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append("\t").append(c).append("\t");

										// next come the related forms:
										for (int f = 1; f< related_forms[c][0]; f++)
										{
											if (f > 1)
												hitsoutput.append(",");
											hitsoutput.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
											
										}
										hitsoutput.append("\t");
											 
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[c][0]; f++)
										{
											if (f > 1)
												hitsoutput.append(",");
											hitsoutput.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
											
										}
										hitsoutput.append("\t");

										if (mapping_constraints[c].impugner > -1)
										{
											hitsoutput.append( mapping_constraints[c].impugner).append("\t");
											hitsoutput.append("Impugned by constraint ").append(printConstraint(mapping_constraints[c].impugner)).append("\n");
										}
										else hitsoutput.append("\t\t\n");
										localoutputscounter++;
									}
									i++;								
							} // end of while (loop through i's)
							
							// now we also want to know what the best hits among wug-time constraints is
							if (wug_learning_on)
							{
								// find wug constraint with highest hits
								highest_wug_hits = 0;
								for (int u = (firstWugtimeConstraint + 1); u < newestconstraint; u++)
								{
									if (mapping_constraints[u].hits > val_to_beat)
										highest_wug_hits = u;
										val_to_beat = mapping_constraints[u].hits;
								}
								
								// now if there was one (which I guess must always be the case), use it to derive the output:
								if (highest_wug_hits > 0)
								{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), highest_wug_hits);
									if (change_location >= 0)
									{
										hitsoutput.append((w+1)).append("\t").append(localchangescounter).append("\thits").append(localoutputscounter).append("\t"
																).append(learnerTask.test_forms[w]).append("\t->\t").append(localoutput).append("\tby\t");
										if (mapping_constraints[highest_wug_hits].A.length == 0)
										{
											hitsoutput.append("[]");
										}
										else
											hitsoutput.append(String.valueOf(mapping_constraints[highest_wug_hits].A)) ;
												
										hitsoutput.append( "\t->\t");
										
										if (mapping_constraints[highest_wug_hits].B.length == 0)
										{	hitsoutput.append("[]");
										}
										else
										{
											hitsoutput.append( String.valueOf(mapping_constraints[highest_wug_hits].B));											
										}
										
										hitsoutput.append("\t").append(current_change);
										hitsoutput.append("\t/\t");
										
										// P residue
										if (mapping_constraints[highest_wug_hits].P_residue)
										{
											hitsoutput.append("X");
										}
										hitsoutput.append("\t");
										
										// P features
									    if (mapping_constraints[highest_wug_hits].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		hitsoutput.append("{");
											hitsoutput.append(compatibleSegments( highest_wug_hits, true ));
									//		hitsoutput.append("}");
									    }
									    // P__Q
										hitsoutput.append("\t").append(String.valueOf(mapping_constraints[highest_wug_hits].P)).append("\t___\t"
														  ).append(String.valueOf(mapping_constraints[highest_wug_hits].Q)).append( "\t");
																				
										// Q features
										if (mapping_constraints[highest_wug_hits].Q_features_active)
										{
										//			hitsoutput.append("{");
										  	hitsoutput.append(compatibleSegments( highest_wug_hits, false ));
										//		   	hitsoutput.append("}");
										}
										hitsoutput.append("\t");
										// Q residue
										if (mapping_constraints[highest_wug_hits].Q_residue)
										{
										  hitsoutput.append("Y");										  
										}
										hitsoutput.append("\t");
											
										// an indication of wug-time status:
											hitsoutput.append("W\t");
											
										// scope and reliability						
										hitsoutput.append( mapping_constraints[highest_wug_hits].scope
														).append( "\t" ).append( mapping_constraints[highest_wug_hits].hits).append( "\t");
										
										// the token frequency
										hitsoutput.append( mapping_constraints[highest_wug_hits].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										hitsoutput.append( (Math.floor(mapping_constraints[highest_wug_hits].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
										
										// now output the overall confidence for this guy:
										hitsoutput.append( (Math.floor(mapping_constraints[highest_wug_hits].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										hitsoutput.append( (Math.floor( mapping_constraints[highest_wug_hits].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");

										// and weighted by length:
										hitsoutput.append( (Math.floor(mapping_constraints[highest_wug_hits].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");


										// and the particular confidences
										hitsoutput.append( (Math.floor(mapping_constraints[highest_wug_hits].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[highest_wug_hits].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_hits].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_hits].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_hits].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_hits].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( highest_wug_hits ).append( "\t");	 
										
										// next come the related forms:
										for (int f = 1; f< related_forms[highest_wug_hits][0]; f++)
										{
											if (f > 1)
											{
												hitsoutput.append(", ");
											}
											hitsoutput.append( String.valueOf(known_forms[ related_forms[highest_wug_hits][f] ][0]));											
											
										}
										hitsoutput.append("\t");
										
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[highest_wug_hits][0]; f++)
										{
											if (f > 1)
											{
												hitsoutput.append(", ");
											}	
											hitsoutput.append( String.valueOf(known_forms[ exceptions[highest_wug_hits][f] ][0]));											
										}
										hitsoutput.append("\t");
										
										// now print out the impugner info
										if (mapping_constraints[highest_wug_hits].impugner > -1)
										{
											hitsoutput.append( mapping_constraints[highest_wug_hits].impugner ).append( "\t" 
															).append( mapping_constraints[mapping_constraints[highest_wug_hits].impugner].scope ).append( "\t"
															).append( mapping_constraints[mapping_constraints[highest_wug_hits].impugner].hits ).append( "\t");
											hitsoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[highest_wug_hits].impugner) ).append( "\n");											
										}
										else{
										 	hitsoutput.append("\t\t\n");
										}
										localoutputscounter++;
										
									} // fi change_location >=0
									
								} // fi  (highest_wug_hits > 0)
							} // fi (wug_learning_on)

							
						} // end of findbesthits block	
						
							hitsfile.write(hitsoutput.toString());
							hitsoutput = new StringBuffer(500);
						
										
						} // end if saveHitsFile
						if (saveC90File)
						{
						findbestc90:{
							current_batch = (ConstraintBatch) changes_by_c90.get( current_change );
							
							localoutputscounter = 1;
							i=1;
							val_to_beat = -50;				


							while (localoutputscounter <= 1 && i < current_batch.size() ) // used to be <=
							{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
									if (change_location >=0)
									{
										// localoutput was already built above, no need to redo
										c=current_batch.constraints[i];
										val_to_beat = mapping_constraints[c].lowerConfidence.p90;
										
										c90output.append((w+1)).append("\t").append(localchangescounter).append("\tc90").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");

										if (mapping_constraints[c].A.length == 0)
											c90output.append("[]");
										else
											c90output.append(String.valueOf(mapping_constraints[c].A)) ;
										c90output.append( "\t->\t");
										if (mapping_constraints[c].B.length == 0)
											c90output.append("[]");
										else
											c90output.append( String.valueOf(mapping_constraints[c].B));
											
										c90output.append("\t" ).append( current_change);	
										c90output.append("\t/\t");
										// P residue
										if (mapping_constraints[c].P_residue)
										{
											c90output.append("X");
										}
										c90output.append("\t");
										// P features
									    if (mapping_constraints[c].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		c90output.append("{");
											c90output.append(compatibleSegments( c, true ));
									//		c90output.append("}");
									    }
									    // P__Q
										c90output.append("\t" ).append( String.valueOf(mapping_constraints[c].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[c].Q) ).append( "\t");
										// Q features
										if (mapping_constraints[c].Q_features_active)
										{
										//			c90output.append("{");
										  	c90output.append(compatibleSegments( c, false ));
										//		   	c90output.append("}");
										}
										c90output.append("\t");
										// Q residue
										if (mapping_constraints[c].Q_residue)
										{
										  c90output.append("Y");
										}
										c90output.append("\t");
										
										// an indication of doppelgaengerhood	
										if (mapping_constraints[c].doppelgaenger)
										{
											c90output.append("D\t");
										}
										else
										{
											c90output.append("\t");
										}
										
										
										// scope and reliability						
										c90output.append( mapping_constraints[c].scope
												).append( "\t" ).append( mapping_constraints[c].hits ).append( "\t");

										// the token frequency
										c90output.append( mapping_constraints[c].hits_frequency ).append( "\t");												
										// rel token frequency for this guy:
										c90output.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000) ).append( "\t");

										// now output the overall confidence for this guy:
										c90output.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000) ).append( "\t");
										// and weighted by freq
										c90output.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
										// and weighted by length:
										c90output.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");

										// and the particular confidences
										c90output.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( c ).append( "\t");

										// next come the related forms:
										for (int f = 1; f< related_forms[c][0]; f++)
										{
											if (f > 1)
												c90output.append(",");
											c90output.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
											
										}
										c90output.append("\t");
											 
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[c][0]; f++)
										{
											if (f > 1)
												c90output.append(",");
											c90output.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
											
										}
										c90output.append("\t");

										if (mapping_constraints[c].impugner > -1)
										{
											c90output.append( mapping_constraints[c].impugner ).append( "\t");
											c90output.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[c].impugner) ).append( "\n");
										}
										else c90output.append("\t\t\n");
										localoutputscounter++;
									}
									i++;
							} // end of while (loop through i's)
							
							// now we also want to know what the best c90 among wug-time constraints is
							if (wug_learning_on)
							{
								// find wug constraint with highest c90
								highest_wug_c90 = 0;
								for (int u = (firstWugtimeConstraint + 1); u < newestconstraint; u++)
								{
									if (mapping_constraints[u].lowerConfidence.p90 > val_to_beat)
										highest_wug_c90 = u;
										val_to_beat = mapping_constraints[u].lowerConfidence.p90;
								}
								
								// now if there was one (which I guess must always be the case), use it to derive the output:
								if (highest_wug_c90 > 0)
								{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), highest_wug_c90);
									if (change_location >= 0)
									{
										c90output.append((w+1)).append("\t").append(localchangescounter).append("\tc90").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
										if (mapping_constraints[highest_wug_c90].A.length == 0)
										{
											c90output.append("[]");
										}
										else
											c90output.append(String.valueOf(mapping_constraints[highest_wug_c90].A)) ;
												
										c90output.append( "\t->\t");
										
										if (mapping_constraints[highest_wug_c90].B.length == 0)
										{	c90output.append("[]");
										}
										else
										{
											c90output.append( String.valueOf(mapping_constraints[highest_wug_c90].B));											
										}
										
										c90output.append("\t" ).append( current_change);
										c90output.append("\t/\t");
										
										// P residue
										if (mapping_constraints[highest_wug_c90].P_residue)
										{
											c90output.append("X");
										}
										c90output.append("\t");
										
										// P features
									    if (mapping_constraints[highest_wug_c90].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		c90output.append("{");
											c90output.append(compatibleSegments( highest_wug_c90, true ));
									//		c90output.append("}");
									    }
									    // P__Q
										c90output.append("\t" ).append( String.valueOf(mapping_constraints[highest_wug_c90].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[highest_wug_c90].Q) ).append( "\t");
																				
										// Q features
										if (mapping_constraints[highest_wug_c90].Q_features_active)
										{
										//			c90output.append("{");
										  	c90output.append(compatibleSegments( highest_wug_c90, false ));
										//		   	c90output.append("}");
										}
										c90output.append("\t");
										// Q residue
										if (mapping_constraints[highest_wug_c90].Q_residue)
										{
										  c90output.append("Y");										  
										}
										c90output.append("\t");
											
										// an indication of wug-time status:
											c90output.append("W\t");
											
										// scope and reliability						
										c90output.append( mapping_constraints[highest_wug_c90].scope
														).append( "\t" ).append( mapping_constraints[highest_wug_c90].hits).append( "\t");
										
										// the token frequency
										c90output.append( mapping_constraints[highest_wug_c90].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										c90output.append( (Math.floor(mapping_constraints[highest_wug_c90].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
										
										// now output the overall confidence for this guy:
										c90output.append( (Math.floor(mapping_constraints[highest_wug_c90].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										c90output.append( (Math.floor( mapping_constraints[highest_wug_c90].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");

										// and weighted by length:
										c90output.append( (Math.floor(mapping_constraints[highest_wug_c90].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");


										// and the particular confidences
										c90output.append( (Math.floor(mapping_constraints[highest_wug_c90].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[highest_wug_c90].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_c90].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_c90].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_c90].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_c90].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( highest_wug_c90 ).append( "\t");	 
										
										// next come the related forms:
										for (int f = 1; f< related_forms[highest_wug_c90][0]; f++)
										{
											if (f > 1)
											{
												c90output.append(", ");
											}
											c90output.append( String.valueOf(known_forms[ related_forms[highest_wug_c90][f] ][0]));											
											
										}
										c90output.append("\t");
										
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[highest_wug_c90][0]; f++)
										{
											if (f > 1)
											{
												c90output.append(", ");
											}	
											c90output.append( String.valueOf(known_forms[ exceptions[highest_wug_c90][f] ][0]));											
										}
										c90output.append("\t");
										
										// now print out the impugner info
										if (mapping_constraints[highest_wug_c90].impugner > -1)
										{
											c90output.append( mapping_constraints[highest_wug_c90].impugner ).append( "\t" 
															).append( mapping_constraints[mapping_constraints[highest_wug_c90].impugner].scope ).append( "\t"
															).append( mapping_constraints[mapping_constraints[highest_wug_c90].impugner].hits ).append( "\t");
											c90output.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[highest_wug_c90].impugner) ).append( "\n");											
										}
										else{
										 	c90output.append("\t\t\n");
										}
										localoutputscounter++;
										
									} // fi change_location >=0
									
								} // fi  (highest_wug_c90 > 0)
							} // fi (wug_learning_on)
							
						} // end of findbestc90 block
						c90file.write(c90output.toString());
						c90output = new StringBuffer(500);
						
											
						} // end if saveC90File
						if (saveRawRelFile)
						{
						findbestraw:{
							current_batch = (ConstraintBatch) changes_by_raw_rel.get( current_change );
							
							localoutputscounter = 1;
							i=1;			
							val_to_beat = -50;			


							while (localoutputscounter <= 1 && i < current_batch.size() ) // used to be <=
							{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
									if (change_location >=0)
									{
										// localoutput was already built above, no need to redo
										c=current_batch.constraints[i];
										val_to_beat = mapping_constraints[c].reliability;
										rawoutput.append((w+1)).append("\t").append(localchangescounter).append("\traw").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");

										if (mapping_constraints[c].A.length == 0)
											rawoutput.append("[]");
										else
											rawoutput.append(String.valueOf(mapping_constraints[c].A)) ;
										rawoutput.append( "\t->\t");
										if (mapping_constraints[c].B.length == 0)
											rawoutput.append("[]");
										else
											rawoutput.append( String.valueOf(mapping_constraints[c].B));
										rawoutput.append("\t").append(current_change);
										rawoutput.append("\t/\t");
										// P residue
										if (mapping_constraints[c].P_residue)
										{
											rawoutput.append("X");
										}
										rawoutput.append("\t");
										// P features
									    if (mapping_constraints[c].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		rawoutput.append("{");
											rawoutput.append(compatibleSegments( c, true ));
									//		rawoutput.append("}");
									    }
									    // P__Q
										rawoutput.append("\t" ).append( String.valueOf(mapping_constraints[c].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[c].Q) ).append( "\t");
										// Q features
										if (mapping_constraints[c].Q_features_active)
										{
										//			rawoutput.append("{");
										  	rawoutput.append(compatibleSegments( c, false ));
										//		   	rawoutput.append("}");
										}
										rawoutput.append("\t");
										// Q residue
										if (mapping_constraints[c].Q_residue)
										{
										  rawoutput.append("Y");
										}
										rawoutput.append("\t");
										
										// an indication of doppelgaengerhood	
										if (mapping_constraints[c].doppelgaenger)
										{
											rawoutput.append("D\t");
										}
										else
										{
											rawoutput.append("\t");
										}
										
										
										// scope and reliability						
										rawoutput.append( mapping_constraints[c].scope
												).append( "\t" ).append( mapping_constraints[c].hits ).append( "\t");

										// the token frequency
										rawoutput.append( mapping_constraints[c].hits_frequency ).append( "\t");												
										// rel token frequency for this guy:
										rawoutput.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000) ).append( "\t");

										// now output the overall confidence for this guy:
										rawoutput.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000) ).append( "\t");
										// and weighted by freq
										rawoutput.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
										// and weighted by length:
										rawoutput.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");

										// and the particular confidences
										rawoutput.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( c ).append( "\t");

										// next come the related forms:
										for (int f = 1; f< related_forms[c][0]; f++)
										{
											if (f > 1)
												rawoutput.append(",");
											rawoutput.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
											
										}
										rawoutput.append("\t");
											 
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[c][0]; f++)
										{
											if (f > 1)
												rawoutput.append(",");
											rawoutput.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
											
										}
										rawoutput.append("\t");

										if (mapping_constraints[c].impugner > -1)
										{
											rawoutput.append( mapping_constraints[c].impugner ).append( "\t");
											rawoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[c].impugner) ).append( "\n");
										}
										else rawoutput.append("\t\t\n");
										localoutputscounter++;
									}
									i++;								
							} // end of while (loop through i's)
							
							// now we also want to know what the best raw rel among wug-time constraints is
							if (wug_learning_on)
							{
								// find wug constraint with highest raw rel
								highest_wug_raw = 0;
								for (int u = (firstWugtimeConstraint + 1); u < newestconstraint; u++)
								{
									if (mapping_constraints[u].reliability > val_to_beat)
										highest_wug_raw = u;
										val_to_beat = mapping_constraints[u].reliability;
								}
								
								// now if there was one (which I guess must always be the case), use it to derive the output:
								if (highest_wug_raw > 0)
								{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), highest_wug_raw);
									if (change_location >= 0)
									{
										rawoutput.append((w+1)).append("\t").append(localchangescounter).append("\traw").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
										if (mapping_constraints[highest_wug_raw].A.length == 0)
										{
											rawoutput.append("[]");
										}
										else
											rawoutput.append(String.valueOf(mapping_constraints[highest_wug_raw].A)) ;
												
										rawoutput.append( "\t->\t");
										
										if (mapping_constraints[highest_wug_raw].B.length == 0)
										{	rawoutput.append("[]");
										}
										else
										{
											rawoutput.append( String.valueOf(mapping_constraints[highest_wug_raw].B));											
										}
										
										rawoutput.append("\t" ).append( current_change);
										rawoutput.append("\t/\t");
										
										// P residue
										if (mapping_constraints[highest_wug_raw].P_residue)
										{
											rawoutput.append("X");
										}
										rawoutput.append("\t");
										
										// P features
									    if (mapping_constraints[highest_wug_raw].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		rawoutput.append("{");
											rawoutput.append(compatibleSegments( highest_wug_raw, true ));
									//		rawoutput.append("}");
									    }
									    // P__Q
										rawoutput.append("\t" ).append( String.valueOf(mapping_constraints[highest_wug_raw].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[highest_wug_raw].Q) ).append( "\t");
																				
										// Q features
										if (mapping_constraints[highest_wug_raw].Q_features_active)
										{
										//			rawoutput.append("{");
										  	rawoutput.append(compatibleSegments( highest_wug_raw, false ));
										//		   	rawoutput.append("}");
										}
										rawoutput.append("\t");
										// Q residue
										if (mapping_constraints[highest_wug_raw].Q_residue)
										{
										  rawoutput.append("Y");										  
										}
										rawoutput.append("\t");
											
										// an indication of wug-time status:
											rawoutput.append("W\t");
											
										// scope and reliability						
										rawoutput.append( mapping_constraints[highest_wug_raw].scope
														).append( "\t" ).append( mapping_constraints[highest_wug_raw].hits).append( "\t");
										
										// the token frequency
										rawoutput.append( mapping_constraints[highest_wug_raw].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										rawoutput.append( (Math.floor(mapping_constraints[highest_wug_raw].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
										
										// now output the overall confidence for this guy:
										rawoutput.append( (Math.floor(mapping_constraints[highest_wug_raw].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										rawoutput.append( (Math.floor( mapping_constraints[highest_wug_raw].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");

										// and weighted by length:
										rawoutput.append( (Math.floor(mapping_constraints[highest_wug_raw].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");


										// and the particular confidences
										rawoutput.append( (Math.floor(mapping_constraints[highest_wug_raw].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[highest_wug_raw].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_raw].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_raw].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_raw].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_raw].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( highest_wug_raw ).append( "\t");	 
										
										// next come the related forms:
										for (int f = 1; f< related_forms[highest_wug_raw][0]; f++)
										{
											if (f > 1)
											{
												rawoutput.append(", ");
											}
											rawoutput.append( String.valueOf(known_forms[ related_forms[highest_wug_raw][f] ][0]));											
											
										}
										rawoutput.append("\t");
										
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[highest_wug_raw][0]; f++)
										{
											if (f > 1)
											{
												rawoutput.append(", ");
											}	
											rawoutput.append( String.valueOf(known_forms[ exceptions[highest_wug_raw][f] ][0]));											
										}
										rawoutput.append("\t");
										
										// now print out the impugner info
										if (mapping_constraints[highest_wug_raw].impugner > -1)
										{
											rawoutput.append( mapping_constraints[highest_wug_raw].impugner ).append( "\t" 
															).append( mapping_constraints[mapping_constraints[highest_wug_raw].impugner].scope ).append( "\t"
															).append( mapping_constraints[mapping_constraints[highest_wug_raw].impugner].hits ).append( "\t");
											rawoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[highest_wug_raw].impugner) ).append( "\n");											
										}
										else{
										 	rawoutput.append("\t\t\n");
										}
										localoutputscounter++;
										
									} // fi change_location >=0
									
								} // fi  (highest_wug_raw > 0)
							} // fi (wug_learning_on)
							
							
						} // end of findbestraw block	
						rawrelfile.write(rawoutput.toString());
						rawoutput = new StringBuffer(500);
				
					} // end if saveRawRelFile						
					if (saveWeightedFile)
					{
						findbestweighted:{
							current_batch = (ConstraintBatch) changes_by_weighted.get( current_change );
							
							localoutputscounter = 1;
							i=1;
							val_to_beat = -50;						


							while (localoutputscounter <= 1 && i < current_batch.size() ) // used to be <=
							{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
									if (change_location >=0)
									{
										// localoutput was already built above, no need to redo
										c=current_batch.constraints[i];
										val_to_beat = mapping_constraints[c].weightedConfidence;
										wbloutput.append((w+1)).append("\t").append(localchangescounter).append("\twbl").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");

										if (mapping_constraints[c].A.length == 0)
											wbloutput.append("[]");
										else
											wbloutput.append(String.valueOf(mapping_constraints[c].A)) ;
										wbloutput.append( "\t->\t");
										if (mapping_constraints[c].B.length == 0)
											wbloutput.append("[]");
										else
											wbloutput.append( String.valueOf(mapping_constraints[c].B));
										wbloutput.append("\t").append(current_change);
										wbloutput.append("\t/\t");
										// P residue
										if (mapping_constraints[c].P_residue)
										{
											wbloutput.append("X");
										}
										wbloutput.append("\t");
										// P features
									    if (mapping_constraints[c].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		wbloutput.append("{");
											wbloutput.append(compatibleSegments( c, true ));
									//		wbloutput.append("}");
									    }
									    // P__Q
										wbloutput.append("\t" ).append( String.valueOf(mapping_constraints[c].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[c].Q) ).append( "\t");
										// Q features
										if (mapping_constraints[c].Q_features_active)
										{
										//			wbloutput.append("{");
										  	wbloutput.append(compatibleSegments( c, false ));
										//		   	wbloutput.append("}");
										}
										wbloutput.append("\t");
										// Q residue
										if (mapping_constraints[c].Q_residue)
										{
										  wbloutput.append("Y");
										}
										wbloutput.append("\t");
										
										// an indication of doppelgaengerhood	
										if (mapping_constraints[c].doppelgaenger)
										{
											wbloutput.append("D\t");
										}
										else
										{
											wbloutput.append("\t");
										}
										
										
										// scope and reliability						
										wbloutput.append( mapping_constraints[c].scope
												).append( "\t" ).append( mapping_constraints[c].hits ).append( "\t");

										// the token frequency
										wbloutput.append( mapping_constraints[c].hits_frequency ).append( "\t");												
										// rel token frequency for this guy:
										wbloutput.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000) ).append( "\t");

										// now output the overall confidence for this guy:
										wbloutput.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000) ).append( "\t");
										// and weighted by freq
										wbloutput.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
										// and weighted by length:
										wbloutput.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");

										// and the particular confidences
										wbloutput.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( c ).append( "\t");

										// next come the related forms:
										for (int f = 1; f< related_forms[c][0]; f++)
										{
											if (f > 1)
												wbloutput.append(",");
											wbloutput.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
											
										}
										wbloutput.append("\t");
											 
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[c][0]; f++)
										{
											if (f > 1)
												wbloutput.append(",");
											wbloutput.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
											
										}
										wbloutput.append("\t");

										if (mapping_constraints[c].impugner > -1)
										{
											wbloutput.append( mapping_constraints[c].impugner ).append( "\t");
											wbloutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[c].impugner) ).append( "\n");
										}
										else wbloutput.append("\t\t\n");
										localoutputscounter++;
									}
									i++;
								
							} // end of while (loop through i's)
							
							// now we also want to know what the best weighted by length among wug-time constraints is
							if (wug_learning_on)
							{
								// find wug constraint with highest weighted by length (c75) value
								highest_wug_wbl = 0;
								for (int u = (firstWugtimeConstraint + 1); u < newestconstraint; u++)
								{
									if (mapping_constraints[u].weightedConfidence > val_to_beat)
										highest_wug_wbl = u;
										val_to_beat = mapping_constraints[u].weightedConfidence;
								}
								
								// now if there was one (which I guess must always be the case), use it to derive the output:
								if (highest_wug_wbl > 0)
								{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), highest_wug_wbl);
									if (change_location >= 0)
									{
										wbloutput.append((w+1)).append("\t").append(localchangescounter).append("\twbl").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
										if (mapping_constraints[highest_wug_wbl].A.length == 0)
										{
											wbloutput.append("[]");
										}
										else
											wbloutput.append(String.valueOf(mapping_constraints[highest_wug_wbl].A)) ;
												
										wbloutput.append( "\t->\t");
										
										if (mapping_constraints[highest_wug_wbl].B.length == 0)
										{	wbloutput.append("[]");
										}
										else
										{
											wbloutput.append( String.valueOf(mapping_constraints[highest_wug_wbl].B));											
										}
										
										wbloutput.append("\t" ).append( current_change);
										wbloutput.append("\t/\t");
										
										// P residue
										if (mapping_constraints[highest_wug_wbl].P_residue)
										{
											wbloutput.append("X");
										}
										wbloutput.append("\t");
										
										// P features
									    if (mapping_constraints[highest_wug_wbl].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		wbloutput.append("{");
											wbloutput.append(compatibleSegments( highest_wug_wbl, true ));
									//		wbloutput.append("}");
									    }
									    // P__Q
										wbloutput.append("\t" ).append( String.valueOf(mapping_constraints[highest_wug_wbl].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[highest_wug_wbl].Q) ).append( "\t");
																				
										// Q features
										if (mapping_constraints[highest_wug_wbl].Q_features_active)
										{
										//			wbloutput.append("{");
										  	wbloutput.append(compatibleSegments( highest_wug_wbl, false ));
										//		   	wbloutput.append("}");
										}
										wbloutput.append("\t");
										// Q residue
										if (mapping_constraints[highest_wug_wbl].Q_residue)
										{
										  wbloutput.append("Y");										  
										}
										wbloutput.append("\t");
											
										// an indication of wug-time status:
											wbloutput.append("W\t");
											
										// scope and reliability						
										wbloutput.append( mapping_constraints[highest_wug_wbl].scope
														).append( "\t" ).append( mapping_constraints[highest_wug_wbl].hits).append( "\t");
										
										// the token frequency
										wbloutput.append( mapping_constraints[highest_wug_wbl].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										wbloutput.append( (Math.floor(mapping_constraints[highest_wug_wbl].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
										
										// now output the overall confidence for this guy:
										wbloutput.append( (Math.floor(mapping_constraints[highest_wug_wbl].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										wbloutput.append( (Math.floor( mapping_constraints[highest_wug_wbl].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");

										// and weighted by length:
										wbloutput.append( (Math.floor(mapping_constraints[highest_wug_wbl].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");


										// and the particular confidences
										wbloutput.append( (Math.floor(mapping_constraints[highest_wug_wbl].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[highest_wug_wbl].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_wbl].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_wbl].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_wbl].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_wbl].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( highest_wug_wbl ).append( "\t");	 
										
										// next come the related forms:
										for (int f = 1; f< related_forms[highest_wug_wbl][0]; f++)
										{
											if (f > 1)
											{
												wbloutput.append(", ");
											}
											wbloutput.append( String.valueOf(known_forms[ related_forms[highest_wug_wbl][f] ][0]));											
											
										}
										wbloutput.append("\t");
										
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[highest_wug_wbl][0]; f++)
										{
											if (f > 1)
											{
												wbloutput.append(", ");
											}	
											wbloutput.append( String.valueOf(known_forms[ exceptions[highest_wug_wbl][f] ][0]));											
										}
										wbloutput.append("\t");
										
										// now print out the impugner info
										if (mapping_constraints[highest_wug_wbl].impugner > -1)
										{
											wbloutput.append( mapping_constraints[highest_wug_wbl].impugner ).append( "\t" 
															).append( mapping_constraints[mapping_constraints[highest_wug_wbl].impugner].scope ).append( "\t"
															).append( mapping_constraints[mapping_constraints[highest_wug_wbl].impugner].hits ).append( "\t");
											wbloutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[highest_wug_wbl].impugner) ).append( "\n");											
										}
										else{
										 	wbloutput.append("\t\t\n");
										}
										localoutputscounter++;
										
									} // fi change_location >=0
									
								} // fi  (highest_wug_wbl > 0)
							} // fi (wug_learning_on)
							
						} // end of findbestweighted block	
						
						weightedfile.write(wbloutput.toString());
						wbloutput = new StringBuffer(500);

										
					} // end if saveWeightedFile						
					if (saveFreqFile)
					{
						findbestfreq:{
							current_batch = (ConstraintBatch) changes_by_freq.get( current_change );
							
							localoutputscounter = 1;
							i=1;	
							val_to_beat = -50;					


							while (localoutputscounter <= 1 && i < current_batch.size() ) // used to be <=
							{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
									if (change_location >=0)
									{
										// localoutput was already built above, no need to redo
										c=current_batch.constraints[i];
										val_to_beat = mapping_constraints[c].hits_frequency;
										freqoutput.append((w+1)).append("\t").append(localchangescounter).append("\traw").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");

										if (mapping_constraints[c].A.length == 0)
											freqoutput.append("[]");
										else
											freqoutput.append(String.valueOf(mapping_constraints[c].A)) ;
										freqoutput.append( "\t->\t");
										if (mapping_constraints[c].B.length == 0)
											freqoutput.append("[]");
										else
											freqoutput.append( String.valueOf(mapping_constraints[c].B));
										freqoutput.append("\t").append(current_change);
										freqoutput.append("\t/\t");
										// P residue
										if (mapping_constraints[c].P_residue)
										{
											freqoutput.append("X");
										}
										freqoutput.append("\t");
										// P features
									    if (mapping_constraints[c].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		freqoutput.append("{");
											freqoutput.append(compatibleSegments( c, true ));
									//		freqoutput.append("}");
									    }
									    // P__Q
										freqoutput.append("\t" ).append( String.valueOf(mapping_constraints[c].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[c].Q) ).append( "\t");
										// Q features
										if (mapping_constraints[c].Q_features_active)
										{
										//			freqoutput.append("{");
										  	freqoutput.append(compatibleSegments( c, false ));
										//		   	freqoutput.append("}");
										}
										freqoutput.append("\t");
										// Q residue
										if (mapping_constraints[c].Q_residue)
										{
										  freqoutput.append("Y");
										}
										freqoutput.append("\t");
										
										// an indication of doppelgaengerhood	
										if (mapping_constraints[c].doppelgaenger)
										{
											freqoutput.append("D\t");
										}
										else
										{
											freqoutput.append("\t");
										}
										
										
										// scope and reliability						
										freqoutput.append( mapping_constraints[c].scope
												).append( "\t" ).append( mapping_constraints[c].hits).append( "\t");

										// the token frequency
										freqoutput.append( mapping_constraints[c].hits_frequency ).append( "\t");												
										// rel token frequency for this guy:
										freqoutput.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000) ).append( "\t");

										// now output the overall confidence for this guy:
										freqoutput.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000) ).append( "\t");
										// and weighted by freq
										freqoutput.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
										// and weighted by length:
										freqoutput.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");

										// and the particular confidences
										freqoutput.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( c ).append( "\t");

										// next come the related forms:
										for (int f = 1; f< related_forms[c][0]; f++)
										{
											if (f > 1)
												freqoutput.append(",");
											freqoutput.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
											
										}
										freqoutput.append("\t");
											 
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[c][0]; f++)
										{
											if (f > 1)
												freqoutput.append(",");
											freqoutput.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
											
										}
										freqoutput.append("\t");

										if (mapping_constraints[c].impugner > -1)
										{
											freqoutput.append( mapping_constraints[c].impugner ).append( "\t");
											freqoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[c].impugner) ).append( "\n");
										}
										else freqoutput.append("\t\t\n");
										localoutputscounter++;
									}
									i++;

							} // end of while (loop through i's)
							
							// now we also want to know what the best freq among wug-time constraints is
							if (wug_learning_on)
							{
								// find wug constraint with highest freq
								highest_wug_freq = 0;
								for (int u = (firstWugtimeConstraint + 1); u < newestconstraint; u++)
								{
									if (mapping_constraints[u].hits_frequency > val_to_beat)
										highest_wug_freq = u;
										val_to_beat = mapping_constraints[u].hits_frequency;
								}
								
								// now if there was one (which I guess must always be the case), use it to derive the output:
								if (highest_wug_freq > 0)
								{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), highest_wug_freq);
									if (change_location >= 0)
									{
										freqoutput.append((w+1)).append("\t").append(localchangescounter).append("\tfreq").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
										if (mapping_constraints[highest_wug_freq].A.length == 0)
										{
											freqoutput.append("[]");
										}
										else
											freqoutput.append(String.valueOf(mapping_constraints[highest_wug_freq].A)) ;
												
										freqoutput.append( "\t->\t");
										
										if (mapping_constraints[highest_wug_freq].B.length == 0)
										{	freqoutput.append("[]");
										}
										else
										{
											freqoutput.append( String.valueOf(mapping_constraints[highest_wug_freq].B));											
										}
										
										freqoutput.append("\t" ).append( current_change);
										freqoutput.append("\t/\t");
										
										// P residue
										if (mapping_constraints[highest_wug_freq].P_residue)
										{
											freqoutput.append("X");
										}
										freqoutput.append("\t");
										
										// P features
									    if (mapping_constraints[highest_wug_freq].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		freqoutput.append("{");
											freqoutput.append(compatibleSegments( highest_wug_freq, true ));
									//		freqoutput.append("}");
									    }
									    // P__Q
										freqoutput.append("\t" ).append( String.valueOf(mapping_constraints[highest_wug_freq].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[highest_wug_freq].Q) ).append( "\t");
																				
										// Q features
										if (mapping_constraints[highest_wug_freq].Q_features_active)
										{
										//			freqoutput.append("{");
										  	freqoutput.append(compatibleSegments( highest_wug_freq, false ));
										//		   	freqoutput.append("}");
										}
										freqoutput.append("\t");
										// Q residue
										if (mapping_constraints[highest_wug_freq].Q_residue)
										{
										  freqoutput.append("Y");										  
										}
										freqoutput.append("\t");
											
										// an indication of wug-time status:
											freqoutput.append("W\t");
											
										// scope and reliability						
										freqoutput.append( mapping_constraints[highest_wug_freq].scope
														).append( "\t" ).append( mapping_constraints[highest_wug_freq].hits).append( "\t");
										
										// the token frequency
										freqoutput.append( mapping_constraints[highest_wug_freq].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										freqoutput.append( (Math.floor(mapping_constraints[highest_wug_freq].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
										
										// now output the overall confidence for this guy:
										freqoutput.append( (Math.floor(mapping_constraints[highest_wug_freq].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										freqoutput.append( (Math.floor( mapping_constraints[highest_wug_freq].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");

										// and weighted by length:
										freqoutput.append( (Math.floor(mapping_constraints[highest_wug_freq].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");


										// and the particular confidences
										freqoutput.append( (Math.floor(mapping_constraints[highest_wug_freq].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[highest_wug_freq].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_freq].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_freq].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_freq].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_freq].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( highest_wug_freq ).append( "\t");	 
										
										// next come the related forms:
										for (int f = 1; f< related_forms[highest_wug_freq][0]; f++)
										{
											if (f > 1)
											{
												freqoutput.append(", ");
											}
											freqoutput.append( String.valueOf(known_forms[ related_forms[highest_wug_freq][f] ][0]));											
											
										}
										freqoutput.append("\t");
										
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[highest_wug_freq][0]; f++)
										{
											if (f > 1)
											{
												freqoutput.append(", ");
											}	
											freqoutput.append( String.valueOf(known_forms[ exceptions[highest_wug_freq][f] ][0]));											
										}
										freqoutput.append("\t");
										
										// now print out the impugner info
										if (mapping_constraints[highest_wug_freq].impugner > -1)
										{
											freqoutput.append( mapping_constraints[highest_wug_freq].impugner ).append( "\t" 
															).append( mapping_constraints[mapping_constraints[highest_wug_freq].impugner].scope ).append( "\t"
															).append( mapping_constraints[mapping_constraints[highest_wug_freq].impugner].hits ).append( "\t");
											freqoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[highest_wug_freq].impugner) ).append( "\n");											
										}
										else{
										 	freqoutput.append("\t\t\n");
										}
										localoutputscounter++;
										
									} // fi change_location >=0
									
								} // fi  (highest_wug_freq > 0)
							} // fi (wug_learning_on)
							freqfile.write(freqoutput.toString());
							freqoutput = new StringBuffer(500);	
						} // end of findbestfreq block					


						findbestrelfreq:{
							current_batch = (ConstraintBatch) changes_by_rel_freq.get( current_change );
							
							localoutputscounter = 1;
							i=1;
							val_to_beat = -50;					


							while (localoutputscounter <= 1 && i < current_batch.size() ) // used to be <=
							{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
									if (change_location >=0)
									{
										// localoutput was already built above, no need to redo
										c=current_batch.constraints[i];
										val_to_beat = mapping_constraints[c].rel_frequency;
										relfreqoutput.append((w+1)).append("\t").append(localchangescounter).append("\traw").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");

										if (mapping_constraints[c].A.length == 0)
											relfreqoutput.append("[]");
										else
											relfreqoutput.append(String.valueOf(mapping_constraints[c].A)) ;
										relfreqoutput.append( "\t->\t");
										if (mapping_constraints[c].B.length == 0)
											relfreqoutput.append("[]");
										else
											relfreqoutput.append( String.valueOf(mapping_constraints[c].B));
										relfreqoutput.append("\t").append(current_change);
										relfreqoutput.append("\t/\t");
										// P residue
										if (mapping_constraints[c].P_residue)
										{
											relfreqoutput.append("X");
										}
										relfreqoutput.append("\t");
										// P features
									    if (mapping_constraints[c].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		relfreqoutput.append("{");
											relfreqoutput.append(compatibleSegments( c, true ));
									//		relfreqoutput.append("}");
									    }
									    // P__Q
										relfreqoutput.append("\t" ).append( String.valueOf(mapping_constraints[c].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[c].Q) ).append( "\t");
										// Q features
										if (mapping_constraints[c].Q_features_active)
										{
										//			relfreqoutput.append("{");
										  	relfreqoutput.append(compatibleSegments( c, false ));
										//		   	relfreqoutput.append("}");
										}
										relfreqoutput.append("\t");
										// Q residue
										if (mapping_constraints[c].Q_residue)
										{
										  relfreqoutput.append("Y");
										}
										relfreqoutput.append("\t");
										
										// an indication of doppelgaengerhood	
										if (mapping_constraints[c].doppelgaenger)
										{
											relfreqoutput.append("D\t");
										}
										else
										{
											relfreqoutput.append("\t");
										}
										
										
										// scope and reliability						
										relfreqoutput.append( mapping_constraints[c].scope
												).append( "\t" ).append( mapping_constraints[c].hits).append( "\t");

										// the token frequency
										relfreqoutput.append( mapping_constraints[c].hits_frequency ).append( "\t");												
										// rel token frequency for this guy:
										relfreqoutput.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000) ).append( "\t");

										// now output the overall confidence for this guy:
										relfreqoutput.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000) ).append( "\t");
										// and weighted by freq
										relfreqoutput.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
										// and weighted by length:
										relfreqoutput.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");

										// and the particular confidences
										relfreqoutput.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( c ).append( "\t");

										// next come the related forms:
										for (int f = 1; f< related_forms[c][0]; f++)
										{
											if (f > 1)
												relfreqoutput.append(",");
											relfreqoutput.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
											
										}
										relfreqoutput.append("\t");
											 
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[c][0]; f++)
										{
											if (f > 1)
												relfreqoutput.append(",");
											relfreqoutput.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
											
										}
										relfreqoutput.append("\t");

										if (mapping_constraints[c].impugner > -1)
										{
											relfreqoutput.append( mapping_constraints[c].impugner ).append( "\t");
											relfreqoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[c].impugner) ).append( "\n");
										}
										else relfreqoutput.append("\t\t\n");
										localoutputscounter++;
									}
									i++;

								
							} // end of while (loop through i's)
							
							// now we also want to know what the best rfreq among wug-time constraints is
							if (wug_learning_on)
							{
								// find wug constraint with highest rfreq
								highest_wug_rfreq = 0;
								for (int u = (firstWugtimeConstraint + 1); u < newestconstraint; u++)
								{
									if (mapping_constraints[u].rel_frequency > val_to_beat)
										highest_wug_rfreq = u;
										val_to_beat = mapping_constraints[u].rel_frequency;
								}
								
								// now if there was one (which I guess must always be the case), use it to derive the output:
								if (highest_wug_rfreq > 0)
								{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), highest_wug_rfreq);
									if (change_location >= 0)
									{
										relfreqoutput.append((w+1)).append("\t").append(localchangescounter).append("\trfreq").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
										if (mapping_constraints[highest_wug_rfreq].A.length == 0)
										{
											relfreqoutput.append("[]");
										}
										else
											relfreqoutput.append(String.valueOf(mapping_constraints[highest_wug_rfreq].A)) ;
												
										relfreqoutput.append( "\t->\t");
										
										if (mapping_constraints[highest_wug_rfreq].B.length == 0)
										{	relfreqoutput.append("[]");
										}
										else
										{
											relfreqoutput.append( String.valueOf(mapping_constraints[highest_wug_rfreq].B));											
										}
										
										relfreqoutput.append("\t" ).append( current_change);
										relfreqoutput.append("\t/\t");
										
										// P residue
										if (mapping_constraints[highest_wug_rfreq].P_residue)
										{
											relfreqoutput.append("X");
										}
										relfreqoutput.append("\t");
										
										// P features
									    if (mapping_constraints[highest_wug_rfreq].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		relfreqoutput.append("{");
											relfreqoutput.append(compatibleSegments( highest_wug_rfreq, true ));
									//		relfreqoutput.append("}");
									    }
									    // P__Q
										relfreqoutput.append("\t" ).append( String.valueOf(mapping_constraints[highest_wug_rfreq].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[highest_wug_rfreq].Q) ).append( "\t");
																				
										// Q features
										if (mapping_constraints[highest_wug_rfreq].Q_features_active)
										{
										//			relfreqoutput.append("{");
										  	relfreqoutput.append(compatibleSegments( highest_wug_rfreq, false ));
										//		   	relfreqoutput.append("}");
										}
										relfreqoutput.append("\t");
										// Q residue
										if (mapping_constraints[highest_wug_rfreq].Q_residue)
										{
										  relfreqoutput.append("Y");										  
										}
										relfreqoutput.append("\t");
											
										// an indication of wug-time status:
											relfreqoutput.append("W\t");
											
										// scope and reliability						
										relfreqoutput.append( mapping_constraints[highest_wug_rfreq].scope
														).append( "\t" ).append( mapping_constraints[highest_wug_rfreq].hits).append( "\t");
										
										// the token frequency
										relfreqoutput.append( mapping_constraints[highest_wug_rfreq].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										relfreqoutput.append( (Math.floor(mapping_constraints[highest_wug_rfreq].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
										
										// now output the overall confidence for this guy:
										relfreqoutput.append( (Math.floor(mapping_constraints[highest_wug_rfreq].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										relfreqoutput.append( (Math.floor( mapping_constraints[highest_wug_rfreq].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");

										// and weighted by length:
										relfreqoutput.append( (Math.floor(mapping_constraints[highest_wug_rfreq].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");


										// and the particular confidences
										relfreqoutput.append( (Math.floor(mapping_constraints[highest_wug_rfreq].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[highest_wug_rfreq].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_rfreq].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_rfreq].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_rfreq].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_rfreq].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( highest_wug_rfreq ).append( "\t");	 
										
										// next come the related forms:
										for (int f = 1; f< related_forms[highest_wug_rfreq][0]; f++)
										{
											if (f > 1)
											{
												relfreqoutput.append(", ");
											}
											relfreqoutput.append( String.valueOf(known_forms[ related_forms[highest_wug_rfreq][f] ][0]));											
											
										}
										relfreqoutput.append("\t");
										
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[highest_wug_rfreq][0]; f++)
										{
											if (f > 1)
											{
												relfreqoutput.append(", ");
											}	
											relfreqoutput.append( String.valueOf(known_forms[ exceptions[highest_wug_rfreq][f] ][0]));											
										}
										relfreqoutput.append("\t");
										
										// now print out the impugner info
										if (mapping_constraints[highest_wug_rfreq].impugner > -1)
										{
											relfreqoutput.append( mapping_constraints[highest_wug_rfreq].impugner ).append( "\t" 
															).append( mapping_constraints[mapping_constraints[highest_wug_rfreq].impugner].scope ).append( "\t"
															).append( mapping_constraints[mapping_constraints[highest_wug_rfreq].impugner].hits ).append( "\t");
											relfreqoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[highest_wug_rfreq].impugner) ).append( "\n");											
										}
										else{
										 	relfreqoutput.append("\t\t\n");
										}
										localoutputscounter++;
										
									} // fi change_location >=0
									
								} // fi  (highest_wug_rfreq > 0)
							} // fi (wug_learning_on)							
							
						relfreqfile.write(relfreqoutput.toString());
						relfreqoutput = new StringBuffer(500);
							
						} // end of findbestrelfreq block					

					    } // end if savefreqFile						
						if (saveWeightedByFreqFile)
						{
						findbestweightedbyfreq:{
							current_batch = (ConstraintBatch) changes_by_weighted_by_freq.get( current_change );
							
							localoutputscounter = 1;
							i=1;
							val_to_beat = -50;


							while (localoutputscounter <= 1 && i < current_batch.size() ) // used to be <=
							{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), current_batch.constraints[i]);
									if (change_location >=0)
									{
										// localoutput was already built above, no need to redo
										c=current_batch.constraints[i];
										val_to_beat = mapping_constraints[c].weightedByFreq;
										wbfoutput.append((w+1)).append("\t").append(localchangescounter).append("\traw").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");

										if (mapping_constraints[c].A.length == 0)
											wbfoutput.append("[]");
										else
											wbfoutput.append(String.valueOf(mapping_constraints[c].A)) ;
										wbfoutput.append( "\t->\t");
										if (mapping_constraints[c].B.length == 0)
											wbfoutput.append("[]");
										else
											wbfoutput.append( String.valueOf(mapping_constraints[c].B));
										wbfoutput.append("\t").append(current_change);
										wbfoutput.append("\t/\t");
										// P residue
										if (mapping_constraints[c].P_residue)
										{
											wbfoutput.append("X");
										}
										wbfoutput.append("\t");
										// P features
									    if (mapping_constraints[c].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		wbfoutput.append("{");
											wbfoutput.append(compatibleSegments( c, true ));
									//		wbfoutput.append("}");
									    }
									    // P__Q
										wbfoutput.append("\t" ).append( String.valueOf(mapping_constraints[c].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[c].Q) ).append( "\t");
										// Q features
										if (mapping_constraints[c].Q_features_active)
										{
										//			wbfoutput.append("{");
										  	wbfoutput.append(compatibleSegments( c, false ));
										//		   	wbfoutput.append("}");
										}
										wbfoutput.append("\t");
										// Q residue
										if (mapping_constraints[c].Q_residue)
										{
										  wbfoutput.append("Y");
										}
										wbfoutput.append("\t");
										
										// an indication of doppelgaengerhood	
										if (mapping_constraints[c].doppelgaenger)
										{
											wbfoutput.append("D\t");
										}
										else
										{
											wbfoutput.append("\t");
										}
										
										
										// scope and reliability						
										wbfoutput.append( mapping_constraints[c].scope 
												).append( "\t" ).append(mapping_constraints[c].hits).append( "\t");

										// the token frequency
										wbfoutput.append( mapping_constraints[c].hits_frequency ).append( "\t");												
										// rel token frequency for this guy:
										wbfoutput.append( (Math.floor(mapping_constraints[c].rel_frequency * 1000 + .5) / 1000) ).append( "\t");

										// now output the overall confidence for this guy:
										wbfoutput.append( (Math.floor(mapping_constraints[c].overallconfidence * 1000 + .5) / 1000) ).append( "\t");
										// and weighted by freq
										wbfoutput.append( (Math.floor( mapping_constraints[c].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");
										// and weighted by length:
										wbfoutput.append( (Math.floor(mapping_constraints[c].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");

										// and the particular confidences
										wbfoutput.append( (Math.floor(mapping_constraints[c].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[c].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[c].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( c ).append( "\t");

										// next come the related forms:
										for (int f = 1; f< related_forms[c][0]; f++)
										{
											if (f > 1)
												wbfoutput.append(",");
											wbfoutput.append( String.valueOf(known_forms[ related_forms[c][f] ][0]));
											
										}
										wbfoutput.append("\t");
											 
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[c][0]; f++)
										{
											if (f > 1)
												wbfoutput.append(",");
											wbfoutput.append( String.valueOf(known_forms[ exceptions[c][f] ][0]));
											
										}
										wbfoutput.append("\t");

										if (mapping_constraints[c].impugner > -1)
										{
											wbfoutput.append( mapping_constraints[c].impugner ).append( "\t");
											wbfoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[c].impugner) ).append( "\n");
										}
										else wbfoutput.append("\t\t\n");
										localoutputscounter++;
									}
									i++;

							} // end of while (loop through i's)
							// now we also want to know what the best weighted by freq among wug-time constraints is
							if (wug_learning_on)
							{
								// find wug constraint with highest weighted by frequency value
								highest_wug_wbf = 0;
								for (int u = (firstWugtimeConstraint + 1); u < newestconstraint; u++)
								{
									if (mapping_constraints[u].weightedByFreq > val_to_beat)
										highest_wug_wbf = u;
										val_to_beat = mapping_constraints[u].weightedByFreq;
								}
								
								// now if there was one (which I guess must always be the case), use it to derive the output:
								if (highest_wug_wbf > 0)
								{
									change_location = strucDescMet(learnerTask.test_forms[w].toCharArray(), highest_wug_wbf);
									if (change_location >= 0)
									{
										wbfoutput.append((w+1)).append("\t").append(localchangescounter).append("\twbf").append(localoutputscounter).append("\t"
																).append( learnerTask.test_forms[w]).append( "\t->\t" ).append( localoutput ).append( "\tby\t");
										if (mapping_constraints[highest_wug_wbf].A.length == 0)
										{
											wbfoutput.append("[]");
										}
										else
											wbfoutput.append(String.valueOf(mapping_constraints[highest_wug_wbf].A)) ;
												
										wbfoutput.append( "\t->\t");
										
										if (mapping_constraints[highest_wug_wbf].B.length == 0)
										{	wbfoutput.append("[]");
										}
										else
										{
											wbfoutput.append( String.valueOf(mapping_constraints[highest_wug_wbf].B));											
										}
										
										wbfoutput.append("\t" ).append( current_change);
										wbfoutput.append("\t/\t");
										
										// P residue
										if (mapping_constraints[highest_wug_wbf].P_residue)
										{
											wbfoutput.append("X");
										}
										wbfoutput.append("\t");
										
										// P features
									    if (mapping_constraints[highest_wug_wbf].P_features_active)
									    {
											// we want to print a list of the compatible segments, for human readability
									//		wbfoutput.append("{");
											wbfoutput.append(compatibleSegments( highest_wug_wbf, true ));
									//		wbfoutput.append("}");
									    }
									    // P__Q
										wbfoutput.append("\t" ).append( String.valueOf(mapping_constraints[highest_wug_wbf].P) ).append( "\t___\t"
														  ).append( String.valueOf(mapping_constraints[highest_wug_wbf].Q) ).append( "\t");
																				
										// Q features
										if (mapping_constraints[highest_wug_wbf].Q_features_active)
										{
										//			wbfoutput.append("{");
										  	wbfoutput.append(compatibleSegments( highest_wug_wbf, false ));
										//		   	wbfoutput.append("}");
										}
										wbfoutput.append("\t");
										// Q residue
										if (mapping_constraints[highest_wug_wbf].Q_residue)
										{
										  wbfoutput.append("Y");										  
										}
										wbfoutput.append("\t");
											
										// an indication of wug-time status:
											wbfoutput.append("W\t");
											
										// scope and reliability						
										wbfoutput.append( mapping_constraints[highest_wug_wbf].scope
														).append( "\t" ).append( mapping_constraints[highest_wug_wbf].hits).append( "\t");
										
										// the token frequency
										wbfoutput.append( mapping_constraints[highest_wug_wbf].hits_frequency ).append( "\t");
										// rel token frequency for this guy:
										wbfoutput.append( (Math.floor(mapping_constraints[highest_wug_wbf].rel_frequency * 1000 + .5) / 1000) ).append( "\t");
										
										// now output the overall confidence for this guy:
										wbfoutput.append( (Math.floor(mapping_constraints[highest_wug_wbf].overallconfidence * 1000 + .5) / 1000) ).append( "\t");

										// and weighted by freq
										wbfoutput.append( (Math.floor( mapping_constraints[highest_wug_wbf].weightedByFreq * 1000 + .5) / 1000 ) ).append( "\t");

										// and weighted by length:
										wbfoutput.append( (Math.floor(mapping_constraints[highest_wug_wbf].weightedConfidence * 1000 + .5) / 1000 ) ).append( "\t");


										// and the particular confidences
										wbfoutput.append( (Math.floor(mapping_constraints[highest_wug_wbf].lowerConfidence.p75 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor( mapping_constraints[highest_wug_wbf].lowerConfidence.p90 * 1000 + .5) / 1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_wbf].lowerConfidence.p95 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_wbf].impugnedConfidence.p75 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_wbf].impugnedConfidence.p90 * 1000 + .5 ) /1000 )
											 ).append( "\t" ).append( (Math.floor(mapping_constraints[highest_wug_wbf].impugnedConfidence.p95 * 1000 + .5 ) /1000 ) 
											 ).append( "\t" ).append( highest_wug_wbf ).append( "\t");	 
										
										// next come the related forms:
										for (int f = 1; f< related_forms[highest_wug_wbf][0]; f++)
										{
											if (f > 1)
											{
												wbfoutput.append(", ");
											}
											wbfoutput.append( String.valueOf(known_forms[ related_forms[highest_wug_wbf][f] ][0]));											
											
										}
										wbfoutput.append("\t");
										
										// next come the exceptions to this constraint:
										for (int f = 1; f< exceptions[highest_wug_wbf][0]; f++)
										{
											if (f > 1)
											{
												wbfoutput.append(", ");
											}	
											wbfoutput.append( String.valueOf(known_forms[ exceptions[highest_wug_wbf][f] ][0]));											
										}
										wbfoutput.append("\t");
										
										// now print out the impugner info
										if (mapping_constraints[highest_wug_wbf].impugner > -1)
										{
											wbfoutput.append( mapping_constraints[highest_wug_wbf].impugner ).append( "\t" 
															).append( mapping_constraints[mapping_constraints[highest_wug_wbf].impugner].scope ).append( "\t"
															).append( mapping_constraints[mapping_constraints[highest_wug_wbf].impugner].hits ).append( "\t");
											wbfoutput.append("Impugned by constraint " ).append( printConstraint(mapping_constraints[highest_wug_wbf].impugner) ).append( "\n");											
										}
										else{
										 	wbfoutput.append("\t\t\n");
										}
										localoutputscounter++;
										
									} // fi change_location >=0
									
								} // fi  (highest_wug_wbf > 0)
							} // fi (wug_learning_on)

						} // end of findbestweightedbyfreq block	
						weightedbyfreqfile.write(wbfoutput.toString());
						wbfoutput = new StringBuffer(500);
										
					    } // end if saveweightedbyfreqFile						
										

					// forget about wug-time constraints, if we discovered any to begin with:
					if (wug_learning_on)
					{
						newestconstraint = firstWugtimeConstraint;
					}

			} // end of this relevant change
			} // end of deriveonechange block

		} // end of while (all_changes.hasNext() )

		outputfile.write(output.toString());
		output = new StringBuffer(2000);


	} // end of loop through test forms

}

	public char[] applyConstraintChange( char f[], int c ) throws NullPointerException
	{
		char stem[];
		char output[];
		int change_location;  // (likewise)
		int stemlength;
		int f_length = f.length;
		int a_length = mapping_constraints[c].A.length;
		int b_length = mapping_constraints[c].B.length;
		String form, mapping, change, outputstring;
		String stem1, stem2;
		boolean strucDescMet = true;
		
		
		if ( mapping_constraints[c].affixType == SUFFIX )
		{

			// in this case, we need to check to make sure the form ends with the correct A,
			// and then we strip that off and apply the B
			for (int i=1; i <= a_length; i++)
			{
				if (mapping_constraints[c].A[ a_length - i ] != f[ f_length - i])
				{
					strucDescMet = false;
					return null;
				}
			}
			if (strucDescMet)
			{			
				if (debug)
					System.out.print(" (Applying suffixation to yield [");

				stemlength = f_length - a_length;
				output = new char[ (stemlength + b_length) ];
				for (int i = 0; i < stemlength; i++)
					output[i] = f[i];
				for (int i = 0; i < b_length; i++)
					output[ stemlength+i ] = mapping_constraints[c].B[i];
				if (verbose)
					outputfile.write( output + "] before phonology)\n");
				
				return (output);
			}
			else return null;
		}
		else if ( mapping_constraints[c].affixType == PREFIX )
		{
			for (int i = 0; i< a_length; i++)
			{
				if (mapping_constraints[c].A[i] != f[i])
				{
					strucDescMet = false;
					return null;
				}
			}
			if (strucDescMet)
			{
				if (debug)
					System.out.println(" (Applying prefixation)");

				stemlength = f_length - a_length;
				output = new char[ b_length + stemlength ];
				for (int i = 0; i < b_length; i++)
					output[i] = mapping_constraints[c].B[i];
				for (int i = 0; i < stemlength; i++)
					output[ b_length+i] = f[ a_length + i ];

				return ( output );
			}
			else return null;
		}
		else if ( mapping_constraints[c].affixType == SUPPLETION )
		{
			if (debug)
				System.out.println(" (Applying suppletion)");


			return mapping_constraints[c].mappings[1];
		}
		else 
		{
			// some type of completely internal morphology; we have to do some work.
			
			// the easiest way i can think of to do this is to convert the form
			// and the mapping to strings, and let java do the work:

			form = new String (f);
			mapping = new String( mapping_constraints[c].A);

			if (verbose)
				System.out.println(" (Applying internal morphology)");

			

			change_location = form.indexOf(mapping);
			// we want to enforce internalness here, so change_location has to be
			// at least 1, and the change has to end before the end of the word
			if ((change_location > 0) && (change_location+mapping.length() < f_length))
			{

				stem1 = new String(form.substring(0,change_location));
				stem2 = new String( form.substring( change_location + 
											mapping.length(), form.length() ));
			
				change = new String(mapping_constraints[c].B);
				outputstring = stem1 + change +stem2;

				return (outputstring.toCharArray());
			}
			// otherwise perhaps the change was at an edge...
			else return null;
			// if change_location = -1, no region matches.  this should NEVER happen,
			// because this method is only used once we known that strucDesc is met.					
		}	
	}
	
 }