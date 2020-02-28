package application;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import com.sun.javafx.scene.control.skin.ProgressIndicatorSkin;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MonitorController 
{
	
	static int counter = 0;
	static int maxDataPoint = 0;
	
	// properties for insulin and glucagon level
	static float insulinLevel = 100;
	static float glucagonLevel = 100;
	
	// properties to check raise in BSL
	static int decreasingCarboCounter = 0;
	static int increasingCarboCounter = 1;
	
	protected static boolean checkForBsl = false;
	protected static boolean showHighBslMsg = true;
	protected static boolean showStableBslMsg = true;
	
	public static boolean isAutoMode = true;
	public static boolean autoInjectionStarted = false;
	
	public static boolean inManualInject = false;
	public static boolean inManualIdealDecrease = false;
	
	static XYChart.Series<String, Number> sugarLevelSeries;
	static XYChart.Series<String, Number> minSugarLevelSeries;
	static XYChart.Series<String, Number> maxSugarLevelSeries;
	static LineChart<String, Number> _bloodSugarLevelChart;
	
	private String actEat = "Food";
	private String actExercise = "Exercise";
	
	
	@FXML Label dateAndTime;
	@FXML Button autobutton;
	
	//making manual mode button visible
	@FXML TextField insulindosage;
	@FXML TextField glucagondosage;
	@FXML Button insulindosageButton;
	@FXML Button glucagondosageButton;
	
	@FXML ImageView batteryImage;
	
	@FXML ListView<Text> messageBox;
	
	@FXML LineChart<String, Number> bslChart;
	@FXML NumberAxis yaxis;
	
	@FXML TextField currentBSL;
	@FXML TextField previousBSL;
	
	//Progress bar and Indicator
	@FXML ProgressBar insulinCapacity;
	@FXML ProgressBar glucagonCapacity;
	
	@FXML ProgressIndicator insulinPercentage;
	@FXML ProgressIndicator glucagonPercentage;
	
	//Activity
	
	@FXML ChoiceBox<String> activityType;
	@FXML TextField carbsIntake;
	@FXML Label carbsLabel;
	@FXML Button confirmIntake;
	
	private static ObservableList<Text> msgBoxItems = FXCollections.observableArrayList();
	;
	@FXML
    void initialize() 
	{	
		LocalDate currentDate = LocalDate.now(); // 2020-01-17
		DayOfWeek dow = currentDate.getDayOfWeek();
		dateAndTime.setText(currentDate+", "+dow);
		
		messageBox.setItems(msgBoxItems);
		addMessagetoBox("Welcome John!",Color.DARKGREEN);
		
		setInsulinAndGlucagonCapacity(insulinCapacity,insulinPercentage);
		setInsulinAndGlucagonCapacity(glucagonCapacity,glucagonPercentage);
		
		initializeBSLChart();
		
		currentBSL.setText(Integer.toString(StaticValuesforBSL.InitialBSL)+"  mg/dl");
		previousBSL.setText(Integer.toString(StaticValuesforBSL.InitialBSL)+"  mg/dl");
		
		// Battery TimeLine
		
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(30000), x -> BatteryController.changeBatteryStatus(batteryImage)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
        // BSL TimeLine
        Timeline bslTimeline = new Timeline();
        bslTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000), (ActionEvent actionEvent) -> InitializeBloodSugarLevelSimulator()));
        bslTimeline.setCycleCount(Animation.INDEFINITE);
        bslTimeline.play(); 
        
        activityType.getItems().clear();
        activityType.getItems().add(actEat);
        activityType.getItems().add(actExercise);
        activityType.setValue(actEat);
		
        activityType.setOnAction(e -> selectOption());
	}
	
	private void InitializeBloodSugarLevelSimulator() 
	{
		maxDataPoint++;
		
		if(_bloodSugarLevelChart != null) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String time = dtf.format(now).toString();
			
			
			sugarLevelSeries.getData().add(new Data<String, Number>(time, getChangedBSL()));
			maxSugarLevelSeries.getData().add(new Data<String, Number>(time, StaticValuesforBSL.MaximumBloodSugarLevel));
			minSugarLevelSeries.getData().add(new Data<String, Number>(time, StaticValuesforBSL.MinimumBloodSugarLevel));
			
			if(maxDataPoint > 40) {
				minSugarLevelSeries.getData().remove(0);
				sugarLevelSeries.getData().remove(0);
				maxSugarLevelSeries.getData().remove(0);
			}
		}
		
	}

	private double getChangedBSL() 
	{
		double bsl = 0;
		
		// will be true when user take food or exercise
		if(checkForBsl) {	
			if(isAutoMode) {

				if(StaticValuesforBSL.TempBSL > 120 && StaticValuesforBSL.CurrentBSL > 120) {
					displayDifferenceinBSL(StaticValuesforBSL.CurrentBSL);
					autoInjectionStarted = true;
	
					bsl = BloodSugarLevelCalculation.getInstance().bslOnInsulinDosage(injectInsulin(true));
					StaticValuesforBSL.CurrentBSL = bsl;
				}
				else if(StaticValuesforBSL.TempBSL < 80 && StaticValuesforBSL.CurrentBSL < 80) {
					
					if(StaticValuesforBSL.TempBSL < 70 && StaticValuesforBSL.CurrentBSL < 70) {
						displayDifferenceinBSL(StaticValuesforBSL.CurrentBSL);
						autoInjectionStarted = true;			
					}
	
					bsl = BloodSugarLevelCalculation.getInstance().bslOnGlucagonDosage(injectGlucagon(true));
					StaticValuesforBSL.CurrentBSL = bsl;
					
				}
				else if(StaticValuesforBSL.CurrentBSL < 120 && autoInjectionStarted){
					bsl = BloodSugarLevelCalculation.getInstance().bslOnIdeal();
				}
				else {
					bsl = BloodSugarLevelCalculation.getInstance().bslAfterActivity(StaticValuesforBSL.CarbohydrateIntake , 3 * increasingCarboCounter);
				}
	
				bsl = Double.parseDouble(new DecimalFormat("###.##").format(bsl));
				
				if(bsl != StaticValuesforBSL.CurrentBSL) {
					StaticValuesforBSL.TempBSL = StaticValuesforBSL.CurrentBSL;
				}
				StaticValuesforBSL.CurrentBSL = bsl;		
			}
			else if(!isAutoMode){
				displayDifferenceinBSLInManual(StaticValuesforBSL.CurrentBSL);
				if(!inManualInject) {
					bsl = BloodSugarLevelCalculation.getInstance().bslAfterActivity(StaticValuesforBSL.CarbohydrateIntake , 1 * increasingCarboCounter);
				}
				else if (inManualIdealDecrease || (StaticValuesforBSL.CurrentBSL >= 70 && StaticValuesforBSL.CurrentBSL <= 120)) {
					bsl = BloodSugarLevelCalculation.getInstance().bslOnIdeal();
				}
				else {
					if(StaticValuesforBSL.CurrentBSL > 120) {
						//displayDifferenceinBSLInManual(StaticValuesforBSL.CurrentBSL);
						bsl = BloodSugarLevelCalculation.getInstance().bslOnInsulinDosage(injectInsulin(false));
					}
					else if(StaticValuesforBSL.CurrentBSL < 70) {
						//displayDifferenceinBSLInManual(StaticValuesforBSL.CurrentBSL);
						bsl = BloodSugarLevelCalculation.getInstance().bslOnGlucagonDosage(injectInsulin(false));
					}
					inManualInject = true;
					inManualIdealDecrease = true;
				}			
				
				bsl = Double.parseDouble(new DecimalFormat("###.##").format(bsl));		
				if(bsl != StaticValuesforBSL.CurrentBSL) {
					StaticValuesforBSL.TempBSL = StaticValuesforBSL.CurrentBSL;
				}
				StaticValuesforBSL.CurrentBSL = bsl;
			}
			increasingCarboCounter++;
		}
		
		currentBSL.setText(Double.toString(StaticValuesforBSL.CurrentBSL)+"  mg/dl");
		previousBSL.setText(Double.toString(StaticValuesforBSL.TempBSL)+"  mg/dl");
	    return  StaticValuesforBSL.CurrentBSL;     
	}

	private double injectGlucagon(boolean auto) {
		double glucagon = 0;
		
		if(auto) {
			glucagon =	CalculationOfInsulinGlucagonDosage.getGlucagonDosageValue(StaticValuesforBSL.CurrentBSL);
		}
		else {
			String glucagonInjected = glucagondosage.getText();					
			if(glucagonInjected.isEmpty()) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Glucagon Injection");
		        alert.setContentText("Glucagon cannot be empty");
		        alert.show();
			}
			glucagon = Double.parseDouble(glucagonInjected);
			if(glucagon > 1) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Glucagon");
		        alert.setContentText("Cannot inject more than 2 unit of Glucagon");
		        alert.show();
			}
		}
		if(glucagon > 0 && glucagon<=1) {
			glucagonLevel -= glucagon;
			StaticValuesforBSL.CurrentBSL = BloodSugarLevelCalculation.getInstance().bslOnGlucagonDosage(glucagon);
			
			float glucagonLevelIndicator = glucagonLevel/100;
			if(glucagonLevelIndicator <= 0.5) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Low Glucagon");
		        alert.setContentText("Glucagon Reservior is very low. Please Refill the reservior!");
		        alert.show();
				addMessagetoBox("Glucagon Reservior is very low. Please Refill the reservior!", Color.RED);
			}
			double bslAfterGlucagon = BloodSugarLevelCalculation.getInstance().bslOnGlucagonDosage(glucagon);
			addMessagetoBox("After " + glucagon + " of glucagon injection, BSL increased from " + StaticValuesforBSL.CurrentBSL + " to " + bslAfterGlucagon + "mg/dl");
			glucagonCapacity.setProgress(glucagonLevelIndicator);
		}	
		return glucagon;
	}

	private double injectInsulin(boolean auto) {
		double insulin = 0;
		if(auto) {
			insulin = CalculationOfInsulinGlucagonDosage.getInsulinDosageValue(StaticValuesforBSL.CurrentBSL);
		}
		else {
			String insulinInjected = insulindosage.getText();					
			if(insulinInjected.isEmpty()) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Insulin Injection");
		        alert.setContentText("Insulin cannot be empty");
		        alert.show();
			}
			insulin = Double.parseDouble(insulinInjected);
			if(insulin > 1) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Insulin");
		        alert.setContentText("Cannot inject more than 1 unit of Insulin");
		        alert.show();
			}
		}
		if(insulin > 0) {
			insulinLevel -= insulin;
			float insulinLevelIndicator = insulinLevel/100;
				
			if(insulinLevelIndicator <= 0.5) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Low Insulin");
		        alert.setContentText("Insulin Reservior is very low. Please Refill the reservior!");
		        alert.show();
		        addMessagetoBox("Insulin Reservior is very low. Please Refill the reservior!", Color.RED);
			}
			double bslAfterInsulin = BloodSugarLevelCalculation.getInstance().bslOnInsulinDosage(insulin);
			addMessagetoBox("After " + insulin + " of insulin injection, BSL decreased from " + StaticValuesforBSL.CurrentBSL + " to " + bslAfterInsulin + "mg/dl");
			insulinCapacity.setProgress(insulinLevelIndicator);
		}
		return insulin;
	}

	// The method will show messages if BSL goes above or below the band
		private void displayDifferenceinBSL(double bsl) {
			if(bsl > StaticValuesforBSL.MaximumBloodSugarLevel && showHighBslMsg) {
				addMessagetoBox("Blood Sugar Level exceeded the threshold level. Insulin needs to be injected!", Color.RED);
				showHighBslMsg = false;
			}
			else if(bsl < StaticValuesforBSL.MinimumBloodSugarLevel && showHighBslMsg) {
				addMessagetoBox("Blood Sugar Level is very Low. Glucagon needs to be injected!", Color.RED);
				showHighBslMsg = false;
			}
			
			if((bsl >= StaticValuesforBSL.MinimumBloodSugarLevel && bsl <= StaticValuesforBSL.MaximumBloodSugarLevel) && showStableBslMsg) {
				addMessagetoBox("Blood Sugar Level is back to stable!", Color.GREEN);
				showStableBslMsg = false;
			}	
		}
	
		private void displayDifferenceinBSLInManual(double bsl) {
			//System.out.println("Current BSL ==> "+bsl);
			if(bsl > StaticValuesforBSL.MaximumBloodSugarLevel) {
				addMessagetoBox("Blood Sugar Level exceeded the threshold level. Insulin needs to be injected!", Color.RED);
				showHighBslMsg = false;
			}
			else if(bsl < StaticValuesforBSL.MinimumBloodSugarLevel) {
				addMessagetoBox("Blood Sugar Level is very Low. Glucagon needs to be injected!", Color.RED);
				showHighBslMsg = false;
			}
			
		/*
		 * if((bsl >= StaticValuesforBSL.MinimumBloodSugarLevel && bsl <=
		 * StaticValuesforBSL.MaximumBloodSugarLevel) && showStableBslMsg) {
		 * addMessagetoBox("Blood Sugar Level is back to stable!", Color.GREEN);
		 * showStableBslMsg = false; }
		 */	
		}
		
	private void setInsulinAndGlucagonCapacity(ProgressBar insulinGlucagonCapacity, ProgressIndicator insulinGlucagonPercentage)
	{
		try {
			insulinGlucagonPercentage.progressProperty().bind(insulinGlucagonCapacity.progressProperty());
			setInsulinGlucagonPercentage(insulinGlucagonPercentage);
			insulinGlucagonCapacity.setProgress(1);	
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		
	}

	
	private void setInsulinGlucagonPercentage(ProgressIndicator insulinGlucagonPercentage) 
	{
		ProgressIndicatorSkin pis = new ProgressIndicatorSkin(insulinGlucagonPercentage);
		insulinGlucagonPercentage.skinProperty().set(pis);
		insulinGlucagonPercentage.progressProperty().addListener(new ChangeListener<Number>( ) {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if(newValue.doubleValue() >= 1) {
					//System.out.println("Here!...");
					((Text) insulinGlucagonPercentage.lookup(".text.percentage")).setText("100%");
				}			
			}		
		});
		
	}

	private void initializeBSLChart() {
		addMessagetoBox("..Blood Sugar Level Simulator Started..");
		sugarLevelSeries = new XYChart.Series<String, Number>();
		sugarLevelSeries.setName("Current Blood Sugar Level");
		
		maxSugarLevelSeries = new XYChart.Series<String, Number>();
		maxSugarLevelSeries.setName("Maximum Blood Sugar Level (" + StaticValuesforBSL.MaximumBloodSugarLevel + ")");
		
		minSugarLevelSeries = new XYChart.Series<String, Number>();
		minSugarLevelSeries.setName("Minimum Blood Sugar Level (" + StaticValuesforBSL.MinimumBloodSugarLevel + ")");
		
		yaxis.setAutoRanging(false);
		yaxis.setLowerBound(0);
		yaxis.setUpperBound(300);
		yaxis.setTickUnit(50);
		yaxis.setLabel("Blood Sugar Level");
		
		
		if(bslChart != null) {
			addMessagetoBox("The Blood Sugar Level is Stable :  " +  StaticValuesforBSL.CurrentBSL + "mg/dl", Color.GREEN);
			
			_bloodSugarLevelChart = bslChart;	
			DateTimeFormatter dtformat = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalDateTime currentTime = LocalDateTime.now();
			String time1 = dtformat.format(currentTime).toString();
			minSugarLevelSeries.getData().add(new Data<String, Number>(time1, StaticValuesforBSL.MinimumBloodSugarLevel));
			sugarLevelSeries.getData().add(new Data<String, Number>(time1, StaticValuesforBSL.CurrentBSL));
			maxSugarLevelSeries.getData().add(new Data<String, Number>(time1, StaticValuesforBSL.MaximumBloodSugarLevel));
			
			_bloodSugarLevelChart.getData().add(sugarLevelSeries);
			_bloodSugarLevelChart.getData().add(maxSugarLevelSeries);
			_bloodSugarLevelChart.getData().add(minSugarLevelSeries);	
		}
		
	}

	public void rechargeBattery(MouseEvent event) throws Exception
	{
		counter = 0;
		batteryImage.setImage(new Image("Images/full.PNG"));
		addMessagetoBox("Battery recharged to full capacity", Color.GREEN);
	}
	
	public void enableManualMode(ActionEvent event) throws Exception
	{
		if(isAutoMode) {
			Parent loginForm = FXMLLoader.load(getClass().getResource("/application/DoctorLogin.fxml"));
			Scene scene = new Scene(loginForm);	
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Doctor Login");
			window.showAndWait();
			
			String modeName = isAutoMode ? "Auto" : "Manual";
			
			if(!isAutoMode) {
				addMessagetoBox("Doctor Login was Successful!");
				addMessagetoBox("The Mode will be changed to " + modeName);
				//grpManualInj.setVisible(true);
				autobutton.setText(modeName);
				insulindosage.setDisable(false);
				glucagondosage.setDisable(false);
				insulindosageButton.setDisable(false);
				glucagondosageButton.setDisable(false);
				
			}
		}
		else {
			//grpManualInj.setVisible(false);
			isAutoMode = true;
			addMessagetoBox("The Mode will be changed to Auto");
			autobutton.setText("Auto");
			insulindosage.setDisable(true);
			glucagondosage.setDisable(true);
			insulindosageButton.setDisable(true);
			glucagondosageButton.setDisable(true);
		}
		autobutton.setStyle(isAutoMode ? "-fx-background-color:  #90EE90" : "-fx-background-color: grey;");
	}
	
	
	/* The method will handle change of battery icon
		public void changeBatteryIcon() {
			counter++;
			if(batteryImage != null) {
				switch(counter) {
				case 2: 
					batteryImage.setImage(new Image("images/full.PNG"));
					break;
					
				case 3:
					batteryImage.setImage(new Image("images/medium.PNG"));
					break;
					
				case 4:
					batteryImage.setImage(new Image("images/low.PNG"));
					break;
							
				case 5: 
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Alert Dialog");
			        alert.setHeaderText("Low Battery");
			        alert.setContentText("Please plug in to charger, else the system will shut down");
			        alert.show();
			        addMessagetoBox("Battery is low. Please plug to charger.", Color.RED);
			        break;
			        
				case 8: 
			        addMessagetoBox("Battery is critical!. Please plug to charger.", Color.RED);
			        break;
			        
				default:	
					if(counter == 10) {
						addMessagetoBox("System shut down due to low battery", Color.RED);
						System.exit(0);
					}
					break;
				}	
			}
		}
	*/
	// method to close Application
	public void powerOff(MouseEvent event) throws Exception 
	{
		if(buildAlertMessage(AlertType.CONFIRMATION, "Confirmation Dialog", "System Shutdown", "The system will shutdown. Do you want to continue?")) {
			System.exit(0);
		}

	}


	// Generic method to build alert messages
	public Boolean buildAlertMessage(AlertType alertType, String title, String headerText, String contextText) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
        	return true;
        }
        return false;
	}
	
	
	// The method to add messages to list item
		public static void addMessagetoBox(String message, Color color) {
			Text msg = new Text(message);
			msg.setStroke(color);
			msg.setFont(new Font(15));
			msgBoxItems.add(msg);
			
			if (msgBoxItems.size() > 10) {
				msgBoxItems.remove(0);
			}

		}
		
		// The method to add messages to list item
		public static void addMessagetoBox(String message) 
		{
			addMessagetoBox(message,Color.GREY);

		}
		
		public void injectInsulinManually(ActionEvent event) 
		{
			if (StaticValuesforBSL.CurrentBSL >= 70 && StaticValuesforBSL.CurrentBSL <= 120) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Insulin Injection");
		        alert.setContentText("Blood Sugar Level is stable. No need to Inject Insulin");
		        alert.show();
		        return;
			}
			else if(StaticValuesforBSL.CurrentBSL<=70) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Insulin Injection");
		        alert.setContentText("Cannot Inject Insulin when Blood Sugar level is low");
		        alert.show();
		        return;
			}
			//System.out.println("StaticValuesforBSL.CurrentBSL === >"+StaticValuesforBSL.CurrentBSL);
			inManualInject = true;
			inManualIdealDecrease = false;
		}
		
		public void injectGlucagonManually(ActionEvent event)
		{
			if (StaticValuesforBSL.CurrentBSL >= 70 && StaticValuesforBSL.CurrentBSL <= 120) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Glucagon Injection");
		        alert.setContentText("Blood Sugar Level is stable. No need to Inject Glucagon");
		        alert.show();
		        return;
			}
			else if(StaticValuesforBSL.CurrentBSL>=120) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Alert Dialog");
		        alert.setHeaderText("Glucagon Injection");
		        alert.setContentText("Cannot Inject Glucagon when Blood Sugar level is High");
		        alert.show();
		        return;
			}
			injectGlucagon(false);
			//double glucagonLevel = glucagonCapacity.getProgress();
			//double ipGlucagon = Double.parseDouble(glucagondosage.getText());
			//addMessagetoBox(glucagonLevel + "" + ipGlucagon);
		}
		
		private void selectOption() {
			String selectedValue = activityType.getValue();
			if(selectedValue == actExercise) {
				carbsIntake.setVisible(false);
				carbsLabel.setVisible(false);
			}
			else {
				carbsIntake.setVisible(true);
				carbsLabel.setVisible(true);
			}
			
		}
		
		public void confirmActivity(ActionEvent event) throws IOException {
			double carbo = 0;
			String selectedValue = activityType.getValue();
			
			if(selectedValue == actExercise) {
				carbo -= 30;
				addMessagetoBox("Amount of Carbohydrates lost : " + carbo + "");
			}
			else {
				carbo = Double.parseDouble(carbsIntake.getText());
				addMessagetoBox("Amount of Food/Carbs taken : " + carbo + " g");
			}
			
			StaticValuesforBSL.CarbohydrateIntake = carbo;
			
			double bsl = BloodSugarLevelCalculation.getInstance().bslAfterActivity(carbo, 0);
			if(selectedValue == actExercise) 
			{
				addMessagetoBox("Blood Sugar Level decreased from " + bsl + " mg/dl");
			}
			else {
				addMessagetoBox("Blood Sugar Level increased from " + bsl + " mg/dl");
			}
			setInsulinControllerDefaultValue();
			
			StaticValuesforBSL.PreviousBSL = StaticValuesforBSL.CurrentBSL;
			StaticValuesforBSL.CurrentBSL = bsl;
			StaticValuesforBSL.TempBSL = bsl;
			
		}
		private void setInsulinControllerDefaultValue() {
			checkForBsl = true;
			showHighBslMsg = true;
			increasingCarboCounter = 1;
			autoInjectionStarted = false;
			inManualIdealDecrease = false;
			inManualInject = false;
		}
		
		// The event will handle the insulin refill click
		public void insulinRecharge(MouseEvent event) {
			insulinLevel = 100;
			insulinCapacity.setProgress(1);
			addMessagetoBox("Insulin Bank is Refilled! ", Color.GREEN);
			
		}
		
		// The event will handle the glucagon refill click
		public void glucagonRecharge(MouseEvent event) {
			glucagonLevel = 100;
			glucagonCapacity.setProgress(1);
			addMessagetoBox("Glucagon Bank is Refilled! ", Color.GREEN);
		}
				
}
