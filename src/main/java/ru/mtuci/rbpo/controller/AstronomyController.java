package ru.mtuci.rbpo.controller;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AstronomyController {

    /** Возвращает текст: приветствие наблюдателю. */
    @GetMapping("/greeting")
    public String greeting(@RequestParam(defaultValue = "наблюдатель") String name) {
        return "Ясного неба, " + name + "!";
    }

    /** Возвращает числа: номера объектов каталога Мессье в заданном диапазоне (1..110). */
    @GetMapping("/messier")
    public List<Integer> messier(@RequestParam(defaultValue = "1") int from,
                                 @RequestParam(defaultValue = "10") int to) {
        int start = Math.max(1, from);
        int end = Math.min(110, to);
        return IntStream.rangeClosed(start, end).boxed().toList();
    }
}
