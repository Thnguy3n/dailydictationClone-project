package com.example.audioservice.service.Impl;

import com.example.audioservice.model.Request.TranscriptRequest;
import com.example.audioservice.service.TranscriptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
@Service
public class TranscriptServiceImpl implements TranscriptService {
    @Value("${assemblyAI_API_KEY}")
    private String API_KEY;
    @Value("${assemblyAI_TRANSCRIPT_URL}")
    private String TRANSCRIPT_URL;
    private static final HttpClient client = HttpClient.newHttpClient();

    @Override
    public String Transcript(TranscriptRequest transcriptRequest) throws Exception {
        String uploadURL = transcriptRequest.getAudioUrl();
        String transcriptId = createTranscript(uploadURL);
        String result = getTranscriptResult(transcriptId);
//        if(result.contains(".")){
//            result = result.replace(".", ".\n");
//        }
        return result;
    }

    private String createTranscript(String audioUrl) throws Exception {
        String jsonBody = String.format("{\"audio_url\": \"%s\"}",audioUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseJson(response.body(), "id");
        }
        throw new RuntimeException("Error creating transcript: " + response.body());
    }

    private static String parseJson(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return null;

            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex == -1) return null;

            int valueStartIndex = json.indexOf("\"", colonIndex);
            if (valueStartIndex == -1) return null;

            int valueEndIndex = json.indexOf("\"", valueStartIndex + 1);
            if (valueEndIndex == -1) return null;

            return json.substring(valueStartIndex + 1, valueEndIndex);
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            System.out.println("JSON string: " + json);
            return null;
        }
    }
    private  String getTranscriptResult(String transcriptId) throws Exception {
        String status = "";
        String resultUrl = TRANSCRIPT_URL + "/" + transcriptId;

        HttpResponse<String> response = null;
        while (!status.equals("completed")) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resultUrl))
                    .header("Authorization", API_KEY)
                    .GET()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error getting transcript: " + response.body());
            }

            status = parseJson(response.body(), "status");

            if (status.equals("error")) {
                throw new RuntimeException("Transcription failed: " + parseJson(response.body(), "error"));
            }

            if (!status.equals("completed")) {
                Thread.sleep(5000);
            }
        }

        return parseJson(response.body(), "text");
    }

}
