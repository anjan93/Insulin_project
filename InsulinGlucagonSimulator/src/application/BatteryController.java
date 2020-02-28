package application;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class BatteryController {
	
	// The method will handle change of battery icon
		
			public static void changeBatteryStatus(ImageView batteryImage) {
				MonitorController.counter++;
				if(batteryImage != null) {
					switch(MonitorController.counter) {
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
				        MonitorController.addMessagetoBox("Battery is low. Please plug to charger.", Color.RED);
				        break;
				        
					case 8: 
						MonitorController.addMessagetoBox("Battery is critical!. Please plug to charger.", Color.RED);
				        break;
				        
					default:	
						if(MonitorController.counter == 10) {
							MonitorController.addMessagetoBox("System shut down due to low battery", Color.RED);
							System.exit(0);
						}
						break;
					}	
				}
			}

}
