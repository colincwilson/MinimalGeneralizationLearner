This repository provides a command-line interface for the Minimal Generalization Learner (Albright & Hayes 2002, 2003, etc.) and slightly modified input files for English past-tense simulations. Send comments or questions to colin-dot-wilson-at-jhu-dot-edu.

### **version1/**

The original version of the learner with a GUI interface, files downloaded [06/14/2021] from http://www.mit.edu/~albright/mgl/ and https://linguistics.ucla.edu/people/hayes/RulesVsAnalogy/index.html. MinGenLearner.jar seems to provide the most up-to-date compiled code. Run with: `java -jar MinGenLearner.jar`

### **version2/**

The new version of the learner with a command-line interface. The (only) file added to the original code is src/LearnerCommandline.java. Arguments for the learner are specified with [YAML](https://yaml.org/) files, see for example english.yaml. Run the English past-tense simulation with `00runme.sh`.

This version comes already compiled (with Java 16). Recompile with: `ant -buildfile MinimalGeneralizationLearner.xml`. The one external dependency is [SnakeYAML](https://github.com/asomov/snakeyaml), included here as extern/snakeyaml-1.29.jar.

The new version also has the original GUI interace, run with: `java -jar bin/mingenlearn.jar`

### **english/**

English past-tense data provided by Albright & Hayes, with various transcriptions.

- **English1** and **English2** have the original small and large data sets, respectively. Some erroneous past-tense forms in the original English2 data are listed in English2_errors.txt.

- **English2_unicode** has transcriptions that are closer to IPA but adhere to the one-symbol-per-phoneme format required by the learner; these are the input files for the example simulation in `version2/00runme.sh`. Also in this folder are the English data from the [SIGMORPHON 2021 Shared Task on Generalization in Morphological Inflection Generation](https://github.com/sigmorphon/2021Task0).

- **English2_IPA** has the data in space-separated IPA transcription, which is incompatible with the learner.

- English_phonemes.ods compares the transcription systems and provides a feature matrix.

### **feature matrix format**

The format of feature (.fea) files is quite strict, as follows:

ASCII\<tab\>Seg.\<tab\><long feature names, tab-separated>  
\<tab\>\<tab\><short feature names, tab-separated>  
<id1>\<tab\><symbol1><tab><feature specifications (+1, 0, -1), tab-separated>  
<id2>\<tab\><symbol2><tab><feature specifications, tab-separated>  
...  
<id_m>\<tab\><ˈ>\<tab\><feature specifications of stress symbol, tab-separated>  
<id_n>\<tab\><~>\<tab\><feature specifications of empty symbol, tab-separated>  
  
The 'ASCII' values must be unique integers, but are otherwise arbitrary.  
The stress symbol can be omitted if it does not appear in the transcriptions.
