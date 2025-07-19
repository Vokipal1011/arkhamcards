package com.arkham.arkhamcards;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class CardController {

    private final CardRepository cardRepository;

    public CardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Value("${card.image.folder}")
    private String imageFolder;

    @GetMapping("/cards/add")
    public String showAddCardForm() {
        return "add_card";
    }

    @PostMapping("/cards/add")
    public String addCard(@RequestParam CardSource source,
                          @RequestParam CardType type,
                          @RequestParam String subtype,
                          @RequestParam String title,
                          @RequestParam String aspect,
                          @RequestParam MultipartFile image,
                          Model model) throws IOException {

        // Сформируем имя файла
        String filename = type.name().toLowerCase() + "_" + subtype + "_" + title.replaceAll("\\s+", "_") + ".png";
        String fullPath = Paths.get(imageFolder, filename).toString();

        // Сохраняем файл на диск
        image.transferTo(new File(fullPath));

        // Сохраняем мета-данные в БД
        Card card = new Card();
        card.setType(type);
        card.setSubtype(subtype);
        card.setTitle(title);
        card.setAspect(aspect);
        card.setSource(source);
        card.setImagePath(filename); // сохраняем только имя файла, не полный путь

        cardRepository.save(card);

        model.addAttribute("message", "Карта успешно добавлена!");
        return "redirect:/cards/add";
    }


    @GetMapping("/cards/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws MalformedURLException {
        Path imagePath = Paths.get(imageFolder).resolve(filename).normalize();
        Resource resource = new UrlResource(imagePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/cards/delete/{id}")
    public String deleteCard(@PathVariable Long id) {
        cardRepository.findById(id).ifPresent(card -> {
            // Удалим файл изображения
            File imageFile = Paths.get(imageFolder, card.getImagePath()).toFile();
            if (imageFile.exists()) {
                imageFile.delete();
            }

            // Удалим запись из базы
            cardRepository.deleteById(id);
        });

        return "redirect:/cards";
    }


    @GetMapping("/cards/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Карта не найдена: " + id));
        model.addAttribute("card", card);
        return "edit_card";
    }

    @PostMapping("/cards/edit/{id}")
    public String updateCard(@PathVariable Long id,
                             @RequestParam CardSource source,
                             @RequestParam CardType type,
                             @RequestParam String subtype,
                             @RequestParam String title,
                             @RequestParam String aspect,
                             @RequestParam(required = false) MultipartFile image) throws IOException {

        Card card = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Карта не найдена: " + id));

        card.setSource(source);
        card.setType(type);
        card.setSubtype(subtype);
        card.setTitle(title);
        card.setAspect(aspect);

        if (image != null && !image.isEmpty()) {
            String filename = type.name().toLowerCase() + "_" + subtype + "_" + title.replaceAll("\\s+", "_") + ".png";
            String fullPath = Paths.get(imageFolder, filename).toString();
            image.transferTo(new File(fullPath));
            card.setImagePath(filename);
        }

        cardRepository.save(card);

        return "redirect:/cards";
    }

    @GetMapping("/cards")
    public String viewCards(
            @RequestParam(required = false) CardType type,
            @RequestParam(required = false) CardSource source,
            Model model) {

        List<Card> cards = cardRepository.findAll();

        // Фильтрация по типу и источнику, если выбраны
        if (type != null) {
            cards = cards.stream()
                    .filter(c -> c.getType() == type)
                    .toList();
        }

        if (source != null) {
            cards = cards.stream()
                    .filter(c -> c.getSource() == source)
                    .toList();
        }

        model.addAttribute("cards", cards);
        model.addAttribute("allTypes", CardType.values());
        model.addAttribute("allSources", CardSource.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedSource", source);

        return "card_catalog";
    }


}
