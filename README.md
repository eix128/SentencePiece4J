# SentencePiece4J
SentencePiece in pure Java , No JNI Required.
Works Cross Platform for all OS

use with :

#   mvn package

# Usage
    import com.sentencepiece.Model;
    import com.sentencepiece.Scoring;
    import com.sentencepiece.SentencePieceAlgorithm;

    import java.io.IOException;
    import java.nio.file.Paths;
    import java.util.List;

    public class Main {
        public static void main(String[] args) throws IOException {
        Model model = Model.parseFrom(Paths.get("sentencepiece.bpe.model"));
        SentencePieceAlgorithm algorithm = new SentencePieceAlgorithm( true, Scoring.HIGHEST_SCORE );

        {
            String raw = "o captain! my captain! our fearful trip is done," +
                    "the ship has weather’d every rack, the prize we sought is won," +
                    "the port is near, the bells i hear, the people all exulting,";
            List<Integer> ids = model.encodeNormalized(raw, algorithm);

            System.out.println("Token IDs: " + ids);
            System.out.println("Decoded text: " + model.decodeSmart(ids));
            assert (raw.hashCode() ==  model.decodeSmart(ids).hashCode());
            System.out.println("✔ Success");

        }

        //Test Hebrew
        {
            String raw = "השתיקה יפה לחכמים";
            List<Integer> ids = model.encodeNormalized(raw, algorithm);

            System.out.println("Token IDs: " + ids);
            System.out.println("Decoded text: " + model.normalizeHebrew(model.decodeSmart(ids) , true ));
            assert (raw.hashCode() ==  model.decodeSmart(ids).hashCode());
            System.out.println("✔ Success");
        }


        //Test Turkish
        {
            String raw = "kusur bulmak için bakma birine! kakmak için bakarsan bulursun.kusursuz olmayı marifet edin kendine , işte asıl o zaman kusursuz olursun!...";
            List<Integer> ids = model.encodeNormalized(raw, algorithm);

            System.out.println("Token IDs: " + ids);
            System.out.println("Decoded text: " + model.decodeSmart(ids));
            assert (raw.hashCode() ==  model.decodeSmart(ids).hashCode());
            System.out.println("✔ Success");
        }

    }
}

