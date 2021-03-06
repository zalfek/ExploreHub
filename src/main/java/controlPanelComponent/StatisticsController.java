package controlPanelComponent;

import alerts.CustomAlertType;
import authentification.loginProcess.CurrentAccountSingleton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextArea;
import handlers.Convenience;
import handlers.HandleNet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import mainUI.MainPane;
import models.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Class which is responsible for controlling the Statistics tab.
 *
 * @author Aleksejs Marmiss
 */
@SuppressWarnings("JpaQueryApiInspection")
public class StatisticsController {
    @FXML
    private Label usersOnline;
    @FXML
    private Label nrBookReject;
    @FXML
    private Label nrBookCanc;
    @FXML
    private Label nrBookPend;
    @FXML
    private StackPane stackpane;
    @FXML
    private Label averageRating;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button reply;
    @FXML
    private Label nrOfUsers;
    @FXML
    private Label nrOfBookingsPerEvent;
    @FXML
    private Label pastEvents;
    @FXML
    private Label nrOfEvents;
    @FXML
    private Label nrOfBookingsPerUser;
    @FXML
    private Label moneySpent;
    @FXML
    private Label nrBookVal;
    @FXML
    private Label nrOver;
    @FXML
    private NumberAxis y;
    @FXML
    private CategoryAxis x;
    @FXML
    private LineChart<?,?> linechart;
    @FXML
    private Pagination feedbacks;
    private final Account admin = CurrentAccountSingleton.getInstance().getAccount();
    private List<Transactions> transactionsList;
    private List<User> usersList;
    private List<Events> eventsList;
    private List<Feedback> feedbackList;
    private Long nrOfUsersOnline;

    /**
     * Method that initializes the Statisticcs tab.
     * @param eventsList List of events taken from the database.
     * @param usersList List of users taken from the database.
     * @param transactionsList List of transactions taken from the database.
     */
    public void initialize(List<Events> eventsList, List<User> usersList,List<Transactions> transactionsList, Long nrOfUsersOnline) {

        this.eventsList = eventsList;
        this.usersList = usersList;
        this.transactionsList = transactionsList;
        this.nrOfUsersOnline = nrOfUsersOnline;
        calculateStatistics();
    }

    /**
     * Method that loads the feedback from database.
     */
    private void loadFeedbacks(){
        EntityManager entityManager = admin.getConnection();
        TypedQuery<Feedback> feedbackQuery;
        feedbackQuery = entityManager.createNamedQuery(
                "Feedback.findAllFeedbacks",
                Feedback.class);
        if(!HandleNet.hasNetConnection()){
            try {
                throw new Exception("Internet Connection lost");
            }catch(Exception exc){
                Convenience.showAlert(CustomAlertType.WARNING, "Oops, something went wrong. Please, try again later.");
            }
        }
        feedbackList = new ArrayList<>(feedbackQuery.getResultList());
    }

    /**
     * Method which populates a page in pagination.
     * @param pageIndex index of the page which has to be populated.
     * @return VBox with content.
     */
    private VBox createPage(int pageIndex){
        VBox pageBox = new VBox();
        JFXTextArea messageContent = new JFXTextArea();
        messageContent.setWrapText(true);
        messageContent.setMaxWidth(1170);
        messageContent.setMinHeight(150);
        messageContent.setMaxHeight(150);
        messageContent.setEditable(false);
        messageContent.setStyle("-fx-text-fill:  #32a4ba; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: Calisto MT Bold; -fx-font-style: Italic");
        int size = feedbackList.size();
        if(size < 1) {
            feedbacks.setPageCount(1);
        } else {
            feedbacks.setPageCount(feedbackList.size());
        }
        try {
            messageContent.setText(
                            "From: " + feedbackList.get(pageIndex).getUserID().getFirstname()
                            + " " + feedbackList.get(pageIndex).getUserID().getLastname() + "\n" +
                            "Rating: " + feedbackList.get(pageIndex).getRatingScore() + "\n" + "\n" +
                            feedbackList.get(pageIndex).getRatingDescription());
        }catch (IndexOutOfBoundsException ioe){
            messageContent.setText("No feedbacks at the moment....");
        }
        messageContent.setFont(Font.font("Serif", FontWeight.BOLD, 16));

        pageBox.getChildren().add(messageContent);
        return pageBox;
    }

    /**
     * Method which opens the homepage.
     * @param mouseEvent Mouse event triggered by the click of the button.
     */
    public void goHome(MouseEvent mouseEvent) {
        try{
            Convenience.openHome();
        }catch(Exception ex){
            Convenience.showAlert(CustomAlertType.WARNING, "Oops, something went wrong. Please, try again later.");
        }
    }

