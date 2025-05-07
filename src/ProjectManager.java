import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;

/**
 * Helper class to manage project files and metadata.
 * Handles loading, saving, and organizing FurnishView design projects.
 */
public class ProjectManager {

    // Directory for storing design files
    private static final String DESIGNS_DIR = "./designs";

    // Default file extension for project files
    private static final String FILE_EXTENSION = ".furn";

    /**
     * Represents metadata for a design project
     */
    public static class ProjectMetadata implements Serializable {
        private static final long serialVersionUID = 1L;

        public String filename;
        public String projectName;
        public Date creationDate;
        public Date lastModifiedDate;
        public String roomType;
        public int itemCount;
        public String description;
        public String createdBy;

        // Constructor
        public ProjectMetadata(String filename, String projectName, String roomType,
                               int itemCount, String createdBy) {
            this.filename = filename;
            this.projectName = projectName;
            this.creationDate = new Date();
            this.lastModifiedDate = new Date();
            this.roomType = roomType;
            this.itemCount = itemCount;
            this.description = "";
            this.createdBy = createdBy;
        }

        // Returns a formatted string with last modified date
        public String getFormattedLastModified() {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            return sdf.format(lastModifiedDate);
        }
    }

    /**
     * Initialize the designs directory if it doesn't exist
     */
    public static void initializeDesignsDirectory() {
        File dir = new File(DESIGNS_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Created designs directory at: " + dir.getAbsolutePath());
            } else {
                System.err.println("Failed to create designs directory");
            }
        }
    }

    /**
     * Get a list of all available projects for a specific user
     * @param username The username to filter projects by
     * @return List of ProjectMetadata objects belonging to the specified user
     */
    public static List<ProjectMetadata> getProjectsForUser(String username) {
        List<ProjectMetadata> projects = new ArrayList<>();
        File dir = new File(DESIGNS_DIR);

        if (!dir.exists() || !dir.isDirectory()) {
            initializeDesignsDirectory();
            return projects; // Return empty list
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(FILE_EXTENSION));
        if (files == null) return projects;

        for (File file : files) {
            try {
                // Try to extract metadata from the project file
                DesignModel model = loadDesignModel(file);
                if (model != null) {
                    String roomType = "Unknown";
                    int itemCount = 0;
                    String owner = model.getCreatedBy(); // Get the owner from the model

                    // Skip if the owner doesn't match the requested username
                    // If admin, show all projects
                    if (owner != null && !owner.equals(username) && !UserManager.isAdmin(username)) {
                        continue;
                    }

                    if (model.getRoom() != null) {
                        roomType = model.getRoom().getShape().toString();
                    }

                    if (model.getFurnitureList() != null) {
                        itemCount = model.getFurnitureList().size();
                    }

                    // Extract the project name from the file name
                    String projectName = file.getName();
                    if (projectName.toLowerCase().endsWith(FILE_EXTENSION)) {
                        projectName = projectName.substring(0, projectName.length() - FILE_EXTENSION.length());
                    }

                    // Create metadata entry
                    ProjectMetadata metadata = new ProjectMetadata(
                            file.getAbsolutePath(),
                            projectName,
                            roomType,
                            itemCount,
                            owner != null ? owner : "Unknown"
                    );

                    // Update timestamps based on file
                    metadata.creationDate = new Date(file.lastModified());
                    metadata.lastModifiedDate = new Date(file.lastModified());

                    projects.add(metadata);
                }
            } catch (Exception e) {
                System.err.println("Error reading project file: " + file.getName() + " - " + e.getMessage());
            }
        }

        // Sort by last modified date (newest first)
        projects.sort((p1, p2) -> p2.lastModifiedDate.compareTo(p1.lastModifiedDate));

        return projects;
    }

    /**
     * Get a list of all available projects (for backward compatibility)
     * @return List of ProjectMetadata objects
     */
    public static List<ProjectMetadata> getAllProjects() {
        return getProjectsForUser(null);
    }

    /**
     * Load a design model from a file
     * @param file The project file to load
     * @return The loaded DesignModel, or null if loading failed
     */
    public static DesignModel loadDesignModel(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof DesignModel) {
                return (DesignModel) obj;
            }
        } catch (Exception e) {
            System.err.println("Error loading design model: " + e.getMessage());
        }

        return null;
    }

    /**
     * Save a design model to a new file
     * @param model The model to save
     * @param projectName The name of the project
     * @param username The username of the creator
     * @return The metadata for the saved project, or null if saving failed
     */
    public static ProjectMetadata saveNewProject(DesignModel model, String projectName, String username) {
        if (model == null || projectName == null || projectName.trim().isEmpty()) {
            return null;
        }

        // Set the creator in the model
        model.setCreatedBy(username);

        // Sanitize project name for use as filename
        String safeName = projectName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = DESIGNS_DIR + File.separator + safeName + FILE_EXTENSION;

        // Check if file already exists
        File file = new File(filename);
        if (file.exists()) {
            // Append timestamp to make unique
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            filename = DESIGNS_DIR + File.separator + safeName + "_" +
                    sdf.format(new Date()) + FILE_EXTENSION;
            file = new File(filename);
        }

        // Ensure the directory exists
        initializeDesignsDirectory();

        // Save the model
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(model);

            // Create and return metadata
            String roomType = "Unknown";
            int itemCount = 0;

            if (model.getRoom() != null) {
                roomType = model.getRoom().getShape().toString();
            }

            if (model.getFurnitureList() != null) {
                itemCount = model.getFurnitureList().size();
            }

            ProjectMetadata metadata = new ProjectMetadata(
                    file.getAbsolutePath(),
                    projectName,
                    roomType,
                    itemCount,
                    username
            );

            return metadata;
        } catch (Exception e) {
            System.err.println("Error saving design model: " + e.getMessage());
            return null;
        }
    }

    /**
     * Save changes to an existing project
     * @param model The updated model
     * @param originalFile The original file to update
     * @return True if the save was successful
     */
    public static boolean updateProject(DesignModel model, File originalFile) {
        if (model == null || originalFile == null || !originalFile.exists()) {
            return false;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(originalFile))) {
            oos.writeObject(model);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating project: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a project file
     * @param metadata The metadata for the project to delete
     * @return True if deletion was successful
     */
    public static boolean deleteProject(ProjectMetadata metadata) {
        if (metadata == null || metadata.filename == null) {
            return false;
        }

        File file = new File(metadata.filename);
        if (!file.exists()) {
            return false;
        }

        try {
            return Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            System.err.println("Error deleting project: " + e.getMessage());
            return false;
        }
    }

    /**
     * Duplicate a project with a new name
     * @param sourceMetadata The metadata for the source project
     * @param newProjectName The name for the duplicate project
     * @param username The username of the person creating the duplicate
     * @return The metadata for the new project, or null if duplication failed
     */
    public static ProjectMetadata duplicateProject(ProjectMetadata sourceMetadata,
                                                   String newProjectName,
                                                   String username) {
        if (sourceMetadata == null || newProjectName == null || newProjectName.trim().isEmpty()) {
            return null;
        }

        // Load the source project
        File sourceFile = new File(sourceMetadata.filename);
        DesignModel model = loadDesignModel(sourceFile);

        if (model == null) {
            return null;
        }

        // Update the creator
        model.setCreatedBy(username);

        // Save as a new project
        return saveNewProject(model, newProjectName, username);
    }

    /**
     * Generate a thumbnail image for a project (placeholder for future implementation)
     * @param model The design model
     * @return Path to the thumbnail image, or null if generation failed
     */
    public static String generateThumbnail(DesignModel model) {
        // This is a placeholder for future implementation
        // In a complete implementation, this would render a small preview image
        // of the design and save it to a file
        return null;
    }
}