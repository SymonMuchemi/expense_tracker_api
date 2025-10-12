package com.muchemi.expense_tracker;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@OpenAPIDefinition(
        info = @Info(
                title = "Expense Tracker API",
                version = "1.0",
                description = "API for managing personal expenses"
        )
)
@SpringBootApplication
@RestController
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }

    @GetMapping
    public String helloWorld() {
        return "Hello from Expense Tracker API";
    }
}
