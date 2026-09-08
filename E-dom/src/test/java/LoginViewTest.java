import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class LoginViewTest extends ApplicationTest {

    private TextField txtUsername;
    private PasswordField txtPassword;
    private Button btnLogin;
    private Label lblError;
    private CheckBox chkRememberMe;

    @Override
    public void start(Stage stage) throws Exception {
        URL resource = getClass().getResource("login-view.fxml");
        if (resource == null) {
            throw new RuntimeException("FXML file not found at /views/login-view.fxml");
        }
        FXMLLoader loader = new FXMLLoader(resource);
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

        txtUsername = (TextField) scene.lookup("#txtUsername");
        txtPassword = (PasswordField) scene.lookup("#txtPassword");
        btnLogin = (Button) scene.lookup("#btnLogin");
        lblError = (Label) scene.lookup("#lblError");
        chkRememberMe = (CheckBox) scene.lookup("#chkRememberMe");
    }


    @Test
    public void testUsernameFieldIsEmpty() {
        assertNotNull(txtUsername, "Username field not found");
        assertTrue(txtUsername.getText().isEmpty(), "Username field should be empty initially");
    }

    @Test
    public void testPasswordFieldIsEmpty() {
        assertNotNull(txtPassword, "Password field not found");
        assertTrue(txtPassword.getText().isEmpty(), "Password field should be empty initially");
    }

    @Test
    public void testLoginButtonExists() {
        assertNotNull(btnLogin, "Login button not found");
    }

    @Test
    public void testUsernameInput() {
        clickOn(txtUsername);
        write("testuser");
        assertEquals("testuser", txtUsername.getText(), "Username input failed");
    }

    @Test
    public void testPasswordInput() {
        clickOn(txtPassword);
        write("password123");
        assertEquals("password123", txtPassword.getText(), "Password input failed");
    }

    @Test
    public void testLoginButtonClickable() {
        clickOn(txtUsername);
        write("testuser");
        clickOn(txtPassword);
        write("password123");
        clickOn(btnLogin);
    }

    @Test
    public void testRememberMeCheckBox() {
        assertNotNull(chkRememberMe, "Remember me checkbox not found");
        assertFalse(chkRememberMe.isSelected(), "Checkbox should not be selected initially");
        clickOn(chkRememberMe);
        assertTrue(chkRememberMe.isSelected(), "Checkbox should be selected after click");
    }
}
