package com.app.frontend;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    // ---- Health ----
    public String checkHealth() throws Exception { return get("/api/health"); }

    // ---- Students ----
    public List<Map<String, Object>> getStudents() throws Exception { return getList("/api/students"); }
    public Map<String, Object> createStudent(Map<String, Object> data) throws Exception { return post("/api/students", data); }
    public Map<String, Object> updateStudent(long id, Map<String, Object> data) throws Exception { return put("/api/students/" + id, data); }
    public boolean deleteStudent(long id) throws Exception { return delete("/api/students/" + id); }

    // ---- Courses ----
    public List<Map<String, Object>> getCourses() throws Exception { return getList("/api/courses"); }
    public Map<String, Object> createCourse(Map<String, Object> data) throws Exception { return post("/api/courses", data); }
    public Map<String, Object> updateCourse(long id, Map<String, Object> data) throws Exception { return put("/api/courses/" + id, data); }
    public boolean deleteCourse(long id) throws Exception { return delete("/api/courses/" + id); }

    // ---- Doctors ----
    public List<Map<String, Object>> getDoctors() throws Exception { return getList("/api/doctors"); }
    public Map<String, Object> createDoctor(Map<String, Object> data) throws Exception { return post("/api/doctors", data); }
    public Map<String, Object> updateDoctor(long id, Map<String, Object> data) throws Exception { return put("/api/doctors/" + id, data); }
    public boolean deleteDoctor(long id) throws Exception { return delete("/api/doctors/" + id); }

    // ---- Enrollments ----
    public List<Map<String, Object>> getEnrollments() throws Exception { return getList("/api/enrollments"); }
    public Map<String, Object> enroll(long studentId, long courseId) throws Exception {
        return post("/api/enrollments", Map.of("studentId", studentId, "courseId", courseId));
    }
    public void setGrade(long enrollmentId, String grade) throws Exception {
        patch("/api/enrollments/" + enrollmentId + "/grade", Map.of("grade", grade));
    }
    public boolean dropEnrollment(long id) throws Exception { return delete("/api/enrollments/" + id); }

    // ---- HTTP Helpers ----

    private String get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + path)).GET().build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private List<Map<String, Object>> getList(String path) throws Exception {
        Type t = new TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(get(path), t);
    }

    private Map<String, Object> post(String path, Object data) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(data))).build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        Type t = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(resp.body(), t);
    }

    private Map<String, Object> put(String path, Object data) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(data))).build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        Type t = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(resp.body(), t);
    }

    private void patch(String path, Object data) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(data))).build();
        httpClient.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private boolean delete(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + path)).DELETE().build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 204;
    }
}
