package com.magazine.backend.controllers;

import com.magazine.backend.models.News;
import com.magazine.backend.models.Picture;
import com.magazine.backend.models.ReadStatus;
import com.magazine.backend.models.User;
import com.magazine.backend.payload.response.MessageResponse;
import com.magazine.backend.repository.NewsRepository;
import com.magazine.backend.repository.PictureRepository;
import com.magazine.backend.repository.ReadStatusRepository;
import com.magazine.backend.repository.UserRepository;
import com.magazine.backend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/news")
public class NewsController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PictureRepository pictureRepository;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    ReadStatusRepository readStatusRepository;

    @PostMapping("/create")
    public ResponseEntity<MessageResponse> createNews(@RequestParam("title") String title,
                                                      @RequestParam("text") String text,
                                                      @RequestParam("status") Boolean status,
                                                      @RequestParam(value = "picture", required = false) MultipartFile picture,
                                                      @RequestParam("validFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
                                                      @RequestParam("validTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.getById(userDetails.getId());

        // Save picture to disk
        String picturePath = null;
        if (picture != null)
            picturePath = "src/main/resources/static/files/" + saveFile(picture);

        // Create News entity
        News news = new News();
        news.setTitle(title);
        news.setText(text);
        news.setCreationDate(LocalDate.now());
        news.setValidFrom(validFrom);
        news.setValidTo(validTo);
        news.setPublished(status);
        news.setAuthor(user);
        newsRepository.save(news);


        // Create Picture entity and set the path
        Picture newsPicture = new Picture();
        newsPicture.setName(picture.getOriginalFilename());
        newsPicture.setPath(picturePath);
        newsPicture.setNews(news);
        pictureRepository.save(newsPicture);

        news.setPictures(newsPicture);
        // Save News entity
        newsRepository.save(news);

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("Article Created Successfully");
        return ResponseEntity.ok(messageResponse);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MessageResponse> updateNews(@PathVariable Long id,
                                                      @RequestParam("title") String title,
                                                      @RequestParam("text") String text,
                                                      @RequestParam("status") Boolean status,
                                                      @RequestParam(value = "picture", required = false) MultipartFile picture,
                                                      @RequestParam("validFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
                                                      @RequestParam("validTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.getById(userDetails.getId());

        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();
        News news = newsRepository.findById(id).get();
        MessageResponse messageResponse = new MessageResponse();
        if ((userRole.equals("ROLE_ADMIN") || (userRole.equals("ROLE_AUTHOR") && user.getId() == news.getAuthor().getId()))) {
            // Save picture to disk
            String picturePath = null;
            if (picture != null) {
                pictureRepository.delete(pictureRepository.findByNews(news));
                picturePath = "src/main/resources/static/files/" + saveFile(picture);
            }

            // Create News entity
            news.setTitle(title);
            news.setText(text);
            news.setCreationDate(LocalDate.now());
            news.setValidFrom(validFrom);
            news.setValidTo(validTo);
            news.setPublished(status);
            news.setAuthor(user);
            newsRepository.save(news);

            // Create Picture entity and set the path
            if (picturePath != null) {
                // Create Picture entity and set the path
                Picture newsPicture = new Picture();
                newsPicture.setName(picture.getOriginalFilename());
                newsPicture.setPath(picturePath);
                newsPicture.setNews(news);
                pictureRepository.save(newsPicture);

                news.setPictures(newsPicture);
                // Save News entity
                newsRepository.save(news);
            }
            messageResponse.setMessage("Successfully Updated");
            return ResponseEntity.ok(messageResponse);
        }

        messageResponse.setMessage("Don't have permission");
        return ResponseEntity.ok(messageResponse);
    }

    private String saveFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path uploadPath = Paths.get("src/main/resources/static/files");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    @GetMapping()
    public List<News> getNews() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.getById(userDetails.getId());

        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();
        if (userRole.equals("ROLE_ADMIN")) {
            return newsRepository.findAll();
        } else if (userRole.equals("ROLE_AUTHOR")) {
            return newsRepository.findByAuthor(user);
        } else {
            List<Long> readNewsIds = readStatusRepository.findReadNewsIdsByUserId(user.getId());
            if (readNewsIds.isEmpty()) {

                return newsRepository.findLast10News();
            } else {
                return newsRepository.findLast10UnreadNews(readNewsIds);
            }
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNewsById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.getById(userDetails.getId());
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        News news = newsRepository.findById(id).get();
        if (news.isPublished() || (userRole.equals("ROLE_ADMIN") || userRole.equals("ROLE_AUTHOR"))) {
            if ((!userRole.equals("ROLE_ADMIN") && !userRole.equals("ROLE_AUTHOR"))) {
                ReadStatus readStatus = new ReadStatus();
                readStatus.setNews(news);
                readStatus.setUser_id(user);
                readStatus.setReadDate(LocalDate.now());
                readStatusRepository.save(readStatus);
            }
            MessageResponse messageResponse = new MessageResponse();
            messageResponse.setData(news);
            return ResponseEntity.ok(messageResponse);
        }
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("Don't have permission to see");
        return ResponseEntity.ok(messageResponse);
    }

    @PutMapping("/publish/{id}")
    public ResponseEntity<?> publishNews(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.getById(userDetails.getId());
        News news = newsRepository.findByAuthorAndNewsID(user, id);
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        MessageResponse messageResponse = new MessageResponse();
        if ((userRole.equals("ROLE_ADMIN") || (userRole.equals("ROLE_AUTHOR") && user.getId() == news.getAuthor().getId()))) {
            news.setPublished(true);
            newsRepository.save(news);
            messageResponse.setMessage("Article Published Successfully");
        } else {
            messageResponse.setMessage("Don't have permission to publish");
        }

        return ResponseEntity.ok(messageResponse);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.getById(userDetails.getId());
        News news = newsRepository.findById(id).get();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        MessageResponse messageResponse = new MessageResponse();
        if ((userRole.equals("ROLE_ADMIN") || (userRole.equals("ROLE_AUTHOR") && user.getId() == news.getAuthor().getId()))) {
            if (pictureRepository.findById(id).isPresent())
                pictureRepository.delete(pictureRepository.findById(id).get());
            readStatusRepository.deleteByNewsId(id);
            newsRepository.delete(news);
            messageResponse.setMessage("Article Deleted Successfully");
        } else {
            messageResponse.setMessage("Don't have permission to publish");
        }

        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getEventImage(@PathVariable Long id) throws IOException {
        Optional<News> newsOptional = newsRepository.findById(id);
        if (newsOptional.isPresent()) {
            News news = newsOptional.get();
            Path imagePath = Paths.get(pictureRepository.findByNews(news).getPath());
            Resource resource = new UrlResource(imagePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
