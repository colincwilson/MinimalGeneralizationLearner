import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class LearnerFrame extends Frame {
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

	// declare the menus
	java.awt.MenuBar menuBar;
	java.awt.Menu fileMenu;
	java.awt.Menu editMenu;
	java.awt.Menu learnerMenu;
	java.awt.CheckboxMenuItem menuItemUsePhonology;
	java.awt.CheckboxMenuItem menuItemIntermediateWugTests;
	java.awt.CheckboxMenuItem menuItemWugtimeLearning;
	java.awt.CheckboxMenuItem menuItemSmallInitial;
	java.awt.CheckboxMenuItem menuItemUseFeatures;
	java.awt.CheckboxMenuItem menuItemUseDoppelgaengers;
	java.awt.CheckboxMenuItem menuItemUseImpugnment;
	java.awt.CheckboxMenuItem menuItemSaveConstraintsFile;
	java.awt.CheckboxMenuItem menuItemHitsOutput;
	java.awt.CheckboxMenuItem menuItemRawRelOutput;
	java.awt.CheckboxMenuItem menuItemC75Output;
	java.awt.CheckboxMenuItem menuItemC90Output;
	java.awt.CheckboxMenuItem menuItemWeightedOutput;
	java.awt.CheckboxMenuItem menuItemFreqOutput;
	java.awt.CheckboxMenuItem menuItemWeightedByFreqOutput;

	java.awt.MenuItem menuItemLearnPhonology;

	// declare the controls
	java.awt.Label labelLanguageName;
	java.awt.Label labelFileName;
	java.awt.Label labelUserName;
	java.awt.Label labelFileNameDisplay;
	java.awt.Button buttonOpenFile;
	java.awt.Button buttonLearnPhonology;

	java.awt.Label labelUserNameDisplay;
	java.awt.Label labelLanguageNameDisplay;
	java.awt.Checkbox checkboxUsePhonology;
	java.awt.Checkbox checkboxIntermediateWugTests;
	java.awt.Checkbox checkboxSmallInitial;
	java.awt.Checkbox checkboxWugtimeLearning;
	java.awt.Checkbox checkboxUseFeatures;
	java.awt.Checkbox checkboxUseDoppelgaengers;
	java.awt.Checkbox checkboxUseImpugnment;
	java.awt.Checkbox checkboxSaveConstraintsFile;
	java.awt.Checkbox checkboxHitsOutput;
	java.awt.Checkbox checkboxRawRelOutput;
	java.awt.Checkbox checkboxC75Output;
	java.awt.Checkbox checkboxC90Output;
	java.awt.Checkbox checkboxWeightedOutput;
	java.awt.Checkbox checkboxFreqOutput;
	java.awt.Checkbox checkboxWeightedByFreqOutput;

	public LearnerFrame() {
	}

	public void initComponents() throws Exception {
		// set up the menus:
		menuBar = new java.awt.MenuBar();

		fileMenu = new java.awt.Menu("File");
		fileMenu.add("Open .in file");
		fileMenu.add("Open .fea file");
		fileMenu.add("Open batch file");
		fileMenu.addSeparator();
		fileMenu.add("Close");
		fileMenu.addSeparator();
		fileMenu.add("Quit");
		menuBar.add(fileMenu);

		editMenu = new java.awt.Menu("Edit");
		editMenu.add("Undo");
		editMenu.addSeparator();
		editMenu.add("Cut");
		editMenu.add("Copy");
		editMenu.add("Paste");
		menuBar.add(editMenu);

		learnerMenu = new java.awt.Menu("Learner");
		menuItemUsePhonology = new java.awt.CheckboxMenuItem("Use Phonology");
		menuItemUsePhonology.setState(phonologyOnFlag);
		learnerMenu.add(menuItemUsePhonology);
		menuItemIntermediateWugTests = new java.awt.CheckboxMenuItem("Intermediate Wug Tests");
		menuItemIntermediateWugTests.setState(intermediateWugTests);
		learnerMenu.add(menuItemIntermediateWugTests);
		menuItemWugtimeLearning = new java.awt.CheckboxMenuItem("Wug-time learning");
		menuItemWugtimeLearning.setState(wugtimeLearning);
		learnerMenu.add(menuItemWugtimeLearning);
		menuItemUseFeatures = new java.awt.CheckboxMenuItem("Use Features");
		menuItemUseFeatures.setState(useFeatures);
		learnerMenu.add(menuItemUseFeatures);
		menuItemUseDoppelgaengers = new java.awt.CheckboxMenuItem("Use Doppelg�ngers");
		menuItemUseDoppelgaengers.setState(useDoppelgaengers);
		learnerMenu.add(menuItemUseDoppelgaengers);
		menuItemUseImpugnment = new java.awt.CheckboxMenuItem("Impugnment");
		menuItemUseImpugnment.setState(useImpugnment);
		learnerMenu.add(menuItemUseImpugnment);
		menuItemSmallInitial = new java.awt.CheckboxMenuItem("Use small initial array size");
		menuItemSmallInitial.setState(smallInitial);
		learnerMenu.add(menuItemSmallInitial);
		fileMenu.addSeparator();
		menuItemSaveConstraintsFile = new java.awt.CheckboxMenuItem("Save .con file");
		menuItemSaveConstraintsFile.setState(saveConFile);
		learnerMenu.add(menuItemSaveConstraintsFile);
		menuItemC75Output = new java.awt.CheckboxMenuItem("Output by c75");
		menuItemC75Output.setState(saveC75File);
		learnerMenu.add(menuItemC75Output);
		menuItemC90Output = new java.awt.CheckboxMenuItem("Output by c90");
		menuItemC90Output.setState(saveC90File);
		learnerMenu.add(menuItemC90Output);
		menuItemRawRelOutput = new java.awt.CheckboxMenuItem("Output by raw rel");
		menuItemRawRelOutput.setState(saveRawRelFile);
		learnerMenu.add(menuItemRawRelOutput);
		menuItemHitsOutput = new java.awt.CheckboxMenuItem("Output by hits");
		menuItemHitsOutput.setState(saveHitsFile);
		learnerMenu.add(menuItemHitsOutput);
		menuBar.add(learnerMenu);
		menuItemWeightedOutput = new java.awt.CheckboxMenuItem("Output by weighted c75");
		menuItemWeightedOutput.setState(saveWeightedFile);
		learnerMenu.add(menuItemWeightedOutput);
		menuItemFreqOutput = new java.awt.CheckboxMenuItem("Output by token frequency");
		menuItemFreqOutput.setState(saveFreqFile);
		learnerMenu.add(menuItemFreqOutput);
		menuItemWeightedByFreqOutput = new java.awt.CheckboxMenuItem("Output by c75 weighted by freq");
		menuItemWeightedByFreqOutput.setState(saveFreqFile);
		learnerMenu.add(menuItemWeightedByFreqOutput);
		menuBar.add(learnerMenu);

		setMenuBar(menuBar);
		// show();

		setLayout(null);
		resize(insets().left + insets().right + 550, insets().top + insets().bottom + 300);
		setBackground(new Color(12632256));
		labelLanguageName = new java.awt.Label("Language: ");
		labelLanguageName.reshape(insets().left + 25, insets().top + 79, 75, 20);
		labelLanguageName.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(labelLanguageName);
		labelFileName = new java.awt.Label("File:");
		labelFileName.reshape(insets().left + 25, insets().top + 37, 40, 20);
		labelFileName.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(labelFileName);
		labelUserName = new java.awt.Label("User:");
		labelUserName.reshape(insets().left + 25, insets().top + 58, 40, 20);
		labelUserName.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(labelUserName);
		/*
		 * labelCreateDate = new java.awt.Label("Creation Date:");
		 * labelCreateDate.reshape(insets().left + 25,insets().top + 100,100,20);
		 * labelCreateDate.setFont(new Font("Dialog", Font.PLAIN, 12));
		 * add(labelCreateDate); labelLastModified = new
		 * java.awt.Label("Last Modified:"); labelLastModified.reshape(insets().left +
		 * 25,insets().top + 121,100,20); labelLastModified.setFont(new Font("Dialog",
		 * Font.PLAIN, 12)); add(labelLastModified);
		 */
		labelFileNameDisplay = new java.awt.Label(" ");
		labelFileNameDisplay.reshape(insets().left + 65, insets().top + 37, 200, 20);
		labelFileNameDisplay.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(labelFileNameDisplay);
		/*
		 * labelCreateDateDisplay = new java.awt.Label("");
		 * labelCreateDateDisplay.reshape(insets().left + 125,insets().top +
		 * 100,175,20); labelCreateDateDisplay.setFont(new Font("Dialog", Font.PLAIN,
		 * 12)); add(labelCreateDateDisplay); labelLastModifiedDisplay = new
		 * java.awt.Label(""); labelLastModifiedDisplay.reshape(insets().left +
		 * 125,insets().top + 121,175,20); labelLastModifiedDisplay.setFont(new
		 * Font("Dialog", Font.PLAIN, 12)); add(labelLastModifiedDisplay);
		 * labelGlobalComments = new java.awt.Label("Comments:");
		 * labelGlobalComments.reshape(insets().left + 25,insets().top + 142,80,20);
		 * labelGlobalComments.setFont(new Font("Dialog", Font.PLAIN, 12));
		 * add(labelGlobalComments);
		 */
		/*
		 * textAreaGlobalComments = new java.awt.TextArea();
		 * textAreaGlobalComments.reshape(insets().left + 25,insets().top + 163,569,66);
		 * textAreaGlobalComments.setFont(new Font("Dialog", Font.PLAIN, 12));
		 * textAreaGlobalComments.setForeground(new Color(0));
		 * textAreaGlobalComments.setBackground(new Color(16777215));
		 * textAreaGlobalComments.setEditable(false); add(textAreaGlobalComments);
		 */
		buttonOpenFile = new java.awt.Button("Open input file...");
		buttonOpenFile.reshape(insets().left + 290, insets().top + 41, 197, 26);
		buttonOpenFile.setBackground(new Color(16777215));
		add(buttonOpenFile);
		buttonLearnPhonology = new java.awt.Button("Learn Phonology...");
		buttonLearnPhonology.reshape(insets().left + 290, insets().top + 75, 197, 26);
		buttonLearnPhonology.setBackground(new Color(16777215));
		add(buttonLearnPhonology);

		labelUserNameDisplay = new java.awt.Label(" ");
		labelUserNameDisplay.reshape(insets().left + 65, insets().top + 58, 200, 20);
		labelUserNameDisplay.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(labelUserNameDisplay);
		/*
		 * textFieldUserNameDisplay = new java.awt.TextField();
		 * textFieldUserNameDisplay.reshape(insets().left + 65,insets().top +
		 * 58,235,20); textFieldUserNameDisplay.setFont(new Font("Dialog", Font.PLAIN,
		 * 12)); textFieldUserNameDisplay.setForeground(new Color(0));
		 * textFieldUserNameDisplay.setBackground(new Color(16777215));
		 * textFieldUserNameDisplay.setEditable(false); add(textFieldUserNameDisplay);
		 */

		labelLanguageNameDisplay = new java.awt.Label(" ");
		labelLanguageNameDisplay.reshape(insets().left + 100, insets().top + 79, 200, 20);
		labelLanguageNameDisplay.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(labelLanguageNameDisplay);
		/*
		 * textFieldLanguageNameDisplay = new java.awt.TextField();
		 * textFieldLanguageNameDisplay.reshape(insets().left + 100,insets().top +
		 * 79,200,20); textFieldLanguageNameDisplay.setFont(new Font("Dialog",
		 * Font.PLAIN, 12)); textFieldLanguageNameDisplay.setForeground(new Color(0));
		 * textFieldLanguageNameDisplay.setBackground(new Color(16777215));
		 * textFieldLanguageNameDisplay.setEditable(false);
		 * add(textFieldLanguageNameDisplay);
		 */
		/*
		 * checkboxUsePhonology = new java.awt.Checkbox(" Use Phonology");
		 * checkboxUsePhonology.reshape(insets().left + 428,insets().top + 119,143,17);
		 * checkboxUsePhonology.setFont(new Font("Dialog", Font.PLAIN, 12));
		 * add(checkboxUsePhonology); checkboxUsePhonology.setState(phonologyOnFlag);
		 */

		checkboxUseDoppelgaengers = new java.awt.Checkbox(" Use Doppelg�ngers");
		checkboxUseDoppelgaengers.reshape(insets().left + 280, insets().top + 119, 200, 17);
		checkboxUseDoppelgaengers.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxUseDoppelgaengers);
		checkboxUseDoppelgaengers.setState(useDoppelgaengers);

		checkboxUseImpugnment = new java.awt.Checkbox(" Use Impugnment");
		checkboxUseImpugnment.reshape(insets().left + 280, insets().top + 139, 200, 17);
		checkboxUseImpugnment.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxUseImpugnment);
		checkboxUseImpugnment.setState(useImpugnment);

		checkboxUsePhonology = new java.awt.Checkbox(" Use Phonology");
		checkboxUsePhonology.reshape(insets().left + 280, insets().top + 159, 200, 17);
		checkboxUsePhonology.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxUsePhonology);
		checkboxUsePhonology.setState(phonologyOnFlag);

		checkboxUseFeatures = new java.awt.Checkbox(" Use Features");
		checkboxUseFeatures.reshape(insets().left + 280, insets().top + 179, 200, 17);
		checkboxUseFeatures.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxUseFeatures);
		checkboxUseFeatures.setState(useFeatures);

		checkboxIntermediateWugTests = new java.awt.Checkbox(" Intermediate Wug Tests");
		checkboxIntermediateWugTests.reshape(insets().left + 280, insets().top + 199, 250, 17);
		checkboxIntermediateWugTests.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxIntermediateWugTests);
		checkboxIntermediateWugTests.setState(intermediateWugTests);

		checkboxWugtimeLearning = new java.awt.Checkbox(" Wug-time Learning");
		checkboxWugtimeLearning.reshape(insets().left + 280, insets().top + 219, 200, 17);
		checkboxWugtimeLearning.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxWugtimeLearning);
		checkboxWugtimeLearning.setState(wugtimeLearning);

		checkboxSmallInitial = new java.awt.Checkbox(" Use small arrays");
		checkboxSmallInitial.reshape(insets().left + 280, insets().top + 239, 200, 17);
		checkboxSmallInitial.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxSmallInitial);
		checkboxSmallInitial.setState(smallInitial);

		checkboxSaveConstraintsFile = new java.awt.Checkbox(" Save .con file");
		checkboxSaveConstraintsFile.reshape(insets().left + 55, insets().top + 119, 200, 17);
		checkboxSaveConstraintsFile.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxSaveConstraintsFile);
		checkboxSaveConstraintsFile.setState(saveConFile);

		checkboxC75Output = new java.awt.Checkbox(" Unimpugned c75");
		checkboxC75Output.reshape(insets().left + 55, insets().top + 139, 200, 17);
		checkboxC75Output.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxC75Output);
		checkboxC75Output.setState(saveC75File);

		checkboxC90Output = new java.awt.Checkbox(" Unimpugned c90");
		checkboxC90Output.reshape(insets().left + 55, insets().top + 159, 200, 17);
		checkboxC90Output.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxC90Output);
		checkboxC90Output.setState(saveC90File);

		checkboxRawRelOutput = new java.awt.Checkbox(" Raw reliability");
		checkboxRawRelOutput.reshape(insets().left + 55, insets().top + 179, 200, 17);
		checkboxRawRelOutput.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxRawRelOutput);
		checkboxRawRelOutput.setState(saveRawRelFile);

		checkboxHitsOutput = new java.awt.Checkbox(" Type frequency");
		checkboxHitsOutput.reshape(insets().left + 55, insets().top + 199, 200, 17);
		checkboxHitsOutput.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxHitsOutput);
		checkboxHitsOutput.setState(saveHitsFile);

		checkboxWeightedOutput = new java.awt.Checkbox(" Weighted by length");
		checkboxWeightedOutput.reshape(insets().left + 55, insets().top + 219, 200, 17);
		checkboxWeightedOutput.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxWeightedOutput);
		checkboxWeightedOutput.setState(saveWeightedFile);

		checkboxFreqOutput = new java.awt.Checkbox(" Token frequency");
		checkboxFreqOutput.reshape(insets().left + 55, insets().top + 239, 200, 17);
		checkboxFreqOutput.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxFreqOutput);
		checkboxFreqOutput.setState(saveFreqFile);

		checkboxWeightedByFreqOutput = new java.awt.Checkbox(" Weighted by token freq");
		checkboxWeightedByFreqOutput.reshape(insets().left + 55, insets().top + 259, 200, 17);
		checkboxWeightedByFreqOutput.setFont(new Font("Dialog", Font.PLAIN, 12));
		add(checkboxWeightedByFreqOutput);
		checkboxWeightedByFreqOutput.setState(saveWeightedByFreqFile);

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing();
			}
		});

		show();
	}

	// Close the window when the close box is clicked
	public void thisWindowClosing() {
		setVisible(false);
		dispose();
		System.exit(0);
	}

	// __________________________________________________________________________________
	// methods to handle clicks in boxes
	public boolean handleEvent(Event event) {
		// clicking the window close box
		if (event.id == Event.WINDOW_DESTROY) {
			thisWindowClosing();
			return true;
		}
		// clicking the "use phonology" checkbox
		if (event.target == checkboxUsePhonology && event.id == Event.ACTION_EVENT) {
			phonologyOnFlag = checkboxUsePhonology.getState();
			toggleUsePhonology(phonologyOnFlag);
			return true;
		}
		// clicking the "intermediate wug tests" checkbox
		if (event.target == checkboxIntermediateWugTests && event.id == Event.ACTION_EVENT) {
			intermediateWugTests = checkboxIntermediateWugTests.getState();
			toggleIntermediateWugTests(intermediateWugTests);
			return true;
		}
		// clicking the "wug-time learning" checkbox
		if (event.target == checkboxWugtimeLearning && event.id == Event.ACTION_EVENT) {
			wugtimeLearning = checkboxWugtimeLearning.getState();
			toggleWugtimeLearning(wugtimeLearning);
			return true;
		}
		// clicking the "small initial arrays" checkbox
		if (event.target == checkboxSmallInitial && event.id == Event.ACTION_EVENT) {
			smallInitial = checkboxSmallInitial.getState();
			toggleSmallInitial(smallInitial);
			return true;
		}
		// clicking the "UseFeatures" checkbox
		if (event.target == checkboxUseFeatures && event.id == Event.ACTION_EVENT) {
			useFeatures = checkboxUseFeatures.getState();
			toggleFeatures(useFeatures);
			return true;
		}
		// clicking the "UseDoppelgaengers" checkbox
		if (event.target == checkboxUseDoppelgaengers && event.id == Event.ACTION_EVENT) {
			useDoppelgaengers = checkboxUseDoppelgaengers.getState();
			toggleDoppelgaengers(useDoppelgaengers);
			return true;
		}
		// clicking the "UseImpugnment" checkbox
		if (event.target == checkboxUseImpugnment && event.id == Event.ACTION_EVENT) {
			useImpugnment = checkboxUseImpugnment.getState();
			toggleImpugnment(useImpugnment);
			return true;
		} // clicking the "SaveConstraintsFile" checkbox
		if (event.target == checkboxSaveConstraintsFile && event.id == Event.ACTION_EVENT) {
			saveConFile = checkboxSaveConstraintsFile.getState();
			toggleConFile(saveConFile);
			return true;
		} // clicking the "HitsOutput" checkbox
		if (event.target == checkboxHitsOutput && event.id == Event.ACTION_EVENT) {
			saveHitsFile = checkboxHitsOutput.getState();
			toggleHitsFile(saveHitsFile);
			return true;
		} // clicking the "RawRelOutput" checkbox
		if (event.target == checkboxRawRelOutput && event.id == Event.ACTION_EVENT) {
			saveRawRelFile = checkboxRawRelOutput.getState();
			toggleRawRelFile(saveRawRelFile);
			return true;
		} // clicking the "C75Output" checkbox
		if (event.target == checkboxC75Output && event.id == Event.ACTION_EVENT) {
			saveC75File = checkboxC75Output.getState();
			toggleC75File(saveC75File);
			return true;
		} // clicking the "C90Output" checkbox
		if (event.target == checkboxC90Output && event.id == Event.ACTION_EVENT) {
			saveC90File = checkboxC90Output.getState();
			toggleC90File(saveC90File);
			return true;
		}
		// clicking the "WeightedOutput" checkbox
		if (event.target == checkboxWeightedOutput && event.id == Event.ACTION_EVENT) {
			saveWeightedFile = checkboxWeightedOutput.getState();
			toggleWeightedFile(saveWeightedFile);
			return true;
		}
		// clicking the "FreqOutput" checkbox
		if (event.target == checkboxFreqOutput && event.id == Event.ACTION_EVENT) {
			saveFreqFile = checkboxFreqOutput.getState();
			toggleFreqFile(saveFreqFile);
			return true;
		}
		// clicking the "FreqOutput" checkbox
		if (event.target == checkboxWeightedByFreqOutput && event.id == Event.ACTION_EVENT) {
			saveWeightedByFreqFile = checkboxWeightedByFreqOutput.getState();
			toggleWeightedByFreqFile(saveWeightedByFreqFile);
			return true;
		}
		// clicking the "learn phonology" button
		if (event.target == buttonLearnPhonology && event.id == Event.ACTION_EVENT) {
			startTheLearner();
			return true;
		}
		// clicking the "open input file" button
		if (event.target == buttonOpenFile && event.id == Event.ACTION_EVENT) {
			selectedOpenInput();

			return true;
		}

		return super.handleEvent(event);
	} // end of click-only events

	// __________________________________________________________________________________
	public boolean action(Event event, Object arg) {
		// handle menu-related events
		if (event.target instanceof MenuItem) {
			String Label = (String) arg;
			if (Label.equalsIgnoreCase("Learn Phonology")) {
				startTheLearner(); // call method to find a grammar
				return true;
			} else if (Label.equalsIgnoreCase("Close")) {
				thisWindowClosing(); // call method to close the window safely
			} else if (Label.equalsIgnoreCase("Quit")) {
				thisWindowClosing();
				return true;
			} else if (Label.equalsIgnoreCase("Open .in file")) {
				selectedOpenInput(); // call method to open file
				return true;
			} else if (Label.equalsIgnoreCase("Open .fea file")) {
				selectedOpenFeatures(); // call method to open file
				return true;
			} else if (Label.equalsIgnoreCase("Open batch file")) {
				selectedOpenBatch(); // call method to open file
				return true;
			} else if (event.arg.equals(menuItemUsePhonology.getLabel())) {
				phonologyOnFlag = (!menuItemUsePhonology.getState());
				toggleUsePhonology(phonologyOnFlag);
			} else if (event.arg.equals(menuItemIntermediateWugTests.getLabel())) {
				intermediateWugTests = (!menuItemIntermediateWugTests.getState());
				toggleIntermediateWugTests(intermediateWugTests);
			} else if (event.arg.equals(menuItemWugtimeLearning.getLabel())) {
				wugtimeLearning = (!menuItemWugtimeLearning.getState());
				toggleWugtimeLearning(wugtimeLearning);
			} else if (event.arg.equals(menuItemSmallInitial.getLabel())) {
				smallInitial = (!menuItemSmallInitial.getState());
				toggleSmallInitial(smallInitial);
			} else if (event.arg.equals(menuItemUseFeatures.getLabel())) {
				useFeatures = (!menuItemUseFeatures.getState());
				toggleFeatures(useFeatures);
			} else if (event.arg.equals(menuItemUseDoppelgaengers.getLabel())) {
				useDoppelgaengers = (!menuItemUseDoppelgaengers.getState());
				toggleDoppelgaengers(useDoppelgaengers);
			} else if (event.arg.equals(menuItemUseImpugnment.getLabel())) {
				useImpugnment = (!menuItemUseImpugnment.getState());
				toggleImpugnment(useImpugnment);
			} else if (event.arg.equals(menuItemSaveConstraintsFile.getLabel())) {
				saveConFile = (!menuItemSaveConstraintsFile.getState());
				toggleConFile(saveConFile);
			} else if (event.arg.equals(menuItemC75Output.getLabel())) {
				saveC75File = (!menuItemC75Output.getState());
				toggleC75File(saveC75File);
			} else if (event.arg.equals(menuItemC90Output.getLabel())) {
				saveC90File = (!menuItemC90Output.getState());
				toggleC90File(saveC90File);
			} else if (event.arg.equals(menuItemRawRelOutput.getLabel())) {
				saveRawRelFile = (!menuItemRawRelOutput.getState());
				toggleRawRelFile(saveRawRelFile);
			} else if (event.arg.equals(menuItemHitsOutput.getLabel())) {
				saveHitsFile = (!menuItemHitsOutput.getState());
				toggleHitsFile(saveHitsFile);
			} else if (event.arg.equals(menuItemWeightedOutput.getLabel())) {
				saveWeightedFile = (!menuItemWeightedOutput.getState());
				toggleWeightedFile(saveWeightedFile);
			} else if (event.arg.equals(menuItemFreqOutput.getLabel())) {
				saveFreqFile = (!menuItemFreqOutput.getState());
				toggleFreqFile(saveFreqFile);
			} else if (event.arg.equals(menuItemWeightedByFreqOutput.getLabel())) {
				saveWeightedByFreqFile = (!menuItemWeightedByFreqOutput.getState());
				toggleWeightedByFreqFile(saveWeightedByFreqFile);
			}
		}
		return super.action(event, arg);
	} // end of menu-related events

	void toggleUsePhonology(boolean newvalue) {
		// update the checkbox
		this.checkboxUsePhonology.setState(newvalue);
		this.menuItemUsePhonology.setState(newvalue);
	}

	void toggleWugtimeLearning(boolean newvalue) {
		// update the checkbox
		this.checkboxWugtimeLearning.setState(newvalue);
		this.menuItemWugtimeLearning.setState(newvalue);
	}

	void toggleSmallInitial(boolean newvalue) {
		// update the checkbox
		this.checkboxSmallInitial.setState(newvalue);
		this.menuItemSmallInitial.setState(newvalue);
	}

	void toggleIntermediateWugTests(boolean newvalue) {
		// update the checkbox
		this.checkboxIntermediateWugTests.setState(newvalue);
		this.menuItemIntermediateWugTests.setState(newvalue);
	}

	void toggleFeatures(boolean newvalue) {
		// update the checkbox
		this.checkboxUseFeatures.setState(newvalue);
		this.menuItemUseFeatures.setState(newvalue);
	}

	void toggleDoppelgaengers(boolean newvalue) {
		// update the checkbox
		this.checkboxUseDoppelgaengers.setState(newvalue);
		this.menuItemUseDoppelgaengers.setState(newvalue);
	}

	void toggleImpugnment(boolean newvalue) {
		// update the checkbox
		this.checkboxUseImpugnment.setState(newvalue);
		this.menuItemUseImpugnment.setState(newvalue);
	}

	void toggleConFile(boolean newvalue) {
		// update the checkbox
		this.checkboxSaveConstraintsFile.setState(newvalue);
		this.menuItemSaveConstraintsFile.setState(newvalue);
	}

	void toggleC75File(boolean newvalue) {
		// update the checkbox
		this.checkboxC75Output.setState(newvalue);
		this.menuItemC75Output.setState(newvalue);
	}

	void toggleC90File(boolean newvalue) {
		// update the checkbox
		this.checkboxC90Output.setState(newvalue);
		this.menuItemC90Output.setState(newvalue);
	}

	void toggleRawRelFile(boolean newvalue) {
		// update the checkbox
		this.checkboxRawRelOutput.setState(newvalue);
		this.menuItemRawRelOutput.setState(newvalue);
	}

	void toggleHitsFile(boolean newvalue) {
		// update the checkbox
		this.checkboxHitsOutput.setState(newvalue);
		this.menuItemHitsOutput.setState(newvalue);
	}

	void toggleWeightedFile(boolean newvalue) {
		// update the checkbox
		this.checkboxWeightedOutput.setState(newvalue);
		this.menuItemWeightedOutput.setState(newvalue);
	}

	void toggleFreqFile(boolean newvalue) {
		// update the checkbox
		this.checkboxFreqOutput.setState(newvalue);
		this.menuItemFreqOutput.setState(newvalue);
	}

	void toggleWeightedByFreqFile(boolean newvalue) {
		// update the checkbox
		this.checkboxWeightedByFreqOutput.setState(newvalue);
		this.menuItemWeightedByFreqOutput.setState(newvalue);
	}

	// __________________________________________________________________________________
	// methods for menu items
	// __________________________________________________________________________________
	// menu item: Open .in file
	public void selectedOpenInput() {
		// a string to hold the selected filename while we check it:
		String tempFileDirectory, tempFileName;

		FileDialog fileDialog = new FileDialog(this, "Open Learner File:", FileDialog.LOAD);
		fileDialog.show();

		tempFileName = fileDialog.getDirectory() + fileDialog.getFile();

		if (tempFileName == null) {
			System.out.print("Invalid input file! Please try again.\r");
			return;
		}

		// read the selected file
		readInputFile(fileDialog.getDirectory(), fileDialog.getFile());
	}

	// __________________________________________________________________________________
	// menu item: Open .fea file
	public void selectedOpenFeatures() {
		// a string to hold the selected filename while we check it:
		String tempFileDirectory, tempFileName;

		FileDialog fileDialog = new FileDialog(this, "Open Features File:", FileDialog.LOAD);
		fileDialog.show();

		tempFileName = fileDialog.getDirectory() + fileDialog.getFile();

		if (tempFileName == null) {
			System.out.print("No file! Please try again.\r");
			return;
		}

		// set the features file name
		this.featuresFileName = new String(fileDialog.getDirectory() + fileDialog.getFile());
	}

	// __________________________________________________________________________________
	// menu item: Open batch file
	public void selectedOpenBatch() {
		// a string to hold the selected filename while we check it:
		String tempFileName, dir, nextline, nextinputfile;
		StringTokenizer st;
		BufferedReader batchFile;

		DoneDialog done;

		FileDialog fileDialog = new FileDialog(this, "Open Batch File:", FileDialog.LOAD);
		fileDialog.show();

		tempFileName = fileDialog.getDirectory() + fileDialog.getFile();

		if (tempFileName == null) {
			System.out.print("No file! Please try again.\r");
			return;
		}

		// otherwise, go ahead and do a batch run
		try {
			batch = true;
			dir = fileDialog.getDirectory();
			batchFile = new BufferedReader(new FileReader(fileDialog.getDirectory() + fileDialog.getFile()));

			nextline = batchFile.readLine();
			while (nextline != null) {
				// we'll assume that each line is the name of a .in file to process
				st = new StringTokenizer(nextline);
				nextinputfile = st.nextToken();

				readInputFile(dir, nextinputfile);
				startTheLearner();

				// now we need to free up some resources
				this.learnerTask = new LearnerTask();
				System.gc();

				nextline = batchFile.readLine();
			}
		} catch (FileNotFoundException e) {
			System.err.println("Can't open " + filename);
			return;
		} catch (IOException e) {
			System.err.println("Error reading " + filename);
			return;
		}

		done = new DoneDialog(this);
	}

	// __________________________________________________________________________________
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

			// if we've gotten to this point, it means that a file has successfully
			// been read. now open a new learner window and display the info
			setTitle(language_name);

			// textFieldUserNameDisplay.setText( user_name );
			labelUserNameDisplay.setText(user_name);
			// textFieldLanguageNameDisplay.setText( language_name );
			labelLanguageNameDisplay.setText(language_name);
			labelFileNameDisplay.setText(filename);
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

	// __________________________________________________________________________________
	public void startTheLearner() {
		boolean success;
		DoneDialog done;
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

		if (!batch)
			done = new DoneDialog(this);
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

}