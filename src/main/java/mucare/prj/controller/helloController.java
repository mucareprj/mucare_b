package mucare.prj.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class helloController {
    @GetMapping("/api/pb/hopesuccess")
    public String hello() {
        return "성공기원 마음위로 화이팅!!!!!";
    }
}
