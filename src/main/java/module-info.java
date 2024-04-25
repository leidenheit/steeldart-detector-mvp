module de.leidenheit.steeldartdetectormvp {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires opencv;
    requires lombok;

    opens de.leidenheit.steeldartdetectormvp to javafx.fxml;
    exports de.leidenheit.steeldartdetectormvp;

    opens de.leidenheit.steeldartdetectormvp.detection to javafx.fxml;
    exports de.leidenheit.steeldartdetectormvp.detection;

    opens de.leidenheit.steeldartdetectormvp.steps to javafx.fxml;
    exports de.leidenheit.steeldartdetectormvp.steps;

    opens de.leidenheit.steeldartdetectormvp.steps.dartboard to javafx.fxml;
    exports de.leidenheit.steeldartdetectormvp.steps.dartboard;

    opens de.leidenheit.steeldartdetectormvp.steps.dart to javafx.fxml;
    exports de.leidenheit.steeldartdetectormvp.steps.dart;

    opens de.leidenheit.steeldartdetectormvp.steps.eval to javafx.fxml;
    exports de.leidenheit.steeldartdetectormvp.steps.eval;

    opens de.leidenheit.steeldartdetectormvp.steps.home to javafx.fxml;
    exports de.leidenheit.steeldartdetectormvp.steps.home;
}
