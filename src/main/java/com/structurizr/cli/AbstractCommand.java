package com.structurizr.cli;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.Styles;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.nio.charset.Charset;

public abstract class AbstractCommand {

    private static final Log log = LogFactory.getLog(AbstractCommand.class);

    private static final int HTTP_OK_STATUS = 200;

    protected AbstractCommand() {
    }

    public abstract void run(String... args) throws Exception;

    String getAgent() {
        return "structurizr-cli/" + getClass().getPackage().getImplementationVersion();

    }

    protected void addDefaultViewsAndStyles(Workspace workspace) {
        if (workspace.getViews().isEmpty()) {
            log.info(" - no views defined; creating default views");
            workspace.getViews().createDefaultViews();
        }

        Styles styles = workspace.getViews().getConfiguration().getStyles();
        if (styles.getElements().isEmpty() && styles.getRelationships().isEmpty() && workspace.getViews().getConfiguration().getThemes() == null) {
            log.info(" - no styles or themes defined; use the \"default\" theme to add some default styles");
        }
    }

    protected Workspace loadWorkspace(String workspacePathAsString) throws Exception {
        Workspace workspace;

        if (workspacePathAsString.endsWith(".json")) {
            if (workspacePathAsString.startsWith("http://") || workspacePathAsString.startsWith("https")) {
                String json = readFromUrl(workspacePathAsString);
                workspace = WorkspaceUtils.fromJson(json);
            } else {
                File workspaceFile = new File(workspacePathAsString);
                if (!workspaceFile.exists()) {
                    throw new StructurizrCliException(workspaceFile.getAbsolutePath() + " does not exist");
                }

                if (!workspaceFile.isFile()) {
                    throw new StructurizrCliException(workspaceFile.getAbsolutePath() + " is not a JSON or DSL file");
                }

                workspace = WorkspaceUtils.loadWorkspaceFromJson(workspaceFile);
            }

        } else {
            StructurizrDslParser structurizrDslParser = new StructurizrDslParser();
            structurizrDslParser.setCharacterEncoding(Charset.defaultCharset());

            if (workspacePathAsString.startsWith("http://") || workspacePathAsString.startsWith("https://")) {
                String dsl = readFromUrl(workspacePathAsString);
                structurizrDslParser.parse(dsl);
            } else {
                File workspaceFile = new File(workspacePathAsString);
                if (!workspaceFile.exists()) {
                    throw new StructurizrCliException(workspaceFile.getAbsolutePath() + " does not exist");
                }

                if (!workspaceFile.isFile()) {
                    throw new StructurizrCliException(workspaceFile.getAbsolutePath() + " is not a JSON or DSL file");
                }

                structurizrDslParser.parse(workspaceFile);
            }

            workspace = structurizrDslParser.getWorkspace();

            if (workspace == null) {
                throw new StructurizrCliException("No workspace definition was found - please check your DSL");
            }
        }

        return workspace;
    }

    protected String readFromUrl(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createSystem()) {
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpGet);

            if (response.getCode() == HTTP_OK_STATUS) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception ioe) {
            log.error(ioe);
        }

        return "";
    }

}