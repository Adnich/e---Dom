package controllers;

import dao.StudentDAO;
import dao.PrijavaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label lblStudenti;

    @FXML
    private Label lblPrijave;

    private final StudentDAO studentDAO = new StudentDAO();
    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    @FXML
    public void initialize() {
        int totalStudents = studentDAO.countStudents();
        int totalPrijave = prijavaDAO.countPrijave();

        lblStudenti.setText(String.valueOf(totalStudents));
        lblPrijave.setText(String.valueOf(totalPrijave));
    }
}