    /**
     * Method that calculates statistics and displays the data.
     */
    public void calculateStatistics(){
        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < transactionsList.size(); i++) {
            series.getData().add(new XYChart.Data(transactionsList.get(i).getDate().toString(), i));
        }
        linechart.getData().clear();
        linechart.getData().addAll(series);
        Calendar todayDate = Calendar.getInstance();
        Date date = new Date();
        todayDate.setTime(date);
        List<Transactions> listPend = new ArrayList<>();
        List<Transactions> listOver = new ArrayList<>();
        List<Transactions> listAccept = new ArrayList<>();
        List<Transactions> listCanceled = new ArrayList<>();
        List<Transactions> listRejected = new ArrayList<>();
        for (Transactions transaction : transactionsList
        ) {
            if (transaction.getCompleted() == 0) {
                listPend.add(transaction);
            }else if(transaction.getCompleted() == 1){
                listAccept.add(transaction);
            }else if(transaction.getCompleted() == 3){
                listCanceled.add(transaction);
            }else if(transaction.getCompleted() == 2){
                listRejected.add(transaction);
            }

            Calendar transDate = Calendar.getInstance();
            Calendar eventDate = Calendar.getInstance();
            transDate.setTime(transaction.getDate());
            eventDate.setTime(transaction.getEvent().getDate());
            if (todayDate.after(eventDate) && transaction.getCompleted() != 1) {
                listOver.add(transaction);
            }
        }
        nrBookVal.setText(String.valueOf(listAccept.size()));
        nrBookPend.setText(String.valueOf(listPend.size()));
        nrBookCanc.setText(String.valueOf(listCanceled.size()));
        nrBookReject.setText(String.valueOf(listRejected.size()));
        nrOver.setText(String.valueOf(listOver.size()));
        nrOfUsers.setText(String.valueOf(usersList.size()));
        usersOnline.setText(String.valueOf(nrOfUsersOnline));
        DecimalFormat df = new DecimalFormat("0.00");
        nrOfBookingsPerUser.setText(df.format(Double.valueOf(transactionsList.size()) / Double.valueOf(usersList.size())));
        double total = 0;
        for (Transactions transaction : transactionsList
        ) {
            if(transaction.getCompleted() == 1) {
                total += transaction.getEvent().getPrice();
            }
        }
        moneySpent.setText(df.format(total / usersList.size()) + " €");
        nrOfEvents.setText(String.valueOf(eventsList.size()));
        nrOfBookingsPerEvent.setText(df.format(transactionsList.size() / eventsList.size()));
        List<Events> pastEventsList = new ArrayList<>();
        for (Events event : eventsList
        ) {
            Calendar eventDate = Calendar.getInstance();
            eventDate.setTime(event.getDate());
            if (todayDate.after(eventDate)) {
                pastEventsList.add(event);
            }
        }
        pastEvents.setText(String.valueOf(pastEventsList.size()));
        loadFeedbacks();
        double average = 0;
        for (Feedback feedback : feedbackList) {
            average += feedback.getRatingScore();
        }
        average = average/feedbackList.size();
        averageRating.setText(df.format(average));
        feedbacks.setPageFactory(this::createPage);
    }


    /**
     * Opens the admin panel
     *
     * @param mouseEvent - the even which triggered the method
     */
    @FXML
    private void handleRefreshClicked(MouseEvent mouseEvent) throws IOException{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/FXML/PreLoader.fxml"));
        JFXDialogLayout dialogLayout = new JFXDialogLayout();
        loader.load();
        PreLoader controller = (PreLoader) loader.getController();
        dialogLayout.setBody(new JFXProgressBar());
        dialogLayout.setMaxWidth(200);
        JFXDialog dialog = new JFXDialog(MainPane.getInstance().getStackPane(), dialogLayout, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        try {
            controller.setLoading(dialog);
            controller.initialization(false, dialog);
        }catch (Exception e){
            if (!HandleNet.hasNetConnection()) {
                try {
                    Convenience.popupDialog(MainPane.getInstance().getStackPane(), MainPane.getInstance().getBorderPane(), getClass().getResource("/FXML/noInternet.fxml"));
                } catch (IOException e1) {
                    Convenience.showAlert(CustomAlertType.ERROR, "Something went wrong. Please, try again later.");
                }
            } else{
                Convenience.showAlert(CustomAlertType.ERROR, "Something went wrong. Please, try again later.");
            }
        }

        dialog.show();
    }

}




