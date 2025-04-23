module com.example.ruangonglab1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.sql;
    requires guru.nidi.graphviz;
    // ✅ 允许 FXML 通过反射访问 controller
    opens application to javafx.fxml;
    opens application.controller to javafx.fxml;

    // 如果有其他 controller 包，也需要对应 opens
    exports application;
    exports application.controller;
}
