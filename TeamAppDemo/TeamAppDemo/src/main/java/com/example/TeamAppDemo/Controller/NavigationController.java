
package com.example.TeamAppDemo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationController {
    @GetMapping({"/", "/index"})
    public String index() {
        return "forward:/index.html";
    }
}
