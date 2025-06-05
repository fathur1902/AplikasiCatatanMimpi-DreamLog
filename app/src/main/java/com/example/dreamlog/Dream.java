package com.example.dreamlog;

public class Dream {
    private int id;
    private String title;
    private String description;
    private String emotion;

    public Dream(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.emotion = determineEmotion(description);
    }

    public Dream(String title, String description) {
        this.id = -1; // Default ID, akan di-set oleh server setelah POST
        this.title = title;
        this.description = description;
        this.emotion = determineEmotion(description);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title != null ? title : "No Title";
    }

    public String getDescription() {
        return description != null ? description : "No Description";
    }

    public String getEmotion() {
        return emotion != null ? emotion : "tidak diketahui";
    }

    private String determineEmotion(String description) {
        if (description == null) return "tidak diketahui";

        String lowerDesc = description.toLowerCase();

        if (lowerDesc.contains("senang") || lowerDesc.contains("bahagia") || lowerDesc.contains("gembira")) {
            return "senang";
        } else if (lowerDesc.contains("sedih") || lowerDesc.contains("menangis") || lowerDesc.contains("air mata")) {
            return "sedih";
        } else if (lowerDesc.contains("takut") || lowerDesc.contains("seram") || lowerDesc.contains("mimpi buruk")) {
            return "seram";
        } else if (lowerDesc.contains("aneh") || lowerDesc.contains("janggal") || lowerDesc.contains("unik")) {
            return "aneh";
        } else {
            return "tidak diketahui";
        }
    }
}