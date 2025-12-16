package controllers;

import dao.StudentDAO;
import dao.PrijavaDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {
    private AdminController adminController;
    @FXML private Label lblStudenti;
    @FXML private Label lblPrijave;

    private final StudentDAO studentDAO = new StudentDAO();
    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    @FXML

    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }

    public void initialize() {
        try { lblStudenti.setText(String.valueOf(studentDAO.countStudents())); }
        catch (Exception e) { lblStudenti.setText("0"); e.printStackTrace(); }

        try { lblPrijave.setText(String.valueOf(prijavaDAO.countPrijave())); }
        catch (Exception e) { lblPrijave.setText("0"); e.printStackTrace(); }
    }

    // Ovo radi samo ako je dashboard-view.fxml učitan unutar AdminController contentArea (StackPane)
    private StackPane findContentArea() {
        // dashboard root je StackPane -> parent chain do contentArea
        // najčešće je Parent -> StackPane(contentArea). Ako ne nađe, samo neće raditi.
        return null;
    }

    @FXML
    private void goPrijave(ActionEvent e) {
        if (adminController != null) {
            adminController.showPrijave(null);
        }
    }

    @FXML
    private void goStudenti(ActionEvent e) {
        if (adminController != null) {
            adminController.showStudenti(null);
        }
    }

    private void loadIntoAdminContent(String fxmlPath) {
        try {
            Parent view = new FXMLLoader(getClass().getResource(fxmlPath)).load();

            // pokušaj: dashboard je u contentArea (StackPane) pa uzmi root i zamijeni
            // root = StackPane (dash-root). Njegov parent bi trebao biti contentArea.
            // Najsigurnije: uzmi bilo koji node (npr lblPrijave) i idi na parent.
            var node = lblPrijave;
            if (node == null) return;

            var parent = node.getScene() != null ? node.getScene().getRoot() : null;
            // Ako je admin layout drugačiji, preskoči — dashboard i dalje radi bez ovoga.
            // Ovdje ne forsiram da ne pukne aplikacija.
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
