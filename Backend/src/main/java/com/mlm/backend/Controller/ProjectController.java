package com.mlm.backend.Controller;

import com.mlm.backend.Model.Project;
import com.mlm.backend.Security.CustomUserDetails;
import com.mlm.backend.Service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestBody Project project,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // Automatically lock the project owner to the logged-in user
        project.setOwner(userDetails.getActualUsername());
        Project createdProject = projectService.createProject(project);
        return ResponseEntity.ok(createdProject);
    }

    // Only Admins or Reviewers should see ALL projects globally
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    // NEW: Secure endpoint that automatically fetches projects for the logged-in user
    @GetMapping("/my-projects")
    public ResponseEntity<List<Project>> getMyProjects(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUsername = userDetails.getActualUsername();
        return ResponseEntity.ok(projectService.getProjectsByOwner(currentUsername));
    }

    // Optional: Keep the original, but restrict it to Admins who might need to audit specific users
    @GetMapping("/owner/{owner}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Project>> getProjectsByOwner(@PathVariable String owner) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(owner));
    }

    @PatchMapping("/{id}/savings")
    public ResponseEntity<Project> updateProjectSavings(@PathVariable Long id, @RequestParam Double savings) {
        Project updatedProject = projectService.addSavingsToProject(id, savings);
        return ResponseEntity.ok(updatedProject);
    }
}