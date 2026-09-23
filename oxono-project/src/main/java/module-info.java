module be.esi.dev.oxono {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    exports be.esi.dev.oxono.main;
    exports be.esi.dev.oxono.controller.fxml;
    exports be.esi.dev.oxono.model;
    exports be.esi.dev.oxono.setup;

    opens be.esi.dev.oxono.controller.fxml to javafx.fxml;
}