package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonPlaceHolder {
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    public static void main(String[] args) throws IOException {
        JsonPlaceHolder apiClient = new JsonPlaceHolder();


        String newUserJson = "{\"name\": \"John Doe\", \"username\": \"johndoe\", \"email\": \"johndoe@example.com\"}";
        String createdUserJson = apiClient.post("/users", newUserJson);
        System.out.println("Created user: " + createdUserJson);


        String updatedUserJson = apiClient.put("/users/1", "{\"name\": \"Jane Doe\"}");
        System.out.println("Updated user: " + updatedUserJson);


        int responseCode = apiClient.delete("/users/1");
        System.out.println("Delete user response code: " + responseCode);


        String allUsersJson = apiClient.get("/users");
        System.out.println("All users: " + allUsersJson);


        String userByIdJson = apiClient.get("/users/2");
        System.out.println("User by id: " + userByIdJson);


        String userByUsernameJson = apiClient.get("/users?username=johndoe");
        System.out.println("User by username: " + userByUsernameJson);

        apiClient.getOpenUserTodos(1);
    }

    private String get(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return getResponse(con);
    }

    private String post(String path, String jsonBody) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        return getResponse(con);
    }

    private String put(String path, String jsonBody) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        return getResponse(con);
    }

    private int delete(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");
        return con.getResponseCode();
    }

    private String getResponse(HttpURLConnection con) throws IOException {
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("Response code: " + responseCode);
        }
    }
    private static final String COMMENT_FILE_PATH = "user-%d-post-%d-comments.json";

    public void getUserPostComments(int userId) throws IOException {
        String userPostsJson = get("/users/" + userId + "/posts");
        JSONArray userPosts = new JSONArray(userPostsJson);

        JSONObject latestPost = null;
        int latestPostId = 0;
        for (int i = 0; i < userPosts.length(); i++) {
            JSONObject post = userPosts.getJSONObject(i);
            int postId = post.getInt("id");
            if (postId > latestPostId) {
                latestPost = post;
                latestPostId = postId;
            }
        }

        if (latestPost != null) {

            String commentsJson = get("/posts/" + latestPost.getInt("id") + "/comments");
            JSONArray comments = new JSONArray(commentsJson);

            String filename = String.format(COMMENT_FILE_PATH, userId, latestPost.getInt("id"));
            try (FileWriter file = new FileWriter(filename)) {
                file.write(comments.toString());
                System.out.println("Comments written to file: " + filename);
            }
        } else {
            System.out.println("User has no posts.");
        }
    }
    public void getOpenUserTodos(int userId) throws IOException {
        String userTodosJson = get("/users/" + userId + "/todos");
        JSONArray userTodos = new JSONArray(userTodosJson);

        for (int i = 0; i < userTodos.length(); i++) {
            JSONObject todo = userTodos.getJSONObject(i);
            boolean completed = todo.getBoolean("completed");
            if (!completed) {
                System.out.println(todo.toString());
            }
        }
    }
}
