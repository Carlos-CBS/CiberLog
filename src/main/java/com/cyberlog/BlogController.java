package com.cyberlog;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BlogController {

    @GetMapping("/create-blog")
    public String showCreateBlogPage() {
        return "create-blog";
    }

    @GetMapping("/")
    @ResponseBody
    public String showHomePage(HttpServletRequest request) {
        return "home, este es tu sessionID " +request.getSession().getId();
    }

    private List<Student> students = new ArrayList<>(List.of(
            new Student(1, "navin", 60),
            new Student(2, "Sol", 80)
    ));

    @GetMapping("/students")
    @ResponseBody
    public List<Student> getStudents() {
        return students;
    }

    @PostMapping("/students")
    @ResponseBody
    public List<Student> addStudent(@RequestBody Student student) {
        students.add(student);
        return students;
    }

    @GetMapping("/crsf-token")
    @ResponseBody
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @PostMapping("/create-blog")
    public String submitBlog(@RequestParam("content") String content, Model model) {
        model.addAttribute("content", content);
        return "preview-blog";
    }
}
