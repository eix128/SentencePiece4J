import com.sentencepiece.SentencePieceProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Tests2 {
    public static void main(String[] args) throws IOException {
        Path modelPath = Paths.get("models/sentencepiece.bpe.model");
        SentencePieceProcessor processor = new SentencePieceProcessor(modelPath);

        String raw = "Akşam eve gidince yağlı ballı ekmek yemek istiyorum.";
        List<Integer> ids = processor.encode(raw);

        System.out.println("IDs: " + ids);
        System.out.println("Decoded: " + processor.decode(ids));
        System.out.println("Escaped: " + processor.decodeSmart(ids));

    }
}
