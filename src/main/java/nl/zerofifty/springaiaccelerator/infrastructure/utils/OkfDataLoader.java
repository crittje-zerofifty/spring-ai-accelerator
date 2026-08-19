package nl.zerofifty.springaiaccelerator.infrastructure.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Profile("okf")
public class OkfDataLoader {

    private static final Logger log = LoggerFactory.getLogger(OkfDataLoader.class);

    @Bean
    CommandLineRunner loadOkfData(VectorStore vectorStore) {
        return args -> {
            log.info("Loading OKF YAML files from classpath:okf/*.okf.yaml...");
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:okf/*.okf.yaml");

            List<Document> allDocuments = new ArrayList<>();
            Yaml yaml = new Yaml();

            for (Resource resource : resources) {
                try {
                    log.info("Reading OKF file: {}", resource.getFilename());
                    
                    TextReader textReader = new TextReader(resource);
                    List<Document> docs = textReader.get();
                    
                    Map<String, Object> yamlData = yaml.load(resource.getInputStream());
                    
                    for (Document doc : docs) {
                        if (yamlData != null) {
                            yamlData.forEach((key, value) -> {
                                if (value instanceof Map) {
                                    flattenMetadata(doc.getMetadata(), key, (Map<String, Object>) value);
                                    return;
                                }
                                doc.getMetadata().put(key, value);

                            });
                        }
                        allDocuments.add(doc);
                    }
                } catch (Exception e) {
                    log.error("Failed to read OKF file: {}", resource.getFilename(), e);
                }
            }

            if (!allDocuments.isEmpty()) {
                vectorStore.add(allDocuments);
                log.info("Successfully indexed {} OKF documents in the vector store.", allDocuments.size());
                return;
            }
            log.warn("No OKF documents found to index.");

        };
    }

    private void flattenMetadata(Map<String, Object> metadata, String prefix, Map<String, Object> map) {
        map.forEach((key, value) -> {
            String fullKey = prefix + "." + key;
            if (value instanceof Map) {
                flattenMetadata(metadata, fullKey, (Map<String, Object>) value);
            } else {
                metadata.put(fullKey, value);
            }
        });
    }
}
