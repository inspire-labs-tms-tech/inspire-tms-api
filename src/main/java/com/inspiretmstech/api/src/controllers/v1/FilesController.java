package com.inspiretmstech.api.src.controllers.v1;

import com.inspiretmstech.api.src.auth.methods.apikey.Authority;
import com.inspiretmstech.api.src.constants.EnvironmentVariables;
import com.inspiretmstech.api.src.models.ResponseException;
import com.inspiretmstech.api.src.models.controllers.Controller;
import com.inspiretmstech.api.src.utils.PDFUtil;
import com.inspiretmstech.common.postgres.FileUploader;
import com.inspiretmstech.common.postgres.PostgresConnection;
import com.inspiretmstech.common.utils.Environment;
import com.inspiretmstech.db.tables.records.FilesRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.hc.core5.net.URIBuilder;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@Tag(name = "Files", description = "User endpoints for managing files")
@RequestMapping("/v1/files")
public class FilesController extends Controller {

    public FilesController() {
        super(FilesController.class);
    }

    @Secured(Authority.Authorities.USER)
    @Operation(summary = "Merge a list of PDFs")
    @PostMapping("/pdfs/merge")
    public String mergePDFs(
            @Valid @NotNull @RequestBody List<String> pdfs,
            @Nullable @RequestParam(required = false, defaultValue = "true") Boolean upload,
            @Nullable @RequestParam(required = false, defaultValue = "") String filename,
            @Nullable @RequestParam(required = false, defaultValue = "true") Boolean isPublic
    ) {

        if (pdfs.size() < 2)
            throw new ResponseException("Invalid Request", "Cannot merge less than 2 PDFs", "Got " + pdfs.size() + " PDFs");

        // validate each URL
        List<URL> urls = new ArrayList<>();
        for (int i = 0; i < pdfs.size(); i++)
            try {
                urls.add(new URL(pdfs.get(i)));
            } catch (MalformedURLException e) {
                throw new ResponseException("Malformed URL", "URL at position " + (i + 1) + " (index " + i + ") is invalid");
            }

        // do merging
        String result;
        try {
            result = PDFUtil.convertToBase64URI(PDFUtil.mergeFrom(urls));
        } catch (IOException e) {
            this.logger.error(e.getMessage());
            throw new ResponseException("Merging Error", "An error occurred while merging the files");
        }

        // save to db
        try {
            if(Objects.nonNull(upload) && !upload) return result;
            FilesRecord record = FileUploader.using(PostgresConnection.getInstance()).upload(result, Optional.ofNullable(isPublic).orElse(true), filename);
            URIBuilder builder = new URIBuilder(Environment.get(EnvironmentVariables.SITE_URL));
            builder.setPath("/api/v1/files/" + record.getId().toString());
            return builder.build().toString();
        } catch (SQLException e) {
            this.logger.error(e.getMessage());
            throw new ResponseException("Connection Error", "An error occurred while connecting to the database");
        } catch (URISyntaxException e) {
            this.logger.error(e.getMessage());
            throw new ResponseException("Environment Variable '" + EnvironmentVariables.SITE_URL.name() + "' ('" + Environment.get(EnvironmentVariables.SITE_URL) + "') is invalid");
        } catch (Exception e) {
            this.logger.error(e.getMessage());
            throw new ResponseException("Uploading Error", "An error occurred while uploading the merged file");
        }
    }
}
