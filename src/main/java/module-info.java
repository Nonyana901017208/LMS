module com.example.javafxgroupassignment {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires org.apache.pdfbox;
    requires org.postgresql.jdbc;


    opens com.example.javafxgroupassignment to javafx.fxml;
    exports com.example.javafxgroupassignment;
}