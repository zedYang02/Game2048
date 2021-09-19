module com.example.game2048_javafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.game2048_javafx to javafx.fxml;
    exports com.example.game2048_javafx;
}