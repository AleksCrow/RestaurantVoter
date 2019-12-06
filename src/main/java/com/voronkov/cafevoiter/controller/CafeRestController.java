package com.voronkov.cafevoiter.controller;

import com.voronkov.cafevoiter.model.Cafe;
import com.voronkov.cafevoiter.model.User;
import com.voronkov.cafevoiter.repository.CrudCafeRepository;
import com.voronkov.cafevoiter.service.CafeService;
import com.voronkov.cafevoiter.to.CafeTo;
import com.voronkov.cafevoiter.utils.CafeUtil;
import com.voronkov.cafevoiter.validator.CafeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("cafes")
public class CafeRestController {

    private static Logger log = LoggerFactory.getLogger(CafeRestController.class);

    @Autowired
    private CafeService cafeService;

//    @Autowired
//    private CrudCafeRepository cafeRepository;

    private final CafeValidator cafeValidator;

    @Autowired
    public CafeRestController(CrudCafeRepository cafeRepository, CafeValidator cafeValidator) {
        this.cafeValidator = cafeValidator;
    }

//    @GetMapping
//    public List<Cafe> getAll() {
//        return cafeRepository.findAll();
//    }

    @GetMapping
    public List<CafeTo> getAllCafes(@AuthenticationPrincipal User user) {
        return CafeUtil.getCafesWithVotes(cafeService.getAll());
    }

    @GetMapping("{id}")
    public CafeTo getOne(@PathVariable("id") int id) {
        return CafeUtil.createWithVote(cafeService.getById(id), false);
    }

    @PostMapping
    public Cafe createCafe(@RequestBody Cafe cafe, BindingResult result) {
        cafeValidator.validate(cafe, result);
        if (!result.hasErrors()) {
            cafe.setCreatedDate(LocalDateTime.now());
            return cafeService.save(cafe);
        }
        return null;
    }

    @PutMapping("{id}")
    public Cafe update(@PathVariable("id") Cafe cafeFromDb, @RequestBody Cafe cafe) {
        //cafeFromDb - кафе из бд, которе редактируем, берём его значения и заменяем новыми, всеми кроме id и даты
        BeanUtils.copyProperties(cafe, cafeFromDb, "id", "date");
        return cafeService.save(cafeFromDb);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Cafe cafe) {
        cafeService.delete(cafe);
    }

    @GetMapping("{id}/vote")
    public Cafe vote(@AuthenticationPrincipal User currentUser, @PathVariable("id") Cafe cafe) {

        log.info("IN ЮЗЕР: {}", currentUser);
        log.info("IN КАФЕ: {}", cafe);

        Set<User> votes = cafe.getVotes();

        if (votes.contains(currentUser)) {
            votes.remove(currentUser);
        } else {
            votes.add(currentUser);
        }
        return cafe;
    }
}


/*команды для консоли

// GET all
fetch('/message/').then(response => response.json().then(console.log))

// GET one
        fetch('/message/2').then(response => response.json().then(console.log))

// POST add new one
        fetch(
        '/message',
        {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: 'Fourth message (4)', id: 10 })
        }
        ).then(result => result.json().then(console.log))

// PUT save existing
        fetch(
        '/message/4',
        {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: 'Fourth message', id: 10 })
        }
        ).then(result => result.json().then(console.log));

// DELETE existing
        fetch('/message/4', { method: 'DELETE' }).then(result => console.log(result))*/