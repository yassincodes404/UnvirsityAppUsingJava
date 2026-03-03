package com.app.frontend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class App extends Application {

    private final ApiClient api = new ApiClient("http://localhost:8081");
    private Label statusLabel;
    private StackPane contentArea;
    private final List<Button> navButtons = new ArrayList<>();

    // Data
    private final ObservableList<StudentRow> studentList = FXCollections.observableArrayList();
    private final ObservableList<DoctorRow> doctorList = FXCollections.observableArrayList();
    private final ObservableList<CourseRow> courseList = FXCollections.observableArrayList();
    private final ObservableList<EnrollRow> enrollList = FXCollections.observableArrayList();

    private List<Map<String, Object>> doctorsData = new ArrayList<>();

    private Label statStudents, statDoctors, statCourses, statEnrollments;

    @Override
    public void start(Stage stage) {

        // Header
        Label title = new Label("🎓  Student Information System");
        title.getStyleClass().add("header-title");
        statusLabel = new Label("Connecting...");
        statusLabel.getStyleClass().add("header-status");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(title, spacer, statusLabel);
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_LEFT);

        // Sidebar
        VBox sidebar = createSidebar();

        // Content
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: #f0f2f5;");
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        HBox body = new HBox(sidebar, contentArea);
        VBox.setVgrow(body, Priority.ALWAYS);

        VBox root = new VBox(header, body);
        Scene scene = new Scene(root, 1060, 660);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("SIS – Student Information System");
        stage.setScene(scene);
        stage.show();

        showView("dashboard");
        asyncRun(() -> {
            try { api.checkHealth(); setStatus("✅ Connected", false); }
            catch (Exception e) { setStatus("❌ Backend offline", true); }
        });
        refreshAll();
    }

    // ============================= SIDEBAR =============================

    private VBox createSidebar() {
        Button b1 = navBtn("📊  Dashboard",    "dash");
        Button b2 = navBtn("👤  Students",      "stud");
        Button b3 = navBtn("🩺  Doctors",       "doct");
        Button b4 = navBtn("📚  Courses",       "cour");
        Button b5 = navBtn("📝  Enrollments",   "enro");
        b1.getStyleClass().add("nav-button-active");
        VBox sb = new VBox(4, b1, b2, b3, b4, b5);
        sb.getStyleClass().add("sidebar");
        return sb;
    }

    private Button navBtn(String text, String id) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> showView(id));
        navButtons.add(btn);
        return btn;
    }

    private void showView(String id) {
        navButtons.forEach(b -> b.getStyleClass().remove("nav-button-active"));
        navButtons.stream().filter(b -> b.getText().toLowerCase().contains(id)).findFirst()
                .ifPresent(b -> b.getStyleClass().add("nav-button-active"));
        Node view = switch (id) {
            case "stud" -> buildStudentsView();
            case "doct" -> buildDoctorsView();
            case "cour" -> buildCoursesView();
            case "enro" -> buildEnrollmentsView();
            default -> buildDashboard();
        };
        contentArea.getChildren().setAll(view);
    }

    // ============================= DASHBOARD =============================

    private Node buildDashboard() {
        Label h = new Label("Dashboard");
        h.getStyleClass().add("section-title");

        statStudents = new Label(String.valueOf(studentList.size()));
        statDoctors = new Label(String.valueOf(doctorList.size()));
        statCourses = new Label(String.valueOf(courseList.size()));
        statEnrollments = new Label(String.valueOf(enrollList.size()));

        HBox stats = new HBox(16,
                statCard("👤", statStudents, "Students"),
                statCard("🩺", statDoctors, "Doctors"),
                statCard("📚", statCourses, "Courses"),
                statCard("📝", statEnrollments, "Enrollments"));

        Label hint = new Label("Navigate using the sidebar to manage the university data.");
        hint.setStyle("-fx-text-fill: #78909c; -fx-font-size: 13px; -fx-padding: 10 0;");

        VBox v = new VBox(14, h, stats, hint);
        v.setPadding(new Insets(4));
        return v;
    }

    private VBox statCard(String icon, Label val, String label) {
        Label ic = new Label(icon); ic.setStyle("-fx-font-size: 24px;");
        val.getStyleClass().add("stat-value");
        Label lb = new Label(label); lb.getStyleClass().add("stat-label");
        VBox c = new VBox(4, ic, val, lb);
        c.getStyleClass().add("stat-card");
        c.setAlignment(Pos.CENTER_LEFT);
        c.setPrefWidth(180);
        return c;
    }

    private void updateStats() {
        Platform.runLater(() -> {
            if (statStudents != null) statStudents.setText(String.valueOf(studentList.size()));
            if (statDoctors != null) statDoctors.setText(String.valueOf(doctorList.size()));
            if (statCourses != null) statCourses.setText(String.valueOf(courseList.size()));
            if (statEnrollments != null) statEnrollments.setText(String.valueOf(enrollList.size()));
        });
    }

    // ============================= STUDENTS =============================

    @SuppressWarnings("unchecked")
    private Node buildStudentsView() {
        Label h = new Label("Manage Students"); h.getStyleClass().add("section-title");

        TableView<StudentRow> table = new TableView<>(studentList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                col("Student ID", "studentId", 90),
                col("First Name", "firstName", 100),
                col("Last Name", "lastName", 100),
                col("Email", "email", 160),
                col("Major", "major", 120),
                numCol("Year", "yearLevel", 50),
                col("Enrolled", "enrollmentDate", 95));
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField sidF = tf("Student ID", 90), fnF = tf("First Name", 100), lnF = tf("Last Name", 100);
        TextField emF = tf("Email", 160), mjF = tf("Major", 120), yrF = tf("Year", 50);

        Button addBtn = btn("➕ Add", "btn-success");
        Button editBtn = btn("✏️ Edit Selected", "btn-primary");
        Button delBtn = btn("🗑 Delete", "btn-danger");
        Button refBtn = btn("🔄", "btn-secondary");

        HBox row1 = new HBox(8, lf("Student ID", sidF), lf("First Name", fnF), lf("Last Name", lnF), lf("Email", emF));
        HBox row2 = new HBox(8, lf("Major", mjF), lf("Year", yrF));
        HBox acts = new HBox(8, addBtn, editBtn, delBtn, refBtn);
        acts.setAlignment(Pos.CENTER_LEFT);
        acts.setPadding(new Insets(4, 0, 6, 0));

        VBox form = new VBox(6, row1, row2, acts);
        form.getStyleClass().add("card");

        // Fill form on table click
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                sidF.setText(n.getStudentId()); fnF.setText(n.getFirstName()); lnF.setText(n.getLastName());
                emF.setText(n.getEmail()); mjF.setText(n.getMajor()); yrF.setText(String.valueOf(n.getYearLevel()));
            }
        });

        addBtn.setOnAction(e -> {
            if (sidF.getText().trim().isEmpty()) { setStatus("⚠️ Student ID required", true); return; }
            Map<String, Object> d = studentMap(sidF, fnF, lnF, emF, mjF, yrF);
            asyncRun(() -> { try { api.createStudent(d); ok("Student added"); refreshAll(); } catch (Exception ex) { err(ex); } });
            clearFields(sidF, fnF, lnF, emF, mjF, yrF);
        });

        editBtn.setOnAction(e -> {
            StudentRow sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { setStatus("⚠️ Select a student first", true); return; }
            Map<String, Object> d = studentMap(sidF, fnF, lnF, emF, mjF, yrF);
            asyncRun(() -> { try { api.updateStudent(sel.getDbId(), d); ok("Student updated"); refreshAll(); } catch (Exception ex) { err(ex); } });
        });

        delBtn.setOnAction(e -> {
            StudentRow sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) asyncRun(() -> { try { api.deleteStudent(sel.getDbId()); ok("Student deleted"); refreshAll(); } catch (Exception ex) { err(ex); } });
        });

        refBtn.setOnAction(e -> refreshAll());

        VBox v = new VBox(12, h, form, table); v.setPadding(new Insets(4)); return v;
    }

    private Map<String, Object> studentMap(TextField sid, TextField fn, TextField ln, TextField em, TextField mj, TextField yr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("studentId", sid.getText().trim());
        m.put("firstName", fn.getText().trim());
        m.put("lastName", ln.getText().trim());
        m.put("email", em.getText().trim());
        m.put("major", mj.getText().trim());
        try { m.put("yearLevel", Integer.parseInt(yr.getText().trim())); } catch (Exception e) { m.put("yearLevel", 1); }
        return m;
    }

    // ============================= DOCTORS =============================

    @SuppressWarnings("unchecked")
    private Node buildDoctorsView() {
        Label h = new Label("Manage Doctors"); h.getStyleClass().add("section-title");

        TableView<DoctorRow> table = new TableView<>(doctorList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                numCol("ID", "dbId", 45),
                col("First Name", "firstName", 100),
                col("Last Name", "lastName", 100),
                col("Email", "email", 170),
                col("Department", "department", 120),
                col("Specialization", "specialization", 130),
                col("Phone", "phone", 100));
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField fnF = tf("First Name", 100), lnF = tf("Last Name", 100), emF = tf("Email", 160);
        TextField depF = tf("Department", 120), specF = tf("Specialization", 130), phF = tf("Phone", 100);

        Button addBtn = btn("➕ Add", "btn-success");
        Button editBtn = btn("✏️ Edit Selected", "btn-primary");
        Button delBtn = btn("🗑 Delete", "btn-danger");
        Button refBtn = btn("🔄", "btn-secondary");

        HBox row1 = new HBox(8, lf("First Name", fnF), lf("Last Name", lnF), lf("Email", emF));
        HBox row2 = new HBox(8, lf("Department", depF), lf("Specialization", specF), lf("Phone", phF));
        HBox acts = new HBox(8, addBtn, editBtn, delBtn, refBtn);
        acts.setAlignment(Pos.CENTER_LEFT); acts.setPadding(new Insets(4, 0, 6, 0));

        VBox form = new VBox(6, row1, row2, acts); form.getStyleClass().add("card");

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                fnF.setText(n.getFirstName()); lnF.setText(n.getLastName()); emF.setText(n.getEmail());
                depF.setText(n.getDepartment()); specF.setText(n.getSpecialization()); phF.setText(n.getPhone());
            }
        });

        addBtn.setOnAction(e -> {
            if (fnF.getText().trim().isEmpty()) { setStatus("⚠️ First Name required", true); return; }
            Map<String, Object> d = doctorMap(fnF, lnF, emF, depF, specF, phF);
            asyncRun(() -> { try { api.createDoctor(d); ok("Doctor added"); refreshAll(); } catch (Exception ex) { err(ex); } });
            clearFields(fnF, lnF, emF, depF, specF, phF);
        });

        editBtn.setOnAction(e -> {
            DoctorRow sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { setStatus("⚠️ Select a doctor first", true); return; }
            Map<String, Object> d = doctorMap(fnF, lnF, emF, depF, specF, phF);
            asyncRun(() -> { try { api.updateDoctor(sel.getDbId(), d); ok("Doctor updated"); refreshAll(); } catch (Exception ex) { err(ex); } });
        });

        delBtn.setOnAction(e -> {
            DoctorRow sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) asyncRun(() -> { try { api.deleteDoctor(sel.getDbId()); ok("Doctor deleted"); refreshAll(); } catch (Exception ex) { err(ex); } });
        });

        refBtn.setOnAction(e -> refreshAll());

        VBox v = new VBox(12, h, form, table); v.setPadding(new Insets(4)); return v;
    }

    private Map<String, Object> doctorMap(TextField fn, TextField ln, TextField em, TextField dep, TextField spec, TextField ph) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("firstName", fn.getText().trim()); m.put("lastName", ln.getText().trim());
        m.put("email", em.getText().trim()); m.put("department", dep.getText().trim());
        m.put("specialization", spec.getText().trim()); m.put("phone", ph.getText().trim());
        return m;
    }

    // ============================= COURSES =============================

    @SuppressWarnings("unchecked")
    private Node buildCoursesView() {
        Label h = new Label("Manage Courses"); h.getStyleClass().add("section-title");

        TableView<CourseRow> table = new TableView<>(courseList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                col("Code", "courseCode", 80),
                col("Course Name", "courseName", 180),
                numCol("Credits", "credits", 60),
                col("Doctor", "doctorName", 160));
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField codeF = tf("Code", 80), nameF = tf("Name", 180), credF = tf("Credits", 60);
        TextField docIdF = tf("Doctor DB ID", 80);

        Label docHint = new Label("→ Use Doctor ID from Doctors tab");
        docHint.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 11px;");

        Button addBtn = btn("➕ Add", "btn-success");
        Button editBtn = btn("✏️ Edit Selected", "btn-primary");
        Button delBtn = btn("🗑 Delete", "btn-danger");
        Button refBtn = btn("🔄", "btn-secondary");

        HBox row1 = new HBox(8, lf("Code", codeF), lf("Course Name", nameF), lf("Credits", credF), lf("Doctor ID", docIdF));
        HBox acts = new HBox(8, addBtn, editBtn, delBtn, refBtn, docHint);
        acts.setAlignment(Pos.CENTER_LEFT); acts.setPadding(new Insets(4, 0, 6, 0));

        VBox form = new VBox(6, row1, acts); form.getStyleClass().add("card");

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                codeF.setText(n.getCourseCode()); nameF.setText(n.getCourseName());
                credF.setText(String.valueOf(n.getCredits())); docIdF.setText(String.valueOf(n.getDoctorId()));
            }
        });

        addBtn.setOnAction(e -> {
            if (codeF.getText().trim().isEmpty()) { setStatus("⚠️ Course Code required", true); return; }
            Map<String, Object> d = courseMap(codeF, nameF, credF, docIdF);
            asyncRun(() -> { try { api.createCourse(d); ok("Course added"); refreshAll(); } catch (Exception ex) { err(ex); } });
            clearFields(codeF, nameF, credF, docIdF);
        });

        editBtn.setOnAction(e -> {
            CourseRow sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { setStatus("⚠️ Select a course first", true); return; }
            Map<String, Object> d = courseMap(codeF, nameF, credF, docIdF);
            asyncRun(() -> { try { api.updateCourse(sel.getDbId(), d); ok("Course updated"); refreshAll(); } catch (Exception ex) { err(ex); } });
        });

        delBtn.setOnAction(e -> {
            CourseRow sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) asyncRun(() -> { try { api.deleteCourse(sel.getDbId()); ok("Course deleted"); refreshAll(); } catch (Exception ex) { err(ex); } });
        });

        refBtn.setOnAction(e -> refreshAll());

        VBox v = new VBox(12, h, form, table); v.setPadding(new Insets(4)); return v;
    }

    private Map<String, Object> courseMap(TextField code, TextField name, TextField cred, TextField docId) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("courseCode", code.getText().trim()); m.put("courseName", name.getText().trim());
        try { m.put("credits", Integer.parseInt(cred.getText().trim())); } catch (Exception e) { m.put("credits", 3); }
        try { m.put("doctorId", Long.parseLong(docId.getText().trim())); } catch (Exception e) { /* no doctor */ }
        return m;
    }

    // ============================= ENROLLMENTS =============================

    @SuppressWarnings("unchecked")
    private Node buildEnrollmentsView() {
        Label h = new Label("Manage Enrollments"); h.getStyleClass().add("section-title");

        TableView<EnrollRow> table = new TableView<>(enrollList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(
                col("Student", "studentName", 150),
                col("Course", "courseInfo", 200),
                col("Grade", "grade", 60),
                col("Date", "enrolledDate", 100));
        VBox.setVgrow(table, Priority.ALWAYS);

        TextField stuF = tf("Student DB ID", 90), couF = tf("Course DB ID", 90), graF = tf("Grade", 60);

        Button enrollBtn = btn("📝 Enroll", "btn-success");
        Button gradeBtn = btn("📊 Set Grade", "btn-purple");
        Button dropBtn = btn("🗑 Drop", "btn-danger");
        Button refBtn = btn("🔄", "btn-secondary");

        Label hint = new Label("💡 Select a row to set grade or drop. Use DB IDs from Students/Courses tabs.");
        hint.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 11px;");

        HBox row1 = new HBox(8, lf("Student ID", stuF), lf("Course ID", couF), enrollBtn);
        row1.setAlignment(Pos.BOTTOM_LEFT);
        HBox row2 = new HBox(8, lf("Grade", graF), gradeBtn, dropBtn, refBtn);
        row2.setAlignment(Pos.BOTTOM_LEFT);

        VBox form = new VBox(6, row1, row2, hint); form.getStyleClass().add("card");

        enrollBtn.setOnAction(e -> {
            try {
                long sid = Long.parseLong(stuF.getText().trim()), cid = Long.parseLong(couF.getText().trim());
                asyncRun(() -> { try { api.enroll(sid, cid); ok("Enrolled"); refreshAll(); } catch (Exception ex) { err(ex); } });
                clearFields(stuF, couF);
            } catch (NumberFormatException ex) { setStatus("⚠️ Enter valid IDs", true); }
        });

        gradeBtn.setOnAction(e -> {
            EnrollRow sel = table.getSelectionModel().getSelectedItem();
            String g = graF.getText().trim();
            if (sel != null && !g.isEmpty())
                asyncRun(() -> { try { api.setGrade(sel.getDbId(), g); ok("Grade → " + g); refreshAll(); } catch (Exception ex) { err(ex); } });
            graF.clear();
        });

        dropBtn.setOnAction(e -> {
            EnrollRow sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) asyncRun(() -> { try { api.dropEnrollment(sel.getDbId()); ok("Dropped"); refreshAll(); } catch (Exception ex) { err(ex); } });
        });

        refBtn.setOnAction(e -> refreshAll());

        VBox v = new VBox(12, h, form, table); v.setPadding(new Insets(4)); return v;
    }

    // ============================= DATA REFRESH =============================

    private void refreshAll() {
        asyncRun(() -> {
            try {
                var students = api.getStudents();
                var doctors = api.getDoctors();
                var courses = api.getCourses();
                var enrollments = api.getEnrollments();
                Platform.runLater(() -> {
                    studentList.setAll(students.stream().map(s -> new StudentRow(
                            toLong(s, "id"), str(s, "studentId"), str(s, "firstName"), str(s, "lastName"),
                            str(s, "email"), str(s, "major"), toInt(s, "yearLevel"), str(s, "enrollmentDate")
                    )).toList());

                    doctorsData = doctors;
                    doctorList.setAll(doctors.stream().map(d -> new DoctorRow(
                            toLong(d, "id"), str(d, "firstName"), str(d, "lastName"),
                            str(d, "email"), str(d, "department"), str(d, "specialization"), str(d, "phone")
                    )).toList());

                    courseList.setAll(courses.stream().map(c -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> doc = (Map<String, Object>) c.get("doctor");
                        String docName = doc != null ? "Dr. " + str(doc, "firstName") + " " + str(doc, "lastName") : "—";
                        long docId = doc != null ? toLong(doc, "id") : 0;
                        return new CourseRow(toLong(c, "id"), str(c, "courseCode"), str(c, "courseName"),
                                toInt(c, "credits"), docName, docId);
                    }).toList());

                    enrollList.setAll(enrollments.stream().map(e -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> stu = (Map<String, Object>) e.get("student");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> crs = (Map<String, Object>) e.get("course");
                        String sn = stu != null ? str(stu, "firstName") + " " + str(stu, "lastName") + " (" + str(stu, "studentId") + ")" : "?";
                        String cn = crs != null ? str(crs, "courseCode") + " — " + str(crs, "courseName") : "?";
                        String gr = e.get("grade") != null ? e.get("grade").toString() : "—";
                        return new EnrollRow(toLong(e, "id"), sn, cn, gr, str(e, "enrolledDate"));
                    }).toList());

                    updateStats();
                    setStatus("✅ Synced — " + studentList.size() + " students, " + doctorList.size() +
                            " doctors, " + courseList.size() + " courses, " + enrollList.size() + " enrollments", false);
                });
            } catch (Exception e) { setStatus("❌ Sync failed: " + e.getMessage(), true); }
        });
    }

    // ============================= HELPERS =============================

    private TextField tf(String prompt, double w) { TextField t = new TextField(); t.setPromptText(prompt); t.setPrefWidth(w); return t; }
    private VBox lf(String label, TextField tf) { Label l = new Label(label); l.getStyleClass().add("field-label"); return new VBox(1, l, tf); }
    private Button btn(String text, String css) { Button b = new Button(text); b.getStyleClass().add(css); return b; }

    @SuppressWarnings("unchecked")
    private <S> TableColumn<S, String> col(String title, String prop, double w) {
        TableColumn<S, String> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        return c;
    }

    @SuppressWarnings("unchecked")
    private <S> TableColumn<S, Number> numCol(String title, String prop, double w) {
        TableColumn<S, Number> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        return c;
    }

    private void setStatus(String t, boolean err) {
        Platform.runLater(() -> { statusLabel.setText(t); statusLabel.setStyle("-fx-text-fill: " + (err ? "#ef5350" : "#a5d6a7") + ";"); });
    }
    private void ok(String msg) { setStatus("✅ " + msg, false); }
    private void err(Exception ex) { setStatus("❌ " + ex.getMessage(), true); }
    private void asyncRun(Runnable r) { Thread t = new Thread(r); t.setDaemon(true); t.start(); }
    private void clearFields(TextField... fields) { for (TextField f : fields) f.clear(); }

    private String str(Map<String, Object> m, String k) { Object v = m.get(k); return v == null ? "" : v.toString(); }
    private long toLong(Map<String, Object> m, String k) { Object v = m.get(k); return v instanceof Number n ? n.longValue() : 0; }
    private int toInt(Map<String, Object> m, String k) { Object v = m.get(k); return v instanceof Number n ? n.intValue() : 0; }

    // ============================= ROW MODELS =============================

    public static class StudentRow {
        private final long dbId;
        private final SimpleStringProperty studentId, firstName, lastName, email, major, enrollmentDate;
        private final SimpleIntegerProperty yearLevel;
        public StudentRow(long id, String sid, String fn, String ln, String em, String mj, int yr, String dt) {
            dbId=id; studentId=new SimpleStringProperty(sid); firstName=new SimpleStringProperty(fn);
            lastName=new SimpleStringProperty(ln); email=new SimpleStringProperty(em);
            major=new SimpleStringProperty(mj); yearLevel=new SimpleIntegerProperty(yr);
            enrollmentDate=new SimpleStringProperty(dt);
        }
        public long getDbId(){return dbId;} public String getStudentId(){return studentId.get();}
        public String getFirstName(){return firstName.get();} public String getLastName(){return lastName.get();}
        public String getEmail(){return email.get();} public String getMajor(){return major.get();}
        public int getYearLevel(){return yearLevel.get();} public String getEnrollmentDate(){return enrollmentDate.get();}
    }

    public static class DoctorRow {
        private final long dbId;
        private final SimpleStringProperty firstName, lastName, email, department, specialization, phone;
        public DoctorRow(long id, String fn, String ln, String em, String dep, String spec, String ph) {
            dbId=id; firstName=new SimpleStringProperty(fn); lastName=new SimpleStringProperty(ln);
            email=new SimpleStringProperty(em); department=new SimpleStringProperty(dep);
            specialization=new SimpleStringProperty(spec); phone=new SimpleStringProperty(ph);
        }
        public long getDbId(){return dbId;} public String getFirstName(){return firstName.get();}
        public String getLastName(){return lastName.get();} public String getEmail(){return email.get();}
        public String getDepartment(){return department.get();} public String getSpecialization(){return specialization.get();}
        public String getPhone(){return phone.get();}
    }

    public static class CourseRow {
        private final long dbId;
        private final long doctorId;
        private final SimpleStringProperty courseCode, courseName, doctorName;
        private final SimpleIntegerProperty credits;
        public CourseRow(long id, String code, String name, int cred, String doc, long docId) {
            dbId=id; doctorId=docId; courseCode=new SimpleStringProperty(code);
            courseName=new SimpleStringProperty(name); credits=new SimpleIntegerProperty(cred);
            doctorName=new SimpleStringProperty(doc);
        }
        public long getDbId(){return dbId;} public long getDoctorId(){return doctorId;}
        public String getCourseCode(){return courseCode.get();} public String getCourseName(){return courseName.get();}
        public int getCredits(){return credits.get();} public String getDoctorName(){return doctorName.get();}
    }

    public static class EnrollRow {
        private final long dbId;
        private final SimpleStringProperty studentName, courseInfo, grade, enrolledDate;
        public EnrollRow(long id, String stu, String crs, String gr, String dt) {
            dbId=id; studentName=new SimpleStringProperty(stu); courseInfo=new SimpleStringProperty(crs);
            grade=new SimpleStringProperty(gr); enrolledDate=new SimpleStringProperty(dt);
        }
        public long getDbId(){return dbId;} public String getStudentName(){return studentName.get();}
        public String getCourseInfo(){return courseInfo.get();} public String getGrade(){return grade.get();}
        public String getEnrolledDate(){return enrolledDate.get();}
    }

    public static void main(String[] args) { launch(args); }
}
