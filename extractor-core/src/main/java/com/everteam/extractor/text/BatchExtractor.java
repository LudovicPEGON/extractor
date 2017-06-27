package com.everteam.extractor.text;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.everteam.extractor.domain.TextData;
import com.everteam.extractor.service.TextExtractorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BatchExtractor {
    private static final Logger log = LoggerFactory.getLogger(BatchExtractor.class);


    @Value("${extractor.text.batch.inputDirectory:}")
    private String inputDirText;

    @Value("${extractor.text.batch.outputDirectory:}")
    private String outputDirText;

    @Value("${extractor.text.batch.numberOfThreads:#{10}}")
    private int numberOfThreads;

    @Autowired
    TextExtractorFactory textFactory;

    private ExecutorService executor;

    @PostConstruct
    public void init() {
        try {
            executor = Executors.newFixedThreadPool(numberOfThreads);
            extractExistingFiles();
            WatchService watcher = FileSystems.getDefault().newWatchService();
            executor.submit(() -> {
                try {
                    watchInputDir(watcher);
                } catch (Exception e) {
                    log.error("Error while initializing batch extractor", e);
                }
            });
        } catch (Exception e) {
            log.error("Error while initializing batch extractor", e);
        }
    }

    private void extractExistingFiles() {
        if (StringUtils.isEmpty(inputDirText))
            return;
        HashSet<String> outputFiles = new HashSet<String>();
        Path outputDir = Paths.get(outputDirText);

        try {
            if (!Files.exists(outputDir))
                Files.createDirectories(outputDir);
            Files.list(outputDir).forEach(path -> {
                if (!Files.isDirectory(path)) {
                    String fileName = path.getFileName().toString();
                    fileName = fileName.replace(".txt", "");
                    outputFiles.add(fileName);
                }
            });

            Path inputDir = Paths.get(inputDirText);
            if (!Files.exists(inputDir))
                Files.createDirectories(inputDir);
            Files.list(inputDir).forEach(path -> {
                if (!Files.isDirectory(path)) {
                    String fileName = path.getFileName().toString();
                    if (!outputFiles.contains(fileName)) {
                        extractFile(path);
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error while ectracting existing files ", e);
        }

    }

    private void watchInputDir(WatchService watcher) throws IOException {
        if (StringUtils.isEmpty(inputDirText))
            return;
        Path inputDir = Paths.get(inputDirText);
        if (!Files.exists(inputDir))
            Files.createDirectories(inputDir);
        inputDir.register(watcher, ENTRY_CREATE);

        while (true) {

            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filePath = ev.context();
                if (kind == ENTRY_CREATE) {
                    extractFile(Paths.get(inputDir.toString(),
                            filePath.toString()));
                }
            }

            if (!key.reset()) {
                break;
            }
        }
    }

    private void extractFile(Path filePath) {
        executor.submit(() -> {
            try {
                String contentType = Files.probeContentType(filePath);
                ITextExtractor extractor = textFactory
                        .getExtractor(contentType);
                TextData textData = extractor.getText(Files
                        .newInputStream(filePath));
                Path outputPath = Paths.get(outputDirText, filePath
                        .getFileName().toString() + ".txt");
                ObjectMapper mapper = new ObjectMapper();

                Files.write(outputPath, mapper.writeValueAsString(textData).getBytes("UTF-8"));
            } catch (Exception e) {
                log.error(
                        "Error while extracting file " + filePath.getFileName(),
                        e);
            }
        });
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        executor.shutdown();
    }
}
