package com.moudjane.crud_spring.Controller;

import com.moudjane.crud_spring.Models.User;
import com.moudjane.crud_spring.Models.Login;
import com.moudjane.crud_spring.Repo.UserRepo;
import com.moudjane.crud_spring.Util.PasswordUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ApiControllers {

    @Autowired
    private final UserRepo userRepo;

    public ApiControllers(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/")
    public String getPage() {
        return "Welcome";
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userRepo.findAll();
    }

    @PostMapping("/login/{username}")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Login login,
            @PathVariable String username,
            HttpServletResponse response
    ) {
        System.out.println("Login attempt for username: " + username);
        User user = userRepo.findByUsername(username);

        Map<String, String> responseBody = new HashMap<>();

        if (user == null) {
            System.out.println("User not found");
            responseBody.put("message", "User not found");
            return ResponseEntity.status(404).body(responseBody);
        }

        String password = login.getPassword();
        System.out.println("Provided password: " + password);
        System.out.println("Stored hashed password: " + user.getPassword());
        boolean isPasswordCorrect = PasswordUtils.checkPassword(password, user.getPassword());

        if (isPasswordCorrect) {
            long id = user.getId();
            System.out.println("Password correct, user ID: " + id);

            // Create a cookie with the user ID
            ResponseCookie cookie = ResponseCookie.from("userId", Long.toString(id))
                    .path("/")
                    .httpOnly(true)
                    .secure(false) // Change to true if you are using HTTPS
                    .sameSite("None") // Add this to ensure the cookie is sent cross-site
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            responseBody.put("message", "Login successful");
            return ResponseEntity.ok(responseBody);
        } else {
            System.out.println("Invalid credentials");
            responseBody.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(responseBody);
        }
    }

    @PostMapping("/register/{username}")
    public ResponseEntity<Map<String, String>> register(@RequestBody Login login, @PathVariable String username) {
        User existingUser = userRepo.findByUsername(username);

        Map<String, String> responseBody = new HashMap<>();

        if (existingUser != null) {
            responseBody.put("message", "Username already taken");
            return ResponseEntity.status(400).body(responseBody);
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(PasswordUtils.hashPassword(login.getPassword()));
        userRepo.save(newUser);

        responseBody.put("message", "User registered successfully");
        return ResponseEntity.status(201).body(responseBody);
    }

    @PostMapping(value = "/save")
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        User savedUser = userRepo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<String> updateUser(@PathVariable long id, @RequestBody User user){
        User updatedUser = userRepo.findById(id).orElse(null);
        if (updatedUser != null) {
            updatedUser.setFirstName(user.getFirstName());
            updatedUser.setLastName(user.getLastName());
            updatedUser.setOccupation(user.getOccupation());
            updatedUser.setAge(user.getAge());
            userRepo.save(updatedUser);
            return ResponseEntity.ok("Updated...");
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id){
        User deleteUser = userRepo.findById(id).orElse(null);
        if (deleteUser != null) {
            userRepo.delete(deleteUser);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}