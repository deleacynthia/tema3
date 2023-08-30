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
        calculatorService.setStatusList(filename, operations.size());

        List<Double> results = new ArrayList<>(Collections.nCopies(operations.size(), null));

        for (int i = 0; i < operations.size(); i++) {
            Calculator op = operations.get(i);
            int aux = i;
            Thread t = new Thread(() -> {
                Double result = calculatorService.calculate(op);
                results.set(aux, result);
                calculatorService.checkStatus(filename, aux, true);

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
        return ResponseEntity.ok(calculatorService.checkFinished(filename));
    }

    @GetMapping("/results/{filename}")
    public ResponseEntity<Object> getResults(@PathVariable String filename) {

        if(!calculatorService.checkFinished(filename)) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Op still in process");
        }

        try {
            Optional<List<Double>> results = calculatorService.readFile(filename);
            if (results.isPresent()) {
                return ResponseEntity.ok(results.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Res not found");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }


}
