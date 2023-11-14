package com.structurizr.cli;

import com.structurizr.cli.export.ExportCommand;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExportCommandTests {

    @Test
    public void run() throws Exception {
        File tmpDir = Files.createTempDirectory("structurizr").toFile();

        String[] args = {
                "export",
                "-workspace", "src/test/dsl/workspace.dsl",
                "-output", tmpDir.getCanonicalPath(),
                "-format", "plantuml"
        };
        new ExportCommand().run(args);

        assertTrue(new File(tmpDir, "structurizr-SystemLandscape.puml").exists());
    }

    @Test
    public void exportWorkspaceToJSONAndMerge() throws Exception {
        File tmpDir = Files.createTempDirectory("structurizr").toFile();

        String[] args = {
                "export",
                "-workspace", "src/test/dsl/workspace.dsl",
                "-output", tmpDir.getCanonicalPath(),
                "-format", "json",
                "-merge", "src/test/dsl/workspace.json"

        };
        StructurizrCliApplication.main(args);

        assertTrue(new File(tmpDir, "workspace.json").exists());
    }

}
