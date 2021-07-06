public class InputParadigm {
    // a paradigm has a declension, a gloss, and maybe some comments
    String declension[];
    String gloss;
    int frequency;
    StringBuffer comments;

    // now the constructor
    public InputParadigm() {
        frequency = 0;
        gloss = new String();
        comments = new StringBuffer();
        // won't initialize the declension yet because we don't know how big to make it
    }

    public InputParadigm(int mcats) {
        declension = new String[mcats];
        frequency = 0;
        gloss = new String();
        comments = new StringBuffer();
        // won't initialize the declension yet because we don't know how big to make it
    }

}// end of InputParadigm class definition