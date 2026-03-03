package com.app.controller;

import com.app.model.Course;
import com.app.model.Doctor;
import com.app.repository.CourseRepository;
import com.app.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseRepository courseRepository;
    private final DoctorRepository doctorRepository;

    public CourseController(CourseRepository courseRepository, DoctorRepository doctorRepository) {
        this.courseRepository = courseRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Body: {"courseCode":"CS101","courseName":"Intro","credits":3,"doctorId":1} */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Course course = new Course();
        course.setCourseCode((String) body.get("courseCode"));
        course.setCourseName((String) body.get("courseName"));
        course.setCredits(body.get("credits") != null ? ((Number) body.get("credits")).intValue() : 3);

        if (body.get("doctorId") != null) {
            Long doctorId = ((Number) body.get("doctorId")).longValue();
            Doctor doc = doctorRepository.findById(doctorId).orElse(null);
            if (doc == null) return ResponseEntity.badRequest().body(Map.of("error", "Doctor not found"));
            course.setDoctor(doc);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(courseRepository.save(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return courseRepository.findById(id)
                .map(c -> {
                    if (body.containsKey("courseCode")) c.setCourseCode((String) body.get("courseCode"));
                    if (body.containsKey("courseName")) c.setCourseName((String) body.get("courseName"));
                    if (body.containsKey("credits")) c.setCredits(((Number) body.get("credits")).intValue());
                    if (body.containsKey("doctorId")) {
                        Long docId = body.get("doctorId") != null ? ((Number) body.get("doctorId")).longValue() : null;
                        c.setDoctor(docId != null ? doctorRepository.findById(docId).orElse(null) : null);
                    }
                    return ResponseEntity.ok(courseRepository.save(c));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
