module nouveau.jeuxoint {
    requires javafx.controls;
    requires javafx.fxml;

    requires transitive javafx.graphics;

    opens nouveau.jeuxoint to javafx.fxml;
    exports nouveau.jeuxoint;
}
