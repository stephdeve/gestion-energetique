module org.example.gestionenergetique {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires de.jensd.fx.glyphs.materialdesignicons;
    requires java.desktop;
    requires java.xml.crypto;
    requires bcrypt;
    requires com.fazecast.jSerialComm;

    exports org.example.gestionenergetique;
    exports org.example.gestionenergetique.controller;
    opens org.example.gestionenergetique to javafx.fxml;
}