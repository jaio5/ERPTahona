package alicanteweb.erp.controller;

import alicanteweb.erp.service.ClienteService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

@Component
public class MainController {
    @FXML
    private Label lbWelcome;

    private final ClienteService ClienteService;
    public MainController(ClienteService ClienteService) {
        this.ClienteService = ClienteService;
    }
    @FXML
    public void initialize(){
        lbWelcome.setText("Welcome");

        System.out.println("Número de clientes: " + ClienteService.countClientes());
    }
}