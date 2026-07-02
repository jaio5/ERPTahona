package alicanteweb.erp.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
public class WebUiController {

    @GetMapping("/")
    public String root() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/app")
    public String app() {
        return "redirect:/web/dashboard";
    }

    @GetMapping("/app/{moduleId}")
    public String appModule() {
        return "redirect:/web/dashboard";
    }
}
