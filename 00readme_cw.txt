Original files downloaded [06/14/2021] from:
https://linguistics.ucla.edu/people/hayes/RulesVsAnalogy/index.html

= JAR =

MinGenLearner.jar seems to be most recent version (?)

Run with:
java -jar MinGenLearner.jar

= Inputs =

English2/
Original English input files (based on CELEX)
Missing / incorrect past-tense forms:
    bid ~ bid, not bade
    kneel ~ kneeled, not knelt (**double)
    put ~ put
    let ~ let
    read ~ read
    set ~ set
    cut ~ cut
    strike ~ struck
    hit ~ hit
    spread ~ spread
    beat ~ beat
    shut ~ shut
    hurt ~ hurt
    cast ~ cast
    burst ~ burst
    split ~ split
    depend ~ depended
    weep ~ wept
    spin ~ spun
    thrust ~ thrusted
    shed ~ shed
    weave ~ wove
    sneak ~ snuck, not sneaked
    lord ~ lorded
    stink ~ stunk, not stank
    slit ~ slit
    fend ~ fended
    slink ~ slunk
    sod ~ sodded
    bell ~ belled
    bide ~ bided
    grit ~ gritted
    toe ~ toed
    bid (orthography indicates past "bade")

English2_unicode/
[CW] Working English input files converted to unicode

English2_unicode/sigmorphon2021
[CW] Sigmorphon2021 wugs retranscribed and added to unicode input files

English2_ipa/
[CW] Feature and inputs from converted to IPA; incompatible with MinGenLearner because transcriptions are space-separated
xxx todo: replace with files from Google Drive

= Source code =

source/
Source for older (original?) version

source/src/LearnerTask.java
ASCII values (first column in feature matrix) are ignored!

source_cw/
[CW] Commandline interface, minor code fixes (GUI), manifest file, ant buildfile
[CW] Build: ant -buildfile MinimalGeneralizationLearner.xml
[CW] Run GUI: java -jar bin/mingenlearn.jar
[CW] Run on commandline with settings in config.yaml:
java -server -cp $CLASSPATH:extern/snakeyaml-1.29.jar:bin/mingenlearn.jar LearnerCommandline config.yaml
