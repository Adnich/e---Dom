import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

public class test extends ApplicationTest {

    private TextField txtUsername;
    private PasswordField txtPassword;
    private Button btnLogin;
    private Label lblError;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

        txtUsername = (TextField) scene.lookup("#txtUsername");
        txtPassword = (PasswordField) scene.lookup("#txtPassword");
        btnLogin = (Button) scene.lookup("#btnLogin");
        lblError = (Label) scene.lookup("#lblError");
    }

    @Test
    public void testUsernameFieldIsEmpty() {
        verifyThat(txtUsername, control -> control.getText().isEmpty());
    }

    @Test
    public void testPasswordFieldIsEmpty() {
        verifyThat(txtPassword, control -> control.getText().isEmpty());
    }

    @Test
    public void testLoginButtonIsClickable() {
        clickOn(txtUsername);
        write("testuser");
        clickOn(txtPassword);
        write("password123");
        clickOn(btnLogin);
    }

    @Test
    public void testRememberMeCheckBox() {
        clickOn("#chkRememberMe");
    }
}
