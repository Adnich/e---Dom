package controllers;

import dao.StudentDAO;
import dao.PrijavaDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class DashboardController {

    private AdminController adminController;

    // ====== TOP STAT ======
    @FXML private Label lblStudenti;
    @FXML private Label lblPrijave;
    @FXML private PieChart pieStatusChart;
    @FXML private Label lblAdminIme;

    // ====== STATUSI ======
    @FXML private Label lblNaPregledu;
    @FXML private Label lblOdobrene;
    @FXML private Label lblOdbijene;
    @FXML private Label lblBezBodova;
    @FXML private Label lblZakljucene;

    private final StudentDAO studentDAO = new StudentDAO();
    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    // ================= INIT =================
    @FXML
    public void initialize() {

        // STUDENTI
        try {
            lblStudenti.setText(String.valueOf(studentDAO.countStudents()));
        } catch (Exception e) {
            lblStudenti.setText("0");
            e.printStackTrace();
        }

        // PRIJAVE
        try {
            lblPrijave.setText(String.valueOf(prijavaDAO.countPrijave()));
        } catch (Exception e) {
            lblPrijave.setText("0");
            e.printStackTrace();
        }

        initStatusStatistiku();
        initPieChartStatusa();
    }

    private void initPieChartStatusa() {
        try {
            ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                    new PieChart.Data("Na pregledu", prijavaDAO.countPrijaveByStatusId(1)),
                    new PieChart.Data("Odobrene", prijavaDAO.countPrijaveByStatusId(4)),
                    new PieChart.Data("Odbijene", prijavaDAO.countPrijaveByStatusId(5)),
                    new PieChart.Data("Bez bodova", prijavaDAO.countPrijaveBezBodova()),
                    new PieChart.Data("Zaključene", prijavaDAO.countPrijaveByStatusId(3))
            );

            pieStatusChart.setData(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= STATUSI =================
    private void initStatusStatistiku() {
        try {
            // na pregledu (ID = 1)
            lblNaPregledu.setText(
                    String.valueOf(prijavaDAO.countPrijaveByStatusId(1))
            );

            // odobrene (ID = 4)
            lblOdobrene.setText(
                    String.valueOf(prijavaDAO.countPrijaveByStatusId(4))
            );

            // odbijene (ID = 5)
            lblOdbijene.setText(
                    String.valueOf(prijavaDAO.countPrijaveByStatusId(5))
            );

            // bez bodova
            lblBezBodova.setText(
                    String.valueOf(prijavaDAO.countPrijaveBezBodova())
            );
            // zaključene (ID = 3)
            lblZakljucene.setText(
                    String.valueOf(prijavaDAO.countPrijaveByStatusId(3))
            );

        } catch (Exception e) {
            e.printStackTrace();
            lblNaPregledu.setText("0");
            lblOdobrene.setText("0");
            lblOdbijene.setText("0");
            lblBezBodova.setText("0");
        }
    }

    // ================= NAVIGACIJA =================
    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
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
}
