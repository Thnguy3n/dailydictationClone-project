package com.example.audioservice.config;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FireBaseConfig {

    @Bean
    public Storage firebaseStorage() throws IOException {
        InputStream serviceAccount = new ClassPathResource(
                "shopyame-66346-firebase-adminsdk-rs0rp-010880ac7d.json"
        ).getInputStream();
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setStorageBucket("shopyame-66346.appspot.com")
                    .build();
            FirebaseApp.initializeApp(options);
        }

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
