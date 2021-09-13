This repository provides a commandline interface for the Minimal Generalization Learner (Albright & Hayes 2002, 2003, etc.) and slightly modified input files for English past-tense simulations.

Contents:

- **version1/**  
  The original version of the learner with a GUI interface, files downloaded [06/14/2021] from [](https://linguistics.ucla.edu/people/hayes/RulesVsAnalogy/index.html) and [](http://www.mit.edu/~albright/mgl/). MinGenLearner.jar seems to provide the most up-to-date compiled version. Run with: java -jar MinGenLearner.jar

- **version2/**  
  The version of the learner with a commandline interface; see src/LearnerCommandline.java for the (only) file that was added to the original version. Commandline arguments are specified in [YAML](https://yaml.org/) files, for example english.yaml. See 00runme.sh for an example of how to run this version.

  The commandline version comes already compiled (with Java 16). Recompile with: ant -buildfile MinimalGeneralizationLearner.xml. The one external dependency is [SnakeYAML](https://github.com/asomov/snakeyaml), included here as extern/snakeyaml-1.29.jar.

  The new version includes the original GUI interace, run it with: java -jar bin/mingenlearn.jar

* **english/**  
  English past-tense data provided by Albright & Hayes, with various transcriptions. **English1** and **English2** have the original small and large data sets, respectively. Some errors in the original English2 data are listed in errors.txt. **English2_unicode** has transcriptions that are closer to IPA but adhere to the one-symbol-per-phoneme requirement of the learner; this gives the input files for the example simulation mentioned above. Also in this folder are the English data from the [SIGMORPHON 2021 Shared Task on Generalization in Morphological Inflection Generation](https://github.com/sigmorphon/2021Task0). **English2_IPA** has the data in space-separated IPA transcription, so is incompatible with the learner. English_phonemes.ods compares the transcription systems and provides a feature matrix.

Send comments or questions to colin-dot-wilson-at-jhu-dot-edu
