/*
    A basic extension of the java.awt.Dialog class
 */

import java.awt.*;

class DoneDialog extends Dialog {
    private Label label;
    private Button okButton;
    private Panel p, p2;
    private LearnerFrame parent;

    public DoneDialog(Frame f) {
        super(f, "Yahoo!", true);
        parent = (LearnerFrame) f;

        okButton = new Button("OK");
        p = new Panel();
        p2 = new Panel();
        label = new Label("Done processing forms!");

        p.add(label);
        p2.add(okButton);

        add("Center", p);
        add("South", p2);
        resize(200, 100);
        show();
    }

    public boolean handleEvent(Event event) {
        if (event.id == Event.WINDOW_DESTROY) {
            removeDialog();
            return true;
        }
        return super.handleEvent(event);
    }

    public boolean action(Event e, Object o) {
        if (e.target == okButton)
            removeDialog();

        return true;
    }

    public void removeDialog() {
        hide();
        dispose();
    }
}
