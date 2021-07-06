import java.io.*;

public class PhonologicalRule {
    char structuralDescription[];
    char structuralChange[];

    // the default constructor:
    public PhonologicalRule() {
        structuralDescription = new char[0];
        structuralChange = new char[0];
    }

    // also a constructor with 2 arguments:
    public PhonologicalRule(char desc[], char change[]) {
        structuralDescription = desc;
        structuralChange = change;
    }

    public void write(DataOutputStream file) throws IOException {
        String strucDescString = new String(structuralDescription);
        String strucChangeString = new String(structuralChange);

        byte desc[] = new byte[3];
        if (strucDescString != null)
            strucDescString.getBytes(0, strucDescString.length(), desc, 0);

        file.write(desc);

        byte change[] = new byte[3];
        if (strucChangeString != null)
            strucChangeString.getBytes(0, strucChangeString.length(), change, 0);
        file.write(change);
    }

    public void read(RandomAccessFile file) throws IOException {
        String strucDescString, strucChangeString;

        byte desc[] = new byte[3];
        file.readFully(desc);
        strucDescString = new String(desc, 0);
        strucDescString = strucDescString.trim();

        structuralDescription = strucDescString.toCharArray();

        byte change[] = new byte[3];
        file.readFully(change);
        strucChangeString = new String(change, 0);
        strucChangeString = strucChangeString.trim();

        structuralChange = strucChangeString.toCharArray();
    }

}