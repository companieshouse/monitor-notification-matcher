package uk.gov.companieshouse.monitornotification.matcher;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static final String NAMESPACE = "monitor-subscription-matcher";

    public static void main(String[] args) {
		run(Application.class, args);
	}

}
