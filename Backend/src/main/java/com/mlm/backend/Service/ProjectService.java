package com.mlm.backend.Service;

import com.mlm.backend.Model.Project;
import com.mlm.backend.Repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    public List<Project> getProjectsByOwner(String owner) {
        return projectRepository.findByOwner(owner);
    }

    @Transactional
    public Project addSavingsToProject(Long projectId, Double newSavings) {
        Project project = getProjectById(projectId);
        double currentSavings = project.getTotalEstimatedSavings() != null ? project.getTotalEstimatedSavings() : 0.0;
        project.setTotalEstimatedSavings(currentSavings + newSavings);
        return projectRepository.save(project);
    }
}