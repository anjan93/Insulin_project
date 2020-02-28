package application;

import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Login {
	
	@FXML
	private Label status;
	
	@FXML
	private TextField username;
	
	@FXML
	private TextField password;
	
	@FXML
	private TextField doctorname;
	
	@FXML
	private TextField doctorpassword;
	
	@FXML Label doctorStatus;
	
	Stage primaryStage ;
	
	public void login(ActionEvent event) throws Exception 
	{
		if (username.getText().equalsIgnoreCase("John") && password.getText().equals("123")) 
		{
			//status.setText("Login Success!");
			 primaryStage = (Stage)((Node)event.getSource()).getScene().getWindow();
			primaryStage.close();
			primaryStage.setOnCloseRequest(confirmCloseEventHandler);
			//Stage primaryStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Insulin Glucgon Pump");
			primaryStage.show();
			
			/*
			 * primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			 * 
			 * @Override public void handle(WindowEvent event) { Alert alert = new
			 * Alert(AlertType.WARNING); alert.setTitle("Alert Dialog");
			 * alert.setHeaderText("Confirmation");
			 * alert.setContentText("Do you want to exit?"); alert.show();
			 * 
			 * } });
			 */

		}
		else
		{
			status.setText("Invalid Credentials!!!");
		}
	}
	
	public void doctorLogin(ActionEvent event) throws Exception 
	{
		if(doctorname.getText().equalsIgnoreCase("Doctor") && doctorpassword.getText().equalsIgnoreCase("123")) 
		{
			MonitorController.isAutoMode = false;
			Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
			window.close();
		}
		else {
			doctorStatus.setText("Login Failed!!!");
		}
	}
	
	public void cancelDoctorLogin(ActionEvent event) {
		MonitorController.isAutoMode = true;
		Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
		window.close();
	}
	
	public void stop(ActionEvent event) throws Exception 
	{
		System.out.println("Application Terminated....");
		System.exit(0);
	}

	 private EventHandler<WindowEvent> confirmCloseEventHandler = event -> {
	        Alert closeConfirmation = new Alert(
	                Alert.AlertType.CONFIRMATION,
	                "Are you sure you want to exit?"
	        );
	        Button exitButton = (Button) closeConfirmation.getDialogPane().lookupButton(
	                ButtonType.OK
	        );
	        exitButton.setText("Exit");
	        closeConfirmation.setHeaderText("Confirm Exit");
	        closeConfirmation.initModality(Modality.APPLICATION_MODAL);
	        closeConfirmation.initOwner(primaryStage);

	        // normally, you would just use the default alert positioning,
	        // but for this simple sample the main stage is small,
	        // so explicitly position the alert so that the main window can still be seen.
	        closeConfirmation.setX(720);
	        closeConfirmation.setY(320);

	        Optional<ButtonType> closeResponse = closeConfirmation.showAndWait();
	        if (!ButtonType.OK.equals(closeResponse.get())) {
	            event.consume();
	        }
	    };
}
