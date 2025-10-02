import com.sentencepiece.Model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class Tests {
    public static void main(String[] args) throws IOException {
        Path modelPath = Paths.get("models/sentencepiece.bpe.model");

        // Load the model
        System.out.println("Loading model...");
        Model model = Model.parseFrom(modelPath);
        System.out.println("Model loaded with maxScore = " + model.getMaxScore());

        // Test encode
        String text = "▁this ▁is ▁a ▁test";
        List<Integer> ids = model.encode(text);
        System.out.println("Encoded: " + ids);

        // Test decode
        String decoded = model.decode(ids);
        System.out.println("Decoded: " + decoded);

        // Extra: reverse check
        for (int id : ids) {
            System.out.printf("id: %d → token: '%s'%n", id, model.getTokenById(id));
        }
    }
}