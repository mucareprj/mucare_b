package mucare.prj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("mucare.prj.mapper")
public class MucareApplication {

	public static void main(String[] args) {
		SpringApplication.run(MucareApplication.class, args);
	}

}

