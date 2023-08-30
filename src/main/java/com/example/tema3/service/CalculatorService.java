package com.example.tema3.service;

import com.example.tema3.model.Calculator;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalculatorService {
    private Map<String, List<Boolean>> statusMap = Collections.synchronizedMap(new HashMap<>());
    public Double calculate(Calculator op) {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (op == null) {
            throw new IllegalArgumentException("Operation object null");
        }

        String operation = op.getOperator();
        if (operation == null) {
            throw new IllegalArgumentException("Operation type null");
        }

        List<Double> operands = op.getOperands();
        if (operands == null || operands.size() < 2) {
            throw new IllegalArgumentException("You need more operands");
        }

        switch (operation) {
            case "sum":
                return operands.stream().mapToDouble(Double::doubleValue).sum();
            case "sub":
                return operands.get(0) - operands.get(1);
            case "mul":
                return operands.get(0) * operands.get(1);
            case "div":
                if (operands.get(1) == 0) {
                    throw new ArithmeticException("Div by zero");
                }
                return operands.get(0) / operands.get(1);
            default:
                throw new IllegalArgumentException("Error");
        }

    }


    public void saveToFile(String filename, List<Double> results) throws IOException {
        Path path = Paths.get(filename);
        Files.write(path, results.stream().map(String::valueOf).collect(Collectors.toList()));
    }


    public Optional<List<Double>> readFile(String filename) throws IOException {
        Path path = Paths.get(filename);
        if (Files.exists(path)) {
            List<String> lines = Files.readAllLines(path);
            return Optional.of(lines.stream().map(Double::valueOf).collect(Collectors.toList()));
        }
        return Optional.empty();
    }

    public synchronized void setStatusList(String filename, int size) {
        List<Boolean> statusList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            statusList.add(false);
        }
        statusMap.put(filename, statusList);
    }

    public synchronized boolean checkFinished(String filename) {
        List<Boolean> statusList = statusMap.get(filename);
        if (statusList == null) {
            return false;
        }
        return !statusList.contains(false);
    }

    public synchronized void checkStatus(String filename, int index, boolean status) {
        List<Boolean> statusList = statusMap.get(filename);
        if (statusList != null && index >= 0 && index < statusList.size()) {
            statusList.set(index, status);
        }
    }
}