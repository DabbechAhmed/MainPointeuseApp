module com.example.mainapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.mainapp to javafx.fxml;
    opens com.example.dto to javafx.base;
    exports com.example.mainapp;
    exports com.example.mainapp.controller;
    opens com.example.mainapp.controller to javafx.fxml;
    exports com.example.mainapp.controller.employee;
    opens com.example.mainapp.controller.employee to javafx.fxml;
    exports com.example.mainapp.controller.attendance;
    opens com.example.mainapp.controller.attendance to javafx.fxml;
    exports com.example.mainapp.controller.departement;
    opens com.example.mainapp.controller.departement to javafx.fxml;
    exports com.example.mainapp.model;
    opens com.example.mainapp.model to javafx.fxml;
}