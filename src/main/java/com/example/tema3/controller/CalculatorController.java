package com.example.tema3.controller;

import com.example.tema3.model.Calculator;
import com.example.tema3.service.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/api")
public class CalculatorController {

    @Autowired
    private CalculatorService calculatorService;

    @PostMapping("/do-math")
    public ResponseEntity<String> doMath(@RequestBody List<Calculator> operations) {
        String filename = UUID.randomUUID().toString() + ".txt";
        calculatorService.resetStatusList(filename, operations.size());

        List<Double> results = new ArrayList<>(Collections.nCopies(operations.size(), null));

        for (int i = 0; i < operations.size(); i++) {
            Calculator op = operations.get(i);
            int finalI = i;
            Thread t = new Thread(() -> {
                Double result = calculatorService.performOperationSync(op);
                results.set(finalI, result);
                calculatorService.updateStatus(filename, finalI, true);

                if (!results.contains(null)) {
                    try {
                        calculatorService.saveToFile(filename, results);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }

        return ResponseEntity.ok(filename);
    }

    @GetMapping("/check-finished/{filename}")
    public ResponseEntity<Boolean> checkFinished(@PathVariable String filename) {
        return ResponseEntity.ok(calculatorService.allCalculationsFinished(filename));
    }

    @GetMapping("/results/{filename}")
    public ResponseEntity<Object> getResults(@PathVariable String filename) {

        if(!calculatorService.allCalculationsFinished(filename)) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Operations are still processing. Try again later.");
        }

        try {
            Optional<List<Double>> results = calculatorService.readFromFile(filename);
            if (results.isPresent()) {
                return ResponseEntity.ok(results.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Results not found.");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving results.");
        }
    }


}
