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
        User createdUserJson = apiClient.post("/users", newUserJson);
        System.out.println("Created user: " + createdUserJson);


        User updatedUserJson = apiClient.put("/users/1", "{\"name\": \"Jane Doe\"}");
        System.out.println("Updated user: " + updatedUserJson);


        int responseCode = apiClient.delete("/users/1");
        System.out.println("Delete user response code: " + responseCode);


        User allUsersJson = apiClient.get("/users");
        System.out.println("All users: " + allUsersJson);


        User userByIdJson = apiClient.get("/users/2");
        System.out.println("User by id: " + userByIdJson);


        User userByUsernameJson = apiClient.get("/users?username=johndoe");
        System.out.println("User by username: " + userByUsernameJson);

        apiClient.getOpenUserTodos(1);
    }

    private User get(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return getResponse(con);
    }

    private User post(String path, String jsonBody) throws IOException {
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

    private User put(String path, String jsonBody) throws IOException {
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
    private User getResponse(HttpURLConnection con) throws IOException {
        int status = con.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            return new User(content.toString());
        } else {
            con.disconnect();
            throw new IOException("Request failed with error code " + status);
        }
    }

    private static final String COMMENT_FILE_PATH = "user-%d-post-%d-comments.json";

    public JSONArray getUserPosts(int userId) throws IOException {
        User userPostsJson = get("/users/" + userId + "/posts");
        return new JSONArray(userPostsJson);
    }

    public JSONObject findLatestPost(JSONArray userPosts) {
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
        return latestPost;
    }

    public JSONArray getPostComments(int postId) throws IOException {
        User commentsJson = get("/posts/" + postId + "/comments");
        return new JSONArray(commentsJson);
    }

    public void writeCommentsToFile(int userId, int postId, JSONArray comments) throws IOException {
        String filename = String.format(COMMENT_FILE_PATH, userId, postId);
        try (FileWriter file = new FileWriter(filename)) {
            file.write(comments.toString());
            System.out.println("Comments written to file: " + filename);
        }
    }
    public JSONObject getOpenUserTodos(int userId) throws IOException {
        User userTodosJson = get("/users/" + userId + "/todos");
        JSONArray userTodos = new JSONArray(userTodosJson);

        for (int i = 0; i < userTodos.length(); i++) {
            JSONObject todo = userTodos.getJSONObject(i);
            boolean completed = todo.getBoolean("completed");
            if (!completed)
                return todo;
        }
        return null;
    }
}
